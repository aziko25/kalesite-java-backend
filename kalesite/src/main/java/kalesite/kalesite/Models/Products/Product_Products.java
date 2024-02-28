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
@Table(name = "product_product")
public class Product_Products {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID guid;

    @Column(name = "\"created_at\"")
    private LocalDateTime createdAt;
    private String code;
    private String title;
    private String description;
    private Double price;
    private String material;
    private String unit;

    @Column(name = "\"file3D\"")
    private String file3D;

    private String brand;
    private String size;
    private String manufacturer;
    private String photo;
    private Integer quantity;
    private Double discount;

    @Column(name = "\"isTop\"")
    private Boolean isTop = false;

    @Column(name = "\"cornerStatus\"")
    private Integer cornerStatus;

    private Integer status;

    @ManyToOne
    @JoinColumn(name = "subcategory_id")
    private Product_Subcategories subcategoryId;

    @Column(name = "brand_en")
    private String brandEn;

    @Column(name = "brand_ru")
    private String brandRu;

    @Column(name = "brand_uz")
    private String brandUz;

    @Column(name = "description_en")
    private String descriptionEn;

    @Column(name = "description_ru")
    private String descriptionRu;

    @Column(name = "description_uz")
    private String descriptionUz;

    @Column(name = "manufacturer_en")
    private String manufacturerEn;

    @Column(name = "manufacturer_ru")
    private String manufacturerRu;

    @Column(name = "manufacturer_uz")
    private String manufacturerUz;

    @Column(name = "material_en")
    private String materialEn;

    @Column(name = "material_ru")
    private String materialRu;

    @Column(name = "material_uz")
    private String materialUz;

    @Column(name = "title_en")
    private String titleEn;

    @Column(name = "title_ru")
    private String titleRu;

    @Column(name = "title_uz")
    private String titleUz;

    @Column(name = "\"discountPrice\"")
    private Double discountPrice;
}