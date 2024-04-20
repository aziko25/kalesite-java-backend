package kalesite.kalesite.Repositories.Payme;

import kalesite.kalesite.Models.Payme.Entities.CustomerOrder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends CrudRepository<CustomerOrder, Long> {

}