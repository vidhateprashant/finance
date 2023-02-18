package com.monstarbill.finance.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.monstarbill.finance.models.MakePaymentHistory;

public interface MakePaymentHistoryRepository extends JpaRepository<MakePaymentHistory, String> {

	public List<MakePaymentHistory> findByPaymentId(Long id, Pageable pageable);


}
