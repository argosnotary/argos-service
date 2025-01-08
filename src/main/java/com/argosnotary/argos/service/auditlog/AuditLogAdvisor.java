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
package com.argosnotary.argos.service.auditlog;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j(topic = "argos.AuditLog")
@Order(value = 2)
public class AuditLogAdvisor {
    public static final String ARGOS_AUDIT_LOG = "argos.AuditLog";    
    
    private final ObjectMapper objectMapper;

    @Pointcut("@annotation(auditLog)")
    public void auditLogPointCut(AuditLog auditLog) {
        //This is an AspectJ pointcut implemented as method
    }

    @AfterReturning(value = "auditLogPointCut(auditLog)", argNames = "joinPoint,auditLog,returnValue", returning = "returnValue")
    public void auditLog(JoinPoint joinPoint, AuditLog auditLog, Object returnValue) throws JsonProcessingException {

        Object[] argumentValues = joinPoint.getArgs();
        String serializedReturnValue = objectMapper.writeValueAsString(returnValue);
        CodeSignature methodSignature = (CodeSignature) joinPoint.getSignature();
        String[] parameterNames = methodSignature.getParameterNames();
        
        Map<String, String> parameterValueMap = new HashMap<>();
        for (int i=0;i < parameterNames.length;i++) {
        	parameterValueMap.put(parameterNames[i], objectMapper.writeValueAsString(argumentValues[i]));
        }
        AuditLogData auditLogData = AuditLogData.builder()
                .argumentData(parameterValueMap)
                .methodName(methodSignature.getName())
                .returnValue(serializedReturnValue)
                .build();
        log.info("AuditLog: {}", objectMapper.writeValueAsString(auditLogData));
    }
}
