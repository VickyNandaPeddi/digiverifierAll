package com.aashdit.digiverifier.vendorcheck.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SubmittedCandidates {

    private String candidateId;
    private String psNo;
    private String name;
    private String requestId;
    private String requestType;
    private String vendorId;
    private Integer applicantId;
    private String status;
    private String createdBy;

    private Date createdOn;

    private String reportResponse;

    private List<LicheckRequiredResponseDto> licheckRequiredResponseDtos;


}
