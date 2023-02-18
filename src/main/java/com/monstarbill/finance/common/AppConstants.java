package com.monstarbill.finance.common;

public class AppConstants {
	// Change type to maintain in history
	public static final String UI = "UI";

	// form names useful in history
	// 1. Supplier and it's childs
	public static final String SUPPLIER = "Supplier";
	public static final String SUPPLIER_CONTACT = "Contact";
	public static final String SUPPLIER_ACCESS = "Access";
	public static final String SUPPLIER_SUBSIDIARY = "Subsidiary";
	public static final String SUPPLIER_ACCOUNTING = "Accounting";
	public static final String SUPPLIER_ADDRESS = "Address";
	public static final String SUPPLIER_ROLE = "Role";

	// Tax rate rules
	public static final String TAX_RATE = "Tax Rate Rules";

	// Subsidiary & it's childs
	public static final String SUBSIDIARY = "Subsidiary";
	public static final String SUBSIDIARY_ADDRESS = "Address";

	// Bank & it's childs
	public static final String BANK = "Bank";
	public static final String BANK_PAYMENT_INSTRUMENTS = "Payment Instruments";

	// Account
	public static final String ACCOUNT = "Account";
	public static final String ACCOUNT_SUBSIDIARY = "Subsidiary";
	public static final String ACCOUNT_DEPARTMENT = "Restricted Department";
	public static final String ACCOUNT_COST_CENTER = "Cost Center";
	public static final String ACCOUNT_LOCATION = "Location";

	// Project
	public static final String PROJECT = "Project";

	// Currency
	public static final String CURRENCY = "Currency";

	// Item & it's childs
	public static final String ITEM = "Item";

	// PR and It's child
	public static final String PR = "Purchase Requisition";
	public static final String PR_ITEM = "Item";

	// Location
	public static final String LOCATION = "Location";
	public static final String LOCATION_ADDRESS = "Address";

	// Role & it's child
	public static final String ROLE = "Role";
	public static final String ROLE_SUBSIDIARY = "Subsidiary";
	public static final String ROLE_ACCESS_POINT = "Permissions";
	public static final String ROLE_RESTRICTED_DEPARTMENT = "Restricted Department";

	// RFQ (Quotation) & it's child
	public static final String QUOTATION = "RFQ";
	public static final String QUOTATION_PR_NUMBER = "Purchase Requisition";
	public static final String QUOTATION_ITEM = "Item";
//	public static final String QUOTATION_ITEM_VENDOR = "Quotation Item Vendor Mapping";
	public static final String QUOTATION_VENDOR = "Vendor";
	public static final String QUOTATION_GENERAL_INFO = "General Information";

	// QA & it's child
	public static final String QUOTATION_ANALYSIS = "Quotation Analysis";
	public static final String QUOTATION_ANALYSIS_ITEM = "Item";

	// Bid Type
	public static final String BID_OPEN = "Open";
	public static final String BID_CLOSE = "Close";

	// Document Sequence
	public static final String DOCUMENT_SEQUENCE = "Document Sequence";

	// PO
	public static final String PURCHASE_ORDER = "Purchase Order";
	public static final String PURCHASE_ORDER_ITEM = "Item";

	// Invoice
	public static final String INVOICE = "AP Invoice";
	public static final String INVOICE_ITEM = "Item";

	// Tax Group & it's childs
	public static final String TAX_GROUP = "Tax Group";
	public static final String TAX_RATE_RULE = "Rule";

	// Fiscal Calender
	public static final String FISCAL_CALENDER = "Fiscal Calender";
	public static final String FISCAL_CALENDER_ACCOUNTING = "Accounting";

	// Approval Preference
	public static final String APPROVAL_PREFERENCE = "Approval Preference";
	public static final String APPROVAL_PREFERENCE_CONDITION = "Condition";
	public static final String APPROVAL_PREFERENCE_SEQUENCE = "Sequence";

	// Employee
	public static final String EMPLOYEE = "Employee";
	public static final String EMPLOYEE_ACCOUNTING = "Accounting";
	public static final String EMPLOYEE_ADDRESS = "Address";
	public static final String EMPLOYEE_CONTACT = "Contact";
	public static final String EMPLOYEE_ROLE = "Role";
	public static final String EMPLOYEE_ACCESS = "Access";

	// Make Payment
	public static final String MAKE_PAYMENT = "Make Payment";
	public static final String MAKE_PAYMENT_BILLS = "Bills";
	public static final String MAKE_PAYMENT_LIST = "List";

	// General Preference
	public static final String COSTING_PREFERENCE = "Costing Preference";
	public static final String ACCOUNTING_PREFERENCE = "Accounting Preference";
	public static final String NUMBERING_PREFERENCE = "Numbering Preference";
	public static final String PREFERENCE = "General Preference";
	public static final String PREFERENCES_SUBSIDIARY = "Preference Subsidiary";
	public static final String APPROVAL_ROUTING_PREFERENCE = "Approval Routing Preference";
	public static final String OTHER_PREFERENCE = "Other Preference";

	// RTV
	public static final String RTV = "RTV";
	public static final String RTV_ITEM = "Item";

	// Advance Payment
	public static final String ADVANCE_PAYMENT = "Advance Payment";
	public static final String ADVANCE_PAYMENT_APPLY = "Apply";

	// Debit Note
	public static final String DEBIT_NOTE = "Debit Note";
	public static final String DEBIT_NOTE_ITEM = "Item";

	// Approval Type
	public static final String APPROVAL_TYPE_CHAIN = "CHAIN";
	public static final String APPROVAL_TYPE_INDIVIDUAL = "INDIVIDUAL";

	public static final String SUPPLIER_MASTER = "Supplier Master";
	public static final String EMPLOYEE_MASTER = "Employee Master";
}
