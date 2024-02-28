package kalesite.kalesite.Repositories.Products;

import kalesite.kalesite.Models.Products.Product_Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Product_CategoriesRepository extends JpaRepository<Product_Categories, Long> {
}
