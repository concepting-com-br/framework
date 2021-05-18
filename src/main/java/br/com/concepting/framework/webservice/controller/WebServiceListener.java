package br.com.concepting.framework.webservice.controller;

import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.util.ReflectionUtil;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import java.util.Set;

/**
 * Class that defines the web services listener.
 *
 * @author fvilarinho
 * @since 3.8.1
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
@ApplicationPath("/webServices")
public class WebServiceListener extends ResourceConfig{
    /**
     * Default constructor.
     *
     * @throws InternalErrorException Occurs when was not possible to locate the web services.
     */
    public WebServiceListener() throws InternalErrorException{
        packages("br.com.concepting.framework.webservice.helpers", "com.fasterxml.jackson.jaxrs.json", "org.glassfish.jersey.media.multipart");
        
        register(MultiPartFeature.class);
        
        Set<Class<?>> servicesClasses = ReflectionUtil.getTypesAnnotatedWith(Path.class);
        
        if(servicesClasses != null && !servicesClasses.isEmpty())
            for(Class<?> serviceClass : servicesClasses)
                register(serviceClass);
    }
}
