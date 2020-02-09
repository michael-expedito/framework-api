package br.com.detrasoft.framework.api.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GenericProcessService<T> extends GenericService {

	public T openProcess(Object object);

	public Page<?> openListProcess(Object object, Pageable pageable);
		
	public T process(T object);
	
	public T revertProcess(Object object);
	
}
