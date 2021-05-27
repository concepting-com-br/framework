package br.com.concepting.framework.model;

import br.com.concepting.framework.model.annotations.Model;
import br.com.concepting.framework.model.annotations.Property;
import br.com.concepting.framework.model.types.ConditionType;
import br.com.concepting.framework.model.types.ValidationType;
import br.com.concepting.framework.persistence.types.RelationJoinType;
import br.com.concepting.framework.persistence.types.RelationType;
import br.com.concepting.framework.util.types.SearchType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;

/**
 * Class that defines the basic implementation of a data model that stores
 * information of a form.
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
@Model
public class FormModel extends BaseModel{
    private static final long serialVersionUID = -2926108936145777194L;
    
    @Property(isIdentity = true)
    private Integer id = null;
    
    @Property(isForSearch = true, relationType = RelationType.ONE_TO_ONE, validations = ValidationType.REQUIRED)
    private SystemModuleModel systemModule = null;
    
    @Property(isForSearch = true, searchType = SearchType.CASE_INSENSITIVE, validations = ValidationType.REQUIRED)
    private String name = null;
    
    @Property(isForSearch = true, searchType = SearchType.CASE_INSENSITIVE, searchCondition = ConditionType.CONTAINS, validations = ValidationType.REQUIRED)
    private String title = null;
    
    @Property(isForSearch = true, searchType = SearchType.CASE_INSENSITIVE, searchCondition = ConditionType.CONTAINS, maximumLength = 1000)
    private String description = null;
    
    @Property(relationType = RelationType.ONE_TO_MANY, relationJoinType = RelationJoinType.INNER_JOIN)
    @JsonIgnore
    private Collection<? extends ObjectModel> objects = null;
    
    /**
     * Returns the description of the form.
     *
     * @return String that contains the description.
     */
    public String getDescription(){
        return this.description;
    }
    
    /**
     * Defines the description of the form.
     *
     * @param description String that contains the description.
     */
    public void setDescription(String description){
        this.description = description;
    }
    
    /**
     * Returns the identifier of the form.
     *
     * @return Numeric value that contains the identifier.
     */
    public Integer getId(){
        return this.id;
    }
    
    /**
     * Defines the identifier of the form.
     *
     * @param id Numeric value that contains the identifier.
     */
    public void setId(Integer id){
        this.id = id;
    }
    
    /**
     * Returns the name of the form.
     *
     * @return String that contains name of the form.
     */
    public String getName(){
        return this.name;
    }
    
    /**
     * Defines the name of the form.
     *
     * @param name String that contains name of the form.
     */
    public void setName(String name){
        this.name = name;
    }
    
    /**
     * Returns the list of objects of the form.
     *
     * @return Instance that contains the list of objects of the form.
     */
    public Collection<? extends ObjectModel> getObjects(){
        return this.objects;
    }
    
    /**
     * Defines the list of objects of the form.
     *
     * @param objects Instance that contains the list of objects of the form.
     */
    public void setObjects(Collection<? extends ObjectModel> objects){
        this.objects = objects;
    }
    
    /**
     * Returns the instance that contains the attributes of the system.
     *
     * @param <S> Class that define the system module data model.
     * @return Instance that contains that attributes of the system.
     */
    @SuppressWarnings("unchecked")
    public <S extends SystemModuleModel> S getSystemModule(){
        return (S) this.systemModule;
    }
    
    /**
     * Defines the instance that contains the attributes of the system.
     *
     * @param systemModule Instance that contains that attributes of the system.
     */
    public void setSystemModule(SystemModuleModel systemModule){
        this.systemModule = systemModule;
    }
    
    /**
     * Returns the title of the form.
     *
     * @return String that contains the title of the form.
     */
    public String getTitle(){
        return this.title;
    }
    
    /**
     * Defines the title of the form.
     *
     * @param title String that contains the title of the form.
     */
    public void setTitle(String title){
        this.title = title;
    }
    
    /**
     * Returns the instance of an object of the form.
     *
     * @param objectName String that contains the identifier of the object.
     * @return Instance that contains the object.
     */
    public ObjectModel getObject(String objectName){
        if(this.objects != null)
            for(ObjectModel object: this.objects)
                if(object.getName().equals(objectName))
                    return object;
        
        return null;
    }
}