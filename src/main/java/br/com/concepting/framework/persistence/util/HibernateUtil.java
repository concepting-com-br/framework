package br.com.concepting.framework.persistence.util;

import br.com.concepting.framework.caching.CachedObject;
import br.com.concepting.framework.caching.Cacher;
import br.com.concepting.framework.caching.CacherManager;
import br.com.concepting.framework.constants.Constants;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.BaseModel;
import br.com.concepting.framework.model.annotations.Model;
import br.com.concepting.framework.model.exceptions.ItemAlreadyExistsException;
import br.com.concepting.framework.model.exceptions.ItemNotFoundException;
import br.com.concepting.framework.persistence.constants.PersistenceConstants;
import br.com.concepting.framework.persistence.resources.PersistenceResources;
import br.com.concepting.framework.persistence.types.RepositoryType;
import br.com.concepting.framework.resources.FactoryResources;
import br.com.concepting.framework.util.PropertyUtil;
import br.com.concepting.framework.util.ReflectionUtil;
import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.Cache;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.*;
import org.hibernate.jdbc.Work;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class responsible to manipulate the persistence service with Hibernate.
 *
 * @author fvilarinho
 * @since 1.0.0
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
public class HibernateUtil{
    /**
     * Clears the persistence cache.
     *
     * @param persistenceResources Instance that contains the persistence service
     * resources.
     * @throws InternalErrorException Occurs when was not possible to establish
     * the connection.
     */
    public static void clearSessionCache(PersistenceResources persistenceResources) throws InternalErrorException{
        SessionFactory sessionFactory = buildSessionFactory(persistenceResources);
        
        if(sessionFactory != null && !sessionFactory.isClosed()){
            Cache cache = sessionFactory.getCache();
            
            if(cache != null){
                cache.evictAllRegions();
                cache.evictCollectionData();
                cache.evictQueryRegions();
            }
        }
    }
    
    /**
     * Establishes the connection with the persistence service using the default
     * resources.
     *
     * @return Instance that contains the persistence connection.
     * @throws InternalErrorException Occurs when was not possible to establish
     * the connection.
     */
    public static Session getSession() throws InternalErrorException{
        return getSession(PersistenceUtil.getDefaultPersistenceResources());
    }
    
    /**
     * Establishes the connection with the persistence service.
     *
     * @param persistenceResources Instance that contains the persistence service
     * resources.
     * @return Instance that contains the persistence connection.
     * @throws InternalErrorException Occurs when was not possible to establish
     * the connection.
     */
    public static Session getSession(PersistenceResources persistenceResources) throws InternalErrorException{
        SessionFactory sessionFactory = buildSessionFactory(persistenceResources);
        Session session = sessionFactory.getCurrentSession();
        
        if(session == null || !session.isOpen())
            session = sessionFactory.openSession();
        
        return session;
    }
    
    /**
     * Returns the established connection with the persistence service.
     *
     * @return Instance that contains the persistence connection.
     * @throws InternalErrorException Occurs when was not possible to get the
     * connection.
     */
    public static Connection getConnection() throws InternalErrorException{
        return getConnection(PersistenceUtil.getDefaultPersistenceResources());
    }
    
    /**
     * Establishes the connection with the persistence service.
     *
     * @param persistenceResources Instance that contains the persistence service
     * resources.
     * @return Instance that contains the persistence connection.
     * @throws InternalErrorException Occurs when was not possible to establish
     * the connection.
     */
    public static Connection getConnection(PersistenceResources persistenceResources) throws InternalErrorException{
        Session session = getSession(persistenceResources);
        
        class ConnectionGetter implements Work{
            private Connection connection = null;
            
            /**
             * Returns the established connection with the persistence service.
             *
             * @return Instance that contains the persistence connection.
             */
            public Connection getConnection(){
                return this.connection;
            }
            
            /**
             * Defines the established connection with the persistence service.
             *
             * @param connection Instance that contains the persistence connection.
             */
            public void setConnection(Connection connection){
                this.connection = connection;
            }
            
            /**
             * @see org.hibernate.jdbc.Work#execute(java.sql.Connection)
             */
            public void execute(Connection connection) throws SQLException{
                setConnection(connection);
            }
        }
        
        ConnectionGetter connectionGetter = new ConnectionGetter();
        
        session.doWork(connectionGetter);
        
        return connectionGetter.getConnection();
    }
    
    /**
     * Returns the persistence session dialect.
     *
     * @param persistenceResources Instance that contains the persistence resources.
     * @return Instance that contains the persistence session dialect.
     */
    public static Class<? extends Dialect> getSessionDialect(PersistenceResources persistenceResources){
        FactoryResources persistenceFactoryResources = persistenceResources.getFactoryResources();
        RepositoryType repositoryType = RepositoryType.valueOf(persistenceFactoryResources.getType().toUpperCase());
        
        if(repositoryType == RepositoryType.MYSQL)
            return MariaDBDialect.class;
        else if(repositoryType == RepositoryType.SYBASE)
            return SybaseASE15Dialect.class;
        else if(repositoryType == RepositoryType.MSSQL)
            return SQLServerDialect.class;
        else if(repositoryType == RepositoryType.ORACLE)
            return Oracle10gDialect.class;
        else if(repositoryType == RepositoryType.POSTGRESQL)
            return PostgresPlusDialect.class;
        else if(repositoryType == RepositoryType.DB2)
            return DB2Dialect.class;
        else if(repositoryType == RepositoryType.FIREBIRD)
            return FirebirdDialect.class;
        
        return Dialect.class;
    }
    
    /**
     * Build the persistence session properties.
     *
     * @param persistenceResources Instance that contains the persistence resources.
     * @return Map that contains the persistence session properties.
     */
    public static Map<String, Object> buildSessionProperties(PersistenceResources persistenceResources){
        Map<String, Object> hibernateSessionProperties = PropertyUtil.instantiate(Constants.DEFAULT_MAP_CLASS);
        
        hibernateSessionProperties.put(Environment.DIALECT, getSessionDialect(persistenceResources).getName());
        
        FactoryResources persistenceFactoryResources = persistenceResources.getFactoryResources();
        String persistenceFactoryClass = persistenceFactoryResources.getClazz();
        String persistenceFactoryUrl = persistenceFactoryResources.getUri();
        String persistenceUserName = persistenceResources.getUserName();
        String persistencePassword = persistenceResources.getPassword();
        
        persistenceFactoryUrl = PropertyUtil.fillPropertiesInString(persistenceResources, persistenceFactoryUrl);
        
        BasicDataSource datasource = new BasicDataSource();
        
        datasource.setDriverClassName(persistenceFactoryClass);
        datasource.setUrl(persistenceFactoryUrl);
        
        if(persistenceUserName != null && persistenceUserName.length() > 0)
            datasource.setUsername(persistenceUserName);
        
        if(persistencePassword != null && persistencePassword.length() > 0)
            datasource.setPassword(persistencePassword);
        
        hibernateSessionProperties.put(Environment.DATASOURCE, datasource);
        hibernateSessionProperties.put(Environment.GLOBALLY_QUOTED_IDENTIFIERS, true);
        hibernateSessionProperties.put(Environment.WRAP_RESULT_SETS, true);
        hibernateSessionProperties.put(Environment.USE_REFLECTION_OPTIMIZER, true);
        hibernateSessionProperties.putAll(persistenceResources.getOptions());
        
        return hibernateSessionProperties;
    }
    
    /**
     * Establishes the connection with the persistence service.
     *
     * @param persistenceResources Instance that contains the persistence service
     * resources.
     * @return Instance that contains the persistence connection.
     * @throws InternalErrorException Occurs when was not possible to establish
     * the connection.
     */
    private static SessionFactory buildSessionFactory(PersistenceResources persistenceResources) throws InternalErrorException{
        Cacher<SessionFactory> cacher = CacherManager.getInstance().getCacher(HibernateUtil.class);
        CachedObject<SessionFactory> object = null;
        SessionFactory sessionFactory = null;
        
        try{
            object = cacher.get(persistenceResources.getId());
            sessionFactory = object.getContent();
        }
        catch(ItemNotFoundException e){
            MetadataSources sources = new MetadataSources(new StandardServiceRegistryBuilder().applySettings(buildSessionProperties(persistenceResources)).build());
            Set<Class<?>> classes = ReflectionUtil.getTypesAnnotatedWith(Model.class);
            
            if(classes != null && !classes.isEmpty()){
                Collection<Class<? extends BaseModel>> mappings = classes.parallelStream().map(c -> (Class<? extends BaseModel>)c).collect(Collectors.toList());
            
                if(mappings != null && !mappings.isEmpty()){
                    Model modelAnnotation = null;
                    InputStream persistenceMappingStream = null;
                    StringBuilder persistenceMappingName = null;
                
                    for(Class<? extends BaseModel> mapping : mappings){
                        modelAnnotation = mapping.getAnnotation(Model.class);
                        
                        if(modelAnnotation.mappedRepositoryId() != null && modelAnnotation.mappedRepositoryId().length() > 0 && (modelAnnotation.persistenceResourcesId() == null || modelAnnotation.persistenceResourcesId().length() == 0 || modelAnnotation.persistenceResourcesId().equals(persistenceResources.getId()))){
                            if(persistenceMappingName == null)
                                persistenceMappingName = new StringBuilder();
                            else
                                persistenceMappingName.delete(0, persistenceMappingName.length());
                            
                            persistenceMappingName.append(PersistenceConstants.DEFAULT_MAPPINGS_DIR);
                            persistenceMappingName.append(mapping.getName());
                            persistenceMappingName.append(PersistenceConstants.DEFAULT_MAPPING_FILE_EXTENSION);
                            
                            persistenceMappingStream = HibernateUtil.class.getClassLoader().getResourceAsStream(persistenceMappingName.toString());
                            
                            if(persistenceMappingStream != null)
                                sources.addInputStream(persistenceMappingStream);
                        }
                    }
                }
            }
            
            Metadata metadata = sources.getMetadataBuilder().build();
            
            sessionFactory = metadata.getSessionFactoryBuilder().build();
            
            object = new CachedObject<SessionFactory>();
            object.setId(persistenceResources.getId());
            object.setContent(sessionFactory);
            
            try{
                cacher.add(object);
            }
            catch(ItemAlreadyExistsException e1){
            }
        }
        
        return sessionFactory;
    }
}