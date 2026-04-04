package br.dev.fabiorvs.lti.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "lti.keys")
public class LtiKeyProperties {
    private String privateKey;
    private String keyId;
}