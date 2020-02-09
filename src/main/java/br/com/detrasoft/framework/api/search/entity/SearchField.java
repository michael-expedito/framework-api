package br.com.detrasoft.framework.api.search.entity;

public class SearchField {

	private String header;
	private String field;
	private String columnName;
	private String subfield;
	private FieldType type;
	private Object value;
	
	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public FieldType getType() {
		return type;
	}

	public void setType(FieldType type) {
		this.type = type;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getSubfield() {
		return subfield;
	}

	public void setSubfield(String subfield) {
		this.subfield = subfield;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public SearchField(String header, String field, String subfield, FieldType type, String columnName) {
		super();
		this.header = header;
		this.field = field;
		this.subfield = subfield;
		this.type = type;
		this.columnName = columnName;
	}
}
