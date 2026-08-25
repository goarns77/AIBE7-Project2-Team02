package org.example.matcheat.account.config;

import org.example.matcheat.account.domain.UserAccount;
import org.example.matcheat.account.domain.UserRole;
import org.example.matcheat.account.port.out.PasswordHasher;
import org.example.matcheat.account.port.out.UserCredentialRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AdminAccountBootstrapTest {
    @Test
    void disabledBootstrapDoesNothing() {
        InMemoryRepository repository = new InMemoryRepository();
        AccountProperties properties = new AccountProperties();
        AdminAccountBootstrap bootstrap = new AdminAccountBootstrap(properties, repository, fixedHasher());

        bootstrap.run(new DefaultApplicationArguments(new String[0]));

        assertThat(repository.accounts).isEmpty();
    }

    @Test
    void createsAdminOnceWithoutOverwritingOnRestart() {
        InMemoryRepository repository = new InMemoryRepository();
        AccountProperties properties = enabledProperties();
        AdminAccountBootstrap bootstrap = new AdminAccountBootstrap(properties, repository, fixedHasher());

        bootstrap.run(new DefaultApplicationArguments(new String[0]));
        bootstrap.run(new DefaultApplicationArguments(new String[0]));

        assertThat(repository.accounts).hasSize(1);
        UserAccount admin = repository.accounts.get(0);
        assertThat(admin.email()).isEqualTo("admin@matcheat.local");
        assertThat(admin.role()).isEqualTo(UserRole.ADMIN);
        assertThat(admin.passwordHash()).isEqualTo("{bcrypt}admin-hash");
    }

    private static AccountProperties enabledProperties() {
        AccountProperties properties = new AccountProperties();
        properties.getAdminBootstrap().setEnabled(true);
        properties.getAdminBootstrap().setEmail("ADMIN@matcheat.local");
        properties.getAdminBootstrap().setPassword("adminpassword1234");
        properties.getAdminBootstrap().setName("관리자");
        return properties;
    }

    private static PasswordHasher fixedHasher() {
        return new PasswordHasher() {
            @Override
            public String hash(String rawPassword) {
                return "{bcrypt}admin-hash";
            }

            @Override
            public boolean matches(String rawPassword, String encodedPassword) {
                return false;
            }
        };
    }

    private static final class InMemoryRepository implements UserCredentialRepository {
        private final List<UserAccount> accounts = new ArrayList<>();

        @Override
        public Optional<UserAccount> findByEmail(String email) {
            return accounts.stream().filter(account -> account.email().equals(email)).findFirst();
        }

        @Override
        public boolean existsByEmail(String email) {
            return findByEmail(email).isPresent();
        }

        @Override
        public boolean existsByRole(UserRole role) {
            return accounts.stream().anyMatch(account -> account.role() == role);
        }

        @Override
        public UserAccount save(UserAccount account) {
            UserAccount saved = UserAccount.restore(
                    (long) accounts.size() + 1,
                    account.email(),
                    account.passwordHash(),
                    account.name(),
                    account.role(),
                    account.status(),
                    account.tokenVersion(),
                    account.withdrawnAt());
            accounts.add(saved);
            return saved;
        }
    }
}
