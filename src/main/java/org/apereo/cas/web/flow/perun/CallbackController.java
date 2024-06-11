package org.apereo.cas.web.flow.perun;

import ch.qos.logback.classic.Logger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.core5.net.URIBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.webflow.execution.Action;

@Slf4j
@Controller("callbackController")
@RequiredArgsConstructor
@Getter
public class CallbackController {
    private final CasConfigurationProperties casProperties;

    @GetMapping("/registration/callback")
    public View handleRegistrationCallback(final HttpServletRequest request,
                                           final HttpServletResponse response) throws Exception {
        LOGGER.warn("EXECUTING IDM CHECK ACTION");
        val urlBuilder = new URIBuilder(casProperties.getServer().getLoginUrl());
        request.getParameterMap().forEach((k, v) -> {
            val value = request.getParameter(k);
            urlBuilder.addParameter(k, value);
        });
        val url = urlBuilder.toString();
        LOGGER.debug("Received response from PERUN USER REDIRECT; Redirecting to [{}]", url);
        LOGGER.debug(request.toString());
        LOGGER.debug(response.toString());
        return new RedirectView(url);
    }
}
