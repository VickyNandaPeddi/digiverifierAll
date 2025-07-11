package com.aashdit.digiverifier.vendorcheck.model;

import com.aashdit.digiverifier.config.admin.model.User;
import com.aashdit.digiverifier.config.candidate.model.StatusMaster;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "t_dgv_conventional_candidate_request")
public class ConventionalVendorCandidatesSubmitted {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidate_id")
    private String candidateId;
    @Column(name = "ps_no")
    private String psNo;
    @Column(name = "name")
    private String name;
    @Column(name = "request_id", unique = true)
    private String requestId;
    @Column(name = "request_type")
    private String requestType;
    @Column(name = "vendor_id")
    private String vendorId;
    @Column(name = "applicant_id")
    private Integer applicantId;
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
    @Column(name = "created_on")
    private Date createdOn;

    //status from vendor check master
    @OneToOne
    @JoinColumn(name = "status")
    private StatusMaster status;

    @Column(name = "verification_status")
    private String verificationStatus;


    @Column(name = "old_request_type")
    private String oldRequestType;

    @Column(name = "fast_track")
    private String fastTrack;

    @Column(name = "stop_check_recived_date")
    private String stopCheckRecivedDate;


    @Column(name = "fetch_licheck_and_candidate_data")
    private Boolean fetchLicheckAndCandidateData;

    @Column(name="pending_InterimReport_OnBasicChecks_Flag")
    private String pendingInterimReportOnBasicChecksFlag;

    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "updated_on")
    private Date updatedOn;

    @Column(name = "purged_date")
    private Date purgedDate;

}
