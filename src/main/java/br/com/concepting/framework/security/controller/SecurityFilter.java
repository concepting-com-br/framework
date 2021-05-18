package br.com.concepting.framework.security.controller;

import br.com.concepting.framework.constants.SystemConstants;
import br.com.concepting.framework.controller.BaseFilter;
import br.com.concepting.framework.controller.SystemController;
import br.com.concepting.framework.controller.form.util.ActionFormUtil;
import br.com.concepting.framework.controller.helpers.RequestParameterInfo;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.*;
import br.com.concepting.framework.model.exceptions.ItemAlreadyExistsException;
import br.com.concepting.framework.model.exceptions.ItemNotFoundException;
import br.com.concepting.framework.model.util.ModelUtil;
import br.com.concepting.framework.processors.ExpressionProcessorUtil;
import br.com.concepting.framework.security.constants.SecurityConstants;
import br.com.concepting.framework.security.exceptions.InvalidMfaTokenException;
import br.com.concepting.framework.security.exceptions.LoginSessionExpiredException;
import br.com.concepting.framework.security.exceptions.PermissionDeniedException;
import br.com.concepting.framework.security.exceptions.UserNotAuthorizedException;
import br.com.concepting.framework.security.model.LoginParameterModel;
import br.com.concepting.framework.security.model.LoginSessionModel;
import br.com.concepting.framework.security.model.UserModel;
import br.com.concepting.framework.security.service.interfaces.LoginSessionService;
import br.com.concepting.framework.security.util.SecurityUtil;
import br.com.concepting.framework.service.interfaces.IService;
import br.com.concepting.framework.util.DateTimeUtil;
import br.com.concepting.framework.util.StringUtil;
import br.com.concepting.framework.util.helpers.DateTime;
import br.com.concepting.framework.util.types.DateFieldType;

import javax.servlet.annotation.WebFilter;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class responsible to apply security filters in the system requests.
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
@WebFilter(filterName = "securityFilter", urlPatterns = {"*.ui", "*.jsp", "/webServices/*"})
@SuppressWarnings("unchecked")
public class SecurityFilter extends BaseFilter{
    /**
     * Process the security filter.
     *
     * @throws UserNotAuthorizedException Occurs when the user is not authorized.
     * @throws PermissionDeniedException Occurs when the user doesn't have permission.
     * @throws InternalErrorException Occurs when was not possible to process
     * the filter.
     */
    protected <L extends LoginSessionModel,
               U extends UserModel,
               LP extends LoginParameterModel,
               F extends FormModel,
               UL extends UrlModel,
               SM extends SystemModuleModel,
               SS extends SystemSessionModel> void process() throws UserNotAuthorizedException, PermissionDeniedException, InternalErrorException{
        SystemController systemController = getSystemController();
        SecurityController securityController = getSecurityController();
        
        if(securityController.isLoginSessionExpired()){
            systemController.removeCookie(SecurityConstants.LOGIN_SESSION_ATTRIBUTE_ID);
            
            throw new LoginSessionExpiredException();
        }
        
        L loginSession = securityController.getLoginSession();
        SM systemModule = loginSession.getSystemModule();
        Collection<UL> exclusionUrls = (Collection<UL>)systemModule.getExclusionUrls();
        String requestUri = systemController.getRequestURI();
        
        if(requestUri != null && requestUri.length() > 0){
            StringBuilder requestUriBuffer = new StringBuilder();
            
            requestUriBuffer.append(requestUri);
            
            Map<String, RequestParameterInfo> requestParameters = systemController.getRequestParameters();
            
            if(requestParameters != null && !requestParameters.isEmpty()){
                requestUriBuffer.append("?");
                
                int cont2 = 0;
                
                for(Entry<String, RequestParameterInfo> entry: requestParameters.entrySet()){
                    String requestParameterName = entry.getKey();
                    RequestParameterInfo requestParameterInfo = entry.getValue();
                    
                    if(requestParameterInfo.getContent() == null){
                        String[] requestParameterValues = requestParameterInfo.getValues();
                        
                        if(requestParameterValues != null && requestParameterValues.length > 0){
                            for(int cont1 = 0; cont1 < requestParameterValues.length; cont1++){
                                if(cont2 > 0)
                                    requestUriBuffer.append("&");
                                
                                requestUriBuffer.append(requestParameterName);
                                requestUriBuffer.append("=");
                                requestUriBuffer.append(requestParameterValues[cont1]);
                                
                                cont2++;
                            }
                        }
                    }
                }
            }
            
            requestUri = requestUriBuffer.toString();
        }
        
        requestUri = StringUtil.replaceAll(requestUri, systemController.getContextPath(), "");
        
        U user = loginSession.getUser();
        Boolean excludeUrl = false;
        
        if(exclusionUrls != null && !exclusionUrls.isEmpty()){
            for(UL exclusionUrl: exclusionUrls){
                String urlPattern = StringUtil.toRegex(exclusionUrl.getPath());
                Pattern regex = Pattern.compile(urlPattern);
                Matcher matcher = regex.matcher(requestUri);
                
                if(matcher.matches()){
                    excludeUrl = true;
                    
                    break;
                }
            }
            
            if(!excludeUrl)
                if(!securityController.isLoginSessionAuthenticated())
                    throw new PermissionDeniedException();
        }
        
        if(securityController.isLoginSessionAuthenticated())
            if(!user.isSuperUser() && !user.hasPermission(requestUri))
                throw new PermissionDeniedException();
        
        if(!excludeUrl){
            LP loginParameter = user.getLoginParameter();
            
            if(loginParameter != null && loginParameter.hasMfa() != null && loginParameter.hasMfa())
                if(loginParameter.isMfaTokenValidated() == null || !loginParameter.isMfaTokenValidated())
                    throw new InvalidMfaTokenException();
        }
        
        SS systemSession = loginSession.getSystemSession();
        String domain = systemSession.getId();
        
        ExpressionProcessorUtil.setVariable(domain, SecurityConstants.LOGIN_SESSION_ATTRIBUTE_ID, loginSession);
    }
    
    /**
     * Initializes the security filter.
     *
     * @throws UserNotAuthorizedException Occurs when the user is not authorized.
     * @throws PermissionDeniedException Occurs when the user doesn't have permission.
     * @throws InternalErrorException Occurs when was not possible to process
     * the filter.
     */
    @SuppressWarnings("unchecked")
    protected <L extends LoginSessionModel,
               U extends UserModel,
               LP extends LoginParameterModel,
               F extends FormModel,
               UL extends UrlModel,
               SM extends SystemModuleModel,
               SS extends SystemSessionModel> void initialize() throws UserNotAuthorizedException, PermissionDeniedException, InternalErrorException{
        SystemController systemController = getSystemController();
        SecurityController securityController = getSecurityController();
        L loginSession = (securityController != null ? securityController.getLoginSession() : null);
        
        if(loginSession != null){
            Boolean isWebServicesRequest = (systemController != null ? systemController.isWebServicesRequest() : null);
            
            if(isWebServicesRequest != null && isWebServicesRequest){
                if(loginSession.getId() != null && loginSession.getId().length() > 0){
                    Class<L> loginSessionClass = (Class<L>)loginSession.getClass();
                    LoginSessionService<L, U, LP> loginSessionService = null;
                    
                    try{
                        loginSessionService = getService(loginSessionClass);
                    }
                    catch(InternalErrorException e){
                    }
                    
                    if(loginSessionService != null){
                        try{
                            loginSession = loginSessionService.find(loginSession);
                            
                            if(loginSession == null || !loginSession.isActive())
                                throw new ItemNotFoundException();
                        }
                        catch(ItemNotFoundException e1){
                            systemController.removeCookie(SecurityConstants.LOGIN_SESSION_ATTRIBUTE_ID);
                            
                            throw new PermissionDeniedException();
                        }
                    }
                    
                    DateTime now = new DateTime();
                    DateTime startDateTime = loginSession.getStartDateTime();
                    Long ttl = DateTimeUtil.diff(now, startDateTime, DateFieldType.MINUTES);
                    Integer loginSessionTimeout = SecurityUtil.getLoginSessionTimeout();
                    
                    if(loginSessionTimeout == null || ttl >= loginSessionTimeout){
                        if(loginSessionService != null)
                            loginSessionService.logOut();
                        
                        systemController.removeCookie(SecurityConstants.LOGIN_SESSION_ATTRIBUTE_ID);
                        
                        throw new LoginSessionExpiredException();
                    }
                }
            }
            
            SM systemModule = loginSession.getSystemModule();
            Class<SM> modelClass = (Class<SM>) systemModule.getClass();
            IService<SM> systemModuleService = null;
            
            try{
                systemModuleService = getService(modelClass);
            }
            catch(InternalErrorException e){
            }
            
            if(systemModuleService != null){
                try{
                    systemModule = systemModuleService.find(systemModule);
                    
                    if(systemModule.isActive() == null || !systemModule.isActive())
                        throw new PermissionDeniedException();
                }
                catch(ItemNotFoundException e){
                    throw new PermissionDeniedException();
                }
            }
            
            systemModule = systemModuleService.loadReference(systemModule, SystemConstants.EXCLUSION_URLS_ATTRIBUTE_ID);
            
            if(isWebServicesRequest == null || !isWebServicesRequest){
                systemModule = systemModuleService.loadReference(systemModule, SystemConstants.FORMS_ATTRIBUTE_ID);
                
                Class<? extends MainConsoleModel> mainConsoleClass = ModelUtil.getMainConsoleClass();
                
                if(mainConsoleClass != null){
                    F form = systemModule.getForm(ActionFormUtil.getActionFormIdByModel(mainConsoleClass));
    
                    if(form != null){
                        Class<F> formClass = (Class<F>) form.getClass();
                        IService<F> formService = getService(formClass);
        
                        form = formService.loadReference(form, SystemConstants.OBJECTS_ATTRIBUTE_ID);
        
                        systemModule.setForm(form);
                    }
                }
            }
            
            loginSession.setSystemModule(systemModule);
            
            SS systemSession = loginSession.getSystemSession();
            
            if(systemSession != null && systemSession.getId() != null && systemSession.getId().length() > 0){
                if(systemSession.getStartDateTime() == null){
                    systemSession.setStartDateTime(new DateTime());
                    
                    Class<SS> systemSessionClass = (Class<SS>) systemSession.getClass();
                    IService<SS> systemSessionService = null;
                    
                    try{
                        systemSessionService = getService(systemSessionClass);
                    }
                    catch(InternalErrorException e){
                    }
                    
                    if(systemSessionService != null){
                        try{
                            systemSession = systemSessionService.save(systemSession);
                        }
                        catch(ItemAlreadyExistsException e){
                        }
                    }
                    
                    loginSession.setSystemSession(systemSession);
                }
            }
            else
                throw new PermissionDeniedException();
            
            securityController.setLoginSession(loginSession);
        }
        else
            throw new PermissionDeniedException();
    }
}