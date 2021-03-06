/**
 * Copyright 2013 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2013-10-17 下午1:23:18
 */
package com.absir.appserv.configure.xls;

import java.lang.reflect.Field;
import java.util.Map;

import com.absir.appserv.system.helper.HelperString;
import com.absir.core.dyna.DynaBinder;

/**
 * @author absir
 * 
 */
public class XlsAccessorParamMap extends XlsAccessorParam {

	/** keyClass */
	private Class<?> keyClass;

	/** valueClass */
	private Class<?> valueClass;

	/**
	 * @param field
	 * @param beanClass
	 */
	public XlsAccessorParamMap(Field field, Class<?> beanClass, Class<?> valueClass) {
		super(field, beanClass);
		// TODO Auto-generated constructor stub
		this.keyClass = beanClass;
		this.valueClass = valueClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.absir.appserv.configure.xls.XlsAccessorParam#getParamValues(java.
	 * lang.String)
	 */
	@Override
	protected Object getParamValues(String value) {
		return DynaBinder.INSTANCE.bind(HelperString.paramMap(value, keyClass, valueClass), null, getField().getGenericType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.absir.appserv.configure.xls.XlsAccessorParam#getValueParams(java.
	 * lang.Object)
	 */
	@Override
	protected String getValueParams(Object value) {
		return HelperString.paramMap((Map<?, ?>) value);
	}
}
