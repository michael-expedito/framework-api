package br.com.detrasoft.framework.api.domain.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface GenericCRUDRepository<T> extends PagingAndSortingRepository<T, Long> {

}
