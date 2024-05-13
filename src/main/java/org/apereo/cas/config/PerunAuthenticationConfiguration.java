package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationPolicyResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@Configuration(value = "PerunAuthenticationConfiguration", proxyBeanMethods = false)
public class PerunAuthenticationConfiguration {
    @Configuration(value = "PerunAuthenticationExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class PerunAuthenticationExecutionPlanConfiguration {
        @ConditionalOnMissingBean(name = "perunAuthenticationProcessorExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer perunAuthenticationProcessorExecutionPlanConfigurer() {
            return plan -> {
                if (CasRuntimeHintsRegistrar.notInNativeImage()) {
                    plan.registerAuthenticationPostProcessor(new PerunIdentityAuthenticationPostProcessor());
                }
            };
        }
    }
}
