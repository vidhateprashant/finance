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
@Table(schema = "procure", name = "grn_item")
@ToString
@Audited
@AuditTable("grn_item_aud")
public class GrnItem implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "grn_id")
	private Long grnId;
	
	@Column(name = "po_id")
	private Long poId;
	
	private Long poiId;
	
	@Column(name = "item_id")
	private Long itemId;
	
	@Column(name = "invoice_id")
	private Long invoiceId;
	
	@Column(name = "po_number")
	private String poNumber;
	
	@Column(name = "grn_number")
	private String grnNumber;
	
	private String itemName;
	
	private String status;
	
	private String itemDescription;
	
	private String itemUom;
	
	@Column(name = "tax_group_id")
	private Long taxGroupId;
	
	@Column(precision=10, scale=2)
	private Double quantity;
	
	@Column(precision=10, scale=2)
	private Double reciveQuantity;
	
	@Column(precision=10, scale=2)
	private Double remainQuantity;
	
	@Column(precision=10, scale=2)
	private Double unbilledQuantity;
	
	@Column(name = "lot_number")
	private String lotNumber;
	
	@Column(name = "rate", precision=10, scale=2)
	private Double rate;
	
	@Column(name = "rtv_quantity", precision=10, scale=2)
	private Double rtvQuantity;
	
	@Column(name = "invoice_number")
	private String invoiceNumber;
	
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
