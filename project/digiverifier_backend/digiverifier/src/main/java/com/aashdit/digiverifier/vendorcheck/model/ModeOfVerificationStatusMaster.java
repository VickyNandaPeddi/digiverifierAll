package com.aashdit.digiverifier.vendorcheck.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "t_dgv_conventional_mode_of_verification_master")
public class ModeOfVerificationStatusMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long modeTypeCode;
    private String modeOfVerification;
}
