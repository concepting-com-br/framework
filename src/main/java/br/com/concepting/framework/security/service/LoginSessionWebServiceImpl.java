package br.com.concepting.framework.security.service;

import br.com.concepting.framework.audit.annotations.Auditable;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.security.constants.SecurityConstants;
import br.com.concepting.framework.security.exceptions.*;
import br.com.concepting.framework.security.model.LoginParameterModel;
import br.com.concepting.framework.security.model.LoginSessionModel;
import br.com.concepting.framework.security.model.UserModel;
import br.com.concepting.framework.security.service.interfaces.LoginSessionService;
import br.com.concepting.framework.security.service.interfaces.LoginSessionWebService;
import br.com.concepting.framework.service.BaseWebService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Class that defines the basic implementation of the login session WebService
 * implementation.
 *
 * @param <L> Class that defines the login session data model.
 * @param <U> Class that defines the user data model.
 * @param <LP> Class that defines the login parameter data model.
 * @since 3.3.0
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
public abstract class LoginSessionWebServiceImpl<L extends LoginSessionModel, U extends UserModel, LP extends LoginParameterModel> extends BaseWebService<L> implements LoginSessionWebService<L, U, LP>{
    /**
     * @see br.com.concepting.framework.security.service.interfaces.LoginSessionService#sendForgottenPassword(br.com.concepting.framework.security.model.UserModel)
     */
    @POST
    @Path("sendForgottenPassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Auditable
    @Override
    public void sendForgottenPassword(U user) throws UserNotFoundException, UserBlockedException, InternalErrorException{
        LoginSessionService<L, U, LP> loginSessionService = getService();
        
        loginSessionService.sendForgottenPassword(user);
    }
    
    /**
     * @see br.com.concepting.framework.security.service.interfaces.LoginSessionService#changePassword(br.com.concepting.framework.security.model.UserModel)
     */
    @SuppressWarnings("unchecked")
    @POST
    @Path("changePassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Auditable
    @Override
    public U changePassword(U user) throws PasswordIsNotStrongException, PasswordsNotMatchException, PasswordWithoutMinimumLengthException, InternalErrorException{
        LoginSessionService<L, U, LP> service = getService();
        L loginSession = getLoginSession();
        U currentUser = loginSession.getUser();
        
        user.setId(currentUser.getId());
        
        user = service.changePassword(user);
        
        return user;
    }
    
    /**
     * @see br.com.concepting.framework.security.service.interfaces.LoginSessionService#validateMfaToken(br.com.concepting.framework.security.model.UserModel)
     */
    @SuppressWarnings("unchecked")
    @POST
    @Path("validateMfaToken")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Auditable
    @Override
    public void validateMfaToken(U user) throws InvalidMfaTokenException, InternalErrorException{
        LoginSessionService<L, U, LP> service = getService();
        L loginSession = getLoginSession();
        U currentUser = loginSession.getUser();
        
        currentUser.setMfaToken(user.getMfaToken());
        
        service.validateMfaToken(currentUser);
    }
    
    /**
     * @see br.com.concepting.framework.security.service.interfaces.LoginSessionService#logOut()
     */
    @POST
    @Path("logOut")
    @Auditable
    @Override
    public void logOut() throws InternalErrorException{
        LoginSessionService<L, U, LP> service = getService();
        
        service.logOut();
        
        getSystemController().removeCookie(SecurityConstants.LOGIN_SESSION_ATTRIBUTE_ID);
    }
    
    /**
     * @see br.com.concepting.framework.security.service.interfaces.LoginSessionService#logOutAll()
     */
    @Override
    public void logOutAll() throws InternalErrorException{
    }
    
    /**
     * @see br.com.concepting.framework.security.service.interfaces.LoginSessionService#logIn(br.com.concepting.framework.security.model.UserModel)
     */
    @POST
    @Path("logIn")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Auditable
    @Override
    public L logIn(U user) throws UserAlreadyLoggedInException, UserNotFoundException, UserBlockedException, InvalidPasswordException, PermissionDeniedException, InternalErrorException{
        LoginSessionService<L, U, LP> service = getService();
        L loginSession = service.logIn(user);
        
        getSystemController().addCookie(SecurityConstants.LOGIN_SESSION_ATTRIBUTE_ID, loginSession.getId(), true);
        
        return loginSession;
    }
}