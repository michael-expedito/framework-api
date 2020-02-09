package br.com.detrasoft.framework.api.resource;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.detrasoft.framework.api.domain.entity.GenericEntity;
import br.com.detrasoft.framework.api.domain.entity.Message;
import br.com.detrasoft.framework.api.domain.entity.TypeMessage;
import br.com.detrasoft.framework.api.domain.service.GenericCRUDService;
import br.com.detrasoft.framework.api.domain.utils.exception.ProcessException;
import br.com.detrasoft.framework.api.domain.utils.library.GeneralFunctions;
import br.com.detrasoft.framework.api.search.component.SearchReponse;
import br.com.detrasoft.framework.api.search.entity.SearchField;
import br.com.detrasoft.framework.api.search.repository.SearchRepository;
import br.com.detrasoft.framework.api.search.service.SearchComponentService;

public abstract class GenericCRUDResource<Entity> {

	protected GenericCRUDService<Entity> service;

	public GenericCRUDResource(GenericCRUDService<Entity> service) {
		this.service = service;
	}
	
	@Autowired
	SearchComponentService searchComponentService;

	@Autowired
	SearchRepository searchRepository;
	
	@PostMapping()
	public ResponseEntity<Response<Entity>> create(@RequestBody Entity entity, BindingResult result) {
		Response<Entity> response = new Response<Entity>();
		try {
			GeneralFunctions.clearPropertiesEmpty((GenericEntity)entity, true, null);
			beforeCreate(entity);
			Entity entityPersisted = (Entity) service.process(entity);
			afterCreate(entity);
			clearInfiniteRecursion(entity);
			response.setData(entityPersisted);
			List<Message> messages = service.getMessages();
			GeneralFunctions.clearMessagesDuplicate(messages);
			response.setMessages(messages);

		} catch (Exception e) {
			GeneralFunctions.clearMessagesSuccess(service.getMessages());
			if (!e.getClass().equals(ProcessException.class))
				response.getMessages().add(new Message("FAT01", TypeMessage.error, e.getMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		return ResponseEntity.ok(response);
	}
	protected void beforeCreate(Entity entity) {}
	protected void afterCreate(Entity entity) {}
	
	@PutMapping()
	public ResponseEntity<Response<Entity>> update(@RequestBody Entity entity, BindingResult result) {
		Response<Entity> response = new Response<Entity>();
		try {
			GeneralFunctions.clearPropertiesEmpty((GenericEntity)entity, true, null);
			validateUpdate(entity, result);
			if (result.hasErrors()) {
				result.getAllErrors().forEach(error -> response.getMessages()
						.add(new Message("FAT01", TypeMessage.error, error.getDefaultMessage())));
				return ResponseEntity.badRequest().body(response);
			}
			beforeUpdate(entity);
			Entity userPersisted = (Entity) service.process(entity);
			afterUpdate(entity);
			clearInfiniteRecursion(entity);
			response.setData(userPersisted);
			List<Message> messages = service.getMessages();
			GeneralFunctions.clearMessagesDuplicate(messages);
			response.setMessages(messages);
		} catch (Exception e) {
			GeneralFunctions.clearMessagesSuccess(service.getMessages());
			if (!e.getClass().equals(ProcessException.class))
				response.getMessages().add(new Message("FAT01", TypeMessage.error, e.getMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		return ResponseEntity.ok(response);
	}

	protected void beforeUpdate(Entity entity) {}
	protected void afterUpdate(Entity entity) { }
	
	private void validateUpdate(Entity entity, BindingResult result) {
		if (((GenericEntity) entity).getId() == null) {
			result.addError(new ObjectError("Entity", "Id não informado"));
			return;
		}
	}

	@GetMapping(value = "{id}")
	public ResponseEntity<Response<Entity>> findById(@PathVariable("id") Long id) {
		Response<Entity> response = new Response<Entity>();
		beforeFindById(id); 
		Entity entity = service.openProcess(id);
		if (entity == null ) {
			response.getMessages()
					.add(new Message("FAT01", TypeMessage.error, "Registro não encontrado para o id:" + id));
			return ResponseEntity.badRequest().body(response);
		}
		afterFindById(entity, service.getMessages());
		clearInfiniteRecursion(entity);
		response.setData(entity);
		return ResponseEntity.ok(response);
	}
	
	protected void beforeFindById(Long id) {}
	protected void afterFindById(Entity entity, List<Message> messages) {}
	
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Response<Entity>> delete(@PathVariable("id") Long id) {
		Response<Entity> response = new Response<Entity>();
		Entity entity = service.openProcess(id);
		if (entity == null) {
			response.getMessages()
					.add(new Message("FAT01", TypeMessage.error, "Registro não encontrado para o id:" + id));
			return ResponseEntity.badRequest().body(response);
		}
		beforeDelete(entity);
		Entity object = service.revertProcess(entity);
		afterDelete(object);
		clearInfiniteRecursion(entity);
		List<Message> messages = service.getMessages();
		GeneralFunctions.clearMessagesDuplicate(messages);
		response.setMessages(messages);
		
		return ResponseEntity.ok(response);
	}
	
	protected void beforeDelete(Entity entity) {}
	protected void afterDelete(Entity entity) {}

	protected void clearInfiniteRecursion(Entity entity ) {}
	
	@GetMapping
	public ResponseEntity<Response<List<Entity>>> findAll() {
		Response<List<Entity>> response = new Response<List<Entity>>();
		List<Entity> resultList = service.findAll();
		response.setData(resultList);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping(value = "/paged")
	public ResponseEntity<Response<Page<Entity>>> findAll(Pageable pageable) {
		Response<Page<Entity>> response = new Response<Page<Entity>>();
		Page<Entity> resultList = service.findAll(pageable);
		response.setData(resultList);
		return ResponseEntity.ok(response);
	}

	@PostMapping(value = "/search")
	public ResponseEntity<SearchReponse> findSearch(@RequestBody List<SearchField> searchFields, Pageable pageable) {

		SearchReponse response = new SearchReponse();

		try {

			Object entity = getGenericClass().newInstance();
			Class<?> classe = entity.getClass();
			
			if (searchFields.size() > 0) {
				for (SearchField searchField : searchFields) {
					
					String nameField = searchField.getField().indexOf('.') > 0 
							? searchField.getField().substring(0, searchField.getField().indexOf('.')) 
							: searchField.getField();
					String subField = searchField.getField().indexOf('.') > 0 
							? searchField.getField().substring(searchField.getField().indexOf('.') + 1, searchField.getField().length()) 
							: ""; 
							
					for (Field field : classe.getDeclaredFields()) {
						field.setAccessible(true);
								
						if (field.getName().equals(nameField) 
								&& !"".equals(nameField)
								&& (searchField.getValue() != null && !searchField.getValue().equals(""))) {
							if (field.getType().equals(String.class)) {
								field.set(entity, searchField.getValue());
							}
							if (field.getType().equals(Long.class)) {
								field.set(entity, new Long((String) searchField.getValue()));
							}
							if (field.getType().getGenericSuperclass() != null 
									&& field.getType().getGenericSuperclass().equals(GenericEntity.class)) {
								Object value = field.getType().newInstance();
								for (Field fieldSubClass : value.getClass().getDeclaredFields()) {
									fieldSubClass.setAccessible(true);
									if ( (fieldSubClass.getName().equals(subField) && !"".equals(subField))
											|| (fieldSubClass.isAnnotationPresent(br.com.detrasoft.framework.api.search.annotation.SearchField.class))) {
										fieldSubClass.set(value, searchField.getValue());
									}
								}
								field.set(entity, value);
							}
							if (field.getType().equals(java.util.List.class)) {
								
						        ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
						        Class<?> classeList = (Class<?>) stringListType.getActualTypeArguments()[0];
						        GenericEntity objEntity = (GenericEntity) classeList.newInstance();
								for (Field fieldSubClass : objEntity.getClass().getDeclaredFields()) {
									fieldSubClass.setAccessible(true);
									if (fieldSubClass.isAnnotationPresent(br.com.detrasoft.framework.api.search.annotation.SearchField.class)) {
										fieldSubClass.set(objEntity, searchField.getValue());
									}
								}
						        List<GenericEntity> value = new ArrayList<>();
						        value.add(objEntity);
						        field.set(entity, value);
							}
						}
					}
				}

				String query = searchComponentService.getSQLNativeCommand(entity);

				List<?> resultList = searchRepository.findNativeSQL(query, getGenericClass());
				Page<?> resultListPage = new PageImpl<>(resultList, pageable, resultList.size());
				response.setData(resultListPage);
			}
			if (searchFields.size() == 0) {
				String title = searchComponentService.getTitle(getGenericClass());
				List<SearchField> columns = searchComponentService.getColumns(getGenericClass());
				response.setTitle(title);
				response.setColumns(columns);
			}

		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return ResponseEntity.ok(response);
	}

	@SuppressWarnings("unchecked")
	private Class<?> getGenericClass() {
		ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
		return ((Class<Entity>) (type).getActualTypeArguments()[0]);
	}
}
