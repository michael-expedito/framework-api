package br.com.detrasoft.framework.api.search.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import br.com.detrasoft.framework.api.domain.entity.GenericEntity;
import br.com.detrasoft.framework.api.domain.utils.annotation.Resource;
import br.com.detrasoft.framework.api.search.entity.FieldType;
import br.com.detrasoft.framework.api.search.entity.SearchField;

@Service
public class SearchComponentService {

	private ResourceBundle bundle;
	private Locale locale;

	private FieldType getTypeByField(Field field) {
		switch (field.getType().getSimpleName()) {
		case "String":
			return FieldType.string;
		case "Long":
			return FieldType.number;
		case "Integer":
			return FieldType.number;
		case "BigDecimal":
			return FieldType.currency;
		case "Date":
			return FieldType.date;
		case "List":
			return FieldType.list;
		default:
			return FieldType.entity;
		}
	}

	public String getTitle(Class<?> classe) {
		String resourceDirectory = "";

		if (classe.isAnnotationPresent(Resource.class)) {
			Resource resource = classe.getAnnotation(Resource.class);
			resourceDirectory = resource.directory();
			if (resourceDirectory != null) {
				locale = LocaleContextHolder.getLocale();
				bundle = ResourceBundle.getBundle(resourceDirectory, locale);
			}
		}
		
		return bundle.getString("entityName");
	}
	
	public List<SearchField> getColumns(Class<?> classe) {

		List<SearchField> listReturn = new ArrayList<SearchField>();

		String resourceDirectory = "";

		if (classe.isAnnotationPresent(Resource.class)) {
			Resource resource = classe.getAnnotation(Resource.class);
			resourceDirectory = resource.directory();
			if (resourceDirectory != null) {
				locale = LocaleContextHolder.getLocale();
				bundle = ResourceBundle.getBundle(resourceDirectory, locale);
			}
		}

		for (Field field : classe.getDeclaredFields()) {

			String fieldNameTranlated = field.getName();

			if (!fieldNameTranlated.equals("serialVersionUID")) {
				if (field.isAnnotationPresent(Resource.class)) {
					String fieldLabel = field.getAnnotation(Resource.class).field();
					if (fieldLabel != null) {
						fieldNameTranlated = bundle.getString(fieldLabel);
					}
				} else {
					fieldNameTranlated = bundle.getString(field.getName());
				}

				String name = "", subField = "";
				
				FieldType type = FieldType.string;
				
				if (field.isAnnotationPresent(ManyToOne.class)) {
					Class<?> fieldClass = field.getType();
					for (Field fieldSubClass : fieldClass.getDeclaredFields()) {
						if (fieldSubClass.isAnnotationPresent(Column.class)) {
							Column column = fieldSubClass.getAnnotation(Column.class);
							if (column.unique()) {
								name = field.getName();
								subField = fieldSubClass.getName();
							}
						}
					}
				} else {
					name = field.getName();
				}
				type = getTypeByField(field);
				listReturn.add(new SearchField(fieldNameTranlated, name, subField, type, null));
			}
		}

		return listReturn;
	}

	public String getSQLNativeCommand(Object entitySearch) {

		String select = "", from = "", join = "", where = "", orderBy = "", columnId = "";
		try {
			Class<?> classe = entitySearch.getClass();
			for (Field fieldSubClass : classe.getDeclaredFields()) {
				fieldSubClass.setAccessible(true);
				if (fieldSubClass.isAnnotationPresent(Column.class)) {
					Column columnSubClass = fieldSubClass.getAnnotation(Column.class);
					if (fieldSubClass.isAnnotationPresent(Id.class)) {
						columnId = columnSubClass.name();
					}
				}
			}
			
			if (classe != null) {
				for (Field field : classe.getDeclaredFields()) {
					if (field.isAnnotationPresent(Column.class) 
							|| field.isAnnotationPresent(JoinColumn.class)
							|| field.isAnnotationPresent(JoinTable.class)) {

						Column column = field.getAnnotation(Column.class);
						JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
						JoinTable joinTable = field.getAnnotation(JoinTable.class);
						
						String name = "";
						
						if (column != null)
							name = column.name();
						else if (joinColumn != null)
							name = joinColumn.name();
						
						if (name != "")
							select = select + name + ", ";
						
						field.setAccessible(true);
						Object valueObj = field.get(entitySearch);
						if (valueObj != null) {
							if (valueObj.getClass().equals(String.class)) {
								if (!"".equals(valueObj.toString())) {
									where = where + "lower(" +name + ") like lower('%" + valueObj.toString() + "%') and ";
								}
							} else if (valueObj.getClass().equals(Long.class)) {
								where = where + name + " = " + valueObj.toString() + " and ";
							} else if(valueObj.getClass().equals(Boolean.class)) {
								where = where + name + " = " + valueObj.toString() + " and ";
							} else if (field.getType().getGenericSuperclass() != null
									&& field.getType().getGenericSuperclass().equals(GenericEntity.class)) {
								
								JoinColumn joinColumnSubClass = field.getAnnotation(JoinColumn.class);
								String columnJoin = joinColumnSubClass.name();
								
								Class<?> subClass = field.getType();
								
								if (subClass.isAnnotationPresent(Table.class)) {
									Table table = subClass.getAnnotation(Table.class);
									join = join + "inner join " + table.name() + " on (" + columnJoin + " =" ;
								}
								
								for (Field fieldSubClass : subClass.getDeclaredFields()) {
									
									fieldSubClass.setAccessible(true);
									
									if (fieldSubClass.isAnnotationPresent(Column.class)) {
										Column columnSubClass = fieldSubClass.getAnnotation(Column.class);
										
										if (fieldSubClass.isAnnotationPresent(Id.class)) {
											join = join + " " + columnSubClass.name() + ") ";
										}
										
										Object valueSubClass = fieldSubClass.get(valueObj);
										if (valueSubClass != null) {
											if (valueSubClass.getClass().equals(String.class)) {
												where = where + "lower(" +columnSubClass.name() + ") like lower('%" + valueSubClass.toString() + "%') and ";
											}
										}
									}
								}
								
							} if (field.getType().equals(java.util.List.class)) {
								
								JoinTable joinColumnSubClass = field.getAnnotation(JoinTable.class);	
								String tableList = joinColumnSubClass.name();
								String columnList = joinColumnSubClass.joinColumns()[0].name();
								String inverseColumnList = joinColumnSubClass.inverseJoinColumns()[0].name();
								
								join = join + "left join " + tableList + " on (" + columnList + " = " + columnId + ") " ;
								
								if (((java.util.List)valueObj).size() == 1) {
									Class<?> valueList = ((java.util.List)valueObj).get(0).getClass();
									String tableRelation = "";
									if (valueList.isAnnotationPresent(Table.class)){
										tableRelation = valueList.getAnnotation(Table.class).name();
									}
									
									for (Field fieldSubClass : ((java.util.List)valueObj).get(0).getClass().getDeclaredFields()) {
										fieldSubClass.setAccessible(true);
										
										if (fieldSubClass.isAnnotationPresent(Id.class)){
											join = join + "left join " + tableRelation + " on (" + inverseColumnList + " = " + fieldSubClass.getAnnotation(Column.class).name() +") " ;
										}
										
										if (fieldSubClass.isAnnotationPresent(br.com.detrasoft.framework.api.search.annotation.SearchField.class)
												&& fieldSubClass.isAnnotationPresent(Column.class)) {
											Column columnSearch = fieldSubClass.getAnnotation(Column.class);
											Object valueSubClass = fieldSubClass.get(((java.util.List)valueObj).get(0));
											where = where + "lower(" +columnSearch.name() + ") like lower('%" + valueSubClass.toString() + "%') and ";
										}
									}
									
								}
							}
						}
					}
				}

				if (!select.equals("")) {
					select = "select " + select.substring(0, select.length() - 2) + " ";
				}

				if (!where.equals("")) {
					where = "where " + where.substring(0, where.length() - 5) + " ";
				}

				// Pegando o nome da tabela da entidade
				if (classe.isAnnotationPresent(Table.class)) {
					Table table = classe.getAnnotation(Table.class);
					from = from + table.name();
				}

				if (!from.equals("")) {
					from = "from " + from + " ";
				}

			}

		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return select + from + join + where + orderBy;

	}

}
