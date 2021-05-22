package br.com.concepting.framework.ui.components;

import br.com.concepting.framework.constants.Constants;
import br.com.concepting.framework.constants.ProjectConstants;
import br.com.concepting.framework.controller.SystemController;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.resources.PropertiesResources;
import br.com.concepting.framework.ui.constants.UIConstants;
import br.com.concepting.framework.ui.controller.UIController;
import br.com.concepting.framework.util.DateTimeUtil;
import br.com.concepting.framework.util.StringUtil;
import br.com.concepting.framework.util.types.AlignmentType;
import br.com.concepting.framework.util.types.ContentType;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormatSymbols;
import java.util.Locale;

/**
 * Class that defines the page component.
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
public class PageComponent extends BaseComponent{
    private static final long serialVersionUID = -5686043597731581091L;
    
    private String title = null;
    private String encoding = null;
    
    /**
     * Returns the title of the page.
     *
     * @return String that contains the title of the page.
     */
    public String getTitle(){
        return this.title;
    }
    
    /**
     * Defines the title of the page.
     *
     * @param title String that contains the title of the page.
     */
    public void setTitle(String title){
        this.title = title;
    }
    
    /**
     * Returns the page encoding.
     *
     * @return String that contains the encoding.
     */
    public String getEncoding(){
        return this.encoding;
    }
    
    /**
     * Defines the page encoding.
     *
     * @param encoding String that contains the encoding.
     */
    public void setEncoding(String encoding){
        this.encoding = encoding;
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseComponent#buildStyle()
     */
    protected void buildStyle() throws InternalErrorException{
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseComponent#buildName()
     */
    protected void buildName() throws InternalErrorException{
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseComponent#buildEvents()
     */
    protected void buildEvents() throws InternalErrorException{
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseComponent#initialize()
     */
    protected void initialize() throws InternalErrorException{
        SystemController systemController = getSystemController();
        UIController uiController = getUIController();
        
        if(systemController == null || uiController == null)
            return;
        
        super.initialize();
        
        String encoding = getEncoding();
        
        if(encoding == null || encoding.length() == 0){
            encoding = Constants.DEFAULT_UNICODE_ENCODING;
            
            setEncoding(encoding);
            
            systemController.setEncoding(encoding);
        }
        
        uiController.hasPageComponentInstance(true);
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseComponent#renderOpen()
     */
    protected void renderOpen() throws InternalErrorException{
        SystemController systemController = getSystemController();
        
        if(systemController != null){
            println("<html>");
            println("<head>");
            
            if(this.encoding != null && this.encoding.length() > 0){
                print("<meta http-equiv=\"Content-Type\" content=\"");
                print(ContentType.HTML.getMimeType());
                print("; charset=");
                print(this.encoding);
                println("\">");
            }
            
            Locale currentLanguage = getCurrentLanguage();
            
            if(currentLanguage != null){
                print("<meta http-equiv=\"Content-Language\" content=\"");
                print(currentLanguage);
                println("\">");
            }
            
            if(this.title != null && this.title.length() > 0){
                print("<title>");
                print(this.title);
                println("</title>");
            }
            
            renderImports();
            renderEvents();
            
            println("</head>");
            println("<body>");
            
            renderLoadingBox();
            renderPageShade();
            
            super.renderOpen();
        }
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseComponent#renderClose()
     */
    protected void renderClose() throws InternalErrorException{
        SystemController systemController = getSystemController();
        
        if(systemController != null){
            super.renderClose();
            
            renderMessageBoxes();
            
            println("</body>");
            println("</html>");
        }
    }
    
    /**
     * Renders the loading box.
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void renderLoadingBox() throws InternalErrorException{
        renderLoadingBox(getSystemController(), getDefaultResources());
    }
    
    /**
     * Renders the loading box.
     *
     * @param systemController Instance that contains the system controller.
     * @param resources Instance that contains the resources.
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected static void renderLoadingBox(SystemController systemController, PropertiesResources resources) throws InternalErrorException{
        if(systemController == null || resources == null)
            return;
        
        PrintWriter outputStream = systemController.getOutputStream();
        
        if(outputStream == null)
            return;
        
        outputStream.print("<div id=\"");
        outputStream.print(UIConstants.DEFAULT_LOADING_BOX_ID);
        outputStream.print("\" class=\"");
        outputStream.print(UIConstants.DEFAULT_LOADING_BOX_STYLE_CLASS);
        outputStream.println("\">");
        
        outputStream.print("<div id=\"");
        outputStream.print(UIConstants.DEFAULT_LOADING_BOX_INFO_ID);
        outputStream.print("\" class=\"");
        outputStream.print(UIConstants.DEFAULT_LOADING_BOX_INFO_STYLE_CLASS);
        outputStream.println("\">");
        
        outputStream.println("<table>");
        outputStream.println("<tr>");
        outputStream.print("<td align=\"");
        outputStream.print(AlignmentType.CENTER);
        outputStream.println("\">");
        outputStream.print("<div class=\"");
        outputStream.print(UIConstants.DEFAULT_LOADING_BOX_ICON_STYLE_CLASS);
        outputStream.println("\"></div>");
        outputStream.println("</td>");
        outputStream.println("</tr>");
        
        String message = resources.getProperty(UIConstants.DEFAULT_LOADING_BOX_INFO_ID);
        
        if(message != null && message.length() > 0){
            outputStream.println("<tr>");
            outputStream.print("<td class=\"");
            outputStream.print(UIConstants.DEFAULT_LOADING_BOX_TEXT_STYLE_CLASS);
            outputStream.print("\">");
            outputStream.print(message);
            outputStream.println("</td>");
            outputStream.println("</tr>");
        }
        
        outputStream.println("</table>");
        
        outputStream.println("</div>");
        
        outputStream.println("</div>");
    }
    
    /**
     * Renders the page imports (CSS, JavaScripts, etc.)
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void renderImports() throws InternalErrorException{
        renderImports(getSystemController(), getCurrentLanguage());
    }
    
    /**
     * Renders the page events.
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void renderEvents() throws InternalErrorException{
        renderEvents(getSystemController());
    }
    
    /**
     * Renders the message boxes.
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void renderMessageBoxes() throws InternalErrorException{
        renderMessageBoxes(getSystemController());
    }
    
    /**
     * Renders the page shade.
     *
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected void renderPageShade() throws InternalErrorException{
        renderPageShade(getSystemController());
    }
    
    /**
     * Renders the page shade.
     *
     * @param systemController Instance that contains the system controller.
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected static void renderPageShade(SystemController systemController) throws InternalErrorException{
        if(systemController == null)
            return;
        
        PrintWriter outputStream = systemController.getOutputStream();
        
        if(outputStream == null)
            return;
        
        outputStream.print("<div id=\"");
        outputStream.print(UIConstants.DEFAULT_PAGE_SHADE_ID);
        outputStream.print("\" class=\"");
        outputStream.print(UIConstants.DEFAULT_PAGE_SHADE_STYLE_CLASS);
        outputStream.println("\"></div>");
    }
    
    /**
     * Renders the page events.
     *
     * @param systemController Instance that contains the system controller.
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected static void renderEvents(SystemController systemController) throws InternalErrorException{
        if(systemController == null)
            return;
        
        UIController uiController = systemController.getUIController();
        
        if(uiController == null)
            return;
        
        Boolean hasPageEvents = uiController.hasPageEvents();
        
        if(hasPageEvents == null || !hasPageEvents){
            StringBuilder content = new StringBuilder();
            
            content.append("addMouseMoveEvent(slideIt);");
            content.append(StringUtil.getLineBreak());
            content.append("addLoadEvent(centralizeDialogBoxes);");
            content.append(StringUtil.getLineBreak());
            content.append("addScrollEvent(centralizeDialogBoxes);");
            content.append(StringUtil.getLineBreak());
            content.append("addResizeEvent(centralizeDialogBoxes);");
            
            ScriptComponent scriptComponent = new ScriptComponent();
            
            scriptComponent.setPageContext(systemController.getPageContext());
            scriptComponent.setOutputStream(systemController.getOutputStream());
            scriptComponent.setContent(content.toString());
            
            try{
                scriptComponent.doStartTag();
                scriptComponent.doEndTag();
            }
            catch(JspException e){
                throw new InternalErrorException(e);
            }
            
            uiController.hasPageEvents(true);
        }
    }
    
    /**
     * Renders the message boxes.
     *
     * @param systemController Instance that contains the system controller.
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected static void renderMessageBoxes(SystemController systemController) throws InternalErrorException{
        if(systemController == null)
            return;
        
        MessageBoxComponent messageBoxComponent = new MessageBoxComponent();
        
        messageBoxComponent.setPageContext(systemController.getPageContext());
        messageBoxComponent.setOutputStream(systemController.getOutputStream());
        messageBoxComponent.setShowOnLoad(true);
        
        try{
            messageBoxComponent.doStartTag();
            messageBoxComponent.doEndTag();
        }
        catch(JspException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Renders the page imports (CSS, JavaScripts, etc.)
     *
     * @param systemController Instance that contains the system controller.
     * @param currentLanguage Instance that contains the current language.
     * @throws InternalErrorException Occurs when was not possible to render.
     */
    protected static void renderImports(SystemController systemController, Locale currentLanguage) throws InternalErrorException{
        if(systemController == null)
            return;
        
        PageContext pageContext = systemController.getPageContext();
        PrintWriter outputStream = systemController.getOutputStream();
        
        if(pageContext == null || outputStream == null)
            return;
        
        String contextPath = systemController.getContextPath();
        
        if(contextPath == null || contextPath.length() == 0)
            return;
        
        StyleComponent styleComponent = new StyleComponent();
        
        styleComponent.setPageContext(pageContext);
        styleComponent.setOutputStream(outputStream);
        
        String requestPath = StringUtil.replaceAll(systemController.getRequestPath(), UIConstants.DEFAULT_PAGES_DIR, "");
        
        int pos = (requestPath != null && requestPath.length() > 0 ? requestPath.lastIndexOf("/") : -1);
        
        try{
            if(pos >= 0)
                requestPath = requestPath.substring(0, pos);
            
            if(requestPath != null && requestPath.startsWith("/")){
                StringBuilder url = new StringBuilder();
                
                url.append(requestPath.substring(1));
                url.append(UIConstants.DEFAULT_STYLES_RESOURCES_DIR);
                url.append(UIConstants.DEFAULT_PAGE_STYLE_RESOURCES_ID.substring(1));
                
                styleComponent.setUrl(url.toString());
                styleComponent.doStartTag();
                styleComponent.doEndTag();
            }
            
            styleComponent.setUrl(UIConstants.DEFAULT_PAGE_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            styleComponent.setUrl(UIConstants.DEFAULT_COMMON_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            styleComponent.setUrl(UIConstants.DEFAULT_ACCORDION_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            styleComponent.setUrl(UIConstants.DEFAULT_CALENDAR_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            styleComponent.setUrl(UIConstants.DEFAULT_CLOCK_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            styleComponent.setUrl(UIConstants.DEFAULT_COLOR_PICKER_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            styleComponent.setUrl(UIConstants.DEFAULT_DIALOG_BOX_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            styleComponent.setUrl(UIConstants.DEFAULT_GRID_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            styleComponent.setUrl(UIConstants.DEFAULT_GUIDES_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            styleComponent.setUrl(UIConstants.DEFAULT_LOGIN_SESSION_BOX_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            styleComponent.setUrl(UIConstants.DEFAULT_MAPS_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            styleComponent.setUrl(UIConstants.DEFAULT_MENU_BAR_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            styleComponent.setUrl(UIConstants.DEFAULT_MESSAGE_BOX_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            styleComponent.setUrl(UIConstants.DEFAULT_PAGER_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            styleComponent.setUrl(UIConstants.DEFAULT_PROGRESS_BAR_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            styleComponent.setUrl(UIConstants.DEFAULT_RICH_TEXT_AREA_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            styleComponent.setUrl(UIConstants.DEFAULT_SLIDER_BAR_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            styleComponent.setUrl(UIConstants.DEFAULT_SPINNER_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            styleComponent.setUrl(UIConstants.DEFAULT_SUGGESTION_BOX_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            styleComponent.setUrl(UIConstants.DEFAULT_TIMER_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            styleComponent.setUrl(UIConstants.DEFAULT_TREE_VIEW_STYLE_RESOURCES_ID);
            styleComponent.doStartTag();
            styleComponent.doEndTag();
            
            ScriptComponent scriptComponent = new ScriptComponent();
            
            scriptComponent.setPageContext(pageContext);
            scriptComponent.setOutputStream(outputStream);
            
            if(requestPath != null && requestPath.startsWith("/")){
                StringBuilder url = new StringBuilder();
                
                url.append(requestPath.substring(1));
                url.append(UIConstants.DEFAULT_SCRIPTS_RESOURCES_DIR);
                url.append(UIConstants.DEFAULT_PAGE_SCRIPT_RESOURCES_ID.substring(1));
                
                scriptComponent.setUrl(url.toString());
                scriptComponent.doStartTag();
                scriptComponent.doEndTag();
            }
            
            scriptComponent.setUrl(UIConstants.DEFAULT_PAGE_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            scriptComponent.setUrl(UIConstants.DEFAULT_COMMON_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            scriptComponent.setUrl(UIConstants.DEFAULT_ACCORDION_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            scriptComponent.setUrl(UIConstants.DEFAULT_CALENDAR_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            DateFormatSymbols symbols = DateTimeUtil.getFormatSymbols(currentLanguage);
            String[] weekNames = symbols.getWeekdays();
            String[] monthNames = symbols.getMonths();
            StringBuilder content = new StringBuilder();
            
            content.append("initializeCalendarWeekNames(");
            
            for(int cont = 1; cont < weekNames.length; cont++){
                if(cont > 1)
                    content.append(", ");
                
                content.append("\"");
                content.append(weekNames[cont]);
                content.append("\"");
            }
            
            content.append(");");
            content.append(StringUtil.getLineBreak());
            content.append("initializeCalendarMonthNames(");
            
            for(int cont = 0; cont < monthNames.length; cont++){
                if(cont > 0)
                    content.append(", ");
                
                content.append("\"");
                content.append(monthNames[cont]);
                content.append("\"");
            }
            
            content.append(");");
            
            scriptComponent.setContent(content.toString());
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            scriptComponent.setContent(null);
            scriptComponent.setUrl(UIConstants.DEFAULT_CLOCK_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            scriptComponent.setUrl(UIConstants.DEFAULT_COLOR_PICKER_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            scriptComponent.setUrl(UIConstants.DEFAULT_DIALOG_BOX_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            scriptComponent.setUrl(UIConstants.DEFAULT_GRID_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            scriptComponent.setUrl(UIConstants.DEFAULT_GUIDES_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            scriptComponent.setUrl(UIConstants.DEFAULT_LOGIN_SESSION_BOX_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            scriptComponent.setUrl(UIConstants.DEFAULT_MAPS_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            scriptComponent.setUrl(UIConstants.DEFAULT_MENU_BAR_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            scriptComponent.setUrl(UIConstants.DEFAULT_MESSAGE_BOX_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            scriptComponent.setUrl(UIConstants.DEFAULT_PAGER_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            scriptComponent.setUrl(UIConstants.DEFAULT_PROGRESS_BAR_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            scriptComponent.setUrl(UIConstants.DEFAULT_RICH_TEXT_AREA_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            scriptComponent.setUrl(UIConstants.DEFAULT_SLIDER_BAR_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            scriptComponent.setUrl(UIConstants.DEFAULT_SPINNER_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            scriptComponent.setUrl(UIConstants.DEFAULT_SUGGESTION_BOX_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            scriptComponent.setUrl(UIConstants.DEFAULT_TIMER_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
            
            scriptComponent.setUrl(UIConstants.DEFAULT_TREE_VIEW_SCRIPT_RESOURCES_ID);
            scriptComponent.doStartTag();
            scriptComponent.doEndTag();
        }
        catch(JspException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseComponent#clearAttributes()
     */
    protected void clearAttributes() throws InternalErrorException{
        super.clearAttributes();
        
        setEncoding(null);
        setTitle(null);
    }
}