package kalesite.kalesite.Repositories;

import kalesite.kalesite.Models.Address_Addresses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Address_AddressesRepository extends JpaRepository<Address_Addresses, Long> {
}