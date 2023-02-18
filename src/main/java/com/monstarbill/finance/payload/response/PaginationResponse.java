package com.monstarbill.finance.payload.response;

import java.util.List;

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
public class PaginationResponse {
	private List<?> list;
	private int pageNumber;
	private int pageSize;
	private Long totalRecords;
	private Long totalPages;
}
