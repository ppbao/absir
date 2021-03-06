/**
 * Copyright 2014 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2014-3-3 下午3:30:15
 */
package com.absir.orm.transaction;

import java.lang.reflect.Method;
import java.util.Iterator;

import net.sf.cglib.proxy.MethodProxy;

import com.absir.aop.AopInterceptor;
import com.absir.aop.AopInterceptorAbstract;
import com.absir.aop.AopProxyHandler;

/**
 * @author absir
 * 
 */
@SuppressWarnings("rawtypes")
public class TransactionInterceptor extends AopInterceptorAbstract<TransactionManager> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.absir.aop.AopInterceptorAbstract#before(java.lang.Object,
	 * java.util.Iterator, java.lang.Object, com.absir.aop.AopProxyHandler,
	 * java.lang.reflect.Method, java.lang.Object[],
	 * net.sf.cglib.proxy.MethodProxy)
	 */
	@Override
	public Object before(Object proxy, Iterator<AopInterceptor> iterator, TransactionManager interceptor, AopProxyHandler proxyHandler, Method method, Object[] args, MethodProxy methodProxy)
			throws Throwable {
		// TODO Auto-generated method stub
		interceptor.open();
		return AopProxyHandler.VOID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.absir.aop.AopInterceptorAbstract#after(java.lang.Object,
	 * java.lang.Object, java.lang.Object, com.absir.aop.AopProxyHandler,
	 * java.lang.reflect.Method, java.lang.Object[], java.lang.Throwable)
	 */
	@Override
	public Object after(Object proxy, Object returnValue, TransactionManager interceptor, AopProxyHandler proxyHandler, Method method, Object[] args, Throwable e) throws Throwable {
		// TODO Auto-generated method stub
		e = interceptor.close(e);
		if (e != null) {
			throw e;
		}

		return returnValue;
	}
}
