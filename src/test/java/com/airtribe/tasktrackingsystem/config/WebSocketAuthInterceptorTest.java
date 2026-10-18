package com.airtribe.tasktrackingsystem.config;

import com.airtribe.tasktrackingsystem.service.CustomUserDetailsService;
import com.airtribe.tasktrackingsystem.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthInterceptorTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private MessageChannel channel;

    @InjectMocks
    private WebSocketAuthInterceptor interceptor;

    @Test
    void connectWithValidTokenSetsPrincipalToUsername() {
        UserDetails details = User.withUsername("alice").password("x").authorities(new ArrayList<>()).build();
        when(jwtService.extractUsername("good-token")).thenReturn("alice");
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(details);
        when(jwtService.validateToken("good-token", details)).thenReturn(true);

        Message<?> result = interceptor.preSend(connect("Bearer good-token"), channel);

        Principal principal = StompHeaderAccessor.wrap(result).getUser();
        assertEquals("alice", principal.getName());
    }

    @Test
    void connectWithoutAuthorizationHeaderIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> interceptor.preSend(connect(null), channel));
    }

    @Test
    void connectWithInvalidTokenIsRejected() {
        UserDetails details = User.withUsername("alice").password("x").authorities(new ArrayList<>()).build();
        when(jwtService.extractUsername("bad-token")).thenReturn("alice");
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(details);
        when(jwtService.validateToken("bad-token", details)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> interceptor.preSend(connect("Bearer bad-token"), channel));
    }

    @Test
    void nonConnectFrameIsPassedThroughUnchanged() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setLeaveMutable(true);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, channel);

        assertNull(StompHeaderAccessor.wrap(result).getUser());
    }

    private Message<byte[]> connect(String authorization) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        if (authorization != null) {
            accessor.setNativeHeader("Authorization", authorization);
        }
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
