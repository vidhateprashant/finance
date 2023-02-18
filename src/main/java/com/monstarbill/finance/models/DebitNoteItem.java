package com.monstarbill.finance.models;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import com.monstarbill.finance.common.AppConstants;
import com.monstarbill.finance.common.CommonUtils;
import com.monstarbill.finance.enums.Operation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "finance", name = "debit_note_item")
@ToString
@Audited
@AuditTable("debit_note_item_aud")
public class DebitNoteItem implements Cloneable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="debit_note_id")
	private Long debitNoteId;
	
	@Column(name="debit_note_number")
	private String debitNoteNumber;
	
	@Column(name="item_id")
	private Long itemId;
	
	@Column(name="item_name")
	private String itemName;
	
	@Column(name="item_description")
	private String itemDescription;

	@Column(name="gl_code")
	private String glCode;
	
	@Column(precision=10, scale=2)
	private Double quantity;
	
	@Column(precision=10, scale=2)
	private Double rate;
	
	@Column(name="basic_amount", precision=10, scale=2)
	private Double basicAmount;
	
	@Column(name="tax_group_id")
	private Long taxGroupId;
	
	@Column(name="tax_amount", precision=10, scale=2)
	private Double taxAmount;
	
	@Column(name="total_amount", precision=10, scale=2)
	private Double totalAmount;
	
	@Column(name = "is_deleted", columnDefinition = "boolean default false")
	private boolean isDeleted;
	
	@CreationTimestamp
	@Column(name="created_date", updatable = false)
	private Date createdDate;

	@Column(name="created_by", updatable = false)
	private String createdBy;

	@UpdateTimestamp
	@Column(name="last_modified_date")
	private Timestamp lastModifiedDate;

	@Column(name="last_modified_by")
	private String lastModifiedBy;
	
	@Transient
	private String taxName;
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	
	/**
	 * Compare the fields and values of 2 objects in order to find out the
	 * difference between old and new value
	 * 
	 * @param debitNoteItem
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public List<DebitNoteHistory> compareFields(DebitNoteItem debitNoteItem)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<DebitNoteHistory> debitNoteHistories = new ArrayList<DebitNoteHistory>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(debitNoteItem);

				if (oldValue == null) {
					if (newValue != null) {
						debitNoteHistories.add(this.prepareDebitNoteHistory(debitNoteItem, field));
					}
				} else if (!oldValue.equals(newValue)) {
					debitNoteHistories.add(this.prepareDebitNoteHistory(debitNoteItem, field));
				}
			}
		}
		return debitNoteHistories;
	}
	
	private DebitNoteHistory prepareDebitNoteHistory(DebitNoteItem debitNoteItem, Field field) throws IllegalAccessException {
		DebitNoteHistory debitNoteHistory = new DebitNoteHistory();
		debitNoteHistory.setDebitNoteNumber(debitNoteItem.getDebitNoteNumber());
		debitNoteHistory.setChildId(debitNoteItem.getId());
		debitNoteHistory.setModuleName(AppConstants.DEBIT_NOTE_ITEM);
		debitNoteHistory.setChangeType(AppConstants.UI);
		debitNoteHistory.setLastModifiedBy(debitNoteItem.getLastModifiedBy());
		debitNoteHistory.setOperation(Operation.UPDATE.toString());
		debitNoteHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if (field.get(this) != null) debitNoteHistory.setOldValue(field.get(this).toString());
		if (field.get(debitNoteItem) != null) debitNoteHistory.setNewValue(field.get(debitNoteItem).toString());
		return debitNoteHistory;
	}

}
