package kalesite.kalesite.Models.Products;

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
@Table(name = "product_category")
public class Product_Categories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID guid;
    private LocalDateTime createdAt;

    private String title;
    private String photo;

    private String titleEn;
    private String titleRu;
    private String titleUz;
}
