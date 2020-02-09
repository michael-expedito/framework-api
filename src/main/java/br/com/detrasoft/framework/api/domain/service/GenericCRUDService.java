package br.com.detrasoft.framework.api.domain.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GenericCRUDService<T> extends GenericProcessService<T> {
	Page<T> findAll(Pageable pageable);
	List<T> findAll();
}
