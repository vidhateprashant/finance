package com.monstarbill.finance.models;

import java.sql.Timestamp;
import java.util.Date;

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
@Table(schema = "setup",name = "supplier")
@ToString
@Audited
@AuditTable("supplier_aud")
public class Supplier implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "external_id")
	private String externalId;

	@NotBlank(message = "Vendor Name is mandatory")
	@Column(nullable = false, unique = true)
	private String name;

	@Column(name = "legal_name")
	private String legalName;
	
	@Column(name = "payment_term")
	private String paymentTerm;

	@Column(name = "vendor_number")
	private String vendorNumber;

	@NotBlank(message = "Vendor Type is mandatory")
	@Column(name = "vendor_type")
	private String vendorType;

//	@NotBlank(message = "Unique Identification Number is mandatory")
//	@Column(nullable = false)
	private String uin;

	@Column(name = "approval_status")
	private String approvalStatus;
	
	@Column(name = "reject_comments")
	private String rejectComments;

	@Column(name = "nature_of_supply")
	private String natureOfSupply;

	@Column(name = "unique_number")
	private String uniqueNumber;

	@Column(name = "invoice_mail")
	private String invoiceMail;

	@Column(name = "tds_witholding")
	private String tdsWitholding;
	
	// -----------------------------APPROVAL START--------------------------------------------------
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
	
	@Transient
	private boolean isApprovalRoutingActive;
	// ------------------------------------------------------------------

	@Column(name = "is_active", columnDefinition = "boolean default true")
	private boolean isActive;
	
	@Column(name="active_date")
	private Date activeDate;

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

	
}
