package de.byoc.todo.api;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.byoc.todo.data.TodoItem;
import de.byoc.todo.jersey.PATCH;
import de.byoc.todo.service.TodoItemService;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TodoItemResource {

	private static final Logger log = LoggerFactory
			.getLogger(TodoItemResource.class);

	private TodoItemService service;

	@Inject
	public TodoItemResource(TodoItemService service) {
		this.service = service;
	}

	@GET
	public List<TodoItem> getRootResource() {
		return service.getAllItems();
	}
	
	@GET
	@Path("{item-id}")
	public TodoItem getItem(@PathParam("item-id") String itemId) {
		TodoItem item = service.getItem(itemId);
		if(item == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		return item;
	}

	@POST
	public TodoItem createTodoItem(TodoItem item, @Context UriInfo uriInfo) {
		log.debug("Receiving {}", item);
		return service.createItem(item, uriInfo.getBaseUri().toASCIIString());
	}

	@PATCH
	@Path("{item-id}")
	public Response updateTodoItem(@PathParam("item-id") String itemId,
			TodoItem item) {
		log.debug("Updateing item {}", item);
		service.updateItem(item);
		return Response.ok(item).build();
	}

	@DELETE
	public Response deleteAllTodos() {
		log.debug("Deleting all Todos!");
		service.deleteAllItems();
		return Response.accepted().build();
	}
	
	@Path("{item-id}")
	@DELETE
	public Response deleteItem(@PathParam("item-id") String itemId) {
		service.deleteItem(itemId);
		return Response.accepted().build();
	}

}
