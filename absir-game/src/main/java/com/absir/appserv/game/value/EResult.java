/**
 * Copyright 2013 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2013-10-17 下午7:14:39
 */
package com.absir.appserv.game.value;

import com.absir.appserv.system.bean.value.JaLang;

/**
 * @author absir
 * 
 */
public enum EResult {

	@JaLang("继续战斗")
	CONTINUE,

	@JaLang("动作完成")
	DONE,

	@JaLang("战斗胜利")
	VICTORY,

	@JaLang("战斗失败")
	LOSS, ;

	/**
	 * @param eFight
	 * @return
	 */
	public static EResult getReverse(EResult eFight) {
		if (eFight == VICTORY) {
			eFight = LOSS;

		} else if (eFight == LOSS) {
			eFight = VICTORY;
		}

		return eFight;
	}
}
