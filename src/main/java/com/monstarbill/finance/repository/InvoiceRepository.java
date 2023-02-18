package com.monstarbill.finance.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.monstarbill.finance.models.Invoice;


@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor {

	
	
	List<Invoice> getAllInvoiceBySubsidiaryIdAndSupplierId(@Param("subsidiaryId") Long subsidiaryId,@Param("supplierId") Long supplierId);

	List<Invoice> getAllInvoiceBySupplierId(Long supplierId);

	@Query(" select i from Invoice i where i.supplierId = :supplierId and i.amountDue != 0.0 ")
	List<Invoice> findBySupplierId(Long supplierId);
	
	Optional<Invoice> findByInvoiceId(Long invoiceId);

	Optional<Invoice> getByInvoiceId(Long invoiceId);

	public List<Invoice> findByIntegratedIdAndSubsidiaryIdAndCreatedDateBetween(String integratedId, Long subsidiaryId,
			Date startDate, Date endDate);

	@Query(" select i from Invoice i where i.supplierId = :supplierId and i.currency = :currency and i.amountDue != 0.0 ")
	List<Invoice> findBySupplierIdAndCurrency(Long supplierId, String currency);

	List<Invoice> getAllInvoiceBySubsidiaryIdAndSupplierIdAndCurrency(@Param("subsidiaryId") Long subsidiaryId,@Param("supplierId") Long supplierId,
			@Param("currency")String currency);
}
