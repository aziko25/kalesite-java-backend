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
@Table(name = "product_product")
public class Product_Products {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID guid;

    private LocalDateTime createdAt;
    private String code;
    private String title;
    private String description;
    private Double price;
    private String material;
    private String unit;

    private String file3D;

    private String brand;
    private String size;
    private String manufacturer;
    private String photo;
    private Integer quantity;
    private Double discount;

    private Boolean isTop = false;

    private Integer cornerStatus;

    private Integer status;

    @ManyToOne
    @JoinColumn(name = "subcategory_id")
    private Product_Subcategories subcategoryId;

    private String brandEn;
    private String brandRu;
    private String brandUz;

    private String descriptionEn;
    private String descriptionRu;
    private String descriptionUz;

    private String manufacturerEn;
    private String manufacturerRu;
    private String manufacturerUz;

    private String materialEn;
    private String materialRu;
    private String materialUz;

    private String titleEn;
    private String titleRu;
    private String titleUz;

    private Double discountPrice;
}