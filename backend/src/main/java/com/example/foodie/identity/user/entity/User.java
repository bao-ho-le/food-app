package com.example.foodie.identity.user.entity;

import com.example.foodie.identity.user.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message="Tên không được để trống")
    private String fullName;

    @JsonFormat(pattern="dd-MM-yyyy")
    @Past(message = "Ngày sinh phải trong quá khứ")
    private LocalDate birthday;

    @NotNull(message="Giới tính không được để trống")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotNull(message="Số điện thoại không được để trống")
    @Column(unique=true)
    @Pattern(
            regexp = "^(0|\\+84)(\\d{9})$",
            message = "Số điện thoại không hợp lệ (phải bắt đầu bằng 0 hoặc +84 và có 10 số)"
    )
    private String phoneNumber;

    @NotNull(message="Email không được để trống")
    @Column(unique=true)
    @Email(message="Email sai định dạng")
    private String email;

    @NotNull(message="Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    @JsonIgnore
    private String password;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="role_id", referencedColumnName="id",
            foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE RESTRICT"))
    private Role role;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @NotNull
    @Builder.Default
    private boolean isActive = true;
}

// Giải thích cho @Builder.Default
/*
Mặc dù bạn viết = true, nhưng builder không gọi setter cho isActive → giá trị false (0 trong MySQL) được persist
@Builder do Lombok tạo ra chỉ set những field bạn khai trong builder, các field khác sẽ giữ giá trị default của Java object
Với boolean isActive mặc định trong Java object chưa set → là false
Khi JPA persist → MySQL lưu 0

Dùng @Builder.Default → Lombok sẽ dùng giá trị default khi builder không set giá trị
 */