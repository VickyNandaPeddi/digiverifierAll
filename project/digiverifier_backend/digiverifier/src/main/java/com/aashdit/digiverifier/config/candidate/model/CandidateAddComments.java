package com.aashdit.digiverifier.config.candidate.model;

import com.aashdit.digiverifier.config.admin.model.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;

import java.io.Serializable;
import java.sql.Types;
import java.util.Date;

@Data
@Entity
@Table(name = "t_dgv_candidate_caf_addcomments")
public class CandidateAddComments implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4386254957126811179L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "candidate_addcommen_id")
    private Long candidateAddcommentId;

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @Column(name = "Comments")
    private String comments;
    @JdbcTypeCode(Types.LONGVARBINARY)
    @Column(name = "attachments_documents", columnDefinition = "LONGBLOB")
    private byte[] attachments;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_on")
    private Date createdOn;


}
