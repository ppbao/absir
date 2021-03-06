/**
 * Copyright 2014 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2014年9月10日 下午4:54:30
 */
package com.absir.appserv.advice;

import java.lang.reflect.Method;

/**
 * @author absir
 *
 */
public abstract class MethodAfter extends MethodAdvice {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.absir.appserv.advice.IMethodAdvice#after(java.lang.Object,
	 * java.lang.Object, java.lang.reflect.Method, java.lang.Object[],
	 * java.lang.Throwable)
	 */
	@Override
	public Object after(Object proxy, Object returnValue, Method method, Object[] args, Throwable e) throws Throwable {
		// TODO Auto-generated method stub
		if (e == null) {
			advice(proxy, returnValue, method, args);
		}

		return returnValue;
	}

	/**
	 * @param proxy
	 * @param returnValue
	 * @param method
	 * @param args
	 */
	public abstract void advice(Object proxy, Object returnValue, Method method, Object[] args);
}
