/**
 * Copyright 2013 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2013-10-14 下午7:21:33
 */
package org.hibernate.type;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.hibernate.MappingException;
import org.hibernate.internal.util.ReflectHelper;

import com.absir.core.kernel.KernelObject;
import com.absir.orm.value.JiType;

/**
 * @author absir
 * 
 */
@SuppressWarnings({ "serial" })
public class TypeResolverLocal extends TypeResolver {

	/** fieldTypes */
	private Set<JiType> jiTypes = new HashSet<JiType>();

	/**
	 * @param typeResolver
	 */
	public TypeResolverLocal(TypeResolver typeResolver) {
		KernelObject.clone(typeResolver, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hibernate.type.TypeResolver#registerTypeOverride(org.hibernate.type
	 * .BasicType)
	 */
	@Override
	public void registerTypeOverride(BasicType type) {
		super.registerTypeOverride(type);
		if (type instanceof JiType) {
			jiTypes.add((JiType) type);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hibernate.type.TypeResolver#heuristicType(java.lang.String,
	 * java.util.Properties)
	 */
	@Override
	public Type heuristicType(String typeName, Properties parameters) throws MappingException {
		Type type = basic(typeName);
		if (type != null) {
			return type;
		}

		try {
			Class<?> typeClass = ReflectHelper.classForName(typeName);
			if (typeClass != null) {
				type = getTypeFactory().byClass(typeClass, parameters);
				if (type != null) {
					return type;
				}

				for (JiType jiType : jiTypes) {
					type = jiType.byClass(typeClass, parameters);
					if (type != null) {
						return type;
					}
				}
			}

		} catch (ClassNotFoundException ignore) {
		}

		return null;
	}

}
