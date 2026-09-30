package com.hungjava.bookstore.repository;

import com.hungjava.bookstore.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    boolean existsByDeliveryId(Integer deliveryId);
    boolean existsByPaymentId(Integer paymentId);

    // Tìm đơn hàng theo đúng ID và đúng người mua
    Optional<Order> findByIdAndUserId(Integer id, Integer userId);

    // Lấy danh sách đơn hàng của user, kèm thông tin tổng items, sách đầu tiên, ảnh sách đầu tiên
    @Query(value = "SELECT o.id AS id, " +
            "o.order_code AS orderCode, " +
            "o.status AS status, " +
            "o.total_price AS totalPrice, " +
            "d.name AS deliveryName, " +
            "p.name AS paymentName, " +
            "COALESCE(SUM(od.quantity), 0) AS totalItems, " +
            "o.created_at AS createdAt, " +
            "fb.first_book_name AS firstBookName, " +
            "fb.first_book_image AS firstBookImage " +
            "FROM orders o " +
            "LEFT JOIN deliveries d ON o.delivery_id = d.id " +
            "LEFT JOIN payments p ON o.payment_id = p.id " +
            "LEFT JOIN order_details od ON o.id = od.order_id " +
            "LEFT JOIN LATERAL (" +
            "   SELECT b.name AS first_book_name, i.url_image AS first_book_image " +
            "   FROM order_details od2 " +
            "   JOIN books b ON od2.book_id = b.id " +
            "   LEFT JOIN images i ON b.id = i.book_id AND i.is_icon = true " +
            "   WHERE od2.order_id = o.id " +
            "   ORDER BY od2.id ASC " +
            "   LIMIT 1" +
            ") fb ON true " +
            "WHERE o.user_id = :userId " +
            "GROUP BY o.id, o.order_code, o.status, o.total_price, d.name, p.name, o.created_at, fb.first_book_name, fb.first_book_image " +
            "ORDER BY o.created_at DESC",
            nativeQuery = true)
    List<OrderProjection> findAllOrdersByUserId(@Param("userId") Integer userId);

    @Query(value = "SELECT o.id AS id, " +
            "o.order_code AS orderCode, " +
            "o.status AS status, " +
            "o.total_price AS totalPrice, " +
            "d.name AS deliveryName, " +
            "p.name AS paymentName, " +
            "COALESCE(SUM(od.quantity), 0) AS totalItems, " +
            "o.created_at AS createdAt, " +
            "fb.first_book_name AS firstBookName, " +
            "fb.first_book_image AS firstBookImage " +
            "FROM orders o " +
            "LEFT JOIN deliveries d ON o.delivery_id = d.id " +
            "LEFT JOIN payments p ON o.payment_id = p.id " +
            "LEFT JOIN order_details od ON o.id = od.order_id " +
            "LEFT JOIN LATERAL (" +
            "   SELECT b.name AS first_book_name, i.url_image AS first_book_image " +
            "   FROM order_details od2 " +
            "   JOIN books b ON od2.book_id = b.id " +
            "   LEFT JOIN images i ON b.id = i.book_id AND i.is_icon = true " +
            "   WHERE od2.order_id = o.id " +
            "   ORDER BY od2.id ASC " +
            "   LIMIT 1" +
            ") fb ON true " +
            "GROUP BY o.id, o.order_code, o.status, o.total_price, d.name, p.name, o.created_at, fb.first_book_name, fb.first_book_image " +
            "ORDER BY o.created_at DESC",
            nativeQuery = true)
    List<OrderProjection> findAllOrdersForAdmin();
}
