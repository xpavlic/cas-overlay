package org.apereo.cas.web.flow.perun;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.execution.RequestContext;

@Slf4j
@RestController
public class CallbackController {
    private final Action checkUserInIdmsAction;

    public CallbackController(Action checkUserInIdmsAction) {
        this.checkUserInIdmsAction = checkUserInIdmsAction;
    }

    @GetMapping("/registration/callback")
    public void handleRegistrationCallback() throws Exception {
        LOGGER.warn("EXECUTING IDM CHECK ACTION");
        RequestContext context = RequestContextHolder.getRequestContext();
        checkUserInIdmsAction.execute(context);
    }
}
