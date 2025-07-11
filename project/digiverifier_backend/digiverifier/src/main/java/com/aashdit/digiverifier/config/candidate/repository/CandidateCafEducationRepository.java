package com.aashdit.digiverifier.config.candidate.repository;

import com.aashdit.digiverifier.config.candidate.model.CandidateCafEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateCafEducationRepository extends JpaRepository<CandidateCafEducation, Long> {

    List<CandidateCafEducation> findAllByCandidateCandidateCode(String candidateCode);

//    @Query(value = "select distinct (color.colorName) from CandidateCafEducation where candidate.candidateCode =:candidateCode")
//    List<String> findDistinctColors(@Param("candidateCode") String candidateCode);

    List<CandidateCafEducation> findAllByCandidateCandidateId(Long candidateId);

//    @Modifying
//    void deleteAllByCandidateConventionalCandidateId(Long conventionalCandiateId);
    @Modifying
    void deleteAllByCandidateConventionalRequestId(Long conventionalRequestID);

    void deleteById(Long id);
}
