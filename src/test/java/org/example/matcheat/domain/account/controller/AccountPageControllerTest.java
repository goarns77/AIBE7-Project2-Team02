package org.example.matcheat.domain.account.controller;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class AccountPageControllerTest {
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AccountPageController()).build();

    @ParameterizedTest
    @ValueSource(strings = {
            "/mypage",
            "/mypage/requests",
            "/mypage/purchases",
            "/mypage/sales",
            "/mypage/offers",
            "/mypage/chats"
    })
    void rendersMypageShellForEveryAccountView(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(view().name("account/mypage"));
    }
}
