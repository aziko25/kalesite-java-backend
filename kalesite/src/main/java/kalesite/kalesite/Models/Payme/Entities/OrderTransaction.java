package kalesite.kalesite.Models.Payme.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payme_order_transaction")
public class OrderTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String paycomId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date paycomTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Long createTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date performTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date cancelTime;

    @Enumerated(EnumType.STRING) // Assuming OrderCancelReason is an enum
    private OrderCancelReason reason;

    @Enumerated(EnumType.STRING) // Assuming TransactionState is an enum
    private TransactionState state;

    @OneToOne
    private CustomerOrder order;

    public void setCreateTime(Date createTime) {
        this.createTime = createTime.getTime();
    }
}