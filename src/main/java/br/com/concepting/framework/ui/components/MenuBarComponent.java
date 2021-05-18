package br.com.concepting.framework.ui.components;

import br.com.concepting.framework.constants.Constants;
import br.com.concepting.framework.controller.form.util.ActionFormUtil;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.FormModel;
import br.com.concepting.framework.model.MainConsoleModel;
import br.com.concepting.framework.model.ObjectModel;
import br.com.concepting.framework.model.SystemModuleModel;
import br.com.concepting.framework.model.util.ModelUtil;
import br.com.concepting.framework.resources.PropertiesResources;
import br.com.concepting.framework.security.controller.SecurityController;
import br.com.concepting.framework.security.model.LoginSessionModel;
import br.com.concepting.framework.security.model.UserModel;
import br.com.concepting.framework.ui.constants.UIConstants;
import br.com.concepting.framework.util.types.AlignmentType;
import br.com.concepting.framework.util.types.ComponentType;

import javax.servlet.jsp.JspException;
import javax.ws.rs.HttpMethod;
import java.util.List;

/**
 * Class that defines the menu bar component.
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
public class MenuBarComponent extends BaseActionFormComponent{
    private static final long serialVersionUID = -1595323252569482926L;
    
    private Boolean isFixed = null;
    
    /**
     * Indicates if the menu bar is fixed.
     *
     * @return True/False.
     */
    public Boolean isFixed(){
        return this.isFixed;
    }
    
    /**
     * Indicates if the menu bar is fixed.
     *
     * @return True/False.
     */
    public Boolean getIsFixed(){
        return isFixed();
    }
    
    /**
     * Defines if the menu bar is fixed.
     *
     * @param isFixed True/False.
     */
    public void setIsFixed(Boolean isFixed){
        this.isFixed = isFixed;
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
     * Loads the menu items.
     *
     * @param <C> Class that define the list of menu items.
     * @return Instance that contains the list of menu items.
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    @SuppressWarnings("unchecked")
    protected <C extends List<? extends ObjectModel>> C lookupMenuItems() throws InternalErrorException{
        SecurityController securityController = getSecurityController();
        String actionFormName = getActionFormName();
        C objects = null;
        
        if(actionFormName != null && actionFormName.length() > 0 && securityController != null){
            LoginSessionModel loginSession = securityController.getLoginSession();
            SystemModuleModel systemModule = (loginSession != null ? loginSession.getSystemModule() : null);
            FormModel form = (systemModule != null ? systemModule.getForm(actionFormName) : null);
            
            if(form != null)
                objects = (C) form.getObjects();
        }
        
        return objects;
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
     * @see br.com.concepting.framework.ui.components.BasePropertyComponent#buildEvents()
     */
    protected void buildEvents() throws InternalErrorException{
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseActionFormComponent#buildAlignment()
     */
    protected void buildAlignment() throws InternalErrorException{
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseComponent#initialize()
     */
    protected void initialize() throws InternalErrorException{
        setComponentType(ComponentType.MENU_BAR);
        
        super.initialize();
    }
    
    /**
     * Renders the menu items.
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void renderMenuItems() throws InternalErrorException{
        List<? extends ObjectModel> menuItems = lookupMenuItems();
        
        if(menuItems != null && menuItems.size() > 0)
            renderMenuItems(menuItems, null);
    }
    
    /**
     * Renders the menu items.
     *
     * @param menuItems List that contains the menu items.
     * @param parentMenu Instance that contains the parent menu item.
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    private void renderMenuItems(List<? extends ObjectModel> menuItems, ObjectModel parentMenu) throws InternalErrorException{
        String contextPath = getContextPath();
        SecurityController securityController = getSecurityController();
        
        if(menuItems == null || menuItems.size() == 0 || contextPath == null || contextPath.length() == 0 || securityController == null)
            return;
        
        ModelUtil.sort(menuItems, Constants.SEQUENCE_ATTRIBUTE_ID);
        
        print("<table class=\"");
        
        if(parentMenu != null)
            print(UIConstants.DEFAULT_MENU_BOX_CONTENT_STYLE_CLASS);
        else
            print(UIConstants.DEFAULT_MENU_BAR_CONTENT_STYLE_CLASS);
        
        println("\">");
        println("<tr>");
        
        LoginSessionModel loginSession = securityController.getLoginSession();
        Boolean authenticated = securityController.isLoginSessionAuthenticated();
        UserModel user = (loginSession != null ? loginSession.getUser() : null);
        Boolean superUser = (user != null && user.isSuperUser() != null ? user.isSuperUser() : null);
        PropertiesResources resources = getResources();
        PropertiesResources mainConsoleResources = getMainConsoleResources();
        PropertiesResources defaultResources = getDefaultResources();
        Boolean hasPermission = null;
        ObjectModel parentMenuItem = null;
        String menuItemName = null;
        String menuItemLabel = null;
        String menuItemTooltip = null;
        String menuItemAction = null;
        String menuItemActionTarget = null;
        Boolean hasSubmenuItems = null;
        ComponentType menuItemType = null;
        StringBuilder propertyId = null;
        
        for(ObjectModel menuItem: menuItems){
            menuItemName = menuItem.getName();
            
            if(menuItemName == null || menuItemName.length() == 0)
                continue;
            
            parentMenuItem = menuItem.getParent();
            
            if((parentMenu == null && parentMenuItem == null) || (parentMenu != null && parentMenu.equals(parentMenuItem))){
                if(authenticated != null && authenticated)
                    hasPermission = (superUser != null && superUser || user.hasPermission(menuItem));
                else
                    hasPermission = false;
                
                if(hasPermission){
                    hasSubmenuItems = menuItem.hasChildren();
                    menuItemType = menuItem.getType();
                    
                    if(menuItemType != ComponentType.MENU_ITEM && menuItemType != ComponentType.MENU_ITEM_SEPARATOR)
                        continue;
                    
                    if(menuItemType == ComponentType.MENU_ITEM_SEPARATOR){
                        if(parentMenu != null){
                            print("<td align=\"");
                            print(AlignmentType.CENTER);
                            println("\" colspan=\"3\">");
                            print("<div class=\"");
                            print(UIConstants.DEFAULT_MENU_BOX_SEPARATOR_STYLE_CLASS);
                            println("\"></div>");
                            println("</td>");
                        }
                        else{
                            print("<td align=\"");
                            print(AlignmentType.CENTER);
                            println("\" height=\"20\" width=\"2\">");
                            print("<div class=\"");
                            print(UIConstants.DEFAULT_MENU_BAR_SEPARATOR_STYLE_CLASS);
                            println("\"></div>");
                            println("</td>");
                        }
                    }
                    else{
                        print("<td id=\"");
                        print(menuItemName);
                        print(".");
                        print(UIConstants.DEFAULT_MENU_ITEM_ID);
                        print("\" class=\"");
                        print(UIConstants.DEFAULT_MENU_ITEM_STYLE_CLASS);
                        print("\"");
                        
                        menuItemAction = menuItem.getAction();
                        
                        if(menuItemAction != null && menuItemAction.length() > 0){
                            print(" onClick=\"");
                            
                            if(menuItemAction.toLowerCase().startsWith("javascript:"))
                                print(menuItemAction);
                            else{
                                print("submitRequest('");
                                print(HttpMethod.GET);
                                print("', '");
                                print(contextPath);
                                print(menuItemAction);
                                print("'");
                                
                                menuItemActionTarget = menuItem.getActionTarget();
                                
                                if(menuItemActionTarget != null && menuItemActionTarget.length() > 0){
                                    print(", null, null, null, '");
                                    print(menuItemActionTarget);
                                    print("'");
                                }
                                
                                print(");");
                            }
                            
                            print("\"");
                        }
                        
                        print(" onMouseover=\"selectMenuItem(this, '");
                        print(UIConstants.DEFAULT_MENU_ITEM_SELECTED_STYLE_CLASS);
                        print("');\" onMouseout=\"unselectMenuItem(this, '");
                        print(UIConstants.DEFAULT_MENU_ITEM_STYLE_CLASS);
                        print("');\"");
                        
                        menuItemTooltip = menuItem.getDescription();
                        
                        if(menuItemTooltip == null || menuItemTooltip.length() == 0){
                            if(propertyId == null)
                                propertyId = new StringBuilder();
                            else
                                propertyId.delete(0, propertyId.length());
                            
                            propertyId.append(menuItemName);
                            propertyId.append(".");
                            propertyId.append(Constants.TOOLTIP_ATTRIBUTE_ID);
                            
                            menuItemTooltip = (resources != null ? resources.getProperty(propertyId.toString(), false) : null);
                            
                            if(menuItemTooltip == null)
                                menuItemTooltip = (mainConsoleResources != null ? mainConsoleResources.getProperty(propertyId.toString(), false) : null);
                            
                            if(menuItemTooltip == null)
                                menuItemTooltip = (defaultResources != null ? defaultResources.getProperty(propertyId.toString(), false) : null);
                        }
                        
                        if(menuItemTooltip != null && menuItemTooltip.length() > 0){
                            print(" title=\"");
                            print(menuItemTooltip);
                            print("\"");
                        }
                        
                        print(">");
                        
                        menuItemLabel = menuItem.getTitle();
                        
                        if(menuItemLabel == null || menuItemLabel.length() == 0){
                            if(propertyId == null)
                                propertyId = new StringBuilder();
                            else
                                propertyId.delete(0, propertyId.length());
                            
                            propertyId.append(menuItemName);
                            propertyId.append(".");
                            propertyId.append(Constants.LABEL_ATTRIBUTE_ID);
                            
                            menuItemLabel = (resources != null ? resources.getProperty(propertyId.toString(), false) : null);
                            
                            if(menuItemLabel == null)
                                menuItemLabel = (mainConsoleResources != null ? mainConsoleResources.getProperty(propertyId.toString(), false) : null);
                            
                            if(menuItemLabel == null)
                                menuItemLabel = (defaultResources != null ? defaultResources.getProperty(propertyId.toString()) : null);
                        }
                        
                        print("&nbsp;");
                        print(menuItemLabel);
                        print("&nbsp;");
                        
                        println("</td>");
                        
                        if(parentMenu != null){
                            print("<td id=\"");
                            print(menuItemName);
                            print(".");
                            print(UIConstants.DEFAULT_MENU_ITEM_ARROW_ID);
                            print("\" class=\"");
                            print(UIConstants.DEFAULT_MENU_ITEM_STYLE_CLASS);
                            print("\" align=\"");
                            print(AlignmentType.RIGHT);
                            print("\" width=\"1\">");
                            
                            if(hasSubmenuItems != null && hasSubmenuItems)
                                println("&raquo;</td>");
                        }
                    }
                    
                    if(parentMenu != null){
                        println("</tr>");
                        println("<tr>");
                    }
                }
            }
        }
        
        println("</tr>");
        println("</table>");
        
        List<? extends ObjectModel> submenusItems = null;
        Boolean hasSubmenusItems = null;
        
        for(ObjectModel menuItem: menuItems){
            menuItemName = menuItem.getName();
            parentMenuItem = menuItem.getParent();
            
            if((parentMenu == null && parentMenuItem == null) || (parentMenu != null && parentMenu.equals(parentMenuItem))){
                hasSubmenusItems = menuItem.hasChildren();
                
                if(hasSubmenusItems != null && hasSubmenusItems){
                    submenusItems = menuItem.getChildren();
                    
                    print("<div id=\"");
                    print(menuItemName);
                    print(".");
                    print(UIConstants.DEFAULT_MENU_BOX_ID);
                    print("\" class=\"");
                    print(UIConstants.DEFAULT_MENU_BOX_STYLE_CLASS);
                    print("\">");
                    
                    renderMenuItems(submenusItems, menuItem);
                    
                    println("</div>");
                }
            }
        }
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseActionFormComponent#renderOpen()
     */
    protected void renderOpen() throws InternalErrorException{
        String name = getName();
        
        if(name != null && name.length() > 0){
            print("<div id=\"");
            print(name);
            print(".");
            print(UIConstants.DEFAULT_MENU_BAR_ID);
            print("\" class=\"");
            print(UIConstants.DEFAULT_MENU_BAR_STYLE_CLASS);
            println("\">");
        }
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BasePropertyComponent#renderBody()
     */
    protected void renderBody() throws InternalErrorException{
        renderMenuItems();
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BasePropertyComponent#renderClose()
     */
    protected void renderClose() throws InternalErrorException{
        String name = getName();
        
        if(name != null && name.length() > 0){
            println("</div>");
            
            if(this.isFixed != null && this.isFixed){
                StringBuilder content = new StringBuilder();
                
                content.append("addScrollEvent(renderFixedMenuBar(\"");
                content.append(name);
                content.append("\"));");
                
                ScriptComponent scriptComponent = new ScriptComponent();
                
                scriptComponent.setPageContext(this.pageContext);
                scriptComponent.setOutputStream(getOutputStream());
                scriptComponent.setContent(content.toString());
                
                try{
                    scriptComponent.doStartTag();
                    scriptComponent.doEndTag();
                }
                catch(JspException e){
                    throw new InternalErrorException(e);
                }
            }
        }
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseActionFormComponent#clearAttributes()
     */
    protected void clearAttributes() throws InternalErrorException{
        super.clearAttributes();
        
        setIsFixed(null);
    }
}