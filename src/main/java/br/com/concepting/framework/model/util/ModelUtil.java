package br.com.concepting.framework.model.util;

import br.com.concepting.framework.annotations.System;
import br.com.concepting.framework.caching.CachedObject;
import br.com.concepting.framework.caching.Cacher;
import br.com.concepting.framework.caching.CacherManager;
import br.com.concepting.framework.constants.Constants;
import br.com.concepting.framework.controller.form.util.ActionFormValidator;
import br.com.concepting.framework.model.BaseModel;
import br.com.concepting.framework.model.MainConsoleModel;
import br.com.concepting.framework.model.annotations.Model;
import br.com.concepting.framework.model.constants.ModelConstants;
import br.com.concepting.framework.model.exceptions.ItemAlreadyExistsException;
import br.com.concepting.framework.model.exceptions.ItemNotFoundException;
import br.com.concepting.framework.model.helpers.ModelInfo;
import br.com.concepting.framework.model.types.ConditionType;
import br.com.concepting.framework.persistence.types.RelationJoinType;
import br.com.concepting.framework.resources.constants.ResourcesConstants;
import br.com.concepting.framework.security.model.LoginSessionModel;
import br.com.concepting.framework.util.*;
import br.com.concepting.framework.util.helpers.PropertyInfo;
import br.com.concepting.framework.util.types.SortOrderType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.beanutils.ConstructorUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Class responsible to manipulate data models.
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
public class ModelUtil{
    private static final ObjectMapper propertyMapper = PropertyUtil.getMapper();
    
    public static Class<? extends MainConsoleModel> getMainConsoleClass(){
        Set<Class<?>> classes = ReflectionUtil.getTypesAnnotatedWith(System.class);
        
        if(classes != null && !classes.isEmpty()){
            try{
                return classes.parallelStream().map(c -> (Class<? extends MainConsoleModel>) c).findFirst().get();
            }
            catch(Throwable e){
            }
        }
        
        return MainConsoleModel.class;
    }
    
    /**
     * Returns the serial number of the form.
     *
     * @return Numeric value that contains the serial number.
     */
    public static Long generateSerialVersionUID(){
        return (long) (Math.random() * NumberUtil.getMaximumRange(Long.class).longValue());
    }
    
    /**
     * Generates a data model based on its identifier.
     *
     * @param <M> Class that defines the data model.
     * @param value String that contains the identifier.
     * @return Instance that contains the data model.
     * @throws IOException Occurs when was not possible to execute the
     * operation.
     * @throws InvocationTargetException Occurs when was not possible to execute
     * the operation.
     * @throws IllegalArgumentException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchMethodException Occurs when was not possible to execute the
     * operation.
     * @throws IllegalAccessException Occurs when was not possible to execute
     * the operation.
     * @throws InstantiationException Occurs when was not possible to execute
     * the operation.
     * @throws ClassNotFoundException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchFieldException Occurs when was not possible to execute the
     * operation.
     */
    public static <M extends BaseModel> M fromIdentifierString(String value) throws IOException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException, NoSuchFieldException{
        if(value != null && value.length() > 0){
            ByteArrayInputStream in = null;
            
            try{
                in = new ByteArrayInputStream(ByteUtil.fromBase64(value));
                
                return fromIdentifierMap(propertyMapper.readValue(in, new TypeReference<Map<String, Object>>(){
                }));
            }
            finally{
                if(in != null)
                    in.close();
            }
        }
        
        return null;
    }
    
    /**
     * Generates a data model based on its map.
     *
     * @param <M> Class that defines the data model.
     * @param map String that contains the map.
     * @return Instance that contains the data model.
     * @throws InvocationTargetException Occurs when was not possible to execute
     * the operation.
     * @throws IllegalArgumentException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchMethodException Occurs when was not possible to execute the
     * operation.
     * @throws IllegalAccessException Occurs when was not possible to execute
     * the operation.
     * @throws InstantiationException Occurs when was not possible to execute
     * the operation.
     * @throws ClassNotFoundException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchFieldException Occurs when was not possible to execute the
     * operation.
     */
    @SuppressWarnings("unchecked")
    private static <M extends BaseModel> M fromIdentifierMap(Map<String, Object> map) throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException, NoSuchFieldException{
        if(map == null || map.size() == 0)
            return null;
        
        Class<M> modelClass = (Class<M>) Class.forName(map.get(Constants.CLASS_ATTRIBUTE_ID).toString());
        M model = ConstructorUtils.invokeConstructor(modelClass, null);
        ModelInfo modelInfo = ModelUtil.getInfo(modelClass);
        
        map.remove(Constants.CLASS_ATTRIBUTE_ID);
        
        for(Entry<String, Object> entry: map.entrySet()){
            Object value = entry.getValue();
            
            if(value == null)
                continue;
            
            PropertyInfo propertyInfo = modelInfo.getPropertyInfo(entry.getKey());
            
            if((propertyInfo.isIdentity() != null && propertyInfo.isIdentity()) || (propertyInfo.isUnique() != null && propertyInfo.isUnique()) || (propertyInfo.isSerializable() != null && propertyInfo.isSerializable())){
                if(propertyInfo.isModel() != null && propertyInfo.isModel())
                    value = fromIdentifierMap((Map<String, Object>) value);
                
                if(propertyInfo.getClazz().equals(Class.class))
                    PropertyUtil.setValue(model, entry.getKey(), Class.forName((String) value));
                else
                    PropertyUtil.setValue(model, entry.getKey(), value);
            }
        }
        
        return model;
    }
    
    /**
     * Generates a map that contains the auditable properties of a data model.
     *
     * @param <M> Class that defines the data model.
     * @param model Instance that contains the data model.
     * @return Instance that contains the map of a data model.
     * @throws IllegalArgumentException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchMethodException Occurs when was not possible to execute the
     * operation.
     * @throws IllegalAccessException Occurs when was not possible to execute
     * the operation.
     * @throws InvocationTargetException Occurs when was not possible to execute
     * the operation.
     * @throws InstantiationException Occurs when was not possible to execute
     * the operation.
     * @throws ClassNotFoundException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchFieldException Occurs when was not possible to execute
     * the operation.
     */
    @SuppressWarnings("unchecked")
    public static <M extends BaseModel> Map<String, Object> generateAuditableMap(M model) throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException, NoSuchFieldException{
        if(model == null)
            return null;
        
        Map<String, Object> map = null;
        ModelInfo modelInfo = ModelUtil.getInfo(model.getClass());
        Collection<PropertyInfo> auditablePropertiesInfos = modelInfo.getAuditablePropertiesInfo();
        
        if(auditablePropertiesInfos != null && !auditablePropertiesInfos.isEmpty()){
            for(PropertyInfo auditablePropertyInfo: auditablePropertiesInfos){
                Object value = PropertyUtil.getValue(model, auditablePropertyInfo.getId());
                
                if(value == null)
                    continue;
                
                if(auditablePropertyInfo.isModel() != null && auditablePropertyInfo.isModel())
                    value = generateAuditableMap((M) value);
                
                if(value != null){
                    if(map == null)
                        map = PropertyUtil.instantiate(Constants.DEFAULT_MAP_CLASS);
                    
                    map.put(auditablePropertyInfo.getId(), value);
                }
            }
        }
        
        return map;
    }
    
    /**
     * Generates an identifier for a auditable data model.
     *
     * @param <M> Class that defines the data model.
     * @param model Instance that contains the data model.
     * @return String that contains the identifier.
     * @throws IOException Occurs when was not possible to execute the
     * operation.
     * @throws NoSuchMethodException Occurs when was not possible to execute the
     * operation.
     * @throws IllegalAccessException Occurs when was not possible to execute
     * the operation.
     * @throws InvocationTargetException Occurs when was not possible to execute
     * the operation.
     * @throws InstantiationException Occurs when was not possible to execute
     * the operation.
     * @throws IllegalArgumentException Occurs when was not possible to execute
     * the operation.
     * @throws ClassNotFoundException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchFieldException Occurs when was not possible to execute
     * the operation.
     */
    public static <M extends BaseModel> String toAuditableString(M model) throws IOException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException, NoSuchFieldException{
        if(model == null)
            return null;
        
        return propertyMapper.writeValueAsString(generateAuditableMap(model));
    }
    
    /**
     * Generates a map that contains the identity, unique and serializable properties of a data model.
     *
     * @param <M> Class that defines the data model.
     * @param model Instance that contains the data model.
     * @return Instance that contains the map of a data model.
     * @throws IllegalArgumentException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchMethodException Occurs when was not possible to execute the
     * operation.
     * @throws IllegalAccessException Occurs when was not possible to execute
     * the operation.
     * @throws InvocationTargetException Occurs when was not possible to execute
     * the operation.
     * @throws InstantiationException Occurs when was not possible to execute
     * the operation.
     * @throws ClassNotFoundException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchFieldException Occurs when was not possible to execute
     * the operation.
     */
    @SuppressWarnings("unchecked")
    public static <M extends BaseModel> Map<String, Object> generateIdentifierMap(M model) throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException, NoSuchFieldException{
        if(model == null)
            return null;
        
        Class<M> modelClass = (Class<M>) model.getClass();
        Map<String, Object> map = PropertyUtil.instantiate(Constants.DEFAULT_MAP_CLASS);
        
        map.put(Constants.CLASS_ATTRIBUTE_ID, modelClass.getName());
        
        ModelInfo modelInfo = ModelUtil.getInfo(model.getClass());
        Collection<PropertyInfo> identityPropertiesInfo = modelInfo.getIdentityPropertiesInfo();
        
        if(identityPropertiesInfo != null && !identityPropertiesInfo.isEmpty()){
            for(PropertyInfo identityPropertyInfo: identityPropertiesInfo){
                Object value = PropertyUtil.getValue(model, identityPropertyInfo.getId());
                
                if(value == null)
                    continue;
                
                if(identityPropertyInfo.isModel() != null && identityPropertyInfo.isModel())
                    value = generateIdentifierMap((M) value);
                
                if(value != null)
                    map.put(identityPropertyInfo.getId(), value);
            }
        }
        
        Collection<PropertyInfo> uniquePropertiesInfo = modelInfo.getUniquePropertiesInfo();
        
        if(uniquePropertiesInfo != null && !uniquePropertiesInfo.isEmpty()){
            for(PropertyInfo uniquePropertyInfo: uniquePropertiesInfo){
                Object value = PropertyUtil.getValue(model, uniquePropertyInfo.getId());
                
                if(value == null)
                    continue;
                
                if(uniquePropertyInfo.isModel() != null && uniquePropertyInfo.isModel())
                    value = generateIdentifierMap((M) value);
                
                if(value != null)
                    map.put(uniquePropertyInfo.getId(), value);
            }
        }
        
        Collection<PropertyInfo> serializablePropertiesInfo = modelInfo.getSerializablePropertiesInfo();
        
        if(serializablePropertiesInfo != null && !serializablePropertiesInfo.isEmpty()){
            for(PropertyInfo serializablePropertyInfo: serializablePropertiesInfo){
                Object value = PropertyUtil.getValue(model, serializablePropertyInfo.getId());
                
                if(value == null)
                    continue;
                
                if(serializablePropertyInfo.isModel() != null && serializablePropertyInfo.isModel())
                    value = generateIdentifierMap((M) value);
                
                if(value != null)
                    map.put(serializablePropertyInfo.getId(), value);
            }
        }
        
        return map;
    }
    
    /**
     * Generates an identifier for a data model.
     *
     * @param <M> Class that defines the data model.
     * @param model Instance that contains the data model.
     * @return String that contains the identifier.
     * @throws IOException Occurs when was not possible to execute the
     * operation.
     * @throws NoSuchMethodException Occurs when was not possible to execute the
     * operation.
     * @throws IllegalAccessException Occurs when was not possible to execute
     * the operation.
     * @throws InvocationTargetException Occurs when was not possible to execute
     * the operation.
     * @throws InstantiationException Occurs when was not possible to execute
     * the operation.
     * @throws IllegalArgumentException Occurs when was not possible to execute
     * the operation.
     * @throws ClassNotFoundException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchFieldException Occurs when was not possible to execute
     * the operation.
     */
    public static <M extends BaseModel> String toIdentifierString(M model) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IllegalArgumentException, ClassNotFoundException, NoSuchFieldException{
        Map<String, Object> map = generateIdentifierMap(model);
        
        if(map != null)
            return ByteUtil.toBase64(propertyMapper.writeValueAsString(map).getBytes());
        
        return null;
    }
    
    /**
     * Returns the instance that contains the attributes of the data model.
     *
     * @param modelClass Class that defines the data model.
     * @return Instance that contains the attributes of the data model.
     * @throws IllegalArgumentException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchMethodException Occurs when was not possible to execute the
     * operation.
     * @throws IllegalAccessException Occurs when was not possible to execute
     * the operation.
     * @throws InvocationTargetException Occurs when was not possible to execute
     * the operation.
     * @throws InstantiationException Occurs when was not possible to execute
     * the operation.
     * @throws ClassNotFoundException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchFieldException Occurs when was not possible to execute
     * the operation.
     */
    @SuppressWarnings("unchecked")
    public static ModelInfo getInfo(Class<? extends BaseModel> modelClass) throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException, NoSuchFieldException{
        if(!PropertyUtil.isModel(modelClass))
            throw new IllegalArgumentException(modelClass.getName());
        
        ModelInfo modelInfo = new ModelInfo();
        String modelClassName = modelClass.getName();
        int pos = modelClassName.indexOf("_$");
        
        if(pos >= 0){
            modelClassName = modelClassName.substring(0, pos);
            modelClass = (Class<? extends BaseModel>) Class.forName(modelClassName);
        }
        
        Cacher<ModelInfo> cacher = CacherManager.getInstance().getCacher(ModelUtil.class);
        CachedObject<ModelInfo> cachedObject = null;
        
        try{
            cachedObject = cacher.get(modelClassName);
            modelInfo = cachedObject.getContent();
        }
        catch(ItemNotFoundException e){
            Model modelAnnotation = modelClass.getAnnotation(Model.class);
            
            modelInfo = new ModelInfo();
            modelInfo.setClazz(modelClass);
            modelInfo.setIsAbstract(Modifier.isAbstract(modelClass.getModifiers()));
            modelInfo.setMappedRepositoryId(modelAnnotation.mappedRepositoryId());
            modelInfo.setCacheable(modelAnnotation.cacheable());
            modelInfo.setDescriptionPattern(modelAnnotation.descriptionPattern());
            modelInfo.setUi(modelAnnotation.ui());
            modelInfo.setTemplateId(modelAnnotation.templateId());
            modelInfo.setActionFormValidatorClass(modelAnnotation.actionFormValidatorClass());
            modelInfo.setGenerateUi((modelAnnotation.ui() != null && modelAnnotation.ui().length() > 0));
            modelInfo.setGenerateActionsAndForm(modelInfo.generateUi());
            
            if(modelAnnotation.generatePersistence())
                modelInfo.setGeneratePersistence(true);
            else
                modelInfo.setGeneratePersistence(modelAnnotation.mappedRepositoryId() != null && modelAnnotation.mappedRepositoryId().length() > 0);
            
            if(modelInfo.generateUi() || modelInfo.generatePersistence())
                modelInfo.setGenerateService(true);
            else
                modelInfo.setGenerateService(modelAnnotation.generateService());
            
            modelInfo.setGenerateWebService(modelAnnotation.generateWebService());
            
            Class<?> superClass = modelClass.getSuperclass();
            
            while(superClass != null && !superClass.equals(BaseModel.class)){
                modelAnnotation = superClass.getAnnotation(Model.class);
                
                if(modelAnnotation != null){
                    if(modelInfo.getMappedRepositoryId() == null || modelInfo.getMappedRepositoryId().length() == 0)
                        modelInfo.setMappedRepositoryId(modelAnnotation.mappedRepositoryId());
                    
                    if(modelInfo.getCacheable() == null || !modelInfo.getCacheable())
                        modelInfo.setCacheable(modelAnnotation.cacheable());
                    
                    if(modelInfo.getDescriptionPattern() == null || modelInfo.getDescriptionPattern().length() == 0)
                        modelInfo.setDescriptionPattern(modelAnnotation.descriptionPattern());
                    
                    if(modelInfo.getUi() == null || modelInfo.getUi().length() == 0)
                        modelInfo.setUi(modelAnnotation.ui());
                    
                    if(modelInfo.getTemplateId() == null || modelInfo.getTemplateId().length() == 0)
                        modelInfo.setTemplateId(modelAnnotation.templateId());
                    
                    if(modelInfo.getActionFormValidatorClass() == null || modelInfo.getActionFormValidatorClass().equals(ActionFormValidator.class))
                        modelInfo.setActionFormValidatorClass(modelAnnotation.actionFormValidatorClass());
                    
                    if(modelInfo.getGenerateUi() == null || !modelInfo.getGenerateUi())
                        modelInfo.setGenerateUi(modelAnnotation.ui() != null && modelAnnotation.ui().length() > 0);
                    
                    if(modelInfo.getGenerateActionsAndForm() == null || !modelInfo.getGenerateActionsAndForm())
                        modelInfo.setGenerateActionsAndForm(modelAnnotation.ui() != null && modelAnnotation.ui().length() > 0);
                    
                    if(modelInfo.getGenerateWebService() == null || !modelInfo.getGenerateWebService())
                        modelInfo.setGenerateWebService(modelAnnotation.generateWebService());
                    
                    if(modelInfo.getGenerateService() == null || !modelInfo.getGenerateService())
                        modelInfo.setGenerateService((modelAnnotation.ui() != null && modelAnnotation.ui().length() > 0) || (modelAnnotation.mappedRepositoryId() != null && modelAnnotation.mappedRepositoryId().length() > 0) || modelAnnotation.generateService());
                    
                    if(modelInfo.getGeneratePersistence() == null || !modelInfo.getGeneratePersistence())
                        modelInfo.setGeneratePersistence(modelAnnotation.mappedRepositoryId() != null && modelAnnotation.mappedRepositoryId().length() > 0);
                }
                
                superClass = superClass.getSuperclass();
            }
            
            if(modelInfo.getTemplateId().length() == 0)
                modelInfo.setTemplateId(Constants.DEFAULT_ATTRIBUTE_ID);
            
            modelInfo.setPropertiesInfo(PropertyUtil.getInfos(modelClass));
            
            cachedObject = new CachedObject<ModelInfo>();
            cachedObject.setId(modelClassName);
            cachedObject.setContent(modelInfo);
            
            try{
                cacher.add(cachedObject);
            }
            catch(ItemAlreadyExistsException e1){
            }
        }
        
        return modelInfo;
    }
    
    /**
     * Returns a sublist of a of data models.
     *
     * @param <M> Class that defines the data model.
     * @param list List that contains the data models.
     * @param propertyId String that contains the identifier of the property
     * that will be used.
     * @param propertyValue Instance that contains the value of the property
     * that will be used.
     * @return Subset that contains the data models.
     * @throws InstantiationException Occurs when was not possible to execute
     * the operation.
     * @throws InvocationTargetException Occurs when was not possible to execute
     * the operation.
     * @throws IllegalAccessException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchMethodException Occurs when was not possible to execute the
     * operation.
     */
    public static <M extends BaseModel> List<M> subList(List<M> list, String propertyId, Object propertyValue) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException{
        if(list == null || propertyId == null || propertyId.length() == 0)
            return null;
        
        List<M> resultList = list.parallelStream().filter(i -> {
            try{
                Object comparePropertyValue = PropertyUtil.getProperty(i, propertyId);
                
                if(propertyValue == null && comparePropertyValue == null)
                    return true;
                
                return (propertyValue != null && comparePropertyValue != null && propertyValue.equals(comparePropertyValue));
            }
            catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
                return false;
            }
        }).collect(Collectors.toList());
        
        return resultList;
    }
    
    /**
     * Sorts a list of data models.
     *
     * @param <M> Class that defines the data model.
     * @param list Instance that contains the list of data models.
     * @throws UnsupportedOperationException Occurs when was not possible to
     * execute the operation.
     * @throws IllegalArgumentException Occurs when was not possible to execute
     * the operation.
     * @throws ClassCastException Occurs when was not possible to execute the
     * operation.
     */
    public static <M extends BaseModel> void sort(List<M> list) throws ClassCastException, IllegalArgumentException, UnsupportedOperationException{
        sort(list, Constants.DEFAULT_SORT_ORDER_TYPE);
    }
    
    /**
     * Sorts a list of data models.
     *
     * @param <M> Class that defines the data model.
     * @param list Instance that contains the list of data models.
     * @param sortPropertyId String that contains the identifier of the property
     * what will be used.
     * @throws UnsupportedOperationException Occurs when was not possible to
     * execute the operation.
     * @throws IllegalArgumentException Occurs when was not possible to execute
     * the operation.
     * @throws ClassCastException Occurs when was not possible to execute the
     * operation.
     */
    public static <M extends BaseModel> void sort(List<M> list, String sortPropertyId) throws ClassCastException, IllegalArgumentException, UnsupportedOperationException{
        sort(list, sortPropertyId, Constants.DEFAULT_SORT_ORDER_TYPE);
    }
    
    /**
     * Sorts a list of data models.
     *
     * @param <M> Class that defines the data model.
     * @param list Instance that contains the list of data models.
     * @param sortOrder Instance that contains the sort type.
     * @throws UnsupportedOperationException Occurs when was not possible to
     * execute the operation.
     * @throws IllegalArgumentException Occurs when was not possible to execute
     * the operation.
     * @throws ClassCastException Occurs when was not possible to execute the
     * operation.
     */
    public static <M extends BaseModel> void sort(List<M> list, SortOrderType sortOrder) throws ClassCastException, IllegalArgumentException, UnsupportedOperationException{
        if(sortOrder == null || sortOrder == SortOrderType.ASCEND){
            Collections.sort(list);
        }
        else
            Collections.sort(list, Collections.reverseOrder());
    }
    
    /**
     * Sorts a list of data models.
     *
     * @param <M> Class that defines the data model.
     * @param list Instance that contains the list of data models.
     * @param sortPropertyId String that contains the identifier of the property
     * what will be used.
     * @param sortOrder Instance that contains the sort type.
     * @throws UnsupportedOperationException Occurs when was not possible to
     * execute the operation.
     * @throws IllegalArgumentException Occurs when was not possible to execute
     * the operation.
     * @throws ClassCastException Occurs when was not possible to execute the
     * operation.
     */
    public static <M extends BaseModel> void sort(List<M> list, String sortPropertyId, SortOrderType sortOrder) throws ClassCastException, IllegalArgumentException, UnsupportedOperationException{
        if(list != null && !list.isEmpty()){
            if(sortPropertyId != null && sortPropertyId.length() > 0)
                for(BaseModel item: list)
                    item.setSortPropertyId(sortPropertyId);
            
            sort(list, sortOrder);
        }
    }
    
    /**
     * Aggregates and sorts a list of data models.
     *
     * @param <M> Class that defines the data model.
     * @param list List that contains the data models.
     * @param propertiesIds List that contains the identifiers of the
     * aggregation.
     * @return List that contains the processed data models.
     * @throws InstantiationException Occurs when was not possible to execute
     * the operation.
     * @throws InvocationTargetException Occurs when was not possible to execute
     * the operation.
     * @throws IllegalAccessException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchMethodException Occurs when was not possible to execute the
     * operation.
     * @throws UnsupportedEncodingException Occurs when was not possible to
     * execute the operation.
     * @throws IllegalArgumentException Occurs when was not possible to execute
     * the operation.
     * @throws ClassCastException Occurs when was not possible to execute the
     * operation.
     */
    public static <M extends BaseModel> List<M> aggregateAndSort(List<M> list, String[] propertiesIds) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassCastException, IllegalArgumentException, UnsupportedEncodingException{
        return aggregateAndSort(list, propertiesIds, null, null);
    }
    
    /**
     * Aggregates and sorts a list of data models.
     *
     * @param <M> Class that defines the data model.
     * @param list List that contains the data models.
     * @param propertiesIds List that contains the identifiers of the
     * aggregation.
     * @param sortPropertyId String that contains the identifier of the sort property.
     * @param sortOrder Instance that contains the sort order.
     * @return List that contains the processed data models.
     * @throws InstantiationException Occurs when was not possible to execute
     * the operation.
     * @throws InvocationTargetException Occurs when was not possible to execute
     * the operation.
     * @throws IllegalAccessException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchMethodException Occurs when was not possible to execute the
     * operation.
     * @throws UnsupportedEncodingException Occurs when was not possible to
     * execute the operation.
     * @throws IllegalArgumentException Occurs when was not possible to execute
     * the operation.
     * @throws ClassCastException Occurs when was not possible to execute the
     * operation.
     */
    public static <M extends BaseModel> List<M> aggregateAndSort(List<M> list, String[] propertiesIds, String sortPropertyId, SortOrderType sortOrder) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassCastException, IllegalArgumentException, UnsupportedEncodingException{
        if(list == null || list.size() == 0 || propertiesIds == null || propertiesIds.length == 0)
            return null;
        
        String propertyId = null;
        Object propertyValue = null;
        String[] bufferPropertiesIds = null;
        M bufferItem = null;
        List<M> bufferSubList = null;
        List<M> resultList = PropertyUtil.instantiate(Constants.DEFAULT_LIST_CLASS);
        
        for(int cont1 = 0; cont1 < propertiesIds.length; cont1++){
            propertyId = propertiesIds[cont1];
            
            sort(list, propertyId, SortOrderType.ASCEND);
            
            for(int cont2 = 0; cont2 < list.size(); cont2++){
                bufferItem = list.get(cont2);
                
                if(bufferItem != null){
                    propertyValue = PropertyUtil.getValue(bufferItem, propertyId);
                    bufferSubList = subList(list, propertyId, propertyValue);
                    
                    if(propertiesIds.length > 1){
                        bufferPropertiesIds = Arrays.copyOfRange(propertiesIds, 1, propertiesIds.length);
                        bufferSubList = aggregateAndSort(bufferSubList, bufferPropertiesIds, sortPropertyId, sortOrder);
                    }
                    
                    if(bufferSubList != null && !bufferSubList.isEmpty()){
                        if(sortPropertyId != null && sortPropertyId.length() > 0){
                            if(sortOrder == null)
                                sortOrder = SortOrderType.ASCEND;
                            
                            sort(bufferSubList, sortPropertyId, sortOrder);
                        }
                        
                        resultList.addAll(bufferSubList);
                        
                        cont2 += bufferSubList.size() - 1;
                    }
                }
            }
        }
        
        return resultList;
    }
    
    /**
     * Returns the sum value of a property in a data model list.
     *
     * @param <N> Class that defines the numeric value.
     * @param <M> Class that defines the data model.
     * @param <C> Class that defines the list of data models.
     * @param list List that contains the data models.
     * @param propertyId String that contains the identifier of property that
     * will be used.
     * @return Numeric value that contains the average.
     * @throws IllegalAccessException Occurs when was not possible to execute
     * the operation.
     * @throws InvocationTargetException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchMethodException Occurs when was not possible to execute the
     * operation.
     */
    @SuppressWarnings("unchecked")
    public static <N extends Number, C extends List<M>, M extends BaseModel> N sum(C list, String propertyId) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException{
        if(list != null && list.size() > 0 && propertyId != null && propertyId.length() > 0){
            Number result = null;
            Number buffer = null;
            
            for(M item: list){
                buffer = PropertyUtil.getValue(item, propertyId);
                
                if(result == null)
                    result = buffer;
                else
                    result = NumberUtil.add(result, buffer);
            }
            
            return (N) result;
        }
        
        return null;
    }
    
    /**
     * Returns the average value of a property in a data model list.
     *
     * @param <N> Class that defines the numeric value.
     * @param <M> Class that defines the data model.
     * @param <C> Class that defines the list of data models.
     * @param list List that contains the data models.
     * @param propertyId String that contains the identifier of property that
     * will be used.
     * @return Numeric value that contains the average.
     * @throws IllegalAccessException Occurs when was not possible to execute
     * the operation.
     * @throws InvocationTargetException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchMethodException Occurs when was not possible to execute the
     * operation.
     */
    @SuppressWarnings("unchecked")
    public static <N extends Number, C extends List<M>, M extends BaseModel> N average(C list, String propertyId) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException{
        if(list != null && list.size() > 0 && propertyId != null && propertyId.length() > 0){
            Number result = sum(list, propertyId);
            
            result = NumberUtil.divide(result, list.size());
            
            return (N) result;
        }
        
        return null;
    }
    
    /**
     * Returns the data model whose property has the maximum value in the list.
     *
     * @param <M> Class that defines the data model.
     * @param <C> Class that defines the list of data models.
     * @param list List that contains the data models.
     * @param propertyId String that contains the identifier of property that
     * will be used.
     * @return Instance that contains the data model.
     * @throws ClassCastException Occurs when was not possible to execute the
     * operation.
     * @throws NoSuchElementException Occurs when was not possible to execute
     * the operation.
     */
    public static <C extends List<M>, M extends BaseModel> M max(C list, String propertyId) throws ClassCastException, NoSuchElementException{
        if(list != null && !list.isEmpty()){
            if(propertyId != null && propertyId.length() > 0)
                for(BaseModel item: list)
                    item.setSortPropertyId(propertyId);
            
            return Collections.max(list);
        }
        
        return null;
    }
    
    /**
     * Returns the data model whose property has the minimum value in the list.
     *
     * @param <M> Class that defines the data model.
     * @param <C> Class that defines the list of data models.
     * @param list List that contains the data models.
     * @param propertyId String that contains the identifier of property that
     * will be used.
     * @return Instance that contains the data model.
     * @throws ClassCastException Occurs when was not possible to execute the
     * operation.
     * @throws NoSuchElementException Occurs when was not possible to execute
     * the operation.
     */
    public static <C extends List<M>, M extends BaseModel> M min(C list, String propertyId) throws ClassCastException, NoSuchElementException{
        if(list != null && !list.isEmpty()){
            if(propertyId != null && propertyId.length() > 0)
                for(BaseModel item: list)
                    item.setSortPropertyId(propertyId);
            
            return Collections.min(list);
        }
        
        return null;
    }
    
    /**
     * Builds the phonetic mapping.
     *
     * @param <M> Class that defines the data model.
     * @param modelClass Class that defines the data model.
     * @return Instance that contains the phonetic mapping.
     * @throws ClassNotFoundException Occurs when was not possible to execute
     * the operation.
     * @throws IllegalArgumentException Occurs when was not possible to execute
     * the operation.
     * @throws InstantiationException Occurs when was not possible to execute
     * the operation.
     * @throws InvocationTargetException Occurs when was not possible to execute
     * the operation.
     * @throws IllegalAccessException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchMethodException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchFieldException Occurs when was not possible to execute
     * the operation.
     */
    public static <M extends BaseModel> Map<String, PropertyInfo> buildPhoneticMap(Class<M> modelClass) throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException, NoSuchFieldException{
        if(modelClass != null){
            Map<String, PropertyInfo> phoneticMap = PropertyUtil.instantiate(Constants.DEFAULT_MAP_CLASS);
            Collection<Class<? extends BaseModel>> processedRelations = PropertyUtil.instantiate(Constants.DEFAULT_LIST_CLASS);
            
            buildPhoneticMap(modelClass, processedRelations, phoneticMap);
            
            return phoneticMap;
        }
        
        return null;
    }
    
    /**
     * Builds the phonetic mapping.
     *
     * @param modelClass Class that defines the data model.
     * @param processedRelations List that contains the properties already
     * processed.
     * @param phoneticMap Instance that contains the phonetic mapping.
     * @throws ClassNotFoundException Occurs when was not possible to execute
     * the operation.
     * @throws IllegalArgumentException Occurs when was not possible to execute
     * the operation.
     * @throws InstantiationException Occurs when was not possible to execute
     * the operation.
     * @throws InvocationTargetException Occurs when was not possible to execute
     * the operation.
     * @throws IllegalAccessException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchMethodException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchFieldException Occurs when was not possible to execute
     * the operation.
     */
    @SuppressWarnings("unchecked")
    public static void buildPhoneticMap(Class<? extends BaseModel> modelClass, Collection<Class<? extends BaseModel>> processedRelations, Map<String, PropertyInfo> phoneticMap) throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException, NoSuchFieldException{
        if(modelClass == null)
            return;
        
        ModelInfo modelInfo = ModelUtil.getInfo(modelClass);
        Collection<PropertyInfo> propertiesInfo = modelInfo.getPropertiesInfo();
        Class<? extends BaseModel> relationModelClass = null;
        
        if(propertiesInfo != null && !propertiesInfo.isEmpty()){
            if(phoneticMap == null)
                phoneticMap = PropertyUtil.instantiate(Constants.DEFAULT_MAP_CLASS);
            
            for(PropertyInfo propertyInfo: propertiesInfo){
                if(propertyInfo.isModel() != null && propertyInfo.isModel() && propertyInfo.getRelationJoinType() != RelationJoinType.NONE){
                    relationModelClass = (Class<? extends BaseModel>) propertyInfo.getClazz();
                    
                    if(relationModelClass != null && !relationModelClass.equals(modelClass)){
                        if(processedRelations == null || !processedRelations.contains(propertyInfo.getClazz())){
                            if(processedRelations == null)
                                processedRelations = PropertyUtil.instantiate(Constants.DEFAULT_LIST_CLASS);
                            
                            processedRelations.add(relationModelClass);
                            
                            buildPhoneticMap(relationModelClass, processedRelations, phoneticMap);
                        }
                    }
                }
                else if(propertyInfo.getSearchCondition() == ConditionType.PHONETIC)
                    phoneticMap.put(propertyInfo.getId(), propertyInfo);
            }
        }
    }
    
    /**
     * Fills the phonetic properties in the data model.
     *
     * @param <M> Class that defines the data model.
     * @param model Instance that contains the data model.
     * @throws ClassNotFoundException Occurs when was not possible to execute
     * the operation.
     * @throws IllegalArgumentException Occurs when was not possible to execute
     * the operation.
     * @throws InstantiationException Occurs when was not possible to execute
     * the operation.
     * @throws InvocationTargetException Occurs when was not possible to execute
     * the operation.
     * @throws IllegalAccessException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchMethodException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchFieldException Occurs when was not possible to execute
     * the operation.
     */
    @SuppressWarnings("unchecked")
    public static <M extends BaseModel> void fillPhoneticProperties(M model) throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException, NoSuchFieldException{
        if(model == null)
            return;
        
        Class<M> modelClass = (Class<M>) model.getClass();
        Map<String, PropertyInfo> phoneticMap = buildPhoneticMap(modelClass);
        
        if(phoneticMap != null && !phoneticMap.isEmpty()){
            PropertyInfo phoneticPropertyInfo = null;
            String phoneticPropertyId = null;
            String phoneticPropertyValue = null;
            
            for(Entry<String, PropertyInfo> entry: phoneticMap.entrySet()){
                phoneticPropertyInfo = entry.getValue();
                phoneticPropertyId = phoneticPropertyInfo.getPhoneticPropertyId();
                phoneticPropertyValue = PhoneticUtil.soundCode(PropertyUtil.getValue(model, entry.getKey()));
                
                PropertyUtil.setValue(model, phoneticPropertyId, phoneticPropertyValue);
            }
        }
    }
    
    /**
     * Filters the list of data models by phonetic.
     *
     * @param <M> Class that defines the data model.
     * @param <L> Class that defines the list of data models.
     * @param model Instance that contains the criteria of filtering.
     * @param modelList List that contains the data models before the filtering.
     * @return List that contains the data models after the filtering.
     * @throws ClassNotFoundException Occurs when was not possible to execute
     * the operation.
     * @throws IllegalArgumentException Occurs when was not possible to execute
     * the operation.
     * @throws InstantiationException Occurs when was not possible to execute
     * the operation.
     * @throws InvocationTargetException Occurs when was not possible to execute
     * the operation.
     * @throws IllegalAccessException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchMethodException Occurs when was not possible to execute
     * the operation.
     * @throws NoSuchFieldException Occurs when was not possible to execute
     * the operation.
     */
    public static <M extends BaseModel, L extends List<M>> L filterByPhonetic(M model, L modelList) throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException, NoSuchFieldException{
        if(model == null || modelList == null || modelList.size() == 0)
            return modelList;
        
        Map<String, PropertyInfo> phoneticMap = buildPhoneticMap(model.getClass());
        
        if(phoneticMap != null && !phoneticMap.isEmpty()){
            String comparePropertyValue = null;
            String propertyValue = null;
            Double comparePhoneticAccuracy = null;
            Double phoneticAccuracy = null;
            Integer phoneticAccuracyCount = null;
            PropertyInfo phoneticPropertyInfo = null;
            String phoneticPropertyId = null;
            M modelListItem = null;
            
            for(int cont = 0; cont < modelList.size(); cont++){
                modelListItem = modelList.get(cont);
                comparePhoneticAccuracy = 0d;
                phoneticAccuracy = 0d;
                phoneticAccuracyCount = 0;
                
                for(Entry<String, PropertyInfo> entry: phoneticMap.entrySet()){
                    phoneticPropertyInfo = entry.getValue();
                    phoneticPropertyId = phoneticPropertyInfo.getPhoneticPropertyId();
                    comparePropertyValue = PropertyUtil.getValue(modelListItem, phoneticPropertyId);
                    propertyValue = PhoneticUtil.soundCode(PropertyUtil.getValue(model, entry.getKey()));
                    
                    if(propertyValue != null && propertyValue.length() > 0){
                        phoneticAccuracy += PhoneticUtil.getAccuracy(propertyValue, comparePropertyValue);
                        comparePhoneticAccuracy += phoneticPropertyInfo.getPhoneticAccuracy();
                        
                        phoneticAccuracyCount++;
                    }
                }
                
                phoneticAccuracy = phoneticAccuracy / phoneticAccuracyCount;
                comparePhoneticAccuracy = comparePhoneticAccuracy / phoneticAccuracyCount;
                
                if(phoneticAccuracy < comparePhoneticAccuracy){
                    modelList.remove(cont);
                    
                    cont--;
                }
                else{
                    modelListItem.setCompareAccuracy(phoneticAccuracy);
                    
                    modelList.set(cont, modelListItem);
                }
            }
            
            ModelUtil.sort(modelList, ModelConstants.COMPARE_ACCURACY_ATTRIBUTE_ID, SortOrderType.DESCEND);
        }
        
        return modelList;
    }
    
    /**
     * Returns the package prefix of a data model.
     *
     * @param modelClass Class that defines the data model.
     * @return String that contains the package prefix.
     */
    public static String getPackagePrefix(Class<? extends BaseModel> modelClass){
        if(modelClass != null)
            return getPackagePrefix(modelClass.getName());
        
        return null;
    }
    
    /**
     * Returns the package prefix of a data model.
     *
     * @param modelClassName String that defines the data model.
     * @return String that contains the package prefix.
     */
    public static String getPackagePrefix(String modelClassName){
        if(modelClassName == null || modelClassName.length() == 0)
            return null;
        
        String[] packageItems = StringUtil.split(modelClassName, ".");
        StringBuilder packagePrefixBuffer = new StringBuilder();
        
        for(int cont = 0; cont < 4; cont++){
            if(cont >= packageItems.length)
                break;
            
            if(cont > 0)
                packagePrefixBuffer.append(".");
            
            packagePrefixBuffer.append(packageItems[cont]);
        }
        
        String packagePrefix = packagePrefixBuffer.toString();
        
        return packagePrefix;
    }
    
    /**
     * Returns a URL based on a data model class name.
     *
     * @param modelClass Class that defines the data model.
     * @return String that contains the URL.
     */
    public static String getUrlByModel(Class<? extends BaseModel> modelClass){
        if(modelClass != null)
            return getUrlByModel(modelClass.getName());
        
        return null;
    }
    
    /**
     * Returns the URL based on a data model class name.
     *
     * @param modelClassName String that contains the data model class name.
     * @return String that contains the URL.
     */
    public static String getUrlByModel(String modelClassName){
        if(modelClassName != null && modelClassName.length() > 0){
            String url = modelClassName;
            String prefix = ModelUtil.getPackagePrefix(modelClassName);
            
            url = StringUtil.replaceAll(url, prefix, "");
            
            StringBuilder searchBuffer = new StringBuilder();
            
            searchBuffer.append(".");
            searchBuffer.append(ModelConstants.DEFAULT_ID);
            
            url = StringUtil.replaceAll(url, searchBuffer.toString(), "");
            url = StringUtil.replaceAll(url, StringUtil.capitalize(ModelConstants.DEFAULT_ID), "");
            
            if(url.startsWith("."))
                url = url.substring(1);
            
            StringBuilder urlBuffer = new StringBuilder();
            int pos = url.lastIndexOf(".");
            
            if(pos >= 0){
                urlBuffer.append(url, 0, pos + 1);
                urlBuffer.append(url.substring(pos + 1).substring(0, 1).toLowerCase());
                urlBuffer.append(url.substring(pos + 1).substring(1));
            }
            else{
                urlBuffer.append(url.substring(0, 1).toLowerCase());
                urlBuffer.append(url.substring(1));
            }
            
            url = StringUtil.replaceAll(urlBuffer.toString(), ".", "/");
            
            urlBuffer = new StringBuilder();
            urlBuffer.append("/");
            urlBuffer.append(url);
            
            url = urlBuffer.toString();
            
            return url;
        }
        
        return null;
    }
    
    /**
     * Returns the resources identifier based on a data model class name.
     *
     * @param modelClassName String that contains the data model class name.
     * @return String that contains the identifier.
     */
    public static String getResourcesIdByModel(String modelClassName){
        if(modelClassName != null && modelClassName.length() > 0){
            StringBuilder resourcesId = new StringBuilder();
            String resourcesPrefix = ResourcesConstants.DEFAULT_PROPERTIES_RESOURCES_DIR;
            
            resourcesId.append(resourcesPrefix, 0, resourcesPrefix.length() - 1);
            resourcesId.append(getUrlByModel(modelClassName));
            
            return StringUtil.replaceAll(resourcesId.toString(), "/", ".");
        }
        
        return null;
    }
    
    /**
     * Returns the resources identifier based on a data model.
     *
     * @param modelClass Class that contains the data model.
     * @return String that contains the identifier.
     */
    public static String getResourcesIdByModel(Class<? extends BaseModel> modelClass){
        if(modelClass != null)
            return getResourcesIdByModel(modelClass.getName());
        
        return null;
    }
}