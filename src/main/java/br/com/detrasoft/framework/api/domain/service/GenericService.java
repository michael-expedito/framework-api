package br.com.detrasoft.framework.api.domain.service;

import java.util.List;

import br.com.detrasoft.framework.api.domain.entity.Message;
import br.com.detrasoft.framework.api.domain.entity.TypeMessage;

public interface GenericService {

	List<Message> getMessages();
	boolean hasFatalError();
	boolean hasFatalError(List<Message> Mensagens);
	void addMessage(String code, TypeMessage type, String description);
	void addMessageTranslated(String code, TypeMessage type);
}
