package br.com.concepting.framework.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.concepting.framework.constants.Constants;
import br.com.concepting.framework.controller.action.types.ActionType;
import br.com.concepting.framework.controller.form.ActionFormController;
import br.com.concepting.framework.controller.form.BaseActionForm;
import br.com.concepting.framework.controller.form.constants.ActionFormConstants;
import br.com.concepting.framework.controller.types.ScopeType;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.BaseModel;
import br.com.concepting.framework.resources.exceptions.InvalidResourcesException;
import br.com.concepting.framework.security.controller.SecurityController;
import br.com.concepting.framework.util.ExceptionUtil;
import br.com.concepting.framework.util.types.ContentType;

/**
 * Class responsible to route the request to the right UI.
 *
 * @author fvilarinho
 * @version 3.3.0
 * 
 * <pre>Copyright (C) 2007 Innovative Thinking.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.</pre>
 */
@WebServlet(name = "actionForm", urlPatterns = "*.ui")
public class ActionFormServlet extends HttpServlet{
	private static final long serialVersionUID = -6893835539662603177L;

	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doPost(request, response);
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		response.setCharacterEncoding(Constants.DEFAULT_UNICODE_ENCODING);

		SystemController systemController = new SystemController(request, response);
		String action = systemController.getRequestParameterValue(ActionFormConstants.ACTION_ATTRIBUTE_ID);

		if(action != null && action.equals(ActionType.DOWNLOAD.getMethod())){
			String contentId = systemController.getRequestParameterValue(Constants.CONTENT_ATTRIBUTE_ID);

			if(contentId != null && contentId.length() > 0){
				byte content[] = systemController.getAttribute(contentId, ScopeType.SESSION);

				if(content != null){
					String contentType = systemController.getRequestParameterValue(Constants.CONTENT_TYPE_ATTRIBUTE_ID);

					if(contentType == null || contentType.length() == 0)
						contentType = ContentType.BINARY.getMimeType();

					String contentFilename = systemController.getRequestParameterValue(Constants.CONTENT_FILENAME_ATTRIBUTE_ID);

					systemController.outputContent(content, contentType, contentFilename);
				}
			}
		}
		else{
			SecurityController securityController = systemController.getSecurityController();
			ActionFormController actionFormController = systemController.getActionFormController();

			try{
				BaseActionForm<? extends BaseModel> actionFormInstance = actionFormController.getActionFormInstance();

				if(actionFormInstance == null)
					throw new InvalidResourcesException(request.getRequestURI());

				actionFormInstance.processRequest(systemController, actionFormController, securityController);
			}
			catch(Throwable e){
				Throwable exception = ExceptionUtil.getCause(e);

				if(!ExceptionUtil.isExpectedException(exception) && !ExceptionUtil.isInternalErrorException(exception))
					exception = new InternalErrorException(exception);

				systemController.forward(exception);
			}
		}
	}
}