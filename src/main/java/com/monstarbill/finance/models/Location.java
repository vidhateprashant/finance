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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.AuditTable;

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
@Table( schema = "setup",name = "location")
@ToString
@AuditTable("location_aud")

public class Location implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Location Name is mandatory")
	@Column(name = "location_name", nullable = false, unique = true)
	private String locationName;

	@Column(name = "parent_location_id", updatable = false)
	private Long parentLocationId;

	@NotNull(message = "Subsidiary Id is mandatory")
	@Column(name = "subsidiary_id")
	private Long subsidiaryId;

	@NotBlank(message = "Location Type is mandetory")
	@Column(name = "location_type", nullable = false)
	private String locationType;

	@Column(name = "is_deleted", columnDefinition = "boolean default false")
	private boolean isDeleted;

	@NotNull(message = "Effective from is mandatory")
	@Column(name = "effective_from", nullable = false)
	private Date effectiveFrom;

	@Column(name = "effective_to")
	private Date effectiveTo;

	@Column(name = "is_parent_location", columnDefinition = "boolean default false")
	private boolean isParentLocation;
	
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
	private String parentLocationName;

	
}
