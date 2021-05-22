package br.com.concepting.framework.controller;

import br.com.concepting.framework.controller.action.types.ActionType;
import br.com.concepting.framework.controller.form.constants.ActionFormConstants;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.*;
import br.com.concepting.framework.model.util.ModelUtil;
import br.com.concepting.framework.resources.exceptions.InvalidResourcesException;
import br.com.concepting.framework.security.constants.SecurityConstants;
import br.com.concepting.framework.security.controller.SecurityController;
import br.com.concepting.framework.security.exceptions.PermissionDeniedException;
import br.com.concepting.framework.security.exceptions.UserNotAuthorizedException;
import br.com.concepting.framework.security.model.LoginParameterModel;
import br.com.concepting.framework.security.model.LoginSessionModel;
import br.com.concepting.framework.security.model.UserModel;
import br.com.concepting.framework.security.util.SecurityUtil;
import br.com.concepting.framework.ui.constants.UIConstants;

import javax.servlet.annotation.WebFilter;

@WebFilter(filterName = "systemFilter", urlPatterns = {"/", "/index.jsp"})
public class SystemFilter extends BaseFilter{
    @Override
    protected <L extends LoginSessionModel,
               U extends UserModel,
               LP extends LoginParameterModel,
               F extends FormModel,
               UL extends UrlModel,
               SM extends SystemModuleModel,
               SS extends SystemSessionModel> void process() throws UserNotAuthorizedException, PermissionDeniedException, InternalErrorException{
        SystemController systemController = getSystemController();
        SecurityController securityController = getSecurityController();
        L loginSession = (securityController != null ? securityController.getLoginSession() : null);
        SM systemModule = (loginSession != null ? loginSession.getSystemModule() : null);
    
        if(systemModule != null && systemModule.getId() != null && systemModule.getId() > 0 && systemModule.getActive() != null && systemModule.getActive() && systemController != null && securityController != null){
            U user = (loginSession != null ? loginSession.getUser() : null);
            LP loginParameter = (user != null ? user.getLoginParameter() : null);
            Class<? extends BaseModel> modelClass = null;
            String action = systemController.getRequestParameterValue(ActionFormConstants.ACTION_ATTRIBUTE_ID);
            String actionMethod = null;
            StringBuilder url = new StringBuilder();
        
            if(securityController.isLoginSessionAuthenticated() != null && securityController.isLoginSessionAuthenticated()){
                if(loginParameter != null){
                    if(loginParameter.hasMfa() != null && loginParameter.hasMfa() && (loginParameter.isMfaTokenValidated() == null || !loginParameter.isMfaTokenValidated())){
                        modelClass = SecurityUtil.getLoginSessionClass();
                        actionMethod = ActionType.REFRESH.getMethod();
                    }
                    else{
                        if(loginParameter.changePassword() == null || !loginParameter.changePassword()){
                            modelClass = ModelUtil.getMainConsoleClass();
                            actionMethod = ActionType.INIT.getMethod();
                        }
                        else{
                            modelClass = SecurityUtil.getLoginSessionClass();
                            actionMethod = ActionType.REFRESH.getMethod();
                        }
                    }
                }
                else{
                    modelClass = SecurityUtil.getLoginSessionClass();
                    actionMethod = ActionType.REFRESH.getMethod();
                }
            }
            else{
                modelClass = SecurityUtil.getLoginSessionClass();
            
                if(action != null && action.equals(SecurityConstants.DEFAULT_SEND_FORGOTTEN_PASSWORD_ID))
                    actionMethod = ActionType.REFRESH.getMethod();
                else
                    actionMethod = ActionType.INIT.getMethod();
            }
        
            if(modelClass != null){
                String actionFormUrl = ModelUtil.getUrlByModel(modelClass);
            
                url.append(actionFormUrl);
                url.append(ActionFormConstants.DEFAULT_ACTION_SERVLET_FILE_EXTENSION);
                url.append("?");
                url.append(ActionFormConstants.ACTION_ATTRIBUTE_ID);
                url.append("=");
                url.append(actionMethod);
            
                systemController.forward(url.toString());
            }
        }
        else
            throw new InvalidResourcesException(systemController.getRequestURL());
    }
}