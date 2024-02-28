package kalesite.kalesite.Models.Orders;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_order_products")
public class Order_Order_Products {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order_Orders orderId;

    @ManyToOne
    @JoinColumn(name = "orderproduct_id")
    private Order_OrderProducts orderProductId;
}
