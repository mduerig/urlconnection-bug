package michid.urlconnectionbug;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

public class HttpUrlConnectionTest {
    private HttpServer server;

    @BeforeEach
    void setup() throws IOException {
        server = HttpServer.create();
        server.bind(new InetSocketAddress(8080), 0);
        server.createContext("/post", exchange -> {
            String response = "post";
            exchange.getResponseHeaders().add("Location", "http://localhost:8080/redirect");
            exchange.sendResponseHeaders(302, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
        server.createContext("/redirect", exchange -> {
            String response = "redirect";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });

        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void urlConnection() throws IOException {
        for (int k = 0; k < 10000; k++) {
            HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8080/post").openConnection();
            byte[] content = ("mgnlUserId=superuser&mgnlUserPSWD=superuser&csrf=ABCDEFGHABCDEFGHABCDEFGHABCDEFGH").getBytes(StandardCharsets.UTF_8);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString(content.length));
            connection.getOutputStream().write(content);
            connection.connect();
            Assertions.assertEquals(connection.getResponseCode(), 200, "failed after " + k + " iterations.");
        }
    }
}