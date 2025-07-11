package com.aashdit.digiverifier.vendorcheck.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConventionalExperienceDto {

    //    private Long Id;
    private String employmentType;
    private String duration;
    private String designation;
    private String candidateEmployerName;
    private String inputDateOfJoining;
    private String inputDateOfExit;
    private String uan;
    private String employeeCode;

    private String hrName;

    private String hrContactNumber;

    private String hrEmailId;

    private String superiorName;

    private String superiorContactNumber;

    private String superiorEmailID;

    private String superiorDesignation;

    private String lastSalary;

    private String grossSalary;

    private String insufficiencyRemarks;

    private String isSuspect;

    private String reasonForSuspect;

    private String officeAddress;
}
