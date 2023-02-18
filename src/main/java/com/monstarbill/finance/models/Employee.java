package com.monstarbill.finance.models;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
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
@Table(schema ="setup",name = "employee")
@ToString
@Audited
@AuditTable("employee_aud")

public class Employee implements Cloneable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="subsidiary_id")
	private Long subsidiaryId; 

	private String initials;
	
	@NotBlank(message = "Designation is mandatory")
	private String designation;
	
	@Column(name="employee_number", unique = true)
	private String employeeNumber;
	
	@Column(unique = true)
	private String email;
	
	private String department;
	
	private String salutation;
	
	@Column( unique = true)
	private String pan;
	
	private String currency;
	
	@Column(name="first_name")
	private String firstName;				
	
	@Column(name="middle_name")
	private String middleName;
	
	@Column(name="last_name")
	private String lastName;
	
	@Column(name="full_name", unique = true)
	private String fullName;
	
	private String supervisor;
	
	@Column(name = "signature_metadata")
	private String signatureMetadata;
	
	@Lob
	private byte[] signature;
	
	@Column(name = "image_metadata")
	private String imageMetadata;
	
	@Lob
	private byte[] image;
	
	@Column(name = "is_active", columnDefinition = "boolean default true")
	private boolean isActive;
	
	@Column(name="active_date")
	private Date activeDate;
	
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
}
