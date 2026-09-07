package com.airtribe.tasktrackingsystem.repository;

import com.airtribe.tasktrackingsystem.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByUsername(String username);
    Optional<Users> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM Users u WHERE u.username = :username OR u.email = :email")
    Optional<Users> findByusernameOrEmail(@Param("username") String username, @Param("email") String email);
}
