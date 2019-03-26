package com.contrastsecurity.examples.grizzly;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.jupiter.api.Test;

final class AppTest {
  @Test
  void it_echoes_a_message() throws Exception {
    // GIVEN a mocked request with a message
    final Request request = mock(Request.class);
    when(request.getMethod()).thenReturn(Method.GET);
    when(request.getParameter("message")).thenReturn("foo");

    // AND a mocked response
    final Response response = mock(Response.class);
    final PrintWriter responseWriter = mock(PrintWriter.class);
    when(response.getWriter()).thenReturn(responseWriter);

    // WHEN the request is handled
    App.ECHO_HANDLER.service(request, response);

    // THEN the message is written to the response
    verify(response).setStatus(HttpStatus.OK_200);
    verify(response).setContentType("text/html");
    verify(response).setCharacterEncoding("UTF-8");
    verify(responseWriter).write("<body><h1>foo</h1></body>");
  }
}
