package com.example.foodie.identity.user.repository;

import com.example.foodie.identity.user.entity.Role;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.enums.RoleName;
import com.example.foodie.support.AbstractMySqlDataJpaTest;
import com.example.foodie.support.TestDataFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User.role là quan hệ LAZY -- findByIdWithRole tồn tại chỉ để JOIN FETCH nạp sẵn role trong
 * cùng một query, tránh N+1 và tránh LazyInitializationException khi truy cập role ngoài
 * transaction gốc. Test này CHỈ có ý nghĩa nếu persistence context bị clear() trước khi truy
 * cập -- nếu không, entity vẫn còn managed trong context và test xanh giả kể cả khi JOIN
 * FETCH bị gỡ bỏ khỏi query.
 */
class UserRepositoryTest extends AbstractMySqlDataJpaTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Test
    void should_notThrowLazyInitializationException_when_accessingRoleAfterContextCleared() {
        Role role = TestDataFixtures.role(em, RoleName.ADMIN);
        User user = TestDataFixtures.user(em, role);

        em.clear();

        User reloaded = userRepository.findByIdWithRole(user.getId()).orElseThrow();
        assertThat(reloaded.getRole().getRoleName()).isEqualTo(RoleName.ADMIN);
    }
}
