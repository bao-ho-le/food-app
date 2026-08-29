package com.example.foodie.ordering.order.repository;

import com.example.foodie.identity.user.entity.Role;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.enums.RoleName;
import com.example.foodie.ordering.order.entity.Order;
import com.example.foodie.ordering.order.enums.Status;
import com.example.foodie.support.AbstractMySqlDataJpaTest;
import com.example.foodie.support.TestDataFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * sumTotalPriceByStatusAndCreatedAtBetween là nguồn số liệu doanh thu (dashboard). Ba điểm
 * dễ sai của một aggregate JPQL: SUM trên tập rỗng trả null (không phải 0, tầng service có
 * nhánh xử lý riêng cho null), lọc đúng status, và BETWEEN có bao gồm hai đầu mút hay không.
 */
class OrderRepositoryTest extends AbstractMySqlDataJpaTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private OrderRepository orderRepository;

    private static final Instant START = Instant.now().minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MICROS);
    private static final Instant END = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MICROS);

    private User newUser() {
        Role role = TestDataFixtures.role(em, RoleName.USER);
        return TestDataFixtures.user(em, role);
    }

    // created_at là @CreationTimestamp -- Hibernate luôn tự set "now" lúc INSERT, entity
    // setter bị bỏ qua ở flush thường (updatable = false chỉ chặn auto dirty-checking, không
    // chặn JPQL UPDATE tường minh). Dùng JPQL (không phải native SQL) để ghi đè: cùng một
    // đường convert Instant<->DATETIME(6) mà sumTotalPriceByStatusAndCreatedAtBetween dùng để
    // đọc lại -- native SQL với java.sql.Timestamp đi qua driver JDBC thô, từng lệch múi giờ
    // vài giờ so với cách Hibernate tự bind Instant, làm item biên (đặt createdAt = START/END
    // rồi mong BETWEEN khớp tuyệt đối) sai lệch dù dữ liệu "đúng" theo mắt thường.
    private void backdateCreatedAt(Order order, Instant when) {
        em.getEntityManager()
                .createQuery("UPDATE Order o SET o.createdAt = :when WHERE o.id = :id")
                .setParameter("when", when)
                .setParameter("id", order.getId())
                .executeUpdate();
        em.clear();
    }

    // ---- item 1: không có đơn nào khớp -> null, không phải 0 ----

    @Test
    void should_returnNull_when_noOrderMatches() {
        Float sum = orderRepository.sumTotalPriceByStatusAndCreatedAtBetween(Status.DELIVERED, START, END);
        assertThat(sum).isNull();
    }

    // ---- item 2: chỉ cộng đúng status truyền vào ----

    @Test
    void should_sumOnlyMatchingStatus_when_mixedStatusesExist() {
        User user = newUser();
        TestDataFixtures.order(em, user, Status.DELIVERED, 100_000f);
        TestDataFixtures.order(em, user, Status.DELIVERED, 250_000f);
        TestDataFixtures.order(em, user, Status.PENDING, 500_000f);

        Float sum = orderRepository.sumTotalPriceByStatusAndCreatedAtBetween(Status.DELIVERED, START, END);
        assertThat(sum).isEqualTo(350_000f);
    }

    // ---- item 3: BETWEEN bao gồm cả hai đầu mút ----

    @Test
    void should_includeBothBoundaries_when_createdAtEqualsStartOrEnd() {
        User user = newUser();
        Order atStart = TestDataFixtures.order(em, user, Status.DELIVERED, 100_000f);
        Order atEnd = TestDataFixtures.order(em, user, Status.DELIVERED, 200_000f);
        backdateCreatedAt(atStart, START);
        backdateCreatedAt(atEnd, END);

        Float sum = orderRepository.sumTotalPriceByStatusAndCreatedAtBetween(Status.DELIVERED, START, END);
        assertThat(sum).isEqualTo(300_000f);
    }
}
