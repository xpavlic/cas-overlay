package org.apereo.cas.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "perun-user")
public class PerunUserProperties {
    private String perunRpcUrl;
    private String perunRpcUsername;
    private String perunRpcPassword;
}
