package br.com.concepting.framework.persistence.resources;

import br.com.concepting.framework.constants.Constants;
import br.com.concepting.framework.network.constants.NetworkConstants;
import br.com.concepting.framework.persistence.constants.PersistenceConstants;
import br.com.concepting.framework.persistence.types.RepositoryType;
import br.com.concepting.framework.processors.ExpressionProcessorUtil;
import br.com.concepting.framework.resources.FactoryResources;
import br.com.concepting.framework.resources.XmlResourcesLoader;
import br.com.concepting.framework.resources.constants.FactoryConstants;
import br.com.concepting.framework.resources.constants.ResourcesConstants;
import br.com.concepting.framework.resources.exceptions.InvalidResourcesException;
import br.com.concepting.framework.security.constants.SecurityConstants;
import br.com.concepting.framework.util.NumberUtil;
import br.com.concepting.framework.util.helpers.XmlNode;

import java.text.ParseException;
import java.util.List;

/**
 * Class responsible to manipulate persistence resources.
 *
 * @author fvilarinho
 * @since 1.0.0 This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU
 * General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.
 */
public class PersistenceResourcesLoader extends XmlResourcesLoader<PersistenceResources>{
    /**
     * Constructor - Manipulates the default resource.
     *
     * @throws InvalidResourcesException Occurs when the default resources could
     * not be read.
     */
    public PersistenceResourcesLoader() throws InvalidResourcesException{
        super(PersistenceConstants.DEFAULT_RESOURCES_ID);
    }
    
    /**
     * Constructor - Manipulates specific resources.
     *
     * @param resourcesDirname String that contains the directory where the resources
     * are stored.
     * @throws InvalidResourcesException Occurs when the resources could not be read.
     */
    public PersistenceResourcesLoader(String resourcesDirname) throws InvalidResourcesException{
        super(resourcesDirname, PersistenceConstants.DEFAULT_RESOURCES_ID);
    }
    
    /**
     * @see br.com.concepting.framework.resources.XmlResourcesLoader#parseResources(br.com.concepting.framework.util.helpers.XmlNode)
     */
    public PersistenceResources parseResources(XmlNode resourcesNode) throws InvalidResourcesException{
        String resourcesDirname = getResourcesDirname();
        String resourcesId = getResourcesId();
        PersistenceResources resources = super.parseResources(resourcesNode);
        XmlNode serverNameNode = resourcesNode.getNode(NetworkConstants.SERVER_NAME_ATTRIBUTE_ID);
        String serverName = null;
        
        if(serverNameNode != null){
            serverName = serverNameNode.getValue();
            
            if(serverName == null || serverName.length() == 0)
                throw new InvalidResourcesException(resourcesDirname, resourcesId, serverNameNode.getText());
            
            serverName = ExpressionProcessorUtil.fillEnvironmentInString(serverName);
        }
        else
            serverName = NetworkConstants.DEFAULT_LOCALHOST_NAME_ID;
        
        resources.setServerName(serverName);
        
        String factoryResourcesId = resourcesNode.getAttribute(FactoryConstants.RESOURCES_ATTRIBUTE_ID);
        
        if(factoryResourcesId != null && factoryResourcesId.length() > 0)
            factoryResourcesId = ExpressionProcessorUtil.fillEnvironmentInString(factoryResourcesId);
        
        PersistenceFactoryResourcesLoader loader = new PersistenceFactoryResourcesLoader(resourcesDirname);
        FactoryResources factoryResources = loader.get(factoryResourcesId);
        
        resources.setFactoryResources(factoryResources);
        
        RepositoryType repositoryType = RepositoryType.valueOf(factoryResources.getType().toUpperCase());
        XmlNode serverPortNode = resourcesNode.getNode(NetworkConstants.SERVER_PORT_ATTRIBUTE_ID);
        String serverPort = null;
        
        if(serverPortNode != null){
            serverPort = serverPortNode.getValue();
            
            if(serverPort == null || serverPort.length() == 0)
                throw new InvalidResourcesException(resourcesDirname, resourcesId, serverPortNode.getText());
            
            try{
                serverPort = ExpressionProcessorUtil.fillEnvironmentInString(serverPort);
                
                resources.setServerPort(NumberUtil.parseInt(serverPort));
            }
            catch(ParseException e){
                throw new InvalidResourcesException(resourcesDirname, resourcesId, serverPortNode.getText(), e);
            }
        }
        else
            resources.setServerPort(repositoryType.getDefaultPort());
        
        XmlNode userNameNode = resourcesNode.getNode(SecurityConstants.USER_NAME_ATTRIBUTE_ID);
        
        if(userNameNode != null){
            String userName = userNameNode.getValue();
            
            if(userName != null && userName.length() > 0){
                userName = ExpressionProcessorUtil.fillEnvironmentInString(userName);
                
                resources.setUserName(userName);
            }
        }
        
        XmlNode passwordNode = resourcesNode.getNode(SecurityConstants.PASSWORD_ATTRIBUTE_ID);
        
        if(passwordNode != null){
            String password = passwordNode.getValue();
            
            if(password != null && password.length() > 0){
                password = ExpressionProcessorUtil.fillEnvironmentInString(password);
                
                resources.setPassword(password);
            }
        }
        
        XmlNode instanceNode = resourcesNode.getNode(PersistenceConstants.INSTANCE_ATTRIBUTE_ID);
        
        if(instanceNode != null){
            String instanceId = instanceNode.getValue();
            
            if(instanceId != null && instanceId.length() > 0){
                instanceId = ExpressionProcessorUtil.fillEnvironmentInString(instanceId);
                
                resources.setInstanceId(instanceId);
            }
            else
                throw new InvalidResourcesException(resourcesDirname, resourcesId, instanceNode.getText());
        }
        
        XmlNode repositoryNode = resourcesNode.getNode(PersistenceConstants.REPOSITORY_ATTRIBUTE_ID);
        
        if(repositoryNode != null){
            String repositoryId = repositoryNode.getValue();
            
            if(repositoryId != null && repositoryId.length() > 0){
                repositoryId = ExpressionProcessorUtil.fillEnvironmentInString(repositoryId);
                
                resources.setRepositoryId(repositoryId);
            }
            else
                throw new InvalidResourcesException(resourcesDirname, resourcesId, repositoryNode.getText());
        }
        else
            throw new InvalidResourcesException(resourcesDirname, resourcesId, resourcesNode.getText());
        
        XmlNode optionsNode = resourcesNode.getNode(ResourcesConstants.OPTIONS_ATTRIBUTE_ID);
        String optionId = null;
        String optionValue = null;
        
        if(optionsNode != null){
            List<XmlNode> childNodes = optionsNode.getChildren();
            
            for(XmlNode childNode: childNodes){
                optionId = childNode.getAttribute(Constants.IDENTITY_ATTRIBUTE_ID);
                
                if(optionId == null || optionId.length() == 0)
                    throw new InvalidResourcesException(resourcesDirname, resourcesId, childNode.getText());
                
                optionValue = childNode.getAttribute(Constants.VALUE_ATTRIBUTE_ID);
                
                if(optionValue == null)
                    throw new InvalidResourcesException(resourcesDirname, resourcesId, childNode.getText());
                
                resources.addOption(optionId, optionValue);
            }
        }
        
        return resources;
    }
}