package br.dev.fabiorvs.lti.controller;

import br.dev.fabiorvs.lti.config.LtiKeyProperties;
import br.dev.fabiorvs.lti.service.LtiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JwksController {

    private final LtiKeyService ltiKeyService;
    private final LtiKeyProperties ltiKeyProperties;

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getKeys() {
        RSAPublicKey publicKey = ltiKeyService.getPublicKey();

        // Converte os componentes da chave para Base64 URL-safe (formato JWK)
        String n = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(publicKey.getModulus().toByteArray());
        String e = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(publicKey.getPublicExponent().toByteArray());

        return Map.of(
                "keys", List.of(Map.of(
                        "kty", "RSA",
                        "kid", ltiKeyProperties.getKeyId(),
                        "use", "sig",
                        "alg", "RS256",
                        "n",   n,
                        "e",   e
                ))
        );
    }
}