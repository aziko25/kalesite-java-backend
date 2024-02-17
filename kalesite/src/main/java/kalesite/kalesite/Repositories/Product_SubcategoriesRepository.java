package kalesite.kalesite.Repositories;

import kalesite.kalesite.Models.Product_Subcategories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Product_SubcategoriesRepository extends JpaRepository<Product_Subcategories, Long> {

    Product_Subcategories findByTitle(String title);
}
