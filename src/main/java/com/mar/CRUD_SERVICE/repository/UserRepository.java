package com.mar.CRUD_SERVICE.repository;

import com.mar.CRUD_SERVICE.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository <User, Long>{

}
