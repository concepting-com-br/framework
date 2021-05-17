package br.com.concepting.framework.controller.action;

import br.com.concepting.framework.constants.SystemConstants;
import br.com.concepting.framework.controller.SystemController;
import br.com.concepting.framework.controller.form.ActionFormController;
import br.com.concepting.framework.controller.form.BaseActionForm;
import br.com.concepting.framework.controller.form.annotations.ActionForm;
import br.com.concepting.framework.controller.form.annotations.Forward;
import br.com.concepting.framework.controller.form.util.ActionFormUtil;
import br.com.concepting.framework.controller.types.ScopeType;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.BaseModel;
import br.com.concepting.framework.model.FormModel;
import br.com.concepting.framework.model.SystemModuleModel;
import br.com.concepting.framework.model.exceptions.ItemNotFoundException;
import br.com.concepting.framework.resources.exceptions.InvalidResourcesException;
import br.com.concepting.framework.security.controller.SecurityController;
import br.com.concepting.framework.security.exceptions.PermissionDeniedException;
import br.com.concepting.framework.security.model.LoginSessionModel;
import br.com.concepting.framework.service.interfaces.IService;
import br.com.concepting.framework.service.util.ServiceUtil;
import br.com.concepting.framework.util.PropertyUtil;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.MethodUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Class that defines the basic implementation of the actions of a form.
 *
 * @param <M> Class that defines the data model.
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
public abstract class BaseAction<M extends BaseModel>{
    private SystemController systemController = null;
    private ActionFormController actionFormController = null;
    private SecurityController securityController = null;
    private BaseActionForm<M> actionForm = null;
    
    /**
     * Returns the instance of the system controller.
     *
     * @return Instance that contains the system controller.
     */
    protected SystemController getSystemController(){
        return this.systemController;
    }
    
    /**
     * Returns the instance of the form controller.
     *
     * @return Instance that contains the form controller.
     */
    protected ActionFormController getActionFormController(){
        return this.actionFormController;
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
    @SuppressWarnings({"unchecked"})
    protected <S extends IService<? extends BaseModel>> S getService(Class<? extends BaseModel> modelClass) throws InternalErrorException{
        if(modelClass == null){
            Class<? extends BaseAction<M>> actionClass = (Class<? extends BaseAction<M>>) getClass();
            
            try{
                modelClass = ActionFormUtil.getModelClassByAction(actionClass);
            }
            catch(ClassNotFoundException e){
                throw new InternalErrorException(e);
            }
        }
        
        LoginSessionModel loginSession = this.securityController.getLoginSession();
        
        return ServiceUtil.getByModelClass(modelClass, loginSession);
    }
    
    /**
     * Returns the service implementation of the default data model.
     *
     * @param <S> Class that defines the service implementation.
     * @return Instance that contains the service implementation of the default data model.
     * @throws InternalErrorException Occurs when was not possible to
     * instantiate the service implementation.
     */
    protected <S extends IService<? extends BaseModel>> S getService() throws InternalErrorException{
        return getService(null);
    }
    
    /**
     * Returns the action form of the data model.
     *
     * @param <B> Class that defines the data model.
     * @param <F> Class that defines the action form.
     * @param modelClass Class that defines the data model.
     * @return Instance of the action form.
     * @throws InternalErrorException Occurs when was not possible to
     * instantiate the service implementation.
     */
    protected <B extends BaseModel, F extends BaseActionForm<B>> F getActionForm(Class<B> modelClass) throws InternalErrorException{
        String actionFormId = ActionFormUtil.getActionFormIdByModel(modelClass);
        
        return getSystemController().getAttribute(actionFormId, ScopeType.SESSION);
    }
    
    /**
     * Returns the action implementation of the data model.
     *
     * @param <B> Class that defines the data model.
     * @param <A> Class that defines the action implementation.
     * @param modelClass Class that defines the data model.
     * @return Instance of the action implementation.
     * @throws InternalErrorException Occurs when was not possible to
     * instantiate the service implementation.
     */
    @SuppressWarnings("unchecked")
    protected <B extends BaseModel, A extends BaseAction<B>> A getAction(Class<B> modelClass) throws InternalErrorException{
        try{
            Class<? extends BaseAction<M>> actionClass = ActionFormUtil.getActionClassByModel(modelClass);
            
            if(actionClass != null)
                return (A) ConstructorUtils.invokeConstructor(actionClass, null);
            
            return null;
        }
        catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Returns the instance of the form.
     *
     * @param <F> Class that defines the form.
     * @return Instance that contains the form.
     * @throws InternalErrorException Occurs when was not possible to
     * instantiate the service implementation.
     */
    @SuppressWarnings("unchecked")
    protected <F extends BaseActionForm<M>> F getActionForm() throws InternalErrorException{
        return (F) this.actionForm;
    }
    
    /**
     * Initialize the form.
     *
     * @throws InternalErrorException Occurs when was not possible to initialize
     * the form.
     */
    protected void initializeActionForm() throws InternalErrorException{
        if(this.actionForm == null)
            return;
        
        String action = this.actionForm.getAction();
        
        PropertyUtil.clearAllProperties(this.actionForm);
        
        this.actionForm.setAction(action);
    }
    
    /**
     * Loads the form objects.
     *
     * @throws InternalErrorException Occurs when was not possible to load the
     * objects of the form.
     */
    @SuppressWarnings("unchecked")
    protected void loadActionFormObjects() throws InternalErrorException{
        if(this.securityController == null)
            return;
        
        try{
            LoginSessionModel loginSession = this.securityController.getLoginSession();
            SystemModuleModel systemModule = (loginSession != null ? loginSession.getSystemModule() : null);
            ActionForm actionForm = this.actionForm.getClass().getAnnotation(ActionForm.class);
            FormModel form = (systemModule != null ? systemModule.getForm(actionForm.name()) : null);
            
            if(form != null){
                Class<FormModel> formClass = (Class<FormModel>) form.getClass();
                IService<FormModel> formService = null;
                
                try{
                    formService = getService(formClass);
                }
                catch(InternalErrorException e){
                }
                
                if(formService != null){
                    form = formService.find(form);
                    form = formService.loadReference(form, SystemConstants.OBJECTS_ATTRIBUTE_ID);
                    
                    systemModule.setForm(form);
                    
                    loginSession.setSystemModule(systemModule);
                    
                    this.securityController.setLoginSession(loginSession);
                }
            }
        }
        catch(ItemNotFoundException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Action that initializes the form.
     *
     * @param <F> Class that defines the form.
     * @throws Throwable Occurs when was not possible to execute the action.
     */
    public <F extends BaseActionForm<M>> void init() throws Throwable{
        initializeActionForm();
        
        loadActionFormObjects();
    }
    
    /**
     * Action to back to a previous state.
     *
     * @param <F> Class that defines the form.
     * @throws Throwable Occurs when was not possible to execute the action.
     */
    public <F extends BaseActionForm<M>> void back() throws Throwable{
        this.actionForm.removeActionHistory();
    }
    
    /**
     * Action to cancel an operation.
     *
     * @param <F> Class that defines the form.
     * @throws Throwable Occurs when was not possible to execute the action.
     */
    public <F extends BaseActionForm<M>> void cancel() throws Throwable{
    }
    
    /**
     * Action to refresh a content.
     *
     * @param <F> Class that defines the form.
     * @throws Throwable Occurs when was not possible to execute the action.
     */
    public <F extends BaseActionForm<M>> void refresh() throws Throwable{
        back();
    }
    
    /**
     * Action to upload a content.
     *
     * @param <F> Class that defines the form.
     * @throws Throwable Occurs when was not possible to execute the action.
     */
    public <F extends BaseActionForm<M>> void upload() throws Throwable{
        back();
    }
    
    /**
     * Process the action form request.
     *
     * @param actionForm Instance that contains the action form.
     * @param systemController Instance that contains the system controller.
     * @param actionFormController Instance that contains the action form
     * controller.
     * @param securityController Instance that contains the security controller.
     * @throws Throwable Occurs when was not possible to execution the
     * operation.
     */
    public Forward processRequest(BaseActionForm<M> actionForm, SystemController systemController, ActionFormController actionFormController, SecurityController securityController) throws Throwable{
        if(actionForm == null || systemController == null || actionFormController == null || securityController == null)
            return null;
        
        this.actionForm = actionForm;
        this.systemController = systemController;
        this.actionFormController = actionFormController;
        this.securityController = securityController;
        
        try{
            String action = this.actionForm.getAction();
            
            if(action != null && action.length() > 0){
                Method method = getClass().getMethod(action);
                Forward forward = method.getAnnotation(Forward.class);
                
                method.invoke(this);
                
                return forward;
            }
            
            return null;
        }
        catch(Throwable e){
            if(e instanceof NoSuchMethodException)
                throw new InvalidResourcesException(systemController.getRequestURI(), e);
            else if(e instanceof IllegalAccessException)
                throw new PermissionDeniedException(e);
            
            throw e;
        }
    }
}