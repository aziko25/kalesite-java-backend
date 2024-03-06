package kalesite.kalesite.Models.Orders;

import jakarta.persistence.*;
import kalesite.kalesite.Models.Address_Addresses;
import kalesite.kalesite.Models.User_Users;
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
@Table(name = "order_order")
public class Order_Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID guid;

    @Column(name = "\"payme_transaction_id\"")
    private String paymeTransactionId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "\"totalAmount\"")
    private Double totalAmount;

    private Boolean installation;

    @Column(name = "\"orderedTime\"")
    private LocalDateTime orderedTime;

    @Column(name = "\"deliveredTime\"")
    private LocalDateTime deliveredTime;

    private Integer status;
    private String comment;

    @Column(name = "\"paymentStatus\"")
    private String paymentStatus;

    @Column(name = "\"paymentType\"")
    private Integer paymentType;

    private String code;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User_Users userId;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address_Addresses addressId;
}
