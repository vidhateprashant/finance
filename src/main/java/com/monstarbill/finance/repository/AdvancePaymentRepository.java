package com.monstarbill.finance.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.monstarbill.finance.models.AdvancePayment;


public interface AdvancePaymentRepository extends JpaRepository<AdvancePayment, String> {

	public Optional<AdvancePayment> findByIdAndIsDeleted(Long id, boolean isDeleted);

	@Query(" select new com.monstarbill.finance.models.AdvancePayment(a.id, a.advanceAmount, a.paymentAmount, a.prePaymentNumber, a.subsidiaryId, a.supplierId, a.prePaymentDate, a.rejectedComments, a.memo, a.status, a.unappliedAmount, "
			+ " s.name as subsidiaryName, su.name as supplierName, a.currency) from AdvancePayment a "
			+ " inner join Subsidiary s ON s.id = a.subsidiaryId inner join"
			+ " Supplier su ON su.id = a.supplierId where a.status = :status")
	public List<AdvancePayment> findByStatus(String status);

	
	@Query(" select new com.monstarbill.finance.models.AdvancePayment(a.id, a.prePaymentNumber, a.subsidiaryId, a.supplierId, a.prePaymentDate, a.currency,"
			+ " a.subsidiaryCurrency, a.proformaInvoice, a.exchangeRate, a.type, a.advanceAmount, a.paymentAmount, a.paymentMode,"
			+ " a.approvedBy, a.nextApprover, a.nextApproverLevel, a.memo, a.status, a.netsuiteId, a.rejectedComments,"
			+ " a.isDeleted, a.createdDate, a.createdBy, a.lastModifiedBy) from AdvancePayment a "
			+ " inner join MakePayment mp ON a.supplierId = mp.supplierId where a.supplierId = :supplierId AND a.isDeleted = :isDeleted ")
	public List<AdvancePayment> getAllDetailsAndIsDeleted(@Param("supplierId") Long supplierId, @Param("isDeleted") boolean isDeleted);

	public AdvancePayment findById(Long invoiceId);
	
	public List<AdvancePayment> findByIntegratedIdAndSubsidiaryIdAndCreatedDateBetween(String integratedId,
			Long subsidiaryId, Date startDate, Date endDate);


}
