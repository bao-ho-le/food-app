package com.example.foodie.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ===================== Auth =====================
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "Yêu cầu đăng nhập"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập tài nguyên này"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập đã hết hạn"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Token không hợp lệ"),
    REFRESH_TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "Refresh token đã bị thu hồi"),
    REFRESH_TOKEN_REUSED(HttpStatus.UNAUTHORIZED, "Phát hiện refresh token bị sử dụng lại, vui lòng đăng nhập lại"),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "Tài khoản đã bị vô hiệu hoá"),

    // ===================== Identity.User =====================
    USER_NOT_AUTHENTICATED(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập"),
    USER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "Id user không được để trống"),
    USER_ID_INVALID(HttpStatus.BAD_REQUEST, "Id user không hợp lệ"),
    USER_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "Email không được để trống"),
    USER_PHONE_REQUIRED(HttpStatus.BAD_REQUEST, "Số điện thoại không được để trống"),
    USER_REQUEST_REQUIRED(HttpStatus.BAD_REQUEST, "Thông tin đăng ký không được để trống"),
    USER_FULL_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "Tên không được để trống"),
    USER_GENDER_REQUIRED(HttpStatus.BAD_REQUEST, "Giới tính không được để trống"),
    USER_LOGIN_REQUEST_REQUIRED(HttpStatus.BAD_REQUEST, "Thông tin đăng nhập không được để trống"),
    USER_LOGIN_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "Mật khẩu không được để trống"),
    USER_RESET_PASSWORD_REQUEST_REQUIRED(HttpStatus.BAD_REQUEST, "Thông tin đổi mật khẩu không được để trống"),
    USER_OLD_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "Mật khẩu cũ không được để trống"),
    USER_PROFILE_REQUEST_REQUIRED(HttpStatus.BAD_REQUEST, "Thông tin hồ sơ không được để trống"),
    USER_BLOCK_TYPE_INVALID(HttpStatus.BAD_REQUEST, "Loại không hợp lệ"),
    USER_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "Mật khẩu không được để trống"),
    USER_PASSWORD_TOO_SHORT(HttpStatus.BAD_REQUEST, "Mật khẩu phải có ít nhất 6 ký tự"),
    USER_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email đã tồn tại"),
    USER_PHONE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Sdt đã tồn tại"),
    USER_ROLE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "Role không tồn tại trong hệ thống"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User không tồn tại"),
    USER_OLD_PASSWORD_INCORRECT(HttpStatus.BAD_REQUEST, "Mật khẩu cũ không đúng"),

    // ===================== Identity.Address =====================
    ADDRESS_ID_REQUIRED(HttpStatus.BAD_REQUEST, "Id địa chỉ không được để trống"),
    ADDRESS_ID_INVALID(HttpStatus.BAD_REQUEST, "Id địa chỉ không hợp lệ"),
    ADDRESS_REQUEST_REQUIRED(HttpStatus.BAD_REQUEST, "Thông tin địa chỉ không được để trống"),
    ADDRESS_LINE_REQUIRED(HttpStatus.BAD_REQUEST, "Địa chỉ không được để trống"),
    ADDRESS_LINE_TOO_LONG(HttpStatus.BAD_REQUEST, "Địa chỉ không được dài quá 255 ký tự"),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "Địa chỉ không tồn tại"),
    ADDRESS_ALREADY_EXISTS(HttpStatus.CONFLICT, "User đã có địa chỉ này rồi"),

    // ===================== Identity.Dashboard =====================
    DASHBOARD_INVALID_TREND_PERIOD(HttpStatus.BAD_REQUEST, "Khoảng thời gian không hợp lệ, chỉ chấp nhận 7, 14 hoặc 30 ngày"),

    // ===================== Catalog.Restaurant =====================
    RESTAURANT_ID_REQUIRED(HttpStatus.BAD_REQUEST, "Id nhà hàng không được để trống"),
    RESTAURANT_ID_INVALID(HttpStatus.BAD_REQUEST, "Id nhà hàng không hợp lệ"),
    RESTAURANT_REQUEST_REQUIRED(HttpStatus.BAD_REQUEST, "Thông tin nhà hàng không được để trống"),
    RESTAURANT_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "Tên nhà hàng không được để trống"),
    RESTAURANT_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "Tên nhà hàng không được dài quá 255 ký tự"),
    RESTAURANT_BLOCK_TYPE_INVALID(HttpStatus.BAD_REQUEST, "Loại không hợp lệ"),
    RESTAURANT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy nhà hàng"),
    RESTAURANT_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "Nhà hàng đã tồn tại"),
    RESTAURANT_PHONE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Số điện thoại đã tồn tại"),

    // ===================== Catalog.Dish =====================
    DISH_ID_REQUIRED(HttpStatus.BAD_REQUEST, "Id món ăn không được để trống"),
    DISH_ID_INVALID(HttpStatus.BAD_REQUEST, "Id món ăn không hợp lệ"),
    DISH_REQUEST_REQUIRED(HttpStatus.BAD_REQUEST, "Thông tin món ăn không được để trống"),
    DISH_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "Tên món ăn không được để trống"),
    DISH_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "Tên món ăn không được dài quá 255 ký tự"),
    DISH_PRICE_NEGATIVE(HttpStatus.BAD_REQUEST, "Giá món ăn không được âm"),
    DISH_TAGS_REQUIRED(HttpStatus.BAD_REQUEST, "Danh sách tag không được để trống"),
    DISH_BLOCK_TYPE_INVALID(HttpStatus.BAD_REQUEST, "Type không hợp lệ"),
    DISH_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy món ăn"),
    DISH_STOCK_QUANTITY_REQUIRED(HttpStatus.BAD_REQUEST, "Số lượng nhập kho không được để trống"),
    DISH_STOCK_QUANTITY_INVALID(HttpStatus.BAD_REQUEST, "Số lượng nhập kho phải lớn hơn 0"),
    DISH_OUT_OF_STOCK(HttpStatus.CONFLICT, "Món ăn không đủ số lượng tồn kho"),
    DISH_NOT_AVAILABLE(HttpStatus.CONFLICT, "Món ăn hiện không khả dụng"),

    // ===================== Catalog.Image =====================
    IMAGE_REQUEST_REQUIRED(HttpStatus.BAD_REQUEST, "Thông tin ảnh không được để trống"),
    IMAGE_DATA_INVALID(HttpStatus.BAD_REQUEST, "Dữ liệu ảnh không hợp lệ"),
    IMAGE_FILE_EMPTY(HttpStatus.BAD_REQUEST, "Gửi file ảnh rỗng"),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể upload ảnh"),

    // ===================== Catalog.Category =====================
    CATEGORY_ID_REQUIRED(HttpStatus.BAD_REQUEST, "Id category không được để trống"),
    CATEGORY_ID_INVALID(HttpStatus.BAD_REQUEST, "Id category không hợp lệ"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tồn tại category nào"),

    // ===================== Catalog.Tag =====================
    TAG_ID_REQUIRED(HttpStatus.BAD_REQUEST, "Id tag không được để trống"),
    TAG_ID_INVALID(HttpStatus.BAD_REQUEST, "Id tag không hợp lệ"),
    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tồn tại tag nào"),

    // ===================== Catalog.DishTag =====================
    DISHTAG_ID_REQUIRED(HttpStatus.BAD_REQUEST, "Id dish-tag không được để trống"),
    DISHTAG_ID_INVALID(HttpStatus.BAD_REQUEST, "Id dish-tag không hợp lệ"),
    DISHTAG_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tồn tại dish-tag này"),
    DISHTAG_ALREADY_EXISTS(HttpStatus.CONFLICT, "Dish đã có tag này rồi"),

    // ===================== Ordering.Order =====================
    ORDER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "Id đơn hàng không được để trống"),
    ORDER_ID_INVALID(HttpStatus.BAD_REQUEST, "Id đơn hàng không hợp lệ"),
    ORDER_CART_EMPTY(HttpStatus.BAD_REQUEST, "Không có món nào trong giỏ hàng để đặt"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Đơn hàng không tồn tại"),
    ORDER_DISH_NOT_FOUND(HttpStatus.NOT_FOUND, "Order dish không tồn tại"),
    ORDER_STATUS_REQUIRED(HttpStatus.BAD_REQUEST, "Trạng thái đơn hàng không được để trống"),
    ORDER_INVALID_STATUS_TRANSITION(HttpStatus.CONFLICT, "Không thể chuyển sang trạng thái này"),
    ORDER_STATUS_CONFLICT(HttpStatus.CONFLICT, "Trạng thái đơn hàng vừa được cập nhật bởi thao tác khác, vui lòng tải lại"),
    ORDER_NOT_OWNER(HttpStatus.FORBIDDEN, "Bạn không có quyền thao tác trên đơn hàng này"),
    ORDER_DELETE_FORBIDDEN(HttpStatus.CONFLICT, "Đơn hàng đã giao không thể xoá, chỉ được lưu trữ"),

    // ===================== Ordering.UserDish =====================
    USERDISH_ID_REQUIRED(HttpStatus.BAD_REQUEST, "Id món trong giỏ không được để trống"),
    USERDISH_ID_INVALID(HttpStatus.BAD_REQUEST, "Id món trong giỏ không hợp lệ"),
    USERDISH_REQUEST_REQUIRED(HttpStatus.BAD_REQUEST, "Thông tin món trong giỏ không được để trống"),
    USERDISH_QUANTITY_REQUIRED(HttpStatus.BAD_REQUEST, "Số lượng không được để trống"),
    USERDISH_QUANTITY_INVALID(HttpStatus.BAD_REQUEST, "Số lượng phải >= 1"),
    USERDISH_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy món trong giỏ"),

    // ===================== Feedback.Review =====================
    REVIEW_ORDER_DISH_ID_REQUIRED(HttpStatus.BAD_REQUEST, "Id món trong đơn hàng không được để trống"),
    REVIEW_ORDER_DISH_ID_INVALID(HttpStatus.BAD_REQUEST, "Id món trong đơn hàng không hợp lệ"),
    REVIEW_REQUEST_REQUIRED(HttpStatus.BAD_REQUEST, "Thông tin đánh giá không được để trống"),
    REVIEW_RATING_REQUIRED(HttpStatus.BAD_REQUEST, "Điểm đánh giá không được để trống"),
    REVIEW_RATING_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, "Điểm đánh giá phải từ 1 đến 5"),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "Bạn đã review món này rồi"),
    REVIEW_NOT_ORDER_OWNER(HttpStatus.FORBIDDEN, "Bạn không có quyền đánh giá đơn hàng này"),
    ORDER_NOT_DELIVERED(HttpStatus.CONFLICT, "Chỉ có thể đánh giá món ăn trong đơn hàng đã giao"),

    // ===================== Common.Idempotency =====================
    IDEMPOTENCY_KEY_TOO_LONG(HttpStatus.BAD_REQUEST, "Idempotency-Key không được dài quá 36 ký tự"),
    IDEMPOTENCY_KEY_REQUEST_MISMATCH(HttpStatus.UNPROCESSABLE_ENTITY, "Idempotency-Key đã được dùng cho một yêu cầu khác với nội dung khác"),
    IDEMPOTENCY_KEY_REQUIRED(HttpStatus.BAD_REQUEST, "Idempotency-Key là bắt buộc"),

    // ===================== Common =====================
    SOMETHING_WENT_WRONG(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong"),
    REQUEST_IN_PROGRESS(HttpStatus.CONFLICT, "Yêu cầu đang được xử lý, vui lòng thử lại sau");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
