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
import javax.validation.constraints.NotBlank;

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
@Table(schema = "finance", name = "advance_payment")
@ToString
@Audited
@AuditTable("advance_payment_aud")
public class AdvancePayment implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "payment_number")
	private String prePaymentNumber;

	@Column(name = "subsidiary_id")
	private Long subsidiaryId;

	@Column(name = "supplier_id")
	private Long supplierId;

	@Column(name = "location_id")
	private Long locationId;
	
	@Column(name = "pre_payment_date")
	private Date prePaymentDate;

	@Column(name = "currency")
	private String currency;

	@Column(name = "subsidiary_currency")
	private String subsidiaryCurrency;

	@NotBlank(message = "proforma invoice is mandetory")
	@Column(name = "proforma_invoice" , unique = true)
	private String proformaInvoice;

	@Column(name = "exchange_rate")
	private Double exchangeRate;

	@Column(name = "type")
	private String type;

	@Column(name = "advance_amount", precision=10, scale=2)
	private Double advanceAmount;

	@Column(name = "payment_amount", precision=10, scale=2)
	private Double paymentAmount;
	
	@Column(name = "due_amount", precision=10, scale=2)
	private Double dueAmount;
	
	@Column(name = "unapplied_amount", precision=10, scale=2)
	private Double unappliedAmount;

	@Column(name = "payment_mode")
	private String paymentMode;

	@Column(name = "memo")
	private String memo;

	@Column(name = "status")
	private String status;

	@Column(name = "netsuite_id")
	private String netsuiteId;

	@Column(name = "rejected_comments")
	private String rejectedComments;
	
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
	
	@Column(name = "note_to_approver")
	private String noteToApprover;
	
	@Column(name = "ns_message")
	private String nsMessage;

	@Column(name = "ns_status")
	private String nsStatus;

	@Column(name = "integrated_id")
	private String integratedId;
	
	private String department;
	
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

//	@Transient
//	private Double amountDue;

	@Transient
	private Double partPaymentAmount;
	
	@Transient
	private List<AdvancePaymentApply> advancePaymentApply;

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

//	public AdvancePayment(Long id, String prePaymentNumber, Date prePaymentDate, String subsidiaryName,
//			String supplierName) {
//		this.id = id;
//		this.prePaymentNumber = prePaymentNumber;
//		this.prePaymentDate = prePaymentDate;
//		this.subsidiaryName = subsidiaryName;
//		this.supplierName = supplierName;
//	}

	public AdvancePayment(Long id, Double advanceAmount, Double paymentAmount, String prePaymentNumber, Long subsidiaryId, Long supplierId, Date prePaymentDate,
			String rejectedComments, String memo, String status, Double unappliedAmount, String subsidiaryName, String supplierName, String currency) {
		this.id = id;
		this.advanceAmount =  advanceAmount;
		this.paymentAmount = paymentAmount;
		this.prePaymentNumber = prePaymentNumber;
		this.subsidiaryId = subsidiaryId;
		this.supplierId = supplierId;
		this.prePaymentDate = prePaymentDate;
		this.rejectedComments = rejectedComments;
		this.memo = memo;
		this.status = status;
		this.unappliedAmount = unappliedAmount;
		this.subsidiaryName = subsidiaryName;
		this.supplierName = supplierName;
		this.currency = currency;
		
	}
	
	public AdvancePayment(Long id, String prePaymentNumber, Long subsidiaryId, Long supplierId, Date prePaymentDate,
			String currency, String subsidiaryCurrency, String proformaInvoice, Double exchangeRate, String type,
			Double advanceAmount, Double paymentAmount, String paymentMode, String approvedBy, String nextApprover,
		String nextApproverLevel, String memo, String status, String netsuiteId, String rejectedComments,
			boolean isDeleted, Date createdDate, String createdBy, String lastModifiedBy) {
		this.id = id;
	this.prePaymentNumber = prePaymentNumber;
		this.subsidiaryId = subsidiaryId;
		this.supplierId = supplierId;
		this.prePaymentDate = prePaymentDate;
		this.currency = currency;
		this.subsidiaryCurrency = subsidiaryCurrency;
		this.proformaInvoice = proformaInvoice;
		this.exchangeRate = exchangeRate;
		this.type = type;
		this.advanceAmount = advanceAmount;
		this.paymentAmount = paymentAmount;
		this.paymentMode = paymentMode;
		this.approvedBy = approvedBy;
		this.nextApprover = nextApprover;
		this.nextApproverLevel = nextApproverLevel;
		this.memo = memo;
		this.status = status;
		this.netsuiteId = netsuiteId;
		this.rejectedComments = rejectedComments;
		this.isDeleted = isDeleted;
		this.createdDate = createdDate;
		this.createdBy = createdBy;
		this.lastModifiedBy = lastModifiedBy;
	}

	/**
	 * Compare the fields and values of 2 objects in order to find out the
	 * difference between old and new value
	 * 
	 * @param advance payment
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public List<AdvancePaymentHistory> compareFields(AdvancePayment advancePayment)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<AdvancePaymentHistory> advancePaymentHistories = new ArrayList<AdvancePaymentHistory>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(advancePayment);

				if (oldValue == null) {
					if (newValue != null) {
						advancePaymentHistories.add(this.prepareAdvancePaymentHistory(advancePayment, field));
					}
				} else if (!oldValue.equals(newValue)) {
					advancePaymentHistories.add(this.prepareAdvancePaymentHistory(advancePayment, field));
				}
			}
		}
		return advancePaymentHistories;
	}

	private AdvancePaymentHistory prepareAdvancePaymentHistory(AdvancePayment advancePayment, Field field)
			throws IllegalAccessException {
		AdvancePaymentHistory advancePaymentHistory = new AdvancePaymentHistory();
		advancePaymentHistory.setAdvancePaymentNumber(advancePayment.getPrePaymentNumber());
		advancePaymentHistory.setModuleName(AppConstants.ADVANCE_PAYMENT);
		advancePaymentHistory.setChangeType(AppConstants.UI);
		advancePaymentHistory.setOperation(Operation.UPDATE.toString());
		advancePaymentHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if (field.get(this) != null) {
			advancePaymentHistory.setOldValue(field.get(this).toString());
		}
		if (field.get(advancePayment) != null) {
			advancePaymentHistory.setNewValue(field.get(advancePayment).toString());
		}
		advancePaymentHistory.setLastModifiedBy(advancePayment.getLastModifiedBy());
		return advancePaymentHistory;
	}

	

}
