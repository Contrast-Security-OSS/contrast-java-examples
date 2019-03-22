package com.contrastsecurity.examples.servlet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

final class EchoServletTest {
  @Test
  void it_echoes_a_message() throws IOException {
    final PrintWriter responseWriter = mock(PrintWriter.class);
    final HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getWriter()).thenReturn(responseWriter);

    final HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter("message")).thenReturn("foo");

    new EchoServlet().doGet(request, response);

    verify(response).setStatus(200);
    verify(response).setContentType("text/html");
    verify(response).setCharacterEncoding("UTF-8");
    verify(responseWriter).println("<body><h1>foo</h1></body>");
  }
}
