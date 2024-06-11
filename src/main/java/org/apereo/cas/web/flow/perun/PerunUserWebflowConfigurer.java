package org.apereo.cas.web.flow.perun;

import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

public class PerunUserWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public PerunUserWebflowConfigurer(FlowBuilderServices flowBuilderServices,
                                      FlowDefinitionRegistry flowDefinitionRegistry,
                                      ConfigurableApplicationContext applicationContext,
                                      CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    protected void createTransitionStateToPerunUser(final Flow flow) {
        val submit = getRealSubmissionState(flow);
        createTransitionForState(submit, CasWebflowConstants.TRANSITION_ID_SUCCESS, PerunWebflowConstants.STATE_ID_PERUN_USER, true);
    }

    protected ActionState getRealSubmissionState(final Flow flow) {
        return getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
    }

    @Override
    protected void doInitialize() {
        var flow = super.getLoginFlow();

        // Create the redirect subflow
        val redirectFlow = buildFlow(PerunWebflowConstants.FLOW_ID_REGISTRATION_REDIRECT);
        createEndState(redirectFlow, CasWebflowConstants.STATE_ID_SUCCESS);

        // Register the subflow
        mainFlowDefinitionRegistry.registerFlowDefinition(redirectFlow);

        // Define the redirect action for the transition
        val redirectAction = createActionState(flow, PerunWebflowConstants.STATE_ID_PERUN_USER_REDIRECT, "perunUserRedirectAction");

        // Define the action state for Perun user handling
        val actionState = createActionState(flow, PerunWebflowConstants.STATE_ID_PERUN_USER, PerunWebflowConstants.ACTION_ID_PERUN_USER);

        val transitionSet = actionState.getTransitionSet();

        // Get the target state after success
        val target = getRealSubmissionState(flow).getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();

        // Add transitions for both success and redirect scenarios
        transitionSet.add(createTransition(PerunWebflowConstants.TRANSITION_ID_PERUN_USER_SUCCESS, target));

        // Redirect to the redirect action if TRANSITION_ID_PERUN_USER_REDIRECT is triggered
        transitionSet.add(createTransition(PerunWebflowConstants.TRANSITION_ID_PERUN_USER_REDIRECT, PerunWebflowConstants.STATE_ID_PERUN_USER_REDIRECT));

        // After the redirect action completes, return to the target state
        createTransitionForState(redirectAction, PerunWebflowConstants.TRANSITION_ID_PERUN_USER_SUCCESS, CasWebflowConstants.STATE_ID_END_WEBFLOW);

        // Connect the Perun user state to the flow
        createTransitionStateToPerunUser(flow);
    }
}