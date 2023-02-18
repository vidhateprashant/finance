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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monstarbill.finance.common.AppConstants;
import com.monstarbill.finance.common.CommonUtils;
import com.monstarbill.finance.common.CustomException;
import com.monstarbill.finance.common.CustomMessageException;
import com.monstarbill.finance.common.FilterNames;
import com.monstarbill.finance.dao.MakePaymentDao;
import com.monstarbill.finance.enums.FormNames;
import com.monstarbill.finance.enums.Operation;
import com.monstarbill.finance.enums.TransactionStatus;
import com.monstarbill.finance.feignclients.MasterServiceClient;
import com.monstarbill.finance.feignclients.SetupServiceClient;
import com.monstarbill.finance.models.AdvancePayment;
import com.monstarbill.finance.models.Bank;
import com.monstarbill.finance.models.Invoice;
import com.monstarbill.finance.models.InvoicePayment;
import com.monstarbill.finance.models.MakePayment;
import com.monstarbill.finance.models.MakePaymentHistory;
import com.monstarbill.finance.models.MakePaymentList;
import com.monstarbill.finance.models.Supplier;
import com.monstarbill.finance.payload.request.ApprovalRequest;
import com.monstarbill.finance.payload.request.PaginationRequest;
import com.monstarbill.finance.payload.response.ApprovalPreference;
import com.monstarbill.finance.payload.response.PaginationResponse;
import com.monstarbill.finance.repository.AdvancePaymentRepository;
import com.monstarbill.finance.repository.InvoicePaymentRepository;
//import com.monster.bill.repository.AdvancePaymentRepository;
import com.monstarbill.finance.repository.InvoiceRepository;
import com.monstarbill.finance.repository.MakePaymentHistoryRepository;
import com.monstarbill.finance.repository.MakePaymentListRepository;
import com.monstarbill.finance.repository.MakePaymentRepository;
import com.monstarbill.finance.service.MakePaymentService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class MakePaymentServiceImpl implements MakePaymentService {

	@Autowired
	private MakePaymentRepository makePaymentRepository;

	@Autowired
	private MakePaymentDao makePaymentDao;

	@Autowired
	private MakePaymentHistoryRepository makePaymentHistoryRepository;

	@Autowired
	private InvoiceRepository invoiceRepository;

	@Autowired
	private MakePaymentListRepository makePaymentListRepository;

	@Autowired
	private InvoicePaymentRepository invoicePaymentRepository;

	@Autowired
	private AdvancePaymentRepository advancePaymentRepository;

	@Autowired
	private SetupServiceClient setupServiceClient;

	@Autowired
	private MasterServiceClient masterServiceClient;

	@Override
	public MakePayment save(MakePayment makePayment) {
		Optional<MakePayment> oldMakePayment = Optional.empty();
		Optional<Invoice> invoice = Optional.empty();

		Long paymentId = null;
		String paymentNumber = null;
		if (makePayment.getId() == null) {
			makePayment.setCreatedBy(CommonUtils.getLoggedInUsername());
			String transactionalDate = CommonUtils.convertDateToFormattedString(makePayment.getPaymentDate());
			String documentSequenceNumber = setupServiceClient.getDocumentSequenceNumber(transactionalDate,
					makePayment.getSubsidiaryId(), FormNames.MAKE_PAYMENT.getFormName(), false);
			if (StringUtils.isEmpty(documentSequenceNumber)) {
				throw new CustomMessageException(
						"Please validate your configuration to generate the Payment number Number");
			}
			makePayment.setPaymentNumber(documentSequenceNumber);
			makePayment.setType(FormNames.MAKE_PAYMENT.getFormName());
			makePayment.setStatus(TransactionStatus.OPEN.getTransactionStatus());
		} else {
			// Get the existing object using the deep copy
			oldMakePayment = this.makePaymentRepository.findByIdAndIsDeleted(makePayment.getId(), false);
			if (oldMakePayment.isPresent()) {
				try {
					oldMakePayment = Optional.ofNullable((MakePayment) oldMakePayment.get().clone());
				} catch (CloneNotSupportedException e) {
					log.error("Error while Cloning the object. Please contact administrator.");
					throw new CustomException("Error while Cloning the object. Please contact administrator.");
				}
			}
		}
		makePayment.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		MakePayment savedMakePayment = this.makePaymentRepository.save(makePayment);
		log.info(" Data is saved for Make Payment - " + makePayment);
		this.updateMakePaymentHistory(savedMakePayment, oldMakePayment);
		log.info(" Make Payment history is saved : " + oldMakePayment);
		paymentId = savedMakePayment.getId();
		paymentNumber = savedMakePayment.getPaymentNumber();

		// ------make payment list-----//
		List<MakePaymentList> makePaymentLists = makePayment.getMakePaymentList();
		List<InvoicePayment> invoicePaymentList = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(makePaymentLists)) {
			for (MakePaymentList payment : makePaymentLists) {
				invoice = this.invoiceRepository.findByInvoiceId(payment.getInvoiceId());
				if (!invoice.isPresent()) {
					log.error("Invoice is not found against the provided value : " + payment.getInvoiceId());
					throw new CustomMessageException(
							"Invoice is not found against the provided value : " + payment.getInvoiceId());
				}
				this.savedMakePaymentList(paymentId, payment, paymentNumber);
				InvoicePayment invoicePayment = new InvoicePayment();
				invoicePayment.setPaymentId(paymentId);
				invoicePayment.setInvoiceId(payment.getInvoiceId());
				invoicePayment.setBillNo(payment.getPaymentNumber());
				invoicePayment.setAmount(payment.getPaymentAmount());
				invoicePayment.setBankName(makePayment.getBankAccountName());
				invoicePayment.setBillDate(makePayment.getPaymentDate());
				invoicePayment.setType("Make Payment");
				invoicePaymentList.add(invoicePayment);
				Double totalAmountPaid = 0.0;

				totalAmountPaid = this.invoicePaymentRepository
						.findTotalAmountByInvoiceIdAndIsDeleted(payment.getInvoiceId(), false);
				if (totalAmountPaid == null) {
					totalAmountPaid = 0.0;
				}
				totalAmountPaid = totalAmountPaid + payment.getPaymentAmount();
				log.info("total amount due " + totalAmountPaid);
				invoice.get().setTotalPaidAmount(totalAmountPaid);
				invoice.get().setAmountDue(invoice.get().getTotalAmount() - invoice.get().getTotalPaidAmount());
				this.invoiceRepository.save(invoice.get());
			}
			invoicePaymentList = this.invoicePaymentRepository.saveAll(invoicePaymentList);
			System.gc();
			savedMakePayment = this.getMakePaymentById(savedMakePayment.getId());
		}

		return makePayment;
	}

	private void savedMakePaymentList(Long paymentId, MakePaymentList makePaymentList, String paymentNumber) {
		Optional<MakePaymentList> oldMakePaymentList = Optional.empty();
		if (makePaymentList.getId() == null) {
			if (makePaymentList.getPaymentAmount() == 0) {
				log.error("payment amount cannot be 0 .");
				throw new CustomException("payment amount cannot be 0 .");
			}
			if (makePaymentList.getPaymentAmount() > makePaymentList.getAmountDue()) {
				log.error("payment amount :" + makePaymentList.getPaymentAmount()
						+ ": cannot be greater than due amount :" + makePaymentList.getAmountDue());
				throw new CustomException("payment amount :" + makePaymentList.getPaymentAmount()
						+ ": cannot be greater than due amount :" + makePaymentList.getAmountDue());
			}
			makePaymentList.setCreatedBy(CommonUtils.getLoggedInUsername());
		} else {
			// Get the existing object using the deep copy
			oldMakePaymentList = this.makePaymentListRepository.findByIdAndIsDeleted(makePaymentList.getId(), false);
			if (oldMakePaymentList.isPresent()) {
				try {
					oldMakePaymentList = Optional.ofNullable((MakePaymentList) oldMakePaymentList.get().clone());
				} catch (CloneNotSupportedException e) {
					log.error("Error while Cloning the object. Please contact administrator.");
					throw new CustomException("Error while Cloning the object. Please contact administrator.");
				}
			}
		}
		makePaymentList.setPaymentNumber(paymentNumber);
		makePaymentList.setPaymentId(paymentId);
		makePaymentList.setPaidAmount(makePaymentList.getPaymentAmount());
		MakePaymentList makePaymentListSaved = this.makePaymentListRepository.save(makePaymentList);
		if (makePaymentListSaved == null) {
			log.info("Error while Saving the Item list in make payment.");
			throw new CustomMessageException("Error while Saving the Item list in make payment.");
		}
		// update the data in Item history table
		this.updateMakePaymentListHistory(makePaymentListSaved, oldMakePaymentList);

	}

	private void updateMakePaymentListHistory(MakePaymentList makePaymentList,
			Optional<MakePaymentList> oldMakePaymentList) {
		if (oldMakePaymentList.isPresent()) {
			// insert the updated fields in history table
			List<MakePaymentHistory> makePaymentHistories = new ArrayList<MakePaymentHistory>();
			try {
				makePaymentHistories = oldMakePaymentList.get().compareFields(makePaymentList);
				if (CollectionUtils.isNotEmpty(makePaymentHistories)) {
					this.makePaymentHistoryRepository.saveAll(makePaymentHistories);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				log.error("Error while comparing the new and old objects. Please contact administrator.");
				throw new CustomException(
						"Error while comparing the new and old objects. Please contact administrator.");
			}
			log.info("Make payment list History is updated successfully");
		} else {
			// Insert in history table as Operation - INSERT
			this.makePaymentHistoryRepository.save(this.prepareMakePaymentHistory(makePaymentList.getId(),
					makePaymentList.getId(), AppConstants.MAKE_PAYMENT_LIST, Operation.CREATE.toString(),
					makePaymentList.getLastModifiedBy(), null, String.valueOf(makePaymentList.getId())));
		}
	}

	private void updateMakePaymentHistory(MakePayment makePayment, Optional<MakePayment> oldMakePayment) {
		if (oldMakePayment.isPresent()) {
			// insert the updated fields in history table
			List<MakePaymentHistory> makePaymentHistoryHistories = new ArrayList<MakePaymentHistory>();
			try {
				makePaymentHistoryHistories = oldMakePayment.get().compareFields(makePayment);
				if (CollectionUtils.isNotEmpty(makePaymentHistoryHistories)) {
					this.makePaymentHistoryRepository.saveAll(makePaymentHistoryHistories);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				log.error("Error while comparing the new and old objects. Please contact administrator.");
				throw new CustomException(
						"Error while comparing the new and old objects. Please contact administrator.");
			}
			log.info("Make Payment History is updated successfully");
		} else {
			// Insert in history table as Operation - INSERT
			this.makePaymentHistoryRepository.save(this.prepareMakePaymentHistory(makePayment.getId(), null,
					AppConstants.MAKE_PAYMENT, Operation.CREATE.toString(), makePayment.getLastModifiedBy(), null,
					makePayment.getPaymentNumber()));
		}
		log.info("Make Payment History is Completed.");
	}

	/**
	 * Prepares the make payment history object
	 * 
	 * @param payment
	 * @param childId
	 * @param moduleName
	 * @param operation
	 * @param lastModifiedBy
	 * @param oldValue
	 * @param newValue
	 * @return
	 */
	public MakePaymentHistory prepareMakePaymentHistory(Long paymentId, Long childId, String moduleName,
			String operation, String lastModifiedBy, String oldValue, String newValue) {
		MakePaymentHistory makePaymentHistory = new MakePaymentHistory();
		makePaymentHistory.setPaymentId(paymentId);
		makePaymentHistory.setChildId(childId);
		makePaymentHistory.setModuleName(moduleName);
		makePaymentHistory.setChangeType(AppConstants.UI);
		makePaymentHistory.setOperation(operation);
		makePaymentHistory.setOldValue(oldValue);
		makePaymentHistory.setNewValue(newValue);
		makePaymentHistory.setLastModifiedBy(lastModifiedBy);
		return makePaymentHistory;

	}

	@Override
	public MakePayment getMakePaymentById(Long id) {

		Supplier supplier = new Supplier();

		Bank bank = new Bank();
		Optional<MakePayment> makePayment = Optional.empty();
		makePayment = makePaymentRepository.findByIdAndIsDeleted(id, false);

		supplier = masterServiceClient.findSupplierById(makePayment.get().getSupplierId());
		if (supplier != null) {
			String supplierName = supplier.getName();
			makePayment.get().setSupplierName(supplierName);
		} else {
			log.error("supplier is Not Found against given supplier id : " + makePayment.get().getSupplierId());
			throw new CustomMessageException(
					"supplier is Not Found against given supplier id : " + makePayment.get().getSupplierId());
		}

		bank = masterServiceClient.getBankById(makePayment.get().getBankId());
		if (bank != null) {
			makePayment.get().setBankAccountName(bank.getName());

		} else {
			log.error("bank Not Found against given bank id : " + makePayment.get().getBankId());
			throw new CustomMessageException(
					"bank  Not Found against given bank id : " + makePayment.get().getBankId());
		}
		if (makePayment.isPresent()) {
			Long paymentId = makePayment.get().getId();
			log.info("payment found against given id : " + id);
			// Get list
			List<MakePaymentList> makePaymentList = makePaymentListRepository.findByPaymentId(paymentId);
			if (CollectionUtils.isNotEmpty(makePaymentList)) {
				for (MakePaymentList makePaymentLine : makePaymentList) {
					Long invoiceId = makePaymentLine.getInvoiceId();

					if (invoiceId != null) {
						if (FormNames.ADVANCE_PAYMENT.getFormName().equalsIgnoreCase(makePaymentLine.getType())) {
							Optional<AdvancePayment> advancePayment = this.advancePaymentRepository.findByIdAndIsDeleted(makePaymentLine.getInvoiceId(), false);
							if (advancePayment.isPresent()) {
								makePaymentLine.setCurrency(advancePayment.get().getCurrency());
							}
						}
						
						if (FormNames.INVOICE.getFormName().equalsIgnoreCase(makePaymentLine.getType())) {
							Optional<Invoice> invoice = this.invoiceRepository.findByInvoiceId(invoiceId);
							if (invoice.isPresent()) {
								makePaymentLine.setCurrency(invoice.get().getCurrency());
							}
						}
					}
				}
				makePayment.get().setMakePaymentList(makePaymentList);
			}
		} else {
			log.error("Make Payment Not Found against given payment id : " + id);
			throw new CustomMessageException("Make Payment Not Found against given payment id : " + id);
		}

		return makePayment.get();
	}

	@Override
	public PaginationResponse findAll(PaginationRequest paginationRequest) {
		List<MakePayment> makePayment = new ArrayList<MakePayment>();

		// preparing where clause
		String whereClause = this.prepareWhereClause(paginationRequest).toString();

		// get list
		makePayment = this.makePaymentDao.findAll(whereClause, paginationRequest);

		// getting count
		Long totalRecords = this.makePaymentDao.getCount(whereClause);

		return CommonUtils.setPaginationResponse(paginationRequest.getPageNumber(), paginationRequest.getPageSize(),
				makePayment, totalRecords);
	}

	private Object prepareWhereClause(PaginationRequest paginationRequest) {
		Long supplierId = null;
		Long subsidiaryId = null;
		Map<String, ?> filters = paginationRequest.getFilters();

		if (filters.containsKey(FilterNames.SUPPLIER_ID))
			supplierId = ((Number) filters.get(FilterNames.SUPPLIER_ID)).longValue();
		if (filters.containsKey(FilterNames.SUBSIDIARY_ID))
			subsidiaryId = ((Number) filters.get(FilterNames.SUBSIDIARY_ID)).longValue();

		StringBuilder whereClause = new StringBuilder(" AND mp.isDeleted is false");
		if (supplierId != null && supplierId != 0) {
			whereClause.append(" AND mp.supplierId = ").append(supplierId);
		}
		if (subsidiaryId != null && subsidiaryId != 0) {
			whereClause.append(" AND mp.subsidiaryId = ").append(subsidiaryId);
		}
		return whereClause;
	}

	@Override
	public List<MakePaymentHistory> findHistoryById(Long id, Pageable pageable) {
		List<MakePaymentHistory> makePaymentHistories = new ArrayList<MakePaymentHistory>();
		makePaymentHistories = this.makePaymentHistoryRepository.findByPaymentId(id, pageable);
		log.info("Make Payment running successfully " + makePaymentHistories);
		return makePaymentHistories;
	}

	@Override
	public List<Invoice> getPaymentDetailsBySupplier(Long supplierId) {
		List<Invoice> invoices = new ArrayList<Invoice>();
		invoices = this.invoiceRepository.findBySupplierId(supplierId);
//		if(CollectionUtils.isNotEmpty(invoices)) {
//		for (Invoice invoice : invoices) {
//			Double remainDueAmount = 0.0;
//			remainDueAmount = this.invoicePaymentRepository.findTotalAmountByInvoiceId(invoice.getInvoiceId());
//			if(remainDueAmount == null) {
//				remainDueAmount= 0.0;
//			}
//			log.info("total amount due " + remainDueAmount);
//			invoice.setTotalPaidAmount(remainDueAmount);
//			invoice.setAmountDue(invoice.getTotalAmount() - invoice.getTotalPaidAmount());		
//			this.invoiceRepository.save(invoice);
//		}
//		}
		return invoices;
	}

//	@Override
//	public List<AdvancePayment> makePayments(List<AdvancePayment> advancePayments) {
//		for (AdvancePayment advancePayment : advancePayments) {
//			if (advancePayment.getId() == null) {
//				advancePayment.setCreatedBy(CommonUtils.getLoggedInUsername());
//			} 
//			advancePayment.setLastModifiedBy(CommonUtils.getLoggedInUsername());
//			advancePayment.setPaymentAmount(advancePayment.getPaymentAmount() + advancePayment.getPartPaymentAmount());
//			AdvancePayment savedAdvancePayment = this.advancePaymentRepository.save(advancePayment);
//			log.info("Make Payment is done for Advance Payment " + savedAdvancePayment);
//		}
//		return advancePayments;
//	}
	@Override
	public List<InvoicePayment> deleteById(Long paymentId, String type) {
		Optional<Invoice> invoice = Optional.empty();
		List<InvoicePayment> invoicePayments = new ArrayList<InvoicePayment>();
		invoicePayments = this.invoicePaymentRepository.getByPaymentIdAndType(paymentId, type);
		for (InvoicePayment invoicePayment : invoicePayments) {
			// invoicePayment = this.getInvoicePaymentByPaymentId(paymentId);
			log.info(" invoice payment list " + invoicePayment);
			invoicePayment.setDeleted(true);

			invoice = this.invoiceRepository.findByInvoiceId(invoicePayment.getInvoiceId());
			if (!invoice.isPresent()) {
				log.error("Invoice is not found against the provided value : " + invoicePayment.getInvoiceId());
				throw new CustomMessageException(
						"Invoice is not found against the provided value : " + invoicePayment.getInvoiceId());
			}
			invoice.get().setAmountDue(invoice.get().getAmountDue() + invoicePayment.getAmount());
			this.invoiceRepository.save(invoice.get());

			/**
			 * calls only if type is make payment
			 */
			if (FormNames.MAKE_PAYMENT.getFormName().equalsIgnoreCase(type)) {
				List<MakePayment> MakePayments = new ArrayList<MakePayment>();
				MakePayments = this.makePaymentRepository.getByIdAndType(paymentId, type);
				for (MakePayment MakePayment : MakePayments) {
					MakePayment.setAmount(0.0);
					MakePayment.setStatus(TransactionStatus.VOID.getTransactionStatus());
					makePaymentRepository.save(MakePayment);
				}

				List<MakePaymentList> MakePaymentLists = new ArrayList<MakePaymentList>();
				MakePaymentLists = this.makePaymentListRepository.getByPaymentIdAndType(paymentId, type);
				for (MakePaymentList MakePaymentList : MakePaymentLists) {
					MakePaymentList.setPaidAmount(0.0);
					log.info(" make payment void set " + MakePaymentList);
				}
				MakePaymentLists = this.makePaymentListRepository.saveAll(MakePaymentLists);
			} else if (FormNames.ADVANCE_PAYMENT.getFormName().equalsIgnoreCase(type)) {
				// TODO confirm from Amar
			}
		}
		invoicePayments = this.invoicePaymentRepository.saveAll(invoicePayments);
		return invoicePayments;
	}

	@Override
	public @Valid MakePayment saveForAdvancePayment(@Valid MakePayment makePayment) {
		AdvancePayment advancePayment = new AdvancePayment();
		Optional<MakePayment> oldMakePayment = Optional.empty();
		Long paymentId = null;
		String paymentNumber = null;
		if (makePayment.getId() == null) {
			makePayment.setCreatedBy(CommonUtils.getLoggedInUsername());
			String transactionalDate = CommonUtils.convertDateToFormattedString(makePayment.getPaymentDate());

			String documentSequenceNumber = setupServiceClient.getDocumentSequenceNumber(transactionalDate,
					makePayment.getSubsidiaryId(), FormNames.MAKE_PAYMENT.getFormName(), false);
			if (StringUtils.isEmpty(documentSequenceNumber)) {
				throw new CustomMessageException(
						"Please validate your configuration to generate the Payment number Number");
			}
			makePayment.setPaymentNumber(documentSequenceNumber);
		} else {
			// Get the existing object using the deep copy
			oldMakePayment = this.makePaymentRepository.findByIdAndIsDeleted(makePayment.getId(), false);
			if (oldMakePayment.isPresent()) {
				try {
					oldMakePayment = Optional.ofNullable((MakePayment) oldMakePayment.get().clone());
				} catch (CloneNotSupportedException e) {
					log.error("Error while Cloning the object. Please contact administrator.");
					throw new CustomException("Error while Cloning the object. Please contact administrator.");
				}
			}
		}
		makePayment.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		MakePayment savedMakePayment = this.makePaymentRepository.save(makePayment);
		log.info(" Data is saved for Make Payment - " + makePayment);
		this.updateMakePaymentHistory(savedMakePayment, oldMakePayment);
		log.info(" Make Payment history is saved : " + oldMakePayment);
		paymentId = savedMakePayment.getId();
		paymentNumber = savedMakePayment.getPaymentNumber();

		// ------make payment list-----//
		List<MakePaymentList> makePaymentLists = makePayment.getMakePaymentList();

		if (CollectionUtils.isNotEmpty(makePaymentLists)) {
			for (MakePaymentList makePaymentList : makePaymentLists) {
				this.savedMakePaymentList(paymentId, makePaymentList, paymentNumber);
				advancePayment = this.advancePaymentRepository.findById(makePaymentList.getInvoiceId());
				if (advancePayment == null) {
					log.error("Advance Payment is not found against the provided value : "
							+ makePaymentList.getInvoiceId());
					throw new CustomException("Advance Payment is not found against the provided value : "
							+ makePaymentList.getInvoiceId());
				}
				advancePayment.setPaymentAmount(advancePayment.getPaymentAmount() + makePaymentList.getPaymentAmount());
				advancePayment.setUnappliedAmount(advancePayment.getPaymentAmount());
				advancePayment.setDueAmount(advancePayment.getDueAmount() - makePaymentList.getPaymentAmount());
			}
			System.gc();
			savedMakePayment = this.getMakePaymentById(savedMakePayment.getId());
		}
		return makePayment;
	}

	/**
	 * Save PO after the approval details change
	 * 
	 * @param makePayment
	 */
	private void savePaymentForApproval(MakePayment makePayment, Optional<MakePayment> oldMakePayment) {
		makePayment.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		makePayment = this.makePaymentRepository.save(makePayment);

		if (makePayment == null) {
			log.info("Error while saving the Make Payment after the Approval.");
			throw new CustomMessageException("Error while saving the Make Payment after the Approval.");
		}
		log.info("Make Payment saved successfully :: " + makePayment.getPaymentNumber());

		// update the data in PR history table
		this.updateMakePaymentHistory(makePayment, oldMakePayment);
		log.info("Make Payment history is updated");
	}

	/**
	 * Set/Prepares the approver details in the PO object
	 * 
	 * @param purchaseRequisition
	 * @param approvalRequest
	 */
	private void updateApproverDetailsInMakePayment(Optional<MakePayment> makePayment,
			ApprovalRequest approvalRequest) {
		makePayment.get().setApprovedBy(makePayment.get().getNextApprover());
		makePayment.get().setNextApprover(approvalRequest.getNextApprover());
		makePayment.get().setNextApproverRole(approvalRequest.getNextApproverRole());
		makePayment.get().setNextApproverLevel(approvalRequest.getNextApproverLevel());
	}

	private Optional<MakePayment> findOldDeepCopiedMakePayment(Long id) {
		Optional<MakePayment> oldMakePayment = this.makePaymentRepository.findByIdAndIsDeleted(id, false);
		if (oldMakePayment.isPresent()) {
			try {
				oldMakePayment = Optional.ofNullable((MakePayment) oldMakePayment.get().clone());
				log.info("Existing Make Payment is copied.");
			} catch (CloneNotSupportedException e) {
				log.error("Error while Cloning the object. Please contact administrator.");
				throw new CustomException("Error while Cloning the object. Please contact administrator.");
			}
		}
		return oldMakePayment;
	}

	public Optional<MakePayment> findById(Long id) {
		Optional<MakePayment> makePayment = Optional.empty();
		makePayment = this.makePaymentRepository.findByIdAndIsDeleted(id, false);

		if (!makePayment.isPresent()) {
			log.info("make Payment is not found against the provided makePayment-ID :: " + id);
			throw new CustomMessageException("make Payment is not found against the provided makePayment-ID :: " + id);
		}
		makePayment.get()
				.setApprovalRoutingActive(this.findIsApprovalRoutingActive(makePayment.get().getSubsidiaryId()));

		log.info("make Payment is found against the make Payment-ID :: " + id);
		return makePayment;
	}

	private boolean findIsApprovalRoutingActive(Long subsidiaryId) {

		return masterServiceClient.findIsApprovalRoutingActive(subsidiaryId, FormNames.PO.getFormName());
	}

	@Override
	public Boolean sendForApproval(Long id) {
		Boolean isSentForApproval = false;

		try {
			/**
			 * Due to single transaction we are getting updated value when we find from repo
			 * after the update hence finding old one first
			 */
			// Get the existing object using the deep copy
			Optional<MakePayment> oldMakePayment = this.findOldDeepCopiedMakePayment(id);

			Optional<MakePayment> makePayment = Optional.empty();
			makePayment = this.findById(id);

			/**
			 * Check routing is active or not
			 */
			boolean isRoutingActive = makePayment.get().isApprovalRoutingActive();
			if (!isRoutingActive) {
				log.error(
						"Routing is not active for the Make Payment : " + id + ". Please update your configuration. ");
				throw new CustomMessageException(
						"Routing is not active for the Make Payment : " + id + ". Please update your configuration. ");
			}

			Double transactionalAmount = makePayment.get().getAmount();
			log.info("Total estimated transaction amount for makePayment is :: " + transactionalAmount);

			// if amount is null then throw error
			if (transactionalAmount == null || transactionalAmount == 0.0) {
				log.error("There is no available Approval Process for this transaction.");
				throw new CustomMessageException("There is no available Approval Process for this transaction.");
			}

			ApprovalRequest approvalRequest = new ApprovalRequest();
			approvalRequest.setSubsidiaryId(makePayment.get().getSubsidiaryId());
			approvalRequest.setFormName(FormNames.MAKE_PAYMENT.getFormName());
			approvalRequest.setTransactionAmount(transactionalAmount);
			// TODO - location & Department is missing
			log.info("Approval object us prepared : " + approvalRequest.toString());

			/**
			 * method will give max level & it's sequence if match otherwise throw error
			 * message as no approver process exist if level or sequence id is null then
			 * also throws error message.
			 */

			ApprovalPreference approvalPreference = masterServiceClient.findApproverMaxLevel(approvalRequest);
			Long sequenceId = approvalPreference.getSequenceId();
			String level = approvalPreference.getLevel();
			Long approverPreferenceId = approvalPreference.getId();
			log.info("Max level & sequence is found :: " + approvalPreference.toString());

			makePayment.get().setApproverSequenceId(sequenceId);
			makePayment.get().setApproverMaxLevel(level);
			makePayment.get().setApproverPreferenceId(approverPreferenceId);

			String levelToFindRole = "L1";
			if (AppConstants.APPROVAL_TYPE_INDIVIDUAL.equals(approvalPreference.getApprovalType())) {
				levelToFindRole = level;
			}

			approvalRequest = masterServiceClient.findApproverByLevelAndSequence(approverPreferenceId, levelToFindRole,
					sequenceId);

			this.updateApproverDetailsInMakePayment(makePayment, approvalRequest);
			makePayment.get().setPaymentStatus(TransactionStatus.PENDING_APPROVAL.getTransactionStatus());
			log.info("Approver is found and details is updated for make Payment :: " + makePayment.get());

			this.savePaymentForApproval(makePayment.get(), oldMakePayment);
			log.info("make Payment is saved successfully with Approver details.");

			masterServiceClient.sendEmailByApproverId(makePayment.get().getNextApprover(),
					FormNames.MAKE_PAYMENT.getFormName());

			isSentForApproval = true;
		} catch (Exception e) {
			log.error("Error while sending make Payment for approval for id - " + id);
			e.printStackTrace();
			throw new CustomMessageException("Error while sending make Payment for approval for id - " + id
					+ ", Message : " + e.getLocalizedMessage());
		}

		return isSentForApproval;
	}

	@Override
	public Boolean approveAllPayments(List<Long> paymentIds) {
		Boolean isAllPaymentApproved = false;
		try {
			for (Long paymentId : paymentIds) {
				log.info("Approval Process is started for payment-id :: " + paymentId);

				/**
				 * Due to single transaction we are getting updated value when we find from repo
				 * after the update hence finding old one first
				 */
				// Get the existing object using the deep copy
				Optional<MakePayment> oldMakePayment = this.findOldDeepCopiedMakePayment(paymentId);

				Optional<MakePayment> makePayment = Optional.empty();
				makePayment = this.findById(paymentId);

				/**
				 * Check routing is active or not
				 */
				boolean isRoutingActive = makePayment.get().isApprovalRoutingActive();
				if (!isRoutingActive) {
					log.error("Routing is not active for the MakePayment : " + paymentId
							+ ". Please update your configuration. ");
					throw new CustomMessageException("Routing is not active for the MakePayment : " + paymentId
							+ ". Please update your configuration. ");
				}

				// meta data
				Long approvalPreferenceId = makePayment.get().getApproverPreferenceId();
				Long sequenceId = makePayment.get().getApproverSequenceId();
				String maxLevel = makePayment.get().getApproverMaxLevel();

				ApprovalRequest approvalRequest = new ApprovalRequest();

				if (!maxLevel.equals(makePayment.get().getNextApproverLevel())) {
					Long currentLevelNumber = Long
							.parseLong(makePayment.get().getNextApproverLevel().replaceFirst("L", "")) + 1;
					String currentLevel = "L" + currentLevelNumber;

					approvalRequest = masterServiceClient.findApproverByLevelAndSequence(approvalPreferenceId,
							currentLevel, sequenceId);
					makePayment.get().setPaymentStatus(TransactionStatus.PARTIALLY_APPROVED.getTransactionStatus());
				} else {
					makePayment.get().setPaymentStatus(TransactionStatus.APPROVED.getTransactionStatus());
				}
				log.info("Approval Request is found for MakePayment :: " + approvalRequest.toString());

				this.updateApproverDetailsInMakePayment(makePayment, approvalRequest);
				log.info("Approver is found and details is updated :: " + makePayment.get());

				this.savePaymentForApproval(makePayment.get(), oldMakePayment);
				log.info("MakePayment is saved successfully with Approver details.");

				masterServiceClient.sendEmailByApproverId(makePayment.get().getNextApprover(),
						FormNames.MAKE_PAYMENT.getFormName());
				log.info("Approval Process is Finished for MakePayment :: " + makePayment.get().getId());
			}

			isAllPaymentApproved = true;
		} catch (Exception e) {
			log.error("Error while approving the Make Payment.");
			e.printStackTrace();
			throw new CustomMessageException(
					"Error while approving the Make Payment. Message : " + e.getLocalizedMessage());
		}
		return isAllPaymentApproved;
	}

	@Override
	public Boolean rejectAllPayments(List<MakePayment> payments) {
		for (MakePayment payment : payments) {
			String rejectComments = payment.getRejectedComments();

			if (StringUtils.isEmpty(rejectComments)) {
				log.error("Reject Comments is required.");
				throw new CustomException(
						"Reject Comments is required. It is missing for Make Payment : " + payment.getId());
			}

			Optional<MakePayment> oldMakePayment = this.findOldDeepCopiedMakePayment(payment.getId());

			Optional<MakePayment> makePayment = this.makePaymentRepository.findByIdAndIsDeleted(payment.getId(), false);

			makePayment.get().setPaymentStatus(TransactionStatus.REJECTED.getTransactionStatus());
			makePayment.get().setRejectedComments(rejectComments);
			makePayment.get().setApprovedBy(null);
			makePayment.get().setNextApprover(null);
			makePayment.get().setNextApproverRole(null);
			makePayment.get().setNextApproverLevel(null);
			makePayment.get().setApproverSequenceId(null);
			makePayment.get().setApproverMaxLevel(null);
			makePayment.get().setApproverPreferenceId(null);
			makePayment.get().setNoteToApprover(null);

			log.info("Approval Fields are restored to empty. For Purchase Order : " + payment);

			this.savePaymentForApproval(makePayment.get(), oldMakePayment);
			log.info("Make Payment is saved successfully with restored Approver details.");

			log.info("Approval Process is Finished for Make Payment-id :: " + payment.getId());
		}
		return true;
	}

	@Override
	public Boolean selfApprove(Long paymentId) {
		Optional<MakePayment> makePayment = this.makePaymentRepository.findByIdAndIsDeleted(paymentId, false);

		if (!makePayment.isPresent()) {
			log.error("makePayment Not Found against given makePayment id : " + paymentId);
			throw new CustomMessageException("makePayment Not Found against given makePayment id : " + paymentId);
		}
		makePayment.get().setPaymentStatus(TransactionStatus.APPROVED.getTransactionStatus());
		makePayment.get().setLastModifiedBy(CommonUtils.getLoggedInUsername());

		if (this.makePaymentRepository.save(makePayment.get()) != null)
			return true;
		else
			throw new CustomException("Error in self approve. Please contact System Administrator");
	}

	@Override
	public List<Invoice> getPaymentDetailsBySupplierAndCurrency(Long supplierId, String currency) {
		List<Invoice> invoices = new ArrayList<Invoice>();
		invoices = this.invoiceRepository.findBySupplierIdAndCurrency(supplierId, currency);
		return invoices;
	}

	@Override
	public List<MakePayment> getPaymentApproval(String user) {
		List<String> status = new ArrayList<String>();
		status.add(TransactionStatus.PENDING_APPROVAL.getTransactionStatus());
		status.add(TransactionStatus.PARTIALLY_APPROVED.getTransactionStatus());
		List<MakePayment> makePayment = new ArrayList<MakePayment>();
		makePayment = makePaymentRepository.getPaymentForApproval(status, user);
		return makePayment;
	}

}
