package com.monstarbill.finance.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.monstarbill.finance.models.DebitNoteHistory;

@Repository
public interface DebitNoteHistoryRepository extends JpaRepository<DebitNoteHistory, String> {

	public List<DebitNoteHistory> findByDebitNoteNumber(String debitNoteNumber, Pageable pageable);
}
