package com.monstarbill.finance.models;

import java.sql.Timestamp;
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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

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
@Table(schema = "procure", name = "purchase_order")
@ToString
@Audited
@AuditTable("purchase_order_aud")
public class PurchaseOrder implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "external_id")
	private String externalId;

	@Column(name = "po_number", nullable = false, unique = true)
	private String poNumber;
	
	@NotNull(message = "Subsidiary is mandatory")
	@Column(name = "subsidiary_id", nullable = false)
	private Long subsidiaryId;
	
	@NotBlank(message = "PO type is mandatory")
	@Column(name = "po_type")
	private String poType;
	
	@Column(name = "location_id")
	private Long locationId;
	
	@Column(name = "location")
	private String location;

//	@Column(name = "pr_number")
//	private String prNumber;
	
	private String prId;

//	@Column(name = "qa_number")
//	private String qaNumber;
	
	private Long qaId;
	
	@NotNull(message = "Supplier is mandatory")
	@Column(name = "supplier_id")
	private Long supplierId;

	@Column(precision = 10, scale = 2)
	private Double amount;

	@Column(precision = 10, scale = 2)
	private Double taxAmount;

	@Column(precision = 10, scale = 2)
	private Double totalAmount;

	@Column(name = "original_supplier_id")
	private Long originalSupplierId;
	
	@Column(name="is_supplier_updatable", columnDefinition = "boolean default false")
	private boolean isSupplierUpdatable;

	@NotNull(message = "PO date is mandatory")
	@Column(name = "po_date")
	private Date poDate;

	@Column(name = "rejected_comments")
	private String rejectedComments;

	@Column(name = "payment_term")
	private String paymentTerm;

	@NotBlank(message = "Match type is mandatory")
	@Column(name = "match_type")
	private String matchType;

	@NotBlank(message = "Currency is mandatory")
	private String currency;

	@Column(name = "exchange_rate")
	private Double exchangeRate;

	@Column(name = "po_status")
	private String poStatus;

	private String memo;
	
	@Column(name = "bill_to")
	private Long billTo;
	
	@Column(name = "ship_to")
	private Long shipTo;
	
	@Column(name = "trn")
	private String trn;

	@Column(name = "netsuite_id")
	private String netsuiteId;
	
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
	
	@Transient
	private boolean isApprovalRoutingActive;
	
	@Column(name="is_deleted", columnDefinition = "boolean default false")
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
	private String subsidiaryName;
	
	@Transient
	private String supplierName;

	@Transient
	private String locationName;

	@Transient
	private boolean hasError;

	@Transient
	private Double totalValue;

	@Transient
	private List<PurchaseOrderItem> purchaseOrderItems;
	
	@Transient
	private int revision;
	
	@Transient
	private String approvedByName;
	
	@Transient
	private String qaNumber;
	
}
