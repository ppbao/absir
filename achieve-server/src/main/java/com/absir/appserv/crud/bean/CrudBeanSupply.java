/**
 * Copyright 2014 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2014-5-23 下午4:00:57
 */
package com.absir.appserv.crud.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.absir.appserv.crud.CrudSupply;
import com.absir.appserv.feature.menu.value.MaEntity;
import com.absir.bean.basis.Basis;
import com.absir.bean.basis.BeanDefine;
import com.absir.bean.config.IBeanDefineSupply;
import com.absir.bean.core.BeanFactoryImpl;
import com.absir.bean.inject.value.Bean;
import com.absir.core.kernel.KernelClass;
import com.absir.core.kernel.KernelLang;
import com.absir.orm.hibernate.SessionFactoryUtils;
import com.absir.orm.value.JaEntity;

/**
 * @author absir
 * 
 */
@SuppressWarnings("unchecked")
@Bean
@Basis
public class CrudBeanSupply extends CrudSupply implements IBeanDefineSupply {

	/** Crud_Map_Class */
	protected static Map<String, Class<? extends CrudBean>> Crud_Map_Class = new HashMap<String, Class<? extends CrudBean>>();

	/**
	 * @return the Crud_Map_Class
	 */
	public static Map<String, Class<? extends CrudBean>> getCrudMapClass() {
		return Crud_Map_Class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.absir.core.kernel.KernelList.Orderable#getOrder()
	 */
	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 32;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.absir.bean.config.IBeanDefineSupply#getBeanDefines(com.absir.bean
	 * .core.BeanFactoryImpl, java.lang.Class)
	 */
	@Override
	public List<BeanDefine> getBeanDefines(BeanFactoryImpl beanFactory, Class<?> beanType) {
		// TODO Auto-generated method stub
		if (CrudBean.class.isAssignableFrom(beanType)) {
			JaEntity jaEntity = beanType.getAnnotation(JaEntity.class);
			if (jaEntity != null || beanType.getAnnotation(MaEntity.class) != null) {
				String entityName = beanType.getSimpleName();
				Crud_Map_Class.put(entityName, (Class<? extends CrudBean>) beanType);
				if (jaEntity != null && jaEntity.permissions().length > 0) {
					SessionFactoryUtils.get().getNameMapPermissions().put(entityName, jaEntity.permissions());
				}

				return KernelLang.NULL_LIST_SET;
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.absir.appserv.crud.ICrudSupply#getEntityClass(java.lang.String)
	 */
	@Override
	public Class<?> getEntityClass(String entityName) {
		// TODO Auto-generated method stub
		return Crud_Map_Class.get(entityName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.absir.appserv.crud.ICrudSupply#create(java.lang.String)
	 */
	@Override
	public Object create(String entityName) {
		// TODO Auto-generated method stub
		return KernelClass.newInstance(Crud_Map_Class.get(entityName));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.absir.appserv.crud.ICrudSupply#mergeEntity(java.lang.String,
	 * java.lang.Object, boolean)
	 */
	@Override
	public void mergeEntity(String entityName, Object entity, boolean create) {
		// TODO Auto-generated method stub
		((CrudBean) entity).merge();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.absir.appserv.crud.ICrudSupply#deleteEntity(java.lang.String,
	 * java.lang.Object)
	 */
	@Override
	public void deleteEntity(String entityName, Object entity) {
		// TODO Auto-generated method stub
		// ((CrudBean) entity).delete();
	}
}