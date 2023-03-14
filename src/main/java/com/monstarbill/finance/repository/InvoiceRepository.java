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

	@Query(" select i from Invoice i where i.subsidiaryId = :subsidiaryId and i.supplierId = :supplierId and i.currency = :currency and i.invStatus in :status ")
	List<Invoice> getAllInvoiceBySubsidiaryIdAndSupplierIdAndCurrencyAndInvStatus(@Param("subsidiaryId") Long subsidiaryId,@Param("supplierId") Long supplierId,
			@Param("currency")String currency, @Param("status")List<String> status);


	public List<Invoice> findBySubsidiaryIdAndIntegratedIdAndCreatedDateBetween(Long subsidiaryId, String integratedId,
			java.sql.Date startDate, java.sql.Date endDate);
	
	@Query("select new com.monstarbill.finance.models.Invoice(i.id,i.invoiceDate, i.invoiceNo, i.amount, i.taxAmount, i.totalAmount, "
			+ " i.approvedBy, i.nextApprover, i.nextApproverRole, i.invStatus, l.locationName, s.name as subsidiaryName , sup.name as supplierName,i.rejectComment,i.supplierId,i.subsidiaryId,i.locationId,e.fullName) from Invoice i "
			+ "inner join Supplier sup on sup.id = i.supplierId " 
			+ "inner join Subsidiary s on s.id = i.subsidiaryId  "
			+ "inner join Location l on l.id = i.locationId "
			+ "left join Employee e on CAST(e.id as text) = i.approvedBy where i.invStatus in :status AND i.nextApprover = :user")
	public List<Invoice> getInvoiceForApproval(@Param("status")List<String> status,@Param("user")String user);

}
