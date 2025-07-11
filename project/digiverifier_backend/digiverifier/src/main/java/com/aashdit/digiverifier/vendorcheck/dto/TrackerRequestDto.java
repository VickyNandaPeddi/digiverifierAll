package com.aashdit.digiverifier.vendorcheck.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TrackerRequestDto<t> {

    private String fromDate;

    private String toDate;

    private String userId;

    private List<t> trackerDtoList;

}
