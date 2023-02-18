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
@Table(schema = "finance", name = "make_payment")
@ToString
@Audited
@AuditTable("make_payment_aud")
public class MakePayment implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "payment_number")
	private String paymentNumber;

	@Column(name = "subsidiary_id")
	private Long subsidiaryId;

	@Column(name = "account_id")
	private Long accountId;
	
	@Column(name = "bank_id")
	private Long bankId;

	@Column(name = "supplier_id")
	private Long supplierId;

	@Column(name = "payment_date")
	private Date paymentDate;

	@Column(name = "currency")
	private String currency;

	@Column(name = "subsidiary_currency")
	private String subsidiaryCurrency;

	@Column(name = "exchange_rate")
	private Double exchangeRate;

	@Column(name = "payment_mode")
	private String paymentMode;

	@Column(name = "amount")
	private Double amount;

	@Column(name = "bank_transaction_type")
	private String bankTransactionType;

	@Column(name = "bank_reference_number")
	private String bankReferenceNumber;

	@Column(name = "memo")
	private String memo;

	@Column(name = "netsuite_id")
	private String netsuiteId;

	@Column(name = "rejected_comments")
	private String rejectedComments;
	
	@Column(name = "note_to_approver")
	private String noteToApprover;

	@Column(name = "void_description")
	private String voidDescription;
	
	@Column(name = "void_date")
	private Date voidDate;
	
	private String type;
	
	private String status;
	
	@Column(name = "ns_message")
	private String nsMessage;

	@Column(name = "ns_status")
	private String nsStatus;

	@Column(name = "integrated_id")
	private String integratedId;

	
	//--------approval process----------//
	
	@Column(name = "payment_status")
	private String paymentStatus;

	@Column(name = "approved_by")
	private String approvedBy;
	
	@Column(name = "next_approver")
	private String nextApprover;
	
	@Column(name = "next_approver_role")
	private String nextApproverRole;

	// stores the next approver level i.e. L1,L2,L3 etc.
	@Column(name = "next_approver_level")
	private String nextApproverLevel;
	
	// store's the id of approver preference
	@Column(name = "approver_preference_id")
	private Long approverPreferenceId;
	
	// stores the approver sequence id (useful internally in order to change the approver)
	@Column(name = "approver_sequence_id")
	private Long approverSequenceId;
	
	// stores the max level to approve, after that change status to approve
	@Column(name = "approver_max_level")
	private String approverMaxLevel;
	
	@Transient
	private boolean isApprovalRoutingActive;

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
	private String subsidiaryName;

	@Transient
	private String supplierName;

	@Transient
	private String bankAccountName;
	
	@Transient
	private String bankName;

	@Transient
	private Double paymentAmount;
	
	@Transient
	private String approvedByName;

	@Transient
	private List<MakePaymentList> makePaymentList;
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public MakePayment(Long id, String paymentNumber, Date paymentDate, String currency, Double amount,
			String paymentMode,
			String approvedBy, String nextApprover, String nextApproverRole, String paymentStatus,
			String rejectedComments, Long subsidiaryId, Long supplierId, String bankReferenceNumber, Long bankId,
			String subsidiaryName, String supplierName, String bankName, String approvedByName) {
		this.id = id;
		this.paymentNumber = paymentNumber;
		this.paymentDate = paymentDate;
		this.currency = currency;
		this.amount = amount;
		this.paymentMode = paymentMode;
		this.approvedBy = approvedBy;
		this.nextApprover = nextApprover;
		this.nextApproverRole = nextApproverRole;
		this.paymentStatus = paymentStatus;
		this.rejectedComments = rejectedComments;
		this.subsidiaryId = subsidiaryId;
		this.supplierId = supplierId;
		this.bankReferenceNumber = bankReferenceNumber;
		this.bankId = bankId;
		this.subsidiaryName = subsidiaryName;
		this.supplierName = supplierName;
		this.bankName = bankName;
		this.approvedByName = approvedByName;
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
	public List<MakePaymentHistory> compareFields(MakePayment makePayment)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<MakePaymentHistory> makePaymentHistories = new ArrayList<MakePaymentHistory>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(makePayment);

				if (oldValue == null) {
					if (newValue != null) {
						makePaymentHistories.add(this.prepareMakePaymentHistory(makePayment, field));
					}
				} else if (!oldValue.equals(newValue)) {
					makePaymentHistories.add(this.prepareMakePaymentHistory(makePayment, field));
				}
			}
		}
		return makePaymentHistories;
	}

	private MakePaymentHistory prepareMakePaymentHistory(MakePayment makePayment, Field field)
			throws IllegalAccessException {
		MakePaymentHistory makePaymentHistory = new MakePaymentHistory();
		makePaymentHistory.setPaymentId(makePayment.getId());
		makePaymentHistory.setModuleName(AppConstants.MAKE_PAYMENT);
		makePaymentHistory.setChangeType(AppConstants.UI);
		makePaymentHistory.setOperation(Operation.UPDATE.toString());
		makePaymentHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if (field.get(this) != null)
			makePaymentHistory.setOldValue(field.get(this).toString());
		if (field.get(makePayment) != null)
			makePaymentHistory.setNewValue(field.get(makePayment).toString());
		makePaymentHistory.setLastModifiedBy(makePayment.getLastModifiedBy());
		return makePaymentHistory;
	}



	public MakePayment(Long id, String paymentNumber, Long subsidiaryId, Long bankId, Long supplierId,
			String currency, String paymentMode, Double amount, Date paymentDate, String status, String subsidiaryName, String supplierName,
			String bankName) {
		this.id = id;
		this.paymentNumber = paymentNumber;
		this.subsidiaryId = subsidiaryId;
		this.bankId = bankId;
		this.supplierId = supplierId;
		this.currency = currency;
		this.paymentMode = paymentMode;
		this.amount = amount;
		this.paymentDate = paymentDate;
		this.status = status;
		this.subsidiaryName = subsidiaryName;
		this.supplierName = supplierName;
		this.bankName = bankName;
	}

}
