package com.contrastsecurity.examples.servlet;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An intentionally vulnerable {@link HttpServlet} for demonstrating how to integrate a project with
 * the Contrast agent. This code is vulnerable to a Cross-Site Scripting (XSS) attack.
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
    // message is not sanitized before being written to the response and therefore may is an XSS
    // attack vector
    response.getWriter().println("<body><h1>" + message + "</h1></body>");
  }
}
