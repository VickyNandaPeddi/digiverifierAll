package com.aashdit.digiverifier.vendorcheck.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurgeResponseDto {
    private String applicantId;

    private String name;

    private String createdOn;

    private String dateOfBirth;

    private String purgedDate;

}
