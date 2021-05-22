package br.com.concepting.framework.model.processors;

import br.com.concepting.framework.audit.Auditor;
import br.com.concepting.framework.audit.resources.AuditorResources;
import br.com.concepting.framework.audit.resources.AuditorResourcesLoader;
import br.com.concepting.framework.constants.Constants;
import br.com.concepting.framework.constants.ProjectConstants;
import br.com.concepting.framework.constants.SystemConstants;
import br.com.concepting.framework.controller.action.BaseAction;
import br.com.concepting.framework.controller.form.BaseActionForm;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.BaseModel;
import br.com.concepting.framework.model.SystemModuleModel;
import br.com.concepting.framework.model.SystemSessionModel;
import br.com.concepting.framework.model.helpers.ModelInfo;
import br.com.concepting.framework.model.util.ModelUtil;
import br.com.concepting.framework.network.constants.NetworkConstants;
import br.com.concepting.framework.persistence.constants.PersistenceConstants;
import br.com.concepting.framework.persistence.resources.PersistenceResources;
import br.com.concepting.framework.persistence.resources.PersistenceResourcesLoader;
import br.com.concepting.framework.persistence.types.RepositoryType;
import br.com.concepting.framework.processors.*;
import br.com.concepting.framework.processors.annotations.Tag;
import br.com.concepting.framework.processors.constants.ProcessorConstants;
import br.com.concepting.framework.processors.helpers.ProjectBuild;
import br.com.concepting.framework.resources.FactoryResources;
import br.com.concepting.framework.resources.constants.ResourcesConstants;
import br.com.concepting.framework.security.constants.SecurityConstants;
import br.com.concepting.framework.security.model.LoginSessionModel;
import br.com.concepting.framework.security.model.UserModel;
import br.com.concepting.framework.security.util.SecurityUtil;
import br.com.concepting.framework.service.interfaces.IService;
import br.com.concepting.framework.service.util.ServiceUtil;
import br.com.concepting.framework.ui.constants.UIConstants;
import br.com.concepting.framework.util.*;
import br.com.concepting.framework.util.constants.XmlConstants;
import br.com.concepting.framework.util.helpers.DateTime;
import br.com.concepting.framework.util.helpers.JavaIndent;
import br.com.concepting.framework.util.helpers.JspIndent;
import br.com.concepting.framework.util.helpers.XmlNode;
import org.dom4j.DocumentException;
import org.dom4j.DocumentType;
import org.dom4j.tree.DefaultDocumentType;

import javax.tools.Diagnostic;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;

/**
 * Class that defines the data model processor.
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
public class ModelAnnotationProcessor extends BaseAnnotationProcessor{
    private ModelInfo modelInfo = null;
    private ProjectBuild build = null;
    
    /**
     * Constructor - Initializes the annotation processor.
     *
     * @param declaration Instance that contains the annotated class.
     * @param annotationProcessorFactory Instance that contains the annotation processor
     * factory.
     * @throws InternalErrorException Occurs when was not possible to
     * instantiate the processor.
     */
    public ModelAnnotationProcessor(Class<? extends BaseModel> declaration, AnnotationProcessorFactory annotationProcessorFactory) throws InternalErrorException{
        super(declaration, annotationProcessorFactory);
        
        try{
            this.modelInfo = ModelUtil.getInfo(declaration);
            this.build = annotationProcessorFactory.getBuild();
        }
        catch(IllegalArgumentException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException | NoSuchFieldException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Returns the instance that contains the properties of the data model.
     *
     * @return Instance that contains the properties of the data model.
     */
    public ModelInfo getModelInfo(){
        return this.modelInfo;
    }
    
    /**
     * Defines the instance that contains the properties of the data model.
     *
     * @param modelInfo Instance that contains the properties of the data model.
     */
    public void setModelInfo(ModelInfo modelInfo){
        this.modelInfo = modelInfo;
    }
    
    /**
     * @see br.com.concepting.framework.processors.BaseAnnotationProcessor#process()
     */
    public void process() throws InternalErrorException{
        if(modelInfo.isAbstract() != null && modelInfo.isAbstract())
            return;
        
        LoginSessionModel loginSession = SecurityUtil.getLoginSession(this.build.getResourcesDirname());
        
        loginSession.setStartDateTime(new DateTime());
        
        UserModel user = loginSession.getUser();
        
        user.setName(SecurityUtil.getSystemUserName());
        
        SystemModuleModel systemModule = loginSession.getSystemModule();
        
        systemModule.setName(this.build.getName());
        systemModule.setVersion(this.build.getVersion());
        
        SystemSessionModel systemSession = loginSession.getSystemSession();
        
        systemSession.setId(SecurityUtil.generateToken());
        
        try{
            InetAddress localhost = InetAddress.getLocalHost();
            
            systemSession.setIp(localhost.getHostAddress());
            systemSession.setHostName(localhost.getHostName());
        }
        catch(UnknownHostException e){
            systemSession.setIp(NetworkConstants.DEFAULT_LOCALHOST_ADDRESS_ID);
            systemSession.setHostName(NetworkConstants.DEFAULT_LOCALHOST_NAME_ID);
        }
        
        ExpressionProcessorUtil.setVariable(ProcessorConstants.DEFAULT_NOW_ID, loginSession.getStartDateTime());
        ExpressionProcessorUtil.setVariable(ProcessorConstants.DEFAULT_RANDOM_ID, (int) (Math.random() * NumberUtil.getMaximumRange(Long.class)));
        ExpressionProcessorUtil.setVariable(SecurityConstants.USER_ATTRIBUTE_ID, loginSession.getUser());
        
        StringBuilder templateFilesDirname = new StringBuilder();
        
        templateFilesDirname.append(this.build.getBaseDirname());
        templateFilesDirname.append(ProjectConstants.DEFAULT_TEMPLATES_DIR);
        templateFilesDirname.append(this.modelInfo.getTemplateId());
        
        File templateFilesDir = new File(templateFilesDirname.toString());
        
        if(templateFilesDir.exists()){
            Method[] templateMethods = getClass().getMethods();
            File[] templateFiles = templateFilesDir.listFiles();
    
            AuditorResourcesLoader loader = new AuditorResourcesLoader(this.build.getResourcesDirname());
            AuditorResources auditorResources = loader.getDefault();
            Auditor auditor = null;
            
            for(File templateFile: templateFiles){
                for(Method templateMethod: templateMethods){
                    try{
                        Tag methodTag = templateMethod.getAnnotation(Tag.class);
                        
                        if(methodTag != null && methodTag.value().equals(templateFile.getName())){
                            if(auditorResources != null){
                                Class<?> entity = getClass();
                                Method business = templateMethod;
                                
                                auditor = new Auditor(entity, business, new Object[]{this.modelInfo.getClazz().getName()}, loginSession, auditorResources);
                                auditor.start();
                            }
    
                            templateMethod.invoke(this, templateFile.getAbsolutePath());
                            
                            if(auditor != null)
                                auditor.end();
                        }
                    }
                    catch(Throwable e){
                        if(auditor != null)
                            auditor.end(e);
                        else
                            getAnnotationProcessorFactory().getEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR,ExceptionUtil.getTrace(e));
                    }
                }
            }
        }
        else
            getAnnotationProcessorFactory().getEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR, "No templates found in ".concat(templateFilesDir.getAbsolutePath()).concat("!"));
    }
    
    /**
     * Generates the action class.
     *
     * @param actionClassTemplateFilename String that contains the action class template filename.
     * @throws InternalErrorException Occurs when was not possible to generate
     * the class.
     */
    @SuppressWarnings("unchecked")
    @Tag(ProjectConstants.DEFAULT_ACTION_CLASS_TEMPLATE_FILE_ID)
    public void generateActionClass(String actionClassTemplateFilename) throws InternalErrorException{
        try{
            File actionClassTemplateFile = new File(actionClassTemplateFilename);
            XmlReader actionClassTemplateReader = new XmlReader(actionClassTemplateFile);
            String encoding = actionClassTemplateReader.getEncoding();
            XmlNode actionClassTemplateNode = actionClassTemplateReader.getRoot();
            List<XmlNode> actionClassTemplateArtifactsNode = actionClassTemplateNode.getChildren();
            
            if(actionClassTemplateArtifactsNode != null && !actionClassTemplateArtifactsNode.isEmpty()){
                ProjectBuild build = getAnnotationProcessorFactory().getBuild();
                ProcessorFactory processorFactory = ProcessorFactory.getInstance();
                ExpressionProcessor expressionProcessor = new ExpressionProcessor(this.modelInfo);
                
                for(XmlNode actionClassTemplateArtifactNode: actionClassTemplateArtifactsNode){
                    String outputDir = expressionProcessor.evaluate(actionClassTemplateArtifactNode.getAttribute(Constants.OUTPUT_DIRECTORY_ATTRIBUTE_ID));
                    String packagePrefix = expressionProcessor.evaluate(actionClassTemplateArtifactNode.getAttribute(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID));
                    String packageName = expressionProcessor.evaluate(actionClassTemplateArtifactNode.getAttribute(Constants.PACKAGE_NAME_ATTRIBUTE_ID));
                    String packageSuffix = expressionProcessor.evaluate(actionClassTemplateArtifactNode.getAttribute(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID));
                    String name = expressionProcessor.evaluate(actionClassTemplateArtifactNode.getAttribute(Constants.NAME_ATTRIBUTE_ID));
                    StringBuilder packageNameBuffer = new StringBuilder();
                    
                    if(packagePrefix != null && packagePrefix.length() > 0)
                        packageNameBuffer.append(packagePrefix);
                    
                    if(packageName != null && packageName.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageName);
                    }
                    
                    if(packageSuffix != null && packageSuffix.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageSuffix);
                    }
                    
                    packageName = packageNameBuffer.toString();
                    
                    StringBuilder actionClassName = new StringBuilder();
                    
                    if(packageName != null && packageName.length() > 0){
                        actionClassName.append(packageName);
                        actionClassName.append(".");
                    }
                    
                    actionClassName.append(name);
                    
                    StringBuilder actionClassFilename = new StringBuilder();
                    
                    if(outputDir != null && outputDir.length() > 0){
                        actionClassFilename.append(outputDir);
                        actionClassFilename.append(FileUtil.getDirectorySeparator());
                    }
                    else{
                        actionClassFilename.append(build.getBaseDirname());
                        actionClassFilename.append(FileUtil.getDirectorySeparator());
                        actionClassFilename.append(ProjectConstants.DEFAULT_JAVA_DIR);
                    }
                    
                    actionClassFilename.append(StringUtil.replaceAll(actionClassName.toString(), ".", FileUtil.getDirectorySeparator()));
                    actionClassFilename.append(ProjectConstants.DEFAULT_JAVA_FILE_EXTENSION);
                    
                    File actionClassFile = new File(actionClassFilename.toString());
                    
                    if(!actionClassFile.exists()){
                        if(this.modelInfo.generateActionsAndForm() != null && this.modelInfo.generateActionsAndForm()){
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID, packagePrefix);
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID, packageSuffix);
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_NAME_ATTRIBUTE_ID, packageName);
                            ExpressionProcessorUtil.setVariable(Constants.NAME_ATTRIBUTE_ID, name);
                            
                            GenericProcessor processor = processorFactory.getProcessor(this.modelInfo, actionClassTemplateArtifactNode);
                            String actionClassContent = StringUtil.indent(processor.process(), JavaIndent.getRules());
                            
                            if(actionClassContent != null && actionClassContent.length() > 0)
                                FileUtil.toTextFile(actionClassFilename.toString(), actionClassContent, encoding);
                        }
                    }
                    else{
                        if(this.modelInfo.generateActionsAndForm() == null || !this.modelInfo.generateActionsAndForm()){
                            Class<? extends BaseAction<? extends BaseModel>> actionClass = null;
                            
                            try{
                                actionClass = (Class<? extends BaseAction<? extends BaseModel>>) Class.forName(actionClassName.toString());
                                
                                if(!Modifier.isAbstract(actionClass.getModifiers()))
                                    actionClassFile.delete();
                            }
                            catch(ClassNotFoundException e){
                                actionClassFile.delete();
                            }
                        }
                    }
                }
            }
        }
        catch(IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Generates the form class.
     *
     * @param actionFormClassTemplateFilename String that contains the action form class template filename.
     * @throws InternalErrorException Occurs when was not possible to generate
     * the class.
     */
    @SuppressWarnings("unchecked")
    @Tag(ProjectConstants.DEFAULT_ACTION_FORM_CLASS_TEMPLATE_FILE_ID)
    public void generateActionFormClass(String actionFormClassTemplateFilename) throws InternalErrorException{
        try{
            File actionFormClassTemplateFile = new File(actionFormClassTemplateFilename);
            XmlReader actionFormClassTemplateReader = new XmlReader(actionFormClassTemplateFile);
            String encoding = actionFormClassTemplateReader.getEncoding();
            XmlNode actionFormClassTemplateNode = actionFormClassTemplateReader.getRoot();
            List<XmlNode> actionFormClassTemplateArtifactsNode = actionFormClassTemplateNode.getChildren();
            
            if(actionFormClassTemplateArtifactsNode != null && !actionFormClassTemplateArtifactsNode.isEmpty()){
                ProcessorFactory processorFactory = ProcessorFactory.getInstance();
                ExpressionProcessor expressionProcessor = new ExpressionProcessor(this.modelInfo);
                
                for(XmlNode actionFormClassTemplateArtifactNode: actionFormClassTemplateArtifactsNode){
                    String outputDir = expressionProcessor.evaluate(actionFormClassTemplateArtifactNode.getAttribute(Constants.OUTPUT_DIRECTORY_ATTRIBUTE_ID));
                    String packagePrefix = expressionProcessor.evaluate(actionFormClassTemplateArtifactNode.getAttribute(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID));
                    String packageName = expressionProcessor.evaluate(actionFormClassTemplateArtifactNode.getAttribute(Constants.PACKAGE_NAME_ATTRIBUTE_ID));
                    String packageSuffix = expressionProcessor.evaluate(actionFormClassTemplateArtifactNode.getAttribute(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID));
                    String name = expressionProcessor.evaluate(actionFormClassTemplateArtifactNode.getAttribute(Constants.NAME_ATTRIBUTE_ID));
                    StringBuilder packageNameBuffer = new StringBuilder();
                    
                    if(packagePrefix != null && packagePrefix.length() > 0)
                        packageNameBuffer.append(packagePrefix);
                    
                    if(packageName != null && packageName.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageName);
                    }
                    
                    if(packageSuffix != null && packageSuffix.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageSuffix);
                    }
                    
                    packageName = packageNameBuffer.toString();
                    
                    StringBuilder actionFormClassName = new StringBuilder();
                    
                    if(packageName != null && packageName.length() > 0){
                        actionFormClassName.append(packageName);
                        actionFormClassName.append(".");
                    }
                    
                    actionFormClassName.append(name);
                    
                    StringBuilder actionFormClassFilename = new StringBuilder();
                    
                    if(outputDir != null && outputDir.length() > 0){
                        actionFormClassFilename.append(outputDir);
                        actionFormClassFilename.append(FileUtil.getDirectorySeparator());
                    }
                    else{
                        actionFormClassFilename.append(this.build.getBaseDirname());
                        actionFormClassFilename.append(FileUtil.getDirectorySeparator());
                        actionFormClassFilename.append(ProjectConstants.DEFAULT_JAVA_DIR);
                    }
                    
                    actionFormClassFilename.append(StringUtil.replaceAll(actionFormClassName.toString(), ".", FileUtil.getDirectorySeparator()));
                    actionFormClassFilename.append(ProjectConstants.DEFAULT_JAVA_FILE_EXTENSION);
                    
                    File actionFormClassFile = new File(actionFormClassFilename.toString());
                    
                    if(!actionFormClassFile.exists()){
                        if(this.modelInfo.generateActionsAndForm() != null && this.modelInfo.generateActionsAndForm()){
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID, packagePrefix);
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID, packageSuffix);
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_NAME_ATTRIBUTE_ID, packageName);
                            ExpressionProcessorUtil.setVariable(Constants.NAME_ATTRIBUTE_ID, name);
                            
                            GenericProcessor processor = processorFactory.getProcessor(this.modelInfo, actionFormClassTemplateArtifactNode);
                            String actionFormClassContent = StringUtil.indent(processor.process(), JavaIndent.getRules());
                            
                            if(actionFormClassContent != null && actionFormClassContent.length() > 0)
                                FileUtil.toTextFile(actionFormClassFilename.toString(), actionFormClassContent, encoding);
                        }
                    }
                    else{
                        if(this.modelInfo.generateActionsAndForm() == null || !this.modelInfo.generateActionsAndForm()){
                            Class<? extends BaseActionForm<? extends BaseModel>> actionFormClass = null;
                            
                            try{
                                actionFormClass = (Class<? extends BaseActionForm<? extends BaseModel>>) Class.forName(actionFormClassName.toString());
                                
                                if(!Modifier.isAbstract(actionFormClass.getModifiers()))
                                    actionFormClassFile.delete();
                            }
                            catch(ClassNotFoundException e){
                                actionFormClassFile.delete();
                            }
                        }
                    }
                }
            }
        }
        catch(IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Generates the model class.
     *
     * @param modelClassTemplateFilename String that contains the model class template filename.
     * @throws InternalErrorException Occurs when was not possible to generate
     * the class.
     */
    @Tag(ProjectConstants.DEFAULT_MODEL_CLASS_TEMPLATE_FILE_ID)
    public void generateModelClass(String modelClassTemplateFilename) throws InternalErrorException{
        try{
            File modelClassTemplateFile = new File(modelClassTemplateFilename);
            XmlReader modelClassTemplateReader = new XmlReader(modelClassTemplateFile);
            String encoding = modelClassTemplateReader.getEncoding();
            XmlNode modelClassTemplateNode = modelClassTemplateReader.getRoot();
            List<XmlNode> modelClassTemplateArtifactsNode = modelClassTemplateNode.getChildren();
            
            if(modelClassTemplateArtifactsNode != null && !modelClassTemplateArtifactsNode.isEmpty()){
                ProcessorFactory processorFactory = ProcessorFactory.getInstance();
                ExpressionProcessor expressionProcessor = new ExpressionProcessor(this.modelInfo);
                
                for(XmlNode modelClassTemplateArtifactNode: modelClassTemplateArtifactsNode){
                    String outputDir = expressionProcessor.evaluate(modelClassTemplateArtifactNode.getAttribute(Constants.OUTPUT_DIRECTORY_ATTRIBUTE_ID));
                    String packagePrefix = expressionProcessor.evaluate(modelClassTemplateArtifactNode.getAttribute(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID));
                    String packageName = expressionProcessor.evaluate(modelClassTemplateArtifactNode.getAttribute(Constants.PACKAGE_NAME_ATTRIBUTE_ID));
                    String packageSuffix = expressionProcessor.evaluate(modelClassTemplateArtifactNode.getAttribute(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID));
                    String name = expressionProcessor.evaluate(modelClassTemplateArtifactNode.getAttribute(Constants.NAME_ATTRIBUTE_ID));
                    StringBuilder packageNameBuffer = new StringBuilder();
                    
                    if(packagePrefix != null && packagePrefix.length() > 0)
                        packageNameBuffer.append(packagePrefix);
                    
                    if(packageName != null && packageName.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageName);
                    }
                    
                    if(packageSuffix != null && packageSuffix.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageSuffix);
                    }
                    
                    packageName = packageNameBuffer.toString();
                    
                    StringBuilder modelClassName = new StringBuilder();
                    
                    if(packageName != null && packageName.length() > 0){
                        modelClassName.append(packageName);
                        modelClassName.append(".");
                    }
                    
                    modelClassName.append(name);
                    
                    StringBuilder modelClassFilename = new StringBuilder();
                    
                    if(outputDir != null && outputDir.length() > 0){
                        modelClassFilename.append(outputDir);
                        modelClassFilename.append(FileUtil.getDirectorySeparator());
                    }
                    else{
                        modelClassFilename.append(this.build.getBaseDirname());
                        modelClassFilename.append(FileUtil.getDirectorySeparator());
                        modelClassFilename.append(ProjectConstants.DEFAULT_JAVA_DIR);
                    }
                    
                    modelClassFilename.append(StringUtil.replaceAll(modelClassName.toString(), ".", FileUtil.getDirectorySeparator()));
                    modelClassFilename.append(ProjectConstants.DEFAULT_JAVA_FILE_EXTENSION);
                    
                    File modelClassFile = new File(modelClassFilename.toString());
                    
                    if(!modelClassFile.exists()){
                        if((this.modelInfo.generatePersistence() != null && this.modelInfo.generatePersistence()) || (this.modelInfo.generateService() != null && this.modelInfo.generateService())){
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID, packagePrefix);
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID, packageSuffix);
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_NAME_ATTRIBUTE_ID, packageName);
                            ExpressionProcessorUtil.setVariable(Constants.NAME_ATTRIBUTE_ID, name);
                            
                            GenericProcessor processor = processorFactory.getProcessor(this.modelInfo, modelClassTemplateArtifactNode);
                            String modelClassContent = StringUtil.indent(processor.process(), JavaIndent.getRules());
                            
                            if(modelClassContent != null && modelClassContent.length() > 0)
                                FileUtil.toTextFile(modelClassFilename.toString(), modelClassContent, encoding);
                        }
                        else{
                            if((this.modelInfo.generatePersistence() == null || !this.modelInfo.generatePersistence()) && (this.modelInfo.generateService() == null || !this.modelInfo.generateService()))
                                modelClassFile.delete();
                        }
                    }
                }
            }
        }
        catch(IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Generates the unit tests of the model class.
     *
     * @param modelClassTestTemplateFilename String that contains the model class unit test template filename.
     * @throws InternalErrorException Occurs when was not possible to generate
     * the class.
     */
    @Tag(ProjectConstants.DEFAULT_MODEL_CLASS_TEST_TEMPLATE_FILE_ID)
    public void generateModelClassTest(String modelClassTestTemplateFilename) throws InternalErrorException{
        try{
            File modelClassTestTemplateFile = new File(modelClassTestTemplateFilename);
            XmlReader modelClassTestTemplateReader = new XmlReader(modelClassTestTemplateFile);
            String encoding = modelClassTestTemplateReader.getEncoding();
            XmlNode modelClassTestTemplateNode = modelClassTestTemplateReader.getRoot();
            List<XmlNode> modelClassTestTemplateArtifactsNode = modelClassTestTemplateNode.getChildren();
            
            if(modelClassTestTemplateArtifactsNode != null && !modelClassTestTemplateArtifactsNode.isEmpty()){
                ProcessorFactory processorFactory = ProcessorFactory.getInstance();
                ExpressionProcessor expressionProcessor = new ExpressionProcessor(this.modelInfo);
                
                for(XmlNode modelClassTestTemplateArtifactNode: modelClassTestTemplateArtifactsNode){
                    String outputDir = expressionProcessor.evaluate(modelClassTestTemplateArtifactNode.getAttribute(Constants.OUTPUT_DIRECTORY_ATTRIBUTE_ID));
                    String packagePrefix = expressionProcessor.evaluate(modelClassTestTemplateArtifactNode.getAttribute(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID));
                    String packageName = expressionProcessor.evaluate(modelClassTestTemplateArtifactNode.getAttribute(Constants.PACKAGE_NAME_ATTRIBUTE_ID));
                    String packageSuffix = expressionProcessor.evaluate(modelClassTestTemplateArtifactNode.getAttribute(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID));
                    String name = expressionProcessor.evaluate(modelClassTestTemplateArtifactNode.getAttribute(Constants.NAME_ATTRIBUTE_ID));
                    StringBuilder packageNameBuffer = new StringBuilder();
                    
                    if(packagePrefix != null && packagePrefix.length() > 0)
                        packageNameBuffer.append(packagePrefix);
                    
                    if(packageName != null && packageName.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageName);
                    }
                    
                    if(packageSuffix != null && packageSuffix.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageSuffix);
                    }
                    
                    packageName = packageNameBuffer.toString();
                    
                    StringBuilder modelClassName = new StringBuilder();
                    
                    if(packageName != null && packageName.length() > 0){
                        modelClassName.append(packageName);
                        modelClassName.append(".");
                    }
                    
                    modelClassName.append(name);
                    
                    StringBuilder modelClassTestFilename = new StringBuilder();
                    
                    if(outputDir != null && outputDir.length() > 0){
                        modelClassTestFilename.append(outputDir);
                        modelClassTestFilename.append(FileUtil.getDirectorySeparator());
                    }
                    else{
                        modelClassTestFilename.append(this.build.getBaseDirname());
                        modelClassTestFilename.append(FileUtil.getDirectorySeparator());
                        modelClassTestFilename.append(ProjectConstants.DEFAULT_TESTS_DIR);
                    }
                    
                    modelClassTestFilename.append(StringUtil.replaceAll(modelClassName.toString(), ".", FileUtil.getDirectorySeparator()));
                    modelClassTestFilename.append(ProjectConstants.DEFAULT_JAVA_FILE_EXTENSION);
                    
                    File modelClassFile = new File(modelClassTestFilename.toString());
                    
                    if(!modelClassFile.exists()){
                        ExpressionProcessorUtil.setVariable(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID, packagePrefix);
                        ExpressionProcessorUtil.setVariable(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID, packageSuffix);
                        ExpressionProcessorUtil.setVariable(Constants.PACKAGE_NAME_ATTRIBUTE_ID, packageName);
                        ExpressionProcessorUtil.setVariable(Constants.NAME_ATTRIBUTE_ID, name);
                        
                        GenericProcessor processor = processorFactory.getProcessor(this.modelInfo, modelClassTestTemplateArtifactNode);
                        String modelClassTestContent = StringUtil.indent(processor.process(), JavaIndent.getRules());
                        
                        if(modelClassTestContent != null && modelClassTestContent.length() > 0)
                            FileUtil.toTextFile(modelClassTestFilename.toString(), modelClassTestContent, encoding);
                    }
                }
            }
        }
        catch(IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Generates the resources.
     *
     * @param resourcesTemplateFilename String that contains the resources template filename.
     * @throws InternalErrorException Occurs when was not possible to generate
     * the file.
     */
    @Tag(ProjectConstants.DEFAULT_RESOURCES_TEMPLATE_FILE_ID)
    public void generateResources(String resourcesTemplateFilename) throws InternalErrorException{
        try{
            File resourcesTemplateFile = new File(resourcesTemplateFilename);
            XmlReader resourcesTemplateReader = new XmlReader(resourcesTemplateFile);
            String encoding = resourcesTemplateReader.getEncoding();
            XmlNode resourcesTemplateNode = resourcesTemplateReader.getRoot();
            List<XmlNode> resourcesTemplateArtifactsNode = resourcesTemplateNode.getChildren();
            
            if(resourcesTemplateArtifactsNode != null && !resourcesTemplateArtifactsNode.isEmpty()){
                Collection<String> availableLanguages = LanguageUtil.getAvailableLanguages();
                
                if(availableLanguages != null && !availableLanguages.isEmpty()){
                    ProcessorFactory processorFactory = ProcessorFactory.getInstance();
                    ExpressionProcessor expressionProcessor = new ExpressionProcessor(this.modelInfo);
                    
                    for(XmlNode resourcesTemplateArtifactNode: resourcesTemplateArtifactsNode){
                        String outputDir = expressionProcessor.evaluate(resourcesTemplateArtifactNode.getAttribute(Constants.OUTPUT_DIRECTORY_ATTRIBUTE_ID));
                        String packagePrefix = expressionProcessor.evaluate(resourcesTemplateArtifactNode.getAttribute(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID));
                        String packageName = expressionProcessor.evaluate(resourcesTemplateArtifactNode.getAttribute(Constants.PACKAGE_NAME_ATTRIBUTE_ID));
                        String packageSuffix = expressionProcessor.evaluate(resourcesTemplateArtifactNode.getAttribute(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID));
                        String name = expressionProcessor.evaluate(resourcesTemplateArtifactNode.getAttribute(Constants.NAME_ATTRIBUTE_ID));
                        StringBuilder packageNameBuffer = new StringBuilder();
                        
                        if(packagePrefix != null && packagePrefix.length() > 0)
                            packageNameBuffer.append(packagePrefix);
                        
                        if(packageName != null && packageName.length() > 0){
                            if(packageNameBuffer.length() > 0)
                                packageNameBuffer.append(".");
                            
                            packageNameBuffer.append(packageName);
                        }
                        
                        if(packageSuffix != null && packageSuffix.length() > 0){
                            if(packageNameBuffer.length() > 0)
                                packageNameBuffer.append(".");
                            
                            packageNameBuffer.append(packageSuffix);
                        }
                        
                        packageName = packageNameBuffer.toString();
                        
                        StringBuilder resourcesName = new StringBuilder();
                        
                        if(packageName != null && packageName.length() > 0){
                            resourcesName.append(packageName);
                            resourcesName.append(".");
                        }
                        
                        resourcesName.append(name);
                        
                        for(String availableLanguage: availableLanguages){
                            StringBuilder resourcesFilename = new StringBuilder();
                            
                            if(outputDir != null && outputDir.length() > 0){
                                resourcesFilename.append(outputDir);
                                resourcesFilename.append(FileUtil.getDirectorySeparator());
                            }
                            else{
                                resourcesFilename.append(this.build.getBaseDirname());
                                resourcesFilename.append(FileUtil.getDirectorySeparator());
                                resourcesFilename.append(ProjectConstants.DEFAULT_RESOURCES_DIR);
                            }
                            
                            resourcesFilename.append(StringUtil.replaceAll(resourcesName.toString(), ".", FileUtil.getDirectorySeparator()));
                            resourcesFilename.append("_");
                            resourcesFilename.append(availableLanguage);
                            resourcesFilename.append(ResourcesConstants.DEFAULT_PROPERTIES_RESOURCES_FILE_EXTENSION);
                            
                            File resourcesFile = new File(resourcesFilename.toString());
                            
                            if(!resourcesFile.exists()){
                                if((this.modelInfo.generateWebService() != null && this.modelInfo.generateWebService()) || (this.modelInfo.generateService() != null && this.modelInfo.generateService()) || (this.modelInfo.generateUi() != null && this.modelInfo.generateUi()) || (this.modelInfo.generateActionsAndForm() != null && this.modelInfo.generateActionsAndForm())){
                                    ExpressionProcessorUtil.setVariable(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID, packagePrefix);
                                    ExpressionProcessorUtil.setVariable(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID, packageSuffix);
                                    ExpressionProcessorUtil.setVariable(Constants.PACKAGE_NAME_ATTRIBUTE_ID, packageName);
                                    ExpressionProcessorUtil.setVariable(Constants.NAME_ATTRIBUTE_ID, name);
                                    
                                    GenericProcessor processor = processorFactory.getProcessor(this.modelInfo, resourcesTemplateArtifactNode);
                                    String resourcesContent = StringUtil.indent(processor.process(), JavaIndent.getRules());
                                    
                                    FileUtil.toTextFile(resourcesFilename.toString(), resourcesContent, encoding);
                                }
                            }
                            else{
                                if((this.modelInfo.generateWebService() == null || !this.modelInfo.generateWebService()) && (this.modelInfo.generateService() == null || !this.modelInfo.generateService()) && (this.modelInfo.generateUi() == null || !this.modelInfo.generateUi()) && (this.modelInfo.generateActionsAndForm() == null || !this.modelInfo.generateActionsAndForm()))
                                    resourcesFile.delete();
                            }
                        }
                    }
                }
            }
        }
        catch(IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Generates the persistence class.
     *
     * @param persistenceClassTemplateFilename String that contains the persistence class template filename.
     * @throws InternalErrorException Occurs when was not possible to generate
     * the class.
     */
    @Tag(ProjectConstants.DEFAULT_PERSISTENCE_CLASS_TEMPLATE_FILE_ID)
    public void generatePersistenceClass(String persistenceClassTemplateFilename) throws InternalErrorException{
        try{
            File persistenceClassTemplateFile = new File(persistenceClassTemplateFilename);
            XmlReader persistenceClassTemplateReader = new XmlReader(persistenceClassTemplateFile);
            String encoding = persistenceClassTemplateReader.getEncoding();
            XmlNode persistenceClassTemplateNode = persistenceClassTemplateReader.getRoot();
            List<XmlNode> persistenceClassTemplateArtifactsNode = persistenceClassTemplateNode.getChildren();
            
            if(persistenceClassTemplateArtifactsNode != null && !persistenceClassTemplateArtifactsNode.isEmpty()){
                ProcessorFactory processorFactory = ProcessorFactory.getInstance();
                ExpressionProcessor expressionProcessor = new ExpressionProcessor(this.modelInfo);
                
                for(XmlNode persistenceClassTemplateArtifactNode: persistenceClassTemplateArtifactsNode){
                    String outputDir = expressionProcessor.evaluate(persistenceClassTemplateArtifactNode.getAttribute(Constants.OUTPUT_DIRECTORY_ATTRIBUTE_ID));
                    String packagePrefix = expressionProcessor.evaluate(persistenceClassTemplateArtifactNode.getAttribute(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID));
                    String packageName = expressionProcessor.evaluate(persistenceClassTemplateArtifactNode.getAttribute(Constants.PACKAGE_NAME_ATTRIBUTE_ID));
                    String packageSuffix = expressionProcessor.evaluate(persistenceClassTemplateArtifactNode.getAttribute(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID));
                    String name = expressionProcessor.evaluate(persistenceClassTemplateArtifactNode.getAttribute(Constants.NAME_ATTRIBUTE_ID));
                    StringBuilder packageNameBuffer = new StringBuilder();
                    
                    if(packagePrefix != null && packagePrefix.length() > 0)
                        packageNameBuffer.append(packagePrefix);
                    
                    if(packageName != null && packageName.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageName);
                    }
                    
                    if(packageSuffix != null && packageSuffix.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageSuffix);
                    }
                    
                    packageName = packageNameBuffer.toString();
                    
                    StringBuilder persistenceClassName = new StringBuilder();
                    
                    if(packageName != null && packageName.length() > 0){
                        persistenceClassName.append(packageName);
                        persistenceClassName.append(".");
                    }
                    
                    persistenceClassName.append(name);
                    
                    StringBuilder persistenceClassFilename = new StringBuilder();
                    
                    if(outputDir != null && outputDir.length() > 0){
                        persistenceClassFilename.append(outputDir);
                        persistenceClassFilename.append(FileUtil.getDirectorySeparator());
                    }
                    else{
                        persistenceClassFilename.append(this.build.getBaseDirname());
                        persistenceClassFilename.append(FileUtil.getDirectorySeparator());
                        persistenceClassFilename.append(ProjectConstants.DEFAULT_JAVA_DIR);
                    }
                    
                    persistenceClassFilename.append(StringUtil.replaceAll(persistenceClassName.toString(), ".", FileUtil.getDirectorySeparator()));
                    persistenceClassFilename.append(ProjectConstants.DEFAULT_JAVA_FILE_EXTENSION);
                    
                    File persistenceClassFile = new File(persistenceClassFilename.toString());
                    
                    if(!persistenceClassFile.exists()){
                        if(this.modelInfo.generatePersistence() != null && this.modelInfo.generatePersistence()){
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID, packagePrefix);
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID, packageSuffix);
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_NAME_ATTRIBUTE_ID, packageName);
                            ExpressionProcessorUtil.setVariable(Constants.NAME_ATTRIBUTE_ID, name);
                            
                            GenericProcessor processor = processorFactory.getProcessor(this.modelInfo, persistenceClassTemplateArtifactNode);
                            String persistenceClassContent = StringUtil.indent(processor.process(), JavaIndent.getRules());
                            
                            if(persistenceClassContent != null && persistenceClassContent.length() > 0)
                                FileUtil.toTextFile(persistenceClassFilename.toString(), persistenceClassContent, encoding);
                        }
                    }
                    else{
                        if(this.modelInfo.generatePersistence() == null || !this.modelInfo.generatePersistence())
                            persistenceClassFile.delete();
                    }
                }
            }
        }
        catch(IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Generates the persistence interface.
     *
     * @param persistenceInterfaceTemplateFilename String that contains the persistence interface template filename.
     * @throws InternalErrorException Occurs when was not possible to generate
     * the interface.
     */
    @Tag(ProjectConstants.DEFAULT_PERSISTENCE_INTERFACE_TEMPLATE_FILE_ID)
    public void generatePersistenceInterface(String persistenceInterfaceTemplateFilename) throws InternalErrorException{
        try{
            File persistenceInterfaceTemplateFile = new File(persistenceInterfaceTemplateFilename);
            XmlReader persistenceInterfaceTemplateReader = new XmlReader(persistenceInterfaceTemplateFile);
            String encoding = persistenceInterfaceTemplateReader.getEncoding();
            XmlNode persistenceInterfaceTemplateNode = persistenceInterfaceTemplateReader.getRoot();
            List<XmlNode> persistenceInterfaceTemplateArtifactsNode = persistenceInterfaceTemplateNode.getChildren();
            
            if(persistenceInterfaceTemplateArtifactsNode != null && !persistenceInterfaceTemplateArtifactsNode.isEmpty()){
                ProcessorFactory processorFactory = ProcessorFactory.getInstance();
                ExpressionProcessor expressionProcessor = new ExpressionProcessor(this.modelInfo);
                
                for(XmlNode persistenceInterfaceTemplateArtifactNode: persistenceInterfaceTemplateArtifactsNode){
                    String outputDir = expressionProcessor.evaluate(persistenceInterfaceTemplateArtifactNode.getAttribute(Constants.OUTPUT_DIRECTORY_ATTRIBUTE_ID));
                    String packagePrefix = expressionProcessor.evaluate(persistenceInterfaceTemplateArtifactNode.getAttribute(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID));
                    String packageName = expressionProcessor.evaluate(persistenceInterfaceTemplateArtifactNode.getAttribute(Constants.PACKAGE_NAME_ATTRIBUTE_ID));
                    String packageSuffix = expressionProcessor.evaluate(persistenceInterfaceTemplateArtifactNode.getAttribute(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID));
                    String name = expressionProcessor.evaluate(persistenceInterfaceTemplateArtifactNode.getAttribute(Constants.NAME_ATTRIBUTE_ID));
                    StringBuilder packageNameBuffer = new StringBuilder();
                    
                    if(packagePrefix != null && packagePrefix.length() > 0)
                        packageNameBuffer.append(packagePrefix);
                    
                    if(packageName != null && packageName.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageName);
                    }
                    
                    if(packageSuffix != null && packageSuffix.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageSuffix);
                    }
                    
                    packageName = packageNameBuffer.toString();
                    
                    StringBuilder persistenceInterfaceName = new StringBuilder();
                    
                    if(packageName != null && packageName.length() > 0){
                        persistenceInterfaceName.append(packageName);
                        persistenceInterfaceName.append(".");
                    }
                    
                    persistenceInterfaceName.append(name);
                    
                    StringBuilder persistenceInterfaceFilename = new StringBuilder();
                    
                    if(outputDir != null && outputDir.length() > 0){
                        persistenceInterfaceFilename.append(outputDir);
                        persistenceInterfaceFilename.append(FileUtil.getDirectorySeparator());
                    }
                    else{
                        persistenceInterfaceFilename.append(this.build.getBaseDirname());
                        persistenceInterfaceFilename.append(FileUtil.getDirectorySeparator());
                        persistenceInterfaceFilename.append(ProjectConstants.DEFAULT_JAVA_DIR);
                    }
                    
                    persistenceInterfaceFilename.append(StringUtil.replaceAll(persistenceInterfaceName.toString(), ".", FileUtil.getDirectorySeparator()));
                    persistenceInterfaceFilename.append(ProjectConstants.DEFAULT_JAVA_FILE_EXTENSION);
                    
                    File persistenceInterfaceFile = new File(persistenceInterfaceFilename.toString());
                    
                    if(!persistenceInterfaceFile.exists()){
                        if(this.modelInfo.generatePersistence() != null && this.modelInfo.generatePersistence()){
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID, packagePrefix);
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID, packageSuffix);
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_NAME_ATTRIBUTE_ID, packageName);
                            ExpressionProcessorUtil.setVariable(Constants.NAME_ATTRIBUTE_ID, name);
                            
                            GenericProcessor processor = processorFactory.getProcessor(this.modelInfo, persistenceInterfaceTemplateArtifactNode);
                            String persistenceInterfaceContent = StringUtil.indent(processor.process(), JavaIndent.getRules());
                            
                            if(persistenceInterfaceContent != null && persistenceInterfaceContent.length() > 0)
                                FileUtil.toTextFile(persistenceInterfaceFilename.toString(), persistenceInterfaceContent, encoding);
                        }
                    }
                    else{
                        if(this.modelInfo.generatePersistence() == null || !this.modelInfo.generatePersistence())
                            persistenceInterfaceFile.delete();
                    }
                }
            }
        }
        catch(IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Generates the persistence mapping.
     *
     * @param persistenceMappingTemplateFilename String that contains the persistence mapping template filename.
     * @throws InternalErrorException Occurs when was not possible to generate
     * the mapping.
     */
    @Tag(ProjectConstants.DEFAULT_PERSISTENCE_MAPPING_TEMPLATE_FILE_ID)
    public void generatePersistenceMapping(String persistenceMappingTemplateFilename) throws InternalErrorException{
        try{
            File persistenceMappingTemplateFile = new File(persistenceMappingTemplateFilename);
            XmlReader persistenceMappingTemplateReader = new XmlReader(persistenceMappingTemplateFile);
            XmlNode persistenceMappingTemplateNode = persistenceMappingTemplateReader.getRoot();
            List<XmlNode> persistenceMappingTemplateArtifactsNode = persistenceMappingTemplateNode.getChildren();
            
            if(persistenceMappingTemplateArtifactsNode != null && !persistenceMappingTemplateArtifactsNode.isEmpty()){
                ProcessorFactory processorFactory = ProcessorFactory.getInstance();
                ExpressionProcessor expressionProcessor = new ExpressionProcessor(this.modelInfo);
                
                for(XmlNode persistenceMappingTemplateArtifactNode: persistenceMappingTemplateArtifactsNode){
                    String outputDir = expressionProcessor.evaluate(persistenceMappingTemplateArtifactNode.getAttribute(Constants.OUTPUT_DIRECTORY_ATTRIBUTE_ID));
                    String packagePrefix = expressionProcessor.evaluate(persistenceMappingTemplateArtifactNode.getAttribute(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID));
                    String packageName = expressionProcessor.evaluate(persistenceMappingTemplateArtifactNode.getAttribute(Constants.PACKAGE_NAME_ATTRIBUTE_ID));
                    String packageSuffix = expressionProcessor.evaluate(persistenceMappingTemplateArtifactNode.getAttribute(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID));
                    String name = expressionProcessor.evaluate(persistenceMappingTemplateArtifactNode.getAttribute(Constants.NAME_ATTRIBUTE_ID));
                    StringBuilder packageNameBuffer = new StringBuilder();
                    
                    if(packagePrefix != null && packagePrefix.length() > 0)
                        packageNameBuffer.append(packagePrefix);
                    
                    if(packageName != null && packageName.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageName);
                    }
                    
                    if(packageSuffix != null && packageSuffix.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageSuffix);
                    }
                    
                    packageName = packageNameBuffer.toString();
                    
                    StringBuilder persistenceMappingName = new StringBuilder();
                    
                    if(packageName != null && packageName.length() > 0){
                        persistenceMappingName.append(packageName);
                        persistenceMappingName.append(".");
                    }
                    
                    persistenceMappingName.append(name);
                    
                    StringBuilder persistenceMappingFilename = new StringBuilder();
                    
                    if(outputDir != null && outputDir.length() > 0){
                        persistenceMappingFilename.append(outputDir);
                        persistenceMappingFilename.append(FileUtil.getDirectorySeparator());
                    }
                    else{
                        persistenceMappingFilename.append(this.build.getBaseDirname());
                        persistenceMappingFilename.append(FileUtil.getDirectorySeparator());
                        persistenceMappingFilename.append(ProjectConstants.DEFAULT_RESOURCES_DIR);
                        persistenceMappingFilename.append(PersistenceConstants.DEFAULT_MAPPINGS_DIR);
                    }
                    
                    persistenceMappingFilename.append(persistenceMappingName.toString());
                    persistenceMappingFilename.append(PersistenceConstants.DEFAULT_MAPPING_FILE_EXTENSION);
                    
                    File persistenceMappingFile = new File(persistenceMappingFilename.toString());
                    
                    if(persistenceMappingFile.exists())
                        persistenceMappingFile.delete();
                    
                    if(this.modelInfo.generatePersistence() != null && this.modelInfo.generatePersistence() && this.modelInfo.getMappedRepositoryId() != null && this.modelInfo.getMappedRepositoryId().length() > 0){
                        ExpressionProcessorUtil.setVariable(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID, packagePrefix);
                        ExpressionProcessorUtil.setVariable(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID, packageSuffix);
                        ExpressionProcessorUtil.setVariable(Constants.PACKAGE_NAME_ATTRIBUTE_ID, packageName);
                        ExpressionProcessorUtil.setVariable(Constants.NAME_ATTRIBUTE_ID, name);
                        
                        GenericProcessor processor = processorFactory.getProcessor(this.modelInfo, persistenceMappingTemplateArtifactNode);
                        String persistenceMappingContent = processor.process();
                        
                        if(persistenceMappingContent != null && persistenceMappingContent.length() > 0){
                            DocumentType persistenceMappingDocumentType = new DefaultDocumentType();
                            String persistenceMappingEncoding = persistenceMappingTemplateReader.getEncoding();
                            
                            persistenceMappingDocumentType.setName(ProjectConstants.PERSISTENCE_MAPPING_ATTRIBUTE_ID);
                            persistenceMappingDocumentType.setPublicID(ProjectConstants.DEFAULT_PERSISTENCE_MAPPING_DTD_PUBLIC_ID);
                            persistenceMappingDocumentType.setSystemID(ProjectConstants.DEFAULT_PERSISTENCE_MAPPING_DTD_SYSTEM_ID);
                            
                            XmlWriter persistenceMappingTemplateWriter = new XmlWriter(persistenceMappingFile, persistenceMappingDocumentType, persistenceMappingEncoding);
                            
                            persistenceMappingTemplateWriter.write(persistenceMappingContent);
                        }
                    }
                }
            }
        }
        catch(IOException | DocumentException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Generates the service class.
     *
     * @param serviceClassTemplateFilename String that contains the service class template filename.
     * @throws InternalErrorException Occurs when was not possible to generate
     * the class.
     */
    @SuppressWarnings("unchecked")
    @Tag(ProjectConstants.DEFAULT_SERVICE_CLASS_TEMPLATE_FILE_ID)
    public void generateServiceClass(String serviceClassTemplateFilename) throws InternalErrorException{
        try{
            File serviceClassTemplateFile = new File(serviceClassTemplateFilename);
            XmlReader serviceClassTemplateReader = new XmlReader(serviceClassTemplateFile);
            String encoding = serviceClassTemplateReader.getEncoding();
            XmlNode serviceClassTemplateNode = serviceClassTemplateReader.getRoot();
            List<XmlNode> serviceClassTemplateArtifactsNode = serviceClassTemplateNode.getChildren();
            
            if(serviceClassTemplateArtifactsNode != null && !serviceClassTemplateArtifactsNode.isEmpty()){
                ProcessorFactory processorFactory = ProcessorFactory.getInstance();
                ExpressionProcessor expressionProcessor = new ExpressionProcessor(this.modelInfo);
                
                for(XmlNode serviceClassTemplateArtifactNode: serviceClassTemplateArtifactsNode){
                    String outputDir = expressionProcessor.evaluate(serviceClassTemplateArtifactNode.getAttribute(Constants.OUTPUT_DIRECTORY_ATTRIBUTE_ID));
                    String packagePrefix = expressionProcessor.evaluate(serviceClassTemplateArtifactNode.getAttribute(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID));
                    String packageName = expressionProcessor.evaluate(serviceClassTemplateArtifactNode.getAttribute(Constants.PACKAGE_NAME_ATTRIBUTE_ID));
                    String packageSuffix = expressionProcessor.evaluate(serviceClassTemplateArtifactNode.getAttribute(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID));
                    String name = expressionProcessor.evaluate(serviceClassTemplateArtifactNode.getAttribute(Constants.NAME_ATTRIBUTE_ID));
                    StringBuilder packageNameBuffer = new StringBuilder();
                    
                    if(packagePrefix != null && packagePrefix.length() > 0)
                        packageNameBuffer.append(packagePrefix);
                    
                    if(packageName != null && packageName.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageName);
                    }
                    
                    if(packageSuffix != null && packageSuffix.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageSuffix);
                    }
                    
                    packageName = packageNameBuffer.toString();
                    
                    StringBuilder serviceClassName = new StringBuilder();
                    
                    if(packageName != null && packageName.length() > 0){
                        serviceClassName.append(packageName);
                        serviceClassName.append(".");
                    }
                    
                    serviceClassName.append(name);
                    
                    StringBuilder serviceClassFilename = new StringBuilder();
                    
                    if(outputDir != null && outputDir.length() > 0){
                        serviceClassFilename.append(outputDir);
                        serviceClassFilename.append(FileUtil.getDirectorySeparator());
                    }
                    else{
                        serviceClassFilename.append(this.build.getBaseDirname());
                        serviceClassFilename.append(FileUtil.getDirectorySeparator());
                        serviceClassFilename.append(ProjectConstants.DEFAULT_JAVA_DIR);
                    }
                    
                    serviceClassFilename.append(StringUtil.replaceAll(serviceClassName.toString(), ".", FileUtil.getDirectorySeparator()));
                    serviceClassFilename.append(ProjectConstants.DEFAULT_JAVA_FILE_EXTENSION);
                    
                    File serviceClassFile = new File(serviceClassFilename.toString());
                    
                    if(!serviceClassFile.exists()){
                        if(this.modelInfo.generateService() != null && this.modelInfo.generateService()){
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID, packagePrefix);
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID, packageSuffix);
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_NAME_ATTRIBUTE_ID, packageName);
                            ExpressionProcessorUtil.setVariable(Constants.NAME_ATTRIBUTE_ID, name);
                            
                            GenericProcessor processor = processorFactory.getProcessor(this.modelInfo, serviceClassTemplateArtifactNode);
                            String serviceClassContent = StringUtil.indent(processor.process(), JavaIndent.getRules());
                            
                            if(serviceClassContent != null && serviceClassContent.length() > 0)
                                FileUtil.toTextFile(serviceClassFilename.toString(), serviceClassContent, encoding);
                        }
                    }
                    else{
                        if(this.modelInfo.generateService() == null || !this.modelInfo.generateService()){
                            Class<? extends IService<? extends BaseModel>> serviceClass = null;
                            
                            try{
                                serviceClass = (Class<? extends IService<? extends BaseModel>>) Class.forName(serviceClassName.toString());
                                
                                if(!Modifier.isAbstract(serviceClass.getModifiers()))
                                    serviceClassFile.delete();
                            }
                            catch(ClassNotFoundException e){
                                serviceClassFile.delete();
                            }
                        }
                    }
                }
            }
        }
        catch(IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Generates the service interface.
     *
     * @param serviceInterfaceTemplateFilename String that contains the service interface template filename.
     * @throws InternalErrorException Occurs when was not possible to generate
     * the interface.
     */
    @Tag(ProjectConstants.DEFAULT_SERVICE_INTERFACE_TEMPLATE_FILE_ID)
    public void generateServiceInterface(String serviceInterfaceTemplateFilename) throws InternalErrorException{
        try{
            File serviceInterfaceTemplateFile = new File(serviceInterfaceTemplateFilename);
            XmlReader serviceInterfaceTemplateReader = new XmlReader(serviceInterfaceTemplateFile);
            String encoding = serviceInterfaceTemplateReader.getEncoding();
            XmlNode serviceInterfaceTemplateNode = serviceInterfaceTemplateReader.getRoot();
            List<XmlNode> serviceInterfaceTemplateArtifactsNode = serviceInterfaceTemplateNode.getChildren();
            
            if(serviceInterfaceTemplateArtifactsNode != null && !serviceInterfaceTemplateArtifactsNode.isEmpty()){
                ProcessorFactory processorFactory = ProcessorFactory.getInstance();
                ExpressionProcessor expressionProcessor = new ExpressionProcessor(this.modelInfo);
                
                for(XmlNode serviceInterfaceTemplateArtifactNode: serviceInterfaceTemplateArtifactsNode){
                    String outputDir = expressionProcessor.evaluate(serviceInterfaceTemplateArtifactNode.getAttribute(Constants.OUTPUT_DIRECTORY_ATTRIBUTE_ID));
                    String packagePrefix = expressionProcessor.evaluate(serviceInterfaceTemplateArtifactNode.getAttribute(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID));
                    String packageName = expressionProcessor.evaluate(serviceInterfaceTemplateArtifactNode.getAttribute(Constants.PACKAGE_NAME_ATTRIBUTE_ID));
                    String packageSuffix = expressionProcessor.evaluate(serviceInterfaceTemplateArtifactNode.getAttribute(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID));
                    String name = expressionProcessor.evaluate(serviceInterfaceTemplateArtifactNode.getAttribute(Constants.NAME_ATTRIBUTE_ID));
                    StringBuilder packageNameBuffer = new StringBuilder();
                    
                    if(packagePrefix != null && packagePrefix.length() > 0)
                        packageNameBuffer.append(packagePrefix);
                    
                    if(packageName != null && packageName.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageName);
                    }
                    
                    if(packageSuffix != null && packageSuffix.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageSuffix);
                    }
                    
                    packageName = packageNameBuffer.toString();
                    
                    StringBuilder serviceInterfaceName = new StringBuilder();
                    
                    if(packageName != null && packageName.length() > 0){
                        serviceInterfaceName.append(packageName);
                        serviceInterfaceName.append(".");
                    }
                    
                    serviceInterfaceName.append(name);
                    
                    StringBuilder serviceInterfaceFilename = new StringBuilder();
                    
                    if(outputDir != null && outputDir.length() > 0){
                        serviceInterfaceFilename.append(outputDir);
                        serviceInterfaceFilename.append(FileUtil.getDirectorySeparator());
                    }
                    else{
                        serviceInterfaceFilename.append(this.build.getBaseDirname());
                        serviceInterfaceFilename.append(FileUtil.getDirectorySeparator());
                        serviceInterfaceFilename.append(ProjectConstants.DEFAULT_JAVA_DIR);
                    }
                    
                    serviceInterfaceFilename.append(StringUtil.replaceAll(serviceInterfaceName.toString(), ".", FileUtil.getDirectorySeparator()));
                    serviceInterfaceFilename.append(ProjectConstants.DEFAULT_JAVA_FILE_EXTENSION);
                    
                    File serviceInterfaceFile = new File(serviceInterfaceFilename.toString());
                    
                    if(!serviceInterfaceFile.exists()){
                        if(this.modelInfo.generateService() != null && this.modelInfo.generateService()){
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID, packagePrefix);
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID, packageSuffix);
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_NAME_ATTRIBUTE_ID, packageName);
                            ExpressionProcessorUtil.setVariable(Constants.NAME_ATTRIBUTE_ID, name);
                            
                            GenericProcessor processor = processorFactory.getProcessor(this.modelInfo, serviceInterfaceTemplateArtifactNode);
                            String serviceInterfaceContent = StringUtil.indent(processor.process(), JavaIndent.getRules());
                            
                            if(serviceInterfaceContent != null && serviceInterfaceContent.length() > 0)
                                FileUtil.toTextFile(serviceInterfaceFilename.toString(), serviceInterfaceContent, encoding);
                        }
                    }
                    else{
                        if(this.modelInfo.generateService() == null || !this.modelInfo.generateService()){
                            Class<? extends IService<? extends BaseModel>> serviceClass = null;
                            
                            try{
                                serviceClass = ServiceUtil.getServiceClassByModel(this.modelInfo.getClazz());
                                
                                if(!Modifier.isAbstract(serviceClass.getModifiers()))
                                    serviceInterfaceFile.delete();
                            }
                            catch(ClassNotFoundException e){
                                serviceInterfaceFile.delete();
                            }
                        }
                    }
                }
            }
        }
        catch(IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Generates the web service class.
     *
     * @param webServiceClassTemplateFilename String that contains the web service class template filename.
     * @throws InternalErrorException Occurs when was not possible to generate
     * the class.
     */
    @SuppressWarnings("unchecked")
    @Tag(ProjectConstants.DEFAULT_WEB_SERVICE_CLASS_TEMPLATE_FILE_ID)
    public void generateWebServiceClass(String webServiceClassTemplateFilename) throws InternalErrorException{
        try{
            File webServiceClassTemplateFile = new File(webServiceClassTemplateFilename);
            XmlReader webServiceClassTemplateReader = new XmlReader(webServiceClassTemplateFile);
            String encoding = webServiceClassTemplateReader.getEncoding();
            XmlNode webServiceClassTemplateNode = webServiceClassTemplateReader.getRoot();
            List<XmlNode> webServiceClassTemplateArtifactsNode = webServiceClassTemplateNode.getChildren();
            
            if(webServiceClassTemplateArtifactsNode != null && !webServiceClassTemplateArtifactsNode.isEmpty()){
                ProcessorFactory processorFactory = ProcessorFactory.getInstance();
                ExpressionProcessor expressionProcessor = new ExpressionProcessor(this.modelInfo);
                
                for(XmlNode webServiceClassTemplateArtifactNode: webServiceClassTemplateArtifactsNode){
                    String outputDir = expressionProcessor.evaluate(webServiceClassTemplateArtifactNode.getAttribute(Constants.OUTPUT_DIRECTORY_ATTRIBUTE_ID));
                    String packagePrefix = expressionProcessor.evaluate(webServiceClassTemplateArtifactNode.getAttribute(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID));
                    String packageName = expressionProcessor.evaluate(webServiceClassTemplateArtifactNode.getAttribute(Constants.PACKAGE_NAME_ATTRIBUTE_ID));
                    String packageSuffix = expressionProcessor.evaluate(webServiceClassTemplateArtifactNode.getAttribute(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID));
                    String name = expressionProcessor.evaluate(webServiceClassTemplateArtifactNode.getAttribute(Constants.NAME_ATTRIBUTE_ID));
                    StringBuilder packageNameBuffer = new StringBuilder();
                    
                    if(packagePrefix != null && packagePrefix.length() > 0)
                        packageNameBuffer.append(packagePrefix);
                    
                    if(packageName != null && packageName.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageName);
                    }
                    
                    if(packageSuffix != null && packageSuffix.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageSuffix);
                    }
                    
                    packageName = packageNameBuffer.toString();
                    
                    StringBuilder webServiceClassName = new StringBuilder();
                    
                    if(packageName != null && packageName.length() > 0){
                        webServiceClassName.append(packageName);
                        webServiceClassName.append(".");
                    }
                    
                    webServiceClassName.append(name);
                    
                    StringBuilder webServiceClassFilename = new StringBuilder();
                    
                    if(outputDir != null && outputDir.length() > 0){
                        webServiceClassFilename.append(outputDir);
                        webServiceClassFilename.append(FileUtil.getDirectorySeparator());
                    }
                    else{
                        webServiceClassFilename.append(this.build.getBaseDirname());
                        webServiceClassFilename.append(FileUtil.getDirectorySeparator());
                        webServiceClassFilename.append(ProjectConstants.DEFAULT_JAVA_DIR);
                    }
                    
                    webServiceClassFilename.append(StringUtil.replaceAll(webServiceClassName.toString(), ".", FileUtil.getDirectorySeparator()));
                    webServiceClassFilename.append(ProjectConstants.DEFAULT_JAVA_FILE_EXTENSION);
                    
                    File webServiceClassFile = new File(webServiceClassFilename.toString());
                    
                    if(!webServiceClassFile.exists()){
                        if(this.modelInfo.generateWebService() != null && this.modelInfo.generateWebService()){
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID, packagePrefix);
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID, packageSuffix);
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_NAME_ATTRIBUTE_ID, packageName);
                            ExpressionProcessorUtil.setVariable(Constants.NAME_ATTRIBUTE_ID, name);
                            
                            GenericProcessor processor = processorFactory.getProcessor(this.modelInfo, webServiceClassTemplateArtifactNode);
                            String webServiceClassContent = StringUtil.indent(processor.process(), JavaIndent.getRules());
                            
                            if(webServiceClassContent != null && webServiceClassContent.length() > 0)
                                FileUtil.toTextFile(webServiceClassFilename.toString(), webServiceClassContent, encoding);
                        }
                    }
                    else{
                        if(this.modelInfo.generateWebService() == null || !this.modelInfo.generateWebService()){
                            Class<? extends IService<? extends BaseModel>> webServiceClass = null;
                            
                            try{
                                webServiceClass = (Class<? extends IService<? extends BaseModel>>) Class.forName(webServiceClassName.toString());
                                
                                if(!Modifier.isAbstract(webServiceClass.getModifiers()))
                                    webServiceClassFile.delete();
                            }
                            catch(ClassNotFoundException e){
                                webServiceClassFile.delete();
                            }
                        }
                    }
                }
            }
        }
        catch(IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Generates the web service interface.
     *
     * @param webServiceInterfaceTemplateFilename String that contains the web service interface filename.
     * @throws InternalErrorException Occurs when was not possible to generate
     * the interface.
     */
    @Tag(ProjectConstants.DEFAULT_WEB_SERVICE_INTERFACE_TEMPLATE_FILE_ID)
    public void generateWebServiceInterface(String webServiceInterfaceTemplateFilename) throws InternalErrorException{
        try{
            File webServiceInterfaceTemplateFile = new File(webServiceInterfaceTemplateFilename);
            XmlReader webServiceInterfaceTemplateReader = new XmlReader(webServiceInterfaceTemplateFile);
            String encoding = webServiceInterfaceTemplateReader.getEncoding();
            XmlNode webServiceInterfaceTemplateNode = webServiceInterfaceTemplateReader.getRoot();
            List<XmlNode> webServiceInterfaceTemplateArtifactsNode = webServiceInterfaceTemplateNode.getChildren();
            
            if(webServiceInterfaceTemplateArtifactsNode != null && !webServiceInterfaceTemplateArtifactsNode.isEmpty()){
                ProcessorFactory processorFactory = ProcessorFactory.getInstance();
                ExpressionProcessor expressionProcessor = new ExpressionProcessor(this.modelInfo);
                
                for(XmlNode webServiceInterfaceTemplateArtifactNode: webServiceInterfaceTemplateArtifactsNode){
                    String outputDir = expressionProcessor.evaluate(webServiceInterfaceTemplateArtifactNode.getAttribute(Constants.OUTPUT_DIRECTORY_ATTRIBUTE_ID));
                    String packagePrefix = expressionProcessor.evaluate(webServiceInterfaceTemplateArtifactNode.getAttribute(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID));
                    String packageName = expressionProcessor.evaluate(webServiceInterfaceTemplateArtifactNode.getAttribute(Constants.PACKAGE_NAME_ATTRIBUTE_ID));
                    String packageSuffix = expressionProcessor.evaluate(webServiceInterfaceTemplateArtifactNode.getAttribute(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID));
                    String name = expressionProcessor.evaluate(webServiceInterfaceTemplateArtifactNode.getAttribute(Constants.NAME_ATTRIBUTE_ID));
                    StringBuilder packageNameBuffer = new StringBuilder();
                    
                    if(packagePrefix != null && packagePrefix.length() > 0)
                        packageNameBuffer.append(packagePrefix);
                    
                    if(packageName != null && packageName.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageName);
                    }
                    
                    if(packageSuffix != null && packageSuffix.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageSuffix);
                    }
                    
                    packageName = packageNameBuffer.toString();
                    
                    StringBuilder webServiceInterfaceName = new StringBuilder();
                    
                    if(packageName != null && packageName.length() > 0){
                        webServiceInterfaceName.append(packageName);
                        webServiceInterfaceName.append(".");
                    }
                    
                    webServiceInterfaceName.append(name);
                    
                    StringBuilder webServiceInterfaceFilename = new StringBuilder();
                    
                    if(outputDir != null && outputDir.length() > 0){
                        webServiceInterfaceFilename.append(outputDir);
                        webServiceInterfaceFilename.append(FileUtil.getDirectorySeparator());
                    }
                    else{
                        webServiceInterfaceFilename.append(this.build.getBaseDirname());
                        webServiceInterfaceFilename.append(FileUtil.getDirectorySeparator());
                        webServiceInterfaceFilename.append(ProjectConstants.DEFAULT_JAVA_DIR);
                    }
                    
                    webServiceInterfaceFilename.append(StringUtil.replaceAll(webServiceInterfaceName.toString(), ".", FileUtil.getDirectorySeparator()));
                    webServiceInterfaceFilename.append(ProjectConstants.DEFAULT_JAVA_FILE_EXTENSION);
                    
                    File webServiceInterfaceFile = new File(webServiceInterfaceFilename.toString());
                    
                    if(!webServiceInterfaceFile.exists()){
                        if(this.modelInfo.generateWebService() != null && this.modelInfo.generateWebService()){
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID, packagePrefix);
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID, packageSuffix);
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_NAME_ATTRIBUTE_ID, packageName);
                            ExpressionProcessorUtil.setVariable(Constants.NAME_ATTRIBUTE_ID, name);
                            
                            GenericProcessor processor = processorFactory.getProcessor(this.modelInfo, webServiceInterfaceTemplateArtifactNode);
                            String webServiceInterfaceContent = StringUtil.indent(processor.process(), JavaIndent.getRules());
                            
                            if(webServiceInterfaceContent != null && webServiceInterfaceContent.length() > 0)
                                FileUtil.toTextFile(webServiceInterfaceFilename.toString(), webServiceInterfaceContent, encoding);
                        }
                    }
                    else{
                        if(this.modelInfo.generateWebService() == null || !this.modelInfo.generateWebService()){
                            Class<? extends IService<? extends BaseModel>> webServiceClass = null;
                            
                            try{
                                webServiceClass = ServiceUtil.getWebServiceClassByModel(this.modelInfo.getClazz());
                                
                                if(Modifier.isAbstract(webServiceClass.getModifiers()))
                                    webServiceInterfaceFile.delete();
                            }
                            catch(ClassNotFoundException e){
                                webServiceInterfaceFile.delete();
                            }
                        }
                    }
                }
            }
        }
        catch(IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Generates the persistence structure.
     *
     * @param persistenceStructureTemplateFilename String that contains the persistence class template filename.
     * @throws InternalErrorException Occurs when was not possible to generate
     * the class.
     */
    @Tag(ProjectConstants.DEFAULT_PERSISTENCE_STRUCTURE_TEMPLATE_FILE_ID)
    public void generatePersistenceStructure(String persistenceStructureTemplateFilename) throws InternalErrorException{
        try{
            File persistenceStructureTemplateFile = new File(persistenceStructureTemplateFilename);
            XmlReader persistenceStructureTemplateReader = new XmlReader(persistenceStructureTemplateFile);
            String encoding = persistenceStructureTemplateReader.getEncoding();
            XmlNode persistenceStructureTemplateNode = persistenceStructureTemplateReader.getRoot();
            List<XmlNode> persistenceStructureTemplateArtifactsNode = persistenceStructureTemplateNode.getChildren();
            
            if(persistenceStructureTemplateArtifactsNode != null && !persistenceStructureTemplateArtifactsNode.isEmpty()){
                ProcessorFactory processorFactory = ProcessorFactory.getInstance();
                ExpressionProcessor expressionProcessor = new ExpressionProcessor(this.modelInfo);
                
                for(XmlNode persistenceStructureTemplateArtifactNode: persistenceStructureTemplateArtifactsNode){
                    String outputDir = expressionProcessor.evaluate(persistenceStructureTemplateArtifactNode.getAttribute(Constants.OUTPUT_DIRECTORY_ATTRIBUTE_ID));
                    String packagePrefix = expressionProcessor.evaluate(persistenceStructureTemplateArtifactNode.getAttribute(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID));
                    String packageName = expressionProcessor.evaluate(persistenceStructureTemplateArtifactNode.getAttribute(Constants.PACKAGE_NAME_ATTRIBUTE_ID));
                    String packageSuffix = expressionProcessor.evaluate(persistenceStructureTemplateArtifactNode.getAttribute(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID));
                    String name = expressionProcessor.evaluate(persistenceStructureTemplateArtifactNode.getAttribute(Constants.NAME_ATTRIBUTE_ID));
                    StringBuilder packageNameBuffer = new StringBuilder();
                    
                    if(packagePrefix != null && packagePrefix.length() > 0)
                        packageNameBuffer.append(packagePrefix);
                    
                    if(packageName != null && packageName.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageName);
                    }
                    
                    if(packageSuffix != null && packageSuffix.length() > 0){
                        if(packageNameBuffer.length() > 0)
                            packageNameBuffer.append(".");
                        
                        packageNameBuffer.append(packageSuffix);
                    }
                    
                    packageName = packageNameBuffer.toString();
                    
                    StringBuilder persistenceStructureName = new StringBuilder();
                    
                    if(packageName != null && packageName.length() > 0){
                        persistenceStructureName.append(packageName);
                        persistenceStructureName.append(".");
                    }
                    
                    persistenceStructureName.append(name);
                    
                    StringBuilder persistenceStructureFilename = new StringBuilder();
                    
                    if(outputDir != null && outputDir.length() > 0){
                        persistenceStructureFilename.append(outputDir);
                        persistenceStructureFilename.append(FileUtil.getDirectorySeparator());
                    }
                    else{
                        persistenceStructureFilename.append(this.build.getBaseDirname());
                        persistenceStructureFilename.append(FileUtil.getDirectorySeparator());
                        persistenceStructureFilename.append(ProjectConstants.DEFAULT_PERSISTENCE_DIR);
                    }
                    
                    persistenceStructureFilename.append(StringUtil.replaceAll(persistenceStructureName.toString(), ".", FileUtil.getDirectorySeparator()));
                    persistenceStructureFilename.append(PersistenceConstants.DEFAULT_FILE_EXTENSION);
                    
                    File persistenceStructureFile = new File(persistenceStructureFilename.toString());
                    
                    if(!persistenceStructureFile.exists()){
                        if((this.modelInfo.generatePersistence() != null && this.modelInfo.generatePersistence()) || (this.modelInfo.generateService() != null && this.modelInfo.generateService())){
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID, packagePrefix);
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID, packageSuffix);
                            ExpressionProcessorUtil.setVariable(Constants.PACKAGE_NAME_ATTRIBUTE_ID, packageName);
                            ExpressionProcessorUtil.setVariable(Constants.NAME_ATTRIBUTE_ID, name);
                            
                            GenericProcessor processor = processorFactory.getProcessor(this.modelInfo, persistenceStructureTemplateArtifactNode);
                            String persistenceStructureContent = processor.process();
                            
                            if(persistenceStructureContent != null && persistenceStructureContent.length() > 0)
                                FileUtil.toTextFile(persistenceStructureFilename.toString(), persistenceStructureContent, encoding);
                        }
                    }
                    else{
                        if((this.modelInfo.generatePersistence() == null || !this.modelInfo.generatePersistence()) && (this.modelInfo.generateService() == null || !this.modelInfo.generateService()))
                            persistenceStructureFile.delete();
                    }
                }
            }
        }
        catch(IOException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Generates the persistence data.
     *
     * @param persistenceDataTemplateFilename String that contains the persistence data template filename.
     * @throws InternalErrorException Occurs when was not possible to generate
     * the UI page.
     */
    @Tag(ProjectConstants.DEFAULT_PERSISTENCE_DATA_TEMPLATE_FILE_ID)
    public void generatePersistenceData(String persistenceDataTemplateFilename) throws InternalErrorException{
        try{
            File persistenceDataTemplateFile = new File(persistenceDataTemplateFilename);
            XmlReader persistenceDataTemplateReader = new XmlReader(persistenceDataTemplateFile);
            String encoding = persistenceDataTemplateReader.getEncoding();
            XmlNode persistenceDataTemplateNode = persistenceDataTemplateReader.getRoot();
            List<XmlNode> persistenceDataTemplateArtifactsNode = persistenceDataTemplateNode.getChildren();
            
            if(persistenceDataTemplateArtifactsNode != null && !persistenceDataTemplateArtifactsNode.isEmpty()){
                PersistenceResourcesLoader persistenceResourcesLoader = new PersistenceResourcesLoader(this.build.getResourcesDirname());
                PersistenceResources persistenceResources = persistenceResourcesLoader.getDefault();
                
                if(persistenceResources != null){
                    FactoryResources persistenceFactoryResources = persistenceResources.getFactoryResources();
                    
                    if(persistenceFactoryResources != null){
                        String repositoryTypeBuffer = persistenceFactoryResources.getType();
                        
                        if(repositoryTypeBuffer != null && repositoryTypeBuffer.length() > 0){
                            RepositoryType repositoryType = RepositoryType.valueOf(repositoryTypeBuffer.toUpperCase());
                            String openQuote = repositoryType.getOpenQuote();
                            String closeQuote = repositoryType.getCloseQuote();
                            
                            ExpressionProcessorUtil.setVariable(PersistenceConstants.OPEN_QUOTE_ATTRIBUTE_ID, openQuote);
                            ExpressionProcessorUtil.setVariable(PersistenceConstants.CLOSE_QUOTE_ATTRIBUTE_ID, closeQuote);
                        }
                        
                        ProcessorFactory processorFactory = ProcessorFactory.getInstance();
                        ExpressionProcessor expressionProcessor = new ExpressionProcessor(this.modelInfo);
                        
                        ExpressionProcessorUtil.setVariable(PersistenceConstants.RESOURCES_ATTRIBUTE_ID, persistenceResources);
                        
                        for(XmlNode persistenceDataTemplateArtifactNode: persistenceDataTemplateArtifactsNode){
                            String outputDir = expressionProcessor.evaluate(persistenceDataTemplateArtifactNode.getAttribute(Constants.OUTPUT_DIRECTORY_ATTRIBUTE_ID));
                            String packagePrefix = expressionProcessor.evaluate(persistenceDataTemplateArtifactNode.getAttribute(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID));
                            String packageName = expressionProcessor.evaluate(persistenceDataTemplateArtifactNode.getAttribute(Constants.PACKAGE_NAME_ATTRIBUTE_ID));
                            String packageSuffix = expressionProcessor.evaluate(persistenceDataTemplateArtifactNode.getAttribute(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID));
                            String name = expressionProcessor.evaluate(persistenceDataTemplateArtifactNode.getAttribute(Constants.NAME_ATTRIBUTE_ID));
                            StringBuilder packageNameBuffer = new StringBuilder();
                            
                            if(packagePrefix != null && packagePrefix.length() > 0)
                                packageNameBuffer.append(packagePrefix);
                            
                            if(packageName != null && packageName.length() > 0){
                                if(packageNameBuffer.length() > 0)
                                    packageNameBuffer.append(".");
                                
                                packageNameBuffer.append(packageName);
                            }
                            
                            if(packageSuffix != null && packageSuffix.length() > 0){
                                if(packageNameBuffer.length() > 0)
                                    packageNameBuffer.append(".");
                                
                                packageNameBuffer.append(packageSuffix);
                            }
                            
                            packageName = packageNameBuffer.toString();
                            
                            StringBuilder persistenceDataName = new StringBuilder();
                            
                            if(packageName != null && packageName.length() > 0){
                                persistenceDataName.append(packageName);
                                persistenceDataName.append(".");
                            }
                            
                            persistenceDataName.append(name);
                            
                            StringBuilder persistenceDataFilename = new StringBuilder();
                            
                            if(outputDir != null && outputDir.length() > 0){
                                persistenceDataFilename.append(outputDir);
                                persistenceDataFilename.append(FileUtil.getDirectorySeparator());
                            }
                            else{
                                persistenceDataFilename.append(this.build.getBaseDirname());
                                persistenceDataFilename.append(FileUtil.getDirectorySeparator());
                                persistenceDataFilename.append(ProjectConstants.DEFAULT_PERSISTENCE_DIR);
                            }
                            
                            persistenceDataFilename.append(StringUtil.replaceAll(persistenceDataName.toString(), ".", FileUtil.getDirectorySeparator()));
                            persistenceDataFilename.append(PersistenceConstants.DEFAULT_FILE_EXTENSION);
                            
                            File persistenceDataFile = new File(persistenceDataFilename.toString());
                            
                            if(!persistenceDataFile.exists()){
                                if((this.modelInfo.generatePersistence() != null && this.modelInfo.generatePersistence()) || (this.modelInfo.generateService() != null && this.modelInfo.generateService())){
                                    ExpressionProcessorUtil.setVariable(Constants.PACKAGE_PREFIX_ATTRIBUTE_ID, packagePrefix);
                                    ExpressionProcessorUtil.setVariable(Constants.PACKAGE_SUFFIX_ATTRIBUTE_ID, packageSuffix);
                                    ExpressionProcessorUtil.setVariable(Constants.PACKAGE_NAME_ATTRIBUTE_ID, packageName);
                                    ExpressionProcessorUtil.setVariable(Constants.NAME_ATTRIBUTE_ID, name);
                                    
                                    GenericProcessor processor = processorFactory.getProcessor(this.modelInfo, persistenceDataTemplateArtifactNode);
                                    String persistenceDataContent = processor.process();
                                    
                                    if(persistenceDataContent != null && persistenceDataContent.length() > 0){
                                        StringBuilder persistenceDataContentBuffer = new StringBuilder();
                                        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(persistenceDataContent.getBytes())));
                                        String line = null;
                                        String indentation = null;
                                        
                                        while((line = reader.readLine()) != null){
                                            indentation = StringUtil.replicate(Constants.DEFAULT_INDENT_CHARACTER, Constants.DEFAULT_INDENT_SIZE);
                                            
                                            if(indentation != null && indentation.length() > 0)
                                                persistenceDataContentBuffer.append(indentation);
                                            
                                            persistenceDataContentBuffer.append(StringUtil.trim(line));
                                            persistenceDataContentBuffer.append(StringUtil.getLineBreak());
                                        }
                                        
                                        persistenceDataContent = persistenceDataContentBuffer.toString();
                                        
                                        FileUtil.toTextFile(persistenceDataFilename.toString(), persistenceDataContent, encoding);
                                    }
                                }
                            }
                            else{
                                if((this.modelInfo.generatePersistence() == null || !this.modelInfo.generatePersistence()) && (this.modelInfo.generateService() == null || !this.modelInfo.generateService()))
                                    persistenceDataFile.delete();
                            }
                        }
                    }
                }
            }
        }
        catch(IOException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Generates the UI page.
     *
     * @param uiPageTemplateFilename String that contains the UI page template filename.
     * @throws InternalErrorException Occurs when was not possible to generate
     * the UI page.
     */
    @Tag(ProjectConstants.DEFAULT_UI_PAGE_TEMPLATE_FILE_ID)
    public void generateUiPage(String uiPageTemplateFilename) throws InternalErrorException{
        try{
            File uiPageTemplateFile = new File(uiPageTemplateFilename);
            XmlReader uiPageTemplateReader = new XmlReader(uiPageTemplateFile);
            String encoding = uiPageTemplateReader.getEncoding();
            XmlNode uiPageTemplateNode = uiPageTemplateReader.getRoot();
            List<XmlNode> uiPageTemplateArtifactsNode = uiPageTemplateNode.getChildren();
            
            if(uiPageTemplateArtifactsNode != null && !uiPageTemplateArtifactsNode.isEmpty()){
                ProcessorFactory processorFactory = ProcessorFactory.getInstance();
                ExpressionProcessor expressionProcessor = new ExpressionProcessor(this.modelInfo);
                
                for(XmlNode uiPageTemplateArtifactNode: uiPageTemplateArtifactsNode){
                    String outputDir = expressionProcessor.evaluate(uiPageTemplateArtifactNode.getAttribute(Constants.OUTPUT_DIRECTORY_ATTRIBUTE_ID));
                    String actionFormUrl = expressionProcessor.evaluate(uiPageTemplateArtifactNode.getAttribute(Constants.NAME_ATTRIBUTE_ID));
                    StringBuilder uiPageDirname = new StringBuilder();
                    StringBuilder uiPageImagesDirname = new StringBuilder();
                    StringBuilder uiPageScriptsDirname = new StringBuilder();
                    StringBuilder uiPageStylesDirname = new StringBuilder();
                    StringBuilder uiPageFilename = new StringBuilder();
                    StringBuilder uiPageScriptFilename = new StringBuilder();
                    StringBuilder uiPageStyleFilename = new StringBuilder();
                    File uiPageDir = null;
                    File uiPageFile = null;
                    File uiPageImagesDir = null;
                    File uiPageScriptsDir = null;
                    File uiPageStylesDir = null;
                    File uiPageScriptFile = null;
                    File uiPageStyleFile = null;
                    
                    if(outputDir != null && outputDir.length() > 0){
                        uiPageDirname.append(outputDir);
                        uiPageDirname.append(FileUtil.getDirectorySeparator());
                    }
                    else{
                        uiPageDirname.append(this.build.getBaseDirname());
                        uiPageDirname.append(FileUtil.getDirectorySeparator());
                        uiPageDirname.append(ProjectConstants.DEFAULT_UI_DIR);
                    }
    
                    uiPageDirname.append(actionFormUrl.substring(1));
                    uiPageDir = new File(uiPageDirname.toString());
                    
                    if(outputDir != null && outputDir.length() > 0){
                        uiPageFilename.append(outputDir);
                        uiPageFilename.append(FileUtil.getDirectorySeparator());
                    }
                    else{
                        uiPageFilename.append(this.build.getBaseDirname());
                        uiPageFilename.append(FileUtil.getDirectorySeparator());
                        uiPageFilename.append(ProjectConstants.DEFAULT_UI_DIR);
                        uiPageFilename.append(UIConstants.DEFAULT_PAGES_DIR);
                        uiPageFilename.append(FileUtil.getDirectorySeparator());
                    }
                    
                    uiPageFilename.append(actionFormUrl.substring(1));
                    uiPageFilename.append(FileUtil.getDirectorySeparator());
                    uiPageFilename.append(UIConstants.DEFAULT_PAGE_FILE_ID);
                    uiPageFile = new File(uiPageFilename.toString());
                    
                    uiPageImagesDirname.append(uiPageDirname.toString());
                    uiPageImagesDirname.append(UIConstants.DEFAULT_IMAGES_RESOURCES_DIR);
                    uiPageImagesDir = new File(uiPageImagesDirname.toString());
                    
                    uiPageScriptsDirname.append(uiPageDirname.toString());
                    uiPageScriptsDirname.append(UIConstants.DEFAULT_SCRIPTS_RESOURCES_DIR);
                    uiPageScriptsDir = new File(uiPageScriptsDirname.toString());
                    
                    uiPageStylesDirname.append(uiPageDirname.toString());
                    uiPageStylesDirname.append(UIConstants.DEFAULT_STYLES_RESOURCES_DIR);
                    uiPageStylesDir = new File(uiPageStylesDirname.toString());
                    
                    uiPageScriptFilename.append(uiPageScriptsDirname);
                    uiPageScriptFilename.append(UIConstants.DEFAULT_PAGE_SCRIPT_RESOURCES_ID);
                    uiPageScriptFile = new File(uiPageScriptFilename.toString());
                    
                    uiPageStyleFilename.append(uiPageStylesDirname);
                    uiPageStyleFilename.append(UIConstants.DEFAULT_PAGE_STYLE_RESOURCES_ID);
                    uiPageStyleFile = new File(uiPageStyleFilename.toString());
                    
                    if(this.modelInfo.generateUi() != null && this.modelInfo.generateUi()){
                        if(!uiPageDir.exists())
                            uiPageDir.mkdirs();
                        
                        if(!uiPageImagesDir.exists())
                            uiPageImagesDir.mkdirs();
                        
                        if(!uiPageScriptsDir.exists())
                            uiPageScriptsDir.mkdirs();
                        
                        if(!uiPageStylesDir.exists())
                            uiPageStylesDir.mkdirs();
                        
                        if(!uiPageScriptFile.exists())
                            FileUtil.toTextFile(uiPageScriptFilename.toString(), "");
                        
                        if(!uiPageStyleFile.exists())
                            FileUtil.toTextFile(uiPageStyleFile.toString(), "");
                        
                        if(!uiPageFile.exists()){
                            GenericProcessor processor = processorFactory.getProcessor(this.modelInfo, uiPageTemplateArtifactNode);
                            String uiPageContent = StringUtil.indent(processor.process(), JspIndent.getRules());
                            
                            if(uiPageContent != null && uiPageContent.length() > 0)
                                FileUtil.toTextFile(uiPageFilename.toString(), uiPageContent, encoding);
                        }
                    }
                    else{
                        if(this.modelInfo.generateUi() == null || !this.modelInfo.generateUi()){
                            if(uiPageFile.exists())
                                uiPageFile.delete();
                            
                            if(uiPageScriptFile.exists())
                                uiPageScriptFile.delete();
                            
                            if(uiPageStyleFile.exists())
                                uiPageStyleFile.delete();
                            
                            if(uiPageImagesDir.exists())
                                uiPageImagesDir.delete();
                            
                            if(uiPageScriptsDir.exists())
                                uiPageScriptsDir.delete();
                            
                            if(uiPageStylesDir.exists())
                                uiPageStylesDir.delete();
                            
                            if(uiPageDir.exists())
                                uiPageDir.delete();
                        }
                    }
                }
            }
        }
        catch(IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e){
            throw new InternalErrorException(e);
        }
    }
    
    /**
     * Generates the module mappings.
     *
     * @param moduleMappingsTemplateFilename String that contains the module mappings template filename.
     * @throws InternalErrorException Occurs when was not possible to generate
     * the module mappings file.
     */
    @Tag(ProjectConstants.DEFAULT_MODULE_MAPPING_TEMPLATE_FILE_ID)
    public void generateModuleMappings(String moduleMappingsTemplateFilename) throws InternalErrorException{
        try{
            File moduleMappingTemplateFile = new File(moduleMappingsTemplateFilename);
            XmlReader moduleMappingTemplateReader = new XmlReader(moduleMappingTemplateFile);
            XmlNode moduleMappingTemplateNode = moduleMappingTemplateReader.getRoot();
            List<XmlNode> moduleMappingTemplateArtifactsNode = moduleMappingTemplateNode.getChildren();
            
            if(moduleMappingTemplateArtifactsNode != null && !moduleMappingTemplateArtifactsNode.isEmpty()){
                ProcessorFactory processorFactory = ProcessorFactory.getInstance();
                ExpressionProcessor expressionProcessor = new ExpressionProcessor(this.modelInfo);
                
                for(XmlNode moduleMappingTemplateArtifactNode: moduleMappingTemplateArtifactsNode){
                    String outputDir = expressionProcessor.evaluate(moduleMappingTemplateArtifactNode.getAttribute(Constants.OUTPUT_DIRECTORY_ATTRIBUTE_ID));
                    String name = expressionProcessor.evaluate(moduleMappingTemplateArtifactNode.getAttribute(Constants.NAME_ATTRIBUTE_ID));
                    StringBuilder moduleMappingFilename = new StringBuilder();
                    
                    if(outputDir != null && outputDir.length() > 0){
                        moduleMappingFilename.append(outputDir);
                        moduleMappingFilename.append(FileUtil.getDirectorySeparator());
                    }
                    else{
                        moduleMappingFilename.append(this.build.getBaseDirname());
                        moduleMappingFilename.append(FileUtil.getDirectorySeparator());
                        moduleMappingFilename.append(ProjectConstants.DEFAULT_UI_DIR);
                        moduleMappingFilename.append(SystemConstants.DEFAULT_DESCRIPTORS_DIR);
                        moduleMappingFilename.append(FileUtil.getDirectorySeparator());
                    }
                    
                    moduleMappingFilename.append(name);
                    moduleMappingFilename.append(XmlConstants.DEFAULT_FILE_EXTENSION);
                    
                    File moduleMappingFile = new File(moduleMappingFilename.toString());
                    
                    if(moduleMappingFile.exists())
                        moduleMappingFile.delete();
                    
                    if((this.modelInfo.generateWebService() != null && this.modelInfo.generateWebService()) || (this.modelInfo.generateUi() != null && this.modelInfo.generateUi())){
                        GenericProcessor processor = processorFactory.getProcessor(this.modelInfo, moduleMappingTemplateArtifactNode);
                        String moduleMappingContent = processor.process();
                        
                        if(moduleMappingContent != null && moduleMappingContent.length() > 0){
                            String moduleMappingEncoding = moduleMappingTemplateReader.getEncoding();
                            XmlWriter moduleMappingTemplateWriter = new XmlWriter(moduleMappingFile, moduleMappingEncoding);
                            
                            moduleMappingTemplateWriter.write(moduleMappingContent);
                        }
                    }
                }
            }
        }
        catch(IOException | DocumentException e){
            throw new InternalErrorException(e);
        }
    }
}