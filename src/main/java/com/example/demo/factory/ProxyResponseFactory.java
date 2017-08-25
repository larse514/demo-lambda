package com.example.demo.factory;

import com.example.demo.lambda.ErrorPayload;
import com.example.demo.lambda.ProxyResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.util.Map;

public class ProxyResponseFactory {

    private ObjectMapper objectMapper;
    public ProxyResponseFactory(){
        objectMapper = new ObjectMapper();
    }

    /**
     * Method to create response for Lambda method
     * AWS requires the following JSON formate when API Gateway is set to proxy
     * {
             "statusCode": httpStatusCode,
             "headers": { "headerName": "headerValue", ... },
             "body": "..."
        }
     *
     * Note that body is a string value
     * @param body
     * @param headers
     * @param <T>
     * @return
     * @throws JsonProcessingException
     */
    public <T> ProxyResponse createResponse(T body, int status, Map<String, String> headers) {
        ProxyResponse response = new ProxyResponse();

        try {
            String bodyString;
            bodyString = objectMapper.writeValueAsString(body);
            response.setStatusCode(status);
            response.setBody(bodyString);
            response.setHeaders(headers);
            return response;
        }
        catch (JsonProcessingException e){
            response.setBody("InternalError");
            response.setStatusCode(500);
            return response;
        }
    }

    public ProxyResponse createErrorResponse(int status, int errorCode, String message, Map<String, String> headers) {
        return createResponse(new ErrorPayload(errorCode, message), status, headers);
    }
}
