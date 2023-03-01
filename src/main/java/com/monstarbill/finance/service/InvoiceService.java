package com.monstarbill.finance.service;

import java.sql.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.monstarbill.finance.models.Invoice;
import com.monstarbill.finance.models.InvoiceHistory;
import com.monstarbill.finance.models.InvoicePayment;
import com.monstarbill.finance.payload.request.PaginationRequest;
import com.monstarbill.finance.payload.response.PaginationResponse;




public interface InvoiceService {
	
	Invoice saveInvoice(Invoice invoice);
	
	PaginationResponse getInvoices(PaginationRequest paginationRequest);

	List<Invoice> findBySubsidiaryAndSuppplier(Long subsidiaryId, Long supplierId);

	Invoice getInvoice(Long invoiceId);
	
	List<InvoiceHistory> getInvoiceHistory(Long invoiceId, Pageable pageable);

	List<InvoicePayment> saveInvoicePayment(List<InvoicePayment> invoicePayment);

	public Boolean sendForApproval(Long id);

	public Boolean approveAllInvoices(List<Long> invoiceIds);

	public byte[] downloadTemplate();

	public byte[] upload(MultipartFile file);

	List<Invoice> getIdAndIntegratedIdAndCreatedDateBetween(Long subsidiaryId, Date startDate, Date endDate);

	Boolean selfApprove(Long invoiceId);
	
	List<Invoice> getInvoiceApproval(String user);
	
}
