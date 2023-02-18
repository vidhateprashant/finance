package com.monstarbill.finance.service;

import java.util.List;

import javax.validation.Valid;

import org.springframework.data.domain.Pageable;

import com.monstarbill.finance.models.Invoice;
import com.monstarbill.finance.models.InvoicePayment;
import com.monstarbill.finance.models.MakePayment;
import com.monstarbill.finance.models.MakePaymentHistory;
import com.monstarbill.finance.payload.request.PaginationRequest;
import com.monstarbill.finance.payload.response.PaginationResponse;

public interface MakePaymentService {

	public MakePayment save(MakePayment makePayments);

	public MakePayment getMakePaymentById(Long id);

	public PaginationResponse findAll(PaginationRequest paginationRequest);

	public List<MakePaymentHistory> findHistoryById(Long id, Pageable pageable);

	public List<Invoice> getPaymentDetailsBySupplier(Long supplierId);

	public List<InvoicePayment> deleteById(Long paymentId, String type);

	public @Valid MakePayment saveForAdvancePayment(@Valid MakePayment makePayment);

	public Boolean sendForApproval(Long id);

	public Boolean approveAllPayments(List<Long> paymentIds);

	public Boolean rejectAllPayments(List<MakePayment> payments);

	public Boolean selfApprove(Long id);

	public List<Invoice> getPaymentDetailsBySupplierAndCurrency(Long supplierId, String currency);

	//public List<Invoice> getDueAmount(Long invoiceId);

	//public List<AdvancePayment> makePayments(List<AdvancePayment> advancePayment);

	List<MakePayment> getPaymentApproval(String user);

}
