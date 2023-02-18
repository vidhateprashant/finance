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
@Table(schema = "finance", name = "make_payment_list")
@ToString
@Audited
@AuditTable("make_payment_List_aud")
public class MakePaymentList implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "payment_number")
	private String paymentNumber;

	@Column(name = "payment_id")
	private Long paymentId;
	
	private Long invoiceId;
	
	private String billNo;
	
	private String type;

	@Column(precision=10, scale=2)
	private Double invoiceAmount;
	
	@Column(precision=10, scale=2)
	private Double paidAmount;
	
	@Column(precision=10, scale=2)
	private Double amountDue;
	
	private Date invoiceDate;
	
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
	private Double paymentAmount;
	
	@Transient
	private String currency;

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	
	/**
	 * Compare the fields and values of 2 objects in order to find out the
	 * difference between old and new value
	 * 
	 * @param make payment
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public List<MakePaymentHistory> compareFields(MakePaymentList makePaymentList)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<MakePaymentHistory> makePaymentHistories = new ArrayList<MakePaymentHistory>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(makePaymentList);

				if (oldValue == null) {
					if (newValue != null) {
						makePaymentHistories.add(this.prepareMakePaymentHistory(makePaymentList, field));
					}
				} else if (!oldValue.equals(newValue)) {
					makePaymentHistories.add(this.prepareMakePaymentHistory(makePaymentList, field));
				}
			}
		}
		return makePaymentHistories;
	}

	private MakePaymentHistory prepareMakePaymentHistory(MakePaymentList makePaymentList, Field field) throws IllegalAccessException {
		MakePaymentHistory makePaymentHistory = new MakePaymentHistory();
		makePaymentHistory.setPaymentId(makePaymentList.getPaymentId());
		makePaymentHistory.setChildId(makePaymentList.getId());
		makePaymentHistory.setModuleName(AppConstants.MAKE_PAYMENT_LIST);
		makePaymentHistory.setChangeType(AppConstants.UI);
		makePaymentHistory.setOperation(Operation.UPDATE.toString());
		makePaymentHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if (field.get(this) != null) makePaymentHistory.setOldValue(field.get(this).toString());
		if (field.get(makePaymentList) != null) makePaymentHistory.setNewValue(field.get(makePaymentList).toString());
		makePaymentHistory.setLastModifiedBy(makePaymentList.getLastModifiedBy());
		return makePaymentHistory;
	}
}
