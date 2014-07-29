/**
 * Copyright 2013 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2013-6-8 下午1:09:42
 */
package com.absir.appserv.support;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.absir.appserv.support.developer.IDeveloper;
import com.absir.appserv.system.helper.HelperString;
import com.absir.bean.basis.Configure;
import com.absir.bean.core.BeanFactoryUtils;
import com.absir.core.kernel.KernelLang.CallbackTemplate;
import com.absir.core.kernel.KernelLang.ObjectEntry;
import com.absir.core.kernel.KernelObject;
import com.absir.core.kernel.KernelString;
import com.absir.core.util.UtilFile;
import com.absir.orm.value.JoEntity;

/**
 * @author absir
 * 
 */
@SuppressWarnings("rawtypes")
@Configure
public abstract class Developer {

	/**
	 * @return
	 */
	public static boolean isDeveloper() {
		return IDeveloper.ME != null;
	}

	/** CLASS_FILE_EXTENSION */
	public static final String CLASS_FILE_EXTENSION = ".class";

	/**
	 * @param cls
	 * @return
	 */
	public static File getClassFile(Class cls) {
		File file = new File(cls.getResource(cls.getSimpleName().concat(CLASS_FILE_EXTENSION)).getFile());
		if (!file.exists()) {
			file = new File(cls.getProtectionDomain().getCodeSource().getLocation().getFile());
		}

		return file;
	}

	/**
	 * @param joEntity
	 * @return
	 */
	public static Long lastModified(JoEntity joEntity) {
		return getClassFile(joEntity.getClass()).lastModified();
	}

	/** RUNTIME_PATH */
	public static final String RUNTIME_PATH = "META-RUNTIME/";

	/** RUMTIME_LISTENERS */
	private static final List<CallbackTemplate<Entry<String, File>>> RUMTIME_LISTENERS = new ArrayList<CallbackTemplate<Entry<String, File>>>();

	/**
	 * @param listener
	 */
	public synchronized static void addListener(CallbackTemplate<Entry<String, File>> listener) {
		RUMTIME_LISTENERS.add(listener);
	}

	/**
	 * @param listener
	 */
	public synchronized static void removeListener(CallbackTemplate<Entry<String, File>> listener) {
		RUMTIME_LISTENERS.remove(listener);
	}

	/**
	 * @param entry
	 */
	public static void doEntry(Entry<String, File> entry) {
		for (CallbackTemplate<Entry<String, File>> listener : RUMTIME_LISTENERS) {
			listener.doWith(entry);
		}
	}

	/**
	 * @param file
	 */
	public static void doEntry(File file) {
		String path = file.getPath();
		path = HelperString.substringAfter(path, BeanFactoryUtils.getBeanConfig().getClassPath());
		if (!KernelString.isEmpty(path)) {
			doEntry(new ObjectEntry<String, File>(path, file));
		}
	}

	/**
	 * @param cls
	 * @param propertyName
	 * @return
	 */
	private static File getRuntimeFile(String runtimeName) {
		return new File(BeanFactoryUtils.getBeanConfig().getClassPath() + RUNTIME_PATH + runtimeName);
	}

	/**
	 * @param runtimeName
	 * @return
	 */
	public static Object getRuntime(String runtimeName) {
		try {
			return KernelObject.unserialize(UtilFile.read(getRuntimeFile(runtimeName)));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param runtimeName
	 * @param value
	 */
	public synchronized static void setRuntime(String runtimeName, Object value) {
		if (value == null) {
			return;
		}

		try {
			File runtimeFile = getRuntimeFile(runtimeName);
			UtilFile.write(runtimeFile, KernelObject.serialize(value));
			if (RUMTIME_LISTENERS.size() > 0) {
				ObjectEntry<String, File> entry = new ObjectEntry<String, File>();
				entry.setKey(RUNTIME_PATH + runtimeName);
				entry.setValue(runtimeFile);
				doEntry(entry);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
