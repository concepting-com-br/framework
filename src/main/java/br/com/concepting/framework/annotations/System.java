package br.com.concepting.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface System{
    String defaultLanguage() default "en_US";
    String defaultSkin() default "default";
    String[] availableLanguages() default {"en_US", "es", "pt_BR"};
    String[] availableSkins() default {"default"};
}