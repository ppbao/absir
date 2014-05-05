/**
 * Copyright 2014 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2014-1-6 上午10:34:12
 */
package com.absir.property;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.absir.bean.core.BeanFactoryUtils;
import com.absir.core.kernel.KernelReflect;
import com.absir.core.util.UtilAccessor;
import com.absir.core.util.UtilAccessor.Accessor;

/**
 * @author absir
 * 
 */
public class Property {

	/** field */
	private Field field;

	/** accessor */
	private Accessor accessor;

	/** type */
	private Class<?> type;

	/** hidden <= -2 allow -1(set) 0(set|get) 1(get) */
	private int allow;

	/** include */
	private int include;

	/** exclude */
	private int exclude;

	/** beanName */
	private String beanName;

	/** propertyConvert */
	private PropertyConvert propertyConvert;

	/**
	 * @param beanClass
	 * @param name
	 * @param include
	 * @param exclude
	 * @param beanName
	 * @param factoryClass
	 */
	public Property(Class<?> beanClass, String name, int include, int exclude, String beanName, Class<? extends PropertyFactory> factoryClass) {
		field = KernelReflect.declaredField(beanClass, name);
		accessor = UtilAccessor.getAccessor(beanClass, name, field);
		if (field == null || !Modifier.isPublic(field.getModifiers())) {
			if (accessor == null) {
				allow = -2;

			} else {
				if (accessor.getGetter() == null) {
					allow = accessor.getSetter() == null ? -2 : -1;

				} else {
					allow = accessor.getSetter() == null ? 1 : 0;
				}
			}
		}

		if (allow > -2) {
			if (accessor.getSetter() == null) {
				if (accessor.getGetter() != null) {
					type = accessor.getGetter().getReturnType();
				}

			} else {
				type = accessor.getSetter().getParameterTypes()[0];
			}

			if (field != null) {
				if (type.isAssignableFrom(field.getType())) {
					type = field.getType();
				}
			}

			this.include = include;
			this.exclude = exclude;
			this.beanName = beanName;
			this.propertyConvert = factoryClass == null ? null : BeanFactoryUtils.getBeanTypeInstance(factoryClass).getPropertyConvert(this);
		}
	}

	/**
	 * @return the allow
	 */
	public int getAllow() {
		return allow;
	}

	/**
	 * @return the field
	 */
	public Field getField() {
		return field;
	}

	/**
	 * @return the type
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * @return the accessor
	 */
	public Accessor getAccessor() {
		return accessor;
	}

	/**
	 * @return the include
	 */
	public int getInclude() {
		return include;
	}

	/**
	 * @return the exclude
	 */
	public int getExclude() {
		return exclude;
	}

	/**
	 * @param group
	 * @return
	 */
	public boolean allow(int group) {
		return group == 0 || ((exclude & group) == 0 && (include == 0 || (include & group) != 0));
	}

	/**
	 * @return the toName
	 */
	public String getBeanName() {
		return beanName;
	}

	/**
	 * @param propertyValue
	 * @return
	 */
	public Object getValue(Object propertyValue) {
		return propertyValue == null || propertyConvert == null ? propertyValue : propertyConvert.getValue(propertyValue);
	}

	/**
	 * @param value
	 * @param beanName
	 * @return
	 */
	public Object getPropertyValue(Object value, String beanName) {
		return value == null || propertyConvert == null ? value : propertyConvert.getPropertyValue(value, beanName);
	}
}
