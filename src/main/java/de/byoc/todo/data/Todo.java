package de.byoc.todo.data;

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

public final class Todo {

	public static final String NS = "http://todo.byoc.de/";
	
	public static final URI TYPE = ValueFactoryImpl.getInstance().createURI(NS, "#item");
	
	public static final URI ID = ValueFactoryImpl.getInstance().createURI(NS, "id");
	
	public static final URI TITLE = ValueFactoryImpl.getInstance().createURI(NS, "title");
	
	public static final URI ORDER = ValueFactoryImpl.getInstance().createURI(NS, "order");

	public static final URI COMPLETED = ValueFactoryImpl.getInstance().createURI(NS, "completed");
	
	private Todo() {}
}
