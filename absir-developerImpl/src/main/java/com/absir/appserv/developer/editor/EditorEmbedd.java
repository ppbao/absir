/**
 * Copyright 2014 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2014-4-30 下午5:23:31
 */
package com.absir.appserv.developer.editor;

import com.absir.appserv.system.bean.value.JaEmbedd;
import com.absir.bean.inject.value.Bean;
import com.absir.core.dyna.DynaBinder;
import com.absir.property.PropertyResolverAbstract;

/**
 * @author absir
 * 
 */
@Bean
public class EditorEmbedd extends PropertyResolverAbstract<EditorObject, JaEmbedd> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.absir.property.PropertyResolverAbstract#getPropertyObjectAnnotation
	 * (com.absir.property.PropertyObject, java.lang.annotation.Annotation)
	 */
	@Override
	public EditorObject getPropertyObjectAnnotation(EditorObject propertyObject, JaEmbedd annotation) {
		// TODO Auto-generated method stub
		if (propertyObject == null) {
			propertyObject = new EditorObject();
		}

		propertyObject.setEmbedd(true);
		return propertyObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.absir.property.PropertyResolverAbstract#getPropertyObjectAnnotationValue
	 * (com.absir.property.PropertyObject, java.lang.String)
	 */
	@Override
	public EditorObject getPropertyObjectAnnotationValue(EditorObject propertyObject, String annotationValue) {
		// TODO Auto-generated method stub
		if (propertyObject == null) {
			propertyObject = new EditorObject();
		}

		propertyObject.setEmbedd(DynaBinder.to(annotationValue, boolean.class));
		return propertyObject;
	}

}
