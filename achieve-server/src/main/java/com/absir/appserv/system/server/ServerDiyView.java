/**
 * Copyright 2014 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2014年8月12日 下午1:56:21
 */
package com.absir.appserv.system.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.absir.appserv.support.developer.IDeveloper;
import com.absir.appserv.support.developer.IRender;
import com.absir.appserv.system.asset.Asset_diy;
import com.absir.appserv.system.helper.HelperInput;
import com.absir.bean.basis.Environment;
import com.absir.bean.core.BeanFactoryUtils;
import com.absir.bean.inject.value.Value;
import com.absir.core.helper.HelperFile;
import com.absir.server.in.Input;
import com.absir.server.on.OnPut;
import com.absir.server.route.returned.ReturnedResolverView;
import com.absir.servlet.InputRequest;

/**
 * @author absir
 *
 */
public abstract class ServerDiyView extends ReturnedResolverView implements IRender {

	/** diyView */
	@Value("developer.diy.view")
	private String diyView = "/WEB-INF/developer/diy.html";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.absir.server.route.returned.ReturnedResolverView#resolveReturnedView
	 * (java.lang.String, com.absir.server.on.OnPut)
	 */
	@Override
	public void resolveReturnedView(String view, OnPut onPut) throws Exception {
		// TODO Auto-generated method stub
		Input input = onPut.getInput();
		if (input instanceof InputRequest) {
			render(getView(view), (InputRequest) input);

		} else {
			onPut.getInput().write(view);
		}
	}

	/**
	 * @param view
	 * @param input
	 * @throws Exception
	 */
	public void render(String view, InputRequest input) throws Exception {
		HttpServletRequest request = input.getRequest();
		request.setAttribute("input", input);
		// input.getResponse().setCharacterEncoding(ContextUtils.getCharset().displayName());
		Object[] renders = null;
		int diy = 0;
		Object render = getRender(view, input);
		if (IDeveloper.ME != null) {
			diy = IDeveloper.ME.diy(request);
			if (diy == 1 || BeanFactoryUtils.getEnvironment() != Environment.PRODUCT) {
				renders = getRenders(render, input);
				IDeveloper.ME.generate(view, view, renders);
			}
		}

		if (diy == 2) {
			Asset_diy.authentication(input);
			input.getModel().put("diy_url", HelperInput.getRequestUrl(request));
			input.getModel().put("diy_view", view);
			input.getModel().put("diy_restore", getDiyRestore(view));
			input.getModel().put("diy_include", include(","));
			input.getModel().put("diy_expression", expression(","));
			view = diyView;
		}

		renderView(view, renders, input);
	}

	/**
	 * @param view
	 * @return
	 * @throws IOException
	 */
	public String getDiyRestore(String view) throws IOException {
		if (IDeveloper.ME != null) {
			String diyView = IDeveloper.ME.getDeveloperPath(view);
			File file = new File(getRealPath(diyView + Asset_diy.getDiySuffix()));
			if (file.exists()) {
				return HelperFile.readFileToString(file);
			}

			file = new File(getRealPath(diyView));
			if (file.exists()) {
				return HelperFile.readFileToString(file);
			}

			file = new File(getRealPath(view));
			if (file.exists()) {
				return HelperFile.readFileToString(file);
			}
		}

		return "";
	}

	/**
	 * @param expression
	 * @return
	 */
	protected String expression(String expression) {
		return "<% " + expression + " %>";
	}

	/**
	 * @param view
	 * @return
	 */
	protected abstract String getView(String view);

	/**
	 * @param view
	 * @param renders
	 */
	protected abstract Object getRender(String view, InputRequest input);

	/**
	 * @param render
	 * @param input
	 * @return
	 */
	protected abstract Object[] getRenders(Object render, InputRequest input);

	/**
	 * @param view
	 * @param renders
	 * @param input
	 */
	protected abstract void renderView(String view, Object[] renders, InputRequest input);
}