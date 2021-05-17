package br.com.concepting.framework.persistence.util;

import br.com.concepting.framework.constants.Constants;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.BaseModel;
import br.com.concepting.framework.model.helpers.ModelInfo;
import br.com.concepting.framework.model.types.ConditionOperationType;
import br.com.concepting.framework.model.types.ConditionType;
import br.com.concepting.framework.model.util.ModelUtil;
import br.com.concepting.framework.persistence.HibernatePersistence;
import br.com.concepting.framework.persistence.constants.PersistenceConstants;
import br.com.concepting.framework.persistence.types.QueryType;
import br.com.concepting.framework.persistence.types.RelationJoinType;
import br.com.concepting.framework.util.*;
import br.com.concepting.framework.util.helpers.Filter;
import br.com.concepting.framework.util.helpers.PropertyInfo;
import br.com.concepting.framework.util.types.FormulaType;
import br.com.concepting.framework.util.types.SearchType;
import br.com.concepting.framework.util.types.SortOrderType;
import org.apache.commons.beanutils.ConstructorUtils;
import org.hibernate.CacheMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class responsible to build persistence hibernate queries.
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
public abstract class HibernateQueryBuilder{
    /**
     * Builds a query expression based on a data model.
     *
     * @param <M> Class that defines the data model.
     * @param model Instance that contains the data model.
     * @param filter Instance that contains the search criteria.
     * @param whereClauseParameters Map that contains the search criteria.
     * @return String that contains the query expression.
     * @throws InternalErrorException Occurs when was not possible to build the
     * query.
     */
    public static <M extends BaseModel> String buildExpression(M model, Filter filter, Map<String, Object> whereClauseParameters) throws InternalErrorException{
        String propertyPrefix = "";
        String propertyAlias = "";
        StringBuilder expression = new StringBuilder();
        StringBuilder fieldsClause = new StringBuilder();
        StringBuilder fromClause = new StringBuilder();
        StringBuilder whereClause = new StringBuilder();
        StringBuilder joinClause = new StringBuilder();
        StringBuilder groupByClause = new StringBuilder();
        StringBuilder orderByClause = new StringBuilder();
        Collection<String> processedRelations = PropertyUtil.instantiate(Constants.DEFAULT_LIST_CLASS);
        
        buildExpression(model, filter, propertyPrefix, propertyAlias, fieldsClause, fromClause, joinClause, whereClause, groupByClause, orderByClause, whereClauseParameters, processedRelations, true, false, false);
        
        if(fieldsClause.length() > 0){
            expression.append(fieldsClause);
            expression.append(" ");
        }
        
        expression.append(fromClause);
        
        if(joinClause.length() > 0){
            expression.append(" ");
            expression.append(joinClause);
        }
        
        if(whereClause.length() > 0){
            expression.append(" ");
            expression.append(whereClause);
            expression.append(")");
        }
        
        if(groupByClause.length() > 0){
            expression.append(" ");
            expression.append(groupByClause);
        }
        
        if(orderByClause.length() > 0){
            expression.append(" ");
            expression.append(orderByClause);
        }
        
        return expression.toString();
    }
    
    /**
     * Builds a query expression based on a data model.
     *
     * @param <M> Class that defines the data model.
     * @param model Instance that contains the data model.
     * @param filter Instance that contains the search criteria.
     * @param propertyPrefix String that contains the properties prefix.
     * @param propertyAlias String that contains the properties alias.
     * @param fieldsClause String that contains the return properties.
     * @param fromClause String that contains the FROM clause.
     * @param joinClause String that contains the JOIN clause.
     * @param whereClause String that contains the WHERE clause.
     * @param groupByClause String that contains the GROUP BY clause.
     * @param orderByClause String that contains the ORDER BY clause.
     * @param whereClauseParameters Map that contains the search criteria.
     * @param processedRelations List that contains the relationships already
     * processed.
     * @param considerConditions Indicates if the search criteria will be
     * considered.
     * @param relationIsComponent Indicates if the relation is a data component.
     * @param relationHasModel Indicates if the relation has data models.
     * @throws InternalErrorException Occurs when was not possible to build the
     * query.
     */
    @SuppressWarnings("unchecked")
    public static <M extends BaseModel> void buildExpression(M model, Filter filter, String propertyPrefix, String propertyAlias, StringBuilder fieldsClause, StringBuilder fromClause, StringBuilder joinClause, StringBuilder whereClause, StringBuilder groupByClause, StringBuilder orderByClause, Map<String, Object> whereClauseParameters, Collection<String> processedRelations, Boolean considerConditions, Boolean relationIsComponent, Boolean relationHasModel) throws InternalErrorException{
        try{
            Class<? extends BaseModel> modelClass = model.getClass();
            ModelInfo modelInfo = ModelUtil.getInfo(modelClass);
            StringBuilder propertyAliasBuffer = null;
            
            if(fromClause.length() == 0){
                propertyAliasBuffer = new StringBuilder();
                propertyAliasBuffer.append(modelInfo.getClazz().getSimpleName().toLowerCase());
                propertyAliasBuffer.append((int) (Math.random() * NumberUtil.getMaximumRange(Integer.class)));
                
                propertyAlias = propertyAliasBuffer.toString();
                
                fromClause.append("from ");
                fromClause.append(modelInfo.getClazz().getName());
                fromClause.append(" ");
                fromClause.append(propertyAlias);
            }
            
            Collection<String> returnProperties = (filter != null ? filter.getReturnProperties() : null);
            Map<String, FormulaType> propertiesFormulas = (filter != null ? filter.getPropertiesFormulas() : null);
            Map<String, String> propertiesAliases = (filter != null ? filter.getPropertiesAliases() : null);
            Map<String, RelationJoinType> propertiesRelationsJoins = (filter != null ? filter.getPropertiesRelationsJoins() : null);
            Map<String, Collection<ConditionType>> propertiesConditions = (filter != null ? filter.getPropertiesConditions() : null);
            Map<String, ConditionOperationType> propertiesConditionsOperations = (filter != null ? filter.getPropertiesConditionsOperations() : null);
            Map<String, Collection<Object>> propertiesValues = (filter != null ? filter.getPropertiesValues() : null);
            Map<String, SortOrderType> propertiesSortOrders = (filter != null ? filter.getPropertiesSortOrders() : null);
            Collection<String> groupByProperties = (filter != null ? filter.getGroupByProperties() : null);
            Collection<PropertyInfo> propertiesInfo = modelInfo.getPropertiesInfo();
            Collection<PropertyInfo> identitiesPropertiesInfo = modelInfo.getIdentityPropertiesInfo();
            Collection<PropertyInfo> identitiesPropertiesInfoBuffer = PropertyUtil.instantiate(Constants.DEFAULT_LIST_CLASS);
            StringBuilder propertyIdBuffer = null;
            StringBuilder propertyPrefixBuffer = null;
            StringBuilder propertyParamBuffer = null;
            Boolean propertyIsIdentity = false;
            Boolean propertyIsForSearch = false;
            Collection<ConditionType> propertyCondition = null;
            ConditionOperationType propertyConditionOperation = null;
            Collection<Object> propertyValue = null;
            Object propertyValueBuffer = null;
            SortOrderType propertySortOrder = null;
            RelationJoinType relationJoinType = null;
            Class<?> relationModelClass = null;
            ModelInfo relationModelInfo = null;
            Collection<PropertyInfo> relationModelIdentitiesInfo = null;
            M relationModel = null;
            Object relationModelIdentityValue = null;
            Boolean relationModelHasIdentities = true;
            Boolean processRelation = false;
            Boolean processCondition = false;
            
            for(PropertyInfo propertyInfo: propertiesInfo){
                propertyIsIdentity = propertyInfo.isIdentity();
                propertyIsForSearch = propertyInfo.isForSearch();
                
                if(propertyPrefixBuffer == null)
                    propertyPrefixBuffer = new StringBuilder();
                else
                    propertyPrefixBuffer.delete(0, propertyPrefixBuffer.length());
                
                if(propertyPrefix.length() > 0){
                    propertyPrefixBuffer.append(propertyPrefix);
                    propertyPrefixBuffer.append(".");
                }
                
                propertyPrefixBuffer.append(propertyInfo.getId());
                
                if(propertyIdBuffer == null)
                    propertyIdBuffer = new StringBuilder();
                else
                    propertyIdBuffer.delete(0, propertyIdBuffer.length());
                
                if(propertyAlias.length() > 0){
                    propertyIdBuffer.append(propertyAlias);
                    propertyIdBuffer.append(".");
                }
                
                propertyIdBuffer.append(propertyInfo.getId());
                
                if((returnProperties != null && returnProperties.contains(propertyPrefixBuffer.toString())) || (propertiesFormulas != null && propertiesFormulas.containsKey(propertyPrefixBuffer.toString()))){
                    if(fieldsClause.length() == 0)
                        fieldsClause.append("select ");
                    else
                        fieldsClause.append(",");
                    
                    if(propertiesFormulas != null && propertiesFormulas.containsKey(propertyPrefixBuffer.toString())){
                        FormulaType formula = propertiesFormulas.get(propertyPrefixBuffer.toString());
                        
                        fieldsClause.append(formula.getId());
                        fieldsClause.append("(");
                    }
                    
                    fieldsClause.append(propertyIdBuffer);
                    
                    if(propertiesFormulas != null && propertiesFormulas.containsKey(propertyPrefixBuffer.toString()))
                        fieldsClause.append(")");
                    
                    fieldsClause.append(" as ");
                    
                    if(propertiesAliases != null && propertiesAliases.containsKey(propertyPrefixBuffer.toString()))
                        fieldsClause.append(propertiesAliases.get(propertyPrefixBuffer.toString()));
                    else
                        fieldsClause.append(propertyInfo.getId());
                }
                else if(returnProperties == null){
                    if(fieldsClause.length() == 0){
                        fieldsClause.append("select ");
                        fieldsClause.append(propertyAlias);
                    }
                }
                
                if((groupByProperties != null && groupByProperties.contains(propertyPrefixBuffer.toString())) || (groupByProperties == null && propertiesFormulas == null && propertyPrefix.length() == 0 && propertyIsIdentity != null && propertyIsIdentity && propertyInfo.getMappedPropertyId() != null && propertyInfo.getMappedPropertyId().length() > 0)){
                    if(groupByClause.length() == 0)
                        groupByClause.append("group by ");
                    else
                        groupByClause.append(", ");
                    
                    groupByClause.append(propertyIdBuffer.toString());
                }
                
                if(relationHasModel == null || !relationHasModel){
                    propertySortOrder = (propertiesSortOrders != null ? propertiesSortOrders.get(propertyPrefixBuffer.toString()) : null);
                    
                    if(propertySortOrder == null && (propertiesSortOrders == null || !propertiesSortOrders.containsKey(propertyPrefixBuffer.toString())))
                        propertySortOrder = propertyInfo.getSortOrder();
                    
                    if(propertySortOrder != null && propertySortOrder != SortOrderType.NONE && propertyInfo.getMappedPropertyId() != null && propertyInfo.getMappedPropertyId().length() > 0){
                        if(orderByClause.length() == 0)
                            orderByClause.append("order by ");
                        else
                            orderByClause.append(", ");
                        
                        orderByClause.append(propertyIdBuffer.toString());
                        orderByClause.append(" ");
                        orderByClause.append(propertySortOrder.getId());
                    }
                }
                
                propertyValue = (propertiesValues != null ? propertiesValues.get(propertyPrefixBuffer.toString()) : null);
                propertyCondition = (propertiesConditions != null ? propertiesConditions.get(propertyPrefixBuffer.toString()) : null);
                relationJoinType = (propertiesRelationsJoins != null ? propertiesRelationsJoins.get(propertyPrefixBuffer.toString()) : null);
                relationModelInfo = null;
                relationModelIdentitiesInfo = null;
                relationModelIdentityValue = null;
                relationModelHasIdentities = true;
                relationModelClass = null;
                relationModel = null;
                
                if((propertyCondition != null && propertyCondition.size() > 0) || (propertyValue != null && propertyValue.size() > 0)){
                    propertyIsForSearch = propertyCondition == null || propertyCondition.size() <= 0 || !propertyCondition.contains(ConditionType.NONE);
                }
                
                if(propertyValue == null && (propertiesValues == null || !propertiesValues.containsKey(propertyPrefixBuffer.toString()))){
                    if(propertyIsForSearch != null && propertyIsForSearch){
                        if(propertyInfo.getSearchPropertyId() != null && propertyInfo.getSearchPropertyId().length() > 0)
                            propertyValueBuffer = PropertyUtil.getValue(model, propertyInfo.getSearchPropertyId());
                        else
                            propertyValueBuffer = PropertyUtil.getValue(model, propertyInfo.getId());
                    }
                    else
                        propertyValueBuffer = PropertyUtil.getValue(model, propertyInfo.getId());
                    
                    if(propertyValueBuffer != null)
                        propertyValue = Arrays.asList(propertyValueBuffer);
                }
                
                if(relationJoinType == null && (propertiesRelationsJoins == null || !propertiesRelationsJoins.containsKey(propertyPrefixBuffer.toString())))
                    relationJoinType = propertyInfo.getRelationJoinType();
                
                if((relationJoinType != RelationJoinType.NONE && ((propertyInfo.getMappedPropertiesIds() != null && propertyInfo.getMappedPropertiesIds().length > 0) || (propertyInfo.getMappedRelationPropertiesIds() != null && propertyInfo.getMappedRelationPropertiesIds().length > 0))) || ((propertyIsIdentity != null && propertyIsIdentity && (propertyInfo.getMappedPropertiesIds() != null && propertyInfo.getMappedPropertiesIds().length > 0) || (propertyInfo.getMappedRelationPropertiesIds() != null && propertyInfo.getMappedRelationPropertiesIds().length > 0)) || (propertyIsForSearch != null && propertyIsForSearch && (propertyInfo.getMappedPropertiesIds() != null && propertyInfo.getMappedPropertiesIds().length > 0) || (propertyInfo.getMappedRelationPropertiesIds() != null && propertyInfo.getMappedRelationPropertiesIds().length > 0)) || (propertyInfo.getPropertiesIds() != null && propertyInfo.getPropertiesIds().length > 0 && propertyInfo.getMappedPropertiesIds() != null && propertyInfo.getMappedPropertiesIds().length > 0))){
                    processRelation = !processedRelations.contains(propertyPrefixBuffer.toString()) && !propertyPrefixBuffer.toString().contains("parent.parent");
                    
                    if(processRelation){
                        if(propertyInfo.isModel() != null && propertyInfo.isModel())
                            relationModelClass = propertyInfo.getClazz();
                        else if(propertyInfo.hasModel() != null && propertyInfo.hasModel())
                            relationModelClass = propertyInfo.getCollectionItemsClass();
                        
                        if(relationJoinType != RelationJoinType.NONE || (propertyInfo.getPropertiesIds() != null && propertyInfo.getPropertiesIds().length > 0)){
                            if(propertyInfo.isModel() != null && propertyInfo.isModel()){
                                if(propertyValueBuffer == null || PropertyUtil.isCollection(propertyValueBuffer))
                                    relationModel = (M) ConstructorUtils.invokeConstructor(relationModelClass, null);
                                else
                                    relationModel = (M) propertyValueBuffer;
                            }
                            else if(propertyInfo.hasModel() != null && propertyInfo.hasModel())
                                relationModel = (M) ConstructorUtils.invokeConstructor(relationModelClass, null);
                        }
                        else if((propertyIsIdentity != null && propertyIsIdentity) || (propertyIsForSearch != null && propertyIsForSearch)){
                            if(propertyInfo.getSearchPropertyId() != null && propertyInfo.getSearchPropertyId().length() > 0){
                                PropertyInfo relationSearchPropertyInfo = modelInfo.getPropertyInfo(propertyInfo.getSearchPropertyId());
                                
                                if(relationSearchPropertyInfo.isModel() != null && relationSearchPropertyInfo.isModel()){
                                    relationModel = PropertyUtil.getValue(model, propertyInfo.getSearchPropertyId());
                                    
                                    if(relationModel != null){
                                        relationModelInfo = ModelUtil.getInfo(relationModel.getClass());
                                        relationModelIdentitiesInfo = relationModelInfo.getIdentityPropertiesInfo();
                                        
                                        if(relationModelIdentitiesInfo != null && !relationModelIdentitiesInfo.isEmpty()){
                                            for(PropertyInfo relationModelIdentityInfo: relationModelIdentitiesInfo){
                                                relationModelIdentityValue = PropertyUtil.getValue(relationModel, relationModelIdentityInfo.getId());
    
                                                relationModelHasIdentities = relationModelIdentityValue != null;
                                            }
                                        }
                                    }
                                }
                                else if(relationSearchPropertyInfo.hasModel() != null && relationSearchPropertyInfo.hasModel()){
                                    relationModelClass = relationSearchPropertyInfo.getCollectionItemsClass();
                                    
                                    try{
                                        relationModel = (M) ConstructorUtils.invokeConstructor(relationModelClass, null);
                                    }
                                    catch(Throwable e){
                                        relationModel = null;
                                    }
                                }
                            }
                            else{
                                if(propertyInfo.isModel() != null && propertyInfo.isModel()){
                                    relationModel = (M) propertyValueBuffer;
                                    
                                    if(relationModel == null){
                                        try{
                                            relationModel = (M) ConstructorUtils.invokeConstructor(relationModelClass, null);
                                        }
                                        catch(Throwable e){
                                            relationModel = null;
                                        }
                                    }
                                    
                                    if(propertyIsIdentity != null && propertyIsIdentity)
                                        relationJoinType = RelationJoinType.INNER_JOIN;
                                    else
                                        relationJoinType = RelationJoinType.LEFT_JOIN;
                                }
                                else if(propertyInfo.hasModel() != null && propertyInfo.hasModel()){
                                    try{
                                        relationModel = (M) ConstructorUtils.invokeConstructor(relationModelClass, null);
                                    }
                                    catch(Throwable e){
                                        relationModel = null;
                                    }
                                    
                                    relationJoinType = RelationJoinType.LEFT_JOIN;
                                }
                            }
                        }
                        
                        if(relationModel != null){
                            processedRelations.add(propertyPrefixBuffer.toString());
                            
                            if(propertyInfo.getPropertiesIds() == null || propertyInfo.getPropertiesIds().length == 0){
                                if(propertyAliasBuffer == null)
                                    propertyAliasBuffer = new StringBuilder();
                                else
                                    propertyAliasBuffer.delete(0, propertyAliasBuffer.length());
                                
                                propertyAliasBuffer.append(relationModelClass.getSimpleName().toLowerCase());
                                propertyAliasBuffer.append((int) (Math.random() * NumberUtil.getMaximumRange(Integer.class)));
                                
                                if(joinClause.length() > 0)
                                    joinClause.append(" ");
                                
                                joinClause.append(relationJoinType.getOperator());
                                joinClause.append(" ");
                                joinClause.append(propertyIdBuffer);
                                joinClause.append(" ");
                                joinClause.append(propertyAliasBuffer);
                                
                                if(!((propertyIsForSearch != null && propertyIsForSearch) && relationModelHasIdentities)){
                                    buildExpression(relationModel, filter, propertyPrefixBuffer.toString(), propertyAliasBuffer.toString(), fieldsClause, fromClause, joinClause, whereClause, groupByClause, orderByClause, whereClauseParameters, processedRelations, considerConditions, false, propertyInfo.hasModel());
                                    
                                    continue;
                                }
                                else{
                                    propertyIdBuffer.delete(0, propertyIdBuffer.length());
                                    propertyIdBuffer.append(propertyAliasBuffer);
                                }
                            }
                            else
                                buildExpression(relationModel, filter, propertyPrefixBuffer.toString(), propertyAlias, fieldsClause, fromClause, joinClause, whereClause, groupByClause, orderByClause, whereClauseParameters, processedRelations, true, true, propertyInfo.hasModel());
                        }
                    }
                }
                
                if(((propertyIsIdentity != null && propertyIsIdentity) || (propertyIsForSearch != null && propertyIsForSearch)) && ((propertyInfo.getMappedPropertyId() != null && propertyInfo.getMappedPropertyId().length() > 0) || (propertyInfo.getMappedPropertiesIds() != null && propertyInfo.getMappedPropertiesIds().length > 0) || (propertyInfo.getMappedRelationPropertiesIds() != null && propertyInfo.getMappedRelationPropertiesIds().length > 0) || relationIsComponent) && considerConditions){
                    propertyConditionOperation = (propertiesConditionsOperations != null ? propertiesConditionsOperations.get(propertyPrefixBuffer.toString()) : null);
                    
                    if(propertyConditionOperation == null && (propertiesConditionsOperations == null || !propertiesConditionsOperations.containsKey(propertyPrefixBuffer.toString()))){
                        if(propertyInfo.getSearchConditionOperation() == ConditionOperationType.NONE)
                            propertyConditionOperation = ConditionOperationType.AND;
                        else
                            propertyConditionOperation = propertyInfo.getSearchConditionOperation();
                    }
                    
                    if(propertyCondition == null || propertyCondition.size() == 0){
                        if(propertyInfo.getSearchCondition() == ConditionType.NONE)
                            propertyCondition = Arrays.asList(ConditionType.EQUAL);
                        else
                            propertyCondition = Arrays.asList(propertyInfo.getSearchCondition());
                    }
                    
                    Iterator<ConditionType> propertyConditionIterator = propertyCondition.iterator();
                    Iterator<Object> propertyValueIterator = (propertyValue != null ? propertyValue.iterator() : null);
                    
                    while(propertyConditionIterator.hasNext()){
                        ConditionType propertyConditionItem = propertyConditionIterator.next();
                        Object propertyValueItem = (propertyValueIterator != null && propertyValueIterator.hasNext() ? propertyValueIterator.next() : null);
                        
                        if(PropertyUtil.isString(propertyValueItem)){
                            if(propertyInfo.isDate()){
                                if(propertyInfo.isTime())
                                    propertyValueItem = DateTimeUtil.parse((String) propertyValueItem, Constants.DEFAULT_DATE_TIME_PATTERN);
                                else
                                    propertyValueItem = DateTimeUtil.parse((String) propertyValueItem, Constants.DEFAULT_DATE_PATTERN);
                            }
                        }
                        
                        processCondition = true;
                        
                        switch(propertyConditionItem){
                            case IS_NULL:{
                                if(whereClause.length() == 0)
                                    whereClause.append("where (");
                                else{
                                    if(propertyConditionOperation == ConditionOperationType.AND)
                                        whereClause.append(")");
                                    
                                    whereClause.append(" ");
                                    whereClause.append(propertyConditionOperation.toString().toLowerCase());
                                    
                                    if(propertyConditionOperation == ConditionOperationType.AND)
                                        whereClause.append(" (");
                                    else
                                        whereClause.append(" ");
                                }
                                
                                whereClause.append(propertyIdBuffer);
                                whereClause.append(" ");
                                whereClause.append(propertyConditionItem.getOperator());
                                
                                break;
                            }
                            case IS_NOT_NULL:{
                                if(whereClause.length() == 0)
                                    whereClause.append("where (");
                                else{
                                    if(propertyConditionOperation == ConditionOperationType.AND)
                                        whereClause.append(")");
                                    
                                    whereClause.append(" ");
                                    whereClause.append(propertyConditionOperation.toString().toLowerCase());
                                    
                                    if(propertyConditionOperation == ConditionOperationType.AND)
                                        whereClause.append(" (");
                                    else
                                        whereClause.append(" ");
                                }
                                
                                whereClause.append(propertyIdBuffer);
                                whereClause.append(" ");
                                whereClause.append(propertyConditionItem.getOperator());
                                
                                break;
                            }
                            case NOT_EQUAL:{
                                if(propertyValueItem == null)
                                    processCondition = false;
                                
                                if(processCondition){
                                    if(whereClauseParameters == null)
                                        whereClauseParameters = PropertyUtil.instantiate(Constants.DEFAULT_MAP_CLASS);
                                    
                                    if(whereClause.length() == 0)
                                        whereClause.append("where (");
                                    else{
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(")");
                                        
                                        whereClause.append(" ");
                                        whereClause.append(propertyConditionOperation.toString().toLowerCase());
                                        
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(" (");
                                        else
                                            whereClause.append(" ");
                                    }
                                    
                                    if(propertyParamBuffer == null)
                                        propertyParamBuffer = new StringBuilder();
                                    else
                                        propertyParamBuffer.delete(0, propertyParamBuffer.length());
                                    
                                    propertyParamBuffer.append("param");
                                    propertyParamBuffer.append(whereClauseParameters.size());
                                    
                                    if(PropertyUtil.isString(propertyValue)){
                                        SearchType searchType = propertyInfo.getSearchType();
                                        
                                        if(searchType == SearchType.CASE_SENSITIVE){
                                            whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueItem);
                                            
                                            whereClause.append(propertyIdBuffer);
                                        }
                                        else{
                                            whereClauseParameters.put(propertyParamBuffer.toString(), ((String) propertyValueItem).toLowerCase());
                                            
                                            whereClause.append("lower(");
                                            whereClause.append(propertyIdBuffer);
                                            whereClause.append(")");
                                        }
                                    }
                                    else{
                                        if(!propertyValueItem.getClass().equals(propertyInfo.getClazz()))
                                            propertyValueItem = PropertyUtil.convertTo(propertyValueItem, propertyInfo.getClazz());
                                        
                                        whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueItem);
                                        
                                        whereClause.append(propertyIdBuffer);
                                    }
                                    
                                    whereClause.append(" ");
                                    whereClause.append(propertyConditionItem.getOperator());
                                    whereClause.append(" :");
                                    whereClause.append(propertyParamBuffer);
                                }
                                
                                break;
                            }
                            case GREATER_THAN_EQUAL:{
                                if(propertyValueItem == null)
                                    processCondition = false;
                                
                                if(processCondition){
                                    if(whereClauseParameters == null)
                                        whereClauseParameters = PropertyUtil.instantiate(Constants.DEFAULT_MAP_CLASS);
                                    
                                    if(whereClause.length() == 0)
                                        whereClause.append("where (");
                                    else{
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(")");
                                        
                                        whereClause.append(" ");
                                        whereClause.append(propertyConditionOperation.toString().toLowerCase());
                                        
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(" (");
                                        else
                                            whereClause.append(" ");
                                    }
                                    
                                    if(propertyParamBuffer == null)
                                        propertyParamBuffer = new StringBuilder();
                                    else
                                        propertyParamBuffer.delete(0, propertyParamBuffer.length());
                                    
                                    propertyParamBuffer.append("param");
                                    propertyParamBuffer.append(whereClauseParameters.size());
                                    
                                    whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueItem);
                                    
                                    whereClause.append(propertyIdBuffer);
                                    whereClause.append(" ");
                                    whereClause.append(propertyConditionItem.getOperator());
                                    whereClause.append(" :");
                                    whereClause.append(propertyParamBuffer);
                                }
                                
                                break;
                            }
                            case GREATER_THAN:{
                                if(propertyValueItem == null)
                                    processCondition = false;
                                
                                if(processCondition){
                                    if(whereClauseParameters == null)
                                        whereClauseParameters = PropertyUtil.instantiate(Constants.DEFAULT_MAP_CLASS);
                                    
                                    if(whereClause.length() == 0)
                                        whereClause.append("where (");
                                    else{
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(")");
                                        
                                        whereClause.append(" ");
                                        whereClause.append(propertyConditionOperation.toString().toLowerCase());
                                        
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(" (");
                                        else
                                            whereClause.append(" ");
                                    }
                                    
                                    if(propertyParamBuffer == null)
                                        propertyParamBuffer = new StringBuilder();
                                    else
                                        propertyParamBuffer.delete(0, propertyParamBuffer.length());
                                    
                                    propertyParamBuffer.append("param");
                                    propertyParamBuffer.append(whereClauseParameters.size());
                                    
                                    whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueItem);
                                    
                                    whereClause.append(propertyIdBuffer);
                                    whereClause.append(" ");
                                    whereClause.append(propertyConditionItem.getOperator());
                                    whereClause.append(" :");
                                    whereClause.append(propertyParamBuffer);
                                }
                                
                                break;
                            }
                            case LESS_THAN_EQUAL:{
                                if(propertyValueItem == null)
                                    processCondition = false;
                                
                                if(processCondition){
                                    if(whereClauseParameters == null)
                                        whereClauseParameters = PropertyUtil.instantiate(Constants.DEFAULT_MAP_CLASS);
                                    
                                    if(whereClause.length() == 0)
                                        whereClause.append("where (");
                                    else{
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(")");
                                        
                                        whereClause.append(" ");
                                        whereClause.append(propertyConditionOperation.toString().toLowerCase());
                                        
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(" (");
                                        else
                                            whereClause.append(" ");
                                    }
                                    
                                    if(propertyParamBuffer == null)
                                        propertyParamBuffer = new StringBuilder();
                                    else
                                        propertyParamBuffer.delete(0, propertyParamBuffer.length());
                                    
                                    propertyParamBuffer.append("param");
                                    propertyParamBuffer.append(whereClauseParameters.size());
                                    
                                    whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueItem);
                                    
                                    whereClause.append(propertyIdBuffer);
                                    whereClause.append(" ");
                                    whereClause.append(propertyConditionItem.getOperator());
                                    whereClause.append(" :");
                                    whereClause.append(propertyParamBuffer);
                                }
                                
                                break;
                            }
                            case LESS_THAN:{
                                if(propertyValueItem == null)
                                    processCondition = false;
                                
                                if(processCondition){
                                    if(whereClauseParameters == null)
                                        whereClauseParameters = PropertyUtil.instantiate(Constants.DEFAULT_MAP_CLASS);
                                    
                                    if(whereClause.length() == 0)
                                        whereClause.append("where (");
                                    else{
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(")");
                                        
                                        whereClause.append(" ");
                                        whereClause.append(propertyConditionOperation.toString().toLowerCase());
                                        
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(" (");
                                        else
                                            whereClause.append(" ");
                                    }
                                    
                                    if(propertyParamBuffer == null)
                                        propertyParamBuffer = new StringBuilder();
                                    else
                                        propertyParamBuffer.delete(0, propertyParamBuffer.length());
                                    
                                    propertyParamBuffer.append("param");
                                    propertyParamBuffer.append(whereClauseParameters.size());
                                    
                                    whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueItem);
                                    
                                    whereClause.append(propertyIdBuffer);
                                    whereClause.append(" ");
                                    whereClause.append(propertyConditionItem.getOperator());
                                    whereClause.append(" :");
                                    whereClause.append(propertyParamBuffer);
                                }
                                
                                break;
                            }
                            case BETWEEN:{
                                try{
                                    propertyValueBuffer = PropertyUtil.getValue(model, propertyInfo.getId());
                                    
                                    if(PropertyUtil.isString(propertyValueBuffer)){
                                        if(propertyInfo.isDate()){
                                            if(propertyInfo.isTime())
                                                propertyValueBuffer = DateTimeUtil.parse((String) propertyValueBuffer, Constants.DEFAULT_DATE_TIME_PATTERN);
                                            else
                                                propertyValueBuffer = DateTimeUtil.parse((String) propertyValueBuffer, Constants.DEFAULT_DATE_PATTERN);
                                        }
                                    }
                                    
                                }
                                catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
                                    throw new InternalErrorException(e);
                                }
                                
                                if(propertyValueItem == null && propertyValueBuffer == null)
                                    processCondition = false;
                                
                                if(processCondition){
                                    if(whereClauseParameters == null)
                                        whereClauseParameters = PropertyUtil.instantiate(Constants.DEFAULT_MAP_CLASS);
                                    
                                    if(whereClause.length() == 0)
                                        whereClause.append("where (");
                                    else{
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(")");
                                        
                                        whereClause.append(" ");
                                        whereClause.append(propertyConditionOperation.toString().toLowerCase());
                                        
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(" (");
                                        else
                                            whereClause.append(" ");
                                    }
                                    
                                    whereClause.append(propertyIdBuffer);
                                    whereClause.append(" ");
                                    
                                    if(propertyParamBuffer == null)
                                        propertyParamBuffer = new StringBuilder();
                                    else
                                        propertyParamBuffer.delete(0, propertyParamBuffer.length());
                                    
                                    propertyParamBuffer.append("param");
                                    propertyParamBuffer.append(whereClauseParameters.size());
                                    
                                    if(propertyValueBuffer != null && propertyValueItem == null){
                                        whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueBuffer);
                                        
                                        whereClause.append(ConditionType.GREATER_THAN_EQUAL.getOperator().toLowerCase());
                                    }
                                    else if(propertyValueBuffer == null && propertyValueItem != null){
                                        whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueItem);
                                        
                                        whereClause.append(ConditionType.LESS_THAN_EQUAL.getOperator().toLowerCase());
                                    }
                                    else{
                                        whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueBuffer);
                                        
                                        whereClause.append(ConditionType.GREATER_THAN_EQUAL.getOperator().toLowerCase());
                                        whereClause.append(" :");
                                        whereClause.append(propertyParamBuffer);
                                        whereClause.append(" ");
                                        whereClause.append(ConditionOperationType.AND);
                                        whereClause.append(" ");
                                        whereClause.append(propertyIdBuffer);
                                        whereClause.append(" ");
                                        
                                        propertyParamBuffer.delete(0, propertyParamBuffer.length());
                                        propertyParamBuffer.append("param");
                                        propertyParamBuffer.append(whereClauseParameters.size());
                                        
                                        whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueItem);
                                        
                                        whereClause.append(ConditionType.LESS_THAN_EQUAL.getOperator().toLowerCase());
                                    }
                                    
                                    whereClause.append(" :");
                                    whereClause.append(propertyParamBuffer);
                                }
                                
                                break;
                            }
                            case PHONETIC:{
                                if(propertyValueItem == null)
                                    processCondition = false;
                                else{
                                    if(((String) propertyValueItem).length() == 0 || propertyInfo.getPhoneticPropertyId() == null || propertyInfo.getPhoneticPropertyId().length() == 0)
                                        processCondition = false;
                                }
                                
                                if(processCondition){
                                    if(whereClauseParameters == null)
                                        whereClauseParameters = PropertyUtil.instantiate(Constants.DEFAULT_MAP_CLASS);
                                    
                                    if(whereClause.length() == 0)
                                        whereClause.append("where (");
                                    else{
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(")");
                                        
                                        whereClause.append(" ");
                                        whereClause.append(propertyConditionOperation.toString().toLowerCase());
                                        
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(" (");
                                        else
                                            whereClause.append(" ");
                                    }
                                    
                                    if(propertyParamBuffer == null)
                                        propertyParamBuffer = new StringBuilder();
                                    else
                                        propertyParamBuffer.delete(0, propertyParamBuffer.length());
                                    
                                    propertyParamBuffer.append("param");
                                    propertyParamBuffer.append(whereClauseParameters.size());
                                    
                                    propertyValueBuffer = new StringBuilder();
                                    ((StringBuilder) propertyValueBuffer).append(PhoneticUtil.soundCode((String) propertyValueItem));
                                    ((StringBuilder) propertyValueBuffer).append("%");
                                    
                                    whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueBuffer.toString());
                                    
                                    propertyIdBuffer.delete(0, propertyIdBuffer.length());
                                    
                                    if(propertyAlias.length() > 0){
                                        propertyIdBuffer.append(propertyAlias);
                                        propertyIdBuffer.append(".");
                                    }
                                    
                                    propertyIdBuffer.append(propertyInfo.getPhoneticPropertyId());
                                    
                                    whereClause.append(propertyIdBuffer);
                                    whereClause.append(" like :");
                                    whereClause.append(propertyParamBuffer);
                                }
                                
                                break;
                            }
                            case STARTS_WITH:{
                                if(propertyValueItem == null)
                                    processCondition = false;
                                else{
                                    if(((String) propertyValueItem).length() == 0)
                                        processCondition = false;
                                }
                                
                                if(processCondition){
                                    if(whereClauseParameters == null)
                                        whereClauseParameters = PropertyUtil.instantiate(Constants.DEFAULT_MAP_CLASS);
                                    
                                    if(whereClause.length() == 0)
                                        whereClause.append("where (");
                                    else{
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(")");
                                        
                                        whereClause.append(" ");
                                        whereClause.append(propertyConditionOperation.toString().toLowerCase());
                                        
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(" (");
                                        else
                                            whereClause.append(" ");
                                    }
                                    
                                    propertyValueBuffer = new StringBuilder();
                                    ((StringBuilder) propertyValueBuffer).append(propertyValueItem);
                                    ((StringBuilder) propertyValueBuffer).append("%");
                                    
                                    if(propertyParamBuffer == null)
                                        propertyParamBuffer = new StringBuilder();
                                    else
                                        propertyParamBuffer.delete(0, propertyParamBuffer.length());
                                    
                                    propertyParamBuffer.append("param");
                                    propertyParamBuffer.append(whereClauseParameters.size());
                                    
                                    SearchType searchType = propertyInfo.getSearchType();
                                    
                                    if(searchType == SearchType.CASE_SENSITIVE){
                                        whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueBuffer.toString());
                                        
                                        whereClause.append(propertyIdBuffer);
                                    }
                                    else{
                                        whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueBuffer.toString().toLowerCase());
                                        
                                        whereClause.append("lower(");
                                        whereClause.append(propertyIdBuffer);
                                        whereClause.append(")");
                                    }
                                    
                                    whereClause.append(" ");
                                    whereClause.append(propertyConditionItem.getOperator());
                                    whereClause.append(" :");
                                    whereClause.append(propertyParamBuffer);
                                }
                                
                                break;
                            }
                            case ENDS_WITH:{
                                if(propertyValueItem == null)
                                    processCondition = false;
                                else{
                                    if(((String) propertyValueItem).length() == 0)
                                        processCondition = false;
                                }
                                
                                if(processCondition){
                                    if(whereClauseParameters == null)
                                        whereClauseParameters = PropertyUtil.instantiate(Constants.DEFAULT_MAP_CLASS);
                                    
                                    if(whereClause.length() == 0)
                                        whereClause.append("where (");
                                    else{
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(")");
                                        
                                        whereClause.append(" ");
                                        whereClause.append(propertyConditionOperation.toString().toLowerCase());
                                        
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(" (");
                                        else
                                            whereClause.append(" ");
                                    }
                                    
                                    propertyValueBuffer = new StringBuilder();
                                    ((StringBuilder) propertyValueBuffer).append("%");
                                    ((StringBuilder) propertyValueBuffer).append(propertyValueItem);
                                    
                                    if(propertyParamBuffer == null)
                                        propertyParamBuffer = new StringBuilder();
                                    else
                                        propertyParamBuffer.delete(0, propertyParamBuffer.length());
                                    
                                    propertyParamBuffer.append("param");
                                    propertyParamBuffer.append(whereClauseParameters.size());
                                    
                                    SearchType searchType = propertyInfo.getSearchType();
                                    
                                    if(searchType == SearchType.CASE_SENSITIVE){
                                        whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueBuffer.toString());
                                        
                                        whereClause.append(propertyIdBuffer);
                                    }
                                    else{
                                        whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueBuffer.toString().toLowerCase());
                                        
                                        whereClause.append("lower(");
                                        whereClause.append(propertyIdBuffer);
                                        whereClause.append(")");
                                    }
                                    
                                    whereClause.append(" ");
                                    whereClause.append(propertyConditionItem.getOperator());
                                    whereClause.append(" :");
                                    whereClause.append(propertyParamBuffer);
                                }
                                
                                break;
                            }
                            case NOT_IN:{
                                Collection<Object> propertyValueItems = null;
                                
                                if(propertyValueItem == null)
                                    processCondition = false;
                                else{
                                    if(!PropertyUtil.isCollection(propertyValueItem))
                                        propertyValueItems = Arrays.asList(propertyValueItem);
                                    else{
                                        propertyValueItems = (Collection<Object>) propertyValueItem;
                                        
                                        if(propertyValueItems.size() == 0)
                                            processCondition = false;
                                    }
                                }
                                
                                if(processCondition){
                                    if(whereClauseParameters == null)
                                        whereClauseParameters = PropertyUtil.instantiate(Constants.DEFAULT_MAP_CLASS);
                                    
                                    if(whereClause.length() == 0)
                                        whereClause.append("where (");
                                    else{
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(")");
                                        
                                        whereClause.append(" ");
                                        whereClause.append(propertyConditionOperation.toString().toLowerCase());
                                        
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(" (");
                                        else
                                            whereClause.append(" ");
                                    }
                                    
                                    if(propertyParamBuffer == null)
                                        propertyParamBuffer = new StringBuilder();
                                    else
                                        propertyParamBuffer.delete(0, propertyParamBuffer.length());
                                    
                                    propertyParamBuffer.append("param");
                                    propertyParamBuffer.append(whereClauseParameters.size());
                                    
                                    whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueItems);
                                    
                                    whereClause.append(propertyIdBuffer);
                                    whereClause.append(" ");
                                    whereClause.append(propertyConditionItem.getOperator());
                                    whereClause.append(" :");
                                    whereClause.append(propertyParamBuffer);
                                }
                                
                                break;
                            }
                            case IN:{
                                Collection<Object> propertyValueItems = null;
                                
                                if(propertyValueItem == null)
                                    processCondition = false;
                                else{
                                    if(!PropertyUtil.isCollection(propertyValueItem))
                                        propertyValueItems = Arrays.asList(propertyValueItem);
                                    else{
                                        propertyValueItems = (Collection<Object>) propertyValueItem;
                                        
                                        if(propertyValueItems.size() == 0)
                                            processCondition = false;
                                    }
                                }
                                
                                if(processCondition){
                                    if(whereClauseParameters == null)
                                        whereClauseParameters = PropertyUtil.instantiate(Constants.DEFAULT_MAP_CLASS);
                                    
                                    if(whereClause.length() == 0)
                                        whereClause.append("where (");
                                    else{
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(")");
                                        
                                        whereClause.append(" ");
                                        whereClause.append(propertyConditionOperation.toString().toLowerCase());
                                        
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(" (");
                                        else
                                            whereClause.append(" ");
                                    }
                                    
                                    if(propertyParamBuffer == null)
                                        propertyParamBuffer = new StringBuilder();
                                    else
                                        propertyParamBuffer.delete(0, propertyParamBuffer.length());
                                    
                                    propertyParamBuffer.append("param");
                                    propertyParamBuffer.append(whereClauseParameters.size());
                                    
                                    whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueItems);
                                    
                                    whereClause.append(propertyIdBuffer);
                                    whereClause.append(" ");
                                    whereClause.append(propertyConditionItem.getOperator());
                                    whereClause.append(" :");
                                    whereClause.append(propertyParamBuffer);
                                }
                                
                                break;
                            }
                            case NOT_CONTAINS:{
                                Collection<Object> propertyValueItems = null;
                                
                                if(propertyValueItem == null)
                                    processCondition = false;
                                else{
                                    if(PropertyUtil.isString(propertyValueItem)){
                                        if(((String) propertyValueItem).length() == 0)
                                            processCondition = false;
                                    }
                                    else if(PropertyUtil.isCollection(propertyValueItem)){
                                        try{
                                            propertyValueItems = (Collection<Object>) propertyValueItem;
                                            
                                            if(propertyValueItems.size() == 0)
                                                processCondition = false;
                                        }
                                        catch(Throwable e){
                                            processCondition = false;
                                        }
                                    }
                                    else{
                                        if(!propertyValueItem.getClass().equals(propertyInfo.getClazz()))
                                            propertyValueItem = PropertyUtil.convertTo(propertyValueItem, propertyInfo.getClazz());
                                    }
                                }
                                
                                if(processCondition){
                                    if(whereClauseParameters == null)
                                        whereClauseParameters = PropertyUtil.instantiate(Constants.DEFAULT_MAP_CLASS);
                                    
                                    if(whereClause.length() == 0)
                                        whereClause.append("where (");
                                    else{
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(")");
                                        
                                        whereClause.append(" ");
                                        whereClause.append(propertyConditionOperation.toString().toLowerCase());
                                        
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(" (");
                                        else
                                            whereClause.append(" ");
                                    }
                                    
                                    if(PropertyUtil.isString(propertyValueItem)){
                                        propertyValueBuffer = new StringBuilder();
                                        ((StringBuilder) propertyValueBuffer).append("%");
                                        ((StringBuilder) propertyValueBuffer).append(propertyValueItem);
                                        ((StringBuilder) propertyValueBuffer).append("%");
                                        
                                        if(propertyParamBuffer == null)
                                            propertyParamBuffer = new StringBuilder();
                                        else
                                            propertyParamBuffer.delete(0, propertyParamBuffer.length());
                                        
                                        propertyParamBuffer.append("param");
                                        propertyParamBuffer.append(whereClauseParameters.size());
                                        
                                        SearchType searchType = propertyInfo.getSearchType();
                                        
                                        if(searchType == SearchType.CASE_SENSITIVE){
                                            whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueBuffer.toString());
                                            
                                            whereClause.append(propertyIdBuffer);
                                        }
                                        else{
                                            whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueBuffer.toString().toLowerCase());
                                            
                                            whereClause.append("lower(");
                                            whereClause.append(propertyIdBuffer);
                                            whereClause.append(")");
                                        }
                                        
                                        whereClause.append(" ");
                                        whereClause.append(propertyConditionItem.getOperator());
                                        whereClause.append(" :");
                                        whereClause.append(propertyParamBuffer);
                                    }
                                    else if(PropertyUtil.isCollection(propertyValueItem)){
                                        int index = 0;
                                        
                                        whereClause.append("(");
                                        
                                        for(Object item: propertyValueItems){
                                            if(index > 0){
                                                whereClause.append(" ");
                                                whereClause.append(ConditionOperationType.AND.toString());
                                                whereClause.append(" ");
                                            }
                                            
                                            if(item != null){
                                                if(propertyInfo.hasModel() != null && propertyInfo.hasModel()){
                                                    if(!item.getClass().equals(propertyInfo.getCollectionItemsClass()))
                                                        item = PropertyUtil.convertTo(item, propertyInfo.getCollectionItemsClass());
                                                }
                                                else{
                                                    if(!item.getClass().equals(propertyInfo.getClazz()))
                                                        item = PropertyUtil.convertTo(item, propertyInfo.getClazz());
                                                }
                                            }
                                            
                                            whereClause.append(propertyIdBuffer);
                                            whereClause.append(" ");
                                            
                                            if(item != null){
                                                if(propertyParamBuffer == null)
                                                    propertyParamBuffer = new StringBuilder();
                                                else
                                                    propertyParamBuffer.delete(0, propertyParamBuffer.length());
                                                
                                                propertyParamBuffer.append("param");
                                                propertyParamBuffer.append(whereClauseParameters.size());
                                                
                                                whereClauseParameters.put(propertyParamBuffer.toString(), item);
                                                
                                                whereClause.append(ConditionType.NOT_EQUAL.getOperator());
                                                whereClause.append(" :");
                                                whereClause.append(propertyParamBuffer);
                                            }
                                            else
                                                whereClause.append(ConditionType.IS_NOT_NULL.getOperator());
                                            
                                            index++;
                                        }
                                        
                                        whereClause.append(")");
                                    }
                                }
                                
                                break;
                            }
                            case CONTAINS:{
                                Collection<Object> propertyValueItems = null;
                                
                                if(propertyValueItem == null)
                                    processCondition = false;
                                else{
                                    if(PropertyUtil.isString(propertyValueItem)){
                                        if(((String) propertyValueItem).length() == 0)
                                            processCondition = false;
                                    }
                                    else if(PropertyUtil.isCollection(propertyValueItem)){
                                        try{
                                            propertyValueItems = (Collection<Object>) propertyValueItem;
                                            
                                            if(propertyValueItems.size() == 0)
                                                processCondition = false;
                                        }
                                        catch(Throwable e){
                                            processCondition = false;
                                        }
                                    }
                                    else{
                                        if(!propertyValueItem.getClass().equals(propertyInfo.getClazz()))
                                            propertyValueItem = PropertyUtil.convertTo(propertyValueItem, propertyInfo.getClazz());
                                    }
                                }
                                
                                if(processCondition){
                                    if(whereClauseParameters == null)
                                        whereClauseParameters = PropertyUtil.instantiate(Constants.DEFAULT_MAP_CLASS);
                                    
                                    if(whereClause.length() == 0)
                                        whereClause.append("where (");
                                    else{
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(")");
                                        
                                        whereClause.append(" ");
                                        whereClause.append(propertyConditionOperation.toString().toLowerCase());
                                        
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(" (");
                                        else
                                            whereClause.append(" ");
                                    }
                                    
                                    if(PropertyUtil.isString(propertyValueItem)){
                                        propertyValueBuffer = new StringBuilder();
                                        ((StringBuilder) propertyValueBuffer).append("%");
                                        ((StringBuilder) propertyValueBuffer).append(propertyValueItem);
                                        ((StringBuilder) propertyValueBuffer).append("%");
                                        
                                        if(propertyParamBuffer == null)
                                            propertyParamBuffer = new StringBuilder();
                                        else
                                            propertyParamBuffer.delete(0, propertyParamBuffer.length());
                                        
                                        propertyParamBuffer.append("param");
                                        propertyParamBuffer.append(whereClauseParameters.size());
                                        
                                        SearchType searchType = propertyInfo.getSearchType();
                                        
                                        if(searchType == SearchType.CASE_SENSITIVE){
                                            whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueBuffer.toString());
                                            
                                            whereClause.append(propertyIdBuffer);
                                        }
                                        else{
                                            whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueBuffer.toString().toLowerCase());
                                            
                                            whereClause.append("lower(");
                                            whereClause.append(propertyIdBuffer);
                                            whereClause.append(")");
                                        }
                                        
                                        whereClause.append(" ");
                                        whereClause.append(propertyConditionItem.getOperator());
                                        whereClause.append(" :");
                                        whereClause.append(propertyParamBuffer);
                                    }
                                    else if(PropertyUtil.isCollection(propertyValueItem)){
                                        int index = 0;
                                        
                                        whereClause.append("(");
                                        
                                        for(Object item: propertyValueItems){
                                            if(index > 0){
                                                whereClause.append(" ");
                                                whereClause.append(ConditionOperationType.OR.toString());
                                                whereClause.append(" ");
                                            }
                                            
                                            if(item != null){
                                                if(propertyInfo.hasModel() != null && propertyInfo.hasModel()){
                                                    if(!item.getClass().equals(propertyInfo.getCollectionItemsClass()))
                                                        item = PropertyUtil.convertTo(item, propertyInfo.getCollectionItemsClass());
                                                }
                                                else{
                                                    if(!item.getClass().equals(propertyInfo.getClazz()))
                                                        item = PropertyUtil.convertTo(item, propertyInfo.getClazz());
                                                }
                                            }
                                            
                                            whereClause.append(propertyIdBuffer);
                                            whereClause.append(" ");
                                            
                                            if(item != null){
                                                if(propertyParamBuffer == null)
                                                    propertyParamBuffer = new StringBuilder();
                                                else
                                                    propertyParamBuffer.delete(0, propertyParamBuffer.length());
                                                
                                                propertyParamBuffer.append("param");
                                                propertyParamBuffer.append(whereClauseParameters.size());
                                                
                                                whereClauseParameters.put(propertyParamBuffer.toString(), item);
                                                
                                                whereClause.append(ConditionType.EQUAL.getOperator());
                                                whereClause.append(" :");
                                                whereClause.append(propertyParamBuffer);
                                            }
                                            else
                                                whereClause.append(ConditionType.IS_NULL.getOperator());
                                            
                                            index++;
                                        }
                                        
                                        whereClause.append(")");
                                    }
                                }
                                
                                break;
                            }
                            default:{
                                if(propertyValueItem == null)
                                    processCondition = false;
                                else{
                                    if(PropertyUtil.isNumber(propertyValueItem)){
                                        Double comparePropertyValue = PropertyUtil.convertTo(propertyValueItem, Double.class);
                                        
                                        if(PropertyUtil.compareTo(comparePropertyValue, 0d) == 0)
                                            processCondition = false;
                                    }
                                    else if(PropertyUtil.isString(propertyValueItem) && ((String) propertyValueItem).length() == 0)
                                        processCondition = false;
                                }
                                
                                if(processCondition){
                                    if(whereClauseParameters == null)
                                        whereClauseParameters = PropertyUtil.instantiate(Constants.DEFAULT_MAP_CLASS);
                                    
                                    if(whereClause.length() == 0)
                                        whereClause.append("where (");
                                    else{
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(")");
                                        
                                        whereClause.append(" ");
                                        whereClause.append(propertyConditionOperation.toString().toLowerCase());
                                        
                                        if(propertyConditionOperation == ConditionOperationType.AND)
                                            whereClause.append(" (");
                                        else
                                            whereClause.append(" ");
                                    }
                                    
                                    if(propertyParamBuffer == null)
                                        propertyParamBuffer = new StringBuilder();
                                    else
                                        propertyParamBuffer.delete(0, propertyParamBuffer.length());
                                    
                                    propertyParamBuffer.append("param");
                                    propertyParamBuffer.append(whereClauseParameters.size());
                                    
                                    if(PropertyUtil.isString(propertyValue)){
                                        SearchType searchType = propertyInfo.getSearchType();
                                        
                                        if(searchType == SearchType.CASE_SENSITIVE){
                                            whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueItem);
                                            
                                            whereClause.append(propertyIdBuffer);
                                        }
                                        else{
                                            whereClauseParameters.put(propertyParamBuffer.toString(), ((String) propertyValueItem).toLowerCase());
                                            
                                            whereClause.append("lower(");
                                            whereClause.append(propertyIdBuffer);
                                            whereClause.append(")");
                                        }
                                    }
                                    else{
                                        if(!propertyValueItem.getClass().equals(propertyInfo.getClazz()))
                                            propertyValueItem = PropertyUtil.convertTo(propertyValueItem, propertyInfo.getClazz());
                                        
                                        whereClauseParameters.put(propertyParamBuffer.toString(), propertyValueItem);
                                        
                                        whereClause.append(propertyIdBuffer);
                                    }
                                    
                                    whereClause.append(" ");
                                    whereClause.append(propertyConditionItem.getOperator());
                                    whereClause.append(" :");
                                    whereClause.append(propertyParamBuffer);
                                    
                                    if(considerConditions){
                                        identitiesPropertiesInfoBuffer.add(propertyInfo);
                                        
                                        if(identitiesPropertiesInfoBuffer.containsAll(identitiesPropertiesInfo))
                                            considerConditions = false;
                                    }
                                }
                                
                                break;
                            }
                        }
                    }
                }
            }
        }
        catch(IllegalArgumentException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException | NoSuchFieldException | ParseException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Builds a query based on a persistence.
     *
     * @param queryType Instance that contains the query type.
     * @param persistence Instance that contains the persistence.
     * @return Instance that contains the query.
     * @throws InternalErrorException Occurs when was not possible to build the
     * query.
     */
    @SuppressWarnings({"rawtypes"})
    public static Query build(QueryType queryType, HibernatePersistence<? extends BaseModel> persistence) throws InternalErrorException{
        return build(queryType, null, null, persistence);
    }
    
    /**
     * Builds a query based on a data model.
     *
     * @param <M> Class that defines the data model.
     * @param queryType Instance that contains the query type.
     * @param model Instance that contains the data model.
     * @param persistence Instance that contains the persistence.
     * @return Instance that contains the query.
     * @throws InternalErrorException Occurs when was not possible to build the
     * query.
     */
    @SuppressWarnings({"rawtypes"})
    public static <M extends BaseModel> Query build(QueryType queryType, M model, HibernatePersistence<M> persistence) throws InternalErrorException{
        return build(queryType, model, null, persistence);
    }
    
    /**
     * Builds a query based on a data model.
     *
     * @param <M> Class that defines the data model.
     * @param queryType Instance that contains the query type.
     * @param model Instance that contains the data model.
     * @param filter Instance that contains the search criteria.
     * @param persistence Instance that contains the persistence.
     * @return Instance that contains the query.
     * @throws InternalErrorException Occurs when was not possible to build the
     * query.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <M extends BaseModel> Query build(QueryType queryType, M model, Filter filter, HibernatePersistence<M> persistence) throws InternalErrorException{
        try{
            String expression = null;
            Boolean modelIsNull = (model == null);
            Class<? extends BaseModel> modelClass = (modelIsNull ? PersistenceUtil.getModelClassByPersistence(persistence.getClass()) : model.getClass());
            ModelInfo modelInfo = ModelUtil.getInfo(modelClass);
            
            if(modelIsNull)
                model = (M) ConstructorUtils.invokeConstructor(modelClass, null);
            
            Session connection = persistence.getConnection();
            String statementId = MethodUtil.getMethodFromStackTrace(3).getName();
            Map<String, Object> whereClauseParameters = PropertyUtil.instantiate(Constants.DEFAULT_MAP_CLASS);
            
            expression = buildExpression(model, filter, whereClauseParameters);
            
            Query query = connection.createQuery(expression);
            
            query.setComment(statementId);
            
            if(modelInfo.isCacheable() != null && modelInfo.isCacheable()){
                query.setCacheable(true);
                query.setCacheMode(CacheMode.NORMAL);
            }
            
            if(filter != null && filter.getReturnProperties() != null && filter.getReturnProperties().size() > 0)
                query.setResultTransformer(Transformers.aliasToBean(model.getClass()));
            
            for(Entry<String, Object> entry: whereClauseParameters.entrySet()){
                if(PropertyUtil.isCollection(entry.getValue()))
                    query.setParameterList(entry.getKey(), (Collection<?>) entry.getValue());
                else
                    query.setParameter(entry.getKey(), entry.getValue());
            }
            
            if(queryType == QueryType.FIND)
                query.setMaxResults(1);
            else{
                Integer maximumResults = null;
                
                if(filter != null)
                    maximumResults = filter.getMaximumResults();
                
                if(maximumResults != null && maximumResults > 0)
                    query.setMaxResults(maximumResults);
                else{
                    Map<String, String> resourcesOptions = persistence.getResources().getOptions();
                    
                    try{
                        String queryMaxResultsBuffer = resourcesOptions.get(PersistenceConstants.QUERY_MAXIMUM_RESULTS_ATTRIBUTE_ID);
                        
                        if(queryMaxResultsBuffer != null && queryMaxResultsBuffer.length() > 0)
                            query.setMaxResults(NumberUtil.parseInt(queryMaxResultsBuffer));
                        else
                            query.setMaxResults(PersistenceConstants.DEFAULT_MAXIMUM_RESULTS);
                    }
                    catch(ParseException e){
                        query.setMaxResults(PersistenceConstants.DEFAULT_MAXIMUM_RESULTS);
                    }
                }
            }
            
            return query;
        }
        catch(HibernateException | ClassNotFoundException | IllegalArgumentException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchFieldException e){
            throw new InternalErrorException(e);
        }
    }
    
    
}