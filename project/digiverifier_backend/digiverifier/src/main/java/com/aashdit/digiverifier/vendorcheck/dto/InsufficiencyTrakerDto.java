package com.aashdit.digiverifier.vendorcheck.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class InsufficiencyTrakerDto {

    private Long requestId;
    private String checkUniqueId;
    private String checkName;
    private String InsuffCreatedOn;
    private String inprogressCreatedOn;
    private String InsufficiencyEta;
}
