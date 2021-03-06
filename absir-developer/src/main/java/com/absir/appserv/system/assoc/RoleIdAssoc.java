/**
 * Copyright 2013 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2013-11-27 上午9:39:31
 */
package com.absir.appserv.system.assoc;

import com.absir.appserv.jdbc.JdbcCondition;
import com.absir.appserv.jdbc.JdbcCondition.Conditions;
import com.absir.appserv.system.bean.proxy.JiUserBase;
import com.absir.appserv.system.dao.IAssocDao;
import com.absir.appserv.system.helper.HelperCondition;
import com.absir.orm.value.JePermission;
import com.absir.orm.value.JiAssoc;

/**
 * @author absir
 * 
 */
@Deprecated
public class RoleIdAssoc implements IAssocDao {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.absir.appserv.system.dao.IAssocDao#supportAssocClass(java.lang.Class,
	 * java.lang.String, com.absir.appserv.system.bean.proxy.JiUserBase,
	 * com.absir.appserv.support.entity.value.JePermission)
	 */
	@Override
	public boolean supportAssocClass(Class<? extends JiAssoc> assocClass, String rootEntityName, JiUserBase user, JePermission permission) {
		// TODO Auto-generated method stub
		return user != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.absir.appserv.system.dao.IAssocDao#assocConditions(java.lang.String,
	 * com.absir.appserv.system.bean.proxy.JiUserBase,
	 * com.absir.appserv.support.entity.value.JePermission, java.lang.Object,
	 * com.absir.appserv.jdbc.JdbcCondition,
	 * com.absir.appserv.jdbc.JdbcCondition.Conditions,
	 * com.absir.appserv.jdbc.JdbcCondition.Conditions)
	 */
	@Override
	public void assocConditions(String rootEntityName, JiUserBase user, JePermission permission, Object strategies, JdbcCondition jdbcCondition, Conditions includeConditions,
			Conditions excludeConditions) {
		// TODO Auto-generated method stub
		HelperCondition.concatOR(includeConditions, jdbcCondition.getCurrentPropertyAlias() + ".userId");
		includeConditions.add(user.getUserId());
	}
}
