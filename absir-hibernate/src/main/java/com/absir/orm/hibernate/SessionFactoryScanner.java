/**
 * Copyright 2014 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2014-1-21 下午7:09:05
 */
package com.absir.orm.hibernate;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.persistence.Entity;

import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.internal.SessionFactoryImpl;

import com.absir.bean.basis.Basis;
import com.absir.bean.basis.BeanDefine;
import com.absir.bean.config.IBeanDefineSupply;
import com.absir.bean.config.IBeanFactoryAware;
import com.absir.bean.core.BeanFactoryImpl;
import com.absir.bean.inject.InjectOnce;
import com.absir.bean.inject.value.Bean;
import com.absir.bean.inject.value.Inject;
import com.absir.core.kernel.KernelArray;
import com.absir.core.kernel.KernelLang;
import com.absir.orm.hibernate.boost.EntityAssoc;
import com.absir.orm.value.JaConfig;

/**
 * @author absir
 * 
 */
@SuppressWarnings("unchecked")
@Basis
@Bean
public class SessionFactoryScanner implements IBeanDefineSupply, IBeanFactoryAware, InjectOnce {

	/** entityClasses */
	private List<Class<?>> entityClasses = new ArrayList<Class<?>>();

	/**
	 * @param sessionFactoryBoost
	 */
	@Inject
	private void setSessionFactoryBoost(SessionFactoryBoost sessionFactoryBoost) {
		ConfigurationBoost.sessionFactoryBoost = sessionFactoryBoost;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.absir.core.kernel.KernelList.Orderable#getOrder()
	 */
	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return -256;
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
		if (beanType.getAnnotation(Entity.class) != null) {
			entityClasses.add(beanType);
			return KernelLang.NULL_LIST_SET;
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.absir.bean.config.IBeanFactoryAware#beforeRegister(com.absir.bean
	 * .core.BeanFactoryImpl)
	 */
	@Override
	public void beforeRegister(BeanFactoryImpl beanFactory) {
		// TODO Auto-generated method stub
		File hibernate = new File(beanFactory.getBeanConfig().getClassPath() + "hibernate");
		if (hibernate.exists() && hibernate.isDirectory()) {
			final List<String> names = new ArrayList<String>();
			File[] configFiles = hibernate.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					// TODO Auto-generated method stub
					int length = name.length();
					if (length > 9 && name.endsWith(".cfg.xml")) {
						name = name.substring(0, length - 8);
						if ("hibernate".equals(name)) {
							name = KernelLang.NULL_STRING;
						}

						names.add(name);
						return true;
					}

					return false;
				}
			});

			List<Configuration> configs = new ArrayList<Configuration>();
			for (File configFile : configFiles) {
				ConfigurationBoost configuration = new ConfigurationBoost();
				configs.add(configuration);
				configuration.configure(configFile);

			}

			int size = names.size();
			for (Class<?> entityClass : entityClasses) {
				JaConfig jaConfig = entityClass.getAnnotation(JaConfig.class);
				if (jaConfig != null && jaConfig.value().length == 0) {
					jaConfig = null;
				}

				for (int i = 0; i < size; i++) {
					String name = names.get(i);
					Configuration config = configs.get(i);
					if (jaConfig == null) {
						if (name == KernelLang.NULL_STRING) {
							config.addAnnotatedClass(entityClass);
							break;
						}

					} else if (KernelArray.contain(jaConfig.value(), name)) {
						config.addAnnotatedClass(entityClass);
					}
				}
			}

			for (int i = 0; i < size; i++) {
				String name = names.get(i);
				Configuration config = configs.get(i);
				Properties properties = config.getProperties();
				for (Entry<Object, Object> entry : properties.entrySet()) {
					Object value = entry.getValue();
					if (value != null && value instanceof String) {
						entry.setValue(beanFactory.getBeanConfig().getExpression((String) value));
					}
				}

				/**
				 * new ServiceRegistryBuilder().applySettings(properties).
				 * buildServiceRegistry() (SessionFactoryImpl)
				 * config.buildSessionFactory(new
				 * StandardServiceRegistryBuilder(
				 * ).applySettings(properties).build());
				 */
				SessionFactoryImpl sessionFactory = (SessionFactoryImpl) config.buildSessionFactory(new StandardServiceRegistryBuilder().applySettings(properties).build());
				if (name == KernelLang.NULL_STRING) {
					SessionFactoryUtils.get().sessionFactory = sessionFactory;

				} else {
					SessionFactoryUtils.get().nameMapSessionFactory.put(name, sessionFactory);
				}
			}
		}

		entityClasses.clear();
		EntityAssoc.boost(SessionFactoryUtils.get());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.absir.bean.config.IBeanFactoryAware#afterRegister(com.absir.bean.
	 * core.BeanFactoryImpl)
	 */
	@Override
	public void afterRegister(BeanFactoryImpl beanFactory) {
		// TODO Auto-generated method stub
	}
}
