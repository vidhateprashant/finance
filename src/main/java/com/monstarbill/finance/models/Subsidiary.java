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
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@Table(	schema = "setup",name = "subsidiary")
@ToString
public class Subsidiary implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Name is mandatory")
	@Column(nullable = false, updatable = false, unique = true)
	private String name;

	@Column(name="legal_name")
	private String legalName;

	@Column(name="parent_company", updatable = false)
	private String parentCompany;

	@NotBlank(message = "Country is mandatory")
	@Column(updatable = false)
	private String country;

	@Email(message = "Email is not valid")
	private String email;

	private String website;

	@Column(updatable = false)
	private String language;

	@NotBlank(message = "Currency is mandatory")
	@Column(updatable = false)
	private String currency;

	@NotBlank(message = "Fiscal Calender is mandatory")
	@Column(name="fiscal_calender")
	private String fiscalCalender;

	//@Column(unique = true)
	private String pan;
	
	//@Column(unique = true)
	private String cin;
	
	//@Column(unique = true)
	private String tan;
	
	@Column(name = "bid_mail")
	private String bidMail;

	@Column(name = "invoice_mail")
	private String invoiceMail;
	
	private String invoicePasswd;

	@Column(name = "admin_mail")
	private String adminMail;

	@Column(name="is_active", columnDefinition = "boolean default true")
	private boolean isActive;
	
	@Column(name="active_date")
	private Date activeDate;
	
	@Column(name="is_deleted", columnDefinition = "boolean default false")
	private boolean isDeleted;

	private String integrated_id;
	
	@Column(name = "logo_metadata")
	private String logoMetadata;
	
	@Lob
	private byte[] logo;

	@CreationTimestamp
	@Column(name="created_date", updatable = false)
	private Date createdDate;

	@Column(name="created_by")
	private String createdBy;

	@UpdateTimestamp
	@Column(name="last_modified_date")
	private Timestamp lastModifiedDate;

	@Column(name="last_modified_by")
	private String lastModifiedBy;
	
	
}
