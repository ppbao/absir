/**
 * Copyright 2013 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2013-4-6 下午3:33:33
 */
package com.absir.appserv.system.asset;

import java.io.File;
import java.io.IOException;

import com.absir.appserv.support.Developer;
import com.absir.appserv.support.developer.IDeveloper;
import com.absir.appserv.support.developer.IRender;
import com.absir.appserv.system.bean.proxy.JiUserBase;
import com.absir.appserv.system.service.SecurityService;
import com.absir.bean.basis.Base;
import com.absir.bean.basis.Environment;
import com.absir.bean.core.BeanFactoryUtils;
import com.absir.core.helper.HelperFile;
import com.absir.server.exception.ServerException;
import com.absir.server.exception.ServerStatus;
import com.absir.server.in.Input;
import com.absir.server.value.Before;
import com.absir.server.value.Body;
import com.absir.server.value.Param;
import com.absir.server.value.Server;
import com.absir.servlet.InputRequest;

/**
 * @author absir
 * 
 */
@Base
@Server
public class Asset_diy extends AssetServer {

	/** DIY_AUTHENTICATION */
	private static final String DIY_AUTHENTICATION = Asset_diy.class.getName() + "@DIY_AUTHENTICATION";

	/**
	 * @param input
	 */
	public static void authentication(InputRequest input) {
		input.setSession(DIY_AUTHENTICATION, "");
	}

	/**
	 * @param input
	 * @return
	 * @throws Exception
	 */
	@Before
	protected void onAuthentication(Input input) throws Exception {
		if (BeanFactoryUtils.getEnvironment() == Environment.DEVELOP) {
			return;
		}

		if (input instanceof InputRequest) {
			if (((InputRequest) input).getSession(DIY_AUTHENTICATION) != null) {
				return;
			}

			JiUserBase user = SecurityService.ME.loginRender(input);
			if (user != null && user.isDeveloper()) {
				return;
			}
		}

		throw new ServerException(ServerStatus.IN_404);
	}

	/**
	 * @param index
	 * @param request
	 * @return
	 */
	public String view(String view) {
		return view;
	}

	/**
	 * @param view
	 * @return
	 * @throws IOException
	 */
	@Body
	public String body(String view) throws IOException {
		if (IDeveloper.ME != null) {
			view = IDeveloper.ME.getDeveloperPath(view);
		}

		File file = new File(IRender.ME.getRealPath(view));
		return file.exists() ? HelperFile.readFileToString(file) : "";
	}

	/**
	 * @param view
	 * @param bodies
	 * @return
	 * @throws IOException
	 */
	@Body
	public void save(String view, @Param String body) throws IOException {
		if (IDeveloper.ME != null) {
			view = IDeveloper.ME.getDeveloperPath(view);
			if (body != null) {
				Developer.writeGenerate(view, body);
			}
		}
	}
}
