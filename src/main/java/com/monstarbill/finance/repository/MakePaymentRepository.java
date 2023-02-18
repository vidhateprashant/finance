package com.monstarbill.finance.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.monstarbill.finance.models.MakePayment;

public interface MakePaymentRepository extends JpaRepository<MakePayment, String> {

	Optional<MakePayment> findByIdAndIsDeleted(Long id, boolean isDeleted);

	List<MakePayment> getByIdAndType(Long paymentId, String type);
	
	public List<MakePayment> findByIntegratedIdAndSubsidiaryIdAndIsDeletedAndCreatedDateBetween(String integratedId,
			Long subsidiaryId, boolean isDeleted, Date startDate, Date endDate);
//
	@Query("select new com.monstarbill.finance.models.MakePayment(p.id,p.paymentNumber, p.paymentDate, p.currency, p.amount, p.paymentMode,"
			+ " p.approvedBy,p.nextApprover,p.nextApproverRole, p.paymentStatus, p.rejectedComments, p.subsidiaryId,  p.supplierId, p.bankReferenceNumber, p.bankId, s.name as subsidiaryName,  sup.name as supplierName,  b.name as bankName,  e.fullName) from MakePayment p "
			+ " inner join Supplier sup on sup.id = p.supplierId " + " inner join Subsidiary s on s.id = p.subsidiaryId"
			+ " inner join Bank b on b.id = p.bankId"
			+ " left join Employee e on CAST(e.id as text) = p.approvedBy  where p.paymentStatus in :status AND p.nextApprover = :user")
	public List<MakePayment> getPaymentForApproval(@Param("status") List<String> status, @Param("user")String user);
//	

}
