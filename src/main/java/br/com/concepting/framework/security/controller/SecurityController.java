package br.com.concepting.framework.security.controller;

import br.com.concepting.framework.constants.Constants;
import br.com.concepting.framework.constants.SystemConstants;
import br.com.concepting.framework.controller.SystemController;
import br.com.concepting.framework.controller.types.ScopeType;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.SystemModuleModel;
import br.com.concepting.framework.model.SystemSessionModel;
import br.com.concepting.framework.security.constants.SecurityConstants;
import br.com.concepting.framework.security.model.LoginParameterModel;
import br.com.concepting.framework.security.model.LoginSessionModel;
import br.com.concepting.framework.security.model.UserModel;
import br.com.concepting.framework.security.util.SecurityUtil;
import br.com.concepting.framework.ui.util.SkinUtil;
import br.com.concepting.framework.util.LanguageUtil;
import br.com.concepting.framework.util.StringUtil;

import javax.servlet.http.Cookie;

/**
 * Class that defines the security controller.
 *
 * @author fvilarinho
 * @since 3.0.0
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
public class SecurityController{
    private SystemController systemController = null;
    
    /**
     * Constructor - Initialize the controller.
     *
     * @param systemController Instance that contains the system controller.
     */
    public SecurityController(SystemController systemController){
        super();
        
        this.systemController = systemController;
    }
    
    /**
     * Indicates if the user of the login session is authenticated.
     *
     * @return True/False.
     */
    public Boolean isLoginSessionAuthenticated(){
        try{
            LoginSessionModel loginSession = getLoginSession();
            
            return (loginSession != null && loginSession.getId() != null && loginSession.getId().length() > 0 && loginSession.isActive() != null && loginSession.isActive());
        }
        catch(InternalErrorException e){
            return false;
        }
    }
    
    /**
     * Indicates if the login session has expired.
     *
     * @return True/False.
     */
    public Boolean isLoginSessionExpired(){
        if(!isLoginSessionAuthenticated()){
            Cookie cookie = this.systemController.getCookie(SecurityConstants.LOGIN_SESSION_ATTRIBUTE_ID);
            
            return (cookie != null && cookie.getValue() != null && cookie.getValue().length() > 0);
        }
        
        return false;
    }
    
    /**
     * Returns the instance that contains the login session data model.
     *
     * @param <L> Class that defines the data model.
     * @return Instance that contains the login session data model.
     * @throws InternalErrorException Occurs when was not possible to get the
     * login session.
     */
    @SuppressWarnings("unchecked")
    public <L extends LoginSessionModel> L getLoginSession() throws InternalErrorException{
        if(this.systemController == null)
            return null;
        
        Boolean isWebServicesRequest = this.systemController.isWebServicesRequest();
        LoginSessionModel loginSession = this.systemController.getAttribute(SecurityConstants.LOGIN_SESSION_ATTRIBUTE_ID, (isWebServicesRequest != null && isWebServicesRequest ? ScopeType.REQUEST : ScopeType.SESSION));
        
        loginSession = SecurityUtil.getLoginSession(loginSession);
        
        setLoginSession(loginSession);
        
        String loginSessionId = this.systemController.getRequestHeader(SecurityConstants.LOGIN_SESSION_ATTRIBUTE_ID);
        
        if(loginSessionId != null && loginSessionId.length() > 0 && isWebServicesRequest != null && isWebServicesRequest)
            loginSession.setId(loginSessionId);
        else{
            SystemSessionModel systemSession = loginSession.getSystemSession();
            
            if(systemSession != null){
                String currentSystemSessionId = systemSession.getId();
                
                if(currentSystemSessionId == null || currentSystemSessionId.length() == 0){
                    if(isWebServicesRequest != null && isWebServicesRequest)
                        systemSession.setId(SecurityUtil.generateToken());
                    else
                        systemSession.setId(this.systemController.getSessionId());
                    
                    SystemModuleModel systemModule = loginSession.getSystemModule();
                    
                    if(systemModule != null)
                        systemModule.setUrl(this.systemController.getContextPath());
                }
                
                String ip = this.systemController.getRequestIp();
                
                if(ip != null && ip.length() > 0){
                    String currentIp = systemSession.getIp();
                    
                    if(currentIp == null || !currentIp.equals(ip)){
                        systemSession.setIp(ip);
                        systemSession.setHostName(this.systemController.getRequestHostName());
                    }
                }
            }
            
            UserModel user = loginSession.getUser();
            LoginParameterModel loginParameter = user.getLoginParameter();
            
            if(loginParameter != null){
                if(loginParameter.getLanguage() == null || loginParameter.getLanguage().length() == 0){
                    Cookie languageCookie = this.systemController.getCookie(SystemConstants.CURRENT_LANGUAGE_ATTRIBUTE_ID);
                    
                    if(languageCookie == null){
                        String language = loginParameter.getLanguage();
                        
                        if(language == null || language.length() == 0){
                            String acceptLanguage = this.systemController.getRequestAcceptLanguage();
                            
                            if(acceptLanguage != null && acceptLanguage.length() > 0){
                                int pos = acceptLanguage.indexOf(Constants.DEFAULT_DELIMITER);
                                
                                if(pos >= 0){
                                    acceptLanguage = acceptLanguage.substring(0, pos);
                                    
                                    loginParameter.setLanguage(acceptLanguage);
                                }
                                else
                                    loginParameter.setLanguage(LanguageUtil.getDefaultLanguage().toString());
                            }
                            else
                                loginParameter.setLanguage(LanguageUtil.getDefaultLanguage().toString());
                        }
                        
                        this.systemController.addCookie(SystemConstants.CURRENT_LANGUAGE_ATTRIBUTE_ID, loginParameter.getLanguage());
                    }
                    else
                        loginParameter.setLanguage(languageCookie.getValue());
                }
                
                if(loginParameter.getSkin() == null || loginParameter.getSkin().length() == 0){
                    Cookie skinCookie = this.systemController.getCookie(SystemConstants.CURRENT_SKIN_ATTRIBUTE_ID);
    
                    if(skinCookie == null){
                        String skin = loginParameter.getSkin();
    
                        if(skin == null || skin.length() == 0)
                            loginParameter.setSkin(SkinUtil.getDefaultSkin());
    
                        this.systemController.addCookie(SystemConstants.CURRENT_SKIN_ATTRIBUTE_ID, loginParameter.getSkin());
                    }
                    else
                        loginParameter.setSkin(skinCookie.getValue());
                }
            }
        }
        
        return (L) loginSession;
    }
    
    /**
     * Defines the instance that contains the login session data model.
     *
     * @param loginSession Instance that contains the login session data model.
     */
    public void setLoginSession(LoginSessionModel loginSession){
        Boolean isWebServicesRequest = (this.systemController != null ? this.systemController.isWebServicesRequest() : null);
        
        this.systemController.setAttribute(SecurityConstants.LOGIN_SESSION_ATTRIBUTE_ID, loginSession, (isWebServicesRequest != null && isWebServicesRequest ? ScopeType.REQUEST : ScopeType.SESSION));
    }
    
    /**
     * Returns the name of the user that is stored in cookie to be remembered
     * later.
     *
     * @return String that contains the name of the user.
     */
    public String getRememberedUserName(){
        Cookie cookie = this.systemController.getCookie(SecurityConstants.REMEMBER_USER_AND_PASSWORD_ATTRIBUTE_ID);
        
        if(cookie != null && cookie.getValue() != null){
            String[] values = StringUtil.split(cookie.getValue(), "|");
            
            if(values != null && values.length > 1)
                return values[0];
        }
        
        return null;
    }
    
    /**
     * Returns the password of the user that is stored in cookie to be
     * remembered later.
     *
     * @return String that contains the password of the user.
     */
    public String getRememberedPassword(){
        Cookie cookie = this.systemController.getCookie(SecurityConstants.REMEMBER_USER_AND_PASSWORD_ATTRIBUTE_ID);
        
        if(cookie != null && cookie.getValue() != null){
            String[] values = StringUtil.split(cookie.getValue(), "|");
            
            if(values != null && values.length > 1)
                return values[1];
        }
        
        return null;
    }
    
    /**
     * Stores the user name and password in cookie to be remembered later.
     *
     * @param user Instance that contains the user data model.
     */
    public void rememberUserAndPasword(UserModel user){
        if(user != null && user.getName() != null && user.getName().length() > 0){
            String userName = user.getName();
            String password = user.getPassword();
            String value = userName.concat("|").concat(password);
            
            this.systemController.addCookie(SecurityConstants.REMEMBER_USER_AND_PASSWORD_ATTRIBUTE_ID, value, true);
        }
    }
    
    /**
     * Clears the remembered user name and password.
     */
    public void forgetUserAndPassword(){
        this.systemController.removeCookie(SecurityConstants.REMEMBER_USER_AND_PASSWORD_ATTRIBUTE_ID);
    }
}