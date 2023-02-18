package com.monstarbill.finance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.monstarbill.finance.models.InvoiceHistory;


@Repository
public interface InvoiceHistoryRepository extends JpaRepository<InvoiceHistory, Long> {
	
	public Page<InvoiceHistory> findByInvoiceId(Long invoiceId, Pageable pageable);

}
