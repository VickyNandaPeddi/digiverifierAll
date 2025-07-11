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
@Table(name = "t_dgv_candidate_caf_relationship")
public class CandidateCafRelationship implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -6629477214388045075L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "candidate_relationship_id")
    private Long candidateRelationshipId;

    @ManyToOne
    @JoinColumn(name = "candidate_status_id")
    private CandidateStatus candidateStatus;

    @Column(name = "candidate_relationship")
    private String candidateRelationship;


    @JdbcTypeCode(Types.LONGVARBINARY)
    @Column(name = "document_uploaded", columnDefinition = "BLOB")
    private byte[] documentUploaded;

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

    @Column(name = "rent_type")
    private String rentType;

}
