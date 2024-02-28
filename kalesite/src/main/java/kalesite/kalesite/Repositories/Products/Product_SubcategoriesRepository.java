package kalesite.kalesite.Repositories.Products;

import kalesite.kalesite.Models.Products.Product_Subcategories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Product_SubcategoriesRepository extends JpaRepository<Product_Subcategories, Long> {

    Product_Subcategories findByTitle(String title);
}
