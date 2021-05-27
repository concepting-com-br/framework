package br.com.concepting.framework.controller;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import javax.ws.rs.core.Response;

import br.com.concepting.framework.controller.form.annotations.ActionForm;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import br.com.concepting.framework.constants.Constants;
import br.com.concepting.framework.constants.SystemConstants;
import br.com.concepting.framework.controller.form.ActionFormController;
import br.com.concepting.framework.controller.form.BaseActionForm;
import br.com.concepting.framework.controller.form.constants.ActionFormConstants;
import br.com.concepting.framework.controller.helpers.RequestParameterInfo;
import br.com.concepting.framework.controller.types.ScopeType;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.BaseModel;
import br.com.concepting.framework.model.constants.ModelConstants;
import br.com.concepting.framework.security.controller.SecurityController;
import br.com.concepting.framework.ui.constants.UIConstants;
import br.com.concepting.framework.ui.controller.UIController;
import br.com.concepting.framework.util.ExceptionUtil;
import br.com.concepting.framework.util.PropertyUtil;
import br.com.concepting.framework.util.StringUtil;
import br.com.concepting.framework.util.types.ContentType;
import br.com.concepting.framework.webservice.constants.WebServiceConstants;

/**
 * Class responsible to control the requests of the system.
 *
 * @author fvilarinho
 * @since 1.0.0
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
public class SystemController{
	private PageContext                       pageContext              = null;
	private HttpServletRequest                request                  = null;
	private HttpServletResponse               response                 = null;
	private Map<String, RequestParameterInfo> requestParameters        = null;
	private Collection<Cookie>                cookies                  = null;
	private HttpSession                       session                  = null;
	private Boolean                           hasOutputContent         = null;
	private Boolean                           hasForwardedOrRedirected = null;
	private String                            forwarwedOrRedirectedUrl = null;
	
	/**
	 * Constructor - Initialize the controller.
	 *
	 * @param session Instance that contains the system session.
	 */
	public SystemController(HttpSession session){
		super();

		setSession(session);

		initialize();
	}

	/**
	 * Constructor - Initialize the controller.
	 *
	 * @param request Instance that contains the request.
	 * @param response Instance that contains the response.
	 */
	public SystemController(HttpServletRequest request, HttpServletResponse response){
		super();

		setRequest(request);
		setResponse(response);

		initialize();
	}

	/**
	 * Constructor - Initialize the controller.
	 *
	 * @param pageContext Instance that contains the page.
	 */
	public SystemController(PageContext pageContext){
		this((pageContext != null ? (HttpServletRequest)pageContext.getRequest() : null), (pageContext != null ? (HttpServletResponse)pageContext.getResponse() : null));

		this.pageContext = pageContext;
	}

	/**
	 * Initialize the controller.
	 */
	private void initialize(){
		loadCookies();
		loadSession();
		loadEncoding();
		loadRequestParameters();
	}
	
	public String getForwarwedOrRedirectedUrl(){
		return getAttribute(SystemConstants.FORWARDED_OR_REDIRECTED_URL, ScopeType.REQUEST);
	}
	
	public void setForwarwedOrRedirectedUrl(String forwarwedOrRedirectedUrl){
		setAttribute(SystemConstants.FORWARDED_OR_REDIRECTED_URL, forwarwedOrRedirectedUrl, ScopeType.REQUEST);
	}
	
	/**
	 * Load the request cookies.
	 */
	private void loadCookies(){
		if(this.request != null){
			if(this.request.getCookies() != null && this.request.getCookies().length > 0){
				this.cookies = PropertyUtil.instantiate(Constants.DEFAULT_LIST_CLASS);
				
				for(Cookie cookie : this.request.getCookies())
					this.cookies.add(cookie);
			}
		}
	}

	/**
	 * Loads the request parameters.
	 */
	private void loadRequestParameters(){
		if(this.request != null){
			this.requestParameters = getAttribute(SystemConstants.REQUEST_PARAMETERS_ATTRIBUTE_ID, ScopeType.REQUEST);

			if(this.requestParameters == null){
				String encoding = getEncoding();
				Boolean isUploadRequest = isUploadRequest();

				if(isUploadRequest != null && isUploadRequest()){
					ServletFileUpload uploader = new ServletFileUpload(new DiskFileItemFactory());

					try{
						List<FileItem> items = uploader.parseRequest(this.request);

						if(items != null && !items.isEmpty()){
							for(FileItem item : items){
								try{
									String name = item.getFieldName();

									if(name == null || name.length() == 0)
										continue;

									RequestParameterInfo requestParameter = (this.requestParameters != null ? this.requestParameters.get(name) : null);

									if(requestParameter == null){
										if(item.isFormField()){
											requestParameter = new RequestParameterInfo();
											requestParameter.setName(name);
											requestParameter.setValue(new String(item.getString().getBytes(Constants.DEFAULT_ISO_ENCODING), Constants.DEFAULT_UNICODE_ENCODING));
											requestParameter.setValues(new String[]{requestParameter.getValue()});
										}
										else{
											byte[] content = item.get();

											if(content == null || content.length == 0)
												continue;

											requestParameter = new RequestParameterInfo();
											requestParameter.setName(name);
											requestParameter.setContent(content);
											requestParameter.setContentFilename(item.getName());

											ContentType contentType = ContentType.BINARY;

											try{
												contentType = ContentType.toContentType(item.getContentType());
											}
											catch(IllegalArgumentException | NullPointerException e){
											}

											requestParameter.setContentType(contentType);
										}
									}
									else{
										if(item.isFormField()){
											String value = new String(item.getString().getBytes(Constants.DEFAULT_ISO_ENCODING), Constants.DEFAULT_UNICODE_ENCODING);
											
											if(requestParameter.getValue() == null)
												requestParameter.setValue(value);
											
											String values[] = requestParameter.getValues();
											
											if(values == null){
												values = new String[1];
												values[0] = value;
											}
											else{
												String valuesBuffer[] = new String[values.length + 1];

												for(int cont = 0 ; cont < values.length ; cont++)
													valuesBuffer[cont] = values[cont];

												valuesBuffer[values.length] = value;
											}

											requestParameter.setValues(values);
										}
									}

									requestParameter.setType(PropertyUtil.getType(name));

									if(this.requestParameters == null)
										this.requestParameters = new TreeMap<>();

									this.requestParameters.put(name, requestParameter);
								}
								catch(UnsupportedEncodingException e){
								}
							}
						}
					}
					catch(FileUploadException e){
					}
				}
				else{
					Enumeration<String> requestParametersNames = this.request.getParameterNames();

					if(requestParametersNames != null && requestParametersNames.hasMoreElements()){
						while(requestParametersNames.hasMoreElements()){
							String requestParameterName = requestParametersNames.nextElement();
							String requestParameterValue[] = this.request.getParameterValues(requestParameterName);

							if(requestParameterValue != null && requestParameterValue.length > 0){
								for(int cont = 0 ; cont < requestParameterValue.length ; cont++){
									try{
										requestParameterValue[cont] = new String(requestParameterValue[cont].getBytes(Constants.DEFAULT_ISO_ENCODING), encoding);
									}
									catch(UnsupportedEncodingException e){
									}
								}
							}

							RequestParameterInfo requestParameter = new RequestParameterInfo();

							requestParameter.setName(requestParameterName);
							requestParameter.setValue(requestParameterValue[0]);
							requestParameter.setValues(requestParameterValue);
							requestParameter.setType(PropertyUtil.getType(requestParameterName));

							if(this.requestParameters == null)
								this.requestParameters = new TreeMap<>();

							this.requestParameters.put(requestParameterName, requestParameter);
						}
					}
				}
				
				filterRequestParameters(this.requestParameters);

				setAttribute(SystemConstants.REQUEST_PARAMETERS_ATTRIBUTE_ID, this.requestParameters, ScopeType.REQUEST);
			}
		}
	}
	
	private void filterRequestParameters(Map<String, RequestParameterInfo> requestParameters){
		if(requestParameters != null && !requestParameters.isEmpty()){
			List<String> requestParametersList = PropertyUtil.instantiate(Constants.DEFAULT_LIST_CLASS);
			
			requestParametersList.addAll(requestParameters.keySet());

			for(int cont = 0 ; cont < requestParametersList.size() ; cont++){
				String requestParameterName = requestParametersList.get(cont);

				if(!requestParameterName.endsWith(Constants.LABEL_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(Constants.TOOLTIP_ATTRIBUTE_ID) && 
				   !requestParameterName.endsWith(Constants.PATTERN_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(ActionFormConstants.ACTION_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(ActionFormConstants.FORWARD_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(ActionFormConstants.DATASET_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(ActionFormConstants.DATASET_SCOPE_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(ActionFormConstants.DATASET_START_INDEX_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(ActionFormConstants.DATASET_END_INDEX_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(ModelConstants.VALIDATE_MODEL_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(ModelConstants.VALIDATE_MODEL_PROPERTIES_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.CALENDAR_HOURS_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.CALENDAR_MINUTES_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.CALENDAR_MILLISECONDS_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.CALENDAR_SECONDS_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.COLOR_PICKER_BLUE_VALUE_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.COLOR_PICKER_GREEN_VALUE_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.COLOR_PICKER_RED_VALUE_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.CURRENT_GUIDE_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.CURRENT_SECTIONS_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.CURRENT_TREE_VIEW_NODE_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.IS_TREE_VIEW_NODE_EXPANDED_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.PAGER_ACTION_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.PAGER_CURRENT_PAGE_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.PAGER_ITEMS_PER_PAGE_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.RICH_TEXT_AREA_TOOLBAR_FONT_NAME_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.RICH_TEXT_AREA_TOOLBAR_FONT_SIZE_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.RICH_TEXT_AREA_TOOLBAR_FONT_COLOR_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.RICH_TEXT_AREA_TOOLBAR_BACKGROUND_COLOR_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.SORT_ORDER_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.SORT_PROPERTY_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.SUGGESTION_BOX_DATASET_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.SUGGESTION_BOX_LABEL_PROPERTY_ATTRIBUTE_ID) &&
				   !requestParameterName.endsWith(UIConstants.UPDATE_VIEWS_ATTRIBUTE_ID)){
					int pos = requestParameterName.lastIndexOf(".");
					
					if(pos >= 0){
						String parentRequestParameterName = requestParameterName.substring(0, pos);
						
						pos = requestParametersList.indexOf(parentRequestParameterName);
						
						if(pos >= 0){
							requestParameters.remove(parentRequestParameterName);
							requestParametersList.remove(pos);
							
							cont--;
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the instance of the current exception.
	 *
	 * @return Instance that contains the exception.
	 */
	public Throwable getCurrentException(){
		Boolean isWebServicesRequest = isWebServicesRequest();

		return getAttribute(SystemConstants.CURRENT_EXCEPTION_ATTRIBUTE_ID, (isWebServicesRequest != null && isWebServicesRequest ? ScopeType.REQUEST : ScopeType.SESSION));
	}

	/**
	 * Defines the instance of the current exception.
	 *
	 * @param exception Instance that contains the exception.
	 */
	public void setCurrentException(Throwable exception){
		Boolean isWebServicesRequest = isWebServicesRequest();

		setAttribute(SystemConstants.CURRENT_EXCEPTION_ATTRIBUTE_ID, exception, (isWebServicesRequest != null && isWebServicesRequest ? ScopeType.REQUEST : ScopeType.SESSION));
	}

	/**
	 * Returns the context path.
	 *
	 * @return String that contains the context path.
	 */
	public String getContextPath(){
		if(this.request != null)
			return this.request.getContextPath();

		return null;
	}

	/**
	 * Returns the request URL.
	 *
	 * @return String that contains the request URL.
	 */
	public String getRequestURL(){
		if(this.request != null)
			return this.request.getRequestURL().toString();

		return null;
	}

	/**
	 * Returns the request URI.
	 *
	 * @return String that contains the request URI.
	 */
	public String getRequestURI(){
		if(this.request != null)
			return this.request.getRequestURI();

		return null;
	}

	/**
	 * Returns the request path.
	 *
	 * @return String that contains the request path.
	 */
	public String getRequestPath(){
		if(this.request != null)
			return this.request.getServletPath();

		return null;
	}

	/**
	 * Indicates if is a upload request.
	 * 
	 * @return True/False.
	 */
	public Boolean isUploadRequest(){
		return (this.request != null ? ServletFileUpload.isMultipartContent(this.request) : false);
	}

	/**
	 * Indicates if is a request to a WebService.
	 * 
	 * @return True/False.
	 */
	public Boolean isWebServicesRequest(){
		String contextPath = getContextPath();
		String requestUri = getRequestURI();

		if(contextPath != null && contextPath.length() > 0 && requestUri != null && requestUri.length() > 0){
			StringBuilder urlPattern = new StringBuilder();

			urlPattern.append(contextPath);
			urlPattern.append(StringUtil.toRegex(WebServiceConstants.DEFAULT_URL_PATTERN));

			Pattern regex = Pattern.compile(urlPattern.toString());
			Matcher matcher = regex.matcher(requestUri);

			return matcher.matches();
		}

		return false;
	}

	public ActionFormController getActionFormController(String name){
		return new ActionFormController(this, name);
	}
	
	/**
	 * Returns the form controller instance.
	 *
	 * @return Instance that contains the form controller.
	 */
	public ActionFormController getActionFormController(){
		return new ActionFormController(this);
	}

	/**
	 * Returns the security controller.
	 * 
	 * @return Instance that contains the security controller.
	 */
	public SecurityController getSecurityController(){
		return new SecurityController(this);
	}

	/**
	 * Returns the UI controller.
	 * 
	 * @return Instance that contains the UI controller.
	 */
	public UIController getUIController(){
		return new UIController(this);
	}

	/**
	 * Loads the current encoding.
	 */
	private void loadEncoding(){
		setEncoding(getEncoding());
	}

	/**
	 * Returns the user agent of the request.
	 * 
	 * @return String that contains the user agent.
	 */
	public String getRequestUserAgent(){
		return getRequestHeader(SystemConstants.REQUEST_USER_AGENT_ATTRIBUTE_ID);
	}
	
	/**
	 * Returns the method of the request.
	 * 
	 * @return String that contains the request method.
	 */
	public String getRequestMethod(){
		return this.request.getMethod();
	}

	/**
	 * Returns the accept language of the request.
	 *
	 * @return String that contains the accept language;
	 */
	public String getRequestAcceptLanguage(){
		String acceptLanguage = getRequestHeader(SystemConstants.REQUEST_ACCEPT_LANGUAGE_ATTRIBUTE_ID);

		if(acceptLanguage != null && acceptLanguage.length() > 0)
			acceptLanguage = StringUtil.replaceAll(acceptLanguage, "-", "_");

		return acceptLanguage;
	}

	/**
	 * Returns the value of a specific request header.
	 *
	 * @param headerName String that contains the header name.
	 * @return String that contains the request header value.
	 */
	public String getRequestHeader(String headerName){
		if(this.request != null)
			return this.request.getHeader(headerName);

		return null;
	}

	/**
	 * Returns the request IP.
	 *
	 * @return String that contains the IP.
	 */
	public String getRequestIp(){
		if(this.request != null){
			String ip = this.request.getHeader(SystemConstants.REQUEST_TRUE_CLIENT_IP_ATTRIBUTE_ID);

			if(ip == null || ip.length() == 0){
				ip = this.request.getHeader(SystemConstants.REQUEST_CONNECTING_IP_ATTRIBUTE_ID);

				if(ip == null || ip.length() == 0)
					ip = this.request.getRemoteAddr();
			}

			return ip;
		}

		return null;
	}

	/**
	 * Returns the request host name.
	 *
	 * @return String that contains the host name.
	 */
	public String getRequestHostName(){
		if(this.request != null)
			return this.request.getRemoteHost();

		return null;
	}

	/**
	 * Returns the request parameters.
	 * 
	 * @return Instance that contains the request parameters.
	 */
	public Map<String, RequestParameterInfo> getRequestParameters(){
		return this.requestParameters;
	}

	/**
	 * Returns a request parameter values.
	 *
	 * @param name String that contains the identifier of the request parameter.
	 * @return Array that contains the request parameter values.
	 */
	public String[] getRequestParameterValues(String name){
		RequestParameterInfo requestParameter = (this.requestParameters != null ? this.requestParameters.get(name) : null);

		if(requestParameter != null){
			String values[] = requestParameter.getValues();

			if(values != null && values.length > 0)
				return values;
		}

		return null;
	}

	/**
	 * Returns a request parameter value.
	 * 
	 * @param name String that contains the identifier of the request parameter.
	 * @return String that contains the request parameter value.
	 */
	public String getRequestParameterValue(String name){
		String value = null;

		if(name != null && name.length() > 0){
			String queryString = (this.request != null ? this.request.getQueryString() : null);
			StringBuilder parameterId = new StringBuilder();

			parameterId.append(name);
			parameterId.append("=");

			int pos = (queryString != null ? queryString.indexOf(parameterId.toString()) : -1);

			if(pos >= 0){
				value = queryString.substring(pos + parameterId.length());
				pos = value.indexOf("&");

				if(pos >= 0)
					value = value.substring(0, pos);
			}
			else{
				RequestParameterInfo requestParameter = (this.requestParameters != null ? this.requestParameters.get(name) : null);

				if(requestParameter != null)
					value = requestParameter.getValue();
			}
		}

		return value;
	}

	/**
	 * Returns the current encoding.
	 *
	 * @return String that contains the encoding.
	 */
	public String getEncoding(){
		String encoding = null;

		if(this.request != null)
			encoding = this.request.getParameter(SystemConstants.ENCODING_ATTRIBUTE_ID);

		if(encoding == null || encoding.length() == 0)
			encoding = getAttribute(SystemConstants.ENCODING_ATTRIBUTE_ID, ScopeType.REQUEST);

		if(encoding == null || encoding.length() == 0)
			encoding = Constants.DEFAULT_UNICODE_ENCODING;

		return encoding;
	}

	/**
	 * Defines the current encoding.
	 *
	 * @param encoding String that contains the encoding.
	 */
	public void setEncoding(String encoding){
		if(encoding == null || encoding.length() == 0)
			encoding = Constants.DEFAULT_UNICODE_ENCODING;

		setAttribute(SystemConstants.ENCODING_ATTRIBUTE_ID, encoding, ScopeType.REQUEST);

		if(this.response != null)
			this.response.setCharacterEncoding(encoding);
	}

	/**
	 * Returns the instance of the page.
	 *
	 * @return Instance that contains the page.
	 */
	public PageContext getPageContext(){
		return this.pageContext;
	}
	
	/**
	 * Indicates if a content was redirected or forwarded in the response.
	 *
	 * @return True/False.
	 */
	public Boolean hasForwardedOrRedirected(){
		return this.hasForwardedOrRedirected;
	}
	
	/**
	 * Indicates if a content was flushed in the response.
	 *
	 * @return True/False.
	 */
	public Boolean hasOutputContent(){
		return this.hasOutputContent;
	}

	/**
	 * Defines the instance of the request.
	 *
	 * @param request Instance that contains the request.
	 */
	private void setRequest(HttpServletRequest request){
		this.request = request;

		if(this.request != null){
			Cookie cookies[] = this.request.getCookies();

			this.cookies = PropertyUtil.instantiate(Constants.DEFAULT_LIST_CLASS);

			if(cookies != null && cookies.length > 0)
				for(Cookie cookie : cookies)
					if(cookie != null)
						this.cookies.add(cookie);
		}
	}

	/**
	 * Defines the instance of the response.
	 *
	 * @param response Instance that contains the response.
	 */
	private void setResponse(HttpServletResponse response){
		this.response = response;
	}

	/**
	 * Loads the system session.
	 */
	private void loadSession(){
		Boolean isWebServicesRequest = isWebServicesRequest();

		if(this.request != null && (isWebServicesRequest == null || !isWebServicesRequest))
			this.session = this.request.getSession();
	}

	/**
	 * Defines the system session.
	 *
	 * @param session Instance that contains the system session.
	 */
	private void setSession(HttpSession session){
		this.session = session;
	}

	/**
	 * Returns the current session identifier.
	 *
	 * @return String that contains the current session identifier.
	 */
	public String getSessionId(){
		if(this.session != null)
			return this.session.getId();

		return null;
	}

	/**
	 * Defines the session timeout.
	 *
	 * @param timeout Numeric value that contains the timeout.
	 */
	public void setSessionTimeout(Integer timeout){
		if(this.session != null)
			this.session.setMaxInactiveInterval(timeout * 60);        
	}

	/**
	 * Adds a new system cookie.
	 * 
	 * @param name String that contains the identifier.
	 * @param value String that contains the value.
	 */
	public void addCookie(String name, String value){
		addCookie(name, value, false);
	}

	/**
	 * Adds a new system cookie.
	 * 
	 * @param name String that contains the identifier.
	 * @param value String that contains the value.
	 * @param persistent Indicates if the cookie is persistent even that the
	 * session expire.
	 */
	public void addCookie(String name, String value, Boolean persistent){
		if(name != null && name.length() > 0 && value != null && value.length() > 0){
			if(persistent != null && persistent)
				addCookie(name, value, 60 * 60 * 24 * 365 * 1000);
			else
				addCookie(name, value, -1);
		}
	}

	/**
	 * Adds a new system cookie.
	 * 
	 * @param name String that contains the identifier.
	 * @param value String that contains the value.
	 * @param maxAge Numeric value contains the maximum age (in milliseconds).
	 */
	public void addCookie(String name, String value, Integer maxAge){
		String path = getContextPath();
		
		if(name != null && name.length() > 0 && path != null && path.length() > 0 && value != null && value.length() > 0 && this.request != null && this.response != null){
			Cookie cookie = getCookie(name);
			
			if(cookie != null)
				this.cookies.remove(cookie);

			cookie = new Cookie(name, value);
			cookie.setSecure((this.request != null && this.request.getScheme() != null && this.request.getScheme().equalsIgnoreCase("https")));
			cookie.setPath(getContextPath());

			if(maxAge != null && maxAge > 0)
				cookie.setMaxAge(maxAge);

			cookie.setValue(value);

			this.cookies.add(cookie);
			
			try{
				this.response.addCookie(cookie);
			}
			catch(Throwable e){
			}
		}
	}

	/**
	 * Removes a system cookie.
	 * 
	 * @param name String that contains the identifier.
	 */
	public void removeCookie(String name){
		if(name != null && name.length() > 0 && this.request != null && this.response != null){
			Cookie cookie = getCookie(name);

			if(cookie != null){
				if(this.cookies != null && this.cookies.size() > 0)
					this.cookies.remove(cookie);

				cookie.setMaxAge(0);
				cookie.setPath(getContextPath());
				cookie.setValue("");

				try{
					this.response.addCookie(cookie);
				}
				catch(Throwable e){
				}
			}
		}
	}

	/**
	 * Returns a system cookie instance.
	 *
	 * @param name String that contains the identifier.
	 * @return Instance that contains the cookie.
	 */
	public Cookie getCookie(String name){
		if(this.cookies != null)
			for(Cookie cookie : this.cookies)
				if(cookie != null && cookie.getName() != null && cookie.getName().equals(name))
					return cookie;

		return null;
	}

	/**
	 * Returns the instance of the response stream.
	 *
	 * @return Instance that contains the response stream.
	 */
	public PrintWriter getOutputStream(){
		try{
			if(this.pageContext != null)
				return new PrintWriter(this.pageContext.getOut());

			return new PrintWriter(this.response.getOutputStream());
		}
		catch(Throwable e){
		}

		return null;
	}

	/**
	 * Returns the value of a attribute that is stored in a specific scope. The
	 * attribute identifier must be defined like following: {@code 
	 * <attribute-name>.<child-attribute-name>.<child-attribute-name>} E.g.:
	 * loginSession.user.name
	 * 
	 * @param <T> Class that defines the attribute value.
	 * @param attributeId String that contains the attribute identifier.
	 * @param scopeType Instance that contains the type of the scope.
	 * @return Instance that contains the attribute value.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String attributeId, ScopeType scopeType){
		Object instance = null;

		if(attributeId != null && attributeId.length() > 0){
			switch(scopeType){
				case APPLICATION:{
					instance = (this.session != null ? this.session.getServletContext().getAttribute(attributeId) : null);
	
					break;
				}
				case REQUEST:{
					instance = (this.request != null ? this.request.getAttribute(attributeId) : null);
	
					break;
				}
				default:{
					instance = (this.session != null ? this.session.getAttribute(attributeId) : null);
	
					break;
				}
			}

			if(instance == null){
				try{
					int pos = attributeId.indexOf(".");
					String firstAttributeId = null;

					if(pos >= 0){
						firstAttributeId = attributeId.substring(0, pos);
						attributeId = attributeId.substring(pos + 1);
					}
					else
						firstAttributeId = attributeId;

					Boolean isWebServicesRequest = isWebServicesRequest();

					switch(scopeType){
						case APPLICATION:{
							if(isWebServicesRequest == null || !isWebServicesRequest)
								instance = (this.session != null ? this.session.getServletContext().getAttribute(firstAttributeId) : null);
	
							break;
						}
						case REQUEST:{
							instance = (this.request != null ? this.request.getAttribute(firstAttributeId) : null);
	
							break;
						}
						default:{
							if(isWebServicesRequest == null || !isWebServicesRequest)
								instance = (this.session != null ? this.session.getAttribute(firstAttributeId) : null);
	
							break;
						}
					}

					if(instance != null && !firstAttributeId.equals(attributeId))
						instance = PropertyUtil.getValue(instance, attributeId);
				}
				catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
					instance = null;
				}
			}
		}

		return (T)instance;
	}

	/**
	 * Defines the value of a attribute that is stored in a specific scope. The
	 * attribute identifier must be defined like following: {@code 
	 * <attribute-name>.<child-attribute-name>.<child-attribute-name>} E.g.:
	 * loginSession.user.name
	 * 
	 * @param <T> Class that defines the attribute value.
	 * @param attributeId String that contains the attribute identifier.
	 * @param attributeValue Instance that contains the attribute value.
	 * @param scopeType Instance that contains the type of the scope.
	 */
	public <T> void setAttribute(String attributeId, T attributeValue, ScopeType scopeType){
		if(attributeId != null && attributeId.length() > 0){
			Boolean isWebServicesRequest = isWebServicesRequest();

			if(this.session != null && scopeType == ScopeType.APPLICATION && (isWebServicesRequest == null || !isWebServicesRequest))
				this.session.getServletContext().setAttribute(attributeId, attributeValue);
			else if(this.session != null && scopeType == ScopeType.SESSION && (isWebServicesRequest == null || !isWebServicesRequest))
				this.session.setAttribute(attributeId, attributeValue);
			else if(this.request != null && scopeType == ScopeType.REQUEST)
				this.request.setAttribute(attributeId, attributeValue);
			else if(isWebServicesRequest == null || !isWebServicesRequest){
				if(this.session != null){
					try{
						int pos = attributeId.indexOf(".");
						String firstAttributeId = null;

						if(pos >= 0){
							firstAttributeId = attributeId.substring(0, pos);
							attributeId = attributeId.substring(pos + 1);

							Object instance = getAttribute(firstAttributeId, scopeType);

							if(instance != null)
								PropertyUtil.setValue(instance, attributeId, attributeValue);

							this.session.setAttribute(firstAttributeId, instance);
						}
					}
					catch(InvocationTargetException e){
					}
				}
			}
		}
	}

	/**
	 * Redirects the request to another page.
	 *
	 * @param url String that contains the page.
	 */
	public void redirect(String url){
		String contextPath = getContextPath();

		if(url != null && url.length() > 0 && contextPath != null && contextPath.length() > 0 && this.response != null){
			StringBuilder urlBuffer = new StringBuilder();

			urlBuffer.append(contextPath);
			urlBuffer.append(url);

			try{
				this.response.sendRedirect(urlBuffer.toString());
			}
			catch(Throwable e){
			}
			
			this.hasForwardedOrRedirected = true;
		}
	}

	/**
	 * Forwards the request to another page.
	 *
	 * @param url String that contains the page.
	 */
	public void forward(String url){
		if(url != null && url.length() > 0 && this.request != null && this.response != null){
			try{
				this.request.getRequestDispatcher(url).forward(this.request, this.response);
			}
			catch(Throwable e){
				try{
					this.response.sendError(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
				} 
				catch(Throwable e1){
				}
			}
			
			this.hasForwardedOrRedirected = true;
		}
	}

	/**
	 * Forwards the request to the default error page.
	 * 
	 * @param e Instance that contains the caught exception.
	 */
	public void forward(Throwable e){
		try{
			if(!ExceptionUtil.isExpectedException(e) && !ExceptionUtil.isInternalErrorException(e))
				e = new InternalErrorException(e);

			setCurrentException(e);

			if(this.response != null){
				if(ExceptionUtil.isUserNotAuthorized(e))
					this.response.sendError(Response.Status.UNAUTHORIZED.getStatusCode());
				else if(ExceptionUtil.isPermissionDeniedException(e))
					this.response.sendError(Response.Status.FORBIDDEN.getStatusCode());
				else if(ExceptionUtil.isInvalidResourceException(e))
					this.response.sendError(Response.Status.NOT_FOUND.getStatusCode(), e.getMessage());
				else if(ExceptionUtil.isInternalErrorException(e))
					this.response.sendError(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
				else
					this.response.sendError(Response.Status.PRECONDITION_FAILED.getStatusCode(), e.getMessage());
				
				this.hasForwardedOrRedirected = true;
			}
		}
		catch(Throwable e1){
		}
	}

	/**
	 * Flushes a content in the response.
	 *
	 * @param contentData Byte array of the content.
	 * @param contentType Instance that contains the content type (mime type).
	 */
	public void outputContent(byte contentData[], ContentType contentType){
		outputContent(contentData, contentType, null);
	}

	/**
	 * Flushes a content in the response.
	 *
	 * @param contentData Byte array of the content.
	 * @param contentType String that contains the content type (mime type).
	 */
	public void outputContent(byte contentData[], String contentType){
		outputContent(contentData, contentType, null);
	}

	/**
	 * Flushes a content in the response.
	 *
	 * @param contentData Byte array of the content.
	 * @param contentType Instance that contains the content type (mime type).
	 * @param contentFilename String that contains the filename of the content.
	 */
	public void outputContent(byte contentData[], ContentType contentType, String contentFilename){
		outputContent(contentData, contentType.getMimeType(), contentFilename);
	}

	/**
	 * Flushes a content in the response.
	 *
	 * @param contentData Byte array of the content.
	 * @param contentType String that contains the content type (mime type).
	 * @param contentFilename String that contains the filename of the content.
	 */
	public void outputContent(byte contentData[], String contentType, String contentFilename){
		if(this.response != null){
			try{
				if(contentFilename != null && contentFilename.length() > 0){
					StringBuilder filenameBuffer = new StringBuilder();

					filenameBuffer.append("attachment; filename=\"");
					filenameBuffer.append(contentFilename);
					filenameBuffer.append("\"");

					this.response.setHeader("Content-Disposition", filenameBuffer.toString());
				}

				if(contentType != null && contentType.length() > 0)
					this.response.setContentType(contentType);

				this.response.setContentLength((contentData != null ? contentData.length : 0));

				ServletOutputStream out = this.response.getOutputStream();

				out.write(contentData);
				out.flush();
				out.close();

				this.hasOutputContent = true;
			}
			catch(Throwable e){
				this.hasOutputContent = false;
			}
		}
		else
			this.hasOutputContent = false;
	}
}