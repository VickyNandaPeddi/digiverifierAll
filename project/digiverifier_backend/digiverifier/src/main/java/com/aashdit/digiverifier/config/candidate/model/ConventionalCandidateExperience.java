package com.aashdit.digiverifier.config.candidate.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table(name = "t_dgv_conventional_candidate_caf_experience")
public class ConventionalCandidateExperience implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long Id;
    @Column(name = "employment_type")
    private String employmentType;

    @NotNull
    @Column(name = "candidate_cafexperienc_id")
    private Long candidateCafExperience;

    @Column(name = "conventionalCandidateId")
    private String conventionalCandidateId;

    @Column(name = "conventional_requestid")
    private Long conventionalRequestId;

    @Column(name = "duration")
    private String duration;

    @Column(name = "designation")
    private String designation;

    @Column(name = "employee_code")
    private String employeeCode;

    @Column(name = "hr_name")
    private String hrName;

    @Column(name = "hr_contact_number")
    private String hrContactNumber;

    @Column(name = "hr_email_id")
    private String hrEmailId;

    @Column(name = "superior_name")
    private String superiorName;

    @Column(name = "superior_contact_number")
    private String superiorContactNumber;

    @Column(name = "superior_email_id")
    private String superiorEmailID;

    @Column(name = "superior_designation")
    private String superiorDesignation;

    @Column(name = "last_salary")
    private String lastSalary;

    @Column(name = "gross_salary")
    private String grossSalary;

    @Column(name = "insufficiency_remarks", length = 3000)
    private String insufficiencyRemarks;
    @Column(name = "IsSuspect")
    private String isSuspect;
    @Column(name = "reason_for_suspect")
    private String reasonForSuspect;

    @Column(name = "office_address")
    private String officeAddress;

}
