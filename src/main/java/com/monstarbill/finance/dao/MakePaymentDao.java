package com.monstarbill.finance.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.monstarbill.finance.models.MakePayment;
import com.monstarbill.finance.payload.request.PaginationRequest;

@Component("makePaymentDao")
public interface MakePaymentDao {
	
	public List<MakePayment> findAll(String whereClause, PaginationRequest paginationRequest);
	public Long getCount(String whereClause);

}




