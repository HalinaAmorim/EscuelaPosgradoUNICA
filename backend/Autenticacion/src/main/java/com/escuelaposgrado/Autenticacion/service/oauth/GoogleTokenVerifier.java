package com.escuelaposgrado.Autenticacion.service.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.escuelaposgrado.Autenticacion.dto.response.GoogleUserInfo;

/**
 * Verifica access_token / id_token de Google y valida audiencia cuando hay clientId.
 */
@Component
public class GoogleTokenVerifier {

    private static final Logger logger = LoggerFactory.getLogger(GoogleTokenVerifier.class);
    private static final String GOOGLE_API_BASE = "https://www.googleapis.com";

    private final WebClient webClient;
    private final String googleClientId;

    public GoogleTokenVerifier(@Value("${app.googleOAuth.clientId:}") String googleClientId) {
        this.googleClientId = googleClientId;
        this.webClient = WebClient.builder().baseUrl(GOOGLE_API_BASE).build();
    }

    public GoogleUserInfo verify(String token) {
        GoogleUserInfo fromAccessToken = tryVerifyAsAccessToken(token);
        if (fromAccessToken != null) {
            return fromAccessToken;
        }
        return tryVerifyAsIdToken(token);
    }

    private GoogleUserInfo tryVerifyAsAccessToken(String token) {
        try {
            GoogleUserInfo userInfo = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/oauth2/v2/userinfo")
                    .queryParam("access_token", token)
                    .build())
                .retrieve()
                .bodyToMono(GoogleUserInfo.class)
                .block();
            if (userInfo != null) {
                logger.debug("Token verificado como access_token: {}", userInfo.getEmail());
            }
            return userInfo;
        } catch (Exception e) {
            logger.debug("Fallo verificación como access_token, intentando como id_token");
            return null;
        }
    }

    private GoogleUserInfo tryVerifyAsIdToken(String token) {
        try {
            GoogleUserInfo userInfo = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/oauth2/v3/tokeninfo")
                    .queryParam("id_token", token)
                    .build())
                .retrieve()
                .bodyToMono(GoogleUserInfo.class)
                .block();
            if (userInfo == null) {
                return null;
            }
            if (!isAudienceValid(userInfo)) {
                logger.warn("id_token rechazado: audiencia no coincide con clientId configurado");
                return null;
            }
            logger.debug("Token verificado como id_token: {}", userInfo.getEmail());
            return userInfo;
        } catch (Exception e) {
            logger.debug("Fallo verificación como id_token: {}", e.getMessage());
            return null;
        }
    }

    private boolean isAudienceValid(GoogleUserInfo userInfo) {
        if (!StringUtils.hasText(googleClientId)) {
            return true;
        }
        String audience = firstNonBlank(userInfo.getAud(), userInfo.getAzp());
        if (!StringUtils.hasText(audience)) {
            return true;
        }
        return googleClientId.equals(audience);
    }

    private static String firstNonBlank(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first;
        }
        return second;
    }
}
