package de.byoc.todo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
public class RootResource {

	@GET
	public Response getRootResource() {
		return Response.ok().build();
	}
	
}
