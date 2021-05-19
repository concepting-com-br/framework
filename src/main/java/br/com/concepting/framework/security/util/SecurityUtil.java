package br.com.concepting.framework.security.util;

import br.com.concepting.framework.constants.SystemConstants;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.SystemModuleModel;
import br.com.concepting.framework.model.SystemSessionModel;
import br.com.concepting.framework.model.helpers.ModelInfo;
import br.com.concepting.framework.model.util.ModelUtil;
import br.com.concepting.framework.security.annotations.Security;
import br.com.concepting.framework.security.constants.SecurityConstants;
import br.com.concepting.framework.security.model.LoginParameterModel;
import br.com.concepting.framework.security.model.LoginSessionModel;
import br.com.concepting.framework.security.model.UserModel;
import br.com.concepting.framework.security.util.interfaces.ICrypto;
import br.com.concepting.framework.util.ByteUtil;
import br.com.concepting.framework.util.ReflectionUtil;
import br.com.concepting.framework.util.StringUtil;
import br.com.concepting.framework.util.helpers.PropertyInfo;
import org.apache.commons.beanutils.ConstructorUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

/**
 * Class that defines utility routines for security.
 *
 * @author fvilarinho
 * @since 3.3.0
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
public class SecurityUtil{
    private static final SecureRandom secureRandom = new SecureRandom();
    
    public static Class<? extends LoginSessionModel> getLoginSessionClass(){
        Set<Class<?>> classes = ReflectionUtil.getTypesAnnotatedWith(Security.class);
    
        if(classes != null && !classes.isEmpty()){
            try{
                return classes.parallelStream().map(c -> (Class<? extends LoginSessionModel>) c).findFirst().get();
            }
            catch(Throwable e){
            }
        }
        
        return LoginSessionModel.class;
    }
    
    public static Integer getLoginSessionTimeout(){
        Set<Class<?>> classes = ReflectionUtil.getTypesAnnotatedWith(Security.class);
    
        if(classes != null && !classes.isEmpty()){
            try{
                Security security = classes.parallelStream().map(c -> c.getAnnotation(Security.class)).findFirst().get();
    
                return security.loginSessionTimeout();
            }
            catch(Throwable e){
            }
        }
        
        return SecurityConstants.DEFAULT_LOGIN_SESSION_TIMEOUT;
    }
    
    public static String getCryptoAlgorithm(){
        Set<Class<?>> classes = ReflectionUtil.getTypesAnnotatedWith(Security.class);
    
        if(classes != null && !classes.isEmpty()){
            try{
                Security security = classes.parallelStream().map(c -> c.getAnnotation(Security.class)).findFirst().get();
            
                return security.cryptoAlgorithm();
            }
            catch(Throwable e){
            }
        }
    
        return SecurityConstants.DEFAULT_CRYPTO_ALGORITHM_ID;
    }
    
    public static Integer getCryptoKeySize(){
        Set<Class<?>> classes = ReflectionUtil.getTypesAnnotatedWith(Security.class);
        
        if(classes != null && !classes.isEmpty()){
            try{
                Security security = classes.parallelStream().map(c -> c.getAnnotation(Security.class)).findFirst().get();
                
                return security.cryptoKeySIze();
            }
            catch(Throwable e){
            }
        }
        
        return SecurityConstants.DEFAULT_CRYPTO_KEY_SIZE;
    }

    /**
     * Generated a secure token.
     *
     * @return String that contains the secure token.
     */
    public static String generateToken(){
        byte[] randomBytes = new byte[SecurityConstants.DEFAULT_TOKEN_SIZE];
        
        secureRandom.nextBytes(randomBytes);
        
        try{
            return ByteUtil.toBase64(randomBytes);
        }
        catch(UnsupportedEncodingException e){
            return generateBasicToken();
        }
    }
    
    /**
     * Indicates if a password is strong.
     *
     * @param password String that contains the password.
     * @return True/False.
     */
    public static Boolean isStrongPassword(String password){
        if(password == null)
            return false;
        
        if(!password.matches(".*[0-9].*"))
            return false;
        
        if(!password.matches(".*[a-z].*"))
            return false;
        
        if(!password.matches(".*[A-Z].*"))
            return false;
        
        return password.matches(".*[\\!|\\@|\\#|\\$|\\%|\\ˆ|\\&|\\*|\\(|\\)|\\_|\\+|\\-|\\=|\\[|\\]\\|\\|\\{|\\}\\||\\;|\\'|\\:|\\\"|\\,|\\.|\\/|\\<|\\>|\\?].*");
    }
    
    /**
     * Encrypts a password.
     *
     * @param password String that contains the decrypted password.
     * @return String that contains the encrypted password.
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    public static String encryptPassword(String password) throws InternalErrorException{
        try{
            String cryptographyAlgorithm = getCryptoAlgorithm();
            Integer cryptographyKeySize = getCryptoKeySize();
            CryptoDigest digest = new CryptoDigest(true);
            String cryptographyKey = digest.encrypt(password);
            ICrypto crypto = CryptoFactory.getInstance(cryptographyAlgorithm, cryptographyKey, cryptographyKeySize, true);
            
            password = crypto.encrypt(password);
            
            return password;
        }
        catch(IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Returns the operating system user name.
     *
     * @return String that contains the user name.
     */
    public static String getSystemUserName(){
        return System.getProperty("user.name");
    }
    
    /**
     * Generates a basic token.
     *
     * @return String that contains a basic token.
     */
    public static String generateBasicToken(){
        String token = StringUtil.replaceAll(UUID.randomUUID().toString(), "-", "");
        
        return token;
    }
    
    /**
     * Instantiate the login session data model based on the security resources.
     *
     * @param <L> Class that defines the data model.
     * @return Instance that contains the login session data model.
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    public static <L extends LoginSessionModel> L getLoginSession() throws InternalErrorException{
        return getLoginSession(null, null);
    }
    
    /**
     * Instantiate the login session data model based on the security resources.
     *
     * @param <L> Class that defines the data model.
     * @param resourcesDirname String that contains the resources directory.
     * @return Instance that contains the login session data model.
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    public static <L extends LoginSessionModel> L getLoginSession(String resourcesDirname) throws InternalErrorException{
        return getLoginSession(resourcesDirname, null);
    }
    
    /**
     * Instantiate the login session data model based on the security resources.
     *
     * @param <L> Class that defines the data model.
     * @param loginSession Instance of the login session data model.
     * @return Instance that contains the login session data model.
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    public static <L extends LoginSessionModel> L getLoginSession(L loginSession) throws InternalErrorException{
        return getLoginSession(null, loginSession);
    }
    
    /**
     * Instantiate the login session data model based on the security resources.
     *
     * @param <L> Class that defines the data model.
     * @param resourcesDirname String that contains the resources directory.
     * @param loginSession Instance of the login session data model.
     * @return Instance that contains the login session data model.
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    @SuppressWarnings("unchecked")
    public static <L extends LoginSessionModel> L getLoginSession(String resourcesDirname, L loginSession) throws InternalErrorException{
        try{
            if(loginSession == null)
                loginSession = (L) ConstructorUtils.invokeConstructor(getLoginSessionClass(), null);
            
            UserModel user = loginSession.getUser();
            Class<L> loginSessionClass = (Class<L>) loginSession.getClass();
            ModelInfo modelInfo = ModelUtil.getInfo(loginSessionClass);
            
            if(user == null){
                try{
                    PropertyInfo propertyInfo = modelInfo.getPropertyInfo(SecurityConstants.USER_ATTRIBUTE_ID);
                    
                    user = (UserModel) ConstructorUtils.invokeConstructor(propertyInfo.getClazz(), null);
                }
                catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchFieldException e){
                    user = new UserModel();
                }
                
                LoginParameterModel loginParameter = user.getLoginParameter();
                
                if(loginParameter == null){
                    try{
                        ModelInfo userModelInfo = ModelUtil.getInfo(user.getClass());
                        PropertyInfo propertyInfo = userModelInfo.getPropertyInfo(SecurityConstants.LOGIN_PARAMETER_ATTRIBUTE_ID);
                        
                        loginParameter = (LoginParameterModel) ConstructorUtils.invokeConstructor(propertyInfo.getClazz(), null);
                    }
                    catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchFieldException e){
                        loginParameter = new LoginParameterModel();
                    }
                    
                    user.setLoginParameter(loginParameter);
                }
                
                loginSession.setUser(user);
            }
            
            SystemModuleModel systemModule = loginSession.getSystemModule();
            
            if(systemModule == null){
                try{
                    PropertyInfo propertyInfo = modelInfo.getPropertyInfo(SystemConstants.SYSTEM_MODULE_ATTRIBUTE_ID);
                    
                    systemModule = (SystemModuleModel) ConstructorUtils.invokeConstructor(propertyInfo.getClazz(), null);
                }
                catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchFieldException e){
                    systemModule = new SystemModuleModel();
                }
                
                loginSession.setSystemModule(systemModule);
            }
            
            SystemSessionModel systemSession = loginSession.getSystemSession();
            
            if(systemSession == null){
                try{
                    PropertyInfo propertyInfo = modelInfo.getPropertyInfo(SystemConstants.SYSTEM_SESSION_ATTRIBUTE_ID);
                    
                    systemSession = (SystemSessionModel) ConstructorUtils.invokeConstructor(propertyInfo.getClazz(), null);
                }
                catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchFieldException e){
                    systemSession = new SystemSessionModel();
                }
                
                loginSession.setSystemSession(systemSession);
            }
            
            return loginSession;
        }
        catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException | IllegalArgumentException | NoSuchFieldException e){
            throw new InternalErrorException(e);
        }
    }
}