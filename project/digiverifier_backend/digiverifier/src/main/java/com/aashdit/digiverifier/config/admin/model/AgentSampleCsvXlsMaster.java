package com.aashdit.digiverifier.config.admin.model;

import com.aashdit.digiverifier.config.superadmin.model.Organization;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;

import java.io.Serializable;
import java.sql.Types;
import java.util.Date;

@Data
@Entity
@Table(name = "t_dgv_agent_sample_csv_xls_master")
public class AgentSampleCsvXlsMaster implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -3891677023404920940L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agent_sample_id")
    private Long agentSampleId;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @JdbcTypeCode(Types.LONGVARBINARY)
    @Column(name = "agent_sample_csv", columnDefinition = "BLOB")
    private byte[] agentSampleCsv;

    @JdbcTypeCode(Types.LONGVARBINARY)
    @Column(name = "agent_sample_xls", columnDefinition = "BLOB")
    private byte[] agentSampleXls;

    @Column(name = "uploaded_timestamp")
    private Date uploadedTimestamp;

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
