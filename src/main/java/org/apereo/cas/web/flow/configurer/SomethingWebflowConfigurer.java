package org.apereo.cas.web.flow.configurer;

import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

public class SomethingWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public SomethingWebflowConfigurer(FlowBuilderServices flowBuilderServices,
                                      FlowDefinitionRegistry flowDefinitionRegistry,
                                      ConfigurableApplicationContext applicationContext,
                                      CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        var flow = super.getLoginFlow();

        val perunUserFlow = buildFlow("getPerunUserCompleted");
        val actionState = createActionState(perunUserFlow, "getPerunUserCompletedFlow", createEvaluateAction("checkUserInIdmsAction"));
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_SUCCESS)



        createSubflowState(flow, "getPerunUserSubflow", );
    }
}
