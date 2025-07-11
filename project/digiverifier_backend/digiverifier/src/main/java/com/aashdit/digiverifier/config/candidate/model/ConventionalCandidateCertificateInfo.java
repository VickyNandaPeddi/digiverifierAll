package com.aashdit.digiverifier.config.candidate.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "t_dgv_conventional_candidate_certificate_info")
public class ConventionalCandidateCertificateInfo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_name", nullable = true)
    private String courseName;

    @Column(name = "course_start_date", nullable = true)
    private String courseStartDate;

    @Column(name = "course_completion_date", nullable = true)
    private String courseCompletionDate;

    @Column(name = "institute_name", nullable = true)
    private String instituteName;

    @Column(name = "institute_contact_number", nullable = true)
    private String instituteContactNumber;

    @Column(name = "institute_email_id", nullable = true)
    private String instituteEmailID;

    @Column(name = "prof_certificate_documents", nullable = true)
    private String profCertificateDocuments;

    @Column(name = "created_on", nullable = true)
    private Date createdOn;

    @Column(name = "created_by", nullable = true)
    private String createdBy;


    @Column(name = "updated_on", nullable = true)
    private Date updatedOn;

    @Column(name = "updated_by", nullable = true)
    private String updatedBy;

    @Column(name = "request_id", nullable = false)
    private Long requestId;
}
