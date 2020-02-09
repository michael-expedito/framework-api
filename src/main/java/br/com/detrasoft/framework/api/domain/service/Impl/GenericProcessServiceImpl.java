package br.com.detrasoft.framework.api.domain.service.Impl;

import java.io.Serializable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.detrasoft.framework.api.domain.entity.TypeMessage;
import br.com.detrasoft.framework.api.domain.service.GenericProcessService;
import br.com.detrasoft.framework.api.domain.utils.exception.ProcessException;

public abstract class GenericProcessServiceImpl<T> extends GenericServiceImpl implements GenericProcessService<T>, Serializable {

	private static final long serialVersionUID = 3166881201338795238L;
	
	public T process(T object){
		T objectReturn = null;
		clearMessages();

		try{
			beforeProcess(object);
	
			if (!hasFatalError())
				validateProcess(object);
	
			if (!hasFatalError())
				objectReturn = processingOperations(object);
				
			if (!hasFatalError())
				afterProcess(objectReturn);
			
			processBackInCaseFatalError();
			
		} catch (Exception e) {
			if (!e.getClass().equals(ProcessException.class))
				addMessage("Erro", TypeMessage.error, e.getMessage());
			else 
				throw e;
		}
		
		return objectReturn;
	}

	public T revertProcess(Object object){
		T objectReturn = null;
		clearMessages();

		try{
			beforeRevertProcess(object);
		
			if (!hasFatalError())
				validateRevertProcess(object);

			if (!hasFatalError())
				objectReturn = revertProcessingOperations(object);
		
			if (!hasFatalError())
				afterRevertProcess(object);
			
			processBackInCaseFatalError();
			
		} catch (Exception e) {
			if (!e.getClass().equals(ProcessException.class))
				addMessage("Erro", TypeMessage.error, e.getMessage());
			else
				throw e;
		}
		return objectReturn;
	}
	
	public abstract T openProcess(Object object);

	public abstract Page<?> openListProcess(Object object, Pageable pageable);
	
	protected void processBackInCaseFatalError() {}

	protected void beforeProcess(T object) {}

	protected void afterProcess(T object) {}
	
	protected void beforeRevertProcess(Object object){}
	
	protected void afterRevertProcess(Object object) {}

	protected abstract T processingOperations(T object);

	protected abstract T revertProcessingOperations(Object object);

	protected abstract void validateProcess(T object);

	protected abstract void validateRevertProcess(Object object);
}
