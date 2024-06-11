package org.apereo.cas.web.flow.perun;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.model.PerunUser;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class PerunUserAction extends BaseCasWebflowAction {

    @Value("${perun.rpc.url}")
    private String rpcUrl;

    @Value("${perun.rpc.username}")
    private String rpcUsername;

    @Value("${perun.rpc.password}")
    private String rpcPassword;

    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecuteInternal(RequestContext requestContext) throws Throwable {
        var authentication = WebUtils.getAuthentication(requestContext);
        Map<String, List<Object>> attributes = authentication.getPrincipal().getAttributes();
        Map<String, List<Object>> auth_attrs = authentication.getAttributes();
        LOGGER.warn("SCOPE: {}", requestContext.getFlowScope().toString());

        List<Object> uidAttrs = attributes.getOrDefault("urn:oid:1.3.6.1.4.1.5923.1.1.1.6", List.of());

        if (!uidAttrs.isEmpty()) {
            String issuerId = (String) attributes.get("issuerId").getFirst();
            String extLogin = (String) uidAttrs.getFirst();

            PerunUser perunUser = checkUserInIdms(extLogin, issuerId);

            if (perunUser != null) {
                attributes.put("perun_user_id", List.of(perunUser.getId()));
                LOGGER.warn("PERUN USER ID: {}", perunUser.getId());
            } else {
                LOGGER.warn("PERUN USER NOT FOUND");
                String registrationUrl = "https://google.com" + "&callbackUrl=" + getCallbackUrl(requestContext);
                requestContext.getExternalContext().requestExternalRedirect(registrationUrl);
                return new EventFactorySupport().event(this, CasWebflowConstants.STATE_ID_REDIRECT);
            }
        }

        for (Map.Entry<String, List<Object>> entry : attributes.entrySet()) {
            String key = entry.getKey();
            List<Object> values = entry.getValue();
            LOGGER.warn("Key: {} Value: {}", key, values);
        }

        LOGGER.warn("XXXXXXXXXXXXX");
        for (Map.Entry<String, List<Object>> entry : auth_attrs.entrySet()) {
            String key = entry.getKey();
            List<Object> values = entry.getValue();
            LOGGER.warn("Key: {} Value: {}", key, values);
        }


        val eventFactorySupport = new EventFactorySupport();
        return eventFactorySupport.event(this, PerunWebflowConstants.TRANSITION_ID_PERUN_USER_SUCCESS);
    }

    private PerunUser checkUserInIdms(String extLogin, String issuerId) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(rpcUsername, rpcPassword);
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            // Create the request body
            String requestBody = String.format("{\"extSourceName\": \"%s\", \"extLogin\": \"%s\"}", issuerId, extLogin);

            String requestUrl = rpcUrl + "/usersManager/getUserByExtSourceNameAndExtLogin";
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<PerunUser> response = restTemplate.exchange(requestUrl, HttpMethod.POST, entity, PerunUser.class);

            LOGGER.warn("Response code: {}, response body: {}", response.getStatusCode(), response.getStatusCode().is2xxSuccessful() ? response.getBody() : null);

            return response.getStatusCode().is2xxSuccessful() ? response.getBody() : null;
        } catch (Exception e) {
            LOGGER.error("Error checking user in IDMS: ", e);
            return null;
        }
    }

    private String getCallbackUrl(RequestContext requestContext) {
        String service = requestContext.getRequestParameters().get("service");
        String baseUrl = casProperties.getServer().getPrefix() + "/registration/callback";
        return baseUrl + "?fromRegistration=true&service=" + service;
    }

}
