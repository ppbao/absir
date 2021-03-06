/**
 * Copyright 2013 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2013-11-5 下午2:34:52
 */
package com.absir.appserv.game.value;

/**
 * @author absir
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class OBuff_MAX_HP_T extends OBuffRound<OCard> {

	// 最大生命提升
	int maxHpT;

	// 积累最大生命提升
	int maxHpTR;

	/**
	 * @return the maxHpT
	 */
	public int getMaxHpT() {
		return maxHpT;
	}

	/**
	 * @param maxHpT
	 *            the maxHpT to set
	 */
	public void setMaxHpT(int maxHpT) {
		this.maxHpT = maxHpT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.absir.appserv.game.value.OBuffRound#stepRound(com.absir.appserv.game
	 * .value.OObject, long, int, com.absir.appserv.game.value.OResult)
	 */
	@Override
	public void stepRound(OCard object, long time, int round, IResult result) {
		// TODO Auto-generated method stub
		maxHpTR += maxHpT;
		int maxHp = object.getMaxHp();
		object.setMaxHp((int) object.getBuffAtt("maxHp", object.baseHp(), maxHpT));
		maxHp -= maxHp;
		if (maxHp > 0) {
			object.damage(null, maxHp, null, result);

		} else {
			object.treat(null, -maxHp, null, result);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.absir.appserv.game.value.OBuffReverse#revert(com.absir.appserv.game
	 * .value.OObject, com.absir.appserv.game.value.OResult)
	 */
	@Override
	public void revert(OCard object, IResult result) {
		// TODO Auto-generated method stub
		object.setMaxHp((int) object.getBuffAtt("maxHp", object.baseHp(), -maxHpTR));
		if (object.getHp() > object.getMaxHp()) {
			object.setHp(object.getMaxHp());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.absir.appserv.game.value.OBuff#effect(com.absir.appserv.game.value
	 * .OObject, com.absir.appserv.game.value.OResult)
	 */
	@Override
	public void effect(OCard object, IResult result) {
		// TODO Auto-generated method stub
	}

}
