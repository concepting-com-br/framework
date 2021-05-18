package br.com.concepting.framework.security.controller;

import br.com.concepting.framework.controller.SystemController;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.BaseModel;
import br.com.concepting.framework.security.model.LoginParameterModel;
import br.com.concepting.framework.security.model.LoginSessionModel;
import br.com.concepting.framework.security.model.UserModel;
import br.com.concepting.framework.security.service.interfaces.LoginSessionService;
import br.com.concepting.framework.security.util.SecurityUtil;
import br.com.concepting.framework.service.interfaces.IService;
import br.com.concepting.framework.service.util.ServiceUtil;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Class responsible to listen the events of a login session.
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
@WebListener
public class LoginSessionListener implements HttpSessionListener{
    private SystemController systemController = null;
    private SecurityController securityController = null;
    private LoginSessionModel loginSession = null;
    
    /**
     * Returns the instance of the system controller.
     *
     * @return Instance that contains the system controller.
     */
    protected SystemController getSystemController(){
        return this.systemController;
    }
    
    /**
     * Returns the instance of the security controller.
     *
     * @return Instance that contains the security controller.
     */
    protected SecurityController getSecurityController(){
        return this.securityController;
    }
    
    /**
     * Returns the service implementation of a specific data model.
     *
     * @param <S> Class that defines the service implementation.
     * @return Instance that contains the service implementation of the data model.
     * @throws InternalErrorException Occurs when was not possible to
     * instantiate the service implementation.
     */
    @SuppressWarnings("unchecked")
    protected <S extends LoginSessionService<? extends LoginSessionModel, ? extends UserModel, ? extends LoginParameterModel>> S getService() throws InternalErrorException{
        if(this.loginSession != null)
            return getService(this.loginSession.getClass());
        
        return null;
    }
    
    /**
     * Returns the service implementation of a specific data model.
     *
     * @param <S> Class that defines the service implementation.
     * @param modelClass Class that defines the data model.
     * @return Instance that contains the service implementation of the data model.
     * @throws InternalErrorException Occurs when was not possible to
     * instantiate the service implementation.
     */
    protected <S extends IService<? extends BaseModel>> S getService(Class<? extends BaseModel> modelClass) throws InternalErrorException{
        if(this.loginSession != null){
            if(modelClass == null)
                modelClass = this.loginSession.getClass();
            
            S service = ServiceUtil.getByModelClass(modelClass, this.loginSession);
            
            return service;
        }
        
        return null;
    }
    
    /**
     * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionCreated(HttpSessionEvent event){
        this.systemController = new SystemController(event.getSession());
        this.securityController = this.systemController.getSecurityController();
        
        try{
            initialize();
            
            onCreate();
        }
        catch(InternalErrorException e){
            if(this.systemController != null)
                this.systemController.setCurrentException(e);
        }
    }
    
    /**
     * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionDestroyed(HttpSessionEvent event){
        try{
            if(this.securityController != null){
                this.loginSession = this.securityController.getLoginSession();
                
                onDestroy();
            }
        }
        catch(InternalErrorException e){
        }
    }
    
    /**
     * Initializes the listener.
     *
     * @throws InternalErrorException Occurs when was not possible to read the
     * security resources.
     */
    protected void initialize() throws InternalErrorException{
        if(this.systemController != null)
            this.systemController.setSessionTimeout(SecurityUtil.getLoginSessionTimeout());
    }
    
    /**
     * Executed on the creation of the login session.
     *
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    protected void onCreate() throws InternalErrorException{
    }
    
    /**
     * Executed on the destroy of the login session.
     *
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    protected void onDestroy() throws InternalErrorException{
        if(this.loginSession != null){
            LoginSessionService<? extends LoginSessionModel, ? extends UserModel, ? extends LoginParameterModel> loginSessionService = getService();
            
            loginSessionService.logOut();
            
            this.loginSession = null;
        }
    }
}