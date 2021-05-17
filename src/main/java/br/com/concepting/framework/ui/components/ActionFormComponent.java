package br.com.concepting.framework.ui.components;

import br.com.concepting.framework.controller.SystemController;
import br.com.concepting.framework.controller.form.ActionFormController;
import br.com.concepting.framework.controller.form.BaseActionForm;
import br.com.concepting.framework.controller.form.annotations.ActionForm;
import br.com.concepting.framework.controller.form.constants.ActionFormConstants;
import br.com.concepting.framework.controller.form.types.ActionFormMessageType;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.BaseModel;
import br.com.concepting.framework.model.constants.ModelConstants;
import br.com.concepting.framework.resources.PropertiesResources;
import br.com.concepting.framework.ui.constants.UIConstants;
import br.com.concepting.framework.ui.controller.UIController;
import br.com.concepting.framework.util.types.ContentType;

import javax.servlet.jsp.JspException;
import javax.ws.rs.HttpMethod;

/**
 * Class that defines the actionForm component.
 *
 * @author fvilarinho
 * @since 2.0.0
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
public class ActionFormComponent extends BaseComponent{
    private static final long serialVersionUID = -800612013578741201L;
    
    private String action = null;
    private String target = null;
    private String encoding = null;
    private String onSubmit = null;
    private ActionFormController actionFormController = null;
    
    private String getAction(){
        return this.action;
    }
    
    protected void setAction(String action){
        this.action = action;
    }
    
    /**
     * Returns the identifier of the submit target.
     *
     * @return String that contains the identifier.
     */
    public String getTarget(){
        return this.target;
    }
    
    /**
     * Defines the identifier of the submit target.
     *
     * @param target String that contains the identifier.
     */
    public void setTarget(String target){
        this.target = target;
    }
    
    /**
     * Returns the submit event.
     *
     * @return String that contains the submit event.
     */
    public String getOnSubmit(){
        return this.onSubmit;
    }
    
    /**
     * Defines the submit event.
     *
     * @param onSubmit String that contains the submit event.
     */
    public void setOnSubmit(String onSubmit){
        this.onSubmit = onSubmit;
    }
    
    /**
     * Returns the action form encoding.
     *
     * @return String that contains the encoding.
     */
    public String getEncoding(){
        return this.encoding;
    }
    
    /**
     * Defines the action form encoding.
     *
     * @param encoding String that contains the encoding.
     */
    public void setEncoding(String encoding){
        this.encoding = encoding;
    }
    
    /**
     * Returns the instance of the form controller.
     *
     * @return Instance that contains the form controller.
     */
    protected ActionFormController getActionFormController(){
        if(this.actionFormController == null)
            this.actionFormController = getSystemController().getActionFormController(this.getName());
        
        return this.actionFormController;
    }
    
    /**
     * Builds the events of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    protected void buildEvents() throws InternalErrorException{
        setOnSubmit("return ".concat(Boolean.FALSE.toString()));
        
        super.buildEvents();
    }
    
    /**
     * Builds the resources of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    protected void buildResources() throws InternalErrorException{
        PageComponent pageComponent = (PageComponent) findAncestorWithClass(this, PageComponent.class);
        
        if(pageComponent != null){
            String resourcesId = getResourcesId();
            
            if(resourcesId == null || resourcesId.length() == 0){
                resourcesId = pageComponent.getResourcesId();
                
                setResourcesId(resourcesId);
            }
            
            String encoding = getEncoding();
            
            if(encoding == null || encoding.length() == 0){
                encoding = pageComponent.getEncoding();
                
                setEncoding(encoding);
            }
        }
        
        super.buildResources();
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseComponent#buildAlignment()
     */
    protected void buildAlignment() throws InternalErrorException{
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseComponent#buildDimensions()
     */
    protected void buildDimensions() throws InternalErrorException{
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseComponent#buildStyleClass()
     */
    protected void buildStyleClass() throws InternalErrorException{
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseComponent#buildStyle()
     */
    protected void buildStyle() throws InternalErrorException{
    }
    
    /**
     * Intializes the component.
     *
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    protected void initialize() throws InternalErrorException{
        super.initialize();
        
        String name = getName();
        
        if(name == null || name.length() == 0)
            return;
        
        SystemController systemController = getSystemController();
        
        if(systemController == null)
            return;
        
        this.actionFormController = systemController.getActionFormController(name);
    
        UIController uiController = getUIController();
    
        if(uiController == null)
            return;
        
        try{
            uiController.setActionFormComponentInstance(name, (ActionFormComponent)this.clone());
        }
        catch(CloneNotSupportedException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseComponent#renderStyle()
     */
    protected void renderStyle() throws InternalErrorException{
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseComponent#renderEvents()
     */
    protected void renderEvents() throws InternalErrorException{
        super.renderEvents();
        
        if(this.onSubmit != null && this.onSubmit.length() > 0){
            print(" onSubmit=\"");
            print(this.onSubmit);
            print("\"");
        }
    }
    
    /**
     * Renders the opening of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void renderOpen() throws InternalErrorException{
        SystemController systemController = getSystemController();
        PropertiesResources defaultResources = getDefaultResources();
        
        if(systemController == null || defaultResources == null)
            return;
        
        UIController uiController = getUIController();
        Boolean hasPageComponentInstance = (uiController != null ? uiController.hasPageComponentInstance() : null);
        Boolean hasPageImports = (uiController != null ? uiController.hasPageImports() : null);
        
        if((hasPageComponentInstance == null || !hasPageComponentInstance) && (hasPageImports == null || !hasPageImports)){
            PageComponent.renderImports(systemController, getCurrentLanguage());
            PageComponent.renderLoadingBox(systemController, defaultResources);
            PageComponent.renderPageShade(systemController);
            
            uiController.hasPageImports(true);
        }
        
        super.renderOpen();
        
        print("<form");
        
        renderAttributes();
        
        println(">");
        
        renderDataAttributes();
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseComponent#renderAttributes()
     */
    protected void renderAttributes() throws InternalErrorException{
        super.renderAttributes();
        
        print(" method=\"");
        print(HttpMethod.POST);
        print("\"");
        
        String contextPath = getContextPath();
        BaseActionForm<? extends BaseModel> actionFormInstance = this.actionFormController.getActionFormInstance();
        
        if(actionFormInstance != null){
            ActionForm actionForm = actionFormInstance.getClass().getAnnotation(ActionForm.class);
            String action = actionForm.action();
    
            if(contextPath != null && contextPath.length() > 0 && action != null && action.length() > 0){
                print(" action=\"");
                print(contextPath);
                print(action);
                print(ActionFormConstants.DEFAULT_ACTION_SERVLET_FILE_EXTENSION);
                print("\"");
            }
        }
        
        if(this.encoding != null && this.encoding.length() > 0){
            print(" accept-charset=\"");
            print(this.encoding);
            print("\"");
        }
        
        print(" enctype=\"");
        print(ContentType.ACTION_FORM_DATA.getMimeType());
        print("\"");
    }
    
    /**
     * Renders the closing of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void renderClose() throws InternalErrorException{
        SystemController systemController = getSystemController();
        
        if(systemController == null)
            return;
        
        println("</form>");
        
        UIController uiController = getUIController();
        Boolean hasPageComponentInstance = (uiController != null ? uiController.hasPageComponentInstance() : null);
        
        if(hasPageComponentInstance == null || !hasPageComponentInstance){
            PageComponent.renderMessageBoxes(systemController);
            PageComponent.renderEvents(systemController);
        }
        
        renderMessageBoxes();
        
        super.renderClose();
    }
    
    /**
     * Renders the message boxes.
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void renderMessageBoxes() throws InternalErrorException{
        String actionFormName = getName();
        
        if(actionFormName == null || actionFormName.length() == 0)
            return;
        
        String resourcesId = getResourcesId();
        MessageBoxComponent infoMessageBoxComponent = new MessageBoxComponent();
        
        infoMessageBoxComponent.setPageContext(this.pageContext);
        infoMessageBoxComponent.setOutputStream(getOutputStream());
        infoMessageBoxComponent.setActionFormName(actionFormName);
        infoMessageBoxComponent.setResourcesId(resourcesId);
        infoMessageBoxComponent.setMessageType(ActionFormMessageType.INFO);
        infoMessageBoxComponent.setShowOnLoad(true);
        
        try{
            infoMessageBoxComponent.doStartTag();
            infoMessageBoxComponent.doEndTag();
        }
        catch(JspException e){
            throw new InternalErrorException(e);
        }
        
        MessageBoxComponent warningMessageBoxComponent = new MessageBoxComponent();
        
        warningMessageBoxComponent.setPageContext(this.pageContext);
        warningMessageBoxComponent.setOutputStream(getOutputStream());
        warningMessageBoxComponent.setActionFormName(actionFormName);
        warningMessageBoxComponent.setResourcesId(resourcesId);
        warningMessageBoxComponent.setMessageType(ActionFormMessageType.WARNING);
        warningMessageBoxComponent.setShowOnLoad(true);
        
        try{
            warningMessageBoxComponent.doStartTag();
            warningMessageBoxComponent.doEndTag();
        }
        catch(JspException e){
            throw new InternalErrorException(e);
        }
        
        MessageBoxComponent errorMessageBoxComponent = new MessageBoxComponent();
        
        errorMessageBoxComponent.setPageContext(this.pageContext);
        errorMessageBoxComponent.setOutputStream(getOutputStream());
        errorMessageBoxComponent.setActionFormName(actionFormName);
        errorMessageBoxComponent.setResourcesId(resourcesId);
        errorMessageBoxComponent.setMessageType(ActionFormMessageType.ERROR);
        errorMessageBoxComponent.setShowOnLoad(true);
        
        try{
            errorMessageBoxComponent.doStartTag();
            errorMessageBoxComponent.doEndTag();
        }
        catch(JspException e){
            throw new InternalErrorException(e);
        }
        
        MessageBoxComponent validationMessageBoxComponent = new MessageBoxComponent();
        
        validationMessageBoxComponent.setPageContext(this.pageContext);
        validationMessageBoxComponent.setOutputStream(getOutputStream());
        validationMessageBoxComponent.setActionFormName(actionFormName);
        validationMessageBoxComponent.setResourcesId(resourcesId);
        validationMessageBoxComponent.setMessageType(ActionFormMessageType.VALIDATION);
        validationMessageBoxComponent.setShowOnLoad(true);
        
        try{
            validationMessageBoxComponent.doStartTag();
            validationMessageBoxComponent.doEndTag();
        }
        catch(JspException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Renders the attributes of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void renderDataAttributes() throws InternalErrorException{
        String actionFormName = getName();
        
        if(actionFormName == null || actionFormName.length() == 0)
            return;
        
        HiddenPropertyComponent actionPropertyComponent = new HiddenPropertyComponent();
        
        actionPropertyComponent.setPageContext(this.pageContext);
        actionPropertyComponent.setOutputStream(getOutputStream());
        actionPropertyComponent.setActionFormName(actionFormName);
        actionPropertyComponent.setName(ActionFormConstants.ACTION_ATTRIBUTE_ID);
        
        try{
            actionPropertyComponent.doStartTag();
            actionPropertyComponent.doEndTag();
        }
        catch(JspException e){
            throw new InternalErrorException(e);
        }
        
        HiddenPropertyComponent forwardPropertyComponent = new HiddenPropertyComponent();
        
        forwardPropertyComponent.setPageContext(this.pageContext);
        forwardPropertyComponent.setOutputStream(getOutputStream());
        forwardPropertyComponent.setActionFormName(actionFormName);
        forwardPropertyComponent.setName(ActionFormConstants.FORWARD_ATTRIBUTE_ID);
        forwardPropertyComponent.setValue(getSystemController().getRequestParameterValue(ActionFormConstants.FORWARD_ATTRIBUTE_ID));
        
        try{
            forwardPropertyComponent.doStartTag();
            forwardPropertyComponent.doEndTag();
        }
        catch(JspException e){
            throw new InternalErrorException(e);
        }
        
        HiddenPropertyComponent updateViewsPropertyComponent = new HiddenPropertyComponent();
        
        updateViewsPropertyComponent.setPageContext(this.pageContext);
        updateViewsPropertyComponent.setOutputStream(getOutputStream());
        updateViewsPropertyComponent.setActionFormName(actionFormName);
        updateViewsPropertyComponent.setName(UIConstants.UPDATE_VIEWS_ATTRIBUTE_ID);
        
        try{
            updateViewsPropertyComponent.doStartTag();
            updateViewsPropertyComponent.doEndTag();
        }
        catch(JspException e){
            throw new InternalErrorException(e);
        }
        
        HiddenPropertyComponent validateModelPropertyComponent = new HiddenPropertyComponent();
        
        validateModelPropertyComponent.setPageContext(this.pageContext);
        validateModelPropertyComponent.setOutputStream(getOutputStream());
        validateModelPropertyComponent.setActionFormName(actionFormName);
        validateModelPropertyComponent.setName(ModelConstants.VALIDATE_MODEL_ATTRIBUTE_ID);
        validateModelPropertyComponent.setValue(false);
        
        try{
            validateModelPropertyComponent.doStartTag();
            validateModelPropertyComponent.doEndTag();
        }
        catch(JspException e){
            throw new InternalErrorException(e);
        }
        
        HiddenPropertyComponent validateModelPropertiesComponent = new HiddenPropertyComponent();
        
        validateModelPropertiesComponent.setPageContext(this.pageContext);
        validateModelPropertiesComponent.setOutputStream(getOutputStream());
        validateModelPropertiesComponent.setActionFormName(actionFormName);
        validateModelPropertiesComponent.setName(ModelConstants.VALIDATE_MODEL_PROPERTIES_ATTRIBUTE_ID);
        
        try{
            validateModelPropertiesComponent.doStartTag();
            validateModelPropertiesComponent.doEndTag();
        }
        catch(JspException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Renders the page shade.
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void renderPageShade() throws InternalErrorException{
        PageComponent pageComponent = (PageComponent) findAncestorWithClass(this, PageComponent.class);
        
        if(pageComponent == null)
            PageComponent.renderPageShade(getSystemController());
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseComponent#clearAttributes()
     */
    protected void clearAttributes() throws InternalErrorException{
        super.clearAttributes();
        
        setAction(null);
        setTarget(null);
        setEncoding(null);
        setOnSubmit(null);
        
        this.actionFormController = null;
    }
}