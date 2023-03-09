package com.monstarbill.finance.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.monstarbill.finance.common.AppConstants;
import com.monstarbill.finance.common.CommonUtils;
import com.monstarbill.finance.common.CustomException;
import com.monstarbill.finance.common.CustomMessageException;
import com.monstarbill.finance.common.ExcelHelper;
import com.monstarbill.finance.common.FilterNames;
import com.monstarbill.finance.enums.FormNames;
import com.monstarbill.finance.enums.Operation;
import com.monstarbill.finance.enums.TransactionStatus;
import com.monstarbill.finance.feignclients.MasterServiceClient;
import com.monstarbill.finance.feignclients.ProcureServiceClient;
import com.monstarbill.finance.feignclients.SetupServiceClient;
import com.monstarbill.finance.models.Grn;
import com.monstarbill.finance.models.GrnItem;
import com.monstarbill.finance.models.Invoice;
import com.monstarbill.finance.models.InvoiceHistory;
import com.monstarbill.finance.models.InvoiceItem;
import com.monstarbill.finance.models.InvoicePayment;
import com.monstarbill.finance.models.Item;
import com.monstarbill.finance.models.Location;
import com.monstarbill.finance.models.PurchaseOrderItem;
import com.monstarbill.finance.models.Subsidiary;
import com.monstarbill.finance.models.Supplier;
import com.monstarbill.finance.models.SupplierAddress;
import com.monstarbill.finance.models.TaxGroup;
import com.monstarbill.finance.payload.request.ApprovalRequest;
import com.monstarbill.finance.payload.request.PaginationRequest;
import com.monstarbill.finance.payload.response.ApprovalPreference;
import com.monstarbill.finance.payload.response.PaginationResponse;
import com.monstarbill.finance.repository.InvoiceHistoryRepository;
import com.monstarbill.finance.repository.InvoiceItemRepository;
import com.monstarbill.finance.repository.InvoicePaymentRepository;
import com.monstarbill.finance.repository.InvoiceRepository;
import com.monstarbill.finance.service.InvoiceService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InvoiceServiceImpl implements InvoiceService {

	@Autowired
	private SetupServiceClient setupServiceClient;

	@Autowired
	private MasterServiceClient masterServiceClient;

	@Autowired
	private ProcureServiceClient procureServiceClient;

	@Autowired
	private InvoiceRepository invoiceRepository;

	@Autowired
	private InvoiceItemRepository invoiceItemRepository;

	@Autowired
	private InvoiceHistoryRepository invoiceHistoryRepository;

	@Autowired
	private InvoicePaymentRepository invoicePaymentRepository;

	@Override
	@Transactional
	public Invoice saveInvoice(Invoice invoice) {
		Invoice invoiceSaved;
		Optional<Invoice> oldInvoice = Optional.empty();
		Long invoiceId = invoice.getInvoiceId();
		if (invoiceId == null) {
			invoice.setInvStatus(TransactionStatus.OPEN.getTransactionStatus());
			invoice.setTotalPaidAmount(0.0);
			invoice.setAmountDue(invoice.getTotalAmount());
			invoice.setCreatedBy(CommonUtils.getLoggedInUsername());
			String transactionalDate = CommonUtils.convertDateToFormattedString(invoice.getInvoiceDate());
			String documentSequenceNumber = this.setupServiceClient.getDocumentSequenceNumber(transactionalDate,
					invoice.getSubsidiaryId(), AppConstants.INVOICE, false);
			if (StringUtils.isEmpty(documentSequenceNumber)) {
				throw new CustomMessageException("Please validate your configuration to generate the Invoice Number");
			}
			invoice.setInvoiceCode(documentSequenceNumber);
		} else {
			oldInvoice = invoiceRepository.findById(invoiceId);
			// Get the existing object using the deep copy
			if (oldInvoice.isPresent()) {
				try {
					oldInvoice = Optional.ofNullable((Invoice) oldInvoice.get().clone());
				} catch (CloneNotSupportedException e) {
					log.error("Error while Cloning the object. Please contact administrator.");
					throw new CustomException("Error while Cloning the object. Please contact administrator.");
				}
			}
		}
		
		invoice.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		log.info("Save invoice started.");
		try {
			invoiceSaved = invoiceRepository.save(invoice);
		} catch (DataAccessException e) {
			log.error("Error while saving the Invoice :: " + e.getMostSpecificCause());
			throw new CustomException("Error while saving the Invoice: " + e.getMostSpecificCause());
		}
		log.info("Saved invoice: " + invoiceSaved);
		updateInvoiceHistory(invoiceSaved, oldInvoice);
		
		log.info("Invoice Items are started.");
		
		Set<Long> grnIds = new TreeSet<Long>();
		boolean isGrnBasedInvoice = false;
		
		List<InvoiceItem> invoiceItems = invoice.getInvoiceItems();
		if (CollectionUtils.isNotEmpty(invoiceItems)) {
			List<InvoiceItem> invoiceItemsSaved = new ArrayList<>();
			for(InvoiceItem invoiceItem: invoiceItems) {
				invoiceItem.setInvoiceId(invoiceSaved.getInvoiceId());
				InvoiceItem invoiceItemSaved = saveInvoiceItem(invoiceItem);
				invoiceItemsSaved.add(invoiceItemSaved);
				
				if (invoiceItem.getPoId() != null && invoiceItem.getGrnId() != null) {
					isGrnBasedInvoice = true;
					grnIds.add(invoiceItem.getGrnId());
				}
			};
			invoiceSaved.setInvoiceItems(invoiceItemsSaved);
			
			/**
			 * execute below code only if Invoice is PO+GRN Based
			 */
			if (isGrnBasedInvoice) {
				log.info("Grn header level status update started.");
				List<Grn> grns = new ArrayList<Grn>();
				for (Long grnId : grnIds) {
					Grn grn = this.procureServiceClient.findGrnByGrnId(grnId);
					Boolean isProcessed = this.procureServiceClient.isGrnFullyProcessed(grnId);
					String status = TransactionStatus.PARTIALLY_BILLED.getTransactionStatus();
					if (isProcessed) {
						// if (!TransactionStatus.PARTIALLY_RETURN.getTransactionStatus().equalsIgnoreCase(grn.getRtvStatus())) {
							status = TransactionStatus.BILLED.getTransactionStatus();
						// }
					}
					
					grn.setStatus(status);
					grn.setBillStatus(status);
					grns.add(grn);
				}
				this.procureServiceClient.saveGrn(grns);
				log.info("Grn header level status update Finished.");
			}
		}
		log.info("Invoice Items are Finished.");
		return invoiceSaved;
	}

	@Transactional
	private InvoiceItem saveInvoiceItem(InvoiceItem invoiceItem) {
		PurchaseOrderItem purchaseOrderItem = new PurchaseOrderItem();
		GrnItem grnitem = new GrnItem();
		grnitem = this.procureServiceClient.findGrnItemByGrnIdAndItemId(invoiceItem.getGrnId(), invoiceItem.getItemId());
		purchaseOrderItem = this.procureServiceClient.getByPoItemId(invoiceItem.getPoId(), invoiceItem.getItemId());
		Double unbilledQuantity = 0.0;
		InvoiceItem invoiceItemSaved = null;
		Optional<InvoiceItem> oldInvoiceItem = Optional.empty();
		Long invoiceItemId = invoiceItem.getInvoiceItemId();
		
//		Double remainingGrnQuantity = 0.0;
		if (invoiceItemId == null) {
			invoiceItem.setCreatedBy(CommonUtils.getLoggedInUsername());
			/**
			 * Scenario - Invoice - PO Without GRN
			 */
			if (invoiceItem.getPoId() != null && invoiceItem.getGrnId() == null) {
				purchaseOrderItem.setUnbilledQuantity(purchaseOrderItem.getUnbilledQuantity() - invoiceItem.getBillQty());
				if (purchaseOrderItem.getUnbilledQuantity() == 0) {
					purchaseOrderItem.setStatus(TransactionStatus.BILLED.getTransactionStatus());
				} else {
					purchaseOrderItem.setStatus(TransactionStatus.PARTIALLY_BILLED.getTransactionStatus());
				}
				this.procureServiceClient.savePoItem(purchaseOrderItem);
			}
			/**
			 * Scenario - Invoice - PO With GRN
			 */
			if (invoiceItem.getPoId() != null && invoiceItem.getGrnId() != null) {
				grnitem.setUnbilledQuantity(grnitem.getUnbilledQuantity() - invoiceItem.getBillQty());
				if (grnitem.getUnbilledQuantity() == 0) {
					grnitem.setStatus(TransactionStatus.BILLED.getTransactionStatus());
					grnitem.setBillStatus(TransactionStatus.BILLED.getTransactionStatus());
				} else {
					grnitem.setStatus(TransactionStatus.PARTIALLY_BILLED.getTransactionStatus());
					grnitem.setBillStatus(TransactionStatus.PARTIALLY_BILLED.getTransactionStatus());
				}
				this.procureServiceClient.saveGrnItemObject(grnitem);
			}
		} else {
			// Get the existing object using the deep copy
			oldInvoiceItem = invoiceItemRepository.findById(invoiceItemId);
			if (oldInvoiceItem.isPresent()) {
				try {
					oldInvoiceItem = Optional.ofNullable((InvoiceItem) oldInvoiceItem.get().clone());
				} catch (CloneNotSupportedException e) {
					log.error("Error while Cloning the object. Please contact administrator.");
					throw new CustomException("Error while Cloning the object. Please contact administrator.");
				}
			}
			
			Double newQuantity = invoiceItem.getBillQty();
			Double oldQuantity = oldInvoiceItem.get().getBillQty();
			Double difference = newQuantity - oldQuantity;
			/**
			 * Scenario - Invoice - PO Without GRN
			 */
			if (invoiceItem.getPoId() != null && invoiceItem.getGrnId() == null) {
				unbilledQuantity = purchaseOrderItem.getUnbilledQuantity() - difference;
				if (unbilledQuantity < 0) {
					throw new CustomException("Billed quantity should be less than or equals to Unbilled quantity.");
				}
				purchaseOrderItem.setUnbilledQuantity(unbilledQuantity);
				if (purchaseOrderItem.getUnbilledQuantity() == 0) {
					purchaseOrderItem.setStatus(TransactionStatus.BILLED.getTransactionStatus());
					grnitem.setBillStatus(TransactionStatus.BILLED.getTransactionStatus());
				} else {
					purchaseOrderItem.setStatus(TransactionStatus.PARTIALLY_BILLED.getTransactionStatus());
					grnitem.setBillStatus(TransactionStatus.PARTIALLY_BILLED.getTransactionStatus());
				}
				this.procureServiceClient.savePoItem(purchaseOrderItem);
			}
			
			/**
			 * Scenario - Invoice - PO With GRN
			 */
			if (invoiceItem.getPoId() != null && invoiceItem.getGrnId() != null) {
				unbilledQuantity = grnitem.getUnbilledQuantity() - difference;
				grnitem.setUnbilledQuantity(unbilledQuantity);
				if (unbilledQuantity < 0) {
					throw new CustomException("Billed quantity should be less than or equals to Unbilled quantity.");
				}
				if (grnitem.getUnbilledQuantity() == 0) {
					grnitem.setStatus(TransactionStatus.BILLED.getTransactionStatus());
					grnitem.setBillStatus(TransactionStatus.BILLED.getTransactionStatus());
				} else {
					grnitem.setStatus(TransactionStatus.PARTIALLY_BILLED.getTransactionStatus());
					grnitem.setBillStatus(TransactionStatus.PARTIALLY_BILLED.getTransactionStatus());
				}
				this.procureServiceClient.saveGrnItemObject(grnitem);
				log.info("Grn Item status is updated");
			}
		}
		
		invoiceItem.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		log.info("Save invoice Item started.");
		try {
			invoiceItemSaved = invoiceItemRepository.save(invoiceItem);
		} catch (DataAccessException e) {
			log.error("Error while saving the Invoice Item :: " + e.getMostSpecificCause());
			throw new CustomException("Error while saving the Invoice Item: " + e.getMostSpecificCause());
		}
		log.info("Saved invoice Item: " + invoiceItemSaved);
		updateInvoiceItemHistory(invoiceItemSaved, oldInvoiceItem);

		return invoiceItemSaved;

	}

	/**
	 * This method save the data in history table Add entry as a Insert if Invoice
	 * is new Add entry as a Update if Invoice is exists
	 * 
	 * @param invoice
	 * @param oldInvoice
	 */
	@Transactional
	private void updateInvoiceHistory(Invoice invoice, Optional<Invoice> oldInvoice) {
		log.info("Invoice History is started.");
		if (oldInvoice.isPresent()) {
			// insert the updated fields in history table
			List<InvoiceHistory> invoiceHistories = new ArrayList<>();
			try {
				invoiceHistories = oldInvoice.get().compareFields(invoice);
				if (CollectionUtils.isNotEmpty(invoiceHistories)) {
					this.invoiceHistoryRepository.saveAll(invoiceHistories);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				log.error("Error while comparing the new and old objects. Please contact administrator.");
				throw new CustomException(
						"Error while comparing the new and old objects. Please contact administrator.");
			}
			log.info("Invoice History is updated successfully");
		} else {
			// Insert in history table as Operation - INSERT
			this.invoiceHistoryRepository
					.save(this.prepareInvoiceHistory(invoice.getInvoiceId(), null, AppConstants.INVOICE,
							Operation.CREATE.toString(), invoice.getLastModifiedBy(), null, invoice.getInvoiceNo()));
		}
		log.info("Invoice History is Completed.");
	}

	@Transactional
	private void updateInvoiceItemHistory(InvoiceItem invoiceItem, Optional<InvoiceItem> oldInvoiceItem) {
		log.info("Invoice Item History is started.");
		if (oldInvoiceItem.isPresent()) {
			// insert the updated fields in history table
			List<InvoiceHistory> invoiceHistories = new ArrayList<>();
			try {
				invoiceHistories = oldInvoiceItem.get().compareFields(invoiceItem);
				if (CollectionUtils.isNotEmpty(invoiceHistories)) {
					this.invoiceHistoryRepository.saveAll(invoiceHistories);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				log.error("Error while comparing the new and old objects. Please contact administrator.");
				throw new CustomException(
						"Error while comparing the new and old objects. Please contact administrator.");
			}
			log.info("Invoice Item History is updated successfully");
		} else {
			// Insert in history table as Operation - INSERT
			this.invoiceHistoryRepository.save(this.prepareInvoiceHistory(invoiceItem.getInvoiceId(),
					invoiceItem.getInvoiceItemId(), AppConstants.INVOICE_ITEM, Operation.CREATE.toString(),
					invoiceItem.getLastModifiedBy(), null, String.valueOf(invoiceItem.getInvoiceItemId())));
		}
		log.info("Invoice Item History is Completed.");
	}

	/**
	 * Prepares the Invoice history object
	 * 
	 * @param invoiceId
	 * @param childId
	 * @param moduleName
	 * @param operation
	 * @param lastModifiedBy
	 * @param oldValue
	 * @param newValue
	 * @return
	 */
	public InvoiceHistory prepareInvoiceHistory(Long invoiceId, Long childId, String moduleName, String operation,
			String lastModifiedBy, String oldValue, String newValue) {
		InvoiceHistory invoiceHistory = new InvoiceHistory();
		invoiceHistory.setInvoiceId(invoiceId);
		invoiceHistory.setChildId(childId);
		invoiceHistory.setModuleName(moduleName);
		invoiceHistory.setChangeType(AppConstants.UI);
		invoiceHistory.setOperation(operation);
		invoiceHistory.setOldValue(oldValue);
		invoiceHistory.setNewValue(newValue);
		invoiceHistory.setLastModifiedBy(lastModifiedBy);
		return invoiceHistory;
	}

	@Override
	public PaginationResponse getInvoices(PaginationRequest paginationRequest) {
		List<Invoice> invoices = null;
		log.info("Get all invoices started.");
		Specification<Invoice> specification = new Specification<Invoice>() {
			@Override
			public Predicate toPredicate(Root<Invoice> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				Long subsidiaryId = null, supplierId = null;
				String fromDate = null;
				String toDate = null;
				Map<String, ?> filters = paginationRequest.getFilters();
				if (filters.containsKey(FilterNames.SUBSIDIARY_ID))
					subsidiaryId = ((Number) filters.get(FilterNames.SUBSIDIARY_ID)).longValue();
				if (filters.containsKey(FilterNames.SUPPLIER_ID))
					supplierId = ((Number) filters.get(FilterNames.SUPPLIER_ID)).longValue();
				if (filters.containsKey(FilterNames.FROM_DATE))
					fromDate = (String) filters.get(FilterNames.FROM_DATE);
				if (filters.containsKey(FilterNames.TO_DATE))
					toDate = (String) filters.get(FilterNames.TO_DATE);
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (subsidiaryId != null && subsidiaryId != 0)
					predicates.add(criteriaBuilder.equal(root.get("subsidiaryId"), subsidiaryId));
				if (supplierId != null && supplierId != 0)
					predicates.add(criteriaBuilder.equal(root.get("supplierId"), supplierId));
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				try {
					if (fromDate != null && !fromDate.isEmpty()) {
						predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.<Date>get("invoiceDate"),
								sdf.parse(fromDate)));
					}
					if (toDate != null && !toDate.isEmpty()) {
						predicates.add(
								criteriaBuilder.lessThanOrEqualTo(root.<Date>get("invoiceDate"), sdf.parse(toDate)));
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				return criteriaBuilder.and(predicates.toArray(new Predicate[] {}));
			}
		};
		Pageable pageable = null;
		if (paginationRequest.getSortColumn().equals("supplierName")
				|| paginationRequest.getSortColumn().equals("subsidiaryName")) {
			pageable = PageRequest.of(paginationRequest.getPageNumber(), paginationRequest.getPageSize());
		} else {
			Sort sort = paginationRequest.getSortOrder().equalsIgnoreCase("asc")
					? Sort.by(paginationRequest.getSortColumn()).ascending()
					: Sort.by(paginationRequest.getSortColumn()).descending();
			pageable = PageRequest.of(paginationRequest.getPageNumber(), paginationRequest.getPageSize(), sort);
		}
		Page<Invoice> pgInvoices = invoiceRepository.findAll(specification, pageable);
		if (pgInvoices.hasContent())
			invoices = pgInvoices.getContent();
		if (CollectionUtils.isNotEmpty(invoices))
			invoices.forEach(invoice -> {
				Subsidiary optSubsidiary = setupServiceClient.getSubsidiaryById(invoice.getSubsidiaryId());
				if (optSubsidiary != null)
					invoice.setSubsidiaryName(optSubsidiary.getName());
				Supplier supOptional = masterServiceClient.findSupplierById(invoice.getSupplierId());
				if (supOptional != null)
					invoice.setSupplierName(supOptional.getName());
			});
		if (paginationRequest.getSortColumn().equals("supplierName")) {
			invoices = new ArrayList<Invoice>(invoices);
			Collections.sort(invoices, new Comparator<Invoice>() {

				@Override
				public int compare(Invoice o1, Invoice o2) {
					if (paginationRequest.getSortOrder().equalsIgnoreCase("asc"))
						return o1.getSupplierName().compareTo(o2.getSupplierName());
					else
						return o2.getSupplierName().compareTo(o1.getSupplierName());
				}
			});
		} else if (paginationRequest.getSortColumn().equals("subsidiaryName")) {
			invoices = new ArrayList<Invoice>(invoices);
			Collections.sort(invoices, new Comparator<Invoice>() {

				@Override
				public int compare(Invoice o1, Invoice o2) {
					if (paginationRequest.getSortOrder().equalsIgnoreCase("asc"))
						return o1.getSubsidiaryName().compareTo(o2.getSubsidiaryName());
					else
						return o2.getSubsidiaryName().compareTo(o1.getSubsidiaryName());
				}
			});
		}
		log.info("Get all invoices: " + invoices);
		Long totalRecords = invoiceRepository.count(specification);
		return CommonUtils.setPaginationResponse(paginationRequest.getPageNumber(), paginationRequest.getPageSize(),
				invoices, totalRecords);
	}

	@Override
	public List<Invoice> findBySubsidiaryAndSuppplier(Long subsidiaryId, Long supplierId) {
		List<Invoice> invoices = new ArrayList<Invoice>();
		invoices = this.invoiceRepository.getAllInvoiceBySubsidiaryIdAndSupplierId(subsidiaryId, supplierId);
		log.info("Get all invoice by subsidiaryId and supplierId ." + invoices);
		return invoices;
	}

	@Override
	public Invoice getInvoice(Long invoiceId) {

		Invoice invoice = null;
		log.info("Get invoice by id started.");
		Optional<Invoice> optInvoice = invoiceRepository.findById(invoiceId);
		if (optInvoice.isPresent()) {
			invoice = optInvoice.get();
			List<InvoiceItem> invoiceItems = invoiceItemRepository.findByInvoiceId(invoice.getInvoiceId());
			List<InvoicePayment> invoicePayment = invoicePaymentRepository.findByInvoiceId(invoice.getInvoiceId());
			invoice.setInvoiceItems(invoiceItems);
			invoice.setInvoicePayments(invoicePayment);
		}
		log.info("Get invoice: " + invoice);

		return invoice;
	}

	@Override
	public List<InvoiceHistory> getInvoiceHistory(Long invoiceId, Pageable pageable) {
		List<InvoiceHistory> invoiceHistories = null;
		log.info("Get History by Invoice id started.");
		Page<InvoiceHistory> pgInvoiceHistories = invoiceHistoryRepository.findByInvoiceId(invoiceId, pageable);
		if (pgInvoiceHistories.hasContent())
			invoiceHistories = pgInvoiceHistories.getContent();
		log.info("Get Invoice History: " + invoiceHistories);
		return invoiceHistories;
	}

	@Override
	public List<InvoicePayment> saveInvoicePayment(List<InvoicePayment> invoicePayments) {
		for (InvoicePayment invoicePayment : invoicePayments) {
			Optional<Invoice> invoice = Optional.empty();
			if (invoicePayment.getInvoicePaymentId() == null) {
				invoicePayment.setCreatedBy(CommonUtils.getLoggedInUsername());
			}
			invoicePayment.setModifiedBy(CommonUtils.getLoggedInUsername());
			invoice = this.invoiceRepository.findByInvoiceId(invoicePayment.getInvoiceId());
			if (!invoice.isPresent()) {
				log.error(" Invoice is not created for id : " + invoicePayment.getInvoiceId());
				throw new CustomException(" Invoice is not created for id : " + invoicePayment.getInvoiceId());
			}
			if (invoice.get().getAmountDue() == 0.0) {
				log.error(" There is no due for the invoice id : " + invoicePayment.getInvoiceId());
				throw new CustomException(" There is no due for the invoice id : " + invoicePayment.getInvoiceId());
			}
			Double remainDueAmount = 0.0;
			remainDueAmount = this.invoicePaymentRepository
					.findTotalAmountByInvoiceIdAndIsDeleted(invoicePayment.getInvoiceId(), false);
			if (remainDueAmount == null) {
				remainDueAmount = 0.0;
			}
			remainDueAmount = invoicePayment.getAmount() + remainDueAmount;
			log.info("total amount due " + remainDueAmount);
			invoice.get().setTotalPaidAmount(remainDueAmount);
			invoice.get().setAmountDue(invoice.get().getTotalAmount() - invoice.get().getTotalPaidAmount());
			// log.info("Saved invoice payment : " + savedInvoicePayment);
			this.invoiceRepository.save(invoice.get());
			InvoicePayment savedInvoicePayment = this.invoicePaymentRepository.save(invoicePayment);
			log.info("Saved invoice payment : " + savedInvoicePayment);
		}
		return invoicePayments;
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
			Optional<Invoice> oldInvoice = this.findOldDeepCopiedInvoice(id);

			Optional<Invoice> invoice = Optional.empty();
			invoice = this.findById(id);

			/**
			 * Check routing is active or not
			 */
			boolean isRoutingActive = invoice.get().isApprovalRoutingActive();
			if (!isRoutingActive) {
				log.error("Routing is not active for the Invoice : " + id + ". Please update your configuration. ");
				throw new CustomMessageException(
						"Routing is not active for the Invoice : " + id + ". Please update your configuration. ");
			}

			Double transactionalAmount = invoice.get().getTotalAmount();
			log.info("Total estimated transaction amount for Invoice is :: " + transactionalAmount);

			// if amount is null then throw error
			if (transactionalAmount == null || transactionalAmount == 0.0) {
				log.error("There is no available Approval Process for this transaction.");
				throw new CustomMessageException("There is no available Approval Process for this transaction.");
			}

			ApprovalRequest approvalRequest = new ApprovalRequest();
			approvalRequest.setSubsidiaryId(invoice.get().getSubsidiaryId());
			approvalRequest.setFormName(FormNames.INVOICE.getFormName());
			approvalRequest.setTransactionAmount(transactionalAmount);
			approvalRequest.setLocationId(invoice.get().getLocationId());
			// TODO department
			log.info("Approval object us prepared : " + approvalRequest.toString());

			/**
			 * method will give max level & it's sequence if match otherwise throw error
			 * message as no approver process exist if level or sequence id is null then
			 * also throws error message.
			 */
			ApprovalPreference approvalPreference = this.masterServiceClient.findApproverMaxLevel(approvalRequest);
			Long sequenceId = approvalPreference.getSequenceId();
			String level = approvalPreference.getLevel();
			Long approverPreferenceId = approvalPreference.getId();
			log.info("Max level & sequence is found :: " + approvalPreference.toString());

			invoice.get().setApproverSequenceId(sequenceId);
			invoice.get().setApproverMaxLevel(level);
			invoice.get().setApproverPreferenceId(approverPreferenceId);

			String levelToFindRole = "L1";
			if (AppConstants.APPROVAL_TYPE_INDIVIDUAL.equals(approvalPreference.getApprovalType())) {
				levelToFindRole = level;
			}
			approvalRequest = this.masterServiceClient.findApproverByLevelAndSequence(approverPreferenceId,
					levelToFindRole, sequenceId);

			this.updateApproverDetailsInInvoice(invoice, approvalRequest);
			invoice.get().setInvStatus(TransactionStatus.PENDING_APPROVAL.getTransactionStatus());
			log.info("Approver is found and details is updated for Invoice :: " + invoice.get());

			this.saveInvoiceForApproval(invoice.get(), oldInvoice);
			log.info("Invoice is saved successfully with Approver details.");

			masterServiceClient.sendEmailByApproverId(invoice.get().getNextApprover(), FormNames.INVOICE.getFormName());

			isSentForApproval = true;
		} catch (Exception e) {
			log.error("Error while sending PR for approval for id - " + id);
			e.printStackTrace();
			throw new CustomMessageException(
					"Error while sending PO for approval for id - " + id + ", Message : " + e.getLocalizedMessage());
		}

		return isSentForApproval;
	}

	/**
	 * Save Invoice after the approval details change
	 * 
	 * @param invoice
	 */
	private void saveInvoiceForApproval(Invoice invoice, Optional<Invoice> oldInvoice) {
		invoice.setLastModifiedBy(CommonUtils.getLoggedInUsername());
		invoice = this.invoiceRepository.save(invoice);

		if (invoice == null) {
			log.info("Error while saving the Invoice after the Approval.");
			throw new CustomMessageException("Error while saving the Invoice after the Approval.");
		}
		log.info("Invoice saved successfully :: " + invoice.getInvoiceNo());

		// update the data in Invoice history table
		this.updateInvoiceHistory(invoice, oldInvoice);
		log.info("Invoice history is updated");
	}

	/**
	 * Set/Prepares the approver details in the Invoice object
	 * 
	 * @param Invoice
	 * @param approvalRequest
	 */
	private void updateApproverDetailsInInvoice(Optional<Invoice> invoice, ApprovalRequest approvalRequest) {
		invoice.get().setApprovedBy(invoice.get().getNextApprover());
		invoice.get().setNextApprover(approvalRequest.getNextApprover());
		invoice.get().setNextApproverRole(approvalRequest.getNextApproverRole());
		invoice.get().setNextApproverLevel(approvalRequest.getNextApproverLevel());
	}

	private Optional<Invoice> findOldDeepCopiedInvoice(Long id) {
		Optional<Invoice> invoice = this.invoiceRepository.findByInvoiceId(id);
		if (invoice.isPresent()) {
			try {
				invoice = Optional.ofNullable((Invoice) invoice.get().clone());
				log.info("Existing Invoice is copied.");
			} catch (CloneNotSupportedException e) {
				log.error("Error while Cloning the object. Please contact administrator.");
				throw new CustomException("Error while Cloning the object. Please contact administrator.");
			}
		}
		return invoice;
	}

	/**
	 * Find PO by it's ID
	 * 
	 * @param id
	 * @return
	 */
	public Optional<Invoice> findById(Long id) {
		Optional<Invoice> invoice = Optional.empty();
		invoice = this.invoiceRepository.findByInvoiceId(id);

		if (!invoice.isPresent()) {
			log.info("Invoice is not found against the provided Invoice-ID :: " + id);
			throw new CustomMessageException("Invoice is not found against the provided Invoice-ID :: " + id);
		}
		invoice.get().setApprovalRoutingActive(this.findIsApprovalRoutingActive(invoice.get().getSubsidiaryId()));
		log.info("Invoice is found against the Invoice-ID :: " + id);
		return invoice;
	}

	private boolean findIsApprovalRoutingActive(Long subsidiaryId) {
		return this.masterServiceClient.findIsApprovalRoutingActive(subsidiaryId, FormNames.INVOICE.getFormName());
	}

	@Override
	public Boolean approveAllInvoices(List<Long> invoiceIds) {
		Boolean isAllPoApproved = false;
		try {
			for (Long poId : invoiceIds) {
				log.info("Approval Process is started for po-id :: " + poId);

				/**
				 * Due to single transaction we are getting updated value when we find from repo
				 * after the update hence finding old one first
				 */
				// Get the existing object using the deep copy
				Optional<Invoice> oldInvoice = this.findOldDeepCopiedInvoice(poId);

				Optional<Invoice> invoice = Optional.empty();
				invoice = this.findById(poId);

				/**
				 * Check routing is active or not
				 */
				boolean isRoutingActive = invoice.get().isApprovalRoutingActive();
				if (!isRoutingActive) {
					log.error(
							"Routing is not active for the Invoice : " + poId + ". Please update your configuration. ");
					throw new CustomMessageException(
							"Routing is not active for the Invoice : " + poId + ". Please update your configuration. ");
				}

				// meta data
				Long approvalPreferenceId = invoice.get().getApproverPreferenceId();
				Long sequenceId = invoice.get().getApproverSequenceId();
				String maxLevel = invoice.get().getApproverMaxLevel();

				ApprovalRequest approvalRequest = new ApprovalRequest();

				if (!maxLevel.equals(invoice.get().getNextApproverLevel())) {
					Long currentLevelNumber = Long.parseLong(invoice.get().getNextApproverLevel().replaceFirst("L", ""))
							+ 1;
					String currentLevel = "L" + currentLevelNumber;
					approvalRequest = this.masterServiceClient.findApproverByLevelAndSequence(approvalPreferenceId,
							currentLevel, sequenceId);
					invoice.get().setInvStatus(TransactionStatus.PARTIALLY_APPROVED.getTransactionStatus());
				} else {
					invoice.get().setInvStatus(TransactionStatus.APPROVED.getTransactionStatus());
				}
				log.info("Approval Request is found for Invoice :: " + approvalRequest.toString());

				this.updateApproverDetailsInInvoice(invoice, approvalRequest);
				log.info("Approver is found and details is updated :: " + invoice.get());

				this.saveInvoiceForApproval(invoice.get(), oldInvoice);
				log.info("Invoice is saved successfully with Approver details.");

				masterServiceClient.sendEmailByApproverId(invoice.get().getNextApprover(),
						FormNames.INVOICE.getFormName());
				log.info("Approval Process is Finished for Invoice :: " + invoice.get().getInvoiceNo());
			}

			isAllPoApproved = true;
		} catch (Exception e) {
			log.error("Error while approving the Invoice.");
			e.printStackTrace();
			throw new CustomMessageException("Error while approving the Invoice. Message : " + e.getLocalizedMessage());
		}
		return isAllPoApproved;
	}

	@Override
	public byte[] downloadTemplate() {
		DefaultResourceLoader loader = new DefaultResourceLoader();
		try {
			File is = loader.getResource("classpath:/templates/invoice_template.xlsx").getFile();
			return Files.readAllBytes(is.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public byte[] upload(MultipartFile file) {
		try {
			return this.importPrsFromExcel(file);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(
					"Something went wrong. Please Contact Administrator. Error : " + e.getLocalizedMessage());
		}
	}

	private byte[] importPrsFromExcel(MultipartFile inputFile) {
		try {
			InputStream inputStream = inputFile.getInputStream();
			@SuppressWarnings("resource")
			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheet("Invoice");
			Iterator<Row> rows = sheet.iterator();

			int statusColumnNumber = 0;
			int rowNumber = 0;

			boolean isError = false;
			StringBuilder errorMessage = new StringBuilder();

			Map<String, Invoice> invoiceMapping = new TreeMap<String, Invoice>();

			while (rows.hasNext()) {
				isError = false;
				errorMessage = new StringBuilder();
				int errorCount = 1;
				Row inputCurrentRow = rows.next();
				if (rowNumber == 0) {
					statusColumnNumber = inputCurrentRow.getLastCellNum();
					Cell cell = inputCurrentRow.createCell(statusColumnNumber);
					cell.setCellValue("Imported Status");
					rowNumber++;
					continue;
				}

				boolean isRowEmpty = ExcelHelper.checkIfRowIsEmpty(inputCurrentRow);

				// if row is empty it means all records completed.
				if (isRowEmpty)
					break;

				Invoice invoice = new Invoice();
				String externalId = null;

				// External ID - REQUIRED
				try {
					if (inputCurrentRow.getCell(0) != null) {
						externalId = new DataFormatter().formatCellValue(inputCurrentRow.getCell(0));
					} else {
						log.error("External ID should not be empty.");
						continue;
					}
				} catch (Exception e) {
					log.error("Exception External ID " + e.getLocalizedMessage());
					errorMessage.append(errorCount + ") Value of External ID is invalid.");
					isError = true;
					errorCount++;
					throw new CustomException("External ID should not be empty.");
				}
				// ----------------- Invoice header fields STARTED -----------------------
				if (invoiceMapping.containsKey(externalId)) {
					invoice = invoiceMapping.get(externalId);
				}
				invoice.setExternalId(externalId);

				// Invoice number - REQUIRED
				try {
					if (inputCurrentRow.getCell(1) != null) {
						invoice.setInvoiceNo(inputCurrentRow.getCell(1).getStringCellValue());
					} else {
						errorMessage.append(errorCount + ") Invoice number is required.");
						log.error("Invoice number is required.");
						isError = true;
						errorCount++;
					}
				} catch (Exception e) {
					log.error("Exception Invoice number " + e.getLocalizedMessage());
					errorMessage.append(errorCount + ") Value of Invoice number is invalid.");
					isError = true;
					errorCount++;
				}
				// Subsidiary - REQUIRED
				try {
					if (inputCurrentRow.getCell(2) != null) {
						String name = inputCurrentRow.getCell(2).getStringCellValue();
						Long subsidiaryId = this.setupServiceClient.getSubsidiaryIdByName(name);
						if (subsidiaryId == null) {
							errorMessage.append(errorCount + ") Subsidiary : " + name
									+ " is not found Please enter the valid Subsidiary Name. ");
							log.error(
									"Subsidiary : " + name + " is not found. Please enter the valid Subsidiary Name. ");
							isError = true;
							errorCount++;
						} else {
							invoice.setSubsidiaryId(subsidiaryId);
							invoice.setCurrency(this.setupServiceClient.findCurrencyBySubsidiaryName(name));
						}
					} else {
						errorMessage.append(errorCount + ") Subsidiary is required. ");
						log.error("Subsidiary is required. Please enter the valid Subsidiary Name. ");
						isError = true;
						errorCount++;
					}
				} catch (Exception e) {
					log.error("Exception subsidiary " + e.getLocalizedMessage());
					errorMessage.append(errorCount + ") Value of Subsidiary Name is invalid.");
					isError = true;
					errorCount++;
				}
				// Vendor Name - REQUIRED
				Long supplierId = null;
				try {
					if (inputCurrentRow.getCell(3) != null) {
						String vendorName = inputCurrentRow.getCell(3).getStringCellValue();
						invoice.setSupplierName(vendorName);
						Supplier supplier = this.masterServiceClient.findBySupplierName(vendorName);
						if (supplier == null) {
							errorMessage.append(errorCount + ") Vendor Name is not exist. ");
							log.error("Vendor Name is not exist.");
							isError = true;
							errorCount++;
						} else {
							supplierId = supplier.getId();
							invoice.setSupplierId(supplierId);
						}
					} else {
						errorMessage.append(errorCount + ") Vendor Name is required. ");
						log.error("Vendor Name is required.");
						isError = true;
						errorCount++;
					}
				} catch (Exception e) {
					log.error("Exception Vendor Name " + e.getLocalizedMessage());
					errorMessage.append(errorCount + ") Value of Vendor Name is invalid.");
					isError = true;
					errorCount++;
				}

				// Invoice Date - REQUIRED
				try {
					if (inputCurrentRow.getCell(4) != null) {
						invoice.setInvoiceDate(inputCurrentRow.getCell(4).getDateCellValue());
					} else {
						errorMessage.append(errorCount + ") Invoice Date is required. ");
						log.error("Invoice Date is required.");
						isError = true;
						errorCount++;
					}
				} catch (Exception e) {
					log.error("Exception Invoice Date " + e.getLocalizedMessage());
					errorMessage.append(errorCount + ") Value of Invoice Date is invalid.");
					isError = true;
					errorCount++;
				}

				// Supplier Invoice Number - REQUIRED
				try {
					if (inputCurrentRow.getCell(5) != null) {
						invoice.setInvoiceSupplyNumber(inputCurrentRow.getCell(5).getStringCellValue());
					} else {
						errorMessage.append(errorCount + ") Supplier Invoice Number is required.");
						log.error("Supplier Invoice Number is required.");
						isError = true;
						errorCount++;
					}
				} catch (Exception e) {
					log.error("Exception Supplier Invoice Number " + e.getLocalizedMessage());
					errorMessage.append(errorCount + ") Value of Supplier Invoice Number  is invalid.");
					isError = true;
					errorCount++;
				}
//				// PO - number
//				try {
//					if (inputCurrentRow.getCell(6) != null) {
//						String poNumber = inputCurrentRow.getCell(6).getStringCellValue();
//						invoice.setPoNumber(poNumber);
//						Optional<PurchaseOrder> po = this.purchaseOrderRepository.findByPoNumberAndIsDeleted(poNumber, false);
//						if (po.isPresent()) {
//							invoice.setPoId(po.get().getId());
//						} else {
//							invoice.setPoId(null);
//						}
//					}
//				} catch (Exception e) {
//					log.error("Exception po number " + e.getLocalizedMessage());
//					errorMessage.append(errorCount + ") Value of po number is invalid.");
//					isError = true;
//					errorCount++;
//				}
				// Location - REQUIRED
				try {
					if (inputCurrentRow.getCell(7) != null) {
						String locationName = inputCurrentRow.getCell(7).getStringCellValue();
						List<Location> locations = this.masterServiceClient.getLocationsByLocationName(locationName);
						Long locationId = null;
						if (CollectionUtils.isNotEmpty(locations))
							locationId = locations.get(0).getId();
						else {
							errorMessage.append(errorCount + ") Location is not valid.");
							log.error("Location is not valid.");
							isError = true;
							errorCount++;
						}
						invoice.setLocationId(locationId);
					} else {
						errorMessage.append(errorCount + ") Location is required.");
						log.error("Location is required.");
						isError = true;
						errorCount++;
					}
				} catch (Exception e) {
					log.error("Exception location " + e.getLocalizedMessage());
					errorMessage.append(errorCount + ") Value of location invalid.");
					isError = true;
					errorCount++;
				}
				// Payment term
				try {
					if (inputCurrentRow.getCell(8) != null) {
						invoice.setPaymentTerm(inputCurrentRow.getCell(8).getStringCellValue());
					}
				} catch (Exception e) {
					log.error("Exception payment term " + e.getLocalizedMessage());
					errorMessage.append(errorCount + ") Value of payment term is invalid.");
					isError = true;
					errorCount++;
				}

				// Exchange Rate
				try {
					if (inputCurrentRow.getCell(10) != null) {
						double exchangeRate = inputCurrentRow.getCell(10).getNumericCellValue();
						invoice.setFxRate(exchangeRate);
					}
				} catch (Exception e) {
					log.error("Exception Exchange Rate " + e.getLocalizedMessage());
					errorMessage.append(errorCount + ") Value of Exchange Rate is invalid.");
					isError = true;
					errorCount++;
				}
				// ----------------- invoice header fields completed --------------------------

				// -------------- invoice Item IS STARTED -------------------------------
				InvoiceItem invoiceItem = new InvoiceItem();
				// grn number
//				try {
//					if (inputCurrentRow.getCell(11) != null) {
//						String grnNumber = inputCurrentRow.getCell(11).getStringCellValue();
//						invoiceItem.setGrnNumber(grnNumber);
//						Optional<Grn> grn = this.grnRepository.findByGrnNumberAndIsDeleted(grnNumber, false);
////						Long grnId = null;
////						grnId = grn.get().getId();
//						if (grn.isPresent()) {
//							invoiceItem.setGrnId(grn.get().getId());
//						} else {
//							invoiceItem.setGrnId(null);
//						}
//					}
//				} catch (Exception e) {
//					log.error("Exception grn number " + e.getLocalizedMessage());
//					errorMessage.append(errorCount + ") Value of grn number is invalid.");
//					isError = true;
//					errorCount++;
//				}

				// item code
				try {
					if (inputCurrentRow.getCell(12) != null) {
						String itemCode = inputCurrentRow.getCell(12).getStringCellValue();
						invoiceItem.setItemName(itemCode);
						Item item = this.masterServiceClient.findByName(itemCode);
						if (item != null) {
							invoiceItem.setItemId(item.getId());
							invoiceItem.setItemUom(item.getUom());
							invoiceItem.setItemDescription(item.getDescription());
						} else {
							invoiceItem.setItemId(null);
							invoiceItem.setItemUom(null);
							invoiceItem.setItemDescription(null);
						}
					}
				} catch (Exception e) {
					log.error("Exception Item Code " + e.getLocalizedMessage());
					errorMessage.append(errorCount + ") Value of Item Code is invalid.");
					isError = true;
					errorCount++;
				}
				// item quantity
				try {
					if (inputCurrentRow.getCell(13) != null) {
						double quantity = inputCurrentRow.getCell(13).getNumericCellValue();
						invoiceItem.setBillQty(quantity);
					}
				} catch (Exception e) {
					log.error("Exception quantity " + e.getLocalizedMessage());
					errorMessage.append(errorCount + ") Value of quantity is invalid.");
					isError = true;
					errorCount++;
				}

				// rate
				try {
					if (inputCurrentRow.getCell(14) != null) {
						double rate = inputCurrentRow.getCell(14).getNumericCellValue();
						invoiceItem.setRate(rate);
					}
				} catch (Exception e) {
					log.error("Exception rate " + e.getLocalizedMessage());
					errorMessage.append(errorCount + ") Value of rate is invalid.");
					isError = true;
					errorCount++;
				}

				// tax group
				try {
					if (inputCurrentRow.getCell(15) != null) {
						String taxGroupName = inputCurrentRow.getCell(15).getStringCellValue();
						TaxGroup taxGroup = this.setupServiceClient.findByTaxGroupName(taxGroupName);
						if (taxGroup != null) {
							invoiceItem.setTaxGroupId(taxGroup.getId());
						} else {
							invoiceItem.setTaxGroupId(null);
						}
					}
				} catch (Exception e) {
					log.error("Exception Tax Group " + e.getLocalizedMessage());
					errorMessage.append(errorCount + ") Value of Tax Group is invalid.");
					isError = true;
					errorCount++;
				}
				// depertment
				try {
					if (inputCurrentRow.getCell(16) != null) {
						String depertment = inputCurrentRow.getCell(16).getStringCellValue();
						invoiceItem.setDepartment(depertment);
					}
				} catch (Exception e) {
					log.error("Exception depertment " + e.getLocalizedMessage());
					errorMessage.append(errorCount + ") Value of depertment is invalid.");
					isError = true;
					errorCount++;
				}

				List<InvoiceItem> invoiceItems = new ArrayList<InvoiceItem>();
				invoiceItems = invoice.getInvoiceItems();
				if (CollectionUtils.isEmpty(invoiceItems))
					invoiceItems = new ArrayList<InvoiceItem>();

				invoiceItems.add(invoiceItem);
				invoiceItems = invoiceItems.stream().distinct().collect(Collectors.toList());

				invoice.setInvoiceItems(invoiceItems);
				// invoice line finished

				// Bill to address -- REQUIRED
				try {
					if (inputCurrentRow.getCell(17) != null) {
						String billingAddressCode = inputCurrentRow.getCell(17).getStringCellValue();

						List<SupplierAddress> addresses = this.masterServiceClient
								.findAddressBySupplierIdAndAddressCode(supplierId, billingAddressCode);
						if (CollectionUtils.isEmpty(addresses)) {
							errorMessage.append(errorCount + ") Billing Address Code is not exist. ");
							log.error("Billing Address Code is not exist.");
							isError = true;
							errorCount++;
						} else {
							String billTo = addresses.get(0).getId().toString();
							// invoice.setBillToId(billTo);
							// String billToAdress = addresses.get(0).getAddress1();
							invoice.setBillTo(billTo);
							invoice.setTaxRegNumber(addresses.get(0).getTaxRegistrationNumber());
						}
					} else {
						errorMessage.append(errorCount + ") Billing Address Code is required. ");
						log.error("Billing Address Code is required.");
						isError = true;
						errorCount++;
					}
				} catch (Exception e) {
					log.error("Exception Billing Address Code " + e.getLocalizedMessage());
					errorMessage.append(errorCount + ") Value of Billing Address Code is invalid.");
					isError = true;
					errorCount++;
				}

				// Shipping Address
				try {
					if (inputCurrentRow.getCell(18) != null) {
						String shippingAddressCode = inputCurrentRow.getCell(18).getStringCellValue();

						List<SupplierAddress> addresses = this.masterServiceClient
								.findAddressBySupplierIdAndAddressCode(supplierId, shippingAddressCode);
						if (CollectionUtils.isEmpty(addresses)) {
							errorMessage.append(errorCount + ") Shipping Address Code is not exist. ");
							log.error("Shipping Address Code is not exist.");
							isError = true;
							errorCount++;
						} else {
							String shipTo = addresses.get(0).getId().toString();
							// invoice.setShipToId(shipTo);
							// String shipToAdress = addresses.get(0).getAddress1();
							invoice.setShipTo(shipTo);
						}
					} else {
						errorMessage.append(errorCount + ") Shipping Address Code is required. ");
						log.error("Shipping Address Code is required.");
						isError = true;
						errorCount++;
					}
				} catch (Exception e) {
					log.error("Exception Shipping Address Code " + e.getLocalizedMessage());
					errorMessage.append(errorCount + ") Value of Shipping Address Code is invalid.");
					isError = true;
					errorCount++;

				}
				// ADDED IN MAP
				invoiceMapping.put(externalId, invoice);
				Cell cell = inputCurrentRow.createCell(statusColumnNumber);
				if (isError) {
					cell.setCellValue(errorMessage.toString());
					invoice.setHasError(true);
					continue;
				} else if (invoice.isHasError()) {
					cell.setCellValue("Data is not valid for parent or sibling items.");
				} else {
					cell.setCellValue("Imported");
				}
			}
			for (Map.Entry<String, Invoice> map : invoiceMapping.entrySet()) {
				log.info(map.getKey() + " ==== >>> " + map.getValue());
				Invoice invoice = map.getValue();
				if (invoice != null && !invoice.isHasError()) {
					this.saveInvoice(invoice);
					log.info("invoice is saved.");
				}
			}

			FileOutputStream out = null;
			File outputFile = new File("invoice_export.xlsx");
			try {
				// Writing the workbook
				out = new FileOutputStream(outputFile);
				workbook.write(out);
				log.info("invoice_export.xlsx written successfully on disk.");
			} catch (Exception e) {
				// Display exceptions along with line number
				// using printStackTrace() method
				e.printStackTrace();
				throw new CustomException("Something went wrong. Please Contact Administrator.");
			} finally {
				out.close();
				workbook.close();
			}
			return Files.readAllBytes(outputFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
			throw new CustomException("Something went wrong. Please Contact Administrator..");
		}
	}

	@Override
	public List<Invoice> getIdAndIntegratedIdAndCreatedDateBetween(Long subsidiaryId, java.sql.Date startDate,
			java.sql.Date endDate) {
		List<Invoice> optInvoice = invoiceRepository
				.findBySubsidiaryIdAndIntegratedIdAndCreatedDateBetween(subsidiaryId, null, startDate, endDate);
		return optInvoice;
	}

	@Override
	public Boolean selfApprove(Long invoiceId) {
		Optional<Invoice> invoice = this.invoiceRepository.findByInvoiceId(invoiceId);

		if (!invoice.isPresent()) {
			log.error("Invoice Not Found against given invoice id : " + invoiceId);
			throw new CustomMessageException("Invoice Not Found against given invoice id : " + invoiceId);
		}
		invoice.get().setInvStatus(TransactionStatus.APPROVED.getTransactionStatus());
		invoice.get().setLastModifiedBy(CommonUtils.getLoggedInUsername());

		if (this.invoiceRepository.save(invoice.get()) != null)
			return true;
		else
			throw new CustomException("Error in self approve. Please contact System Administrator");
	}

	@Override
	public List<Invoice> getInvoiceApproval(String user) {
		List<String> status = new ArrayList<String>();
		status.add(TransactionStatus.PENDING_APPROVAL.getTransactionStatus());
		status.add(TransactionStatus.PARTIALLY_APPROVED.getTransactionStatus());
		List<Invoice> invoices = new ArrayList<Invoice>();
		invoices = invoiceRepository.getInvoiceForApproval(status, user);
		return invoices;
	}

}
