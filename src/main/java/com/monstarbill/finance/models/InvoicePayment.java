package com.monstarbill.finance.models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import javax.persistence.Column;
import lombok.Data;

@Data
@Entity
@Audited
@Table(schema = "finance")
public class InvoicePayment {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoicePaymentId;

	private Long  invoiceId, paymentId;
	private double amount;
	private String billNo, type, bankName;
	private Date billDate;
	

	@Column(name = "is_deleted", columnDefinition = "boolean default false")
	private boolean isDeleted;
	
	private String createdBy, modifiedBy;
	
	@CreationTimestamp
    private Date dateCreated;

    @UpdateTimestamp
    private Date lastUpdated;
	

}
