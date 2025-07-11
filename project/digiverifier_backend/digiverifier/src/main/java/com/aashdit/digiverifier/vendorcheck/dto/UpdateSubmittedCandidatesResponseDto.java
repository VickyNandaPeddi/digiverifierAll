package com.aashdit.digiverifier.vendorcheck.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class UpdateSubmittedCandidatesResponseDto {
    //    @JsonProperty("candidateId")
    private String CandidateID;

    @JsonProperty("PSNO")
    private String PSNO;

    @JsonProperty("Name")
    private String Name;

    @JsonProperty("RequestID")
    private String RequestID;

    @JsonProperty("VendorName")
    private String VendorName;

    @JsonProperty("liChecksDetails")
    private List<liChecksDetails> liChecksDetails;

    @JsonProperty("liReportDetails")
    private List<liReportDetails> liReportDetails;


}
