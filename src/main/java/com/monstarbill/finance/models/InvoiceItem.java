package com.monstarbill.finance.models;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import com.monstarbill.finance.common.AppConstants;
import com.monstarbill.finance.common.CommonUtils;
import com.monstarbill.finance.enums.Operation;

import lombok.Data;

@Data
@Entity
@Audited
@Table(schema = "finance")
public class InvoiceItem implements Cloneable {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceItemId;

	private Long itemId, grnId, taxGroupId, invoiceId, poId;
	private double billQty, rate, amount, taxAmount, totalAmount;
	private String department, itemDescription, itemUom;
	
	private String createdBy, lastModifiedBy;
	
	@CreationTimestamp
    private Date createdDate;

    @UpdateTimestamp
    private Date lastModifiedDate;
	
    @Transient
	private String itemName, description, uom, discount, netAmount;
    
    @Transient
	private String grnNumber;
    
    @Override
    public Object clone() throws CloneNotSupportedException {
    	return super.clone();
    }
    
    /**
	 * Compare the fields and values of 2 objects in order to find out the
	 * difference between old and new value
	 * 
	 * @param invoiceItem
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public List<InvoiceHistory> compareFields(InvoiceItem invoiceItem)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		List<InvoiceHistory> invoiceHistories = new ArrayList<>();
		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			String fieldName = field.getName();

			if (!CommonUtils.getUnusedFieldsOfHistory().contains(fieldName.toLowerCase())) {
				Object oldValue = field.get(this);
				Object newValue = field.get(invoiceItem);

				if (oldValue == null) {
					if (newValue != null) {
						invoiceHistories.add(this.prepareInvoiceHistory(invoiceItem, field));
					}
				} else if (!oldValue.equals(newValue)) {
					invoiceHistories.add(this.prepareInvoiceHistory(invoiceItem, field));
				}
			}
		}
		return invoiceHistories;
	}
    
    private InvoiceHistory prepareInvoiceHistory(InvoiceItem invoiceItem, Field field) throws IllegalAccessException {
    	InvoiceHistory invoiceHistory = new InvoiceHistory();
		invoiceHistory.setInvoiceId(invoiceItem.getInvoiceId());
		invoiceHistory.setChildId(invoiceItem.getInvoiceItemId());
		invoiceHistory.setModuleName(AppConstants.INVOICE_ITEM);
		invoiceHistory.setChangeType(AppConstants.UI);
		invoiceHistory.setOperation(Operation.UPDATE.toString());
		invoiceHistory.setFieldName(CommonUtils.splitCamelCaseWithCapitalize(field.getName()));
		if (field.get(this) != null) invoiceHistory.setOldValue(field.get(this).toString());
		if (field.get(invoiceItem) != null) invoiceHistory.setNewValue(field.get(invoiceItem).toString());
		invoiceHistory.setLastModifiedBy(invoiceItem.getLastModifiedBy());
		return invoiceHistory;
	}
	
}
