package br.com.concepting.framework.controller.form.util;

import br.com.concepting.framework.controller.SystemController;
import br.com.concepting.framework.controller.action.types.ActionType;
import br.com.concepting.framework.controller.form.ActionFormController;
import br.com.concepting.framework.controller.form.BaseActionForm;
import br.com.concepting.framework.controller.form.annotations.ActionForm;
import br.com.concepting.framework.controller.form.constants.ActionFormConstants;
import br.com.concepting.framework.controller.helpers.RequestParameterInfo;
import br.com.concepting.framework.controller.types.ScopeType;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.BaseModel;
import br.com.concepting.framework.model.constants.ModelConstants;
import br.com.concepting.framework.model.helpers.ModelInfo;
import br.com.concepting.framework.model.util.ModelUtil;
import br.com.concepting.framework.security.controller.SecurityController;
import br.com.concepting.framework.security.model.LoginParameterModel;
import br.com.concepting.framework.security.model.LoginSessionModel;
import br.com.concepting.framework.security.model.UserModel;
import br.com.concepting.framework.ui.constants.UIConstants;
import br.com.concepting.framework.util.*;
import br.com.concepting.framework.util.helpers.PropertyInfo;
import br.com.concepting.framework.util.types.InputType;
import br.com.concepting.framework.util.types.PropertyType;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Class responsible to populate the data model properties.
 *
 * @author fvilarinho
 * @since 2.0.0
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
public class ActionFormPopulator{
    private SystemController systemController = null;
    private ActionFormController actionFormController = null;
    private SecurityController securityController = null;
    private BaseActionForm<? extends BaseModel> actionForm = null;
    
    /**
     * Constructor - Initializes the population.
     *
     * @param <F> Class that defines the form.
     * @param actionForm Instance that contains the form.
     * @param systemController Instance that contains the system controller.
     * @param actionFormController Instance that contains the action form controller.
     * @param securityController Instance that contains the security controller.
     */
    public <F extends BaseActionForm<? extends BaseModel>> ActionFormPopulator(F actionForm, SystemController systemController, ActionFormController actionFormController, SecurityController securityController){
        super();
        
        this.actionForm = actionForm;
        this.systemController = systemController;
        this.actionFormController = actionFormController;
        this.securityController = securityController;
    }
    
    /**
     * Returns the instance that contains the current language.
     *
     * @return Instance that contains the current language.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    private Locale getCurrentLanguage() throws InternalErrorException{
        if(this.securityController != null){
            LoginSessionModel loginSession = this.securityController.getLoginSession();
            UserModel user = (loginSession != null ? loginSession.getUser() : null);
            LoginParameterModel loginParameter = (user != null ? user.getLoginParameter() : null);
            
            if(loginParameter != null && loginParameter.getLanguage() != null && loginParameter.getLanguage().length() > 0)
                return LanguageUtil.getLanguageByString(loginParameter.getLanguage());
        }
        
        return LanguageUtil.getDefaultLanguage();
    }
    
    /**
     * Populates the action form.
     *
     * @throws InternalErrorException Occurs when was not possible to populate
     * the action form.
     */
    public void populateActionForm() throws InternalErrorException{
        if(this.actionForm != null && this.systemController != null){
            String action = this.systemController.getRequestParameterValue(ActionFormConstants.ACTION_ATTRIBUTE_ID);
            
            if(action == null || action.length() == 0)
                action = ActionType.INIT.getMethod();
            
            this.actionForm.addActionHistory(action);
            this.actionForm.setForward(this.systemController.getRequestParameterValue(ActionFormConstants.FORWARD_ATTRIBUTE_ID));
            this.actionForm.setUpdateViews(this.systemController.getRequestParameterValue(UIConstants.UPDATE_VIEWS_ATTRIBUTE_ID));
            this.actionForm.setValidateModel(Boolean.valueOf(this.systemController.getRequestParameterValue(ModelConstants.VALIDATE_MODEL_ATTRIBUTE_ID)));
            this.actionForm.setValidateModelProperties(this.systemController.getRequestParameterValue(ModelConstants.VALIDATE_MODEL_PROPERTIES_ATTRIBUTE_ID));
        }
    }
    
    /**
     * Populates the data model.
     *
     * @throws InternalErrorException Occurs when was not possible to populate
     * the data model.
     */
    public void populateModel() throws InternalErrorException{
        if(this.systemController != null && this.actionFormController != null){
            Map<String, RequestParameterInfo> requestParameters = this.systemController.getRequestParameters();
            
            if(requestParameters != null && !requestParameters.isEmpty()){
                for(Entry<String, RequestParameterInfo> entry: requestParameters.entrySet()){
                    RequestParameterInfo requestParameterInfo = entry.getValue();
                    
                    if(requestParameterInfo != null && requestParameterInfo.getType() == PropertyType.MODEL){
                        if(!this.actionFormController.hasValidationMessage(entry.getKey())){
                            if(ActionFormUtil.isDatasetProperty(entry.getKey()))
                                populateDatasetProperty(requestParameterInfo);
                            else
                                populateProperty(requestParameterInfo);
                        }
                    }
                }
            }
            
            ActionForm actionForm = this.actionForm.getClass().getAnnotation(ActionForm.class);
            
            this.systemController.setAttribute(actionForm.name(), this.actionForm, ScopeType.SESSION);
        }
    }
    
    /**
     * Populates a property of the data model.
     *
     * @param requestParameterInfo Instance that contains the request parameter.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    private void populateProperty(RequestParameterInfo requestParameterInfo) throws InternalErrorException{
        if(this.actionForm != null && requestParameterInfo != null){
            try{
                BaseModel model = this.actionForm.getModel();
                Class<? extends BaseModel> modelClass = model.getClass();
                ModelInfo modelInfo = ModelUtil.getInfo(modelClass);
                String propertyName = requestParameterInfo.getName();
                PropertyInfo propertyInfo = modelInfo.getPropertyInfo(propertyName);
                Class<?> propertyClass = null;
                Object propertyValue = PropertyUtil.getValue(model, propertyName);
                
                if(propertyInfo.getPropertyTypeId() != null && propertyInfo.getPropertyTypeId().length() > 0){
                    StringBuilder propertyTypeId = new StringBuilder();
                    int pos = propertyName.indexOf(".");
                    
                    if(pos >= 0)
                        propertyTypeId.append(propertyName.subSequence(0, pos + 1));
                    
                    propertyTypeId.append(propertyInfo.getPropertyTypeId());
                    
                    propertyClass = PropertyUtil.getValue(model, propertyTypeId.toString());
                }
                else
                    propertyClass = (propertyValue != null ? propertyValue.getClass() : null);
                
                if(propertyClass != null && (propertyValue == null || !propertyClass.equals(propertyValue.getClass()))){
                    if(PropertyUtil.isNumber(propertyClass))
                        propertyValue = PropertyUtil.convertTo(0, propertyClass);
                    else
                        propertyValue = propertyClass.newInstance();
                }
                else
                    propertyValue = populateProperty(requestParameterInfo, propertyInfo, propertyClass);
                
                PropertyUtil.setValue(model, propertyName, propertyValue);
                
                String contentFilenamePropertyNameBuffer = propertyInfo.getContentFilenamePropertyId();
                
                if(contentFilenamePropertyNameBuffer != null && contentFilenamePropertyNameBuffer.length() > 0){
                    StringBuilder contentFilenamePropertyName = new StringBuilder();
                    int pos = propertyName.lastIndexOf(".");
                    
                    if(pos >= 0){
                        contentFilenamePropertyName.append(propertyName, 0, pos);
                        contentFilenamePropertyName.append(".");
                    }
                    
                    contentFilenamePropertyName.append(contentFilenamePropertyNameBuffer);
                    
                    PropertyUtil.setValue(model, contentFilenamePropertyName.toString(), requestParameterInfo.getContentFilename());
                }
                
                String contentTypePropertyNameBuffer = propertyInfo.getContentTypePropertyId();
                
                if(contentTypePropertyNameBuffer != null && contentTypePropertyNameBuffer.length() > 0){
                    StringBuilder contentTypePropertyName = new StringBuilder();
                    int pos = propertyName.lastIndexOf(".");
                    
                    if(pos >= 0){
                        contentTypePropertyName.append(propertyName, 0, pos);
                        contentTypePropertyName.append(".");
                    }
                    
                    contentTypePropertyName.append(contentTypePropertyNameBuffer);
                    
                    PropertyUtil.setValue(model, contentTypePropertyName.toString(), requestParameterInfo.getContentType());
                }
            }
            catch(NoSuchFieldException | NoSuchMethodException e){
            }
            catch(IllegalArgumentException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e){
                throw new InternalErrorException(e);
            }
        }
    }
    
    /**
     * Populates a property of the dataset.
     *
     * @param requestParameterInfo Instance that contains the request parameter.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    @SuppressWarnings("unchecked")
    private void populateDatasetProperty(RequestParameterInfo requestParameterInfo) throws InternalErrorException{
        if(this.actionFormController != null && this.actionForm != null && requestParameterInfo != null){
            String propertyName = requestParameterInfo.getName();
            String[] datasetPropertyBuffer = StringUtil.split(propertyName, ":");
            
            if(datasetPropertyBuffer != null && datasetPropertyBuffer.length > 0){
                String datasetPropertyName = datasetPropertyBuffer[0];
                List<? extends BaseModel> datasetValues = this.actionFormController.getPropertyDatasetValues(datasetPropertyName);
                
                if(datasetValues != null && !datasetValues.isEmpty()){
                    int datasetIndex = -1;
                    
                    try{
                        datasetIndex = NumberUtil.parseInt(datasetPropertyBuffer[2]);
                    }
                    catch(ParseException e){
                    }
                    
                    if(datasetIndex >= 0){
                        try{
                            BaseModel model = this.actionForm.getModel();
                            Class<? extends BaseModel> modelClass = model.getClass();
                            ModelInfo modelInfo = ModelUtil.getInfo(modelClass);
                            PropertyInfo datasetPropertyInfo = modelInfo.getPropertyInfo(datasetPropertyName);
                            
                            if((datasetPropertyInfo.isModel() == null || !datasetPropertyInfo.isModel()) && (datasetPropertyInfo.hasModel() == null || !datasetPropertyInfo.hasModel())){
                                int pos = datasetPropertyName.lastIndexOf(".");
                                
                                if(pos >= 0){
                                    datasetPropertyName = datasetPropertyName.substring(0, pos);
                                    datasetPropertyInfo = modelInfo.getPropertyInfo(datasetPropertyName);
                                }
                            }
                            
                            if((datasetPropertyInfo.isModel() != null && datasetPropertyInfo.isModel()) || (datasetPropertyInfo.hasModel() != null && datasetPropertyInfo.hasModel())){
                                Class<? extends BaseModel> datasetModelClass = (datasetPropertyInfo.isModel() != null && datasetPropertyInfo.isModel() ? (Class<? extends BaseModel>) datasetPropertyInfo.getClazz() : (Class<? extends BaseModel>) datasetPropertyInfo.getCollectionItemsClass());
                                ModelInfo datasetModelInfo = ModelUtil.getInfo(datasetModelClass);
                                Class<?> datasetPropertyClass = null;
                                Object datasetPropertyValue = null;
                                BaseModel datasetValue = datasetValues.get(datasetIndex);
                                
                                if(datasetValue != null){
                                    datasetPropertyInfo = datasetModelInfo.getPropertyInfo(datasetPropertyBuffer[1]);
                                    
                                    String contentFilenamePropertyNameBuffer = datasetPropertyInfo.getContentFilenamePropertyId();
                                    
                                    if(contentFilenamePropertyNameBuffer != null && contentFilenamePropertyNameBuffer.length() > 0){
                                        StringBuilder contentFilenamePropertyName = new StringBuilder();
                                        
                                        contentFilenamePropertyName.append(datasetPropertyName);
                                        contentFilenamePropertyName.append(".");
                                        contentFilenamePropertyName.append(contentFilenamePropertyNameBuffer);
                                        
                                        PropertyUtil.setValue(datasetValue, contentFilenamePropertyName.toString(), requestParameterInfo.getContentFilename());
                                    }
                                    
                                    String contentTypePropertyNameBuffer = datasetPropertyInfo.getContentTypePropertyId();
                                    
                                    if(contentTypePropertyNameBuffer != null && contentTypePropertyNameBuffer.length() > 0){
                                        StringBuilder contentTypePropertyName = new StringBuilder();
                                        
                                        contentTypePropertyName.append(datasetPropertyName);
                                        contentTypePropertyName.append(".");
                                        contentTypePropertyName.append(contentTypePropertyNameBuffer);
                                        
                                        PropertyUtil.setValue(datasetValue, contentTypePropertyName.toString(), requestParameterInfo.getContentType());
                                    }
                                    
                                    datasetPropertyName = datasetPropertyInfo.getId();
                                    
                                    if(datasetPropertyInfo.getPropertyTypeId() != null && datasetPropertyInfo.getPropertyTypeId().length() > 0)
                                        datasetPropertyClass = PropertyUtil.getValue(datasetValue, datasetPropertyInfo.getPropertyTypeId());
                                    else{
                                        datasetPropertyValue = PropertyUtil.getValue(datasetValue, datasetPropertyName);
                                        datasetPropertyClass = (datasetPropertyValue != null ? datasetPropertyValue.getClass() : null);
                                    }
                                    
                                    datasetPropertyValue = populateProperty(requestParameterInfo, datasetPropertyInfo, datasetPropertyClass);
                                    
                                    PropertyUtil.setValue(datasetValue, datasetPropertyName, datasetPropertyValue);
                                }
                            }
                        }
                        catch(IllegalArgumentException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException | NoSuchFieldException e){
                            throw new InternalErrorException(e);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Populates a property of the data model.
     *
     * @param requestParameterInfo Instance that contains the request parameter.
     * @param propertyInfo Instance that contains the property attributes.
     * @param currentPropertyClass Class that defines the current type of the property.
     * @return Instance that contains the property value.
     */
    private Object populateProperty(RequestParameterInfo requestParameterInfo, PropertyInfo propertyInfo, Class<?> currentPropertyClass){
        Class<?> propertyClass = (currentPropertyClass != null ? currentPropertyClass : propertyInfo.getClazz());
        String requestParameterName = requestParameterInfo.getName();
        String requestParameterValue = requestParameterInfo.getValue();
        Object propertyValue = null;
        
        if(PropertyUtil.isCollection(propertyClass) || propertyInfo.isCollection())
            propertyValue = populateCollectionProperty(requestParameterInfo, propertyInfo);
        else if(PropertyUtil.isModel(propertyClass) || propertyInfo.isModel())
            propertyValue = populateModelProperty(requestParameterValue);
        else if(PropertyUtil.isEnum(propertyClass) || propertyInfo.isEnum())
            propertyValue = populateEnumProperty(propertyClass, requestParameterValue);
        else if(PropertyUtil.isNumber(propertyClass) || propertyInfo.isNumber()){
            Boolean useGroupSeparator = propertyInfo.useGroupSeparator();
            Integer precision = propertyInfo.getPrecision();
            
            propertyValue = populateNumberProperty(propertyClass, requestParameterValue, useGroupSeparator, precision);
        }
        else if(PropertyUtil.isDate(propertyClass) || propertyInfo.isDate()){
            String propertyPattern = propertyInfo.getPattern();
            
            propertyValue = populateDateTimeProperty(requestParameterName, requestParameterValue, propertyPattern);
        }
        else if(PropertyUtil.isBoolean(propertyClass) || propertyInfo.isBoolean())
            propertyValue = populateBooleanProperty(requestParameterValue);
        else if(PropertyUtil.isByteArray(propertyClass) || propertyInfo.isByteArray()){
            if(requestParameterInfo.getContentType() != null){
                byte[] content = requestParameterInfo.getContent();
                
                if(content != null && content.length > 0)
                    propertyValue = content;
            }
        }
        else{
            String propertyPattern = propertyInfo.getPattern();
            Boolean persistPattern = propertyInfo.persistPattern();
            InputType inputType = propertyInfo.getInputType();
            
            propertyValue = populateStringProperty(requestParameterName, requestParameterValue, propertyPattern, persistPattern, inputType);
        }
        
        return propertyValue;
    }
    
    /**
     * Populates a numeric property of the data model.
     *
     * @param <N> Class that defines the numeric value.
     * @param propertyClass Class that defines the property.
     * @param propertyValue String that contains the value.
     * @param useGroupSeparator Indicates if the group separator must be considered.
     * @param precision Numeric value that contains the precision.
     * @return Instance that contains the numeric value.
     */
    private <N extends Number> N populateNumberProperty(Class<?> propertyClass, String propertyValue, Boolean useGroupSeparator, Integer precision){
        if(propertyClass != null && propertyValue != null){
            try{
                return NumberUtil.parse(propertyClass, propertyValue, useGroupSeparator, precision, getCurrentLanguage());
            }
            catch(Throwable e){
            }
        }
        
        return null;
    }
    
    /**
     * Populates a date/time property of the data model.
     *
     * @param <D> Class that defines the date/time value.
     * @param propertyName String that contains the identifier of the property.
     * @param propertyValue String that contains the value.
     * @param propertyPattern String that contains the pattern.
     * @return Instance that contains the date/time.
     */
    private <D extends Date> D populateDateTimeProperty(String propertyName, String propertyValue, String propertyPattern){
        if(propertyName != null && propertyName.length() > 0 && propertyValue != null && propertyValue.length() > 0){
            try{
                String propertyPatternBuffer = this.actionFormController.getPropertyPattern(propertyName);
                
                if(propertyPatternBuffer != null && propertyPatternBuffer.length() > 0)
                    propertyPattern = propertyPatternBuffer;
                
                if(propertyPattern != null && propertyPattern.length() > 0)
                    return DateTimeUtil.parse(propertyValue, propertyPattern);
                
                return DateTimeUtil.parse(propertyValue, getCurrentLanguage());
            }
            catch(Throwable e){
            }
        }
        
        return null;
    }
    
    /**
     * Populates a boolean property of the data model.
     *
     * @param propertyValue String that contains the value.
     * @return True/False.
     */
    private Boolean populateBooleanProperty(String propertyValue){
        return Boolean.valueOf(propertyValue);
    }
    
    /**
     * Populates a string property of the data model.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyValue String that contains the value.
     * @param propertyPattern String that contains the pattern.
     * @param persistPattern Indicates if the pattern should be persisted in the string.
     * @param inputType Indicates the type of input should be considered.
     * @return Instance that contains the string.
     */
    private String populateStringProperty(String propertyName, String propertyValue, String propertyPattern, Boolean persistPattern, InputType inputType){
        if(propertyName != null && propertyName.length() > 0){
            String propertyPatternBuffer = this.actionFormController.getPropertyPattern(propertyName);
            
            if(propertyPatternBuffer != null && propertyPatternBuffer.length() > 0)
                propertyPattern = propertyPatternBuffer;
            
            if(propertyPattern != null && propertyPattern.length() > 0)
                if(persistPattern == null || !persistPattern)
                    propertyValue = StringUtil.unformat(propertyValue, propertyPattern);
            
            if(propertyValue != null){
                if(inputType == InputType.UPPERCASE)
                    propertyValue = propertyValue.toUpperCase();
                else if(inputType == InputType.LOWERCASE)
                    propertyValue = propertyValue.toLowerCase();
                else if(inputType == InputType.CAPITALIZE)
                    propertyValue = StringUtil.capitalize(propertyValue, false);
            }
        }
        
        return propertyValue;
    }
    
    /**
     * Populates a data model.
     *
     * @param <M> Class the defines the data model.
     * @param propertyValue String that contains the value.
     * @return Instance that contains the data model.
     */
    private <M extends BaseModel> M populateModelProperty(String propertyValue){
        if(propertyValue != null && propertyValue.length() > 0){
            try{
                return ModelUtil.fromIdentifierString(propertyValue);
            }
            catch(InvocationTargetException | IllegalArgumentException | NoSuchMethodException | IllegalAccessException | InstantiationException | ClassNotFoundException | NoSuchFieldException | IOException e){
            }
        }
        
        return null;
    }
    
    /**
     * Populates a list of data models.
     *
     * @param requestParameterInfo Instance that contains the request parameter.
     * @param propertyInfo Instance that contains the property attributes.
     * @return List of data models.
     */
    private Collection<?> populateCollectionProperty(RequestParameterInfo requestParameterInfo, PropertyInfo propertyInfo){
        if(requestParameterInfo == null || propertyInfo == null)
            return null;
        
        Collection<Object> currentPropertyValues = null;
        
        try{
            Class<?> propertyClass = propertyInfo.getClazz();
            Class<?> propertyItemClass = propertyInfo.getCollectionItemsClass();
            BaseModel model = this.actionForm.getModel();
            
            if(model != null){
                String propertyName = requestParameterInfo.getName();
                String[] propertyValues = requestParameterInfo.getValues();
                
                currentPropertyValues = PropertyUtil.getValue(model, propertyName);
                
                if(propertyValues != null && propertyValues.length > 0){
                    Collection<Object> selectedPropertyValues = PropertyUtil.instantiate(propertyClass);
                    Object selectedPropertyValue = null;
                    
                    for(String propertyValue: propertyValues){
                        if(propertyValue.length() > 0){
                            if(propertyInfo.hasModel() != null && propertyInfo.hasModel())
                                selectedPropertyValue = populateModelProperty(propertyValue);
                            else if(propertyInfo.hasEnum() != null && propertyInfo.hasEnum())
                                selectedPropertyValue = populateEnumProperty(propertyItemClass, propertyValue);
                            else if(propertyInfo.isNumber() != null && propertyInfo.isNumber())
                                selectedPropertyValue = populateNumberProperty(propertyItemClass, propertyValue, propertyInfo.useGroupSeparator(), propertyInfo.getPrecision());
                            else if(propertyInfo.isDate() != null && propertyInfo.isDate())
                                selectedPropertyValue = populateDateTimeProperty(propertyName, propertyValue, propertyInfo.getPattern());
                            else if(propertyInfo.isBoolean() != null && propertyInfo.isBoolean())
                                selectedPropertyValue = populateBooleanProperty(propertyValue);
                            else if(propertyInfo.isString() != null && propertyInfo.isString())
                                selectedPropertyValue = populateStringProperty(propertyName, propertyValue, propertyInfo.getPattern(), propertyInfo.persistPattern(), propertyInfo.getInputType());
                            
                            if(selectedPropertyValue != null)
                                selectedPropertyValues.add(selectedPropertyValue);
                        }
                    }
                    
                    if(currentPropertyValues == null || currentPropertyValues.size() == 0)
                        currentPropertyValues = selectedPropertyValues;
                    else{
                        Integer propertyDatasetStartIndex = this.actionFormController.getPropertyDatasetStartIndex(propertyName);
                        Integer propertyDatasetEndIndex = this.actionFormController.getPropertyDatasetEndIndex(propertyName);
                        
                        if(propertyDatasetEndIndex == null || propertyDatasetEndIndex == 0)
                            currentPropertyValues = selectedPropertyValues;
                        else{
                            List<?> propertyDatasetValues = this.actionFormController.getPropertyDatasetValues(propertyName);
                            
                            if(propertyDatasetValues != null && propertyDatasetValues.size() > 0)
                                propertyDatasetValues = propertyDatasetValues.subList(propertyDatasetStartIndex, propertyDatasetEndIndex);
                            
                            currentPropertyValues.removeAll(propertyDatasetValues);
                            currentPropertyValues.addAll(selectedPropertyValues);
                        }
                    }
                }
            }
        }
        catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
        }
        
        return currentPropertyValues;
    }
    
    /**
     * Populates an enumeration property of the data model.
     *
     * @param propertyClass Class that defines the property.
     * @param propertyValue String that contains the value.
     * @return Instance that contains the enumeration.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Enum<?> populateEnumProperty(Class<?> propertyClass, String propertyValue){
        if(propertyClass != null && propertyValue != null && propertyValue.length() > 0){
            try{
                return Enum.valueOf((Class) propertyClass, propertyValue);
            }
            catch(IllegalArgumentException e){
            }
        }
        
        return null;
    }
}