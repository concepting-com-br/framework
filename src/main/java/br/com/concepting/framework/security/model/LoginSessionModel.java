package br.com.concepting.framework.security.model;

import br.com.concepting.framework.audit.annotations.Auditable;
import br.com.concepting.framework.model.BaseModel;
import br.com.concepting.framework.model.SystemModuleModel;
import br.com.concepting.framework.model.SystemSessionModel;
import br.com.concepting.framework.model.annotations.Model;
import br.com.concepting.framework.model.annotations.Property;
import br.com.concepting.framework.persistence.types.RelationJoinType;
import br.com.concepting.framework.persistence.types.RelationType;
import br.com.concepting.framework.security.annotations.Security;
import br.com.concepting.framework.util.helpers.DateTime;
import br.com.concepting.framework.util.types.SortOrderType;

/**
 * Class that defines the data model that stores the information of the login
 * session.
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
@Auditable
@Model(ui = "Login Session", templateId = "loginSession", generateWebService = true)
@Security
public class LoginSessionModel extends BaseModel{
    private static final long serialVersionUID = -8507789965528582224L;
    
    @Auditable
    @Property(isIdentity = true)
    private String id = null;
    
    @Auditable
    @Property(sortOrder = SortOrderType.DESCEND)
    private DateTime startDateTime = null;
    
    @Auditable
    @Property(relationType = RelationType.ONE_TO_ONE, relationJoinType = RelationJoinType.INNER_JOIN)
    private SystemSessionModel systemSession = null;
    
    @Auditable
    @Property(relationType = RelationType.ONE_TO_ONE, relationJoinType = RelationJoinType.INNER_JOIN)
    private SystemModuleModel systemModule = null;
    
    @Auditable
    @Property(relationType = RelationType.ONE_TO_ONE, relationJoinType = RelationJoinType.INNER_JOIN)
    private UserModel user = null;
    
    @Property(isForSearch = true)
    private Boolean active = null;
    
    @Property
    private Boolean rememberUserAndPassword = null;
    
    /**
     * Indicates if the name of the user and his password should be remembered.
     *
     * @return True/False.
     */
    public Boolean rememberUserAndPassword(){
        return this.rememberUserAndPassword;
    }
    
    /**
     * Indicates if the name of the user and his password should be remembered.
     *
     * @return True/False.
     */
    public Boolean getRememberUserAndPassword(){
        return rememberUserAndPassword();
    }
    
    /**
     * Defines if the name of the user and his password should be remembered.
     *
     * @param rememberUserAndPassword True/False.
     */
    public void setRememberUserAndPassword(Boolean rememberUserAndPassword){
        this.rememberUserAndPassword = rememberUserAndPassword;
    }
    
    /**
     * Indicates if the login session is active.
     *
     * @return True/False.
     */
    public Boolean isActive(){
        return this.active;
    }
    
    /**
     * Indicates if the login session is active.
     *
     * @return True/False.
     */
    public Boolean getActive(){
        return isActive();
    }
    
    /**
     * Defines if the login session is active.
     *
     * @param active True/False.
     */
    public void setActive(Boolean active){
        this.active = active;
    }
    
    /**
     * Returns the instance of the data model that stores the information of the
     * system session.
     *
     * @param <SS> Class that define the system session data model.
     * @return Instance that contains the data model.
     */
    @SuppressWarnings("unchecked")
    public <SS extends SystemSessionModel> SS getSystemSession(){
        return (SS) this.systemSession;
    }
    
    /**
     * Defines the instance of the data model that stores the information of the
     * system session.
     *
     * @param systemSession Instance that contains the data model.
     */
    public void setSystemSession(SystemSessionModel systemSession){
        this.systemSession = systemSession;
    }
    
    /**
     * Returns the login session date/time.
     *
     * @return Instance that contains the date/time of the login session.
     */
    public DateTime getStartDateTime(){
        return this.startDateTime;
    }
    
    /**
     * Defines the login session date/time.
     *
     * @param startDateTime Instance that contains the date/time of the login
     * session.
     */
    public void setStartDateTime(DateTime startDateTime){
        this.startDateTime = startDateTime;
    }
    
    /**
     * Returns the identifier of the login session.
     *
     * @return String that contains the identifier.
     */
    public String getId(){
        return this.id;
    }
    
    /**
     * Defines the identifier of the login session.
     *
     * @param id String that contains the identifier.
     */
    public void setId(String id){
        this.id = id;
    }
    
    /**
     * Returns the instance of the user data model.
     *
     * @param <U> Class that defines the user data model.
     * @return Instance that contains the data model.
     */
    @SuppressWarnings("unchecked")
    public <U extends UserModel> U getUser(){
        return (U) this.user;
    }
    
    /**
     * Defines the instance of the user data model.
     *
     * @param user Instance that contains the data model.
     */
    public void setUser(UserModel user){
        this.user = user;
    }
    
    /**
     * Returns the instance of the system module data model.
     *
     * @param <SM> Class that define the system module data model.
     * @return Instance that contains the data model.
     */
    @SuppressWarnings("unchecked")
    public <SM extends SystemModuleModel> SM getSystemModule(){
        return (SM) this.systemModule;
    }
    
    /**
     * Defines the instance of the system module data model.
     *
     * @param systemModule Instance that contains the data model.
     */
    public void setSystemModule(SystemModuleModel systemModule){
        this.systemModule = systemModule;
    }
}