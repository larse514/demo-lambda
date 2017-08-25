package com.example.demo.lambda;

import java.util.Map;

public class ProxyRequest {
    private Map<String, String> pathParameters;
    private Map<String, String> queryStringParameters;
    private String body;
    private Map<String, String> headers;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getPathParameters() {
        return pathParameters;
    }

    public void setPathParameters(Map<String, String> pathParameters) {
        this.pathParameters = pathParameters;
    }

    public Map<String, String> getQueryStringParameters() {
        return queryStringParameters;
    }

    public void setQueryStringParameters(Map<String, String> queryStringParameters) {
        this.queryStringParameters = queryStringParameters;
    }

    @Override
    public String toString() {
        return "ProxyRequest{" +
                "pathParameters=" + pathParameters +
                ", body='" + body + '\'' +
                ", headers=" + headers +
                ", queryStringParameters=" + queryStringParameters +
                '}';
    }
}
