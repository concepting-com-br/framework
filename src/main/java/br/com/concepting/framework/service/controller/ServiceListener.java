package br.com.concepting.framework.service.controller;

import br.com.concepting.framework.constants.Constants;
import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.BaseModel;
import br.com.concepting.framework.model.SystemModuleModel;
import br.com.concepting.framework.model.SystemSessionModel;
import br.com.concepting.framework.model.exceptions.ItemAlreadyExistsException;
import br.com.concepting.framework.resources.SystemResources;
import br.com.concepting.framework.resources.SystemResourcesLoader;
import br.com.concepting.framework.security.model.LoginParameterModel;
import br.com.concepting.framework.security.model.LoginSessionModel;
import br.com.concepting.framework.security.model.UserModel;
import br.com.concepting.framework.security.resources.SecurityResources;
import br.com.concepting.framework.security.resources.SecurityResourcesLoader;
import br.com.concepting.framework.security.service.interfaces.LoginSessionService;
import br.com.concepting.framework.security.util.SecurityUtil;
import br.com.concepting.framework.service.annotations.Service;
import br.com.concepting.framework.service.helpers.ServiceThread;
import br.com.concepting.framework.service.interfaces.IService;
import br.com.concepting.framework.service.util.ServiceUtil;
import br.com.concepting.framework.util.PropertyUtil;
import br.com.concepting.framework.util.helpers.DateTime;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class that defines the service listener.
 *
 * @author fvilarinho
 * @since 3.5.0
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
@WebListener
@SuppressWarnings("unchecked")
public class ServiceListener implements ServletContextListener{
    private static ScheduledExecutorService scheduledExecutor = null;
    private static ExecutorService executor = null;
    
    private LoginSessionModel loginSession = null;
    private ServletContextEvent event = null;
    
    /**
     * Returns the instance of the event.
     *
     * @return Instance that contains the event.
     */
    public ServletContextEvent getEvent(){
        return this.event;
    }
    
    /**
     * Defines the instance of the event.
     *
     * @param event Instance that contains the event.
     */
    public void setEvent(ServletContextEvent event){
        this.event = event;
    }
    
    /**
     * Returns the service implementation of a specific data model.
     *
     * @param <S> Class that defines the service implementation.
     * @param modelClass Class that defines the data model.
     * @return Instance that contains the service implementation of the data model.
     * @throws InternalErrorException Occurs when was not possible to
     * instantiate the service implementation.
     */
    protected <S extends IService<? extends BaseModel>> S getService(Class<? extends BaseModel> modelClass) throws InternalErrorException{
        if(modelClass != null)
            return ServiceUtil.getByModelClass(modelClass, this.loginSession);
        
        return null;
    }
    
    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent event){
        onDestroy(event);
    }
    
    protected <L extends LoginSessionModel, U extends UserModel, LP extends LoginParameterModel> void onDestroy(ServletContextEvent event){
        setEvent(event);
    
        try{
            SecurityResourcesLoader loader = new SecurityResourcesLoader();
            SecurityResources resources = loader.getDefault();
            Class<L> loginSessionClass = (Class<L>)resources.getLoginSessionClass();
            LoginSessionService<L, U, LP> loginSessionService = getService(loginSessionClass);
            
            loginSessionService.logOutAll();
        }
        catch(InternalErrorException e){
            event.getServletContext().log(null, e);
        }
        
        if(scheduledExecutor != null)
            scheduledExecutor.shutdown();
        
        if(executor != null)
            executor.shutdown();
        
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        
        while(drivers.hasMoreElements()){
            Driver driver = drivers.nextElement();
            
            try{
                DriverManager.deregisterDriver(driver);
            }
            catch(SQLException e){
            }
        }
        
        AbandonedConnectionCleanupThread.checkedShutdown();
    }
    
    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent event){
        onInitialize(event);
    }
    
    protected <L extends LoginSessionModel, U extends UserModel, LP extends LoginParameterModel, SM extends SystemModuleModel, SS extends SystemSessionModel> void onInitialize(ServletContextEvent event){
        setEvent(event);

        Runnable serviceListenerRunnable = new Runnable(){
            /**
             * @see java.lang.Runnable#run()
             */
            @SuppressWarnings("unchecked")
            public void run(){
                try{
                    ServiceListener.this.loginSession = SecurityUtil.getLoginSession();
                    ServiceListener.this.loginSession.setId(SecurityUtil.generateToken());
                    ServiceListener.this.loginSession.setStartDateTime(new DateTime());
                    ServiceListener.this.loginSession.setActive(true);
                    
                    U user = ServiceListener.this.loginSession.getUser();
                    Class<U> userClass = (Class<U>)user.getClass();
                    IService<U> userService = getService(userClass);
                    
                    user.setName(SecurityUtil.getSystemUserName());
                    user.setActive(true);
                    
                    Collection<U> users = userService.search(user);
                    
                    if(users != null && !users.isEmpty()){
                        user = users.iterator().next();
                        
                        ServiceListener.this.loginSession.setUser(user);
                        
                        SM systemModule = ServiceListener.this.loginSession.getSystemModule();
                        Class<SM> systemModuleClass = (Class<SM>) systemModule.getClass();
                        IService<SM> systemModuleService = getService(systemModuleClass);
                        
                        systemModule.setUrl(event.getServletContext().getContextPath());
                        
                        Collection<SM> systemModules = systemModuleService.search(systemModule);
                        
                        if(systemModules != null && !systemModules.isEmpty()){
                            systemModule = systemModules.iterator().next();
                            
                            ServiceListener.this.loginSession.setSystemModule(systemModule);
                            
                            InetAddress localhost = InetAddress.getLocalHost();
                            SS systemSession = ServiceListener.this.loginSession.getSystemSession();
                            Class<SS> systemSessionClass = (Class<SS>) systemSession.getClass();
                            IService<SS> systemSessionService = getService(systemSessionClass);
                            
                            systemSession.setId(ServiceListener.this.loginSession.getId());
                            systemSession.setStartDateTime(ServiceListener.this.loginSession.getStartDateTime());
                            systemSession.setIp(localhost.getHostAddress());
                            systemSession.setHostName(localhost.getHostName());
                            
                            try{
                                systemSession = systemSessionService.save(systemSession);
                            }
                            catch(ItemAlreadyExistsException e1){
                            }
                            
                            ServiceListener.this.loginSession.setSystemSession(systemSession);
                            
                            Class<L> loginSessionClass = (Class<L>) ServiceListener.this.loginSession.getClass();
                            LoginSessionService<L, U, LP> loginSessionService = getService(loginSessionClass);
                            
                            try{
                                ServiceListener.this.loginSession = loginSessionService.save((L)ServiceListener.this.loginSession);
                            }
                            catch(ItemAlreadyExistsException e1){
                            }
                            
                            SystemResourcesLoader systemResourcesLoader = new SystemResourcesLoader();
                            SystemResources systemResources = systemResourcesLoader.getDefault();
                            Reflections reflections = new Reflections(systemResources.getPackagesPrefix(), new TypeAnnotationsScanner());
                            Set<Class<?>> servicesClasses = reflections.getTypesAnnotatedWith(Service.class);
                            Collection<Class<? extends IService<? extends BaseModel>>> recurrentServicesClasses = null;
                            
                            for(Class<?> clazz : servicesClasses){
                                Class<? extends IService<? extends BaseModel>> serviceClass = (Class<? extends IService<? extends BaseModel>>)clazz;
                                Service serviceAnnotation = serviceClass.getAnnotation(Service.class);
    
                                if(serviceAnnotation != null && serviceAnnotation.isDaemon()){
                                    if(!serviceAnnotation.isRecurrent()){
                                        if(executor == null)
                                            executor = Executors.newWorkStealingPool();
    
                                        executor.submit(new ServiceThread(serviceClass, ServiceListener.this.loginSession));
                                    }
                                    else{
                                        if(recurrentServicesClasses == null)
                                            recurrentServicesClasses = PropertyUtil.instantiate(Constants.DEFAULT_LIST_CLASS);
    
                                        recurrentServicesClasses.add(serviceClass);
                                    }
                                }
                            }
    
                            if(recurrentServicesClasses != null && !recurrentServicesClasses.isEmpty()){
                                Calendar now = Calendar.getInstance();
                                Integer initialDelay = (60 - now.get(Calendar.SECOND));
    
                                if(initialDelay == 60)
                                    initialDelay = 0;
    
                                if(scheduledExecutor == null)
                                    scheduledExecutor = Executors.newScheduledThreadPool(servicesClasses.size());
                            
                                for(Class<? extends IService<? extends BaseModel>> serviceClass : recurrentServicesClasses)
                                    scheduledExecutor.scheduleAtFixedRate(new ServiceThread(serviceClass, ServiceListener.this.loginSession), initialDelay, 60, TimeUnit.SECONDS);
                            }
                        }
                    }
                }
                catch(InternalErrorException | UnknownHostException e){
                    event.getServletContext().log(null, e);
                }
            }
        };
        
        Thread serviceListenerThread = new Thread(serviceListenerRunnable);
        
        serviceListenerThread.start();
    }
}