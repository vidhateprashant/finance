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
@Table(	schema = "setup",name = "approval_preference")
@ToString
@Audited
@AuditTable("approval_preference_aud")
public class ApprovalPreference implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "Subsidiary is mandatory")
	@Column(name = "subsidiary_id", nullable = false)
	private Long subsidiaryId;

	@Column(name = "approval_type")
	private String approvalType;

	@Column(name = "record_type")
	private String recordType;

	@Column(name = "sub_type")
	private String subType;
	
	@Column(name="is_deleted", columnDefinition = "boolean default false")
	private boolean isDeleted;
	
	@Column(name="is_active", columnDefinition = "boolean default true")
	private boolean isActive;
	
	@Column(name = "inactive_date")
	private Date inactiveDate;
	
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
	private Long approverPreferenceConditionId;

	@Transient
	private Long approverPreferenceSequenceId;

	@Transient
	private Long approverId;

	@Transient
	private Long roleId;
	
	@Transient
	private Long sequenceId;
	
	@Transient
	private String level;
	
	@Transient
	private String subsidiaryName;
	
	
	
}