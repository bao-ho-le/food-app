package com.example.foodie.support;

import com.example.foodie.auth.entity.RefreshToken;
import com.example.foodie.catalog.category.entity.Category;
import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.catalog.dishtag.entity.DishTag;
import com.example.foodie.catalog.restaurant.entity.Restaurant;
import com.example.foodie.catalog.tag.entity.Tag;
import com.example.foodie.common.idempotency.entity.IdempotencyKey;
import com.example.foodie.common.idempotency.enums.IdempotencyStatus;
import com.example.foodie.feedback.review.entity.Review;
import com.example.foodie.identity.user.entity.Role;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.enums.Gender;
import com.example.foodie.identity.user.enums.RoleName;
import com.example.foodie.ordering.order.entity.Order;
import com.example.foodie.ordering.order.entity.OrderDish;
import com.example.foodie.ordering.order.enums.Status;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixture builder dùng chung cho toàn bộ test Phase 4, tránh 8 test class lặp lại code dựng
 * đồ thị entity (Role -> User -> Order -> OrderDish -> Dish -> Restaurant,
 * Category -> Tag -> DishTag, OrderDish -> Review).
 *
 * Mỗi hàm persist + flush ngay (không chỉ persist) để id sinh ra (IDENTITY) và các ràng buộc
 * DB (unique, NOT NULL, FK) được kiểm tại chỗ gọi thay vì trôi tới cuối test.
 *
 * @DataJpaTest chạy trong một transaction bị rollback sau mỗi test method, nên các giá trị
 * "duy nhất" ở đây (email, phoneNumber, jti, tên...) chỉ cần không trùng NHAU trong cùng một
 * test -- bộ đếm tĩnh dưới đây đã đủ, không cần UUID.
 */
public final class TestDataFixtures {

    private static final AtomicInteger SEQ = new AtomicInteger();

    private TestDataFixtures() {
    }

    // role_name có ràng buộc UNIQUE. Một số test (RefreshTokenRepositoryTest, phần
    // revokeAllByUser) buộc phải commit thật giữa chừng (TestTransaction) để vượt qua
    // @Transactional(REQUIRES_NEW) -- dữ liệu commit đó không bị rollback nên tồn tại xuyên
    // suốt các test method sau, kể cả khi @DataJpaTest bình thường tự rollback. Nếu cứ insert
    // Role mới mỗi lần gọi, lần thứ hai sẽ đụng UNIQUE constraint. Tra trước rồi mới tạo.
    public static Role role(TestEntityManager em, RoleName roleName) {
        Role existing = em.getEntityManager()
                .createQuery("SELECT r FROM Role r WHERE r.roleName = :name", Role.class)
                .setParameter("name", roleName)
                .getResultStream().findFirst().orElse(null);
        if (existing != null) {
            return existing;
        }
        Role role = new Role();
        role.setRoleName(roleName);
        return em.persistAndFlush(role);
    }

    public static User user(TestEntityManager em, Role role) {
        int n = SEQ.incrementAndGet();
        User user = User.builder()
                .fullName("Test User " + n)
                .gender(Gender.OTHER)
                // Offset để không rơi vào dải "0900000000"/"0900000001" mà một số test khác
                // (FlywayBaselineMigrationTest, app.admin.phone-number) hard-code sẵn -- các
                // test đó chạy trên @SpringBootTest (không rollback), nên nếu trùng số, insert
                // ở đây sẽ đụng UNIQUE constraint tuỳ thứ tự chạy test giữa các class.
                .phoneNumber("0" + String.format("%09d", 500_000_000 + (n % 400_000_000)))
                .email("user" + n + "@test.local")
                .password("password123")
                .role(role)
                .isActive(true)
                .build();
        return em.persistAndFlush(user);
    }

    public static Restaurant restaurant(TestEntityManager em) {
        int n = SEQ.incrementAndGet();
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant " + n)
                .isAvailable(true)
                .build();
        return em.persistAndFlush(restaurant);
    }

    public static Dish dish(TestEntityManager em, Restaurant restaurant) {
        int n = SEQ.incrementAndGet();
        Dish dish = Dish.builder()
                .name("Dish " + n)
                .price(10000f)
                .restaurant(restaurant)
                .isAvailable(true)
                .stockQuantity(100)
                .build();
        return em.persistAndFlush(dish);
    }

    public static Category category(TestEntityManager em) {
        int n = SEQ.incrementAndGet();
        Category category = Category.builder().name("Category " + n).build();
        return em.persistAndFlush(category);
    }

    public static Tag tag(TestEntityManager em, Category category) {
        int n = SEQ.incrementAndGet();
        Tag tag = Tag.builder().name("Tag " + n).category(category).build();
        return em.persistAndFlush(tag);
    }

    public static DishTag dishTag(TestEntityManager em, Dish dish, Tag tag) {
        DishTag dishTag = DishTag.builder().dish(dish).tag(tag).build();
        return em.persistAndFlush(dishTag);
    }

    public static Order order(TestEntityManager em, User user, Status status, float totalPrice) {
        Order order = Order.builder()
                .user(user)
                .status(status)
                .totalPrice(totalPrice)
                .deliveryAddress("123 Test Street")
                .build();
        return em.persistAndFlush(order);
    }

    public static OrderDish orderDish(TestEntityManager em, Order order, Dish dish, int quantity, float price) {
        OrderDish orderDish = OrderDish.builder()
                .order(order)
                .dish(dish)
                .quantity(quantity)
                .price(price)
                .build();
        return em.persistAndFlush(orderDish);
    }

    public static Review review(TestEntityManager em, int rating) {
        Review review = Review.builder().rating(rating).build();
        return em.persistAndFlush(review);
    }

    public static RefreshToken refreshToken(TestEntityManager em, User user, Instant expiresAt) {
        int n = SEQ.incrementAndGet();
        RefreshToken token = RefreshToken.builder()
                .jti("jti-" + n)
                .user(user)
                .expiresAt(expiresAt)
                .build();
        return em.persistAndFlush(token);
    }

    public static IdempotencyKey idempotencyKey(TestEntityManager em, Integer userId, String fingerprint) {
        int n = SEQ.incrementAndGet();
        IdempotencyKey key = IdempotencyKey.builder()
                .key("idem-key-" + n)
                .userId(userId)
                .scope("CREATE_ORDER")
                .status(IdempotencyStatus.COMPLETED)
                .requestFingerprint(fingerprint)
                .build();
        return em.persistAndFlush(key);
    }
}
