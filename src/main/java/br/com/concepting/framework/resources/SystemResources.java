package br.com.concepting.framework.resources;

import br.com.concepting.framework.model.MainConsoleModel;
import br.com.concepting.framework.util.helpers.XmlNode;

import java.util.Collection;
import java.util.Locale;

/**
 * Class responsible to store the system resources.
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
public class SystemResources extends BaseResources<XmlNode>{
    private static final long serialVersionUID = -1115220620296191917L;
    
    private String packagesPrefix = null;
    private Class<? extends MainConsoleModel> mainConsoleClass = null;
    private Collection<Locale> languages = null;
    private Locale defaultLanguage = null;
    private Collection<String> skins = null;
    private String defaultSkin = null;
    
    public String getPackagesPrefix(){
        return this.packagesPrefix;
    }
    
    public void setPackagesPrefix(String packagesPrefix){
        this.packagesPrefix = packagesPrefix;
    }
    
    /**
     * Returns the class that defines the main console data model.
     *
     * @return Class that defines the main console data model.
     */
    public Class<? extends MainConsoleModel> getMainConsoleClass(){
        return this.mainConsoleClass;
    }
    
    /**
     * Defines the class that defines the main console data model.
     *
     * @param mainConsoleClassName String that contains the identifier of the
     * main console data model.
     * @throws ClassNotFoundException Occurs when was not possible to
     * instantiate the class.
     * @throws ClassCastException Occurs when was not possible to instantiate
     * the class.
     */
    @SuppressWarnings("unchecked")
    public void setMainConsoleClass(String mainConsoleClassName) throws ClassNotFoundException, ClassCastException{
        if(mainConsoleClassName != null && mainConsoleClassName.length() > 0)
            setMainConsoleClass((Class<? extends MainConsoleModel>) Class.forName(mainConsoleClassName));
    }
    
    /**
     * Defines the class that defines the main console data model.
     *
     * @param mainConsoleClass Class that defines the main console data model.
     */
    public void setMainConsoleClass(Class<? extends MainConsoleModel> mainConsoleClass){
        this.mainConsoleClass = mainConsoleClass;
    }
    
    /**
     * Returns the list of available skins.
     *
     * @return List of available skins.
     */
    public Collection<String> getSkins(){
        return this.skins;
    }
    
    /**
     * Defines the list of available skins.
     *
     * @param skins List of available skins.
     */
    public void setSkins(Collection<String> skins){
        this.skins = skins;
    }
    
    /**
     * Returns the list of available languages.
     *
     * @return List of available languages.
     */
    public Collection<Locale> getLanguages(){
        return this.languages;
    }
    
    /**
     * Defines the list of available languages.
     *
     * @param languages List of available languages.
     */
    public void setLanguages(Collection<Locale> languages){
        this.languages = languages;
    }
    
    /**
     * Returns the default language.
     *
     * @return Instance that contains default language.
     */
    public Locale getDefaultLanguage(){
        return this.defaultLanguage;
    }
    
    /**
     * Defines the default language.
     *
     * @param defaultLanguage Instance that contains default language.
     */
    public void setDefaultLanguage(Locale defaultLanguage){
        this.defaultLanguage = defaultLanguage;
    }
    
    /**
     * Returns the default skin.
     *
     * @return String that contains default skin.
     */
    public String getDefaultSkin(){
        return this.defaultSkin;
    }
    
    /**
     * Defines the default skin.
     *
     * @param defaultSkin String that contains default skin.
     */
    public void setDefaultSkin(String defaultSkin){
        this.defaultSkin = defaultSkin;
    }
}