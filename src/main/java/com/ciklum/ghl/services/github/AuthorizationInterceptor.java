package com.ciklum.ghl.services.github;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.List;

/**
 * Passes Bearer token from application.properties into github requests
 * You can get your personal token here: https://github.com/settings/tokens
 */
@Slf4j
@AllArgsConstructor
public class AuthorizationInterceptor implements ClientHttpRequestInterceptor {

    private String token;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (this.token != null && !this.token.isBlank()) {
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", this.token));
        }
        ClientHttpResponse response = execution.execute(request, body);
        List<String> headers = response.getHeaders().get("X-RateLimit-Remaining");
        if (headers != null && headers.size() > 0) {
            log.debug(String.format("Github remaining requests %s", headers.get(0)));
        }
        return response;
    }

}
