package kalesite.kalesite.Models;

import jakarta.persistence.*;
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
@Table(name = "address_address")
public class Address_Addresses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID guid;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private String region;
    private String district;
    private String street;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User_Users userId;
}