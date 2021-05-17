package br.com.concepting.framework.ui.components;

import br.com.concepting.framework.constants.Constants;
import br.com.concepting.framework.controller.action.types.ActionType;
import br.com.concepting.framework.controller.form.ActionFormController;
import br.com.concepting.framework.controller.form.BaseActionForm;
import br.com.concepting.framework.controller.form.constants.ActionFormConstants;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.BaseModel;
import br.com.concepting.framework.model.constants.ModelConstants;
import br.com.concepting.framework.ui.constants.UIConstants;
import br.com.concepting.framework.util.ByteUtil;
import br.com.concepting.framework.util.ImageUtil;
import br.com.concepting.framework.util.PropertyUtil;
import br.com.concepting.framework.util.StringUtil;
import br.com.concepting.framework.util.helpers.PropertyInfo;
import br.com.concepting.framework.util.types.ComponentType;
import br.com.concepting.framework.util.types.ContentType;

import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Class that defines the download box component.
 *
 * @author fvilarinho
 * @since 3.0.0
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
public class DownloadPropertyComponent extends BasePropertyComponent{
    private static final long serialVersionUID = -1340422255244638637L;
    
    private Boolean showContentFilename = null;
    private Boolean showContentSize = null;
    private String contentWidth = null;
    private String contentHeight = null;
    
    /**
     * Indicates if the content filename should be shown.
     *
     * @return True/False.
     */
    public Boolean showContentFilename(){
        return this.showContentFilename;
    }
    
    /**
     * Indicates if the content filename should be shown.
     *
     * @return True/False.
     */
    public Boolean getShowContentFilename(){
        return showContentFilename();
    }
    
    /**
     * Defines if the content filename should be shown.
     *
     * @param showContentFilename True/False.
     */
    public void setShowContentFilename(Boolean showContentFilename){
        this.showContentFilename = showContentFilename;
    }
    
    /**
     * Indicates if the content size should be shown.
     *
     * @return True/False.
     */
    public Boolean isShowContentSize(){
        return this.showContentSize;
    }
    
    /**
     * Indicates if the content size should be shown.
     *
     * @return True/False.
     */
    public Boolean getShowContentSize(){
        return isShowContentSize();
    }
    
    /**
     * Defines if the content size should be shown.
     *
     * @param showContentSize True/False.
     */
    public void setShowContentSize(Boolean showContentSize){
        this.showContentSize = showContentSize;
    }
    
    /**
     * Returns the width of the content.
     *
     * @return String that contains the width.
     */
    public String getContentWidth(){
        return this.contentWidth;
    }
    
    /**
     * Defines the width of the content.
     *
     * @param contentWidth String that contains the width.
     */
    public void setContentWidth(String contentWidth){
        this.contentWidth = contentWidth;
    }
    
    /**
     * Returns the height of the content.
     *
     * @return String that contains the height.
     */
    public String getContentHeight(){
        return this.contentHeight;
    }
    
    /**
     * Defines the height of the content.
     *
     * @param contentHeight String that contains the height.
     */
    public void setContentHeight(String contentHeight){
        this.contentHeight = contentHeight;
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BasePropertyComponent#buildRestrictions()
     */
    protected void buildRestrictions() throws InternalErrorException{
        if(this.showContentFilename == null)
            this.showContentFilename = false;
        
        if(this.showContentSize == null)
            this.showContentSize = false;
        
        super.buildRestrictions();
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BasePropertyComponent#initialize()
     */
    protected void initialize() throws InternalErrorException{
        ComponentType componentType = getComponentType();
        
        if(componentType == null){
            componentType = ComponentType.DOWNLOAD;
            
            setComponentType(componentType);
        }
        
        super.initialize();
    }
    
    /**
     * Renders the download icon of the content.
     *
     * @param contentFilename String that contains the filename of the content.
     * @param contentType Instance that contains the type of the content.
     * @throws InternalErrorException Occurs when was not possible to render the
     * icon of the content.
     */
    private void renderDownloadIcon(String contentFilename, ContentType contentType) throws InternalErrorException{
        String contextPath = getContextPath();
        ActionFormController actionFormController = getActionFormController();
        BaseActionForm<? extends BaseModel> actionFormInstance = actionFormController.getActionFormInstance();
        String action = actionFormInstance.getAction();
        
        if(contextPath == null || contextPath.length() == 0 || action == null || action.length() == 0)
            return;
        
        print("<div class=\"");
        print(StringUtil.normalize(contentType.toString().toLowerCase()));
        print(StringUtil.capitalize(UIConstants.DEFAULT_ICON_ID));
        print("\"");
        
        if((this.contentWidth != null && this.contentWidth.length() > 0) || (this.contentHeight != null && this.contentHeight.length() > 0)){
            print(" style=\"");
            
            if(this.contentWidth != null && this.contentWidth.length() > 0){
                print("width: ");
                print(this.contentWidth);
                
                if(this.contentWidth.endsWith(";"))
                    print(";");
            }
            
            if(this.contentHeight != null && this.contentHeight.length() > 0){
                if(this.contentWidth != null && this.contentWidth.length() > 0)
                    print(" ");
                
                print("height: ");
                print(this.contentHeight);
                
                if(this.contentHeight.endsWith(";"))
                    print(";");
            }
            
            print("\"");
        }
        
        print(" onClick=\"window.open('");
        print(contextPath);
        print(action);
        print("?");
        print(ActionFormConstants.ACTION_ATTRIBUTE_ID);
        print("=");
        print(ActionType.DOWNLOAD.getMethod());
        print("&");
        print(Constants.CONTENT_ATTRIBUTE_ID);
        print("=");
        
        StringBuilder contentId = new StringBuilder();
        
        contentId.append(getActionFormName());
        contentId.append(".");
        contentId.append(ModelConstants.DEFAULT_ID);
        contentId.append(".");
        contentId.append(getName());
        
        print(contentId);
        
        if(contentFilename != null && contentFilename.length() > 0){
            print("&");
            print(Constants.CONTENT_FILENAME_ATTRIBUTE_ID);
            print("=");
            print(contentFilename);
        }
        
        print("&");
        print(Constants.CONTENT_TYPE_ATTRIBUTE_ID);
        print("=");
        print(contentType.getMimeType());
        print("', '_");
        print(contentId);
        println("');\"></div>");
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BasePropertyComponent#renderBody()
     */
    protected void renderBody() throws InternalErrorException{
        ActionFormController actionFormController = getActionFormController();
        PropertyInfo propertyInfo = getPropertyInfo();
        String actionFormName = getActionFormName();
        String name = getName();
        
        if(actionFormController == null || propertyInfo == null || actionFormName == null || actionFormName.length() == 0 || name == null || name.length() == 0)
            renderInvalidPropertyMessage();
        else{
            byte[] content = getValue();
            String contentTypePropertyId = propertyInfo.getContentTypePropertyId();
            String contentFilenamePropertyId = propertyInfo.getContentFilenamePropertyId();
            ContentType contentType = null;
            String contentFilename = null;
            
            if(content != null){
                try{
                    if((contentTypePropertyId != null && contentTypePropertyId.length() > 0) || (contentFilenamePropertyId != null && contentFilenamePropertyId.length() > 0)){
                        int pos = name.lastIndexOf(".");
                        String prefix = null;
                        
                        if(pos >= 0)
                            prefix = name.substring(0, pos);
                        
                        BaseActionForm<? extends BaseModel> actionFormInstance = actionFormController.getActionFormInstance();
                        BaseModel model = (actionFormInstance != null ? actionFormInstance.getModel() : null);
                        
                        if(contentTypePropertyId != null && contentTypePropertyId.length() > 0){
                            StringBuilder contentTypePropertyIdBuffer = new StringBuilder();
                            
                            if(prefix != null && prefix.length() > 0){
                                contentTypePropertyIdBuffer.append(prefix);
                                contentTypePropertyIdBuffer.append(".");
                            }
                            
                            contentTypePropertyIdBuffer.append(contentTypePropertyId);
                            
                            contentTypePropertyId = contentTypePropertyIdBuffer.toString();
                            contentType = PropertyUtil.getValue(model, contentTypePropertyId);
                        }
                        
                        if(contentFilenamePropertyId != null && contentFilenamePropertyId.length() > 0){
                            StringBuilder contentFilenamePropertyIdBuffer = new StringBuilder();
                            
                            if(prefix != null && prefix.length() > 0){
                                contentFilenamePropertyIdBuffer.append(prefix);
                                contentFilenamePropertyIdBuffer.append(".");
                            }
                            
                            contentFilenamePropertyIdBuffer.append(contentFilenamePropertyId);
                            
                            contentFilenamePropertyId = contentFilenamePropertyIdBuffer.toString();
                            contentFilename = PropertyUtil.getValue(model, contentFilenamePropertyId);
                        }
                    }
                    
                    if(contentFilename != null && contentFilename.length() > 0)
                        contentType = ContentType.toContentType(contentFilename);
                    
                    if(contentType == null)
                        contentType = ImageUtil.getImageFormat(content);
                }
                catch(NullPointerException | IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | IOException e){
                    contentType = ContentType.BINARY;
                }
            }
            
            if((contentType == ContentType.GIF || contentType == ContentType.JPG || contentType == ContentType.PNG) && (this.showContentFilename == null || !this.showContentFilename || contentFilename == null || contentFilename.length() == 0) && (this.showContentSize == null || !this.showContentSize)){
                ImageComponent imageComponent = new ImageComponent();
                
                imageComponent.setPageContext(this.pageContext);
                imageComponent.setOutputStream(getOutputStream());
                imageComponent.setActionFormName(actionFormName);
                imageComponent.setPropertyInfo(propertyInfo);
                imageComponent.setResourcesId(getResourcesId());
                imageComponent.setResourcesKey(getResourcesKey());
                imageComponent.setId(getId());
                imageComponent.setName(getName());
                imageComponent.setLabel(getLabel());
                imageComponent.setShowLabel(false);
                imageComponent.setStyleClass(UIConstants.DEFAULT_IMAGE_THUMBNAIL_STYLE_CLASS);
                imageComponent.setWidth(this.contentWidth);
                imageComponent.setHeight(this.contentHeight);
                imageComponent.setValue(content);
                
                try{
                    imageComponent.doStartTag();
                    imageComponent.doEndTag();
                }
                catch(JspException e){
                    throw new InternalErrorException(e);
                }
            }
            else if(contentType != null){
                print("<table class=\"");
                print(UIConstants.DEFAULT_DOWNLOAD_STYLE_CLASS);
                println("\">");
                
                println("<tr>");
                println("<td>");
                
                renderDownloadIcon(contentFilename, contentType);
                
                println("</td>");
                println("<td>");
                
                if(this.showContentFilename != null && this.showContentFilename && contentFilename != null && contentFilename.length() > 0){
                    LabelComponent filenameComponent = new LabelComponent();
                    
                    filenameComponent.setPageContext(this.pageContext);
                    filenameComponent.setOutputStream(getOutputStream());
                    filenameComponent.setShowLabel(false);
                    filenameComponent.setStyle("font-weight: bold;");
                    filenameComponent.setValue(contentFilename);
                    
                    try{
                        filenameComponent.doStartTag();
                        filenameComponent.doEndTag();
                    }
                    catch(JspException e){
                        throw new InternalErrorException(e);
                    }
                    
                    println("<br/>");
                }
                
                if(this.showContentSize != null && this.showContentSize){
                    LabelComponent contentSizeComponent = new LabelComponent();
                    
                    contentSizeComponent.setPageContext(this.pageContext);
                    contentSizeComponent.setOutputStream(getOutputStream());
                    contentSizeComponent.setShowLabel(false);
                    contentSizeComponent.setValue(ByteUtil.formatBytes(new Long(content.length), getCurrentLanguage()));
                    
                    try{
                        contentSizeComponent.doStartTag();
                        contentSizeComponent.doEndTag();
                    }
                    catch(JspException e){
                        throw new InternalErrorException(e);
                    }
                }
                
                println("</td>");
                println("</tr>");
                
                println("</table>");
            }
        }
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BasePropertyComponent#clearAttributes()
     */
    protected void clearAttributes() throws InternalErrorException{
        super.clearAttributes();
        
        setContentWidth(null);
        setContentHeight(null);
        setShowContentFilename(null);
        setShowContentSize(null);
    }
}