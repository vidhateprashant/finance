package com.monstarbill.finance.models;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import com.monstarbill.finance.common.AppConstants;
import com.monstarbill.finance.common.CommonUtils;
import com.monstarbill.finance.enums.Operation;

import lombok.Data;

@Data
@Entity
@Audited
@Table(schema = "finance", uniqueConstraints = {@UniqueConstraint(columnNames = {"subsidiaryId", "supplierId", "invoiceNo"})})
public class Invoice implements Cloneable {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;
	
	private Long supplierId, subsidiaryId, poId, locationId, billToId, shipToId;
	private String invoiceNo, invStatus, paymentTerm, integratedId, currency, billTo, shipTo, invoiceCode, invoiceSupplyNumber, taxRegNumber;
	private Date invoiceDate, dueDate;
	private double fxRate, amount, taxAmount, totalAmount, paymentAmount, amountDue;

	@Column(name = "external_id")
	private String externalId;
	
	@Transient
	private boolean hasError;
	
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

	@Transient
	private boolean isApprovalRoutingActive;
	
	@Transient
	private String subsidiaryName;
	
	@Transient
	private String supplierName;
	
	@Transient
	private String poNumber;

	@Transient
	private List<InvoiceItem> invoiceItems;

	@Transient
	private double totalPaidAmount;
	
	@Transient
	private List<InvoicePayment> invoicePayments;
	
	private String createdBy, lastModifiedBy;
	
	@CreationTimestamp
    private Date createdDate;

    @UpdateTimestamp
    private Date lastModifiedDate;

    public Invoice(Long invoiceId, Long supplierId, String invoiceNo, double totalAmount, double amountDue) {
		this.invoiceId = invoiceId;
		this.supplierId = supplierId;
		this.invoiceNo = invoiceNo;
		this.totalAmount = totalAmount;
		this.amountDue = amountDue;
	}

	public Invoice() {
		
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	/**
	 * Compare the fields and values of 2 objects in order to find out the
	 * difference between old and new value
	 * 
	 * @param invoice
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public List<InvoiceHistory> compareFields(Invoice invoice)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<InvoiceHistory> invoiceHistories = new ArrayList<>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(invoice);

				if (oldValue == null) {
					if (newValue != null) {
						invoiceHistories.add(this.prepareInvoiceHistory(invoice, field));
					}
				} else if (!oldValue.equals(newValue)) {
					invoiceHistories.add(this.prepareInvoiceHistory(invoice, field));
				}
			}
		}
		return invoiceHistories;
	}
	
	private InvoiceHistory prepareInvoiceHistory(Invoice invoice, Field field) throws IllegalAccessException {
		InvoiceHistory invoiceHistory = new InvoiceHistory();
		invoiceHistory.setInvoiceId(invoice.getInvoiceId());
		invoiceHistory.setModuleName(AppConstants.INVOICE);
		invoiceHistory.setChangeType(AppConstants.UI);
		invoiceHistory.setOperation(Operation.UPDATE.toString());
		invoiceHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if (field.get(this) != null) invoiceHistory.setOldValue(field.get(this).toString());
		if (field.get(invoice) != null) invoiceHistory.setNewValue(field.get(invoice).toString());
		invoiceHistory.setLastModifiedBy(invoice.getLastModifiedBy());
		return invoiceHistory;
	}
	
/*	private String location, payTerm;
	private Date dueDate;*/


}
