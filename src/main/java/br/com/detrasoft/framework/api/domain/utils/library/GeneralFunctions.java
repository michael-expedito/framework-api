package br.com.detrasoft.framework.api.domain.utils.library;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.collection.internal.PersistentBag;

import br.com.detrasoft.framework.api.domain.entity.GenericEntity;
import br.com.detrasoft.framework.api.domain.entity.Message;
import br.com.detrasoft.framework.api.domain.entity.TypeMessage;

public class GeneralFunctions {
	
	public static boolean checkEmpty(GenericEntity entity) {

		Class<?> classe = entity.getClass();

		for (Field field : classe.getDeclaredFields()) {
			try {
				field.setAccessible(true);
				if (field.getName() != "serialVersionUID") {
					Object valueObj = field.get(entity);
					if (valueObj != null) {
						if (valueObj.getClass().getSuperclass() != GenericEntity.class
								&& !valueObj.toString().equals("")) {
							return false;
						}
					}
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	public static void clearPropertiesEmpty(GenericEntity entity, Boolean clearList, Class mainClass) {
		try {
			Class<?> classe = entity.getClass();
			for (Field field : classe.getDeclaredFields()) {
				field.setAccessible(true);
				if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(ManyToMany.class)
				        || field.isAnnotationPresent(OneToMany.class)) {
					if (field.get(entity) != null) {

						if (clearList && field.get(entity).getClass() == ArrayList.class) {
							if (((ArrayList) field.get(entity)).size() == 0) {
								field.set(entity, null);
							}
						} else if (clearList && field.get(entity).getClass() == PersistentBag.class) {
							if (((PersistentBag) field.get(entity)).size() == 0) {
								field.set(entity, null);
							}
						} else if(field.get(entity).getClass().getSuperclass() == GenericEntity.class 
								&& field.get(entity).getClass() != mainClass) {
							if (checkEmpty((GenericEntity) field.get(entity))) {
								field.set(entity, null);
							} else {
								clearPropertiesEmpty((GenericEntity) field.get(entity), clearList, mainClass);
							}	
						}
					}
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public static void clearMessagesSuccess(List<Message> listMessages){
		Boolean temErro = false;
		for (Message msg : listMessages){
			if (msg.getType() == TypeMessage.error) {
				temErro = true;
				break;
			}
		}
		if (temErro){
			listMessages.removeIf(x -> x.getType() == TypeMessage.success);
		}	
	}
	
	public static List<Message> clearMessagesDuplicate(List<Message> listMessages) {
		return listMessages.stream().distinct().collect(Collectors.toList());	
	}
	
	public static boolean hasFatalError(List<Message> messages) {
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
	
	public static Date ObjectToDate(Object dateValue) {
		Date resultDate = null;
		if (dateValue != null) {
			if (dateValue.getClass().equals(Timestamp.class)) {
				resultDate = new Date( ((Timestamp) dateValue).getTime() );
			}
			if (dateValue.getClass().equals(java.sql.Date.class)) {
				resultDate = new Date( ((java.sql.Date) dateValue).getTime() );
			}
		}
		return resultDate;
	}
		
	public static Long ObjectToLong(Object longValue) {
		Long result = null;
		if (longValue != null) {
			if (longValue.getClass().equals(String.class)) {
				result = Long.parseLong(longValue.toString());
			}
			if (longValue.getClass().equals(BigInteger.class)) {
				result = ((BigInteger)longValue).longValue();
			} 
		} 
		return result;
	}
	
	public static String ObjectToString(Object stringValue) {
		String result = null;
		if (stringValue != null) {
			result = stringValue.toString();
		}
		return result;
	}
	
	public static BigDecimal ObjectToBigDecimal(Object bigDecimalValue) {
		BigDecimal result = null;
		if (bigDecimalValue != null) {
			result = new BigDecimal(bigDecimalValue.toString());
		}
		return result;
	}
	
}
