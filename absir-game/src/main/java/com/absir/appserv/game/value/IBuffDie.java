/**
 * Copyright 2013 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2013-11-6 下午8:51:50
 */
package com.absir.appserv.game.value;

/**
 * @author absir
 * 
 */
@SuppressWarnings("rawtypes")
public interface IBuffDie<T, O extends OObject> extends IBuffFrom<T> {

	/**
	 * @param self
	 * @param damage
	 * @param damageFrom
	 * @param result
	 */
	public void die(O self, int damage, T damageFrom, IResult result);

	/**
	 * @param self
	 * @param target
	 * @param damage
	 * @param damageFrom
	 * @param result
	 */
	public void die(O self, O target, int damage, T damageFrom, IResult result);

}
