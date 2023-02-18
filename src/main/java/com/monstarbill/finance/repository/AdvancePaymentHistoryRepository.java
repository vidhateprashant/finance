package com.monstarbill.finance.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.monstarbill.finance.models.AdvancePaymentHistory;


public interface AdvancePaymentHistoryRepository extends JpaRepository<AdvancePaymentHistory, String> {


	List<AdvancePaymentHistory> findByAdvancePaymentNumberOrderById(String payment, Pageable pageable);


}
