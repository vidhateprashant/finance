package com.monstarbill.finance.feignclients;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.monstarbill.finance.models.Grn;
import com.monstarbill.finance.models.GrnItem;
import com.monstarbill.finance.models.PurchaseOrder;
import com.monstarbill.finance.models.PurchaseOrderItem;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@FeignClient(name = "procure-ws")
public interface ProcureServiceClient {

	Logger logger = org.slf4j.LoggerFactory.getLogger(ProcureServiceClient.class);

	/**
	 * get grn item by grnID and itemId
	 * 
	 * @param name
	 * @return
	 */
	@GetMapping("/grn/get-by-grn-id-and-item-id")
	@Retry(name = "procure-ws")
	@CircuitBreaker(name = "procure-ws", fallbackMethod = "findByGrnIdAndItemIdFallback")
	public List<GrnItem> findByGrnIdAndItemId(@RequestParam("grnId") Long grnId, @RequestParam("itemId") Long itemId);

	default List<GrnItem> findByGrnIdAndItemIdFallback(Long grnId, Long itemId, Throwable exception) {
		logger.error("grn id  : " + grnId + ", grn is not found exception."+"item id  : " + itemId + ", item is not found exception.");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}
	
	/**
	 * save GrnItem
	 * 
	 * @param grnItems
	 * @return
	 */
	@PostMapping("/grn/save-grn-item")
	@Retry(name = "procure-ws")
	@CircuitBreaker(name = "procure-ws", fallbackMethod = "saveGrnItemFallback")
	public List<GrnItem> saveGrnItem(@RequestBody List<GrnItem> grnItems);

	default List<GrnItem> saveGrnItemFallback(List<GrnItem> grnItems, Throwable exception) {
		logger.error("grn cannot be saved  : " + grnItems + ", saveGrnItemFallback.");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}
	
	/**
	 * get po by id
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/po/get")
	@Retry(name = "procure-ws")
	@CircuitBreaker(name = "procure-ws", fallbackMethod = "findByPoIdFallback")
	public PurchaseOrder findPoById(@RequestParam("id") Long id);

	default PurchaseOrder findByIdFallback(Long id, Throwable exception) {
		logger.error("Getting exception from MS to getByPoIdFallback. ");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}
	
	/**
	 * get po item by poid and itemId
	 * 
	 * @param id, itemId
	 * @return
	 */
	@GetMapping("/po/getByPoItemId")
	@Retry(name = "procure-ws")
	@CircuitBreaker(name = "procure-ws", fallbackMethod = "getByPoItemIdFallback")
	public PurchaseOrderItem getByPoItemId(@RequestParam("poId") Long poId, @RequestParam("itemId") Long itemId);

	default PurchaseOrderItem getByPoItemIdFallback(Long poId,Long itemId, Throwable exception) {
		logger.error("Getting exception from MS to getByPoItemIdFallback. ");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}
	
	/**
	 *save PurchaseOrderItem
	 * 
	 * @param PurchaseOrderItem
	 * @return
	 */
	@PostMapping("/po/save-po-item")
	@Retry(name = "procure-ws")
	@CircuitBreaker(name = "procure-ws", fallbackMethod = "savePoItemFallback")
	public PurchaseOrderItem savePoItem(@RequestBody PurchaseOrderItem purchaseOrderItem);

	default PurchaseOrderItem savePoItemFallback(PurchaseOrderItem poItems, Throwable exception) {
		logger.error("purchase order : " + poItems + ", cannot be saved .");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}
	
	/**
	 * get grn item by grnID and itemId
	 * 
	 * @param name
	 * @return
	 */
	@GetMapping("/grn/getByGrnItemId")
	@Retry(name = "procure-ws")
	@CircuitBreaker(name = "procure-ws", fallbackMethod = "findGrnItemByGrnIdAndItemIdFallback")
	public GrnItem findGrnItemByGrnIdAndItemId(@RequestParam("grnId") Long grnId, @RequestParam("itemId") Long itemId);

	default GrnItem findGrnItemByGrnIdAndItemIdFallback(Long grnId, Long itemId, Throwable exception) {
		logger.error("grn id  : " + grnId + ", grn is not found exception."+"item id  : " + itemId + ", item is not found exception.");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}
	
	/**
	 * save GrnItem
	 * 
	 * @param grnItems
	 * @return
	 */
	@PostMapping("/grn/save-grn-item-object")
	@Retry(name = "procure-ws")
	@CircuitBreaker(name = "procure-ws", fallbackMethod = "saveGrnItemObjectFallback")
	public GrnItem saveGrnItemObject(@RequestBody GrnItem grnItem);

	default GrnItem saveGrnItemObjectFallback(GrnItem grnItems, Throwable exception) {
		logger.error("grn cannot be saved  : " + grnItems + ", saveGrnItemFallback.");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}
	
	
	/**
	 * get po by number
	 * 
	 * @param number
	 * @return
	 */
	@GetMapping("/po/find-by-po-number")
	@Retry(name = "procure-ws")
	@CircuitBreaker(name = "procure-ws", fallbackMethod = "findByPoNumberFallback")
	public Optional<PurchaseOrder> findByPoNumber(@RequestParam("poNumber") String poNumber);

	default Optional<PurchaseOrder> findByPoNumberFallback(String poNumber, Throwable exception) {
		logger.error("Getting exception from MS to findByPoNumberFallback. ");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}
	
	/**
	 * find whether GRN is fully processed or not. More description is given on controlller level
	 * @param grnId
	 * @return
	 */
	@GetMapping("/grn/is-grn-fully-processed")
	@Retry(name = "procure-ws")
	@CircuitBreaker(name = "procure-ws", fallbackMethod = "isGrnFullyProcessedFallback")
	public Boolean isGrnFullyProcessed(@RequestParam("grnId") Long grnId);

	default Boolean isGrnFullyProcessedFallback(Long grnId, Throwable exception) {
		logger.error("Erroe while getting isGrnFullyProcessed for grn-id : " + grnId + ", isGrnFullyProcessedFallback.");
		logger.error("Exception : " + exception.getLocalizedMessage());
		return null;
	}

	/**
	 * find GRN BY Grn id
	 * @param grnId
	 * @return
	 */
	@GetMapping("/grn/get")
	@Retry(name = "procure-ws")
	@CircuitBreaker(name = "procure-ws", fallbackMethod = "findGrnByGrnIdFallback")
	public Grn findGrnByGrnId(@RequestParam("id") Long grnId);

	default Grn findGrnByGrnIdFallback(Long grnId, Throwable exception) {
		logger.error("grn id  : " + grnId + " is not found exception.");
		logger.error("findGrnByGrnIdFallback Exception : " + exception.getLocalizedMessage());
		return null;
	}

	/**
	 * save the GRN
	 * @param grns
	 * @return
	 */
	@PostMapping("/grn/save")
	@Retry(name = "procure-ws")
	@CircuitBreaker(name = "procure-ws", fallbackMethod = "saveGrnFallback")
	public List<Grn> saveGrn(@RequestBody List<Grn> grns);

	default List<Grn> saveGrnFallback(List<Grn> grns, Throwable exception) {
		logger.error("grn save Not found exception.");
		logger.error("saveGrnFallback Exception : " + exception.getLocalizedMessage());
		return null;
	}
}