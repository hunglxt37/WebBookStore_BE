package com.hungjava.bookstore.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {

    // Lỗi ứng dụng
    USER_NOT_FOUND("Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    BOOK_NOT_FOUND("Không tìm thấy sách", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND("Không tìm thấy danh mục", HttpStatus.NOT_FOUND),
    REVIEW_NOT_FOUND("Không tìm thấy đánh giá", HttpStatus.NOT_FOUND),
    GENRE_NOT_FOUND("Không tìm thấy thể loại", HttpStatus.NOT_FOUND),
    DELIVERY_NOT_FOUND("Không tìm thấy phương thức vận chuyển", HttpStatus.NOT_FOUND),
    DELIVERY_IN_USE("Phương thức vận chuyển đã được sử dụng trong đơn hàng, không thể xóa", HttpStatus.BAD_REQUEST),
    PAYMENT_IN_USE("Phương thức thanh toán đã được sử dụng trong đơn hàng, không thể xóa", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_FOUND("Không tìm thấy phương thức thanh toán", HttpStatus.NOT_FOUND),
    CART_EMPTY("Giỏ hàng của người dùng trống", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_METHOD("Phương thức thanh toán phải là Tiền mặt (COD)", HttpStatus.BAD_REQUEST),
    OUT_OF_STOCK("Số lượng sách trong kho không đủ", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND("Không tìm thấy đơn đặt hàng",HttpStatus.NOT_FOUND),
    CANNOT_CANCEL_ORDER("Đơn hàng đang giao hoặc đã giao, không thể hủy",HttpStatus.BAD_REQUEST),
    INVALID_USER("Tài khoản không tồn tại", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD("Email hoặc mật khẩu không chính xác", HttpStatus.BAD_REQUEST),
    USER_ALREADY_EXISTS("Tên đăng nhập đã tồn tại", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS("Email đã tồn tại", HttpStatus.BAD_REQUEST),
    CART_ITEM_NOT_FOUND("Không tìm thấy sản phẩm trong giỏ hàng", HttpStatus.NOT_FOUND),

    //Lỗi hệ thống & phân quyền
    UNAUTHORIZED("Bạn chưa đăng nhập", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("Bạn không có quyền truy cập", HttpStatus.FORBIDDEN),
    USER_INACTIVE("Tài khoản chưa được kích hoạt", HttpStatus.BAD_REQUEST),
    INVALID_ACTIVATION_TOKEN("Mã kích hoạt không hợp lệ hoặc đã hết hạn", HttpStatus.BAD_REQUEST);


    String message;
    HttpStatus status;
}
