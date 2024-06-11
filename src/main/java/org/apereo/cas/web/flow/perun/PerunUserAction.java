package org.apereo.cas.web.flow.perun;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.model.PerunUser;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class PerunUserAction extends BaseCasWebflowAction {

    private final String PERUN_RPC_URL = "https://perun-api.aai.muni.cz/ba/rpc/json/usersManager/getUserByExtSourceNameAndExtLogin";

    private final String IDM_USERNAME = "";
    private final String IDM_PASSWORD = "";

    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecuteInternal(RequestContext requestContext) throws Throwable {
        var authentication = WebUtils.getAuthentication(requestContext);
        Map<String, List<Object>> attributes = authentication.getPrincipal().getAttributes();

        List<Object> uidAttrs = attributes.getOrDefault("urn:oid:1.3.6.1.4.1.5923.1.1.1.6", List.of());

        if (!uidAttrs.isEmpty()) {
            String issuerId = (String) attributes.get("issuerId").getFirst();
            String extLogin = (String) uidAttrs.getFirst();

            PerunUser perunUser = checkUserInIdms(extLogin, issuerId);

            if (perunUser != null) {
                attributes.put("perun_user_id", List.of(perunUser.getId()));
                LOGGER.warn("PERUN USER ID: {}", perunUser.getId());
            } else {
                String registrationUrl = "https://google.com" + "&callbackUrl=" + getCallbackUrl(requestContext);
                requestContext.getExternalContext().requestExternalRedirect(registrationUrl);
                return new Event(this, "redirect");
            }
        }

        for (Map.Entry<String, List<Object>> entry : attributes.entrySet()) {
            String key = entry.getKey();
            List<Object> values = entry.getValue();
            LOGGER.warn("Key: {} Value: {}", key, values);
        }
        return success();
    }

    private PerunUser checkUserInIdms(String extLogin, String issuerId) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(IDM_USERNAME, IDM_PASSWORD);
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            // Create the request body
            String requestBody = String.format("{\"extSourceName\": \"%s\", \"extLogin\": \"%s\"}", issuerId, extLogin);



            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<PerunUser> response = restTemplate.exchange(PERUN_RPC_URL, HttpMethod.POST, entity, PerunUser.class);

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
