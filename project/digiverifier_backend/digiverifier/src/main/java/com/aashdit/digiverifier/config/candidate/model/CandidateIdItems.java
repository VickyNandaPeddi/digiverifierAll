package com.aashdit.digiverifier.config.candidate.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "t_dgv_conventional_candidate_id_items")
public class CandidateIdItems implements Serializable {

    private static final long serialVersionUID = -2084642723714482L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "items_id")
    private Long candidateItemsId;

    @Column(name = "candidate_id")
    private String candidateId;

    @Column(name = "created_on")
    private Date createdOn;

    @Column(name = "id_number")
    private String idNumber;

    @Column(name = "id_holder_name")
    private String idHolderName;

    @Column(name = "id_holder_dob")
    private String idHolderDob;

    @Column(name = "id_holder_issuedate")
    private String idHolderIssueDate;

    @Column(name = "id_type")
    private String idType;

    @Column(name = "document_id")
    private String documentId;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "country")
    private String country;

    @Column(name = "expiryDate")
    private String expiryDate;

}
