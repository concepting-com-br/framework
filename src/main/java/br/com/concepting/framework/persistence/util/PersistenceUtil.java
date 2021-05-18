package br.com.concepting.framework.persistence.util;

import br.com.concepting.framework.constants.Constants;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.BaseModel;
import br.com.concepting.framework.model.constants.ModelConstants;
import br.com.concepting.framework.persistence.constants.PersistenceConstants;
import br.com.concepting.framework.persistence.interfaces.IPersistence;
import br.com.concepting.framework.persistence.resources.PersistenceResources;
import br.com.concepting.framework.persistence.resources.PersistenceResourcesLoader;
import br.com.concepting.framework.resources.exceptions.InvalidResourcesException;
import br.com.concepting.framework.util.FileUtil;
import br.com.concepting.framework.util.StringUtil;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.EnumSet;

/**
 * Class responsible to manipulate the persistence service.
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
public class PersistenceUtil{
    /**
     * Returns the default persistence resources.
     *
     * @return Instance that contains the persistence resources.
     * @throws InvalidResourcesException Occurs when was not load the persistence
     * resources.
     */
    public static PersistenceResources getDefaultPersistenceResources() throws InvalidResourcesException{
        PersistenceResourcesLoader loader = new PersistenceResourcesLoader();
        
        return loader.getDefault();
    }
    
    /**
     * Returns a specific persistence resources.
     *
     * @param persistenceResourcesId String that contains the identifier of the
     * persistence resources.
     * @return Instance that contains the persistence resources.
     * @throws InvalidResourcesException Occurs when was not load the persistence
     * resources.
     */
    public static PersistenceResources getPersistenceResources(String persistenceResourcesId) throws InvalidResourcesException{
        PersistenceResourcesLoader loader = new PersistenceResourcesLoader();
        
        return loader.get(persistenceResourcesId);
    }
    
    /**
     * Returns the class of a data model based on a persistence implementation.
     *
     * @param <D> Class that defines the persistence.
     * @param persistenceClass Class that defines the persistence.
     * @return Class that defines the data model.
     * @throws ClassNotFoundException Occurs when was not possible to execute
     * the operation.
     */
    @SuppressWarnings("unchecked")
    public static <D extends IPersistence<? extends BaseModel>> Class<? extends BaseModel> getModelClassByPersistence(Class<D> persistenceClass) throws ClassNotFoundException{
        if(persistenceClass != null){
            String modelClassId = StringUtil.replaceLast(persistenceClass.getName(), StringUtil.capitalize(PersistenceConstants.DEFAULT_IMPLEMENTATION_ID), StringUtil.capitalize(ModelConstants.DEFAULT_ID));
            
            modelClassId = StringUtil.replaceLast(modelClassId, StringUtil.capitalize(PersistenceConstants.DEFAULT_ID), StringUtil.capitalize(ModelConstants.DEFAULT_ID));
            modelClassId = StringUtil.replaceAll(modelClassId, ".".concat(PersistenceConstants.DEFAULT_ID), ".".concat(ModelConstants.DEFAULT_ID));
            
            return (Class<? extends BaseModel>) Class.forName(modelClassId);
        }
        
        return null;
    }
    
    /**
     * Returns the class name of the persistence implementation of a data model.
     *
     * @param modelClass Class that defines the data model.
     * @return String that contains the class name.
     */
    public static String getPersistenceClassNameByModel(Class<? extends BaseModel> modelClass){
        if(modelClass != null){
            String persistenceClassId = StringUtil.replaceLast(modelClass.getName(), StringUtil.capitalize(ModelConstants.DEFAULT_ID), StringUtil.capitalize(PersistenceConstants.DEFAULT_IMPLEMENTATION_ID));
            
            return StringUtil.replaceAll(persistenceClassId, ".".concat(ModelConstants.DEFAULT_ID), ".".concat(PersistenceConstants.DEFAULT_ID));
        }
        
        return null;
    }
    
    /**
     * Returns the name of the persistence implementation of a data model.
     *
     * @param modelClass Class that defines the data model.
     * @return String that contains the identifier.
     */
    public static String getPersistenceNameByModel(Class<? extends BaseModel> modelClass){
        if(modelClass != null)
            return StringUtil.replaceLast(modelClass.getSimpleName(), StringUtil.capitalize(ModelConstants.DEFAULT_ID), StringUtil.capitalize(PersistenceConstants.DEFAULT_IMPLEMENTATION_ID));
        
        return null;
    }
    
    /**
     * Returns the class of the persistence implementation of a data model.
     *
     * @param <D> Class that defines the persistence.
     * @param modelClass Class that defines the data model.
     * @return Instance that contains the persistence class.
     * @throws ClassNotFoundException Occurs when was not possible to execute
     * the operation.
     */
    @SuppressWarnings("unchecked")
    public static <D extends IPersistence<? extends BaseModel>> Class<D> getPersistenceClassByModel(Class<? extends BaseModel> modelClass) throws ClassNotFoundException{
        if(modelClass != null)
            return (Class<D>) Class.forName(getPersistenceClassNameByModel(modelClass));
        
        return null;
    }
    
    /**
     * Returns the interface class name of the persistence implementation of a
     * data model.
     *
     * @param modelClass Class that defines the data model.
     * @return String that contains the interface class name.
     */
    public static String getPersistenceInterfaceClassNameByModel(Class<? extends BaseModel> modelClass){
        if(modelClass != null){
            String persistenceClassId = StringUtil.replaceLast(modelClass.getName(), StringUtil.capitalize(ModelConstants.DEFAULT_ID), StringUtil.capitalize(PersistenceConstants.DEFAULT_ID));
            
            return StringUtil.replaceAll(persistenceClassId, ".".concat(ModelConstants.DEFAULT_ID), ".".concat(PersistenceConstants.DEFAULT_ID).concat(".").concat(Constants.DEFAULT_INTERFACES_ID));
        }
        
        return null;
    }
    
    /**
     * Returns the interface name of the persistence implementation of a data
     * model.
     *
     * @param modelClass Class that defines the data model.
     * @return String that contains the interface name.
     */
    public static String getPersistenceInterfaceNameByModel(Class<? extends BaseModel> modelClass){
        if(modelClass != null)
            return StringUtil.replaceLast(modelClass.getSimpleName(), StringUtil.capitalize(ModelConstants.DEFAULT_ID), StringUtil.capitalize(PersistenceConstants.DEFAULT_ID));
        
        return null;
    }
    
    /**
     * Returns the interface class of the persistence implementation of a data
     * model.
     *
     * @param <D> Class that defines the persistence.
     * @param modelClass Class that defines the data model.
     * @return Instance that contains the persistence interface class.
     * @throws ClassNotFoundException Occurs when was not possible to execute
     * the operation.
     */
    @SuppressWarnings("unchecked")
    public static <D extends IPersistence<? extends BaseModel>> Class<D> getPersistenceInterfaceClassByModel(Class<? extends BaseModel> modelClass) throws ClassNotFoundException{
        if(modelClass != null)
            return (Class<D>) Class.forName(getPersistenceInterfaceClassNameByModel(modelClass));
        
        return null;
    }
    
    /**
     * Returns the package name of the persistence implementation of a data
     * model.
     *
     * @param modelClass Class that defines the data model.
     * @return String that contains the package name.
     */
    public static String getPersistencePackageByModel(Class<? extends BaseModel> modelClass){
        if(modelClass != null)
            return StringUtil.replaceAll(modelClass.getPackage().getName(), ".".concat(ModelConstants.DEFAULT_ID), ".".concat(PersistenceConstants.DEFAULT_ID));
        
        return null;
    }
    
    /**
     * Returns the interface package name of the persistence implementation of a
     * data model.
     *
     * @param modelClass Class that defines the data model.
     * @return String that contains the interface package name.
     */
    public static String getPersistenceInterfacePackageByModel(Class<? extends BaseModel> modelClass){
        if(modelClass != null)
            return StringUtil.replaceAll(modelClass.getPackage().getName(), ".".concat(ModelConstants.DEFAULT_ID), ".".concat(PersistenceConstants.DEFAULT_ID).concat(".").concat(Constants.DEFAULT_INTERFACES_ID));
        
        return null;
    }
    
    public static String generateStructure(String resourcesDirname) throws InternalErrorException{
        try{
            String content = null;
            PersistenceResourcesLoader loader = new PersistenceResourcesLoader(resourcesDirname);
            PersistenceResources persistenceResources = loader.getDefault();
            
            if(persistenceResources != null){
                StringBuilder mappingsDirname = new StringBuilder();
                
                mappingsDirname.append(resourcesDirname);
                mappingsDirname.append(FileUtil.getDirectorySeparator());
                mappingsDirname.append(PersistenceConstants.DEFAULT_MAPPINGS_DIR);
                
                File mappingsDirectory = new File(mappingsDirname.toString());
                
                if(mappingsDirectory.exists()){
                    File[] mappings = mappingsDirectory.listFiles();
                    
                    if(mappings != null && mappings.length > 0){
                        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().applySetting(Environment.DIALECT, HibernateUtil.getSessionDialect(persistenceResources).getName()).applySetting(Environment.GLOBALLY_QUOTED_IDENTIFIERS, true).build();
                        MetadataSources sources = new MetadataSources(registry);
                        
                        for(File persistenceMapping: mappings)
                            sources.addFile(persistenceMapping.getAbsolutePath());
                        
                        StringBuilder destinationFilename = new StringBuilder();
                        
                        destinationFilename.append(FileUtil.getTempDirectoryPath());
                        destinationFilename.append(FileUtil.getDirectorySeparator());
                        destinationFilename.append(new SecureRandom().nextLong());
                        
                        File destinationFile = new File(destinationFilename.toString());
                        
                        if(destinationFile.exists())
                            destinationFile.delete();
                        
                        SchemaExport exporter = new SchemaExport();
                        
                        exporter.setDelimiter(";");
                        exporter.setFormat(true);
                        exporter.setOutputFile(destinationFilename.toString());
                        exporter.create(EnumSet.of(TargetType.SCRIPT), sources.buildMetadata());
                        
                        content = FileUtil.fromTextFile(destinationFilename.toString());
                        content = StringUtil.replaceAll(content, " engine=InnoDB", "");
                        
                        if(content != null && content.length() > 0){
                            StringBuilder contentBuffer = new StringBuilder();
                            int pos = content.toLowerCase().indexOf("create table");
                            
                            if(pos >= 0){
                                content = content.substring(pos);
                                
                                String indentation = StringUtil.replicate(Constants.DEFAULT_INDENT_CHARACTER, Constants.DEFAULT_INDENT_SIZE);
                                
                                if(indentation != null && indentation.length() > 0)
                                    contentBuffer.append(indentation);
                                
                                contentBuffer.append(content);
                            }
                            else
                                contentBuffer.append(content);
                            
                            content = contentBuffer.toString();
                        }
                    }
                }
            }
            
            return content;
        }
        catch(IOException | InvalidResourcesException e){
            throw new InternalErrorException(e);
        }
    }
}