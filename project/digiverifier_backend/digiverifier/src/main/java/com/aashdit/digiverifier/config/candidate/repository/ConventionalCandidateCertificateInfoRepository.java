package com.aashdit.digiverifier.config.candidate.repository;

import com.aashdit.digiverifier.config.candidate.model.ConventionalCandidateCertificateInfo;
import com.aashdit.digiverifier.config.candidate.model.ConventionalCandidateReferenceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConventionalCandidateCertificateInfoRepository extends JpaRepository<ConventionalCandidateCertificateInfo, Long> {

    Boolean existsByRequestId(Long requestId);

    void deleteAllByRequestId(Long conventionalRequestId);


    List<ConventionalCandidateCertificateInfo> findByRequestId(Long conventionalRequestId);
}
