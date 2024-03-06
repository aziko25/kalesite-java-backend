package kalesite.kalesite.Repositories.Orders;

import kalesite.kalesite.Models.Orders.Order_Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface Order_OrdersRepository extends JpaRepository<Order_Orders, Long> {

    Order_Orders findFirstByPaymentTypeAndGuidIsNull(Integer paymentType);
    Order_Orders findByGuid(UUID guid);
}
