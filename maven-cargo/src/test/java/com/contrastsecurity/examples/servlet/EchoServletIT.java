package com.contrastsecurity.examples.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link EchoServlet} which makes an HTTP request to the web application
 * server running on localhost:8080 and verifies that it echoes the input in its response.
 */
final class EchoServletIT {
  @Test
  void it_echoes_a_message() throws IOException {
    final URLConnection connection =
        new URL("http://localhost:8080/echo?message=foo").openConnection();

    try (final InputStream responseStream = connection.getInputStream();
        final Scanner scanner = new Scanner(responseStream)) {
      assertEquals("<body><h1>foo</h1></body>", scanner.nextLine());
      assertFalse(scanner.hasNext());
    }
  }
}
