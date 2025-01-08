/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2025 Gerard Borst <gerard.borst@argosnotary.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.argosnotary.argos.service.rest;

import static com.argosnotary.argos.service.openapi.rest.model.RestErrorMessage.TypeEnum.MODEL_CONSISTENCY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.service.openapi.rest.model.RestError;
import com.argosnotary.argos.service.openapi.rest.model.RestErrorMessage;
import com.argosnotary.argos.service.openapi.rest.model.RestErrorMessage.TypeEnum;
import com.argosnotary.argos.service.rest.layout.LayoutValidationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.NotFoundException;

@ExtendWith(MockitoExtension.class)
class RestServiceExceptionHandlerTest {

    private RestServiceExceptionHandler handler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private NotFoundException notFoundException;

    @Mock
    private ConstraintViolationException constraintViolationException;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private ConstraintViolation constraintViolation;

    @Mock
    private Path path;

    @Mock
    private FieldError fieldError;

    @Mock
    private ResponseStatusException responseStatusException;

    @Mock
    private AccessDeniedException accessDeniedException;

    @Mock
    private LayoutValidationException layoutValidationException;

    @Mock
    private JsonMappingException jsonMappingException;

    @Mock
    private ArgosError argosError;

    @BeforeEach
    void setUp() {
        handler = new RestServiceExceptionHandler();
    }

    @Test
    void handleMethodArgumentNotValidException() {
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));
        when(fieldError.getField()).thenReturn("field");
        when(fieldError.getDefaultMessage()).thenReturn("message");
        ResponseEntity<RestError> response = handler.handleMethodArgumentNotValidException(methodArgumentNotValidException);
        assertThat(response.getStatusCode().value(), is(400));
        assertThat(response.getBody().getMessages().get(0).getField(), is("field"));
        assertThat(response.getBody().getMessages().get(0).getMessage(), is("message"));
        assertThat(response.getBody().getMessages().get(0).getType(), is(RestErrorMessage.TypeEnum.DATA_INPUT));
    }

    @Test
    void handleLayoutValidationException() {
        when(layoutValidationException.getValidationMessages())
                .thenReturn(new ArrayList<>(List.of(
                		new RestErrorMessage().field("key2").message("message2").type(MODEL_CONSISTENCY),
                		new RestErrorMessage().field("key1").message("message1").type(MODEL_CONSISTENCY))));
        ResponseEntity<RestError> response = handler.handleLayoutValidationException(layoutValidationException);
        assertThat(response.getStatusCode().value(), is(400));
        assertThat(response.getBody().getMessages().get(0).getField(), is("key1"));
        assertThat(response.getBody().getMessages().get(0).getMessage(), is("message1"));
        assertThat(response.getBody().getMessages().get(0).getType(), is(MODEL_CONSISTENCY));
        assertThat(response.getBody().getMessages().get(1).getField(), is("key2"));
        assertThat(response.getBody().getMessages().get(1).getMessage(), is("message2"));
        assertThat(response.getBody().getMessages().get(1).getType(), is(MODEL_CONSISTENCY));
    }

    @Test
    void handleConstraintViolationException() {
        when(constraintViolationException.getConstraintViolations()).thenReturn(Set.of(constraintViolation));
        when(constraintViolation.getPropertyPath()).thenReturn(path);
        when(constraintViolation.getMessage()).thenReturn("message");
        when(path.toString()).thenReturn("field");
        ResponseEntity<RestError> response = handler.handleConstraintViolationException(constraintViolationException);
        assertThat(response.getStatusCode().value(), is(400));
        assertThat(response.getBody().getMessages().get(0).getField(), is("field"));
        assertThat(response.getBody().getMessages().get(0).getMessage(), is("message"));
        assertThat(response.getBody().getMessages().get(0).getType(), is(RestErrorMessage.TypeEnum.DATA_INPUT));
    }


    @Test
    void handleJsonMappingException() {
    	when(jsonMappingException.getMessage()).thenReturn("json exception");
        ResponseEntity<RestError> response = handler.handleJsonMappingException(jsonMappingException);
        assertThat(response.getStatusCode().value(), is(400));
        assertThat(response.getBody().getMessages().get(0).getMessage(), is("invalid json: json exception"));
        assertThat(response.getBody().getMessages().get(0).getType(), is(RestErrorMessage.TypeEnum.DATA_INPUT));
    }

    @Test
    void handleResponseStatusException() {
        when(responseStatusException.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(responseStatusException.getReason()).thenReturn("not found");
        ResponseEntity<RestError> response = (ResponseEntity<RestError>) handler.handleResponseStatusException(responseStatusException);
        assertThat(response.getStatusCode().value(), is(404));
        assertThat(response.getBody().getMessages().get(0).getMessage(), is("not found"));
        assertThat(response.getBody().getMessages().get(0).getType(), is(TypeEnum.OTHER));
    }

    @Test
    void handleResponseStatusExceptionBadRequest() {
        when(responseStatusException.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(responseStatusException.getReason()).thenReturn("bad request");
        ResponseEntity<RestError> response = (ResponseEntity<RestError>) handler.handleResponseStatusException(responseStatusException);
        assertThat(response.getStatusCode().value(), is(400));
        assertThat(response.getBody().getMessages().get(0).getMessage(), is("bad request"));
        assertThat(response.getBody().getMessages().size(), is(1));
        assertThat(response.getBody().getMessages().get(0).getType(), is(TypeEnum.DATA_INPUT));
    }

    @Test
    void handleResponseStatusAccessDenied() {
        when(accessDeniedException.getMessage()).thenReturn("access denied");
        ResponseEntity<RestError> response = (ResponseEntity<RestError>) handler.handleAccessDeniedException(accessDeniedException);
        assertThat(response.getStatusCode().value(), is(403));
        assertThat(response.getBody().getMessages().get(0).getMessage(), is("access denied"));
        assertThat(response.getBody().getMessages().get(0).getType(), is(TypeEnum.OTHER));
        assertThat(response.getBody().getMessages().size(), is(1));
    }

    @Test
    void handleArgosErrorERROR() {
        when(argosError.getLevel()).thenReturn(ArgosError.Level.ERROR);
        when(argosError.getMessage()).thenReturn("message");
        ResponseEntity<RestError> response = (ResponseEntity<RestError>) handler.handleArgosError(argosError);
        assertThat(response.getStatusCode().value(), is(500));
        assertThat(response.getBody().getMessages().get(0).getMessage(), is("message"));
    }

    @Test
    void handleArgosErrorWARNING() {
        when(argosError.getLevel()).thenReturn(ArgosError.Level.WARNING);
        when(argosError.getMessage()).thenReturn("message");
        ResponseEntity<RestError> response = (ResponseEntity<RestError>) handler.handleArgosError(argosError);
        assertThat(response.getStatusCode().value(), is(400));
        assertThat(response.getBody().getMessages().get(0).getMessage(), is("message"));
        assertThat(response.getBody().getMessages().get(0).getType(), is(RestErrorMessage.TypeEnum.DATA_INPUT));
    }

    @Test
    void handleNotFoundException() {
        when(notFoundException.getMessage()).thenReturn("message");
        ResponseEntity<RestError> exception = handler.handleNotFoundException(notFoundException);
        assertThat(exception.getStatusCode().value(), is(HttpStatus.NOT_FOUND.value()));
        assertThat(exception.getBody().getMessages().get(0).getMessage(), is("message"));
    }
}
