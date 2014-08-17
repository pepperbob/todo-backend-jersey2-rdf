package de.byoc.todo;

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
import de.byoc.todo.service.TodoItemService;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RootResource {

	private static final Logger logger = LoggerFactory
			.getLogger(RootResource.class);

	private TodoItemService service;

	@Inject
	public RootResource(TodoItemService service) {
		this.service = service;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
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
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public TodoItem createTodoItem(TodoItem item, @Context UriInfo uriInfo) {
		logger.debug("Receiving {}", item);
		return service.createItem(item, uriInfo.getBaseUri().toASCIIString());
	}

	@PATCH
	@Path("{item-id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateTodoItem(@PathParam("item-id") String itemId,
			TodoItem item) {
		logger.debug("Updateing item {}", item);
		service.updateItem(item);
		return Response.ok(item).build();
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteAllTodos() {
		logger.debug("Deleting all Todos!");
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
