package br.com.concepting.framework.ui.components;

import br.com.concepting.framework.annotations.System;
import br.com.concepting.framework.constants.Constants;
import br.com.concepting.framework.constants.SystemConstants;
import br.com.concepting.framework.controller.SystemController;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.resources.PropertiesResources;
import br.com.concepting.framework.resources.PropertiesResourcesLoader;
import br.com.concepting.framework.security.controller.SecurityController;
import br.com.concepting.framework.security.model.LoginParameterModel;
import br.com.concepting.framework.security.model.LoginSessionModel;
import br.com.concepting.framework.security.model.UserModel;
import br.com.concepting.framework.ui.components.types.EventType;
import br.com.concepting.framework.ui.constants.UIConstants;
import br.com.concepting.framework.ui.controller.UIController;
import br.com.concepting.framework.ui.util.SkinUtil;
import br.com.concepting.framework.util.ExceptionUtil;
import br.com.concepting.framework.util.LanguageUtil;
import br.com.concepting.framework.util.ReflectionUtil;
import br.com.concepting.framework.util.types.ComponentType;

import javax.servlet.http.Cookie;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Set;

/**
 * Class that defines the basic implementation of a component.
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
public abstract class BaseComponent extends BodyTagSupport implements Cloneable{
    private static final long serialVersionUID = -7267112866725230814L;
    
    private ComponentType componentType = null;
    private String name = null;
    private String resourcesId = null;
    private String resourcesKey = null;
    private String styleClass = null;
    private String style = null;
    private Boolean focus = null;
    private String onBlur = null;
    private String onFocus = null;
    private String onClick = null;
    private String onMouseOver = null;
    private String onMouseOut = null;
    private String width = null;
    private String height = null;
    private Boolean enabled = null;
    private Boolean hasBody = null;
    private Boolean render = null;
    private Boolean renderWhenAuthenticated = null;
    private SystemController systemController = null;
    private SecurityController securityController = null;
    private UIController uiController = null;
    private PrintWriter outputStream = null;
    
    /**
     * Indicates when the component will get focus.
     *
     * @return True/False.
     */
    protected Boolean focus(){
        return this.focus;
    }
    
    /**
     * Indicates when the component will get focus.
     *
     * @return True/False.
     */
    protected Boolean getFocus(){
        return focus();
    }
    
    /**
     * Defines when the component will get focus.
     *
     * @param focus True/False.
     */
    public void setFocus(Boolean focus){
        this.focus = focus;
    }
    
    /**
     * Returns if the component has body content.
     *
     * @return True/False.
     */
    protected Boolean hasBody(){
        return this.hasBody;
    }
    
    /**
     * Returns if the component has body content.
     *
     * @return True/False.
     */
    protected Boolean getHasBody(){
        return hasBody();
    }
    
    /**
     * Defines if the component has body content.
     *
     * @param hasBody True/False.
     */
    private void setHasBody(Boolean hasBody){
        this.hasBody = hasBody;
    }
    
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
     * Returns the instance of the UI controller.
     *
     * @return Instance that contains the security controller.
     */
    protected UIController getUIController(){
        return this.uiController;
    }
    
    /**
     * Returns the instance that contains the resources.
     *
     * @return Instance that contains the resources.
     * @throws InternalErrorException Occurs when was not possible to execute the operation..
     */
    protected PropertiesResources getResources() throws InternalErrorException{
        return getResources(this.resourcesId);
    }
    
    /**
     * Returns the instance that contains the resources.
     *
     * @param resourcesId String that contains the identifier of the resources.
     * @return Instance that contains the resources.
     * @throws InternalErrorException Occurs when was not possible to execute the operation..
     */
    protected PropertiesResources getResources(String resourcesId) throws InternalErrorException{
        if(resourcesId == null || resourcesId.length() == 0)
            resourcesId = UIConstants.DEFAULT_COMMON_RESOURCES_ID;
        
        PropertiesResourcesLoader loader = new PropertiesResourcesLoader(resourcesId, getCurrentLanguage());
        PropertiesResources resources = loader.getContent();
        
        return resources;
    }
    
    /**
     * Returns the instance that contains the default resources.
     *
     * @return Instance that contains the resources.
     * @throws InternalErrorException Occurs when was not possible to execute the operation..
     */
    protected PropertiesResources getDefaultResources() throws InternalErrorException{
        return getResources(null);
    }
    
    /**
     * Returns the identifier of the resources.
     *
     * @return String that contains the identifier of the resources.
     */
    public String getResourcesId(){
        return this.resourcesId;
    }
    
    /**
     * Returns the key of the resources.
     *
     * @return String that contains the identifier of the key.
     */
    public String getResourcesKey(){
        return this.resourcesKey;
    }
    
    /**
     * Defines the identifier of the resources.
     *
     * @param resourcesId String that contains the identifier of the resources.
     */
    public void setResourcesId(String resourcesId){
        this.resourcesId = resourcesId;
    }
    
    /**
     * Defines the key of the resources.
     *
     * @param resourcesKey String that contains the identifier of the key.
     */
    public void setResourcesKey(String resourcesKey){
        this.resourcesKey = resourcesKey;
    }
    
    /**
     * Indicates if the component will be render only if the user is
     * authenticated.
     *
     * @return True/False.
     */
    public Boolean renderWhenAuthenticated(){
        return this.renderWhenAuthenticated;
    }
    
    /**
     * Indicates if the component will be render only if the user is
     * authenticated.
     *
     * @return True/False.
     */
    public Boolean getRenderWhenAuthenticated(){
        return renderWhenAuthenticated();
    }
    
    /**
     * Defines if the component will be render only if the user is
     * authenticated.
     *
     * @param renderWhenAuthenticated True/False.
     */
    public void setRenderWhenAuthenticated(Boolean renderWhenAuthenticated){
        this.renderWhenAuthenticated = renderWhenAuthenticated;
    }
    
    /**
     * Indicates if the component will be render.
     *
     * @return True/False.
     */
    public Boolean render(){
        return this.render;
    }
    
    /**
     * Indicates if the component will be render.
     *
     * @return True/False.
     */
    public Boolean getRender(){
        return render();
    }
    
    /**
     * Defines if the component will be render.
     *
     * @param rendered True/False.
     */
    public void setRender(Boolean rendered){
        this.render = rendered;
    }
    
    /**
     * Returns the type of the component.
     *
     * @return Instance that contains the type of the component.
     */
    protected ComponentType getComponentType(){
        return this.componentType;
    }
    
    /**
     * Defines the type of the component.
     *
     * @param componentType Instance that contains the type of the component.
     */
    protected void setComponentType(ComponentType componentType){
        this.componentType = componentType;
    }
    
    /**
     * Returns the width of the component.
     *
     * @return String that contains the width of the component.
     */
    public String getWidth(){
        return this.width;
    }
    
    /**
     * Defines the width of the component.
     *
     * @param width String that contains the width of the component.
     */
    public void setWidth(String width){
        this.width = width;
    }
    
    /**
     * Returns the height of the component.
     *
     * @return String that contains the height of the component.
     */
    public String getHeight(){
        return this.height;
    }
    
    /**
     * Defines the height of the component.
     *
     * @param height String that contains the height of the component.
     */
    public void setHeight(String height){
        this.height = height;
    }
    
    /**
     * Returns the identifier of the component.
     *
     * @return String that contains the identifier.
     */
    public String getName(){
        return this.name;
    }
    
    /**
     * Defines the identifier of the component.
     *
     * @param name String that contains the identifier.
     */
    public void setName(String name){
        this.name = name;
    }
    
    /**
     * Returns the CSS style of the component.
     *
     * @return String that contains the CSS style.
     */
    public String getStyleClass(){
        return this.styleClass;
    }
    
    /**
     * Defines the CSS style of the component.
     *
     * @param styleClass String that contains the CSS style.
     */
    public void setStyleClass(String styleClass){
        this.styleClass = styleClass;
    }
    
    /**
     * Returns the CSS style of the component.
     *
     * @return String that contains the CSS style.
     */
    public String getStyle(){
        return this.style;
    }
    
    /**
     * Defines the CSS style of the component.
     *
     * @param style String that contains the CSS style.
     */
    public void setStyle(String style){
        this.style = style;
    }
    
    /**
     * Returns the event of the blur.
     *
     * @return String that contains the event.
     */
    public String getOnBlur(){
        return this.onBlur;
    }
    
    /**
     * Defines the event of the blur.
     *
     * @param onBlur String that contains the event.
     */
    public void setOnBlur(String onBlur){
        this.onBlur = onBlur;
    }
    
    /**
     * Returns the event of the click.
     *
     * @return String that contains the event.
     */
    public String getOnClick(){
        return this.onClick;
    }
    
    /**
     * Defines the event of the click.
     *
     * @param onClick String that contains the event.
     */
    public void setOnClick(String onClick){
        this.onClick = onClick;
    }
    
    /**
     * Returns the event of the focus.
     *
     * @return String that contains the event.
     */
    public String getOnFocus(){
        return this.onFocus;
    }
    
    /**
     * Defines the event of the focus.
     *
     * @param onFocus String that contains the event.
     */
    public void setOnFocus(String onFocus){
        this.onFocus = onFocus;
    }
    
    /**
     * Returns the event of the mouse out.
     *
     * @return String that contains the event.
     */
    public String getOnMouseOut(){
        return this.onMouseOut;
    }
    
    /**
     * Defines the event of the mouse out.
     *
     * @param onMouseOut String that contains the event.
     */
    public void setOnMouseOut(String onMouseOut){
        this.onMouseOut = onMouseOut;
    }
    
    /**
     * Returns the event of the mouse over.
     *
     * @return String that contains the event.
     */
    public String getOnMouseOver(){
        return this.onMouseOver;
    }
    
    /**
     * Defines the event of the mouse over.
     *
     * @param onMouseOver String that contains the event.
     */
    public void setOnMouseOver(String onMouseOver){
        this.onMouseOver = onMouseOver;
    }
    
    /**
     * Indicates if the component is enabled.
     *
     * @return True/False.
     */
    public Boolean isEnabled(){
        return this.enabled;
    }
    
    /**
     * Indicates if the component is enabled.
     *
     * @return True/False.
     */
    public Boolean getEnabled(){
        return isEnabled();
    }
    
    /**
     * Defines if the component is enabled.
     *
     * @param enabled True/False.
     */
    public void setEnabled(Boolean enabled){
        this.enabled = enabled;
    }
    
    /**
     * Returns the context path.
     *
     * @return String that contains the context path.
     */
    protected String getContextPath(){
        if(this.systemController != null)
            return this.systemController.getContextPath();
        
        return null;
    }
    
    /**
     * Returns the request path.
     *
     * @return String that contains the request path.
     */
    protected String getRequestPath(){
        if(this.systemController != null)
            return this.systemController.getRequestPath();
        
        return null;
    }
    
    /**
     * Returns the instance that contains the current language.
     *
     * @return Instance that contains the current language.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    protected Locale getCurrentLanguage() throws InternalErrorException{
        if(this.systemController != null){
            Cookie languageCookie = this.systemController.getCookie(SystemConstants.CURRENT_LANGUAGE_ATTRIBUTE_ID);
    
            if(languageCookie != null)
                return LanguageUtil.getLanguageByString(languageCookie.getValue());
            
            if(this.securityController != null){
                LoginSessionModel loginSession = this.securityController.getLoginSession();
                UserModel user = (loginSession != null ? loginSession.getUser() : null);
                LoginParameterModel loginParameter = (user != null ? user.getLoginParameter() : null);
    
                if(loginParameter != null && loginParameter.getLanguage() != null && loginParameter.getLanguage().length() > 0)
                    return LanguageUtil.getLanguageByString(loginParameter.getLanguage());
            }
        }
        
        return LanguageUtil.getDefaultLanguage();
    }
    
    /**
     * Returns the instance that contains the current skin.
     *
     * @return Instance that contains the current skin.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    protected String getCurrentSkin() throws InternalErrorException{
        if(this.systemController != null){
            Cookie skinCookie = this.systemController.getCookie(SystemConstants.CURRENT_SKIN_ATTRIBUTE_ID);
    
            if(skinCookie != null)
                return skinCookie.getValue();

            if(this.securityController != null){
                LoginSessionModel loginSession = this.securityController.getLoginSession();
                UserModel user = (loginSession != null ? loginSession.getUser() : null);
                LoginParameterModel loginParameter = (user != null ? user.getLoginParameter() : null);
    
                if(loginParameter != null && loginParameter.getSkin() != null && loginParameter.getSkin().length() > 0)
                    return loginParameter.getSkin();
            }
        }
    
        return SkinUtil.getDefaultSkin();
    }
    
    /**
     * Prints a content.
     *
     * @param <O> Class that defines the content.
     * @param value Instance that contains the content.
     */
    protected <O> void print(O value){
        this.outputStream.print(value);
    }
    
    /**
     * Prints a content with line break.
     *
     * @param <O> Class that defines the content.
     * @param value Instance that contains the content.
     */
    protected <O> void println(O value){
        print(value);
        println();
    }
    
    /**
     * Prints a line break.
     */
    protected void println(){
        this.outputStream.println();
    }
    
    /**
     * Returns the instance of the page context.
     *
     * @return Instance that contains the page context.
     */
    protected PageContext getPageContext(){
        return this.pageContext;
    }
    
    /**
     * @see javax.servlet.jsp.tagext.TagSupport#setPageContext(javax.servlet.jsp.PageContext)
     */
    public void setPageContext(PageContext pageContext){
        super.setPageContext(pageContext);
        
        if(pageContext != null){
            this.systemController = new SystemController(pageContext);
            this.securityController = this.systemController.getSecurityController();
            this.uiController = this.systemController.getUIController();
            this.outputStream = this.systemController.getOutputStream();
        }
    }
    
    /**
     * Defines the stream of the component used in the rendering.
     *
     * @param outputStream Instance that contains the rendering.
     */
    protected void setOutputStream(PrintWriter outputStream){
        this.outputStream = outputStream;
    }
    
    /**
     * Returns the stream of the component used in the rendering.
     *
     * @return Instance that contains the rendering.
     */
    protected PrintWriter getOutputStream(){
        return this.outputStream;
    }
    
    /**
     * Builds the resources of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    protected void buildResources() throws InternalErrorException{
    }
    
    /**
     * Builds the CSS style of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    protected void buildStyleClass() throws InternalErrorException{
    }
    
    /**
     * Builds the CSS style of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    protected void buildStyle() throws InternalErrorException{
    }
    
    /**
     * Builds the component name.
     *
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    protected void buildName() throws InternalErrorException{
        String name = getName();
        ComponentType componentType = getComponentType();
        
        if((name == null || name.length() == 0) && componentType != null){
            name = componentType.getId();
            
            setName(name);
        }
        
        String id = getId();
        
        if(id == null){
            id = name;
            
            setId(id);
        }
    }
    
    /**
     * Builds the component dimensions.
     *
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    protected void buildDimensions() throws InternalErrorException{
        StringBuilder styleContent = null;
        
        if((this.width != null && this.width.length() > 0) || (this.height != null && this.height.length() > 0)){
            styleContent = new StringBuilder();
            
            if(this.width != null && this.width.length() > 0){
                if(this.style != null && this.style.length() > 0){
                    styleContent.append(this.style);
                    
                    if(!this.style.endsWith(";"))
                        styleContent.append(";");
                    
                    styleContent.append(" ");
                }
                
                styleContent.append("width: ");
                styleContent.append(this.width);
                
                if(!this.width.contains("px") && !this.width.contains("%"))
                    styleContent.append("px");
                
                if(!this.width.endsWith(";"))
                    styleContent.append(";");
            }
            
            if(this.height != null && this.height.length() > 0){
                if(styleContent.length() == 0){
                    styleContent = new StringBuilder();
                    
                    if(this.style != null && this.style.length() > 0){
                        styleContent.append(this.style);
                        
                        if(!this.style.endsWith(";"))
                            styleContent.append(";");
                        
                        styleContent.append(" ");
                    }
                }
                else
                    styleContent.append(" ");
                
                styleContent.append("height: ");
                styleContent.append(this.height);
                
                if(!this.height.contains("px") && !this.height.contains("%"))
                    styleContent.append("px");
                
                if(!this.height.endsWith(";"))
                    styleContent.append(";");
            }
            
            setStyle(styleContent.toString());
        }
    }
    
    /**
     * Builds the alignment of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    protected void buildAlignment() throws InternalErrorException{
    }
    
    /**
     * Builds the events of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    protected void buildEvents() throws InternalErrorException{
    }
    
    /**
     * Builds the restrictions of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    protected void buildRestrictions() throws InternalErrorException{
        if(this.enabled == null)
            this.enabled = true;
    }
    
    /**
     * Builds the permissions of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    protected void buildPermissions() throws InternalErrorException{
        BaseComponent parentComponent = (BaseComponent) findAncestorWithClass(this, BaseComponent.class);
        
        if(parentComponent == null){
            try{
                parentComponent = (BaseComponent) getParent();
            }
            catch(ClassCastException e){
            }
        }
        
        if(parentComponent != null){
            if(this.renderWhenAuthenticated != null && this.renderWhenAuthenticated)
                this.renderWhenAuthenticated = parentComponent.renderWhenAuthenticated();
            
            if(this.render != null && this.render)
                this.render = parentComponent.render();
        }
        
        if(this.renderWhenAuthenticated != null && this.renderWhenAuthenticated)
            this.render = this.securityController.isLoginSessionAuthenticated();
        
        if(this.render == null)
            this.render = true;
    }
    
    /**
     * Initializes the component.
     *
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    protected void initialize() throws InternalErrorException{
        buildRestrictions();
        buildName();
        buildDimensions();
        buildResources();
        buildPermissions();
        buildEvents();
        buildAlignment();
        buildStyleClass();
        buildStyle();
    }
    
    /**
     * Renders the identifier of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void renderId() throws InternalErrorException{
        String id = getId();
        
        if(id != null && id.length() > 0){
            print(" id=\"");
            print(id);
            print("\"");
        }
    }
    
    /**
     * Renders the name of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void renderName() throws InternalErrorException{
        String name = getName();
        
        if(name != null && name.length() > 0){
            print(" name=\"");
            print(name);
            print("\"");
        }
    }
    
    /**
     * Renders the events of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void renderEvents() throws InternalErrorException{
        if(this.onBlur != null && this.onBlur.length() > 0){
            print(" ");
            print(EventType.ON_BLUR.getId());
            print("=\"");
            print(this.onBlur);
            print("\"");
        }
        
        if(this.onFocus != null && this.onFocus.length() > 0){
            print(" ");
            print(EventType.ON_FOCUS.getId());
            print("=\"");
            print(this.onFocus);
            print("\"");
        }
        
        if(this.onClick != null && this.onClick.length() > 0){
            print(" ");
            print(EventType.ON_CLICK.getId());
            print("=\"");
            print(this.onClick);
            print("\"");
        }
        
        if(this.onMouseOver != null && this.onMouseOver.length() > 0){
            print(" ");
            print(EventType.ON_MOUSE_OVER.getId());
            print("=\"");
            print(this.onMouseOver);
            print("\"");
        }
        
        if(this.onMouseOut != null && this.onMouseOut.length() > 0){
            print(" ");
            print(EventType.ON_MOUSE_OUT.getId());
            print("=\"");
            print(this.onMouseOut);
            print("\"");
        }
    }
    
    /**
     * Renders the attributes of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void renderAttributes() throws InternalErrorException{
        renderId();
        renderName();
        renderStyle();
        renderEvents();
    }
    
    /**
     * Renders the CSS styles of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void renderStyle() throws InternalErrorException{
        if(this.styleClass != null && this.styleClass.length() > 0){
            print(" class=\"");
            print(this.styleClass);
            print("\"");
        }
        
        if(this.style != null && this.style.length() > 0){
            print(" style=\"");
            print(this.style);
            print("\"");
        }
    }
    
    /**
     * Renders the opening of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void renderOpen() throws InternalErrorException{
    }
    
    /**
     * Renders the body of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void renderBody() throws InternalErrorException{
        BodyContent bodyContent = getBodyContent();
        
        if(bodyContent != null){
            String content = bodyContent.getString();
            
            if(content != null)
                println(content);
        }
    }
    
    /**
     * Renders the closing of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void renderClose() throws InternalErrorException{
        String name = getName();
        Boolean focus = focus();
        
        if(focus != null && focus && name != null && name.length() > 0){
            StringBuilder content = new StringBuilder();
            
            content.append("focusObject('");
            content.append(name);
            content.append("');");
            
            ScriptComponent focusScriptComponent = new ScriptComponent();
            
            focusScriptComponent.setPageContext(this.pageContext);
            focusScriptComponent.setOutputStream(getOutputStream());
            focusScriptComponent.setContent(content.toString());
            
            try{
                focusScriptComponent.doStartTag();
                focusScriptComponent.doEndTag();
            }
            catch(JspException e){
                throw new InternalErrorException(e);
            }
        }
    }
    
    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
     */
    public int doStartTag() throws JspException{
        if(this.systemController != null){
            try{
                initialize();
                
                if(this.render != null && this.render)
                    renderOpen();
            }
            catch(Throwable e){
                if(!ExceptionUtil.isExpectedException(e) && !ExceptionUtil.isInternalErrorException(e))
                    e = new InternalErrorException(e);
                
                this.systemController.setCurrentException(e);
            }
        }
        
        return super.doStartTag();
    }
    
    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#doAfterBody()
     */
    public int doAfterBody() throws JspException{
        if(this.systemController != null){
            try{
                if(this.render != null && this.render){
                    renderBody();
                    
                    this.hasBody = true;
                }
            }
            catch(Throwable e){
                if(!ExceptionUtil.isExpectedException(e) && !ExceptionUtil.isInternalErrorException(e))
                    e = new InternalErrorException(e);
                
                this.systemController.setCurrentException(e);
            }
        }
        
        return super.doAfterBody();
    }
    
    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
     */
    public int doEndTag() throws JspException{
        if(this.systemController != null){
            try{
                if(this.render != null && this.render){
                    if(this.hasBody == null || !this.hasBody)
                        renderBody();
                    
                    renderClose();
                }
            }
            catch(Throwable e){
                if(!ExceptionUtil.isExpectedException(e) && !ExceptionUtil.isInternalErrorException(e))
                    e = new InternalErrorException(e);
                
                this.systemController.setCurrentException(e);
            }
            finally{
                try{
                    clearAttributes();
                }
                catch(InternalErrorException e){
                }
            }
        }
        
        return super.doEndTag();
    }
    
    /**
     * Clears the properties of the component.
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void clearAttributes() throws InternalErrorException{
        setComponentType(null);
        setId(null);
        setName(null);
        setResourcesId(null);
        setResourcesKey(null);
        setWidth(null);
        setHeight(null);
        setFocus(null);
        setStyleClass(null);
        setStyle(null);
        setOnBlur(null);
        setOnClick(null);
        setOnFocus(null);
        setOnMouseOut(null);
        setOnMouseOver(null);
        setEnabled(null);
        setRender(null);
        setRenderWhenAuthenticated(null);
        setHasBody(null);
    }
}