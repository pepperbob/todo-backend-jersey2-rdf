package de.byoc.todo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.ws.rs.GET;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

import org.glassfish.jersey.message.MessageBodyWorkers;

@Provider
@PATCH
public class PatchReader implements ReaderInterceptor {
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
  public Object aroundReadFrom(ReaderInterceptorContext readerInterceptorContext) throws IOException, WebApplicationException {
    // Get the resource we are being called on, 
    // and find the GET method
    Object resource = info.getMatchedResources().get(0);

    Method found = null;
    for (Method next : resource.getClass().getMethods()) {
      if (next.getAnnotation(GET.class) != null) {
        found = next;
        break;
      }
    }


    if (found != null) {

      // Invoke the get method to get the state we are trying to patch
      //
      Object bean;
      try {
        bean = found.invoke(resource);
      } catch (Exception e) {
        throw new WebApplicationException(e);
      }
      
      // Convert this object to a an aray of bytes 
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      MessageBodyWriter<? super Object> bodyWriter =
        workers.getMessageBodyWriter(Object.class, bean.getClass(), 
          new Annotation[0], MediaType.APPLICATION_JSON_TYPE);

      bodyWriter.writeTo(bean, bean.getClass(), bean.getClass(), 
          new Annotation[0], MediaType.APPLICATION_JSON_TYPE,
          new MultivaluedHashMap<String, Object>(), baos);


      // Use the Jackson 2.x classes to convert both the incoming patch  
      // and the current state of the object into a JsonNode / JsonPatch
      ObjectMapper mapper = new ObjectMapper();
      JsonNode serverState = mapper.readValue(baos.toByteArray(), 
        JsonNode.class);
      JsonNode patchAsNode = mapper.readValue(
         readerInterceptorContext.getInputStream(), 
        JsonNode.class);
      JsonPatch patch = JsonPatch.fromJson(patchAsNode);

      try {
        // Apply the patch
        JsonNode result = patch.apply(serverState);

        // Stream the result & modify the stream on the readerInterceptor
        ByteArrayOutputStream resultAsByteArray = 
          new ByteArrayOutputStream();
        mapper.writeValue(resultAsByteArray, result);
        readerInterceptorContext.setInputStream(
          new ByteArrayInputStream(
            resultAsByteArray.toByteArray()));

        // Pass control back to the Jersey code
        return readerInterceptorContext.proceed();


      } catch (JsonPatchException e) {
        throw new WebApplicationException(
          Response.status(500).type("text/plain").entity(e.getMessage()).build());
      }

    } else {
      throw new IllegalArgumentException("No matching GET method on resource");
    }


  }
}
