package com.aashdit.digiverifier.vendorcheck.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
@Getter
@Setter
public class VendorTrackerDto {
    private String checkUniqueId;
    private String requestId;
    private String candidateId;
    private String candidateName;
    private String checkName;
    private String checkRemarks;
    private String statusCode;
    private String clientCheckStatus;
    private String caseReceivedOn;
    private String ageing;
    private String stopChecks;
    private String completedDate;
    private String insuffRaisedDate;
    private String insuffClearedDate;
    private String insuffTat;


    public VendorTrackerDto(String checkUniqueId, String requestId, String candidateId, String candidateName,
                            String checkName, String statusCode, String clientCheckStatus,
                            String caseReceivedOn, String ageing, String stopChecks, String completedDate) {
        this.checkUniqueId = checkUniqueId;
        this.requestId = requestId;
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.checkName = checkName;
        this.statusCode = statusCode;
        this.clientCheckStatus = clientCheckStatus;   // set here
        this.caseReceivedOn = caseReceivedOn;
        this.ageing = ageing;
        this.stopChecks = stopChecks;
        this.completedDate = completedDate;
    }
}
