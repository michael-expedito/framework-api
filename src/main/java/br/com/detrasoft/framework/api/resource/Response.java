package br.com.detrasoft.framework.api.resource;

import java.util.ArrayList;
import java.util.List;

import br.com.detrasoft.framework.api.domain.entity.Message;

public class Response<T> {

	private T data;
	private List<Message> messages;

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
	
	public List<Message> getMessages() {
		if (this.messages == null) {
			this.messages = new ArrayList<>();
		}
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

	public Response() {
		this.messages = new ArrayList<Message>();
	}

	public Response(T data, List<Message> messages) {
		this.data = data;
		this.messages = messages;
	}	
}