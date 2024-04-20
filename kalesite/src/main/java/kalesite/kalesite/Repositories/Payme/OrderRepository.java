package kalesite.kalesite.Repositories.Payme;

import kalesite.kalesite.Models.Payme.Entities.CustomerOrder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends CrudRepository<CustomerOrder, Long> {

    Optional<CustomerOrder> findByStringId(String id);
}