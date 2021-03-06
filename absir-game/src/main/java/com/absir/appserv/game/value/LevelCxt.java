/**
 * Copyright 2013 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2013-10-15 下午1:38:44
 */
package com.absir.appserv.game.value;

/**
 * @author absir
 * 
 */
public class LevelCxt<T extends ILevel> {

	/**
	 * @param obj
	 * @return
	 */
	public int getLevel(T obj) {
		return obj.getLevel();
	}

	/**
	 * @param obj
	 * @param level
	 */
	public void setLevel(T obj, int level) {
		obj.setLevel(level);
	}
}
