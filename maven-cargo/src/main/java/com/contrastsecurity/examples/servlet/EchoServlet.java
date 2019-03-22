package com.contrastsecurity.examples.servlet;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An intentionally insecure {@link HttpServlet} for demonstrating how to integrate a project with
 * the Contrast agent
 */
@WebServlet("/echo")
public final class EchoServlet extends HttpServlet {
  @Override
  public void doGet(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {
    final String message = request.getParameter("message");
    if (message == null || message.equals("")) {
      response.setStatus(400);
      response.getWriter().println("Parameter 'message' is required.");
      return;
    }

    response.setStatus(200);
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().println("<body><h1>" + message + "</h1></body>");
  }
}
