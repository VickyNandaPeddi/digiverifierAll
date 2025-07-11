package com.aashdit.digiverifier.vendorcheck.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class PurgeDto {
    private List<String> requestIds;
}
