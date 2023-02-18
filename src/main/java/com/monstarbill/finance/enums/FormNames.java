package com.monstarbill.finance.enums;

/**
 * Form names of the Application
 * @author Prashant
 * 10-09-2022
 */
public enum FormNames {

	// enum fields
	PO("Purchase Order"),
	PR("Purchase Requisition"),
	INVOICE("AP Invoice"),
	RTV("Return To Supplier"),
	PAYMENT("Payment"), 
	SUPPLIER("Supplier"), 
	ADVANCE_PAYMENT("Advance Payment"),
	QA("Quotation Analysis"),
	RFQ("Request For Quotation"),
	GRN("Goods Received Note"),
	MAKE_PAYMENT("Make Payment"),
	DEBIT_NOTE("Debit Note");

    // internal state
    private String formName;
    
	// constructor
    private FormNames(final String formName) {
        this.formName = formName;
    }
 
    public String getFormName() {
        return formName;
    }
    
}
