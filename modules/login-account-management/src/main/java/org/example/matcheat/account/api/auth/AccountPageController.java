package org.example.matcheat.account.api.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountPageController {
    @GetMapping("/login")
    public String login() {
        return "account/login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "account/signup";
    }
}
