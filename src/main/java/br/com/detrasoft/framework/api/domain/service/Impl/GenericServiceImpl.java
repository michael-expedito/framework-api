package br.com.detrasoft.framework.api.domain.service.Impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.detrasoft.framework.api.domain.entity.Message;
import br.com.detrasoft.framework.api.domain.entity.TypeMessage;
import br.com.detrasoft.framework.api.domain.service.GenericService;
import br.com.detrasoft.framework.api.domain.utils.library.ResourceFunctions;

public abstract class GenericServiceImpl implements GenericService, Serializable {

	private static final long serialVersionUID = 8589785023145103088L;

	protected List<Message> messages;
	
	public GenericServiceImpl() {
		messages = new ArrayList<Message>();
	}
	
	public List<Message> getMessages() {
		return messages;
	}
	
	public void clearMessages() {
		this.messages.clear();
	}

	public boolean hasFatalError() {
		boolean fatalError = false;
		for (Message messageService : messages) {
			if (messageService.getType() == TypeMessage.error) {
				fatalError = true;
				break;
			} else
				fatalError = false;
		}
		return fatalError;
	}
	
	public boolean hasFatalError(List<Message> messages) {
		boolean fatalError = false;
		for (Message messageService : messages) {
			if (messageService.getType() == TypeMessage.error) {
				fatalError = true;
				break;
			} else
				fatalError = false;
		}
		return fatalError;
	}
	
	public void addMessage(String code, TypeMessage type, String description ) {
		messages.add(new Message(code, type, description));
	}
	
	public void addMessageTranslated(String code, TypeMessage type) {
		String message = ResourceFunctions.getText(this.getClass(), code);
		if (message == null) {
			message = "MENSAGEM N�O ENCONTRADA, C�DIGO: "+code;
		}
		messages.add(new Message(code, type, message));
	}
	
	public void addMessageTranslated(String code, String complement, TypeMessage type) {
		String message = ResourceFunctions.getText(this.getClass(), code);
		
		if (message == null) {
			message = "MENSAGEM N�O ENCONTRADA, C�DIGO: "+code;
		} else {
			message = message + " " + complement;
		}
		messages.add(new Message(code, type, message));
	}
}
