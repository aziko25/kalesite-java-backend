package kalesite.kalesite.Repositories.Orders;

import kalesite.kalesite.Models.Orders.Order_Order_Products;
import kalesite.kalesite.Models.Orders.Order_Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Order_Order_ProductsRepository extends JpaRepository<Order_Order_Products, Long> {

    List<Order_Order_Products> findAllByOrderId(Order_Orders orderId);
}