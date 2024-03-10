package kalesite.kalesite.Repositories.Orders;

import kalesite.kalesite.Models.Orders.Order_OrderProducts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface Order_OrderProductRepository extends JpaRepository<Order_OrderProducts, Long> {

    List<Order_OrderProducts> findAllByIdIn(List<Long> id);
}
