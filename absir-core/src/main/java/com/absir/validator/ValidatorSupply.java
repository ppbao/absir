/**
 * Copyright 2014 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2014-1-8 下午3:59:58
 */
package com.absir.validator;

import java.util.List;

import com.absir.bean.basis.Base;
import com.absir.bean.inject.value.Bean;
import com.absir.property.PropertySupply;

/**
 * @author absir
 * 
 */
@Base
@Bean
public class ValidatorSupply extends PropertySupply<ValidatorObject, List<Validator>> {

}
