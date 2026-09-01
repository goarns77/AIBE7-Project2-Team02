package org.example.matcheat.domain.account.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminAccountPageController {
    @GetMapping({"/admin", "/admin/users", "/admin/sellers"})
    public String admin() {
        return "account/admin";
    }
}
