package com.monstarbill.finance.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.monstarbill.finance.models.DebitNote;
import com.monstarbill.finance.models.DebitNoteHistory;
import com.monstarbill.finance.payload.request.PaginationRequest;
import com.monstarbill.finance.payload.response.PaginationResponse;

public interface DebitNoteService {
	
	public DebitNote save(DebitNote debitNote);

	public DebitNote getDebitNoteById(Long id);
	
	public List<DebitNoteHistory> findAuditByDebitNoteNumber(String debitNoteNumber, Pageable pageable);
	
	public PaginationResponse findAll(PaginationRequest paginationRequest);
	
}
