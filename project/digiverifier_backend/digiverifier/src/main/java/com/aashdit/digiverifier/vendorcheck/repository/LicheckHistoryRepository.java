package com.aashdit.digiverifier.vendorcheck.repository;

import com.aashdit.digiverifier.vendorcheck.model.LicheckHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface LicheckHistoryRepository extends JpaRepository<LicheckHistory,Long> {
    @Query(value = "SELECT * FROM t_dgv_conventional_licheck_history tdclh " +
            "WHERE tdclh.check_unique_id = :checkUniqueId " +
            "  AND tdclh.request_id = :requestId " +
            "  AND tdclh.created_on = (" +
            "    SELECT MAX(created_on) " +
            "    FROM t_dgv_conventional_licheck_history " +
            "    WHERE check_unique_id = :checkUniqueId " +
            "      AND request_id = :requestId )", nativeQuery = true)
    LicheckHistory findLatestCheckByUniqueIdAndRequestIdAndStatus(
            @Param("checkUniqueId") String checkUniqueId,
            @Param("requestId") String requestId
    );

    @Query(value = "SELECT * FROM t_dgv_conventional_licheck_history WHERE check_status = 'INSUFFICIENCY' AND check_unique_id = :checkUniqueId ORDER BY created_on DESC LIMIT 1", nativeQuery = true)
    Optional<LicheckHistory> findLatestByCheckStatusInsufficiency(@Param("checkUniqueId") Long checkUniqueId);

    @Query(value = "SELECT * FROM t_dgv_conventional_licheck_history WHERE check_status = 'INPROGRESS' AND check_unique_id = :checkUniqueId ORDER BY created_on DESC LIMIT 1", nativeQuery = true)
    Optional<LicheckHistory> findLatestByCheckStatusInProgress(@Param("checkUniqueId") Long checkUniqueId);




}
