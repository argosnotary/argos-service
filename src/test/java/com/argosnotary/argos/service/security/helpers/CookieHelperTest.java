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
package com.argosnotary.argos.service.security.helpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@ExtendWith(MockitoExtension.class)
class CookieHelperTest {

    private final String cookieName = "cookieName";
    private final String cookieValue = "cookieValue";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Captor
    private ArgumentCaptor<Cookie> cookieArgumentCaptor;

    @Test
    void generateCookie() {
        Cookie cookie = CookieHelper.generate(cookieName, cookieValue, Duration.ofMinutes(1));
        assertThat(cookie.getName(), is(cookieName));
        assertThat(cookie.getMaxAge(), is(60));
        assertThat(cookie.getPath(), is("/"));
        assertThat(cookie.getSecure(), is(true));
        assertThat(cookie.isHttpOnly(), is(true));
    }

    @Test
    void retrieve() {
        Cookie cookie = CookieHelper.generate(cookieName, cookieValue, Duration.ofMinutes(1));
    	Cookie[] cookies = new Cookie[]{cookie};
        assertThat(CookieHelper.retrieve(cookies, "other"), is(Optional.empty()));
        String actual = CookieHelper.retrieve(cookies, cookieName).get();
        assertThat(CookieHelper.retrieve(cookies, cookieName).get(), is(cookieValue));
    }
    
    @Test
    void retrieveNullCookies() {
        Cookie[] cookies = new Cookie[]{};
        assertThat(CookieHelper.retrieve(cookies, "other"), is(Optional.empty()));
        cookies = null;
        assertThat(CookieHelper.retrieve(cookies, "other"), is(Optional.empty()));
    }

    @Test
    void generateExpiredCookie() {
        Cookie cookie = CookieHelper.generateExpiredCookie(cookieName);
        assertThat(cookie.getName(), is(cookieName));
        assertThat(cookie.getMaxAge(), is(0));
        assertThat(cookie.getPath(), is("/"));
        assertThat(cookie.getSecure(), is(true));
        
    }
    
    @Test
    void cookieNullParms() {
        
        Throwable exception = assertThrows(java.lang.NullPointerException.class, () -> {
        	CookieHelper.generateExpiredCookie(null); 
          });
        
        assertEquals("name is marked non-null but is null", exception.getMessage());
        
        exception = assertThrows(java.lang.NullPointerException.class, () -> {
        	CookieHelper.retrieve(null, null); 
          });
        
        assertEquals("name is marked non-null but is null", exception.getMessage());
        
        exception = assertThrows(java.lang.NullPointerException.class, () -> {
        	CookieHelper.generate(null, cookieValue, Duration.ofMinutes(1)); 
          });
        
        assertEquals("name is marked non-null but is null", exception.getMessage());
        
        exception = assertThrows(java.lang.NullPointerException.class, () -> {
        	CookieHelper.generate(cookieName, null, Duration.ofMinutes(1)); 
          });
        
        assertEquals("value is marked non-null but is null", exception.getMessage());
        
        exception = assertThrows(java.lang.NullPointerException.class, () -> {
        	CookieHelper.generate(cookieName, cookieValue, null); 
          });
        
        assertEquals("maxAge is marked non-null but is null", exception.getMessage());
        
    }
    
    @Test
    void deleteCookie() {     
    	Cookie cookie = CookieHelper.generate(cookieName, cookieValue, Duration.ofMinutes(1));
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        CookieHelper.deleteCookie(request, response, cookieName);
        assertThat(cookie.getName(), is(cookieName));
        verify(response).addCookie(ArgumentMatchers.any(Cookie.class)); //CookieHelper.generate("name", "-", Duration.ZERO));
    }
    
    @Test
    void deleteNoCookies() { 
        when(request.getCookies()).thenReturn(new Cookie[]{});
        CookieHelper.deleteCookie(request, response, cookieName);
        verify(response, times(0)).addCookie(ArgumentMatchers.any(Cookie.class));
        
        when(request.getCookies()).thenReturn(null);
        CookieHelper.deleteCookie(request, response, cookieName);
        verify(response, times(0)).addCookie(ArgumentMatchers.any(Cookie.class));
        
        Cookie cookie = CookieHelper.generate(cookieName, cookieValue, Duration.ofMinutes(1));
        CookieHelper.deleteCookie(request, response, "otherName");
        verify(response, times(0)).addCookie(ArgumentMatchers.any(Cookie.class));
    }
    
}