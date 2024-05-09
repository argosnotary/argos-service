/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2024 Gerard Borst <gerard.borst@argosnotary.com>
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

import static com.argosnotary.argos.service.openapi.rest.model.RestErrorMessage.TypeEnum.DATA_INPUT;
import static com.argosnotary.argos.service.openapi.rest.model.RestErrorMessage.TypeEnum.OTHER;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.service.openapi.rest.model.RestError;
import com.argosnotary.argos.service.openapi.rest.model.RestErrorMessage;
import com.argosnotary.argos.service.rest.layout.LayoutValidationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RestServiceExceptionHandler {
	private Comparator<RestErrorMessage> comparator = Comparator
            .comparing(RestErrorMessage::getField)
            .thenComparing(RestErrorMessage::getMessage);

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<RestError> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        List<RestErrorMessage> validationMessages = ex.getBindingResult().getAllErrors()
                .stream()
                .filter(FieldError.class::isInstance)
                .map(error -> new RestErrorMessage(DATA_INPUT, error.getDefaultMessage())
                        .field(((FieldError) error).getField())
                )
                .sorted(comparator)
                .toList();

        RestError restValidationError = new RestError(validationMessages);
        return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(restValidationError);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RestError> handleConstraintViolationException(
            ConstraintViolationException ex) {
        List<RestErrorMessage> validationMessages = ex.getConstraintViolations()
                .stream()
                .map(error ->new RestErrorMessage(DATA_INPUT, error.getMessage())
                        .field(error.getPropertyPath().toString())
                )
                .sorted(comparator)
                .toList();
        RestError restValidationError = new RestError(validationMessages);
        return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(restValidationError);
    }

    @ExceptionHandler(value = {JsonMappingException.class})
    public ResponseEntity<RestError> handleJsonMappingException(JsonMappingException ex) {
        return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(createValidationError(String.format("invalid json: %s", ex.getMessage())));
    }

    @ExceptionHandler(value = {LayoutValidationException.class})
    public ResponseEntity<RestError> handleLayoutValidationException(LayoutValidationException ex) {
        return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(createValidationError(ex));
    }


    @ExceptionHandler(value = {ResponseStatusException.class})
    public ResponseEntity<RestError> handleResponseStatusException(ResponseStatusException ex) {
        if (BAD_REQUEST == ex.getStatusCode()) {
            return ResponseEntity.status(ex.getStatusCode()).contentType(APPLICATION_JSON).body(createValidationError(ex.getReason()));
        } else {
            return ResponseEntity.status(ex.getStatusCode()).contentType(APPLICATION_JSON).body(createRestErrorMessage(ex.getReason()));
        }
    }

    @ExceptionHandler(value = {ArgosError.class})
    public ResponseEntity<RestError> handleArgosError(ArgosError ex) {
        if (ex.getLevel() == ArgosError.Level.WARNING) {
            log.debug("{}", ex.getMessage(), ex);
            return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(createValidationError(ex.getMessage()));
        } else {
            log.error("{}", ex.getMessage(), ex);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).contentType(APPLICATION_JSON).body(createRestErrorMessage(ex.getMessage()));
        }
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    public ResponseEntity<RestError> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(FORBIDDEN).contentType(APPLICATION_JSON).body(createRestErrorMessage(ex.getMessage()));
    }

    @ExceptionHandler(value = {NotFoundException.class})
    public ResponseEntity<RestError> handleNotFoundException(NotFoundException ex) {
        return ResponseEntity.status(NOT_FOUND).contentType(APPLICATION_JSON).body(createRestErrorMessage(ex.getMessage()));
    }

    private RestError createValidationError(LayoutValidationException ex) {
        List<RestErrorMessage> validationMessages = new ArrayList<>(ex.getValidationMessages());
        validationMessages.sort(comparator);
        validationMessages.forEach(message -> log.error("Rest Validation Error: [{}]", message));
        return new RestError(validationMessages);
    }

    private RestError createValidationError(String reason) {
        log.error("Rest Validation Error: [{}]", reason);
        return new RestError(singletonList(new RestErrorMessage(DATA_INPUT,reason)));
    }

    private RestError createRestErrorMessage(String message) {
        log.error("Rest Error: [{}]", message);
        return new RestError(singletonList(new RestErrorMessage(OTHER, message)));
    }

}
