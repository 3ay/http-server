package ru.netology.homework;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
    public static void main(String[] args) {
        final var validPaths = List.of(
                "/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css",
                "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js", "/messages.html"
        );
        Server server = new Server(validPaths);
        server.addHandler("GET", "/messages", (request, outputStream) -> {
            int lastMessagesCount = 10; // значение по умолчанию
            String lastParam = request.getQueryParam("last");
            if (lastParam != null && !lastParam.isEmpty()) {
                try {
                    lastMessagesCount = Integer.parseInt(lastParam);
                } catch (NumberFormatException e) {
                    // обработка ошибки
                }
            }

            // Здесь ваш код для получения последних сообщений и отправки их в outputStream
        });
        server.addHandler("GET", "/showParams", (request, outputStream) -> {
            Map<String, String> queryParams = request.getQueryParams();

            ObjectMapper mapper = new ObjectMapper();
            String json;
            try {
                json = mapper.writeValueAsString(queryParams);
                byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
                outputStream.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: application/json\r\n" +
                                "Content-Length: " + responseBytes.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                outputStream.write(responseBytes);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        server.start(9999);
    }
}
