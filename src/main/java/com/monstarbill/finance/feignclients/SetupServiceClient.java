package com.monstarbill.finance.feignclients;

import org.slf4j.Logger;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.monstarbill.finance.models.Subsidiary;
import com.monstarbill.finance.models.TaxGroup;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@FeignClient(name = "setup-ws")
public interface SetupServiceClient {

	Logger logger = org.slf4j.LoggerFactory.getLogger(SetupServiceClient.class);

	/**
	 * get subsidiary id by subsidiary name
	 * 
	 * @param name
	 * @return
	 */
	@GetMapping("/subsidiary/get-subsidiary-id-by-name")
	@Retry(name = "setup-ws")
	@CircuitBreaker(name = "setup-ws", fallbackMethod = "getSubsidiaryIdByNameFallback")
	public Long getSubsidiaryIdByName(@RequestParam("name") String name);

	default Long getSubsidiaryIdByNameFallback(String name, Throwable exception) {
		logger.error("Subsidiary Name : " + name + ", Subsidiary is not found exception.");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}

	@GetMapping("/document-sequence/get-document-sequence-numbers")
	@Retry(name = "setup-ws")
	@CircuitBreaker(name = "setup-ws", fallbackMethod = "getDocumentSequenceNumberFallback")
	public String getDocumentSequenceNumber(@RequestParam("transactionalDate") String transactionalDate, @RequestParam("subsidiaryId") Long subsidiaryId, @RequestParam("formName") String formName, @RequestParam("isDeleted") boolean isDeleted);

	default String getDocumentSequenceNumberFallback(String transactionalDate, Long subsidiaryId, String formName,
			boolean isDeleted, Throwable exception) {
		logger.error("Getting exception from MS to generating the document sequence number");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}
	
	@GetMapping("/subsidiary/get-currency-by-subsidiary-name")
	@Retry(name = "setup-ws")
	@CircuitBreaker(name = "setup-ws", fallbackMethod = "findCurrencyBySubsidiaryNameFallback")
	public String findCurrencyBySubsidiaryName(@RequestParam("name") String name);

	default String findCurrencyBySubsidiaryNameFallback(String name, Throwable exception) {
		logger.error("Subsidiary Name : " + name + ", Subsidiary is not found exception.");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}

	@GetMapping("/tax-group/find-by-name")
	@Retry(name = "setup-ws")
	@CircuitBreaker(name = "setup-ws", fallbackMethod = "findByTaxGroupNameFallback")
	public TaxGroup findByTaxGroupName(@RequestParam("taxGroupName") String taxGroupName);

	default TaxGroup findByTaxGroupNameFallback(String taxGroupName, Throwable exception) {
		logger.error("Getting exception from MS to findByTaxGroupNameFallback. ");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}
	
	@GetMapping("/subsidiary/get")
	@Retry(name = "setup-ws")
	@CircuitBreaker(name = "setup-ws", fallbackMethod = "getSubsidiaryByIdFallback")
	public Subsidiary getSubsidiaryById(@RequestParam("id") Long id);

	default Subsidiary getByIdFallback(Long id, Throwable exception) {
		logger.error("Getting exception from MS to getSubsidiaryByIdFallback. ");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}
	
}