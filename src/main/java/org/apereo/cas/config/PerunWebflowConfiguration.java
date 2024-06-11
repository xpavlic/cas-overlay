package org.apereo.cas.config;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.controller.DefaultDelegatedAuthenticationNavigationController;
import org.apereo.cas.web.flow.perun.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class PerunWebflowConfiguration implements CasWebflowExecutionPlanConfigurer {

    private final CasConfigurationProperties casProperties;

    private final FlowDefinitionRegistry loginFlowRegistry;

    private final ConfigurableApplicationContext applicationContext;

    private final FlowBuilderServices flowBuilderServices;

    public PerunWebflowConfiguration(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry loginFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices
    ) {
        this.casProperties = casProperties;
        this.loginFlowRegistry = loginFlowRegistry;
        this.applicationContext = applicationContext;
        this.flowBuilderServices = flowBuilderServices;
    }

    @ConditionalOnMissingBean(name = "somethingWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer somethingWebflowConfigurer() {
        return new PerunUserWebflowConfigurer(flowBuilderServices,
                loginFlowRegistry, applicationContext, casProperties);
    }

    @Override
    public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(somethingWebflowConfigurer());
    }

    @ConditionalOnMissingBean(name = PerunWebflowConstants.ACTION_ID_PERUN_USER)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action perunUserAction() {
        return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new PerunUserAction(casProperties))
                .withId(PerunWebflowConstants.ACTION_ID_PERUN_USER)
                .build()
                .get();
    }

    @ConditionalOnMissingBean(name = "perunUserRedirectAction")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action perunUserRedirectAction() {
        return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new PerunUserRedirectAction(casProperties))
                .withId("perunUserRedirectAction")
                .build()
                .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "callbackController")
    public CallbackController callbackController() {
        return new CallbackController(casProperties);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebSecurityConfigurer<HttpSecurity> javaMelodyMonitoringEndpointConfigurer() {
        return new CasWebSecurityConfigurer<>() {

            @Override
            @CanIgnoreReturnValue
            public CasWebSecurityConfigurer<HttpSecurity> configure(final HttpSecurity http) throws Exception {
                http.authorizeHttpRequests(customizer -> customizer.requestMatchers(new AntPathRequestMatcher("/registration/callback")).permitAll());
                return this;
            }
        };
    }
}