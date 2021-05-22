package br.com.concepting.framework.controller.form;

import br.com.concepting.framework.constants.Constants;
import br.com.concepting.framework.constants.ProjectConstants;
import br.com.concepting.framework.constants.SystemConstants;
import br.com.concepting.framework.controller.SystemController;
import br.com.concepting.framework.controller.action.BaseAction;
import br.com.concepting.framework.controller.action.types.ActionType;
import br.com.concepting.framework.controller.form.annotations.ActionForm;
import br.com.concepting.framework.controller.form.annotations.Forward;
import br.com.concepting.framework.controller.form.constants.ActionFormConstants;
import br.com.concepting.framework.controller.form.helpers.ActionFormMessage;
import br.com.concepting.framework.controller.form.types.ActionFormMessageType;
import br.com.concepting.framework.controller.form.util.ActionFormPopulator;
import br.com.concepting.framework.controller.form.util.ActionFormUtil;
import br.com.concepting.framework.controller.form.util.ActionFormValidator;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.BaseModel;
import br.com.concepting.framework.model.helpers.ModelInfo;
import br.com.concepting.framework.model.util.ModelUtil;
import br.com.concepting.framework.security.controller.SecurityController;
import br.com.concepting.framework.security.exceptions.PermissionDeniedException;
import br.com.concepting.framework.security.model.LoginSessionModel;
import br.com.concepting.framework.security.model.UserModel;
import br.com.concepting.framework.ui.constants.UIConstants;
import br.com.concepting.framework.util.ExceptionUtil;
import br.com.concepting.framework.util.PropertyUtil;
import org.apache.commons.beanutils.ConstructorUtils;
import org.checkerframework.checker.guieffect.qual.UI;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

/**
 * Class that defines the basic implementation of a form.
 *
 * @param <M> Class that defines the form data model.
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
 * @author fvilarinho
 * @since 1.0.0
 */
@ActionForm
public abstract class BaseActionForm<M extends BaseModel> implements Serializable{
    private static final long serialVersionUID = -4308647670235033678L;
    
    private List<String> actionsHistory = null;
    private String action = null;
    private String forward = null;
    private String updateViews = null;
    private Boolean validateModel = null;
    private String validateModelProperties = null;
    private BaseModel model = null;
    private Collection<ActionFormMessage> messages = null;
    
    /**
     * Returns the actions history.
     *
     * @return List that contains the actions history.
     */
    public List<String> getActionsHistory(){
        return this.actionsHistory;
    }
    
    /**
     * Defines the actions history.
     *
     * @param actionsHistory List that contains the actions history.
     */
    public void setActionsHistory(List<String> actionsHistory){
        this.actionsHistory = actionsHistory;
    }
    
    /**
     * Adds a new action in the history.
     *
     * @param action String that contains the action identifier.
     */
    public void addActionHistory(String action){
        if(!action.equals(ActionType.BACK.getMethod())){
            if(this.actionsHistory == null)
                this.actionsHistory = PropertyUtil.instantiate(Constants.DEFAULT_LIFO_QUEUE_CLASS);
            
            this.actionsHistory.add(action);
        }
        
        this.setAction(action);
    }
    
    /**
     * Removes the last action from the history.
     */
    public void removeActionHistory(){
        if(this.actionsHistory != null && !actionsHistory.isEmpty()){
            actionsHistory.remove(actionsHistory.size() - 1);
            
            if(!actionsHistory.isEmpty())
                setAction(actionsHistory.get(actionsHistory.size() - 1));
        }
    }
    
    /**
     * Clears all form messages.
     */
    public void clearAllMessages(){
        if(this.messages != null && this.messages.size() > 0)
            this.messages.clear();
    }
    
    /**
     * Clears all form messages of a specific type.
     *
     * @param type Instance that contains the type.
     */
    public void clearMessages(ActionFormMessageType type){
        if(this.messages != null && this.messages.size() > 0 && type != null){
            Collection<ActionFormMessage> typeMessages = getMessages(type);
            
            if(typeMessages != null && typeMessages.size() > 0)
                this.messages.removeAll(typeMessages);
        }
    }
    
    /**
     * Returns all form messages.
     *
     * @param <C> Class that defines the list of messages.
     * @return List that contains the messages.
     */
    @SuppressWarnings("unchecked")
    public <C extends Collection<ActionFormMessage>> C getMessages(){
        return (C) this.messages;
    }
    
    /**
     * Returns all form messages of a specific type.
     *
     * @param <C> Class that defines the list of messages.
     * @param type Instance that contains the type.
     * @return List that contains the messages.
     */
    @SuppressWarnings("unchecked")
    public <C extends Collection<ActionFormMessage>> C getMessages(ActionFormMessageType type){
        Collection<ActionFormMessage> result = null;
        
        if(this.messages != null && this.messages.size() > 0 && type != null){
            for(ActionFormMessage message: this.messages){
                if(type.equals(message.getType())){
                    if(result == null)
                        result = PropertyUtil.instantiate(Constants.DEFAULT_LIST_CLASS);
                    
                    result.add(message);
                }
            }
        }
        
        return (C) result;
    }
    
    /**
     * Adds a new form message.
     *
     * @param message Instance that contains the message.
     */
    public void addMessage(ActionFormMessage message){
        if(this.messages == null)
            this.messages = PropertyUtil.instantiate(Constants.DEFAULT_LIST_CLASS);
        
        this.messages.add(message);
    }
    
    /**
     * Defines the form messages.
     *
     * @param messages List that contains the messages.
     */
    public void setMessages(Collection<ActionFormMessage> messages){
        this.messages = messages;
    }
    
    /**
     * Returns a list (comma separated string) containing the views that should
     * be refreshed after an action execution.
     *
     * @return String that contains the views.
     */
    public String getUpdateViews(){
        return this.updateViews;
    }
    
    /**
     * Defines a list (comma separated string) containing the views that should
     * be refreshed after an action execution.
     *
     * @param updateViews String that contains the views.
     */
    public void setUpdateViews(String updateViews){
        this.updateViews = updateViews;
    }
    
    /**
     * Returns the identifier of the current action.
     *
     * @return String that contains the identifier.
     */
    public String getAction(){
        return this.action;
    }
    
    /**
     * Defines the identifier of the current action.
     *
     * @param action String that contains the identifier.
     */
    public void setAction(String action){
        this.action = action;
    }
    
    /**
     * Returns the identifier of the forward in case of success of the action.
     *
     * @return String that contains the identifier.
     */
    public String getForward(){
        return this.forward;
    }
    
    /**
     * Defines the identifier of the forward in case of success of the action.
     *
     * @param forward String that contains the identifier.
     */
    public void setForward(String forward){
        this.forward = forward;
    }
    
    /**
     * Indicates if the data model must be validated.
     *
     * @return True/False.
     */
    public Boolean validateModel(){
        return this.validateModel;
    }
    
    /**
     * Indicates if the data model must be validated.
     *
     * @return True/False.
     */
    public Boolean getValidateModel(){
        return validateModel();
    }
    
    /**
     * Defines if the data model must be validated.
     *
     * @param validateModel True/False.
     */
    public void setValidateModel(Boolean validateModel){
        this.validateModel = validateModel;
    }
    
    /**
     * Returns the instance of the data model.
     *
     * @return Instance that contains the data model.
     */
    @SuppressWarnings("unchecked")
    public M getModel(){
        if(this.model == null){
            try{
                Class<M> modelClass = ActionFormUtil.getModelClassByActionForm(getClass());
                
                this.model = ConstructorUtils.invokeConstructor(modelClass, null);
            }
            catch(ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e){
            }
        }
        
        return (M) this.model;
    }
    
    /**
     * Defines the instance of the data model.
     *
     * @param model Instance that contains the data model.
     */
    public void setModel(BaseModel model){
        this.model = model;
    }
    
    /**
     * Returns a list (comma separated string) containing the data model
     * properties that must be validated.
     *
     * @return String that contains the properties.
     */
    public String getValidateModelProperties(){
        return this.validateModelProperties;
    }
    
    /**
     * Defines a list (comma separated string) containing the data model
     * properties that must be validated.
     *
     * @param validateModelProperties String that contains the properties.
     */
    public void setValidateModelProperties(String validateModelProperties){
        this.validateModelProperties = validateModelProperties;
    }
    
    private String findForwardUrl() throws InternalErrorException{
        if(this.forward == null || this.forward.length() == 0)
            this.forward = ActionFormConstants.DEFAULT_FORWARD_ID;
        
        StringBuilder forwardUrl = new StringBuilder();
        ActionForm actionForm = getClass().getAnnotation(ActionForm.class);
        
        if(actionForm != null){
            Forward defaultForward = actionForm.defaultForward();
            Forward[] forwards = actionForm.forwards();
    
            for(Forward forward: forwards){
                if(this.forward.equals(forward.name())){
                    forwardUrl.append("/");
                    forwardUrl.append(UIConstants.DEFAULT_PAGES_DIR);
                    forwardUrl.append(actionForm.action());
                    forwardUrl.append("/");
                    forwardUrl.append(forward.url());
                    
                    break;
                }
            }

            if(forwardUrl.length() == 0){
                forwardUrl.append("/");
                forwardUrl.append(UIConstants.DEFAULT_PAGES_DIR);
                forwardUrl.append(actionForm.action());
                forwardUrl.append("/");
                forwardUrl.append(defaultForward.url());
            }
        }
        else
            forwardUrl.append("/");
    
        return forwardUrl.toString();
    }
    
    /**
     * Process the action form request.
     *
     * @param systemController Instance that contains the system controller.
     * @param actionFormController Instance that contains the action form
     * controller.
     * @param securityController Instance that contains the security controller.
     * @throws Throwable Occurs when was not possible to execution the operation.
     */
    @SuppressWarnings("unchecked")
    public <L extends LoginSessionModel, U extends UserModel> void processRequest(SystemController systemController, ActionFormController actionFormController, SecurityController securityController) throws Throwable{
        if(systemController == null || actionFormController == null || securityController == null)
            return;
    
        String url = null;
        
        try{
            ActionFormPopulator actionFormPopulator = new ActionFormPopulator(this, systemController, actionFormController, securityController);
            
            actionFormPopulator.populateActionForm();
            
            url = findForwardUrl();
            
            if(securityController.isLoginSessionAuthenticated()){
                L loginSession = securityController.getLoginSession();
                U user = loginSession.getUser();
                
                if(!user.isSuperUser() && !user.hasPermission(url))
                    throw new PermissionDeniedException();
            }
            
            Class<? extends BaseActionForm<M>> actionFormClass = (Class<? extends BaseActionForm<M>>) getClass();
            Class<M> modelClass = ActionFormUtil.getModelClassByActionForm(actionFormClass);
            ModelInfo modelInfo = ModelUtil.getInfo(modelClass);
            Class<? extends ActionFormValidator> actionFormValidatorClass = modelInfo.getActionFormValidatorClass();
            ActionFormValidator actionFormValidator = null;
            Boolean validated = null;
            
            if(actionFormValidatorClass != null){
                actionFormValidator = ConstructorUtils.invokeConstructor(actionFormValidatorClass, new Object[]{this, systemController, actionFormController, securityController});
                
                validated = actionFormValidator.validateModel();
            }
            
            actionFormPopulator.populateModel();
            
            if(validated != null && validated){
                Class<? extends BaseAction<M>> actionClass = ActionFormUtil.getActionClassByModel(modelClass);
                
                if(actionClass != null){
                    BaseAction<M> actionInstance = ConstructorUtils.invokeConstructor(actionClass, null);
                    String methodUrl = actionInstance.processRequest(this, systemController, actionFormController, securityController);
                    
                    if(methodUrl != null && methodUrl.length() > 0)
                        url = methodUrl;
                }
            }
    
            Collection<ActionFormMessage> actionFormMessages = actionFormController.getMessages(ActionFormMessageType.VALIDATION);
            
            if(actionFormMessages == null || actionFormMessages.isEmpty())
                actionFormMessages = actionFormController.getMessages(ActionFormMessageType.WARNING);
            
            if(actionFormMessages == null || actionFormMessages.isEmpty())
                actionFormMessages = actionFormController.getMessages(ActionFormMessageType.ERROR);
            
            if(actionFormMessages != null && !actionFormMessages.isEmpty())
                removeActionHistory();
            
            systemController.forward(url);
        }
        catch(Throwable e){
            removeActionHistory();
            
            Throwable exception = ExceptionUtil.getCause(e);
            
            if(!ExceptionUtil.isExpectedException(exception) && !ExceptionUtil.isInternalErrorException(exception))
                exception = new InternalErrorException(exception);
            
            systemController.setCurrentException(exception);
            
            if(ExceptionUtil.isExpectedException(exception))
                systemController.forward(url);
            else
                systemController.forward(e);
        }
    }
}