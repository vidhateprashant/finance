package com.monstarbill.finance.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monstarbill.finance.common.AppConstants;
import com.monstarbill.finance.common.CommonUtils;
//import com.monstarbill.finance.common.ComponentUtility;
import com.monstarbill.finance.common.CustomException;
import com.monstarbill.finance.common.CustomMessageException;
import com.monstarbill.finance.common.FilterNames;
import com.monstarbill.finance.dao.AdvancePaymentDao;
import com.monstarbill.finance.enums.FormNames;
import com.monstarbill.finance.enums.Operation;
import com.monstarbill.finance.enums.TransactionStatus;
import com.monstarbill.finance.feignclients.MasterServiceClient;
import com.monstarbill.finance.feignclients.SetupServiceClient;
import com.monstarbill.finance.models.AdvancePayment;
import com.monstarbill.finance.models.AdvancePaymentApply;
import com.monstarbill.finance.models.AdvancePaymentHistory;
//import com.monstarbill.finance.models.ApprovalPreference;
import com.monstarbill.finance.models.Invoice;
import com.monstarbill.finance.models.InvoicePayment;
import com.monstarbill.finance.payload.request.ApprovalRequest;
import com.monstarbill.finance.payload.request.PaginationRequest;
import com.monstarbill.finance.payload.response.ApprovalPreference;
import com.monstarbill.finance.payload.response.PaginationResponse;
import com.monstarbill.finance.repository.AdvancePaymentApplyRepository;
import com.monstarbill.finance.repository.AdvancePaymentHistoryRepository;
import com.monstarbill.finance.repository.AdvancePaymentRepository;
import com.monstarbill.finance.repository.InvoicePaymentRepository;
import com.monstarbill.finance.repository.InvoiceRepository;
import com.monstarbill.finance.service.AdvancePaymentService;
//import com.monstarbill.finance.service.ApprovalPreferenceService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class AdvancePaymentServiceImpl implements AdvancePaymentService {

	@Autowired
	private AdvancePaymentRepository advancePaymentRepository;
	
	@Autowired
	private AdvancePaymentDao advancePaymentDao;

	@Autowired
	private AdvancePaymentHistoryRepository advancePaymentHistoryRepository;
	
	@Autowired
	private SetupServiceClient setupServiceClient;
	
	@Autowired
	private MasterServiceClient masterServiceClient;

	@Autowired
	private AdvancePaymentApplyRepository advancePaymentApplyRepository;

	@Autowired
    private InvoiceRepository invoiceRepository;

	@Autowired
	private InvoicePaymentRepository invoicePaymentRepository;
	
	@Override
	public AdvancePayment save(AdvancePayment advancePayment) {
		Optional<AdvancePayment> oldAdvancePayment = Optional.empty();
		if (advancePayment.getId() == null) {
			advancePayment.setDueAmount(advancePayment.getAdvanceAmount());
			advancePayment.setCreatedBy(CommonUtils.getLoggedInUsername());
			if (advancePayment.getPaymentAmount() == null) {
				advancePayment.setPaymentAmount(0.0);
			}
			if (advancePayment.getUnappliedAmount() == null) {
				advancePayment.setUnappliedAmount(advancePayment.getPaymentAmount());
			}
			String transactionalDate = CommonUtils.convertDateToFormattedString(advancePayment.getPrePaymentDate());
			String documentSequenceNumber = this.setupServiceClient.getDocumentSequenceNumber(transactionalDate,
					advancePayment.getSubsidiaryId(), FormNames.ADVANCE_PAYMENT.getFormName(), false);
			if (StringUtils.isEmpty(documentSequenceNumber)) {
				throw new CustomMessageException(
						"Please validate your configuration to generate the Advance payment Number");
			}
			advancePayment.setPrePaymentNumber(documentSequenceNumber);
		} else {
			// Get the existing object using the deep copy
			oldAdvancePayment = this.advancePaymentRepository.findByIdAndIsDeleted(advancePayment.getId(), false);
			if (oldAdvancePayment.isPresent()) {
				try {
					oldAdvancePayment = Optional.ofNullable((AdvancePayment) oldAdvancePayment.get().clone());
				} catch (CloneNotSupportedException e) {
					log.error("Error while Cloning the object. Please contact administrator.");
					throw new CustomException("Error while Cloning the object. Please contact administrator.");
				}
			}
		}
		advancePayment.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		advancePayment.setType(FormNames.ADVANCE_PAYMENT.getFormName());
		advancePayment.setStatus(TransactionStatus.OPEN.getTransactionStatus());

		AdvancePayment savedAdvancePayment;
		try {
			savedAdvancePayment = this.advancePaymentRepository.save(advancePayment);
		} catch (DataIntegrityViolationException e) {
			log.error("Advance payment unique constrain violetd." + e.getMostSpecificCause());
			throw new CustomException("Advance payment unique constrain violetd :" + e.getMostSpecificCause());
		}
		this.updateAdvancePaymentHistory(savedAdvancePayment, oldAdvancePayment);
		log.info(" Advance Payment History is saved : " + oldAdvancePayment);
		log.info(" Data is saved for Advance Payment : " + advancePayment);
		return advancePayment;
	}

	private void saveAdvancePaymentApply(Long advancePaymentId, AdvancePaymentApply advancePaymentApply,
			String advancePaymentNumber) {
		Optional<AdvancePaymentApply> oldAdvancePaymentApply = Optional.empty();
		if (advancePaymentApply.getId() == null) {
			advancePaymentApply.setCreatedBy(CommonUtils.getLoggedInUsername());
		} else {
			// Get the existing object using the deep copy
			oldAdvancePaymentApply = this.advancePaymentApplyRepository.findByIdAndIsDeleted(advancePaymentApply.getId(), false);
			if (oldAdvancePaymentApply.isPresent()) {
				try {
					oldAdvancePaymentApply = Optional.ofNullable((AdvancePaymentApply) oldAdvancePaymentApply.get().clone());
				} catch (CloneNotSupportedException e) {
					log.error("Error while Cloning the object. Please contact administrator.");
					throw new CustomException("Error while Cloning the object. Please contact administrator.");
				}
			}
		}
		advancePaymentApply.setPrePaymentId(advancePaymentId);
		advancePaymentApply.setPrePaymentNumber(advancePaymentNumber);
		//advancePaymentApply.setAmountDue(advancePaymentApply.getInvoiceAmount() - advancePaymentApply.getApplyAmount());
		advancePaymentApply.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		AdvancePaymentApply advancePaymentApplySaved = this.advancePaymentApplyRepository.save(advancePaymentApply);
		if (advancePaymentApplySaved == null) {
			log.info("Error while Saving the apply list in Advance Payment.");
			throw new CustomMessageException("Error while Saving the list list in Advance payment.");
		}
		// update the data in Item history table
		this.updateAdvancePaymentApplyHistory(advancePaymentApplySaved, oldAdvancePaymentApply);
		
	}

	private void updateAdvancePaymentApplyHistory(AdvancePaymentApply advancePaymentApply,
			Optional<AdvancePaymentApply> oldAdvancePaymentApply) {
		if (oldAdvancePaymentApply.isPresent()) {
			// insert the updated fields in history table
			List<AdvancePaymentHistory> advancePaymentHistories = new ArrayList<AdvancePaymentHistory>();
			try {
				advancePaymentHistories = oldAdvancePaymentApply.get().compareFields(advancePaymentApply);
				if (CollectionUtils.isNotEmpty(advancePaymentHistories)) {
					this.advancePaymentHistoryRepository.saveAll(advancePaymentHistories);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				log.error("Error while comparing the new and old objects. Please contact administrator.");
				throw new CustomException(
						"Error while comparing the new and old objects. Please contact administrator.");
			}
			log.info("Advance payment apply History is updated successfully");
		} else {
			// Insert in history table as Operation - INSERT
			this.advancePaymentHistoryRepository.save(this.prepareAdvancePaymentHistory(advancePaymentApply.getPrePaymentNumber(), advancePaymentApply.getId(),
					AppConstants.ADVANCE_PAYMENT_APPLY, Operation.CREATE.toString(), advancePaymentApply.getLastModifiedBy(), null,
					String.valueOf(advancePaymentApply.getId())));
		}
	}
		

	private void updateAdvancePaymentHistory(AdvancePayment advancePayment,
			Optional<AdvancePayment> oldAdvancePayment) {
		if (oldAdvancePayment.isPresent()) {
			// insert the updated fields in history table
			List<AdvancePaymentHistory> advancePaymentHistories = new ArrayList<AdvancePaymentHistory>();
			try {
				advancePaymentHistories = oldAdvancePayment.get().compareFields(advancePayment);
				if (CollectionUtils.isNotEmpty(advancePaymentHistories)) {
					this.advancePaymentHistoryRepository.saveAll(advancePaymentHistories);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				log.error("Error while comparing the new and old objects. Please contact administrator.");
				throw new CustomException(
						"Error while comparing the new and old objects. Please contact administrator.");
			}
			log.info("Make Payment History is updated successfully");
		} else {
			// Insert in history table as Operation - INSERT
			this.advancePaymentHistoryRepository
					.save(this.prepareAdvancePaymentHistory(advancePayment.getPrePaymentNumber(), null,
							AppConstants.ADVANCE_PAYMENT, Operation.CREATE.toString(),
							advancePayment.getLastModifiedBy(), null, advancePayment.getPrePaymentNumber()));
		}
		log.info("Make Payment History is Completed.");

	}

	public AdvancePaymentHistory prepareAdvancePaymentHistory(String payment, Long childId, String moduleName,
			String operation, String lastModifiedBy, String oldValue, String newValue) {
		AdvancePaymentHistory advancePaymentHistory = new AdvancePaymentHistory();
		advancePaymentHistory.setAdvancePaymentNumber(payment);
		advancePaymentHistory.setChildId(childId);
		advancePaymentHistory.setModuleName(moduleName);
		advancePaymentHistory.setChangeType(AppConstants.UI);
		advancePaymentHistory.setOperation(operation);
		advancePaymentHistory.setOldValue(oldValue);
		advancePaymentHistory.setNewValue(newValue);
		advancePaymentHistory.setLastModifiedBy(lastModifiedBy);
		return advancePaymentHistory;
	}

	@Override
	public AdvancePayment getAdvancePaymentById(Long id) {
		Optional<AdvancePayment> advancePayment = Optional.empty();
		advancePayment = advancePaymentRepository.findByIdAndIsDeleted(id, false);
		if (!advancePayment.isPresent()) {
			log.error("advance Payment Not Found against given payment id : " + id);
			throw new CustomMessageException("advance Payment Not Found against given payment id : " + id);
		}
		log.info("advance Payment found against given id : " + id);
		boolean isRoutingActive = this.findIsApprovalRoutingActive(advancePayment.get().getSubsidiaryId());
		if (isRoutingActive) {
			String status = advancePayment.get().getStatus();
			if (!TransactionStatus.OPEN.getTransactionStatus().equalsIgnoreCase(status) && !TransactionStatus.REJECTED.getTransactionStatus().equalsIgnoreCase(status)) {
				isRoutingActive = false;
			}
		}
		advancePayment.get().setApprovalRoutingActive(isRoutingActive);
		return advancePayment.get();
	}

	@Override
	public PaginationResponse findAll(PaginationRequest paginationRequest) {
		List<AdvancePayment> makePayment = new ArrayList<AdvancePayment>();

		// preparing where clause
		String whereClause = this.prepareWhereClause(paginationRequest).toString();

		// get list
		makePayment = this.advancePaymentDao.findAll(whereClause, paginationRequest);

		// getting count
		Long totalRecords = this.advancePaymentDao.getCount(whereClause);

		return CommonUtils.setPaginationResponse(paginationRequest.getPageNumber(), paginationRequest.getPageSize(),
				makePayment, totalRecords);
	}

	private Object prepareWhereClause(PaginationRequest paginationRequest) {
		Long subsidiaryId = null;
		Long supplierId = null;
		String status = null;
		Map<String, ?> filters = paginationRequest.getFilters();
		if (filters.containsKey(FilterNames.SUBSIDIARY_ID))
			subsidiaryId = ((Number) filters.get(FilterNames.SUBSIDIARY_ID)).longValue();
		if (filters.containsKey(FilterNames.SUPPLIER_ID))
			supplierId = ((Number) filters.get(FilterNames.SUPPLIER_ID)).longValue();
		if (filters.containsKey(FilterNames.STATUS))
			status = (String) filters.get(FilterNames.STATUS);
		StringBuilder whereClause = new StringBuilder(" AND ap.isDeleted is false");
		if (subsidiaryId != null && subsidiaryId != 0) {
			whereClause.append(" AND ap.subsidiaryId = ").append(subsidiaryId);
		}
		if (supplierId != null && supplierId != 0) {
			whereClause.append(" AND ap.supplierId = ").append(supplierId);
		}
		if (StringUtils.isNotEmpty(status)) {
			whereClause.append(" AND lower(ap.status) like lower('%").append(status).append("%')");
		}
		return whereClause;
	}

	@Override
	public List<AdvancePayment> getPrePaymentApproval() {
		List<AdvancePayment> advancePayments = new ArrayList<AdvancePayment>();
		advancePayments = this.advancePaymentRepository.findByStatus(TransactionStatus.PENDING_APPROVAL.getTransactionStatus());
		return advancePayments;
	}

	@Override
	public List<AdvancePaymentHistory> findHistoryById(String payment, Pageable pageable) {
		List<AdvancePaymentHistory> histories = this.advancePaymentHistoryRepository.findByAdvancePaymentNumberOrderById(payment, pageable);
		String createdBy = histories.get(0).getLastModifiedBy();
		histories.forEach(e->{
			e.setCreatedBy(createdBy);
		});
		return histories;
	}

	@Override
	public Boolean sendForApproval(Long id) {
		Boolean isSentForApproval = false;

		try {
			/**
			 * Due to single transaction we are getting updated value when we find from repo after the update
			 * hence finding old one first
			 */
			// Get the existing object using the deep copy
			Optional<AdvancePayment> oldAdvancePayment = this.findOldDeepCopiedAdvancePayment(id);

			Optional<AdvancePayment> advancePayment = Optional.empty();
			advancePayment = this.findById(id);

			/**
			 * Check routing is active or not
			 */
			boolean isRoutingActive = advancePayment.get().isApprovalRoutingActive();
			if (!isRoutingActive) {
				log.error("Routing is not active for the Advance Payment : " + id + ". Please update your configuration. ");
				throw new CustomMessageException("Routing is not active for the Advance Payment : " + id + ". Please update your configuration. ");
			}
			
			Double transactionalAmount = advancePayment.get().getAdvanceAmount();
			log.info("Transaction amount for Advance Payment is :: " + transactionalAmount);
			
			// if amount is null then throw error
			if (transactionalAmount == null || transactionalAmount == 0.0) {
				log.error("There is no available Approval Process for this transaction.");
				throw new CustomMessageException("There is no available Approval Process for this transaction.");
			}
			
			ApprovalRequest approvalRequest = new ApprovalRequest();
			approvalRequest.setSubsidiaryId(advancePayment.get().getSubsidiaryId());
			approvalRequest.setFormName(FormNames.ADVANCE_PAYMENT.getFormName());
			approvalRequest.setTransactionAmount(transactionalAmount);
			approvalRequest.setLocationId(advancePayment.get().getLocationId());
			log.info("Approval object us prepared : " + approvalRequest.toString());

			/**
			 * method will give max level & it's sequence if match otherwise throw error message as no approver process exist
			 * if level or sequence id is null then also throws error message.
			 */
			ApprovalPreference approvalPreference = this.masterServiceClient.findApproverMaxLevel(approvalRequest);
			Long sequenceId = approvalPreference.getSequenceId();
			String level = approvalPreference.getLevel();
			Long approverPreferenceId = approvalPreference.getId();
			log.info("Max level & sequence is found :: " + approvalPreference.toString());
			
			advancePayment.get().setApproverSequenceId(sequenceId);
			advancePayment.get().setApproverMaxLevel(level);
			advancePayment.get().setApproverPreferenceId(approverPreferenceId);
			
			String levelToFindRole = "L1";
			if (AppConstants.APPROVAL_TYPE_INDIVIDUAL.equals(approvalPreference.getApprovalType())) {
				levelToFindRole = level;
			}
			approvalRequest = this.masterServiceClient.findApproverByLevelAndSequence(approverPreferenceId, levelToFindRole, sequenceId);

			this.updateApproverDetailsInAdvancePayment(advancePayment, approvalRequest);
			advancePayment.get().setStatus(TransactionStatus.PENDING_APPROVAL.getTransactionStatus());
			log.info("Approver is found and details is updated for Advance Payment :: " + advancePayment.get());
			
			this.saveAdvancePaymentForApproval(advancePayment.get(), oldAdvancePayment);
			log.info("advancePayment is saved successfully with Approver details.");
			
			masterServiceClient.sendEmailByApproverId(advancePayment.get().getNextApprover(), FormNames.ADVANCE_PAYMENT.getFormName());
			
			isSentForApproval = true;
		} catch (Exception e) {
			log.error("Error while sending advance Payment for approval for id - " + id);
			e.printStackTrace();
			throw new CustomMessageException("Error while sending advance Payment for approval for id - " + id + ", Message : " + e.getLocalizedMessage());
		}
		
		return isSentForApproval;
	}
	
	/**
	 * Save Advance Payment after the approval details change
	 * @param advancePayment
	 */
	private void saveAdvancePaymentForApproval(AdvancePayment advancePayment, Optional<AdvancePayment> oldAdvancePayment) {
		advancePayment.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		advancePayment = this.advancePaymentRepository.save(advancePayment);
		
		if (advancePayment == null) {
			log.info("Error while saving the Advance Payment after the Approval.");
			throw new CustomMessageException("Error while saving the Advance Payment after the Approval.");
		}
		log.info("Advance Payment saved successfully :: " + advancePayment.getPrePaymentNumber());
		
		// update the data in PR history table
		this.updateAdvancePaymentHistory(advancePayment, oldAdvancePayment);
		log.info("Advance Payment history is updated. after approval change.");		
	}
	
	/**
	 * Set/Prepares the approver details in the advance Payment object
	 * 
	 * @param purchaseRequisition
	 * @param approvalRequest
	 */
	private void updateApproverDetailsInAdvancePayment(Optional<AdvancePayment> advancePayment, ApprovalRequest approvalRequest) {
		advancePayment.get().setApprovedBy(advancePayment.get().getNextApprover());
		advancePayment.get().setNextApprover(approvalRequest.getNextApprover());
		advancePayment.get().setNextApproverRole(approvalRequest.getNextApproverRole());
		advancePayment.get().setNextApproverLevel(approvalRequest.getNextApproverLevel());
	}
	
	private Optional<AdvancePayment> findOldDeepCopiedAdvancePayment(Long id) {
		Optional<AdvancePayment> advancePayment = this.advancePaymentRepository.findByIdAndIsDeleted(id, false);
		if (advancePayment.isPresent()) {
			try {
				advancePayment = Optional.ofNullable((AdvancePayment) advancePayment.get().clone());
				log.info("Existing advance Payment is copied.");
			} catch (CloneNotSupportedException e) {
				log.error("Error while Cloning the object. Please contact administrator.");
				throw new CustomException("Error while Cloning the object. Please contact administrator.");
			}
		}
		return advancePayment;
	}
	
	public Optional<AdvancePayment> findById(Long id) {
		Optional<AdvancePayment> advancePayment = Optional.empty();
		advancePayment = this.advancePaymentRepository.findByIdAndIsDeleted(id, false);

		if (!advancePayment.isPresent()) {
			log.error("Advance Payment Not Found against given Advance Payment id : " + id);
			throw new CustomMessageException("Advance Payment Not Found against given Advance Payment id : " + id);
		}
		advancePayment.get().setApprovalRoutingActive(this.findIsApprovalRoutingActive(advancePayment.get().getSubsidiaryId()));
		
		return advancePayment;
	}
	
	@Override
	public Boolean approveAllAdvancePayments(List<Long> advancePaymentIds) {
		Boolean isAllPoApproved = false;
		try {
			for (Long advancePaymentId : advancePaymentIds) {
				log.info("Approval Process is started for RTV-id :: " + advancePaymentId);

				/**
				 * Due to single transaction we are getting updated value when we find from repo after the update
				 * hence finding old one first
				 */
				// Get the existing object using the deep copy
				Optional<AdvancePayment> oldAdvancePayment = this.findOldDeepCopiedAdvancePayment(advancePaymentId);

				Optional<AdvancePayment> advancePayment = Optional.empty();
				advancePayment = this.findById(advancePaymentId);

				/**
				 * Check routing is active or not
				 */
				boolean isRoutingActive = advancePayment.get().isApprovalRoutingActive();
				if (!isRoutingActive) {
					log.error("Routing is not active for the Advance Payment : " + advancePaymentId + ". Please update your configuration. ");
					throw new CustomMessageException("Routing is not active for the Advance Payment : " + advancePaymentId + ". Please update your configuration. ");
				}
				
				// meta data
				Long approvalPreferenceId = advancePayment.get().getApproverPreferenceId();
				Long sequenceId = advancePayment.get().getApproverSequenceId();
				String maxLevel = advancePayment.get().getApproverMaxLevel();
				
				String approvalPreferenceType = this.masterServiceClient.getTypeByApprovalId(approvalPreferenceId);
				
				ApprovalRequest approvalRequest = new ApprovalRequest();
				
				if (AppConstants.APPROVAL_TYPE_CHAIN.equalsIgnoreCase(approvalPreferenceType)
						&& !maxLevel.equals(advancePayment.get().getNextApproverLevel())) {
					Long currentLevelNumber = Long.parseLong(advancePayment.get().getNextApproverLevel().replaceFirst("L", "")) + 1;
					String currentLevel = "L" + currentLevelNumber;
					approvalRequest = this.masterServiceClient.findApproverByLevelAndSequence(approvalPreferenceId, currentLevel, sequenceId);
					advancePayment.get().setStatus(TransactionStatus.PARTIALLY_APPROVED.getTransactionStatus());
				} else {
					advancePayment.get().setStatus(TransactionStatus.APPROVED.getTransactionStatus());
				}
				log.info("Approval Request is found for Rtv :: " + approvalRequest.toString());

				this.updateApproverDetailsInAdvancePayment(advancePayment, approvalRequest);
				log.info("Approver is found and details is updated :: " + advancePayment.get());
				
				this.saveAdvancePaymentForApproval(advancePayment.get(), oldAdvancePayment);
				log.info("Advance Payment is saved successfully with Approver details.");

				masterServiceClient.sendEmailByApproverId(advancePayment.get().getNextApprover(), FormNames.ADVANCE_PAYMENT.getFormName());
				
				log.info("Approval Process is Finished for Advance Payment :: " + advancePayment.get().getPrePaymentNumber());
			}
			
			isAllPoApproved = true;
		} catch (Exception e) {
			log.error("Error while approving the Rtv.");
			e.printStackTrace();
			throw new CustomMessageException("Error while approving the Rtv. Message : " + e.getLocalizedMessage());
		}
		return isAllPoApproved;
	}

	private boolean findIsApprovalRoutingActive(Long subsidiaryId) {
		return this.masterServiceClient.findIsApprovalRoutingActive(subsidiaryId, FormNames.ADVANCE_PAYMENT.getFormName());
	}
	
	@Override
	public Boolean rejectAllAdvancePayments(List<AdvancePayment> advancePayments) {
		for (AdvancePayment advancePayment : advancePayments) {
			String rejectComments = advancePayment.getRejectedComments();
			
			if (StringUtils.isEmpty(rejectComments)) {
				log.error("Reject Comments is required.");
				throw new CustomException("Reject Comments is required. It is missing for Advance Payment : " + advancePayment.getId());
			}
			
			Optional<AdvancePayment> oldAdvancePayment = this.findOldDeepCopiedAdvancePayment(advancePayment.getId());

			Optional<AdvancePayment> existingAdvancePayment = this.advancePaymentRepository.findByIdAndIsDeleted(advancePayment.getId(), false);
			existingAdvancePayment.get().setStatus(TransactionStatus.REJECTED.getTransactionStatus());
			existingAdvancePayment.get().setRejectedComments(rejectComments);
			existingAdvancePayment.get().setApprovedBy(null);
			existingAdvancePayment.get().setNextApprover(null);
			existingAdvancePayment.get().setNextApproverRole(null);
			existingAdvancePayment.get().setNextApproverLevel(null);
			existingAdvancePayment.get().setApproverSequenceId(null);
			existingAdvancePayment.get().setApproverMaxLevel(null);
			existingAdvancePayment.get().setApproverPreferenceId(null);
			existingAdvancePayment.get().setNoteToApprover(null);

			log.info("Approval Fields are restored to empty. For Advance Payment : " + advancePayment);
			
			this.saveAdvancePaymentForApproval(existingAdvancePayment.get(), oldAdvancePayment);
			log.info("Advance Payment is saved successfully with restored Approver details.");

			log.info("Approval Process is Finished for Advance Payment-id :: " + advancePayment.getId());
		}
		return true;
	}

	@Override
	public Boolean updateNextApprover(Long approverId, Long advancePaymentId) {
		Optional<AdvancePayment> advancePayment = this.advancePaymentRepository.findByIdAndIsDeleted(advancePaymentId, false);
		
		if (!advancePayment.isPresent()) {
			log.error("Advance Payment Not Found against given Advance Payment id : " + advancePaymentId);
			throw new CustomMessageException("Advance Payment Not Found against given Advance Payment id : " + advancePaymentId);
		}
		advancePayment.get().setNextApprover(String.valueOf(approverId));
		advancePayment.get().setLastModifiedBy(CommonUtils.getLoggedInUsername());
		this.advancePaymentRepository.save(advancePayment.get());
		
		return true;
	}
	
	@Override
	public Boolean selfApprove(Long advancePaymentId) {
		Optional<AdvancePayment> advancePayment = this.advancePaymentRepository.findByIdAndIsDeleted(advancePaymentId, false);
		
		if (!advancePayment.isPresent()) {
			log.error("AdvancePayment Not Found against given AdvancePayment id : " + advancePaymentId);
			throw new CustomMessageException("AdvancePayment Not Found against given AdvancePayment id : " + advancePaymentId);
		}
		advancePayment.get().setStatus(TransactionStatus.APPROVED.getTransactionStatus());
		advancePayment.get().setLastModifiedBy(CommonUtils.getLoggedInUsername());
		
		if (this.advancePaymentRepository.save(advancePayment.get()) != null) return true;
		else throw new CustomException("Error in self approve. Please contact System Administrator");
	}

	@Override
	public AdvancePayment saveApply(@Valid AdvancePayment advancePayment) {
		Optional<AdvancePayment> oldAdvancePayment = Optional.empty();
		Optional<Invoice> invoice = Optional.empty();
		List<InvoicePayment> invoicePaymentList = new ArrayList<>();
		Long advancePaymentId = null;
		String advancePaymentNumber = null;
		if (advancePayment.getId() == null) {
			advancePayment.setDueAmount(advancePayment.getAdvanceAmount());
			advancePayment.setCreatedBy(CommonUtils.getLoggedInUsername());
			if (advancePayment.getPaymentAmount() == null) {
				advancePayment.setPaymentAmount(0.0);
			}
			if (advancePayment.getUnappliedAmount() == null) {
				advancePayment.setUnappliedAmount(0.0);
			}
		} else {
			// Get the existing object using the deep copy
			oldAdvancePayment = this.advancePaymentRepository.findByIdAndIsDeleted(advancePayment.getId(), false);
			if (oldAdvancePayment.isPresent()) {
				try {
					oldAdvancePayment = Optional.ofNullable((AdvancePayment) oldAdvancePayment.get().clone());
				} catch (CloneNotSupportedException e) {
					log.error("Error while Cloning the object. Please contact administrator.");
					throw new CustomException("Error while Cloning the object. Please contact administrator.");
				}
			}
		}
		if ((advancePayment.getUnappliedAmount() < advancePayment.getPaymentAmount()) && (advancePayment.getUnappliedAmount() > 0)) {
			advancePayment.setStatus(TransactionStatus.PARTIALLY_APPLIED.getTransactionStatus());
		} else
			advancePayment.setStatus(TransactionStatus.APPLIED.getTransactionStatus());

		advancePayment.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		advancePayment.setType("Advance Payment");
		AdvancePayment savedAdvancePayment;
		try {
			savedAdvancePayment = this.advancePaymentRepository.save(advancePayment);
		} catch (DataIntegrityViolationException e) {
			log.error("Advance payment unique constrain violetd." + e.getMostSpecificCause());
			throw new CustomException("Advance payment unique constrain violetd :" + e.getMostSpecificCause());
		}
		advancePaymentId = savedAdvancePayment.getId();
		advancePaymentNumber = savedAdvancePayment.getPrePaymentNumber();
		
		this.updateAdvancePaymentHistory(savedAdvancePayment, oldAdvancePayment);
		log.info(" Advance Payment History is saved : " + oldAdvancePayment);
		if(advancePayment.getId()!= null) {
			//-------------------apply----------------------\\
			List<AdvancePaymentApply> advancePaymentApplies =  advancePayment.getAdvancePaymentApply();
			if(CollectionUtils.isNotEmpty(advancePaymentApplies)) {
				for (AdvancePaymentApply advancePaymentApply : advancePaymentApplies) {
					this.saveAdvancePaymentApply(advancePaymentId,advancePaymentApply,advancePaymentNumber);	
					InvoicePayment invoicePayment = new InvoicePayment();
					invoicePayment.setPaymentId(advancePaymentApply.getPrePaymentId());
					invoicePayment.setInvoiceId(advancePaymentApply.getInvoiceId());
					invoicePayment.setBillNo(advancePaymentApply.getPrePaymentNumber());
					invoicePayment.setAmount(advancePaymentApply.getApplyAmount());
					invoicePayment.setBillDate(advancePaymentApply.getApplyDate());
					invoicePayment.setType("Advance Payment");
					invoicePaymentList.add(invoicePayment);
					invoice = this.invoiceRepository.findByInvoiceId(invoicePayment.getInvoiceId());
					if(!invoice.isPresent()) {
						log.error(" Invoice is not created for id : " + invoicePayment.getInvoiceId());
						throw new CustomException(" Invoice is not created for id : " + invoicePayment.getInvoiceId());
					}
					if(invoice.get().getAmountDue()==0.0) {
						log.error(" There is no due for the invoice id : " + invoicePayment.getInvoiceId());
						throw new CustomException(" There is no due for the invoice id : " + invoicePayment.getInvoiceId());
					}
					Double totalAmountPaid = 0.0;
					totalAmountPaid = this.invoicePaymentRepository.findTotalAmountByInvoiceIdAndIsDeleted(invoicePayment.getInvoiceId(),false);	
					if(totalAmountPaid == null) {
						totalAmountPaid = 0.0;
					}
					totalAmountPaid= totalAmountPaid + invoicePayment.getAmount();
					log.info("total amount due " + totalAmountPaid);
					invoice.get().setTotalPaidAmount(totalAmountPaid);
					invoice.get().setAmountDue(invoice.get().getTotalAmount() - invoice.get().getTotalPaidAmount());
					//log.info("Saved invoice payment : " + savedInvoicePayment);
					this.invoiceRepository.save(invoice.get());
					InvoicePayment savedInvoicePayment = this.invoicePaymentRepository.save(invoicePayment);
					log.info("Saved invoice payment : " + savedInvoicePayment);
				}
			}
		}
		log.info(" Data is saved for Advance Payment : " + advancePayment);
		return advancePayment;
	}

	@Override
	public List<Invoice> findBySubsidiaryAndSuppplierAndCurrency(Long subsidiaryId, Long supplierId, String currency) {
		List<Invoice> invoices = new ArrayList<Invoice>();
		invoices = this.invoiceRepository
				.getAllInvoiceBySubsidiaryIdAndSupplierIdAndCurrency( subsidiaryId, supplierId, currency);
		log.info("Get all invoice by subsidiaryId and supplierId and currency ." + invoices);
		return invoices;
	}
}
