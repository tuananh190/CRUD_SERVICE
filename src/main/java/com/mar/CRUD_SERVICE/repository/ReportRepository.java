package com.mar.CRUD_SERVICE.repository;

import com.mar.CRUD_SERVICE.model.Report;
import com.mar.CRUD_SERVICE.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByStatusOrderByCreatedAtAsc(String status);

    List<Report> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(r) > 0 FROM Report r WHERE r.reporter = :reporter AND r.targetType = :targetType AND r.targetId = :targetId AND r.status = 'PENDING'")
    boolean existsPendingReportByReporterAndTarget(
            @Param("reporter") User reporter,
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId
    );

    @Query("SELECT r FROM Report r WHERE r.targetType = :targetType AND r.targetId = :targetId AND r.status = 'PENDING'")
    List<Report> findPendingByTarget(
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId
    );

    void deleteAllByTargetTypeAndTargetId(String targetType, Long targetId);
}
