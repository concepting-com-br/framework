package br.com.concepting.framework.security.model;

import br.com.concepting.framework.model.*;
import br.com.concepting.framework.model.annotations.Model;
import br.com.concepting.framework.model.annotations.Property;
import br.com.concepting.framework.model.types.ConditionType;
import br.com.concepting.framework.model.types.ValidationType;
import br.com.concepting.framework.persistence.types.RelationJoinType;
import br.com.concepting.framework.persistence.types.RelationType;
import br.com.concepting.framework.util.StringUtil;
import br.com.concepting.framework.util.types.SearchType;

import java.util.Collection;

/**
 * Class that defines the data model that stores the information of the groups.
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
public class GroupModel extends BaseModel{
    private static final long serialVersionUID = -5618172953127972175L;
    
    @Property(isIdentity = true)
    private Integer id = null;
    
    @Property(isForSearch = true, searchType = SearchType.CASE_INSENSITIVE, validations = ValidationType.REQUIRED)
    private String name = null;
    
    @Property(isForSearch = true, searchType = SearchType.CASE_INSENSITIVE, searchCondition = ConditionType.CONTAINS, validations = ValidationType.REQUIRED)
    private String title = null;
    
    @Property(isForSearch = true, searchType = SearchType.CASE_INSENSITIVE, searchCondition = ConditionType.CONTAINS, maximumLength = 1000)
    private String description = null;
    
    @Property(relationType = RelationType.MANY_TO_MANY)
    private Collection<? extends UserModel> users = null;
    
    @Property(relationType = RelationType.MANY_TO_MANY, relationJoinType = RelationJoinType.INNER_JOIN)
    private Collection<? extends ObjectModel> objects = null;
    
    @Property(relationType = RelationType.MANY_TO_MANY, relationJoinType = RelationJoinType.INNER_JOIN)
    private Collection<? extends AccessModel> accesses = null;
    
    /**
     * Returns the accesses of the group.
     *
     * @return List that contains the URLs.
     */
    public Collection<? extends AccessModel> getAccesses(){
        return this.accesses;
    }
    
    /**
     * Defines the accesses of the group.
     *
     * @param accesses List that contains the accesses.
     */
    public void setAccesses(Collection<? extends AccessModel> accesses){
        this.accesses = accesses;
    }
    
    /**
     * Returns the identifier of the group.
     *
     * @return String that contains the identifier.
     */
    public String getName(){
        return this.name;
    }
    
    /**
     * Defines the identifier of the group.
     *
     * @param name String that contains the identifier.
     */
    public void setName(String name){
        this.name = name;
    }
    
    /**
     * Returns the list of users of the group.
     *
     * @return List that contains the users of the group.
     */
    public Collection<? extends UserModel> getUsers(){
        return this.users;
    }
    
    /**
     * Defines the list of users of the group.
     *
     * @param users List that contains the users of the group.
     */
    public void setUsers(Collection<? extends UserModel> users){
        this.users = users;
    }
    
    /**
     * Returns the identifier of the group.
     *
     * @return Numeric value that contains the identifier of the group.
     */
    public Integer getId(){
        return this.id;
    }
    
    /**
     * Defines the identifier of the group.
     *
     * @param id Numeric value that contains the identifier of the group.
     */
    public void setId(Integer id){
        this.id = id;
    }
    
    /**
     * Returns the title of the group.
     *
     * @return String that contains the title of the group.
     */
    public String getTitle(){
        return this.title;
    }
    
    /**
     * Defines the title of the group.
     *
     * @param title String that contains the title of the group.
     */
    public void setTitle(String title){
        this.title = title;
    }
    
    /**
     * Returns the description of the group.
     *
     * @return String that contains the description.
     */
    public String getDescription(){
        return this.description;
    }
    
    /**
     * Defines the description of the group.
     *
     * @param description String that contains the description.
     */
    public void setDescription(String description){
        this.description = description;
    }
    
    /**
     * Returns the objects of the groups.
     *
     * @return List that contains the objects.
     */
    public Collection<? extends ObjectModel> getObjects(){
        return this.objects;
    }
    
    /**
     * Defines the objects of the groups.
     *
     * @param objects List that contains the objects.
     */
    public void setObjects(Collection<? extends ObjectModel> objects){
        this.objects = objects;
    }
    
    /**
     * Indicates if the group has permission to access a specific object.
     *
     * @param compareObject Instance that contains the object.
     * @return True/False.
     */
    public Boolean hasPermission(ObjectModel compareObject){
        try{
            return (this.objects.contains(compareObject));
        }
        catch(Throwable e){
        }
        
        return true;
    }
    
    /**
     * Indicates if the group has permission to access the objects.
     *
     * @return True/False.
     */
    public Boolean hasPermissions(){
        try{
            return ((this.objects != null && this.objects.size() > 0) || (this.accesses != null && this.accesses.size() > 0));
        }
        catch(Throwable e){
        }
        
        return true;
    }
    
    /**
     * Indicates if the group has permission to access the path.
     *
     * @param path String that contains the path.
     * @return True/False.
     */
    public Boolean hasPermission(String path){
        if(this.accesses != null && !this.accesses.isEmpty()){
            for(AccessModel access: this.accesses){
                UrlModel url = access.getUrl();
                String urlPath = StringUtil.toRegex(url.getPath());
                
                if(path.matches(urlPath) && !access.isBlocked())
                    return true;
            }
            
            return false;
        }
        
        return true;
    }
    
    /**
     * Indicates if the group has permission to access the objects o system.
     *
     * @param compareSystemModule Instance that contains the system module.
     * @return True/False.
     */
    public Boolean hasPermission(SystemModuleModel compareSystemModule){
        try{
            if(this.objects != null && !this.objects.isEmpty()){
                for(ObjectModel object: this.objects){
                    FormModel form = object.getForm();
                    
                    if(form != null){
                        SystemModuleModel systemModule = form.getSystemModule();
                        
                        if(systemModule != null && systemModule.equals(compareSystemModule))
                            return true;
                    }
                }
                
                return false;
            }
        }
        catch(Throwable e){
        }
        
        return true;
    }
}