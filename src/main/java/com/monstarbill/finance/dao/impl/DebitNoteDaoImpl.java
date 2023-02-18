package com.monstarbill.finance.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.monstarbill.finance.common.CommonUtils;
import com.monstarbill.finance.common.CustomException;
import com.monstarbill.finance.dao.DebitNoteDao;
import com.monstarbill.finance.models.DebitNote;
import com.monstarbill.finance.payload.request.PaginationRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("debitNoteDaoImpl")
public class DebitNoteDaoImpl implements DebitNoteDao{
	@PersistenceContext
	private EntityManager entityManager;
	
	public static final String GET_ALL_DEBIT_NOTES = " select new com.monstarbill.finance.models.DebitNote(d.id, sub.name, d.debitNoteDate, d.debitNoteNumber, sup.name) "
			+ " from DebitNote d  "
			+ " INNER join Subsidiary sub ON d.subsidiaryId = sub.id "
			+ " INNER join Supplier sup ON d.supplierId = sup.id "
			+ " WHERE d.isDeleted is false AND 1 = 1 ";
	
	public static final String GET_ALL_DEBIT_NOTES_COUNT = " select count(1) "
			+ " from DebitNote d  "
			+ " INNER join Subsidiary sub ON d.subsidiaryId = sub.id "
			+ " INNER join Supplier sup ON d.supplierId = sup.id "
			+ " WHERE d.isDeleted is false AND 1 = 1 ";

	@Override
	public List<DebitNote> findAll(String whereClause, PaginationRequest paginationRequest) {
		List<DebitNote> debitNote = new ArrayList<DebitNote>();
		
		StringBuilder finalSql = new StringBuilder(GET_ALL_DEBIT_NOTES);
		// where clause
		if (StringUtils.isNotEmpty(whereClause)) finalSql.append(whereClause.toString());
		// order by clause
		finalSql.append(CommonUtils.prepareOrderByClause(paginationRequest.getSortColumn(), paginationRequest.getSortOrder()));
		log.info("Final SQL to get all debitNote w/w/o filter :: " + finalSql.toString());
		
		try {
			TypedQuery<DebitNote> sql = this.entityManager.createQuery(finalSql.toString(), DebitNote.class);
			// pagination
			sql.setFirstResult(paginationRequest.getPageNumber() * paginationRequest.getPageSize());
			sql.setMaxResults(paginationRequest.getPageSize());
			debitNote = sql.getResultList();
		} catch (Exception ex) {
			log.error("Exception occured at the time of fetching the list of debitNote :: " + ex.toString());
			
			String errorExceptionMessage = ex.getLocalizedMessage();
			if (errorExceptionMessage == null) errorExceptionMessage = ex.toString();
			
			throw new CustomException(errorExceptionMessage);
		}
		return debitNote;
	}
	
	@Override
	public Long getCount(String whereClause) {
		Long count = 0L;
		
		StringBuilder finalSql = new StringBuilder(GET_ALL_DEBIT_NOTES_COUNT);
		// where clause
		if (StringUtils.isNotEmpty(whereClause)) finalSql.append(whereClause.toString());
		
		log.info("Final SQL to get all debitNote Count w/w/o filter :: " + finalSql.toString());
		try {
			TypedQuery<Long> sql = this.entityManager.createQuery(finalSql.toString(), Long.class);
			count = sql.getSingleResult();
		} catch (Exception ex) {
			log.error("Exception occured at the time of fetching the count of debitNote :: " + ex.toString());
			
			String errorExceptionMessage = ex.getLocalizedMessage();
			if (errorExceptionMessage == null) errorExceptionMessage = ex.toString();
			
			throw new CustomException(errorExceptionMessage);
		}
		return count;
	}}
