package com.monstarbill.finance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monstarbill.finance.models.MakePaymentList;

public interface MakePaymentListRepository extends JpaRepository<MakePaymentList, String> {

	public Optional<MakePaymentList> findByIdAndIsDeleted(Long id, boolean b);

	public List<MakePaymentList> findByPaymentId(Long paymentId);

	public List<MakePaymentList> getByPaymentId(Long paymentId);

	public List<MakePaymentList> getByPaymentIdAndType(Long paymentId, String type);

	

}
