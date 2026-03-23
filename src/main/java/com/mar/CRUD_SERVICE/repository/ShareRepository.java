package com.mar.CRUD_SERVICE.repository;

import com.mar.CRUD_SERVICE.model.Share;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShareRepository extends JpaRepository<Share, Long> {
}
