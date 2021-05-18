package br.com.concepting.framework.security.constants;

import br.com.concepting.framework.resources.constants.ResourcesConstants;

/**
 * Class that defines the constants used in the security routines.
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
@SuppressWarnings("javadoc")
public abstract class SecurityConstants{
    public static final String ACCESSES_ATTRIBUTE_ID = "accesses";
    public static final String AUTHENTICATION_TYPE_ATTRIBUTE_ID = "authenticationType";
    public static final String GROUPS_ATTRIBUTE_ID = "groups";
    public static final String LOGIN_PARAMETER_ATTRIBUTE_ID = "loginParameter";
    public static final String LOGIN_SESSION_ATTRIBUTE_ID = "loginSession";
    public static final String PASSWORD_ATTRIBUTE_ID = "password";
    public static final String REMEMBER_USER_AND_PASSWORD_ATTRIBUTE_ID = "rememberUserAndPassword";
    public static final String RESOURCES_ATTRIBUTE_ID = "securityResources";
    public static final String SECURITY_FILTER_ATTRIBUTE_ID = "securityFilter";
    public static final String USER_ATTRIBUTE_ID = "user";
    public static final String USERS_ATTRIBUTE_ID = "users";
    public static final String USER_NAME_ATTRIBUTE_ID = "userName";
    public static final String DEFAULT_CHANGE_PASSWORD_ID = "changePassword";
    public static final String DEFAULT_CRYPTO_3DES_ALGORITHM_ID = "DESede";
    public static final int DEFAULT_CRYPTO_3DES_KEY_SIZE = 168;
    public static final String DEFAULT_CRYPTO_AES_ALGORITHM_ID = "AES";
    public static final int DEFAULT_CRYPTO_AES_KEY_SIZE = 256;
    public static final String DEFAULT_CRYPTO_DES_ALGORITHM_ID = "SHA-256";
    public static final int DEFAULT_CRYPTO_DES_KEY_SIZE = 56;
    public static final String DEFAULT_CRYPTO_ALGORITHM_ID = DEFAULT_CRYPTO_AES_ALGORITHM_ID;
    public static final int DEFAULT_CRYPTO_KEY_SIZE = DEFAULT_CRYPTO_AES_KEY_SIZE;
    public static final int DEFAULT_TOKEN_SIZE = 32;
    public static final String DEFAULT_DIGEST_ALGORITHM_ID = "MD5";
    public static final String DEFAULT_LOAD_CHANGE_PASSWORD_ID = "loadChangePassword";
    public static final String DEFAULT_LOAD_FORGOT_PASSWORD_ID = "loadForgotPassword";
    public static final String DEFAULT_LOGIN_ID = "logIn";
    public static final int DEFAULT_LOGIN_SESSION_TIMEOUT = 1440;
    public static final String DEFAULT_LOGOUT_ID = "logOut";
    public static final int DEFAULT_MINIMUM_PASSWORD_LENGTH = 8;
    public static final String DEFAULT_REMEMBER_USER_AND_PASSWORD_ID = "rememberUserAndPassword";
    public static final String DEFAULT_RESOURCES_ID = ResourcesConstants.DEFAULT_RESOURCES_DIR.concat("securityResources.xml");
    public static final String DEFAULT_SEND_FORGOTTEN_PASSWORD_ID = "sendForgottenPassword";
    public static final String DEFAULT_SEND_FORGOTTEN_PASSWORD_MESSAGE_ID = DEFAULT_SEND_FORGOTTEN_PASSWORD_ID.concat("Message");
}