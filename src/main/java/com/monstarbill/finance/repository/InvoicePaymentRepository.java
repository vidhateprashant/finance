package com.monstarbill.finance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.monstarbill.finance.models.InvoicePayment;



@Repository
public interface InvoicePaymentRepository extends JpaRepository<InvoicePayment, String> {

	List<InvoicePayment> findByInvoiceId(Long invoiceId);

	@Query("SELECT SUM(i.amount) FROM InvoicePayment i WHERE i.invoiceId = :invoiceId")
	public Double findTotalAmountByInvoiceId (@Param("invoiceId") Long invoiceId);


	@Query("SELECT SUM(i.amount) FROM InvoicePayment i WHERE i.invoiceId = :invoiceId and i.isDeleted = :isDeleted")
	public Double findTotalAmountByInvoiceIdAndIsDeleted (@Param("invoiceId")Long invoiceId, @Param("isDeleted")boolean isDeleted);

	//Optional<InvoicePayment> findByPaymentIdAndIsDeleted(Long paymentId, boolean isDeleted);

	@Query("SELECT SUM(i.amount) FROM InvoicePayment i WHERE i.invoiceId = :invoiceId and i.isDeleted = :isDeleted")
	public Double getTotalAmountByInvoiceIdAndIsDeleted(Long invoiceId, boolean isDeleted);

	public Optional<InvoicePayment> findByPaymentIdAndTypeAndIsDeleted(Long paymentId, String type, boolean isDeleted);

	public Optional<InvoicePayment> findByPaymentIdAndIsDeleted(Long paymentId, boolean isDeleted);

	public List<InvoicePayment> getByPaymentIdAndType(Long paymentId, String type);
}
