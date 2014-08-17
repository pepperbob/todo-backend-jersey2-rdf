package de.byoc.todo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

import org.glassfish.jersey.message.MessageBodyWorkers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import de.byoc.todo.data.TodoItem;

/**
 * Naive approach to apply the json-patch.
 * 
 * @author michael
 * 
 */
@Provider
@PATCH
public class PatchReader implements ReaderInterceptor {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private UriInfo info;
	private MessageBodyWorkers workers;

	@Context
	public void setInfo(UriInfo info) {
		this.info = info;
	}

	@Context
	public void setWorkers(MessageBodyWorkers workers) {
		this.workers = workers;
	}

	@Override
	public Object aroundReadFrom(
			ReaderInterceptorContext readerInterceptorContext)
			throws IOException, WebApplicationException {
		try {
			// Get the resource we are being called on,
			// and find the GET method
			Object resource = info.getMatchedResources().get(0);

			Method found = resource.getClass().getMethod("getItem", String.class);
			log.debug("Will call Method {} w/ Paremeter ({}) to receive ServerState",
					found, info.getPath());
			Object bean = found.invoke(resource, info.getPath());

			if (bean == null) {
				throw new NotFoundException();
			}

			log.debug("Found Bean {}", bean);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			MessageBodyWriter<TodoItem> mbw = workers.getMessageBodyWriter(
					TodoItem.class, TodoItem.class, new Annotation[] {},
					MediaType.APPLICATION_JSON_TYPE);

			mbw.writeTo((TodoItem) bean, TodoItem.class, TodoItem.class,
					new Annotation[] {}, MediaType.APPLICATION_JSON_TYPE,
					new MultivaluedHashMap<String, Object>(), baos);

			ObjectMapper mapper = new ObjectMapper();
			LinkedHashMap<String, Object> serverState = mapper.readValue(
					baos.toByteArray(), LinkedHashMap.class);
			LinkedHashMap<String, Object> patchAsNode = mapper.readValue(
					readerInterceptorContext.getInputStream(),
					LinkedHashMap.class);

			log.debug("ServerState: {}", serverState);
			log.debug("PatchState: {}", patchAsNode);
			
			// merge the patch with server state
			for (String k : serverState.keySet()) {
				if (patchAsNode.containsKey(k)) {
					log.debug("PatchState: {} = {}", k, patchAsNode.get(k));
					serverState.put(k, patchAsNode.get(k));
				}
			}

			// write back merged state
			ByteArrayOutputStream resultAsByteArray = new ByteArrayOutputStream();
			mapper.writeValue(resultAsByteArray, serverState);
			readerInterceptorContext.setInputStream(new ByteArrayInputStream(resultAsByteArray.toByteArray()));

			// Pass control back to the Jersey code
			return readerInterceptorContext.proceed();
		} catch (NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e1) {
			throw Throwables.propagate(e1);
		}
	}
}
