package org.focus.logmeet.controller.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthSignupRequest {
    private String email;
    private String password;
}
