package br.com.concepting.framework.constants;

/**
 * Class that defines the constants used in the manipulation of the project.
 *
 * @author fvilarinho
 * @since 3.1.0
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
public abstract class ProjectConstants{
    public static final String BUILD_ATTRIBUTE_ID = "build";
    public static final String BUILD_DIR_ATTRIBUTE_ID = "buildDir";
    public static final String BUILD_NAME_ATTRIBUTE_ID = "buildName";
    public static final String BUILD_VERSION_ATTRIBUTE_ID = "buildVersion";
    public static final String PERSISTENCE_MAPPING_ATTRIBUTE_ID = "hibernate-mapping";
    public static final String DEFAULT_ACTION_CLASS_TEMPLATE_FILE_ID = "actionClass.xml";
    public static final String DEFAULT_ACTION_FORM_CLASS_TEMPLATE_FILE_ID = "actionFormClass.xml";
    public static final String DEFAULT_RESOURCES_TEMPLATE_FILE_ID = "resources.xml";
    public static final String DEFAULT_SOURCE_DIR = "src/";
    public static final String DEFAULT_JAVA_DIR = DEFAULT_SOURCE_DIR.concat("main/java/");
    public static final String DEFAULT_RESOURCES_DIR = DEFAULT_SOURCE_DIR.concat("main/resources/");
    public static final String DEFAULT_UI_DIR = DEFAULT_SOURCE_DIR.concat("main/ui/");
    public static final String DEFAULT_JAVA_FILE_EXTENSION = ".java";
    public static final String DEFAULT_MODEL_CLASS_TEMPLATE_FILE_ID = "modelClass.xml";
    public static final String DEFAULT_MODEL_CLASS_TEST_TEMPLATE_FILE_ID = "modelClassTest.xml";
    public static final String DEFAULT_MODULE_DESCRIPTORS_DIR = "WEB-INF";
    public static final String DEFAULT_MODULE_MAPPINGS_FILE_ID = DEFAULT_RESOURCES_DIR.concat(DEFAULT_MODULE_DESCRIPTORS_DIR).concat("/web.xml");
    public static final String DEFAULT_MODULE_MAPPING_TEMPLATE_FILE_ID = "moduleMapping.xml";
    public static final String DEFAULT_PERSISTENCE_CLASS_TEMPLATE_FILE_ID = "persistenceClass.xml";
    public static final String DEFAULT_PERSISTENCE_DATA_TEMPLATE_FILE_ID = "persistenceData.xml";
    public static final String DEFAULT_PERSISTENCE_DIR = DEFAULT_SOURCE_DIR.concat("sql/");
    public static final String DEFAULT_PERSISTENCE_INTERFACE_TEMPLATE_FILE_ID = "persistenceInterface.xml";
    public static final String DEFAULT_PERSISTENCE_MAPPING_DTD_PUBLIC_ID = "-//Hibernate/Hibernate Mapping DTD 3.0//EN";
    public static final String DEFAULT_PERSISTENCE_MAPPING_DTD_SYSTEM_ID = "http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd";
    public static final String DEFAULT_PERSISTENCE_MAPPING_TEMPLATE_FILE_ID = "persistenceMapping.xml";
    public static final String DEFAULT_PERSISTENCE_STRUCTURE_TEMPLATE_FILE_ID = "persistenceStructure.xml";
    public static final String DEFAULT_REPORTS_DIR = DEFAULT_SOURCE_DIR.concat("reports/");
    public static final String DEFAULT_SERVICE_CLASS_TEMPLATE_FILE_ID = "serviceClass.xml";
    public static final String DEFAULT_SERVICE_INTERFACE_TEMPLATE_FILE_ID = "serviceInterface.xml";
    public static final String DEFAULT_TEMPLATES_DIR = DEFAULT_SOURCE_DIR.concat("templates/");
    public static final String DEFAULT_TESTS_DIR = DEFAULT_SOURCE_DIR.concat("tests/java/");
    public static final String DEFAULT_UI_PAGE_FILE_ID = "index.jsp";
    public static final String DEFAULT_UI_PAGE_TEMPLATE_FILE_ID = "uiPage.xml";
    public static final String DEFAULT_UI_PAGES_DIR = DEFAULT_MODULE_DESCRIPTORS_DIR.concat("/jsp/");
    public static final String DEFAULT_WEB_SERVICE_CLASS_TEMPLATE_FILE_ID = "webServiceClass.xml";
    public static final String DEFAULT_WEB_SERVICE_INTERFACE_TEMPLATE_FILE_ID = "webServiceInterface.xml";
}