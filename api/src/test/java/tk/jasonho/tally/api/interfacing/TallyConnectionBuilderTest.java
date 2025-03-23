package tk.jasonho.tally.api.interfacing;

import com.google.gson.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

/**
 * @see TallyConnectionBuilder
 */
class TallyConnectionBuilderTest {

    private MockHttp server;

    @BeforeEach
    void setUp() {
        tk.jasonho.tally.api.util.TallyLogger.verbose = true;
        this.server = new MockHttp();
    }

    @AfterEach
    void tearDown() {
        this.server.stop();
    }

    @Test
    void get() {
        TallyConnectionBuilder builder = new TallyConnectionBuilder(
                this.server.urlTo("/get")
        );
        String read = builder.get().readIn().getRead();
        assertEquals("get", read);
        assertNotEquals("post", read);
    }

    @Test
    void post() {
        TallyConnectionBuilder builder = new TallyConnectionBuilder(
                this.server.urlTo("/post")
        );
        String read = builder.post().readIn().getRead();
        assertNotEquals("get", read);
        assertEquals("post", read);
    }

    @Test
    void header() {
        String testKey = "testKey";
        String testVal = "testVal";
        String testRoute = "/header_test";
        AtomicLong execCount = this.server.route(testRoute, exchange -> {
            assertTrue(exchange.getRequestHeaders().containsKey(testKey));
            assertEquals(testVal, exchange.getRequestHeaders().getFirst(testKey));
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, -1);
            exchange.close();
        });

        new TallyConnectionBuilder(this.server.urlTo(testRoute))
                .header(testKey, testVal)
                .readIn();

        assertTrue(execCount.get() > 0);
    }

    @Test
    void authBearer() {
        String testKey = "testKey";
        String testRoute = "/auth_test";
        AtomicLong execCount = this.server.route(testRoute, exchange -> {
            List<String> authHeaders = exchange.getRequestHeaders().get("Authorization");

            assertTrue(authHeaders.stream()
                    .filter((e) -> e.contains(testKey))
                    .filter((e) -> e.contains("Bearer"))
                    .filter((e) -> e.contains("Bearer " + testKey))
                    .count() > 0);
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, -1);
            exchange.close();
        });

        new TallyConnectionBuilder(this.server.urlTo(testRoute))
                .authBearer(testKey)
                .readIn();

        assertTrue(execCount.get() > 0);
    }

    @Test
    void json() {
        String testRoute = "/json_test";
        AtomicLong execCount = this.server.route(testRoute, exchange -> {
            assertTrue(!exchange.getRequestHeaders().get("Accept").isEmpty());
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, -1);
            exchange.close();
        });

        new TallyConnectionBuilder(this.server.urlTo(testRoute))
                .json()
                .readIn();

        assertTrue(execCount.get() > 0);
    }

    @Test
    void writeJson() {
        String testRoute = "/write_json_test";
        AtomicLong execCount = this.server.route(testRoute, exchange -> {
            StringBuilder stringBuilder = new StringBuilder();
            InputStreamReader inReader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader bReader = new BufferedReader(inReader);
            String line;
            while((line = bReader.readLine()) != null) {
                stringBuilder.append(line.trim());
            }
            String body = stringBuilder.toString();
            assertTrue(!body.isEmpty());
            try {
                JsonElement parse = new JsonParser().parse(body);
            } catch (Exception e) {
                assertTrue(false);
            }

            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, -1);
            exchange.close();
        });

        new TallyConnectionBuilder(this.server.urlTo(testRoute))
                .writeJson(new JsonObject())
                .readIn();

        new TallyConnectionBuilder(this.server.urlTo(testRoute))
                .writeJson(new JsonArray())
                .readIn();

        new TallyConnectionBuilder(this.server.urlTo(testRoute))
                .writeJson(JsonNull.INSTANCE)
                .readIn();

        new TallyConnectionBuilder(this.server.urlTo(testRoute))
                .writeJson(new JsonPrimitive(true))
                .readIn();

        new TallyConnectionBuilder(this.server.urlTo(testRoute))
                .writeJson(new JsonPrimitive(1))
                .readIn();

        new TallyConnectionBuilder(this.server.urlTo(testRoute))
                .writeJson(new JsonPrimitive("test"))
                .readIn();

        assertTrue(execCount.get() > 0);
    }

    @Test
    void writeOut() {
        String testString = "test string goes here";
        String testRoute = "/write_out_test";
        AtomicLong execCount = this.server.route(testRoute, exchange -> {
            StringBuilder stringBuilder = new StringBuilder();
            InputStreamReader inReader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader bReader = new BufferedReader(inReader);
            String line;
            while((line = bReader.readLine()) != null) {
                stringBuilder.append(line.trim());
            }
            String body = stringBuilder.toString();
            assertTrue(!body.isEmpty());
            assertEquals(testString, body);

            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, -1);
            exchange.close();
        });

        new TallyConnectionBuilder(this.server.urlTo(testRoute))
                .writeOut(testString)
                .readIn();

        assertTrue(execCount.get() > 0);
    }

    @Test
    void getReadJsonGood() {
        String testRoute = "/good_get_read_json_test";
        AtomicLong execCount = this.server.route(testRoute, exchange -> {
            String resp = "{}";
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, resp.getBytes().length);
            exchange.getResponseBody().write(resp.getBytes());

            exchange.close();
        });

        try {
            new TallyConnectionBuilder(this.server.urlTo(testRoute))
                    .get()
                    .readIn()
                    .getReadJson();
        } catch(Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }


        assertTrue(execCount.get() > 0);
    }

    @Test
    void getReadJsonBad() {
        String testRoute = "/bad_get_read_json_test";
        AtomicLong execCount = this.server.route(testRoute, exchange -> {
            String resp = "...{]"; // some bad json
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, resp.getBytes().length);
            exchange.getResponseBody().write(resp.getBytes());

            exchange.close();
        });

        try {
            new TallyConnectionBuilder(this.server.urlTo(testRoute))
                    .get()
                    .readIn()
                    .getReadJson();
        } catch(Exception e) {
            e.printStackTrace();
            // pass
        }

        assertTrue(execCount.get() > 0);
    }

    @Test
    void verifyJsonThrowingGood() {
        String testRoute = "/good_verify_json_test";
        AtomicLong execCount = this.server.route(testRoute, exchange -> {
            String resp = "{}"; // some bad json
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, resp.getBytes().length);
            exchange.getResponseBody().write(resp.getBytes());

            exchange.close();
        });

        try {
            new TallyConnectionBuilder(this.server.urlTo(testRoute))
                    .get()
                    .readIn()
                    .verifyJsonThrowing();
        } catch(Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }

        assertTrue(execCount.get() > 0);
    }

    @Test
    void verifyJsonThrowingBad() {
        String testRoute = "/bad_verify_json_test";
        AtomicLong execCount = this.server.route(testRoute, exchange -> {
            String resp = "...{]"; // some bad json
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, resp.getBytes().length);
            exchange.getResponseBody().write(resp.getBytes());

            exchange.close();
        });

        try {
            new TallyConnectionBuilder(this.server.urlTo(testRoute))
                    .get()
                    .readIn()
                    .verifyJsonThrowing();
        } catch(Exception e) {
            // pass
            e.printStackTrace();
        }

        assertTrue(execCount.get() > 0);
    }

    @Test
    void verifyJsonGood() {
        String testRoute = "/good_verify_json_test";
        AtomicLong execCount = this.server.route(testRoute, exchange -> {
            String resp = "{}"; // some bad json
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, resp.getBytes().length);
            exchange.getResponseBody().write(resp.getBytes());

            exchange.close();
        });

        assertTrue(new TallyConnectionBuilder(this.server.urlTo(testRoute))
                .get()
                .readIn()
                .verifyJson());

        assertTrue(execCount.get() > 0);
    }

    @Test
    void verifyJsonBad() {
        String testRoute = "/bad_verify_json_test";
        AtomicLong execCount = this.server.route(testRoute, exchange -> {
            String resp = "...{]"; // some bad json
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, resp.getBytes().length);
            exchange.getResponseBody().write(resp.getBytes());

            exchange.close();
        });

        assertFalse(new TallyConnectionBuilder(this.server.urlTo(testRoute))
                .get()
                .readIn()
                .verifyJson());

        assertTrue(execCount.get() > 0);
    }

    @Test
    void readIn() {
        String testRoute = "/test_readin";
        String testVal = "abc123";
        AtomicLong execCount = this.server.route(testRoute, exchange -> {
            String resp = testVal; // some bad json
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, resp.getBytes().length);
            exchange.getResponseBody().write(resp.getBytes());

            exchange.close();
        });

        assertEquals(testVal, new TallyConnectionBuilder(this.server.urlTo(testRoute))
                .get()
                .readIn()
                .getRead());

        assertTrue(execCount.get() > 0);
    }
}