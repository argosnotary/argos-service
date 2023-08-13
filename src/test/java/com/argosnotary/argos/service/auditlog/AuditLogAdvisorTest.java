/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2023 Gerard Borst <gerard.borst@argosnotary.com>
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AuditLogAdvisorTest {
    private static final String STRING_ARGUMENT_VALUE = "argumentValue";
    private static final String ARGUMENT_NAME = "argumentName";
    private static final String BEAN_NAME = "beanName";
    private static final String METHOD_NAME = "dummyMethod";
    private static final String STRING_RETURN_VALUE = "stringReturnValue";
    private static final String VALUE_STRINGVALUE = "{\"value\":\"stringvalue\"}";
    private static final String FILTERED_VALUE = "{\"value\":\"value\"}";
    private static final String FILTER_BEAN_NAME = "filterBeanName";
    protected static final UUID LABEL_ID = UUID.randomUUID();
    protected static final String PATH = "path";
    protected static final String MRBEAN = "mrbean";
    
    @Mock
    private ObjectMapper objectMapper;

    @Mock(lenient = true)
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature signature;

    private Method method;

    @Mock
    private AuditLog auditLog;

    @Captor
    private ArgumentCaptor<AuditLogData> serializerArgumentCaptor;

    
    private AuditLogAdvisor auditLogAdvisor;

    private static final Object[] ARGUMENT_VALUES = {LABEL_ID, STRING_ARGUMENT_VALUE};
    private static final String[] PARAMETER_NAMES = {"param1", "param2"};
    private static final Class[] PARAMETER_TYPES = {UUID.class, String.class};
    
    @Mock
    private CodeSignature methodSignature;

    @BeforeEach
    void setup() throws NoSuchMethodException, SecurityException {
        auditLogAdvisor = new AuditLogAdvisor(objectMapper); //, nodeRepository, objectMapper);

        method = DummyClass.class.getMethod(METHOD_NAME);
        when(joinPoint.getSignature()).thenReturn(signature);


    }
    
    class DummyClass {
        
	    public void dummyMethod() {
	    	
	    }
    }
    
    class AuditLogDummyClass {
    
	    @AuditLog
    	public void dummyMethod(UUID param1, String param2) {
	    	
	    }
    }
    
    
    @Test
    void auditLogWithStringArgument() throws NoSuchMethodException, SecurityException, JsonProcessingException {
        when(joinPoint.getArgs()).thenReturn(ARGUMENT_VALUES);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(PARAMETER_NAMES);
        when(methodSignature.getName()).thenReturn(METHOD_NAME);
        when(objectMapper.writeValueAsString(STRING_RETURN_VALUE)).thenReturn("\""+STRING_RETURN_VALUE+"\"");
        when(objectMapper.writeValueAsString(LABEL_ID)).thenReturn("\""+LABEL_ID.toString()+"\"");
        when(objectMapper.writeValueAsString(STRING_ARGUMENT_VALUE)).thenReturn("\""+STRING_ARGUMENT_VALUE.toString()+"\"");
        auditLogAdvisor.auditLog(joinPoint, auditLog, STRING_RETURN_VALUE);
        verify(objectMapper).writeValueAsString(serializerArgumentCaptor.capture());
        
        AuditLogData auditLogData = serializerArgumentCaptor.getValue();
        assertThat(auditLogData.getMethodName(), is(METHOD_NAME));
        assertThat(auditLogData.getReturnValue(), is("\""+STRING_RETURN_VALUE+"\""));
        assertThat(auditLogData.getArgumentData().get("param1"), is("\""+LABEL_ID.toString()+"\""));
        assertThat(auditLogData.getArgumentData().get("param2"), is("\""+STRING_ARGUMENT_VALUE.toString()+"\""));
    }
}