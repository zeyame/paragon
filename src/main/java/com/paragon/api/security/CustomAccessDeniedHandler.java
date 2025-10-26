package com.paragon.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paragon.api.dtos.ErrorDto;
import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.exceptions.PermissionDeniedException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        PermissionDeniedException exception = PermissionDeniedException.accessDenied();

        var errorDto = new ErrorDto(exception.getMessage(), exception.getErrorCode());
        ResponseDto<Void> responseDto = new ResponseDto<>(null, errorDto);

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
    }
}