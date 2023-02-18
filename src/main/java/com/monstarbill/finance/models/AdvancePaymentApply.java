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
@Table(schema = "finance", name = "advance_payment_apply")
@ToString
@Audited
@AuditTable("advance_payment_apply_aud")
public class AdvancePaymentApply implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "pre_payment_id")
	private Long prePaymentId;
	
	@Column(name = "invoice_id")
	private Long invoiceId;
	
	@Column(name = "pre_payment_number")
	private String prePaymentNumber;

	@Column(name = "apply_date")
	private Date applyDate;
	
	private String curency;
	
	@Column(name = "invoice_number")
	private String invoiceNumber;
	
	@Column(name = "invoice_amount", precision=10, scale=2)
	private Double invoiceAmount;
		
	@Column(name = "apply_amount", precision=10, scale=2)
	private Double applyAmount;
	
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

	@Transient
	private Double amountDue;


	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * Compare the fields and values of 2 objects in order to find out the
	 * difference between old and new value
	 * 
	 * @param advancePaymentApply
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public List<AdvancePaymentHistory> compareFields(AdvancePaymentApply advancePaymentApply)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<AdvancePaymentHistory> advancePaymentHistories = new ArrayList<AdvancePaymentHistory>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(advancePaymentApply);

				if (oldValue == null) {
					if (newValue != null) {
						advancePaymentHistories.add(this.prepareAdvancePaymentHistory(advancePaymentApply, field));
					}
				} else if (!oldValue.equals(newValue)) {
					advancePaymentHistories.add(this.prepareAdvancePaymentHistory(advancePaymentApply, field));
				}
			}
		}
		return advancePaymentHistories;
	}

	private AdvancePaymentHistory prepareAdvancePaymentHistory(AdvancePaymentApply advancePaymentApply, Field field) throws IllegalAccessException {
		AdvancePaymentHistory advancePaymentHistory = new AdvancePaymentHistory();
		advancePaymentHistory.setAdvancePaymentNumber(advancePaymentApply.getPrePaymentNumber());
		advancePaymentHistory.setChildId(advancePaymentApply.getId());
		advancePaymentHistory.setModuleName(AppConstants.ADVANCE_PAYMENT_APPLY);
		advancePaymentHistory.setChangeType(AppConstants.UI);
		advancePaymentHistory.setOperation(Operation.UPDATE.toString());
		advancePaymentHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if (field.get(this) != null) advancePaymentHistory.setOldValue(field.get(this).toString());
		if (field.get(advancePaymentApply) != null) advancePaymentHistory.setNewValue(field.get(advancePaymentApply).toString());
		advancePaymentHistory.setLastModifiedBy(advancePaymentApply.getLastModifiedBy());
		return advancePaymentHistory;
	}

}
