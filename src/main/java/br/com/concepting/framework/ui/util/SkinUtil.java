package br.com.concepting.framework.ui.util;

import br.com.concepting.framework.annotations.System;
import br.com.concepting.framework.model.MainConsoleModel;
import br.com.concepting.framework.ui.constants.UIConstants;
import br.com.concepting.framework.util.ReflectionUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public class SkinUtil{
    public static String getDefaultSkin(){
        Set<Class<?>> classes = ReflectionUtil.getTypesAnnotatedWith(System.class);
    
        if(classes != null && !classes.isEmpty()){
            try{
                System system = classes.parallelStream().map(c -> c.getAnnotation(System.class)).skip(classes.size() - 1).findFirst().get();
    
                return system.defaultSkin();
            }
            catch(Throwable e){
            }
        }
    
        return UIConstants.DEFAULT_SKIN;
    }
    
    public static Collection<String> getAvailableSkins(){
        Set<Class<?>> classes = ReflectionUtil.getTypesAnnotatedWith(System.class);
    
        if(classes != null && !classes.isEmpty()){
            try{
                System system = classes.parallelStream().map(c -> c.getAnnotation(System.class)).skip(classes.size() - 1).findFirst().get();
            
                return Arrays.asList(system.availableSkins());
            }
            catch(Throwable e){
            }
        }
    
        return null;
    }
}