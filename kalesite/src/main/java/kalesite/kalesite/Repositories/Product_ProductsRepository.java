package kalesite.kalesite.Repositories;

import kalesite.kalesite.Models.Product_Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Product_ProductsRepository extends JpaRepository<Product_Products, Long> {

    Product_Products findByCode(String code);
}