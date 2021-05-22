package br.com.concepting.framework.util;

import br.com.concepting.framework.annotations.System;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

/**
 * Class responsible to manipulate the language resources.
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
public class LanguageUtil{
    public static Collection<String> getAvailableLanguages(){
        Set<Class<?>> classes = ReflectionUtil.getTypesAnnotatedWith(System.class);
        
        if(classes != null && !classes.isEmpty()){
            try{
                System system = classes.parallelStream().map(c -> c.getAnnotation(System.class)).findFirst().get();
                
                return Arrays.asList(system.availableLanguages());
            }
            catch(Throwable e){
            }
        }
        
        return null;
    }
    
    /**
     * Returns the instance of the language based on its identifier.
     *
     * @param value String that contains the identifier of the language.
     * @return Instance that contains the language.
     */
    public static Locale getLanguageByString(String value){
        if(value != null && value.length() > 0){
            Locale language = null;
            String languageBuffer[] = StringUtil.split((String) value, "_");
            
            if(languageBuffer.length == 1)
                language = new Locale(languageBuffer[0]);
            else if(languageBuffer.length == 2)
                language = new Locale(languageBuffer[0], languageBuffer[1]);
            else if(languageBuffer.length == 3)
                language = new Locale(languageBuffer[0], languageBuffer[1], languageBuffer[2]);
            
            return language;
        }
        
        return null;
    }
    
    /**
     * Returns the instance of the default language.
     *
     * @return Instance that contains the language.
     */
    public static Locale getDefaultLanguage(){
        Set<Class<?>> classes = ReflectionUtil.getTypesAnnotatedWith(System.class);
    
        if(classes != null && !classes.isEmpty()){
            try{
                System system = classes.parallelStream().map(c -> c.getAnnotation(System.class)).skip(classes.size() - 1).findFirst().get();
            
                return LanguageUtil.getLanguageByString(system.defaultLanguage());
            }
            catch(Throwable e){
            }
        }
        
        return Locale.getDefault();
    }
}