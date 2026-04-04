package br.dev.fabiorvs.lti.service;

import br.dev.fabiorvs.lti.config.LtiKeyProperties;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class LtiKeyService {

    private final LtiKeyProperties keyProperties;

    @Getter
    private RSAPrivateKey privateKey;
    @Getter
    private RSAPublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        // 1. Decodifica o Base64 → obtém o conteúdo PEM
        byte[] pemBytes = Base64.getDecoder().decode(keyProperties.getPrivateKey().trim());
        String pem = new String(pemBytes);

        // 2. Remove os headers do PEM e decodifica o Base64 interno
        String base64Key = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(base64Key);

        // 3. Gera a chave privada
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        this.privateKey = (RSAPrivateKey) kf.generatePrivate(spec);

        // 4. Deriva a chave pública
        RSAPrivateCrtKey privCrt = (RSAPrivateCrtKey) this.privateKey;
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
                privCrt.getModulus(),
                privCrt.getPublicExponent()
        );
        this.publicKey = (RSAPublicKey) kf.generatePublic(publicKeySpec);
    }
}