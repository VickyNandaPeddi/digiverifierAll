package com.aashdit.digiverifier.vendorcheck.repository;

import com.aashdit.digiverifier.vendorcheck.model.ConventionalVendorCandidatesSubmitted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ConventionalCandidatesSubmittedRepository extends JpaRepository<ConventionalVendorCandidatesSubmitted, Long> {

//    @Query("SELECT new com.aashdit.digiverifier.vendorcheck.dto.SubmittedCandidates(fm.candidateId,fm.psNo,fm.name,fm.requestId,fm.requestType,fm.vendorId,fm.applicantId,fm.createdOn,fm.status.statusCode) FROM #{#entityName} fm ")
//    List<SubmittedCandidates> findAllSubmittedCandidates();


//    @Query("SELECT new com.aashdit.digiverifier.vendorcheck.dto.SubmittedCandidates(fm.candidateId,fm.psNo,fm.name,fm.requestId,fm.requestType,fm.vendorId,fm.applicantId,fm.createdOn,fm.status) FROM #{#entityName} fm WHERE fm.createdOn BETWEEN :startDate AND :endDate")

//    @Query("SELECT new com.aashdit.digiverifier.vendorcheck.dto.SubmittedCandidates(fm.candidateId,fm.psNo,fm.name,fm.requestId,fm.requestType,fm.vendorId,fm.applicantId,fm.createdOn,fm.status.statusCode) FROM #{#entityName} fm WHERE fm.createdOn BETWEEN ?1 AND ?2")
////    List<SubmittedCandidates> findAllSubmittedCandidatesByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
//    List<SubmittedCandidates> findAllSubmittedCandidatesByDateRange(Date startDate, Date endDate);

//    @Query("SELECT new com.aashdit.digiverifier.vendorcheck.dto.UpdateSubmittedCandidatesResponseDto(fm.candidateId,fm.psNo,fm.name,fm.requestId,fm.vendorId) FROM #{#entityName} fm where fm.candidateId=?1 ")
//    UpdateSubmittedCandidatesResponseDto findUpdateSubmittedCandidatesResponseDtoByConventinalCandidateId(Long canidateId);

//    Boolean existsByCandidateId(Long candidateId);

    Boolean existsByRequestId(String requestID);

    ConventionalVendorCandidatesSubmitted findByRequestId(String requestID);
//    List<ConventionalVendorCandidatesSubmitted> findByRequestIdIn(List<String> requestIds);

    @Query(value = "select * from t_dgv_conventional_candidate_request where created_on BETWEEN :startDate AND :endDate", nativeQuery = true)
    List<ConventionalVendorCandidatesSubmitted> findByRequestIdByDateRange( @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query(value = "SELECT * FROM t_dgv_conventional_candidate_request " +
            "WHERE  (candidate_id  LIKE %:userSearchInput% " +
            "OR status  LIKE %:userSearchInput% " +
            "OR request_type  LIKE %:userSearchInput% " +
            "OR request_id  LIKE %:userSearchInput% " +
            "OR ps_no  LIKE %:userSearchInput% " +
            "OR applicant_id  LIKE %:userSearchInput% " +
            "OR name LIKE %:userSearchInput%) " +
            "AND created_on BETWEEN :startDate AND :endDate", nativeQuery = true)
    List<ConventionalVendorCandidatesSubmitted> searchAllCandidate(
            @Param("userSearchInput") String userSearchInput,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);
    @Query("SELECT c FROM ConventionalVendorCandidatesSubmitted c " +
            "WHERE c.purgedDate BETWEEN :startDate AND :endDate " +
            "AND c.status.statusMasterId = :statusId")
    List<ConventionalVendorCandidatesSubmitted> findByDateRangePurged(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("statusId") Long statusId
    );



    @Query(value = "SELECT * FROM t_dgv_conventional_candidate_request " +
            "WHERE status  LIKE :userSearchInput " +
            "AND created_on BETWEEN :startDate AND :endDate", nativeQuery = true)
    List<ConventionalVendorCandidatesSubmitted> searchAllCandidateStatus(
            @Param("userSearchInput") String userSearchInput,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    @Query(value = "SELECT request_id FROM t_dgv_conventional_candidate_request creq WHERE creq.created_on BETWEEN :startDate AND :endDate", nativeQuery = true)
    List<Long> findRequestIdsByDateRange(@Param("startDate") Date startDate,
                                         @Param("endDate") Date endDate);

}
