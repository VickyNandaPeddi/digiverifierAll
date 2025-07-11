package com.aashdit.digiverifier.config.candidate.model;

import com.aashdit.digiverifier.common.model.Content;
import com.aashdit.digiverifier.config.admin.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "t_dgv_candidate_resume_upload")
public class CandidateResumeUpload implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7795148256100383688L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "candidate_resume_upload_id")
    private Long candidateResumeUploadId;

//    @Type(type = "org.hibernate.type.BinaryType")
    @Column(name = "candidate_resume", columnDefinition = "BLOB")
    private byte[] candidateResume;

    @OneToOne
    @JoinColumn(name = "content_id", insertable = false, updatable = false)
    private Content content;

    @Column(name = "content_id")
    private Long contentId;

    @NotNull
    @OneToOne
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_on")
    private Date createdOn;

    @ManyToOne
    @JoinColumn(name = "last_updated_by")
    private User lastUpdatedBy;

    @Column(name = "last_updated_on")
    private Date lastUpdatedOn;
}
