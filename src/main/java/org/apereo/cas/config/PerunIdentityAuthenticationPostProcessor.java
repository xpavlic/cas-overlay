package org.apereo.cas.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.principal.Principal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

@Slf4j
@Setter
@Getter
public class PerunIdentityAuthenticationPostProcessor implements AuthenticationPostProcessor {
    @Override
    public void process(AuthenticationBuilder builder, AuthenticationTransaction transaction) throws Throwable {
        LOGGER.warn("PERUN IDENTITY AUTH POST PROCESSOR");

        Authentication authentication = builder.build();

        Principal principal = authentication.getPrincipal();
        Map<String, List<Object>> attributes = principal.getAttributes();

        List<Object> uidAttrs = attributes.getOrDefault("urn:oid:1.3.6.1.4.1.5923.1.1.1.6", List.of());

        if (!uidAttrs.isEmpty()) {
            String url = "https://perun-api.aai.muni.cz/ba/rpc/json/usersManager/getUserByExtSourceNameAndExtLogin";
            String username = "id-muni";
            String password = "";
            String issuerId = (String) attributes.get("issuerId").getFirst();
            String extLogin = (String) uidAttrs.getFirst();
            String requestBody = String.format("{\"extSourceName\": \"%s\", \"extLogin\": \"%s\"}", issuerId, extLogin);
            String auth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            URL apiUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + auth);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            // Write request body
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestBody.getBytes());
            outputStream.flush();

            // Get response code
            int responseCode = connection.getResponseCode();
            String responseBody = "";
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read response body
                LOGGER.warn("RESPONSE WAS 200");
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                responseBody = response.toString();

                JsonObject jsonObject = JsonValue.readHjson(responseBody).asObject();

                Integer perunUserId = jsonObject.getInt("id", -1);

                attributes.put("perun_user_id", List.of(perunUserId));
                LOGGER.warn("PERUN USER ID: {}", perunUserId);
            } else {
                LOGGER.warn("RESPONSE WAS NOT 200: {} {}", responseCode, connection.getErrorStream().toString());
                attributes.put("ADDED_ATTR", List.of("ADDED_ATTR"));
                LOGGER.warn("ATTR_ADDED");
            }
            connection.disconnect();
        }

        for (Map.Entry<String, List<Object>> entry : attributes.entrySet()) {
            String key = entry.getKey();
            List<Object> values = entry.getValue();
            LOGGER.warn("Key: {} Value: {}", key, values);
        }
    }
}
