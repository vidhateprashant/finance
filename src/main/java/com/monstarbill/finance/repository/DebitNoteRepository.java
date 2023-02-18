package com.monstarbill.finance.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monstarbill.finance.models.DebitNote;

public interface DebitNoteRepository extends JpaRepository<DebitNote, String>{
	public Optional<DebitNote> findByIdAndIsDeleted(Long id, boolean isDeleted);
	
	public List<DebitNote> findByIntegratedIdAndSubsidiaryIdAndIsDeletedAndCreatedDateBetween(String integratedId,
			Long subsidiaryId, boolean isDeleted, Date startDate,
			Date endDate);

}
