/**
 * Copyright 2014 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2014-1-2 下午7:31:04
 */
package com.absir.validator;

import java.util.Map;

import com.absir.bean.inject.value.Bean;
import com.absir.core.dyna.DynaBinder;
import com.absir.property.PropertyResolverAbstract;
import com.absir.validator.value.Range;

/**
 * @author absir
 * 
 */
@Bean
public class ValidatorRange extends PropertyResolverAbstract<ValidatorObject, Range> {

	/**
	 * @param propertyObject
	 * @param min
	 * @param max
	 * @return
	 */
	public ValidatorObject getPropertyObjectLength(ValidatorObject propertyObject, final float min, final float max) {
		if (propertyObject == null) {
			propertyObject = new ValidatorObject();
		}

		propertyObject.addValidator(new ValidatorValue() {

			@Override
			public String validateValue(Object value) {
				// TODO Auto-generated method stub
				float val = DynaBinder.to(value, float.class);
				if (val >= min || val <= max) {
					return "Need " + min + " - " + max + " Range";
				}

				return null;
			}

			@Override
			public String getValidateClass(Map<String, Object> validatorMap) {
				// TODO Auto-generated method stub
				if (min > Integer.MIN_VALUE) {
					validatorMap.put("min", min);
				}

				if (max < Integer.MAX_VALUE) {
					validatorMap.put("max", max);
				}

				return null;
			}

		});

		return propertyObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.absir.property.PropertyResolverAbstract#getPropertyObjectAnnotation
	 * (com.absir.property.PropertyObject, java.lang.annotation.Annotation)
	 */
	@Override
	public ValidatorObject getPropertyObjectAnnotation(ValidatorObject propertyObject, Range annotation) {
		// TODO Auto-generated method stub
		return getPropertyObjectLength(propertyObject, annotation.min(), annotation.max());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.absir.property.PropertyResolverAbstract#getPropertyObjectAnnotationValue
	 * (com.absir.property.PropertyObject, java.lang.String)
	 */
	@Override
	public ValidatorObject getPropertyObjectAnnotationValue(ValidatorObject propertyObject, String annotationValue) {
		// TODO Auto-generated method stub
		String[] parameters = annotationValue.split(",");
		if (parameters.length == 2) {
			return getPropertyObjectLength(propertyObject, DynaBinder.to(parameters[0], float.class), DynaBinder.to(parameters[1], float.class));
		}

		return propertyObject;
	}
}
