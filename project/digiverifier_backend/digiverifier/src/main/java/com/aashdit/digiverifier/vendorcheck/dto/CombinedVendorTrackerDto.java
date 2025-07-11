package com.aashdit.digiverifier.vendorcheck.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CombinedVendorTrackerDto {

    private List<VendorTrackerDto> candidateTrackerDtos;

    private List<InsufficiencyTrakerDto> insufficiencyTrakerDtos;
}
