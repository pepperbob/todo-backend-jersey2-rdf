package de.byoc.todo;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

@Provider
public class CorsFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext request,
			ContainerResponseContext response) throws IOException {
		MultivaluedMap<String, Object> headers = response.getHeaders();

		headers.add("Access-Control-Allow-Origin", "*");
		headers.add("Access-Control-Allow-Methods",
				"POST, GET, OPTIONS, DELETE, PATCH");
		headers.add("Access-Control-Max-Age", "3600");
		headers.add("Access-Control-Allow-Headers",
				"x-requested-with, origin, content-type, accept");

		if ("OPTIONS".equals(request.getMethod())) {
			if (response.getHeaderString("Accept-Patch") == null) {
				response.getHeaders().add("Accept-Patch",
						"application/json-patch+json");
			}
		}
	}
}
