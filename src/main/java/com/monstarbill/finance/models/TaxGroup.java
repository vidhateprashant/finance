package com.monstarbill.finance.models;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
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
@Table(schema ="setup",name = "tax_group")
@ToString
@Audited
@AuditTable("tax_group_aud")
public class TaxGroup implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "Subsidiary Id is mandatory")
	@Column(name = "subsidiary_id", updatable = false)
	private Long subsidiaryId;

	@Column(name = "country", updatable = false)
	private String country;

	@NotBlank(message = "Tax Group name is mandatory")
	@Column(name = "name", updatable = false, unique = true)
	private String name;

	private String description;

	@NotBlank(message = "Available on is mandatory")
	@Column(name = "available_on")
	private String availableOn;

	@Column(name = "is_inclusive", columnDefinition = "boolean default false")
	private boolean isInclusive;

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
