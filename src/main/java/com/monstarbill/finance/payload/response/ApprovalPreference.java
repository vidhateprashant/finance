package com.monstarbill.finance.payload.response;

import java.sql.Timestamp;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ApprovalPreference implements Cloneable {

	private Long id;

	private Long subsidiaryId;

	private String approvalType;

	private String recordType;

	private String subType;

	private boolean isDeleted;

	private Date createdDate;

	private String createdBy;

	private Timestamp lastModifiedDate;

	private String lastModifiedBy;

	private Long sequenceId;

	private String level;

}