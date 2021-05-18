package br.com.concepting.framework.model;

import br.com.concepting.framework.annotations.System;
import br.com.concepting.framework.model.annotations.Model;
import br.com.concepting.framework.model.annotations.Property;
import br.com.concepting.framework.model.types.ValidationType;

/**
 * Class that defines the basic implementation of the main console data model
 *
 * @author fvilarinho
 * @since 3.2.0
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
@Model(ui = "Main Console", templateId = "mainConsole")
@System
public abstract class MainConsoleModel extends BaseModel{
    private static final long serialVersionUID = -8176019419161691084L;
    
    @Property(validations = ValidationType.REQUIRED)
    private String currentLanguage = null;
    
    @Property(validations = ValidationType.REQUIRED)
    private String currentSkin = null;
    
    /**
     * Returns the identifier of the current language.
     *
     * @return String that contains the identifier of the language.
     */
    public String getCurrentLanguage(){
        return this.currentLanguage;
    }
    
    /**
     * Defines the identifier of the current language.
     *
     * @param currentLanguage String that contains the identifier of the
     * language.
     */
    public void setCurrentLanguage(String currentLanguage){
        this.currentLanguage = currentLanguage;
    }
    
    /**
     * Returns the identifier of the current skin.
     *
     * @return String that contains the identifier of the skin.
     */
    public String getCurrentSkin(){
        return this.currentSkin;
    }
    
    /**
     * Defines the identifier of the current skin.
     *
     * @param currentSkin String that contains the identifier of the skin.
     */
    public void setCurrentSkin(String currentSkin){
        this.currentSkin = currentSkin;
    }
}