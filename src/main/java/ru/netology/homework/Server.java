package ru.netology.homework;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final List<String> validPaths;
    private final ExecutorService threadPool;
    private final Map<String, Map<String, Handler>> handlers = new HashMap<>();

    public Server(List<String> validPaths) {
        this.validPaths = validPaths;
        this.threadPool = Executors.newFixedThreadPool(64);
    }

    public void start(int port) {
        try (var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                threadPool.execute(() -> handleConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new HashMap<>()).put(path, handler);
    }

    private Handler findHandler(String method, String path) {
        return handlers.getOrDefault(method, Map.of()).get(path);
    }

    private void handleConnection(Socket socket) {
        try (
                var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                return;
            }

            var path = parts[1];
            Map<String, String> queryParams = new HashMap<>();

            if (path.contains("?")) {
                String[] splitPath = path.split("\\?", 2);
                path = splitPath[0];
                List<NameValuePair> pairs = URLEncodedUtils.parse(splitPath[1], StandardCharsets.UTF_8);
                for (NameValuePair pair : pairs) {
                    queryParams.put(pair.getName(), pair.getValue());
                }
            }
            Request request = new Request(parts[0], path, queryParams);
            Handler handler = findHandler(request.getMethod(), request.getPath());
            if (handler != null) {
                handler.handle(request, out);
            } else {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
            }
            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);

            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
                return;
            }

            final var length = Files.size(filePath);
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }
}
