package com.monstarbill.finance.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.monstarbill.finance.common.CustomException;
import com.monstarbill.finance.models.AdvancePayment;
import com.monstarbill.finance.models.AdvancePaymentHistory;
import com.monstarbill.finance.models.Invoice;
//import com.monstarbill.finance.models.Invoice;
import com.monstarbill.finance.payload.request.PaginationRequest;
import com.monstarbill.finance.payload.response.PaginationResponse;
//import com.monstarbill.finance.service.AdvancePaymentService;
import com.monstarbill.finance.service.AdvancePaymentService;

import lombok.extern.slf4j.Slf4j;

/**
 * All WS's of the pre payment and it's child components if any
 * 
 * @author Prithwish 06-10-2022
 */
@Slf4j
@RestController
@RequestMapping("/advance")
//@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 4800, allowCredentials = "false")
public class AdvancePaymentController {

	@Autowired
	private AdvancePaymentService advancePaymentService;

	/**
	 * Save/update the Advance Payment
	 * 
	 * @param advancePayment
	 * @return
	 */
	@PostMapping("/save")
	public ResponseEntity<AdvancePayment> save(@Valid @RequestBody AdvancePayment advancePayment) {
		log.info("Saving the advance payment :: " + advancePayment.toString());
		try {
			advancePayment = advancePaymentService.save(advancePayment);
		} catch (Exception e) {
			log.error("Error while saving the advance payment :: ");
			e.printStackTrace();
			throw new CustomException("Error while saving the advance payment " + e.toString());
		}
		log.info("advance payment saved successfully");
		return ResponseEntity.ok(advancePayment);
	}

	/**
	 * get the advance payment by id
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/get")
	public ResponseEntity<AdvancePayment> findById(@RequestParam Long id) {
		log.info("Get Advance Payment for ID :: " + id);
		AdvancePayment advancePayments = advancePaymentService.getAdvancePaymentById(id);
		if (advancePayments == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		log.info("Returning from find by id Advance Payment");
		return new ResponseEntity<>(advancePayments, HttpStatus.OK);
	}

	@GetMapping("/self-approve")
	public ResponseEntity<Boolean> selfApprove(@RequestParam Long advancePaymentId) {
		log.info("Self approve for AdvancePayment ID :: " + advancePaymentId);
		Boolean isApproved = this.advancePaymentService.selfApprove(advancePaymentId);
		log.info("Self approve for AdvancePayment id Finished");
		return new ResponseEntity<>(isApproved, HttpStatus.OK);
	}
	
	/**
	 * get the all values for Advance Payment
	 * 
	 * @return
	 */
	@PostMapping("/get/all")
	public ResponseEntity<PaginationResponse> findAll(@RequestBody PaginationRequest paginationRequest) {
		log.info("Get all Advance Payment started.");
		PaginationResponse paginationResponse = new PaginationResponse();
		paginationResponse = advancePaymentService.findAll(paginationRequest);
		log.info("Get all Make Payment completed.");
		return new ResponseEntity<>(paginationResponse, HttpStatus.OK);
	}

	@GetMapping("/get/history")
	public ResponseEntity<List<AdvancePaymentHistory>> findHistoryById(@RequestParam String payment,
			@RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "0") int pageNumber,
			@RequestParam(defaultValue = "id") String sortColumn) {
		log.info("Get advance payment Audit for grn ID :: " + payment);
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortColumn));
		List<AdvancePaymentHistory> advancePaymentHistories = this.advancePaymentService.findHistoryById(payment,
				pageable);
		log.info("Returning from advance payment Audit by payment Number.");
		return new ResponseEntity<>(advancePaymentHistories, HttpStatus.OK);
	}

	/**
	 * get the all values for pre payment approval process
	 * 
	 * @return
	 */
	@GetMapping("/get-pre-payment-appoval")
	public ResponseEntity<List<AdvancePayment>> getPoApproval() {
		List<AdvancePayment> advancePayment = new ArrayList<AdvancePayment>();
		try {
			advancePayment = advancePaymentService.getPrePaymentApproval();
			log.info("Getting the Advance Payment for approval " + advancePayment);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(
					"Exception while getting the approval process for the Advance Payment :: " + e.toString());
		}
		return ResponseEntity.ok(advancePayment);
	}
	
	/**
	 * Send RTV for the approval for the first time
	 * @param id
	 * @return
	 */
	@GetMapping("/send-for-approval")
	public ResponseEntity<Boolean> sendForApproval(@RequestParam Long id) {
		log.info("Send for approval started for AdvancePayment ID :: " + id);
		Boolean isSentForApproval = this.advancePaymentService.sendForApproval(id);
		log.info("Send for approval Finished for Advance Payment ID :: " + id);
		return new ResponseEntity<>(isSentForApproval, HttpStatus.OK);
	}
	
	/**
	 * Approve all the selected RTV's from the Approval For RTV
	 * @param advancePaymentIds
	 * @return
	 */
	@PostMapping("/approve-all-advance-payments")
	public ResponseEntity<Boolean> approveAllAdvancePayments(@RequestBody List<Long> advancePaymentIds) {
		log.info("Approve all Advance Payment's is started...");
		Boolean isAllApproved = this.advancePaymentService.approveAllAdvancePayments(advancePaymentIds);
		log.info("Approve all Advance Payment's is Finished...");
		return new ResponseEntity<>(isAllApproved, HttpStatus.OK);
	}

	@PostMapping("/reject-all-advance-payments")
	public ResponseEntity<Boolean> rejectAllAdvancePayments(@RequestBody List<AdvancePayment> advancePayments) {
		log.info("Reject all Advance Payments's is started...");
		Boolean isAllRejected = this.advancePaymentService.rejectAllAdvancePayments(advancePayments);
		log.info("Reject all Advance Payments's is Finished...");
		return new ResponseEntity<>(isAllRejected, HttpStatus.OK);
	}

	/*
	 * For LINE LEVEL next approver change
	 */
	@GetMapping("/update-next-approver")
	public ResponseEntity<Boolean> updateNextApproverByLine(@RequestParam Long approverId, @RequestParam Long advancePaymentId) {
		return new ResponseEntity<>(this.advancePaymentService.updateNextApprover(approverId, advancePaymentId), HttpStatus.OK);
	}
	
//	/**
//	 * Save/update the Advance Payment apply
//	 * 
//	 * @param advancePayment
//	 * @return
//	 */
//	@PostMapping("/save-advance-payment-apply")
//	public ResponseEntity<List<AdvancePaymentApply>> saveApply(@RequestBody List<AdvancePaymentApply> advancePaymentApply) {
//		log.info("Saving the advance payment apply :: " + advancePaymentApply.toString());
//		try {
//			advancePaymentApply = advancePaymentService.saveApply(advancePaymentApply);
//		} catch (Exception e) {
//			log.error("Error while saving the advance payment apply :: ");
//			e.printStackTrace();
//			throw new CustomException("Error while saving the advance payment apply " + e.toString());
//		}
//		log.info("advance payment saved successfully");
//		return ResponseEntity.ok(advancePaymentApply);
//	}
	/**
	 * Save/update the Advance Payment
	 * 
	 * @param advancePayment
	 * @return
	 */
	@PostMapping("/save-advance-payment-apply")
	public ResponseEntity<AdvancePayment> saveApply(@Valid @RequestBody AdvancePayment advancePayment) {
		log.info("Saving the advance payment :: " + advancePayment.toString());
		try {
			advancePayment = advancePaymentService.saveApply(advancePayment);
		} catch (Exception e) {
			log.error("Error while saving the advance payment :: ");
			e.printStackTrace();
			throw new CustomException("Error while saving the advance payment " + e.toString());
		}
		log.info("advance payment saved successfully");
		return ResponseEntity.ok(advancePayment);
	}
	
	/**
	 * get invoice by subsidiary and supplier
	 * @param subsidiaryId, supplierId
	 * @return 
	 */
	@GetMapping("/get-invoice-by-supplier-and-subsidiary-and-currency")
	public ResponseEntity<List<Invoice>> findBySubsidiaryAndSuppplier(@RequestParam Long subsidiaryId, @RequestParam Long supplierId, @RequestParam String currency ) {
		List<Invoice> invoices = new ArrayList<Invoice>();
		try {
			invoices = advancePaymentService.findBySubsidiaryAndSuppplierAndCurrency( subsidiaryId, supplierId, currency);
			log.info("Getting the invoice by subsidiaryId and supplierId " + invoices);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(
					"Exception while getting the  invoice by subsidiaryId and supplierId :: " + e.toString());
		}
		return ResponseEntity.ok(invoices);
	}
	
	/**
	 * void payment
	 * @param paymentId,type
	 * @return
	 */
	@GetMapping("/void-payment-for-advance")
	public ResponseEntity<String> voidPayment(@RequestParam Long paymentId, @RequestParam String type) {
		log.info(" Delete Payment by ID :: " + paymentId);
		String paymentDetalis = advancePaymentService.voidPayment(paymentId, type);
		log.info("Delete Payment ID Completed.");
		return new ResponseEntity<>(paymentDetalis, HttpStatus.OK);
	}
}
