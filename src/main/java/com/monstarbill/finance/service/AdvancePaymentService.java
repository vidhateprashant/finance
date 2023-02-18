package com.monstarbill.finance.service;

import java.util.List;

import javax.validation.Valid;

import org.springframework.data.domain.Pageable;

import com.monstarbill.finance.models.AdvancePayment;
import com.monstarbill.finance.models.AdvancePaymentHistory;
import com.monstarbill.finance.models.Invoice;
//import com.monstarbill.finance.models.Invoice;
import com.monstarbill.finance.payload.request.PaginationRequest;
import com.monstarbill.finance.payload.response.PaginationResponse;
//import com.monstarbill.finance.service.AdvancePaymentService;

public interface AdvancePaymentService {

	public AdvancePayment save(AdvancePayment advancePayment);

	public AdvancePayment getAdvancePaymentById(Long id);

	public PaginationResponse findAll(PaginationRequest paginationRequest);

	public List<AdvancePayment> getPrePaymentApproval();

	public List<AdvancePaymentHistory> findHistoryById(String payment, Pageable pageable);

	public Boolean sendForApproval(Long id);

	public Boolean approveAllAdvancePayments(List<Long> advancePaymentIds);

	public Boolean rejectAllAdvancePayments(List<AdvancePayment> advancePayments);

	public Boolean updateNextApprover(Long approverId, Long advancePaymentId);

	public Boolean selfApprove(Long advancePaymentId);

	public AdvancePayment saveApply(@Valid AdvancePayment advancePayment);

	public List<Invoice> findBySubsidiaryAndSuppplierAndCurrency(Long subsidiaryId, Long supplierId, String currency);

}
