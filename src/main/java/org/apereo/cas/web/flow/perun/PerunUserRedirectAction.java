package org.apereo.cas.web.flow.perun;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;


@Slf4j
@RequiredArgsConstructor
public class PerunUserRedirectAction extends AbstractAction {

  private final CasConfigurationProperties casProperties;

  @Override
  protected Event doExecute(RequestContext requestContext) throws Exception {
    // Construct the registration URL with the callback
    String callbackUrl = getCallbackUrl(requestContext);
    String registrationUrl = "http://localhost:8080/register" + "?callbackUrl=" + callbackUrl;

    LOGGER.info("Redirecting to registration URL: {}", registrationUrl);

    // Perform external redirect
    requestContext.getExternalContext().requestExternalRedirect(registrationUrl);

    // Return event for REDIRECT transition
    return new EventFactorySupport().event(this, PerunWebflowConstants.TRANSITION_ID_PERUN_USER_SUCCESS);
  }

  private String getCallbackUrl(RequestContext requestContext) {
    return casProperties.getServer().getPrefix() + "/registration/callback";
  }
}