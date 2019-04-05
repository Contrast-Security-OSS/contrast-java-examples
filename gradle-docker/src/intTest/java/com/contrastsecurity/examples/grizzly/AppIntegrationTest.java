package com.contrastsecurity.examples.grizzly;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

final class AppIntegrationTest {
  @Test
  void it_echoes_a_message_within_ten_tries() throws Exception {
    final URL url = new URL("http://localhost:8080/echo?message=foo");

    for (int i = 0; i < 30; i++) {
      try (final InputStream responseStream = url.openStream();
          final Scanner scanner = new Scanner(responseStream)) {
        assertEquals("<body><h1>foo</h1></body>", scanner.nextLine());
        assertFalse(scanner.hasNext());
        return;
      } catch (final IOException e) {
        mustSleep(Duration.ofSeconds(1));
      }
    }

    fail("Failed to connect to application within 10 tries");
  }

  private static void mustSleep(final Duration duration) {
    try {
      Thread.sleep(duration.toMillis());
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }
}
