package com.contrastsecurity.examples.grizzly;

import java.io.IOException;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HttpStatus;

final class App {
  public static void main(final String[] args) throws IOException, InterruptedException {
    String bindAddress = System.getenv("CONTRAST_GRADLE_BIND_ADDR");
    bindAddress = bindAddress == null ? "localhost" : bindAddress;

    final HttpServer server = HttpServer.createSimpleServer(".", bindAddress, 8080);
    final ServerConfiguration configuration = server.getServerConfiguration();
    configuration.addHttpHandler(ECHO_HANDLER, "/echo");

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Stopping server");
                  server.shutdownNow();
                },
                "shutdownHook"));

    server.start();
    System.out.println("Server listening on 8080");
    Thread.currentThread().join();
  }

  static final HttpHandler ECHO_HANDLER =
      new HttpHandler() {
        @Override
        public void service(final Request request, final Response response) throws IOException {
          if (request.getMethod() != Method.GET) {
            response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
            response.setHeader(Header.Allow, Method.GET.getMethodString());
            return;
          }

          final String message = request.getParameter("message");
          if (message == null || message.equals("")) {
            response.setStatus(HttpStatus.BAD_REQUEST_400);
            response.getWriter().write("Parameter 'message' is required.\n");
            return;
          }

          response.setStatus(HttpStatus.OK_200);
          response.setContentType("text/html");
          response.setCharacterEncoding("UTF-8");
          response.getWriter().write("<body><h1>" + message + "</h1></body>");
        }
      };
}
