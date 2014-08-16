package de.byoc.todo;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TodoApplication extends ResourceConfig {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	public TodoApplication() {
		log.debug("Registering AplicationBinder");
		register(new TodoApplicationBinder());
	}

}
