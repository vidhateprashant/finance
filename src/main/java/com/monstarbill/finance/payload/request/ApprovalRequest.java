package com.monstarbill.finance.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest  {
	
	// Form Level Details -- --------------------------------------------------------
	private Long subsidiaryId;

	private String formName;
	// -----------------------------------------------------------------------------
	// segment Level Details -- --------------------------------------------------------
	// PR, PO, RTV
	private Double transactionAmount;

	// PR, PO, RTV
	private Long locationId;

	// PR
	private String department;

	// supplier
	private String natureOfSupply;
	// -----------------------------------------------------------------------------
	// Approval level details -- --------------------------------------------------------
	private String approvedBy;

	private String nextApprover;

	private String nextApproverRole;
	
	private String nextApproverLevel;
	
	// stores the next approver sequence id (useful internally in order to change the approver)
	private Long nextApproverSequenceId;
	
	// stores the max level to approve, after that change status to approve
	private String approverMaxLevel;
	// -----------------------------------------------------------------------------

}
