/**
 * Copyright 2014 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2014-4-30 下午5:23:31
 */
package com.absir.appserv.developer.editor;

import java.util.Map;

import com.absir.appserv.system.bean.value.JaEdit;
import com.absir.appserv.system.helper.HelperJson;
import com.absir.bean.inject.value.Bean;
import com.absir.property.PropertyResolverAbstract;

/**
 * @author absir
 * 
 */
@Bean
public class EditorEdit extends PropertyResolverAbstract<EditorObject, JaEdit> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.absir.property.PropertyResolverAbstract#getPropertyObjectAnnotation
	 * (com.absir.property.PropertyObject, java.lang.annotation.Annotation)
	 */
	@Override
	public EditorObject getPropertyObjectAnnotation(EditorObject propertyObject, JaEdit annotation) {
		// TODO Auto-generated method stub
		if (propertyObject == null) {
			propertyObject = new EditorObject();
		}

		propertyObject.setEdit(annotation);
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

		Map<?, ?> map = HelperJson.decodeMap(annotationValue);
		propertyObject.setMetas(map);
		return propertyObject;
	}
}
