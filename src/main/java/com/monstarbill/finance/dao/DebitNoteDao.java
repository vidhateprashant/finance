package com.monstarbill.finance.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.monstarbill.finance.models.DebitNote;
import com.monstarbill.finance.payload.request.PaginationRequest;

@Component("rtvDao")
public interface DebitNoteDao {
	public List<DebitNote> findAll(String whereClause, PaginationRequest paginationRequest);

	public Long getCount(String whereClause);

}
