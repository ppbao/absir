/**
 * Copyright 2013 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2013-9-9 上午10:42:56
 */
package com.absir.appserv.system.service.utils;

import com.absir.appserv.jdbc.JdbcCondition;
import com.absir.appserv.system.bean.base.JbPermission;
import com.absir.appserv.system.bean.base.JbStragety;
import com.absir.appserv.system.bean.proxy.JiUpdate;
import com.absir.appserv.system.bean.proxy.JiUserBase;
import com.absir.appserv.system.domain.DCondition;
import com.absir.orm.hibernate.SessionFactoryUtils;
import com.absir.orm.value.JePermission;

/**
 * @author absir
 * 
 */
public abstract class AccessServiceUtils {

	/**
	 * @param entityName
	 * @param jdbcCondition
	 * @return
	 */
	public static JdbcCondition selectCondition(String entityName, JdbcCondition jdbcCondition) {
		return selectCondition(entityName, SecurityServiceUtils.getUserBase(), jdbcCondition);
	}

	/**
	 * @param entityName
	 * @param user
	 * @param jdbcCondition
	 * @return
	 */
	public static JdbcCondition selectCondition(String entityName, JiUserBase user, JdbcCondition jdbcCondition) {
		return selectCondition(entityName, user, null, jdbcCondition);
	}

	/**
	 * 列表实体条件
	 * 
	 * @param entityName
	 * @param user
	 * @param condition
	 * @param jdbcCondition
	 * @return
	 */
	public static JdbcCondition selectCondition(String entityName, JiUserBase user, DCondition condition, JdbcCondition jdbcCondition) {
		return selectCondition(entityName, SessionFactoryUtils.getEntityClass(entityName), user, condition, jdbcCondition);
	}

	/**
	 * 
	 * @param entityName
	 * @param entityClass
	 * @param user
	 * @param permission
	 * @param condition
	 * @param jdbcCondition
	 * @return
	 */
	public static JdbcCondition selectCondition(String entityName, Class<?> entityClass, JiUserBase user, DCondition condition, JdbcCondition jdbcCondition) {
		if (entityClass == null) {
			return jdbcCondition;
		}

		if (jdbcCondition == null) {
			jdbcCondition = new JdbcCondition();
		}

		if (condition != null) {
			if (condition.getUpdateTime() > 0) {
				if (entityClass != null && JiUpdate.class.isAssignableFrom(entityClass)) {
					jdbcCondition.getConditions().add("o.updateTime >= ?");
					jdbcCondition.getConditions().add(condition.getUpdateTime());
				}

				if (condition.getCreateStrategies() != null) {
					AssocServiceUtils.assocConditions(JbStragety.class, entityName, user, JePermission.SELECT, condition.getCreateStrategies(), jdbcCondition);
				}
			}
		}

		AssocServiceUtils.assocConditions(JbPermission.class, entityName, user, JePermission.SELECT, jdbcCondition);
		if (condition != null && condition.isStrategy()) {
			AssocServiceUtils.assocConditions(JbStragety.class, entityName, user, JePermission.SELECT, condition.getMapStrategies(), jdbcCondition);
		}

		return jdbcCondition;
	}

	/**
	 * @param entityName
	 * @param jdbcCondition
	 * @return
	 */
	public static JdbcCondition updateCondition(String entityName, JdbcCondition jdbcCondition) {
		return updateCondition(entityName, SecurityServiceUtils.getUserBase(), jdbcCondition);
	}

	/**
	 * 编辑实体条件
	 * 
	 * @param entityName
	 * @param user
	 * @param jdbcCondition
	 * @return
	 */
	public static JdbcCondition updateCondition(String entityName, JiUserBase user, JdbcCondition jdbcCondition) {
		Class<?> entityClass = SessionFactoryUtils.getEntityClass(entityName);
		if (entityClass == null) {
			return jdbcCondition;
		}

		if (jdbcCondition == null) {
			jdbcCondition = new JdbcCondition();
		}

		AssocServiceUtils.assocConditions(JbPermission.class, entityName, user, JePermission.UPDATE, jdbcCondition);
		return jdbcCondition;
	}

	/**
	 * @param entityName
	 * @param jdbcCondition
	 * @return
	 */
	public static JdbcCondition deleteCondition(String entityName, JdbcCondition jdbcCondition) {
		return deleteCondition(entityName, SecurityServiceUtils.getUserBase(), jdbcCondition);
	}

	/**
	 * 删除实体条件
	 * 
	 * @param entityName
	 * @param user
	 * @param jdbcCondition
	 * @return
	 */
	public static JdbcCondition deleteCondition(String entityName, JiUserBase user, JdbcCondition jdbcCondition) {
		Class<?> entityClass = SessionFactoryUtils.getEntityClass(entityName);
		if (entityClass == null) {
			return jdbcCondition;
		}

		if (jdbcCondition == null) {
			jdbcCondition = new JdbcCondition();
		}

		AssocServiceUtils.assocConditions(JbPermission.class, entityName, user, JePermission.DELETE, jdbcCondition);
		return jdbcCondition;
	}

	/**
	 * @param entityName
	 * @param jdbcCondition
	 * @return
	 */
	public static JdbcCondition suggestCondition(String entityName, JdbcCondition jdbcCondition) {
		return suggestCondition(entityName, SecurityServiceUtils.getUserBase(), jdbcCondition);
	}

	/**
	 * 建议实体条件
	 * 
	 * @param entityName
	 * @param user
	 * @param jdbcCondition
	 * @return
	 */
	public static JdbcCondition suggestCondition(String entityName, JiUserBase user, JdbcCondition jdbcCondition) {
		Class<?> entityClass = SessionFactoryUtils.getEntityClass(entityName);
		if (entityClass == null) {
			return jdbcCondition;
		}

		if (jdbcCondition == null) {
			jdbcCondition = new JdbcCondition();
		}

		AssocServiceUtils.assocConditions(JbPermission.class, entityName, user, JePermission.INSERT, jdbcCondition);
		return jdbcCondition;
	}
}
