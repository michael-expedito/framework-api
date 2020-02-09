package br.com.detrasoft.framework.api.search.component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;

import br.com.detrasoft.framework.api.domain.utils.annotation.Resource;
import br.com.detrasoft.framework.api.search.entity.FieldType;
import br.com.detrasoft.framework.api.search.entity.SearchField;

public abstract class SearchJPA<TEntity> {

	private TEntity entity;

	private String resourceDirectory;

	private ResourceBundle bundle;
	private Class<?> classe;
	private Locale locale;
	
	public abstract TEntity createEntity();
	
	public SearchJPA() {
		
		entity = createEntity();
		
		Class<?> classe = entity.getClass();
		
		// Pegando o diretorio do resource da entidade
		if (classe.isAnnotationPresent(Resource.class)) {
			Resource resource = classe.getAnnotation(Resource.class);
			resourceDirectory = resource.directory();
			if (resourceDirectory != null) {
				locale = LocaleContextHolder.getLocale();
				bundle = ResourceBundle.getBundle(resourceDirectory, locale);
			}
		}
	}
	
	private FieldType getTypeByField(Field field) {
		switch (field.getClass().getSimpleName()) {
		case "Long":
			return FieldType.number;
		case "Integer":
			return FieldType.number;
		case "BigDecimal":
			return FieldType.currency;
		case "Date":
			return FieldType.date;
		default:
			return FieldType.string;
		}
	}
	
	public String description;

	public Page<TEntity> listResult;

	public List<SearchField> getColumns() {

		List<SearchField> listReturn = new ArrayList<SearchField>();
		for (Field field : classe.getDeclaredFields()) {

			String fieldNameTranlated = field.getName();

			if (field.isAnnotationPresent(Resource.class)) {
				String fieldLabel = field.getAnnotation(Resource.class).field();
				if (fieldLabel != null) {
					fieldNameTranlated = bundle.getString(fieldLabel);
				}
			}

			listReturn.add(new SearchField(fieldNameTranlated, field.getName(), "", getTypeByField(field), null));
		}

		return listReturn;
	}
	
}
