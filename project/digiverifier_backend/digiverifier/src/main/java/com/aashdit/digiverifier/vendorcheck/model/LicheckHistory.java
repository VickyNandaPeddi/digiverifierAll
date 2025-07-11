package com.aashdit.digiverifier.vendorcheck.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "t_dgv_conventional_licheck_history")
public class LicheckHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long Id;

    private Long requestId;

    private String candidateId;

    private String requestType;

    private Long checkUniqueId;

    private String checkStatus;

    private String CheckName;

    private Date createdOn;

    private String createdBy;

    private String candidateStatus;

    @Lob
    private String checkResponse;
}
