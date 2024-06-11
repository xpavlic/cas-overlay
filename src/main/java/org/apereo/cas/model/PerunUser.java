package org.apereo.cas.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PerunUser {
    private String id;
    private String firstName;
    private String middleName;
    private String lastName;

    @Override
    public String toString() {
        return "PerunUser{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}

