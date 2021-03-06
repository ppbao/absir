/**
 * Copyright 2014 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2014年9月10日 下午4:54:30
 */
package com.absir.appserv.advice;

import java.lang.reflect.Method;

import com.absir.aop.AopProxyHandler;

/**
 * @author absir
 *
 */
public abstract class MethodThrowable extends MethodAdvice {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.absir.appserv.advice.IMethodAdvice#before(com.absir.appserv.advice
	 * .AdviceInvoker, java.lang.Object, java.lang.reflect.Method,
	 * java.lang.Object[])
	 */
	@Override
	public Object before(AdviceInvoker invoker, Object proxy, Method method, Object[] args) throws Throwable {
		// TODO Auto-generated method stub
		try {
			return AopProxyHandler.VOID;

		} catch (Exception e) {
			// TODO: handle exception
			return advice(proxy, method, args, e);
		}
	}

	/**
	 * @param proxy
	 * @param returnValue
	 * @param method
	 * @param args
	 * @return
	 */
	public abstract Object advice(Object proxy, Method method, Object[] args, Throwable e) throws Throwable;
}
