package br.dev.fabiorvs.lti.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "lti.platform")
public class LtiPlatformProperties {
    private String issuer;
    private String clientId;
    private String jwksUrl;
    private String oidcAuthUrl;
    private String accessTokenUrl;
    private String signatureAlg;
}