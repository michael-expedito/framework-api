package br.com.detrasoft.framework.api.resource;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.detrasoft.framework.api.domain.entity.Message;
import br.com.detrasoft.framework.api.domain.entity.TypeMessage;
import br.com.detrasoft.framework.api.domain.service.GenericProcessService;
import br.com.detrasoft.framework.api.domain.utils.exception.ProcessException;
import br.com.detrasoft.framework.api.domain.utils.library.GeneralFunctions;

public abstract class GenericProcessResource<VOP> {

	protected GenericProcessService<VOP> processService;

	public GenericProcessResource(GenericProcessService<VOP> processService) {
		this.processService = processService;
	}
	
	@PostMapping()
	public ResponseEntity<Response<VOP>> postProcess(@RequestBody VOP object, BindingResult result) {
		Response<VOP> response = new Response<VOP>();
		try {
			beforeCreate(object);
			VOP objectProcessed = processService.process(object);
			List<Message> messages = processService.getMessages();
			afterCreate(objectProcessed, messages);
			clearInfiniteRecursion(object);
			response.setData(objectProcessed);
			messages = GeneralFunctions.clearMessagesDuplicate(messages);
			if (GeneralFunctions.hasFatalError(messages)) {
				GeneralFunctions.clearMessagesSuccess(messages);
			}
			response.setMessages(messages);

		} catch (Exception e) {
			GeneralFunctions.clearMessagesSuccess(processService.getMessages());
			response.setMessages(GeneralFunctions.clearMessagesDuplicate(processService.getMessages()));
			if (!e.getClass().equals(ProcessException.class)) {
				response.getMessages().add(new Message("FAT01", TypeMessage.error, e.getMessage()));
				return ResponseEntity.badRequest().body(response);
			}
		}
		return ResponseEntity.ok(response);
	}

	@PutMapping(value = "/{id-function-process}")
	public ResponseEntity<Response<VOP>> putProcess(
			@PathVariable("id-function-process") Integer IdFunctionProcess, @RequestBody VOP object, BindingResult result) {
		Response<VOP> response = new Response<VOP>();
		try {
			putProcessFunction(IdFunctionProcess, object, response);
			clearInfiniteRecursion(object);
			response.setData(object);
			List<Message> messages = processService.getMessages();
			messages = GeneralFunctions.clearMessagesDuplicate(messages);
			if (GeneralFunctions.hasFatalError(messages)) {
				GeneralFunctions.clearMessagesSuccess(messages);
			}
			response.setMessages(messages);
		} catch (Exception e) {
			GeneralFunctions.clearMessagesSuccess(processService.getMessages());
			response.setMessages(processService.getMessages());
			if (!e.getClass().equals(ProcessException.class)) {
				response.getMessages().add(new Message("FAT01", TypeMessage.error, e.getMessage()));
				return ResponseEntity.badRequest().body(response);
			}
		}
		return ResponseEntity.ok(response);
	}
	
	protected void putProcessFunction(Integer IdFunctionProcess, VOP object, Response<VOP> response) {}
	
	@GetMapping(value = "{id}")
	public ResponseEntity<Response<VOP>> openProcess(@PathVariable("id") Object id) {
		Response<VOP> response = new Response<VOP>();
		
		beforeOpenProcess(id);
		VOP object = processService.openProcess(id);
		List<Message> messages = processService.getMessages();
		afterOpenProcess(object, messages);
		clearInfiniteRecursion(object);
		response.setData(object);
		messages = GeneralFunctions.clearMessagesDuplicate(messages);
		if (GeneralFunctions.hasFatalError(messages)) {
			GeneralFunctions.clearMessagesSuccess(messages);
		}
		response.setMessages(messages);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Response<VOP>> revertProcess(@PathVariable("id") Long id) {
		Response<VOP> response = new Response<VOP>();
		try {
			beforeDelete(id);
			processService.revertProcess(id);
			List<Message> messages = processService.getMessages();
			afterDelete(messages);
			messages = GeneralFunctions.clearMessagesDuplicate(messages);
			if (GeneralFunctions.hasFatalError(messages)) {
				GeneralFunctions.clearMessagesSuccess(messages);
			}
			response.setMessages(messages);
		} catch (Exception e) {
			GeneralFunctions.clearMessagesSuccess(processService.getMessages());
			response.setMessages(processService.getMessages());
			if (!e.getClass().equals(ProcessException.class)) {
				response.getMessages().add(new Message("FAT01", TypeMessage.error, e.getMessage()));
				return ResponseEntity.badRequest().body(response);
			}
		}
		return ResponseEntity.ok(response);
	}
	
	protected void beforeCreate(VOP object) {}
	protected void afterCreate(VOP object, List<Message> messages) {}
	
	protected void beforeDelete(Long id) {}
	protected void afterDelete(List<Message> messages) {}
	
	protected void beforeOpenProcess(Object object) {}
	protected void afterOpenProcess(VOP object, List<Message> messages) {}
	
	protected void clearInfiniteRecursion(VOP object ) {}
}
