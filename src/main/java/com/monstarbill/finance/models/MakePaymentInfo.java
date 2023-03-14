package com.monstarbill.finance.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
@Table(schema = "finance", name = "make_payment_info")
@ToString
@Audited
@AuditTable("make_payment_info_aud")
public class MakePaymentInfo {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "payment_date")
	private Date paymentDate;
	
	@Column(name = "payment_number")
	private String paymentNumber;
	
	@Column(name="make_payment_id")
	private Long makePaymentId;
	
	@Column(name="advance_payment_id")
	private Long advancePaymentId;
	
	private String type;
	
	@Column(name = "payment_status")
	private String paymentStatus;
	
	@Column(name = "exchange_rate")
	private Double exchangeRate;
	
	@Column(name = "amount")
	private Double amount;
	
	@Column(name="payment_doc_number")
	private String paymentDocNumber;
	
	@Column(name="void_date")
	private Date voidDate;
	

}
