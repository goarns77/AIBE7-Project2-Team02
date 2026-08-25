package org.example.matcheat.account.config;

import org.example.matcheat.account.application.EmailNormalizer;
import org.example.matcheat.account.application.PasswordPolicy;
import org.example.matcheat.account.domain.UserAccount;
import org.example.matcheat.account.domain.UserRole;
import org.example.matcheat.account.port.out.PasswordHasher;
import org.example.matcheat.account.port.out.UserCredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminAccountBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminAccountBootstrap.class);

    private final AccountProperties properties;
    private final UserCredentialRepository repository;
    private final PasswordHasher passwordHasher;

    public AdminAccountBootstrap(
            AccountProperties properties,
            UserCredentialRepository repository,
            PasswordHasher passwordHasher) {
        this.properties = properties;
        this.repository = repository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        AccountProperties.AdminBootstrap bootstrap = properties.getAdminBootstrap();
        if (!bootstrap.isEnabled()) {
            return;
        }
        if (isBlank(bootstrap.getEmail()) || isBlank(bootstrap.getPassword()) || isBlank(bootstrap.getName())) {
            log.warn("관리자 bootstrap 설정이 불완전하여 계정을 생성하지 않습니다.");
            return;
        }

        String email = EmailNormalizer.normalize(bootstrap.getEmail());
        if (repository.existsByRole(UserRole.ADMIN) || repository.existsByEmail(email)) {
            return;
        }

        PasswordPolicy.validate(bootstrap.getPassword());
        repository.save(UserAccount.registerAdmin(
                email,
                passwordHasher.hash(bootstrap.getPassword()),
                bootstrap.getName().trim()));
        log.info("초기 관리자 계정을 생성했습니다.");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
