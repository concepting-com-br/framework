package br.com.concepting.framework.ui.components;

import br.com.concepting.framework.constants.Constants;
import br.com.concepting.framework.controller.SystemController;
import br.com.concepting.framework.controller.form.BaseActionForm;
import br.com.concepting.framework.controller.form.annotations.ActionForm;
import br.com.concepting.framework.controller.form.annotations.Forward;
import br.com.concepting.framework.controller.form.constants.ActionFormConstants;
import br.com.concepting.framework.controller.form.util.ActionFormUtil;
import br.com.concepting.framework.controller.types.ScopeType;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.*;
import br.com.concepting.framework.model.util.ModelUtil;
import br.com.concepting.framework.security.controller.SecurityController;
import br.com.concepting.framework.security.model.LoginSessionModel;
import br.com.concepting.framework.ui.constants.UIConstants;
import br.com.concepting.framework.util.PropertyUtil;
import br.com.concepting.framework.util.ReflectionUtil;
import br.com.concepting.framework.util.types.ComponentType;

import javax.servlet.jsp.JspException;
import javax.ws.rs.HttpMethod;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Class that defines the bread crumb componente (navigation history).
 *
 * @author fvilarinho
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
public class BreadCrumbComponent extends BaseActionFormComponent{
    private static final long serialVersionUID = 4253305064847746149L;
    
    /**
     * Lookup the navigation history.
     *
     * @return Instance that contains the current object of the navigation history.
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    protected ObjectModel lookupNavigation() throws InternalErrorException{
        SystemController systemController = getSystemController();
        SecurityController securityController = getSecurityController();
        String actionFormName = getActionFormName();
        ObjectModel object = null;
        
        if(actionFormName != null && actionFormName.length() > 0 && systemController != null && securityController != null){
            LoginSessionModel loginSession = securityController.getLoginSession();
            SystemModuleModel systemModule = loginSession.getSystemModule();
            FormModel form = systemModule.getForm(actionFormName);
            Collection<? extends ObjectModel> objects = (form != null ? form.getObjects() : null);
            
            try{
                if(objects != null && !objects.isEmpty()){
                    String path = systemController.getRequestPath();
                    String action = path;
                    int pos = (action != null ? action.lastIndexOf("/") : -1);
                    
                    if(pos >= 0)
                        action = action.substring(0, pos);
                    
                    Set<Class<?>> actionFormClasses = ReflectionUtil.getTypesAnnotatedWith(ActionForm.class);
                    BaseActionForm<? extends BaseModel> actionFormInstance = null;
                    Forward[] actionFormForwards = null;

                    if(actionFormClasses != null && !actionFormClasses.isEmpty()){
                        for(Class<?> actionFormClass : actionFormClasses){
                            ActionForm actionForm = actionFormClass.getAnnotation(ActionForm.class);
                            
                            if(action.equals(actionForm.action())){
                                actionFormForwards = actionForm.forwards();
                                actionFormInstance = getSystemController().getAttribute(actionForm.name(), ScopeType.SESSION);
                                
                                break;
                            }
                        }
                    }
                    
                    if(actionFormInstance != null){
                        if(actionFormForwards != null && actionFormForwards.length > 0){
                            Forward actionFormForward = null;
                            
                            for(int cont = 0; cont < actionFormForwards.length ; cont++){
                                actionFormForward = actionFormForwards[cont];
                                
                                if(path.equals(actionFormForward.url()))
                                    break;
                                
                                actionFormForward = null;
                            }
                            
                            StringBuilder actionFormUrl = new StringBuilder();
                            
                            actionFormUrl.append(action);
                            actionFormUrl.append(ActionFormConstants.DEFAULT_ACTION_SERVLET_FILE_EXTENSION);
                            actionFormUrl.append(".*");
                            
                            if(actionFormForward != null && !actionFormForward.name().equals(ActionFormConstants.DEFAULT_FORWARD_ID)){
                                actionFormUrl.append(ActionFormConstants.FORWARD_ATTRIBUTE_ID);
                                actionFormUrl.append("=");
                                actionFormUrl.append(actionFormForward.name());
                                actionFormUrl.append(".*");
                            }
                            
                            Iterator<? extends ObjectModel> iterator = objects.iterator();
                            
                            while(iterator.hasNext()){
                                object = iterator.next();
                                
                                if(object != null && object.getType() != null && object.getType().equals(ComponentType.MENU_ITEM))
                                    if(object.getAction() != null && object.getAction().matches(actionFormUrl.toString()))
                                        break;
                                
                                object = null;
                            }
                        }
                    }
                }
            }
            catch(Throwable e){
                object = null;
            }
        }
        
        return object;
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseActionFormComponent#getActionFormComponent()
     */
    protected ActionFormComponent getActionFormComponent() throws InternalErrorException{
        String actionFormName = getActionFormName();
        
        if(actionFormName == null || actionFormName.length() == 0){
            Class<? extends MainConsoleModel> modelClass = ModelUtil.getMainConsoleClass();
            
            if(modelClass != null){
                actionFormName = ActionFormUtil.getActionFormIdByModel(modelClass);
                
                setActionFormName(actionFormName);
            }
        }
        
        return super.getActionFormComponent();
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseActionFormComponent#buildLabel()
     */
    protected void buildLabel() throws InternalErrorException{
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseActionFormComponent#buildTooltip()
     */
    protected void buildTooltip() throws InternalErrorException{
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseActionFormComponent#buildAlignment()
     */
    protected void buildAlignment() throws InternalErrorException{
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BasePropertyComponent#buildEvents()
     */
    protected void buildEvents() throws InternalErrorException{
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseActionFormComponent#buildRestrictions()
     */
    protected void buildRestrictions() throws InternalErrorException{
        setShowLabel(false);
        
        super.buildRestrictions();
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseActionFormComponent#initialize()
     */
    protected void initialize() throws InternalErrorException{
        setComponentType(ComponentType.BREAD_CRUMB);
        
        super.initialize();
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseActionFormComponent#renderOpen()
     */
    protected void renderOpen() throws InternalErrorException{
        print("<table");
        
        String styleClass = getStyleClass();
        
        if(styleClass != null && styleClass.length() > 0){
            print(" class=\"");
            print(styleClass);
            print("\"");
        }
        
        String style = getStyle();
        
        if(style != null && style.length() > 0){
            print(" style=\"");
            print(style);
            
            if(!style.endsWith(";"))
                print(";");
            
            print("\"");
        }
        
        println(">");
        println("<tr>");
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseActionFormComponent#renderBody()
     */
    protected void renderBody() throws InternalErrorException{
        println("<td>");
        
        SecurityController securityController = getSecurityController();
        Boolean authenticated = (securityController != null ? securityController.isLoginSessionAuthenticated() : null);
        String contextPath = getContextPath();
        String actionFormName = getActionFormName();
        
        if(contextPath != null && contextPath.length() > 0 && actionFormName != null && actionFormName.length() > 0 && authenticated != null && authenticated){
            LinkComponent linkComponent = new LinkComponent();
            
            linkComponent.setPageContext(this.pageContext);
            linkComponent.setOutputStream(getOutputStream());
            linkComponent.setActionFormName(actionFormName);
            linkComponent.setLabel(UIConstants.DEFAULT_BREAD_CRUMB_HOME_ID);
            linkComponent.setTooltip(UIConstants.DEFAULT_BREAD_CRUMB_HOME_ID);
            
            StringBuilder onClick = new StringBuilder();
            
            onClick.append("submitRequest('");
            onClick.append(HttpMethod.GET);
            onClick.append("', '");
            onClick.append(contextPath);
            onClick.append("/');");
            
            linkComponent.setOnClick(onClick.toString());
            
            try{
                linkComponent.doStartTag();
                linkComponent.doEndTag();
            }
            catch(JspException e){
                throw new InternalErrorException(e);
            }
            
            renderNavigation();
        }
        
        println("</td>");
    }
    
    /**
     * Renders the navigation history.
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void renderNavigation() throws InternalErrorException{
        String actionFormName = getActionFormName();
        String contextPath = getContextPath();
        
        if(contextPath != null && contextPath.length() > 0 && actionFormName != null && actionFormName.length() > 0){
            try{
                ObjectModel object = lookupNavigation();
                
                if(object == null)
                    return;
                
                ObjectModel parentObject = object;
                List<ObjectModel> navigationHistory = PropertyUtil.instantiate(Constants.DEFAULT_LIST_CLASS);
                
                navigationHistory.add(object);
                
                do{
                    parentObject = parentObject.getParent();
                    
                    if(parentObject != null)
                        navigationHistory.add(parentObject);
                }
                while(parentObject != null);
                
                String action = null;
                ObjectModel navigationHistoryItem = null;
                
                for(int cont = navigationHistory.size() - 1; cont >= 0; cont--){
                    println(" / ");
                    
                    navigationHistoryItem = navigationHistory.get(cont);
                    
                    LinkComponent linkComponent = new LinkComponent();
                    
                    linkComponent.setPageContext(this.pageContext);
                    linkComponent.setOutputStream(getOutputStream());
                    linkComponent.setActionFormName(actionFormName);
                    linkComponent.setName(navigationHistoryItem.getName());
                    linkComponent.setLabel(navigationHistoryItem.getTitle());
                    linkComponent.setTooltip(navigationHistoryItem.getTooltip());
                    linkComponent.setTarget(navigationHistoryItem.getActionTarget());
                    linkComponent.setResourcesId(getResourcesId());
                    linkComponent.setResourcesKey(navigationHistoryItem.getName());
                    
                    action = navigationHistoryItem.getAction();
                    
                    if(action != null && action.length() > 0){
                        if(!action.toLowerCase().startsWith("javascript")){
                            StringBuilder onClick = new StringBuilder();
                            
                            onClick.append("submitRequest('");
                            onClick.append(HttpMethod.GET);
                            onClick.append("', '");
                            onClick.append(contextPath);
                            onClick.append(action);
                            onClick.append("'");
                            
                            String actionTarget = navigationHistoryItem.getActionTarget();
                            
                            if(actionTarget != null && actionTarget.length() > 0){
                                onClick.append(", null, null, null, '");
                                onClick.append(actionTarget);
                                onClick.append("'");
                            }
                            
                            onClick.append(");");
                            
                            linkComponent.setOnClick(onClick.toString());
                        }
                        else
                            linkComponent.setUrl(action);
                    }
                    
                    linkComponent.doStartTag();
                    linkComponent.doEndTag();
                }
            }
            catch(JspException e){
                throw new InternalErrorException(e);
            }
        }
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseActionFormComponent#renderClose()
     */
    protected void renderClose() throws InternalErrorException{
        println("</tr>");
        println("</table>");
    }
}