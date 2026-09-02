package org.example.matcheat.domain.account.repository;

import org.example.matcheat.domain.account.entity.AccountReportEntity;
import org.example.matcheat.domain.account.enums.AccountReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountReportRepository extends JpaRepository<AccountReportEntity, Long> {
    Page<AccountReportEntity> findByReporterId(Long reporterId, Pageable pageable);

    @Query("select report from AccountReportEntity report where (:status is null or report.status = :status)")
    Page<AccountReportEntity> search(@Param("status") AccountReportStatus status, Pageable pageable);
}
