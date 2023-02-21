package com.monstarbill.finance.controllers;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.monstarbill.finance.common.CustomException;
import com.monstarbill.finance.common.CustomMessageException;
import com.monstarbill.finance.common.ExcelHelper;
import com.monstarbill.finance.models.Invoice;
import com.monstarbill.finance.models.InvoiceHistory;
import com.monstarbill.finance.models.InvoicePayment;
import com.monstarbill.finance.payload.request.PaginationRequest;
import com.monstarbill.finance.payload.response.PaginationResponse;
import com.monstarbill.finance.service.InvoiceService;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@RestController
@RequestMapping("/invoice")
//@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 4800, allowCredentials = "false")
public class InvoiceController {
	
	@Autowired
	private InvoiceService invoiceService;
	
	/**
	 * Save Invoice with Items
	 * 
	 * @param invoice
	 * @return Invoice
	 */
	@PostMapping("/save")
    public Invoice saveInvoice(@RequestBody Invoice invoice)
    {
        return invoiceService.saveInvoice(invoice);
    }
	
	/**
	 * Get all Invoices
	 * 
	 * @param
	 * @return Invoices
	 */
	@PostMapping("/get/all")
    public PaginationResponse getInvoices(@RequestBody PaginationRequest paginationRequest)
    {
		return invoiceService.getInvoices(paginationRequest);
    }
	
	/**
	 * get invoice by subsidiary and supplier
	 * @param subsidiaryId, supplierId
	 * @return 
	 */
	@GetMapping("/get-invoice-by-supplier-and-subsidiary")
	public ResponseEntity<List<Invoice>> findBySubsidiaryAndSuppplier(@RequestParam Long subsidiaryId, @RequestParam Long supplierId ) {
		List<Invoice> invoices = new ArrayList<Invoice>();
		try {
			invoices = invoiceService.findBySubsidiaryAndSuppplier( subsidiaryId, supplierId);
			log.info("Getting the invoice by subsidiaryId and supplierId " + invoices);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(
					"Exception while getting the  invoice by subsidiaryId and supplierId :: " + e.toString());
		}
		return ResponseEntity.ok(invoices);
	}

	/**
	 * Get Invoice with Items
	 * 
	 * @param invoiceId
	 * @return Invoice
	 */
	@GetMapping("/get")
    public Invoice getInvoice(@RequestParam Long invoiceId)
    {
        return invoiceService.getInvoice(invoiceId);
    }
	
	/**
	 * Get history by Invoice id
	 * @return Invoice Histories
	 */
	@GetMapping("/get/history")
	public ResponseEntity<List<InvoiceHistory>> getInvoiceHistory(@RequestParam Long invoiceId, @RequestParam(defaultValue = "10") int pageSize, 
			@RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "id") String sortColumn) {
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortColumn));
		List<InvoiceHistory> invoiceHistories = invoiceService.getInvoiceHistory(invoiceId, pageable);
		return new ResponseEntity<>(invoiceHistories, HttpStatus.OK);
	}
	
	@PostMapping("/save-invoice-payment")
    public List<InvoicePayment> saveInvoice(@RequestBody List<InvoicePayment> invoicePayment)
    {
        return invoiceService.saveInvoicePayment(invoicePayment);
    }

	
	/**
	 * Send's the Invoice for approval
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/send-for-approval")
	public ResponseEntity<Boolean> sendForApproval(@RequestParam Long id) {
		log.info("Send for approval started for Invoice ID :: " + id);
		Boolean isSentForApproval = this.invoiceService.sendForApproval(id);
		log.info("Send for approval Finished for Invoice ID :: " + id);
		return new ResponseEntity<>(isSentForApproval, HttpStatus.OK);
	}
	
	/**
	 * Approve all the selected Invoice's from the Approval For PO
	 * 
	 * @param invoiceIds
	 * @return
	 */
	@PostMapping("/approve-all-po")
	public ResponseEntity<Boolean> approveAllPos(@RequestBody List<Long> invoiceIds) {
		log.info("Approve all Invoice's is started...");
		Boolean isAllApproved = this.invoiceService.approveAllInvoices(invoiceIds);
		log.info("Approve all Invoice's is Finished...");
		return new ResponseEntity<>(isAllApproved, HttpStatus.OK);
	}
	
	/**
	 * download excel for invoice
	 * 
	 */
	@GetMapping("/download-template")
	public HttpEntity<ByteArrayResource> downloadTemplate() {
		try {
			byte[] excelContent = this.invoiceService.downloadTemplate();

			HttpHeaders header = new HttpHeaders();
			header.setContentType(new MediaType("application", "force-download"));
			header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_template.xlsx");

			return new HttpEntity<>(new ByteArrayResource(excelContent), header);
		} catch (Exception e) {
			log.error("Something went wrong while downloading the Template. Please contact Administrator. Message : " + e.getLocalizedMessage());
			throw new CustomMessageException("Something went wrong while downloading the Template. Please contact Administrator. Message : " + e.getLocalizedMessage());
		}
	}
	
	/**
	 * upload excel for invoice
	 * 
	 */
	@PostMapping("/upload")
	public HttpEntity<ByteArrayResource> uploadFile(@RequestParam("file") MultipartFile file) {
		if (ExcelHelper.hasExcelFormat(file)) {
			try {
				byte[] excelContent = this.invoiceService.upload(file);

				HttpHeaders header = new HttpHeaders();
				header.setContentType(new MediaType("application", "force-download"));
				header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_import_status.xlsx");

				return new HttpEntity<>(new ByteArrayResource(excelContent), header);
			} catch (Exception e) {
				String message = "Could not upload the file: " + file.getOriginalFilename() + "!";
				throw new CustomMessageException(message + ", Message : " + e.getLocalizedMessage());
			}
		}
		return null;
	}
	
	@GetMapping("/get-subsidiary-id-and-date-between")
	public ResponseEntity<List<Invoice>> findByIdAndIntegratedIdAndCreatedDateBetween(@RequestParam Long subsidiaryId, @RequestParam Date startDate, @RequestParam Date endDate) {
		log.info("Get invoice List By invoiceId and date between . STARTED ... ");
		List<Invoice> invoice = invoiceService.getIdAndIntegratedIdAndCreatedDateBetween(subsidiaryId, startDate, endDate);
		if (invoice == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		log.info("Get invoice List By invoiceId and date between. FINISHED ... ");
		return new ResponseEntity<>(invoice, HttpStatus.OK);
	}
}
