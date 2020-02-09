package br.com.detrasoft.framework.api.search.component;

import java.util.List;

import org.springframework.data.domain.Page;

import br.com.detrasoft.framework.api.domain.entity.Message;
import br.com.detrasoft.framework.api.search.entity.SearchField;

public class SearchReponse {

	private String title;
	private Page<?> data;
	private List<SearchField> columns;
	private List<Message> messages;
	
	public Page<?> getData() {
		return data;
	}

	public void setData(Page<?> data) {
		this.data = data;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}
	
	public List<SearchField> getColumns() {
		return columns;
	}

	public void setColumns(List<SearchField> columns) {
		this.columns = columns;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public SearchReponse(String title, Page<?> listReturn, List<SearchField> columns) {
		this.data = listReturn;
		this.columns = columns;
		this.title = title;
	}
	
	public SearchReponse() {
	}
}
