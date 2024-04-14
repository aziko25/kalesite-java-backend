package kalesite.kalesite.Repositories.Orders;

import kalesite.kalesite.Models.Orders.Order_Projects;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Order_ProjectsRepository extends JpaRepository<Order_Projects, Long> {
}