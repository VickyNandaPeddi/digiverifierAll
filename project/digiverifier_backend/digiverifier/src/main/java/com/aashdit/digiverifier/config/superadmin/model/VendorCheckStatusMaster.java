package com.aashdit.digiverifier.config.superadmin.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "t_dgv_vendor_checkstatus_master")
public class VendorCheckStatusMaster implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7858395421908518082L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vendor_checkstatus_master_id")
    private Long vendorCheckStatusMasterId;

    @Column(name = "checkstatus_name")
    private String checkStatusName;

    @Column(name = "checkstatus_code")
    private String checkStatusCode;

    @Column(name = "created_on")
    private Date createdOn;

    @Column(name = "vendor_check_status")
    private Boolean VendorCheckStatus;


}
