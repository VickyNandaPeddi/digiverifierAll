package com.aashdit.digiverifier.vendorcheck.repository;

import com.aashdit.digiverifier.vendorcheck.model.AgentUploadedChecks;
import com.aashdit.digiverifier.vendorcheck.model.ConventionalAttributesMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentUplaodedChecksRepository  extends JpaRepository<AgentUploadedChecks, Long>{
    @Query(value = "SELECT * FROM t_dgv_conventional_agent_uploaded_checks " +
            "WHERE request_id = :requestId AND source = :source " +
            "ORDER BY created_on DESC LIMIT 1", nativeQuery = true)
    AgentUploadedChecks findByRequestIdAndSource(@Param("requestId") String requestId, @Param("source") String source);

    List<AgentUploadedChecks> findByRequestID(String requestId);
}
