package br.com.concepting.framework.controller.form.annotations;

import br.com.concepting.framework.controller.form.constants.ActionFormConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ActionForm{
    String name() default "";
    String action() default "";
    Forward[] forwards() default {@Forward(name=ActionFormConstants.DEFAULT_FORWARD_ID, isDefault = true, url="index.jsp"),
                                  @Forward(name=ActionFormConstants.DEFAULT_ROOT_FORWARD_ID, isDefault = false, url="/")};
}