package br.com.detrasoft.framework.api.domain.service;

import br.com.detrasoft.framework.api.domain.entity.TypeProcess;

public interface GenericValidationService<T> extends GenericService {

	void validate(T object, TypeProcess typeProcess);
}
