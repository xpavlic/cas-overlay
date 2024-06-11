package org.apereo.cas.web.flow.perun;

import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
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

        val actionState = createActionState(flow, PerunWebflowConstants.STATE_ID_PERUN_USER, PerunWebflowConstants.ACTION_ID_PERUN_USER);

        val transitionSet = actionState.getTransitionSet();
        val target = getRealSubmissionState(flow).getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();

        transitionSet.add(createTransition(PerunWebflowConstants.TRANSITION_ID_PERUN_USER_SUCCESS, target));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_REDIRECT, target )); // TODO REDIRECT ACTION

        createTransitionStateToPerunUser(flow);
    }
}
