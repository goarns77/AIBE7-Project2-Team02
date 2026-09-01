package org.example.matcheat.domain.account.service;

import org.example.matcheat.domain.account.entity.IssuedAccessToken;
import org.example.matcheat.domain.account.entity.UserAccount;
import org.example.matcheat.domain.account.enums.SellerVerificationStatus;
import org.example.matcheat.domain.account.enums.UserRole;
import org.example.matcheat.domain.account.enums.UserStatus;
import org.example.matcheat.domain.account.repository.AdminAccountRepository;
import org.example.matcheat.domain.account.repository.UserCredentialRepository;
import org.example.matcheat.domain.account.security.AccessTokenIssuer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:admin-account;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.account.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="
})
@Transactional
class AdminAccountIntegrationTest {
    @Autowired
    private UserCredentialRepository users;

    @Autowired
    private SellerApplicationService sellerApplications;

    @Autowired
    private AdminAccountService adminService;

    @Autowired
    private AccessTokenIssuer tokenIssuer;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void managesUserAndSellerApplicationThroughJpa() {
        UserAccount admin = users.save(UserAccount.registerAdmin(
                "admin@example.com", "{bcrypt}hash", "관리자"));
        UserAccount member = users.save(UserAccount.registerUser(
                "member@example.com", "{bcrypt}hash", "회원"));

        AdminAccountRepository.PageResult<AdminAccountRepository.UserSummary> search =
                adminService.searchUsers("member", UserStatus.ACTIVE, 0, 20);
        assertThat(search.content()).extracting(AdminAccountRepository.UserSummary::userId)
                .containsExactly(member.id());

        AdminAccountRepository.UserSummary suspended = adminService.changeUserStatus(
                admin.id(), member.id(), UserStatus.SUSPENDED);
        assertThat(suspended.status()).isEqualTo(UserStatus.SUSPENDED);
        assertThat(suspended.tokenVersion()).isEqualTo(1);

        adminService.changeUserStatus(admin.id(), member.id(), UserStatus.ACTIVE);
        SellerApplicationService.ApplicationResult application = sellerApplications.apply(
                member.id(), "매치잇 상회", "123-45-67890",
                new BigDecimal("37.5665"), new BigDecimal("126.9780"), new BigDecimal("10.0"));
        IssuedAccessToken memberToken = tokenIssuer.issue(users.findById(member.id()).orElseThrow());

        AdminAccountRepository.SellerSummary approved = adminService.reviewSellerApplication(
                admin.id(), application.sellerId(), SellerVerificationStatus.APPROVED, null);
        assertThat(approved.status()).isEqualTo(SellerVerificationStatus.APPROVED);
        assertThat(approved.reviewedAt()).isNotNull();
        assertThat(adminService.dashboard().pendingSellerApplications()).isZero();

        UserAccount seller = users.findById(member.id()).orElseThrow();
        assertThat(seller.role()).isEqualTo(UserRole.SELLER);
        assertThat(seller.tokenVersion()).isEqualTo(2);
        assertThatThrownBy(() -> jwtDecoder.decode(memberToken.value())).isInstanceOf(JwtException.class);

        Jwt sellerJwt = jwtDecoder.decode(tokenIssuer.issue(seller).value());
        assertThat(sellerJwt.getClaimAsString("role")).isEqualTo("SELLER");
    }
}
