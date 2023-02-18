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
@Table(schema ="setup",	name = "item")
@ToString
@Audited
@AuditTable("item_aud")
public class Item implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "external_id")
	private String externalId;
	
	@NotNull(message = "Subsidiary is mandatory")
	@Column(name = "subsidiary_id", nullable = false, updatable = false)
	private Long subsidiaryId;
	
	@NotBlank(message = "Item category is mandatory")
	@Column(updatable = false)
	private String category;

	@NotBlank(message = "Item Name is mandatory")
	private String name;

	private String description;

	@NotBlank(message = "UOM is mandatory")
	private String uom;

	private String costingMethod;

	@Column(name="is_purchasable", columnDefinition = "boolean default false")
	private boolean isPurchasable;
	
	@Column(name="is_salable", columnDefinition = "boolean default false")
	private boolean isSalable;

	@Column(name = "hsn_sac_code")
	private String hsnSacCode;
	
	private String integratedId;

	@Column(name = "nature_of_item")
	private String natureOfItem;
	
	// ------- ACCOUNTING -------------------------
	@Column(name = "expense_account_id")
	private Long expenseAccountId;
	
	@Column(name = "cogs_account")
	private String cogsAccount;
	
	@Column(name = "income_account")
	private String incomeAccount;
	
	@Column(name = "asset_account_id")
	private Long assetAccountId;
	// ------- ACCOUNTING -------------------------
	
	@Column(name="is_active", columnDefinition = "boolean default true")
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
	
	@Transient
	private String status;
	
	@Transient
	private String subsidiaryName;
	
	@Transient
	private Long accountId;
	
	@Transient
	private String assetAccountName;
	
	@Transient
	private String expenseAccountName;

}
