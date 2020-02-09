package br.com.detrasoft.framework.api.resource;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.detrasoft.framework.api.domain.utils.library.ResourceFunctions;
import br.com.detrasoft.framework.api.search.component.SearchReponse;
import br.com.detrasoft.framework.api.search.entity.FieldType;
import br.com.detrasoft.framework.api.search.entity.SearchField;
import br.com.detrasoft.framework.api.search.repository.SearchRepository;

public abstract class GenericSearchResource<DTO> {

	private List<SearchField> columns;
	private List<DTO> resultList;
	private String title;
	private String from;
	private String where;
	private String groupBy;
	private String orderBy;
	
	@Autowired
	SearchRepository searchRepository;
	
	private void start() {
		title = getTitle();
		from = getFrom();
		where = getWhere();
		groupBy = getGroupBy();
		orderBy = getOrderBy();
		columns = getColumns();
	}
	
	@GetMapping
	public ResponseEntity<SearchReponse> getSchema() {
		start();
		SearchReponse response = new SearchReponse();
		response.setColumns(columns);
		response.setTitle(title);
		return ResponseEntity.ok(response);
	}

	@PostMapping
	public ResponseEntity<SearchReponse> search(@RequestBody List<SearchField> searchFields, Pageable pageable) {
		start();
		SearchReponse response = new SearchReponse();
		resultList = new ArrayList<>();
		String query = getSQLNativeCommand(searchFields);
		List<Object[]> resultSQL = searchRepository.findNativeSQL(query, pageable);
		resultSQL.forEach(x-> resultList.add(objectToDTO(x)));
		Page<?> resultListPage = new PageImpl<>(resultList, pageable, resultList.size());
		
		response.setTitle(title);
		response.setColumns(columns);
		response.setData(resultListPage);

		return ResponseEntity.ok(response);
	}

	private String getSQLNativeCommand(List<SearchField> searchFields) {
		String selectSQL = "SELECT ";
		for (int i = 0; i < columns.size(); i++) {
			selectSQL = selectSQL + columns.get(i).getColumnName() + " AS C" + i + " ";
			if (!(i == columns.size() - 1)) {
				selectSQL = selectSQL + ", ";
			}
		}

		String fromSQL = " FROM " + from;
		
		String whereSQL = (where != null ? where + " AND " : "");
		
		for (int i = 0; i < searchFields.size(); i++) {
			if ((searchFields.get(i).getValue() != null) && !(searchFields.get(i).getValue().equals(""))) {
								
				String columnName = searchFields.get(i).getColumnName();
				if (columnName == null || columnName.equals("")) {
					for (SearchField column : columns) {
						if (column.getField().equals(searchFields.get(i).getField())) {
							columnName = column.getColumnName();
						}	
					}
				}
								
				if (searchFields.get(i).getType().equals(FieldType.string)) {
					String value = searchFields.get(i).getValue().toString().replace(" ", "%");
					whereSQL = whereSQL + "LOWER(" +columnName + ") LIKE LOWER('%" + value + "%')";
				} else if(searchFields.get(i).getType().equals(FieldType.date)) {
					String value = searchFields.get(i).getValue().toString().replace(" ", "%");
					whereSQL = whereSQL + columnName + " = '" + value + "'";
				
				} else if(searchFields.get(i).getType().equals(FieldType.currency)) {
					String value = searchFields.get(i).getValue().toString().replace(" ", "%");
					whereSQL = whereSQL + columnName + " = " + value.replace(',','.');
					
				} else {
					whereSQL = whereSQL + columnName + " = " + searchFields.get(i).getValue().toString();
				}
					
				whereSQL = whereSQL + " AND ";
			
			}
		}
		if (whereSQL.length() > 5 
				&& whereSQL.substring(whereSQL.length()-5, whereSQL.length()).equals(" AND ")) {
			whereSQL = whereSQL.substring(0, whereSQL.length() - 5);
		}
		whereSQL =  (!whereSQL.equals("") ? " WHERE " + whereSQL : "");
		
		String groupBySQL = groupBy != null ? " GROUP BY " + groupBy : "";
		
		String orderBySQL = orderBy != null ? " ORDER BY " + orderBy : "";

		return selectSQL + fromSQL + whereSQL + groupBySQL + orderBySQL;
	}
	
	protected abstract List<SearchField> getColumns();
	protected abstract DTO objectToDTO(Object[] object);
	protected abstract String getFrom();
	protected abstract String getWhere();
	protected abstract String getGroupBy();
	protected abstract String getOrderBy();
	
	
	protected String getTitle() {
		return getTextFieldTranslated("title");
	}
	
	protected String getTextFieldTranslated(String keyBundle) {
		try {
			Object dtoClass = getGenericClass().newInstance();
			Class<?> classe = dtoClass.getClass();
			return ResourceFunctions.getText(classe, keyBundle);
		} catch (InstantiationException | IllegalAccessException e) {
			return "";
		}
	}
	
	@SuppressWarnings("unchecked")
	private Class<?> getGenericClass() {
		ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
		return ((Class<DTO>) (type).getActualTypeArguments()[0]);
	}
}
