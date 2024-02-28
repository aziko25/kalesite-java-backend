package kalesite.kalesite.Repositories;

import kalesite.kalesite.Models.User_Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface User_UsersRepository extends JpaRepository<User_Users, Long> {
}
