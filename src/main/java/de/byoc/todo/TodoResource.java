package de.byoc.todo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.byoc.todo.data.TodoItem;

@Path("/todos")
public class TodoResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public TodoItem getAllTodos() {
		TodoItem item = new TodoItem();
		return item;
	}

}
