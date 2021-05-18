package br.com.concepting.framework.util;

import br.com.concepting.framework.caching.CachedObject;
import br.com.concepting.framework.caching.Cacher;
import br.com.concepting.framework.caching.CacherManager;
import br.com.concepting.framework.model.exceptions.ItemAlreadyExistsException;
import br.com.concepting.framework.model.exceptions.ItemNotFoundException;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.lang.annotation.Annotation;
import java.util.Set;

@SuppressWarnings("unchecked")
public class ReflectionUtil{
    private static Cacher cacher = CacherManager.getInstance().getCacher(ReflectionUtil.class);
    
    public static Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation){
        Set<Class<?>> types = null;
        
        try{
            CachedObject object = cacher.get(annotation.getName());
            
            types = (Set<Class<?>>)object.getContent();
        }
        catch(ItemNotFoundException e){
            Reflections reflections = new Reflections("", new TypeAnnotationsScanner(), new SubTypesScanner());
            
            types = reflections.getTypesAnnotatedWith(annotation);
            
            CachedObject object = new CachedObject();
            
            object.setId(annotation.getName());
            object.setContent(types);
            
            try{
                cacher.add(object);
            }
            catch(ItemAlreadyExistsException e1){
            }
        }
    
        return types;
    }
}