package com.monstarbill.finance.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monstarbill.finance.models.AdvancePaymentApply;


public interface AdvancePaymentApplyRepository extends JpaRepository<AdvancePaymentApply, String> {

	Optional<AdvancePaymentApply> findByIdAndIsDeleted(Long id, boolean isDeleted);

}
