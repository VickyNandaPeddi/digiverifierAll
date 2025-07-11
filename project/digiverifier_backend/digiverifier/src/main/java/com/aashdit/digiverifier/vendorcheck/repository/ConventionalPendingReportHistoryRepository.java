package com.aashdit.digiverifier.vendorcheck.repository;

import com.aashdit.digiverifier.vendorcheck.model.CandidatePendingReportHistory;
import com.aashdit.digiverifier.vendorcheck.model.LicheckHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConventionalPendingReportHistoryRepository extends JpaRepository<CandidatePendingReportHistory,Long> {




        @Query(value = "SELECT * FROM t_dgv_conventional_candidate_pending_report_history  WHERE request_Id = :requestId AND request_type LIKE %:requestType%",nativeQuery = true)
        CandidatePendingReportHistory findByRequestIdAndRequestTypeLike(@Param("requestId") Long requestId, @Param("requestType") String requestType);





}
