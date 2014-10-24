/**
 * Copyright 2014 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2014年10月23日 下午2:05:28
 */
package com.absir.scene;

/**
 * @author absir
 *
 */
public interface ISceneObject {

	/**
	 * 对象步进
	 * 
	 * @param time
	 * @param result
	 */
	public boolean stepDone(long contextTime);

	/**
	 * 是否感知
	 * 
	 * @return
	 */
	public boolean isSensory();

	/**
	 * 获取当前状态
	 * 
	 * @return
	 */
	public Object getStatusObject();

}
