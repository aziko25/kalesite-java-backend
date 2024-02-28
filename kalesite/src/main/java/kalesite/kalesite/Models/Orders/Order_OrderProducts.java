package kalesite.kalesite.Models.Orders;

import jakarta.persistence.*;
import kalesite.kalesite.Models.Products.Product_Products;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_orderproduct")
public class Order_OrderProducts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID guid;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private Double quantity;

    @Column(name = "\"orderPrice\"")
    private Double orderPrice;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product_Products productId;

    private Double discount;
}
