package com.aashdit.digiverifier.vendorcheck.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CombinedTrackerDto {

    private List<CandidateTrackerDto> candidateTrackerDtos;

    private List<InsufficiencyTrakerDto> insufficiencyTrakerDtos;
}
