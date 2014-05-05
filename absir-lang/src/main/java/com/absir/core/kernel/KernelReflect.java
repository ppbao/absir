/**
 * Copyright 2013 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2013-3-5 下午2:31:23
 */
package com.absir.core.kernel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.absir.core.kernel.KernelLang.BreakException;
import com.absir.core.kernel.KernelLang.CallbackBreak;

/**
 * @author absir
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class KernelReflect {

	/**
	 * @param member
	 * @return
	 */
	public static <T extends Member> T memberStatic(T member) {
		return memberStatic(member, null);
	}

	/**
	 * @param member
	 * @param defaultValue
	 * @return
	 */
	public static <T extends Member> T memberStatic(T member, T defaultValue) {
		if (member == null || !Modifier.isStatic(member.getModifiers())) {
			return defaultValue;
		}

		return member;
	}

	/**
	 * @param member
	 * @return
	 */
	public static <T extends Member> T memberAccessor(T member) {
		return memberAccessor(member, null);
	}

	/**
	 * @param member
	 * @param defaultValue
	 * @return
	 */
	public static <T extends Member> T memberAccessor(T member, T defaultValue) {
		if (member == null || Modifier.isFinal(member.getModifiers()) || Modifier.isStatic(member.getModifiers())) {
			return defaultValue;
		}

		return member;
	}

	/**
	 * @param cls
	 * @param parameterTypes
	 * @return
	 */
	public static <T> Constructor<T> constructor(Class<T> cls, Class... parameterTypes) {
		return declaredConstructor(cls, false, parameterTypes);
	}

	/**
	 * @param cls
	 * @param parameterTypes
	 * @return
	 */
	public static <T> Constructor<T> declaredConstructor(Class<T> cls, Class... parameterTypes) {
		return declaredConstructor(cls, true, parameterTypes);
	}

	/**
	 * @param cls
	 * @param declared
	 * @param parameterTypes
	 * @return
	 */
	public static <T> Constructor<T> declaredConstructor(Class<T> cls, boolean declared, Class... parameterTypes) {
		try {
			Constructor<T> constructor = declared ? cls.getDeclaredConstructor(parameterTypes) : cls.getConstructor(parameterTypes);
			if (constructor != null) {
				if (declared && !constructor.isAccessible()) {
					constructor.setAccessible(true);
				}

				return constructor;
			}

		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}

		return null;
	}

	/**
	 * @param cls
	 * @param parameterTypes
	 * @return
	 */
	public static <T> Constructor<T> assignableConstructor(Class<T> cls, Class... parameterTypes) {
		return assignableConstructor(cls, false, parameterTypes);
	}

	/**
	 * @param cls
	 * @param declared
	 * @param parameterTypes
	 * @return
	 */
	public static <T> Constructor<T> assignableConstructor(Class<T> cls, boolean declared, Class... parameterTypes) {
		Constructor<T>[] constructors = (Constructor<T>[]) (declared ? cls.getDeclaredConstructors() : cls.getConstructors());
		int similar = -2;
		Constructor<T> constructor = null;
		for (Constructor<T> structor : constructors) {
			if (KernelClass.isMatchableFrom(structor.getParameterTypes(), parameterTypes)) {
				int sim = KernelClass.similar(parameterTypes, structor.getParameterTypes());
				if (similar < sim) {
					similar = sim;
					structor.setAccessible(true);
					constructor = structor;
				}
			}
		}

		return constructor;
	}

	/**
	 * @param cls
	 * @param declared
	 * @param assignable
	 * @param parameterTypes
	 * @return
	 */
	public static <T> Constructor<T> assignableConstructor(Class<T> cls, boolean declared, boolean assignable, Class... parameterTypes) {
		return assignable ? assignableConstructor(cls, declared, parameterTypes) : declaredConstructor(cls, declared, parameterTypes);
	}

	/**
	 * @param constructor
	 * @param initargs
	 * @return
	 */
	public static <T> T newInstance(Constructor<T> constructor, Object... initargs) {
		return newInstance(constructor, null, initargs);
	}

	/**
	 * @param constructor
	 * @param defaultValue
	 * @param initargs
	 * @return
	 */
	public static <T> T newInstance(Constructor<T> constructor, T defaultValue, Object... initargs) {
		if (constructor != null) {
			try {
				return constructor.newInstance(initargs);

			} catch (IllegalArgumentException e) {
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}

		return defaultValue;
	}

	/**
	 * @param cls
	 * @param name
	 * @return
	 */
	public static Field field(Class cls, String name) {
		return field(cls, name, 0);
	}

	/**
	 * @param cls
	 * @param name
	 * @param ancest
	 * @return
	 */
	public static Field field(Class cls, String name, int ancest) {
		return declaredField(cls, name, ancest, false);
	}

	/**
	 * @param cls
	 * @param name
	 * @return
	 */
	public static Field declaredField(Class cls, String name) {
		return declaredField(cls, name, 0);
	}

	/**
	 * @param cls
	 * @param name
	 * @param ancest
	 * @return
	 */
	public static Field declaredField(Class cls, String name, int ancest) {
		return declaredField(cls, name, ancest, true);
	}

	/**
	 * @param cls
	 * @param name
	 * @param ancest
	 * @param declared
	 * @return
	 */
	public static Field declaredField(Class cls, String name, int ancest, boolean declared) {
		Field field = null;
		while (cls != null) {
			try {
				field = declared ? cls.getDeclaredField(name) : cls.getField(name);
				break;

			} catch (SecurityException e) {
				break;

			} catch (NoSuchFieldException e) {
			}

			if (ancest < 1 || ancest-- != 1) {
				cls = cls.getSuperclass();

			} else {
				cls = null;
			}
		}

		if (field != null) {
			if (declared && !field.isAccessible()) {
				field.setAccessible(true);
			}
		}

		return field;
	}

	/**
	 * @param cls
	 * @return
	 */
	public static List<Field> fields(Class cls) {
		return fields(cls, 0);
	}

	/**
	 * @param cls
	 * @param ancest
	 * @return
	 */
	public static List<Field> fields(Class cls, int ancest) {
		return declaredFields(cls, ancest, false);
	}

	/**
	 * @param cls
	 * @return
	 */
	public static List<Field> declaredFields(Class cls) {
		return declaredFields(cls, 0);
	}

	/**
	 * @param cls
	 * @param ancest
	 * @return
	 */
	public static List<Field> declaredFields(Class cls, int ancest) {
		return declaredFields(cls, ancest, true);
	}

	/**
	 * @param cls
	 * @param ancest
	 * @param declared
	 * @return
	 */
	public static List<Field> declaredFields(Class cls, int ancest, boolean declared) {
		List<Field> declaredFields = new ArrayList<Field>();
		while (cls != null) {
			Field[] fields = declared ? cls.getDeclaredFields() : cls.getFields();
			for (Field field : fields) {
				if (declared && !field.isAccessible()) {
					field.setAccessible(true);
				}

				declaredFields.add(field);
			}

			if (ancest < 1 || ancest-- != 1) {
				cls = cls.getSuperclass();

			} else {
				cls = null;
			}
		}

		return declaredFields;
	}

	/**
	 * @param obj
	 * @param field
	 * @param value
	 * @return
	 */
	public static boolean set(Object obj, Field field, Object value) {
		if (field != null) {
			try {
				field.set(obj, value);
				return true;

			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
		}

		return false;
	}

	/**
	 * @param obj
	 * @param field
	 * @return
	 */
	public static Object get(Object obj, Field field) {
		return KernelReflect.get(obj, null, field);
	}

	/**
	 * @param obj
	 * @param defaultValue
	 * @param field
	 * @return
	 */
	public static Object get(Object obj, Object defaultValue, Field field) {
		if (field != null) {
			try {
				return field.get(obj);

			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
		}

		return defaultValue;
	}

	/** Class_Method_Map_Method */
	protected final static Map<String, Method> Class_Method_Map_Method = new HashMap<String, Method>();

	/**
	 * @param cls
	 * @param name
	 * @param parameterTypes
	 * @return
	 */
	public static Method method(Class cls, String name, Class... parameterTypes) {
		return method(cls, name, 0, parameterTypes);
	}

	/**
	 * @param cls
	 * @param name
	 * @param ancest
	 * @param parameterTypes
	 * @return
	 */
	public static Method method(Class cls, String name, int ancest, Class... parameterTypes) {
		return declaredMethod(cls, name, ancest, false, parameterTypes);
	}

	/**
	 * @param cls
	 * @param name
	 * @param parameterTypes
	 * @return
	 */
	public static Method declaredMethod(Class cls, String name, Class... parameterTypes) {
		return declaredMethod(cls, name, 0, parameterTypes);
	}

	/**
	 * @param cls
	 * @param name
	 * @param ancest
	 * @param parameterTypes
	 * @return
	 */
	public static Method declaredMethod(Class cls, String name, int ancest, Class... parameterTypes) {
		return declaredMethod(cls, name, ancest, true, parameterTypes);
	}

	/**
	 * @param cls
	 * @param name
	 * @param ancest
	 * @param declared
	 * @param parameterTypes
	 * @return
	 */
	public static Method declaredMethod(Class cls, String name, int ancest, boolean declared, Class... parameterTypes) {
		return assignableMethod(cls, name, ancest, declared, false, false, parameterTypes);
	}

	/**
	 * @param cls
	 * @param name
	 * @param parameterTypes
	 * @return
	 */
	public static Method assignableMethod(Class cls, String name, Class... parameterTypes) {
		return assignableMethod(cls, name, 0, parameterTypes);
	}

	/**
	 * @param cls
	 * @param name
	 * @param ancest
	 * @param parameterTypes
	 * @return
	 */
	public static Method assignableMethod(Class cls, String name, int ancest, Class... parameterTypes) {
		return assignableMethod(cls, name, ancest, false, parameterTypes);
	}

	/**
	 * @param cls
	 * @param name
	 * @param ancest
	 * @param declared
	 * @param parameterTypes
	 * @return
	 */
	public static Method assignableMethod(Class cls, String name, int ancest, boolean declared, Class... parameterTypes) {
		return assignableMethod(cls, name, ancest, declared, true, true, parameterTypes);
	}

	/**
	 * @param cls
	 * @param name
	 * @param ancest
	 * @param declared
	 * @param assignable
	 * @param cacheable
	 * @param parameterTypes
	 * @return
	 */
	public static Method assignableMethod(Class cls, String name, int ancest, boolean declared, boolean assignable, boolean cacheable, Class... parameterTypes) {
		return assignableMethod(cls, null, name, ancest, declared, assignable, cacheable, parameterTypes);
	}

	/**
	 * @param cls
	 * @param name
	 * @param ancest
	 * @param declared
	 * @param assignable
	 * @param cacheable
	 * @param parameterTypes
	 * @return
	 */
	public static Method assignableMethod(Class cls, final Class returnType, final String name, int ancest, boolean declared, boolean assignable, boolean cacheable, final Class... parameterTypes) {
		String class_method_key = null;
		if (cacheable) {
			class_method_key = cls.getName() + ":" + name + ":" + parameterTypes.length;
			Method method = Class_Method_Map_Method.get(class_method_key);
			if (method != null && KernelClass.isMatchableFrom(method.getParameterTypes(), parameterTypes)) {
				return method;
			}
		}

		Method method = null;
		while (cls != null) {
			if (assignable && (returnType != null || parameterTypes.length > 0)) {
				final List<Method> matchMethods = new ArrayList<Method>();
				CallbackBreak<Method> callback = new CallbackBreak<Method>() {

					@Override
					public void doWith(Method template) throws BreakException {
						// TODO Auto-generated method stub
						if (template.getName().equals(name) && KernelClass.isMatchableFrom(template.getParameterTypes(), parameterTypes)) {
							// for (Method method : matchMethods) {
							// if (KernelArray.equal(method.getParameterTypes(),
							// template.getParameterTypes())) {
							// return;
							// }
							// }
							if (returnType == null || returnType.isAssignableFrom(template.getReturnType())) {
								matchMethods.add(template);
							}
						}
					}

				};

				if (declared) {
					doWithDeclaredMethods(cls, callback);

				} else {
					doWithMethods(cls, callback);
				}

				if (matchMethods.size() > 0) {
					if (matchMethods.size() == 1) {
						method = matchMethods.get(0);

					} else {
						cacheable = false;
						int similar = -2;
						for (Method match : matchMethods) {
							int sim = KernelClass.similar(match.getParameterTypes(), parameterTypes);
							if (returnType != null) {
								sim += KernelClass.similar(returnType, match.getReturnType());
							}

							if (similar < sim) {
								similar = sim;
								method = match;
							}
						}
					}
				}

			} else {
				try {
					method = declared ? cls.getDeclaredMethod(name, parameterTypes) : cls.getMethod(name, parameterTypes);
					break;

				} catch (SecurityException e) {
					break;

				} catch (NoSuchMethodException e) {
				}
			}

			if (ancest < 1 || ancest-- != 1) {
				cls = cls.getSuperclass();

			} else {
				cls = null;
			}
		}

		if (method != null) {
			if (declared && !method.isAccessible()) {
				method.setAccessible(true);
			}

			if (cacheable) {
				Class_Method_Map_Method.put(class_method_key, method);
			}
		}

		return method;
	}

	/**
	 * @param obj
	 * @param method
	 * @param args
	 * @return
	 */
	public static Object invoke(Object obj, Method method, Object... args) {
		return KernelReflect.invoke(obj, null, method, args);
	}

	/**
	 * @param obj
	 * @param defaultValue
	 * @param method
	 * @param args
	 * @return
	 */
	public static Object invoke(Object obj, Object defaultValue, Method method, Object... args) {
		if (method != null) {
			try {
				return method.invoke(obj, args);

			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}

		return defaultValue;
	}

	/**
	 * @param obj
	 * @param method
	 * @param args
	 * @return
	 */
	public static boolean run(Object obj, Method method, Object... args) {
		if (method != null) {
			try {
				method.invoke(obj, args);
				return true;

			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}

		return false;
	}

	/**
	 * @param cls
	 * @param callback
	 */
	public static void doWithClasses(Class cls, CallbackBreak<Class<?>> callback) {
		doWithClasses(cls, callback, null);
	}

	/**
	 * @param cls
	 * @param callback
	 * @param superClass
	 */
	public static void doWithClasses(Class cls, CallbackBreak<Class<?>> callback, Class superClass) {
		try {
			while (cls != null && cls != Object.class && cls != superClass) {
				callback.doWith(cls);
				cls = cls.getSuperclass();

			}
		} catch (BreakException e) {
		}
	}

	/**
	 * @param cls
	 * @param callback
	 */
	public static void doWithFields(Class cls, CallbackBreak<Field> callback) {
		doWithFields(cls, callback, null);
	}

	/**
	 * @param cls
	 * @param callback
	 * @param superClass
	 */
	public static void doWithFields(Class cls, CallbackBreak<Field> callback, Class superClass) {
		try {
			while (cls != null && cls != Object.class && cls != superClass) {
				for (Field field : cls.getFields()) {
					callback.doWith(field);
				}

				cls = cls.getSuperclass();
			}
		} catch (BreakException e) {
		}
	}

	/**
	 * @param cls
	 * @param callback
	 */
	public static void doWithDeclaredFields(Class cls, CallbackBreak<Field> callback) {
		try {
			for (Field field : cls.getDeclaredFields()) {
				callback.doWith(field);
			}

		} catch (BreakException e) {
		}
	}

	/**
	 * @param cls
	 * @param callback
	 */
	public static void doWithMethods(Class cls, CallbackBreak<Method> callback) {
		try {
			for (Method method : cls.getMethods()) {
				callback.doWith(method);
			}

		} catch (BreakException e) {
		}
	}

	/**
	 * @param cls
	 * @param callback
	 */
	public static void doWithDeclaredMethods(Class cls, CallbackBreak<Method> callback) {
		doWithDeclaredMethods(cls, callback, null);
	}

	/**
	 * @param cls
	 * @param callback
	 * @param superClass
	 */
	public static void doWithDeclaredMethods(Class cls, CallbackBreak<Method> callback, Class superClass) {
		try {
			while (cls != null && cls != Object.class && cls != superClass) {
				for (Method method : cls.getDeclaredMethods()) {
					callback.doWith(method);
				}

				cls = cls.getSuperclass();
			}

		} catch (BreakException e) {

		}
	}
}
