package com.financemanager.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class SessionTrackingConfig {

    private static final Map<String, HttpSession> sessions = new ConcurrentHashMap<>();

    @Component
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public static class SessionMappingFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            if (request instanceof HttpServletRequest httpRequest) {
                System.out.println("SessionMappingFilter: request URI = " + httpRequest.getRequestURI());
                Cookie[] cookies = httpRequest.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("FMSESSIONID".equals(cookie.getName())) {
                            String sessionId = cookie.getValue();
                            System.out.println("SessionMappingFilter: found cookie FMSESSIONID = " + sessionId);
                            HttpSession session = sessions.get(sessionId);
                            if (session != null) {
                                try {
                                    session.getAttribute("SPRING_SECURITY_CONTEXT"); // test if valid
                                    System.out.println("SessionMappingFilter: mapped to valid session: " + sessionId);
                                    httpRequest = new SessionMappingRequestWrapper(httpRequest, session);
                                    request = httpRequest;
                                } catch (IllegalStateException e) {
                                    System.out.println("SessionMappingFilter: session was invalidated: " + sessionId);
                                    sessions.remove(sessionId);
                                }
                                break;
                            } else {
                                System.out.println("SessionMappingFilter: session not found in map: " + sessionId + ". Current map keys: " + sessions.keySet());
                            }
                        }
                    }
                }

                chain.doFilter(request, response);

                try {
                    HttpSession createdSession = httpRequest.getSession(false);
                    if (createdSession != null) {
                        System.out.println("SessionMappingFilter: request completed. Active session ID = " + createdSession.getId());
                        sessions.put(createdSession.getId(), createdSession);
                    } else {
                        System.out.println("SessionMappingFilter: request completed. No active session.");
                    }
                } catch (IllegalStateException e) {
                    System.out.println("SessionMappingFilter: request completed. Session was invalidated during request.");
                }
            } else {
                chain.doFilter(request, response);
            }
        }
    }

    private static class SessionMappingRequestWrapper extends HttpServletRequestWrapper {
        private final HttpSession session;

        public SessionMappingRequestWrapper(HttpServletRequest request, HttpSession session) {
            super(request);
            this.session = session;
        }

        @Override
        public HttpSession getSession(boolean create) {
            return session;
        }

        @Override
        public HttpSession getSession() {
            return session;
        }
    }
}
