package br.com.concepting.framework.controller.form;

import br.com.concepting.framework.constants.Constants;
import br.com.concepting.framework.constants.ProjectConstants;
import br.com.concepting.framework.controller.SystemController;
import br.com.concepting.framework.controller.form.annotations.ActionForm;
import br.com.concepting.framework.controller.form.constants.ActionFormConstants;
import br.com.concepting.framework.controller.form.constants.ActionFormMessageConstants;
import br.com.concepting.framework.controller.form.helpers.ActionFormMessage;
import br.com.concepting.framework.controller.form.types.ActionFormMessageType;
import br.com.concepting.framework.controller.form.util.ActionFormMessageUtil;
import br.com.concepting.framework.controller.form.util.ActionFormValidationMessageUtil;
import br.com.concepting.framework.controller.types.ScopeType;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.BaseModel;
import br.com.concepting.framework.model.constants.ModelConstants;
import br.com.concepting.framework.model.types.ConditionType;
import br.com.concepting.framework.ui.constants.UIConstants;
import br.com.concepting.framework.util.NumberUtil;
import br.com.concepting.framework.util.PropertyUtil;
import br.com.concepting.framework.util.ReflectionUtil;
import br.com.concepting.framework.util.StringUtil;
import br.com.concepting.framework.util.types.ByteMetricType;
import br.com.concepting.framework.util.types.ContentType;
import org.apache.commons.beanutils.ConstructorUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.Collection;
import java.util.Set;

/**
 * Class that defines the form controller.
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
public class ActionFormController{
    private String actionFormName = null;
    private SystemController systemController = null;
    
    public ActionFormController(SystemController systemController){
        super();
        
        this.systemController = systemController;
    }

    public ActionFormController(SystemController systemController, String actionFormName){
        this(systemController);
        
        this.actionFormName = actionFormName;
    }
    
    /**
     * Returns the instance of the form.
     *
     * @return Instance that contains the form.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public BaseActionForm<? extends BaseModel> getActionFormInstance() throws InternalErrorException{
        BaseActionForm<? extends BaseModel> actionFormInstance = null;
        
        if(this.actionFormName != null && this.actionFormName.length() > 0){
            actionFormInstance = this.systemController.getAttribute(this.actionFormName, ScopeType.SESSION);
    
            if(actionFormInstance == null){
                Set<Class<?>> actionFormClasses = ReflectionUtil.getTypesAnnotatedWith(ActionForm.class);
        
                if(actionFormClasses != null && !actionFormClasses.isEmpty()){
                    for(Class<?> actionFormClass: actionFormClasses){
                        ActionForm actionForm = actionFormClass.getAnnotation(ActionForm.class);
                        String name = actionForm.name();
    
                        if(name.equals(this.actionFormName)){
                            try{
                                actionFormInstance = (BaseActionForm<? extends BaseModel>) ConstructorUtils.invokeConstructor(actionFormClass, null);
    
                                this.systemController.setAttribute(this.actionFormName, actionFormInstance, ScopeType.SESSION);
    
                                break;
                            }
                            catch(InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e){
                                throw new InternalErrorException(e);
                            }
                        }
                    }
                }
            }
        }
        else{
            String action = StringUtil.replaceAll(this.systemController.getRequestURI(), this.systemController.getContextPath(), "");
    
            action = StringUtil.replaceAll(action, ActionFormConstants.DEFAULT_ACTION_SERVLET_FILE_EXTENSION, "");
            action = StringUtil.replaceAll(action, UIConstants.DEFAULT_PAGES_DIR, "");
    
            int pos = action.indexOf(UIConstants.DEFAULT_PAGE_FILE_EXTENSION);
    
            if(pos >= 0){
                action = action.substring(0, pos);
                pos = action.lastIndexOf("/");
        
                if(pos >= 0)
                    action = action.substring(0, pos);
            }
            
            Set<Class<?>> actionFormClasses = ReflectionUtil.getTypesAnnotatedWith(ActionForm.class);
        
            if(actionFormClasses != null && !actionFormClasses.isEmpty()){
                for(Class<?> actionFormClass: actionFormClasses){
                    ActionForm actionForm = actionFormClass.getAnnotation(ActionForm.class);
            
                    if(action != null && actionForm != null && action.equals(actionForm.action())){
                        this.actionFormName = actionForm.name();
                        
                        actionFormInstance = this.systemController.getAttribute(actionForm.name(), ScopeType.SESSION);
                        
                        if(actionFormInstance == null){
                            try{
                                actionFormInstance = (BaseActionForm<? extends BaseModel>) ConstructorUtils.invokeConstructor(actionFormClass, null);
    
                                this.systemController.setAttribute(this.actionFormName, actionFormInstance, ScopeType.SESSION);
                            }
                            catch(InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e){
                                throw new InternalErrorException(e);
                            }
                        }
                        
                        break;
                    }
                }
            }
        }
        
        return actionFormInstance;
    }
    
    /**
     * Returns the identifier of the property dataset.
     *
     * @param propertyName String that contains the identifier of the property.
     * @return String that contains the identifier of the property dataset.
     */
    private String getPropertyDataset(String propertyName){
        if(propertyName != null && propertyName.length() > 0){
            StringBuilder key = new StringBuilder();
            
            key.append(propertyName);
            key.append(".");
            key.append(ActionFormConstants.DATASET_ATTRIBUTE_ID);
            
            String keyValue = this.systemController.getRequestParameterValue(key.toString());
            
            if(keyValue != null && keyValue.length() > 0){
                StringBuilder result = new StringBuilder();
                ScopeType propertyDatasetScope = getPropertyDatasetScope(propertyName);
                
                if(propertyDatasetScope == ScopeType.MODEL){
                    result.append(this.actionFormName);
                    result.append(".");
                    result.append(ModelConstants.DEFAULT_ID);
                    result.append(".");
                }
                
                result.append(keyValue);
                
                return result.toString();
            }
        }
        
        return null;
    }
    
    /**
     * Returns the instance of the property dataset.
     *
     * @param <C> Class that defines the data set.
     * @param propertyName String that contains the identifier of the property.
     * @return Instance that contains the property dataset.
     */
    public <C extends Collection<?>> C getPropertyDatasetValues(String propertyName){
        if(propertyName != null && propertyName.length() > 0){
            String propertyDataset = getPropertyDataset(propertyName);
            
            if(propertyDataset != null && propertyDataset.length() > 0)
                return this.systemController.getAttribute(propertyDataset, ScopeType.SESSION);
        }
        
        return null;
    }
    
    /**
     * Returns the instance of the scope type of the property dataset.
     *
     * @param propertyName String that contains the identifier of the property.
     * @return Instance that contains the scope type of the property dataset.
     */
    private ScopeType getPropertyDatasetScope(String propertyName){
        if(propertyName != null && propertyName.length() > 0){
            StringBuilder key = new StringBuilder();
            
            key.append(propertyName);
            key.append(".");
            key.append(ActionFormConstants.DATASET_SCOPE_ATTRIBUTE_ID);
            
            try{
                String scopeType = this.systemController.getRequestParameterValue(key.toString());
                
                if(scopeType != null)
                    return ScopeType.valueOf(scopeType.toUpperCase());
                else
                    return ActionFormConstants.DEFAULT_DATASET_SCOPE_TYPE;
            }
            catch(IllegalArgumentException e){
                return ActionFormConstants.DEFAULT_DATASET_SCOPE_TYPE;
            }
        }
        
        return null;
    }
    
    /**
     * Returns the start index of the property dataset.
     *
     * @param propertyName String that contains the identifier of the property.
     * @return Numeric value that contains the index.
     */
    public Integer getPropertyDatasetStartIndex(String propertyName){
        if(propertyName != null && propertyName.length() > 0){
            StringBuilder key = new StringBuilder();
            
            key.append(propertyName);
            key.append(".");
            key.append(ActionFormConstants.DATASET_START_INDEX_ATTRIBUTE_ID);
            
            String value = this.systemController.getRequestParameterValue(key.toString());
            Integer propertyIndex = null;
            
            if(value != null && value.length() > 0){
                try{
                    propertyIndex = NumberUtil.parseInt(value);
                }
                catch(ParseException e){
                }
            }
            
            return propertyIndex;
        }
        
        return null;
    }
    
    /**
     * Returns the end index of the property dataset.
     *
     * @param propertyName String that contains the identifier of the property.
     * @return Numeric value that contains the index.
     */
    public Integer getPropertyDatasetEndIndex(String propertyName){
        if(propertyName != null && propertyName.length() > 0){
            StringBuilder key = new StringBuilder();
            
            key.append(propertyName);
            key.append(".");
            key.append(ActionFormConstants.DATASET_END_INDEX_ATTRIBUTE_ID);
            
            String value = this.systemController.getRequestParameterValue(key.toString());
            Integer propertyIndex = null;
            
            if(value != null && value.length() > 0){
                try{
                    propertyIndex = NumberUtil.parseInt(value);
                }
                catch(ParseException e){
                }
            }
            
            return propertyIndex;
        }
        
        return null;
    }
    
    /**
     * Returns the pattern of the property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @return String that contains the pattern.
     */
    public String getPropertyPattern(String propertyName){
        if(propertyName != null && propertyName.length() > 0){
            StringBuilder key = new StringBuilder();
            
            key.append(propertyName);
            key.append(".");
            key.append(Constants.PATTERN_ATTRIBUTE_ID);
            
            String encoding = this.systemController.getEncoding();
            String value = this.systemController.getRequestParameterValue(key.toString());
            
            if(value != null && value.length() > 0){
                try{
                    value = URLDecoder.decode(value, encoding);
                }
                catch(UnsupportedEncodingException e){
                }
            }
            
            return value;
        }
        
        return null;
    }
    
    /**
     * Returns the label of the property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @return String that contains the label.
     */
    public String getPropertyLabel(String propertyName){
        if(propertyName != null && propertyName.length() > 0){
            StringBuilder key = new StringBuilder();
            
            key.append(propertyName);
            key.append(".");
            key.append(Constants.LABEL_ATTRIBUTE_ID);
            
            String encoding = this.systemController.getEncoding();
            String value = this.systemController.getRequestParameterValue(key.toString());
            
            if(value != null && value.length() > 0){
                try{
                    value = URLDecoder.decode(value, encoding);
                }
                catch(UnsupportedEncodingException e){
                }
            }
            
            if(value == null || value.length() == 0)
                value = propertyName;
            
            return value;
        }
        
        return null;
    }
    
    /**
     * Clear all messages.
     *
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void clearAllMessages() throws InternalErrorException{
        clearMessages(ActionFormMessageType.INFO);
        clearMessages(ActionFormMessageType.ERROR);
        clearMessages(ActionFormMessageType.WARNING);
        clearMessages(ActionFormMessageType.VALIDATION);
        
        if(this.systemController != null)
            this.systemController.setCurrentException(null);
    }
    
    /**
     * Clear the messages of a specific type.
     *
     * @param type Instance that contains the type of the message.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void clearMessages(ActionFormMessageType type) throws InternalErrorException{
        BaseActionForm<? extends BaseModel> actionFormInstance = getActionFormInstance();
        
        if(actionFormInstance != null)
            actionFormInstance.clearMessages(type);
    }
    
    /**
     * Returns a list of messages for a specific type.
     *
     * @param <C> Class that defines the list of messages.
     * @param type Instance that contains the type of the message.
     * @return Instance that contains the list of messages.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public <C extends Collection<ActionFormMessage>> C getMessages(ActionFormMessageType type) throws InternalErrorException{
        if(type != null){
            BaseActionForm<? extends BaseModel> actionFormInstance = getActionFormInstance();
            
            if(actionFormInstance != null)
                return actionFormInstance.getMessages(type);
        }
        
        return null;
    }
    
    /**
     * Verifies if a property of a data model has validation messages.
     *
     * @param propertyName String that contains the identifier of the property.
     * @return True/False.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public Boolean hasValidationMessage(String propertyName) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0){
            Collection<ActionFormMessage> validationMessages = getValidationMessages(propertyName);
            
            return (validationMessages != null && validationMessages.size() > 0);
        }
        
        return null;
    }
    
    /**
     * Returns a list containing all validation messages.
     *
     * @param <C> Class that defines the list of messages.
     * @param propertyName String that contains the identifier of the property.
     * @return List containing all validation messages.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    @SuppressWarnings("unchecked")
    public <C extends Collection<ActionFormMessage>> C getValidationMessages(String propertyName) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0){
            Collection<ActionFormMessage> validationMessages = getMessages(ActionFormMessageType.VALIDATION);
            
            if(validationMessages != null && !validationMessages.isEmpty()){
                Collection<ActionFormMessage> result = null;
                
                for(ActionFormMessage validationMessage: validationMessages){
                    String value = validationMessage.getAttribute(Constants.NAME_ATTRIBUTE_ID);
                    
                    if(value != null && value.equals(propertyName)){
                        if(result == null)
                            result = PropertyUtil.instantiate(Constants.DEFAULT_LIST_CLASS);
                        
                        result.add(validationMessage);
                    }
                }
                
                return (C) result;
            }
        }
        
        return null;
    }
    
    /**
     * Adds a message.
     *
     * @param actionFormMessage Instance that contains the message.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addMessage(ActionFormMessage actionFormMessage) throws InternalErrorException{
        if(actionFormMessage != null){
            BaseActionForm<? extends BaseModel> actionFormInstance = getActionFormInstance();
            
            if(actionFormInstance != null)
                actionFormInstance.addMessage(actionFormMessage);
        }
    }
    
    /**
     * Adds a success information message.
     *
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addSuccessMessage() throws InternalErrorException{
        addInfoMessage(ActionFormMessageConstants.DEFAULT_SUCCESS_ID);
    }
    
    /**
     * Adds an information message.
     *
     * @param key String that contains the identifier.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addInfoMessage(String key) throws InternalErrorException{
        if(key != null && key.length() > 0)
            addMessage(ActionFormMessageUtil.createInfoMessage(key));
    }
    
    /**
     * Adds a warning message.
     *
     * @param key String that contains the identifier.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addWarningMessage(String key) throws InternalErrorException{
        if(key != null && key.length() > 0)
            addMessage(ActionFormMessageUtil.createWarningMessage(key));
    }
    
    /**
     * Adds an error message.
     *
     * @param key String that contains the identifier.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addErrorMessage(String key) throws InternalErrorException{
        if(key != null && key.length() > 0)
            addMessage(ActionFormMessageUtil.createErrorMessage(key));
    }
    
    /**
     * Adds a message based on a caught exception.
     *
     * @param e Instance that contains the caught exception.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addMessage(Throwable e) throws InternalErrorException{
        if(e != null)
            addMessage(ActionFormMessageUtil.createMessage(e));
    }
    
    /**
     * Adds a content type validation message.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyContentType Instance that contains the content type of the property.
     * @param permittedContentTypes Array that contains the permitted content types.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addContentTypeValidationMessage(String propertyName, ContentType propertyContentType, ContentType permittedContentTypes[]) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0 && propertyContentType != null && permittedContentTypes != null && permittedContentTypes.length > 0)
            addContentTypeValidationMessage(propertyName, getPropertyLabel(propertyName), propertyContentType, permittedContentTypes);
    }
    
    /**
     * Adds a content type validation message.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyLabel String that contains the label of the property.
     * @param propertyContentType Instance that contains the content type of the property.
     * @param permittedContentTypes Array that contains the permitted content types.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addContentTypeValidationMessage(String propertyName, String propertyLabel, ContentType propertyContentType, ContentType permittedContentTypes[]) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0 && propertyContentType != null && permittedContentTypes != null && permittedContentTypes.length > 0)
            addMessage(ActionFormValidationMessageUtil.createContentTypeValidationMessage(propertyName, propertyLabel, propertyContentType, permittedContentTypes));
    }
    
    /**
     * Adds a content size validation message.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyContentSize Numeric value that contains the size of the content.
     * @param propertyContentSizeUnit Instance that content the content size unit.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addContentSizeValidationMessage(String propertyName, Double propertyContentSize, ByteMetricType propertyContentSizeUnit) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0 && propertyContentSize != null && propertyContentSize > 0 && propertyContentSizeUnit != null)
            addContentSizeValidationMessage(propertyName, getPropertyLabel(propertyName), propertyContentSize, propertyContentSizeUnit);
    }
    
    /**
     * Adds a content size validation message.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyLabel String that contains the label of the property.
     * @param propertyContentSize Numeric value that contains the size of the content.
     * @param propertyContentSizeUnit Instance that content the content size unit.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addContentSizeValidationMessage(String propertyName, String propertyLabel, Double propertyContentSize, ByteMetricType propertyContentSizeUnit) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0 && propertyContentSize != null && propertyContentSize > 0 && propertyContentSizeUnit != null)
            addMessage(ActionFormValidationMessageUtil.createContentSizeValidationMessage(propertyName, propertyLabel, propertyContentSize, propertyContentSizeUnit));
    }
    
    /**
     * Adds a property required validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addRequiredValidationMessage(String propertyName) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0)
            addRequiredValidationMessage(propertyName, getPropertyLabel(propertyName));
    }
    
    /**
     * Adds a property required validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyLabel String that contains the label of the property.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addRequiredValidationMessage(String propertyName, String propertyLabel) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0)
            addMessage(ActionFormValidationMessageUtil.createRequiredValidationMessage(propertyName, propertyLabel));
    }
    
    /**
     * Adds a date/time validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addDateTimeValidationMessage(String propertyName) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0)
            addDateTimeValidationMessage(propertyName, getPropertyLabel(propertyName), getPropertyPattern(propertyName));
    }
    
    /**
     * Adds a date/time validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyLabel String that contains the label of the property.
     * @param propertyPattern String that contains the pattern.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addDateTimeValidationMessage(String propertyName, String propertyLabel, String propertyPattern) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0)
            addMessage(ActionFormValidationMessageUtil.createDateTimeValidationMessage(propertyName, propertyLabel, propertyPattern));
    }
    
    /**
     * Adds a numeric validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addNumberValidationMessage(String propertyName) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0)
            addNumberValidationMessage(propertyName, getPropertyLabel(propertyName));
    }
    
    /**
     * Adds a numeric validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyLabel String that contains the label of the property.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addNumberValidationMessage(String propertyName, String propertyLabel) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0)
            addMessage(ActionFormValidationMessageUtil.createNumberValidationMessage(propertyName, propertyLabel));
    }
    
    /**
     * Adds a comparison validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyCompareCondition Instance that contains the comparison
     * type.
     * @param propertyCompareName String that contains the identifier of the
     * comparison property.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addCompareValidationMessage(String propertyName, ConditionType propertyCompareCondition, String propertyCompareName) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0 && propertyCompareCondition != null && propertyCompareName != null && propertyCompareName.length() > 0)
            addCompareValidationMessage(propertyName, getPropertyLabel(propertyName), propertyCompareCondition, propertyCompareName, getPropertyLabel(propertyName));
    }
    
    /**
     * Adds a comparison validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyLabel String that contains the label of the property.
     * @param propertyCompareCondition Instance that contains the comparison
     * type.
     * @param propertyCompareName String that contains the identifier of the
     * comparison property.
     * @param propertyCompareLabel String that contains the label of the
     * comparison property.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addCompareValidationMessage(String propertyName, String propertyLabel, ConditionType propertyCompareCondition, String propertyCompareName, String propertyCompareLabel) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0 && propertyCompareCondition != null && propertyCompareName != null && propertyCompareName.length() > 0)
            addMessage(ActionFormValidationMessageUtil.createCompareValidationMessage(propertyName, propertyLabel, propertyCompareCondition, propertyCompareName, propertyCompareLabel));
    }
    
    /**
     * Adds a word count validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyWordCount Numeric value that contains the word count.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addWordCountValidationMessage(String propertyName, Integer propertyWordCount) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0 && propertyWordCount != null && propertyWordCount > 0)
            addWordCountValidationMessage(propertyName, getPropertyLabel(propertyName), propertyWordCount);
    }
    
    /**
     * Adds a word count validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyLabel String that contains the label of the property.
     * @param propertyWordCount Numeric value that contains the word count.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addWordCountValidationMessage(String propertyName, String propertyLabel, Integer propertyWordCount) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0 && propertyWordCount != null && propertyWordCount > 0)
            addMessage(ActionFormValidationMessageUtil.createWordCountValidationMessage(propertyName, propertyLabel, propertyWordCount));
    }
    
    /**
     * Adds a minimum length validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyMinimumLength Numeric value that contains the minimum
     * length.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addMinimumLengthValidationMessage(String propertyName, Integer propertyMinimumLength) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0 && propertyMinimumLength != null && propertyMinimumLength > 0)
            addMinimumLengthValidationMessage(propertyName, getPropertyLabel(propertyName), propertyMinimumLength);
    }
    
    /**
     * Adds a minimum length validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyLabel String that contains the label of the property.
     * @param propertyMinimumLength Numeric value that contains the minimum
     * length.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addMinimumLengthValidationMessage(String propertyName, String propertyLabel, Integer propertyMinimumLength) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0 && propertyMinimumLength != null && propertyMinimumLength > 0)
            addMessage(ActionFormValidationMessageUtil.createMinimumLengthValidationMessage(propertyName, propertyLabel, propertyMinimumLength));
    }
    
    /**
     * Adds a maximum length validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyMaximumLength Numeric value that contains the maximum
     * length.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addMaximumLengthValidationMessage(String propertyName, Integer propertyMaximumLength) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0 && propertyMaximumLength != null && propertyMaximumLength > 0)
            addMaximumLengthValidationMessage(propertyName, getPropertyLabel(propertyName), propertyMaximumLength);
    }
    
    /**
     * Adds a maximum length validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyLabel String that contains the label of the property.
     * @param propertyMaximumLength Numeric value that contains the maximum
     * length.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addMaximumLengthValidationMessage(String propertyName, String propertyLabel, Integer propertyMaximumLength) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0 && propertyMaximumLength != null && propertyMaximumLength > 0)
            addMessage(ActionFormValidationMessageUtil.createMaximumLengthValidationMessage(propertyName, propertyLabel, propertyMaximumLength));
    }
    
    /**
     * Adds a regular expression validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyRegex String that contains the regular expression.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addRegularExpressionValidationMessage(String propertyName, String propertyRegex) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0 && propertyRegex != null && propertyRegex.length() > 0)
            addRegularExpressionValidationMessage(propertyName, getPropertyLabel(propertyName), propertyRegex);
    }
    
    /**
     * Adds a regular expression validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyLabel String that contains the label of the property.
     * @param propertyRegex String that contains the regular expression.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addRegularExpressionValidationMessage(String propertyName, String propertyLabel, String propertyRegex) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0 && propertyRegex != null && propertyRegex.length() > 0)
            addMessage(ActionFormValidationMessageUtil.createRegularExpressionValidationMessage(propertyName, propertyLabel, propertyRegex));
    }
    
    /**
     * Adds a e-Mail validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addEmailValidationMessage(String propertyName) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0)
            addEmailValidationMessage(propertyName, getPropertyLabel(propertyName));
    }
    
    /**
     * Adds a e-Mail validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyLabel String that contains the label of the property.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addEmailValidationMessage(String propertyName, String propertyLabel) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0)
            addMessage(ActionFormValidationMessageUtil.createEmailValidationMessage(propertyName, propertyLabel));
    }
    
    /**
     * Adds a pattern validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addPatternValidationMessage(String propertyName) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0)
            addPatternValidationMessage(propertyName, getPropertyLabel(propertyName), getPropertyPattern(propertyName));
    }
    
    /**
     * Adds a pattern validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyLabel String that contains the label of the property.
     * @param propertyPattern String that contains the pattern of the property.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addPatternValidationMessage(String propertyName, String propertyLabel, String propertyPattern) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0 && propertyPattern != null && propertyPattern.length() > 0)
            addMessage(ActionFormValidationMessageUtil.createPatternValidationMessage(propertyName, propertyLabel, propertyPattern));
    }
    
    /**
     * Adds a numeric range validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyStartRange Numeric value the contains the start range.
     * @param propertyEndRange Numeric value the contains the end range.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addRangeValidationMessage(String propertyName, Object propertyStartRange, Object propertyEndRange) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0 && propertyStartRange != null && propertyEndRange != null)
            addRangeValidationMessage(propertyName, getPropertyLabel(propertyName), propertyStartRange, propertyEndRange);
    }
    
    /**
     * Adds a numeric range validation message for a data model property.
     *
     * @param propertyName String that contains the identifier of the property.
     * @param propertyLabel String that contains the label of the property.
     * @param propertyStartRange Numeric value the contains the start range.
     * @param propertyEndRange Numeric value the contains the end range.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addRangeValidationMessage(String propertyName, String propertyLabel, Object propertyStartRange, Object propertyEndRange) throws InternalErrorException{
        if(propertyName != null && propertyName.length() > 0 && propertyStartRange != null && propertyEndRange != null)
            addMessage(ActionFormValidationMessageUtil.createRangeValidationMessage(propertyName, propertyLabel, propertyStartRange, propertyEndRange));
    }
    
    /**
     * Adds a custom validation message for a data model property.
     *
     * @param propertyValidation String that contains the identifier of the
     * validation.
     * @param propertyName String that contains the identifier of the property.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addCustomValidationMessage(String propertyValidation, String propertyName) throws InternalErrorException{
        if(propertyValidation != null && propertyValidation.length() > 0 && propertyName != null && propertyName.length() > 0)
            addCustomValidationMessage(propertyValidation, propertyName, getPropertyLabel(propertyName));
    }
    
    /**
     * Adds a custom validation message for a data model property.
     *
     * @param propertyValidation String that contains the identifier of the
     * validation.
     * @param propertyName String that contains the identifier of the property.
     * @param propertyLabel String that contains the label of the property.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    public void addCustomValidationMessage(String propertyValidation, String propertyName, String propertyLabel) throws InternalErrorException{
        if(propertyValidation != null && propertyValidation.length() > 0 && propertyName != null && propertyName.length() > 0)
            addMessage(ActionFormValidationMessageUtil.createCustomValidationMessage(propertyValidation, propertyName, propertyLabel));
    }
}