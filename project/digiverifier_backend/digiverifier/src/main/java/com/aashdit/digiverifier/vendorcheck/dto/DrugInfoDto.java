package com.aashdit.digiverifier.vendorcheck.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DrugInfoDto {
    private String name;
    private String contactNumber;
    private String sampleCollectionDate;
    private String remarks;
    private String houseNumber;
    private String streetAddress;
    private String city;
    private String state;
    private String country;
    private String pincode;
    private String prominentLandmark;
}
