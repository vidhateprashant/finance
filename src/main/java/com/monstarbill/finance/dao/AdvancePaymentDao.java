package com.monstarbill.finance.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.monstarbill.finance.models.AdvancePayment;
import com.monstarbill.finance.payload.request.PaginationRequest;


@Component("advancePaymentDao")
public interface AdvancePaymentDao {

	public List<AdvancePayment> findAll(String whereClause, PaginationRequest paginationRequest);

	public Long getCount(String whereClause);

}
