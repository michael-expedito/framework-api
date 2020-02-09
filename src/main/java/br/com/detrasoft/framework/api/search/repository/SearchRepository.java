package br.com.detrasoft.framework.api.search.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;

public interface SearchRepository {
	List<?> findNativeSQL(String nativeQuery,  Class<?> classe);
	List<Object[]> findNativeSQL(String nativeQuery);
	List<Object[]> findNativeSQL(String nativeQuery, Pageable pageable);
}
