package br.com.detrasoft.framework.api.domain.service.Impl;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.collection.internal.PersistentBag;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import br.com.detrasoft.framework.api.domain.entity.GenericEntity;
import br.com.detrasoft.framework.api.domain.entity.Message;
import br.com.detrasoft.framework.api.domain.entity.TypeMessage;
import br.com.detrasoft.framework.api.domain.service.GenericCRUDService;
import br.com.detrasoft.framework.api.domain.utils.annotation.Resource;
import br.com.detrasoft.framework.api.domain.utils.library.GeneralFunctions;

public abstract class GenericCRUDServiceImpl<T> extends GenericProcessServiceImpl<T>
		implements GenericCRUDService<T>, Serializable {

	private static final long serialVersionUID = -1875235958535018149L;

	protected PagingAndSortingRepository<T, Long> repository;

	public GenericCRUDServiceImpl(PagingAndSortingRepository<T, Long> repository) {
		this.repository = repository;
	}

	@Transactional
	public T saveEntity(T entity) {
		return process(entity);
	}

	@Transactional
	public List<T> saveListEntity(List<T> listEntities) {
		List<T> listReturn = new ArrayList<T>();
		List<Message> tempMessages = new ArrayList<Message>();

		for (T obj : listEntities) {
			T objReturn = process(obj);
			listReturn.add(objReturn);
			tempMessages.addAll(messages);
		}
		clearMessages();
		return listReturn;
	}

	@Transactional
	public T deleteEntity(T entity) {
		return revertProcess(entity);
	}

	@Override
	protected T processingOperations(T object) {
		T retorno = null;

		if (!super.hasFatalError()) {
			try {

				retorno = repository.save(object);
				addMessage("INC0001", TypeMessage.success, "Registro gravado com sucesso.");
			} catch (Exception e) {
				addMessage("INT000?", TypeMessage.error,
						"Erro não rastreado ao persistir a entidade - Erro: " + e.getMessage());
				return null;
			}
		}
		return retorno;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected T revertProcessingOperations(Object object) {
		T retorno = null;
		if (!super.hasFatalError()) {
			try {
				if (object.getClass().equals(Long.class)) {
					repository.deleteById(((Long) object));
				} else {
					repository.deleteById(((GenericEntity) object).getId());
					
				}
				addMessage("INC0001", TypeMessage.success, "Registro excluído com sucesso.");
				retorno = (T) object;
			} catch (Exception e) {
				if (e.getCause().getClass() == org.hibernate.exception.ConstraintViolationException.class) {
					String nomeConstraint = ((org.hibernate.exception.ConstraintViolationException) e.getCause())
							.getConstraintName();
					addMessage("EXC0002", TypeMessage.error,
							"Não é permitido excluir esse registro devido o mesmo já está sendo referenciado na rotina de "
									+ nomeConstraint);
				} else {
					addMessage("INT000?", TypeMessage.error,
							"Erro não rastreado ao deletar a entidade - Erro: " + e.getMessage());
				}
			}
		}
		return retorno;
	}

	@Override
	public T openProcess(Object object) {
		Optional<T> entity;
		if (object.getClass().equals(GenericEntity.class)) {
			entity = repository.findById(((GenericEntity) object).getId());
		} else {
			entity = repository.findById(((Long) object));
		}
		return entity.get();
	}

	@Override
	public Page<?> openListProcess(Object object, Pageable pageable) {
		return this.repository.findAll(pageable);
	}

	@Override
	protected void validateProcess(T object) {
		GeneralFunctions.clearPropertiesEmpty((GenericEntity) object, false, object.getClass());
		validateEntity(object, messages, null);
	}

	@Override
	protected void validateRevertProcess(Object object) {

	}

	public Optional<T> findById(Long id) {
		return repository.findById(id);
	}

	public Page<T> findAll(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public List<T> findAll() {
		Iterable<T> iterable = repository.findAll();
		List<T> result = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
		return result;
	}

	@SuppressWarnings({ "unused", "unchecked" })
	public void validateEntity(T entity, List<Message> messages, String notValidateField) {
		Class<?> classe = entity.getClass();
		String resourceDirectory, fieldNameTranslated = null, classNameTranslated = null;
		ResourceBundle bundle = null;
		if (classe.isAnnotationPresent(Resource.class)) {
			Resource resource = classe.getAnnotation(Resource.class);
			resourceDirectory = resource.directory();
			if (resourceDirectory != null) {
				Locale locale = LocaleContextHolder.getLocale();
				try {
					bundle = ResourceBundle.getBundle(resourceDirectory, locale);
				} catch (Exception e) {
					messages.add(new Message("DESENVOLVEDOR", TypeMessage.error,
							"Arquivo de resource não encontrado para a classe " + classe.getSimpleName() + "Caminho: "
									+ resourceDirectory));
				}
				try {
					classNameTranslated = bundle.getString("entityName");
				} catch (Exception e) {
					messages.add(new Message("DESENVOLVEDOR", TypeMessage.error,
							"Propriedade entityName não está cadastrado no Resrouce. " + classe.getSimpleName()
									+ " Caminho: " + resourceDirectory));
				}
			}
		}

		Long Id = null;

		for (Field field : classe.getDeclaredFields()) {
			try {
				field.setAccessible(true);
				Object valueObj = field.get(entity);

				if (field.isAnnotationPresent(Id.class)) {
					if (valueObj != null) {
						Id = new Long((long) valueObj);
					}
				}
				if (field.isAnnotationPresent(Column.class) && !field.isAnnotationPresent(Id.class)) {

					if (bundle != null) {
						try {
							fieldNameTranslated = bundle.getString(field.getName());
						} catch (Exception e) {
							messages.add(new Message("DESENVOLVEDOR", TypeMessage.error, field.getName()
									+ " não está cadastrado no resource da entidade " + classe.getSimpleName()));
						}
					} else {
						fieldNameTranslated = field.getName();
					}

					Column coluna = field.getAnnotation(Column.class);
					if (valueObj != null) {
						String value = valueObj.toString();
						if ((coluna.nullable() == false) && value.trim().equals("")) {
							messages.add(new Message("INT0002", TypeMessage.error,
									fieldNameTranslated + " é um campo obrigatório"));
						}
						if ((coluna.length() > 0)
								&& (value.length() > coluna.length() && !coluna.columnDefinition().equals("TEXT"))) {
							messages.add(new Message("INT0003", TypeMessage.error, "O campo " + fieldNameTranslated
									+ " pode ter no máximo " + coluna.length() + " caracteres."));
						}

					} else if (valueObj == null && coluna.nullable() == false) {
						messages.add(new Message("INT0002", TypeMessage.error,
								fieldNameTranslated + " é um campo obrigatório"));
					}
				}

				if (field.isAnnotationPresent(ManyToOne.class) && !field.getName().equals(notValidateField)) {
					ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
					JoinColumn colunaJoin = field.getAnnotation(JoinColumn.class);
					if (colunaJoin.nullable() == false && (valueObj == null
					// || libraryService.checkEmpty((GenericEntity) field.get(entity))
					)) {
						if (bundle != null) {
							fieldNameTranslated = bundle.getString(field.getName());
						} else {
							fieldNameTranslated = field.getName();
						}
						messages.add(new Message("INT0002", TypeMessage.error,
								fieldNameTranslated + " é um campo obrigatório"));
					} else if (valueObj != null
							// && !libraryService.checkEmpty((GenericEntity) field.get(entity))
							&& !field.getName().equals(notValidateField) && manyToOne.cascade().length > 0) {
						validateEntity((T) field.get(entity), messages, null);
					}
				}

				if (field.isAnnotationPresent(ManyToMany.class)) {
					field.setAccessible(true);
					List<Object> valueObjList = (List<Object>) field.get(entity);
					if (valueObjList != null && valueObjList.getClass() == ArrayList.class) {
						for (Object object : valueObjList) {
							validateEntity((T) object, messages, null);
						}
					}
				}

				if (field.isAnnotationPresent(OneToMany.class)) {
					field.setAccessible(true);
					List<Object> valueObjList = (List<Object>) field.get(entity);
					OneToMany oneToMany = field.getAnnotation(OneToMany.class);

					if (!oneToMany.targetEntity().equals(void.class)) {
						if ((valueObjList == null) || (valueObjList != null
								&& valueObjList.getClass() == ArrayList.class && valueObjList.size() == 0)) {
							if (bundle != null) {
								fieldNameTranslated = bundle.getString(field.getName());
							} else {
								fieldNameTranslated = field.getName();
							}
							messages.add(new Message("INT0002", TypeMessage.error,
									fieldNameTranslated + " é um campo obrigatório"));
						}
					}

					if (valueObjList != null && (valueObjList.getClass() == ArrayList.class
							|| valueObjList.getClass() == PersistentBag.class) && valueObjList.size() > 0) {
						for (Object object : valueObjList) {
							if (oneToMany != null) {
								validateEntity((T) object, messages, oneToMany.mappedBy());
								// validateEntity((T) object, messages, mainClass);
							}
						}
					}
				}

			} catch (IllegalArgumentException e) {
				messages.add(new Message("INT000?", TypeMessage.error, "Erro não rastreado ao validar a entidade"));
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				messages.add(new Message("INT000?", TypeMessage.error, "Erro não rastreado ao validar a entidade"));
				e.printStackTrace();
			}
		}

	}
}
