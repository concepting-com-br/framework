package br.com.concepting.framework.ui.components;

import br.com.concepting.framework.constants.SystemConstants;
import br.com.concepting.framework.controller.action.types.ActionType;
import br.com.concepting.framework.controller.form.constants.ActionFormConstants;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.MainConsoleModel;
import br.com.concepting.framework.model.util.ModelUtil;
import br.com.concepting.framework.ui.constants.UIConstants;
import br.com.concepting.framework.util.LanguageUtil;

import javax.ws.rs.HttpMethod;
import java.util.Collection;
import java.util.Locale;

/**
 * Class that defines the language selector component.
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
public class LanguageSelectorComponent extends ListPropertyComponent{
    private static final long serialVersionUID = -3260096810965124526L;
    
    /**
     * @see br.com.concepting.framework.ui.components.OptionsPropertyComponent#getOptionLabel(java.lang.Object,
     * java.lang.Integer)
     */
    protected String getOptionLabel(Object option, Integer level) throws InternalErrorException{
        if(option != null){
            Locale language = LanguageUtil.getLanguageByString(option.toString());
            
            return language.getDisplayName(getCurrentLanguage());
        }
        
        return null;
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseActionFormComponent#buildResources()
     */
    protected void buildResources() throws InternalErrorException{
        setResourcesId(UIConstants.DEFAULT_COMMON_RESOURCES_ID);
        setResourcesKey(SystemConstants.CURRENT_LANGUAGE_ATTRIBUTE_ID);
        
        super.buildResources();
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseActionFormComponent#buildName()
     */
    protected void buildName() throws InternalErrorException{
        String name = getName();
        
        if(name == null || name.length() == 0)
            setName(SystemConstants.CURRENT_LANGUAGE_ATTRIBUTE_ID);
        
        super.buildName();
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.OptionsPropertyComponent#buildEvents()
     */
    protected void buildEvents() throws InternalErrorException{
        if(hasInvalidPropertyDefinition() == null){
            Class<? extends MainConsoleModel> modelClass = ModelUtil.getMainConsoleClass();
            
            if(modelClass != null){
                String contextPath = getContextPath();
                String actionFormUrl = ModelUtil.getUrlByModel(modelClass);
                
                if(contextPath != null && contextPath.length() > 0 && actionFormUrl != null && actionFormUrl.length() > 0){
                    StringBuilder url = new StringBuilder();
                    
                    url.append(contextPath);
                    url.append(actionFormUrl);
                    url.append(ActionFormConstants.DEFAULT_ACTION_SERVLET_FILE_EXTENSION);
                    
                    StringBuilder onChange = new StringBuilder();
                    
                    onChange.append("submitRequest('");
                    onChange.append(HttpMethod.POST);
                    onChange.append("', '");
                    onChange.append(url);
                    onChange.append("', '");
                    onChange.append(ActionFormConstants.ACTION_ATTRIBUTE_ID);
                    onChange.append("=");
                    onChange.append(ActionType.CHANGE_CURRENT_LANGUAGE.getMethod());
                    onChange.append("&");
                    onChange.append(ActionFormConstants.FORWARD_ATTRIBUTE_ID);
                    onChange.append("=");
                    onChange.append(ActionFormConstants.DEFAULT_ROOT_FORWARD_ID);
                    onChange.append("&");
                    onChange.append(SystemConstants.CURRENT_LANGUAGE_ATTRIBUTE_ID);
                    onChange.append("=' + this.value);");
                    
                    setOnChange(onChange.toString());
                }
            }
        }

        super.buildEvents();
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.BaseOptionsPropertyComponent#buildRestrictions()
     */
    protected void buildRestrictions() throws InternalErrorException{
        if(getShowFirstOption() == null)
            setShowFirstOption(false);
        
        super.buildRestrictions();
    }
    
    /**
     * @see br.com.concepting.framework.ui.components.ListPropertyComponent#initialize()
     */
    protected void initialize() throws InternalErrorException{
        Collection<String> availableLanguages = LanguageUtil.getAvailableLanguages();
        
        if(availableLanguages != null && !availableLanguages.isEmpty()){
            setDatasetValues(availableLanguages);
            
            String name = getName();
            
            if(name == null || name.length() == 0){
                Locale currentLanguage = getCurrentLanguage();
                
                if(currentLanguage != null)
                    setValue(currentLanguage.toString());
            }
        }
        
        super.initialize();
    }
}