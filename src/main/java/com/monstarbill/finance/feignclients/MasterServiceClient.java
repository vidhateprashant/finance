package com.monstarbill.finance.feignclients;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.monstarbill.finance.models.Bank;
import com.monstarbill.finance.models.Item;
import com.monstarbill.finance.models.Location;
import com.monstarbill.finance.models.Supplier;
import com.monstarbill.finance.models.SupplierAddress;
import com.monstarbill.finance.payload.request.ApprovalRequest;
import com.monstarbill.finance.payload.response.ApprovalPreference;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@FeignClient(name = "masters-ws")
public interface MasterServiceClient {

	Logger logger = LoggerFactory.getLogger(MasterServiceClient.class);

	/**
	 * find the max level for the approval
	 * @param approvalRequest
	 * @return
	 */
	@PostMapping("/approval-preference/find-approver-max-level")
	@Retry(name = "masters-ws")
	@CircuitBreaker(name = "masters-ws", fallbackMethod = "findApproverMaxLevelFallback")
	public ApprovalPreference findApproverMaxLevel(@RequestBody ApprovalRequest approvalRequest);

	default ApprovalPreference findApproverMaxLevelFallback(ApprovalRequest approvalRequest, Throwable exception) {
		logger.error("Getting exception from MS to generating the document sequence number");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}
	
	/**
	 * find approver by the level and the sequence
	 * @param id
	 * @param level
	 * @param sequenceId
	 * @return
	 */
	@GetMapping("/approval-preference/find-approver-by-level-and-sequence")
	@Retry(name = "masters-ws")
	@CircuitBreaker(name = "masters-ws", fallbackMethod = "findApproverByLevelAndSequenceFallback")
	public ApprovalRequest findApproverByLevelAndSequence(@RequestParam("id") Long id, @RequestParam("level") String level, @RequestParam("sequenceId") Long sequenceId);

	default ApprovalRequest findApproverByLevelAndSequenceFallback(Long id, String level, Long sequenceId, Throwable exception) {
		logger.error("Getting exception from MS to findApproverByLevelAndSequence ");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}
	
	/**
	 * send the email by approver for the form
	 * @param approverId
	 * @param formName
	 */
	@GetMapping("/utility/send-email-by-approver-id")
	@Retry(name = "masters-ws")
	@CircuitBreaker(name = "masters-ws", fallbackMethod = "sendEmailByApproverIdFallback")
	public void sendEmailByApproverId(@RequestParam("approverId") String approverId, @RequestParam("formName") String formName);

	default void sendEmailByApproverIdFallback(String approverId, String formName, Throwable exception) {
		logger.error("Getting exception from MS to sendEmailByApproverId ");
		logger.error("Exception : " + exception.getLocalizedMessage());
	}
	
	/**
	 * find that is approval routing is enable for given form or not
	 * @param subsidiaryId
	 * @param formName
	 * @return
	 */
	@GetMapping("/utility/find-is-approval-routing-active")
	@Retry(name = "masters-ws")
	@CircuitBreaker(name = "masters-ws", fallbackMethod = "findIsApprovalRoutingActiveFallback")
	public Boolean findIsApprovalRoutingActive(@RequestParam("subsidiaryId") Long subsidiaryId, @RequestParam("formName") String formName);

	default Boolean findIsApprovalRoutingActiveFallback(Long subsidiaryId, String formName, Throwable exception) {
		logger.error("Getting exception from MS to findIsApprovalRoutingActiveFallback ");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return false;
	}
	
	/**
	 * validate project name
	 * @param name
	 * @return
	 */
	@GetMapping("/project/is-valid-name")
	@Retry(name = "masters-ws")
	@CircuitBreaker(name = "masters-ws", fallbackMethod = "getValidateNameFallback")
	public Boolean getValidateProjectName(@RequestParam("name") String name);

	default Boolean getValidateProjectNameFallback(String name, Throwable exception) {
		logger.error("Getting exception from MS to getValidateName ");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return false;
	}
	
	/**
	 * find locations by name
	 * @param name
	 * @return
	 */
	@GetMapping("/location/get-locations-by-names")
	@Retry(name = "masters-ws")
	@CircuitBreaker(name = "masters-ws", fallbackMethod = "getLocationsByLocationNameFallback")
	public List<Location> getLocationsByLocationName(@RequestParam("name") String name);

	default List<Location> getLocationsByLocationNameFallback(String name, Throwable exception) {
		logger.error("Getting exception from MS to getValidateName ");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return new ArrayList<Location>();
	}
	
	/**
	 * find item by name
	 * @param name
	 * @return
	 */
	@GetMapping("/item/find-by-name")
	@Retry(name = "masters-ws")
	@CircuitBreaker(name = "masters-ws", fallbackMethod = "findByNameFallback")
	public Item findByName(@RequestParam("name") String name);

	default Item findByNameFallback(String name, Throwable exception) {
		logger.error("Getting exception from MS to findByName ");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}
//
//	/**
//	 * find account by id
//	 * @param id
//	 * @return
//	 */
//	@GetMapping("/account/find-by-id")
//	@Retry(name = "masters-ws")
//	@CircuitBreaker(name = "masters-ws", fallbackMethod = "findByAccountIdFallback")
//	public Account findByAccountId(@RequestParam("id") Long id);
//
//	default Account findByAccountIdFallback(Long id, Throwable exception) {
//		logger.error("Getting exception from MS to findByAccountId ");
//		logger.error("Exception : " + exception.getLocalizedMessage());
//		return null;
//	}
//	
//	/**
//	 * find item by id
//	 * @param id
//	 * @return
//	 */
//	@GetMapping("/item/get")
//	@Retry(name = "masters-ws")
//	@CircuitBreaker(name = "masters-ws", fallbackMethod = "findByItemIdFallback")
//	public Item findByItemId(@RequestParam("id") Long id);
//
//	default Item findByItemIdFallback(Long id, Throwable exception) {
//		logger.error("Getting exception from MS to findByItemId ");
//		logger.error("Exception : " + exception.getLocalizedMessage());
//		return null;
//	}
//	
	/**
	 * find supplier by name
	 * @param supplierName
	 * @return
	 */
	@GetMapping("/supplier/find-supplier-by-name")
	@Retry(name = "masters-ws")
	@CircuitBreaker(name = "masters-ws", fallbackMethod = "findBySupplierNameFallback")
	public Supplier findBySupplierName(@RequestParam("supplierName") String supplierName);

	default Supplier findBySupplierNameFallback(String supplierName, Throwable exception) {
		logger.error("Getting exception from MS to findBySupplierNameFallback ");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}
	
	/**
	 * find supplier address by supplier id and it's address code
	 * @param supplierId
	 * @param addressCode
	 * @return
	 */
	@GetMapping("/supplier/find-by-supplier-id-address-code")
	@Retry(name = "masters-ws")
	@CircuitBreaker(name = "masters-ws", fallbackMethod = "findAddressBySupplierIdAndAddressCodeFallback")
	public List<SupplierAddress> findAddressBySupplierIdAndAddressCode(@RequestParam("supplierId") Long supplierId, @RequestParam("addressCode") String addressCode);

	default List<SupplierAddress> findAddressBySupplierIdAndAddressCodeFallback(Long supplierId, String addressCode, Throwable exception) {
		logger.error("Getting exception from MS to findAddressBySupplierIdAndAddressCodeFallback ");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}
//	
//	/**
//	 * find supplier contact by supplier id & is-primary contact
//	 * @param supplierId
//	 * @param isPrimaryContact
//	 * @return
//	 */
//	@GetMapping("/supplier/find-contact-by-supplier-id-and-is-primary-contact")
//	@Retry(name = "masters-ws")
//	@CircuitBreaker(name = "masters-ws", fallbackMethod = "findContactBySupplierIdAndIsPrimaryContactFallback")
//	public SupplierContact findContactBySupplierIdAndIsPrimaryContact(@RequestParam("supplierId") Long supplierId, @RequestParam("isPrimaryContact") Boolean isPrimaryContact);
//
//	default SupplierContact findContactBySupplierIdAndIsPrimaryContactFallback(Long supplierId, Boolean isPrimaryContact, Throwable exception) {
//		logger.error("Getting exception from MS to findAddressBySupplierIdAndAddressCodeFallback ");
//		logger.error("Exception : " + exception.getLocalizedMessage());
//		return null;
//	}
//	
//	/**
//	 * get location by id
//	 * @param id
//	 * @return
//	 */
//	@GetMapping("/location/get")
//	@Retry(name = "masters-ws")
//	@CircuitBreaker(name = "masters-ws", fallbackMethod = "getLocationsByIdFallback")
//	public Location getLocationsById(@RequestParam("id") Long id);
//
//	default Location getLocationsByIdFallback(Long id, Throwable exception) {
//		logger.error("Getting exception from MS to getLocationsByIdFallback ");
//		logger.error("Exception : " + exception.getLocalizedMessage());
//		return null;
//	}
//	
//	/**
//	 * find suppliers by id's
//	 * @param supplierIds
//	 * @return
//	 */
//	@PostMapping("/supplier/get-suppliers-by-ids")
//	@Retry(name = "masters-ws")
//	@CircuitBreaker(name = "masters-ws", fallbackMethod = "getSuppliersByIdsFallback")
//	public List<Supplier> getSuppliersByIds(@RequestBody List<Long> supplierIds);
//
//	default List<Supplier> getSuppliersByIdsFallback(List<Long> supplierIds, Throwable exception) {
//		logger.error("Getting exception from MS to getSuppliersByIds ");
//		logger.error("Exception : " + exception.getLocalizedMessage());
//		return new ArrayList<Supplier>();
//	}
//	
//	/**
//	 * find location id-name mapping by id's
//	 * @param locationIds
//	 * @return
//	 */
//	@PostMapping("/location/find-location-names-by-ids")
//	@Retry(name = "masters-ws")
//	@CircuitBreaker(name = "masters-ws", fallbackMethod = "findLocationNamesByIdsFallback")
//	public List<Location> findLocationNamesByIds(@RequestBody List<Long> locationIds);
//
//	default List<Location> findLocationNamesByIdsFallback(List<Long> locationIds, Throwable exception) {
//		logger.error("Getting exception from MS to findLocationNamesByIds ");
//		logger.error("Exception : " + exception.getLocalizedMessage());
//		return new ArrayList<Location>();
//	}
	
	
	/**
	 * find approver type by approval prefrence id 
	 * @param id
	 * @param level
	 * @param sequenceId
	 * @return
	 */
	@GetMapping("/approval-preference/get-type-by-approval-id")
	@Retry(name = "masters-ws")
	@CircuitBreaker(name = "masters-ws", fallbackMethod = "getTypeByApprovalId")
	public String getTypeByApprovalId(@RequestParam("approvalPreferenceId") Long approvalPreferenceId);

	default ApprovalRequest findApproverByLevelAndSequenceFallback(Long approvalPreferenceId, Throwable exception) {
		logger.error("Getting exception from MS to approvalPreferenceId ");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}
	
	/**
	 * get supplier by it's id
	 * 
	 * @param id
	 * @return supplier
	 */
	
	@GetMapping("/supplier/get")
	@Retry(name = "masters-ws")
	@CircuitBreaker(name = "masters-ws", fallbackMethod = "findSupplierByIdFallback")
	public Supplier findSupplierById(@RequestParam("id") Long id);

	default Supplier findByIdFallback(Long id, Throwable exception) {
		logger.error("Getting exception from MS to findSupplierByIdFallback. ");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}
	
	/**
	 * get bank by it's id
	 * 
	 * @param id
	 * @return bank
	 */

	@GetMapping("/bank/get")
	@Retry(name = "masters-ws")
	@CircuitBreaker(name = "masters-ws", fallbackMethod = "getBankByIdFallback")
	public Bank getBankById(@RequestParam("id") Long id);

	default Bank getBankByIdFallback(Long id, Throwable exception) {
		logger.error("Getting exception from MS to getBankByIdFallback. ");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}
}
