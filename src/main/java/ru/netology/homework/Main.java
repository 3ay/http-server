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
        server.start(9999);
    }
}
