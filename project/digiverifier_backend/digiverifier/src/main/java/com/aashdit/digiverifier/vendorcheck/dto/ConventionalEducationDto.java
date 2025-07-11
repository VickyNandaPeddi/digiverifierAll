package com.aashdit.digiverifier.vendorcheck.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConventionalEducationDto {

//    private Long Id;

    private Long conventionalCandidateId;

    private String educationType;

    private String degreeType;


    private String startDate;

    private String endDate;

    private String insufficiecyRemarks;


    private String qualificationName;


    private String schoolOrCollegeName;

    private String boardOrUniversityName;

    private String isSuspect;

    private String reasonForSuspect;


}
