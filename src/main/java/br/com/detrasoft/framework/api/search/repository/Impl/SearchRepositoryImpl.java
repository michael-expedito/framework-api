package br.com.detrasoft.framework.api.search.repository.Impl;


import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import br.com.detrasoft.framework.api.search.repository.SearchRepository;

@Repository
public class SearchRepositoryImpl implements SearchRepository{

	@Autowired
	EntityManager em;
	
	public List<?> findNativeSQL(String nativeQuery, Class<?> classe) {
		return this.em.createNativeQuery(nativeQuery, classe).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> findNativeSQL(String nativeQuery) {
		return this.em.createNativeQuery(nativeQuery).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> findNativeSQL(String nativeQuery, Pageable pageable) {
		nativeQuery = nativeQuery 
				+ " LIMIT " + pageable.getPageSize()
				+ " OFFSET "+ pageable.getPageNumber()*pageable.getPageSize();
		return this.em.createNativeQuery(nativeQuery).getResultList();
	}
}
