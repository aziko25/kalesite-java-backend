package kalesite.kalesite.Repositories.Products;

import kalesite.kalesite.Models.Products.Product_Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Product_ProductsRepository extends JpaRepository<Product_Products, Long> {

    Product_Products findByCode(String code);
}