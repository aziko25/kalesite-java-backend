package kalesite.kalesite.Models.Products;

import jakarta.persistence.*;
import kalesite.kalesite.Models.Products.Product_Categories;
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
@Table(name = "product_subcategory")
public class Product_Subcategories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID guid;
    private LocalDateTime createdAt;
    private String title;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Product_Categories categoryId;

    private String titleEn;
    private String titleRu;
    private String titleUz;
}
