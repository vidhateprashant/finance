package com.monstarbill.finance.controllers;

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

import com.monstarbill.finance.models.DebitNote;
import com.monstarbill.finance.models.DebitNoteHistory;
import com.monstarbill.finance.payload.request.PaginationRequest;
import com.monstarbill.finance.payload.response.PaginationResponse;
import com.monstarbill.finance.service.DebitNoteService;

import lombok.extern.slf4j.Slf4j;


//@CrossOrigin(origins= "*", allowedHeaders = "*", maxAge = 4800, allowCredentials = "false" )
@RestController
@RequestMapping("/debitNote")
@Slf4j
public class DebitNoteController {
	
	@Autowired
	private DebitNoteService debitNoteService;
	
	@PostMapping("/save")
	public ResponseEntity<DebitNote> saveDebitNote(@Valid @RequestBody DebitNote debitNote) {
		log.info("Saving the DebitNote :: " + debitNote.toString());
		debitNote = debitNoteService.save(debitNote);
		log.info("DebitNote saved successfully");
		return ResponseEntity.ok(debitNote);
	}
	
	@GetMapping("/get")
	public ResponseEntity<DebitNote> findById(@RequestParam Long id) {
		log.info("Get DebitNote for ID :: " + id);
		DebitNote debitNote = debitNoteService.getDebitNoteById(id);
		if (debitNote == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		log.info("Returning from find by id debitNote");
		return new ResponseEntity<>(debitNote, HttpStatus.OK);
	}
	
	/**
	 * get all debitNotes for the table with pagination
	 * @param 
	 * @return
	 */
	@PostMapping("/get/all")
	public ResponseEntity<PaginationResponse> findAll(@RequestBody PaginationRequest paginationRequest) {
		log.info("Get All debitNotes started");
		PaginationResponse paginationResponse = new PaginationResponse();
		paginationResponse = debitNoteService.findAll(paginationRequest);
		log.info("Get All debitNotes Finished");
		return new ResponseEntity<>(paginationResponse, HttpStatus.OK);
	}

	/**
	 * return the audit/history of the Debit Note Form
	 * @param debitNoteNumber
	 * @param pageSize
	 * @param pageNumber
	 * @param sortColumn
	 * @return
	 */
	@GetMapping("/get/history")
	public ResponseEntity<List<DebitNoteHistory>> findHistoryByDebitNoteNumber(@RequestParam String debitNoteNumber, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "id") String sortColumn) {
		log.info("Get Subsidiary Audit for debit Note Number :: " + debitNoteNumber);
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortColumn));
		List<DebitNoteHistory> debitNoteHistories = this.debitNoteService.findAuditByDebitNoteNumber(debitNoteNumber, pageable);
		log.info("Returning from Subsidiary Audit by Subsidiary-id.");
		return new ResponseEntity<>(debitNoteHistories, HttpStatus.OK);
	}
}
