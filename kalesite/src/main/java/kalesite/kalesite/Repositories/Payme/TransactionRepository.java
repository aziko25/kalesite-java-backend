package kalesite.kalesite.Repositories.Payme;

import kalesite.kalesite.Models.Payme.Entities.CustomerOrder;
import kalesite.kalesite.Models.Payme.Entities.OrderTransaction;
import kalesite.kalesite.Models.Payme.Entities.TransactionState;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<OrderTransaction, Long> {

    OrderTransaction findByPaycomId(String id);

    @Query("select o from OrderTransaction o " +
            "where o.paycomTime between ?1 and ?2 and o.state = ?3 ORDER BY o.paycomTime ASC")
    List<OrderTransaction> findByPaycomTimeAndState(Date from, Date to, TransactionState state);

    List<OrderTransaction> findByPaycomTimeBetweenAndState(Date from, Date to, TransactionState state);

    OrderTransaction findByOrder(CustomerOrder order);

    OrderTransaction findFirstByPaycomId(String paycomId);

    OrderTransaction findFirstByOrder(CustomerOrder order);
}