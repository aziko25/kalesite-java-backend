package kalesite.kalesite.Repositories.Orders;

import kalesite.kalesite.Models.Orders.Order_Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Order_OrdersRepository extends JpaRepository<Order_Orders, Long> {
}
