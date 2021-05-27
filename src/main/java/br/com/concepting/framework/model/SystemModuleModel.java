package br.com.concepting.framework.model;

import br.com.concepting.framework.audit.annotations.Auditable;
import br.com.concepting.framework.model.annotations.Model;
import br.com.concepting.framework.model.annotations.Property;
import br.com.concepting.framework.model.types.ConditionType;
import br.com.concepting.framework.model.types.ValidationType;
import br.com.concepting.framework.persistence.types.RelationJoinType;
import br.com.concepting.framework.persistence.types.RelationType;
import br.com.concepting.framework.util.types.ByteMetricType;
import br.com.concepting.framework.util.types.ContentType;
import br.com.concepting.framework.util.types.SearchType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.List;

/**
 * Class that defines the basic implementation of the data model of a system.
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
@Model
public class SystemModuleModel extends BaseModel{
    private static final long serialVersionUID = 2148773632677122349L;
    
    @Property(isIdentity = true)
    private Integer id = null;
    
    @Auditable
    @Property(isForSearch = true, searchType = SearchType.CASE_INSENSITIVE, validations = ValidationType.REQUIRED)
    private String name = null;
    
    @Property(isForSearch = true, searchType = SearchType.CASE_INSENSITIVE, searchCondition = ConditionType.CONTAINS, validations = ValidationType.REQUIRED)
    private String title = null;
    
    @Property(isForSearch = true, searchType = SearchType.CASE_INSENSITIVE, searchCondition = ConditionType.CONTAINS, maximumLength = 1000)
    private String description = null;
    
    @Auditable
    @Property(isForSearch = true, searchType = SearchType.CASE_INSENSITIVE, validations = ValidationType.REQUIRED)
    private String url = null;
    
    @Property(validations = ValidationType.REQUIRED)
    private String domain = null;
    
    @Property(validations = ValidationType.REQUIRED)
    private String copyright = null;
    
    @Auditable
    @Property
    private String version = null;
    
    @Property
    private Boolean active = null;
    
    @Property(validations = ValidationType.CONTENT_TYPE, contentTypes = {ContentType.JPG, ContentType.PNG, ContentType.GIF}, contentSizeUnit = ByteMetricType.MEGA_BYTE)
    private byte[] logo = null;
    
    @Property(relationType = RelationType.ONE_TO_MANY, relationJoinType = RelationJoinType.INNER_JOIN)
    @JsonIgnore
    private Collection<? extends FormModel> forms = null;
    
    @Property(relationType = RelationType.MANY_TO_MANY, relationJoinType = RelationJoinType.INNER_JOIN)
    private Collection<? extends UrlModel> exclusionUrls = null;
    
    /**
     * Returns the exclusions URLs.
     *
     * @return List that contains the URLs.
     */
    public Collection<? extends UrlModel> getExclusionUrls(){
        return this.exclusionUrls;
    }
    
    /**
     * Defines the exclusions URLs.
     *
     * @param exclusionUrls List that contains the URLs.
     */
    public void setExclusionUrls(Collection<? extends UrlModel> exclusionUrls){
        this.exclusionUrls = exclusionUrls;
    }
    
    /**
     * Returns the domain of the system.
     *
     * @return String that contains the domain.
     */
    public String getDomain(){
        return this.domain;
    }
    
    /**
     * Defines the domain of the system.
     *
     * @param domain String that contains the domain.
     */
    public void setDomain(String domain){
        this.domain = domain;
    }
    
    /**
     * Returns the message of the copyright.
     *
     * @return String that contains the message of copyright.
     */
    public String getCopyright(){
        return this.copyright;
    }
    
    /**
     * Defines the message of the copyright.
     *
     * @param copyright String that contains the message of copyright.
     */
    public void setCopyright(String copyright){
        this.copyright = copyright;
    }
    
    /**
     * Returns the identifier of the system.
     *
     * @return Numeric value that contains the identifier.
     */
    public Integer getId(){
        return this.id;
    }
    
    /**
     * Defines the identifier of the system.
     *
     * @param id Numeric value that contains the identifier.
     */
    public void setId(Integer id){
        this.id = id;
    }
    
    /**
     * Returns the name of the system.
     *
     * @return String that contains the name of the system.
     */
    public String getName(){
        return this.name;
    }
    
    /**
     * Defines the name of the system.
     *
     * @param name String that contains the name of the system.
     */
    public void setName(String name){
        this.name = name;
    }
    
    /**
     * Returns the URL of the system.
     *
     * @return String that contains the URL of the system.
     */
    public String getUrl(){
        return this.url;
    }
    
    /**
     * Defines the URL of the system.
     *
     * @param url String that contains the URL of the system.
     */
    public void setUrl(String url){
        this.url = url;
    }
    
    /**
     * Returns the instance that contains a specific form of the system.
     *
     * @param <F> Class that defines the data model.
     * @param formName String that contains the identifier of the form.
     * @return Instance that contains the the properties of the form.
     */
    @SuppressWarnings("unchecked")
    public <F extends FormModel> F getForm(String formName){
        if(this.forms != null)
            for(FormModel form: this.forms)
                if(form.getName().equals(formName))
                    return (F) form;
        
        return null;
    }
    
    /**
     * Returns the list of forms of the system.
     *
     * @return List that contains the forms of the system.
     */
    public Collection<? extends FormModel> getForms(){
        return this.forms;
    }
    
    /**
     * Defines the list of forms of the system.
     *
     * @param forms List that contains the forms of the system.
     */
    public void setForms(Collection<? extends FormModel> forms){
        this.forms = forms;
    }
    
    /**
     * Returns the title of the system.
     *
     * @return String that contains the title of the system.
     */
    public String getTitle(){
        return this.title;
    }
    
    /**
     * Defines the title of the system.
     *
     * @param title String that contains the title of the system.
     */
    public void setTitle(String title){
        this.title = title;
    }
    
    /**
     * Returns o array bytes that contains the logo.
     *
     * @return Byte array that contains the logo.
     */
    public byte[] getLogo(){
        return this.logo;
    }
    
    /**
     * Defines o array bytes that contains the logo.
     *
     * @param logo Byte array that contains the logo.
     */
    public void setLogo(byte[] logo){
        this.logo = logo;
    }
    
    /**
     * Returns the description of the system.
     *
     * @return String that contains the description of the system.
     */
    public String getDescription(){
        return this.description;
    }
    
    /**
     * Defines the description of the system.
     *
     * @param description String that contains the description of the system.
     */
    public void setDescription(String description){
        this.description = description;
    }
    
    /**
     * Returns the identifier the version of the system.
     *
     * @return String that contains the identifier the version.
     */
    public String getVersion(){
        return this.version;
    }
    
    /**
     * Defines the identifier the version of the system.
     *
     * @param version String that contains the identifier the version.
     */
    public void setVersion(String version){
        this.version = version;
    }
    
    /**
     * Indicates if the system is active.
     *
     * @return True/False.
     */
    public Boolean isActive(){
        return this.active;
    }
    
    /**
     * Indicates if the system is active.
     *
     * @return True/False.
     */
    public Boolean getActive(){
        return this.active;
    }
    
    /**
     * Defines if the system is active.
     *
     * @param active True/False.
     */
    public void setActive(Boolean active){
        this.active = active;
    }
}