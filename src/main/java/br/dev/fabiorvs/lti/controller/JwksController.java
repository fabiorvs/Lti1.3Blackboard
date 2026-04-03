package br.dev.fabiorvs.lti.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class JwksController {

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getKeys() {
        return Map.of(
                "keys", List.of(
                        Map.of(
                                "kty", "RSA",
                                "kid", "minha-chave",
                                "use", "sig",
                                "alg", "RS256",
                                "n", "SEU_MODULO_BASE64",
                                "e", "AQAB"
                        )
                )
        );
    }
}