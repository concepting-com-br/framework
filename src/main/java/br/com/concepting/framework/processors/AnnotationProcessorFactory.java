package br.com.concepting.framework.processors;

import br.com.concepting.framework.constants.ProjectConstants;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.BaseModel;
import br.com.concepting.framework.model.MainConsoleModel;
import br.com.concepting.framework.model.processors.ModelAnnotationProcessor;
import br.com.concepting.framework.model.util.ModelUtil;
import br.com.concepting.framework.persistence.constants.PersistenceConstants;
import br.com.concepting.framework.processors.helpers.ProjectBuild;
import br.com.concepting.framework.processors.interfaces.IAnnotationProcessor;
import br.com.concepting.framework.security.model.LoginSessionModel;
import br.com.concepting.framework.security.util.SecurityUtil;
import br.com.concepting.framework.util.ExceptionUtil;
import br.com.concepting.framework.util.FileUtil;
import br.com.concepting.framework.util.PropertyUtil;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import java.io.File;
import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * Class that defines a annotation processor factory.
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
@SupportedAnnotationTypes({"br.com.concepting.framework.model.annotations.Model"})
@SupportedOptions({"buildDir", "buildName", "buildVersion"})
public class AnnotationProcessorFactory extends AbstractProcessor{
    static{
        Logger.getLogger(PersistenceConstants.DEFAULT_PERSISTENCE_PACKAGE_ID).setLevel(Level.OFF);
        
        Thread.currentThread().setContextClassLoader(AnnotationProcessorFactory.class.getClassLoader());
    }

    private ProcessingEnvironment environment = null;
    private ProjectBuild build = null;
    
    /**
     * Returns the instance that contains the build information.
     *
     * @return Instance that contains the build information.
     */
    public ProjectBuild getBuild(){
        return this.build;
    }
    
    /**
     * Defines the instance that contains the build information.
     *
     * @param build Instance that contains the build information.
     */
    public void setBuild(ProjectBuild build){
        this.build = build;
    }
    
    /**
     * Returns the instance that contains the properties of the operating system
     * environment.
     *
     * @return Instance that contains the properties.
     */
    public ProcessingEnvironment getEnvironment(){
        return this.environment;
    }
    
    /**
     * Defines the instance that contains the properties of the operating system
     * environment.
     *
     * @param environment Instance that contains the properties.
     * @throws InternalErrorException Occurs when was not possible to execute the operation.
     */
    private void setEnvironment(ProcessingEnvironment environment) throws InternalErrorException{
        try{
            this.environment = environment;
            this.build = new ProjectBuild();
            this.build.setName(getBuildName());
            this.build.setVersion(getBuildVersion());
            this.build.setBaseDirname(getBuildBaseDirname());
            this.build.setResourcesDirname(getBuildResourcesDirname());
            
            ExpressionProcessorUtil.setVariable(ProjectConstants.BUILD_ATTRIBUTE_ID, this.build);
        }
        catch(Throwable e){
            this.environment.getMessager().printMessage(Kind.ERROR, ExceptionUtil.getTrace(e));
        }
    }
    
    /**
     * @see javax.annotation.processing.AbstractProcessor#getSupportedSourceVersion()
     */
    public SourceVersion getSupportedSourceVersion(){
        return SourceVersion.latestSupported();
    }
    
    /**
     * Returns the identifier of the project.
     *
     * @return String that contains the identifier.
     */
    public String getBuildName(){
        return this.environment.getOptions().get(ProjectConstants.BUILD_NAME_ATTRIBUTE_ID);
    }
    
    /**
     * Returns the version of the project.
     *
     * @return String that contains the version.
     */
    public String getBuildVersion(){
        return this.environment.getOptions().get(ProjectConstants.BUILD_VERSION_ATTRIBUTE_ID);
    }
    
    /**
     * Returns the the directory of the project.
     *
     * @return String that contains the directory.
     */
    private String getBuildBaseDirname(){
        String baseDirname = new File(this.environment.getOptions().get(ProjectConstants.BUILD_DIR_ATTRIBUTE_ID)).getAbsolutePath();
        
        if(!baseDirname.endsWith(FileUtil.getDirectorySeparator()))
            baseDirname = baseDirname.concat(FileUtil.getDirectorySeparator());
        
        return baseDirname;
    }
    
    /**
     * Returns the directory of the project resources.
     *
     * @return String that contains the directory.
     */
    public String getBuildResourcesDirname(){
        String baseDirname = getBuildBaseDirname();
        
        if(baseDirname != null && baseDirname.length() > 0){
            StringBuilder resourcesDirname = new StringBuilder();
            
            resourcesDirname.append(baseDirname);
            resourcesDirname.append(ProjectConstants.DEFAULT_RESOURCES_DIR);
            
            return resourcesDirname.toString();
        }
        
        return null;
    }
    
    /**
     * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.processing.ProcessingEnvironment)
     */
    public synchronized void init(ProcessingEnvironment environment){
        try{
            super.init(environment);
            
            setEnvironment(environment);
        }
        catch(InternalErrorException e){
            this.environment.getMessager().printMessage(Kind.ERROR, ExceptionUtil.getTrace(e));
        }
    }
    
    /**
     * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set,
     * javax.annotation.processing.RoundEnvironment)
     */
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment environment){
        try{
            Set<? extends Element> declarations = null;
            IAnnotationProcessor annotationProcessor = null;
            String declarationClassName = null;
            
            if(annotations != null && !annotations.isEmpty()){
                for(TypeElement annotation: annotations){
                    declarations = environment.getElementsAnnotatedWith(annotation);
                    
                    if(declarations != null && !declarations.isEmpty()){
                        Class<? extends MainConsoleModel> mainConsoleClass = ModelUtil.getMainConsoleClass();
                        Class<? extends LoginSessionModel> loginSessionClass = SecurityUtil.getLoginSessionClass();
                        String loginSessionClassName = null;
                        String mainConsoleClassName = null;
                        
                        for(Element declaration: declarations){
                            declarationClassName = declaration.toString();
                            
                            if(loginSessionClass != null && declarationClassName.equals(loginSessionClass.getName()))
                                loginSessionClassName = declarationClassName;
                            else if(mainConsoleClass != null && declarationClassName.equals(mainConsoleClass.getName()))
                                mainConsoleClassName = declarationClassName;
                            else if(PropertyUtil.isModel(declarationClassName)){
                                annotationProcessor = getAnnotationProcessor(declarationClassName);
                                
                                if(annotationProcessor != null)
                                    annotationProcessor.process();
                            }
                        }
                        
                        if(loginSessionClassName != null){
                            annotationProcessor = getAnnotationProcessor(loginSessionClassName);
                            
                            if(annotationProcessor != null)
                                annotationProcessor.process();
                        }
                        
                        if(mainConsoleClassName != null){
                            annotationProcessor = getAnnotationProcessor(mainConsoleClassName);
                            
                            if(annotationProcessor != null)
                                annotationProcessor.process();
                        }
                    }
                }
            }
            
            return true;
        }
        catch(Throwable e){
            this.environment.getMessager().printMessage(Kind.ERROR, ExceptionUtil.getTrace(e));
            
            return false;
        }
    }
    
    /**
     * Returns the annotation processor based of the declaration of the object
     * that will be processed.
     *
     * @param declarationClassName String that contains the class name of the
     * object that will be processed.
     * @return Instance that contains the annotation processor.
     * @throws InternalErrorException Occurs when was not possible to execute
     * the operation.
     */
    @SuppressWarnings("unchecked")
    public IAnnotationProcessor getAnnotationProcessor(String declarationClassName) throws InternalErrorException{
        try{
            Class<? extends BaseModel> declarationClass = (Class<? extends BaseModel>) Class.forName(declarationClassName);
            
            if(PropertyUtil.isModel(declarationClass) && !Modifier.isAbstract(declarationClass.getModifiers()))
                return new ModelAnnotationProcessor(declarationClass, this);
            
            return null;
        }
        catch(ClassNotFoundException e){
            throw new InternalErrorException(e);
        }
    }
}