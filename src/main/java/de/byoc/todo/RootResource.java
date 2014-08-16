package de.byoc.todo;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.byoc.todo.data.TodoItem;
import de.byoc.todo.service.TodoItemService;

@Path("/")
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

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveTodoItem(TodoItem item) {
		logger.debug("Receiving {}", item);
		item.setId(UUID.randomUUID().toString());
		service.createItem(item);
		return Response.ok(item).build();
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteAllTodos() {
		logger.debug("Deleting all Todos!");
		service.deleteAllItems();
		return Response.status(Response.Status.ACCEPTED).build();
	}

}
