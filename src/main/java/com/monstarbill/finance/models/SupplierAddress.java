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
@Table(schema ="setup",name = "supplier_address")
@ToString
@Audited
@AuditTable("supplier_address_aud")
public class SupplierAddress implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "Supplier ID is mandatory")
	@Column(name = "supplier_id")
	private Long supplierId;
	
	@Column(name = "address_code")
	private String addressCode;

	@NotBlank(message = "Address Line 1 is mandatory")
	private String address1;

	private String address2;
	
	private String city;

	@NotBlank(message = "State is mandatory")
	private String state;

	@NotBlank(message = "Country is mandatory")
	private String country;

	private String pin;

	// @NotBlank(message = "Tax registration number is mandatory")
	@Column(name = "tax_registration_number")
	private String taxRegistrationNumber;
	
	@Column(name = "registration_type")
	private String registrationType;

	@Column(name = "default_billing", columnDefinition = "boolean default false")
	private boolean defaultBilling;

	@Column(name = "default_shipping", columnDefinition = "boolean default false")
	private boolean defaultShipping;

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
