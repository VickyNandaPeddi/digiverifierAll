package com.aashdit.digiverifier.vendorcheck.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConventionalPassportDto {

    private String passportNumber;
    private String nameInPassport;
    private String dateOfBirthInPassport;
    private String country;
    private String dateOfIssue;
    private String expiryDate;
}
