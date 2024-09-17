package org.apereo.cas.web.flow.perun;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

@Slf4j
public class PerunUserWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public PerunUserWebflowConfigurer(FlowBuilderServices flowBuilderServices,
                                      FlowDefinitionRegistry flowDefinitionRegistry,
                                      ConfigurableApplicationContext applicationContext,
                                      CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    protected void createTransitionStateToPerunUser(final Flow flow) {
        val submit = getCreateTicketGrantingTicketState(flow);
        createTransitionForState(submit, CasWebflowConstants.TRANSITION_ID_SUCCESS, PerunWebflowConstants.STATE_ID_PERUN_USER, true);
        val submit2 = getProceedFromAuthenticationWarningView(flow);
        createTransitionForState(submit2, CasWebflowConstants.TRANSITION_ID_PROCEED, PerunWebflowConstants.STATE_ID_PERUN_USER, true);
    }

    protected ActionState getCreateTicketGrantingTicketState(final Flow flow) {
        return getState(flow, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET, ActionState.class);
    }

    protected ViewState getProceedFromAuthenticationWarningView(final Flow flow) {
        return getState(flow, CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS, ViewState.class);
    }

    protected void redirectSubflow(final Flow flow) {
        val redirectFlow = buildFlow(PerunWebflowConstants.FLOW_ID_REGISTRATION_REDIRECT);
        createEndState(redirectFlow, CasWebflowConstants.STATE_ID_SUCCESS);
        mainFlowDefinitionRegistry.registerFlowDefinition(redirectFlow);
        val redirectAction = createActionState(flow, PerunWebflowConstants.STATE_ID_PERUN_USER_REDIRECT, "perunUserRedirectAction");
        createTransitionForState(redirectAction, PerunWebflowConstants.TRANSITION_ID_PERUN_USER_SUCCESS, CasWebflowConstants.STATE_ID_END_WEBFLOW);
    }


    protected void flowWithoutWarning(final Flow flow) {
        val actionState = createActionState(flow, PerunWebflowConstants.STATE_ID_PERUN_USER, PerunWebflowConstants.ACTION_ID_PERUN_USER);
        val transitionSet = actionState.getTransitionSet();
        val target = getCreateTicketGrantingTicketState(flow).getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();
        transitionSet.add(createTransition(PerunWebflowConstants.TRANSITION_ID_PERUN_USER_SUCCESS, target));
        transitionSet.add(createTransition(PerunWebflowConstants.TRANSITION_ID_PERUN_USER_REDIRECT, PerunWebflowConstants.STATE_ID_PERUN_USER_REDIRECT));
    }

    protected void flowWithWarning(final Flow flow) {
        val actionState = createActionState(flow, PerunWebflowConstants.STATE_ID_PERUN_USER_WARNING, PerunWebflowConstants.ACTION_ID_PERUN_USER);
        val transitionSet = actionState.getTransitionSet();
        val target = getProceedFromAuthenticationWarningView(flow).getTransition(CasWebflowConstants.TRANSITION_ID_PROCEED).getTargetStateId();
        transitionSet.add(createTransition(PerunWebflowConstants.TRANSITION_ID_PERUN_USER_SUCCESS, target));
        transitionSet.add(createTransition(PerunWebflowConstants.TRANSITION_ID_PERUN_USER_REDIRECT, PerunWebflowConstants.STATE_ID_PERUN_USER_REDIRECT));
    }

    @Override
    protected void doInitialize() {
        LOGGER.debug("Initializing Perun Webflow configuration...");
        var flow = super.getLoginFlow();
        redirectSubflow(flow);
        flowWithoutWarning(flow);
        flowWithWarning(flow);
        createTransitionStateToPerunUser(flow);
        LOGGER.debug("Perun Webflow configuration initialized successfully.");
    }
}