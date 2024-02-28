package kalesite.kalesite.Repositories.Orders;

import kalesite.kalesite.Models.Orders.Order_Order_Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Order_Order_ProductsRepository extends JpaRepository<Order_Order_Products, Long> {
}