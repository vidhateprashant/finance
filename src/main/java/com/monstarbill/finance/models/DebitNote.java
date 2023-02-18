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
@Table(schema = "finance", name = "debit_note")
@ToString
@Audited
@AuditTable("debit_note_aud")
public class DebitNote implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "debit_note_number" , unique = true)
	private String debitNoteNumber;

	@Column(name = "subsidiary_id")
	private Long subsidiaryId;

	@Column(name = "supplier_id")
	private Long supplierId;

	@Column(name = "debit_note_date")
	private Date debitNoteDate;

	@Column(name = "rtv_number")
	private String rtvNumber;

	@Column(name = "grn_number")
	private String grnNumber;

	@Column(name = "invoice_number")
	private String invoiceNumber;

	private String currency;

	@Column(name = "exchange_rate", precision = 10, scale = 2)
	private Double exchangeRate;

	private String memo;

	@Column(name = "approval_status")
	private String approvalStatus;

	@Column(name = "is_deleted", columnDefinition = "boolean default false")
	private boolean isDeleted;

	@CreationTimestamp
	@Column(name = "created_date", updatable = false)
	private Date createdDate;

	@Column(name = "created_by", updatable = false)
	private String createdBy;

	@UpdateTimestamp
	@Column(name = "last_modified_date")
	private Timestamp lastModifiedDate;

	@Column(name = "last_modified_by")
	private String lastModifiedBy;
	
	@Column(name = "ns_message")
	private String nsMessage;

	@Column(name = "ns_status")
	private String nsStatus;

	@Column(name = "integrated_id")
	private String integratedId;

	@Transient
	private List<DebitNoteItem> debitNoteItem;

	@Transient
	private String subsidiaryName;

	@Transient
	private String supplierName;

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * Compare the fields and values of 2 objects in order to find out the
	 * difference between old and new value
	 * 
	 * @param supplier
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public List<DebitNoteHistory> compareFields(DebitNote debitNote)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<DebitNoteHistory> debitNoteHistories = new ArrayList<DebitNoteHistory>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(debitNote);

				if (oldValue == null) {
					if (newValue != null) {
						debitNoteHistories.add(this.prepareDebitNoteHistory(debitNote, field));
					}
				} else if (!oldValue.equals(newValue)) {
					debitNoteHistories.add(this.prepareDebitNoteHistory(debitNote, field));
				}
			}
		}
		return debitNoteHistories;
	}

	private DebitNoteHistory prepareDebitNoteHistory(DebitNote debitNote, Field field) throws IllegalAccessException {
		DebitNoteHistory debitNoteHistory = new DebitNoteHistory();
		debitNoteHistory.setDebitNoteNumber(debitNote.getDebitNoteNumber());
		debitNoteHistory.setModuleName(AppConstants.DEBIT_NOTE);
		debitNoteHistory.setChangeType(AppConstants.UI);
		debitNoteHistory.setLastModifiedBy(debitNote.getLastModifiedBy());
		debitNoteHistory.setOperation(Operation.UPDATE.toString());
		debitNoteHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if (field.get(this) != null)
			debitNoteHistory.setOldValue(field.get(this).toString());
		if (field.get(debitNote) != null)
			debitNoteHistory.setNewValue(field.get(debitNote).toString());
		return debitNoteHistory;
	}
	
	/**
	 * used for get all debit notes
	 * @param id
	 * @param subsidiaryName
	 * @param debitNoteDate
	 * @param debitNoteNumber
	 * @param supplierName
	 */
	public DebitNote(Long id, String subsidiaryName, Date debitNoteDate, String debitNoteNumber, String supplierName) {
		this.id = id;
		this.subsidiaryName = subsidiaryName;
		this.debitNoteDate = debitNoteDate;
		this.debitNoteNumber = debitNoteNumber;
		this.supplierName = supplierName;
	}
	
}
