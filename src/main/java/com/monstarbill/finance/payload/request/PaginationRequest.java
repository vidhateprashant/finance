package com.monstarbill.finance.payload.request;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PaginationRequest {
	private Map<String, ?> filters;
	private int pageNumber;
	private int pageSize;
	private String sortColumn;
	private String sortOrder;
}
