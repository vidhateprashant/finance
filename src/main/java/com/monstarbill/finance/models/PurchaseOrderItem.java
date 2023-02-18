package com.monstarbill.finance.models;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
@Table(schema = "procure", name = "purchase_order_item")
@ToString
@Audited
@AuditTable("purchase_order_item_aud")
public class PurchaseOrderItem implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "po_number")
	private String poNumber;
	
	@Column(name = "po_id")
	private Long poId;
	
	@Column(name = "item_id")
	private Long itemId;
	
	@Column(name = "item_description")
	private String itemDescription;
	
	@Column(precision=10, scale=2)
	private Double quantity;

	@Column(precision=10, scale=2)
	private Double rate;

	@Column(precision=10, scale=2)
	private Double amount;
	
	@Column(precision=10, scale=2)
	private Double remainQuantity;

	@Column(name = "tax_group_id")
	private Long taxGroupId;

	@Column(name = "tax_amount", precision=10, scale=2)
	private Double taxAmount;
	
	@Column(name = "total_tax_amount", precision=10, scale=2)
	private Double totalTaxAmount;
	
	@Column(name = "total_amount", precision=10, scale=2)
	private Double totalAmount;
	
	@Column(name = "unbilled_quantity", precision=10, scale=2)
	private Double unbilledQuantity;


	@Column(name = "received_by_date")
	private Date receivedByDate;

//	@Column(name = "pr_number")
//	private String prNumber;
	
	private Long prId;

	@Column(name = "ship_to_location_id")
	private Long shipToLocationId;
	
	@Column(name = "ship_to_location")
	private String shipToLocation;

	private String department;

	private String memo;
	
	private String status;
	
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
