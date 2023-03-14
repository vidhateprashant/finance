package com.monstarbill.finance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monstarbill.finance.models.MakePaymentInfo;

public interface MakePaymentInfoRepository extends JpaRepository<MakePaymentInfo, String> {

	public MakePaymentInfo getByMakePaymentId(Long paymentId);

	


}
