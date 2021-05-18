package br.com.concepting.framework.controller;

import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.*;
import br.com.concepting.framework.security.constants.SecurityConstants;
import br.com.concepting.framework.security.controller.SecurityController;
import br.com.concepting.framework.security.exceptions.PermissionDeniedException;
import br.com.concepting.framework.security.exceptions.UserNotAuthorizedException;
import br.com.concepting.framework.security.model.LoginParameterModel;
import br.com.concepting.framework.security.model.LoginSessionModel;
import br.com.concepting.framework.security.model.UserModel;
import br.com.concepting.framework.service.interfaces.IService;
import br.com.concepting.framework.service.util.ServiceUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import java.io.IOException;

public abstract class BaseFilter implements Filter{
    private SystemController systemController = null;
    private SecurityController securityController = null;
    
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
     * @param modelClass Class that defines the data model.
     * @return Instance that contains the service implementation of the data model.
     * @throws InternalErrorException Occurs when was not possible to
     * instantiate the service implementation.
     */
    protected <S extends IService<? extends BaseModel>> S getService(Class<? extends BaseModel> modelClass) throws InternalErrorException{
        if(modelClass != null){
            LoginSessionModel loginSession = this.securityController.getLoginSession();
            
            return ServiceUtil.getByModelClass(modelClass, loginSession);
        }
        
        return null;
    }
    
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
    }
    
    /**
     * Initializes the security filter.
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
               SS extends SystemSessionModel> void initialize() throws UserNotAuthorizedException, PermissionDeniedException, InternalErrorException{
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException{
        ((HttpServletResponse) response).setHeader("Access-Control-Allow-Origin", "*");
        ((HttpServletResponse) response).setHeader("Access-Control-Allow-Methods", "HEAD, DELETE, PUT, POST, GET, OPTIONS");
        ((HttpServletResponse) response).setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, ".concat(SecurityConstants.LOGIN_SESSION_ATTRIBUTE_ID));
        
        if(((HttpServletRequest) request).getMethod().equals(HttpMethod.OPTIONS))
            filterChain.doFilter(request, response);
        else{
            this.systemController = new SystemController((HttpServletRequest) request, (HttpServletResponse) response);
            this.systemController.setCurrentException(null);
            
            this.securityController = this.systemController.getSecurityController();
            
            try{
                initialize();
                process();
                
                filterChain.doFilter(request, response);
            }
            catch(UserNotAuthorizedException | PermissionDeniedException | InternalErrorException e){
                this.systemController.forward(e);
            }
        }
    }
}