/**
 * Copyright 2013 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2013-6-10 上午11:56:50
 */
package com.absir.orm.value;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.absir.core.kernel.KernelObject;
import com.absir.core.util.UtilAbsir;
import com.absir.orm.hibernate.SessionFactoryUtils;

/**
 * @author absir
 * 
 */
@SuppressWarnings("serial")
public class JoEntity implements Serializable {

	/** entityName */
	private String entityName;

	/** entityClass */
	private Class<?> entityClass;

	/**
	 * @param entityName
	 * @param entityClass
	 */
	public JoEntity(String entityName, Class<?> entityClass) {
		if (entityName == null) {
			entityName = SessionFactoryUtils.getJpaEntityName(entityClass);

		} else if (entityClass == null) {
			entityName = SessionFactoryUtils.getJpaEntityName(entityName);
			entityClass = SessionFactoryUtils.getEntityClass(entityName);
		}

		this.entityName = entityName;
		this.entityClass = entityClass;
	}

	/**
	 * @return the entityName
	 */
	public String getEntityName() {
		return entityName;
	}

	/**
	 * @return the entityClass
	 */
	public Class<?> getEntityClass() {
		return entityClass;
	}

	/** Jo_Entity_Map_Token */
	private static final Map<JoEntity, Object> Jo_Entity_Map_Token = new HashMap<JoEntity, Object>();

	/**
	 * @return
	 */
	public Object getEntityToken() {
		return entityClass == null ? UtilAbsir.getToken(entityName, Jo_Entity_Map_Token) : entityClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return entityName + ":" + entityClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return KernelObject.hashCode(entityName) + KernelObject.hashCode(entityClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj != null && obj instanceof JoEntity) {
			JoEntity joEntity = (JoEntity) obj;
			return KernelObject.equals(entityName, joEntity.entityName) && KernelObject.equals(entityClass, joEntity.entityClass);
		}

		return false;
	}
}
