package br.com.concepting.framework.persistence.constants;

import br.com.concepting.framework.persistence.types.RepositoryType;
import br.com.concepting.framework.resources.constants.ResourcesConstants;

import java.util.Arrays;
import java.util.List;

/**
 * Class that defines the constants used in the persistence of data models
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
public abstract class PersistenceConstants{
    public static final String CACHEABLE_QUERY_ATTRIBUTE_ID = "org.hibernate.cacheable";
    public static final String CLOSE_QUOTE_ATTRIBUTE_ID = "closeQuote";
    public static final String FACTORY_CLASS_NAME_ATTRIBUTE_ID = "driverClassName";
    public static final String OPEN_QUOTE_ATTRIBUTE_ID = "openQuote";
    public static final String RESOURCES_ATTRIBUTE_ID = "persistenceResources";
    public static final String QUERY_MAXIMUM_RESULTS_ATTRIBUTE_ID = "hibernate.query_maximum_results";
    public static final String INSTANCE_ATTRIBUTE_ID = "instanceId";
    public static final String REPOSITORY_ATTRIBUTE_ID = "repositoryId";
    public static final String DEFAULT_FILE_EXTENSION = ".sql";
    public static final String DEFAULT_ID = "persistence";
    public static final String DEFAULT_IMPLEMENTATION_ID = "persistenceImpl";
    public static final Integer DEFAULT_MAXIMUM_RESULTS = 512;
    public static final String DEFAULT_MAPPINGS_DIR = ResourcesConstants.DEFAULT_RESOURCES_DIR.concat("mappings/");
    public static final String DEFAULT_MAPPING_FILE_EXTENSION = ".hbm.xml";
    public static final RepositoryType DEFAULT_REPOSITORY_TYPE = RepositoryType.MYSQL;
    public static final String DEFAULT_RESOURCES_ID = ResourcesConstants.DEFAULT_RESOURCES_DIR.concat("persistenceResources.xml");
    public static final List<String> DEFAULT_COLLECTION_TYPES_IDS = Arrays.asList("org.hibernate.collection.internal.AbstractPersistentCollection", "org.hibernate.collection.spi.PersistentCollection", "org.hibernate.collection.internal.PersistentBag");
    public static final String DEFAULT_ENUM_TYPE_ID = "org.hibernate.type.EnumType";
    public static final String DEFAULT_BINARY_TYPE_ID = "binary";
    public static final String DEFAULT_BOOLEAN_TYPE_ID = "boolean";
    public static final String DEFAULT_DATE_TIME_TYPE_ID = "br.com.concepting.framework.persistence.types.DateTimeType";
    public static final String DEFAULT_DATE_TYPE_ID = "date";
    public static final String DEFAULT_TIME_TYPE_ID = "time";
    public static final String DEFAULT_BIG_DECIMAL_TYPE_ID = "big_decimal";
    public static final String DEFAULT_BIG_INTEGER_TYPE_ID = "big_integer";
    public static final String DEFAULT_BYTE_TYPE_ID = "byte";
    public static final String DEFAULT_CURRENCY_TYPE_ID = "currency";
    public static final String DEFAULT_DOUBLE_TYPE_ID = "double";
    public static final String DEFAULT_FLOAT_TYPE_ID = "float";
    public static final String DEFAULT_INTEGER_TYPE_ID = "integer";
    public static final String DEFAULT_LONG_TYPE_ID = "long";
    public static final String DEFAULT_SHORT_TYPE_ID = "short";
    public static final String DEFAULT_STRING_TYPE_ID = "string";
    public static final String DEFAULT_OBJECT_TYPE_ID = "serializable";
}