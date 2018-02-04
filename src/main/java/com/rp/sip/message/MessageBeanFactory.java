package com.rp.sip.message;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.msgpack.MessagePack;

public class MessageBeanFactory extends AbstractFactory {

	public MessageBeanFactory() {
		super();
	}

	@Override
	public boolean createObject(JXPathContext context, Pointer pointer, Object parent, String name, int index) {
		
		if (context.getValue("/" + name) == null) {
			return createObject(parent, name);
		} else {
			return true;
		}
	}

	private boolean createObject(Object parent, String name) {
		try {
			
			String packageName = parent.getClass().getPackage().getName();
			String subjectClasses = name.replaceFirst(Character.toString(name.charAt(0)),
					Character.toString(Character.toUpperCase(name.charAt(0))));
			System.out.println(parent);
			String qualifiedClazz = packageName + "." + subjectClasses;
			Class clz = Class.forName(qualifiedClazz);
			BeanUtils.setProperty(parent, name, clz.newInstance());
			
			return true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return false;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return false;
		} catch (InstantiationException e) {
			e.printStackTrace();
			return false;
		}
	}

}
