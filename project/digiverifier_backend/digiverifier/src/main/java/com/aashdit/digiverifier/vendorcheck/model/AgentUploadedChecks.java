package com.aashdit.digiverifier.vendorcheck.model;


import com.aashdit.digiverifier.config.superadmin.model.Source;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "t_dgv_conventional_agent_uploaded_checks")
public class AgentUploadedChecks {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    @Column(name = "request_id")
    private String requestID;

    @Column(name = "uan_no")
    private String uanNo;

    @OneToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "source")
    private Source source;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "color_code")
    private String colorCode;

    @Column(name = "document_name")
    private String documentName;
    @Lob
    @Column(name = "path_key")
    private String pathKey;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "created_on")
    private Date createdOn;
    @Column(name = "updated_by")
    private String updatedBy;
    @Column(name = "updated_on")
    private Date updatedOn;
}

