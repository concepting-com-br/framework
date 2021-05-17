package br.com.concepting.framework.resources;

import br.com.concepting.framework.constants.Constants;
import br.com.concepting.framework.constants.SystemConstants;
import br.com.concepting.framework.controller.form.constants.ActionFormConstants;
import br.com.concepting.framework.resources.exceptions.InvalidResourcesException;
import br.com.concepting.framework.util.LanguageUtil;
import br.com.concepting.framework.util.PropertyUtil;
import br.com.concepting.framework.util.helpers.XmlNode;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Class responsible to manipulate of the system resources.
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
public class SystemResourcesLoader extends XmlResourcesLoader<SystemResources>{
    /**
     * Constructor - Manipulates the default resources.
     *
     * @throws InvalidResourcesException Occurs when the resources could not be read.
     */
    public SystemResourcesLoader() throws InvalidResourcesException{
        super(SystemConstants.DEFAULT_RESOURCES_ID);
    }
    
    /**
     * Constructor - Manipulates specific resources.
     *
     * @param resourcesDirname String that contains the directory where the resources
     * are stored.
     * @throws InvalidResourcesException Occurs when the resources could not be read.
     */
    public SystemResourcesLoader(String resourcesDirname) throws InvalidResourcesException{
        super(resourcesDirname, SystemConstants.DEFAULT_RESOURCES_ID);
    }
    
    /**
     * @see br.com.concepting.framework.resources.XmlResourcesLoader#parseResources(br.com.concepting.framework.util.helpers.XmlNode)
     */
    public SystemResources parseResources(XmlNode resourcesNode) throws InvalidResourcesException{
        String resourcesDirname = getResourcesDirname();
        String resourcesId = getResourcesId();
        SystemResources resources = new SystemResources();
        
        resources.setId(Constants.DEFAULT_ATTRIBUTE_ID);
        resources.setDefault(true);
        
        XmlNode packagesPrefixNode = resourcesNode.getNode(SystemConstants.PACKAGES_PREFIX_ATTRIBUTE_ID);
        
        if(packagesPrefixNode != null){
            String value = packagesPrefixNode.getValue();
            
            if(value != null && value.length() > 0)
                resources.setPackagesPrefix(value);
            else
                throw new InvalidResourcesException(resourcesDirname, resourcesId, packagesPrefixNode.getText());
        }
        else
            throw new InvalidResourcesException(resourcesDirname, resourcesId, resourcesNode.getText());
        
        XmlNode mainConsoleNode = resourcesNode.getNode(SystemConstants.MAIN_CONSOLE_ATTRIBUTE_ID);
        
        if(mainConsoleNode != null){
            XmlNode classNode = mainConsoleNode.getNode(Constants.CLASS_ATTRIBUTE_ID);
            
            if(classNode != null){
                String value = classNode.getValue();
                
                if(value != null && value.length() > 0){
                    try{
                        resources.setMainConsoleClass(classNode.getValue());
                    }
                    catch(ClassNotFoundException | ClassCastException e){
                        throw new InvalidResourcesException(resourcesDirname, resourcesId, classNode.getText());
                    }
                }
            }
        }
        
        XmlNode skinsNode = resourcesNode.getNode(SystemConstants.SKINS_ATTRIBUTE_ID);
        
        if(skinsNode == null)
            throw new InvalidResourcesException(resourcesDirname, resourcesId, resourcesNode.getText());
        
        List<XmlNode> skinsChildNodes = skinsNode.getChildren();
        
        if(skinsChildNodes == null || skinsChildNodes.size() == 0)
            throw new InvalidResourcesException(resourcesDirname, resourcesId, skinsNode.getText());
        
        Collection<String> skins = PropertyUtil.instantiate(Constants.DEFAULT_LIST_CLASS);
        String skin = null;
        String defaultSkin = null;
        
        for(XmlNode skinChildNode: skinsChildNodes){
            skin = skinChildNode.getAttribute(Constants.IDENTITY_ATTRIBUTE_ID);
            
            if(skin != null && skin.length() > 0){
                if(Boolean.valueOf(skinChildNode.getAttribute(Constants.DEFAULT_ATTRIBUTE_ID)))
                    defaultSkin = skin;
                
                skins.add(skin);
            }
        }
        
        if(skins == null || skins.size() == 0)
            throw new InvalidResourcesException(resourcesDirname, resourcesId, skinsNode.getText());
        
        if(defaultSkin == null || defaultSkin.length() == 0)
            defaultSkin = skins.iterator().next();
        
        resources.setSkins(skins);
        resources.setDefaultSkin(defaultSkin);
        
        XmlNode languagesNode = resourcesNode.getNode(SystemConstants.LANGUAGES_ATTRIBUTE_ID);
        
        if(languagesNode == null)
            throw new InvalidResourcesException(resourcesDirname, resourcesId, resourcesNode.getText());
        
        List<XmlNode> languagesChildNodes = languagesNode.getChildren();
        
        if(languagesChildNodes == null || languagesChildNodes.size() == 0)
            throw new InvalidResourcesException(resourcesDirname, resourcesId, languagesNode.getText());
        
        Collection<Locale> languages = PropertyUtil.instantiate(Constants.DEFAULT_LIST_CLASS);
        String languageBuffer = null;
        Locale language = null;
        Locale defaultLanguage = null;
        
        for(XmlNode languageChildNode: languagesChildNodes){
            languageBuffer = languageChildNode.getAttribute(Constants.IDENTITY_ATTRIBUTE_ID);
            
            if(languageBuffer != null && languageBuffer.length() > 0){
                language = LanguageUtil.getLanguageByString(languageBuffer);
                
                if(Boolean.valueOf(languageChildNode.getAttribute(Constants.DEFAULT_ATTRIBUTE_ID)))
                    defaultLanguage = language;
                
                languages.add(language);
            }
        }
        
        if(languages == null || languages.size() == 0)
            throw new InvalidResourcesException(resourcesDirname, resourcesId, languagesNode.getText());
        
        if(defaultLanguage == null)
            defaultLanguage = languages.iterator().next();
        
        resources.setLanguages(languages);
        resources.setDefaultLanguage(defaultLanguage);
        
        return resources;
    }
}