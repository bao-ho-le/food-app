package com.example.foodie.support;

import com.example.foodie.auth.config.SecurityConfig;
import com.example.foodie.auth.security.JWTFilter;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation dùng chung cho các test @WebMvcTest ở Phase 3.
 *
 * @WebMvcTest mặc định kéo SecurityConfig (@Configuration, khai báo bean SecurityFilterChain)
 * vào context; SecurityConfig lại phụ thuộc JWTFilter/CustomAuthenticationEntryPoint/
 * CustomAccessDeniedHandler/DaoAuthenticationProvider. Riêng JWTFilter còn bị @WebMvcTest tự
 * quét thêm lần nữa vì nó implements jakarta.servlet.Filter -- nằm trong whitelist mặc định
 * của slice này (Filter luôn được nạp bất kể có nằm trong SecurityConfig hay không). Cả hai
 * đều không nạp được vì các bean chúng cần (JWTService, DaoAuthenticationProvider...) không có
 * trong slice.
 *
 * Xử lý bằng cách loại thẳng SecurityConfig + JWTFilter khỏi component scan (excludeFilters)
 * và tắt filter chain (addFilters = false), thay vì cung cấp @MockitoBean cho từng dependency
 * của chúng -- Phase 3 không kiểm phân quyền (xem mục "Không viết trong Phase 3"), nên không
 * cần dựng một security context giả lập đầy đủ.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@WebMvcTest(excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {SecurityConfig.class, JWTFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
public @interface ControllerSliceTest {

    // Forward sang WebMvcTest#controllers -- mỗi test class chỉ nạp đúng controller đang kiểm,
    // tránh việc @WebMvcTest quét toàn bộ controller trong app (sẽ đòi hỏi mock hết mọi service).
    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] controllers() default {};
}
