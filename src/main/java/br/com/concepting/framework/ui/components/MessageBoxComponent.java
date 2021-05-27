package br.com.concepting.framework.ui.components;

import br.com.concepting.framework.constants.Constants;
import br.com.concepting.framework.constants.SystemConstants;
import br.com.concepting.framework.controller.SystemController;
import br.com.concepting.framework.controller.form.ActionFormController;
import br.com.concepting.framework.controller.form.helpers.ActionFormMessage;
import br.com.concepting.framework.controller.form.types.ActionFormMessageType;
import br.com.concepting.framework.controller.form.util.ActionFormMessageUtil;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.SystemSessionModel;
import br.com.concepting.framework.processors.ExpressionProcessorUtil;
import br.com.concepting.framework.resources.PropertiesResources;
import br.com.concepting.framework.resources.exceptions.InvalidResourcesException;
import br.com.concepting.framework.security.controller.SecurityController;
import br.com.concepting.framework.security.exceptions.PermissionDeniedException;
import br.com.concepting.framework.security.exceptions.UserNotAuthorizedException;
import br.com.concepting.framework.security.model.LoginSessionModel;
import br.com.concepting.framework.ui.components.types.VisibilityType;
import br.com.concepting.framework.ui.constants.UIConstants;
import br.com.concepting.framework.util.ExceptionUtil;
import br.com.concepting.framework.util.PropertyUtil;
import br.com.concepting.framework.util.StringUtil;
import br.com.concepting.framework.util.types.AlignmentType;
import br.com.concepting.framework.util.types.ComponentType;
import br.com.concepting.framework.util.types.PositionType;

import javax.servlet.jsp.ErrorData;
import javax.servlet.jsp.JspException;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Locale;

/**
 * Class that defines the message box component.
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
public class MessageBoxComponent extends DialogBoxComponent{
    private static final long serialVersionUID = 6254384099423430512L;
    
    private String type = null;
    private String message = null;
    private Collection<String> messages = null;
    private Throwable exception = null;
    
    /**
     * Defines the exception of the component.
     *
     * @param exception Instance that contains the exception.
     */
    private void setException(Throwable exception){
        this.exception = exception;
    }
    
    /**
     * Returns the type of the messages.
     *
     * @return String that contains the type of the messages.
     */
    public String getType(){
        return this.type;
    }
    
    /**
     * Defines the type of the messages.
     *
     * @param type String that contains the type of the messages.
     */
    public void setType(String type){
        this.type = type;
    }
    
    /**
     * Returns the type of the messages.
     *
     * @return Instance that contains the type of the messages.
     */
    protected ActionFormMessageType getMessageType(){
        if(this.type != null && this.type.length() > 0){
            try{
                return ActionFormMessageType.valueOf(this.type.toUpperCase());
            }
            catch(IllegalArgumentException e){
            }
        }
        
        return null;
    }
    
    /**
     * Defines the type of the messages.
     *
     * @param messageType Instance that contains the type of the messages.
     */
    protected void setMessageType(ActionFormMessageType messageType){
        if(messageType != null)
            this.type = messageType.toString();
    }
    
    /**
     * Defines the current list of messages.
     *
     * @param messages List that contains the messages.
     */
    private void setMessages(Collection<String> messages){
        this.messages = messages;
    }
    
    /**
     * Returns the message.
     *
     * @return String that contains the message.
     */
    public String getMessage(){
        return this.message;
    }
    
    /**
     * Defines the message.
     *
     * @param message String that contains the message.
     */
    public void setMessage(String message){
        this.message = message;
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.DialogBoxComponent#buildResources()
     */
    protected void buildResources() throws InternalErrorException{
        super.buildResources();
        
        SystemController systemController = getSystemController();
        
        if(systemController == null)
            return;
        
        ActionFormMessageType messageType = getMessageType();
        
        if(messageType == null){
            if(this.exception == null){
                this.exception = systemController.getCurrentException();
                
                if(this.exception == null)
                    this.exception = this.pageContext.getException();
                else
                    systemController.setCurrentException(null);
                
                if(this.exception != null)
                    this.exception = ExceptionUtil.getCause(this.exception);
            }
            
            if(this.exception == null){
                ErrorData errorData = this.pageContext.getErrorData();
                
                if(errorData != null && errorData.getStatusCode() >= 0)
                    this.exception = errorData.getThrowable();
            }
            
            if(this.exception != null){
                if(ExceptionUtil.isExpectedException(this.exception)){
                    if(ExceptionUtil.isExpectedWarningException(this.exception))
                        messageType = ActionFormMessageType.WARNING;
                    else
                        messageType = ActionFormMessageType.ERROR;
                }
                else{
                    if(!ExceptionUtil.isInternalErrorException(this.exception))
                        this.exception = new InternalErrorException(this.exception);
                    
                    messageType = ActionFormMessageType.ERROR;
                }
                
                setType(messageType.toString());
                
                if(ExceptionUtil.isInvalidResourceException(this.exception) || ExceptionUtil.isExpectedException(this.exception)){
                    StringBuilder resourcesKey = new StringBuilder();
                    
                    resourcesKey.append(messageType.toString().toLowerCase());
                    resourcesKey.append(".");
                    resourcesKey.append(ExceptionUtil.getId(this.exception));
                    
                    setResourcesKey(resourcesKey.toString());
                }
                
                if(this.exception.getMessage() != null && this.exception.getMessage().length() > 0)
                    setMessage(this.exception.getMessage());
            }
        }
        
        buildTitle();
        buildMessages();
        
        if(ExceptionUtil.isInvalidResourceException(this.exception) || ExceptionUtil.isExpectedException(this.exception))
            this.exception = null;
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.DialogBoxComponent#buildTitle()
     */
    protected void buildTitle() throws InternalErrorException{
        String title = getTitle();
        
        if(title == null || title.length() == 0){
            PropertiesResources resources = getResources();
            PropertiesResources defaultResources = getDefaultResources();
            ActionFormMessageType messageType = getMessageType();
            String name = getName();
            StringBuilder propertyId = new StringBuilder();
            
            if(messageType != null)
                propertyId.append(messageType.toString().toLowerCase());
            else
                propertyId.append(name);
            
            propertyId.append(".");
            propertyId.append(Constants.LABEL_ATTRIBUTE_ID);
            
            title = (resources != null ? resources.getProperty(propertyId.toString(), false) : null);
            
            if(title == null)
                title = (defaultResources != null ? defaultResources.getProperty(propertyId.toString(), false) : null);
            
            setTitle(title);
        }
    }
    
    /**
     * Builds the messages of the dialog box.
     *
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    protected void buildMessages() throws InternalErrorException{
        SystemController systemController = getSystemController();
        
        if(systemController == null)
            return;
        
        SecurityController securityController = systemController.getSecurityController();
        
        if(securityController == null)
            return;
        
        LoginSessionModel loginSession = securityController.getLoginSession();
        SystemSessionModel systemSession = (loginSession != null ? loginSession.getSystemSession() : null);
        
        if(systemSession == null)
            return;
        
        if(this.exception != null)
            if(!ExceptionUtil.isInvalidResourceException(this.exception) && !ExceptionUtil.isExpectedException(this.exception))
                this.message = ExceptionUtil.getTrace(this.exception);
        
        String domain = systemSession.getId();
        Locale currentLanguage = getCurrentLanguage();
        String resourcesKey = getResourcesKey();
        
        if((resourcesKey == null || resourcesKey.length() == 0) && (this.message == null || this.message.length() == 0)){
            ActionFormController actionFormController = getActionFormController();
            Collection<ActionFormMessage> actionFormMessages = null;
            ActionFormMessageType messageType = getMessageType();
            
            if(actionFormController != null && messageType != null)
                actionFormMessages = actionFormController.getMessages(messageType);
            
            if(actionFormMessages != null && !actionFormMessages.isEmpty()){
                PropertiesResources resources = getResources();
                PropertiesResources defaultResources = getDefaultResources();
                StringBuilder propertyId = null;
                String message = null;
                
                for(ActionFormMessage actionFormMessage: actionFormMessages){
                    if(propertyId == null)
                        propertyId = new StringBuilder();
                    else
                        propertyId.delete(0, propertyId.length());
                    
                    propertyId.append(actionFormMessage.getType().toString().toLowerCase());
                    propertyId.append(".");
                    propertyId.append(actionFormMessage.getKey());
                    
                    message = (resources != null ? resources.getProperty(propertyId.toString(), false) : null);
                    
                    if(message == null)
                        message = (defaultResources != null ? defaultResources.getProperty(propertyId.toString()) : null);
                    
                    if(message != null && message.length() > 0){
                        message = ExpressionProcessorUtil.fillVariablesInString(domain, message, currentLanguage);
                        message = ActionFormMessageUtil.fillAttributesInString(actionFormMessage, message, currentLanguage);
                        
                        if(this.messages == null)
                            this.messages = PropertyUtil.instantiate(Constants.DEFAULT_LIST_CLASS);
                        
                        if(!this.messages.contains(message))
                            this.messages.add(message);
                    }
                }
                
                if(actionFormController != null)
                    actionFormController.clearMessages(messageType);
            }
        }
        else if(resourcesKey != null && resourcesKey.length() > 0 && (this.message == null || this.message.length() == 0)){
            if(this.message == null || this.message.length() == 0){
                PropertiesResources resources = getResources();
                PropertiesResources defaultResources = getDefaultResources();
                
                this.message = (resources != null ? resources.getProperty(resourcesKey, false) : null);
                
                if(this.message == null)
                    this.message = (defaultResources != null ? defaultResources.getProperty(resourcesKey) : null);
                
                if(this.message != null && this.message.length() > 0){
                    this.message = ExpressionProcessorUtil.fillVariablesInString(domain, this.message, currentLanguage);
                    this.message = PropertyUtil.fillPropertiesInString(this.exception, this.message, currentLanguage);
                }
            }
        }
        
        if(this.message != null && this.message.length() > 0){
            if(this.messages == null)
                this.messages = PropertyUtil.instantiate(Constants.DEFAULT_LIST_CLASS);
            
            if(!this.messages.contains(this.message))
                this.messages.add(this.message);
        }
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseActionFormComponent#buildPermissions()
     */
    protected void buildPermissions() throws InternalErrorException{
        setRender(this.exception != null || (this.message != null && this.message.length() > 0) || (this.messages != null && this.messages.size() > 0));
        
        super.buildPermissions();
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseActionFormComponent#initialize()
     */
    protected void initialize() throws InternalErrorException{
        setComponentType(ComponentType.MESSAGE_BOX);
        
        super.initialize();
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.DialogBoxComponent#renderBody()
     */
    protected void renderBody() throws InternalErrorException{
        String name = getName();
        
        if(name != null && name.length() > 0){
            print("<table class=\"");
            print(UIConstants.DEFAULT_CONTENT_PANEL_STYLE_CLASS);
            println("\">");
            println("<tr>");
            
            if(this.type != null && this.type.length() > 0){
                println("<td width=\"10\"></td>");
                
                print("<td align=\"");
                print(AlignmentType.RIGHT);
                print("\" valign=\"");
                print(AlignmentType.TOP);
                print("\" width=\"1\">");
                print("<div class=\"");
                print(this.type.toLowerCase());
                print(StringUtil.capitalize(UIConstants.DEFAULT_ICON_ID));
                println("\"></div>");
                println("</td>");
            }
            
            println("<td width=\"10\"></td>");
            
            println("<td>");
            print("<table class=\"");
            print(UIConstants.DEFAULT_CONTENT_PANEL_STYLE_CLASS);
            println("\">");
            println("<tr>");
            
            if(this.exception != null){
                println("<td>");
                
                String actionFormName = getActionFormName();
                LabelComponent errorIdComponent = new LabelComponent();
                
                errorIdComponent.setPageContext(this.pageContext);
                errorIdComponent.setOutputStream(getOutputStream());
                errorIdComponent.setActionFormName(actionFormName);
                errorIdComponent.setResourcesId(UIConstants.DEFAULT_MESSAGE_BOX_RESOURCES_ID);
                errorIdComponent.setResourcesKey(SystemConstants.ERROR_ID_ATTRIBUTE_ID);
                errorIdComponent.setLabelPositionType(PositionType.TOP);
                errorIdComponent.setValue(this.exception.getClass().getName());
                
                try{
                    errorIdComponent.doStartTag();
                    errorIdComponent.doEndTag();
                }
                catch(JspException e){
                    throw new InternalErrorException(e);
                }
                
                println("</td>");
                println("</tr>");
                
                println("<tr>");
                println("<td>");
                println("<br/>");
                
                StringBuilder content = new StringBuilder();
                
                content.append("showHideErrorTrace('");
                content.append(name);
                content.append("');");
                
                LinkComponent linkComponent = new LinkComponent();
                
                linkComponent.setPageContext(this.pageContext);
                linkComponent.setOutputStream(getOutputStream());
                linkComponent.setActionFormName(actionFormName);
                linkComponent.setResourcesId(UIConstants.DEFAULT_MESSAGE_BOX_RESOURCES_ID);
                linkComponent.setResourcesKey(SystemConstants.ERROR_TRACE_ATTRIBUTE_ID);
                linkComponent.setOnClick(content.toString());
                
                try{
                    linkComponent.doStartTag();
                    linkComponent.doEndTag();
                }
                catch(JspException e){
                    throw new InternalErrorException(e);
                }
                
                println("</td>");
                println("</tr>");
                
                println("<tr>");
                println("<td>");
                
                StringBuilder nameBuffer = new StringBuilder();
                
                nameBuffer.append(name);
                nameBuffer.append(".");
                nameBuffer.append(SystemConstants.ERROR_TRACE_ATTRIBUTE_ID);
                
                TextAreaPropertyComponent errorTraceComponent = new TextAreaPropertyComponent();
                
                errorTraceComponent.setPageContext(this.pageContext);
                errorTraceComponent.setOutputStream(getOutputStream());
                errorTraceComponent.setActionFormName(actionFormName);
                errorTraceComponent.setName(nameBuffer.toString());
                errorTraceComponent.setValue(this.message);
                errorTraceComponent.setShowLabel(false);
                errorTraceComponent.setResourcesId(UIConstants.DEFAULT_MESSAGE_BOX_RESOURCES_ID);
                errorTraceComponent.setResourcesKey(SystemConstants.ERROR_TRACE_ATTRIBUTE_ID);
                errorTraceComponent.setStyleClass(UIConstants.DEFAULT_ERROR_TRACE_STYLE_CLASS);
                
                StringBuilder errorTraceStyle = new StringBuilder();
                
                errorTraceStyle.append("display: ");
                errorTraceStyle.append(VisibilityType.NONE);
                errorTraceStyle.append(";");
                
                errorTraceComponent.setStyle(errorTraceStyle.toString());
                errorTraceComponent.setReadOnly(true);
                
                try{
                    errorTraceComponent.doStartTag();
                    errorTraceComponent.doEndTag();
                }
                catch(JspException e){
                    throw new InternalErrorException(e);
                }
                
                println("</td>");
            }
            else{
                print("<td class=\"");
                print(UIConstants.DEFAULT_DIALOG_BOX_TEXT_STYLE_CLASS);
                println("\">");
                
                if(this.messages != null && !this.messages.isEmpty()){
                    for(String message: this.messages){
                        if(this.messages.size() > 1)
                            print("&#8226;&nbsp;");
                        
                        print(message);
                        println("<br/>");
                    }
                }
                
                println("</td>");
            }
            
            println("<td width=\"10\"></td>");
            
            println("</tr>");
            println("</table>");
            
            println("</td>");
            println("</tr>");
            println("</table>");
        }
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseActionFormComponent#clearAttributes()
     */
    protected void clearAttributes() throws InternalErrorException{
        super.clearAttributes();
        
        setType(null);
        setMessage(null);
        setMessages(null);
        setException(null);
    }
}