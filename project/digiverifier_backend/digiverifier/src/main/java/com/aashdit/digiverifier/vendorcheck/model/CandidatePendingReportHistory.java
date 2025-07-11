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
@Table(name = "t_dgv_conventional_candidate_pending_report_history")
public class CandidatePendingReportHistory {
    @Id
    @Column(name = "request_Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_on")
    private Date createdOn;


    @Column(name = "request_type")
    private String RequestType;

    @Lob
    private String reportStatusReponse;


    @OneToOne
    private StatusMaster newstatus;

    @OneToOne
    private StatusMaster oldstatus;


}
