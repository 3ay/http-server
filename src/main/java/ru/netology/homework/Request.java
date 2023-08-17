package ru.netology.homework;

import java.io.BufferedOutputStream;
import java.util.Map;


public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> queryParams;

    public Request(String method, String path, Map<String, String> queryParams) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }
}
