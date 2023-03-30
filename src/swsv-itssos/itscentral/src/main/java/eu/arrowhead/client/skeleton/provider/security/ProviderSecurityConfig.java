package eu.arrowhead.client.skeleton.provider.security;

import eu.arrowhead.client.library.config.DefaultSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class ProviderSecurityConfig extends DefaultSecurityConfig {
}
