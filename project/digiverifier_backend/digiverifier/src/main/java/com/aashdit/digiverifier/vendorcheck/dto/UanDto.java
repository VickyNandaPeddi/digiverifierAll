package com.aashdit.digiverifier.vendorcheck.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class UanDto {

    private String uanNo;

    private String remarks;

    private String colorCode;

    private String sourceId;

    private String requestId;

    //below fields for response as well

    private String createdBy;

    private String checkName;

    private String precisedUrl;

    private String documentName;

    private String pathKey;
}
