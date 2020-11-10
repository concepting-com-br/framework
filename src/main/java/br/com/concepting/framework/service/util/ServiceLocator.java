package br.com.concepting.framework.service.util;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.ConstructorUtils;

import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.BaseModel;
import br.com.concepting.framework.model.helpers.ModelInfo;
import br.com.concepting.framework.model.util.ModelUtil;
import br.com.concepting.framework.security.model.LoginSessionModel;
import br.com.concepting.framework.service.helpers.ServiceInterceptor;
import br.com.concepting.framework.service.interfaces.IService;
import br.com.concepting.framework.util.Observer;

/**
 * Class responsible to find a service in a application context.
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
public class ServiceLocator{
	private static ServiceLocator instance = null;

	/**
	 * Constructor - Initializes the service locator.
	 */ 
	private ServiceLocator(){
		super();
	}

	/**
	 * @return Instance
	 */
	public static ServiceLocator getInstance(){
		if(instance == null)
			instance = new ServiceLocator();

		return instance;
	}

	public <S extends IService<? extends BaseModel>> S lookupByModelClass(Class<? extends BaseModel> modelClass, LoginSessionModel loginSession) throws InternalErrorException{
		if(modelClass == null)
			return null;

		try{
			S serviceLookupObject = null;
			S serviceInstance = null;
			ModelInfo modelInfo = ModelUtil.getInfo(modelClass);
			Class<S> serviceInterfaceClass = ServiceUtil.getServiceInterfaceClassByModel(modelInfo.getClazz());
			Class<S> serviceClass = ServiceUtil.getServiceClassByModel(modelInfo.getClazz());

			if(serviceInterfaceClass == null || serviceClass == null)
				return null;

			serviceLookupObject = ConstructorUtils.invokeConstructor(serviceClass, null);
			serviceLookupObject.setLoginSession(loginSession);

			serviceInstance = Observer.getInstance(serviceLookupObject, serviceInterfaceClass, new ServiceInterceptor());

			return serviceInstance;
		}
		catch(ClassCastException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException | IllegalArgumentException | NoSuchFieldException e){
			throw new InternalErrorException(e);
		}
	}

	public <S extends IService<? extends BaseModel>> S lookupByServiceClass(Class<S> serviceClass, LoginSessionModel loginSession) throws InternalErrorException{
		if(serviceClass == null)
			return null;

		try{
			Class<? extends BaseModel> modelClass = ServiceUtil.getModelClassByService(serviceClass);
			
			return lookupByModelClass(modelClass, loginSession);
		}
		catch(ClassNotFoundException e){
			throw new InternalErrorException(e);
		}
	}
}