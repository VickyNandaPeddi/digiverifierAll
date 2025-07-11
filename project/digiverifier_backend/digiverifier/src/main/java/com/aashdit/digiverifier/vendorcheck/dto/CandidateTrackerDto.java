package com.aashdit.digiverifier.vendorcheck.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CandidateTrackerDto {

    private String requestId;
    private String candidateId;
    private String psNo;
    private String name;
    private String statusCode;
    private String verificationStatus;
    private String fastTrack;
    private String caseInitiatedOn;
    private String bgvInitiatedOn;
    private String ageing;
    private String eta;
    private String currentEmploymentInitiationDate;
    private String finalReportDispatchedDate;
    private List<String> completedChecks;
    private List<String> pendingChecks;
    private List<String> insufficencyChecks; // New field
    private List<String> stopChecks; // New field
    private List<String> nottriggeredchecks; // New field

    private List<InsufficiencyTrakerDto> insufficiencyTrakerDtos;

    public CandidateTrackerDto(
            String requestId,
            String candidateId,
            String psNo,
            String name,
            String statusCode,
            String verificationStatus,
            String fastTrack,
            String caseInitiatedOn,
            String bgvInitiatedOn,
            String ageing,
            String eta,
            String currentEmploymentInitiationDate,
            String finalReportDispatchedDate,
            String completedChecks,
            String pendingChecks,
            String insufficencyChecks,  // Added for insufficencyChecks
            String stopChecks,          // Added for stopChecks
            String nottriggeredchecks  // Added for nottriggeredchecks
    ) {
        this.requestId = requestId;
        this.candidateId = candidateId;
        this.psNo = psNo;
        this.name = name;
        this.statusCode = statusCode;
        this.verificationStatus = verificationStatus;
        this.fastTrack = fastTrack;
        this.caseInitiatedOn = caseInitiatedOn;
        this.bgvInitiatedOn = bgvInitiatedOn;
        this.ageing = ageing;
        this.eta = eta;
        this.currentEmploymentInitiationDate = currentEmploymentInitiationDate;
        this.finalReportDispatchedDate = finalReportDispatchedDate;
        // Split comma-separated checks into lists for completed and pending checks
        this.completedChecks = splitChecks(completedChecks);
        this.pendingChecks = splitChecks(pendingChecks);
        this.insufficencyChecks = splitChecks(insufficencyChecks);  // Split new field
        this.stopChecks = splitChecks(stopChecks);  // Split new field
        this.nottriggeredchecks = splitChecks(nottriggeredchecks);  // Split new field
    }

    // Helper method to split comma-separated checks into a list
    private List<String> splitChecks(String checks) {
        if (checks != null && !checks.isEmpty()) {
            return Arrays.asList(checks.split(","));
        }
        return Collections.emptyList(); // Return an empty list if checks is null or empty
    }
}
