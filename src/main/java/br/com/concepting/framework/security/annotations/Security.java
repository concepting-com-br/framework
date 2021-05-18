package br.com.concepting.framework.security.annotations;

import br.com.concepting.framework.security.constants.SecurityConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Security{
    int loginSessionTimeout() default SecurityConstants.DEFAULT_LOGIN_SESSION_TIMEOUT;
    String cryptoAlgorithm() default SecurityConstants.DEFAULT_CRYPTO_ALGORITHM_ID;
    int cryptoKeySIze() default SecurityConstants.DEFAULT_CRYPTO_KEY_SIZE;
}