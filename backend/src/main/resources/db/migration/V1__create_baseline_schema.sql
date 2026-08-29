-- Baseline schema tái tạo 13 bảng gốc, vốn được sinh ra trong quá khứ bằng
-- ddl-auto=update chứ chưa từng đi qua Flyway. Không có migration nào trước
-- V2 mô tả chúng, nên một database rỗng không thể dựng được schema đầy đủ.
--
-- Nguồn của DDL dưới đây: cho Hibernate tự sinh (jakarta.persistence.schema-
-- generation.scripts.action=create, dialect=MySQL8Dialect) từ chính các entity,
-- rồi bỏ đi mọi thứ do V2-V7 thêm sau: bảng refresh_tokens (V2), cột
-- dish.stock_quantity (V3), cột orders.version (V4), bảng idempotency_keys
-- (V5-V7). V1 mô tả đúng trạng thái schema TRƯỚC V2.
--
-- An toàn với DB dev hiện tại: flyway_schema_history đã có dòng
-- "<< Flyway Baseline >>" ở version 1 (spring.flyway.baseline-version=1),
-- nên Flyway sẽ bỏ qua V1 trên DB đó và chỉ áp dụng nó cho DB mới hoàn toàn.
--
-- Ghi chú: đã kiểm chứng thực tế trên MySQL 8.4 rằng `CREATE TABLE user (...)`
-- không cần backtick (user không phải reserved word chặn vị trí identifier ở
-- MySQL 8.4). Vẫn backtick `user` bên dưới theo yêu cầu, không có tác dụng phụ.

CREATE TABLE role (
    id INT NOT NULL AUTO_INCREMENT,
    role_name ENUM('ADMIN','USER') NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE `user` (
    id INT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(255) NOT NULL,
    birthday DATE,
    gender ENUM('FEMALE','MALE','OTHER') NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    is_active BIT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE address (
    id INT NOT NULL AUTO_INCREMENT,
    address VARCHAR(255) NOT NULL,
    user_id INT NOT NULL,
    is_default BIT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE restaurant (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    phone_number VARCHAR(255),
    is_available BIT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE dish (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    price FLOAT(23) NOT NULL,
    restaurant_id INT,
    is_available BIT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE image (
    id INT NOT NULL AUTO_INCREMENT,
    image_name VARCHAR(255),
    is_thumbnail BIT,
    url VARCHAR(255) NOT NULL,
    image_id INT,
    public_id VARCHAR(255),
    format VARCHAR(255),
    size BIGINT,
    width INT,
    height INT,
    alt_text VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE category (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE tag (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    category_id INT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE dish_tag (
    id INT NOT NULL AUTO_INCREMENT,
    dish_id INT,
    tag_id INT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE user_dish (
    id INT NOT NULL AUTO_INCREMENT,
    quantity INT NOT NULL,
    user_id INT,
    dish_id INT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE orders (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT,
    status ENUM('CANCELLED','DELIVERED','DELIVERING','PENDING','PREPARING') NOT NULL,
    total_price FLOAT(23) NOT NULL,
    delivery_address VARCHAR(255) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE review (
    id INT NOT NULL AUTO_INCREMENT,
    rating INT NOT NULL,
    comment VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE order_dish (
    id INT NOT NULL AUTO_INCREMENT,
    order_id INT NOT NULL,
    dish_id INT NOT NULL,
    quantity INT NOT NULL,
    price FLOAT(23) NOT NULL,
    review_id INT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

ALTER TABLE role
    ADD CONSTRAINT uk_role_role_name UNIQUE (role_name);

ALTER TABLE `user`
    ADD CONSTRAINT uk_user_email UNIQUE (email),
    ADD CONSTRAINT uk_user_phone_number UNIQUE (phone_number),
    ADD CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE RESTRICT;

ALTER TABLE address
    ADD CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE;

ALTER TABLE dish
    ADD CONSTRAINT fk_dish_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant (id) ON DELETE RESTRICT;

ALTER TABLE image
    ADD CONSTRAINT fk_image_dish FOREIGN KEY (image_id) REFERENCES dish (id) ON DELETE CASCADE;

ALTER TABLE category
    ADD CONSTRAINT uk_category_name UNIQUE (name);

ALTER TABLE tag
    ADD CONSTRAINT fk_tag_category FOREIGN KEY (category_id) REFERENCES category (id) ON DELETE RESTRICT;

ALTER TABLE dish_tag
    ADD CONSTRAINT fk_dish_tag_dish FOREIGN KEY (dish_id) REFERENCES dish (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_dish_tag_tag FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE;

ALTER TABLE user_dish
    ADD CONSTRAINT fk_user_dish_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_user_dish_dish FOREIGN KEY (dish_id) REFERENCES dish (id) ON DELETE CASCADE;

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE RESTRICT;

ALTER TABLE order_dish
    ADD CONSTRAINT uk_order_dish_review_id UNIQUE (review_id),
    ADD CONSTRAINT fk_order_dish_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_order_dish_dish FOREIGN KEY (dish_id) REFERENCES dish (id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_order_dish_review FOREIGN KEY (review_id) REFERENCES review (id) ON DELETE SET NULL;
