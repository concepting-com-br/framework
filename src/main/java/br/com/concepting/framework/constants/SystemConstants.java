package br.com.concepting.framework.constants;

import br.com.concepting.framework.resources.constants.ResourcesConstants;

/**
 * Class that defines the constants commonly used.
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
public abstract class SystemConstants{
    public static final String CURRENT_EXCEPTION_ATTRIBUTE_ID = "currentException";
    public static final String CURRENT_LANGUAGE_ATTRIBUTE_ID = "currentLanguage";
    public static final String CURRENT_SKIN_ATTRIBUTE_ID = "currentSkin";
    public static final String ENCODING_ATTRIBUTE_ID = "acceptCharset";
    public static final String ERROR_ID_ATTRIBUTE_ID = "errorId";
    public static final String ERROR_TRACE_ATTRIBUTE_ID = "errorTrace";
    public static final String FORM_ATTRIBUTE_ID = "form";
    public static final String FORMS_ATTRIBUTE_ID = "forms";
    public static final String OBJECTS_ATTRIBUTE_ID = "objects";
    public static final String LANGUAGE_ATTRIBUTE_ID = "language";
    public static final String LANGUAGES_ATTRIBUTE_ID = "languages";
    public static final String MAIN_CONSOLE_ATTRIBUTE_ID = "mainConsole";
    public static final String OPERATING_SYSTEM_ATTRIBUTE_ID = "os";
    public static final String OPERATING_SYSTEMS_ATTRIBUTE_ID = "oss";
    public static final String PACKAGES_PREFIX_ATTRIBUTE_ID = "packagesPrefix";
    public static final String REQUEST_PARAMETERS_ATTRIBUTE_ID = "requestParameters";
    public static final String REQUEST_USER_AGENT_ATTRIBUTE_ID = "User-Agent";
    public static final String REQUEST_TRUE_CLIENT_IP_ATTRIBUTE_ID = "True-Client-IP";
    public static final String REQUEST_CONNECTING_IP_ATTRIBUTE_ID = "CF-Connecting-IP";
    public static final String REQUEST_ACCEPT_LANGUAGE_ATTRIBUTE_ID = "Accept-Language";
    public static final String RESOURCES_ATTRIBUTE_ID = "systemResources";
    public static final String SKINS_ATTRIBUTE_ID = "skins";
    public static final String SYSTEM_MODULE_ATTRIBUTE_ID = "systemModule";
    public static final String SYSTEM_SESSION_ATTRIBUTE_ID = "systemSession";
    public static final String URL_ATTRIBUTE_ID = "url";
    public static final String URLS_ATTRIBUTE_ID = "urls";
    public static final String EXCLUSION_URLS_ATTRIBUTE_ID = "exclusionUrls";
    public static final String DEFAULT_CONTROLLER_ID = "controller";
    public static final String DEFAULT_RESOURCES_ID = ResourcesConstants.DEFAULT_RESOURCES_DIR.concat("systemResources.xml");
}