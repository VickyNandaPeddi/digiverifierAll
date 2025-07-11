/**
 *
 */
package com.aashdit.digiverifier.vendorcheck.repository;

import com.aashdit.digiverifier.vendorcheck.dto.LicheckRequiredResponseDto;
import com.aashdit.digiverifier.vendorcheck.dto.liChecksDetails;
import com.aashdit.digiverifier.vendorcheck.model.ConventionalVendorliChecksToPerform;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author ${Nanda Kishore}
 */
@Repository
public interface LiCheckToPerformRepository extends JpaRepository<ConventionalVendorliChecksToPerform, Long> {

    //    @Query("SELECT new com.aashdit.digiverifier.vendorcheck.dto.LicheckRequiredResponseDto(fm.checkCode,fm.checkName,fm.checkStatus,fm.checkRemarks,fm.modeOfVerificationRequired,fm.modeOfVerificationPerformed,fm.completedDate,fm.createdOn,fm.candidateId,fm.vendorChecks.vendorcheckId,fm.source.sourceId,fm.vendorName,fm.sourceName,fm.vendorChecks.documentname) FROM #{#entityName} fm")
    @Query("SELECT new com.aashdit.digiverifier.vendorcheck.dto.LicheckRequiredResponseDto(fm.id,fm.checkCode,fm.checkName,fm.checkStatus.checkStatusCode,fm.checkRemarks,fm.modeOfVerificationRequired,fm.modeOfVerificationPerformed,fm.completedDate,fm.createdOn,fm.candidateId,fm.vendorChecks.vendorcheckId,fm.source.sourceId) FROM #{#entityName} fm")
    public List<LicheckRequiredResponseDto> findAllLiCheckResponses();

    Boolean existsByCandidateId(String candidateId);

    Boolean existsByRequestId(String requestID);

    @Query("SELECT fm  FROM #{#entityName} fm where fm.candidateId=?1")
    public List<ConventionalVendorliChecksToPerform> findAllLiCheckResponseByCandidateId(String candidateId);

    @Query("SELECT new com.aashdit.digiverifier.vendorcheck.dto.liChecksDetails(fm.checkCode, fm.checkName, fm.checkStatus.vendorCheckStatusMasterId, fm.checkRemarks, fm.modeOfVerificationRequired, fm.modeOfVerificationPerformed, fm.completedDate) FROM #{#entityName} fm WHERE fm.requestId = ?1 AND (fm.stopCheck IS NULL OR UPPER(fm.stopCheck) != 'TRUE') AND fm.checkStatus.vendorCheckStatusMasterId NOT IN (2, 3, 7)")
    public List<liChecksDetails> findAllUpdateLiCheckResponseByRequestId(String requestId);

    @Query("SELECT new com.aashdit.digiverifier.vendorcheck.dto.LicheckRequiredResponseDto(fm.id,fm.checkCode,fm.checkName,fm.checkStatus.checkStatusCode,fm.checkRemarks,fm.modeOfVerificationRequired,fm.modeOfVerificationPerformed,fm.completedDate,fm.createdOn,fm.candidateId,fm.vendorChecks.vendorcheckId,fm.source.sourceId) FROM #{#entityName} fm where fm.checkStatus.vendorCheckStatusMasterId=?1")
    public List<LicheckRequiredResponseDto> findAllLiCheckResponseByCheckStatus(Long checkStatus);

    @Query(value = "select  * from t_dgv_conventional_vendorchecks_to_perform fm where fm.vendor_check=?1", nativeQuery = true)
    public ConventionalVendorliChecksToPerform findByVendorChecksVendorcheckId(Long vendorCheckId);
//    public ConventionalVendorliChecksToPerform findByVendorChecksVendorcheckId(Long vendorCheckId);

    public List<ConventionalVendorliChecksToPerform> findByCandidateId(String canidateId);

    public ConventionalVendorliChecksToPerform findByCheckUniqueId(Long checkUniqueId);


    public List<ConventionalVendorliChecksToPerform> findByRequestId(String requestId);

    @Query("SELECT c.createdOn FROM ConventionalVendorliChecksToPerform c WHERE c.requestId = :requestId GROUP BY c.createdOn ORDER BY COUNT(c) DESC")
    List<Date> findCreatedOnDatesByRequestId(@Param("requestId") String requestId);


    @Query(value = "SELECT created_on FROM t_dgv_conventional_vendorchecks_to_perform WHERE request_id = :requestId AND check_name = :checkName GROUP BY created_on LIMIT 1", nativeQuery = true)
    Date findCreatedOnByRequestIdForCurrentEmployer(@Param("requestId") String requestId, @Param("checkName") String checkName);
    @Modifying
    @Transactional
    @Query(value = "UPDATE t_dgv_conventional_vendorchecks_to_perform " +
            "SET check_status = :statusId, last_updated_by = 'PURGED', last_updated_on = NOW() " +
            "WHERE check_unique_id = :checkUniqueId", nativeQuery = true)
    int updateConventionalCheckStatus(@Param("checkUniqueId") Long checkUniqueId, @Param("statusId") Integer statusId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE t_dgv_vendor_checks " +
            "SET vendor_checkstatus_master_id = :statusId, last_updated_by = 'PURGED', last_updated_on = NOW() " +
            "WHERE vendor_check_id = (SELECT vendor_check FROM t_dgv_conventional_vendorchecks_to_perform WHERE check_unique_id = :checkUniqueId)", nativeQuery = true)
    int updateVendorCheckStatus(@Param("checkUniqueId") Long checkUniqueId, @Param("statusId") Integer statusId);



}
