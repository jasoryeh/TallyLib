package tk.jasonho.tally.api.interfacing;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MockHttp {

    public static int getRandomPort() {
        return ((int) (Math.random() * 1000)) + 9000;
    }

    private static final int BACKLOG_SYSTEM_DEFAULT = 0;

    @Getter
    private HttpServer server;

    public MockHttp() {
        this.server = createTestServer(getRandomPort());
        attachSampleRoutes(this.server);
        startTestServer(this.server);
    }

    public MockHttp stop() {
        stopTestServer(this.server);
        return this;
    }

    private static final String LOCAL_SERVER = "127.0.0.1";
    private static final String LOCAL_SERVER_PROTO = "http";

    public String urlTo(String path) {
        path = path.startsWith("/") ? path.substring(1) : path;
        String url = LOCAL_SERVER_PROTO + "://" + LOCAL_SERVER + ":" + this.server.getAddress().getPort() + "/" + path;
        System.out.println("\t-> " + url);
        return url;
    }

    public AtomicLong route(String path, HttpHandler handler) {
        AtomicLong routeExecuted = new AtomicLong(0);
        this.server.createContext(path, new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                System.out.println("\t-> " + path + " | " + routeExecuted.getAndIncrement());
                handler.handle(exchange);
                System.out.println("\t<- " + path + " | " + routeExecuted.get());
            }
        });
        return routeExecuted;
    }

    // helpers
    @SneakyThrows
    public static HttpServer createTestServer(int port) {
        return HttpServer.create(new InetSocketAddress(port), BACKLOG_SYSTEM_DEFAULT);
    }

    public static HttpHandler simpleResponse(String method, String responseContent) {
        return new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if (exchange.getRequestMethod() == null || exchange.getRequestMethod().equalsIgnoreCase(method)) {
                    System.out.println("\t\t--> " + method + " " + exchange.getRequestURI());
                    byte[] response = responseContent.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                    exchange.close();
                } else {
                    System.out.println("\t\t--!> " + method + " " + exchange.getRequestURI());
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, -1);
                    exchange.close();
                }
            }
        };
    }

    public static HttpServer attachSampleRoutes(HttpServer server) {
        server.createContext("/get", simpleResponse("GET", "get"));
        server.createContext("/post", simpleResponse("POST", "post"));
        return server;
    }

    public static HttpServer startTestServer(HttpServer server) {
        System.out.println("Starting test server on port " + server.getAddress().getPort());
        server.start();
        System.out.println("\t> START");
        return server;
    }

    public static HttpServer stopTestServer(HttpServer server) {
        System.out.println("\t> STOP");
        server.stop(0);
        System.out.println("...stopped.");
        return server;
    }

}
