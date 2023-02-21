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
import com.monstarbill.finance.models.Invoice;
import com.monstarbill.finance.models.InvoicePayment;
import com.monstarbill.finance.models.MakePayment;
import com.monstarbill.finance.models.MakePaymentHistory;
import com.monstarbill.finance.payload.request.PaginationRequest;
import com.monstarbill.finance.payload.response.PaginationResponse;
import com.monstarbill.finance.service.MakePaymentService;

import lombok.extern.slf4j.Slf4j;

/**
 * All WS's of the make payment and it's child components if any
 * 
 * @author Prithwish 20-09-2022
 */
@Slf4j
@RestController
@RequestMapping("/payment")
//@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 4800, allowCredentials = "false")
public class MakePaymentController {

	@Autowired
	private MakePaymentService makePaymentService;

	/**
	 * Save/update the Make Payment
	 * 
	 * @param makePayment
	 * @return
	 */
	@PostMapping("/save")
	public ResponseEntity<MakePayment> save(@Valid @RequestBody MakePayment makePayment) {
		log.info("Saving the Make Payment :: " + makePayment.toString());
		try {
			makePayment = makePaymentService.save(makePayment);
		} catch (Exception e) {
			log.error("Error while saving the Make Payment :: ");
			e.printStackTrace();
			throw new CustomException("Error while saving the Make Payment " + e.toString());
		}
		log.info("make payment saved successfully");
		return ResponseEntity.ok(makePayment);
	}

	/**
	 * get the make payment by id
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/get")
	public ResponseEntity<MakePayment> findById(@RequestParam Long id) {
		log.info("Get Make Payment for ID :: " + id);
		MakePayment makePayments = makePaymentService.getMakePaymentById(id);
		if (makePayments == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		log.info("Returning from find by id Make Payment");
		return new ResponseEntity<>(makePayments, HttpStatus.OK);
	}

	/**
	 * get the all values for Make Payment
	 * 
	 * @return
	 */
	@PostMapping("/get/all")
	public ResponseEntity<PaginationResponse> findAll(@RequestBody PaginationRequest paginationRequest) {
		log.info("Get all Make Payment started.");
		PaginationResponse paginationResponse = new PaginationResponse();
		paginationResponse = makePaymentService.findAll(paginationRequest);
		log.info("Get all Make Payment completed.");
		return new ResponseEntity<>(paginationResponse, HttpStatus.OK);
	}

	@GetMapping("/get/history")
	public ResponseEntity<List<MakePaymentHistory>> findHistoryById(@RequestParam Long id,
			@RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "0") int pageNumber,
			@RequestParam(defaultValue = "id") String sortColumn) {
		log.info("Get make payment Audit for Make Payment ID :: " + id);
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortColumn));
		List<MakePaymentHistory> makePaymentHistories = this.makePaymentService.findHistoryById(id, pageable);
		log.info("Returning from make payment Audit by payment ID.");
		return new ResponseEntity<>(makePaymentHistories, HttpStatus.OK);
	}

	/**
	 * get the all bill number and type for supplier
	 * 
	 * @return
	 */
	@GetMapping("/get-payment-details-by-supplier")
	public ResponseEntity<List<Invoice>> getPaymentDetailsBySupplier(@RequestParam Long supplierId) {
		List<Invoice> paymentDetalis = new ArrayList<Invoice>();
		try {
			paymentDetalis = makePaymentService.getPaymentDetailsBySupplier(supplierId);
			log.info("Getting the bill and type by supplier " + paymentDetalis);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(
					"Exception while getting the Getting the bill and type by supplier :: " + e.toString());
		}
		return ResponseEntity.ok(paymentDetalis);
	}
	
	/**
	 * get the all bill number and type for supplier
	 * 
	 * @return
	 */
//	@GetMapping("/get-due-amount")
//	public ResponseEntity<List<Invoice>> getPaymentDueAmount(@RequestParam Long invoiceId) {
//		List<Invoice> paymentDetalis = new ArrayList<Invoice>();
//		try {
//			paymentDetalis = makePaymentService.getDueAmount(invoiceId);
//			log.info("Getting the due amount " + paymentDetalis);
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new CustomException(
//					"Exception while getting the Getting the due amount :: " + e.toString());
//		}
//		return ResponseEntity.ok(paymentDetalis);
//	}

	/**
	 * Save/update the Make Payment against the advance payment
	 * 
	 * @param makePayment
	 * @return
	 */
	@PostMapping("/save-for-advance-payment")
	public ResponseEntity<MakePayment> saveForAdvancePayment(@Valid @RequestBody MakePayment makePayment) {
		log.info("Saving the Make Payment :: " + makePayment.toString());
		try {
			makePayment = makePaymentService.saveForAdvancePayment(makePayment);
		} catch (Exception e) {
			log.error("Error while saving the Make Payment :: ");
			e.printStackTrace();
			throw new CustomException("Error while saving the Make Payment " + e.toString());
		}
		log.info("make payment saved successfully");
		return ResponseEntity.ok(makePayment);
	}
	
	/**
	 * soft delete the Document sequence by it's id
	 * @param id
	 * @return
	 */
	@GetMapping("/delete")
	public ResponseEntity<List<InvoicePayment>> deleteById(@RequestParam Long paymentId, @RequestParam String type) {
		log.info(" Delete Payment by ID :: " + paymentId);
		List<InvoicePayment> paymentDetalis = new ArrayList<InvoicePayment>();
		paymentDetalis = makePaymentService.deleteById(paymentId, type);
		log.info("Delete Payment ID Completed.");
		return new ResponseEntity<>(paymentDetalis, HttpStatus.OK);
	}
	
	/**
	 * Send's the Make Payment for approval
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/send-for-approval")
	public ResponseEntity<Boolean> sendForApproval(@RequestParam Long id) {
		log.info("Send for approval started for Payment ID :: " + id);
		Boolean isSentForApproval = this.makePaymentService.sendForApproval(id);
		log.info("Send for approval Finished for Payment ID :: " + id);
		return new ResponseEntity<>(isSentForApproval, HttpStatus.OK);
	}

	/**
	 * Approve all the selected Payment's from the Approval For Payment
	 * 
	 * @param paymentIds
	 * @return
	 */
	@PostMapping("/approve-all-payments")
	public ResponseEntity<Boolean> approveAllPayments(@RequestBody List<Long> paymentIds) {
		log.info("Approve all PO's is started...");
		Boolean isAllApproved = this.makePaymentService.approveAllPayments(paymentIds);
		log.info("Approve all Payment's is Finished...");
		return new ResponseEntity<>(isAllApproved, HttpStatus.OK);
	}
	
	@PostMapping("/reject-all-payments")
	public ResponseEntity<Boolean> rejectAllPayment(@RequestBody List<MakePayment> payments) {
		log.info("Reject all Payment's is started...");
		Boolean isAllRejected = this.makePaymentService.rejectAllPayments(payments);
		log.info("Reject all Payment's is Finished...");
		return new ResponseEntity<>(isAllRejected, HttpStatus.OK);
	}
	
	@GetMapping("/self-approve")
	public ResponseEntity<Boolean> selfApprove(@RequestParam Long id) {
		log.info("Self approve for MakePayment ID :: " + id);
		Boolean isApproved = this.makePaymentService.selfApprove(id);
		log.info("Self approve for MakePayment id Finished");
		return new ResponseEntity<>(isApproved, HttpStatus.OK);
	}
	
	/**
	 * get the all bill number and type for supplier and currency
	 * 
	 * @return
	 */
	@GetMapping("/get-payment-details-by-supplier-and-currency")
	public ResponseEntity<List<Invoice>> getPaymentDetailsBySupplierAndCurrency(@RequestParam Long supplierId, @RequestParam String currency) {
		List<Invoice> paymentDetalis = new ArrayList<Invoice>();
		try {
			paymentDetalis = makePaymentService.getPaymentDetailsBySupplierAndCurrency(supplierId, currency);
			log.info("Getting the bill and type by supplier " + paymentDetalis);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(
					"Exception while getting the Getting the bill and type by supplier and currency :: " + e.toString());
		}
		return ResponseEntity.ok(paymentDetalis);
	}

	/**
	 * Get make payment list for approval
	 * 
	 * @Param user or user
	 * @return list of MakePayment
	 * 
	 */

	@GetMapping("/get-payment-approval")
	public ResponseEntity<List<MakePayment>> getApprovalProcess(@RequestParam String user) {
		List<MakePayment> makePayment = new ArrayList<MakePayment>();
		try {
			makePayment = makePaymentService.getPaymentApproval(user);
			log.info("Getting the make payment for approval " + makePayment);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(
					"Exception while getting the approval process for the make payment :: " + e.toString());
		}
		return ResponseEntity.ok(makePayment);

	}
}
