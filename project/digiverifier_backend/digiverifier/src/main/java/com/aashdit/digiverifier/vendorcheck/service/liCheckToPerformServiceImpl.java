/**
 *
 */
package com.aashdit.digiverifier.vendorcheck.service;

import com.aashdit.digiverifier.client.securityDetails.DigilockerClient;
import com.aashdit.digiverifier.common.ContentRepository;
import com.aashdit.digiverifier.common.enums.ContentCategory;
import com.aashdit.digiverifier.common.enums.ContentSubCategory;
import com.aashdit.digiverifier.common.enums.ContentType;
import com.aashdit.digiverifier.common.enums.FileType;
import com.aashdit.digiverifier.common.model.Content;
import com.aashdit.digiverifier.common.model.ServiceOutcome;
import com.aashdit.digiverifier.config.admin.dto.LegalProceedingsDTO;
import com.aashdit.digiverifier.config.admin.dto.VendorInitiatDto;
import com.aashdit.digiverifier.config.admin.dto.VendorUploadChecksDto;
import com.aashdit.digiverifier.config.admin.dto.VendorcheckdashbordtDto;
import com.aashdit.digiverifier.config.admin.model.CriminalCheck;
import com.aashdit.digiverifier.config.admin.model.User;
import com.aashdit.digiverifier.config.admin.model.VendorChecks;
import com.aashdit.digiverifier.config.admin.model.VendorUploadChecks;
import com.aashdit.digiverifier.config.admin.repository.CriminalCheckRepository;
import com.aashdit.digiverifier.config.admin.repository.UserRepository;
import com.aashdit.digiverifier.config.admin.repository.VendorChecksRepository;
import com.aashdit.digiverifier.config.admin.repository.VendorUploadChecksRepository;
import com.aashdit.digiverifier.config.admin.service.UserService;
import com.aashdit.digiverifier.config.candidate.dto.ExecutiveSummaryDto;
import com.aashdit.digiverifier.config.candidate.model.*;
import com.aashdit.digiverifier.config.candidate.repository.*;
import com.aashdit.digiverifier.config.candidate.service.CandidateService;
import com.aashdit.digiverifier.config.candidate.service.CandidateServiceImpl;
import com.aashdit.digiverifier.config.superadmin.Enum.ConventionalVerificationStatus;
import com.aashdit.digiverifier.config.superadmin.Enum.ReportType;
import com.aashdit.digiverifier.config.superadmin.dto.DashboardDto;
import com.aashdit.digiverifier.config.superadmin.model.*;
import com.aashdit.digiverifier.config.superadmin.repository.OrganizationRepository;
import com.aashdit.digiverifier.config.superadmin.repository.SourceRepository;
import com.aashdit.digiverifier.config.superadmin.repository.VendorCheckStatusMasterRepository;
import com.aashdit.digiverifier.config.superadmin.repository.VendorMasterNewRepository;
import com.aashdit.digiverifier.config.superadmin.service.OrganizationService;
import com.aashdit.digiverifier.config.superadmin.service.PdfService;
import com.aashdit.digiverifier.config.superadmin.service.ReportService;
import com.aashdit.digiverifier.email.dto.EmailProperties;
import com.aashdit.digiverifier.globalConfig.EnvironmentVal;
import com.aashdit.digiverifier.utils.*;
import com.aashdit.digiverifier.utils.DateUtil;
import com.aashdit.digiverifier.vendorcheck.dto.*;
import com.aashdit.digiverifier.vendorcheck.model.*;
import com.aashdit.digiverifier.vendorcheck.repository.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.Date;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.aashdit.digiverifier.digilocker.service.DigilockerServiceImpl.DIGIVERIFIER_DOC_BUCKET_NAME;

/**
 * @author ${Nanda Kishore}
 */
@Service
@Slf4j
public class liCheckToPerformServiceImpl<T> implements liCheckToPerformService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DigilockerClient digilockerClient;
    @Autowired
    private ConventionalCandidateRepository conventionalCandidateRepository;
    @Autowired
    FileUtil fileUtil;
    @Autowired
    AWSConfig awsConfig;
    @Autowired
    AmazonS3 s3Client;
    @Autowired
    private AwsUtils awsUtils;
    @Autowired
    private LiCheckToPerformRepository liCheckToPerformRepository;
    @Autowired
    private ConventionalCandidatesSubmittedRepository conventionalCandidatesSubmittedRepository;
    @Autowired
    private ConventionalCandidateDocumentInfoRepository conventionalCandidateDocumentInfoRepository;
    @Autowired
    EnvironmentVal environmentVal;
    @Autowired
    VendorCheckStatusMasterRepository vendorCheckStatusMasterRepository;

    @Autowired
    CandidateRepository candidateRepository;
    @Autowired
    private PdfService pdfService;

    @Autowired
    VendorChecksRepository vendorChecksRepository;
    @Autowired
    StatusMasterRepository statusMasterRepository;
    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    private ContentRepository contentRepository;
    @Autowired
    private VendorUploadChecksRepository vendorUploadChecksRepository;
    @Autowired
    private ReportService reportService;
    @Autowired
    CandidateService candidateService;
    @Autowired
    ConventionalAttributesMasterRepository conventionalAttributesMasterRepository;
    @Autowired
    LicheckHistoryRepository licheckHistoryRepository;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    UserService userService;
    @Autowired
    private ModeOfVerificationStatusMasterRepository modeOfVerificationStatusMasterRepository;
    @Autowired
    EmailSentTask emailSentTask;
    @Autowired
    ConventionalCandidateDrugInfoRepository conventionalCandidateDrugInfoRepository;
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
    @Autowired
    ConventionalVendorCandidatesSubmittedRepository conventionalVendorCandidatesSubmittedRepository;

    @Autowired
    ConventionalCandidateReferenceInfoRepository conventionalCandidateReferenceInfoRepository;

    @Autowired
    ConventionalCandidateExperienceRepository conventionalCandidateExperienceRepository;
    @Autowired
    ConventionalCafCandidateEducationRepository conventionalCafCandidateEducationRepository;
    @Autowired
    ConventionCafAddressRepository conventionCafAddressRepository;
    @Autowired
    CandidateCafExperienceRepository candidateCafExperienceRepository;

    @Autowired
    CandidateCafEducationRepository candidateCafEducationRepository;
    @Autowired
    CandidateIdItemsRepository candidateIdItemsRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    @Lazy
    private EmailProperties emailProperties;
    @Autowired
    CandidateCafAddressRepository candidateCafAddressRepository;
    @Autowired
    CriminalCheckRepository criminalCheckRepository;
    @Autowired
    CandidateVerificationStateRepository candidateVerificationStateRepository;
    @Autowired
    VendorMasterNewRepository vendorMasterNewRepository;

    SimpleDateFormat dateFormater = new SimpleDateFormat("dd/MM/yyyy");
    @Autowired
    QualificationMasterRepository qualificationMasterRepository;
    @Autowired
    CandidateStatusRepository candidateStatusRepository;
    @Autowired
    CandidateServiceImpl candidateServiceImpl;

    @Autowired
    private ConventionalCandidateCertificateInfoRepository conventionalCandidateCertificateInfoRepository;

    @Transactional
    public ServiceOutcome<String> updateBgvCheckRowwiseonProgress(Long requestID, Long checkUniqueId) {
        List<liReportDetails> liReportDetails = new ArrayList<>();
        ServiceOutcome<String> objectServiceOutcome = new ServiceOutcome<>();
        String responedata;
        ConventionalVendorCandidatesSubmitted candidatesSubmitted = null;
        ConventionalVendorliChecksToPerform byCheckUniqueId = null;
        try {
            ArrayList<UpdateSubmittedCandidatesResponseDto> updateSubmittedCandidatesResponseDtos = new ArrayList<>();
            ArrayList<liChecksDetails> liChecksDetails = new ArrayList<>();
            UpdateSubmittedCandidatesResponseDto conventionalVendorCandidatesSubmitted = new UpdateSubmittedCandidatesResponseDto();
            candidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(String.valueOf(requestID));
            conventionalVendorCandidatesSubmitted.setCandidateID(String.valueOf(candidatesSubmitted.getCandidateId()));
            conventionalVendorCandidatesSubmitted.setName(candidatesSubmitted.getName());
            conventionalVendorCandidatesSubmitted.setPSNO(candidatesSubmitted.getPsNo());
            conventionalVendorCandidatesSubmitted.setRequestID(candidatesSubmitted.getRequestId());
            conventionalVendorCandidatesSubmitted.setVendorName(candidatesSubmitted.getVendorId());
            com.aashdit.digiverifier.vendorcheck.dto.liReportDetails liReportDetails1 = new liReportDetails();
            liReportDetails1.setReportFileExtention("");
            liReportDetails1.setReportFileName("");
            liReportDetails1.setReportAttachment("");
            liReportDetails1.setReportStatus("");
            liReportDetails1.setReportType("");
            byCheckUniqueId = liCheckToPerformRepository.findByCheckUniqueId(checkUniqueId);
            log.info("Sent Check to   Ltm With Status   " + byCheckUniqueId.getCheckStatus().getCheckStatusCode() + "  for the check Unique Id " + byCheckUniqueId.getCheckUniqueId());
            com.aashdit.digiverifier.vendorcheck.dto.liChecksDetails liChecksDetails1 = new liChecksDetails();
            liChecksDetails1.setCheckCode(Math.toIntExact(byCheckUniqueId.getCheckCode()));
            liChecksDetails1.setCheckName(byCheckUniqueId.getCheckName());
            liChecksDetails1.setCheckRemarks(byCheckUniqueId.getCheckRemarks());
            liChecksDetails1.setCheckStatus(Math.toIntExact((byCheckUniqueId.getCheckStatus().getVendorCheckStatusMasterId())));
            String pattern = "dd/MM/yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
            liChecksDetails1.setCompletedDate(date);
            liChecksDetails1.setModeOfVerficationPerformed(byCheckUniqueId.getModeOfVerificationRequired());
            liChecksDetails1.setModeOfVerficationRequired(byCheckUniqueId.getModeOfVerificationRequired());
            liChecksDetails.add(liChecksDetails1);
            liReportDetails1.setVendorReferenceID(String.valueOf(candidatesSubmitted.getApplicantId()));
            liReportDetails.add(liReportDetails1);
            conventionalVendorCandidatesSubmitted.setLiReportDetails(liReportDetails);
            conventionalVendorCandidatesSubmitted.setLiChecksDetails(liChecksDetails);
            updateSubmittedCandidatesResponseDtos.add(conventionalVendorCandidatesSubmitted);
            //hitting the update request to third party api
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", environmentVal.getMtGrantType());
            map.add("username", environmentVal.getMtUsername());
            map.add("password", environmentVal.getMtPassword());
            HttpHeaders tokenHeader = new HttpHeaders();
            tokenHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            ResponseEntity<String> responseEntity = null;
            HttpEntity<MultiValueMap<String, String>> requestBodyFormUrlEncoded = new HttpEntity<>(map, tokenHeader);
            responseEntity = restTemplate.postForEntity(environmentVal.getConventionalVendorToken(), requestBodyFormUrlEncoded, String.class);
            JSONObject tokenObject = new JSONObject(responseEntity.getBody());
            String access_token = tokenObject.getString("access_token");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + access_token);
            headers.set("Content-Type", "application/json");
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<List<UpdateSubmittedCandidatesResponseDto>> liCheckDtoHttpEntity = new HttpEntity<>(updateSubmittedCandidatesResponseDtos, headers);
            ResponseEntity<String> icheckRepsonse = restTemplate.exchange(environmentVal.getConventionalUpdateBgvCheckStatusRowwise(), HttpMethod.POST, liCheckDtoHttpEntity, String.class);
            log.info("Response recived from Ltm for CheckUnique Id" + byCheckUniqueId.getCheckUniqueId() + "-- Status  ---" + byCheckUniqueId.getCheckStatus().getCheckStatusCode() + "  --  " + icheckRepsonse);
            int statusCodeValue = icheckRepsonse.getStatusCodeValue();
            responedata = icheckRepsonse.getBody();
            if (statusCodeValue == 200) {
                updateLiCheckHistory(byCheckUniqueId, candidatesSubmitted, String.valueOf(icheckRepsonse));
                objectServiceOutcome.setData(responedata);
                objectServiceOutcome.setMessage(icheckRepsonse.getBody());
                objectServiceOutcome.setStatus(String.valueOf(icheckRepsonse.getStatusCode()));
                objectServiceOutcome.setOutcome(true);
            }
        } catch (HttpClientErrorException e) {
            responedata = e.getResponseBodyAsString();
            updateLiCheckHistory(byCheckUniqueId, candidatesSubmitted, responedata);
            objectServiceOutcome.setData(responedata);
            objectServiceOutcome.setMessage(e.getResponseBodyAsString());
            objectServiceOutcome.setStatus(String.valueOf(e.getStatusCode()));
            objectServiceOutcome.setOutcome(false);
        } catch (Exception e) {
            objectServiceOutcome.setData(null);
            objectServiceOutcome.setMessage(e.getMessage());
            objectServiceOutcome.setOutcome(false);
            objectServiceOutcome.setStatus(String.valueOf(e.getMessage()));
            log.error("updateBgvCheckRowwiseonProgress Exception" + e.getMessage());
        }
        return objectServiceOutcome;
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceOutcome<String> UpdateBGVCheckStatusRowwiseByAgentLogin(CheckUniqueIdRequest checkUniqueIds) throws Exception {
        List<liReportDetails> liReportDetails = new ArrayList<>();
        String responedata = "";
        ServiceOutcome<String> updateRowwiseStatusServiceOutcome = new ServiceOutcome<>();
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseEntity<String> icheckRepsonse = null;
        ConventionalVendorliChecksToPerform byCheckUniqueId = null;
        ConventionalVendorCandidatesSubmitted conventinalCandidate = null;
        try {
            if (!checkUniqueIds.getCheckUniqueIds().isEmpty()) {
                for (String checkUniqueId : checkUniqueIds.getCheckUniqueIds()) {
                    try {
                        ArrayList<UpdateSubmittedCandidatesResponseDto> updateSubmittedCandidatesResponseDtos = new ArrayList<>();
                        ArrayList<liChecksDetails> liChecksDetails = new ArrayList<>();
                        byCheckUniqueId = liCheckToPerformRepository.findByCheckUniqueId(Long.valueOf(checkUniqueId));
                        VendorcheckdashbordtDto vendorcheckdashbordtDto = new VendorcheckdashbordtDto();
                        vendorcheckdashbordtDto.setStatus(String.valueOf(byCheckUniqueId.getCheckStatus().getVendorCheckStatusMasterId()));
                        vendorcheckdashbordtDto.setRemarks(byCheckUniqueId.getCheckRemarks());
                        vendorcheckdashbordtDto.setVendorcheckId(byCheckUniqueId.getVendorChecks().getVendorcheckId());
                        VendorCheckStatusMaster byVendorCheckStatusMasterId = null;
                        if (vendorcheckdashbordtDto.getStatus() != null) {
                            byVendorCheckStatusMasterId = vendorCheckStatusMasterRepository.findByVendorCheckStatusMasterId(Long.valueOf(vendorcheckdashbordtDto.getStatus()));
                        }
                        VendorChecks vendorCheckss = vendorChecksRepository.findByVendorcheckId(vendorcheckdashbordtDto.getVendorcheckId());
                        ConventionalVendorliChecksToPerform conventionalVendorliChecksToPerform = liCheckToPerformRepository.findByVendorChecksVendorcheckId(vendorCheckss.getVendorcheckId());
                        conventionalVendorliChecksToPerform.setModeOfVerificationPerformed(conventionalVendorliChecksToPerform.getModeOfVerificationPerformed());
                        liReportDetails liReportDetails1 = new liReportDetails();
                        conventinalCandidate = conventionalCandidatesSubmittedRepository.findByRequestId(conventionalVendorliChecksToPerform.getRequestId());
                        UpdateSubmittedCandidatesResponseDto conventionalVendorCandidatesSubmitted = new UpdateSubmittedCandidatesResponseDto();
                        conventionalVendorCandidatesSubmitted.setCandidateID(String.valueOf(conventinalCandidate.getCandidateId()));
                        conventionalVendorCandidatesSubmitted.setName(conventinalCandidate.getName());
                        conventionalVendorCandidatesSubmitted.setPSNO(conventinalCandidate.getPsNo());
                        conventionalVendorCandidatesSubmitted.setRequestID(conventinalCandidate.getRequestId());
                        conventionalVendorCandidatesSubmitted.setVendorName(conventinalCandidate.getVendorId());
                        Candidate candidate = candidateRepository.findByConventionalRequestId(Long.valueOf(conventionalVendorCandidatesSubmitted.getRequestID()));
                        liChecksDetails liChecksDetails1 = new liChecksDetails();
                        liChecksDetails1.setCheckCode(Math.toIntExact(conventionalVendorliChecksToPerform.getCheckCode()));
                        liChecksDetails1.setCheckName(conventionalVendorliChecksToPerform.getCheckName());
                        liChecksDetails1.setCheckRemarks(vendorcheckdashbordtDto.getRemarks());
                        liChecksDetails1.setCheckStatus(Integer.valueOf(vendorcheckdashbordtDto.getStatus()));
                        String pattern = "dd/MM/yyyy";
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                        String date = simpleDateFormat.format(new Date());
                        liChecksDetails1.setCompletedDate(date);
                        liChecksDetails1.setModeOfVerficationPerformed(conventionalVendorliChecksToPerform.getModeOfVerificationPerformed());
                        liChecksDetails1.setModeOfVerficationRequired(conventionalVendorliChecksToPerform.getModeOfVerificationRequired());
                        log.info("Sent Check to Ltm with Status - " + byVendorCheckStatusMasterId.getCheckStatusCode() + " - for Check Unique Id - " + conventionalVendorliChecksToPerform.getCheckUniqueId());
                        liChecksDetails.add(liChecksDetails1);
                        liReportDetails1.setVendorReferenceID(String.valueOf(conventinalCandidate.getApplicantId()));
                        if (liChecksDetails1.getCheckStatus() == 4 || liChecksDetails1.getCheckStatus() == 5) {
                            ServiceOutcome<String> stringServiceOutcome = generateDiscrepancyreport(candidate.getCandidateId(), ReportType.DISCREPANCY, "DONT", vendorCheckss.getVendorcheckId());
                            String tempReportPath = stringServiceOutcome.getStatus() != null ? stringServiceOutcome.getStatus() : null;
                            Content content = contentRepository.findByCandidateIdAndCreatedOn(candidate.getCandidateId());
                            String bucketName = content.getBucketName();
                            String path = content.getPath();
                            String[] split = path.split("/");
                            String filename = split[split.length - 1];
                            String fileExtension = filename.substring(filename.length() - 4, filename.length());
                            liReportDetails1.setReportFileExtention(fileExtension);
                            liReportDetails1.setReportFileName(filename);
                            try {
                                byte[] bytes = awsUtils.getbyteArrayFromS3(bucketName, tempReportPath);
                                String base64String = Base64.getEncoder().encodeToString(bytes);
                                liReportDetails1.setReportAttachment(base64String);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            liReportDetails1.setReportStatus(String.valueOf(liChecksDetails1.getCheckStatus()));
                            liReportDetails1.setReportType("1");
                            liReportDetails.add(liReportDetails1);
                        }
                        if (liChecksDetails1.getCheckStatus() == 6) {
                            ServiceOutcome<String> stringServiceOutcome = generateUnableToVerifiyReport(candidate.getCandidateId(), ReportType.INTERIM, "DONT");
                            String tempReportPath = stringServiceOutcome.getStatus() != null ? stringServiceOutcome.getStatus() : null;
                            Content content = contentRepository.findByCandidateIdAndCreatedOn(candidate.getCandidateId());
                            String bucketName = content.getBucketName();
                            String path = content.getPath();
                            String[] split = path.split("/");
                            String filename = split[split.length - 1];
                            String fileExtension = filename.substring(filename.length() - 4, filename.length());
                            liReportDetails1.setReportFileExtention(fileExtension);
                            liReportDetails1.setReportFileName(filename);
                            try {
                                byte[] bytes = awsUtils.getbyteArrayFromS3(bucketName, tempReportPath);
                                String base64String = Base64.getEncoder().encodeToString(bytes);
                                liReportDetails1.setReportAttachment(base64String);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            liReportDetails1.setReportStatus(String.valueOf(liChecksDetails1.getCheckStatus()));
                            liReportDetails1.setReportType("1");
                            liReportDetails.add(liReportDetails1);
                        }
                        conventionalVendorCandidatesSubmitted.setLiReportDetails(liReportDetails);
                        conventionalVendorCandidatesSubmitted.setLiChecksDetails(liChecksDetails);
                        updateSubmittedCandidatesResponseDtos.add(conventionalVendorCandidatesSubmitted);
                        // Hitting the update request to third-party API
                        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                        map.add("grant_type", environmentVal.getMtGrantType());
                        map.add("username", environmentVal.getMtUsername());
                        map.add("password", environmentVal.getMtPassword());
                        HttpHeaders tokenHeader = new HttpHeaders();
                        tokenHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                        ResponseEntity<String> responseEntity = null;
                        HttpEntity<MultiValueMap<String, String>> requestBodyFormUrlEncoded = new HttpEntity<>(map, tokenHeader);
                        responseEntity = restTemplate.postForEntity(environmentVal.getConventionalVendorToken(), requestBodyFormUrlEncoded, String.class);
                        JSONObject tokenObject = new JSONObject(responseEntity.getBody());
                        String access_token = tokenObject.getString("access_token");
                        HttpHeaders headers = new HttpHeaders();
                        headers.set("Authorization", "Bearer " + access_token);
                        headers.set("Content-Type", "application/json");
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<List<UpdateSubmittedCandidatesResponseDto>> liCheckDtoHttpEntity = new HttpEntity<>(updateSubmittedCandidatesResponseDtos, headers);
                        icheckRepsonse = restTemplate.exchange(environmentVal.getConventionalUpdateBgvCheckStatusRowwise(), HttpMethod.POST, liCheckDtoHttpEntity, String.class);
                        int statusCodeValue = icheckRepsonse.getStatusCodeValue();
                        responedata = icheckRepsonse.getBody();
                        log.info("Response Recived from Ltm for the CheckUnique id " + conventionalVendorliChecksToPerform.getCheckUniqueId() + " Status - " + byVendorCheckStatusMasterId.getCheckStatusCode() + " -- " + icheckRepsonse);
                        if (statusCodeValue == 200) {
                            updateLiCheckHistory(conventionalVendorliChecksToPerform, conventinalCandidate, String.valueOf(icheckRepsonse));
                            boolean pendingInterimReportOnBasicChecksFlag = setPendingInterimReportOnBasicChecksFlag(Long.valueOf(conventinalCandidate.getRequestId()));
                            if (pendingInterimReportOnBasicChecksFlag) {
                                ConventionalVendorCandidatesSubmitted pendingInterimReportOnBasicChecksFlagUpdate = conventionalCandidatesSubmittedRepository.findByRequestId(conventinalCandidate.getRequestId());
                                pendingInterimReportOnBasicChecksFlagUpdate.setPendingInterimReportOnBasicChecksFlag("TRUE");
                                ConventionalVendorCandidatesSubmitted updatPendingInterimReportOnBasicChecksFlagUpdate = conventionalCandidatesSubmittedRepository.save(pendingInterimReportOnBasicChecksFlagUpdate);
                                log.info("update pendingInterimReportOnBasicChecksFlagUpdate in Candidate request  Table for Request id" + updatPendingInterimReportOnBasicChecksFlagUpdate.getRequestId());
                            }
                            if (liChecksDetails1.getCheckStatus() == 3) {
                                emailSentTask.notifyAdminOfInsufficiencyRaised(conventionalVendorliChecksToPerform, String.valueOf(icheckRepsonse), environmentVal.getEmailReciver(), environmentVal.getCopiedReciver());
                            }
                            log.info("setting the setIsCheckStatusTriggered flag to  true" + icheckRepsonse);
                            conventionalVendorliChecksToPerform.setIsCheckStatusTriggered(true);
                            liCheckToPerformRepository.save(conventionalVendorliChecksToPerform);
                            updateRowwiseStatusServiceOutcome.setData(responedata);
                            updateRowwiseStatusServiceOutcome.setMessage(icheckRepsonse.getBody());
                            updateRowwiseStatusServiceOutcome.setStatus(String.valueOf(icheckRepsonse.getStatusCode()));
                            updateRowwiseStatusServiceOutcome.setOutcome(true);
                        }
                    } catch (HttpClientErrorException e) {
                        // Handle specific client-side HTTP error (e.g., 400)
                        responedata = e.getResponseBodyAsString();
                        updateLiCheckHistory(byCheckUniqueId, conventinalCandidate, responedata);
                        updateRowwiseStatusServiceOutcome.setData(responedata);
                        updateRowwiseStatusServiceOutcome.setMessage(e.getResponseBodyAsString());
                        updateRowwiseStatusServiceOutcome.setStatus(String.valueOf(e.getStatusCode()));
                        updateRowwiseStatusServiceOutcome.setOutcome(false);
                        log.info("Response Recived from Ltm for the CheckUnique id " + byCheckUniqueId.getCheckUniqueId() + " Status - " + byCheckUniqueId.getCheckStatus().getCheckStatusCode() + " -- " + responedata);

                    } catch (Exception e) {
                        log.error("Exception while processing CheckUniqueId: " + checkUniqueId + ", Exception: " + e.getMessage());
                        updateRowwiseStatusServiceOutcome.setOutcome(false);
                        updateRowwiseStatusServiceOutcome.setMessage("Exception from iverifiy Api\n" + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("updateAcknoledgementRowwise() Exception" + e.getMessage());
            updateRowwiseStatusServiceOutcome.setOutcome(false);
            updateRowwiseStatusServiceOutcome.setMessage("Exception from iverifiy Api\n" + e.getMessage());
        }
        return updateRowwiseStatusServiceOutcome;
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceOutcome<String> UpdateBGVCheckStatusRowwise(String vendorChecksString, String modeOfVerificationPerformed) throws Exception {

        List<liReportDetails> liReportDetails = new ArrayList<>();
        String responedata = "";
        ServiceOutcome<String> updateRowwiseStatusServiceOutcome = new ServiceOutcome<>();
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseEntity<String> icheckRepsonse = null;
        ConventionalVendorliChecksToPerform byCheckUniqueId = null;
        ConventionalVendorCandidatesSubmitted conventinalCandidate = null;
        try {
            ArrayList<UpdateSubmittedCandidatesResponseDto> updateSubmittedCandidatesResponseDtos = new ArrayList<>();
            ArrayList<liChecksDetails> liChecksDetails = new ArrayList<>();
            VendorcheckdashbordtDto vendorcheckdashbordtDto = new ObjectMapper().readValue(vendorChecksString, VendorcheckdashbordtDto.class);
            VendorCheckStatusMaster byVendorCheckStatusMasterId = null;
            if (vendorcheckdashbordtDto.getStatus() != null) {
                byVendorCheckStatusMasterId = vendorCheckStatusMasterRepository.findByVendorCheckStatusMasterId(Long.valueOf(vendorcheckdashbordtDto.getStatus()));
            }
            VendorChecks vendorCheckss = vendorChecksRepository.findByVendorcheckId(vendorcheckdashbordtDto.getVendorcheckId());
            ConventionalVendorliChecksToPerform conventionalVendorliChecksToPerform = liCheckToPerformRepository.findByVendorChecksVendorcheckId(vendorCheckss.getVendorcheckId());
            log.info("Uploading vendorproofs for Check unique id :: " + conventionalVendorliChecksToPerform.getCheckUniqueId() + " --of status --" + conventionalVendorliChecksToPerform.getCheckStatus().getCheckStatusCode());
            conventionalVendorliChecksToPerform.setModeOfVerificationPerformed(conventionalVendorliChecksToPerform.getModeOfVerificationPerformed());
            com.aashdit.digiverifier.vendorcheck.dto.liReportDetails liReportDetails1 = new liReportDetails();
            conventinalCandidate = conventionalCandidatesSubmittedRepository.findByRequestId(conventionalVendorliChecksToPerform.getRequestId());
            UpdateSubmittedCandidatesResponseDto conventionalVendorCandidatesSubmitted = new UpdateSubmittedCandidatesResponseDto();
            conventionalVendorCandidatesSubmitted.setCandidateID(String.valueOf(conventinalCandidate.getCandidateId()));
            conventionalVendorCandidatesSubmitted.setName(conventinalCandidate.getName());
            conventionalVendorCandidatesSubmitted.setPSNO(conventinalCandidate.getPsNo());
            conventionalVendorCandidatesSubmitted.setRequestID(conventinalCandidate.getRequestId());
            conventionalVendorCandidatesSubmitted.setVendorName(conventinalCandidate.getVendorId());
            Candidate candidate = candidateRepository.findByConventionalRequestId(Long.valueOf(conventionalVendorCandidatesSubmitted.getRequestID()));
            ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted1 = conventionalCandidatesSubmittedRepository.findByRequestId(conventionalVendorCandidatesSubmitted.getRequestID());
            com.aashdit.digiverifier.vendorcheck.dto.liChecksDetails liChecksDetails1 = new liChecksDetails();
            liChecksDetails1.setCheckCode(Math.toIntExact(conventionalVendorliChecksToPerform.getCheckCode()));
            liChecksDetails1.setCheckName(conventionalVendorliChecksToPerform.getCheckName());
            liChecksDetails1.setCheckRemarks(vendorcheckdashbordtDto.getRemarks());
            liChecksDetails1.setCheckStatus(Integer.valueOf(vendorcheckdashbordtDto.getStatus()));
            String pattern = "dd/MM/yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
            liChecksDetails1.setCompletedDate(date);
            liChecksDetails1.setModeOfVerficationPerformed(conventionalVendorliChecksToPerform.getModeOfVerificationPerformed());
            liChecksDetails1.setModeOfVerficationRequired(conventionalVendorliChecksToPerform.getModeOfVerificationRequired());
            log.info("Sent Check  to  Ltm with Status  -" + byVendorCheckStatusMasterId.getCheckStatusCode() + "-  for Check Unique Id  -  " + conventionalVendorliChecksToPerform.getCheckUniqueId());
            liChecksDetails.add(liChecksDetails1);
            liReportDetails1.setVendorReferenceID(String.valueOf(conventionalVendorCandidatesSubmitted1.getApplicantId()));
            if (liChecksDetails1.getCheckStatus() == 4 || liChecksDetails1.getCheckStatus() == 5) {
                ServiceOutcome<String> stringServiceOutcome = generateDiscrepancyreport(candidate.getCandidateId(), ReportType.DISCREPANCY, "DONT", vendorCheckss.getVendorcheckId());
                String tempReportPath = stringServiceOutcome.getStatus() != null ? stringServiceOutcome.getStatus() : null;
                Content content = contentRepository.findByCandidateIdAndCreatedOn(candidate.getCandidateId());
                String bucketName = content.getBucketName();
                String path = content.getPath();
                String[] split = path.split("/");
                String filename = split[split.length - 1];
                String fileExtension = filename.substring(filename.length() - 4, filename.length());
                liReportDetails1.setReportFileExtention(fileExtension);
                liReportDetails1.setReportFileName(filename);
                try {
                    byte[] bytes = awsUtils.getbyteArrayFromS3(bucketName, tempReportPath);
                    String base64String = Base64.getEncoder().encodeToString(bytes);
//                    System.out.println("report>>>"+base64String);
                    liReportDetails1.setReportAttachment(base64String);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                liReportDetails1.setReportStatus(String.valueOf(liChecksDetails1.getCheckStatus()));
                liReportDetails1.setReportType("1");
                liReportDetails.add(liReportDetails1);

            }
            if (liChecksDetails1.getCheckStatus() == 6) {
                ServiceOutcome<String> stringServiceOutcome = generateUnableToVerifiyReport(candidate.getCandidateId(), ReportType.INTERIM, "DONT");
                String tempReportPath = stringServiceOutcome.getStatus() != null ? stringServiceOutcome.getStatus() : null;
                Content content = contentRepository.findByCandidateIdAndCreatedOn(candidate.getCandidateId());
                String bucketName = content.getBucketName();
                String path = content.getPath();
                String[] split = path.split("/");
                String filename = split[split.length - 1];
                String fileExtension = filename.substring(filename.length() - 4, filename.length());
                liReportDetails1.setReportFileExtention(fileExtension);
                liReportDetails1.setReportFileName(filename);
                try {
                    byte[] bytes = awsUtils.getbyteArrayFromS3(bucketName, tempReportPath);
                    String base64String = Base64.getEncoder().encodeToString(bytes);
                    liReportDetails1.setReportAttachment(base64String);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                liReportDetails1.setReportStatus(String.valueOf(liChecksDetails1.getCheckStatus()));
                liReportDetails1.setReportType("1");
                liReportDetails.add(liReportDetails1);
            }
            conventionalVendorCandidatesSubmitted.setLiReportDetails(liReportDetails);
            conventionalVendorCandidatesSubmitted.setLiChecksDetails(liChecksDetails);
            updateSubmittedCandidatesResponseDtos.add(conventionalVendorCandidatesSubmitted);
            //hitting the update request to third party api
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", environmentVal.getMtGrantType());
            map.add("username", environmentVal.getMtUsername());
            map.add("password", environmentVal.getMtPassword());
            HttpHeaders tokenHeader = new HttpHeaders();
            tokenHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            ResponseEntity<String> responseEntity = null;
            HttpEntity<MultiValueMap<String, String>> requestBodyFormUrlEncoded = new HttpEntity<>(map, tokenHeader);
            responseEntity = restTemplate.postForEntity(environmentVal.getConventionalVendorToken(), requestBodyFormUrlEncoded, String.class);
            JSONObject tokenObject = new JSONObject(responseEntity.getBody());
            String access_token = tokenObject.getString("access_token");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + access_token);
            headers.set("Content-Type", "application/json");
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<List<UpdateSubmittedCandidatesResponseDto>> liCheckDtoHttpEntity = new HttpEntity<>(updateSubmittedCandidatesResponseDtos, headers);
            icheckRepsonse = restTemplate.exchange(environmentVal.getConventionalUpdateBgvCheckStatusRowwise(), HttpMethod.POST, liCheckDtoHttpEntity, String.class);
            int statusCodeValue = icheckRepsonse.getStatusCodeValue();
            responedata = icheckRepsonse.getBody();
            log.info("Response Recived from Ltm for the CheckUnique id " + conventionalVendorliChecksToPerform.getCheckUniqueId() + " Status -  " + byVendorCheckStatusMasterId.getCheckStatusCode() + "  --  " + icheckRepsonse);
            if (statusCodeValue == 200) {
                updateLiCheckStatusByVendor(vendorcheckdashbordtDto.getStatus(), String.valueOf(vendorCheckss.getVendorcheckId()), vendorcheckdashbordtDto.getRemarks(), conventionalVendorliChecksToPerform.getModeOfVerificationPerformed(), icheckRepsonse.toString());
                boolean pendingInterimReportOnBasicChecksFlag = setPendingInterimReportOnBasicChecksFlag(Long.valueOf(conventinalCandidate.getRequestId()));
                if (pendingInterimReportOnBasicChecksFlag) {
                    ConventionalVendorCandidatesSubmitted pendingInterimReportOnBasicChecksFlagUpdate = conventionalCandidatesSubmittedRepository.findByRequestId(conventinalCandidate.getRequestId());
                    pendingInterimReportOnBasicChecksFlagUpdate.setPendingInterimReportOnBasicChecksFlag("TRUE");
                    ConventionalVendorCandidatesSubmitted updatPendingInterimReportOnBasicChecksFlagUpdate = conventionalCandidatesSubmittedRepository.save(pendingInterimReportOnBasicChecksFlagUpdate);
                    log.info("update pendingInterimReportOnBasicChecksFlagUpdate in Candidate request  Table for Request id" + updatPendingInterimReportOnBasicChecksFlagUpdate.getRequestId());
                }
                updateLiCheckHistory(conventionalVendorliChecksToPerform, conventinalCandidate, String.valueOf(icheckRepsonse));
                if (liChecksDetails1.getCheckStatus() == 3) {
                    emailSentTask.notifyAdminOfInsufficiencyRaised(conventionalVendorliChecksToPerform, String.valueOf(icheckRepsonse), environmentVal.getEmailReciver(), environmentVal.getCopiedReciver());
                }
                updateRowwiseStatusServiceOutcome.setData(responedata);
                updateRowwiseStatusServiceOutcome.setMessage(icheckRepsonse.getBody());
                updateRowwiseStatusServiceOutcome.setStatus(String.valueOf(icheckRepsonse.getStatusCode()));
                updateRowwiseStatusServiceOutcome.setOutcome(true);
            }
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            // Handle specific client-side HTTP error (e.g., 400)
            responedata = e.getResponseBodyAsString();
            updateLiCheckHistory(byCheckUniqueId, conventinalCandidate, responedata);
            updateRowwiseStatusServiceOutcome.setData(responedata);
            updateRowwiseStatusServiceOutcome.setMessage(e.getResponseBodyAsString());
            updateRowwiseStatusServiceOutcome.setStatus(String.valueOf(e.getStatusCode()));
            updateRowwiseStatusServiceOutcome.setOutcome(false);
            log.info("Exception From Iverifiy while processing" + e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception while processing" + e.getMessage());
            updateRowwiseStatusServiceOutcome.setOutcome(false);
            updateRowwiseStatusServiceOutcome.setMessage("Exception from iverifiy Api\n" + e.getMessage());
        }
        return updateRowwiseStatusServiceOutcome;
    }

    public ServiceOutcome<String> reSubmitUpdateBGVCheckStatusRowwise(String checkUniqueId) throws Exception {

        List<liReportDetails> liReportDetails = new ArrayList<>();
        String responedata = "";
        ServiceOutcome<String> updateRowwiseStatusServiceOutcome = new ServiceOutcome<>();
        try {
            ArrayList<UpdateSubmittedCandidatesResponseDto> updateSubmittedCandidatesResponseDtos = new ArrayList<>();
            ArrayList<liChecksDetails> liChecksDetails = new ArrayList<>();
//            VendorcheckdashbordtDto vendorcheckdashbordtDto = new ObjectMapper().readValue(vendorChecksString, VendorcheckdashbordtDto.class);
            VendorCheckStatusMaster byVendorCheckStatusMasterId = null;
            ConventionalVendorliChecksToPerform byCheckUniqueId = liCheckToPerformRepository.findByCheckUniqueId(Long.valueOf(checkUniqueId));

            if (byCheckUniqueId.getCheckStatus() != null) {
                byVendorCheckStatusMasterId = vendorCheckStatusMasterRepository.findByVendorCheckStatusMasterId(Long.valueOf(byCheckUniqueId.getCheckStatus().getVendorCheckStatusMasterId()));

            }
            VendorChecks vendorCheckss = vendorChecksRepository.findByVendorcheckId(byCheckUniqueId.getVendorChecks().getVendorcheckId());
            ConventionalVendorliChecksToPerform conventionalVendorliChecksToPerform = liCheckToPerformRepository.findByVendorChecksVendorcheckId(vendorCheckss.getVendorcheckId());
            log.info("Uploading vendorproofs for Check unique id :: " + conventionalVendorliChecksToPerform.getCheckUniqueId() + " --of status --" + conventionalVendorliChecksToPerform.getCheckStatus().getCheckStatusCode());
            conventionalVendorliChecksToPerform.setModeOfVerificationPerformed(conventionalVendorliChecksToPerform.getModeOfVerificationPerformed());
            com.aashdit.digiverifier.vendorcheck.dto.liReportDetails liReportDetails1 = new liReportDetails();
            ConventionalVendorCandidatesSubmitted conventinalCandidate = conventionalCandidatesSubmittedRepository.findByRequestId(conventionalVendorliChecksToPerform.getRequestId());
            UpdateSubmittedCandidatesResponseDto conventionalVendorCandidatesSubmitted = new UpdateSubmittedCandidatesResponseDto();
            conventionalVendorCandidatesSubmitted.setCandidateID(String.valueOf(conventinalCandidate.getCandidateId()));
            conventionalVendorCandidatesSubmitted.setName(conventinalCandidate.getName());
            conventionalVendorCandidatesSubmitted.setPSNO(conventinalCandidate.getPsNo());
            conventionalVendorCandidatesSubmitted.setRequestID(conventinalCandidate.getRequestId());
            conventionalVendorCandidatesSubmitted.setVendorName(conventinalCandidate.getVendorId());
            Candidate candidate = candidateRepository.findByConventionalRequestId(Long.valueOf(conventionalVendorCandidatesSubmitted.getRequestID()));
            ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted1 = conventionalCandidatesSubmittedRepository.findByRequestId(conventionalVendorCandidatesSubmitted.getRequestID());
            com.aashdit.digiverifier.vendorcheck.dto.liChecksDetails liChecksDetails1 = new liChecksDetails();
            liChecksDetails1.setCheckCode(Math.toIntExact(conventionalVendorliChecksToPerform.getCheckCode()));
            liChecksDetails1.setCheckName(conventionalVendorliChecksToPerform.getCheckName());
            liChecksDetails1.setCheckRemarks(byCheckUniqueId.getCheckRemarks());
            liChecksDetails1.setCheckStatus(Integer.valueOf(byCheckUniqueId.getCheckStatus().getVendorCheckStatusMasterId().toString()));
            String pattern = "dd/MM/yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
            liChecksDetails1.setCompletedDate(date);
            liChecksDetails1.setModeOfVerficationPerformed(conventionalVendorliChecksToPerform.getModeOfVerificationPerformed());
            liChecksDetails1.setModeOfVerficationRequired(conventionalVendorliChecksToPerform.getModeOfVerificationRequired());
            if (liChecksDetails1.getCheckStatus() == 4 || liChecksDetails1.getCheckStatus() == 5 || liChecksDetails1.getCheckStatus() == 6) {
                log.info("adding spaces to  remarks to get it passed");
                String checkRemarks = liChecksDetails1.getCheckRemarks();
                checkRemarks += "    ";
                liChecksDetails1.setCheckRemarks(checkRemarks);
            }
            log.info("Sent Check  to  Ltm with Status  -" + byVendorCheckStatusMasterId.getCheckStatusCode() + "-  for Check Unique Id  -  " + conventionalVendorliChecksToPerform.getCheckUniqueId());
            liChecksDetails.add(liChecksDetails1);
            liReportDetails1.setVendorReferenceID(String.valueOf(conventionalVendorCandidatesSubmitted1.getApplicantId()));
            if (liChecksDetails1.getCheckStatus() == 4 || liChecksDetails1.getCheckStatus() == 5) {
                ServiceOutcome<String> stringServiceOutcome = generateDiscrepancyreport(candidate.getCandidateId(), ReportType.DISCREPANCY, "DONT", vendorCheckss.getVendorcheckId());
                String tempReportPath = stringServiceOutcome.getStatus() != null ? stringServiceOutcome.getStatus() : null;
                Content content = contentRepository.findByCandidateIdAndCreatedOn(candidate.getCandidateId());
                String bucketName = content.getBucketName();
                String path = content.getPath();
                String[] split = path.split("/");
                String filename = split[split.length - 1];
                String fileExtension = filename.substring(filename.length() - 4, filename.length());
                liReportDetails1.setReportFileExtention(fileExtension);
                liReportDetails1.setReportFileName(filename);
                try {
                    byte[] bytes = awsUtils.getbyteArrayFromS3(bucketName, tempReportPath);
                    String base64String = Base64.getEncoder().encodeToString(bytes);
//                    System.out.println("report>>>"+base64String);
                    liReportDetails1.setReportAttachment(base64String);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                liReportDetails1.setReportStatus(String.valueOf(liChecksDetails1.getCheckStatus()));
                liReportDetails1.setReportType("1");
                liReportDetails.add(liReportDetails1);

            }
            if (liChecksDetails1.getCheckStatus() == 6) {
                ConventionalVendorliChecksToPerform conventionalVendorliChecksToPerform1 = liCheckToPerformRepository.findByVendorChecksVendorcheckId(vendorCheckss.getVendorcheckId());
                ServiceOutcome<String> stringServiceOutcome = generateUnableToVerifiyReport(candidate.getCandidateId(), ReportType.INTERIM, "DONT");
                String tempReportPath = stringServiceOutcome.getStatus() != null ? stringServiceOutcome.getStatus() : null;
                System.out.println("stringServiceOutcome URL" + stringServiceOutcome.getData());
                System.out.println("stringServiceOutcome checkId" + stringServiceOutcome.getMessage());
                Content content = contentRepository.findByCandidateIdAndCreatedOn(candidate.getCandidateId());
                String bucketName = content.getBucketName();
                String path = content.getPath();
                String[] split = path.split("/");
                String filename = split[split.length - 1];
                String fileExtension = filename.substring(filename.length() - 4, filename.length());
                liReportDetails1.setReportFileExtention(fileExtension);
                liReportDetails1.setReportFileName(filename);
                try {
                    byte[] bytes = awsUtils.getbyteArrayFromS3(bucketName, tempReportPath);
                    String base64String = Base64.getEncoder().encodeToString(bytes);
//                      System.out.println("report>>>"+base64String);
                    liReportDetails1.setReportAttachment(base64String);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                liReportDetails1.setReportStatus(String.valueOf(liChecksDetails1.getCheckStatus()));
                liReportDetails1.setReportType("1");
                liReportDetails.add(liReportDetails1);
            }
            conventionalVendorCandidatesSubmitted.setLiReportDetails(liReportDetails);
            conventionalVendorCandidatesSubmitted.setLiChecksDetails(liChecksDetails);
            updateSubmittedCandidatesResponseDtos.add(conventionalVendorCandidatesSubmitted);
            //hitting the update request to third party api
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", environmentVal.getMtGrantType());
            map.add("username", environmentVal.getMtUsername());
            map.add("password", environmentVal.getMtPassword());
            HttpHeaders tokenHeader = new HttpHeaders();
            tokenHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            ResponseEntity<String> responseEntity = null;
            HttpEntity<MultiValueMap<String, String>> requestBodyFormUrlEncoded = new HttpEntity<>(map, tokenHeader);
            responseEntity = restTemplate.postForEntity(environmentVal.getConventionalVendorToken(), requestBodyFormUrlEncoded, String.class);
            JSONObject tokenObject = new JSONObject(responseEntity.getBody());
            String access_token = tokenObject.getString("access_token");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + access_token);
            headers.set("Content-Type", "application/json");
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<List<UpdateSubmittedCandidatesResponseDto>> liCheckDtoHttpEntity = new HttpEntity<>(updateSubmittedCandidatesResponseDtos, headers);
            ResponseEntity<String> icheckRepsonse = restTemplate.exchange(environmentVal.getConventionalUpdateBgvCheckStatusRowwise(), HttpMethod.POST, liCheckDtoHttpEntity, String.class);
            int statusCodeValue = icheckRepsonse.getStatusCodeValue();
            responedata = icheckRepsonse.getBody();
            log.info("Response Recived from Ltm for the CheckUnique id " + conventionalVendorliChecksToPerform.getCheckUniqueId() + " Status -  " + byVendorCheckStatusMasterId.getCheckStatusCode() + "  --  " + icheckRepsonse);
            if (statusCodeValue == 200) {
                updateRowwiseStatusServiceOutcome.setData(responedata);
                updateRowwiseStatusServiceOutcome.setMessage(icheckRepsonse.getBody());
                updateRowwiseStatusServiceOutcome.setStatus(String.valueOf(icheckRepsonse.getStatusCode()));
                updateRowwiseStatusServiceOutcome.setOutcome(true);
            } else {
                log.info("Exception from iverifiy Api for the  Check  unique id --" + conventionalVendorliChecksToPerform.getCheckUniqueId() + "- Response -" + icheckRepsonse);
                updateRowwiseStatusServiceOutcome.setMessage("Exception from iverifiy Api");
                updateRowwiseStatusServiceOutcome.setOutcome(false);
            }
        } catch (Exception e) {
            log.error("Retrigger updateAcknoledgementRowwise() Exception" + e.getMessage());
            updateRowwiseStatusServiceOutcome.setData(null);
            updateRowwiseStatusServiceOutcome.setMessage("Exception from iverifiy Api\n" + e.getMessage());
            updateRowwiseStatusServiceOutcome.setOutcome(false);
        }
        return updateRowwiseStatusServiceOutcome;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackFor = Exception.class)
    public ServiceOutcome<LicheckRequiredResponseDto> addUpdateLiCheckToPerformData(FetchVendorConventionalCandidateDto licheckDto, String message) throws Exception {
        // TODO Auto-generated method stub
        try {
            String bgvresponse = "";
//            log.info("addUpdateLiCheckToPerformData() starts");
            ServiceOutcome<LicheckRequiredResponseDto> svcOutcome = new ServiceOutcome<LicheckRequiredResponseDto>();
            List<ConventionalVendorliChecksToPerform> mailConventionalVendorliChecksToPerform = new ArrayList<>();
            if (licheckDto.getLicheckId() == null) {
                User user = (SecurityHelper.getCurrentUser() != null) ? SecurityHelper.getCurrentUser() : userRepository.findByUserId(53l);
                ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(licheckDto.getRequestId());
                JSONObject obj1 = new JSONObject(message);
                log.info("Processing request with ID: {}", licheckDto.getRequestId());
                if (obj1.isNull("FastTrackStatus") == false) {
                    conventionalVendorCandidatesSubmitted.setFastTrack(obj1.getString("FastTrackStatus"));
                    ConventionalVendorCandidatesSubmitted save = conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted);
                    log.info("FastTrack status saved for candidate, requestId: {}", save.getRequestId());
                }


                if (obj1.isNull("liChecksToPerform") == false) {
                    JSONObject liChecksToPerform = obj1.getJSONObject("liChecksToPerform");
                    if (liChecksToPerform.isNull("liChecksRequired") == false) {
                        JSONArray liChecksRequired = liChecksToPerform.getJSONArray("liChecksRequired");
                        List<JSONObject> collect = IntStream.range(0, liChecksRequired.length()).mapToObj(index -> ((JSONObject) liChecksRequired.get(index))).collect(Collectors.toList());
                        //by request id
                        List<ConventionalVendorliChecksToPerform> byCandidateId = liCheckToPerformRepository.findByRequestId(licheckDto.getRequestId());
                        if (licheckDto.getRequestType().equalsIgnoreCase("InsufficiencyClearance")) {
                            for (JSONObject licheckReq : collect) {
                                String checkUniqueId = licheckReq.getString("Check_Unique_ID");
                                ConventionalVendorliChecksToPerform liChecksToPerform1 = liCheckToPerformRepository.findByCheckUniqueId(Long.valueOf(checkUniqueId));
                                if (liChecksToPerform1 != null && (liChecksToPerform1.getCheckStatus().getVendorCheckStatusMasterId() == 3 || liChecksToPerform1.getCheckStatus().getVendorCheckStatusMasterId() == 5)) {
                                    log.info("Insufficiency resubmitted started For Check , requestId: {} - checkUniqueId: {} - Old status: {}", liChecksToPerform1.getRequestId(), liChecksToPerform1.getCheckUniqueId(), liChecksToPerform1.getCheckStatus().getCheckStatusCode());
                                    VendorCheckStatusMaster byCheckStatusCode = vendorCheckStatusMasterRepository.findByVendorCheckStatusMasterId(2l);
                                    liChecksToPerform1.setCheckCode(licheckReq.getLong("CheckCode"));
                                    liChecksToPerform1.setCheckUniqueId(licheckReq.getLong("Check_Unique_ID"));
                                    liChecksToPerform1.setCheckName(licheckReq.getString("CheckName"));
                                    liChecksToPerform1.setCheckStatus(byCheckStatusCode);
                                    liChecksToPerform1.setCheckRemarks(licheckReq.getString("CheckRemarks"));
                                    liChecksToPerform1.setModeOfVerificationRequired(licheckReq.getString("ModeOfVerficationRequired"));
                                    //setting the mode on initial
                                    liChecksToPerform1.setModeOfVerificationPerformed(licheckReq.getString("ModeOfVerficationRequired"));
                                    liChecksToPerform1.setCompletedDate(licheckReq.getString("CompletedDate"));
                                    liChecksToPerform1.setCreatedBy(user);
                                    liChecksToPerform1.setCreatedOn(new Date());
                                    liChecksToPerform1.setCandidateId(String.valueOf(obj1.getString("CandidateID").isBlank() ? obj1.getLong("PSNO") : obj1.getString("CandidateID")));
                                    liChecksToPerform1.setRequestId(obj1.getString("RequestID"));
                                    if (licheckReq.getString("DatetoComplete").isEmpty() == false) {
                                        liChecksToPerform1.setDateToComplete(licheckReq.getString("DatetoComplete"));
                                    }
                                    ConventionalVendorliChecksToPerform saved = liCheckToPerformRepository.save(liChecksToPerform1);
                                    log.info("Insufficiency resubmitted completed For Check, requestId: {} - checkUniqueId: {} - CheckStatus: {}", saved.getRequestId(), saved.getCheckUniqueId(), saved.getCheckStatus().getCheckStatusCode());
                                    mailConventionalVendorliChecksToPerform.add(saved);
                                    ServiceOutcome<String> stringServiceOutcome = updateBgvCheckRowwiseonProgress(Long.valueOf(liChecksToPerform1.getRequestId()), liChecksToPerform1.getCheckUniqueId());
                                    LicheckRequiredResponseDto licheckRequiredResponseDto = new LicheckRequiredResponseDto(saved.getId(), saved.getCheckCode(), saved.getCheckName(), saved.getCheckStatus().getCheckStatusCode(), saved.getCheckRemarks(), saved.getModeOfVerificationRequired(), saved.getModeOfVerificationPerformed(), saved.getCompletedDate());
                                }
                            }
                            ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted1 = conventionalCandidatesSubmittedRepository.findByRequestId(String.valueOf(obj1.getLong("RequestID")));
                            conventionalVendorCandidatesSubmitted1.setRequestType(conventionalVendorCandidatesSubmitted1.getOldRequestType());
                            conventionalVendorCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted1);
                            log.info("Insufficiency Checks Changed To Inprogress - Reverted candidate Request Type For requestId: {} - Current Request Type: {} - Changed to Old Request Type : {}", conventionalVendorCandidatesSubmitted1.getRequestId(), conventionalVendorCandidatesSubmitted1.getRequestType(), conventionalVendorCandidatesSubmitted.getOldRequestType());
                            acknoledgeAfterSavedCandidate(obj1.getLong("RequestID"));
                            emailSentTask.sendEmailOnRaisedInsufficiency(conventionalVendorCandidatesSubmitted, mailConventionalVendorliChecksToPerform, message, environmentVal.getEmailReciver(), environmentVal.getCopiedReciver());
                        } else {
                            for (JSONObject licheckReq : collect) {
                                String checkUniqueId = licheckReq.getString("Check_Unique_ID");
                                ConventionalVendorliChecksToPerform byCheckUniqueId = liCheckToPerformRepository.findByCheckUniqueId(Long.valueOf(checkUniqueId));
                                if (byCheckUniqueId == null) {
                                    VendorCheckStatusMaster byCheckStatusCode = vendorCheckStatusMasterRepository.findByVendorCheckStatusMasterId(7l);
                                    ConventionalVendorliChecksToPerform liChecksToPerform1 = new ConventionalVendorliChecksToPerform();
                                    liChecksToPerform1.setCheckCode(licheckReq.getLong("CheckCode"));
                                    liChecksToPerform1.setCheckUniqueId(licheckReq.getLong("Check_Unique_ID"));
                                    liChecksToPerform1.setCheckName(licheckReq.getString("CheckName"));
                                    liChecksToPerform1.setCheckStatus(byCheckStatusCode);
                                    liChecksToPerform1.setRequestId(licheckDto.getRequestId());
                                    liChecksToPerform1.setCheckRemarks(licheckReq.getString("CheckRemarks"));
                                    liChecksToPerform1.setModeOfVerificationRequired(licheckReq.getString("ModeOfVerficationRequired"));
                                    liChecksToPerform1.setModeOfVerificationPerformed(licheckReq.getString("ModeOfVerficationRequired"));
                                    liChecksToPerform1.setCompletedDate(licheckReq.getString("CompletedDate"));
                                    liChecksToPerform1.setCreatedBy(user);
                                    liChecksToPerform1.setCreatedOn(new Date());
                                    liChecksToPerform1.setCandidateId(String.valueOf(obj1.getString("CandidateID").isBlank() ? obj1.getLong("PSNO") : obj1.getString("CandidateID")));
                                    liChecksToPerform1.setRequestId(obj1.getString("RequestID"));
                                    if (licheckReq.getString("DatetoComplete").isEmpty() == false) {
                                        liChecksToPerform1.setDateToComplete(licheckReq.getString("DatetoComplete"));
                                    }
                                    ConventionalVendorliChecksToPerform saved = liCheckToPerformRepository.save(liChecksToPerform1);
                                    log.info("New check added, requestId: {} - checkUniqueId: {} - Check status: {}  - Candidate RequestType : {}", saved.getRequestId(), saved.getCheckUniqueId(), saved.getCheckStatus().getCheckStatusCode(), licheckDto.getRequestType());
                                    updateLiCheckHistory(saved, conventionalVendorCandidatesSubmitted, "");
                                    LicheckRequiredResponseDto licheckRequiredResponseDto = new LicheckRequiredResponseDto(saved.getId(), saved.getCheckCode(), saved.getCheckName(), saved.getCheckStatus().getCheckStatusCode(), saved.getCheckRemarks(), saved.getModeOfVerificationRequired(), saved.getModeOfVerificationPerformed(), saved.getCompletedDate());
                                    svcOutcome.setData(licheckRequiredResponseDto);
                                }
                            }
                            acknoledgeAfterSavedCandidate(obj1.getLong("RequestID"));
                        }
                    }
                } else {
                    log.info("no licheck from mintree");
                }
            } else {
                if (liCheckToPerformRepository.existsById(licheckDto.getLicheckId())) {
                    ConventionalVendorliChecksToPerform conventionalVendorliChecksToPerform = liCheckToPerformRepository.findById(licheckDto.getLicheckId()).get();
                    log.info("Moving status from --" + conventionalVendorliChecksToPerform.getCheckStatus().getCheckStatusCode() + "-- Check UniqueId --" + conventionalVendorliChecksToPerform.getCheckUniqueId());
                    ConventionalVendorCandidatesSubmitted byRequestId = conventionalCandidatesSubmittedRepository.findByRequestId(conventionalVendorliChecksToPerform.getRequestId());
                    VendorCheckStatusMaster byCheckStatusCode = vendorCheckStatusMasterRepository.findByVendorCheckStatusMasterId(2l);
                    Source source = sourceRepository.findById(licheckDto.getSourceId()).get();
                    conventionalVendorliChecksToPerform.setCheckStatus(byCheckStatusCode);
                    conventionalVendorliChecksToPerform.setSource(source);
                    conventionalVendorliChecksToPerform.setSourceName(licheckDto.getSourceName());
                    conventionalVendorliChecksToPerform.setVendorName(licheckDto.getVendorName());
                    conventionalVendorliChecksToPerform.setLastUpdatedOn(new Date());
                    conventionalVendorliChecksToPerform.setLastUpdatedBy(String.valueOf(String.valueOf(SecurityHelper.getCurrentUser().getUserId())));
                    ConventionalVendorliChecksToPerform updatedLiChecksToPerform = liCheckToPerformRepository.save(conventionalVendorliChecksToPerform);
                    log.info("to  " + updatedLiChecksToPerform.getCheckStatus().getCheckStatusCode() + "--for checkunique id --" + conventionalVendorliChecksToPerform.getCheckUniqueId() + "  Assigned to  " + licheckDto.getVendorName());
                    ServiceOutcome<String> stringServiceOutcome = updateBgvCheckRowwiseonProgress(Long.valueOf(conventionalVendorliChecksToPerform.getRequestId()), conventionalVendorliChecksToPerform.getCheckUniqueId());
                }

            }
            return svcOutcome;

        } catch (Exception e) {
            throw new Exception("exception in addupdateLicheckstoperform() ");
        }

    }

    public ServiceOutcome<List<LicheckRequiredResponseDto>> findAllLiChecksRequired() throws Exception {
        ServiceOutcome<List<LicheckRequiredResponseDto>> serviceOutcome = new ServiceOutcome<List<LicheckRequiredResponseDto>>();
        try {
            User user = (SecurityHelper.getCurrentUser() != null) ? SecurityHelper.getCurrentUser() : userRepository.findByUserId(53l);
            ArrayList<LicheckRequiredResponseDto> licheckRequiredResponseDtos = new ArrayList<>();

            List<ConventionalVendorliChecksToPerform> allLichecks = liCheckToPerformRepository.findAll();

            for (ConventionalVendorliChecksToPerform licheck : allLichecks) {
                LicheckRequiredResponseDto licheckRequiredResponseDto = new LicheckRequiredResponseDto();
                licheckRequiredResponseDto.setId(licheck.getId());
                licheckRequiredResponseDto.setCheckCode(licheck.getCheckCode());
                licheckRequiredResponseDto.setCandidateId(licheck.getCandidateId());
                licheckRequiredResponseDto.setCheckName(licheck.getCheckName());
                licheckRequiredResponseDto.setCheckRemarks(licheck.getCheckRemarks());
                licheckRequiredResponseDto.setCheckStatus(licheck.getCheckStatus().getCheckStatusCode());
                licheckRequiredResponseDto.setCompletedDateTime(licheck.getCompletedDate());
                licheckRequiredResponseDto.setCreatedBy(licheck.getCreatedBy().getUserName());
                licheckRequiredResponseDto.setCreatedOn(licheck.getCreatedOn());
                licheckRequiredResponseDto.setModeOfVerificationPerformed(licheck.getModeOfVerificationPerformed());
                licheckRequiredResponseDto.setModeOfVerificationRequired(licheck.getModeOfVerificationRequired());
                if (licheck.getVendorChecks() != null) {
                    licheckRequiredResponseDto.setDocumentName(licheck.getVendorChecks().getDocumentname());
                    licheckRequiredResponseDto.setVendorId(licheck.getVendorChecks().getVendorcheckId());
                }
                if (licheck.getRequestId().isEmpty() == false) {
                    licheckRequiredResponseDto.setRequestID(licheck.getRequestId());
                }
                if (licheck.getSource() != null) {
                    licheckRequiredResponseDto.setSourceId(licheck.getSource().getSourceId());
                }

                if (licheck.getSourceName() != null) {
                    licheckRequiredResponseDto.setSourceName(licheck.getSourceName());
                }
                if (licheck.getVendorName() != null) {
                    licheckRequiredResponseDto.setVendorName(licheck.getVendorName());
                }

                licheckRequiredResponseDtos.add(licheckRequiredResponseDto);
            }

            serviceOutcome.setData(licheckRequiredResponseDtos);
            serviceOutcome.setOutcome(true);

            return serviceOutcome;
        } catch (Exception e) {
            log.error("exception in findAllLiChecksRequired()" + e.getMessage());
            serviceOutcome.setOutcome(false);
            serviceOutcome.setMessage(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    //by request id
    public ServiceOutcome<List<LicheckRequiredResponseDto>> findAllLiChecksRequiredbyCandidateId(String requestId) throws Exception {
        ServiceOutcome<List<LicheckRequiredResponseDto>> serviceOutcome = new ServiceOutcome<List<LicheckRequiredResponseDto>>();
        ArrayList<LicheckRequiredResponseDto> licheckRequiredResponseDtos = null;
        try {
            User user = (SecurityHelper.getCurrentUser() != null) ? SecurityHelper.getCurrentUser() : userRepository.findByUserId(53l);

            licheckRequiredResponseDtos = new ArrayList<>();
            List<ConventionalVendorliChecksToPerform> allLichecks = liCheckToPerformRepository.findByRequestId(requestId);
            for (ConventionalVendorliChecksToPerform licheck : allLichecks) {
                LicheckRequiredResponseDto licheckRequiredResponseDto = new LicheckRequiredResponseDto();
                licheckRequiredResponseDto.setId(licheck.getId());
                licheckRequiredResponseDto.setCheckCode(licheck.getCheckCode());
                licheckRequiredResponseDto.setCandidateId(licheck.getCandidateId());
                licheckRequiredResponseDto.setCheckName(licheck.getCheckName());
                licheckRequiredResponseDto.setCheckRemarks(licheck.getCheckRemarks());
                licheckRequiredResponseDto.setCheckStatus(licheck.getCheckStatus().getCheckStatusCode());
                licheckRequiredResponseDto.setCompletedDateTime(licheck.getCompletedDate());
                licheckRequiredResponseDto.setCreatedBy(licheck.getCreatedBy().getUserName());
                licheckRequiredResponseDto.setCreatedOn(licheck.getCreatedOn());
                licheckRequiredResponseDto.setCheckUniqueId(licheck.getCheckUniqueId());
                ModeOfVerificationStatusMaster modeOfVerificationStatusMasterPerformed = modeOfVerificationStatusMasterRepository.findById(Long.valueOf(licheck.getModeOfVerificationPerformed())).get();
                licheckRequiredResponseDto.setModeOfVerificationPerformed(modeOfVerificationStatusMasterPerformed.getModeOfVerification());
                ModeOfVerificationStatusMaster modeOfVerificationStatusMasterRequired = modeOfVerificationStatusMasterRepository.findById(Long.valueOf(licheck.getModeOfVerificationRequired())).get();
                licheckRequiredResponseDto.setModeOfVerificationRequired(modeOfVerificationStatusMasterRequired.getModeOfVerification());
                if (licheck.getVendorChecks() != null) {
                    licheckRequiredResponseDto.setDocumentName(licheck.getVendorChecks().getDocumentname());
                    licheckRequiredResponseDto.setVendorId(licheck.getVendorChecks().getVendorcheckId());
                }
                if (licheck.getRequestId().isEmpty() == false) {
                    licheckRequiredResponseDto.setRequestID(licheck.getRequestId());
                    Candidate byConventionalRequestId = candidateRepository.findByConventionalRequestId(Long.valueOf(licheck.getRequestId()));
                    if (byConventionalRequestId != null) {
                        licheckRequiredResponseDto.setCandidateBasicId(String.valueOf(byConventionalRequestId.getCandidateId()));
                    }
                }
                if (licheck.getSource() != null) {
                    licheckRequiredResponseDto.setSourceId(licheck.getSource().getSourceId());
                }

                if (licheck.getSourceName() != null) {
                    licheckRequiredResponseDto.setSourceName(licheck.getSourceName());
                }
                if (licheck.getVendorName() != null) {
                    licheckRequiredResponseDto.setVendorName(licheck.getVendorName());
                }

                if (licheck.getDateToComplete() != null) {
                    licheckRequiredResponseDto.setFastTrackDateTime(String.valueOf(licheck.getDateToComplete()));
                }
                if (licheck.getStopCheck() != null) {
                    licheckRequiredResponseDto.setStopCheckStatus(licheck.getStopCheck());
                }
                ServiceOutcome<CandidateuploadS3Documents> allfilesUploadedurls = findAllfilesUploadedurls(licheck.getRequestId(), licheck.getCheckName());
                if (allfilesUploadedurls.getData() != null) {
                    licheckRequiredResponseDto.setCandidateuploadS3Documents(allfilesUploadedurls.getData());
                    licheckRequiredResponseDto.setDocumentName(licheckRequiredResponseDto.getCandidateuploadS3Documents().getDocumentName());
                }
                if (licheck.getDisabled() != null) {
                    licheckRequiredResponseDto.setDisableStatus(licheck.getDisabled());
                }
                licheckRequiredResponseDtos.add(licheckRequiredResponseDto);
            }

            serviceOutcome.setData(licheckRequiredResponseDtos);
            serviceOutcome.setOutcome(true);
        } catch (Exception e) {
            log.error("exception in findAllLiChecksRequiredbyCandidateId()" + e.getMessage());
            serviceOutcome.setData(licheckRequiredResponseDtos);
        }
        return serviceOutcome;
    }

    public ServiceOutcome<List<LicheckRequiredResponseDto>> findAllStopLiChecksRequiredbyCandidateId(String requestId) throws Exception {
        ServiceOutcome<List<LicheckRequiredResponseDto>> serviceOutcome = new ServiceOutcome<List<LicheckRequiredResponseDto>>();
        ArrayList<LicheckRequiredResponseDto> licheckRequiredResponseDtos = null;
        try {
            User user = (SecurityHelper.getCurrentUser() != null) ? SecurityHelper.getCurrentUser() : userRepository.findByUserId(53l);
            licheckRequiredResponseDtos = new ArrayList<>();
            List<ConventionalVendorliChecksToPerform> allLichecks = liCheckToPerformRepository.findByRequestId(requestId);
            allLichecks = allLichecks.stream().filter(p -> p.getStopCheck() != null).collect(Collectors.toList());
            for (ConventionalVendorliChecksToPerform licheck : allLichecks) {
                LicheckRequiredResponseDto licheckRequiredResponseDto = new LicheckRequiredResponseDto();
                licheckRequiredResponseDto.setId(licheck.getId());
                licheckRequiredResponseDto.setCheckCode(licheck.getCheckCode());
                licheckRequiredResponseDto.setCandidateId(licheck.getCandidateId());
                licheckRequiredResponseDto.setCheckName(licheck.getCheckName());
                licheckRequiredResponseDto.setCheckRemarks(licheck.getCheckRemarks());
                licheckRequiredResponseDto.setCheckStatus(licheck.getCheckStatus().getCheckStatusCode());
                licheckRequiredResponseDto.setCompletedDateTime(licheck.getCompletedDate());
                if (licheck.getCreatedBy() != null) {
                    licheckRequiredResponseDto.setCreatedBy(licheck.getCreatedBy().getUserName());
                }
                licheckRequiredResponseDto.setCreatedOn(licheck.getCreatedOn());
                licheckRequiredResponseDto.setCheckUniqueId(licheck.getCheckUniqueId());
                ModeOfVerificationStatusMaster modeOfVerificationStatusMasterPerformed = modeOfVerificationStatusMasterRepository.findById(Long.valueOf(licheck.getModeOfVerificationPerformed())).get();
                licheckRequiredResponseDto.setModeOfVerificationPerformed(modeOfVerificationStatusMasterPerformed.getModeOfVerification());
                ModeOfVerificationStatusMaster modeOfVerificationStatusMasterRequired = modeOfVerificationStatusMasterRepository.findById(Long.valueOf(licheck.getModeOfVerificationRequired())).get();
                licheckRequiredResponseDto.setModeOfVerificationRequired(modeOfVerificationStatusMasterRequired.getModeOfVerification());
                if (licheck.getVendorChecks() != null) {
                    licheckRequiredResponseDto.setDocumentName(licheck.getVendorChecks().getDocumentname());
                    licheckRequiredResponseDto.setVendorId(licheck.getVendorChecks().getVendorcheckId());
                }
                if (licheck.getRequestId().isEmpty() == false) {
                    licheckRequiredResponseDto.setRequestID(licheck.getRequestId());
                    Candidate byConventionalRequestId = candidateRepository.findByConventionalRequestId(Long.valueOf(licheck.getRequestId()));
                    if (byConventionalRequestId != null) {
                        licheckRequiredResponseDto.setCandidateBasicId(String.valueOf(byConventionalRequestId.getCandidateId()));
                    }
                }
                if (licheck.getSource() != null) {
                    licheckRequiredResponseDto.setSourceId(licheck.getSource().getSourceId());
                }

                if (licheck.getSourceName() != null) {
                    licheckRequiredResponseDto.setSourceName(licheck.getSourceName());
                }
                if (licheck.getVendorName() != null) {
                    licheckRequiredResponseDto.setVendorName(licheck.getVendorName());
                }

                if (licheck.getDateToComplete() != null) {
                    licheckRequiredResponseDto.setFastTrackDateTime(String.valueOf(licheck.getDateToComplete()));
                }
                if (licheck.getStopCheck() != null) {
                    licheckRequiredResponseDto.setStopCheckStatus(licheck.getStopCheck());
                }
                if (licheck.getDisabled() != null) {
                    licheckRequiredResponseDto.setDisableStatus(licheck.getDisabled());
                }

                ServiceOutcome<CandidateuploadS3Documents> allfilesUploadedurls = findAllfilesUploadedurls(licheck.getRequestId(), licheck.getCheckName());
                if (allfilesUploadedurls.getData() != null) {
                    licheckRequiredResponseDto.setCandidateuploadS3Documents(allfilesUploadedurls.getData());
                    licheckRequiredResponseDto.setDocumentName(licheckRequiredResponseDto.getCandidateuploadS3Documents().getDocumentName());
                }
                licheckRequiredResponseDtos.add(licheckRequiredResponseDto);
            }

            serviceOutcome.setData(licheckRequiredResponseDtos);
            serviceOutcome.setOutcome(true);
        } catch (Exception e) {
            log.error("exception in findAllStopLiChecksRequiredbyCandidateId()---" + e.getMessage());
            serviceOutcome.setData(licheckRequiredResponseDtos);
        }
        return serviceOutcome;
    }

    @Transactional
    public ServiceOutcome<List<LicheckRequiredResponseDto>> findAllNewUploadLiChecksRequiredbyCandidateId(String requestId) throws Exception {
        ServiceOutcome<List<LicheckRequiredResponseDto>> serviceOutcome = new ServiceOutcome<List<LicheckRequiredResponseDto>>();
        ArrayList<LicheckRequiredResponseDto> licheckRequiredResponseDtos = null;
        try {
            User user = (SecurityHelper.getCurrentUser() != null) ? SecurityHelper.getCurrentUser() : userRepository.findByUserId(53l);
            licheckRequiredResponseDtos = new ArrayList<>();
            List<ConventionalVendorliChecksToPerform> allLichecks = liCheckToPerformRepository.findByRequestId(requestId);
            List<ConventionalVendorliChecksToPerform> collect = allLichecks.stream().filter(licheck -> licheck.getCheckStatus().getVendorCheckStatusMasterId() == 7l).collect(Collectors.toList());
            List<Source> all = sourceRepository.findAll();
            List<Long> excludedSourceIds = Arrays.asList(1l, 2l, 3l, 4l, 5l, 6l, 9l, 10l);
            all.removeIf(source -> excludedSourceIds.contains(source.getSourceId()));
            for (ConventionalVendorliChecksToPerform licheck : collect) {
                LicheckRequiredResponseDto licheckRequiredResponseDto = new LicheckRequiredResponseDto();
                licheckRequiredResponseDto.setId(licheck.getId());
                licheckRequiredResponseDto.setCheckCode(licheck.getCheckCode());
                licheckRequiredResponseDto.setCandidateId(licheck.getCandidateId());
                licheckRequiredResponseDto.setCheckName(licheck.getCheckName());
                licheckRequiredResponseDto.setCheckRemarks(licheck.getCheckRemarks());
                licheckRequiredResponseDto.setCompletedDateTime(licheck.getCompletedDate());
                if (licheck.getCreatedBy() != null) {
                    licheckRequiredResponseDto.setCreatedBy(licheck.getCreatedBy().getUserName());
                }
                licheckRequiredResponseDto.setCreatedOn(licheck.getCreatedOn());
                licheckRequiredResponseDto.setCheckUniqueId(licheck.getCheckUniqueId());
                ModeOfVerificationStatusMaster modeOfVerificationStatusMasterPerformed = modeOfVerificationStatusMasterRepository.findById(Long.valueOf(licheck.getModeOfVerificationPerformed())).get();
                licheckRequiredResponseDto.setModeOfVerificationPerformed(modeOfVerificationStatusMasterPerformed.getModeOfVerification());
                ModeOfVerificationStatusMaster modeOfVerificationStatusMasterRequired = modeOfVerificationStatusMasterRepository.findById(Long.valueOf(licheck.getModeOfVerificationRequired())).get();
                licheckRequiredResponseDto.setModeOfVerificationRequired(modeOfVerificationStatusMasterRequired.getModeOfVerification());
                if (licheck.getRequestId().isEmpty() == false) {
                    licheckRequiredResponseDto.setRequestID(licheck.getRequestId());
                    Candidate byConventionalRequestId = candidateRepository.findByConventionalRequestId(Long.valueOf(licheck.getRequestId()));
                    if (byConventionalRequestId != null) {
                        licheckRequiredResponseDto.setCandidateBasicId(String.valueOf(byConventionalRequestId.getCandidateId()));
                    }
                }
                Source desiredSource = all.stream().filter(source -> licheckRequiredResponseDto.getCheckName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().contains(source.getSourceName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase())).findFirst().orElse(null);
                if (desiredSource != null) {
                    licheckRequiredResponseDto.setSourceId(desiredSource.getSourceId());
                }
                if (desiredSource != null) {
                    licheckRequiredResponseDto.setSourceName(desiredSource.getSourceName().trim());
                }
                ServiceOutcome<CandidateuploadS3Documents> allfilesUploadedurls = findAllfilesUploadedurls(licheck.getRequestId(), licheck.getCheckName());
                if (allfilesUploadedurls.getData() != null) {
                    licheckRequiredResponseDto.setCandidateuploadS3Documents(allfilesUploadedurls.getData());
                    licheckRequiredResponseDto.setDocumentName(licheckRequiredResponseDto.getCandidateuploadS3Documents().getDocumentName());
                }
                if (licheck.getDisabled() != null) {
                    licheckRequiredResponseDto.setDisableStatus(licheck.getDisabled());
                }
                if (licheck.getDateToComplete() != null) {
                    licheckRequiredResponseDto.setFastTrackDateTime(String.valueOf(licheck.getDateToComplete()));
                }

                if (licheck.getStopCheck() != null) {
                    licheckRequiredResponseDto.setStopCheckStatus(licheck.getStopCheck());
                }
                if (licheck.getVendorChecks() == null) {
                    VendorInitiatDto vendorInitiatDto = new VendorInitiatDto();
                    vendorInitiatDto.setCandidateId(Long.valueOf(licheck.getRequestId()));
                    vendorInitiatDto.setDocumentname(licheckRequiredResponseDto.getDocumentName());
                    vendorInitiatDto.setVendorCheckStatusMasterId(2l);
                    if (licheckRequiredResponseDto.getCandidateuploadS3Documents() != null) {
                        vendorInitiatDto.setDocumentUrl(licheckRequiredResponseDto.getCandidateuploadS3Documents().getPathkey());
                    }
                    vendorInitiatDto.setLicheckId(String.valueOf(licheck.getId()));
                    vendorInitiatDto.setSourceId(licheckRequiredResponseDto.getSourceId());
                    vendorInitiatDto.setSourceName(licheckRequiredResponseDto.getSourceName());
                    if (licheck.getCheckName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().contains("Passport".replaceAll("[^a-zA-Z0-9]", "").toLowerCase())) {
                        User vendor = userRepository.findById(72l).get();
                        vendorInitiatDto.setVendorId(vendor.getUserId());
                        vendorInitiatDto.setVendorName(vendor.getUserFirstName() + vendor.getUserLastName());
                        String vendorUploadData = new ObjectMapper().writeValueAsString(vendorInitiatDto);
                        userService.saveInitiateVendorChecks(vendorUploadData, null, (licheckRequiredResponseDto.getCandidateuploadS3Documents() == null) ? null : licheckRequiredResponseDto.getCandidateuploadS3Documents().getPathkey());
                    } else if (licheck.getCheckName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().contains("Cibil".replaceAll("[^a-zA-Z0-9]", "").toLowerCase())) {
                        User vendor = userRepository.findById(72l).get();
                        vendorInitiatDto.setVendorId(vendor.getUserId());
                        vendorInitiatDto.setVendorName(vendor.getUserFirstName());
                        String vendorUploadData = new ObjectMapper().writeValueAsString(vendorInitiatDto);
                        userService.saveInitiateVendorChecks(vendorUploadData, null, (licheckRequiredResponseDto.getCandidateuploadS3Documents() == null) ? null : licheckRequiredResponseDto.getCandidateuploadS3Documents().getPathkey());

                    } else if (licheck.getCheckName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().contains("EMPLOYMENT".replaceAll("[^a-zA-Z0-9]", "").toLowerCase())) {
                        User vendor = userRepository.findById(72l).get();
                        vendorInitiatDto.setVendorId(vendor.getUserId());
                        vendorInitiatDto.setVendorName(vendor.getUserFirstName());
                        String vendorUploadData = new ObjectMapper().writeValueAsString(vendorInitiatDto);
                        userService.saveInitiateVendorChecks(vendorUploadData, null, (licheckRequiredResponseDto.getCandidateuploadS3Documents() == null) ? null : licheckRequiredResponseDto.getCandidateuploadS3Documents().getPathkey());

                    } else if (licheck.getCheckName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().contains("ofac".replaceAll("[^a-zA-Z0-9]", "").toLowerCase())) {
                        User vendor = userRepository.findById(72l).get();
                        vendorInitiatDto.setVendorId(vendor.getUserId());
                        vendorInitiatDto.setVendorName(vendor.getUserFirstName());
                        String vendorUploadData = new ObjectMapper().writeValueAsString(vendorInitiatDto);
                        userService.saveInitiateVendorChecks(vendorUploadData, null, (licheckRequiredResponseDto.getCandidateuploadS3Documents() == null) ? null : licheckRequiredResponseDto.getCandidateuploadS3Documents().getPathkey());

                    } else if (licheck.getCheckName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().contains("Criminal".replaceAll("[^a-zA-Z0-9]", "").toLowerCase())) {
                        User vendor = userRepository.findById(81l).get();
                        vendorInitiatDto.setVendorId(vendor.getUserId());
                        vendorInitiatDto.setVendorName(vendor.getUserFirstName());
                        String vendorUploadData = new ObjectMapper().writeValueAsString(vendorInitiatDto);
                        userService.saveInitiateVendorChecks(vendorUploadData, null, (licheckRequiredResponseDto.getCandidateuploadS3Documents() == null) ? null : licheckRequiredResponseDto.getCandidateuploadS3Documents().getPathkey());
                    } else if (licheck.getCheckName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().contains("Globaldatabase".replaceAll("[^a-zA-Z0-9]", "").toLowerCase())) {
                        User vendor = userRepository.findById(81l).get();
                        vendorInitiatDto.setVendorId(vendor.getUserId());
                        vendorInitiatDto.setVendorName(vendor.getUserFirstName());
                        String vendorUploadData = new ObjectMapper().writeValueAsString(vendorInitiatDto);
                        userService.saveInitiateVendorChecks(vendorUploadData, null, (licheckRequiredResponseDto.getCandidateuploadS3Documents() == null) ? null : licheckRequiredResponseDto.getCandidateuploadS3Documents().getPathkey());
                    } else if (licheck.getCheckName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().contains("Address".replaceAll("[^a-zA-Z0-9]", "").toLowerCase())) {
                        User vendor = userRepository.findById(81l).get();
                        vendorInitiatDto.setVendorId(vendor.getUserId());
                        vendorInitiatDto.setVendorName(vendor.getUserFirstName());
                        String vendorUploadData = new ObjectMapper().writeValueAsString(vendorInitiatDto);
                        userService.saveInitiateVendorChecks(vendorUploadData, null, (licheckRequiredResponseDto.getCandidateuploadS3Documents() == null) ? null : licheckRequiredResponseDto.getCandidateuploadS3Documents().getPathkey());
                    } else if (licheck.getCheckName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().contains("Education".replaceAll("[^a-zA-Z0-9]", "").toLowerCase())) {
                        User vendor = userRepository.findById(72l).get();
                        vendorInitiatDto.setVendorId(vendor.getUserId());
                        vendorInitiatDto.setVendorName(vendor.getUserFirstName());
                        String vendorUploadData = new ObjectMapper().writeValueAsString(vendorInitiatDto);
                        userService.saveInitiateVendorChecks(vendorUploadData, null, (licheckRequiredResponseDto.getCandidateuploadS3Documents() == null) ? null : licheckRequiredResponseDto.getCandidateuploadS3Documents().getPathkey());
                    } else if (licheck.getCheckName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().contains("Reference".replaceAll("[^a-zA-Z0-9]", "").toLowerCase())) {
                        User vendor = userRepository.findById(72l).get();
                        vendorInitiatDto.setVendorId(vendor.getUserId());
                        vendorInitiatDto.setVendorName(vendor.getUserFirstName());
                        String vendorUploadData = new ObjectMapper().writeValueAsString(vendorInitiatDto);
                        userService.saveInitiateVendorChecks(vendorUploadData, null, (licheckRequiredResponseDto.getCandidateuploadS3Documents() == null) ? null : licheckRequiredResponseDto.getCandidateuploadS3Documents().getPathkey());
                    } else if (licheck.getCheckName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().contains("Drug".replaceAll("[^a-zA-Z0-9]", "").toLowerCase())) {
                        User vendor = userRepository.findById(72l).get();
                        vendorInitiatDto.setVendorId(vendor.getUserId());
                        vendorInitiatDto.setVendorName(vendor.getUserFirstName());
                        String vendorUploadData = new ObjectMapper().writeValueAsString(vendorInitiatDto);
                        userService.saveInitiateVendorChecks(vendorUploadData, null, (licheckRequiredResponseDto.getCandidateuploadS3Documents() == null) ? null : licheckRequiredResponseDto.getCandidateuploadS3Documents().getPathkey());
                    } else if (licheck.getCheckName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().contains("Employment-Physicalvisit".replaceAll("[^a-zA-Z0-9]", "").toLowerCase())) {
                        User vendor = userRepository.findById(72l).get();
                        vendorInitiatDto.setVendorId(vendor.getUserId());
                        vendorInitiatDto.setVendorName(vendor.getUserFirstName());
                        String vendorUploadData = new ObjectMapper().writeValueAsString(vendorInitiatDto);
                        userService.saveInitiateVendorChecks(vendorUploadData, null, (licheckRequiredResponseDto.getCandidateuploadS3Documents() == null) ? null : licheckRequiredResponseDto.getCandidateuploadS3Documents().getPathkey());
                    } else if (licheck.getCheckName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().contains("PAN".replaceAll("[^a-zA-Z0-9]", "").toLowerCase())) {
                        User vendor = userRepository.findById(72l).get();
                        vendorInitiatDto.setVendorId(vendor.getUserId());
                        vendorInitiatDto.setVendorName(vendor.getUserFirstName());
                        String vendorUploadData = new ObjectMapper().writeValueAsString(vendorInitiatDto);
                        userService.saveInitiateVendorChecks(vendorUploadData, null, (licheckRequiredResponseDto.getCandidateuploadS3Documents() == null) ? null : licheckRequiredResponseDto.getCandidateuploadS3Documents().getPathkey());
                    } else if (licheck.getCheckName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().contains("Aadhar".replaceAll("[^a-zA-Z0-9]", "").toLowerCase())) {
                        User vendor = userRepository.findById(72l).get();
                        vendorInitiatDto.setVendorId(vendor.getUserId());
                        vendorInitiatDto.setVendorName(vendor.getUserFirstName());
                        String vendorUploadData = new ObjectMapper().writeValueAsString(vendorInitiatDto);
                        userService.saveInitiateVendorChecks(vendorUploadData, null, (licheckRequiredResponseDto.getCandidateuploadS3Documents() == null) ? null : licheckRequiredResponseDto.getCandidateuploadS3Documents().getPathkey());
                    } else if (licheck.getCheckName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().contains("DrivingLicence".replaceAll("[^a-zA-Z0-9]", "").toLowerCase())) {
                        User vendor = userRepository.findById(72l).get();
                        vendorInitiatDto.setVendorId(vendor.getUserId());
                        vendorInitiatDto.setVendorName(vendor.getUserFirstName());
                        String vendorUploadData = new ObjectMapper().writeValueAsString(vendorInitiatDto);
                        userService.saveInitiateVendorChecks(vendorUploadData, null, (licheckRequiredResponseDto.getCandidateuploadS3Documents() == null) ? null : licheckRequiredResponseDto.getCandidateuploadS3Documents().getPathkey());
                    } else if (licheck.getCheckName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().contains("Creditbureaucheck".replaceAll("[^a-zA-Z0-9]", "").toLowerCase())) {
                        User vendor = userRepository.findById(81l).get();
                        vendorInitiatDto.setVendorId(vendor.getUserId());
                        vendorInitiatDto.setVendorName(vendor.getUserFirstName());
                        String vendorUploadData = new ObjectMapper().writeValueAsString(vendorInitiatDto);
                        userService.saveInitiateVendorChecks(vendorUploadData, null, (licheckRequiredResponseDto.getCandidateuploadS3Documents() == null) ? null : licheckRequiredResponseDto.getCandidateuploadS3Documents().getPathkey());
                    } else if (licheck.getCheckName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().contains("Professional Certificate Check".replaceAll("[^a-zA-Z0-9]", "").toLowerCase())) {
                        User vendor = userRepository.findById(81l).get();
                        vendorInitiatDto.setVendorId(vendor.getUserId());
                        vendorInitiatDto.setVendorName(vendor.getUserFirstName());
                        String vendorUploadData = new ObjectMapper().writeValueAsString(vendorInitiatDto);
                        userService.saveInitiateVendorChecks(vendorUploadData, null, (licheckRequiredResponseDto.getCandidateuploadS3Documents() == null) ? null : licheckRequiredResponseDto.getCandidateuploadS3Documents().getPathkey());
                    }
                }
                if (licheck.getVendorName() != null) {
                    licheckRequiredResponseDto.setVendorName(licheck.getVendorName());
                }
                licheckRequiredResponseDto.setCheckStatus(licheck.getCheckStatus().getCheckStatusCode());

                if (licheck.getCheckStatus().getVendorCheckStatusMasterId() == 7l) {
                    licheckRequiredResponseDtos.add(licheckRequiredResponseDto);
                }
            }
            serviceOutcome.setData(licheckRequiredResponseDtos);
            serviceOutcome.setOutcome(true);
        } catch (Exception e) {
            log.error("exception in findAllNewUploadLiChecksRequiredbyCandidateId() ----" + e.getMessage());
            serviceOutcome.setData(licheckRequiredResponseDtos);
        }
        return serviceOutcome;
    }


    public ServiceOutcome<List<LicheckRequiredResponseDto>> findAllLiChecksRequiredbyCheckStatus(String checkStatus) throws Exception {
        try {

            User user = (SecurityHelper.getCurrentUser() != null) ? SecurityHelper.getCurrentUser() : userRepository.findByUserId(53l);

            ServiceOutcome<List<LicheckRequiredResponseDto>> serviceOutcome = new ServiceOutcome<List<LicheckRequiredResponseDto>>();
            List<LicheckRequiredResponseDto> allLiCheckResponses = new ArrayList<>();

            if (checkStatus.equalsIgnoreCase("NEWUPLOAD")) {
                allLiCheckResponses = liCheckToPerformRepository.findAllLiCheckResponseByCheckStatus(7l);
            }
            if (checkStatus.equalsIgnoreCase("QCPENDING")) {
                allLiCheckResponses = liCheckToPerformRepository.findAllLiCheckResponseByCheckStatus(8l);
            }
            if (checkStatus.equalsIgnoreCase("CLEAR")) {
                allLiCheckResponses = liCheckToPerformRepository.findAllLiCheckResponseByCheckStatus(1l);
            }
            if (checkStatus.equalsIgnoreCase("INPROGRESS")) {
                allLiCheckResponses = liCheckToPerformRepository.findAllLiCheckResponseByCheckStatus(2l);
            }
            if (checkStatus.equalsIgnoreCase("INSUFFICIENCY")) {
                allLiCheckResponses = liCheckToPerformRepository.findAllLiCheckResponseByCheckStatus(3l);
            }
            if (checkStatus.equalsIgnoreCase("MAJORDISCREPANCY")) {
                allLiCheckResponses = liCheckToPerformRepository.findAllLiCheckResponseByCheckStatus(4l);
            }
            if (checkStatus.equalsIgnoreCase("MINORDISCREPANCY")) {
                allLiCheckResponses = liCheckToPerformRepository.findAllLiCheckResponseByCheckStatus(5l);
            }
            if (checkStatus.equalsIgnoreCase("UNABLETOVERIFY")) {
                allLiCheckResponses = liCheckToPerformRepository.findAllLiCheckResponseByCheckStatus(6l);
            }


            allLiCheckResponses.forEach(res -> {

                res.setCreatedBy(user.getUserName());
                ConventionalVendorliChecksToPerform conventionalVendorliChecksToPerform = liCheckToPerformRepository.findById(res.getCheckCode()).get();
                res.setVendorName(conventionalVendorliChecksToPerform.getVendorName());
                res.setSourceName(conventionalVendorliChecksToPerform.getSourceName());
                if (conventionalVendorliChecksToPerform.getRequestId().isEmpty() == false) {
                    res.setRequestID(conventionalVendorliChecksToPerform.getRequestId());
                }

                if (res.getVendorId() != null) {
                    VendorChecks vendorChecks = vendorChecksRepository.findById(res.getVendorId()).get();
                    res.setDocumentName(vendorChecks.getDocumentname());
                }
            });
            if (allLiCheckResponses.isEmpty()) {
                serviceOutcome.setData(new ArrayList<>());
            }
            serviceOutcome.setData(allLiCheckResponses);
            return serviceOutcome;
        } catch (Exception e) {
            log.error(e.getMessage());

            throw new Exception(e.getMessage());
        }
    }


    @Transactional
    public String acknoledgeAfterSavedCandidate(Long requestId) {
        try {
//            log.info("acknoledgeAfterSavedCandidate() starts");
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", environmentVal.getMtGrantType());
            map.add("username", environmentVal.getMtUsername());
            map.add("password", environmentVal.getMtPassword());

            HttpHeaders tokenHeader = new HttpHeaders();
            tokenHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            ResponseEntity<String> responseEntity = null;
            HttpEntity<MultiValueMap<String, String>> requestBodyFormUrlEncoded = new HttpEntity<>(map, tokenHeader);
            responseEntity = restTemplate.postForEntity(environmentVal.getConventionalVendorToken(), requestBodyFormUrlEncoded, String.class);
            JSONObject tokenObject = new JSONObject(responseEntity.getBody());
            String access_token = tokenObject.getString("access_token");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + access_token);
            headers.set("Content-Type", "application/json");
            headers.setContentType(MediaType.APPLICATION_JSON);
            ConventionalVendorCandidatesSubmitted conventionalCandidate = conventionalCandidatesSubmittedRepository.findByRequestId(String.valueOf(requestId));
            log.info("Recived With Candidate id " + conventionalCandidate.getCandidateId() + "   and Request Id   " + conventionalCandidate.getRequestId());
            AcknoledgementDto acknoledgementDto = new AcknoledgementDto();
            acknoledgementDto.setCandidateID(String.valueOf(conventionalCandidate.getCandidateId()));
            acknoledgementDto.setPSNO(conventionalCandidate.getPsNo());
            acknoledgementDto.setRequestID(conventionalCandidate.getRequestId());
            acknoledgementDto.setVENDORID(conventionalCandidate.getVendorId());
            acknoledgementDto.setVendorReferenceID(String.valueOf(conventionalCandidate.getApplicantId()));
            ArrayList<AcknoledgementDto> acknoledgementDtos = new ArrayList<>();
            acknoledgementDtos.add(acknoledgementDto);
            HttpEntity<List<AcknoledgementDto>> acknoledgementDtoHttpEntity = new HttpEntity<>(acknoledgementDtos, headers);
            ResponseEntity<String> acknoledgementData = restTemplate.exchange(environmentVal.getConventionalUpdateBGVRequestAcknowledgement(), HttpMethod.POST, acknoledgementDtoHttpEntity, String.class);
            if (acknoledgementData.getStatusCodeValue() == 200) {
                conventionalCandidate.setFetchLicheckAndCandidateData(false);
                ConventionalVendorCandidatesSubmitted updateFetchLicheck = conventionalCandidatesSubmittedRepository.save(conventionalCandidate);
            }
            log.info("Acknoledgement Sent To Ltm After  ---" + conventionalCandidate.getRequestId() + "and  Candidate  id" + conventionalCandidate.getCandidateId() + "---- response ----" + acknoledgementData);
//            log.info("acknoledgeAfterSavedCandidate() ends");
        } catch (Exception e) {
            log.info("acknoledgeAfterSavedCandidate() exception" + e.getMessage());
        }
        return "Acknoledged";
    }


    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackFor = Exception.class)
    public ServiceOutcome<SubmittedCandidates> saveConventionalVendorSubmittedCandidates(String VendorID, boolean schedularConfdition) throws Exception {
        ServiceOutcome<SubmittedCandidates> serviceOutcome = new ServiceOutcome<>();
        List<ConventionalVendorCandidatesSubmitted> emailSendCandidateList = new ArrayList<>();
        try {
            log.debug("saveConventionalVendorSubmittedCandidates() starts");
            ServiceOutcome<LicheckRequiredResponseDto> svcOutcome = new ServiceOutcome<LicheckRequiredResponseDto>();
            User user = (SecurityHelper.getCurrentUser() != null) ? SecurityHelper.getCurrentUser() : userRepository.findByUserId(53l);
            //To generate token first
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", environmentVal.getMtGrantType());
            map.add("username", environmentVal.getMtUsername());
            map.add("password", environmentVal.getMtPassword());
            HttpHeaders tokenHeader = new HttpHeaders();
            tokenHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            ResponseEntity<String> responseEntity = null;
            HttpEntity<MultiValueMap<String, String>> requestBodyFormUrlEncoded = new HttpEntity<>(map, tokenHeader);
            responseEntity = restTemplate.postForEntity(environmentVal.getConventionalVendorToken(), requestBodyFormUrlEncoded, String.class);
            JSONObject tokenObject = new JSONObject(responseEntity.getBody());
            String access_token = tokenObject.getString("access_token");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + access_token);
            headers.set("Content-Type", "application/json");
            HttpEntity<String> vendorIdHttp = new HttpEntity<>(VendorID, headers);
            ResponseEntity<String> candidateResponse = restTemplate.exchange(environmentVal.getConventionalVendorFetchVendorRequestDetails(), HttpMethod.POST, vendorIdHttp, String.class);
            String message = candidateResponse.getBody();
            JSONArray obj1 = new JSONArray(message);
            List<JSONArray> list = Arrays.asList(obj1);
            List<JSONObject> collect = IntStream.range(0, obj1.length()).mapToObj(index -> ((JSONObject) obj1.get(index))).collect(Collectors.toList());
            for (JSONObject candidate : collect) {
                Long requestID = candidate.getLong("RequestID");
                log.info("Processing candidate with Request ID: " + requestID);
                Boolean candidateExists = conventionalCandidatesSubmittedRepository.existsByRequestId(String.valueOf(requestID));
                log.debug("Candidate existence check completed for Request ID: " + requestID + " - Exists: " + candidateExists);
                if (candidate.getString("RequestType").equalsIgnoreCase("InsufficiencyClearance") == true) {
                    log.info("Request type is 'InsufficiencyClearance' for Request ID: " + requestID);
                    ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(String.valueOf(requestID));
                    if (conventionalVendorCandidatesSubmitted.getRequestType().equalsIgnoreCase("InsufficiencyClearance") == false) {
                        log.info("Updating request type to 'InsufficiencyClearance' for Request ID: " + requestID);
                        conventionalVendorCandidatesSubmitted.setOldRequestType(conventionalVendorCandidatesSubmitted.getRequestType());
                        conventionalVendorCandidatesSubmitted.setRequestType("InsufficiencyClearance");
                        conventionalVendorCandidatesSubmitted.setFetchLicheckAndCandidateData(true);
                        ConventionalVendorCandidatesSubmitted savedSubmittedCandidates = conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted);
                        log.info("Updated Request Type and Fetch Flag for Request ID  --" + conventionalVendorCandidatesSubmitted.getRequestId() + "---Changed to --- " + savedSubmittedCandidates.getRequestType() + " ---fetch Flag status --" + conventionalVendorCandidatesSubmitted.getFetchLicheckAndCandidateData());
                        log.debug("Email sent for insufficiency clearance starts" + environmentVal.getEmailReciver() + " -- copied reciver" + environmentVal.getCopiedReciver());
                        emailSentTask.sendEmailOnRaisedInsufficiency(conventionalVendorCandidatesSubmitted, new ArrayList<>(), "", environmentVal.getEmailReciver(), environmentVal.getCopiedReciver());
                        log.debug("Email sent for insufficiency clearance ends");
                    }
                }
                if (candidateExists == false) {
                    log.info("Candidate does not exist in the repository for Request ID: " + requestID + ". Creating a new entry.");
                    ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = new ConventionalVendorCandidatesSubmitted();
                    if (candidate.getString("CandidateID").isEmpty() || candidate.getString("CandidateID").isBlank()) {
                        conventionalVendorCandidatesSubmitted.setCandidateId(String.valueOf(candidate.getLong("PSNO")));
                    } else {
                        conventionalVendorCandidatesSubmitted.setCandidateId(String.valueOf(candidate.getString("CandidateID")));
                    }
                    conventionalVendorCandidatesSubmitted.setVendorId(candidate.getString("VendorID"));
                    conventionalVendorCandidatesSubmitted.setName(candidate.getString("Name"));
                    conventionalVendorCandidatesSubmitted.setPsNo(candidate.getString("PSNO"));
                    conventionalVendorCandidatesSubmitted.setRequestId(candidate.getString("RequestID"));
                    conventionalVendorCandidatesSubmitted.setRequestType(candidate.getString("RequestType"));
                    StatusMaster newupload = statusMasterRepository.findByStatusCode("NEWUPLOAD");
                    conventionalVendorCandidatesSubmitted.setStatus(newupload);
                    conventionalVendorCandidatesSubmitted.setCreatedBy(user);
                    conventionalVendorCandidatesSubmitted.setCreatedOn(new Date());
                    Random rnd = new Random();
                    int n = 100000 + rnd.nextInt(900000);
                    conventionalVendorCandidatesSubmitted.setApplicantId(n);
                    conventionalVendorCandidatesSubmitted.setFetchLicheckAndCandidateData(true);
                    ConventionalVendorCandidatesSubmitted savedSubmittedCandidates = conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted);
                    log.info("New candidate saved with Candidate ID: " + savedSubmittedCandidates.getCandidateId() + " for Request ID: " + requestID);
                    emailSendCandidateList.add(savedSubmittedCandidates);
                }
                //for updatating the different bgv intiation and email triggering for change in bgv
                ConventionalVendorCandidatesSubmitted requestTypeFetch = conventionalCandidatesSubmittedRepository.findByRequestId(String.valueOf(requestID));
                if (requestTypeFetch.getRequestType().equalsIgnoreCase(candidate.getString("RequestType")) == false
                        && candidate.getString("RequestType").equals("InsufficiencyClearance") == false) {
                    log.info("Request Type change detected for Request ID: " + requestID + " - Previous Type: " + requestTypeFetch.getRequestType() + " ---fetch Flag status Before --" + requestTypeFetch.getFetchLicheckAndCandidateData());
                    requestTypeFetch.setRequestType(candidate.getString("RequestType"));
                    if (candidate.getString("CandidateID").isEmpty() || candidate.getString("CandidateID").isBlank()) {
                        requestTypeFetch.setCandidateId(String.valueOf(candidate.getLong("PSNO")));
                    } else {
                        requestTypeFetch.setCandidateId(candidate.getString("CandidateID"));
                    }
                    requestTypeFetch.setVendorId(candidate.getString("VendorID"));
                    requestTypeFetch.setName(candidate.getString("Name"));
                    requestTypeFetch.setPsNo(candidate.getString("PSNO"));
                    requestTypeFetch.setRequestId(candidate.getString("RequestID"));
                    requestTypeFetch.setFetchLicheckAndCandidateData(true);
                    ConventionalVendorCandidatesSubmitted updatedRequestType = conventionalVendorCandidatesSubmittedRepository.save(requestTypeFetch);
                    log.info("Updated Request Type to " + updatedRequestType.getRequestType() + " for Request ID: " + requestID + " ---fetch Flag status After Update --" + requestTypeFetch.getFetchLicheckAndCandidateData());
                    emailSendCandidateList.add(updatedRequestType);
                }

                if (candidate.getString("RequestType").equalsIgnoreCase("STOPBGV") == true) {
                    ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(String.valueOf(requestID));
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    Date currentDate = new Date();
                    String formattedDate = dateFormat.format(currentDate);
                    conventionalVendorCandidatesSubmitted.setStopCheckRecivedDate(formattedDate);
                    log.info("saving the stop bgv" + conventionalVendorCandidatesSubmitted.getRequestId());
                    ConventionalVendorCandidatesSubmitted savedSubmittedCandidates = conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted);
                    FetchVendorConventionalCandidateDto fetchVendorConventionalCandidateDto = new FetchVendorConventionalCandidateDto(conventionalVendorCandidatesSubmitted.getRequestId(), String.valueOf(conventionalVendorCandidatesSubmitted.getCandidateId()), conventionalVendorCandidatesSubmitted.getPsNo(), conventionalVendorCandidatesSubmitted.getVendorId(), conventionalVendorCandidatesSubmitted.getRequestType());
                    HttpEntity<FetchVendorConventionalCandidateDto> liCheckDtoHttpEntity = new HttpEntity<>(fetchVendorConventionalCandidateDto, headers);
                    ResponseEntity<String> icheckRepsonse = restTemplate.exchange(environmentVal.getConventionalVendorFetchVendorChecks(), HttpMethod.POST, liCheckDtoHttpEntity, String.class);
                    String responseMessage = icheckRepsonse.getBody();
                    JSONObject checkresponse = new JSONObject(responseMessage);
                    if (checkresponse.isNull("liStopChecks") == false) {
                        JSONObject liStopChecks = checkresponse.getJSONObject("liStopChecks");
                        if (liStopChecks.isNull("liChecksToStop") == false) {
                            JSONArray liChecksToStop = liStopChecks.getJSONArray("liChecksToStop");
                            List<JSONObject> stopCollect = IntStream.range(0, liChecksToStop.length()).mapToObj(index -> ((JSONObject) liChecksToStop.get(index))).collect(Collectors.toList());
                            for (JSONObject licheckReq : stopCollect) {
                                Long checkUniqueId = licheckReq.getLong("Check_Unique_ID");

                                ConventionalVendorliChecksToPerform byCheckUniqueId = liCheckToPerformRepository.findByCheckUniqueId(checkUniqueId);
                                if (byCheckUniqueId != null) {
                                    if (byCheckUniqueId.getCheckStatus().getVendorCheckStatusMasterId() != 1l) {
                                        byCheckUniqueId.setStopCheck("TRUE");
                                        ConventionalVendorliChecksToPerform save = liCheckToPerformRepository.save(byCheckUniqueId);
                                        log.info("stopped check by check unique id" + save.getCheckUniqueId());
                                    }
                                }
                                acknoledgeAfterSavedCandidate(checkresponse.getLong("RequestID"));
                                log.info("acknoledged after all check get stopped for Stopped checks");
                            }
                        }
                    }
                }
                log.debug("saveConventionalVendorSubmittedCandidates() ends");
                if (schedularConfdition == true) {
//                    log.info("scheduler for request id    :" + requestID + "   :   starts");
//                    findConventionalCandidateByCandidateId(requestID);
//                    log.info("scheduler for request id    :" + requestID + "   :   ends");
//                    log.info("scheduler for vendorinitiation    :" + requestID + "   :   starts");
//                    findAllNewUploadLiChecksRequiredbyCandidateId(String.valueOf(requestID));
//                    log.info("scheduler for vendorinitiation    :" + requestID + "   :   ends");

                }

            }
            if (emailSendCandidateList.isEmpty() == false) {
                emailSentTask.sendEmailOnSaveCandidates(emailSendCandidateList, environmentVal.getEmailReciver(), environmentVal.getCopiedReciver());
            }
        } catch (Exception e) {
            serviceOutcome.setMessage(e.getMessage());
            serviceOutcome.setOutcome(false);
            serviceOutcome.setStatus(e.getMessage());
            log.error("exception occured in saveConventionalVendorSubmittedCandidates()" + e.getMessage());
        }
        return serviceOutcome;

    }


    public ServiceOutcome<List<SubmittedCandidates>> fetchReportUploadPendingDetails(String VendorID) throws Exception {
        ServiceOutcome<List<SubmittedCandidates>> serviceOutcome = new ServiceOutcome<>();
        try {
            log.debug("FetchReportUploadPendingDetails() starts");
            User user = (SecurityHelper.getCurrentUser() != null) ? SecurityHelper.getCurrentUser() : userRepository.findByUserId(53l);
            //To generate token first
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", environmentVal.getMtGrantType());
            map.add("username", environmentVal.getMtUsername());
            map.add("password", environmentVal.getMtPassword());
            HttpHeaders tokenHeader = new HttpHeaders();
            tokenHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            ResponseEntity<String> responseEntity = null;
            HttpEntity<MultiValueMap<String, String>> requestBodyFormUrlEncoded = new HttpEntity<>(map, tokenHeader);
            responseEntity = restTemplate.postForEntity(environmentVal.getConventionalVendorToken(), requestBodyFormUrlEncoded, String.class);
            JSONObject tokenObject = new JSONObject(responseEntity.getBody());
            String access_token = tokenObject.getString("access_token");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + access_token);
            headers.set("Content-Type", "application/json");
            HttpEntity<String> vendorIdHttp = new HttpEntity<>(VendorID, headers);
            ResponseEntity<String> candidateResponse = restTemplate.exchange(environmentVal.getConventionalVendorFetchPendingProofs(), HttpMethod.POST, vendorIdHttp, String.class);
            String message = candidateResponse.getBody();
            JSONArray obj1 = new JSONArray(message);
            List<JSONArray> list = Arrays.asList(obj1);
            List<JSONObject> collect = IntStream.range(0, obj1.length()).mapToObj(index -> ((JSONObject) obj1.get(index))).collect(Collectors.toList());

            List<SubmittedCandidates> candidatesList = collect.stream().map(json -> {
                SubmittedCandidates candidate = new SubmittedCandidates();
                candidate.setCandidateId(json.optString("CandidateID"));
                candidate.setPsNo(json.optString("PSNO"));
                candidate.setName(json.optString("Name"));
                String requestId = json.optString("RequestID");
                if (requestId != null && !requestId.isEmpty()) {
                    ConventionalVendorCandidatesSubmitted requestID = conventionalCandidatesSubmittedRepository.findByRequestId(requestId);
                    if (requestID != null && requestID.getStatus() != null) {
                        candidate.setStatus(requestID.getStatus().getStatusCode());
                    } else {
                        candidate.setStatus("NO_STATUS"); // Set default status if null
                    }
                    CandidatePendingReportHistory requestType = conventionalPendingReportHistoryRepository.findByRequestIdAndRequestTypeLike(Long.valueOf(requestId), json.optString("RequestType"));

                    if (requestType != null) {
                        // Using Optional to handle potential null value
                        String reportResponse = Optional.ofNullable(requestType.getReportStatusReponse()).orElse("");
                        candidate.setReportResponse(reportResponse);
                    }


                } else {
                    candidate.setStatus("NO_STATUS"); // Set default status if RequestID is null/empty
                }

                candidate.setRequestId(json.optString("RequestID"));
                candidate.setRequestType(json.optString("RequestType"));
                candidate.setVendorId(json.optString("VendorID"));
                return candidate;
            }).collect(Collectors.toList());

            serviceOutcome.setOutcome(true);
            serviceOutcome.setData(candidatesList);
            serviceOutcome.setMessage("Fetched Sucessfully");


        } catch (Exception e) {
            serviceOutcome.setMessage(e.getMessage());
            serviceOutcome.setOutcome(false);
            serviceOutcome.setStatus(e.getMessage());
            log.error("exception occured in FetchReportUploadPendingDetails()" + e.getMessage());
        }
        return serviceOutcome;

    }


    public ServiceOutcome<SubmittedCandidates> triggerCandidateDataAndCheckData(String VendorID, String triggerRequestId) throws Exception {
        ServiceOutcome<SubmittedCandidates> serviceOutcome = new ServiceOutcome<>();
        try {
            log.debug("saveConventionalVendorSubmittedCandidates() starts");
            ServiceOutcome<LicheckRequiredResponseDto> svcOutcome = new ServiceOutcome<LicheckRequiredResponseDto>();
            User user = (SecurityHelper.getCurrentUser() != null) ? SecurityHelper.getCurrentUser() : userRepository.findByUserId(53l);
            //To generate token first
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", environmentVal.getMtGrantType());
            map.add("username", environmentVal.getMtUsername());
            map.add("password", environmentVal.getMtPassword());
            HttpHeaders tokenHeader = new HttpHeaders();
            tokenHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            ResponseEntity<String> responseEntity = null;
            HttpEntity<MultiValueMap<String, String>> requestBodyFormUrlEncoded = new HttpEntity<>(map, tokenHeader);
            responseEntity = restTemplate.postForEntity(environmentVal.getConventionalVendorToken(), requestBodyFormUrlEncoded, String.class);
            JSONObject tokenObject = new JSONObject(responseEntity.getBody());
            String access_token = tokenObject.getString("access_token");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + access_token);
            headers.set("Content-Type", "application/json");
            HttpEntity<String> vendorIdHttp = new HttpEntity<>(VendorID, headers);
            ResponseEntity<String> candidateResponse = restTemplate.exchange(environmentVal.getConventionalVendorFetchVendorRequestDetails(), HttpMethod.POST, vendorIdHttp, String.class);
            String message = candidateResponse.getBody();
            JSONArray obj1 = new JSONArray(message);
            List<JSONArray> list = Arrays.asList(obj1);
            List<JSONObject> collect = IntStream.range(0, obj1.length()).mapToObj(index -> ((JSONObject) obj1.get(index))).collect(Collectors.toList());
            List<ConventionalVendorCandidatesSubmitted> all = conventionalCandidatesSubmittedRepository.findAll();
            if (collect.isEmpty() == true) {
                serviceOutcome.setOutcome(false);
                serviceOutcome.setMessage(null);
                serviceOutcome.setData(new SubmittedCandidates());
            }
            for (JSONObject candidate : collect) {
                String candidateId = candidate.getString("CandidateID").isBlank() ? candidate.getString("PSNO") : candidate.getString("CandidateID");
                Long requestID = candidate.getLong("RequestID");
                long triggre = Long.parseLong(triggerRequestId);
                if (triggre == requestID) {
                    Boolean candidateExists = conventionalCandidatesSubmittedRepository.existsByRequestId(String.valueOf(requestID));
                    if (candidate.getString("RequestType").equalsIgnoreCase("InsufficiencyClearance") == true) {
                        ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(String.valueOf(requestID));
                        if (conventionalVendorCandidatesSubmitted.getRequestType().equalsIgnoreCase("InsufficiencyClearance") == false) {
                            conventionalVendorCandidatesSubmitted.setOldRequestType(conventionalVendorCandidatesSubmitted.getRequestType());
                            conventionalVendorCandidatesSubmitted.setRequestType("InsufficiencyClearance");
                            log.debug("saving the oldrequest type");
                            ConventionalVendorCandidatesSubmitted savedSubmittedCandidates = conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted);
                        }
                    }
                    log.debug("scheduler for request id    :" + requestID + "   :   starts");
                    findConventionalCandidateByCandidateId(requestID);
                    log.debug("scheduler for request id    :" + requestID + "   :   ends");
                    serviceOutcome.setOutcome(true);
                    serviceOutcome.setMessage("Insufficiency Resumittted, No need Of Remarks");

                } else {
                    serviceOutcome.setOutcome(false);
                    serviceOutcome.setMessage(null);
                    serviceOutcome.setData(new SubmittedCandidates());
                }
            }

        } catch (Exception e) {
            serviceOutcome.setMessage(e.getMessage());
//            serviceOutcome.setOutcome(false);
            serviceOutcome.setStatus(e.getMessage());
            log.error("exception occured in saveConventionalVendorSubmittedCandidates()" + e.getMessage());
        }
        return serviceOutcome;

    }


    @Override
    public ServiceOutcome<List<SubmittedCandidates>> findAllConventionalVendorSubmittedCandidates() throws Exception {
        ServiceOutcome<List<SubmittedCandidates>> listServiceOutcome = new ServiceOutcome<>();
        User user = (SecurityHelper.getCurrentUser() != null) ? SecurityHelper.getCurrentUser() : userRepository.findByUserId(53l);
        ;

        try {
            List<SubmittedCandidates> allSubmittedCandidates = new ArrayList<>();
            allSubmittedCandidates.forEach(resp -> {
                resp.setCreatedBy(user.getUserName());
            });
            if (allSubmittedCandidates.isEmpty()) {
                listServiceOutcome.setData(null);
            }
            listServiceOutcome.setData(allSubmittedCandidates);
        } catch (Exception e) {
            log.error(e.getMessage());

            throw new Exception(e.getMessage());

        }
        return listServiceOutcome;
    }


    @Override
    public ServiceOutcome<ConventionalCandidateDocDto> saveConventionalCandidateDocumentInfo(FetchVendorConventionalCandidateDto fetchVendorConventionalCandidateDto) throws Exception {
        ServiceOutcome<ConventionalCandidateDocDto> svcOutcome = new ServiceOutcome<ConventionalCandidateDocDto>();
        try {
            log.debug("saveConventionalCandidateDocumentInfo starts()");
            User user = (SecurityHelper.getCurrentUser() != null) ? SecurityHelper.getCurrentUser() : userRepository.findByUserId(53l);

            //To generate token first
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", environmentVal.getMtGrantType());
            map.add("username", environmentVal.getMtUsername());
            map.add("password", environmentVal.getMtPassword());

            HttpHeaders tokenHeader = new HttpHeaders();
            tokenHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            ResponseEntity<String> responseEntity = null;
            HttpEntity<MultiValueMap<String, String>> requestBodyFormUrlEncoded = new HttpEntity<>(map, tokenHeader);
            responseEntity = restTemplate.postForEntity(environmentVal.getConventionalVendorToken(), requestBodyFormUrlEncoded, String.class);
            JSONObject tokenObject = new JSONObject(responseEntity.getBody());
            String access_token = tokenObject.getString("access_token");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + access_token);
            headers.set("Content-Type", "application/json");
            HttpEntity<FetchVendorConventionalCandidateDto> liCheckDtoHttpEntity = new HttpEntity<>(fetchVendorConventionalCandidateDto, headers);
            ResponseEntity<String> icheckRepsonse = restTemplate.exchange(environmentVal.getConventionalVendorFetchVendorChecks(), HttpMethod.POST, liCheckDtoHttpEntity, String.class);
//            log.info("Response from lICheck response  API " + icheckRepsonse);
            String message = icheckRepsonse.getBody(); //.get("message").toString().replaceAll("=", ":")
            JSONObject obj1 = new JSONObject(message);
            JSONObject liCandidateInformation = obj1.getJSONObject("liCandidateInformation");
            if (liCandidateInformation.isNull("liCandidateDocumentInfo") == false) {
                JSONArray liCandidateDocumentInfo = liCandidateInformation.getJSONArray("liCandidateDocumentInfo");
                //getting the json document array and getting the individual document
                List<JSONObject> collect = IntStream.range(0, liCandidateDocumentInfo.length()).mapToObj(index -> ((JSONObject) liCandidateDocumentInfo.get(index))).collect(Collectors.toList());
                for (JSONObject jsonObject : collect) {
                    if (fetchVendorConventionalCandidateDto.getRequestType().equalsIgnoreCase("InsufficiencyClearance") == false) {
                        Boolean existsByRequestID = conventionalCandidateDocumentInfoRepository.existsByRequestID(obj1.getString("RequestID"));
                        if (existsByRequestID == false) {
                            ConventionalCandidateDocumentInfo conventionalCandidateDocumentInfo = new ConventionalCandidateDocumentInfo();
                            conventionalCandidateDocumentInfo.setCandidateId(String.valueOf(obj1.getString("CandidateID").isBlank() ? obj1.getLong("PSNO") : obj1.getString("CandidateID")));
                            conventionalCandidateDocumentInfo.setDocumentName(jsonObject.getString("DocumentName"));
                            conventionalCandidateDocumentInfo.setFileType(jsonObject.getString("FileType"));
                            conventionalCandidateDocumentInfo.setResubmitted(false);
                            conventionalCandidateDocumentInfo.setRequestID(obj1.getString("RequestID"));
                            byte[] data =  Base64.getDecoder().decode(jsonObject.getString("DocumentAttachment"));
                            //unzipping and creating
                            Path resourcePath = Paths.get("src", "main", "resources", "temp");
                            File file3 = resourcePath.toFile();
                            String absolutePath = file3.getAbsolutePath();
                            if (file3.exists() == false) {
                                file3.mkdir();
                            }
                            String separator = File.separator;
                            String pathtocreate = absolutePath + separator + obj1.getString("RequestID");
//                            log.info("pathserparator" + pathtocreate);
                            File pathofcreate = new File(pathtocreate);
                            if (pathofcreate.exists() == false) {
                                pathofcreate.mkdir();
                            }
                            //unzipped the data to  a file
                            String slash = fileUtil.unzip(data, pathofcreate.toString());
                            if (slash.isEmpty() == false) {
                                if (!file3.exists() || !file3.isDirectory()) {
                                    throw new Exception("The folder does not exist or is not a directory.");
                                }

                                File[] files = pathofcreate.listFiles();
                                if (files == null) {
                                    throw new Exception("The folder does not contain enough files.");
                                }
                                File folder = new File(pathtocreate.toString() + separator + slash);


                                if (!folder.exists() || !folder.isDirectory()) {
                                    throw new Exception("The folder does not exist or is not a directory.");
                                }
                                File[] filesdolder = folder.listFiles();
                                if (files == null || files.length < 1) {
                                    throw new Exception("The folder does not contain enough files.");
                                }

                                String folderKey = "Candidate/Convetional/" + obj1.getString("RequestID") + "/" + "New";
                                ObjectMetadata metadata = new ObjectMetadata();
                                String precisedUrlOfFolder = awsUtils.uploadEmptyFolderAndGeneratePrecisedUrl(DIGIVERIFIER_DOC_BUCKET_NAME, folderKey);
                                // Upload the files to the folder
                                for (File file : filesdolder) {
                                    String key = folderKey + file.getName();
                                    PutObjectRequest request = new PutObjectRequest(DIGIVERIFIER_DOC_BUCKET_NAME, key, file);
                                    s3Client.putObject(request);
                                }
                                conventionalCandidateDocumentInfo.setCreatedBy(user);
                                conventionalCandidateDocumentInfo.setDocumentUrl(folderKey);
                                conventionalCandidateDocumentInfo.setCreatedOn(new Date());
                                ConventionalCandidateDocumentInfo save = conventionalCandidateDocumentInfoRepository.save(conventionalCandidateDocumentInfo);
                                ConventionalCandidateDocDto conventionalCandidateDocDto = new ConventionalCandidateDocDto(save.getDocumentName(), save.getDocumentUrl(), save.getFileType());
                                svcOutcome.setData(conventionalCandidateDocDto);
//                            FileUtils.deleteDirectory(file3);
                            } else {
                                if (!file3.exists() || !file3.isDirectory()) {
                                    throw new Exception("The folder does not exist or is not a directory.");
                                }

                                File[] files = file3.listFiles();
                                if (files == null) {
                                    throw new Exception("The folder does not contain enough files.");
                                }
                                File folder = new File(file3.toString() + separator + obj1.getString("RequestID"));

                                if (!folder.exists() || !folder.isDirectory()) {
                                    throw new Exception("The folder does not exist or is not a directory.");
                                }
                                File[] filesdolder = folder.listFiles();
                                if (files == null || files.length < 1) {
                                    throw new Exception("The folder does not contain enough files.");
                                }

                                String folderKey = "Candidate/Convetional/" + obj1.getString("RequestID") + "/" + "New";
                                ObjectMetadata metadata = new ObjectMetadata();
                                String precisedUrlOfFolder = awsUtils.uploadEmptyFolderAndGeneratePrecisedUrl(DIGIVERIFIER_DOC_BUCKET_NAME, folderKey);
                                // Upload the files to the folder
                                for (File file : filesdolder) {
                                    String key = folderKey + file.getName();
                                    PutObjectRequest request = new PutObjectRequest(DIGIVERIFIER_DOC_BUCKET_NAME, key, file);
                                    s3Client.putObject(request);
                                }
                                conventionalCandidateDocumentInfo.setCreatedBy(user);
                                conventionalCandidateDocumentInfo.setDocumentUrl(folderKey);
                                conventionalCandidateDocumentInfo.setCreatedOn(new Date());
                                ConventionalCandidateDocumentInfo save = conventionalCandidateDocumentInfoRepository.save(conventionalCandidateDocumentInfo);
                                ConventionalCandidateDocDto conventionalCandidateDocDto = new ConventionalCandidateDocDto(save.getDocumentName(), save.getDocumentUrl(), save.getFileType());
                                svcOutcome.setData(conventionalCandidateDocDto);
//                            FileUtils.deleteDirectory(file3);
                            }
                        }

                    } else {
                        log.info("for insufficiency  clearance data adding doucment in s3");
                        ConventionalCandidateDocumentInfo conventionalCandidateDocumentInfo = conventionalCandidateDocumentInfoRepository.findByRequestIdForInsufficiency(obj1.getString("RequestID"));
                        if (conventionalCandidateDocumentInfo.isResubmitted() == false) {
                            conventionalCandidateDocumentInfo.setCandidateId(String.valueOf(obj1.getString("CandidateID").isBlank() ? obj1.getLong("PSNO") : obj1.getString("CandidateID")));
                            conventionalCandidateDocumentInfo.setDocumentName(jsonObject.getString("DocumentName"));
                            conventionalCandidateDocumentInfo.setFileType(jsonObject.getString("FileType"));
                            conventionalCandidateDocumentInfo.setRequestID(obj1.getString("RequestID"));
                            byte[] data =  Base64.getDecoder().decode(jsonObject.getString("DocumentAttachment"));
                            //unzipping and creating
                            Path resourcePath = Paths.get("src", "main", "resources", "temp");
                            File file3 = resourcePath.toFile();
                            String absolutePath = file3.getAbsolutePath();
                            if (file3.exists() == false) {
                                file3.mkdir();
                            }
                            String separator = File.separator;
                            String pathtocreate = absolutePath + separator + obj1.getString("RequestID");
//                            log.info("pathserparator" + pathtocreate);
                            File pathofcreate = new File(pathtocreate);
                            if (pathofcreate.exists() == false) {
                                pathofcreate.mkdir();
                            }
                            //unzipped the data to  a file
                            String slash = fileUtil.unzip(data, pathofcreate.toString());
                            if (slash.isEmpty() == false) {
                                if (!file3.exists() || !file3.isDirectory()) {
                                    throw new Exception("The folder does not exist or is not a directory.");
                                }

                                File[] files = pathofcreate.listFiles();
                                if (files == null) {
                                    throw new Exception("The folder does not contain enough files.");
                                }
                                File folder = new File(pathtocreate.toString() + separator + slash);


                                if (!folder.exists() || !folder.isDirectory()) {
                                    throw new Exception("The folder does not exist or is not a directory.");
                                }
                                File[] filesdolder = folder.listFiles();
                                if (files == null || files.length < 1) {
                                    throw new Exception("The folder does not contain enough files.");
                                }
                                List<ConventionalCandidateDocumentInfo> byCandidateId = conventionalCandidateDocumentInfoRepository.findByRequestID(obj1.getString("RequestID"));
                                // Upload the files to the folder
                                byCandidateId.forEach(byCandidateId1 -> {
                                    for (File file : filesdolder) {
                                        String key = byCandidateId1.getDocumentName() + file.getName() + "Resubmitted";
                                        PutObjectRequest request = new PutObjectRequest(DIGIVERIFIER_DOC_BUCKET_NAME, key, file);
                                        s3Client.putObject(request);
                                    }
                                    conventionalCandidateDocumentInfo.setCreatedBy(user);
                                    conventionalCandidateDocumentInfo.setResubmitted(true);
                                    conventionalCandidateDocumentInfo.setDocumentUrl(byCandidateId1.getDocumentUrl());
                                    conventionalCandidateDocumentInfo.setCreatedOn(new Date());
                                    ConventionalCandidateDocumentInfo save = conventionalCandidateDocumentInfoRepository.save(conventionalCandidateDocumentInfo);
                                    ConventionalCandidateDocDto conventionalCandidateDocDto = new ConventionalCandidateDocDto(save.getDocumentName(), save.getDocumentUrl(), save.getFileType());
                                    svcOutcome.setData(conventionalCandidateDocDto);

                                });
                            }
                        }
                    }
                }
            }
            log.info("saveConventionalCandidateDocumentInfo(ends)");
        } catch (Exception e) {
            log.error("exception occured in saveConventionalCandidateDocumentInfo()" + e.getMessage());

        }
        return svcOutcome;
    }

    @Override
    public ServiceOutcome<List<ConventionalCandidateDocDto>> findAllConventionalCandidateDocumentInfo() throws Exception {
        ServiceOutcome<List<ConventionalCandidateDocDto>> listServiceOutcome = new ServiceOutcome<>();

        try {
            User user = (SecurityHelper.getCurrentUser() != null) ? SecurityHelper.getCurrentUser() : userRepository.findByUserId(53l);
            ;

            List<ConventionalCandidateDocDto> allConventionalCandidateDocs = conventionalCandidateDocumentInfoRepository.findAllConventionalCandidateDocs();
            if (allConventionalCandidateDocs.isEmpty()) {
                listServiceOutcome.setData(null);
            }
            allConventionalCandidateDocs.forEach(resp -> {
                resp.setCreatedBy(user.getUserName());
            });
            listServiceOutcome.setData(allConventionalCandidateDocs);
        } catch (Exception e) {
            log.error(e.getMessage());

            throw new Exception(e.getMessage());

        }
        return listServiceOutcome;
    }

    @Override
    public ServiceOutcome<CandidateuploadS3Documents> findAllfilesUploadedurls(String requestId, String checkName) throws Exception {
        ServiceOutcome<CandidateuploadS3Documents> listServiceOutcome = new ServiceOutcome<>();
        ArrayList<CandidateuploadS3Documents> candidateuploadS3Documents = new ArrayList<>();
        List<ConventionalCandidateDocumentInfo> filteredList = new ArrayList<>();
        try {
            List<ConventionalCandidateDocumentInfo> documentsList = conventionalCandidateDocumentInfoRepository.findByRequestID(requestId);
            String modifiedCheckName = checkName.replaceAll("[\\s-]", "").toLowerCase();
            List<ConventionalCandidateDocumentInfo> matchingDocuments = documentsList.stream().filter(documentInfo -> {
                String documentUrl = documentInfo.getDocumentUrl();
                String[] parts = documentUrl.split("/");
                String lastPart = parts[parts.length - 2].toLowerCase();
                lastPart = lastPart = lastPart.replaceAll("\\s", "");
                boolean checkDocMatch;
                checkDocMatch = modifiedCheckName.contains(lastPart.toLowerCase()) || modifiedCheckName.toLowerCase().contains(lastPart.toLowerCase());
                if (checkDocMatch == false) {
                    if (lastPart.contains("education")) {
                        if (modifiedCheckName.toLowerCase().contains("undergraduate")) {
                            checkDocMatch = true;
                        } else if (modifiedCheckName.toLowerCase().contains("postgraduate")) {
                            // Handle postgraduate case
                            checkDocMatch = true;
                        } else if (modifiedCheckName.toLowerCase().contains("diploma")) {
                            // Handle diploma case
                            checkDocMatch = true;
                        } else if (modifiedCheckName.toLowerCase().contains("highschool")) {
                            // Handle high school case
                            checkDocMatch = true;
                        } else if (modifiedCheckName.toLowerCase().contains("highest")) {
                            // Handle highest education case
                            checkDocMatch = true;
                        } else {
                            List<ConventionalCandidateCafEducation> byConventionalRequestId = conventionalCafCandidateEducationRepository.findByConventionalRequestId(Long.valueOf(requestId));

                        }
                    }
                }
                return checkDocMatch;
            }).collect(Collectors.toList());

            matchingDocuments.forEach(matchingDocument -> {
                CandidateuploadS3Documents candidateuploadS3Documents1 = new CandidateuploadS3Documents();
                candidateuploadS3Documents1.setDocumentName(matchingDocument.getDocumentName());
                candidateuploadS3Documents1.setPathkey(matchingDocument.getDocumentUrl());
                String presignedUrl = awsUtils.getPresignedUrl(DIGIVERIFIER_DOC_BUCKET_NAME, matchingDocument.getDocumentUrl());
                candidateuploadS3Documents1.setDocumentUrl(presignedUrl);
//                candidateuploadS3Documents.add(candidateuploadS3Documents1);
                listServiceOutcome.setData(candidateuploadS3Documents1);
            });

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return listServiceOutcome;
    }


    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackFor = Exception.class)
    public ServiceOutcome<String> updateLiCheckStatusByVendor(String vendorCheckStatusMasterId, String vendorCheckId, String remarks, String modeOfVericationPerformed, String licheckResponse) throws Exception {
        ServiceOutcome<String> serviceOutcome = new ServiceOutcome<>();
        try {
            ConventionalVendorliChecksToPerform byVendorChecksVendorcheckId = liCheckToPerformRepository.findByVendorChecksVendorcheckId(Long.valueOf(vendorCheckId));
            VendorCheckStatusMaster byVendorCheckStatusMasterId = vendorCheckStatusMasterRepository.findByVendorCheckStatusMasterId(Long.valueOf(vendorCheckStatusMasterId));
            ModeOfVerificationStatusMaster modeOfVerificationStatusMaster = modeOfVerificationStatusMasterRepository.findById(Long.valueOf(modeOfVericationPerformed)).get();
            byVendorChecksVendorcheckId.setCheckStatus(byVendorCheckStatusMasterId);
            byVendorChecksVendorcheckId.setModeOfVerificationPerformed(String.valueOf(modeOfVerificationStatusMaster.getModeTypeCode()));
            byVendorChecksVendorcheckId.setCheckRemarks(remarks);
            byVendorChecksVendorcheckId.setLastUpdatedOn(new Date());
            byVendorChecksVendorcheckId.setLastUpdatedBy(String.valueOf(SecurityHelper.getCurrentUser().getUserId()));
            if (!licheckResponse.isBlank() || !licheckResponse.isEmpty()) {
                log.info("setting the setIsCheckStatusTriggered flag to  true" + licheckResponse);
                byVendorChecksVendorcheckId.setIsCheckStatusTriggered(true);
            } else {
                log.info("setting the setIsCheckStatusTriggered flag to  false" + licheckResponse);
                byVendorChecksVendorcheckId.setIsCheckStatusTriggered(false);
            }

            ConventionalVendorliChecksToPerform updatedStatusCode = liCheckToPerformRepository.save(byVendorChecksVendorcheckId);
            ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(byVendorChecksVendorcheckId.getRequestId());
            StatusMaster pendingapproval = statusMasterRepository.findByStatusCode("PENDINGAPPROVAL");
            conventionalVendorCandidatesSubmitted.setStatus(pendingapproval);
            ConventionalVendorCandidatesSubmitted updated = conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted);
            serviceOutcome.setMessage("Check Status Updated  To -" + updatedStatusCode.getCheckStatus().getCheckStatusCode() + " -Sucessfully \n  " + "Please Trigger Check Status To IVerifiy From QC");
            log.info("Check Status Updated  To -" + updatedStatusCode.getCheckStatus().getCheckStatusCode() + " -Sucessfully ");
            serviceOutcome.setOutcome(true);
        } catch (Exception e) {
            serviceOutcome.setData("Some Thing Went Wrong");
            serviceOutcome.setOutcome(false);
            log.info(e.getMessage());
        }
        return serviceOutcome;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackFor = Exception.class)
    public ServiceOutcome<String> findUpdateLicheckWithVendorCheck(Long vendorCheckId, Long liCheckId) throws Exception {
        ServiceOutcome<String> serviceOutcome = new ServiceOutcome<>();
        try {
            VendorChecks vendorChecks = vendorChecksRepository.findByVendorcheckId(vendorCheckId);
            ConventionalVendorliChecksToPerform conventionalVendorliChecksToPerform = liCheckToPerformRepository.findById(liCheckId).get();
            conventionalVendorliChecksToPerform.setVendorChecks(vendorChecks);
            ConventionalVendorliChecksToPerform save1 = liCheckToPerformRepository.save(conventionalVendorliChecksToPerform);
            liCheckToPerformRepository.flush();
            ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(conventionalVendorliChecksToPerform.getRequestId());
            if (conventionalVendorCandidatesSubmitted.getStatus().getStatusCode().equalsIgnoreCase("NEWUPLOAD")) {
                StatusMaster inprogress = statusMasterRepository.findByStatusCode("INPROGRESS");
                conventionalVendorCandidatesSubmitted.setStatus(inprogress);
                ConventionalVendorCandidatesSubmitted save = conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted);
            }
            serviceOutcome.setData(String.valueOf(vendorCheckId));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return serviceOutcome;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackFor = Exception.class)
    public ServiceOutcome<String> updateCandidateStatusByLicheckStatus() {
        ServiceOutcome<String> serviceOutcome = new ServiceOutcome<String>();
        try {
            List<ConventionalVendorCandidatesSubmitted> candidatesSubmitteds = conventionalCandidatesSubmittedRepository.findAll();
            for (ConventionalVendorCandidatesSubmitted candidatesSubmitted : candidatesSubmitteds) {
                if (liCheckToPerformRepository.existsByRequestId(String.valueOf(candidatesSubmitted.getRequestId())) == true) {
                    List<ConventionalVendorliChecksToPerform> liChecks = liCheckToPerformRepository.findByRequestId(String.valueOf(candidatesSubmitted.getRequestId()));
                    Map<String, Long> statusCountMap = liChecks.stream().collect(Collectors.toMap(da -> String.valueOf(da.getCheckStatus().getVendorCheckStatusMasterId()), v -> 1L, Long::sum));
                    ArrayList<String> keydata = new ArrayList<>();
                    statusCountMap.forEach((k, v) -> keydata.add(k));
                    if (candidatesSubmitted.getStatus().getStatusMasterId() != 8l && candidatesSubmitted.getStatus().getStatusMasterId() != 13l) {
                        if (keydata.stream().anyMatch("7"::equals)) {
                            ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(candidatesSubmitted.getRequestId());
                            StatusMaster newupload = statusMasterRepository.findByStatusCode("NEWUPLOAD");
                            conventionalVendorCandidatesSubmitted.setStatus(newupload);
                            conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted);
                        }
                        if (keydata.stream().anyMatch("2"::equals)) {
                            ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(candidatesSubmitted.getRequestId());
                            StatusMaster inprogress = statusMasterRepository.findByStatusCode("INPROGRESS");
                            conventionalVendorCandidatesSubmitted.setStatus(inprogress);
                            conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted);
                        }
                        boolean matchFound = keydata.stream().anyMatch(str -> str.equals("7") || str.equals("2"));
                        boolean otherMatchFound = keydata.stream().anyMatch(str -> !str.equals("7") && !str.equals("2"));


                        if (otherMatchFound) {
                            ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(candidatesSubmitted.getRequestId());
                            StatusMaster pendingapproval = statusMasterRepository.findByStatusCode("PENDINGAPPROVAL");
                            conventionalVendorCandidatesSubmitted.setStatus(pendingapproval);
                            conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted);
                        }
                    }

                }
            }

            serviceOutcome.setData("data came");
            return serviceOutcome;
        } catch (Exception e) {
            log.error(e.getMessage());

        }
        return serviceOutcome;
    }

    @Transactional
    public ServiceOutcome<String> updateCandidateVerificationStatus(String requestID) {
        ServiceOutcome<String> serviceOutcome = new ServiceOutcome<String>();
        try {
            ConventionalVendorCandidatesSubmitted candidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(requestID);
            if (liCheckToPerformRepository.existsByRequestId(String.valueOf(candidatesSubmitted.getRequestId())) == true) {
                List<ConventionalVendorliChecksToPerform> liChecks = liCheckToPerformRepository.findByRequestId(String.valueOf(candidatesSubmitted.getRequestId()));
                List<ConventionalVendorliChecksToPerform> collect = liChecks.stream().filter(licheck -> licheck.getCheckStatus().getVendorCheckStatusMasterId() != 7l && licheck.getCheckStatus().getVendorCheckStatusMasterId() != 3l && licheck.getCheckStatus().getVendorCheckStatusMasterId() != 2l && !"TRUE".equalsIgnoreCase(licheck.getStopCheck())).collect(Collectors.toList());


//                log.info("completed checks" + collect.toString());
                Map<String, Long> statusCountMap = collect.stream().collect(Collectors.toMap(da -> String.valueOf(da.getCheckStatus().getVendorCheckStatusMasterId()), v -> 1L, Long::sum));
                ArrayList<String> keydata = new ArrayList<>();
                statusCountMap.forEach((k, v) -> keydata.add(k));
                keydata.stream().sorted();
                if (keydata.stream().anyMatch("4"::equalsIgnoreCase)) {
                    ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(candidatesSubmitted.getRequestId());
                    conventionalVendorCandidatesSubmitted.setVerificationStatus("MAJORDISCREPANCY");
                    conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted);
                } else if (keydata.stream().anyMatch("5"::equalsIgnoreCase)) {
                    ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(candidatesSubmitted.getRequestId());
                    conventionalVendorCandidatesSubmitted.setVerificationStatus("MINORDISCREPANCY");
                    conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted);
                } else if (keydata.stream().anyMatch("6"::equalsIgnoreCase)) {
                    ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(candidatesSubmitted.getRequestId());
                    conventionalVendorCandidatesSubmitted.setVerificationStatus("UNABLETOVERIFIY");
                    conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted);
                } else if (keydata.stream().anyMatch("3"::equalsIgnoreCase)) {
                    ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(candidatesSubmitted.getRequestId());
                    conventionalVendorCandidatesSubmitted.setVerificationStatus("INSUFFICIENCY");
                    conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted);
                } else if (keydata.stream().anyMatch("2"::equalsIgnoreCase)) {
                    ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(candidatesSubmitted.getRequestId());
                    conventionalVendorCandidatesSubmitted.setVerificationStatus("INPROGRESS");
                    conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted);
                } else if (keydata.stream().allMatch("1"::equalsIgnoreCase)) {
                    ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(candidatesSubmitted.getRequestId());
                    conventionalVendorCandidatesSubmitted.setVerificationStatus("CLEAR");
                    conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted);
                }
            }


            serviceOutcome.setData("data came");
            return serviceOutcome;
        } catch (Exception e) {
            log.error(e.getMessage());

        }
        return serviceOutcome;
    }


    public ServiceOutcome<DashboardDto> findAllConventionalVendorSubmittedCandidatesByDateRange(DashboardDto dashboardDto) throws Exception {
        ServiceOutcome<DashboardDto> listServiceOutcome = new ServiceOutcome<>();
//         User user = (SecurityHelper.getCurrentUser()!=null)?SecurityHelper.getCurrentUser():userRepository.findByUserId(53l);;
        String strToDate = "";
        String strFromDate = "";
        List<ConventionalVendorCandidatesSubmitted> candidatesSubmittedList = new ArrayList<ConventionalVendorCandidatesSubmitted>();
        try {
            Pageable pageable = null;
            if (dashboardDto.getPageNumber() != null) {
                pageable = PageRequest.of(Integer.parseInt(String.valueOf(dashboardDto.getPageNumber())), 10);
            }
            strToDate = dashboardDto.getToDate() != null ? dashboardDto.getToDate() : ApplicationDateUtils.getStringTodayAsDDMMYYYY();
            strFromDate = dashboardDto.getFromDate() != null ? dashboardDto.getFromDate() : ApplicationDateUtils.subtractNoOfDaysFromDateAsDDMMYYYY(new SimpleDateFormat("dd/MM/yyyy").parse(strToDate), 7);
            Date startDate = formatter.parse(strFromDate + " 00:00:00");
            Date endDate = formatter.parse(strToDate + " 23:59:59");
            User user = userRepository.findById(dashboardDto.getUserId()).get();
            User byOrganizationAndRoleId = userRepository.findByOrganizationAndRoleId(user.getOrganization().getOrganizationId(), user.getRole().getRoleId(), user.getUserId());
            Page<ConventionalVendorCandidatesSubmitted> allByUserIdAndDateRangeAll = null;
            if (byOrganizationAndRoleId != null) {
                allByUserIdAndDateRangeAll = conventionalVendorCandidatesSubmittedRepository.findAllByUserIdAndDateRange(startDate, endDate, pageable);
//                candidatesSubmittedList.addAll(allByUserIdAndDateRange.toList());
            }
            if (allByUserIdAndDateRangeAll.isEmpty() == false) {
                if (dashboardDto.getStatus() == null) {
                    dashboardDto.setStatus("NEWUPLOAD");
                }
                if (dashboardDto.getStatus().equalsIgnoreCase("NEWUPLOAD")) {
                    Page<ConventionalVendorCandidatesSubmitted> allByUserIdAndDateRange = conventionalVendorCandidatesSubmittedRepository.findAllByUserIdAndDateRange(startDate, endDate, pageable);
                    candidatesSubmittedList.addAll(allByUserIdAndDateRange.toList());
                    System.out.println(allByUserIdAndDateRange.getTotalPages());
                    DashboardDto dashboardDtoObj = new DashboardDto(strFromDate, strToDate, null, null, null,
                            dashboardDto.getUserId(), dashboardDto.getStatus(), candidatesSubmittedList, dashboardDto.getPageNumber(), String.valueOf(allByUserIdAndDateRange.getTotalPages()));
                    listServiceOutcome.setData(dashboardDtoObj);
                    if (candidatesSubmittedList.isEmpty()) {
                        listServiceOutcome.setData(new DashboardDto());
                    }
                } else if (dashboardDto.getStatus().equalsIgnoreCase("STOPBGV")) {
                    Page<ConventionalVendorCandidatesSubmitted> allByUserIdAndDateRange = conventionalVendorCandidatesSubmittedRepository.findAllByUserIdAndDateRangeForStopBgv(startDate, endDate, pageable);
                    candidatesSubmittedList.addAll(allByUserIdAndDateRange.toList());
                    DashboardDto dashboardDtoObj = new DashboardDto(strFromDate, strToDate, null, null, null,
                            dashboardDto.getUserId(), dashboardDto.getStatus(), candidatesSubmittedList, dashboardDto.getPageNumber(), String.valueOf(allByUserIdAndDateRange.getTotalPages()));
                    listServiceOutcome.setData(dashboardDtoObj);
                    if (candidatesSubmittedList.isEmpty()) {
                        listServiceOutcome.setData(new DashboardDto());
                    }
                } else if (dashboardDto.getStatus().equalsIgnoreCase("FASTTRACK")) {
                    Page<ConventionalVendorCandidatesSubmitted> allByUserIdAndDateRange = conventionalVendorCandidatesSubmittedRepository.findAllByUserIdAndDateRangeForFastTrack(startDate, endDate, pageable);
                    candidatesSubmittedList.addAll(allByUserIdAndDateRange.toList());
                    DashboardDto dashboardDtoObj = new DashboardDto(strFromDate, strToDate, null, null, null,
                            dashboardDto.getUserId(), dashboardDto.getStatus(), candidatesSubmittedList, dashboardDto.getPageNumber(), String.valueOf(allByUserIdAndDateRange.getTotalPages()));
                    listServiceOutcome.setData(dashboardDtoObj);
                    if (candidatesSubmittedList.isEmpty()) {
                        listServiceOutcome.setData(new DashboardDto());
                    }
                } else if (dashboardDto.getStatus().equalsIgnoreCase("PENDINGINTERIMREPORTONBASICCHECKS")) {
                    Page<ConventionalVendorCandidatesSubmitted> allByUserIdAndDateRange = conventionalVendorCandidatesSubmittedRepository.findAllByUserIdAndDateRangeForPendingInterimForBasicChecks(startDate, endDate, pageable);
                    candidatesSubmittedList.addAll(allByUserIdAndDateRange.toList());
                    DashboardDto dashboardDtoObj = new DashboardDto(strFromDate, strToDate, null, null, null,
                            dashboardDto.getUserId(), dashboardDto.getStatus(), candidatesSubmittedList, dashboardDto.getPageNumber(), String.valueOf(allByUserIdAndDateRange.getTotalPages()));
                    listServiceOutcome.setData(dashboardDtoObj);
                    if (candidatesSubmittedList.isEmpty()) {
                        listServiceOutcome.setData(new DashboardDto());
                    }
                } else {
                    Page<ConventionalVendorCandidatesSubmitted> allByUserIdAndDateRange = conventionalVendorCandidatesSubmittedRepository.findAllByUserIdAndDateRangeForStaus(dashboardDto.getStatus(), startDate, endDate, pageable);
                    candidatesSubmittedList.addAll(allByUserIdAndDateRange.toList());
                    DashboardDto dashboardDtoObj = new DashboardDto(strFromDate, strToDate, null, null, null,
                            dashboardDto.getUserId(), dashboardDto.getStatus(), candidatesSubmittedList, dashboardDto.getPageNumber(), String.valueOf(allByUserIdAndDateRange.getTotalPages()));
                    listServiceOutcome.setData(dashboardDtoObj);

                    if (candidatesSubmittedList.isEmpty()) {
                        listServiceOutcome.setData(new DashboardDto());
                    }
                }
            } else {
                listServiceOutcome.setData(new DashboardDto(strFromDate, strToDate, null, null, null,
                        dashboardDto.getUserId(), dashboardDto.getStatus(), new ArrayList<ConventionalVendorCandidatesSubmitted>(), dashboardDto.getPageNumber(), "0"));
            }


        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return listServiceOutcome;
    }

    public ServiceOutcome<DashboardDto> findAllSubmittedCandidatesByDateRangeOnInterimAndFinal(DashboardDto dashboardDto) throws Exception {
        ServiceOutcome<DashboardDto> listServiceOutcome = new ServiceOutcome<>();
        String strToDate = "";
        String strFromDate = "";
        List<ConventionalVendorCandidatesSubmitted> candidatesSubmittedList = new ArrayList<ConventionalVendorCandidatesSubmitted>();
        try {
            Pageable pageable = null;
            if (dashboardDto.getPageNumber() != null) {
                pageable = PageRequest.of(Integer.parseInt(String.valueOf(dashboardDto.getPageNumber())), 10);
            }
            strToDate = dashboardDto.getToDate() != null ? dashboardDto.getToDate() : ApplicationDateUtils.getStringTodayAsDDMMYYYY();
            strFromDate = dashboardDto.getFromDate() != null ? dashboardDto.getFromDate() : ApplicationDateUtils.subtractNoOfDaysFromDateAsDDMMYYYY(new SimpleDateFormat("dd/MM/yyyy").parse(strToDate), 7);
            Date startDate = formatter.parse(strFromDate + " 00:00:00");
            Date endDate = formatter.parse(strToDate + " 23:59:59");
            User user = userRepository.findById(dashboardDto.getUserId()).get();
            Page<ConventionalVendorCandidatesSubmitted> allByUserIdAndDateRange = null;
            if (user.getRole().getRoleCode().equalsIgnoreCase("ROLE_ADMIN") || user.getRole().getRoleCode().equalsIgnoreCase("ROLE_PARTNERADMIN")) {
                allByUserIdAndDateRange = conventionalVendorCandidatesSubmittedRepository.findAllByUserIdAndDateRange(startDate, endDate, pageable);
            }
            if (allByUserIdAndDateRange.isEmpty() == false) {
                if (dashboardDto.getStatus() == null) {
                    dashboardDto.setStatus("NEWUPLOAD");
                }
                if (dashboardDto.getStatus().equalsIgnoreCase("NEWUPLOAD")) {
                    listServiceOutcome.setData(new DashboardDto(strFromDate, strToDate, null, null, null,
                            dashboardDto.getUserId(), dashboardDto.getStatus(), new ArrayList<ConventionalVendorCandidatesSubmitted>(), dashboardDto.getPageNumber(), "0"));
                    if (allByUserIdAndDateRange.isEmpty()) {
                        listServiceOutcome.setData(null);
                    }
                } else {
                    allByUserIdAndDateRange = conventionalVendorCandidatesSubmittedRepository.findAllByUserIdAndDateRangeForStaus(dashboardDto.getStatus(), startDate, endDate, pageable);
                    candidatesSubmittedList.addAll(allByUserIdAndDateRange.toList());
                    DashboardDto dashboardDtoObj = new DashboardDto(strFromDate, strToDate, null, null, null,
                            dashboardDto.getUserId(), dashboardDto.getStatus(), candidatesSubmittedList, dashboardDto.getPageNumber(), String.valueOf(allByUserIdAndDateRange.getTotalPages()));
                    listServiceOutcome.setData(dashboardDtoObj);
                    listServiceOutcome.setData(dashboardDtoObj);
                    if (allByUserIdAndDateRange.isEmpty()) {
                        listServiceOutcome.setData(new DashboardDto());
                    }
                }
            } else {
                listServiceOutcome.setData(new DashboardDto(strFromDate, strToDate, null, null, null,
                        dashboardDto.getUserId(), dashboardDto.getStatus(), new ArrayList<ConventionalVendorCandidatesSubmitted>(), dashboardDto.getPageNumber(), "0"));
            }


        } catch (Exception e) {
            log.error(e.getMessage());

        }
        return listServiceOutcome;
    }


    public ServiceOutcome<List<VendorCheckStatusMaster>> findAllVendorCheckStatus() {
        ServiceOutcome<List<VendorCheckStatusMaster>> vendorCheckStatusMaster = new ServiceOutcome<>();
        try {
            List<VendorCheckStatusMaster> all = vendorCheckStatusMasterRepository.findAll();
            vendorCheckStatusMaster.setData(all);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return vendorCheckStatusMaster;

    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ServiceOutcome<String> generateConventionalCandidateReport(Long candidateId, ReportType reportType, String updated, String reportDeliveryDate) {
        ServiceOutcome<String> stringServiceOutcome = new ServiceOutcome<>();
        ArrayList<LicheckRequiredResponseDto> licheckRequiredResponseDtos = new ArrayList<>();
        List<AgentUploadedChecks> agentUploadedList = new ArrayList<>();
        try {
            log.debug("generateConventionalCandidateReport() Starts*******************************");
//            Candidate candidate = candidateService.findCandidateByCandidateCode(candidateCode);
            Candidate candidate = candidateRepository.findById(candidateId).get();
            ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(String.valueOf(candidate.getConventionalRequestId()));
            ConventionalCandidate byConventionalRequestId = conventionalCandidateRepository.findByConventionalRequestId(Long.valueOf(conventionalVendorCandidatesSubmitted.getRequestId()));
            if (candidate != null) {
                List<VendorUploadChecksDto> vendordocDtoList = new ArrayList<VendorUploadChecksDto>();
                List<ConventionalCandidateReportDto> jsonConventionalCandidateReportDto = new ArrayList<>();
                ConventionalCandidateReportDto candidateReportDTO = new ConventionalCandidateReportDto();
                candidateReportDTO.setCandidateId(String.valueOf(conventionalVendorCandidatesSubmitted.getCandidateId()));
                candidateReportDTO.setApplicantId(String.valueOf(conventionalVendorCandidatesSubmitted.getApplicantId()));
                candidateReportDTO.setOrganizationName(candidate.getOrganization().getOrganizationName());
                candidateReportDTO.setOrganizationLogo(Arrays.toString(candidate.getOrganization().getOrganizationLogo()));
                candidateReportDTO.setRequestId(conventionalVendorCandidatesSubmitted.getRequestId());
                if (byConventionalRequestId != null) {
                    candidateReportDTO.setAddress(byConventionalRequestId.getBirthPlace());
                }
                if (conventionalVendorCandidatesSubmitted.getVerificationStatus() != null) {
                    if (conventionalVendorCandidatesSubmitted.getVerificationStatus().equalsIgnoreCase("UNABLETOVERIFIY")) {
                        candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.UNABLETOVERIFIY);
                        candidateReportDTO.setColorCode("AMBER");
                    }
                    if (conventionalVendorCandidatesSubmitted.getVerificationStatus().equalsIgnoreCase("MAJORDISCREPANCY")) {
                        candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.MAJORDISCREPANCY);
                        candidateReportDTO.setColorCode("RED");
                    }
                    if (conventionalVendorCandidatesSubmitted.getVerificationStatus().equalsIgnoreCase("MINORDISCREPANCY")) {
                        candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.MINORDISCREPANCY);
                        candidateReportDTO.setColorCode("RED");
                    }
                    if (conventionalVendorCandidatesSubmitted.getVerificationStatus().equalsIgnoreCase("INSUFFICIENCY")) {
                        candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.INSUFFICIENCY);
                        candidateReportDTO.setColorCode("YELLOW");
                    }
                    if (conventionalVendorCandidatesSubmitted.getVerificationStatus().equalsIgnoreCase("INPROGRESS")) {
                        candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.INPROGRESS);
                        candidateReportDTO.setColorCode("");
                    }
                    if (conventionalVendorCandidatesSubmitted.getVerificationStatus().equalsIgnoreCase("CLEAR")) {
                        candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.CLEAR);
                        candidateReportDTO.setColorCode("GREEN");
                    }
                } else {
                    candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.NAN);
                }

                if (byConventionalRequestId != null) {
                    candidateReportDTO.setAddress(byConventionalRequestId.getBirthPlace());
                }
                candidateReportDTO.setName(candidate.getCandidateName());
                candidateReportDTO.setReferenceId(candidate.getApplicantId());
                candidateReportDTO.setDob(candidate.getDateOfBirth());
                candidateReportDTO.setContactNo(candidate.getContactNumber());
                candidateReportDTO.setEmailId(candidate.getEmailId());
                candidateReportDTO.setReportType(reportType);
                Organization organization = candidate.getOrganization();
                candidateReportDTO.setProject(organization.getOrganizationName());
                candidateReportDTO.setOrganizationLocation(organization.getOrganizationLocation());
                candidateReportDTO.setOrganizationLogo(organization.getLogoUrl());
                candidateReportDTO.setComments("");
                List<ConventionalVendorliChecksToPerform> byCandidateId = liCheckToPerformRepository.findByRequestId(String.valueOf(candidate.getConventionalRequestId()));
                byCandidateId = byCandidateId.stream()
                        .filter(p -> !"TRUE".equalsIgnoreCase(p.getStopCheck()) && p.getCheckStatus().getVendorCheckStatusMasterId() != 2l && p.getCheckStatus().getVendorCheckStatusMasterId() != 7l && p.getCheckStatus().getVendorCheckStatusMasterId() != 3l)
                        .collect(Collectors.toList());
                byCandidateId = byCandidateId.stream().filter(en -> en.getSource() != null)
                        .sorted(Comparator.comparing(entity -> {

                            Long sourceId = entity.getSource().getSourceId();

                            // Customize the sorting order based on source ID matches
                            List<Long> educationIds = Arrays.asList(24L, 25L, 26L, 27L, 45L);
                            List<Long> employmentIds = Arrays.asList(28L, 29L, 44L, 46L, 50L);
                            List<Long> addressIds = Arrays.asList(19L, 20L, 21L, 22L, 23L, 48L);
                            List<Long> identityIds = Arrays.asList(11L, 12L, 13L, 14L);
                            List<Long> ofacIds = Arrays.asList(39L);
                            List<Long> referenceIds = Arrays.asList(30L, 31l, 32l);
                            List<Long> drugCheck = Arrays.asList(10l, 33l, 34l, 35l, 36l, 37l);
                            List<Long> cvValidation = Arrays.asList(40l);
                            List<Long> criminalIds = Arrays.asList(15l, 16l, 17l, 18l, 49l, 51l, 52l);
                            List<Long> globalIds = Arrays.asList(38L);
                            if (educationIds.contains(sourceId)) {
                                return 1L; // education
                            } else if (employmentIds.contains(sourceId)) {
                                return 2L; // employment
                            } else if (addressIds.contains(sourceId)) {
                                return 3L; // address
                            } else if (identityIds.contains(sourceId)) {
                                return 4L; // identity
                            } else if (ofacIds.contains(sourceId)) {
                                return 5L; // ofac
                            } else if (referenceIds.contains(sourceId)) {
                                return 6L; // reference
                            } else if (criminalIds.contains(sourceId)) {
                                return 7L; // criminal
                            } else if (drugCheck.contains(sourceId)) {
                                return 8L; // drug
                            } else if (cvValidation.contains(sourceId)) {
                                return 9L; // cvvalidation
                            } else if (globalIds.contains(sourceId)) {
                                return 10L; // global
                            } else {
                                return 11L; // other than above
                            }
                        })).collect(Collectors.toList());
// Separate employment checks and other checks
                List<ConventionalVendorliChecksToPerform> employmentChecks = byCandidateId.stream()
                        .filter(entity -> entity.getCheckName().contains("EMPLOYER "))
                        .collect(Collectors.toList());

                List<ConventionalVendorliChecksToPerform> otherChecks = byCandidateId.stream()
                        .filter(entity -> !entity.getCheckName().contains("EMPLOYER "))
                        .collect(Collectors.toList());

// Sort employment checks
                Comparator<ConventionalVendorliChecksToPerform> employmentComparator = (entity1, entity2) -> {
                    int index1 = getLastNumber(entity1.getCheckName());
                    int index2 = getLastNumber(entity2.getCheckName());

                    if (index1 == 1 && index2 != 1) {
                        return -1; // entity1 comes first
                    } else if (index1 != 1 && index2 == 1) {
                        return 1; // entity2 comes first
                    }

                    if (index1 == 0 && index2 != 0) {
                        return 1; // entity2 comes first
                    } else if (index1 != 0 && index2 == 0) {
                        return -1; // entity1 comes first
                    }

                    return Integer.compare(index1, index2);
                };

                employmentChecks = employmentChecks.stream()
                        .sorted(employmentComparator)
                        .collect(Collectors.toList());

// Combine sorted employment checks and other checks
                byCandidateId = new ArrayList<>();
                byCandidateId.addAll(employmentChecks);
                byCandidateId.addAll(otherChecks);

                HashMap<String, LegalProceedingsDTO> criminalCheckListMap = new HashMap<>();
                if (byCandidateId.isEmpty() == false) {
                    List<ConventionalVendorliChecksToPerform> collect1 = byCandidateId.stream().filter(licheck -> licheck.getCheckStatus().getVendorCheckStatusMasterId() != 7l && licheck.getCheckStatus().getVendorCheckStatusMasterId() != 2l).collect(Collectors.toList());
                    collect1.forEach(data -> {
                        if (data.getCheckName().contains("EMPLOYMENT")) {
                            LicheckRequiredResponseDto licheckRequiredResponseDto = new LicheckRequiredResponseDto();
                            licheckRequiredResponseDto.setCheckName(data.getCheckName());
                            licheckRequiredResponseDto.setCheckUniqueId(data.getCheckUniqueId());
                            licheckRequiredResponseDto.setCheckStatus(data.getCheckStatus().getCheckStatusCode());
                            licheckRequiredResponseDto.setCheckRemarks(data.getCheckRemarks());
                            int lastNumber = getLastNumber(data.getCheckName());
                            licheckRequiredResponseDto.setIndexNumber(lastNumber);
                            licheckRequiredResponseDto.setDisableStatus("");
                            licheckRequiredResponseDtos.add(licheckRequiredResponseDto);
                        } else {
                            LicheckRequiredResponseDto licheckRequiredResponseDto = new LicheckRequiredResponseDto();
                            licheckRequiredResponseDto.setCheckName(data.getCheckName());
                            licheckRequiredResponseDto.setCheckUniqueId(data.getCheckUniqueId());
                            licheckRequiredResponseDto.setCheckStatus(data.getCheckStatus().getCheckStatusCode());
                            licheckRequiredResponseDto.setCheckRemarks(data.getCheckRemarks());
                            licheckRequiredResponseDto.setDisableStatus("");
                            licheckRequiredResponseDtos.add(licheckRequiredResponseDto);
                        }
                    });
                    agentUploadedList = agentUplaodedChecksRepository.findByRequestID(conventionalVendorCandidatesSubmitted.getRequestId());

                    if (agentUploadedList.isEmpty() == false) {
                        agentUploadedList.forEach(data -> {
                            LicheckRequiredResponseDto licheckRequiredResponseDto = new LicheckRequiredResponseDto();
                            licheckRequiredResponseDto.setCheckName(data.getSource().getSourceName());
                            licheckRequiredResponseDto.setCheckUniqueId(Long.valueOf(data.getUanNo()));
                            licheckRequiredResponseDto.setCheckStatus(data.getColorCode());
                            licheckRequiredResponseDto.setCheckRemarks(data.getRemarks());
                            licheckRequiredResponseDto.setDisableStatus(data.getUanNo());
                            licheckRequiredResponseDtos.add(licheckRequiredResponseDto);
                        });
                    }

                    Comparator<LicheckRequiredResponseDto> indexComparator = (dto1, dto2) -> {
                        // First, handle special cases where one of the indices is 1
                        if (dto1.getIndexNumber() == 1 && dto2.getIndexNumber() != 1) {
                            return -1; // dto1 comes first
                        } else if (dto1.getIndexNumber() != 1 && dto2.getIndexNumber() == 1) {
                            return 1; // dto2 comes first
                        }

                        // Then, ensure that indexNumber 0 comes last
                        if (dto1.getIndexNumber() == 0 && dto2.getIndexNumber() != 0) {
                            return 1; // dto2 comes first
                        } else if (dto1.getIndexNumber() != 0 && dto2.getIndexNumber() == 0) {
                            return -1; // dto1 comes first
                        }

                        // Finally, compare index numbers as usual
                        return Integer.compare(dto1.getIndexNumber(), dto2.getIndexNumber());
                    };

                    // Sort the list
                    Collections.sort(licheckRequiredResponseDtos, indexComparator);

                    licheckRequiredResponseDtos.forEach(data -> {


                        String modifiedCheckName = data.getCheckName().toLowerCase().replaceAll("[\\s-]", "");

                        if (modifiedCheckName.contains("education") && !modifiedCheckName.contains("educationhighest")) {
                            String executiveSummaryValueForCheck = getExecutiveSummaryValueForCheck(data.getCheckUniqueId());
                            if (executiveSummaryValueForCheck != null) {
                                data.setDisableStatus(executiveSummaryValueForCheck);
                            } else {
                                List<ConventionalCandidateCafEducation> conventionalCandidateCafEducations = conventionalCafCandidateEducationRepository.findByConventionalRequestId(Long.valueOf(conventionalVendorCandidatesSubmitted.getRequestId()));
                                if (conventionalCandidateCafEducations.isEmpty() == false) {
                                    conventionalCandidateCafEducations.forEach(convdata -> {
                                        String degreetype = convdata.getDegreeType().toLowerCase().replaceAll("[\\s-]", "").replaceAll("degree", "");
//                                    log.info("Check NAme" + modifiedCheckName + "===" + "    Education -   " + degreetype);
                                        boolean contains = modifiedCheckName.contains(degreetype);
                                        boolean diplomacontains = modifiedCheckName.contains("dipl");
                                        if (contains == true || diplomacontains == true) {
                                            CandidateCafEducation candidateCafEducation = candidateCafEducationRepository.findById(convdata.getCandidateCafEducationId()).get();
                                            data.setDisableStatus(candidateCafEducation.getQualificationType());
                                        }
//                                    log.info("End Education Type setted" + data.getDisableStatus());
                                    });
                                }
                            }
                        }
                        if (modifiedCheckName.contains("educationhighest")) {
                            String executiveSummaryValueForCheck = getExecutiveSummaryValueForCheck(data.getCheckUniqueId());
                            if (executiveSummaryValueForCheck != null) {
                                data.setDisableStatus(executiveSummaryValueForCheck);
                            } else {
                                List<ConventionalCandidateCafEducation> conventionalCandidateCafEducations = conventionalCafCandidateEducationRepository.findByConventionalRequestId(Long.valueOf(conventionalVendorCandidatesSubmitted.getRequestId()));
                                if (conventionalCandidateCafEducations.isEmpty() == false) {
                                    conventionalCandidateCafEducations.forEach(convdata -> {
                                        String degreetype = convdata.getDegreeType().toLowerCase().replaceAll("[\\s-]", "");
//                                    log.info("Check NAme" + modifiedCheckName + "===" + "    Education -   " + degreetype);
//                                    CandidateCafEducation candidateCafEducation = candidateCafEducationRepository.findById(convdata.getCandidateCafEducationId()).get();
                                        Optional<CandidateCafEducation> optionalCandidateCafEducation = candidateCafEducationRepository.findById(convdata.getCandidateCafEducationId());
                                        if (optionalCandidateCafEducation.isPresent()) {
                                            CandidateCafEducation candidateCafEducation = optionalCandidateCafEducation.get();

                                            if (degreetype.toLowerCase().contains("undergraduate")) {

                                                data.setDisableStatus(candidateCafEducation.getQualificationType());
                                            } else if (degreetype.toLowerCase().contains("postgraduate")) {
                                                // Handle postgraduate case
                                                data.setDisableStatus(candidateCafEducation.getQualificationType());

                                            } else if (degreetype.toLowerCase().contains("diploma")) {
                                                // Handle diploma case

                                                data.setDisableStatus(candidateCafEducation.getQualificationType());
                                            } else if (degreetype.toLowerCase().contains("highschool")) {
                                                // Handle high school case

                                                data.setDisableStatus(candidateCafEducation.getQualificationType());
                                            } else if (degreetype.toLowerCase().contains("highest")) {
                                                // Handle highest education case
                                                data.setDisableStatus(candidateCafEducation.getQualificationType());
                                            } else {

                                                data.setDisableStatus(candidateCafEducation.getQualificationType());
                                            }
                                        }
                                    });
                                }
                            }
                        }
                        if (modifiedCheckName.contains("address")) {
                            List<ConventionalCafAddress> conventionalCafAddresses = conventionCafAddressRepository.findByConventionalRequestId(Long.valueOf(conventionalVendorCandidatesSubmitted.getRequestId()));
                            if (conventionalCafAddresses.isEmpty() == false) {
                                conventionalCafAddresses.forEach(convdata -> {
                                    String addressType = convdata.getAddressType().toLowerCase().replaceAll("[\\s-]", "");
//                                    log.info("Check NAme" + modifiedCheckName + "===" + "Address  type-   " + addressType);
                                    if (modifiedCheckName.contains("present")) {
                                        boolean contains = modifiedCheckName.replaceAll("present", "current").contains(addressType);
                                        if (contains == true) {
                                            CandidateCafAddress candidateCafAddress = candidateCafAddressRepository.findById(convdata.getCandidateCafAddressId()).get();
                                            data.setDisableStatus(candidateCafAddress.getCity() + "," + candidateCafAddress.getState());
//                                            log.info("End Address Type setted Present or current" + data.getDisableStatus());
                                        }

                                    } else if (modifiedCheckName.contains("permenent")) {
                                        boolean contains = modifiedCheckName.replaceAll("permenent", "permanent").contains(addressType);
                                        if (contains == true) {
                                            CandidateCafAddress candidateCafAddress = candidateCafAddressRepository.findById(convdata.getCandidateCafAddressId()).get();
                                            data.setDisableStatus(candidateCafAddress.getCity() + "," + candidateCafAddress.getState());
//                                            log.info("End Address Type setted Present or current" + data.getDisableStatus());
                                        }

                                    } else {
                                        boolean contains = modifiedCheckName.contains(addressType);
                                        if (contains == true) {
                                            CandidateCafAddress candidateCafAddress = candidateCafAddressRepository.findById(convdata.getCandidateCafAddressId()).get();
                                            data.setDisableStatus(candidateCafAddress.getCity() + "," + candidateCafAddress.getState());
//                                            log.info("End Address Type setted permenant" + data.getDisableStatus());
                                        }
                                    }

                                });
                            }
                        }
                        if (modifiedCheckName.contains("employment")) {
                            String executiveSummaryValueForCheck = getExecutiveSummaryValueForCheck(data.getCheckUniqueId());
                            if (executiveSummaryValueForCheck != null) {
                                data.setDisableStatus(executiveSummaryValueForCheck);
                            } else {
                                List<ConventionalCandidateExperience> conventionalexperienceS = conventionalCandidateExperienceRepository.findByConventionalRequestId(Long.valueOf(conventionalVendorCandidatesSubmitted.getRequestId()));
                                if (conventionalexperienceS.isEmpty() == false) {
                                    conventionalexperienceS.forEach(convdata -> {
                                        String employmentType = convdata.getEmploymentType().toLowerCase().replaceAll("[\\s-]", "");
//                                    log.info("Check NAme" + modifiedCheckName + "===" + "Employment -   " + employmentType);
                                        boolean contains = modifiedCheckName.contains(employmentType);
                                        if (contains == true) {
                                            CandidateCafExperience candidateCafExperience = candidateCafExperienceRepository.findById(convdata.getCandidateCafExperience()).get();
                                            data.setDisableStatus(candidateCafExperience.getCandidateEmployerName());

                                        }
                                    });


                                }
                            }
                        }
                        if (modifiedCheckName.contains("pan") && modifiedCheckName.contains("identity")) {
                            String panNumber = candidate.getPanNumber();
                            if (panNumber != null) {
                                String maskedPanNumber = panNumber.substring(0, panNumber.length() - 5) + "XXXXX";
                                data.setDisableStatus(maskedPanNumber);
                            }

                        }
                        if (modifiedCheckName.contains("passport")) {
                            data.setDisableStatus(candidate.getPassportNumber());
                        }
                        if (modifiedCheckName.contains("aadhar")) {
                            data.setDisableStatus(candidate.getAadharNumber());
                        }
                        if (modifiedCheckName.contains("driving")) {
                            data.setDisableStatus(candidate.getDrivingLicenseNumber());
                        }
                        if (modifiedCheckName.contains("criminalcheck")) {
                            LegalProceedingsDTO legalProceedingsDTO = new LegalProceedingsDTO();

                            List<CriminalCheck> civilproceding = criminalCheckRepository.findByCheckUniqueIdAndProceedingsType(String.valueOf(data.getCheckUniqueId()), "CIVILPROCEDING");
                            if (civilproceding.isEmpty() == false) {
                                legalProceedingsDTO.setCivilProceedingList(civilproceding);
                            }
                            List<CriminalCheck> criminalproceding = criminalCheckRepository.findByCheckUniqueIdAndProceedingsType(String.valueOf(data.getCheckUniqueId()), "CRIMINALPROCEDING");
                            if (criminalproceding.isEmpty() == false) {
                                legalProceedingsDTO.setCriminalProceedingList(criminalproceding);
                            }
                            criminalCheckListMap.put(String.valueOf(data.getCheckName()), legalProceedingsDTO);
//                            log.info("criminal check data" + criminalCheckListMap);
                        }

                    });
                    candidateReportDTO.setLiChecksDetails(licheckRequiredResponseDtos);
                } else {
                    candidateReportDTO.setLiChecksDetails(new ArrayList<>());
                }
                candidateReportDTO.setCriminalCheckList(criminalCheckListMap);
                CandidateVerificationState candidateVerificationState = candidateService.getCandidateVerificationStateByCandidateId(candidate.getCandidateId());
                boolean hasCandidateVerificationStateChanged = false;
                if (Objects.isNull(candidateVerificationState)) {
                    candidateVerificationState = new CandidateVerificationState();
                    candidateVerificationState.setCandidate(candidate);
                    final ZoneId id = ZoneId.systemDefault();
//                    candidateVerificationState.setCaseInitiationTime(ZonedDateTime.ofInstant(candidate.getCreatedOn().toInstant(), id));
                }
                ZoneId zoneId = ZoneId.of("Asia/Kolkata");
                ZonedDateTime currentDatetime = ZonedDateTime.now(zoneId);

                // Use ternary operator to set currentDate
                candidateReportDTO.setCurrentDate(reportDeliveryDate != null && !reportDeliveryDate.isBlank() && !reportDeliveryDate.contains("null") ? reportDeliveryDate : DateUtil.convertToString(currentDatetime));

                Instant instant = conventionalVendorCandidatesSubmitted.getCreatedOn().toInstant();
                ZonedDateTime zonedDateTime = instant.atZone(zoneId);
                candidateReportDTO.setCaseInitiationDate(DateUtil.convertToString(zonedDateTime));

                switch (reportType) {
                    case FINAL:
                        if (updated.equalsIgnoreCase("UPDATE")) {
                            candidateVerificationState.setFinalReportTime(ZonedDateTime.now());
                        }
                        break;
                    case INTERIM:
                        if (updated.equalsIgnoreCase("UPDATE")) {
                            candidateVerificationState.setInterimReportTime(ZonedDateTime.now());
                        }
                        break;
                }

                candidateVerificationState = candidateService.addOrUpdateCandidateVerificationStateByCandidateId(candidate.getCandidateId(), candidateVerificationState);
                candidateReportDTO.setFinalReportDate(
                        reportDeliveryDate != null && !reportDeliveryDate.isBlank() && !reportDeliveryDate.contains("null")
                                ? DateUtil.convertToString(ZonedDateTime.parse(reportDeliveryDate))
                                : DateUtil.convertToString(candidateVerificationState.getFinalReportTime())
                );

                candidateReportDTO.setInterimReportDate(
                        reportDeliveryDate != null && !reportDeliveryDate.isBlank() && !reportDeliveryDate.contains("null")
                                ? DateUtil.convertToString(ZonedDateTime.parse(reportDeliveryDate))
                                : DateUtil.convertToString(candidateVerificationState.getInterimReportTime())
                );

                System.out.println("Final Report Time: " + candidateReportDTO.getFinalReportDate());
                System.out.println("Interim Report Time: " + candidateReportDTO.getInterimReportDate());

                Long organizationId = organization.getOrganizationId();
                List<OrganizationExecutive> organizationExecutiveByOrganizationId = organizationService.getOrganizationExecutiveByOrganizationId(organizationId);
                List<Map<String, List<Map<String, String>>>> dataList = new ArrayList<>();
                List<ExecutiveSummaryDto> executiveSummaryDtos = new ArrayList<>();
                List<VendorChecks> vendorList = vendorChecksRepository.findAllByCandidateCandidateId(candidate.getCandidateId());
//                vendorList = vendorList.stream()
//                        .sorted(Comparator.comparing(entity -> {
//                            Long sourceId = entity.getSource().getSourceId();
//                            // Customize the sorting order based on source ID matches
//                            List<Long> educationIds = Arrays.asList(24L, 25L, 26L, 27L, 45L);
//                            List<Long> employmentIds = Arrays.asList(28L, 29L, 44L, 46L, 50L);
//                            List<Long> addressIds = Arrays.asList(19L, 20L, 21L, 22L, 23L, 48L);
//                            List<Long> identityIds = Arrays.asList(11L, 12L, 13L, 14L);
//                            List<Long> ofacIds = Arrays.asList(39L);
//                            if (educationIds.contains(sourceId)) {
//                                return 1L; // education
//                            } else if (employmentIds.contains(sourceId)) {
//                                return 2L; // employment
//                            } else if (addressIds.contains(sourceId)) {
//                                return 3L; // address
//                            } else if (identityIds.contains(sourceId)) {
//                                return 4L; // identity
//                            } else if (ofacIds.contains(sourceId)) {
//                                return 5L; // ofac
//                            } else {
//                                return 6L; // criminal, global
//                            }
//                        }))
//                        .collect(Collectors.toList());
                VendorUploadChecksDto vendorUploadChecksDto = null;
                vendorList = vendorList.stream().filter(vca -> {
                    return vca.getVendorCheckStatusMaster() != null && vca.getVendorCheckStatusMaster().getVendorCheckStatusMasterId() != 3l && vca.getVendorCheckStatusMaster().getVendorCheckStatusMasterId() != 2l;
                }).collect(Collectors.toList());
                for (VendorChecks vendorChecks : vendorList) {
                    User user = userRepository.findByUserId(vendorChecks.getVendorId());
                    VendorUploadChecks vendorChecksss = vendorUploadChecksRepository.findByVendorChecksVendorcheckId(vendorChecks.getVendorcheckId());
                    if (vendorChecksss != null) {
                        // Set vendor attributes
                        ArrayList<VendorAttributeDto> vendorAttributeDtos = new ArrayList<>();
                        VendorAttributeDto vendorAttributeDto = new VendorAttributeDto();

                        Optional<ConventionalVendorliChecksToPerform> optionalCheck = liCheckToPerformRepository.findById(vendorChecksss.getVendorChecks().getLicheckId())
                                .filter(stopcheck -> !"TRUE".equalsIgnoreCase(stopcheck.getStopCheck()));

                        if (optionalCheck.isPresent()) {
                            ConventionalVendorliChecksToPerform conventionalVendorliChecksToPerform = optionalCheck.get();
                            if (conventionalVendorliChecksToPerform.getSource().getSourceName().equalsIgnoreCase("GLOBAL DATABASE CHECK")) {
                                ObjectMapper objectMapper = new ObjectMapper();
                                for (String jsonData : vendorChecksss.getVendorAttirbuteValue()) {
                                    Map<String, List<Map<String, String>>> dataMap = objectMapper.readValue(jsonData, new TypeReference<Map<String, List<Map<String, String>>>>() {
                                    });
                                    dataList.add(dataMap);
                                }
                                vendorAttributeDto.setSourceName(conventionalVendorliChecksToPerform.getCheckName());
                                vendorAttributeDto.setCheckStatus(conventionalVendorliChecksToPerform.getCheckStatus().getCheckStatusCode());
                                vendorAttributeDtos.add(vendorAttributeDto);
                            } else {
                                if (conventionalVendorliChecksToPerform.getSource().getSourceName().contains("EMPLOYMENT")) {
                                    int lastNumber = getLastNumber(conventionalVendorliChecksToPerform.getCheckName());
                                    vendorAttributeDto.setSourceName(conventionalVendorliChecksToPerform.getCheckName());
                                    vendorAttributeDto.setCheckStatus(conventionalVendorliChecksToPerform.getCheckStatus().getCheckStatusCode());
                                    vendorAttributeDto.setVendorAttirbuteValue(vendorChecksss.getVendorAttirbuteValue());
                                    vendorAttributeDto.setIndexNumber(lastNumber);
                                    vendorAttributeDtos.add(vendorAttributeDto);
                                } else {
                                    vendorAttributeDto.setSourceName(conventionalVendorliChecksToPerform.getCheckName());
                                    vendorAttributeDto.setCheckStatus(conventionalVendorliChecksToPerform.getCheckStatus().getCheckStatusCode());
                                    vendorAttributeDto.setVendorAttirbuteValue(vendorChecksss.getVendorAttirbuteValue());
                                    vendorAttributeDtos.add(vendorAttributeDto);
                                }
                            }
                            vendorUploadChecksDto = new VendorUploadChecksDto(user.getUserFirstName(), vendorChecksss.getVendorChecks().getVendorcheckId(), vendorChecksss.getVendorUploadedDocument(), vendorChecksss.getDocumentname(), vendorChecksss.getAgentColor().getColorName(), vendorChecksss.getAgentColor().getColorHexCode(), null);
                            vendorUploadChecksDto.setCheckUniqueId(String.valueOf(conventionalVendorliChecksToPerform.getCheckUniqueId()));
//                        if (vendorUploadChecksDto.getDocument() != null) {
//                            File tempFile = File.createTempFile(vendorUploadChecksDto.getDocumentname(), ".pdf");
//                            FileOutputStream fos = new FileOutputStream(tempFile);
//                            fos.write(vendorUploadChecksDto.getDocument());
//                            fos.close();
//                            log.info("temp file" + tempFile);
//                        }
                            vendorUploadChecksDto.setVendorAttirbuteValue(vendorAttributeDtos);
                            vendordocDtoList.add(vendorUploadChecksDto);
//
                        } else {
                            // Handle the case where the check is not found or stopCheck is "TRUE"
                            // For example, log a warning or throw an exception
                            log.warn("No valid check found for VendorCheck ID: " + vendorChecks.getVendorcheckId());
                        }
                    }
                }
                List<VendorUploadChecksDto> collect1 = vendordocDtoList.stream().filter(da -> da.getVendorAttirbuteValue() != null).collect(Collectors.toList());
                Collections.sort(collect1, Comparator.comparing(dto -> {
                    List<VendorAttributeDto> vendorAttributeValue = dto.getVendorAttirbuteValue();
                    // Sorting based on indexNumber of the first element in vendorAttributeValue
                    return vendorAttributeValue.isEmpty() ? Integer.MAX_VALUE : vendorAttributeValue.get(0).getIndexNumber();
                }));
                candidateReportDTO.setVendorProofDetails(collect1);
                candidateReportDTO.setDataList(dataList);
                List<ConventionalVendorliChecksToPerform> allLichecks = liCheckToPerformRepository.findByRequestId(conventionalVendorCandidatesSubmitted.getRequestId());

                List<ConventionalVendorliChecksToPerform> filteredCheckstatusCodeLicheck = byCandidateId.stream().filter(check -> {
                            String code = check.getCheckStatus().getCheckStatusCode();
                            return code.equals("CLEAR") ||
                                    code.equals("MINORDISCREPANCY") ||
                                    code.equals("QCPENDING") ||
                                    code.equals("UNABLETOVERIFY") ||
                                    code.equals("MAJORDISCREPANCY");
                        })
                        .collect(Collectors.toList());
                filteredCheckstatusCodeLicheck = filteredCheckstatusCodeLicheck.stream().filter(venfilter ->
                {
                    String vendorCheckStatusCoode = venfilter.getVendorChecks().getVendorCheckStatusMaster().getCheckStatusCode();
                    return vendorCheckStatusCoode.equals("CLEAR") ||
                            vendorCheckStatusCoode.equals("MINORDISCREPANCY") ||
                            vendorCheckStatusCoode.equals("QCPENDING") ||
                            vendorCheckStatusCoode.equals("UNABLETOVERIFY") ||
                            vendorCheckStatusCoode.equals("MAJORDISCREPANCY");
                }).collect(Collectors.toList());
                List<VendorUploadChecks> result = new ArrayList<>();

                List<Long> vendorCheckIds = filteredCheckstatusCodeLicheck
                        .stream().map(data -> data.getVendorChecks().getVendorcheckId())
                        .collect(Collectors.toList());
                vendorCheckIds.forEach(vendorcheckId -> {
                    VendorUploadChecks byVendorChecksVendorcheckId = vendorUploadChecksRepository.findByVendorChecksVendorcheckId(vendorcheckId);
                    result.add(byVendorChecksVendorcheckId);
                    System.out.println("vendorcheck id " + vendorcheckId);
                });

                Map<String, ConventionalCandidateReportDto> checkNameToDtoMap = new HashMap<>();
                List<File> tempFileToDelete = new ArrayList<>();
                List<Map<String, List<String>>> encodedImagesList = new ArrayList<>();
                List<InputStream> collect = new ArrayList<>();
                File report = FileUtil.createUniqueTempFile("report", ".pdf");
                tempFileToDelete.add(report);
                String conventionalHtmlStr = null;
                Date createdOn = candidate.getCreatedOn();
                String htmlStr = pdfService.parseThymeleafTemplateForConventionalCandidate("Conventional/BaseView", candidateReportDTO);
                checkNameToDtoMap.put("Conventional/BaseView", candidateReportDTO);
                pdfService.generatePdfFromHtml(htmlStr, report);
                List<Content> contentList = contentRepository.findAllByCandidateIdAndContentTypeIn(candidate.getCandidateId(), Arrays.asList(ContentType.ISSUED, ContentType.AGENT_UPLOADED));
                List<File> files = contentList.stream().map(content -> {
                    File uniqueTempFile = FileUtil.createUniqueTempFile(candidateId + "_issued_" + content.getContentId().toString(), ".pdf");
                    tempFileToDelete.add(uniqueTempFile);
                    awsUtils.getFileFromS3(content.getBucketName(), content.getPath(), uniqueTempFile);
                    return uniqueTempFile;
                }).collect(Collectors.toList());
                File mergedFile = FileUtil.createUniqueTempFile(String.valueOf(candidateId), ".pdf");
                tempFileToDelete.add(mergedFile);
                collect.add(FileUtil.convertToInputStream(report));
                collect.addAll(files.stream().map(FileUtil::convertToInputStream).collect(Collectors.toList()));

                for (VendorUploadChecks vendorUploadCheck : result) {
                    boolean isbase64 = false;
                    Map<String, List<String>> encodedImageMap = new HashMap<>();
                    Long checkId = vendorUploadCheck.getVendorChecks().getVendorcheckId();
                    String sourceName = vendorUploadCheck.getVendorChecks().getSource().getSourceName();
//                    if (checkName != null && !checkName.isEmpty()) {
                    Optional<ConventionalVendorliChecksToPerform> byId = liCheckToPerformRepository.findById(vendorUploadCheck.getVendorChecks().getLicheckId());
                    String nameOfCheck = byId.get().getCheckName();
                    byte[] documentBytes = vendorUploadCheck.getVendorUploadedDocument();
                    String jsonString = null;
                    ObjectMapper objectMapper = new ObjectMapper();
                    String vendorUploadedImages = vendorUploadCheck.getVendorUploadedImage();
                    String documentPresicedUrl = null;
                    if (vendorUploadCheck.getVendorUploadDocumentPathKey() != null) {
                        log.info("inside the aws path key retival for the check    --" + sourceName);
                        try {
                            documentBytes = awsUtils.getbyteArrayFromS3(DIGIVERIFIER_DOC_BUCKET_NAME, vendorUploadCheck.getVendorUploadDocumentPathKey());
                            // Check if the document is not PDF
                            if (!isPDF(documentBytes)) {
                                String base64EncodedDocument = Base64.getEncoder().encodeToString(documentBytes);
                                documentPresicedUrl = base64EncodedDocument;
                                isbase64 = true;
                            } else {
//                                     Use the original document
                                documentPresicedUrl = vendorUploadCheck.getVendorUploadDocumentPathKey();
                                log.info("Pdf Path url  " + documentPresicedUrl);
                                isbase64 = false;
                            }
                            vendorUploadedImages = new String(documentPresicedUrl);
                            // Convert list to JSONArray
                            JSONArray jsonArray = new JSONArray();
//							for (String base64 : imageBase64List) {
                            JSONObject jsonObject = new JSONObject();
                            JSONArray imageArray = new JSONArray();
                            imageArray.put(vendorUploadedImages);
                            jsonObject.put("image", imageArray);
                            jsonArray.put(jsonObject);
//							}
                            // Convert the JSON array to a string
                            jsonString = jsonArray.toString();

                        } catch (IOException e) {
                            log.info("Exception in DIGIVERIFIER_DOC_BUCKET_NAME {}" + e);
                        }
                    } else {

                        String base64EncodedDocument = vendorUploadedImages;
                        documentPresicedUrl = base64EncodedDocument;
                        isbase64 = true;
                        jsonString = documentPresicedUrl;


                    }

                    try {
                        if (vendorUploadedImages != null) {
                            List<Map<String, List<String>>> decodedImageList = objectMapper.readValue(jsonString, new TypeReference
                                    <List<Map<String, List<String>>>>() {
                            });

                            List<String> allEncodedImages = decodedImageList.stream()
                                    .flatMap(imageMap -> imageMap.values().stream())
                                    .flatMap(List::stream)
                                    .collect(Collectors.toList());

                            // Loop through each image byte array and encode it to Base64
                            List<String> encodedImagesForDocument = new ArrayList<>();
                            encodedImageMap.put(nameOfCheck, allEncodedImages);
                        } else {
                            log.info("Vendor uploaded document is null {}");
                            encodedImageMap.put(nameOfCheck, null);

                        }
                        encodedImagesList.add(encodedImageMap);
                        try {
                            ConventionalCandidateReportDto testConventionalCandidateReportDto = null;
                            if (vendorUploadedImages != null) {
                                if (isbase64 == true) {
                                    log.info("BASE64 IMG for " + nameOfCheck + " entry");
                                    List<Map<String, List<String>>> dynamicEncodedImagesList = new ArrayList<>();
                                    // Generate table for this education entry
                                    File allcheckDynamicReport = FileUtil.createUniqueTempFile(byId.get().getCheckName(), ".pdf");
                                    tempFileToDelete.add(allcheckDynamicReport);
                                    String templateName;
                                    if (nameOfCheck.contains("EDUCATION")) {
                                        templateName = "Conventional/EducationCheck";
                                    } else if (nameOfCheck.contains("EMPLOYMENT")) {
                                        templateName = "Conventional/EmploymentCheck";
                                    } else if (nameOfCheck.contains("CRIMINAL")) {
                                        templateName = "Conventional/CriminalCheck";
                                    } else if (nameOfCheck.contains("GLOBAL")) {
                                        templateName = "Conventional/GlobalCheck";
                                    } else if (nameOfCheck.contains("IDENTITY")) {
                                        templateName = "Conventional/IdentityCheck";
                                    } else if (nameOfCheck.contains("LEGAL")) {
                                        templateName = "Conventional/LegalRightCheck";
                                    } else if (nameOfCheck.contains("OFAC")) {
                                        templateName = "Conventional/OfacCheck";
                                    } else if (nameOfCheck.contains("ADDRESS")) {
                                        templateName = "Conventional/AddressCheck";
                                    } else if (nameOfCheck.contains("REFERENCE")) {
                                        templateName = "Conventional/ReferenceCheck";
                                    } else if (nameOfCheck.contains("CV VALIDATION")) {
                                        templateName = "Conventional/CvValidation";
                                    } else if (nameOfCheck.contains("DRUG CHECK")) {
                                        templateName = "Conventional/DrugCheck";
                                    } else if (nameOfCheck.contains("CREDIT BUREAU CHECK")) {
                                        templateName = "Conventional/CreditBreauCheck";
                                    } else if (nameOfCheck.contains("Professional Certificate Check")) {
                                        templateName = "Conventional/Professionalcertificatecheck";
                                    } else if (nameOfCheck.contains("CMotor Vehicle Records(MVR)")) {
                                        templateName = "Conventional/MotorVehicleRecords(MVR)";
                                    } else if (nameOfCheck.contains("FACIS level 3 Check")) {
                                        templateName = "Conventional/FACISlevel3Check";
                                    } else {
                                        templateName = "Conventional/NoTableCheck";
                                    }
                                    testConventionalCandidateReportDto = new ConventionalCandidateReportDto();
                                    List<VendorUploadChecksDto> filteredVendorProofs = candidateReportDTO.getVendorProofDetails().stream().filter(p -> p.getCheckUniqueId().equalsIgnoreCase(String.valueOf(byId.get().getCheckUniqueId())) == true).collect(Collectors.toList());
                                    testConventionalCandidateReportDto.setVendorProofDetails(filteredVendorProofs);
                                    Map<String, LegalProceedingsDTO> filteredCriminalCheckList = candidateReportDTO.getCriminalCheckList()
                                            .entrySet()
                                            .stream()
                                            .filter(entry -> nameOfCheck.equals(entry.getKey()))
                                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                                    testConventionalCandidateReportDto.setCriminalCheckList(filteredCriminalCheckList);
                                    testConventionalCandidateReportDto.setDataList(candidateReportDTO.getDataList());
                                    dynamicEncodedImagesList.add(encodedImageMap);
                                    testConventionalCandidateReportDto.setPdfByes(dynamicEncodedImagesList);
                                    String tableHtmlStr = pdfService.parseThymeleafTemplateForConventionalCandidate(templateName, testConventionalCandidateReportDto);
                                    checkNameToDtoMap.put(templateName, testConventionalCandidateReportDto);
                                    pdfService.generatePdfFromHtml(tableHtmlStr, allcheckDynamicReport);
                                    // Collect education proof and table
                                    List<InputStream> educationProof = new ArrayList<>();
                                    educationProof.add(FileUtil.convertToInputStream(allcheckDynamicReport));
                                    collect.addAll(educationProof);
                                } else {
                                    log.info("Fetching the PDF Proof for :" + nameOfCheck);
                                    // Fetch the PDF file from S3
                                    File fileFromS3 = awsUtils.getFileFromS3(DIGIVERIFIER_DOC_BUCKET_NAME, documentPresicedUrl);
                                    tempFileToDelete.add(fileFromS3);
                                    // Generate table for this education entry
                                    File allcheckDynamicReport = FileUtil.createUniqueTempFile(byId.get().getCheckName(), ".pdf");
                                    tempFileToDelete.add(allcheckDynamicReport);
                                    String templateName;
                                    if (nameOfCheck.contains("EDUCATION")) {
                                        templateName = "Conventional/EducationCheck";
                                    } else if (nameOfCheck.contains("EMPLOYMENT")) {
                                        templateName = "Conventional/EmploymentCheck";
                                    } else if (nameOfCheck.contains("CRIMINAL")) {
                                        templateName = "Conventional/CriminalCheck";
                                    } else if (nameOfCheck.contains("GLOBAL")) {
                                        templateName = "Conventional/GlobalCheck";
                                    } else if (nameOfCheck.contains("IDENTITY")) {
                                        templateName = "Conventional/IdentityCheck";
                                    } else if (nameOfCheck.contains("LEGAL")) {
                                        templateName = "Conventional/LegalRightCheck";
                                    } else if (nameOfCheck.contains("OFAC")) {
                                        templateName = "Conventional/OfacCheck";
                                    } else if (nameOfCheck.contains("ADDRESS")) {
                                        templateName = "Conventional/AddressCheck";
                                    } else if (nameOfCheck.contains("REFERENCE")) {
                                        templateName = "Conventional/ReferenceCheck";
                                    } else if (nameOfCheck.contains("CV VALIDATION")) {
                                        templateName = "Conventional/CvValidation";
                                    } else if (nameOfCheck.contains("DRUG CHECK")) {
                                        templateName = "Conventional/DrugCheck";
                                    } else if (nameOfCheck.contains("CREDIT BUREAU CHECK")) {
                                        templateName = "Conventional/CreditBreauCheck";
                                    } else if (nameOfCheck.contains("Professional Certificate Check")) {
                                        templateName = "Conventional/Professionalcertificatecheck";
                                    } else if (nameOfCheck.contains("Motor Vehicle Records(MVR)")) {
                                        templateName = "Conventional/MotorVehicleRecords(MVR)";
                                    } else if (nameOfCheck.contains("FACIS level 3 Check")) {
                                        templateName = "Conventional/FACISlevel3Check";
                                    } else {
                                        templateName = "Conventional/NoTableChecks";
                                    }

                                    testConventionalCandidateReportDto = new ConventionalCandidateReportDto();
                                    List<VendorUploadChecksDto> filteredVendorProofs = candidateReportDTO.getVendorProofDetails().stream().filter(p -> p.getCheckUniqueId().equalsIgnoreCase(String.valueOf(byId.get().getCheckUniqueId())) == true).collect(Collectors.toList());
                                    testConventionalCandidateReportDto.setVendorProofDetails(filteredVendorProofs);
                                    Map<String, LegalProceedingsDTO> filteredCriminalCheckList = candidateReportDTO.getCriminalCheckList()
                                            .entrySet()
                                            .stream()
                                            .filter(entry -> nameOfCheck.equals(entry.getKey()))
                                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                                    testConventionalCandidateReportDto.setCriminalCheckList(filteredCriminalCheckList);
                                    testConventionalCandidateReportDto.setDataList(candidateReportDTO.getDataList());
                                    String tableHtmlStr = pdfService.parseThymeleafTemplateForConventionalCandidate(templateName, testConventionalCandidateReportDto);
                                    checkNameToDtoMap.put(templateName, testConventionalCandidateReportDto);
                                    pdfService.generatePdfFromHtml(tableHtmlStr, allcheckDynamicReport);
                                    // Collect education proof and table
                                    List<InputStream> educationProof = new ArrayList<>();
                                    educationProof.add(FileUtil.convertToInputStream(allcheckDynamicReport));
                                    educationProof.add(new FileInputStream(fileFromS3));
                                    collect.addAll(educationProof);
                                }
                            }
                        } catch (IOException e) {
                            log.error("Exception occurred: {}", e);
                        }
                    } catch (JsonProcessingException e) {
                        // Handle the exception (e.g., log or throw)
                        e.printStackTrace();
                    }
                }
                candidateReportDTO.setPdfByes(encodedImagesList);
                if (agentUploadedList.isEmpty() == false) {
                    List<InputStream> educationProof = new ArrayList<>();
                    agentUploadedList.forEach(agentchecks -> {
                        File allcheckDynamicReport = FileUtil.createUniqueTempFile("UanCheck", ".pdf");
                        String templateName = "Conventional/UANCheck";
                        ConventionalCandidateReportDto conventionalCandidateReportDto = new ConventionalCandidateReportDto();
                        conventionalCandidateReportDto.setAgentUploadedChecks(agentchecks);
                        String tableHtmlStr = pdfService.parseThymeleafTemplateForConventionalCandidate(templateName, conventionalCandidateReportDto);
                        checkNameToDtoMap.put(templateName, conventionalCandidateReportDto);
                        pdfService.generatePdfFromHtml(tableHtmlStr, allcheckDynamicReport);
                        educationProof.add(FileUtil.convertToInputStream(allcheckDynamicReport));
                        try {
                            File fileFromS3 = awsUtils.getFileFromS3(DIGIVERIFIER_DOC_BUCKET_NAME, agentchecks.getPathKey());
                            tempFileToDelete.add(fileFromS3);
                            educationProof.add(new FileInputStream(fileFromS3));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        tempFileToDelete.add(allcheckDynamicReport);
                        collect.addAll(educationProof);
                    });
                }
                List<Content> uploadedDocContentList = new ArrayList<>();
                List<File> uploadedDocuments = uploadedDocContentList.stream().map(content -> {
                    File uniqueTempFile = FileUtil.createUniqueTempFile(
                            candidateId
                                    + "_issued_" + content.getContentId().toString(), ".pdf");
                    awsUtils.getFileFromS3(content.getBucketName(), content.getPath(), uniqueTempFile);
                    return uniqueTempFile;
                }).collect(Collectors.toList());
                tempFileToDelete.addAll(uploadedDocuments);
                collect.addAll(uploadedDocuments.stream().map(FileUtil::convertToInputStream).collect(Collectors.toList()));


                ClassPathResource resource = new ClassPathResource("disclaimer.pdf");

                try (InputStream inputStream = resource.getInputStream()) {
                    collect.add(resource.getInputStream());
                    PdfUtil.mergePdfFiles(collect, new FileOutputStream(mergedFile.getPath()));
                    log.info("after merge pdf files");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    log.error("Exception 3 occured in generateDocument method in ReportServiceImpl-->", e);
                }
                tempFileToDelete.add(mergedFile);
                String jsonPath = "Digiverifier_Analytics_LTIM_Conventional/Candidate/".concat(candidateId + "/Generated".concat("/").concat(conventionalVendorCandidatesSubmitted.getRequestId() + "_" + conventionalVendorCandidatesSubmitted.getName() + "_" + reportType.name()).concat(".pdf"));
                String jsonPrecisedUrl = awsUtils.uploadDtosAsJsonToS3AndReturnPresignedUrl(checkNameToDtoMap, jsonPath, DIGIVERIFIER_DOC_BUCKET_NAME);
                String tempPath = "TEMP/Candidate/".concat(candidateId + "/Generated".concat("/").concat(conventionalVendorCandidatesSubmitted.getRequestId() + "_" + conventionalVendorCandidatesSubmitted.getName() + "_" + reportType.name()).concat(".pdf"));
                String pdfUrl = awsUtils.uploadFileAndGetPresignedUrlTemp(DIGIVERIFIER_DOC_BUCKET_NAME, tempPath, mergedFile);
                log.info("merged pdf url" + pdfUrl);
                Content content = new Content();
                content.setCandidateId(candidate.getCandidateId());
                content.setContentCategory(ContentCategory.OTHERS);
                if (reportType.name().equalsIgnoreCase("INTERIM")) {
                    content.setContentSubCategory(ContentSubCategory.INTERIM);
                } else if (reportType.name().equalsIgnoreCase("FINAL")) {
                    content.setContentSubCategory(ContentSubCategory.FINAL);
                }
                content.setFileType(FileType.PDF);
                content.setContentType(ContentType.GENERATED);
                content.setBucketName(DIGIVERIFIER_DOC_BUCKET_NAME);
                content.setCreatedOn(new Date());
                content.setPath(jsonPath);
                contentRepository.save(content);
                stringServiceOutcome.setStatus(tempPath);
                stringServiceOutcome.setData(pdfUrl); // Set Base64 string as the data
                //delete temporary files
                if (tempFileToDelete != null && tempFileToDelete.isEmpty() == false) {
                    for (File file : tempFileToDelete) {
                        if (file.exists() && file.isFile()) {
                            if (file.delete()) {
                                log.info("Deleted dynamic report file: " + file.getPath());
                            } else {
                                log.warn("Failed to delete dynamic report file: " + file.getPath());
                            }
                        }
                    }
                }

            } else {
                throw new RuntimeException("unable to generate document for this candidate");
            }
        } catch (Exception e) {
            log.error("exception in generateConventionalCandidateReport() ---" + e.getMessage());
        }
        return stringServiceOutcome;
    }


    public ServiceOutcome<String> generateUnableToVerifiyReport(Long candidateId, ReportType reportType, String updated) {
        ServiceOutcome<String> stringServiceOutcome = new ServiceOutcome<>();
        ArrayList<LicheckRequiredResponseDto> licheckRequiredResponseDtos = new ArrayList<>();
        List<AgentUploadedChecks> agentUploadedList = new ArrayList<>();
        try {
            log.debug("generateConventionalCandidateReport() Starts*******************************");
//            Candidate candidate = candidateService.findCandidateByCandidateCode(candidateCode);
            Candidate candidate = candidateRepository.findById(candidateId).get();
            ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(String.valueOf(candidate.getConventionalRequestId()));
            ConventionalCandidate byConventionalRequestId = conventionalCandidateRepository.findByConventionalRequestId(Long.valueOf(conventionalVendorCandidatesSubmitted.getRequestId()));
            if (candidate != null) {
                List<VendorUploadChecksDto> vendordocDtoList = new ArrayList<VendorUploadChecksDto>();
                List<ConventionalCandidateReportDto> jsonConventionalCandidateReportDto = new ArrayList<>();
                ConventionalCandidateReportDto candidateReportDTO = new ConventionalCandidateReportDto();
                candidateReportDTO.setCandidateId(String.valueOf(conventionalVendorCandidatesSubmitted.getCandidateId()));
                candidateReportDTO.setApplicantId(String.valueOf(conventionalVendorCandidatesSubmitted.getApplicantId()));
                candidateReportDTO.setOrganizationName(candidate.getOrganization().getOrganizationName());
                candidateReportDTO.setOrganizationLogo(Arrays.toString(candidate.getOrganization().getOrganizationLogo()));
                candidateReportDTO.setRequestId(conventionalVendorCandidatesSubmitted.getRequestId());
                if (byConventionalRequestId != null) {
                    candidateReportDTO.setAddress(byConventionalRequestId.getBirthPlace());
                }
                if (conventionalVendorCandidatesSubmitted.getVerificationStatus() != null) {
                    if (conventionalVendorCandidatesSubmitted.getVerificationStatus().equalsIgnoreCase("UNABLETOVERIFIY")) {
                        candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.UNABLETOVERIFIY);
                        candidateReportDTO.setColorCode("AMBER");
                    }
                    if (conventionalVendorCandidatesSubmitted.getVerificationStatus().equalsIgnoreCase("MAJORDISCREPANCY")) {
                        candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.MAJORDISCREPANCY);
                        candidateReportDTO.setColorCode("RED");
                    }
                    if (conventionalVendorCandidatesSubmitted.getVerificationStatus().equalsIgnoreCase("MINORDISCREPANCY")) {
                        candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.MINORDISCREPANCY);
                        candidateReportDTO.setColorCode("RED");
                    }
                    if (conventionalVendorCandidatesSubmitted.getVerificationStatus().equalsIgnoreCase("INSUFFICIENCY")) {
                        candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.INSUFFICIENCY);
                        candidateReportDTO.setColorCode("YELLOW");
                    }
                    if (conventionalVendorCandidatesSubmitted.getVerificationStatus().equalsIgnoreCase("INPROGRESS")) {
                        candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.INPROGRESS);
                        candidateReportDTO.setColorCode("");
                    }
                    if (conventionalVendorCandidatesSubmitted.getVerificationStatus().equalsIgnoreCase("CLEAR")) {
                        candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.CLEAR);
                        candidateReportDTO.setColorCode("GREEN");
                    }
                } else {
                    candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.NAN);
                }

                if (byConventionalRequestId != null) {
                    candidateReportDTO.setAddress(byConventionalRequestId.getBirthPlace());
                }
                candidateReportDTO.setName(candidate.getCandidateName());
                candidateReportDTO.setReferenceId(candidate.getApplicantId());
                candidateReportDTO.setDob(candidate.getDateOfBirth());
                candidateReportDTO.setContactNo(candidate.getContactNumber());
                candidateReportDTO.setEmailId(candidate.getEmailId());
                candidateReportDTO.setReportType(reportType);
                Organization organization = candidate.getOrganization();
                candidateReportDTO.setProject(organization.getOrganizationName());
                candidateReportDTO.setOrganizationLocation(organization.getOrganizationLocation());
                candidateReportDTO.setOrganizationLogo(organization.getLogoUrl());
                candidateReportDTO.setComments("");
                List<ConventionalVendorliChecksToPerform> byCandidateId = liCheckToPerformRepository.findByRequestId(String.valueOf(candidate.getConventionalRequestId()));
                byCandidateId = byCandidateId.stream()
                        .filter(p -> !"TRUE".equalsIgnoreCase(p.getStopCheck()) && p.getCheckStatus().getVendorCheckStatusMasterId() != 2l && p.getCheckStatus().getVendorCheckStatusMasterId() != 7l && p.getCheckStatus().getVendorCheckStatusMasterId() != 3l)
                        .collect(Collectors.toList());
                byCandidateId = byCandidateId.stream().filter(en -> en.getSource() != null)
                        .sorted(Comparator.comparing(entity -> {

                            Long sourceId = entity.getSource().getSourceId();

                            // Customize the sorting order based on source ID matches
                            List<Long> educationIds = Arrays.asList(24L, 25L, 26L, 27L, 45L);
                            List<Long> employmentIds = Arrays.asList(28L, 29L, 44L, 46L, 50L);
                            List<Long> addressIds = Arrays.asList(19L, 20L, 21L, 22L, 23L, 48L);
                            List<Long> identityIds = Arrays.asList(11L, 12L, 13L, 14L);
                            List<Long> ofacIds = Arrays.asList(39L);
                            List<Long> referenceIds = Arrays.asList(30L, 31l, 32l);
                            List<Long> drugCheck = Arrays.asList(10l, 33l, 34l, 35l, 36l, 37l);
                            List<Long> cvValidation = Arrays.asList(40l);
                            List<Long> criminalIds = Arrays.asList(15l, 16l, 17l, 18l, 49l, 51l, 52l);
                            List<Long> globalIds = Arrays.asList(38L);
                            if (educationIds.contains(sourceId)) {
                                return 1L; // education
                            } else if (employmentIds.contains(sourceId)) {
                                return 2L; // employment
                            } else if (addressIds.contains(sourceId)) {
                                return 3L; // address
                            } else if (identityIds.contains(sourceId)) {
                                return 4L; // identity
                            } else if (ofacIds.contains(sourceId)) {
                                return 5L; // ofac
                            } else if (referenceIds.contains(sourceId)) {
                                return 6L; // reference
                            } else if (criminalIds.contains(sourceId)) {
                                return 7L; // criminal
                            } else if (drugCheck.contains(sourceId)) {
                                return 8L; // drug
                            } else if (cvValidation.contains(sourceId)) {
                                return 9L; // cvvalidation
                            } else if (globalIds.contains(sourceId)) {
                                return 10L; // global
                            } else {
                                return 11L; // other than above
                            }
                        })).collect(Collectors.toList());
// Separate employment checks and other checks
                List<ConventionalVendorliChecksToPerform> employmentChecks = byCandidateId.stream()
                        .filter(entity -> entity.getCheckName().contains("EMPLOYER "))
                        .collect(Collectors.toList());

                List<ConventionalVendorliChecksToPerform> otherChecks = byCandidateId.stream()
                        .filter(entity -> !entity.getCheckName().contains("EMPLOYER "))
                        .collect(Collectors.toList());

// Sort employment checks
                Comparator<ConventionalVendorliChecksToPerform> employmentComparator = (entity1, entity2) -> {
                    int index1 = getLastNumber(entity1.getCheckName());
                    int index2 = getLastNumber(entity2.getCheckName());

                    if (index1 == 1 && index2 != 1) {
                        return -1; // entity1 comes first
                    } else if (index1 != 1 && index2 == 1) {
                        return 1; // entity2 comes first
                    }

                    if (index1 == 0 && index2 != 0) {
                        return 1; // entity2 comes first
                    } else if (index1 != 0 && index2 == 0) {
                        return -1; // entity1 comes first
                    }

                    return Integer.compare(index1, index2);
                };

                employmentChecks = employmentChecks.stream()
                        .sorted(employmentComparator)
                        .collect(Collectors.toList());

// Combine sorted employment checks and other checks
                byCandidateId = new ArrayList<>();
                byCandidateId.addAll(employmentChecks);
                byCandidateId.addAll(otherChecks);

                HashMap<String, LegalProceedingsDTO> criminalCheckListMap = new HashMap<>();
                if (byCandidateId.isEmpty() == false) {
                    List<ConventionalVendorliChecksToPerform> collect1 = byCandidateId.stream().filter(licheck -> licheck.getCheckStatus().getVendorCheckStatusMasterId() != 7l && licheck.getCheckStatus().getVendorCheckStatusMasterId() != 2l).collect(Collectors.toList());
                    collect1.forEach(data -> {
                        if (data.getCheckName().contains("EMPLOYMENT")) {
                            LicheckRequiredResponseDto licheckRequiredResponseDto = new LicheckRequiredResponseDto();
                            licheckRequiredResponseDto.setCheckName(data.getCheckName());
                            licheckRequiredResponseDto.setCheckUniqueId(data.getCheckUniqueId());
                            licheckRequiredResponseDto.setCheckStatus(data.getCheckStatus().getCheckStatusCode());
                            licheckRequiredResponseDto.setCheckRemarks(data.getCheckRemarks());
                            int lastNumber = getLastNumber(data.getCheckName());
                            licheckRequiredResponseDto.setIndexNumber(lastNumber);
                            licheckRequiredResponseDto.setDisableStatus("");
                            licheckRequiredResponseDtos.add(licheckRequiredResponseDto);
                        } else {
                            LicheckRequiredResponseDto licheckRequiredResponseDto = new LicheckRequiredResponseDto();
                            licheckRequiredResponseDto.setCheckName(data.getCheckName());
                            licheckRequiredResponseDto.setCheckUniqueId(data.getCheckUniqueId());
                            licheckRequiredResponseDto.setCheckStatus(data.getCheckStatus().getCheckStatusCode());
                            licheckRequiredResponseDto.setCheckRemarks(data.getCheckRemarks());
                            licheckRequiredResponseDto.setDisableStatus("");
                            licheckRequiredResponseDtos.add(licheckRequiredResponseDto);
                        }
                    });
                    agentUploadedList = agentUplaodedChecksRepository.findByRequestID(conventionalVendorCandidatesSubmitted.getRequestId());

                    if (agentUploadedList.isEmpty() == false) {
                        agentUploadedList.forEach(data -> {
                            LicheckRequiredResponseDto licheckRequiredResponseDto = new LicheckRequiredResponseDto();
                            licheckRequiredResponseDto.setCheckName(data.getSource().getSourceName());
                            licheckRequiredResponseDto.setCheckUniqueId(Long.valueOf(data.getUanNo()));
                            licheckRequiredResponseDto.setCheckStatus(data.getColorCode());
                            licheckRequiredResponseDto.setCheckRemarks(data.getRemarks());
                            licheckRequiredResponseDto.setDisableStatus(data.getUanNo());
                            licheckRequiredResponseDtos.add(licheckRequiredResponseDto);
                        });
                    }

                    Comparator<LicheckRequiredResponseDto> indexComparator = (dto1, dto2) -> {
                        // First, handle special cases where one of the indices is 1
                        if (dto1.getIndexNumber() == 1 && dto2.getIndexNumber() != 1) {
                            return -1; // dto1 comes first
                        } else if (dto1.getIndexNumber() != 1 && dto2.getIndexNumber() == 1) {
                            return 1; // dto2 comes first
                        }

                        // Then, ensure that indexNumber 0 comes last
                        if (dto1.getIndexNumber() == 0 && dto2.getIndexNumber() != 0) {
                            return 1; // dto2 comes first
                        } else if (dto1.getIndexNumber() != 0 && dto2.getIndexNumber() == 0) {
                            return -1; // dto1 comes first
                        }

                        // Finally, compare index numbers as usual
                        return Integer.compare(dto1.getIndexNumber(), dto2.getIndexNumber());
                    };

                    // Sort the list
                    Collections.sort(licheckRequiredResponseDtos, indexComparator);

                    licheckRequiredResponseDtos.forEach(data -> {


                        String modifiedCheckName = data.getCheckName().toLowerCase().replaceAll("[\\s-]", "");

                        if (modifiedCheckName.contains("education") && !modifiedCheckName.contains("educationhighest")) {
                            String executiveSummaryValueForCheck = getExecutiveSummaryValueForCheck(data.getCheckUniqueId());
                            if (executiveSummaryValueForCheck != null) {
                                data.setDisableStatus(executiveSummaryValueForCheck);
                            } else {
                                List<ConventionalCandidateCafEducation> conventionalCandidateCafEducations = conventionalCafCandidateEducationRepository.findByConventionalRequestId(Long.valueOf(conventionalVendorCandidatesSubmitted.getRequestId()));
                                if (conventionalCandidateCafEducations.isEmpty() == false) {
                                    conventionalCandidateCafEducations.forEach(convdata -> {
                                        String degreetype = convdata.getDegreeType().toLowerCase().replaceAll("[\\s-]", "").replaceAll("degree", "");
//                                    log.info("Check NAme" + modifiedCheckName + "===" + "    Education -   " + degreetype);
                                        boolean contains = modifiedCheckName.contains(degreetype);
                                        boolean diplomacontains = modifiedCheckName.contains("dipl");
                                        if (contains == true || diplomacontains == true) {
                                            CandidateCafEducation candidateCafEducation = candidateCafEducationRepository.findById(convdata.getCandidateCafEducationId()).get();
                                            data.setDisableStatus(candidateCafEducation.getQualificationType());
                                        }
//                                    log.info("End Education Type setted" + data.getDisableStatus());
                                    });
                                }
                            }
                        }
                        if (modifiedCheckName.contains("educationhighest")) {
                            String executiveSummaryValueForCheck = getExecutiveSummaryValueForCheck(data.getCheckUniqueId());
                            if (executiveSummaryValueForCheck != null) {
                                data.setDisableStatus(executiveSummaryValueForCheck);
                            } else {
                                List<ConventionalCandidateCafEducation> conventionalCandidateCafEducations = conventionalCafCandidateEducationRepository.findByConventionalRequestId(Long.valueOf(conventionalVendorCandidatesSubmitted.getRequestId()));
                                if (conventionalCandidateCafEducations.isEmpty() == false) {
                                    conventionalCandidateCafEducations.forEach(convdata -> {
                                        String degreetype = convdata.getDegreeType().toLowerCase().replaceAll("[\\s-]", "");
//                                    log.info("Check NAme" + modifiedCheckName + "===" + "    Education -   " + degreetype);
//                                    CandidateCafEducation candidateCafEducation = candidateCafEducationRepository.findById(convdata.getCandidateCafEducationId()).get();
                                        Optional<CandidateCafEducation> optionalCandidateCafEducation = candidateCafEducationRepository.findById(convdata.getCandidateCafEducationId());
                                        if (optionalCandidateCafEducation.isPresent()) {
                                            CandidateCafEducation candidateCafEducation = optionalCandidateCafEducation.get();

                                            if (degreetype.toLowerCase().contains("undergraduate")) {

                                                data.setDisableStatus(candidateCafEducation.getQualificationType());
                                            } else if (degreetype.toLowerCase().contains("postgraduate")) {
                                                // Handle postgraduate case
                                                data.setDisableStatus(candidateCafEducation.getQualificationType());

                                            } else if (degreetype.toLowerCase().contains("diploma")) {
                                                // Handle diploma case

                                                data.setDisableStatus(candidateCafEducation.getQualificationType());
                                            } else if (degreetype.toLowerCase().contains("highschool")) {
                                                // Handle high school case

                                                data.setDisableStatus(candidateCafEducation.getQualificationType());
                                            } else if (degreetype.toLowerCase().contains("highest")) {
                                                // Handle highest education case
                                                data.setDisableStatus(candidateCafEducation.getQualificationType());
                                            } else {

                                                data.setDisableStatus(candidateCafEducation.getQualificationType());
                                            }
                                        }
                                    });
                                }
                            }
                        }
                        if (modifiedCheckName.contains("address")) {
                            List<ConventionalCafAddress> conventionalCafAddresses = conventionCafAddressRepository.findByConventionalRequestId(Long.valueOf(conventionalVendorCandidatesSubmitted.getRequestId()));
                            if (conventionalCafAddresses.isEmpty() == false) {
                                conventionalCafAddresses.forEach(convdata -> {
                                    String addressType = convdata.getAddressType().toLowerCase().replaceAll("[\\s-]", "");
//                                    log.info("Check NAme" + modifiedCheckName + "===" + "Address  type-   " + addressType);
                                    if (modifiedCheckName.contains("present")) {
                                        boolean contains = modifiedCheckName.replaceAll("present", "current").contains(addressType);
                                        if (contains == true) {
                                            CandidateCafAddress candidateCafAddress = candidateCafAddressRepository.findById(convdata.getCandidateCafAddressId()).get();
                                            data.setDisableStatus(candidateCafAddress.getCity() + "," + candidateCafAddress.getState());
//                                            log.info("End Address Type setted Present or current" + data.getDisableStatus());
                                        }

                                    } else if (modifiedCheckName.contains("permenent")) {
                                        boolean contains = modifiedCheckName.replaceAll("permenent", "permanent").contains(addressType);
                                        if (contains == true) {
                                            CandidateCafAddress candidateCafAddress = candidateCafAddressRepository.findById(convdata.getCandidateCafAddressId()).get();
                                            data.setDisableStatus(candidateCafAddress.getCity() + "," + candidateCafAddress.getState());
//                                            log.info("End Address Type setted Present or current" + data.getDisableStatus());
                                        }

                                    } else {
                                        boolean contains = modifiedCheckName.contains(addressType);
                                        if (contains == true) {
                                            CandidateCafAddress candidateCafAddress = candidateCafAddressRepository.findById(convdata.getCandidateCafAddressId()).get();
                                            data.setDisableStatus(candidateCafAddress.getCity() + "," + candidateCafAddress.getState());
//                                            log.info("End Address Type setted permenant" + data.getDisableStatus());
                                        }
                                    }

                                });
                            }
                        }
                        if (modifiedCheckName.contains("employment")) {
                            String executiveSummaryValueForCheck = getExecutiveSummaryValueForCheck(data.getCheckUniqueId());
                            if (executiveSummaryValueForCheck != null) {
                                data.setDisableStatus(executiveSummaryValueForCheck);
                            } else {
                                List<ConventionalCandidateExperience> conventionalexperienceS = conventionalCandidateExperienceRepository.findByConventionalRequestId(Long.valueOf(conventionalVendorCandidatesSubmitted.getRequestId()));
                                if (conventionalexperienceS.isEmpty() == false) {
                                    conventionalexperienceS.forEach(convdata -> {
                                        String employmentType = convdata.getEmploymentType().toLowerCase().replaceAll("[\\s-]", "");
//                                    log.info("Check NAme" + modifiedCheckName + "===" + "Employment -   " + employmentType);
                                        boolean contains = modifiedCheckName.contains(employmentType);
                                        if (contains == true) {
                                            CandidateCafExperience candidateCafExperience = candidateCafExperienceRepository.findById(convdata.getCandidateCafExperience()).get();
                                            data.setDisableStatus(candidateCafExperience.getCandidateEmployerName());

                                        }
                                    });


                                }
                            }
                        }
                        if (modifiedCheckName.contains("pan") && modifiedCheckName.contains("identity")) {
                            String panNumber = candidate.getPanNumber();
                            if (panNumber != null) {
                                String maskedPanNumber = panNumber.substring(0, panNumber.length() - 5) + "XXXXX";
                                data.setDisableStatus(maskedPanNumber);
                            }

                        }
                        if (modifiedCheckName.contains("passport")) {
                            data.setDisableStatus(candidate.getPassportNumber());
                        }
                        if (modifiedCheckName.contains("aadhar")) {
                            data.setDisableStatus(candidate.getAadharNumber());
                        }
                        if (modifiedCheckName.contains("driving")) {
                            data.setDisableStatus(candidate.getDrivingLicenseNumber());
                        }
                        if (modifiedCheckName.contains("criminalcheck")) {
                            LegalProceedingsDTO legalProceedingsDTO = new LegalProceedingsDTO();

                            List<CriminalCheck> civilproceding = criminalCheckRepository.findByCheckUniqueIdAndProceedingsType(String.valueOf(data.getCheckUniqueId()), "CIVILPROCEDING");
                            if (civilproceding.isEmpty() == false) {
                                legalProceedingsDTO.setCivilProceedingList(civilproceding);
                            }
                            List<CriminalCheck> criminalproceding = criminalCheckRepository.findByCheckUniqueIdAndProceedingsType(String.valueOf(data.getCheckUniqueId()), "CRIMINALPROCEDING");
                            if (criminalproceding.isEmpty() == false) {
                                legalProceedingsDTO.setCriminalProceedingList(criminalproceding);
                            }
                            criminalCheckListMap.put(String.valueOf(data.getCheckName()), legalProceedingsDTO);
//                            log.info("criminal check data" + criminalCheckListMap);
                        }

                    });
                    candidateReportDTO.setLiChecksDetails(licheckRequiredResponseDtos);
                } else {
                    candidateReportDTO.setLiChecksDetails(new ArrayList<>());
                }
                candidateReportDTO.setCriminalCheckList(criminalCheckListMap);
                CandidateVerificationState candidateVerificationState = candidateService.getCandidateVerificationStateByCandidateId(candidate.getCandidateId());
                boolean hasCandidateVerificationStateChanged = false;
                if (Objects.isNull(candidateVerificationState)) {
                    candidateVerificationState = new CandidateVerificationState();
                    candidateVerificationState.setCandidate(candidate);
                    final ZoneId id = ZoneId.systemDefault();
//                    candidateVerificationState.setCaseInitiationTime(ZonedDateTime.ofInstant(candidate.getCreatedOn().toInstant(), id));
                }
                ZoneId zoneId = ZoneId.of("Asia/Kolkata");
                ZonedDateTime currentDatetime = ZonedDateTime.now(zoneId);
                candidateReportDTO.setCurrentDate(DateUtil.convertToString(currentDatetime));
                Instant instant = conventionalVendorCandidatesSubmitted.getCreatedOn().toInstant();
                ZonedDateTime zonedDateTime = instant.atZone(zoneId);
                candidateReportDTO.setCaseInitiationDate(DateUtil.convertToString(zonedDateTime));
                switch (reportType) {
                    case FINAL:
                        if (updated.equalsIgnoreCase("UPDATE")) {
                            candidateVerificationState.setFinalReportTime(ZonedDateTime.now());
                        }
                        break;
                    case INTERIM:
                        if (updated.equalsIgnoreCase("UPDATE")) {
                            candidateVerificationState.setInterimReportTime(ZonedDateTime.now());
                        }
                        break;
                }
                candidateVerificationState = candidateService.addOrUpdateCandidateVerificationStateByCandidateId(candidate.getCandidateId(), candidateVerificationState);
                candidateReportDTO.setFinalReportDate(DateUtil.convertToString(candidateVerificationState.getFinalReportTime()));
                candidateReportDTO.setInterimReportDate(DateUtil.convertToString(candidateVerificationState.getInterimReportTime()));
                Long organizationId = organization.getOrganizationId();
                List<OrganizationExecutive> organizationExecutiveByOrganizationId = organizationService.getOrganizationExecutiveByOrganizationId(organizationId);
                List<Map<String, List<Map<String, String>>>> dataList = new ArrayList<>();
                List<ExecutiveSummaryDto> executiveSummaryDtos = new ArrayList<>();
                List<VendorChecks> vendorList = vendorChecksRepository.findAllByCandidateCandidateId(candidate.getCandidateId());
//                vendorList = vendorList.stream()
//                        .sorted(Comparator.comparing(entity -> {
//                            Long sourceId = entity.getSource().getSourceId();
//                            // Customize the sorting order based on source ID matches
//                            List<Long> educationIds = Arrays.asList(24L, 25L, 26L, 27L, 45L);
//                            List<Long> employmentIds = Arrays.asList(28L, 29L, 44L, 46L, 50L);
//                            List<Long> addressIds = Arrays.asList(19L, 20L, 21L, 22L, 23L, 48L);
//                            List<Long> identityIds = Arrays.asList(11L, 12L, 13L, 14L);
//                            List<Long> ofacIds = Arrays.asList(39L);
//                            if (educationIds.contains(sourceId)) {
//                                return 1L; // education
//                            } else if (employmentIds.contains(sourceId)) {
//                                return 2L; // employment
//                            } else if (addressIds.contains(sourceId)) {
//                                return 3L; // address
//                            } else if (identityIds.contains(sourceId)) {
//                                return 4L; // identity
//                            } else if (ofacIds.contains(sourceId)) {
//                                return 5L; // ofac
//                            } else {
//                                return 6L; // criminal, global
//                            }
//                        }))
//                        .collect(Collectors.toList());
                VendorUploadChecksDto vendorUploadChecksDto = null;
                vendorList = vendorList.stream().filter(vca -> {
                    return vca.getVendorCheckStatusMaster() != null && vca.getVendorCheckStatusMaster().getVendorCheckStatusMasterId() != 3l && vca.getVendorCheckStatusMaster().getVendorCheckStatusMasterId() != 2l;
                }).collect(Collectors.toList());
                for (VendorChecks vendorChecks : vendorList) {
                    User user = userRepository.findByUserId(vendorChecks.getVendorId());
                    VendorUploadChecks vendorChecksss = vendorUploadChecksRepository.findByVendorChecksVendorcheckId(vendorChecks.getVendorcheckId());
                    if (vendorChecksss != null) {
                        // Set vendor attributes
                        ArrayList<VendorAttributeDto> vendorAttributeDtos = new ArrayList<>();
                        VendorAttributeDto vendorAttributeDto = new VendorAttributeDto();

                        Optional<ConventionalVendorliChecksToPerform> optionalCheck = liCheckToPerformRepository.findById(vendorChecksss.getVendorChecks().getLicheckId())
                                .filter(stopcheck -> !"TRUE".equalsIgnoreCase(stopcheck.getStopCheck()));

                        if (optionalCheck.isPresent()) {
                            ConventionalVendorliChecksToPerform conventionalVendorliChecksToPerform = optionalCheck.get();
                            if (conventionalVendorliChecksToPerform.getSource().getSourceName().equalsIgnoreCase("GLOBAL DATABASE CHECK")) {
                                ObjectMapper objectMapper = new ObjectMapper();
                                for (String jsonData : vendorChecksss.getVendorAttirbuteValue()) {
                                    Map<String, List<Map<String, String>>> dataMap = objectMapper.readValue(jsonData, new TypeReference<Map<String, List<Map<String, String>>>>() {
                                    });
                                    dataList.add(dataMap);
                                }
                                vendorAttributeDto.setSourceName(conventionalVendorliChecksToPerform.getCheckName());
                                vendorAttributeDto.setCheckStatus(conventionalVendorliChecksToPerform.getCheckStatus().getCheckStatusCode());
                                vendorAttributeDtos.add(vendorAttributeDto);
                            } else {
                                if (conventionalVendorliChecksToPerform.getSource().getSourceName().contains("EMPLOYMENT")) {
                                    int lastNumber = getLastNumber(conventionalVendorliChecksToPerform.getCheckName());
                                    vendorAttributeDto.setSourceName(conventionalVendorliChecksToPerform.getCheckName());
                                    vendorAttributeDto.setCheckStatus(conventionalVendorliChecksToPerform.getCheckStatus().getCheckStatusCode());
                                    vendorAttributeDto.setVendorAttirbuteValue(vendorChecksss.getVendorAttirbuteValue());
                                    vendorAttributeDto.setIndexNumber(lastNumber);
                                    vendorAttributeDtos.add(vendorAttributeDto);
                                } else {
                                    vendorAttributeDto.setSourceName(conventionalVendorliChecksToPerform.getCheckName());
                                    vendorAttributeDto.setCheckStatus(conventionalVendorliChecksToPerform.getCheckStatus().getCheckStatusCode());
                                    vendorAttributeDto.setVendorAttirbuteValue(vendorChecksss.getVendorAttirbuteValue());
                                    vendorAttributeDtos.add(vendorAttributeDto);
                                }
                            }
                            vendorUploadChecksDto = new VendorUploadChecksDto(user.getUserFirstName(), vendorChecksss.getVendorChecks().getVendorcheckId(), vendorChecksss.getVendorUploadedDocument(), vendorChecksss.getDocumentname(), vendorChecksss.getAgentColor().getColorName(), vendorChecksss.getAgentColor().getColorHexCode(), null);
                            vendorUploadChecksDto.setCheckUniqueId(String.valueOf(conventionalVendorliChecksToPerform.getCheckUniqueId()));
//                        if (vendorUploadChecksDto.getDocument() != null) {
//                            File tempFile = File.createTempFile(vendorUploadChecksDto.getDocumentname(), ".pdf");
//                            FileOutputStream fos = new FileOutputStream(tempFile);
//                            fos.write(vendorUploadChecksDto.getDocument());
//                            fos.close();
//                            log.info("temp file" + tempFile);
//                        }
                            vendorUploadChecksDto.setVendorAttirbuteValue(vendorAttributeDtos);
                            vendordocDtoList.add(vendorUploadChecksDto);
//
                        } else {
                            // Handle the case where the check is not found or stopCheck is "TRUE"
                            // For example, log a warning or throw an exception
                            log.warn("No valid check found for VendorCheck ID: " + vendorChecks.getVendorcheckId());
                        }
                    }
                }
                List<VendorUploadChecksDto> collect1 = vendordocDtoList.stream().filter(da -> da.getVendorAttirbuteValue() != null).collect(Collectors.toList());
                Collections.sort(collect1, Comparator.comparing(dto -> {
                    List<VendorAttributeDto> vendorAttributeValue = dto.getVendorAttirbuteValue();
                    // Sorting based on indexNumber of the first element in vendorAttributeValue
                    return vendorAttributeValue.isEmpty() ? Integer.MAX_VALUE : vendorAttributeValue.get(0).getIndexNumber();
                }));
                candidateReportDTO.setVendorProofDetails(collect1);
                candidateReportDTO.setDataList(dataList);
                List<ConventionalVendorliChecksToPerform> allLichecks = liCheckToPerformRepository.findByRequestId(conventionalVendorCandidatesSubmitted.getRequestId());

                List<ConventionalVendorliChecksToPerform> filteredCheckstatusCodeLicheck = byCandidateId.stream().filter(check -> {
                            String code = check.getCheckStatus().getCheckStatusCode();
                            return code.equals("CLEAR") ||
                                    code.equals("MINORDISCREPANCY") ||
                                    code.equals("QCPENDING") ||
                                    code.equals("UNABLETOVERIFY") ||
                                    code.equals("MAJORDISCREPANCY");
                        })
                        .collect(Collectors.toList());
                filteredCheckstatusCodeLicheck = filteredCheckstatusCodeLicheck.stream().filter(venfilter ->
                {
                    String vendorCheckStatusCoode = venfilter.getVendorChecks().getVendorCheckStatusMaster().getCheckStatusCode();
                    return vendorCheckStatusCoode.equals("CLEAR") ||
                            vendorCheckStatusCoode.equals("MINORDISCREPANCY") ||
                            vendorCheckStatusCoode.equals("QCPENDING") ||
                            vendorCheckStatusCoode.equals("UNABLETOVERIFY") ||
                            vendorCheckStatusCoode.equals("MAJORDISCREPANCY");
                }).collect(Collectors.toList());
                List<VendorUploadChecks> result = new ArrayList<>();

                List<Long> vendorCheckIds = filteredCheckstatusCodeLicheck
                        .stream().map(data -> data.getVendorChecks().getVendorcheckId())
                        .collect(Collectors.toList());
                vendorCheckIds.forEach(vendorcheckId -> {
                    VendorUploadChecks byVendorChecksVendorcheckId = vendorUploadChecksRepository.findByVendorChecksVendorcheckId(vendorcheckId);
                    result.add(byVendorChecksVendorcheckId);
                    System.out.println("vendorcheck id " + vendorcheckId);
                });

                Map<String, ConventionalCandidateReportDto> checkNameToDtoMap = new HashMap<>();
                List<File> tempFileToDelete = new ArrayList<>();
                List<Map<String, List<String>>> encodedImagesList = new ArrayList<>();
                List<InputStream> collect = new ArrayList<>();
                File report = FileUtil.createUniqueTempFile("report", ".pdf");
                tempFileToDelete.add(report);
                String conventionalHtmlStr = null;
                Date createdOn = candidate.getCreatedOn();
                String htmlStr = pdfService.parseThymeleafTemplateForConventionalCandidate("Conventional/BaseView", candidateReportDTO);
                checkNameToDtoMap.put("Conventional/BaseView", candidateReportDTO);
                pdfService.generatePdfFromHtml(htmlStr, report);
                List<Content> contentList = contentRepository.findAllByCandidateIdAndContentTypeIn(candidate.getCandidateId(), Arrays.asList(ContentType.ISSUED, ContentType.AGENT_UPLOADED));
                List<File> files = contentList.stream().map(content -> {
                    File uniqueTempFile = FileUtil.createUniqueTempFile(candidateId + "_issued_" + content.getContentId().toString(), ".pdf");
                    tempFileToDelete.add(uniqueTempFile);
                    awsUtils.getFileFromS3(content.getBucketName(), content.getPath(), uniqueTempFile);
                    return uniqueTempFile;
                }).collect(Collectors.toList());
                File mergedFile = FileUtil.createUniqueTempFile(String.valueOf(candidateId), ".pdf");
                tempFileToDelete.add(mergedFile);
                collect.add(FileUtil.convertToInputStream(report));
                collect.addAll(files.stream().map(FileUtil::convertToInputStream).collect(Collectors.toList()));

                for (VendorUploadChecks vendorUploadCheck : result) {
                    boolean isbase64 = false;
                    Map<String, List<String>> encodedImageMap = new HashMap<>();
                    Long checkId = vendorUploadCheck.getVendorChecks().getVendorcheckId();
                    String sourceName = vendorUploadCheck.getVendorChecks().getSource().getSourceName();
//                    if (checkName != null && !checkName.isEmpty()) {
                    Optional<ConventionalVendorliChecksToPerform> byId = liCheckToPerformRepository.findById(vendorUploadCheck.getVendorChecks().getLicheckId());
                    String nameOfCheck = byId.get().getCheckName();
                    byte[] documentBytes = vendorUploadCheck.getVendorUploadedDocument();
                    String jsonString = null;
                    ObjectMapper objectMapper = new ObjectMapper();
                    String vendorUploadedImages = vendorUploadCheck.getVendorUploadedImage();
                    String documentPresicedUrl = null;
                    if (vendorUploadCheck.getVendorUploadDocumentPathKey() != null || vendorUploadCheck.getVendorUploadDocumentPathKey() != null) {
                        log.info("inside the aws path key retival for the check    --" + sourceName);

                        try {
                            if (vendorUploadCheck.getVendorUploadDocumentPathKey() != null) {
                                documentBytes = awsUtils.getbyteArrayFromS3(DIGIVERIFIER_DOC_BUCKET_NAME, vendorUploadCheck.getVendorUploadDocumentPathKey());
                            }

                            if (vendorUploadCheck.getVendorUploadImagePathKey() != null) {
                                byte[] imageBytes = awsUtils.getbyteArrayFromS3(DIGIVERIFIER_DOC_BUCKET_NAME, vendorUploadCheck.getVendorUploadImagePathKey());
                                vendorUploadedImages = new String(imageBytes);
                            }
                            // Check if the document is not PDF
                            if (!isPDF(documentBytes)) {
                                String base64EncodedDocument = Base64.getEncoder().encodeToString(documentBytes);
                                documentPresicedUrl = base64EncodedDocument;
                                isbase64 = true;
                            } else {
//                                     Use the original document
                                documentPresicedUrl = vendorUploadCheck.getVendorUploadDocumentPathKey();
                                log.info("Pdf Path url  " + documentPresicedUrl);
                                isbase64 = false;
                            }
                            vendorUploadedImages = new String(documentPresicedUrl);
                            // Convert list to JSONArray
                            JSONArray jsonArray = new JSONArray();
//							for (String base64 : imageBase64List) {
                            JSONObject jsonObject = new JSONObject();
                            JSONArray imageArray = new JSONArray();
                            imageArray.put(vendorUploadedImages);
                            jsonObject.put("image", imageArray);
                            jsonArray.put(jsonObject);
//							}
                            // Convert the JSON array to a string
                            jsonString = jsonArray.toString();

                        } catch (IOException e) {
                            log.info("Exception in DIGIVERIFIER_DOC_BUCKET_NAME {}" + e);
                        }
                    } else {

                        String base64EncodedDocument = vendorUploadedImages;
                        documentPresicedUrl = base64EncodedDocument;
                        isbase64 = true;
                        jsonString = documentPresicedUrl;


                    }


                    try {
                        if (vendorUploadedImages != null) {
                            List<Map<String, List<String>>> decodedImageList = objectMapper.readValue(jsonString, new TypeReference
                                    <List<Map<String, List<String>>>>() {
                            });

                            List<String> allEncodedImages = decodedImageList.stream()
                                    .flatMap(imageMap -> imageMap.values().stream())
                                    .flatMap(List::stream)
                                    .collect(Collectors.toList());

                            // Loop through each image byte array and encode it to Base64
                            List<String> encodedImagesForDocument = new ArrayList<>();
                            encodedImageMap.put(nameOfCheck, allEncodedImages);
                        } else {
                            log.info("Vendor uploaded document is null {}");
                            encodedImageMap.put(nameOfCheck, null);

                        }
                        encodedImagesList.add(encodedImageMap);
                        try {
                            ConventionalCandidateReportDto testConventionalCandidateReportDto = null;
                            if (vendorUploadedImages != null) {
                                if (isbase64 == true) {
                                    log.info("BASE64 IMG for " + nameOfCheck + " entry");
                                    List<Map<String, List<String>>> dynamicEncodedImagesList = new ArrayList<>();
                                    // Generate table for this education entry
                                    File allcheckDynamicReport = FileUtil.createUniqueTempFile(byId.get().getCheckName(), ".pdf");
                                    tempFileToDelete.add(allcheckDynamicReport);
                                    String templateName;
                                    if (nameOfCheck.contains("EDUCATION")) {
                                        templateName = "Conventional/EducationCheck";
                                    } else if (nameOfCheck.contains("EMPLOYMENT")) {
                                        templateName = "Conventional/EmploymentCheck";
                                    } else if (nameOfCheck.contains("CRIMINAL")) {
                                        templateName = "Conventional/CriminalCheck";
                                    } else if (nameOfCheck.contains("GLOBAL")) {
                                        templateName = "Conventional/GlobalCheck";
                                    } else if (nameOfCheck.contains("IDENTITY")) {
                                        templateName = "Conventional/IdentityCheck";
                                    } else if (nameOfCheck.contains("LEGAL")) {
                                        templateName = "Conventional/LegalRightCheck";
                                    } else if (nameOfCheck.contains("OFAC")) {
                                        templateName = "Conventional/OfacCheck";
                                    } else if (nameOfCheck.contains("ADDRESS")) {
                                        templateName = "Conventional/AddressCheck";
                                    } else if (nameOfCheck.contains("REFERENCE")) {
                                        templateName = "Conventional/ReferenceCheck";
                                    } else if (nameOfCheck.contains("CV VALIDATION")) {
                                        templateName = "Conventional/CvValidation";
                                    } else if (nameOfCheck.contains("DRUG CHECK")) {
                                        templateName = "Conventional/DrugCheck";
                                    } else if (nameOfCheck.contains("CREDIT BUREAU CHECK")) {
                                        templateName = "Conventional/CreditBreauCheck";
                                    } else if (nameOfCheck.contains("Professional Certificate Check")) {
                                        templateName = "Conventional/Professionalcertificatecheck";
                                    } else if (nameOfCheck.contains("CMotor Vehicle Records(MVR)")) {
                                        templateName = "Conventional/MotorVehicleRecords(MVR)";
                                    } else if (nameOfCheck.contains("FACIS level 3 Check")) {
                                        templateName = "Conventional/FACISlevel3Check";
                                    } else {
                                        templateName = "Conventional/NoTableCheck";
                                    }
                                    testConventionalCandidateReportDto = new ConventionalCandidateReportDto();
                                    List<VendorUploadChecksDto> filteredVendorProofs = candidateReportDTO.getVendorProofDetails().stream().filter(p -> p.getCheckUniqueId().equalsIgnoreCase(String.valueOf(byId.get().getCheckUniqueId())) == true).collect(Collectors.toList());
                                    testConventionalCandidateReportDto.setVendorProofDetails(filteredVendorProofs);
                                    Map<String, LegalProceedingsDTO> filteredCriminalCheckList = candidateReportDTO.getCriminalCheckList()
                                            .entrySet()
                                            .stream()
                                            .filter(entry -> nameOfCheck.equals(entry.getKey()))
                                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                                    testConventionalCandidateReportDto.setCriminalCheckList(filteredCriminalCheckList);
                                    testConventionalCandidateReportDto.setDataList(candidateReportDTO.getDataList());
                                    dynamicEncodedImagesList.add(encodedImageMap);
                                    testConventionalCandidateReportDto.setPdfByes(dynamicEncodedImagesList);
                                    String tableHtmlStr = pdfService.parseThymeleafTemplateForConventionalCandidate(templateName, testConventionalCandidateReportDto);
                                    checkNameToDtoMap.put(templateName, testConventionalCandidateReportDto);
                                    pdfService.generatePdfFromHtml(tableHtmlStr, allcheckDynamicReport);
                                    // Collect education proof and table
                                    List<InputStream> educationProof = new ArrayList<>();
                                    educationProof.add(FileUtil.convertToInputStream(allcheckDynamicReport));
                                    collect.addAll(educationProof);
                                } else {
                                    log.info("Fetching the PDF Proof for :" + nameOfCheck);
                                    // Fetch the PDF file from S3
                                    File fileFromS3 = awsUtils.getFileFromS3(DIGIVERIFIER_DOC_BUCKET_NAME, documentPresicedUrl);
                                    tempFileToDelete.add(fileFromS3);
                                    // Generate table for this education entry
                                    File allcheckDynamicReport = FileUtil.createUniqueTempFile(byId.get().getCheckName(), ".pdf");
                                    tempFileToDelete.add(allcheckDynamicReport);
                                    String templateName;
                                    if (nameOfCheck.contains("EDUCATION")) {
                                        templateName = "Conventional/EducationCheck";
                                    } else if (nameOfCheck.contains("EMPLOYMENT")) {
                                        templateName = "Conventional/EmploymentCheck";
                                    } else if (nameOfCheck.contains("CRIMINAL")) {
                                        templateName = "Conventional/CriminalCheck";
                                    } else if (nameOfCheck.contains("GLOBAL")) {
                                        templateName = "Conventional/GlobalCheck";
                                    } else if (nameOfCheck.contains("IDENTITY")) {
                                        templateName = "Conventional/IdentityCheck";
                                    } else if (nameOfCheck.contains("LEGAL")) {
                                        templateName = "Conventional/LegalRightCheck";
                                    } else if (nameOfCheck.contains("OFAC")) {
                                        templateName = "Conventional/OfacCheck";
                                    } else if (nameOfCheck.contains("ADDRESS")) {
                                        templateName = "Conventional/AddressCheck";
                                    } else if (nameOfCheck.contains("REFERENCE")) {
                                        templateName = "Conventional/ReferenceCheck";
                                    } else if (nameOfCheck.contains("CV VALIDATION")) {
                                        templateName = "Conventional/CvValidation";
                                    } else if (nameOfCheck.contains("DRUG CHECK")) {
                                        templateName = "Conventional/DrugCheck";
                                    } else if (nameOfCheck.contains("CREDIT BUREAU CHECK")) {
                                        templateName = "Conventional/CreditBreauCheck";
                                    } else if (nameOfCheck.contains("Professional Certificate Check")) {
                                        templateName = "Conventional/Professionalcertificatecheck";
                                    } else if (nameOfCheck.contains("Motor Vehicle Records(MVR)")) {
                                        templateName = "Conventional/MotorVehicleRecords(MVR)";
                                    } else if (nameOfCheck.contains("FACIS level 3 Check")) {
                                        templateName = "Conventional/FACISlevel3Check";
                                    } else {
                                        templateName = "Conventional/NoTableChecks";
                                    }

                                    testConventionalCandidateReportDto = new ConventionalCandidateReportDto();
                                    List<VendorUploadChecksDto> filteredVendorProofs = candidateReportDTO.getVendorProofDetails().stream().filter(p -> p.getCheckUniqueId().equalsIgnoreCase(String.valueOf(byId.get().getCheckUniqueId())) == true).collect(Collectors.toList());
                                    testConventionalCandidateReportDto.setVendorProofDetails(filteredVendorProofs);
                                    Map<String, LegalProceedingsDTO> filteredCriminalCheckList = candidateReportDTO.getCriminalCheckList()
                                            .entrySet()
                                            .stream()
                                            .filter(entry -> nameOfCheck.equals(entry.getKey()))
                                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                                    testConventionalCandidateReportDto.setCriminalCheckList(filteredCriminalCheckList);
                                    testConventionalCandidateReportDto.setDataList(candidateReportDTO.getDataList());
                                    String tableHtmlStr = pdfService.parseThymeleafTemplateForConventionalCandidate(templateName, testConventionalCandidateReportDto);
                                    checkNameToDtoMap.put(templateName, testConventionalCandidateReportDto);
                                    pdfService.generatePdfFromHtml(tableHtmlStr, allcheckDynamicReport);
                                    // Collect education proof and table
                                    List<InputStream> educationProof = new ArrayList<>();
                                    educationProof.add(FileUtil.convertToInputStream(allcheckDynamicReport));
                                    educationProof.add(new FileInputStream(fileFromS3));
                                    collect.addAll(educationProof);
                                }
                            }
                        } catch (IOException e) {
                            log.error("Exception occurred: {}", e);
                        }
                    } catch (JsonProcessingException e) {
                        // Handle the exception (e.g., log or throw)
                        e.printStackTrace();
                    }
                }
                candidateReportDTO.setPdfByes(encodedImagesList);
                if (agentUploadedList.isEmpty() == false) {
                    List<InputStream> educationProof = new ArrayList<>();
                    agentUploadedList.forEach(agentchecks -> {
                        File allcheckDynamicReport = FileUtil.createUniqueTempFile("UanCheck", ".pdf");
                        String templateName = "Conventional/UANCheck";
                        ConventionalCandidateReportDto conventionalCandidateReportDto = new ConventionalCandidateReportDto();
                        conventionalCandidateReportDto.setAgentUploadedChecks(agentchecks);
                        String tableHtmlStr = pdfService.parseThymeleafTemplateForConventionalCandidate(templateName, conventionalCandidateReportDto);
                        checkNameToDtoMap.put(templateName, conventionalCandidateReportDto);
                        pdfService.generatePdfFromHtml(tableHtmlStr, allcheckDynamicReport);
                        educationProof.add(FileUtil.convertToInputStream(allcheckDynamicReport));
                        try {
                            File fileFromS3 = awsUtils.getFileFromS3(DIGIVERIFIER_DOC_BUCKET_NAME, agentchecks.getPathKey());
                            tempFileToDelete.add(fileFromS3);
                            educationProof.add(new FileInputStream(fileFromS3));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        tempFileToDelete.add(allcheckDynamicReport);
                        collect.addAll(educationProof);
                    });
                }
                List<Content> uploadedDocContentList = new ArrayList<>();
                List<File> uploadedDocuments = uploadedDocContentList.stream().map(content -> {
                    File uniqueTempFile = FileUtil.createUniqueTempFile(
                            candidateId
                                    + "_issued_" + content.getContentId().toString(), ".pdf");
                    awsUtils.getFileFromS3(content.getBucketName(), content.getPath(), uniqueTempFile);
                    return uniqueTempFile;
                }).collect(Collectors.toList());
                tempFileToDelete.addAll(uploadedDocuments);
                collect.addAll(uploadedDocuments.stream().map(FileUtil::convertToInputStream).collect(Collectors.toList()));


                ClassPathResource resource = new ClassPathResource("disclaimer.pdf");

                try (InputStream inputStream = resource.getInputStream()) {
                    collect.add(resource.getInputStream());
                    PdfUtil.mergePdfFiles(collect, new FileOutputStream(mergedFile.getPath()));
                    log.info("after merge pdf files");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    log.error("Exception 3 occured in generateDocument method in ReportServiceImpl-->", e);
                }
                tempFileToDelete.add(mergedFile);
                String jsonPath = "Digiverifier_Analytics_LTIM_Conventional/Candidate/".concat(candidateId + "/Generated".concat("/").concat(conventionalVendorCandidatesSubmitted.getRequestId() + "_" + conventionalVendorCandidatesSubmitted.getName() + "_" + reportType.name()).concat(".pdf"));
                String jsonPrecisedUrl = awsUtils.uploadDtosAsJsonToS3AndReturnPresignedUrl(checkNameToDtoMap, jsonPath, DIGIVERIFIER_DOC_BUCKET_NAME);
                String tempPath = "TEMP/Candidate/".concat(candidateId + "/Generated".concat("/").concat(conventionalVendorCandidatesSubmitted.getRequestId() + "_" + conventionalVendorCandidatesSubmitted.getName() + "_" + reportType.name()).concat(".pdf"));
                String pdfUrl = awsUtils.uploadFileAndGetPresignedUrlTemp(DIGIVERIFIER_DOC_BUCKET_NAME, tempPath, mergedFile);

                System.out.println(pdfUrl);
                Content content = new Content();
                content.setCandidateId(candidate.getCandidateId());
                content.setContentCategory(ContentCategory.OTHERS);
                if (reportType.name().equalsIgnoreCase("INTERIM")) {
                    content.setContentSubCategory(ContentSubCategory.INTERIM);
                } else if (reportType.name().equalsIgnoreCase("FINAL")) {
                    content.setContentSubCategory(ContentSubCategory.FINAL);
                }
                content.setFileType(FileType.PDF);
                content.setContentType(ContentType.GENERATED);
                content.setBucketName(DIGIVERIFIER_DOC_BUCKET_NAME);
                content.setCreatedOn(new Date());
                content.setPath(jsonPath);
                contentRepository.save(content);
                stringServiceOutcome.setData(pdfUrl); // Set Base64 string as the data
                stringServiceOutcome.setStatus(tempPath);
                //delete temporary files
                if (tempFileToDelete != null && tempFileToDelete.isEmpty() == false) {
                    for (File file : tempFileToDelete) {
                        if (file.exists() && file.isFile()) {
                            if (file.delete()) {
                                log.info("Deleted dynamic report file: " + file.getPath());
                            } else {
                                log.warn("Failed to delete dynamic report file: " + file.getPath());
                            }
                        }
                    }
                }

            } else {
                throw new RuntimeException("unable to generate document for this candidate");
            }
        } catch (Exception e) {
            log.error("exception in generateConventionalCandidateReport() ---" + e.getMessage());
        }
        return stringServiceOutcome;
    }


    public boolean isBase64Encoded(String value) {
        // Regular expression to match Base64 encoded strings
        String base64Pattern = "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$";
        return value != null && value.matches(base64Pattern);
    }

    public List<byte[]> convertPDFToImage(byte[] pdfBytes) throws IOException {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int numberOfPages = document.getNumberOfPages();

            List<byte[]> imageBytesList = new ArrayList<>();

            for (int pageIndex = 0; pageIndex < numberOfPages; pageIndex++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, 300);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                javax.imageio.ImageIO.write(image, "jpeg", baos);
                imageBytesList.add(baos.toByteArray());
            }

//	        log.info("Number of Images: {}" + imageBytesList.size());


            // If needed, you can return the list of image bytes
            return imageBytesList;
        }
    }


    public static int getLastNumber(String checkName) {
        // Regular expression to match the last number in the string
        Pattern pattern = Pattern.compile("\\d+$");
        Matcher matcher = pattern.matcher(checkName);

        // Find the last number
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        } else {
            // Return some default value if no number found
            return -1;
        }
    }

    public ServiceOutcome<String> generateDiscrepancyreport(Long candidateId, ReportType reportType, String updated, Long uniqueCheckId) {
        ServiceOutcome<String> stringServiceOutcome = new ServiceOutcome<>();
        ArrayList<LicheckRequiredResponseDto> licheckRequiredResponseDtos = new ArrayList<>();
        Map<String, ConventionalCandidateReportDto> checkNameToDtoMap = new HashMap<>();
        try {
            Candidate candidate = candidateRepository.findById(candidateId).get();
            ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(String.valueOf(candidate.getConventionalRequestId()));
            ConventionalCandidate byConventionalRequestId = conventionalCandidateRepository.findByConventionalRequestId(Long.valueOf(conventionalVendorCandidatesSubmitted.getRequestId()));

            if (candidate != null) {
                List<VendorUploadChecksDto> vendordocDtoList = new ArrayList<VendorUploadChecksDto>();
                ConventionalCandidateReportDto candidateReportDTO = new ConventionalCandidateReportDto();
                candidateReportDTO.setCandidateId(String.valueOf(conventionalVendorCandidatesSubmitted.getCandidateId()));
                candidateReportDTO.setApplicantId(String.valueOf(conventionalVendorCandidatesSubmitted.getApplicantId()));
                candidateReportDTO.setOrganizationName(candidate.getOrganization().getOrganizationName());
                candidateReportDTO.setOrganizationLogo(Arrays.toString(candidate.getOrganization().getOrganizationLogo()));
                candidateReportDTO.setRequestId(conventionalVendorCandidatesSubmitted.getRequestId());
                if (byConventionalRequestId != null) {
                    candidateReportDTO.setAddress(byConventionalRequestId.getBirthPlace());
                }
                if (conventionalVendorCandidatesSubmitted.getVerificationStatus() != null) {
                    //                      if (conventionalVendorCandidatesSubmitted.getVerificationStatus().equalsIgnoreCase("UNABLETOVERIFIY")) {
                    //                          candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.UNABLETOVERIFIY);
                    //                          candidateReportDTO.setColorCode("AMBER");
                    //                      }
                    if (conventionalVendorCandidatesSubmitted.getVerificationStatus().equalsIgnoreCase("MAJORDISCREPANCY")) {
                        candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.MAJORDISCREPANCY);
                        candidateReportDTO.setColorCode("RED");
                    }
                    if (conventionalVendorCandidatesSubmitted.getVerificationStatus().equalsIgnoreCase("MINORDISCREPANCY")) {
                        candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.MINORDISCREPANCY);
                        candidateReportDTO.setColorCode("RED");
                    }
                    //                      if (conventionalVendorCandidatesSubmitted.getVerificationStatus().equalsIgnoreCase("INSUFFICIENCY")) {
                    //                          candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.INSUFFICIENCY);
                    //                          candidateReportDTO.setColorCode("YELLOW");
                    //                      }
                    //                      if (conventionalVendorCandidatesSubmitted.getVerificationStatus().equalsIgnoreCase("INPROGRESS")) {
                    //                          candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.INPROGRESS);
                    //                          candidateReportDTO.setColorCode("");
                    //                      }
                    //                      if (conventionalVendorCandidatesSubmitted.getVerificationStatus().equalsIgnoreCase("CLEAR")) {
                    //                          candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.CLEAR);
                    //                          candidateReportDTO.setColorCode("GREEN");
                    //                      }
                } else {
                    candidateReportDTO.setVerificationStatus(ConventionalVerificationStatus.NAN);
                }

                if (byConventionalRequestId != null) {
                    candidateReportDTO.setAddress(byConventionalRequestId.getBirthPlace());
                }
                candidateReportDTO.setName(candidate.getCandidateName());
                candidateReportDTO.setReferenceId(candidate.getApplicantId());
                candidateReportDTO.setDob(candidate.getDateOfBirth());
                candidateReportDTO.setContactNo(candidate.getContactNumber());
                candidateReportDTO.setEmailId(candidate.getEmailId());
                candidateReportDTO.setReportType(reportType);
                Organization organization = candidate.getOrganization();
                candidateReportDTO.setProject(organization.getOrganizationName());
                candidateReportDTO.setOrganizationLocation(organization.getOrganizationLocation());
                candidateReportDTO.setOrganizationLogo(organization.getLogoUrl());
                candidateReportDTO.setComments("");
                //                  log.info("request id" + String.valueOf(candidate.getConventionalRequestId()));
                //                  List<ConventionalVendorliChecksToPerform> byCandidateId = liCheckToPerformRepository.findByRequestId(String.valueOf(candidate.getConventionalRequestId()));
                ConventionalVendorliChecksToPerform vendorCheckId = liCheckToPerformRepository.findByVendorChecksVendorcheckId(uniqueCheckId);
                VendorChecks byVendorcheckId = vendorChecksRepository.findByVendorcheckId(uniqueCheckId);
                //                  byCandidateId = byCandidateId.stream()
                //                          .sorted(Comparator.comparing(entity -> {
                //                              String checkName = entity.getCheckName();
                //                              if (checkName.startsWith("E")) {
                //                                  return "0" + checkName; // Prefix 'E' with '0' to make it come first
                //                              } else if (checkName.startsWith("A")) {
                //                                  return "1" + checkName; // Prefix 'A' with '1' to make it come after 'E'
                //                              }
                //                              else if (checkName.startsWith("I")) {
                //                                  return "2" + checkName; // Prefix 'A' with '1' to make it come after 'E'
                //                              }
                //                              else if (checkName.startsWith("C")) {
                //                                  return "3" + checkName; // Prefix 'A' with '1' to make it come after 'E'
                //                              }
                //
                //                              else if (checkName.startsWith("G")) {
                //                                  return "4" + checkName; // Prefix 'A' with '1' to make it come after 'E'
                //                              }
                //                              else {
                //                                  return "5" + checkName; // Other check names
                //                              }
                //                          }))
                //                          .collect(Collectors.toList());
                //                  byCandidateId = byCandidateId.stream().filter(en -> en.getSource() != null)
                //                          .sorted(Comparator.comparing(entity -> {
                //
                //                              Long sourceId = entity.getSource().getSourceId();
                //
                //                              // Customize the sorting order based on source ID matches
                //                              List<Long> educationIds = Arrays.asList(24L, 25L, 26L, 27L, 45L);
                //                              List<Long> employmentIds = Arrays.asList(28L, 29L, 44L, 46L, 50L);
                //                              List<Long> addressIds = Arrays.asList(19L, 20L, 21L, 22L, 23L, 48L);
                //                              List<Long> identityIds = Arrays.asList(11L, 12L, 13L, 14L);
                //                              List<Long> ofacIds = Arrays.asList(39L);
                //                              List<Long> criminalIds = Arrays.asList(15l, 16l, 17l, 18l, 49l, 51l);
                //                              List<Long> globalIds = Arrays.asList(38L);
                //                              if (educationIds.contains(sourceId)) {
                //                                  return 1L; // education
                //                              } else if (employmentIds.contains(sourceId)) {
                //                                  return 2L; // employment
                //                              } else if (addressIds.contains(sourceId)) {
                //                                  return 3L; // address
                //                              } else if (identityIds.contains(sourceId)) {
                //                                  return 4L; // identity
                //                              } else if (ofacIds.contains(sourceId)) {
                //                                  return 5L; // ofac
                //                              } else if (criminalIds.contains(sourceId)) {
                //                                  return 6L; // criminal
                //                              } else if (globalIds.contains(sourceId)) {
                //                                  return 7L; // global
                //                              } else {
                //                                  return 8L; // other than above
                //                              }
                //                          }))
                //                          .collect(Collectors.toList());


                //                  log.warn("check data outside report" + byCandidateId);
                HashMap<String, LegalProceedingsDTO> criminalCheckListMap = new HashMap<>();
                if (vendorCheckId != null) {
                    //                      List<ConventionalVendorliChecksToPerform> collect1 = byCandidateId.stream().filter(licheck -> licheck.getCheckStatus().getVendorCheckStatusMasterId() != 7l && licheck.getCheckStatus().getVendorCheckStatusMasterId() != 2l).collect(Collectors.toList());
                    //                      collect1.forEach(data -> {
                    //                          if (data.getCheckName().contains("EMPLOYMENT")) {
                    //                              LicheckRequiredResponseDto licheckRequiredResponseDto = new LicheckRequiredResponseDto();
                    //                              licheckRequiredResponseDto.setCheckName(data.getCheckName());
                    //                              licheckRequiredResponseDto.setCheckUniqueId(data.getCheckUniqueId());
                    //                              licheckRequiredResponseDto.setCheckStatus(data.getCheckStatus().getCheckStatusCode());
                    //                              licheckRequiredResponseDto.setCheckRemarks(data.getCheckRemarks());
                    //                              int lastNumber = getLastNumber(data.getCheckName());
                    //                              licheckRequiredResponseDto.setIndexNumber(lastNumber);
                    //                              licheckRequiredResponseDto.setDisableStatus("");
                    //                              licheckRequiredResponseDtos.add(licheckRequiredResponseDto);
                    //                      	}
                    //                      	else {
                    //                              LicheckRequiredResponseDto licheckRequiredResponseDto = new LicheckRequiredResponseDto();
                    //                              licheckRequiredResponseDto.setCheckName(data.getCheckName());
                    //                              licheckRequiredResponseDto.setCheckUniqueId(data.getCheckUniqueId());
                    //                              licheckRequiredResponseDto.setCheckStatus(data.getCheckStatus().getCheckStatusCode());
                    //                              licheckRequiredResponseDto.setCheckRemarks(data.getCheckRemarks());
                    //                              licheckRequiredResponseDto.setDisableStatus("");
                    //                              licheckRequiredResponseDtos.add(licheckRequiredResponseDto);
                    //                          }
                    //                      });

                    LicheckRequiredResponseDto licheckRequiredResponseDto = new LicheckRequiredResponseDto();
                    licheckRequiredResponseDto.setCheckName(vendorCheckId.getCheckName());
                    licheckRequiredResponseDto.setCheckUniqueId(vendorCheckId.getCheckUniqueId());
                    licheckRequiredResponseDto.setCheckStatus(byVendorcheckId.getVendorCheckStatusMaster().getCheckStatusCode());
                    licheckRequiredResponseDto.setCheckRemarks(vendorCheckId.getCheckRemarks());
                    int lastNumber = getLastNumber(vendorCheckId.getCheckName());
                    licheckRequiredResponseDto.setIndexNumber(lastNumber);
                    licheckRequiredResponseDto.setDisableStatus("");
                    licheckRequiredResponseDtos.add(licheckRequiredResponseDto);


                    Comparator<LicheckRequiredResponseDto> indexComparator = (dto1, dto2) -> {
                        // First, handle special cases where one of the indices is 1
                        if (dto1.getIndexNumber() == 1 && dto2.getIndexNumber() != 1) {
                            return -1; // dto1 comes first
                        } else if (dto1.getIndexNumber() != 1 && dto2.getIndexNumber() == 1) {
                            return 1; // dto2 comes first
                        }

                        // Then, ensure that indexNumber 0 comes last
                        if (dto1.getIndexNumber() == 0 && dto2.getIndexNumber() != 0) {
                            return 1; // dto2 comes first
                        } else if (dto1.getIndexNumber() != 0 && dto2.getIndexNumber() == 0) {
                            return -1; // dto1 comes first
                        }

                        // Finally, compare index numbers as usual
                        return Integer.compare(dto1.getIndexNumber(), dto2.getIndexNumber());
                    };

                    // Sort the list
                    Collections.sort(licheckRequiredResponseDtos, indexComparator);

                    licheckRequiredResponseDtos.forEach(data -> {
                        String modifiedCheckName = data.getCheckName().toLowerCase().replaceAll("[\\s-]", "");
                        if (modifiedCheckName.contains("education") && !modifiedCheckName.contains("educationhighest")) {
                            List<ConventionalCandidateCafEducation> conventionalCandidateCafEducations = conventionalCafCandidateEducationRepository.findByConventionalRequestId(Long.valueOf(conventionalVendorCandidatesSubmitted.getRequestId()));
                            if (conventionalCandidateCafEducations.isEmpty() == false) {
                                conventionalCandidateCafEducations.forEach(convdata -> {
                                    String degreetype = convdata.getDegreeType().toLowerCase().replaceAll("[\\s-]", "").replaceAll("degree", "");
                                    //                                      log.info("Check NAme" + modifiedCheckName + "===" + "    Education -   " + degreetype);
                                    boolean contains = modifiedCheckName.contains(degreetype);
                                    boolean diplomacontains = modifiedCheckName.contains("dipl");
                                    if (contains == true || diplomacontains == true) {
                                        CandidateCafEducation candidateCafEducation = candidateCafEducationRepository.findById(convdata.getCandidateCafEducationId()).get();
                                        data.setDisableStatus(candidateCafEducation.getQualificationType());
                                    }
                                    //                                      log.info("End Education Type setted" + data.getDisableStatus());
                                });
                            }
                        }
                        if (modifiedCheckName.contains("educationhighest")) {
                            List<ConventionalCandidateCafEducation> conventionalCandidateCafEducations = conventionalCafCandidateEducationRepository.findByConventionalRequestId(Long.valueOf(conventionalVendorCandidatesSubmitted.getRequestId()));
                            if (conventionalCandidateCafEducations.isEmpty() == false) {
                                conventionalCandidateCafEducations.forEach(convdata -> {
                                    String degreetype = convdata.getDegreeType().toLowerCase().replaceAll("[\\s-]", "");
                                    //log.info("Check NAme" + modifiedCheckName + "===" + "    Education -   " + degreetype);
//    								CandidateCafEducation candidateCafEducation = candidateCafEducationRepository.findById(convdata.getCandidateCafEducationId()).get();
                                    Optional<CandidateCafEducation> optionalCandidateCafEducation = candidateCafEducationRepository.findById(convdata.getCandidateCafEducationId());
                                    if (optionalCandidateCafEducation.isPresent()) {
                                        CandidateCafEducation candidateCafEducation = optionalCandidateCafEducation.get();

                                        if (degreetype.toLowerCase().contains("undergraduate")) {

                                            data.setDisableStatus(candidateCafEducation.getQualificationType());
                                        } else if (degreetype.toLowerCase().contains("postgraduate")) {
                                            // Handle postgraduate case
                                            data.setDisableStatus(candidateCafEducation.getQualificationType());

                                        } else if (degreetype.toLowerCase().contains("diploma")) {
                                            // Handle diploma case

                                            data.setDisableStatus(candidateCafEducation.getQualificationType());
                                        } else if (degreetype.toLowerCase().contains("highschool")) {
                                            // Handle high school case

                                            data.setDisableStatus(candidateCafEducation.getQualificationType());
                                        } else if (degreetype.toLowerCase().contains("highest")) {
                                            // Handle highest education case
                                            data.setDisableStatus(candidateCafEducation.getQualificationType());
                                        } else {

                                            data.setDisableStatus(candidateCafEducation.getQualificationType());
                                        }
                                    }
                                });

                            }
                        }
                        if (modifiedCheckName.contains("address")) {
                            List<ConventionalCafAddress> conventionalCafAddresses = conventionCafAddressRepository.findByConventionalRequestId(Long.valueOf(conventionalVendorCandidatesSubmitted.getRequestId()));
                            if (conventionalCafAddresses.isEmpty() == false) {
                                conventionalCafAddresses.forEach(convdata -> {
                                    String addressType = convdata.getAddressType().toLowerCase().replaceAll("[\\s-]", "");
                                    //                                      log.info("Check NAme" + modifiedCheckName + "===" + "Address  type-   " + addressType);
                                    if (modifiedCheckName.contains("present")) {
                                        boolean contains = modifiedCheckName.replaceAll("present", "current").contains(addressType);
                                        if (contains == true) {
                                            CandidateCafAddress candidateCafAddress = candidateCafAddressRepository.findById(convdata.getCandidateCafAddressId()).get();
                                            data.setDisableStatus(candidateCafAddress.getCity() + "," + candidateCafAddress.getState());
                                            //                                              log.info("End Address Type setted Present or current" + data.getDisableStatus());
                                        }

                                    } else if (modifiedCheckName.contains("permenent")) {
                                        boolean contains = modifiedCheckName.replaceAll("permenent", "permanent").contains(addressType);
                                        if (contains == true) {
                                            CandidateCafAddress candidateCafAddress = candidateCafAddressRepository.findById(convdata.getCandidateCafAddressId()).get();
                                            data.setDisableStatus(candidateCafAddress.getCity() + "," + candidateCafAddress.getState());
                                            //                                              log.info("End Address Type setted Present or current" + data.getDisableStatus());
                                        }

                                    } else {
                                        boolean contains = modifiedCheckName.contains(addressType);
                                        if (contains == true) {
                                            CandidateCafAddress candidateCafAddress = candidateCafAddressRepository.findById(convdata.getCandidateCafAddressId()).get();
                                            data.setDisableStatus(candidateCafAddress.getCity() + "," + candidateCafAddress.getState());
                                            //                                              log.info("End Address Type setted permenant" + data.getDisableStatus());
                                        }
                                    }

                                });
                            }
                        }
                        if (modifiedCheckName.contains("employment")) {
                            List<ConventionalCandidateExperience> conventionalexperienceS = conventionalCandidateExperienceRepository.findByConventionalRequestId(Long.valueOf(conventionalVendorCandidatesSubmitted.getRequestId()));
                            if (conventionalexperienceS.isEmpty() == false) {
                                conventionalexperienceS.forEach(convdata -> {
                                    String employmentType = convdata.getEmploymentType().toLowerCase().replaceAll("[\\s-]", "");
                                    //                                      log.info("Check NAme" + modifiedCheckName + "===" + "Employment -   " + employmentType);
                                    boolean contains = modifiedCheckName.contains(employmentType);
                                    if (contains == true) {
                                        CandidateCafExperience candidateCafExperience = candidateCafExperienceRepository.findById(convdata.getCandidateCafExperience()).get();
                                        data.setDisableStatus(candidateCafExperience.getCandidateEmployerName());
                                        //                                          log.info(modifiedCheckName + "  true condition       -candidate employer name           -" + candidateCafExperience.getCandidateEmployerName());
                                    }
                                    //                                      log.info("End Employment Type setted" + data.getDisableStatus());
                                });


                            }
                        }
                        if (modifiedCheckName.contains("pan")) {
                            data.setDisableStatus(candidate.getPanNumber());
                        }
                        if (modifiedCheckName.contains("passport")) {
                            data.setDisableStatus(candidate.getPassportNumber());
                        }
                        if (modifiedCheckName.contains("aadhar")) {
                            data.setDisableStatus(candidate.getAadharNumber());
                        }
                        if (modifiedCheckName.contains("driving")) {
                            data.setDisableStatus(candidate.getDrivingLicenseNumber());
                        }
                        if (modifiedCheckName.contains("criminalcheck")) {
                            LegalProceedingsDTO legalProceedingsDTO = new LegalProceedingsDTO();

                            List<CriminalCheck> civilproceding = criminalCheckRepository.findByCheckUniqueIdAndProceedingsType(String.valueOf(data.getCheckUniqueId()), "CIVILPROCEDING");
                            if (civilproceding.isEmpty() == false) {
                                legalProceedingsDTO.setCivilProceedingList(civilproceding);
                            }
                            List<CriminalCheck> criminalproceding = criminalCheckRepository.findByCheckUniqueIdAndProceedingsType(String.valueOf(data.getCheckUniqueId()), "CRIMINALPROCEDING");
                            if (criminalproceding.isEmpty() == false) {
                                legalProceedingsDTO.setCriminalProceedingList(criminalproceding);
                            }
                            criminalCheckListMap.put(String.valueOf(data.getCheckName()), legalProceedingsDTO);
                            //                              log.info("criminal check data" + criminalCheckListMap);
                        }

                    });
                    candidateReportDTO.setLiChecksDetails(licheckRequiredResponseDtos);
                } else {
                    candidateReportDTO.setLiChecksDetails(new ArrayList<>());
                }
                candidateReportDTO.setCriminalCheckList(criminalCheckListMap);
                CandidateVerificationState candidateVerificationState = candidateService.getCandidateVerificationStateByCandidateId(candidate.getCandidateId());
                boolean hasCandidateVerificationStateChanged = false;
                if (Objects.isNull(candidateVerificationState)) {
                    System.out.println("candidateVerificationState>>>>>>>>");
                    candidateVerificationState = new CandidateVerificationState();
                    candidateVerificationState.setCandidate(candidate);
                    final ZoneId id = ZoneId.systemDefault();
                    //                      candidateVerificationState.setCaseInitiationTime(ZonedDateTime.ofInstant(candidate.getCreatedOn().toInstant(), id));
                }
                ZoneId zoneId = ZoneId.of("Asia/Kolkata");
                ZonedDateTime currentDatetime = ZonedDateTime.now(zoneId);
                candidateReportDTO.setCurrentDate(DateUtil.convertToString(currentDatetime));

                Instant instant = conventionalVendorCandidatesSubmitted.getCreatedOn().toInstant();
                //                  log.info("instatnt" + instant.toString());
                ZonedDateTime zonedDateTime = instant.atZone(zoneId);
                //                  log.info("case initiation datettime" + zonedDateTime);
                candidateReportDTO.setCaseInitiationDate(DateUtil.convertToString(zonedDateTime));
                //                  log.info("case initiation datettime" + candidateReportDTO.getCaseInitiationDate());
                switch (reportType) {
                    case DISCREPANCY:
                        candidateVerificationState.setDiscrepancyReportTime(ZonedDateTime.now());
                }
                candidateVerificationState = candidateService.addOrUpdateCandidateVerificationStateByCandidateId(candidate.getCandidateId(), candidateVerificationState);
                //                  Content byCandidateIdAndLastUpdatedOnMax = contentRepository.findByCandidateIdAndLastUpdatedOnMax(candidateId);
                //                  candidateReportDTO.setFinalReportDate(String.valueOf(byCandidateIdAndLastUpdatedOnMax.getLastUpdatedOn()));
                candidateReportDTO.setFinalReportDate(DateUtil.convertToString(candidateVerificationState.getFinalReportTime()));
                candidateReportDTO.setInterimReportDate(DateUtil.convertToString(candidateVerificationState.getInterimReportTime()));
                candidateReportDTO.setDiscrepancyReportDate(DateUtil.convertToString(candidateVerificationState.getDiscrepancyReportTime()));

                VendorUploadChecks result = vendorUploadChecksRepository.findByVendorChecksVendorcheckId(uniqueCheckId);
                VendorUploadChecksDto vendorUploadChecksDto = null;
                List<Map<String, List<Map<String, String>>>> dataList = new ArrayList<>();

                if (result != null) {
                    // Set vendor attributes
                    User user = userRepository.findByUserId(result.getVendorChecks().getVendorId());
                    ArrayList<VendorAttributeDto> vendorAttributeDtos = new ArrayList<>();
                    VendorAttributeDto vendorAttributeDto = new VendorAttributeDto();
                    ConventionalVendorliChecksToPerform conventionalVendorliChecksToPerform = liCheckToPerformRepository.findById(result.getVendorChecks().getLicheckId()).get();
                    //                      vendorAttributeDto.setSourceName(vendorChecksss.getVendorChecks().getSource().getSourceName());
                    if (conventionalVendorliChecksToPerform.getSource().getSourceName().equalsIgnoreCase("GLOBAL DATABASE CHECK")) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        for (String jsonData : result.getVendorAttirbuteValue()) {
                            Map<String, List<Map<String, String>>> dataMap = objectMapper.readValue(jsonData, new TypeReference<Map<String, List<Map<String, String>>>>() {
                            });
                            dataList.add(dataMap);
                        }
                        vendorAttributeDto.setSourceName(conventionalVendorliChecksToPerform.getCheckName());
                        vendorAttributeDtos.add(vendorAttributeDto);
                    } else {
                        if (conventionalVendorliChecksToPerform.getSource().getSourceName().contains("EMPLOYMENT")) {
                            int lastNumber = getLastNumber(conventionalVendorliChecksToPerform.getCheckName());
                            vendorAttributeDto.setSourceName(conventionalVendorliChecksToPerform.getCheckName());
                            vendorAttributeDto.setVendorAttirbuteValue(result.getVendorAttirbuteValue());
                            vendorAttributeDto.setIndexNumber(lastNumber);
                            vendorAttributeDtos.add(vendorAttributeDto);
                        } else {
                            vendorAttributeDto.setSourceName(conventionalVendorliChecksToPerform.getCheckName());
                            vendorAttributeDto.setVendorAttirbuteValue(result.getVendorAttirbuteValue());
                            vendorAttributeDtos.add(vendorAttributeDto);
                        }
                    }
                    vendorUploadChecksDto = new VendorUploadChecksDto(user.getUserFirstName(), result.getVendorChecks().getVendorcheckId(), result.getVendorUploadedDocument(), result.getDocumentname(), result.getAgentColor().getColorName(), result.getAgentColor().getColorHexCode(), null);
                    //                      if (vendorUploadChecksDto.getDocument() != null) {
                    //                          File tempFile = File.createTempFile(vendorUploadChecksDto.getDocumentname(), ".pdf");
                    //                          FileOutputStream fos = new FileOutputStream(tempFile);
                    //                          fos.write(vendorUploadChecksDto.getDocument());
                    //                          fos.close();
                    //                          log.info("temp file" + tempFile);
                    //                      }
                    vendorUploadChecksDto.setVendorAttirbuteValue(vendorAttributeDtos);
                    vendordocDtoList.add(vendorUploadChecksDto);

                }

                List<VendorUploadChecksDto> collect1 = vendordocDtoList.stream().filter(da -> da.getVendorAttirbuteValue() != null).collect(Collectors.toList());
                Collections.sort(collect1, Comparator.comparing(dto -> {
                    List<VendorAttributeDto> vendorAttributeValue = dto.getVendorAttirbuteValue();
                    // Sorting based on indexNumber of the first element in vendorAttributeValue
                    return vendorAttributeValue.isEmpty() ? Integer.MAX_VALUE : vendorAttributeValue.get(0).getIndexNumber();
                }));
                candidateReportDTO.setVendorProofDetails(collect1);
                candidateReportDTO.setDataList(dataList);


                List<Map<String, List<String>>> encodedImagesList = new ArrayList<>();


                //                  for (VendorUploadChecks vendorUploadCheck : result) {
                Map<String, List<String>> encodedImageMap = new HashMap<>();

                Long checkId = result.getVendorChecks().getVendorcheckId();
                String sourceName = result.getVendorChecks().getSource().getSourceName();
                if (sourceName != null && !sourceName.isEmpty()) {

                    //                          String nameOfCheck = checkName.isEmpty() ? null : checkName.get(encodedImagesList.size() % checkName.size());
                    Optional<ConventionalVendorliChecksToPerform> byId = liCheckToPerformRepository.findById(result.getVendorChecks().getLicheckId());
                    String nameOfCheck = byId.get().getCheckName();


                    byte[] documentBytes = result.getVendorUploadedDocument();

                    ObjectMapper objectMapper = new ObjectMapper();
                    String vendorUploadedImages = result.getVendorUploadedImage();

                    if (result.getVendorUploadDocumentPathKey() != null) {
                        documentBytes = awsUtils.getbyteArrayFromS3(DIGIVERIFIER_DOC_BUCKET_NAME, result.getVendorUploadDocumentPathKey());
                    }
                    if (result.getVendorUploadImagePathKey() != null) {
                        byte[] imageBytes = awsUtils.getbyteArrayFromS3(DIGIVERIFIER_DOC_BUCKET_NAME, result.getVendorUploadImagePathKey());
                        vendorUploadedImages = new String(imageBytes);
                    }


                    try {
                        if (vendorUploadedImages != null) {
                            List<Map<String, List<String>>> decodedImageList = objectMapper.readValue(vendorUploadedImages, new TypeReference
                                    <List<Map<String, List<String>>>>() {
                            });

                            List<String> allEncodedImages = decodedImageList.stream()
                                    .flatMap(imageMap -> imageMap.values().stream())
                                    .flatMap(List::stream)
                                    .collect(Collectors.toList());

                            // Loop through each image byte array and encode it to Base64
                            List<String> encodedImagesForDocument = new ArrayList<>();
                            encodedImageMap.put(nameOfCheck, allEncodedImages);
                        } else if (documentBytes != null) {
                            List<byte[]> imageBytes = convertPDFToImage(documentBytes);
                            List<String> encodedImagesForDocument = new ArrayList<>();
                            for (int j = 0; j < imageBytes.size(); j++) {
                                byte[] imageBytess = imageBytes.get(j);
                                String encodedImage = Base64.getEncoder().encodeToString(imageBytess);
                                encodedImagesForDocument.add(encodedImage);

                            }
                            encodedImageMap.put(nameOfCheck, encodedImagesForDocument);

                        } else {
                            log.debug("Vendor uploaded document is null {}");
                            encodedImageMap.put(nameOfCheck, null);

                        }

                        encodedImagesList.add(encodedImageMap);

                    } catch (JsonProcessingException e) {
                        // Handle the exception (e.g., log or throw)
                        e.printStackTrace();
                    }

                }
                //                  }
                candidateReportDTO.setPdfByes(encodedImagesList);


                try {
                    //                      File report = FileUtil.createUniqueTempFile("discrepancyReport", ".pdf");
                    List<File> files1 = null;
                    File discrepancyreport = null;
                    if (reportType.label.equalsIgnoreCase("DISCREPANCY")) {
                        discrepancyreport = FileUtil.createUniqueTempFile("discrepancyreport", ".pdf");
                        String discrepancyStr = pdfService.parseThymeleafTemplateForConventionalCandidate("Discrepancy_pdf.html", candidateReportDTO);
                        checkNameToDtoMap.put("Discrepancy_pdf.html", candidateReportDTO);
                        pdfService.generatePdfFromHtml(discrepancyStr, discrepancyreport);
                        List<Content> contentList1 = contentRepository.findAllByCandidateIdAndContentTypeIn(candidate.getCandidateId(), Arrays.asList(ContentType.ISSUED, ContentType.AGENT_UPLOADED));

                        files1 = contentList1.stream().map(content -> {
                            File uniqueTempFile = FileUtil.createUniqueTempFile(candidateId + "_issued_" + content.getContentId().toString(), ".pdf");
                            awsUtils.getFileFromS3(content.getBucketName(), content.getPath(), uniqueTempFile);
                            return uniqueTempFile;
                        }).collect(Collectors.toList());
                    }

                    File mergedFile = FileUtil.createUniqueTempFile(String.valueOf(candidateId), ".pdf");
                    List<InputStream> collect = new ArrayList<>();

                    if (reportType.label.equalsIgnoreCase("DISCREPANCY")) {
                        collect.add(FileUtil.convertToInputStream(discrepancyreport));
                        collect.addAll(files1.stream().map(FileUtil::convertToInputStream).collect(Collectors.toList()));
                    }

                    PdfUtil.mergePdfFiles(collect, new FileOutputStream(mergedFile.getPath()));
                    //                    log.info("merged file path" + mergedFile.getPath());
                    String jsonPath = "Digiverifier_Analytics_LTIM_Conventional/Candidate/".concat(candidateId + "/Generated".concat("/").concat(conventionalVendorCandidatesSubmitted.getRequestId() + "_" + conventionalVendorCandidatesSubmitted.getName() + "_" + reportType.name()).concat(".pdf"));
                    String jsonPrecisedUrl = awsUtils.uploadDtosAsJsonToS3AndReturnPresignedUrl(checkNameToDtoMap, jsonPath, DIGIVERIFIER_DOC_BUCKET_NAME);
                    String tempPath = "TEMP/Candidate/".concat(candidateId + "/Generated".concat("/").concat(conventionalVendorCandidatesSubmitted.getRequestId() + "_" + conventionalVendorCandidatesSubmitted.getName() + "_" + reportType.name()).concat(".pdf"));
                    String pdfUrl = awsUtils.uploadFileAndGetPresignedUrlTemp(DIGIVERIFIER_DOC_BUCKET_NAME, tempPath, mergedFile);
                    System.out.println(pdfUrl);
                    Content content = new Content();
                    content.setCandidateId(candidate.getCandidateId());
                    content.setContentCategory(ContentCategory.OTHERS);
                    // log.info(content+"*******************************************content");
                    if (reportType.name().equalsIgnoreCase("DISCREPANCY")) {
                        content.setContentSubCategory(ContentSubCategory.DISCREPANCY);
                    } else if (reportType.name().equalsIgnoreCase("FINAL")) {
                        content.setContentSubCategory(ContentSubCategory.FINAL);
                    }
                    content.setFileType(FileType.PDF);
                    content.setContentType(ContentType.GENERATED);
                    content.setBucketName(DIGIVERIFIER_DOC_BUCKET_NAME);
                    content.setCreatedOn(new Date());
                    content.setPath(jsonPath);
                    contentRepository.save(content);

                    // delete files
                    files1.stream().forEach(file -> file.delete());
                    stringServiceOutcome.setData(pdfUrl);
                    stringServiceOutcome.setStatus(tempPath);
                    stringServiceOutcome.setMessage(uniqueCheckId.toString());
                    mergedFile.delete();
                    discrepancyreport.delete();


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


            } else {
                throw new RuntimeException("unable to generate Discrepancy report document for this candidate");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return stringServiceOutcome;
    }


    public ServiceOutcome<String> generateDiscrepancyreportAgent(ReportType reportType, Long uniqueCheckId, Long requestId) {
        ServiceOutcome<String> stringServiceOutcome = new ServiceOutcome<>();
        try {
            Candidate candidate = candidateRepository.findByConventionalRequestId(Long.valueOf(requestId));
            stringServiceOutcome = generateDiscrepancyreport(candidate.getCandidateId(), ReportType.DISCREPANCY, "DONT", uniqueCheckId);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return stringServiceOutcome;

    }


    @Transactional(rollbackFor = Exception.class)
    public ServiceOutcome<String> generateJsonRepsonseByConventionalCandidateId(String requestId, ReportType reportType, String update, String reportDeliveryDate) {

        ServiceOutcome<String> listServiceOutcome = new ServiceOutcome<>();
        List<liReportDetails> liReportDetails = new ArrayList<>();
        try {
            ArrayList<UpdateSubmittedCandidatesResponseDto> updateSubmittedCandidatesResponseDtos = new ArrayList<>();
            com.aashdit.digiverifier.vendorcheck.dto.liReportDetails liReportDetails1 = new liReportDetails();
            ConventionalVendorCandidatesSubmitted conventinalCandidate = conventionalCandidatesSubmittedRepository.findByRequestId(requestId);
            updateCandidateVerificationStatus(requestId);
            UpdateSubmittedCandidatesResponseDto conventionalVendorCandidatesSubmitted = new UpdateSubmittedCandidatesResponseDto();
            conventionalVendorCandidatesSubmitted.setCandidateID(String.valueOf(conventinalCandidate.getCandidateId()));
            conventionalVendorCandidatesSubmitted.setName(conventinalCandidate.getName());
            conventionalVendorCandidatesSubmitted.setPSNO(conventinalCandidate.getPsNo());
            conventionalVendorCandidatesSubmitted.setRequestID(conventinalCandidate.getRequestId());
            conventionalVendorCandidatesSubmitted.setVendorName(conventinalCandidate.getVendorId());
            List<liChecksDetails> allLiCheckResponseByCandidateId = liCheckToPerformRepository.findAllUpdateLiCheckResponseByRequestId(String.valueOf(conventionalVendorCandidatesSubmitted.getRequestID()));

            allLiCheckResponseByCandidateId.stream().forEach(lichec -> {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                String formattedDate = dateFormat.format(new Date());
                lichec.setCompletedDate(formattedDate.toString());
                if (lichec.getModeOfVerficationPerformed() == null) {
                    lichec.setModeOfVerficationPerformed("3");
                }
            });
            List<liChecksDetails> collect = allLiCheckResponseByCandidateId.stream().filter(licheck -> (licheck.getCheckStatus() != 2l && licheck.getCheckStatus() != 7l) && licheck.getModeOfVerficationPerformed() != null).collect(Collectors.toList());
            Candidate candidate = candidateRepository.findByConventionalRequestId(Long.valueOf(conventionalVendorCandidatesSubmitted.getRequestID()));
            ServiceOutcome<String> stringServiceOutcome = generateConventionalCandidateReport(candidate.getCandidateId(), reportType, update, reportDeliveryDate);
            String reportUploadedPrecisedUrl = stringServiceOutcome.getData();
            String tempReportPath = stringServiceOutcome.getStatus() != null ? stringServiceOutcome.getStatus() : null;
            if (reportUploadedPrecisedUrl != null) {
                listServiceOutcome.setData(reportUploadedPrecisedUrl);
                List<Content> allByCandidateId = contentRepository.findAllByCandidateId(candidate.getCandidateId());
                allByCandidateId.forEach(content -> {
                    String bucketName = content.getBucketName();
                    String path = content.getPath();
                    String[] split = path.split("/");
                    String filename = split[split.length - 1];
                    String fileExtension = filename.substring(filename.length() - 4, filename.length());
                    liReportDetails1.setReportFileExtention(fileExtension);
                    liReportDetails1.setReportFileName(filename);
                    try {
                        byte[] bytes = awsUtils.getbyteArrayFromS3(bucketName, tempReportPath);
                        String base64String = Base64.getEncoder().encodeToString(bytes);
                        liReportDetails1.setReportAttachment(base64String);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                });

            }

            ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted1 = conventionalCandidatesSubmittedRepository.findByRequestId(conventionalVendorCandidatesSubmitted.getRequestID());

            if (reportType.label.equalsIgnoreCase("INTERIM")) {
                liReportDetails1.setReportType("1");
            }
            if (reportType.label.equalsIgnoreCase("FINAL")) {
                liReportDetails1.setReportType("3");
            }
            if (reportType.label.equalsIgnoreCase("Supplimentry")) {
                liReportDetails1.setReportType("2");
            }

            if (conventionalVendorCandidatesSubmitted1.getVerificationStatus().equalsIgnoreCase("CLEAR")) {
                liReportDetails1.setReportStatus("1");
            }

            if (conventionalVendorCandidatesSubmitted1.getVerificationStatus().equalsIgnoreCase("INPROGRESS")) {
                liReportDetails1.setReportStatus("2");
            }

            if (conventionalVendorCandidatesSubmitted1.getVerificationStatus().equalsIgnoreCase("INSUFFICIENCY")) {
                liReportDetails1.setReportStatus("3");
            }
            if (conventionalVendorCandidatesSubmitted1.getVerificationStatus().equalsIgnoreCase("MAJORDISCREPANCY")) {
                liReportDetails1.setReportStatus("4");
            }

            if (conventionalVendorCandidatesSubmitted1.getVerificationStatus().equalsIgnoreCase("MINORDISCREPANCY")) {
                liReportDetails1.setReportStatus("5");
            }

            if (conventionalVendorCandidatesSubmitted1.getVerificationStatus().equalsIgnoreCase("UNABLETOVERIFIY")) {
                liReportDetails1.setReportStatus("6");
            }

            liReportDetails1.setVendorReferenceID(String.valueOf(conventionalVendorCandidatesSubmitted1.getApplicantId()));
            liReportDetails.add(liReportDetails1);
            conventionalVendorCandidatesSubmitted.setLiReportDetails(liReportDetails);
            conventionalVendorCandidatesSubmitted.setLiChecksDetails(collect);
            updateSubmittedCandidatesResponseDtos.add(conventionalVendorCandidatesSubmitted);

//            listServiceOutcome.setData(conventionalVendorCandidatesSubmitted);
            //hitting the update request to third party api
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", environmentVal.getMtGrantType());
            map.add("username", environmentVal.getMtUsername());
            map.add("password", environmentVal.getMtPassword());

            HttpHeaders tokenHeader = new HttpHeaders();
            tokenHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            ResponseEntity<String> responseEntity = null;
            HttpEntity<MultiValueMap<String, String>> requestBodyFormUrlEncoded = new HttpEntity<>(map, tokenHeader);
            responseEntity = restTemplate.postForEntity(environmentVal.getConventionalVendorToken(), requestBodyFormUrlEncoded, String.class);
            JSONObject tokenObject = new JSONObject(responseEntity.getBody());
            String access_token = tokenObject.getString("access_token");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + access_token);
            headers.set("Content-Type", "application/json");
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (update.equalsIgnoreCase("UPDATE")) {

                HttpEntity<List<UpdateSubmittedCandidatesResponseDto>> liCheckDtoHttpEntity = new HttpEntity<>(updateSubmittedCandidatesResponseDtos, headers);
                ResponseEntity<String> icheckRepsonse = restTemplate.exchange(environmentVal.getConventionalUpdateBgvCheckStatusRowwise(), HttpMethod.POST, liCheckDtoHttpEntity, String.class);

                listServiceOutcome.setMessage(icheckRepsonse.getBody());
                if (reportType.label.equalsIgnoreCase("INTERIM")) {
                    StatusMaster interimreport = statusMasterRepository.findByStatusCode("INTERIMREPORT");
                    log.info("Response On " + reportType.label + " Report  Generation for Request Id " + requestId + " ---" + icheckRepsonse);
                    log.info("interim updated");
                    conventionalVendorCandidatesSubmitted1.setStatus(interimreport);
                    ConventionalVendorCandidatesSubmitted updatedToInterim = conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted1);
                }

                if (reportType.label.equalsIgnoreCase("FINAL")) {
                    StatusMaster interimreport = statusMasterRepository.findByStatusCode("FINALREPORT");
                    log.info("Response On " + reportType.label + " Report  Generation for Request Id " + requestId + " ---" + icheckRepsonse);
                    log.info("final report updated");
                    conventionalVendorCandidatesSubmitted1.setStatus(interimreport);
                    ConventionalVendorCandidatesSubmitted updatedToInterim = conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted1);
                }
            }
            listServiceOutcome.setOutcome(true);
        } catch (HttpClientErrorException e) {
            listServiceOutcome.setData(null);
            listServiceOutcome.setOutcome(false);
            listServiceOutcome.setMessage("Iverifiy Exception  <br>" + e.getMessage());
            log.error("Iverifiy Exception  \n" + e.getMessage());
            // Mark transaction for rollback without throwing an exception
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();


        } catch (Exception e) {
            listServiceOutcome.setData(null);
            listServiceOutcome.setOutcome(false);
            listServiceOutcome.setMessage("Report Generation Failed<br>" + e.getMessage());
            log.error("Exception in Trigger Conventional Candidate Report()  \n" + e.getMessage());
            // Mark transaction for rollback without throwing an exception
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return listServiceOutcome;


    }

    private boolean isPDF(byte[] bytes) {
        // Check if the file magic number matches PDF
        return bytes.length > 4 &&
                bytes[0] == '%' &&
                bytes[1] == 'P' &&
                bytes[2] == 'D' &&
                bytes[3] == 'F';
    }

    @Transactional
    public ServiceOutcome<ConventionalVendorCandidatesSubmitted> findConventionalCandidateByCandidateId(Long requestId) {
        ServiceOutcome<ConventionalVendorCandidatesSubmitted> conventionalVendorCandidatesSubmittedServiceOutcome = new ServiceOutcome<>();
        try {

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", environmentVal.getMtGrantType());
            map.add("username", environmentVal.getMtUsername());
            map.add("password", environmentVal.getMtPassword());
            HttpHeaders tokenHeader = new HttpHeaders();
            tokenHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            ResponseEntity<String> responseEntity = null;
            HttpEntity<MultiValueMap<String, String>> requestBodyFormUrlEncoded = new HttpEntity<>(map, tokenHeader);
            responseEntity = restTemplate.postForEntity(environmentVal.getConventionalVendorToken(), requestBodyFormUrlEncoded, String.class);
            JSONObject tokenObject = new JSONObject(responseEntity.getBody());
            String access_token = tokenObject.getString("access_token");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + access_token);
            headers.set("Content-Type", "application/json");
            HttpEntity<String> vendorIdHttp = new HttpEntity<>("{\"VendorID\":\"2CDC7E3A\"}", headers);
            ResponseEntity<String> candidateResponse = restTemplate.exchange(environmentVal.getConventionalVendorFetchVendorRequestDetails(), HttpMethod.POST, vendorIdHttp, String.class);
            String candidateRequestResponse = candidateResponse.getBody();
            JSONArray obj1 = new JSONArray(candidateRequestResponse);

            List<JSONObject> collect = IntStream.range(0, obj1.length()).mapToObj(index -> ((JSONObject) obj1.get(index))).collect(Collectors.toList());
            boolean b = conventionalCandidatesSubmittedRepository.existsByRequestId(String.valueOf(requestId));
            if (b == true) {
                log.info("addConvetionalCandidateData() starts for requestId: {}", requestId);
                ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(String.valueOf(requestId));
                FetchVendorConventionalCandidateDto fetchVendorConventionalCandidateDto = new FetchVendorConventionalCandidateDto();
                fetchVendorConventionalCandidateDto.setCandidateID(String.valueOf(conventionalVendorCandidatesSubmitted.getCandidateId()));
                fetchVendorConventionalCandidateDto.setRequestId(conventionalVendorCandidatesSubmitted.getRequestId());
                fetchVendorConventionalCandidateDto.setPsno(conventionalVendorCandidatesSubmitted.getPsNo());
                fetchVendorConventionalCandidateDto.setVendorId(conventionalVendorCandidatesSubmitted.getVendorId());
                fetchVendorConventionalCandidateDto.setRequestType(conventionalVendorCandidatesSubmitted.getRequestType());
                fetchVendorConventionalCandidateDto.setCandidateStatus(conventionalVendorCandidatesSubmitted.getStatus().getStatusCode());
                boolean found = collect.stream()
                        .anyMatch(json -> json.getLong("RequestID") == requestId);
                if (found == true || conventionalVendorCandidatesSubmitted.getFetchLicheckAndCandidateData() == true) {
                    if (conventionalVendorCandidatesSubmitted.getFetchLicheckAndCandidateData() == null) {
                        log.debug("updating fetch flag  to true for requestId: {}", requestId);
                        conventionalVendorCandidatesSubmitted.setFetchLicheckAndCandidateData(true);
                        ConventionalVendorCandidatesSubmitted updatedFetchFlag = conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted);
                        log.debug("updated fetch flag  for requestId: {}  -- fetch flag status {}", updatedFetchFlag.getRequestId(), updatedFetchFlag.getFetchLicheckAndCandidateData());
                    }
                    User user = (SecurityHelper.getCurrentUser() != null) ? SecurityHelper.getCurrentUser() : userRepository.findByUserId(53l);
                    //To generate token first
//                    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
//                    map.add("grant_type", environmentVal.getMtGrantType());
//                    map.add("username", environmentVal.getMtUsername());
//                    map.add("password", environmentVal.getMtPassword());
//                    HttpHeaders tokenHeader = new HttpHeaders();
//                    tokenHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//                    ResponseEntity<String> responseEntity = null;
//                    HttpEntity<MultiValueMap<String, String>> requestBodyFormUrlEncoded = new HttpEntity<>(map, tokenHeader);
//                    responseEntity = restTemplate.postForEntity(environmentVal.getConventionalVendorToken(), requestBodyFormUrlEncoded, String.class);
//                    JSONObject tokenObject = new JSONObject(responseEntity.getBody());
//                    String access_token = tokenObject.getString("access_token");
//                    HttpHeaders headers = new HttpHeaders();
//                    headers.set("Authorization", "Bearer " + access_token);
//                    headers.set("Content-Type", "application/json");
                    HttpEntity<FetchVendorConventionalCandidateDto> liCheckDtoHttpEntity = new HttpEntity<>(fetchVendorConventionalCandidateDto, headers);
                    ResponseEntity<String> icheckRepsonse = restTemplate.exchange(environmentVal.getConventionalVendorFetchVendorChecks(), HttpMethod.POST, liCheckDtoHttpEntity, String.class);
                    String message = icheckRepsonse.getBody();
                    log.info("candidate ---- request id: {} ---- request type: {}", requestId, conventionalVendorCandidatesSubmitted.getRequestType());
                    ServiceOutcome<List> listServiceOutcome = candidateService.saveConventionalCandidateInformation(fetchVendorConventionalCandidateDto, message, headers);
                    addUpdateLiCheckToPerformData(fetchVendorConventionalCandidateDto, message);
                    if (listServiceOutcome.getOutcome() == true) {
                        ServiceOutcome<List<LicheckRequiredResponseDto>> allNewUploadLiChecksRequiredbyCandidateId = findAllNewUploadLiChecksRequiredbyCandidateId(String.valueOf(requestId));
                        log.info("After saving Candidate Basic initiating to  Vendor completed outcome --" + allNewUploadLiChecksRequiredbyCandidateId.getOutcome());
                        listServiceOutcome.setOutcome(allNewUploadLiChecksRequiredbyCandidateId.getOutcome());
                    } else {
                        log.info("Candidate Basic Saved , Outcome " + listServiceOutcome.getOutcome());
                    }
                } else {
                    log.debug("fetched falg false condition for request id  -  " + fetchVendorConventionalCandidateDto.getRequestId());
                    conventionalVendorCandidatesSubmittedServiceOutcome.setData(conventionalVendorCandidatesSubmitted);
                    conventionalVendorCandidatesSubmittedServiceOutcome.setOutcome(true);
                }
            }

        } catch (Exception e) {
            conventionalVendorCandidatesSubmittedServiceOutcome.setData(null);
            conventionalVendorCandidatesSubmittedServiceOutcome.setOutcome(false);
            log.error("in findConventionalCandidateByCandidateId " + e.getMessage());
        }
        return conventionalVendorCandidatesSubmittedServiceOutcome;
    }

    @Transactional
    public ServiceOutcome<List<ReportUtilizationDto>> generateJsonResponse() throws Exception {

        ServiceOutcome<List<ReportUtilizationDto>> serviceOutcome = new ServiceOutcome<>();

        ArrayList<ReportUtilizationDto> reportUtilizationDtos = new ArrayList<>();
        try {
            List<VendorChecks> all = vendorChecksRepository.findAllVendorChecksInVendorUploadChecks();
            List<Long> vendorIdList = new ArrayList<>();
            for (VendorChecks vendorChecks : all) {
                ReportUtilizationDto reportUtilizationDto = new ReportUtilizationDto();
                Long vendorId = vendorChecks.getVendorId();
                reportUtilizationDto.setVendorId(vendorId);
                User user = userRepository.findById(vendorId).get();
                reportUtilizationDto.setVendorName(user.getUserFirstName());
                Long vendorcheckId = vendorChecks.getVendorcheckId();
                VendorChecks byVendorcheckId = vendorChecksRepository.findByVendorcheckId(vendorcheckId);
                User caseInitatedBy = userRepository.findById(byVendorcheckId.getCreatedBy().getUserId()).get();
                reportUtilizationDto.setCaseInititatedBy(caseInitatedBy.getUserName());
                ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(String.valueOf(byVendorcheckId.getCandidate().getConventionalRequestId()));
                reportUtilizationDto.setUrnRefNo(String.valueOf(conventionalVendorCandidatesSubmitted.getApplicantId()));
                reportUtilizationDto.setCandidateName(byVendorcheckId.getCandidateName());
                reportUtilizationDto.setCaseAssignedDate(String.valueOf(byVendorcheckId.getCreatedOn()));
                reportUtilizationDto.setReportCode(conventionalVendorCandidatesSubmitted.getVerificationStatus());
                reportUtilizationDto.setCheckName(byVendorcheckId.getSource().getSourceName());
                CandidateVerificationState canidateVerificationData = candidateVerificationStateRepository.findByCandidateCandidateId(byVendorcheckId.getCandidate().getCandidateId());
                if (canidateVerificationData != null) {
                    reportUtilizationDto.setReportSubmittedDate(String.valueOf(canidateVerificationData.getInterimReportTime()));
                }
                vendorIdList.add(vendorId);
                reportUtilizationDtos.add(reportUtilizationDto);
            }
            for (ReportUtilizationDto reportUtilizationDto : reportUtilizationDtos) {
                for (Long vendorId : vendorIdList) {
                    List<VendorChecks> allGroupByVendorCheckId = vendorChecksRepository.findAllGroupByVendorCheckId(vendorId);
                    if (reportUtilizationDto.getVendorId().equals(vendorId)) {
                        ArrayList<ChecksDto> checksDtos = new ArrayList<>();
                        for (VendorChecks checks : allGroupByVendorCheckId) {
                            ConventionalVendorliChecksToPerform byVendorChecksVendorcheckId = liCheckToPerformRepository.findByVendorChecksVendorcheckId(checks.getVendorcheckId());
                            ChecksDto checksDto = new ChecksDto();
                            checksDto.setColorCode(checks.getVendorCheckStatusMaster().getCheckStatusCode());
                            checksDto.setCourceName(checks.getSource().getSourceName());
                            VendorMasterNew byVendorId = vendorMasterNewRepository.findByVendorId(checks.getVendorId());
                            if (byVendorId != null) {
                                checksDto.setPerUnitPrice(byVendorId.getRatePerItem());
                                checksDto.setQuantity((long) allGroupByVendorCheckId.size());
                                checksDto.setTotalCode(String.valueOf(checksDto.getPerUnitPrice() * checksDto.getQuantity()));
                            }
                            checksDtos.add(checksDto);
                        }
                        reportUtilizationDto.setChecksDtos(checksDtos);
                    }

                }
                serviceOutcome.setData(reportUtilizationDtos);
            }
            // Create a new workbook
            Workbook workbook = new XSSFWorkbook();
            // Create a new sheet
            Sheet sheet = workbook.createSheet("Report Excel Data");
            sheet.setDefaultColumnWidth(12);
            // candidate cell style
//            int desiredCellWidth = 10;
            CellStyle headerCellStyle = workbook.createCellStyle();
//            headerCellStyle.setShrinkToFit(true);
            headerCellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontName("Arial");
            headerFont.setFontHeightInPoints((short) 10);
            headerCellStyle.setFont(headerFont);
            //licheck cellstyle
            CellStyle headerCellStyle2 = workbook.createCellStyle();
            headerCellStyle2.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerCellStyle2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont2 = workbook.createFont();
            headerFont2.setBold(true);
            headerFont2.setFontName("Arial");
            headerFont.setFontHeightInPoints((short) 10);
            headerCellStyle2.setFont(headerFont);
            //for the lichek heading -leaving the row


            List<ReportUtilizationDto> data = serviceOutcome.getData();
            int startcellno = 7;
            int endcellNo = 10;
            Row headerRow1 = sheet.createRow(3);
            for (ReportUtilizationDto datum : data) {
                ReportUtilizationDto reportUtilizationDto1 = datum;
                for (ChecksDto checksDto : reportUtilizationDto1.getChecksDtos()) {
                    for (int j = 0; j < reportUtilizationDto1.getChecksDtos().size(); j++) {
                        // Merge cells for office data
                        CellRangeAddress officeDataMergeRegion = new CellRangeAddress(3, 3, startcellno, endcellNo);
                        sheet.addMergedRegion(officeDataMergeRegion);
                        // Place data within merged region
                        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
                        for (CellRangeAddress region : mergedRegions) {
                            if (region.equals(officeDataMergeRegion)) {
                                for (int row = region.getFirstRow(); row <= region.getLastRow(); row++) {
                                    Row mergedRow = sheet.getRow(row);
                                    Cell mergedCell = mergedRow.getCell(region.getFirstColumn());
                                    if (mergedCell == null) {
                                        mergedCell = mergedRow.createCell(region.getFirstColumn());
                                    }
                                    mergedCell.setCellValue(reportUtilizationDto1.getCheckName());

                                }
                            }
                        }

                        startcellno = startcellno + 5;
                        endcellNo = endcellNo + 5;
                    }
                }
            }


            // Create a header row
            Row headerRow = sheet.createRow(4);
            headerRow.createCell(0).setCellValue("Vendor Name");
            headerRow.createCell(1).setCellValue("Case Initiated by (DigiVerifier Spoc))");
            headerRow.createCell(2).setCellValue("URN / Ref No.");
            headerRow.createCell(3).setCellValue("Candidate Name");
            headerRow.createCell(4).setCellValue("Case Assigned Date");
            headerRow.createCell(5).setCellValue("Report Submitted Date");
            headerRow.createCell(6).setCellValue("Report Color Code");
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                cell.setCellStyle(headerCellStyle);
            }
            int rowNum = 5;


            for (ReportUtilizationDto datum : serviceOutcome.getData()) {

                Row dataRow = sheet.createRow(rowNum);
                rowNum++;
                dataRow.createCell(0).setCellValue(datum.getVendorName());
                dataRow.createCell(1).setCellValue(datum.getCaseInititatedBy());
                dataRow.createCell(2).setCellValue(datum.getUrnRefNo());
                dataRow.createCell(3).setCellValue(datum.getCandidateName());
                dataRow.createCell(4).setCellValue(datum.getCaseAssignedDate());
                dataRow.createCell(5).setCellValue(datum.getReportSubmittedDate());
                dataRow.createCell(6).setCellValue(datum.getReportCode());

                if (datum.getChecksDtos() != null) {
                    headerRow.createCell(7).setCellValue("Course Name");
                    headerRow.createCell(8).setCellValue("Qty");
                    headerRow.createCell(9).setCellValue("Price Per Unit");
                    headerRow.createCell(10).setCellValue("Color Code");
                    headerRow.createCell(11).setCellValue("Total Amount");
                    for (int i = 7; i < headerRow.getLastCellNum(); i++) {
                        Cell cell = headerRow.getCell(i);
                        cell.setCellStyle(headerCellStyle2);
                    }
                    for (int i = 1; i <= datum.getChecksDtos().size(); i++) {
                        if (i == 1) {
                            ChecksDto checksDto = datum.getChecksDtos().get(i - 1);
                            dataRow.createCell(7).setCellValue(checksDto.getCourceName());
                            dataRow.createCell(8).setCellValue(checksDto.getQuantity());
                            dataRow.createCell(9).setCellValue(checksDto.getPerUnitPrice());
                            dataRow.createCell(10).setCellValue(checksDto.getColorCode());
                            dataRow.createCell(11).setCellValue(checksDto.getTotalCode());
                        }
                        if (i != 1) {

                            Row dataRow1 = sheet.createRow(rowNum++);
                            ChecksDto checksDto = datum.getChecksDtos().get(i - 1);
                            dataRow1.createCell(7).setCellValue(checksDto.getCourceName());
                            dataRow1.createCell(8).setCellValue(checksDto.getQuantity());
                            dataRow1.createCell(9).setCellValue(checksDto.getPerUnitPrice());
                            dataRow1.createCell(10).setCellValue(checksDto.getColorCode());
                            dataRow1.createCell(11).setCellValue(checksDto.getTotalCode());

                        }

                    }


                }


            }
// Auto-size columns
            for (int i = 0; i < 7; i++) {
                sheet.autoSizeColumn(i);
            }

            File nanda = FileUtil.createUniqueTempFile("nanda", ".xlsx");

            FileOutputStream fileOutputStream = new FileOutputStream(nanda);
            workbook.write(fileOutputStream);
            byte[] fileContent = Files.readAllBytes(Paths.get(nanda.getAbsolutePath()));
            String base64String = Base64.getEncoder().encodeToString(fileContent);
//            log.info(base64String);
            ;
//            reportUtilizationDtos.get(0).setExcelBase64(base64String);
            serviceOutcome.setMessage(base64String);
        } catch (Exception e) {
            log.error("error in generate response  :" + e.getMessage());
        }
        return serviceOutcome;
    }

    //not touched
    @Transactional
    public ServiceOutcome<List<VendorReferenceDataDto>> generateReferenceDataToVendor(Long candidateId, Long sourceId, String checkName) throws Exception {
        ServiceOutcome<List<VendorReferenceDataDto>> serviceOutcome = new ServiceOutcome<>();
        try {
            ArrayList<VendorReferenceDataDto> vendorReferenceDataDtos = new ArrayList<>();
            VendorChecks vc = vendorChecksRepository.findByCandidateIdAndSourceID(candidateId, sourceId);
            String sourceName = checkName;
            String[] words = sourceName.split(" ");
            Stream<String> wordStream = Arrays.stream(words);
            if (wordStream.anyMatch(p -> p.equalsIgnoreCase("REFERENCE"))) {
                Boolean exists = conventionalCandidateReferenceInfoRepository.existsByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                if (exists) {
                    List<ConventionalCandidateReferenceInfo> byConventionalCandiateId = conventionalCandidateReferenceInfoRepository.findByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                    ArrayList<ConventionalCandidateReferenceDto> candidateReferenceDtos = new ArrayList<>();
                    byConventionalCandiateId.forEach(data -> {
                        ConventionalCandidateReferenceDto candidateReferenceDto = new ConventionalCandidateReferenceDto();
//                        candidateReferenceDto.setReferenceId(data.getReferenceId());
                        candidateReferenceDto.setReferenceNumber(data.getReferenceNumber());
                        candidateReferenceDto.setProfessionalRelation(data.getProfessionalRelation());
                        candidateReferenceDto.setName(data.getName());
                        candidateReferenceDto.setDesignation(data.getDesignation());
                        candidateReferenceDto.setCompanyName(data.getCompanyName());
//                        candidateReferenceDto.setContactNumber(data.getContactNumber());
//                        candidateReferenceDto.setEmailId(data.getEmailId());
                        candidateReferenceDto.setInsufficiencyRemarks(data.getInsufficiencyRemarks());
                        candidateReferenceDto.setDurationKnown(data.getDurationKnown());
                        candidateReferenceDtos.add(candidateReferenceDto);
                    });
                    VendorReferenceDataDto vendorReferenceDataDto = new VendorReferenceDataDto();
                    vendorReferenceDataDto.setContactNumber(vc.getCandidate().getContactNumber());
                    vendorReferenceDataDto.setDateOfBirth(vc.getCandidate().getDateOfBirth());
                    vendorReferenceDataDto.setEmailId(vc.getCandidate().getEmailId());
                    ConventionalCandidate byConventionalRequestId = conventionalCandidateRepository.findByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                    vendorReferenceDataDto.setFatherName(byConventionalRequestId.getFatherName());
                    vendorReferenceDataDto.setGender(byConventionalRequestId.getGender());
                    vendorReferenceDataDto.setCandidateId(String.valueOf(vc.getCandidate().getConventionalCandidateId()));
                    vendorReferenceDataDto.setCheckName(vc.getSource().getSourceName());
                    vendorReferenceDataDto.setVendorReferenceData(candidateReferenceDtos);
                    vendorReferenceDataDtos.add(vendorReferenceDataDto);

                }

            } else if (sourceName.contains("EMPLOYMENT")) {
                Boolean exists = conventionalCandidateExperienceRepository.existsByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                if (exists) {
                    List<ConventionalCandidateExperience> byConventionalCandidateId = conventionalCandidateExperienceRepository.findByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                    ArrayList<ConventionalExperienceDto> candidateCafExperienceDtos = new ArrayList<>();
                    byConventionalCandidateId.forEach(data -> {
                        ConventionalExperienceDto convetionalExperienceDto = new ConventionalExperienceDto();
                        convetionalExperienceDto.setSuperiorName(data.getSuperiorName());
                        convetionalExperienceDto.setDesignation(data.getDesignation());
                        convetionalExperienceDto.setDuration(data.getDuration());
                        convetionalExperienceDto.setSuperiorDesignation(data.getSuperiorDesignation());
                        convetionalExperienceDto.setSuperiorEmailID(data.getSuperiorEmailID());
                        convetionalExperienceDto.setEmployeeCode(data.getEmployeeCode());
                        convetionalExperienceDto.setEmploymentType(data.getEmploymentType());
                        convetionalExperienceDto.setGrossSalary(data.getGrossSalary());
                        convetionalExperienceDto.setHrContactNumber(data.getHrContactNumber());
                        convetionalExperienceDto.setHrEmailId(data.getHrEmailId());
                        convetionalExperienceDto.setHrName(data.getHrName());
                        convetionalExperienceDto.setInsufficiencyRemarks(data.getInsufficiencyRemarks());
                        convetionalExperienceDto.setLastSalary(data.getLastSalary());
                        convetionalExperienceDto.setSuperiorContactNumber(data.getSuperiorContactNumber());
                        convetionalExperienceDto.setSuperiorName(data.getSuperiorName());
                        if (data.getIsSuspect() != null) {
                            convetionalExperienceDto.setIsSuspect(data.getIsSuspect());
                        }
                        if (data.getOfficeAddress() != null) {
                            convetionalExperienceDto.setOfficeAddress(data.getOfficeAddress());
                        }
                        if (data.getReasonForSuspect() != null) {
                            convetionalExperienceDto.setReasonForSuspect(data.getReasonForSuspect());
                        }
                        Optional<CandidateCafExperience> byId = candidateCafExperienceRepository.findById(data.getCandidateCafExperience());
                        if (byId.isPresent()) {
                            convetionalExperienceDto.setCandidateEmployerName(byId.get().getCandidateEmployerName());
                            convetionalExperienceDto.setInputDateOfJoining(dateFormater.format(byId.get().getInputDateOfJoining()));
                            convetionalExperienceDto.setInputDateOfExit(dateFormater.format(byId.get().getInputDateOfExit()));
                            if (byId.get().getUan() != null) {
                                convetionalExperienceDto.setUan(byId.get().getUan());

                            }
                        }
                        candidateCafExperienceDtos.add(convetionalExperienceDto);
                    });


                    VendorReferenceDataDto vendorReferenceDataDto = new VendorReferenceDataDto();
                    vendorReferenceDataDto.setContactNumber(vc.getCandidate().getContactNumber());
                    vendorReferenceDataDto.setDateOfBirth(vc.getCandidate().getDateOfBirth());
                    vendorReferenceDataDto.setEmailId(vc.getCandidate().getEmailId());
                    ConventionalCandidate byConventionalRequestId = conventionalCandidateRepository.findByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                    vendorReferenceDataDto.setFatherName(byConventionalRequestId.getFatherName());
                    vendorReferenceDataDto.setGender(byConventionalRequestId.getGender());
                    vendorReferenceDataDto.setCandidateId(String.valueOf(vc.getCandidate().getConventionalCandidateId()));
                    vendorReferenceDataDto.setCheckName(vc.getSource().getSourceName());
                    vendorReferenceDataDto.setVendorReferenceData(candidateCafExperienceDtos);
                    vendorReferenceDataDtos.add(vendorReferenceDataDto);
                }

            } else if (sourceName.contains("EDUCATION")) {
                Boolean aBoolean = conventionalCafCandidateEducationRepository.existsByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                if (true) {
                    List<ConventionalCandidateCafEducation> byConventionalCandidateId = conventionalCafCandidateEducationRepository.findByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                    ArrayList<ConventionalEducationDto> candidateCafEducationDtos = new ArrayList<>();
                    byConventionalCandidateId.forEach(data -> {
                        ConventionalEducationDto conventionalEducationDto = new ConventionalEducationDto();
                        conventionalEducationDto.setDegreeType(data.getDegreeType());
                        conventionalEducationDto.setEducationType(data.getEducationType());
                        conventionalEducationDto.setEndDate(dateFormater.format(data.getEndDate()));
                        conventionalEducationDto.setStartDate(dateFormater.format(data.getStartDate()));
                        conventionalEducationDto.setInsufficiecyRemarks(data.getInsufficiencyRemarks());
                        if (data.getIsSuspect() != null) {
                            conventionalEducationDto.setIsSuspect(data.getIsSuspect());
                        }
                        if (data.getReasonForSuspect() != null) {
                            conventionalEducationDto.setReasonForSuspect(data.getReasonForSuspect());
                        }
                        Optional<CandidateCafEducation> byId = candidateCafEducationRepository.findById(data.getCandidateCafEducationId());
                        if (byId.isPresent()) {
                            conventionalEducationDto.setSchoolOrCollegeName(byId.get().getSchoolOrCollegeName());
                            conventionalEducationDto.setQualificationName(byId.get().getQualificationType());
                            conventionalEducationDto.setBoardOrUniversityName(byId.get().getBoardOrUniversityName());
                        }
                        candidateCafEducationDtos.add(conventionalEducationDto);
                    });
                    VendorReferenceDataDto vendorReferenceDataDto = new VendorReferenceDataDto();
                    vendorReferenceDataDto.setContactNumber(vc.getCandidate().getContactNumber());
                    vendorReferenceDataDto.setDateOfBirth(vc.getCandidate().getDateOfBirth());
                    vendorReferenceDataDto.setEmailId(vc.getCandidate().getEmailId());
                    ConventionalCandidate byConventionalRequestId = conventionalCandidateRepository.findByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                    vendorReferenceDataDto.setFatherName(byConventionalRequestId.getFatherName());
                    vendorReferenceDataDto.setGender(byConventionalRequestId.getGender());
                    vendorReferenceDataDto.setCandidateId(String.valueOf(vc.getCandidate().getConventionalCandidateId()));
                    vendorReferenceDataDto.setCheckName(vc.getSource().getSourceName());
                    vendorReferenceDataDto.setVendorReferenceData(candidateCafEducationDtos);
                    vendorReferenceDataDtos.add(vendorReferenceDataDto);
                }


            } else if (sourceName.contains("ADDRESS")) {
                Boolean exists = conventionCafAddressRepository.existsByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                if (exists) {
                    List<ConventionalCafAddress> byConventionalCandidateId = conventionCafAddressRepository.findByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                    ArrayList<ConventionalAddressDto> conventionalAddressDtos = new ArrayList<>();

                    byConventionalCandidateId.forEach(data -> {
                        ConventionalAddressDto conventionalAddressDto = new ConventionalAddressDto();
                        conventionalAddressDto.setAddressType(data.getAddressType());
                        conventionalAddressDto.setInsufficiencyRemarks(data.getInsufficiencyRemarks());
                        conventionalAddressDto.setStayToDate(dateFormater.format(data.getStayToDate()));
                        conventionalAddressDto.setStayFromDate(dateFormater.format(data.getStayFromDate()));
                        conventionalAddressDto.setHouseType(data.getHouseType());
                        Optional<CandidateCafAddress> byId = candidateCafAddressRepository.findById(data.getCandidateCafAddressId());
                        if (byId.isPresent()) {
                            conventionalAddressDto.setPincode(String.valueOf(byId.get().getPinCode()));
                            conventionalAddressDto.setCandidateAddress(byId.get().getCandidateAddress());
                            conventionalAddressDto.setCity(byId.get().getCity());
                            conventionalAddressDto.setState(byId.get().getState());

                        }
                        conventionalAddressDtos.add(conventionalAddressDto);

                    });

                    VendorReferenceDataDto vendorReferenceDataDto = new VendorReferenceDataDto();
                    vendorReferenceDataDto.setContactNumber(vc.getCandidate().getContactNumber());
                    vendorReferenceDataDto.setDateOfBirth(vc.getCandidate().getDateOfBirth());
                    vendorReferenceDataDto.setEmailId(vc.getCandidate().getEmailId());
                    ConventionalCandidate byConventionalRequestId = conventionalCandidateRepository.findByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                    vendorReferenceDataDto.setFatherName(byConventionalRequestId.getFatherName());
                    vendorReferenceDataDto.setGender(byConventionalRequestId.getGender());
                    vendorReferenceDataDto.setCandidateId(String.valueOf(vc.getCandidate().getConventionalCandidateId()));
                    vendorReferenceDataDto.setCheckName(vc.getSource().getSourceName());
                    vendorReferenceDataDto.setVendorReferenceData(conventionalAddressDtos);
                    vendorReferenceDataDtos.add(vendorReferenceDataDto);
                }
            } else if (sourceName.contains("PAN") && !sourceName.contains("DRUG")) {
                List<CandidateIdItems> pancardList = candidateIdItemsRepository.findByRequestIdAndIdType(String.valueOf(vc.getCandidate().getConventionalRequestId()), "PANCARD");
                if (pancardList.isEmpty() == false) {
                    ArrayList<ConventionalPanDto> conventionalPanDtos = new ArrayList<>();
                    pancardList.forEach(data -> {
                        ConventionalPanDto conventionalPanDto = new ConventionalPanDto();
                        conventionalPanDto.setNameInPan(data.getIdHolderName());
                        conventionalPanDto.setPanNumber(data.getIdNumber());
                        conventionalPanDtos.add(conventionalPanDto);
                    });
                    VendorReferenceDataDto vendorReferenceDataDto = new VendorReferenceDataDto();
                    vendorReferenceDataDto.setContactNumber(vc.getCandidate().getContactNumber());
                    vendorReferenceDataDto.setDateOfBirth(vc.getCandidate().getDateOfBirth());
                    vendorReferenceDataDto.setEmailId(vc.getCandidate().getEmailId());
                    ConventionalCandidate byConventionalRequestId = conventionalCandidateRepository.findByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                    vendorReferenceDataDto.setFatherName(byConventionalRequestId.getFatherName());
                    vendorReferenceDataDto.setGender(byConventionalRequestId.getGender());
                    vendorReferenceDataDto.setCandidateId(String.valueOf(vc.getCandidate().getConventionalCandidateId()));
                    vendorReferenceDataDto.setCheckName(vc.getSource().getSourceName());
                    vendorReferenceDataDto.setVendorReferenceData(conventionalPanDtos);
                    vendorReferenceDataDtos.add(vendorReferenceDataDto);
                }
            } else if (sourceName.contains("PASSPORT")) {
                List<CandidateIdItems> passportList = candidateIdItemsRepository.findByRequestIdAndIdType(String.valueOf(vc.getCandidate().getConventionalRequestId()), "PASSPORT");
                if (passportList.isEmpty() == false) {
                    ArrayList<ConventionalPassportDto> conventionalPassportDto = new ArrayList<>();
                    passportList.forEach(data -> {
                        ConventionalPassportDto convetionalPassportDto = new ConventionalPassportDto();
                        convetionalPassportDto.setPassportNumber(data.getIdNumber());
                        convetionalPassportDto.setNameInPassport(data.getIdHolderName());
                        convetionalPassportDto.setDateOfBirthInPassport(data.getIdHolderDob());
                        convetionalPassportDto.setCountry(data.getCountry());
                        convetionalPassportDto.setDateOfIssue(data.getIdHolderIssueDate());
                        convetionalPassportDto.setExpiryDate(data.getExpiryDate());
                        conventionalPassportDto.add(convetionalPassportDto);
                    });
                    VendorReferenceDataDto vendorReferenceDataDto = new VendorReferenceDataDto();
                    vendorReferenceDataDto.setContactNumber(vc.getCandidate().getContactNumber());
                    vendorReferenceDataDto.setDateOfBirth(vc.getCandidate().getDateOfBirth());
                    vendorReferenceDataDto.setEmailId(vc.getCandidate().getEmailId());
                    ConventionalCandidate byConventionalRequestId = conventionalCandidateRepository.findByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                    vendorReferenceDataDto.setFatherName(byConventionalRequestId.getFatherName());
                    vendorReferenceDataDto.setGender(byConventionalRequestId.getGender());
                    vendorReferenceDataDto.setCandidateId(String.valueOf(vc.getCandidate().getConventionalCandidateId()));
                    vendorReferenceDataDto.setCheckName(vc.getSource().getSourceName());
                    vendorReferenceDataDto.setVendorReferenceData(conventionalPassportDto);
                    vendorReferenceDataDtos.add(vendorReferenceDataDto);
                }
            } else if (sourceName.contains("VOTER")) {
                List<CandidateIdItems> voteridList = candidateIdItemsRepository.findByRequestIdAndIdType(String.valueOf(vc.getCandidate().getConventionalRequestId()), "VOTERID");
                if (voteridList.isEmpty() == false) {
                    ArrayList<ConventionalVoterIdDto> conventionalVoterIdDtos = new ArrayList<>();
                    voteridList.forEach(data -> {
                        ConventionalVoterIdDto conventionalVoterIdDto = new ConventionalVoterIdDto();
                        conventionalVoterIdDto.setDobInVoterId(data.getIdHolderDob());
                        conventionalVoterIdDto.setVoterIdNumber(data.getIdNumber());
                        conventionalVoterIdDtos.add(conventionalVoterIdDto);
                    });
                    VendorReferenceDataDto vendorReferenceDataDto = new VendorReferenceDataDto();
                    vendorReferenceDataDto.setContactNumber(vc.getCandidate().getContactNumber());
                    vendorReferenceDataDto.setDateOfBirth(vc.getCandidate().getDateOfBirth());
                    vendorReferenceDataDto.setEmailId(vc.getCandidate().getEmailId());
                    ConventionalCandidate byConventionalRequestId = conventionalCandidateRepository.findByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                    vendorReferenceDataDto.setFatherName(byConventionalRequestId.getFatherName());
                    vendorReferenceDataDto.setGender(byConventionalRequestId.getGender());
                    vendorReferenceDataDto.setCandidateId(String.valueOf(vc.getCandidate().getConventionalCandidateId()));
                    vendorReferenceDataDto.setCheckName(vc.getSource().getSourceName());
                    vendorReferenceDataDto.setVendorReferenceData(conventionalVoterIdDtos);
                    vendorReferenceDataDtos.add(vendorReferenceDataDto);
                }
            } else if (sourceName.contains("DRIVING")) {
                List<CandidateIdItems> drivinglicenceList = candidateIdItemsRepository.findByRequestIdAndIdType(String.valueOf(vc.getCandidate().getConventionalRequestId()), "DRIVINGLICENCE");
                if (drivinglicenceList.isEmpty() == false) {
                    ArrayList<ConventionalDrivingLicenseDto> conventionalDrivingLicenseDtos = new ArrayList<>();
                    drivinglicenceList.forEach(data -> {
                        ConventionalDrivingLicenseDto conventionalDrivingLicenseDto = new ConventionalDrivingLicenseDto();
                        conventionalDrivingLicenseDto.setDateOfIssue(data.getIdHolderIssueDate());
                        conventionalDrivingLicenseDto.setDrivingLicenseNumber(data.getIdNumber());
                        conventionalDrivingLicenseDtos.add(conventionalDrivingLicenseDto);
                    });
                    VendorReferenceDataDto vendorReferenceDataDto = new VendorReferenceDataDto();
                    vendorReferenceDataDto.setContactNumber(vc.getCandidate().getContactNumber());
                    vendorReferenceDataDto.setDateOfBirth(vc.getCandidate().getDateOfBirth());
                    vendorReferenceDataDto.setEmailId(vc.getCandidate().getEmailId());
                    ConventionalCandidate byConventionalRequestId = conventionalCandidateRepository.findByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                    vendorReferenceDataDto.setFatherName(byConventionalRequestId.getFatherName());
                    vendorReferenceDataDto.setGender(byConventionalRequestId.getGender());
                    vendorReferenceDataDto.setCandidateId(String.valueOf(vc.getCandidate().getConventionalCandidateId()));
                    vendorReferenceDataDto.setCheckName(vc.getSource().getSourceName());
                    vendorReferenceDataDto.setVendorReferenceData(conventionalDrivingLicenseDtos);
                    vendorReferenceDataDtos.add(vendorReferenceDataDto);
                }
            } else if (sourceName.contains("AADHAR")) {
                List<CandidateIdItems> passportList = candidateIdItemsRepository.findByRequestIdAndIdType(String.valueOf(vc.getCandidate().getConventionalRequestId()), "AADHARCARD");
                if (passportList.isEmpty() == false) {
                    ArrayList<ConventionalAadharDto> conventionalAadharDtos = new ArrayList<>();
                    passportList.forEach(data -> {
                        ConventionalAadharDto conventionalAadharDto = new ConventionalAadharDto();
                        conventionalAadharDto.setAadharNumber(data.getIdNumber());
                        conventionalAadharDto.setNameInAadhar(data.getIdHolderName());
                        conventionalAadharDto.setDobInAadhar(data.getIdHolderDob());
                        conventionalAadharDtos.add(conventionalAadharDto);
                    });
                    VendorReferenceDataDto vendorReferenceDataDto = new VendorReferenceDataDto();
                    vendorReferenceDataDto.setContactNumber(vc.getCandidate().getContactNumber());
                    vendorReferenceDataDto.setDateOfBirth(vc.getCandidate().getDateOfBirth());
                    vendorReferenceDataDto.setEmailId(vc.getCandidate().getEmailId());
                    ConventionalCandidate byConventionalRequestId = conventionalCandidateRepository.findByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                    vendorReferenceDataDto.setFatherName(byConventionalRequestId.getFatherName());
                    vendorReferenceDataDto.setGender(byConventionalRequestId.getGender());
                    vendorReferenceDataDto.setCandidateId(String.valueOf(vc.getCandidate().getConventionalCandidateId()));
                    vendorReferenceDataDto.setCheckName(vc.getSource().getSourceName());
                    vendorReferenceDataDto.setVendorReferenceData(conventionalAadharDtos);
                    vendorReferenceDataDtos.add(vendorReferenceDataDto);
                }
            } else if (sourceName.contains("DRUG")) {
                Boolean exists = conventionalCandidateDrugInfoRepository.existsByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                if (exists) {
                    List<ConventionalCandidateDrugInfo> byConventionalDrugCheck = conventionalCandidateDrugInfoRepository.findByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                    ArrayList<ConventionalCandidateDrugInfo> conventionald = new ArrayList<>();
                    ArrayList<DrugInfoDto> drugInfoDtos = new ArrayList<>();
                    if (byConventionalDrugCheck.isEmpty() == false) {
                        byConventionalDrugCheck.forEach(drugInfo -> {
                            if (drugInfo != null) { // Ensure the entity is not null
                                DrugInfoDto drugInfoDto = new DrugInfoDto();
                                drugInfoDto.setName(drugInfo.getName());
                                drugInfoDto.setContactNumber(drugInfo.getContactNumber());
                                drugInfoDto.setSampleCollectionDate(drugInfo.getSampleCollectionDate() != null
                                        ? dateFormater.format(drugInfo.getSampleCollectionDate()) : null);
                                drugInfoDto.setRemarks(drugInfo.getRemarks());
                                drugInfoDto.setHouseNumber(drugInfo.getHouseNumber());
                                drugInfoDto.setStreetAddress(drugInfo.getStreetAddress());
                                drugInfoDto.setCity(drugInfo.getCity());
                                drugInfoDto.setState(drugInfo.getState());
                                drugInfoDto.setCountry(drugInfo.getCountry());
                                drugInfoDto.setPincode(drugInfo.getPincode());
                                drugInfoDto.setProminentLandmark(drugInfo.getProminentLandmark());
                                drugInfoDtos.add(drugInfoDto);
                            }
                        });
                    }
                    VendorReferenceDataDto vendorReferenceDataDto = new VendorReferenceDataDto();
                    vendorReferenceDataDto.setContactNumber(vc.getCandidate().getContactNumber());
                    vendorReferenceDataDto.setDateOfBirth(vc.getCandidate().getDateOfBirth());
                    vendorReferenceDataDto.setEmailId(vc.getCandidate().getEmailId());
                    ConventionalCandidate byConventionalRequestId = conventionalCandidateRepository.findByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                    vendorReferenceDataDto.setFatherName(byConventionalRequestId.getFatherName());
                    vendorReferenceDataDto.setGender(byConventionalRequestId.getGender());
                    vendorReferenceDataDto.setCandidateId(String.valueOf(vc.getCandidate().getConventionalCandidateId()));
                    vendorReferenceDataDto.setCheckName(vc.getSource().getSourceName());
                    vendorReferenceDataDto.setVendorReferenceData(drugInfoDtos);
                    vendorReferenceDataDtos.add(vendorReferenceDataDto);
                }
            } else if (sourceName.contains("Certificate ")) {
                Boolean exists = conventionalCandidateCertificateInfoRepository.existsByRequestId(vc.getCandidate().getConventionalRequestId());
                if (exists) {
                    List<ConventionalCandidateCertificateInfo> certificateInfoList = conventionalCandidateCertificateInfoRepository.findByRequestId(vc.getCandidate().getConventionalRequestId());
                    ArrayList<ConventionalCandidateCertificateInfo> conventionald = new ArrayList<>();
                    ArrayList<ConventionalCertificateInfoDto> certificateInfoDtos = new ArrayList<>();
                    if (certificateInfoList.isEmpty() == false) {
                        certificateInfoList.forEach(certificateInfo -> {
                            if (certificateInfo != null) { // Ensure the entity is not null
                                ConventionalCertificateInfoDto certificateInfoDto = new ConventionalCertificateInfoDto();
                                certificateInfoDto.setCourseName(certificateInfo.getCourseName());
                                certificateInfoDto.setCourseStartDate(certificateInfo.getCourseStartDate());
                                certificateInfoDto.setCourseCompletionDate(certificateInfo.getCourseCompletionDate());
                                certificateInfoDto.setInstituteName(certificateInfo.getInstituteName());
                                certificateInfoDto.setInstituteEmailID(certificateInfo.getInstituteEmailID());
                                certificateInfoDto.setInstituteContactNumber(certificateInfo.getInstituteContactNumber());
                                certificateInfoDtos.add(certificateInfoDto);
                            }
                        });
                    }
                    VendorReferenceDataDto vendorReferenceDataDto = new VendorReferenceDataDto();
                    vendorReferenceDataDto.setContactNumber(vc.getCandidate().getContactNumber());
                    vendorReferenceDataDto.setDateOfBirth(vc.getCandidate().getDateOfBirth());
                    vendorReferenceDataDto.setEmailId(vc.getCandidate().getEmailId());
                    ConventionalCandidate byConventionalRequestId = conventionalCandidateRepository.findByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                    vendorReferenceDataDto.setFatherName(byConventionalRequestId.getFatherName());
                    vendorReferenceDataDto.setGender(byConventionalRequestId.getGender());
                    vendorReferenceDataDto.setCandidateId(String.valueOf(vc.getCandidate().getConventionalCandidateId()));
                    vendorReferenceDataDto.setCheckName(vc.getSource().getSourceName());
                    vendorReferenceDataDto.setVendorReferenceData(certificateInfoDtos);
                    vendorReferenceDataDtos.add(vendorReferenceDataDto);
                }
            } else {
                log.debug("NO Reference Data Found For  this Candidate");
                VendorReferenceDataDto vendorReferenceDataDto = new VendorReferenceDataDto();
                vendorReferenceDataDto.setContactNumber(vc.getCandidate().getContactNumber());
                vendorReferenceDataDto.setDateOfBirth(vc.getCandidate().getDateOfBirth());
                vendorReferenceDataDto.setEmailId(vc.getCandidate().getEmailId());
                ConventionalCandidate byConventionalRequestId = conventionalCandidateRepository.findByConventionalRequestId(vc.getCandidate().getConventionalRequestId());
                vendorReferenceDataDto.setFatherName(byConventionalRequestId.getFatherName());
                vendorReferenceDataDto.setGender(byConventionalRequestId.getGender());
                vendorReferenceDataDto.setCandidateId(String.valueOf(vc.getCandidate().getConventionalCandidateId()));
                vendorReferenceDataDto.setCheckName(vc.getSource().getSourceName());
                vendorReferenceDataDto.setVendorReferenceData(null);
                vendorReferenceDataDtos.add(vendorReferenceDataDto);

            }
//


            serviceOutcome.setData(vendorReferenceDataDtos);
            serviceOutcome.setMessage("fetched response sucessfully");
            serviceOutcome.setOutcome(true);
            List<VendorReferenceDataDto> vendorData = serviceOutcome.getData();
            File nanda = FileUtil.createUniqueTempFile("nanda", ".xlsx");
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Data");
            int rowNum = 0;
            CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = sheet.getWorkbook().createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Candidate ID");
            headerRow.getCell(0).setCellStyle(headerStyle);
            headerRow.createCell(1).setCellValue("Check Name");
            headerRow.getCell(1).setCellStyle(headerStyle);
            headerRow.createCell(2).setCellValue("Date Of Birth");
            headerRow.getCell(2).setCellStyle(headerStyle);
            headerRow.createCell(3).setCellValue("Father Name");
            headerRow.getCell(3).setCellStyle(headerStyle);
            headerRow.createCell(4).setCellValue("Gender");
            headerRow.getCell(4).setCellStyle(headerStyle);
            headerRow.createCell(5).setCellValue("Contact Number");
            headerRow.getCell(5).setCellStyle(headerStyle);
            headerRow.createCell(6).setCellValue("Email Id");
            headerRow.getCell(6).setCellStyle(headerStyle);
            for (VendorReferenceDataDto dto : vendorReferenceDataDtos) {
                List vendorReferenceData = dto.getVendorReferenceData();
                if (vendorReferenceData != null) {
                    for (int i = 0; i < vendorReferenceData.size(); i++) {
                        if (vendorReferenceData.get(i) instanceof ConventionalAddressDto) {
                            ConventionalAddressDto conventionalAddressDto = (ConventionalAddressDto) vendorReferenceData.get(i);
                            // Assuming you want to set the field names of ConventionalAddressDto as headers
                            Field[] fields = ConventionalAddressDto.class.getDeclaredFields();
                            for (int j = 0; j < fields.length; j++) {
                                String fieldName = fields[j].getName();
                                headerRow.createCell(7 + j).setCellValue(fieldName);
                                headerRow.getCell(7 + j).setCellStyle(headerStyle);
                            }
                        } else if (vendorReferenceData.get(i) instanceof ConventionalEducationDto) {
                            ConventionalEducationDto conventionalEducationDto = (ConventionalEducationDto) vendorReferenceData.get(i);
                            // Assuming you want to set the field names of ConventionalAddressDto as headers
                            Field[] fields = ConventionalEducationDto.class.getDeclaredFields();
                            for (int j = 0; j < fields.length; j++) {
                                String fieldName = fields[j].getName();
                                headerRow.createCell(7 + j).setCellValue(fieldName);
                                headerRow.getCell(7 + j).setCellStyle(headerStyle);
                            }
                        } else if (vendorReferenceData.get(i) instanceof ConventionalExperienceDto) {
                            ConventionalExperienceDto conventionalExperienceDto = (ConventionalExperienceDto) vendorReferenceData.get(i);
                            // Assuming you want to set the field names of ConventionalAddressDto as headers
                            Field[] fields = ConventionalExperienceDto.class.getDeclaredFields();
                            for (int j = 0; j < fields.length; j++) {
                                String fieldName = fields[j].getName();
                                headerRow.createCell(7 + j).setCellValue(fieldName);
                                headerRow.getCell(7 + j).setCellStyle(headerStyle);
                            }
                        } else if (vendorReferenceData.get(i) instanceof ConventionalCandidateReferenceDto) {
                            ConventionalCandidateReferenceDto conventionalCandidateReferenceDto = (ConventionalCandidateReferenceDto) vendorReferenceData.get(i);
                            // Assuming you want to set the field names of ConventionalAddressDto as headers
                            Field[] fields = ConventionalCandidateReferenceDto.class.getDeclaredFields();
                            for (int j = 0; j < fields.length; j++) {
                                String fieldName = fields[j].getName();
                                headerRow.createCell(7 + j).setCellValue(fieldName);
                                headerRow.getCell(7 + j).setCellStyle(headerStyle);
                            }
                        } else if (vendorReferenceData.get(i) instanceof ConventionalPanDto) {
                            ConventionalPanDto conventionalPanDto = (ConventionalPanDto) vendorReferenceData.get(i);
                            // Assuming you want to set the field names of ConventionalAddressDto as headers
                            Field[] fields = ConventionalPanDto.class.getDeclaredFields();
                            for (int j = 0; j < fields.length; j++) {
                                String fieldName = fields[j].getName();
                                headerRow.createCell(7 + j).setCellValue(fieldName);
                                headerRow.getCell(7 + j).setCellStyle(headerStyle);
                            }
                        } else if (vendorReferenceData.get(i) instanceof ConventionalPassportDto) {
                            ConventionalPassportDto conventionalPassportDto = (ConventionalPassportDto) vendorReferenceData.get(i);
                            // Assuming you want to set the field names of ConventionalAddressDto as headers
                            Field[] fields = ConventionalPassportDto.class.getDeclaredFields();
                            for (int j = 0; j < fields.length; j++) {
                                String fieldName = fields[j].getName();
                                headerRow.createCell(7 + j).setCellValue(fieldName);
                                headerRow.getCell(7 + j).setCellStyle(headerStyle);
                            }
                        } else if (vendorReferenceData.get(i) instanceof ConventionalVoterIdDto) {
                            ConventionalVoterIdDto conventionalVoterIdDto = (ConventionalVoterIdDto) vendorReferenceData.get(i);
                            // Assuming you want to set the field names of ConventionalAddressDto as headers
                            Field[] fields = ConventionalVoterIdDto.class.getDeclaredFields();
                            for (int j = 0; j < fields.length; j++) {
                                String fieldName = fields[j].getName();
                                headerRow.createCell(7 + j).setCellValue(fieldName);
                                headerRow.getCell(7 + j).setCellStyle(headerStyle);
                            }
                        } else if (vendorReferenceData.get(i) instanceof ConventionalDrivingLicenseDto) {
                            ConventionalDrivingLicenseDto conventionalDrivingLicenseDto = (ConventionalDrivingLicenseDto) vendorReferenceData.get(i);
                            // Assuming you want to set the field names of ConventionalAddressDto as headers
                            Field[] fields = ConventionalDrivingLicenseDto.class.getDeclaredFields();
                            for (int j = 0; j < fields.length; j++) {
                                String fieldName = fields[j].getName();
                                headerRow.createCell(7 + j).setCellValue(fieldName);
                                headerRow.getCell(7 + j).setCellStyle(headerStyle);
                            }
                        } else if (vendorReferenceData.get(i) instanceof ConventionalAadharDto) {
                            ConventionalAadharDto conventionalAadharDto = (ConventionalAadharDto) vendorReferenceData.get(i);
                            // Assuming you want to set the field names of ConventionalAddressDto as headers
                            Field[] fields = ConventionalAadharDto.class.getDeclaredFields();
                            for (int j = 0; j < fields.length; j++) {
                                String fieldName = fields[j].getName();
                                headerRow.createCell(7 + j).setCellValue(fieldName);
                                headerRow.getCell(7 + j).setCellStyle(headerStyle);
                            }
                        } else if (vendorReferenceData.get(i) instanceof DrugInfoDto) {
                            DrugInfoDto drugInfoDto = (DrugInfoDto) vendorReferenceData.get(i);
                            // Assuming you want to set the field names of ConventionalAddressDto as headers
                            Field[] fields = DrugInfoDto.class.getDeclaredFields();
                            for (int j = 0; j < fields.length; j++) {
                                String fieldName = fields[j].getName();
                                headerRow.createCell(7 + j).setCellValue(fieldName);
                                headerRow.getCell(7 + j).setCellStyle(headerStyle);
                            }
                        } else if (vendorReferenceData.get(i) instanceof ConventionalCertificateInfoDto) {
                            ConventionalCertificateInfoDto certificateInfoDto = (ConventionalCertificateInfoDto) vendorReferenceData.get(i);
                            // Assuming you want to set the field names of ConventionalAddressDto as headers
                            Field[] fields = ConventionalCertificateInfoDto.class.getDeclaredFields();
                            for (int j = 0; j < fields.length; j++) {
                                String fieldName = fields[j].getName();
                                headerRow.createCell(7 + j).setCellValue(fieldName);
                                headerRow.getCell(7 + j).setCellStyle(headerStyle);
                            }
                        } else {
                            headerRow.createCell(7).setCellValue("Vendor Reference Data");
                            headerRow.getCell(7).setCellStyle(headerStyle);
                        }

                    }
                } else {
                    headerRow.createCell(7).setCellValue("VendorReference Data");
                    headerRow.getCell(7).setCellStyle(headerStyle);
                }
            }
//            sheet.setColumnWidth(0, 15 * 756); // 15 characters
//            sheet.setColumnWidth(1, 70 * 756); // 70 characters
            for (int j = 7; j < headerRow.getLastCellNum(); j++) {
                sheet.setColumnWidth(j, 15 * 756); // 15 characters for other columns
            }
            int datarowNo = 1;
            for (VendorReferenceDataDto dto : vendorReferenceDataDtos) {
                Row dataRow = null;
                if (dto.getVendorReferenceData() != null) {
                    List vendorReferenceData = dto.getVendorReferenceData();
                    if (vendorReferenceData != null) {
                        for (int i = 0; i < vendorReferenceData.size(); i++) {
                            dataRow = sheet.createRow(datarowNo);
                            dataRow.createCell(0).setCellValue(dto.getCandidateId());
                            dataRow.createCell(1).setCellValue(dto.getCheckName());
                            dataRow.createCell(2).setCellValue(dto.getDateOfBirth());
                            dataRow.createCell(3).setCellValue(dto.getFatherName());
                            dataRow.createCell(4).setCellValue(dto.getGender());
                            dataRow.createCell(5).setCellValue(dto.getContactNumber());
                            dataRow.createCell(6).setCellValue(dto.getEmailId());
                            datarowNo = datarowNo + 1;
                            System.out.println("datarow" + datarowNo);
                            if (vendorReferenceData.get(i) instanceof ConventionalAddressDto) {
                                ConventionalAddressDto conventionalAddressDto = (ConventionalAddressDto) vendorReferenceData.get(i);
                                // Assuming you want to set the field names of ConventionalAddressDto as headers
                                Field[] fields = ConventionalAddressDto.class.getDeclaredFields();
                                for (int j = 0; j < fields.length; j++) {
                                    Field field = fields[j];
                                    String fieldName = field.getName();
                                    // Use reflection to get the field's value from the dto
                                    try {
                                        field.setAccessible(true); // Allow access to private fields
                                        Object value = field.get(conventionalAddressDto);
                                        String valueAsString = (value != null) ? value.toString() : "";
                                        dataRow.createCell(7 + j).setCellValue(valueAsString);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }

                            } else if (vendorReferenceData.get(i) instanceof ConventionalEducationDto) {
                                ConventionalEducationDto conventionalEducationDto = (ConventionalEducationDto) vendorReferenceData.get(i);
                                // Assuming you want to set the field names of ConventionalAddressDto as headers
                                Field[] fields = ConventionalEducationDto.class.getDeclaredFields();
                                for (int j = 0; j < fields.length; j++) {
                                    Field field = fields[j];
                                    String fieldName = field.getName();
                                    // Use reflection to get the field's value from the dto
                                    try {
                                        field.setAccessible(true); // Allow access to private fields
                                        Object value = field.get(conventionalEducationDto);
                                        String valueAsString = (value != null) ? value.toString() : "";
                                        dataRow.createCell(7 + j).setCellValue(valueAsString);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }

                            } else if (vendorReferenceData.get(i) instanceof ConventionalExperienceDto) {
                                ConventionalExperienceDto conventionalExperienceDto = (ConventionalExperienceDto) vendorReferenceData.get(i);
                                // Assuming you want to set the field names of ConventionalAddressDto as headers
                                Field[] fields = ConventionalExperienceDto.class.getDeclaredFields();
                                for (int j = 0; j < fields.length; j++) {
                                    Field field = fields[j];
                                    String fieldName = field.getName();
                                    // Use reflection to get the field's value from the dto
                                    try {
                                        field.setAccessible(true); // Allow access to private fields
                                        Object value = field.get(conventionalExperienceDto);
                                        String valueAsString = (value != null) ? value.toString() : "";
                                        dataRow.createCell(7 + j).setCellValue(valueAsString);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }

                            } else if (vendorReferenceData.get(i) instanceof ConventionalCandidateReferenceDto) {
                                ConventionalCandidateReferenceDto conventionalCandidateReferenceDto = (ConventionalCandidateReferenceDto) vendorReferenceData.get(i);
                                // Assuming you want to set the field names of ConventionalAddressDto as headers
                                Field[] fields = ConventionalCandidateReferenceDto.class.getDeclaredFields();
                                for (int j = 0; j < fields.length; j++) {
                                    Field field = fields[j];
                                    String fieldName = field.getName();
                                    // Use reflection to get the field's value from the dto
                                    try {
                                        field.setAccessible(true); // Allow access to private fields
                                        Object value = field.get(conventionalCandidateReferenceDto);
                                        String valueAsString = (value != null) ? value.toString() : "";
                                        dataRow.createCell(7 + j).setCellValue(valueAsString);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }

                            } else if (vendorReferenceData.get(i) instanceof ConventionalPanDto) {
                                ConventionalPanDto conventionalPanDto = (ConventionalPanDto) vendorReferenceData.get(i);
                                // Assuming you want to set the field names of ConventionalAddressDto as headers
                                Field[] fields = ConventionalPanDto.class.getDeclaredFields();
                                for (int j = 0; j < fields.length; j++) {
                                    Field field = fields[j];
                                    String fieldName = field.getName();
                                    // Use reflection to get the field's value from the dto
                                    try {
                                        field.setAccessible(true); // Allow access to private fields
                                        Object value = field.get(conventionalPanDto);
                                        String valueAsString = (value != null) ? value.toString() : "";
                                        dataRow.createCell(7 + j).setCellValue(valueAsString);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }

                            } else if (vendorReferenceData.get(i) instanceof ConventionalPassportDto) {
                                ConventionalPassportDto conventionalPassportDto = (ConventionalPassportDto) vendorReferenceData.get(i);
                                // Assuming you want to set the field names of ConventionalAddressDto as headers
                                Field[] fields = ConventionalPassportDto.class.getDeclaredFields();
                                for (int j = 0; j < fields.length; j++) {
                                    Field field = fields[j];
                                    String fieldName = field.getName();
                                    // Use reflection to get the field's value from the dto
                                    try {
                                        field.setAccessible(true); // Allow access to private fields
                                        Object value = field.get(conventionalPassportDto);
                                        String valueAsString = (value != null) ? value.toString() : "";
                                        dataRow.createCell(7 + j).setCellValue(valueAsString);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }

                            } else if (vendorReferenceData.get(i) instanceof ConventionalVoterIdDto) {
                                ConventionalVoterIdDto conventionalVoterIdDto = (ConventionalVoterIdDto) vendorReferenceData.get(i);
                                // Assuming you want to set the field names of ConventionalAddressDto as headers
                                Field[] fields = ConventionalVoterIdDto.class.getDeclaredFields();
                                for (int j = 0; j < fields.length; j++) {
                                    Field field = fields[j];
                                    String fieldName = field.getName();
                                    // Use reflection to get the field's value from the dto
                                    try {
                                        field.setAccessible(true); // Allow access to private fields
                                        Object value = field.get(conventionalVoterIdDto);
                                        String valueAsString = (value != null) ? value.toString() : "";
                                        dataRow.createCell(7 + j).setCellValue(valueAsString);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }

                            } else if (vendorReferenceData.get(i) instanceof ConventionalDrivingLicenseDto) {
                                ConventionalDrivingLicenseDto conventionalDrivingLicenseDto = (ConventionalDrivingLicenseDto) vendorReferenceData.get(i);
                                // Assuming you want to set the field names of ConventionalAddressDto as headers
                                Field[] fields = ConventionalDrivingLicenseDto.class.getDeclaredFields();
                                for (int j = 0; j < fields.length; j++) {
                                    Field field = fields[j];
                                    String fieldName = field.getName();
                                    // Use reflection to get the field's value from the dto
                                    try {
                                        field.setAccessible(true); // Allow access to private fields
                                        Object value = field.get(conventionalDrivingLicenseDto);
                                        String valueAsString = (value != null) ? value.toString() : "";
                                        dataRow.createCell(7 + j).setCellValue(valueAsString);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }

                            } else if (vendorReferenceData.get(i) instanceof ConventionalAadharDto) {
                                ConventionalAadharDto conventionalAadharDto = (ConventionalAadharDto) vendorReferenceData.get(i);
                                // Assuming you want to set the field names of ConventionalAddressDto as headers
                                Field[] fields = ConventionalAadharDto.class.getDeclaredFields();
                                for (int j = 0; j < fields.length; j++) {
                                    Field field = fields[j];
                                    String fieldName = field.getName();
                                    // Use reflection to get the field's value from the dto
                                    try {
                                        field.setAccessible(true); // Allow access to private fields
                                        Object value = field.get(conventionalAadharDto);
                                        String valueAsString = (value != null) ? value.toString() : "";
                                        dataRow.createCell(7 + j).setCellValue(valueAsString);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }

                            } else if (vendorReferenceData.get(i) instanceof DrugInfoDto) {
                                DrugInfoDto drugInfoDto = (DrugInfoDto) vendorReferenceData.get(i);
                                // Assuming you want to set the field names of ConventionalAddressDto as headers
                                Field[] fields = DrugInfoDto.class.getDeclaredFields();
                                for (int j = 0; j < fields.length; j++) {
                                    Field field = fields[j];
                                    String fieldName = field.getName();
                                    // Use reflection to get the field's value from the dto
                                    try {
                                        field.setAccessible(true); // Allow access to private fields
                                        Object value = field.get(drugInfoDto);
                                        String valueAsString = (value != null) ? value.toString() : "";
                                        dataRow.createCell(7 + j).setCellValue(valueAsString);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }

                            } else if (vendorReferenceData.get(i) instanceof ConventionalCertificateInfoDto) {
                                ConventionalCertificateInfoDto certificateInfoDto = (ConventionalCertificateInfoDto) vendorReferenceData.get(i);
                                // Assuming you want to set the field names of ConventionalAddressDto as headers
                                Field[] fields = ConventionalCertificateInfoDto.class.getDeclaredFields();
                                for (int j = 0; j < fields.length; j++) {
                                    Field field = fields[j];
                                    String fieldName = field.getName();
                                    // Use reflection to get the field's value from the dto
                                    try {
                                        field.setAccessible(true); // Allow access to private fields
                                        Object value = field.get(certificateInfoDto);
                                        String valueAsString = (value != null) ? value.toString() : "";
                                        dataRow.createCell(7 + j).setCellValue(valueAsString);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }

                            } else {
                                dataRow.createCell(2).setCellValue("null");
                            }
                        }
                    }
                } else {
                    dataRow = sheet.createRow(datarowNo);
                    dataRow.createCell(0).setCellValue(dto.getCandidateId());
                    dataRow.createCell(1).setCellValue(dto.getCheckName());
                    dataRow.createCell(2).setCellValue(dto.getDateOfBirth());
                    dataRow.createCell(3).setCellValue(dto.getFatherName());
                    dataRow.createCell(4).setCellValue(dto.getGender());
                    dataRow.createCell(5).setCellValue(dto.getContactNumber());
                    dataRow.createCell(6).setCellValue(dto.getEmailId());
                    dataRow.createCell(7).setCellValue("NO DATA");
                }
                for (int j = 7; j < dataRow.getLastCellNum(); j++) {
                    sheet.setColumnWidth(j, 15 * 256); // 15 characters for other columns
                }
            }
            FileOutputStream fileOutputStream = new FileOutputStream(nanda);
            workbook.write(fileOutputStream);
            byte[] fileContent = Files.readAllBytes(Paths.get(nanda.getAbsolutePath()));
            String base64String = Base64.getEncoder().encodeToString(fileContent);
            serviceOutcome.setMessage(base64String);


        } catch (Exception e) {
            log.info(e.getMessage());
            serviceOutcome.setOutcome(false);

            serviceOutcome.setMessage(e.getMessage());
        }


        return serviceOutcome;
    }

    //    @Transactional

    public ServiceOutcome<byte[]> generateConventionalUtilizationReport() throws Exception {
        ServiceOutcome<byte[]> serviceOutcome = new ServiceOutcome<>();
        List<ReportUtilizationDto> reportUtilizationDtos = new ArrayList<>();

        try {
            File nanda = FileUtil.createUniqueTempFile("utilreport", ".xlsx");
            Workbook workbook = new XSSFWorkbook();
            List<Source> allSources = sourceRepository.findAll();
            allSources = allSources.stream().filter(p -> p.getSourceId() != 3l).collect(Collectors.toList());
            for (Source source : allSources) {
                source.setSourceName(source.getSourceName().replace("PROFFESSIONAL", "PROF").replace("REFERENCE", "REF"));
                String modifiedSourceName = source.getSourceName().replaceAll("[\\s-]+", "");
                List<ReportUtilizationVendorDto> allVendorCandidateAndSourceId = vendorChecksRepository.findAllVendorCandidateAndSourceId(source.getSourceId());
                CellStyle headerCellStyle = workbook.createCellStyle();
                headerCellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
                headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                CellStyle headerCellStyle2 = workbook.createCellStyle();
                headerCellStyle2.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                headerCellStyle2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                Sheet sheet = workbook.createSheet(source.getSourceName());
                log.debug("source name  :" + source.getSourceName());
                int rowno = 1;
                int datarowNo = 2;
                for (ReportUtilizationVendorDto reportUtilizationVendorDto : allVendorCandidateAndSourceId) {
                    Long candidateId = reportUtilizationVendorDto.getCandidateId();
                    Long vendorId = reportUtilizationVendorDto.getVendorId();

//                        if (sheetExists == false) {
                    sheet.setDefaultColumnWidth(25);
                    Row headerRow = sheet.createRow(rowno);
                    headerRow.createCell(0).setCellValue("Vendor Name");
                    headerRow.createCell(1).setCellValue("Case Initiated by (DigiVerifier Spoc))");
                    headerRow.createCell(2).setCellValue("URN / Ref No.");
                    headerRow.createCell(3).setCellValue("Candidate Name");
                    headerRow.createCell(4).setCellValue("Case Assigned Date");
                    headerRow.createCell(5).setCellValue("Report Status");
                    headerRow.createCell(6).setCellValue("Report Color Code");
                    for (int i = 0; i <= 6; i++) {
                        Cell cell = headerRow.getCell(i);
                        cell.setCellStyle(headerCellStyle);
                    }
                    headerRow.createCell(7).setCellValue("Detail Name");
                    headerRow.createCell(8).setCellValue("Qty");
                    headerRow.createCell(9).setCellValue("Price Per Unit");
                    headerRow.createCell(10).setCellValue("Color Code");
                    headerRow.createCell(11).setCellValue("Total Amount");
                    for (int i = 7; i <= 11; i++) {
                        Cell cell = headerRow.getCell(i);
                        cell.setCellStyle(headerCellStyle2);
                    }

                    List<VendorChecks> byCandidateIdANdVendorIdAndCandidateId = vendorChecksRepository.findByCandidateIdANdVendorIdAndCandidateId(vendorId, candidateId, source.getSourceId());
                    for (VendorChecks vendorChecks : byCandidateIdANdVendorIdAndCandidateId) {
                        datarowNo = datarowNo + 1;
                        Row dataRow = sheet.createRow(datarowNo);
                        User user = userRepository.findById(vendorId).get();
                        dataRow.createCell(0).setCellValue((user.getUserFirstName() != null) ? user.getUserFirstName() : "NA");
                        if (vendorChecks.getCreatedBy() != null) {
                            User caseInitatedBy = userRepository.findById(vendorChecks.getCreatedBy().getUserId()).get();
                            dataRow.createCell(1).setCellValue((String.valueOf(caseInitatedBy.getUserName()) != null) ? caseInitatedBy.getUserName() : "NA");
                        }
                        ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(String.valueOf(vendorChecks.getCandidate().getConventionalRequestId()));
                        dataRow.createCell(2).setCellValue((conventionalVendorCandidatesSubmitted != null) ? conventionalVendorCandidatesSubmitted.getApplicantId() : 0l);
                        dataRow.createCell(3).setCellValue((vendorChecks != null) ? conventionalVendorCandidatesSubmitted.getName() : "NA");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String formattedDate = dateFormat.format(vendorChecks.getCreatedOn());
                        dataRow.createCell(4).setCellValue((vendorChecks.getCreatedOn() != null) ? dateFormat.format(vendorChecks.getCreatedOn()) : dateFormat.format(new Date()));
                        CandidateVerificationState canidateVerificationData = candidateVerificationStateRepository.findByCandidateCandidateId(vendorChecks.getCandidate().getCandidateId());
                        dataRow.createCell(5).setCellValue((canidateVerificationData != null) ? String.valueOf(canidateVerificationData.getInterimReportTime()) : "NA");
                        dataRow.createCell(6).setCellValue((conventionalVendorCandidatesSubmitted != null) ? conventionalVendorCandidatesSubmitted.getStatus().getStatusName() : "NA");
                        dataRow.createCell(7).setCellValue((vendorChecks.getSource() != null) ? vendorChecks.getSource().getSourceName() : "NA");
                        dataRow.createCell(8).setCellValue((byCandidateIdANdVendorIdAndCandidateId != null) ? byCandidateIdANdVendorIdAndCandidateId.size() : 0l);
                        dataRow.createCell(10).setCellValue((vendorChecks.getVendorCheckStatusMaster() != null) ? vendorChecks.getVendorCheckStatusMaster().getCheckStatusCode() : "NA");
                        List<VendorMasterNew> byUserId = vendorMasterNewRepository.findByUserId(vendorId);
                        if (byUserId.isEmpty() == false) {
                            byUserId.forEach(data -> {
                                dataRow.createCell(9).setCellValue((data != null) ? data.getRatePerItem() : 4l);
                                if (byCandidateIdANdVendorIdAndCandidateId.isEmpty() == false) {
                                    dataRow.createCell(11).setCellValue((data != null) ? byCandidateIdANdVendorIdAndCandidateId.size() * data.getRatePerItem() : 0);
                                }
                            });
                        }

                    }
                }


            }


//            File nanda = FileUtil.createUniqueTempFile("nanda", ".xlsx");
//            FileOutputStream fileOutputStream = new FileOutputStream(nanda);
//            workbook.write(fileOutputStream);
//            byte[] fileContent = Files.readAllBytes(Paths.get(nanda.getAbsolutePath()));
//            System.out.println("absoulute oath" + nanda.getAbsolutePath());
//            System.out.println("" + nanda.getPath());
//            serviceOutcome.setMessage(nanda.getAbsolutePath());
            FileOutputStream fileOutputStream = new FileOutputStream(nanda);
            workbook.write(fileOutputStream);
            byte[] fileContent = Files.readAllBytes(Paths.get(nanda.getAbsolutePath()));
            String base64String = Base64.getEncoder().encodeToString(fileContent);
            serviceOutcome.setMessage(base64String);
        } catch (Exception e) {
            log.error("error in generate response  :" + e.getMessage());


        }
        return serviceOutcome;
    }

    public ServiceOutcome<List<ModeOfVerificationStatusMaster>> findAllModeOfVerifcationPerformed() throws Exception {
        ServiceOutcome<List<ModeOfVerificationStatusMaster>> listServiceOutcome = new ServiceOutcome<>();

        try {
            List<ModeOfVerificationStatusMaster> all = modeOfVerificationStatusMasterRepository.findAll();
            if (all.isEmpty()) {
                listServiceOutcome.setData(new ArrayList<>());
            }
            listServiceOutcome.setData(all);
            listServiceOutcome.setOutcome(true);
            listServiceOutcome.setStatus("200");


        } catch (Exception e) {
            log.info("LICheck to  perfom serviceImpl ::  findAllModeOfVerifcationPerformed" + e.getMessage());
        }
        return listServiceOutcome;
    }


    @Override
    public ServiceOutcome<ConventionalAttributesMaster> saveConventionalAttributesMaster(ConventionalAttributesMaster conventionalAttributesMaster) {
        ServiceOutcome<ConventionalAttributesMaster> svcSearchResult = new ServiceOutcome<ConventionalAttributesMaster>();
        try {
            ConventionalAttributesMaster save = conventionalAttributesMasterRepository.save(conventionalAttributesMaster);
            svcSearchResult.setData(save);
        } catch (Exception ex) {

            log.error("Exception occured in saveConventionalAttributesMaster method in userServiceImpl-->" + ex);
        }
        return svcSearchResult;


    }


    public ServiceOutcome<T> getConventionalAttributesMasterById(Long vendorCheckId, String type) {
        ServiceOutcome<T> svcSearchResult = new ServiceOutcome<T>();
        try {
            VendorUploadChecks byVendorChecksVendorcheckId = vendorUploadChecksRepository.findByVendorChecksVendorcheckId(vendorCheckId);
            if (!type.isBlank() && type.contains("NONE") && byVendorChecksVendorcheckId != null && byVendorChecksVendorcheckId.getVendorAttirbuteValue() != null) {
                // Parse key=value pairs into a map
                Map<String, String> attributeMap = new LinkedHashMap<>();
                for (String entry : byVendorChecksVendorcheckId.getVendorAttirbuteValue()) {
                    String[] parts = entry.split("=", 2);
                    if (parts.length == 2) {
                        attributeMap.put(parts[0], parts[1]);
                    }
                }
                svcSearchResult.setData((T) attributeMap);
                svcSearchResult.setMessage("Fetched uploaded attribute values");
                return svcSearchResult;
            }

            VendorChecks byVendorcheckId = vendorChecksRepository.findByVendorcheckId(vendorCheckId);
            List<ConventionalAttributesMaster> all = conventionalAttributesMasterRepository.findAll();
            List<ConventionalAttributesMaster> matchingEntities = all.stream()
                    .filter(attr -> attr.getSourceIds().contains(byVendorcheckId.getSource().getSourceId()))
                    .collect(Collectors.toList());
            svcSearchResult.setMessage("Fetched Data");
            if (matchingEntities.size() > 0) {
                ConventionalAttributesMaster firstMatchingEntity = matchingEntities.get(0);
                if (byVendorcheckId.getSource().getSourceId() == 38L) {
                    List<ConventionalAttributesMaster> collect = matchingEntities.stream()
                            .filter(p -> p.getGlobalCheckType().equalsIgnoreCase(type))
                            .collect(Collectors.toList());
                    svcSearchResult.setData((T) collect.get(0));
                } else {
                    svcSearchResult.setData((T) firstMatchingEntity);
                }
            } else {
                svcSearchResult.setMessage("No matching data found");
                svcSearchResult.setData(null); // Set to appropriate value or handle accordingly
            }

        } catch (Exception e) {
            log.info("exception in getVendorCheckAttributes :" + e.getMessage());
        }
        return svcSearchResult;
    }

    public ServiceOutcome<List<ConventionalVendorCandidatesSubmitted>> searchAllCandidate(String searchText) {
        ServiceOutcome<List<ConventionalVendorCandidatesSubmitted>> svcSearchResult = new ServiceOutcome<>();
        try {
            User user = (SecurityHelper.getCurrentUser() != null) ? SecurityHelper.getCurrentUser() : userRepository.findByUserId(53l);

            Long userId = user.getUserId();
            User byOrganizationAndRoleId = userRepository.findByOrganizationAndRoleId(user.getOrganization().getOrganizationId(), user.getRole().getRoleId(), user.getUserId());
            Date createdOnDate = byOrganizationAndRoleId.getCreatedOn();
//            log.info("CreatedOnDate {}", createdOnDate);
            Date currentDate = new Date();
//            log.info("Current Date: {}", currentDate);
            List<ConventionalVendorCandidatesSubmitted> searchResult = new ArrayList<>();
            List<StatusMaster> all = statusMasterRepository.findAll();
            Optional<StatusMaster> matchedStatus = null;
            String upperCase = searchText.trim().replaceAll("\\s+", "").toUpperCase();
            if ("QCPENDING".contains(upperCase)) {
                searchText = "PENDINGAPPROVAL";
                String finalSearchText = searchText;
                matchedStatus = all.stream().filter(statusMaster -> statusMaster.getStatusCode().contains(finalSearchText.trim().replaceAll("\\s+", "").toUpperCase())).findFirst();
            } else {
                String finalSearchText1 = searchText;
                matchedStatus = all.stream().filter(statusMaster -> statusMaster.getStatusCode().contains(finalSearchText1.trim().replaceAll("\\s+", "").toUpperCase())).findFirst();
            }
            if (matchedStatus.isPresent()) {
                searchResult = conventionalCandidatesSubmittedRepository.searchAllCandidateStatus(String.valueOf(matchedStatus.get().getStatusMasterId()), createdOnDate, currentDate);
            } else {
                searchResult = conventionalCandidatesSubmittedRepository.searchAllCandidate(searchText, createdOnDate, currentDate);
            }
            if (searchResult.isEmpty() == true) {
                svcSearchResult.setData(new ArrayList<ConventionalVendorCandidatesSubmitted>());
            } else {
                svcSearchResult.setData(searchResult);
            }
        } catch (Exception e) {
            log.error("inside search data" + e.getMessage());
        }
        return svcSearchResult;
    }


    public ServiceOutcome<String> getRemarksForValidation(String checkuniqueId) {
        ServiceOutcome<String> stringServiceOutcome = new ServiceOutcome<>();
        try {
            ConventionalVendorliChecksToPerform byCheckUniqueId = liCheckToPerformRepository.findByCheckUniqueId(Long.valueOf(checkuniqueId));
            stringServiceOutcome.setData(byCheckUniqueId.getCheckRemarks());
        } catch (Exception e) {

        }
        return stringServiceOutcome;
    }

    public ServiceOutcome<String> reAssignToAnotherVendor(String checkUniqueId, String vendorId) {
        ServiceOutcome<String> stringServiceOutcome = new ServiceOutcome<>();
        try {
            ConventionalVendorliChecksToPerform byCheckUniqueId = liCheckToPerformRepository.findByCheckUniqueId(Long.valueOf(checkUniqueId));
            if (byCheckUniqueId != null) {
                if (byCheckUniqueId.getVendorChecks() != null) {
                    User byUserId = userRepository.findByUserId(Long.valueOf(vendorId));
                    byCheckUniqueId.setVendorName(byUserId.getUserFirstName() + byUserId.getUserLastName());
                    ConventionalVendorliChecksToPerform updateLichekVendor = liCheckToPerformRepository.save(byCheckUniqueId);
                    VendorChecks byVendorcheckId = vendorChecksRepository.findByVendorcheckId(byCheckUniqueId.getVendorChecks().getVendorcheckId());
                    byVendorcheckId.setVendorId(byUserId.getUserId());
                    VendorChecks updatedVendorCheckVendor = vendorChecksRepository.save(byVendorcheckId);
                    log.info("updated vendor Sucessfully");
                    stringServiceOutcome.setData("Updated VendorSucessfully");
                    stringServiceOutcome.setMessage("Updated VendorSucessfully");
                    stringServiceOutcome.setOutcome(true);
                    stringServiceOutcome.setStatus("200");
                }
            }
        } catch (Exception e) {
            stringServiceOutcome.setData("Not Able To Update  Vendor");
            stringServiceOutcome.setMessage("Not Able To Update  Vendor");
            stringServiceOutcome.setOutcome(false);
            stringServiceOutcome.setStatus("200");
        }
        return stringServiceOutcome;
    }


    public ServiceOutcome<byte[]> downloadAllFilebyRequestId(String requestId) throws Exception {
        ServiceOutcome<byte[]> serviceOutcome = new ServiceOutcome<>();
        File nanda = File.createTempFile("nanda", ".zip");
        String pathkey = "Candidate/Convetional/" + requestId;
        System.out.println("padfs" + pathkey);
        List<S3ObjectSummary> objectSummaries = s3Client.listObjects(DIGIVERIFIER_DOC_BUCKET_NAME, pathkey).getObjectSummaries();
        try (ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(nanda)) {
            for (S3ObjectSummary objectSummary : objectSummaries) {
                S3Object s3Object = s3Client.getObject(DIGIVERIFIER_DOC_BUCKET_NAME, objectSummary.getKey());
                String fileName = objectSummary.getKey();
                ZipArchiveEntry zipEntry = new ZipArchiveEntry(fileName);
                zipOut.putArchiveEntry(zipEntry);
                IOUtils.copy(s3Object.getObjectContent(), zipOut);
                zipOut.closeArchiveEntry();
                s3Object.close();
            }
            Path zipFilePath = Paths.get(nanda.getPath());
            byte[] data = Files.readAllBytes(zipFilePath);
            serviceOutcome.setData(data);
//            String base64String = Base64.getEncoder().encodeToString(data);
            zipOut.finish();
            serviceOutcome.setMessage("");
            serviceOutcome.setOutcome(true);
        } catch (IOException e) {
            serviceOutcome.setOutcome(false);
            log.info(e.getMessage());

        }
        return serviceOutcome;
    }

    @Override
    public ServiceOutcome<String> deleteData(PurgeDto purgeDto) {
        Map<String, Boolean> deletionStatus = new LinkedHashMap<>(); // Track table deletions
        ServiceOutcome<String> serviceOutcome = new ServiceOutcome<>();
        List<String> successfullyDeletedRequestIds = new ArrayList<>(); // List to track successfully deleted request IDs
        // Check if requestIds is not null or empty
        if (purgeDto.getRequestIds() == null || purgeDto.getRequestIds().isEmpty()) {
            serviceOutcome.setOutcome(false);
            serviceOutcome.setMessage("Request IDs are empty or null.");
            return serviceOutcome;
        }
        String[] tableNames = {
                "t_dgv_vendor_uploaded_checks", "t_dgv_conventional_vendorchecks_to_perform",
                "t_dgv_vendor_checks", "t_dgv_candidate_caf_address", "t_dgv_candidate_caf_education",
                "t_dgv_candidate_caf_experience", "t_dgv_candidate_verification_state", "t_dgv_content",
                "t_dgv_conventioanl_candidate_caf_address", "t_dgv_conventional_candidate_caf_education",
                "t_dgv_conventional_candidate_basic", "t_dgv_conventional_candidate_document_info",
                "t_dgv_conventional_candidate_drug_info", "t_dgv_conventional_candidate_reference_info"
        };

        String[] deleteQueries = {
                "DELETE FROM t_dgv_vendor_uploaded_checks WHERE vendor_check_id IN (SELECT vendor_check_id FROM t_dgv_vendor_checks WHERE candidate_id IN (SELECT candidate_id FROM t_dgv_candidate_basic WHERE conventional_request_id IN (SELECT request_id FROM t_dgv_conventional_candidate_request WHERE request_id = ?)))",
                "DELETE FROM t_dgv_conventional_vendorchecks_to_perform WHERE vendor_check IN (SELECT vendor_check_id FROM t_dgv_vendor_checks WHERE candidate_id IN (SELECT candidate_id FROM t_dgv_candidate_basic WHERE conventional_request_id IN (SELECT request_id FROM t_dgv_conventional_candidate_request WHERE request_id = ?)))",
                "DELETE FROM t_dgv_vendor_checks WHERE candidate_id IN (SELECT candidate_id FROM t_dgv_candidate_basic WHERE conventional_request_id IN (SELECT request_id FROM t_dgv_conventional_candidate_request WHERE request_id = ?))",
                "DELETE FROM t_dgv_candidate_caf_address WHERE candidate_id IN (SELECT candidate_id FROM t_dgv_candidate_basic WHERE conventional_request_id IN (SELECT request_id FROM t_dgv_conventional_candidate_request WHERE request_id = ?))",
                "DELETE FROM t_dgv_candidate_caf_education WHERE candidate_id IN (SELECT candidate_id FROM t_dgv_candidate_basic WHERE conventional_request_id IN (SELECT request_id FROM t_dgv_conventional_candidate_request WHERE request_id = ?))",
                "DELETE FROM t_dgv_candidate_caf_experience WHERE candidate_id IN (SELECT candidate_id FROM t_dgv_candidate_basic WHERE conventional_request_id IN (SELECT request_id FROM t_dgv_conventional_candidate_request WHERE request_id = ?))",
                "DELETE FROM t_dgv_candidate_verification_state WHERE candidate_id IN (SELECT candidate_id FROM t_dgv_candidate_basic WHERE conventional_request_id IN (SELECT request_id FROM t_dgv_conventional_candidate_request WHERE request_id = ?))",
                "DELETE FROM t_dgv_content WHERE candidate_id IN (SELECT candidate_id FROM t_dgv_candidate_basic WHERE conventional_request_id IN (SELECT request_id FROM t_dgv_conventional_candidate_request WHERE request_id = ?))",
                "DELETE FROM t_dgv_conventioanl_candidate_caf_address WHERE conventional_requestid IN (SELECT request_id FROM t_dgv_conventional_candidate_request WHERE request_id = ?)",
                "DELETE FROM t_dgv_conventional_candidate_caf_education WHERE conventional_requestid IN (SELECT request_id FROM t_dgv_conventional_candidate_request WHERE request_id = ?)",
                "DELETE FROM t_dgv_conventional_candidate_basic WHERE conventional_request_id IN (SELECT request_id FROM t_dgv_conventional_candidate_request WHERE request_id = ?)",
                "DELETE FROM t_dgv_conventional_candidate_document_info WHERE request_id IN (SELECT request_id FROM t_dgv_conventional_candidate_request WHERE request_id = ?)",
                "DELETE FROM t_dgv_conventional_candidate_drug_info WHERE conventional_requestid IN (SELECT request_id FROM t_dgv_conventional_candidate_request WHERE request_id = ?)",
                "DELETE FROM t_dgv_conventional_candidate_reference_info WHERE conventional_requestid IN (SELECT request_id FROM t_dgv_conventional_candidate_request WHERE request_id = ?)"
        };

        // Create a single connection for all requests
        try (Connection connection = DriverManager.getConnection(environmentVal.getDatasourceUrl(), "digiverifier", "62DFrjznH1Hu")) {
            // Loop through all request IDs
            for (String requestId : purgeDto.getRequestIds()) {
                try {
                    // Fetch S3 paths before deleting
                    List<String> pathKeys = fetchPathKeys(requestId); // Fetch the S3 paths based on the current requestId
                    ServiceOutcome<List<String>> s3Outcome = awsUtils.deletePathsFromS3(pathKeys, DIGIVERIFIER_DOC_BUCKET_NAME, requestId);

                    if (!s3Outcome.getOutcome()) {
                        serviceOutcome.setOutcome(false);
                        serviceOutcome.setMessage("Failed to delete S3 paths for requestId: " + requestId + ". Aborting DB deletion.");
                        return serviceOutcome; // Abort the process if S3 deletion fails
                    }

                    // Database deletion logic for the current requestId
                    Map<String, Boolean> localDeletionStatus = new LinkedHashMap<>(); // Track table deletions for current requestId

                    // Execute the delete queries for the current requestId
                    for (int i = 0; i < deleteQueries.length; i++) {
                        try (PreparedStatement statement = connection.prepareStatement(deleteQueries[i])) {
                            statement.setString(1, requestId);
                            int affectedRows = statement.executeUpdate();
                            localDeletionStatus.put(tableNames[i], affectedRows > 0);
                            log.info("Deleted from table: " + tableNames[i] + " | Rows affected: " + affectedRows);
                        } catch (SQLException e) {
                            localDeletionStatus.put(tableNames[i], false);
                            log.error("Failed to delete from table: " + tableNames[i] + " | Error: " + e.getMessage());
                        }
                    }

                    // Mask sensitive data for the current requestId
                    String[] maskQueries = {
                            "UPDATE t_dgv_candidate_basic SET " +
                                    "email_id = CASE WHEN email_id IS NOT NULL THEN CONCAT(LEFT(email_id, 2), '*****', SUBSTRING(email_id, LOCATE('@', email_id))) ELSE NULL END, " +
                                    "contact_number = CASE WHEN contact_number IS NOT NULL THEN CONCAT('******', RIGHT(contact_number, 4)) ELSE NULL END, " +
                                    "candidate_name = CASE WHEN CHAR_LENGTH(candidate_name) > 2 THEN CONCAT(LEFT(candidate_name, 1), REPEAT('*', CHAR_LENGTH(candidate_name) - 2), RIGHT(candidate_name, 1)) ELSE candidate_name END, " +
                                    "date_of_birth = NULL WHERE conventional_request_id = ?",
                            "UPDATE t_dgv_conventional_candidate_request SET " +
                                    "name = CASE WHEN CHAR_LENGTH(name) > 2 THEN CONCAT(LEFT(name, 1), REPEAT('*', CHAR_LENGTH(name) - 2), RIGHT(name, 1)) ELSE name END, " +
                                    "verification_status = 'PURGED', " +
                                    "status = 2, " +
                                    "purged_date = CURRENT_TIMESTAMP " +
                                    "WHERE request_id = ?"
                    };

                    // Execute the mask queries for the current requestId
                    for (String query : maskQueries) {
                        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                            preparedStatement.setString(1, requestId);
                            int updatedRows = preparedStatement.executeUpdate();
                            log.info("Candidate Basic and Candidate request Table Purged successfully for requestId: " + requestId + " | Rows affected: " + updatedRows);
                        } catch (SQLException e) {
                            log.error("Failed to execute query: " + query + " | Error: " + e.getMessage());
                        }
                    }

                    // Track the success of the current requestId
                    successfullyDeletedRequestIds.add(requestId);

                } catch (Exception e) {
                    log.error("Error during deletion and masking for requestId: " + requestId + " | Error: " + e.getMessage());
                    serviceOutcome.setOutcome(false);
                    serviceOutcome.setMessage("Error during deletion and masking for requestId: " + requestId);
                    return serviceOutcome; // Abort if there's an error with the process
                }
            }

            // Return successful outcome with list of successfully deleted request IDs
            serviceOutcome.setOutcome(true);
            serviceOutcome.setData("Successfully deleted and masked data for request IDs: " + String.join(", ", successfullyDeletedRequestIds));
            serviceOutcome.setMessage("Database deletion and masking process completed for the provided request IDs.");
            return serviceOutcome;

        } catch (SQLException e) {
            log.error("Database connection failed: " + e.getMessage());
            serviceOutcome.setOutcome(false);
            serviceOutcome.setMessage("Database connection failed during deletion process.");
            return serviceOutcome; // Return error if the connection fails
        }
    }

    private List<String> fetchPathKeys(String requestId) {
        List<String> pathKeys = new ArrayList<>();
        String[] queries = {"SELECT document_url FROM t_dgv_conventional_candidate_document_info WHERE request_id in (select request_id from t_dgv_conventional_candidate_request where request_id = ?)",
                "SELECT vendor_upload_document_path_key FROM t_dgv_vendor_uploaded_checks WHERE vendor_check_id IN (SELECT vendor_check_id FROM t_dgv_vendor_checks WHERE candidate_id IN (SELECT candidate_id FROM t_dgv_candidate_basic WHERE conventional_request_id = ?))",
                "SELECT vendor_upload_image_path_key FROM t_dgv_vendor_uploaded_checks WHERE vendor_check_id IN (SELECT vendor_check_id FROM t_dgv_vendor_checks WHERE candidate_id IN (SELECT candidate_id FROM t_dgv_candidate_basic WHERE conventional_request_id = ?))"
                , "SELECT pathkey FROM t_dgv_vendor_checks WHERE candidate_id IN (SELECT candidate_id FROM t_dgv_candidate_basic WHERE conventional_request_id = ?)"};

        try (Connection connection = DriverManager.getConnection(environmentVal.getDatasourceUrl(), "digiverifier", "62DFrjznH1Hu")) {
            for (String query : queries) {
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setString(1, requestId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            pathKeys.add(rs.getString(1));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Error fetching S3 path keys: " + e.getMessage());
        }
        return pathKeys;
    }

    public ServiceOutcome<String> updateIdentityCheckDisableStatus(String checkUniqueId, String disableStatus) {
        ServiceOutcome<String> stringServiceOutcome = new ServiceOutcome<>();
        try {
            ConventionalVendorliChecksToPerform byCheckUniqueId = liCheckToPerformRepository.findByCheckUniqueId(Long.valueOf(checkUniqueId));
            if (byCheckUniqueId != null) {
                byCheckUniqueId.setDisabled(disableStatus);
                ConventionalVendorliChecksToPerform save = liCheckToPerformRepository.save(byCheckUniqueId);
                stringServiceOutcome.setData(disableStatus + " for " + save.getCheckName() + " Done ..");
                stringServiceOutcome.setOutcome(true);
            }
        } catch (Exception e) {
            log.info("exception for updateIdentityCheckDisableStatus :" + e.getMessage());
            stringServiceOutcome.setData("Enable/Disable not possible now");
            stringServiceOutcome.setOutcome(false);
        }
        return stringServiceOutcome;
    }


    @Override
    public ServiceOutcome<ConventionalAttributesMaster> updateConventionalAttributesMaster(
            ConventionalAttributesMaster conventionalAttributesMaster, Long attributeId) {

        ServiceOutcome<ConventionalAttributesMaster> svcSearchResult = new ServiceOutcome<ConventionalAttributesMaster>();
        try {
            ConventionalAttributesMaster conventionalId = conventionalAttributesMasterRepository.findByAttributeId(attributeId);
            if (conventionalId != null) {
                ConventionalAttributesMaster updateAttributes = conventionalId;
                updateAttributes.setCheckAttibutes(conventionalAttributesMaster.getCheckAttibutes());
                updateAttributes.setSourceIds(conventionalAttributesMaster.getSourceIds());

                System.out.println("UPDATEATTRIBUTE ================ " + updateAttributes.getCheckAttibutes().toString());
                ConventionalAttributesMaster save = conventionalAttributesMasterRepository.save(updateAttributes);


                svcSearchResult.setData(save);
                svcSearchResult.setOutcome(true);

            } else {
                System.out.println("No entity found with attributeId: " + attributeId);
                svcSearchResult.setOutcome(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return svcSearchResult;
    }

    @Transactional
    public ServiceOutcome<String> refetchCandidateData(String requestdID, Boolean checkFetchFlag) {
        ServiceOutcome<String> stringServiceOutcome = new ServiceOutcome<>();
        try {
            log.info(" Refetch ConventionalCandidateInformation() starts");
            ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository.findByRequestId(String.valueOf(requestdID));
            FetchVendorConventionalCandidateDto fetchVendorConventionalCandidateDto = new FetchVendorConventionalCandidateDto();
            fetchVendorConventionalCandidateDto.setCandidateID(String.valueOf(conventionalVendorCandidatesSubmitted.getCandidateId()));
            fetchVendorConventionalCandidateDto.setRequestId(conventionalVendorCandidatesSubmitted.getRequestId());
            fetchVendorConventionalCandidateDto.setPsno(conventionalVendorCandidatesSubmitted.getPsNo());
            fetchVendorConventionalCandidateDto.setVendorId(conventionalVendorCandidatesSubmitted.getVendorId());
            fetchVendorConventionalCandidateDto.setRequestType(conventionalVendorCandidatesSubmitted.getRequestType());
            User user = (SecurityHelper.getCurrentUser() != null) ? SecurityHelper.getCurrentUser() : userRepository.findByUserId(53l);
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", environmentVal.getMtGrantType());
            map.add("username", environmentVal.getMtUsername());
            map.add("password", environmentVal.getMtPassword());
            HttpHeaders tokenHeader = new HttpHeaders();
            tokenHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            ResponseEntity<String> responseEntity = null;
            HttpEntity<MultiValueMap<String, String>> requestBodyFormUrlEncoded = new HttpEntity<>(map, tokenHeader);
            responseEntity = restTemplate.postForEntity(environmentVal.getConventionalVendorToken(), requestBodyFormUrlEncoded, String.class);
            JSONObject tokenObject = new JSONObject(responseEntity.getBody());
            String access_token = tokenObject.getString("access_token");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + access_token);
            headers.set("Content-Type", "application/json");
            HttpEntity<FetchVendorConventionalCandidateDto> liCheckDtoHttpEntity = new HttpEntity<>(fetchVendorConventionalCandidateDto, headers);
            ResponseEntity<String> icheckRepsonse = restTemplate.exchange(environmentVal.getConventionalVendorFetchVendorChecks(), HttpMethod.POST, liCheckDtoHttpEntity, String.class);
            String message = icheckRepsonse.getBody();
            JSONObject obj1 = new JSONObject(message);
            if (!checkFetchFlag) {
                if (obj1.isNull("liCandidateInformation") == false) {
                    JSONObject liCandidateInformation = obj1.getJSONObject("liCandidateInformation");
                    if (liCandidateInformation.isNull("liCandidateBasicInfo") == false) {

                        if (liCandidateInformation.isNull("liCandidatePANInfo") == false) {
                            JSONArray liCandidatePANInfo = liCandidateInformation.getJSONArray("liCandidatePANInfo");
                            List<JSONObject> collect = IntStream.range(0, liCandidatePANInfo.length()).mapToObj(index -> ((JSONObject) liCandidatePANInfo.get(index))).collect(Collectors.toList());
                            collect.forEach(candid -> {
                                List<CandidateIdItems> byRequestIdAndIdType = candidateIdItemsRepository.findByRequestIdAndIdType(obj1.getString("RequestID"), "PANCARD");
                                if (byRequestIdAndIdType.isEmpty() == false) {
                                    byRequestIdAndIdType.forEach(candidateIdItems -> {
                                        candidateIdItems.setCreatedOn(new Date());
                                        candidateIdItems.setIdHolderName(candid.getString("NameInPAN"));
                                        candidateIdItems.setIdNumber(candid.getString("PANNumber"));
                                        String documentUrl = candid.getString("PanDocument");
                                        String candidateID = obj1.getString("CandidateID").isBlank() ? obj1.getString("PSNO") : obj1.getString("CandidateID");
                                        Long requestID = obj1.getLong("RequestID");
                                        candidateIdItems.setCandidateId(String.valueOf(candidateID));
                                        candidateIdItems.setRequestId(String.valueOf(requestID));
                                        HttpEntity<String> entity = new HttpEntity<>(headers);
                                        ResponseEntity<Object[]> response = restTemplate.exchange(documentUrl, HttpMethod.POST, entity, Object[].class);
                                        String documentKeyName = "PANCARD";
                                        candidateIdItems.setIdType("PANCARD");
                                        log.info("Document saved for " + documentKeyName);
                                        ConventionalCandidateDocumentInfo conventionalCandidateDocumentInfo = candidateServiceImpl.saveConventionalDocuments(response, requestID, candidateID, documentKeyName, false);
                                        candidateIdItems.setDocumentId(String.valueOf(conventionalCandidateDocumentInfo.getDocId()));
                                        CandidateIdItems save = candidateIdItemsRepository.save(candidateIdItems);
                                    });
                                } else {
                                    CandidateIdItems candidateIdItems = new CandidateIdItems();
                                    candidateIdItems.setCreatedOn(new Date());
                                    candidateIdItems.setIdHolderName(candid.getString("NameInPAN"));
                                    candidateIdItems.setIdNumber(candid.getString("PANNumber"));
                                    String documentUrl = candid.getString("PanDocument");
                                    String candidateID = obj1.getString("CandidateID").isBlank() ? obj1.getString("PSNO") : obj1.getString("CandidateID");
                                    Long requestID = obj1.getLong("RequestID");
                                    candidateIdItems.setCandidateId(String.valueOf(candidateID));
                                    candidateIdItems.setRequestId(String.valueOf(requestID));
                                    HttpEntity<String> entity = new HttpEntity<>(headers);
                                    ResponseEntity<Object[]> response = restTemplate.exchange(documentUrl, HttpMethod.POST, entity, Object[].class);
                                    String documentKeyName = "PANCARD";
                                    candidateIdItems.setIdType("PANCARD");
                                    log.info("Document saved for " + documentKeyName);
                                    ConventionalCandidateDocumentInfo conventionalCandidateDocumentInfo = candidateServiceImpl.saveConventionalDocuments(response, requestID, candidateID, documentKeyName, false);
                                    candidateIdItems.setDocumentId(String.valueOf(conventionalCandidateDocumentInfo.getDocId()));
                                    CandidateIdItems save = candidateIdItemsRepository.save(candidateIdItems);

                                }
                            });
                        }
                        if (liCandidateInformation.isNull("liCandidatePassportInfo") == false) {
                            JSONArray liCandidatePassportInfo = liCandidateInformation.getJSONArray("liCandidatePassportInfo");
                            List<JSONObject> collect = IntStream.range(0, liCandidatePassportInfo.length()).mapToObj(index -> ((JSONObject) liCandidatePassportInfo.get(index))).collect(Collectors.toList());
                            collect.forEach(candid -> {
                                List<CandidateIdItems> byRequestIdAndIdType = candidateIdItemsRepository.findByRequestIdAndIdType(obj1.getString("RequestID"), "PASSPORT");
                                if (byRequestIdAndIdType.isEmpty() == false) {
                                    byRequestIdAndIdType.forEach(candidateIdItems -> {
                                        candidateIdItems.setCreatedOn(new Date());
                                        candidateIdItems.setIdHolderName(candid.getString("NameInPassport"));
                                        candidateIdItems.setIdNumber(candid.getString("PassportNumber"));
                                        candidateIdItems.setIdHolderDob(candid.getString("DOBInPassport"));
                                        candidateIdItems.setCountry(candid.getString("Country"));
                                        candidateIdItems.setIdHolderIssueDate(candid.getString("DateOfIssue"));
                                        candidateIdItems.setExpiryDate(candid.getString("ExpiryDate"));
                                        String documentUrl = candid.getString("PassportDocument");
                                        String candidateID = obj1.getString("CandidateID").isBlank() ? obj1.getString("PSNO") : obj1.getString("CandidateID");
                                        Long requestID = obj1.getLong("RequestID");
                                        candidateIdItems.setCandidateId(String.valueOf(candidateID));
                                        candidateIdItems.setRequestId(String.valueOf(requestID));
                                        HttpEntity<String> entity = new HttpEntity<>(headers);
                                        ResponseEntity<Object[]> response = restTemplate.exchange(documentUrl, HttpMethod.POST, entity, Object[].class);
                                        String documentKeyName = "PASSPORT";
                                        candidateIdItems.setIdType("PASSPORT");
                                        log.info("Document saved for " + documentKeyName);
                                        ConventionalCandidateDocumentInfo conventionalCandidateDocumentInfo = candidateServiceImpl.saveConventionalDocuments(response, requestID, candidateID, documentKeyName, false);
                                        candidateIdItems.setDocumentId(String.valueOf(conventionalCandidateDocumentInfo.getDocId()));
                                        CandidateIdItems save = candidateIdItemsRepository.save(candidateIdItems);
                                    });
                                } else {
                                    CandidateIdItems candidateIdItems = new CandidateIdItems();
                                    candidateIdItems.setCreatedOn(new Date());
                                    candidateIdItems.setIdHolderName(candid.getString("NameInPassport"));
                                    candidateIdItems.setIdNumber(candid.getString("PassportNumber"));
                                    candidateIdItems.setIdHolderDob(candid.getString("DOBInPassport"));
                                    candidateIdItems.setCountry(candid.getString("Country"));
                                    candidateIdItems.setIdHolderIssueDate(candid.getString("DateOfIssue"));
                                    candidateIdItems.setExpiryDate(candid.getString("ExpiryDate"));
                                    String documentUrl = candid.getString("PassportDocument");
                                    String candidateID = obj1.getString("CandidateID").isBlank() ? obj1.getString("PSNO") : obj1.getString("CandidateID");
                                    Long requestID = obj1.getLong("RequestID");
                                    candidateIdItems.setCandidateId(String.valueOf(candidateID));
                                    candidateIdItems.setRequestId(String.valueOf(requestID));
                                    HttpEntity<String> entity = new HttpEntity<>(headers);
                                    ResponseEntity<Object[]> response = restTemplate.exchange(documentUrl, HttpMethod.POST, entity, Object[].class);
                                    String documentKeyName = "PASSPORT";
                                    candidateIdItems.setIdType("PASSPORT");
                                    log.info("Document saved for " + documentKeyName);
                                    ConventionalCandidateDocumentInfo conventionalCandidateDocumentInfo = candidateServiceImpl.saveConventionalDocuments(response, requestID, candidateID, documentKeyName, false);
                                    candidateIdItems.setDocumentId(String.valueOf(conventionalCandidateDocumentInfo.getDocId()));
                                    CandidateIdItems save = candidateIdItemsRepository.save(candidateIdItems);

                                }
                            });
                        }
                        if (liCandidateInformation.isNull("liCandidateVoterIDInfo") == false) {
                            JSONArray liCandidateVoterIDInfo = liCandidateInformation.getJSONArray("liCandidateVoterIDInfo");
                            List<JSONObject> collect = IntStream.range(0, liCandidateVoterIDInfo.length()).mapToObj(index -> ((JSONObject) liCandidateVoterIDInfo.get(index))).collect(Collectors.toList());
                            collect.forEach(candid -> {
                                List<CandidateIdItems> byRequestIdAndIdType = candidateIdItemsRepository.findByRequestIdAndIdType(obj1.getString("RequestID"), "VOTERID");
                                if (byRequestIdAndIdType.isEmpty() == false) {
                                    byRequestIdAndIdType.forEach(candidateIdItems -> {
                                        candidateIdItems.setCreatedOn(new Date());
                                        candidateIdItems.setIdHolderDob(candid.getString("DOBInVoterID"));
                                        candidateIdItems.setIdNumber(candid.getString("VoterIDNumber"));
                                        String documentUrl = candid.getString("VoterIDDocument");
                                        String candidateID = obj1.getString("CandidateID").isBlank() ? obj1.getString("PSNO") : obj1.getString("CandidateID");
                                        Long requestID = obj1.getLong("RequestID");
                                        candidateIdItems.setCandidateId(String.valueOf(candidateID));
                                        candidateIdItems.setRequestId(String.valueOf(requestID));
                                        HttpEntity<String> entity = new HttpEntity<>(headers);
                                        ResponseEntity<Object[]> response = restTemplate.exchange(documentUrl, HttpMethod.POST, entity, Object[].class);
                                        String documentKeyName = "VOTERID";
                                        candidateIdItems.setIdType("VOTERID");
                                        log.info("Document saved for " + documentKeyName);
                                        ConventionalCandidateDocumentInfo conventionalCandidateDocumentInfo = candidateServiceImpl.saveConventionalDocuments(response, requestID, candidateID, documentKeyName, false);
                                        candidateIdItems.setDocumentId(String.valueOf(conventionalCandidateDocumentInfo.getDocId()));
                                        CandidateIdItems save = candidateIdItemsRepository.save(candidateIdItems);
                                    });
                                } else {
                                    CandidateIdItems candidateIdItems = new CandidateIdItems();
                                    candidateIdItems.setCreatedOn(new Date());
                                    candidateIdItems.setIdHolderDob(candid.getString("DOBInVoterID"));
                                    candidateIdItems.setIdNumber(candid.getString("VoterIDNumber"));
                                    String documentUrl = candid.getString("VoterIDDocument");
                                    String candidateID = obj1.getString("CandidateID").isBlank() ? obj1.getString("PSNO") : obj1.getString("CandidateID");
                                    Long requestID = obj1.getLong("RequestID");
                                    candidateIdItems.setCandidateId(String.valueOf(candidateID));
                                    candidateIdItems.setRequestId(String.valueOf(requestID));
                                    HttpEntity<String> entity = new HttpEntity<>(headers);
                                    ResponseEntity<Object[]> response = restTemplate.exchange(documentUrl, HttpMethod.POST, entity, Object[].class);
                                    String documentKeyName = "VOTERID";
                                    candidateIdItems.setIdType("VOTERID");
                                    log.info("Document saved for " + documentKeyName);
                                    ConventionalCandidateDocumentInfo conventionalCandidateDocumentInfo = candidateServiceImpl.saveConventionalDocuments(response, requestID, candidateID, documentKeyName, false);
                                    candidateIdItems.setDocumentId(String.valueOf(conventionalCandidateDocumentInfo.getDocId()));
                                    CandidateIdItems save = candidateIdItemsRepository.save(candidateIdItems);

                                }

                            });
                        }
                        if (liCandidateInformation.isNull("liCandidateDLInfo") == false) {
                            JSONArray liCandidateDLInfo = liCandidateInformation.getJSONArray("liCandidateDLInfo");
                            List<JSONObject> collect = IntStream.range(0, liCandidateDLInfo.length()).mapToObj(index -> ((JSONObject) liCandidateDLInfo.get(index))).collect(Collectors.toList());
                            collect.forEach(candid -> {
                                List<CandidateIdItems> byRequestIdAndIdType = candidateIdItemsRepository.findByRequestIdAndIdType(obj1.getString("RequestID"), "DRIVINGLICENCE");
                                if (byRequestIdAndIdType.isEmpty() == false) {
                                    byRequestIdAndIdType.forEach(candidateIdItems -> {
                                        candidateIdItems.setCreatedOn(new Date());
                                        candidateIdItems.setIdHolderIssueDate(candid.getString("DateOfIssue"));
                                        candidateIdItems.setIdNumber(candid.getString("DLNumber"));
                                        String documentUrl = candid.getString("DLDocument");
                                        String candidateID = obj1.getString("CandidateID").isBlank() ? obj1.getString("PSNO") : obj1.getString("CandidateID");
                                        Long requestID = obj1.getLong("RequestID");
                                        candidateIdItems.setCandidateId(String.valueOf(candidateID));
                                        candidateIdItems.setRequestId(String.valueOf(requestID));
                                        HttpEntity<String> entity = new HttpEntity<>(headers);
                                        ResponseEntity<Object[]> response = restTemplate.exchange(documentUrl, HttpMethod.POST, entity, Object[].class);
                                        String documentKeyName = "DRIVINGLICENCE";
                                        candidateIdItems.setIdType("DRIVINGLICENCE");
                                        log.info("Document saved for " + documentKeyName);
                                        ConventionalCandidateDocumentInfo conventionalCandidateDocumentInfo = candidateServiceImpl.saveConventionalDocuments(response, requestID, candidateID, documentKeyName, false);
                                        candidateIdItems.setDocumentId(String.valueOf(conventionalCandidateDocumentInfo.getDocId()));
                                        CandidateIdItems save = candidateIdItemsRepository.save(candidateIdItems);

                                    });
                                } else {
                                    CandidateIdItems candidateIdItems = new CandidateIdItems();
                                    candidateIdItems.setCreatedOn(new Date());
                                    candidateIdItems.setIdHolderIssueDate(candid.getString("DateOfIssue"));
                                    candidateIdItems.setIdNumber(candid.getString("DLNumber"));
                                    String documentUrl = candid.getString("DLDocument");
                                    String candidateID = obj1.getString("CandidateID").isBlank() ? obj1.getString("PSNO") : obj1.getString("CandidateID");
                                    Long requestID = obj1.getLong("RequestID");
                                    candidateIdItems.setCandidateId(String.valueOf(candidateID));
                                    candidateIdItems.setRequestId(String.valueOf(requestID));
                                    HttpEntity<String> entity = new HttpEntity<>(headers);
                                    ResponseEntity<Object[]> response = restTemplate.exchange(documentUrl, HttpMethod.POST, entity, Object[].class);
                                    String documentKeyName = "DRIVINGLICENCE";
                                    candidateIdItems.setIdType("DRIVINGLICENCE");
                                    log.info("Document saved for " + documentKeyName);
                                    ConventionalCandidateDocumentInfo conventionalCandidateDocumentInfo = candidateServiceImpl.saveConventionalDocuments(response, requestID, candidateID, documentKeyName, false);
                                    candidateIdItems.setDocumentId(String.valueOf(conventionalCandidateDocumentInfo.getDocId()));
                                    CandidateIdItems save = candidateIdItemsRepository.save(candidateIdItems);

                                }
                            });
                        }
                        if (liCandidateInformation.isNull("liCandidateAadhaarInfo") == false) {
                            JSONArray liCandidateAadhaarInfo = liCandidateInformation.getJSONArray("liCandidateAadhaarInfo");
                            List<JSONObject> collect = IntStream.range(0, liCandidateAadhaarInfo.length()).mapToObj(index -> ((JSONObject) liCandidateAadhaarInfo.get(index))).collect(Collectors.toList());
                            collect.forEach(candid -> {

                                List<CandidateIdItems> byRequestIdAndIdType = candidateIdItemsRepository.findByRequestIdAndIdType(obj1.getString("RequestID"), "AADHARCARD");
                                if (byRequestIdAndIdType.isEmpty() == false) {
                                    byRequestIdAndIdType.forEach(candidateIdItems -> {
                                        candidateIdItems.setCreatedOn(new Date());
                                        candidateIdItems.setIdHolderName(candid.getString("NameInAadhaar"));
                                        candidateIdItems.setIdNumber(candid.getString("AadhaarNumber"));
                                        candidateIdItems.setIdHolderDob(candid.getString("DOBInAadhaar"));
                                        String documentUrl = candid.getString("AadhaarDocument");
                                        String candidateID = obj1.getString("CandidateID").isBlank() ? obj1.getString("PSNO") : obj1.getString("CandidateID");
                                        Long requestID = obj1.getLong("RequestID");
                                        candidateIdItems.setCandidateId(String.valueOf(candidateID));
                                        candidateIdItems.setRequestId(String.valueOf(requestID));
                                        HttpEntity<String> entity = new HttpEntity<>(headers);
                                        ResponseEntity<Object[]> response = restTemplate.exchange(documentUrl, HttpMethod.POST, entity, Object[].class);
                                        String documentKeyName = "AADHARCARD";
                                        candidateIdItems.setIdType("AADHARCARD");
                                        log.info("Document saved for " + documentKeyName);
                                        ConventionalCandidateDocumentInfo conventionalCandidateDocumentInfo = candidateServiceImpl.saveConventionalDocuments(response, requestID, candidateID, documentKeyName, false);
                                        candidateIdItems.setDocumentId(String.valueOf(conventionalCandidateDocumentInfo.getDocId()));
                                        CandidateIdItems save = candidateIdItemsRepository.save(candidateIdItems);

                                    });
                                } else {
                                    CandidateIdItems candidateIdItems = new CandidateIdItems();
                                    candidateIdItems.setCreatedOn(new Date());
                                    candidateIdItems.setIdHolderName(candid.getString("NameInAadhaar"));
                                    candidateIdItems.setIdNumber(candid.getString("AadhaarNumber"));
                                    candidateIdItems.setIdHolderDob(candid.getString("DOBInAadhaar"));
                                    String documentUrl = candid.getString("AadhaarDocument");
                                    String candidateID = obj1.getString("CandidateID").isBlank() ? obj1.getString("PSNO") : obj1.getString("CandidateID");
                                    Long requestID = obj1.getLong("RequestID");
                                    candidateIdItems.setCandidateId(String.valueOf(candidateID));
                                    candidateIdItems.setRequestId(String.valueOf(requestID));
                                    HttpEntity<String> entity = new HttpEntity<>(headers);
                                    ResponseEntity<Object[]> response = restTemplate.exchange(documentUrl, HttpMethod.POST, entity, Object[].class);
                                    String documentKeyName = "AADHARCARD";
                                    candidateIdItems.setIdType("AADHARCARD");
                                    log.info("Document saved for " + documentKeyName);
                                    ConventionalCandidateDocumentInfo conventionalCandidateDocumentInfo = candidateServiceImpl.saveConventionalDocuments(response, requestID, candidateID, documentKeyName, false);
                                    candidateIdItems.setDocumentId(String.valueOf(conventionalCandidateDocumentInfo.getDocId()));
                                    CandidateIdItems save = candidateIdItemsRepository.save(candidateIdItems);

                                }
                            });
                        }
                        if (liCandidateInformation.isNull("CVDocuments") == false) {
                            String documentUrl = liCandidateInformation.getString("CVDocuments");
                            String candidateID = obj1.getString("CandidateID").isBlank() ? obj1.getString("PSNO") : obj1.getString("CandidateID");
                            Long requestID = obj1.getLong("RequestID");
                            HttpEntity<String> entity = new HttpEntity<>(headers);
                            ResponseEntity<Object[]> response = restTemplate.exchange(documentUrl, HttpMethod.POST, entity, Object[].class);
                            String documentKeyName = "CVVALIDATION";
                            log.info("Document saved for " + documentKeyName);
                            candidateServiceImpl.saveConventionalDocuments(response, requestID, candidateID, documentKeyName, false);
                        }
                        if (liCandidateInformation.isNull("LOADocuments") == false) {
                            String documentUrl = liCandidateInformation.getString("LOADocuments");
                            String candidateID = obj1.getString("CandidateID").isBlank() ? obj1.getString("PSNO") : obj1.getString("CandidateID");
                            Long requestID = obj1.getLong("RequestID");
                            HttpEntity<String> entity = new HttpEntity<>(headers);
                            ResponseEntity<Object[]> response = restTemplate.exchange(documentUrl, HttpMethod.POST, entity, Object[].class);
                            String documentKeyName = "LOA";
                            log.info("Document saved for " + documentKeyName);
                            candidateServiceImpl.saveConventionalDocuments(response, requestID, candidateID, documentKeyName, false);
                        }
                        if (liCandidateInformation.isNull("GapAnalysisDocuments") == false) {
                            String documentUrl = liCandidateInformation.getString("GapAnalysisDocuments");
                            String candidateID = obj1.getString("CandidateID").isBlank() ? obj1.getString("PSNO") : obj1.getString("CandidateID");
                            Long requestID = obj1.getLong("RequestID");
                            HttpEntity<String> entity = new HttpEntity<>(headers);
                            ResponseEntity<Object[]> response = restTemplate.exchange(documentUrl, HttpMethod.POST, entity, Object[].class);
                            String documentKeyName = "GAPANALYSIS";
                            log.info("Document saved for " + documentKeyName);
                            candidateServiceImpl.saveConventionalDocuments(response, requestID, candidateID, documentKeyName, false);
                        }
                        if (liCandidateInformation.isNull("liCandidateEducationInfo") == false) {
                            JSONArray liCandidateEducationInfo = liCandidateInformation.getJSONArray("liCandidateEducationInfo");
                            List<JSONObject> collect = IntStream.range(0, liCandidateEducationInfo.length()).mapToObj(index -> ((JSONObject) liCandidateEducationInfo.get(index))).collect(Collectors.toList());
                            if (fetchVendorConventionalCandidateDto.getRequestType().equalsIgnoreCase("InsufficiencyClearance") == false) {
                                collect.forEach(candid -> {
                                    if (candid.isNull("EducationDocuments") == false) {
                                        String documentUrl = candid.getString("EducationDocuments");
                                        String candidateID = obj1.getString("CandidateID").isBlank() ? obj1.getString("PSNO") : obj1.getString("CandidateID");
                                        Long requestID = obj1.getLong("RequestID");
                                        HttpEntity<String> entity = new HttpEntity<>(headers);
                                        ResponseEntity<Object[]> response = restTemplate.exchange(documentUrl, HttpMethod.POST, entity, Object[].class);
                                        String documentKeyName = "EDUCATION" + candid.getString("DegreeType").trim().toLowerCase();
                                        log.info("Document saved for " + documentKeyName);
                                        candidateServiceImpl.saveConventionalDocuments(response, requestID, candidateID, documentKeyName, false);
                                    }
                                });
                            }
                        }
                        if (liCandidateInformation.isNull("liCandidateEmploymentInfo") == false) {
                            JSONArray liCandidateEmploymentInfo = liCandidateInformation.getJSONArray("liCandidateEmploymentInfo");
                            List<JSONObject> collect = IntStream.range(0, liCandidateEmploymentInfo.length()).mapToObj(index -> ((JSONObject) liCandidateEmploymentInfo.get(index))).collect(Collectors.toList());
                            if (fetchVendorConventionalCandidateDto.getRequestType().equalsIgnoreCase("InsufficiencyClearance") == false) {
                                collect.forEach(candid -> {
                                    if (candid.isNull("EmploymentDocuments") == false) {
                                        String documentUrl = candid.getString("EmploymentDocuments");
                                        String candidateID = obj1.getString("CandidateID").isBlank() ? obj1.getString("PSNO") : obj1.getString("CandidateID");
                                        Long requestID = obj1.getLong("RequestID");
                                        HttpEntity<String> entity = new HttpEntity<>(headers);
                                        ResponseEntity<Object[]> response = restTemplate.exchange(documentUrl, HttpMethod.POST, entity, Object[].class);
                                        String documentKeyName = candid.getString("EmploymentType").trim();
                                        log.info("Document saved for " + documentKeyName);
                                        candidateServiceImpl.saveConventionalDocuments(response, requestID, candidateID, documentKeyName, false);
                                    }
                                });
                            }
                        }
                        if (liCandidateInformation.isNull("liCandidateCertificateInfo") == false) {
                            JSONArray liCandidateCertificateInfo = liCandidateInformation.getJSONArray("liCandidateCertificateInfo");
                            List<JSONObject> collect = IntStream.range(0, liCandidateCertificateInfo.length()).mapToObj(index -> ((JSONObject) liCandidateCertificateInfo.get(index))).collect(Collectors.toList());
                            if (fetchVendorConventionalCandidateDto.getRequestType().equalsIgnoreCase("InsufficiencyClearance") == false) {
                                collect.forEach(candid -> {
                                    if (candid.isNull("ProfCertificateDocuments") == false) {
                                        String documentUrl = candid.getString("ProfCertificateDocuments");
                                        String candidateID = obj1.getString("CandidateID").isBlank() ? obj1.getString("PSNO") : obj1.getString("CandidateID");
                                        Long requestID = obj1.getLong("RequestID");
                                        HttpEntity<String> entity = new HttpEntity<>(headers);
                                        ResponseEntity<Object[]> response = restTemplate.exchange(documentUrl, HttpMethod.POST, entity, Object[].class);
                                        String documentKeyName = candid.getString("Course_Name").trim();
                                        log.info("Document saved for " + documentKeyName);
                                        candidateServiceImpl.saveConventionalDocuments(response, requestID, candidateID, documentKeyName, false);
                                    }
                                });
                            }
                        }

                        JSONArray liCandidateBasicInfo = liCandidateInformation.getJSONArray("liCandidateBasicInfo");
                        List<JSONObject> collect = IntStream.range(0, liCandidateBasicInfo.length()).mapToObj(index -> ((JSONObject) liCandidateBasicInfo.get(index))).collect(Collectors.toList());


                        collect.forEach(candid -> {
                            Candidate candidate1 = candidateRepository.findByConventionalRequestId(obj1.getLong("RequestID"));
                            if (candidate1 == null) {
                                //save candidate basic
                                Candidate candidate = new Candidate();
                                candidate.setConventionalCandidateId(obj1.getString("CandidateID").isBlank() ? obj1.getString("PSNO") : obj1.getString("CandidateID"));
                                candidate.setConventionalRequestId(obj1.getLong("RequestID"));
                                candidate.setCandidateName(obj1.getString("Name"));
                                candidate.setCreatedBy(user);
                                candidate.setCreatedOn(new Date());
                                candidate.setDateOfBirth(candid.getString("DOB"));
                                candidate.setEmailId(candid.getString("EmailID"));
                                candidate.setContactNumber(candid.getString("PhoneNumber"));
                                candidate.setOrganization(organizationRepository.findById(user.getOrganization().getOrganizationId()).get());
                                Candidate savedCandidate = candidateRepository.save(candidate);
                                log.info("candidate basic  data saved ");
                                //save conventional candidate basic
                                ConventionalCandidate conventionalCandidate1 = conventionalCandidateRepository.findByConventionalRequestId(obj1.getLong("RequestID"));
                                if (conventionalCandidate1 == null) {
                                    ConventionalCandidate conventionalCandidate = new ConventionalCandidate();
                                    conventionalCandidate.setConventionalCandidateId(savedCandidate.getConventionalCandidateId());
                                    conventionalCandidate.setCandidateId(savedCandidate.getCandidateId());
                                    conventionalCandidate.setConventionalRequestId(savedCandidate.getConventionalRequestId());
                                    conventionalCandidate.setGender(candid.getString("Gender"));
                                    conventionalCandidate.setBirthPlace(candid.getString("Birthplace"));
                                    conventionalCandidate.setFirstName(candid.getString("FirstName"));
                                    conventionalCandidate.setMiddleName(candid.getString("MiddleName"));
                                    conventionalCandidate.setLastName(candid.getString("LastName"));
                                    conventionalCandidate.setNationality(candid.getString("Nationality"));
                                    conventionalCandidate.setFatherName(candid.getString("FatherName"));
                                    conventionalCandidate.setFatherDateOfBirth(candid.getString("FatherDOB"));
                                    conventionalCandidate.setInsufficiencyRemarks(candid.getString("Insufficiency_Remarks"));
                                    ConventionalCandidate saveConventionalCandidate = conventionalCandidateRepository.save(conventionalCandidate);
                                    log.info("conventional candidate data saved ");
                                }
                                //candidate address information
                                if (liCandidateInformation.isNull("liCandidateAddressInfo") == false) {
                                    JSONArray liCandidateAddressInfo = liCandidateInformation.getJSONArray("liCandidateAddressInfo");
                                    List<JSONObject> addressCollect = IntStream.range(0, liCandidateAddressInfo.length()).mapToObj(index -> ((JSONObject) liCandidateAddressInfo.get(index))).collect(Collectors.toList());
                                    addressCollect.forEach(addresscoll -> {
                                        CandidateCafAddress candidateCafAddress = new CandidateCafAddress();
                                        candidateCafAddress.setCandidate(savedCandidate);
                                        candidateCafAddress.setState(addresscoll.getString("State"));
                                        candidateCafAddress.setPinCode(addresscoll.getString("PinCode"));
                                        candidateCafAddress.setCity(addresscoll.getString("City"));
                                        candidateCafAddress.setCreatedOn(new Date());
                                        candidateCafAddress.setCandidateAddress(addresscoll.getString("HouseNumber") + "," + addresscoll.getString("StreetAddress") + "," + addresscoll.getString("City") + "," + addresscoll.getString("State") + "," + addresscoll.getString("Country") + "," + addresscoll.getString("ProminentLandmark") + "" + addresscoll.getString("PinCode"));
                                        //candidate  address save
                                        CandidateCafAddress saveCandidateCafAddress = candidateCafAddressRepository.save(candidateCafAddress);
                                        //setting the other fields not present in candidateaddress table to conventional caniddateaddress
                                        ConventionalCafAddress conventionalCafAddress = new ConventionalCafAddress();
                                        conventionalCafAddress.setConventionalRequestId(savedCandidate.getConventionalRequestId());
                                        conventionalCafAddress.setConventionalCandidateId(savedCandidate.getConventionalCandidateId());
                                        conventionalCafAddress.setCandidateCafAddressId(saveCandidateCafAddress.getCandidateCafAddressId());
                                        conventionalCafAddress.setAddressType(addresscoll.getString("AddressType"));
                                        conventionalCafAddress.setContactInfo(addresscoll.getString("ContactInfo"));
                                        conventionalCafAddress.setInsufficiencyRemarks(addresscoll.getString("Insufficiency_Remarks"));
                                        conventionalCafAddress.setHouseType(addresscoll.getString("HouseType"));
                                        try {
                                            conventionalCafAddress.setStayFromDate(dateFormater.parse(addresscoll.getString("StayFromDate")));
                                            conventionalCafAddress.setStayToDate(dateFormater.parse(addresscoll.getString("StayToDate")));
                                        } catch (ParseException e) {
                                            throw new RuntimeException(e);
                                        }
                                        //conventional candidate  address savea
                                        ConventionalCafAddress savedConventionalCafAddress = conventionCafAddressRepository.save(conventionalCafAddress);
                                        log.info("candidate address data saved ");
                                    });
                                }
                                //candidate education information
                                if (liCandidateInformation.isNull("liCandidateEducationInfo") == false) {
                                    JSONArray liCandidateEducationInfo = liCandidateInformation.getJSONArray("liCandidateEducationInfo");
                                    List<JSONObject> educationCollect = IntStream.range(0, liCandidateEducationInfo.length()).mapToObj(index -> ((JSONObject) liCandidateEducationInfo.get(index))).collect(Collectors.toList());
                                    educationCollect.forEach(educandid -> {
                                        CandidateCafEducation candidateCafEducation = new CandidateCafEducation();
                                        candidateCafEducation.setCandidate(savedCandidate);
                                        candidateCafEducation.setSchoolOrCollegeName(educandid.getString("CollegeName"));
                                        candidateCafEducation.setBoardOrUniversityName(educandid.getString("UniversityName"));
                                        candidateCafEducation.setYearOfPassing(educandid.getString("MonthYearOfPassing"));
                                        QualificationMaster bebtech = qualificationMasterRepository.findByQualificationCode("BEBTECH");
                                        candidateCafEducation.setQualificationMaster(bebtech);
                                        candidateCafEducation.setQualificationType(educandid.getString("QualificationType"));
                                        candidateCafEducation.setCountry(educandid.getString("Country"));
                                        CandidateStatus candidateStatus = new CandidateStatus();
                                        candidateStatus.setCandidate(savedCandidate);
                                        candidateStatus.setCreatedBy(user);
                                        candidateStatus.setCandidateStatusId(1l);
                                        candidateStatus.setCreatedOn(new Date());
                                        CandidateStatus save = candidateStatusRepository.save(candidateStatus);
                                        candidateCafEducation.setCandidateStatus(save);
                                        candidateCafEducation.setCreatedOn(new Date());
                                        CandidateCafEducation savedcafEducation = candidateCafEducationRepository.save(candidateCafEducation);
                                        //setting the other fields not present in candidateaddress table to conventional caniddateaddress
                                        ConventionalCandidateCafEducation conventionalCandidateCafEducation = new ConventionalCandidateCafEducation();
                                        conventionalCandidateCafEducation.setCandidateCafEducationId(savedcafEducation.getCandidateCafEducationId());
                                        conventionalCandidateCafEducation.setEducationType(educandid.getString("EducationType"));
                                        conventionalCandidateCafEducation.setDegreeType(educandid.getString("DegreeType"));
                                        if (educandid.isNull("IsSuspect") == false) {
                                            conventionalCandidateCafEducation.setIsSuspect(educandid.getString("IsSuspect"));
                                        }
                                        if (educandid.isNull("Reason_For_Suspect") == false) {
                                            conventionalCandidateCafEducation.setReasonForSuspect(educandid.getString("Reason_For_Suspect"));
                                        }
                                        conventionalCandidateCafEducation.setConventionalCandidateId(savedCandidate.getConventionalCandidateId());
                                        conventionalCandidateCafEducation.setConventionalRequestId(savedCandidate.getConventionalRequestId());
                                        try {
                                            conventionalCandidateCafEducation.setStartDate(dateFormater.parse(educandid.getString("StartDate")));
                                            conventionalCandidateCafEducation.setEndDate(dateFormater.parse(educandid.getString("EndDate")));
                                        } catch (ParseException e) {
                                            throw new RuntimeException(e);
                                        }
                                        conventionalCandidateCafEducation.setInsufficiencyRemarks(educandid.getString("Insufficiency_Remarks"));
                                        ConventionalCandidateCafEducation savedConventionalCafEducational = conventionalCafCandidateEducationRepository.save(conventionalCandidateCafEducation);
                                        log.info("candidate education data saved ");
                                    });

                                }
                                //candidate employment informatation
                                if (liCandidateInformation.isNull("liCandidateEmploymentInfo") == false) {
                                    JSONArray liCandidateEmploymentInfo = liCandidateInformation.getJSONArray("liCandidateEmploymentInfo");
                                    List<JSONObject> empCollect = IntStream.range(0, liCandidateEmploymentInfo.length()).mapToObj(index -> ((JSONObject) liCandidateEmploymentInfo.get(index))).collect(Collectors.toList());
                                    empCollect.forEach(candidEmp -> {
                                        //setting the candidateaddress details
                                        CandidateCafExperience candidateCafExperience = new CandidateCafExperience();
                                        candidateCafExperience.setCandidate(savedCandidate);
                                        candidateCafExperience.setCandidateEmployerName(candidEmp.getString("CompanyName"));
                                        if (candidEmp.isNull("UAN_Number") == false) {
                                            candidateCafExperience.setUan(candidEmp.getString("UAN_Number"));
                                        }
                                        try {
                                            candidateCafExperience.setInputDateOfJoining(dateFormater.parse(candidEmp.getString("FromDate")));
                                            candidateCafExperience.setInputDateOfExit(dateFormater.parse(candidEmp.getString("ToDate")));
                                        } catch (ParseException e) {
                                            throw new RuntimeException(e);
                                        }
                                        candidateCafExperience.setCreatedOn(new Date());
                                        candidateCafExperience.setCandidateStatus(candidateStatusRepository.findByCandidateCandidateId(savedCandidate.getCandidateId()));
                                        CandidateCafExperience savedcafExperience = candidateCafExperienceRepository.save(candidateCafExperience);
                                        ConventionalCandidateExperience conventionalCandidateExperience = new ConventionalCandidateExperience();
                                        conventionalCandidateExperience.setCandidateCafExperience(savedcafExperience.getCandidateCafExperienceId());
                                        conventionalCandidateExperience.setEmploymentType(candidEmp.getString("EmploymentType"));
                                        conventionalCandidateExperience.setDuration(candidEmp.getString("Duration"));
                                        conventionalCandidateExperience.setDesignation(candidEmp.getString("Designation"));
                                        conventionalCandidateExperience.setEmployeeCode(candidEmp.getString("EmployeeCode"));
                                        conventionalCandidateExperience.setHrName(candidEmp.getString("HRName"));
                                        conventionalCandidateExperience.setHrContactNumber(candidEmp.getString("HRContactNumber"));
                                        conventionalCandidateExperience.setHrEmailId(candidEmp.getString("HREmailID"));
                                        conventionalCandidateExperience.setSuperiorName(candidEmp.getString("SuperiorName"));
                                        conventionalCandidateExperience.setSuperiorContactNumber(candidEmp.getString("SuperiorContactNumber"));
                                        conventionalCandidateExperience.setSuperiorEmailID(candidEmp.getString("SuperiorEmailID"));
                                        conventionalCandidateExperience.setSuperiorDesignation(candidEmp.getString("SuperiorDesignation"));
                                        conventionalCandidateExperience.setLastSalary(candidEmp.getString("LastSalary"));
                                        conventionalCandidateExperience.setGrossSalary(candidEmp.getString("GrossSalary"));
                                        conventionalCandidateExperience.setInsufficiencyRemarks(candidEmp.getString("Insufficiency_Remarks"));
                                        if (candidEmp.isNull("IsSuspect") == false) {
                                            conventionalCandidateExperience.setIsSuspect(candidEmp.getString("IsSuspect"));
                                        }
                                        if (candidEmp.isNull("Office_Address") == false) {
                                            conventionalCandidateExperience.setOfficeAddress(candidEmp.getString("Office_Address"));
                                        }
                                        if (candidEmp.isNull("Reason_For_Suspect") == false) {
                                            conventionalCandidateExperience.setReasonForSuspect(candidEmp.getString("Reason_For_Suspect"));
                                        }
                                        conventionalCandidateExperience.setConventionalRequestId(savedCandidate.getConventionalRequestId());
                                        conventionalCandidateExperience.setConventionalCandidateId(String.valueOf(savedCandidate.getConventionalRequestId()));
                                        ConventionalCandidateExperience savedConventionalCandidateExperience = conventionalCandidateExperienceRepository.save(conventionalCandidateExperience);
                                        log.info("candidate experience data saved ");
                                    });
                                }
                                //candidate drug informatation
                                if (liCandidateInformation.isNull("liCandidateDrugInfo") == false) {
                                    JSONArray liCandidateDrugInfo = liCandidateInformation.getJSONArray("liCandidateDrugInfo");
                                    List<JSONObject> drugCollect = IntStream.range(0, liCandidateDrugInfo.length()).mapToObj(index -> ((JSONObject) liCandidateDrugInfo.get(index))).collect(Collectors.toList());
                                    collect.forEach(candidDrug -> {
                                        Candidate byConventionalCandidateId = candidateRepository.findByConventionalRequestId(obj1.getLong("RequestID"));
                                        //setting the candidatereference  details
                                        ConventionalCandidateDrugInfo candidateDrugInfo = new ConventionalCandidateDrugInfo();
                                        candidateDrugInfo.setCandidateId(byConventionalCandidateId.getConventionalCandidateId());
                                        candidateDrugInfo.setConventionalRequestId(byConventionalCandidateId.getConventionalRequestId());
                                        candidateDrugInfo.setName(candidDrug.getString("Name"));
                                        candidateDrugInfo.setContactNumber(candidDrug.getString("ContactNumber"));
                                        try {
                                            candidateDrugInfo.setSampleCollectionDate(dateFormater.parse(candidDrug.getString("SampleCollectionDate")));
                                        } catch (ParseException e) {
                                            throw new RuntimeException(e);
                                        }
                                        if (candidDrug.has("Remarks")) {
                                            candidateDrugInfo.setRemarks(candidDrug.getString("Remarks"));
                                        }

                                        candidateDrugInfo.setHouseNumber(candidDrug.getString("HouseNumber"));
                                        candidateDrugInfo.setStreetAddress(candidDrug.getString("StreetAddress"));
                                        candidateDrugInfo.setCity(candidDrug.getString("City"));
                                        candidateDrugInfo.setState(candidDrug.getString("State"));
                                        candidateDrugInfo.setCountry(candidDrug.getString("Country"));
                                        candidateDrugInfo.setPincode(candidDrug.getString("PinCode"));
                                        candidateDrugInfo.setCreatedOn(new Date());
                                        candidateDrugInfo.setProminentLandmark(candidDrug.getString("ProminentLandmark"));
                                        ConventionalCandidateDrugInfo savedConventionalDrugInfo = conventionalCandidateDrugInfoRepository.save(candidateDrugInfo);
                                        log.info("candidate drug data saved ");
                                    });
                                }
                                //candidate reference info
                                if (liCandidateInformation.isNull("liCandidateReferenceInfo") == false) {
                                    JSONArray liCandidateEmploymentInfo = liCandidateInformation.getJSONArray("liCandidateReferenceInfo");
                                    List<JSONObject> refCollect = IntStream.range(0, liCandidateEmploymentInfo.length()).mapToObj(index -> ((JSONObject) liCandidateEmploymentInfo.get(index))).collect(Collectors.toList());
                                    refCollect.forEach(refcandid -> {
                                        ConventionalCandidateReferenceInfo conventionalCandidateReference = new ConventionalCandidateReferenceInfo();
                                        conventionalCandidateReference.setCandidateId(savedCandidate.getConventionalCandidateId());
                                        conventionalCandidateReference.setConventionalRequestId(savedCandidate.getConventionalRequestId());
                                        conventionalCandidateReference.setName(refcandid.getString("Name"));
                                        conventionalCandidateReference.setReferenceNumber(refcandid.getString("ReferenceNumber"));
                                        conventionalCandidateReference.setDesignation(refcandid.getString("Designation"));
                                        conventionalCandidateReference.setCompanyName(refcandid.getString("CompanyName"));
                                        conventionalCandidateReference.setContactNumber(refcandid.getString("ContactNumber"));
                                        conventionalCandidateReference.setDurationKnown(refcandid.getString("DurationKnown"));
                                        conventionalCandidateReference.setEmailId(refcandid.getString("EmailID"));
                                        conventionalCandidateReference.setProfessionalRelation(refcandid.getString("ProfessionalRelation"));
                                        conventionalCandidateReference.setInsufficiencyRemarks(refcandid.getString("Insufficiency_Remarks"));
                                        ConventionalCandidateReferenceInfo saveConventionalCandidateReference = conventionalCandidateReferenceInfoRepository.save(conventionalCandidateReference);
                                        log.info("candidate reference data saved ");
                                    });
                                }
                                //candidate certificate info
                                if (liCandidateInformation.isNull("liCandidateCertificateInfo") == false) {
                                    JSONArray liCandidateCertificateInfo = liCandidateInformation.getJSONArray("liCandidateCertificateInfo");
                                    List<JSONObject> certificateCollect = IntStream.range(0, liCandidateCertificateInfo.length()).mapToObj(index -> ((JSONObject) liCandidateCertificateInfo.get(index))).collect(Collectors.toList());
                                    certificateCollect.forEach(refcandid -> {
                                        ConventionalCandidateCertificateInfo conventionalCandidateCertificateInfo = new ConventionalCandidateCertificateInfo();
                                        conventionalCandidateCertificateInfo.setRequestId(savedCandidate.getConventionalRequestId());
                                        conventionalCandidateCertificateInfo.setCourseName(refcandid.getString("Course_Name"));
                                        conventionalCandidateCertificateInfo.setCourseCompletionDate(refcandid.getString("Course_CompletionDate"));
                                        conventionalCandidateCertificateInfo.setCourseStartDate(refcandid.getString("Course_StartDate"));
                                        conventionalCandidateCertificateInfo.setCreatedBy(SecurityHelper.getCurrentUser().getUserName());
                                        conventionalCandidateCertificateInfo.setCreatedOn(new Date());
                                        conventionalCandidateCertificateInfo.setInstituteContactNumber(refcandid.getString("Institute_ContactNumber"));
                                        conventionalCandidateCertificateInfo.setInstituteName(refcandid.getString("Institute_Name"));
                                        conventionalCandidateCertificateInfo.setInstituteEmailID(refcandid.getString("Institute_EmailID"));
                                        ConventionalCandidateCertificateInfo savecCandidateCertificateInfo = conventionalCandidateCertificateInfoRepository.save(conventionalCandidateCertificateInfo);
                                        log.info("candidate certificate data saved ");

                                    });
                                }
                            } else {
                                //update candidate basic
                                candidate1.setConventionalCandidateId(obj1.getString("CandidateID").isBlank() ? obj1.getString("PSNO") : obj1.getString("CandidateID"));
                                candidate1.setConventionalRequestId(obj1.getLong("RequestID"));
                                candidate1.setCandidateName(obj1.getString("Name"));
                                candidate1.setCreatedBy(user);
                                candidate1.setCreatedOn(new Date());
                                candidate1.setDateOfBirth(candid.getString("DOB"));
                                candidate1.setEmailId(candid.getString("EmailID"));
                                candidate1.setContactNumber(candid.getString("PhoneNumber"));
                                candidate1.setOrganization(organizationRepository.findById(user.getOrganization().getOrganizationId()).get());
                                Candidate updateCandidate = candidateRepository.save(candidate1);
                                log.info("update candidate basic data saved ");
                                ConventionalCandidate conventionalCandidate1 = conventionalCandidateRepository.findByConventionalRequestId(obj1.getLong("RequestID"));
                                if (conventionalCandidate1 != null) {
                                    conventionalCandidate1.setConventionalCandidateId(updateCandidate.getConventionalCandidateId());
                                    conventionalCandidate1.setCandidateId(updateCandidate.getCandidateId());
                                    conventionalCandidate1.setConventionalRequestId(updateCandidate.getConventionalRequestId());
                                    conventionalCandidate1.setGender(candid.getString("Gender"));
                                    conventionalCandidate1.setBirthPlace(candid.getString("Birthplace"));
                                    conventionalCandidate1.setFirstName(candid.getString("FirstName"));
                                    conventionalCandidate1.setMiddleName(candid.getString("MiddleName"));
                                    conventionalCandidate1.setLastName(candid.getString("LastName"));
                                    conventionalCandidate1.setNationality(candid.getString("Nationality"));
                                    conventionalCandidate1.setFatherName(candid.getString("FatherName"));
                                    conventionalCandidate1.setFatherDateOfBirth(candid.getString("FatherDOB"));
                                    conventionalCandidate1.setInsufficiencyRemarks(candid.getString("Insufficiency_Remarks"));
                                    ConventionalCandidate updateConventionalCandidate = conventionalCandidateRepository.save(conventionalCandidate1);
                                    log.info("update conventional cnadidate  data saved ");
                                }
                                if (liCandidateInformation.isNull("liCandidateAddressInfo") == false) {
                                    //update conventional candidate address
                                    candidateCafAddressRepository.deleteAllByCandidateCandidateId(updateCandidate.getCandidateId());
                                    log.info("deleted the cafaddress by conventional candidate id");
                                    conventionCafAddressRepository.deleteAllByConventionalRequestId(updateCandidate.getConventionalRequestId());
                                    log.info("deleted the conventional address by conventional for update action");
                                    JSONArray liCandidateAddressInfo = liCandidateInformation.getJSONArray("liCandidateAddressInfo");
                                    List<JSONObject> addressCollect = IntStream.range(0, liCandidateAddressInfo.length()).mapToObj(index -> ((JSONObject) liCandidateAddressInfo.get(index))).collect(Collectors.toList());
                                    addressCollect.forEach(updateconvaddress -> {
                                        CandidateCafAddress candidateCafAddress = new CandidateCafAddress();
                                        candidateCafAddress.setCandidate(updateCandidate);
                                        candidateCafAddress.setState(updateconvaddress.getString("State"));
                                        candidateCafAddress.setPinCode(updateconvaddress.getString("PinCode"));
                                        candidateCafAddress.setCity(updateconvaddress.getString("City"));
                                        candidateCafAddress.setCreatedOn(new Date());
                                        candidateCafAddress.setCandidateAddress(updateconvaddress.getString("HouseNumber") + "," + updateconvaddress.getString("StreetAddress") + "," + updateconvaddress.getString("City") + "," + updateconvaddress.getString("State") + "" + updateconvaddress.getString("Country") + "," + updateconvaddress.getString("ProminentLandmark"));
                                        CandidateCafAddress saveCandidateCafAddress = candidateCafAddressRepository.save(candidateCafAddress);
                                        //setting the other fields not present in candidateaddress table to conventional caniddateaddress
                                        ConventionalCafAddress conventionalCafAddress = new ConventionalCafAddress();
                                        conventionalCafAddress.setConventionalCandidateId(updateCandidate.getConventionalCandidateId());
                                        conventionalCafAddress.setConventionalRequestId(updateCandidate.getConventionalRequestId());
                                        conventionalCafAddress.setCandidateCafAddressId(saveCandidateCafAddress.getCandidateCafAddressId());
                                        conventionalCafAddress.setAddressType(updateconvaddress.getString("AddressType"));
                                        conventionalCafAddress.setContactInfo(updateconvaddress.getString("ContactInfo"));
                                        conventionalCafAddress.setInsufficiencyRemarks(updateconvaddress.getString("Insufficiency_Remarks"));
                                        conventionalCafAddress.setHouseType(updateconvaddress.getString("HouseType"));
                                        try {
                                            conventionalCafAddress.setStayFromDate(dateFormater.parse(updateconvaddress.getString("StayFromDate")));
                                            conventionalCafAddress.setStayToDate(dateFormater.parse(updateconvaddress.getString("StayToDate")));
                                        } catch (ParseException e) {
                                            throw new RuntimeException(e);
                                        }
                                        ConventionalCafAddress savedConventionalCafAddress = conventionCafAddressRepository.save(conventionalCafAddress);
                                        log.info("update candidate address information");
                                    });
                                }
                                //updated conventional education information
                                candidateCafEducationRepository.deleteAllByCandidateConventionalRequestId(updateCandidate.getConventionalRequestId());
                                log.info("deleted the  caf education by conventional candidate id update action ");
                                conventionalCafCandidateEducationRepository.deleteAllByConventionalRequestId(updateCandidate.getConventionalRequestId());
                                log.info("deleted the Conventional  caf education by conventional for update action");
                                if (liCandidateInformation.isNull("liCandidateEducationInfo") == false) {
                                    JSONArray liCandidateEducationInfo = liCandidateInformation.getJSONArray("liCandidateEducationInfo");
                                    List<JSONObject> educationCollect = IntStream.range(0, liCandidateEducationInfo.length()).mapToObj(index -> ((JSONObject) liCandidateEducationInfo.get(index))).collect(Collectors.toList());
                                    educationCollect.forEach(updateEducandid -> {
                                        CandidateCafEducation candidateCafEducation = new CandidateCafEducation();
                                        candidateCafEducation.setCandidate(updateCandidate);
                                        candidateCafEducation.setSchoolOrCollegeName(updateEducandid.getString("CollegeName"));
                                        candidateCafEducation.setBoardOrUniversityName(updateEducandid.getString("UniversityName"));
                                        candidateCafEducation.setYearOfPassing(updateEducandid.getString("MonthYearOfPassing"));
                                        QualificationMaster bebtech = qualificationMasterRepository.findByQualificationCode("BEBTECH");
                                        candidateCafEducation.setQualificationMaster(bebtech);
                                        CandidateStatus candidateStatus = new CandidateStatus();
                                        candidateStatus.setCandidate(updateCandidate);
                                        candidateStatus.setCreatedBy(user);
                                        candidateStatus.setCandidateStatusId(1l);
                                        candidateStatus.setCreatedOn(new Date());
                                        CandidateStatus save = candidateStatusRepository.save(candidateStatus);
                                        candidateCafEducation.setCandidateStatus(save);
                                        candidateCafEducation.setCreatedOn(new Date());
                                        candidateCafEducation.setQualificationType(updateEducandid.getString("QualificationType"));
                                        candidateCafEducation.setCountry(updateEducandid.getString("Country"));
                                        CandidateCafEducation savedcafEducation = candidateCafEducationRepository.save(candidateCafEducation);
                                        //setting the other fields not present in candidateaddress table to conventional caniddateaddress
                                        ConventionalCandidateCafEducation conventionalCandidateCafEducation = new ConventionalCandidateCafEducation();
                                        conventionalCandidateCafEducation.setCandidateCafEducationId(savedcafEducation.getCandidateCafEducationId());
                                        conventionalCandidateCafEducation.setEducationType(updateEducandid.getString("EducationType"));
                                        conventionalCandidateCafEducation.setDegreeType(updateEducandid.getString("DegreeType"));
                                        if (updateEducandid.isNull("IsSuspect") == false) {
                                            conventionalCandidateCafEducation.setIsSuspect(updateEducandid.getString("IsSuspect"));
                                        }
                                        if (updateEducandid.isNull("Reason_For_Suspect") == false) {
                                            conventionalCandidateCafEducation.setReasonForSuspect(updateEducandid.getString("Reason_For_Suspect"));
                                        }
                                        conventionalCandidateCafEducation.setConventionalCandidateId(updateCandidate.getConventionalCandidateId());
                                        conventionalCandidateCafEducation.setConventionalRequestId(updateCandidate.getConventionalRequestId());
                                        conventionalCandidateCafEducation.setInsufficiencyRemarks(updateEducandid.getString("Insufficiency_Remarks"));
                                        try {
                                            conventionalCandidateCafEducation.setStartDate(dateFormater.parse(updateEducandid.getString("StartDate")));
                                            conventionalCandidateCafEducation.setEndDate(dateFormater.parse(updateEducandid.getString("EndDate")));
                                        } catch (ParseException e) {
                                            throw new RuntimeException(e);
                                        }
                                        ConventionalCandidateCafEducation savedConventionalCafEducational = conventionalCafCandidateEducationRepository.save(conventionalCandidateCafEducation);
                                        log.info("updated candidate  data in conventional education and basic education");
                                    });

                                }
                                //update conventional experience information
                                candidateCafExperienceRepository.deleteAllByCandidateConventionalRequestId(updateCandidate.getConventionalRequestId());
                                log.warn("delted candidate caf experience for update action  ");
                                conventionalCandidateExperienceRepository.deleteAllByConventionalRequestId(obj1.getLong("RequestID"));
                                log.warn("delted conventional caf experience by conventional  for update action");
                                if (liCandidateInformation.isNull("liCandidateEmploymentInfo") == false) {
                                    JSONArray liCandidateEmploymentInfo = liCandidateInformation.getJSONArray("liCandidateEmploymentInfo");
                                    List<JSONObject> empCollect = IntStream.range(0, liCandidateEmploymentInfo.length()).mapToObj(index -> ((JSONObject) liCandidateEmploymentInfo.get(index))).collect(Collectors.toList());
                                    empCollect.forEach(updateExpCandid -> {
                                        CandidateCafExperience candidateCafExperience = new CandidateCafExperience();
                                        candidateCafExperience.setCandidate(updateCandidate);
                                        candidateCafExperience.setCandidateEmployerName(updateExpCandid.getString("CompanyName"));
                                        if (updateExpCandid.isNull("UAN_Number") == false) {
                                            candidateCafExperience.setUan(updateExpCandid.getString("UAN_Number"));
                                        }
                                        try {
                                            candidateCafExperience.setInputDateOfJoining(dateFormater.parse(updateExpCandid.getString("FromDate")));
                                            candidateCafExperience.setInputDateOfExit(dateFormater.parse(updateExpCandid.getString("ToDate")));
                                        } catch (ParseException e) {
                                            throw new RuntimeException(e);
                                        }
                                        candidateCafExperience.setCreatedOn(new Date());
                                        CandidateCafExperience savedcafExperience = candidateCafExperienceRepository.save(candidateCafExperience);
                                        //setting the other fields not present in candidateaddress table to conventional caniddateaddress
                                        ConventionalCandidateExperience conventionalCandidateExperience = new ConventionalCandidateExperience();
                                        conventionalCandidateExperience.setCandidateCafExperience(savedcafExperience.getCandidateCafExperienceId());
                                        conventionalCandidateExperience.setEmploymentType(updateExpCandid.getString("EmploymentType"));
                                        conventionalCandidateExperience.setDuration(updateExpCandid.getString("Duration"));
                                        conventionalCandidateExperience.setDesignation(updateExpCandid.getString("Designation"));
                                        conventionalCandidateExperience.setEmployeeCode(updateExpCandid.getString("EmployeeCode"));
                                        conventionalCandidateExperience.setHrName(updateExpCandid.getString("HRName"));
                                        conventionalCandidateExperience.setHrContactNumber(updateExpCandid.getString("HRContactNumber"));
                                        conventionalCandidateExperience.setHrEmailId(updateExpCandid.getString("HREmailID"));
                                        conventionalCandidateExperience.setSuperiorName(updateExpCandid.getString("SuperiorName"));
                                        conventionalCandidateExperience.setSuperiorContactNumber(updateExpCandid.getString("SuperiorContactNumber"));
                                        conventionalCandidateExperience.setSuperiorEmailID(updateExpCandid.getString("SuperiorEmailID"));
                                        conventionalCandidateExperience.setSuperiorDesignation(updateExpCandid.getString("SuperiorDesignation"));
                                        conventionalCandidateExperience.setLastSalary(updateExpCandid.getString("LastSalary"));
                                        conventionalCandidateExperience.setGrossSalary(updateExpCandid.getString("GrossSalary"));
                                        conventionalCandidateExperience.setInsufficiencyRemarks(updateExpCandid.getString("Insufficiency_Remarks"));
                                        if (updateExpCandid.isNull("IsSuspect") == false) {
                                            conventionalCandidateExperience.setIsSuspect(updateExpCandid.getString("IsSuspect"));
                                        }
                                        if (updateExpCandid.isNull("Office_Address") == false) {
                                            conventionalCandidateExperience.setOfficeAddress(updateExpCandid.getString("Office_Address"));
                                        }
                                        if (updateExpCandid.isNull("Reason_For_Suspect") == false) {
                                            conventionalCandidateExperience.setReasonForSuspect(updateExpCandid.getString("Reason_For_Suspect"));
                                        }
                                        conventionalCandidateExperience.setConventionalCandidateId(updateCandidate.getConventionalCandidateId());
                                        conventionalCandidateExperience.setConventionalRequestId(updateCandidate.getConventionalRequestId());
                                        ConventionalCandidateExperience savedConventionalCandidateExperience = conventionalCandidateExperienceRepository.save(conventionalCandidateExperience);
                                        log.info("updated candidate experience information");
                                    });
                                }
                                //update drug information
                                conventionalCandidateDrugInfoRepository.deleteAllByConventionalRequestId(updateCandidate.getConventionalRequestId());
                                log.warn("delted conventional candidate drug info by conventional for update action");
                                if (liCandidateInformation.isNull("liCandidateDrugInfo") == false) {
                                    JSONArray liCandidateDrugInfo = liCandidateInformation.getJSONArray("liCandidateDrugInfo");
                                    List<JSONObject> drugCollect = IntStream.range(0, liCandidateDrugInfo.length()).mapToObj(index -> ((JSONObject) liCandidateDrugInfo.get(index))).collect(Collectors.toList());
                                    drugCollect.forEach(updateDrugcandid -> {
                                        ConventionalCandidateDrugInfo candidateDrugInfo = new ConventionalCandidateDrugInfo();
                                        candidateDrugInfo.setCandidateId(updateCandidate.getConventionalCandidateId());
                                        candidateDrugInfo.setConventionalRequestId(updateCandidate.getConventionalRequestId());
                                        candidateDrugInfo.setName(updateDrugcandid.getString("Name"));
                                        candidateDrugInfo.setContactNumber(updateDrugcandid.getString("ContactNumber"));
                                        try {
                                            candidateDrugInfo.setSampleCollectionDate(dateFormater.parse(updateDrugcandid.getString("SampleCollectionDate")));
                                        } catch (ParseException e) {
                                            throw new RuntimeException(e);
                                        }
                                        if (updateDrugcandid.has("Remarks")) {
                                            candidateDrugInfo.setRemarks(updateDrugcandid.getString("Remarks"));
                                        }
                                        candidateDrugInfo.setHouseNumber(updateDrugcandid.getString("HouseNumber"));
                                        candidateDrugInfo.setStreetAddress(updateDrugcandid.getString("StreetAddress"));
                                        candidateDrugInfo.setCity(updateDrugcandid.getString("City"));
                                        candidateDrugInfo.setState(updateDrugcandid.getString("State"));
                                        candidateDrugInfo.setCountry(updateDrugcandid.getString("Country"));
                                        candidateDrugInfo.setPincode(updateDrugcandid.getString("PinCode"));
                                        candidateDrugInfo.setCreatedOn(new Date());
                                        candidateDrugInfo.setProminentLandmark(updateDrugcandid.getString("ProminentLandmark"));
                                        candidateDrugInfo.setConventionalCandidateId(updateCandidate.getConventionalCandidateId());
                                        ConventionalCandidateDrugInfo savedConventionalDrugInfo = conventionalCandidateDrugInfoRepository.save(candidateDrugInfo);
                                        log.info("updated conventional drug information");
                                    });
                                }
                                //update reference data
                                conventionalCandidateReferenceInfoRepository.deleteAllByConventionalRequestId(updateCandidate.getConventionalRequestId());
                                log.warn("deleted the conventional reference info for update action");
                                if (liCandidateInformation.isNull("liCandidateReferenceInfo") == false) {
                                    JSONArray liCandidateEmploymentInfo = liCandidateInformation.getJSONArray("liCandidateReferenceInfo");
                                    List<JSONObject> refCollect = IntStream.range(0, liCandidateEmploymentInfo.length()).mapToObj(index -> ((JSONObject) liCandidateEmploymentInfo.get(index))).collect(Collectors.toList());

                                    refCollect.forEach(updateRefCandid -> {
                                        ConventionalCandidateReferenceInfo conventionalCandidateReference = new ConventionalCandidateReferenceInfo();
                                        conventionalCandidateReference.setCandidateId(updateCandidate.getConventionalCandidateId());
                                        conventionalCandidateReference.setConventionalRequestId(updateCandidate.getConventionalRequestId());
                                        conventionalCandidateReference.setName(updateRefCandid.getString("Name"));
                                        conventionalCandidateReference.setReferenceNumber(updateRefCandid.getString("ReferenceNumber"));
                                        conventionalCandidateReference.setDesignation(updateRefCandid.getString("Designation"));
                                        conventionalCandidateReference.setCompanyName(updateRefCandid.getString("CompanyName"));
                                        conventionalCandidateReference.setContactNumber(updateRefCandid.getString("ContactNumber"));
                                        conventionalCandidateReference.setDurationKnown(updateRefCandid.getString("DurationKnown"));
                                        conventionalCandidateReference.setEmailId(updateRefCandid.getString("EmailID"));
                                        conventionalCandidateReference.setProfessionalRelation(updateRefCandid.getString("ProfessionalRelation"));
                                        conventionalCandidateReference.setInsufficiencyRemarks(updateRefCandid.getString("Insufficiency_Remarks"));
                                        ConventionalCandidateReferenceInfo saveConventionalCandidateReference = conventionalCandidateReferenceInfoRepository.save(conventionalCandidateReference);
                                        log.info("updated conventional reference information ");
                                    });
                                }

                                //update cetfificate data
                                conventionalCandidateCertificateInfoRepository.deleteAllByRequestId(updateCandidate.getConventionalRequestId());
                                log.warn("deleted the conventional certificate info for update action");
                                if (liCandidateInformation.isNull("liCandidateCertificateInfo") == false) {
                                    JSONArray liCandidateCertificateInfo = liCandidateInformation.getJSONArray("liCandidateCertificateInfo");
                                    List<JSONObject> certificateCollect = IntStream.range(0, liCandidateCertificateInfo.length()).mapToObj(index -> ((JSONObject) liCandidateCertificateInfo.get(index))).collect(Collectors.toList());

                                    certificateCollect.forEach(updateRefCandid -> {
                                        ConventionalCandidateCertificateInfo conventionalCandidateCertificateInfo = new ConventionalCandidateCertificateInfo();
                                        conventionalCandidateCertificateInfo.setRequestId(updateCandidate.getConventionalRequestId());
                                        conventionalCandidateCertificateInfo.setCourseName(updateRefCandid.getString("Course_Name"));
                                        conventionalCandidateCertificateInfo.setCourseCompletionDate(updateRefCandid.getString("Course_CompletionDate"));
                                        conventionalCandidateCertificateInfo.setCourseStartDate(updateRefCandid.getString("Course_StartDate"));
                                        conventionalCandidateCertificateInfo.setCreatedBy(SecurityHelper.getCurrentUser().getUserName());
                                        conventionalCandidateCertificateInfo.setCreatedOn(new Date());
                                        conventionalCandidateCertificateInfo.setInstituteContactNumber(updateRefCandid.getString("Institute_ContactNumber"));
                                        conventionalCandidateCertificateInfo.setInstituteName(updateRefCandid.getString("Institute_Name"));
                                        conventionalCandidateCertificateInfo.setInstituteEmailID(updateRefCandid.getString("Institute_EmailID"));
                                        ConventionalCandidateCertificateInfo savecCandidateCertificateInfo = conventionalCandidateCertificateInfoRepository.save(conventionalCandidateCertificateInfo);
                                        log.info("candidate certificate data updated");
                                    });
                                }
                            }

                        });
                    }

                    stringServiceOutcome.setData("candidate Data Refetched Sucessfully");
                    stringServiceOutcome.setOutcome(true);
                    stringServiceOutcome.setStatus("200");
                }
            }
            if (checkFetchFlag) {
                addUpdateLiCheckToPerformData(fetchVendorConventionalCandidateDto, message);
                stringServiceOutcome.setData("Checks Data Refetched Sucessfully");
                stringServiceOutcome.setOutcome(true);
                stringServiceOutcome.setStatus("200");
            }


        } catch (Exception e) {
            log.info(e.getMessage());
            stringServiceOutcome.setData("No Able to Refetch Data");
            stringServiceOutcome.setOutcome(false);
            stringServiceOutcome.setStatus("404");
        }
        return stringServiceOutcome;
    }

    public ServiceOutcome<DashboardDto> exportToExcelByDateRange(DashboardDto dashboardDto) {
        ServiceOutcome<DashboardDto> listServiceOutcome = new ServiceOutcome<>();
        String strToDate = "";
        String strFromDate = "";
        List<ConventionalVendorCandidatesSubmitted> candidatesSubmittedList = new ArrayList<ConventionalVendorCandidatesSubmitted>();
        try {
            strToDate = dashboardDto.getToDate() != null ? dashboardDto.getToDate() : ApplicationDateUtils.getStringTodayAsDDMMYYYY();
            strFromDate = dashboardDto.getFromDate() != null ? dashboardDto.getFromDate() : ApplicationDateUtils.subtractNoOfDaysFromDateAsDDMMYYYY(new SimpleDateFormat("dd/MM/yyyy").parse(strToDate), 7);
            Date startDate = formatter.parse(strFromDate + " 00:00:00");
            Date endDate = formatter.parse(strToDate + " 23:59:59");
            User user = userRepository.findById(dashboardDto.getUserId()).get();
            User byOrganizationAndRoleId = userRepository.findByOrganizationAndRoleId(user.getOrganization().getOrganizationId(), user.getRole().getRoleId(), user.getUserId());
            if (byOrganizationAndRoleId != null) {
                candidatesSubmittedList = conventionalVendorCandidatesSubmittedRepository.findAllByUserIdAndDateRangeForExportExcel(startDate, endDate);
                DashboardDto dashboardDtoObj = new DashboardDto(strFromDate, strToDate, null, null, null,
                        dashboardDto.getUserId(), dashboardDto.getStatus(), candidatesSubmittedList, dashboardDto.getPageNumber(), "0");
                listServiceOutcome.setData(dashboardDtoObj);
                listServiceOutcome.setMessage("Candidate fetched Sucessfully");
                listServiceOutcome.setOutcome(true);
            }
            if (candidatesSubmittedList.isEmpty() == true) {
                listServiceOutcome.setData(new DashboardDto(strFromDate, strToDate, null, null, null,
                        dashboardDto.getUserId(), dashboardDto.getStatus(), new ArrayList<ConventionalVendorCandidatesSubmitted>(), dashboardDto.getPageNumber(), "0"));
                listServiceOutcome.setMessage("Candidate Data Not Found");
                listServiceOutcome.setOutcome(false);
            }

        } catch (Exception e) {
            listServiceOutcome.setData(new DashboardDto(strFromDate, strToDate, null, null, null,
                    dashboardDto.getUserId(), dashboardDto.getStatus(), new ArrayList<ConventionalVendorCandidatesSubmitted>(), dashboardDto.getPageNumber(), "0"));
            listServiceOutcome.setMessage("Candidate fetched Failed");
            listServiceOutcome.setOutcome(false);
            log.info(e.getMessage());
        }
        return listServiceOutcome;
    }


    private String getExecutiveSummaryValueForCheck(Long checkUniqueId) {
        String matchedAttribute = null;
        try {

            ConventionalVendorliChecksToPerform byCheckUniqueId = liCheckToPerformRepository.findByCheckUniqueId(checkUniqueId);
            VendorUploadChecks vendorChecksss = vendorUploadChecksRepository.findByVendorChecksVendorcheckId(byCheckUniqueId.getVendorChecks().getVendorcheckId());
            if (vendorChecksss != null) {
                for (String s : vendorChecksss.getVendorAttirbuteValue()) {
                    // Split the string by ":" to separate key and value
                    String[] parts = s.split("=");

                    // Assuming key is the first part and value is the second part
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        if (key.equalsIgnoreCase("Employers Name")) {
                            matchedAttribute = value;

                        }
                        if (key.equalsIgnoreCase("Qualification Attained")) {
                            matchedAttribute = value;
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.info(e.getMessage());
        }
        return matchedAttribute;
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceOutcome<String> updateCheckStatus(String checkUniqueId, String selectedCheckStatusId) throws Exception {
        ServiceOutcome<String> stringServiceOutcome = new ServiceOutcome<>();
        try {
            ConventionalVendorliChecksToPerform byCheckUniqueId = liCheckToPerformRepository.findByCheckUniqueId(Long.valueOf(checkUniqueId));
            VendorChecks byVendorcheckId = vendorChecksRepository.findByVendorcheckId(byCheckUniqueId.getVendorChecks().getVendorcheckId());
            VendorCheckStatusMaster byVendorCheckStatusMasterId = vendorCheckStatusMasterRepository.findByVendorCheckStatusMasterId(Long.valueOf(selectedCheckStatusId));
            if (byCheckUniqueId != null && byVendorcheckId != null && byVendorCheckStatusMasterId != null) {
                if (byVendorCheckStatusMasterId.getVendorCheckStatusMasterId() != 1l) {
                    if (byVendorCheckStatusMasterId.getVendorCheckStatusMasterId() != 2l) {
                        byCheckUniqueId.setCheckStatus(byVendorCheckStatusMasterId);
                        ConventionalVendorliChecksToPerform updatedLicheckStatus = liCheckToPerformRepository.saveAndFlush(byCheckUniqueId);
                        byVendorcheckId.setVendorCheckStatusMaster(byVendorCheckStatusMasterId);
                        VendorChecks updatedVendorCheckStatus = vendorChecksRepository.saveAndFlush(byVendorcheckId);
                        CheckUniqueIdRequest checkUniqueIdRequest = new CheckUniqueIdRequest();
                        checkUniqueIdRequest.setCheckUniqueIds(List.of(String.valueOf(byCheckUniqueId.getCheckUniqueId())));
                        ServiceOutcome<String> triggerInProgressServiceoutcome = UpdateBGVCheckStatusRowwiseByAgentLogin(checkUniqueIdRequest);
                        if (triggerInProgressServiceoutcome.getOutcome() == true) {
                            stringServiceOutcome.setMessage(triggerInProgressServiceoutcome.getMessage() + "to" + byVendorCheckStatusMasterId.getCheckStatusCode());
                            stringServiceOutcome.setOutcome(true);
                        } else {
                            stringServiceOutcome.setMessage("Failed To Update /n" + triggerInProgressServiceoutcome.getMessage());
                            stringServiceOutcome.setOutcome(false);
                            throw new Exception("Failed To Update /n" + triggerInProgressServiceoutcome.getMessage());
                        }
                    }
                    if (byVendorCheckStatusMasterId.getVendorCheckStatusMasterId() == 2l) {
                        byCheckUniqueId.setCheckStatus(byVendorCheckStatusMasterId);
                        ConventionalVendorliChecksToPerform updatedLicheckStatus = liCheckToPerformRepository.saveAndFlush(byCheckUniqueId);
                        byVendorcheckId.setVendorCheckStatusMaster(byVendorCheckStatusMasterId);
                        VendorChecks updatedVendorCheckStatus = vendorChecksRepository.saveAndFlush(byVendorcheckId);


                        ServiceOutcome<String> triggerInProgressServiceoutcome = updateBgvCheckRowwiseonProgress(Long.valueOf(byCheckUniqueId.getRequestId()), byCheckUniqueId.getCheckUniqueId());
                        if (triggerInProgressServiceoutcome.getOutcome() == true) {
                            stringServiceOutcome.setMessage(triggerInProgressServiceoutcome.getMessage() + "to" + byVendorCheckStatusMasterId.getCheckStatusCode());
                            stringServiceOutcome.setOutcome(true);
                        } else {
                            stringServiceOutcome.setMessage("Failed To Update /n" + triggerInProgressServiceoutcome.getMessage());
                            stringServiceOutcome.setOutcome(false);
                            throw new Exception("Failed To Update /n" + triggerInProgressServiceoutcome.getMessage());
                        }
                    }

                } else {
                    stringServiceOutcome.setMessage("Not Able to Update To " + byVendorCheckStatusMasterId.getCheckStatusCode());
                    stringServiceOutcome.setOutcome(false);
                    throw new Exception("Not Able to Update To " + byVendorCheckStatusMasterId.getCheckStatusCode());
                }
            }
        } catch (Exception e) {
            stringServiceOutcome.setMessage(e.getMessage());
            stringServiceOutcome.setOutcome(false);
        }
        return stringServiceOutcome;
    }

    @Transactional
    public void updateLiCheckHistory(ConventionalVendorliChecksToPerform conventionalVendorliChecksToPerform, ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted, String response) {
        try {
            LicheckHistory licheckHistory = new LicheckHistory();
            licheckHistory.setCandidateId(conventionalVendorliChecksToPerform.getCandidateId());
            licheckHistory.setCheckName(conventionalVendorliChecksToPerform.getCheckName());
            licheckHistory.setCheckUniqueId(conventionalVendorliChecksToPerform.getCheckUniqueId());
            licheckHistory.setCreatedBy(conventionalVendorliChecksToPerform.getCreatedBy().getUserName());
            licheckHistory.setCreatedOn(new Date());
            licheckHistory.setCheckStatus(conventionalVendorliChecksToPerform.getCheckStatus().getCheckStatusCode());
            licheckHistory.setCandidateStatus(conventionalVendorCandidatesSubmitted.getStatus().getStatusCode());
            licheckHistory.setRequestType(conventionalVendorCandidatesSubmitted.getRequestType());
            licheckHistory.setRequestId(Long.valueOf(conventionalVendorliChecksToPerform.getRequestId()));
            licheckHistory.setCheckResponse(response);
            LicheckHistory save = licheckHistoryRepository.save(licheckHistory);
            log.info("Licheck History Saved for Check Unique Id  - " + save.getCheckUniqueId() + " -for  status - " + save.getCheckStatus());
        } catch (Exception e) {
            log.info("Exception in checkStatus Update");
        }
    }

    public boolean setPendingInterimReportOnBasicChecksFlag(Long requestId) {
        boolean allChecksUpdated = false;
        try {
            List<ConventionalVendorliChecksToPerform> byRequestId =
                    liCheckToPerformRepository.findByRequestId(String.valueOf(requestId));

            // Predicate to check if status is not 2L or 7L
            Predicate<VendorCheckStatusMaster> isValidStatus =
                    status -> status != null && status.getVendorCheckStatusMasterId() != 2L && status.getVendorCheckStatusMasterId() != 7L;

            // Check if the global check passes
            boolean globalCheckPassed = byRequestId.stream()
                    .filter(check -> "GLOBAL DATABASE CHECK".equalsIgnoreCase(check.getCheckName()))
                    .anyMatch(check -> isValidStatus.test(check.getCheckStatus()));

            // Check if the OFAC check passes
            boolean ofacCheckPassed = byRequestId.stream()
                    .filter(check -> "OFAC CHECK".equalsIgnoreCase(check.getCheckName()))
                    .anyMatch(check -> isValidStatus.test(check.getCheckStatus()));

            // Check if any identity check (PAN, AADHAR, etc.) passes
            boolean identityCheckPassed = byRequestId.stream()
                    .filter(check -> check.getCheckName().toUpperCase().startsWith("IDENTITY CHECK"))
                    .anyMatch(check -> isValidStatus.test(check.getCheckStatus()));

            // Check if both criminal checks pass
            boolean criminalCheckPassed = byRequestId.stream()
                    .filter(check -> check.getCheckName().toUpperCase().contains("CRIMINAL"))
                    .allMatch(check -> isValidStatus.test(check.getCheckStatus()));

            // If any of the checks pass according to the defined rules, return true
            return globalCheckPassed && ofacCheckPassed && identityCheckPassed && criminalCheckPassed;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception in setPendingInterimReportOnBasicChecksFlag()");
        }
        return allChecksUpdated;

    }

    @Autowired
    ConventionalPendingReportHistoryRepository conventionalPendingReportHistoryRepository;

    @Transactional
    public ServiceOutcome<String> triggerPendingReport(String requestId, ReportType reportType, String update) {

        ServiceOutcome<String> listServiceOutcome = new ServiceOutcome<>();
        List<liReportDetails> liReportDetails = new ArrayList<>();
        ResponseEntity<String> icheckRepsonse = null;
        String responedata = null;
        CandidatePendingReportHistory candidatePendingReportHistory = null;
        try {
            PendingReportsDto conventionalVendorCandidatesSubmitted = new PendingReportsDto();
            com.aashdit.digiverifier.vendorcheck.dto.liReportDetails liReportDetails1 = new liReportDetails();
            ConventionalVendorCandidatesSubmitted conventinalCandidate = conventionalCandidatesSubmittedRepository.findByRequestId(requestId);
            candidatePendingReportHistory = conventionalPendingReportHistoryRepository.findById(Long.valueOf(requestId))
                    .orElseGet(() -> new CandidatePendingReportHistory());


            StatusMaster currentStatus = conventinalCandidate.getStatus();
            candidatePendingReportHistory.setCreatedBy(SecurityHelper.getCurrentUser());
            candidatePendingReportHistory.setCreatedOn(new Date());
            candidatePendingReportHistory.setRequestId(Long.valueOf(conventinalCandidate.getRequestId()));
            candidatePendingReportHistory.setRequestType(reportType.label + " Pending");
            candidatePendingReportHistory.setOldstatus(currentStatus);


            conventionalVendorCandidatesSubmitted.setCandidateID(String.valueOf(conventinalCandidate.getCandidateId()));
            conventionalVendorCandidatesSubmitted.setName(conventinalCandidate.getName());
            conventionalVendorCandidatesSubmitted.setPSNO(conventinalCandidate.getPsNo());
            conventionalVendorCandidatesSubmitted.setRequestID(conventinalCandidate.getRequestId());
            conventionalVendorCandidatesSubmitted.setVendorName(conventinalCandidate.getVendorId());
            Candidate candidate = candidateRepository.findByConventionalRequestId(Long.valueOf(conventionalVendorCandidatesSubmitted.getRequestID()));
            ServiceOutcome<String> stringServiceOutcome = generateConventionalCandidateReport(candidate.getCandidateId(), reportType, update, null);
            String reportUploadedPrecisedUrl = stringServiceOutcome.getData();
            if (reportUploadedPrecisedUrl != null) {
                listServiceOutcome.setData(reportUploadedPrecisedUrl);
                List<Content> allByCandidateId = contentRepository.findAllByCandidateId(candidate.getCandidateId());
                allByCandidateId.forEach(content -> {
                    String bucketName = content.getBucketName();
                    String path = content.getPath();
                    String[] split = path.split("/");
                    String filename = split[split.length - 1];
                    String fileExtension = filename.substring(filename.length() - 4, filename.length());
                    liReportDetails1.setReportFileExtention(fileExtension);
                    liReportDetails1.setReportFileName(filename);
                    try {
                        byte[] bytes = awsUtils.getbyteArrayFromS3(bucketName, path);
                        String base64String = Base64.getEncoder().encodeToString(bytes);
                        liReportDetails1.setReportAttachment(base64String);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

            }

            ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted1 = conventionalCandidatesSubmittedRepository.findByRequestId(conventionalVendorCandidatesSubmitted.getRequestID());
            if (reportType.label.equalsIgnoreCase("INTERIM")) {
                liReportDetails1.setReportType("1");
            }
            if (reportType.label.equalsIgnoreCase("FINAL")) {
                liReportDetails1.setReportType("3");
            }
            if (reportType.label.equalsIgnoreCase("Supplimentry")) {
                liReportDetails1.setReportType("2");
            }

            if (conventionalVendorCandidatesSubmitted1.getVerificationStatus().equalsIgnoreCase("CLEAR")) {
                liReportDetails1.setReportStatus("1");
            }

            if (conventionalVendorCandidatesSubmitted1.getVerificationStatus().equalsIgnoreCase("INPROGRESS")) {
                liReportDetails1.setReportStatus("2");
            }

            if (conventionalVendorCandidatesSubmitted1.getVerificationStatus().equalsIgnoreCase("INSUFFICIENCY")) {
                liReportDetails1.setReportStatus("3");
            }
            if (conventionalVendorCandidatesSubmitted1.getVerificationStatus().equalsIgnoreCase("MAJORDISCREPANCY")) {
                liReportDetails1.setReportStatus("4");
            }

            if (conventionalVendorCandidatesSubmitted1.getVerificationStatus().equalsIgnoreCase("MINORDISCREPANCY")) {
                liReportDetails1.setReportStatus("5");
            }

            if (conventionalVendorCandidatesSubmitted1.getVerificationStatus().equalsIgnoreCase("UNABLETOVERIFIY")) {
                liReportDetails1.setReportStatus("6");
            }

            liReportDetails1.setVendorReferenceID(String.valueOf(conventionalVendorCandidatesSubmitted1.getApplicantId()));
            liReportDetails.add(liReportDetails1);
            conventionalVendorCandidatesSubmitted.setLiReportDetails(liReportDetails);

            //hitting the update request to third party api
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", environmentVal.getMtGrantType());
            map.add("username", environmentVal.getMtUsername());
            map.add("password", environmentVal.getMtPassword());
            HttpHeaders tokenHeader = new HttpHeaders();
            tokenHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            ResponseEntity<String> responseEntity = null;
            HttpEntity<MultiValueMap<String, String>> requestBodyFormUrlEncoded = new HttpEntity<>(map, tokenHeader);
            responseEntity = restTemplate.postForEntity(environmentVal.getConventionalVendorToken(), requestBodyFormUrlEncoded, String.class);
            JSONObject tokenObject = new JSONObject(responseEntity.getBody());
            String access_token = tokenObject.getString("access_token");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + access_token);
            headers.set("Content-Type", "application/json");
            headers.setContentType(MediaType.APPLICATION_JSON);
            log.info("report label" + reportType.label);
            if (update.equalsIgnoreCase("UPDATE")) {
                HttpEntity<PendingReportsDto> liCheckDtoHttpEntity = new HttpEntity<>(conventionalVendorCandidatesSubmitted, headers);
                icheckRepsonse = restTemplate.exchange(environmentVal.getConventionalVendoruploadBgvPendingReports(), HttpMethod.POST, liCheckDtoHttpEntity, String.class);
                int statusCodeValue = icheckRepsonse.getStatusCodeValue();
                responedata = icheckRepsonse.getBody();
                listServiceOutcome.setMessage(icheckRepsonse.getBody());
                if (statusCodeValue == 200) {
                    if (reportType.label.equalsIgnoreCase("interim")) {
                        StatusMaster interimreport = statusMasterRepository.findByStatusCode("INTERIMREPORT");
                        log.info("Response On " + reportType.label + " Report  Generation for Request Id " + requestId + " ---" + icheckRepsonse);
                        log.debug("interim updated");
                        conventionalVendorCandidatesSubmitted1.setStatus(interimreport);
                        ConventionalVendorCandidatesSubmitted updatedToInterim = conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted1);
                        candidatePendingReportHistory.setNewstatus(updatedToInterim.getStatus());
                        candidatePendingReportHistory.setReportStatusReponse(icheckRepsonse.toString());
                        conventionalPendingReportHistoryRepository.save(candidatePendingReportHistory);
                        listServiceOutcome.setData(responedata);
                        listServiceOutcome.setMessage(icheckRepsonse.getBody());
                        listServiceOutcome.setStatus(String.valueOf(icheckRepsonse.getStatusCode()));
                        listServiceOutcome.setOutcome(true);
                        log.info("Pending  Report For  Request id" + updatedToInterim.getRequestId() + " Ltm response \n" + responedata);
                    }

                    if (reportType.label.equalsIgnoreCase("final")) {
                        StatusMaster interimreport = statusMasterRepository.findByStatusCode("FINALREPORT");
                        log.info("Response On " + reportType.label + " Report  Generation for Request Id " + requestId + " ---" + icheckRepsonse);
                        log.debug("final report updated");
                        conventionalVendorCandidatesSubmitted1.setStatus(interimreport);
                        ConventionalVendorCandidatesSubmitted updatedToInterim = conventionalCandidatesSubmittedRepository.save(conventionalVendorCandidatesSubmitted1);
                        candidatePendingReportHistory.setNewstatus(updatedToInterim.getStatus());
                        conventionalPendingReportHistoryRepository.save(candidatePendingReportHistory);
                        candidatePendingReportHistory.setReportStatusReponse(icheckRepsonse.toString());
                        listServiceOutcome.setData(responedata);
                        listServiceOutcome.setMessage(icheckRepsonse.getBody());
                        listServiceOutcome.setStatus(String.valueOf(icheckRepsonse.getStatusCode()));
                        listServiceOutcome.setOutcome(true);
                        log.info("Pending  Report For  Request id" + updatedToInterim.getRequestId() + " Ltm response \n" + responedata);
                    }


                }
            }
        } catch (HttpClientErrorException e) {
            // Handle specific client-side HTTP error (e.g., 400)
            responedata = e.getResponseBodyAsString();
            candidatePendingReportHistory.setReportStatusReponse(responedata);
            conventionalPendingReportHistoryRepository.save(candidatePendingReportHistory);
            listServiceOutcome.setData(icheckRepsonse.toString());
            listServiceOutcome.setMessage(e.getResponseBodyAsString());
            listServiceOutcome.setStatus(String.valueOf(e.getStatusCode()));
            listServiceOutcome.setOutcome(false);
            log.info("Response Recived Pending BGV Upload");

        } catch (Exception e) {

            listServiceOutcome.setOutcome(false);
            listServiceOutcome.setMessage("Report Generation Failed");
            log.error("Exception in Pending ReportUpload()" + e.getMessage());
        }
        return listServiceOutcome;


    }

    //night 11pm  to  morning 5am every 30 minits
//    @Scheduled(cron = "0 */30 23-23,0-5 * * *")
    public void triggerInterimAndFinalReportPendingCron() {
        try {
            log.info("triggerInterimAndFinalReportPendingCron starts()");
            ServiceOutcome<List<SubmittedCandidates>> listServiceOutcome = fetchReportUploadPendingDetails("{\"VendorID\":\"2CDC7E3A\"}");
            List<SubmittedCandidates> data = listServiceOutcome.getData();
            List<SubmittedCandidates> filteredData = data.stream()
                    .filter(cand -> cand.getStatus().equalsIgnoreCase("FINALREPORT") ||
                            cand.getStatus().equalsIgnoreCase("INTERIMREPORT"))
                    .limit(30) // Limit the result to the first 30 elements
                    .collect(Collectors.toList());
            filteredData.forEach(dat -> System.out.println("request id " + dat.getRequestId() + "--" + dat.getStatus()));
            filteredData.forEach(candidte -> {
                if (candidte.getStatus().equals("INTERIMREPORT")) {
                    log.info("Triggering report starts on  uploadPendingReport Api for the request id " + candidte.getRequestId() + "   -   of Reporttype --" + candidte.getStatus());
                    triggerPendingReport(candidte.getRequestId(), ReportType.INTERIM, "UPDATE");
                    log.info("Triggering report ends on  uploadPendingReport Api for the request id " + candidte.getRequestId() + "   -   of Reporttype --" + candidte.getStatus());
                } else if (candidte.getStatus().equals("FINALREPORT")) {
                    log.info("Triggering report starts on  uploadPendingReport Api for the request id " + candidte.getRequestId() + "   -   of Reporttype --" + candidte.getStatus());
                    triggerPendingReport(candidte.getRequestId(), ReportType.FINAL, "UPDATE");
                    log.info("Triggering report ends on  uploadPendingReport Api for the request id " + candidte.getRequestId() + "   -   of Reporttype --" + candidte.getStatus());
                }
            });
            log.info("triggerInterimAndFinalReportPendingCron ends()");
        } catch (Exception e) {
            log.info("exception in triggerInterimAndFinalReportPendingCron()");
            e.printStackTrace();
        }
    }


    private HttpHeaders setHeaderDetails(HttpHeaders headers) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public String getGstToken() {
        String token = "";
        try {
            HttpHeaders tokenHeaders = new HttpHeaders();
            setHeaderDetails(tokenHeaders);
            JSONObject tokenRequest = new JSONObject();

            tokenRequest.put("client_id", environmentVal.getClient_id());
            tokenRequest.put("client_secret", environmentVal.getClient_secret());
            HttpEntity<String> tokenEntity = new HttpEntity<>(tokenRequest.toString(), tokenHeaders);

            //calling remittance token API
            ResponseEntity<String> tokenResponse = restTemplate.exchange(environmentVal.getTokenUrl(), HttpMethod.POST, tokenEntity, String.class);
            String message = tokenResponse.getBody();

            if (message != null && !message.isEmpty()) {
                JSONObject obj = new JSONObject(message);
                token = obj != null ? obj.getJSONObject("message").getString("access_token") : "";
            }
            log.info("GST TOKEN in getGstToken::{}", token);
            return token;

        } catch (JSONException jsn) {
            log.error("JSON Exception occured in getGstToken::{}", jsn);
        } catch (Exception e) {
            log.error("Exception occured in getGstToken::{}", e);
        }
        return token;
    }

    public String getGstTransactionID(String remittanceToken) {
        String tID = "";
        try {
            HttpHeaders headers = new HttpHeaders();
            setHeaderDetails(headers);
            headers.setBearerAuth(remittanceToken);
            headers.add("Bearer", remittanceToken);
            HttpEntity<String> request = new HttpEntity<>(headers);

            //calling remittance transactionID API
            ResponseEntity<String> response = restTemplate.exchange(environmentVal.getTransactionIdUrl(), HttpMethod.GET, request, String.class);
            String message = response.getBody();
            JSONObject obj = new JSONObject(message);
            tID = obj != null ? obj.getString("message") : "";

            log.info("GST TRANSACTION ID in getGstTransactionID::{}", tID);
            return tID;

        } catch (JSONException jsn) {
            log.error("JSON Exception occured in getGstTransactionID::{}", jsn);
        } catch (Exception e) {
            log.error("Exception occured in getGstTransactionID::{}", e);
        }
        return tID;
    }


    public ServiceOutcome<String> getGstBase64(String gstnumber) {
        ServiceOutcome gstserviceOutcome = new ServiceOutcome();
        try {
            log.info("Checking GST validation for  ::{}", gstnumber);
            String gstToken = getGstToken();
            String gstTID = "";

            if (!gstToken.equals("") && !gstToken.isEmpty()) {
                gstTID = getGstTransactionID(gstToken);
            }

            if (!gstTID.equals("") && !gstTID.isEmpty()) {

                HttpHeaders headers = new HttpHeaders();
                setHeaderDetails(headers);
                headers.add("txnid", gstTID);
                // Request object
                JSONObject requestJson = new JSONObject();
                requestJson.put("gst", gstnumber);
                requestJson.put("company_name", "");

                log.info("Request to GST Valid ::{}", requestJson.toString());
                HttpEntity<String> requestEntity = new HttpEntity<>(requestJson.toString(), headers);
                ResponseEntity<String> gstResponse = restTemplate.exchange(
                        environmentVal.getGstFetchUrl() + gstTID,
                        HttpMethod.POST, requestEntity, String.class);
                String redirectUrl = gstResponse.getHeaders().getLocation().toString();

                // Make a new request to the redirect URL
                ResponseEntity<String> validateResp = restTemplate.exchange(redirectUrl, HttpMethod.POST, requestEntity, String.class);

                if (validateResp.getStatusCode() == HttpStatus.OK && validateResp.getBody() != null) {
                    // Parse the JSON response
                    JSONObject jsonResponse = new JSONObject(validateResp.getBody());
                    JSONArray imagesArray = jsonResponse.getJSONObject("message")
                            .getJSONArray("messages")
                            .getJSONObject(0)
                            .getJSONArray("images");
                    // Convert JSONArray to List<String>
                    List<String> imageList = new ArrayList<>();
                    for (int i = 0; i < imagesArray.length(); i++) {
                        imageList.add(imagesArray.getString(i)); // Convert each item to String
                    }

                    // Now you can proceed with the PDF conversion
                    String pdfBase64 = convertImagesToPdfBase64(imageList);

                    // Return the PDF as base64
                    gstserviceOutcome.setData(pdfBase64);
                    gstserviceOutcome.setMessage("Gst Proof Fetched Sucessfully");
                    gstserviceOutcome.setOutcome(true);
                }
            }
        } catch (Exception e) {
            log.error("Exception occurred in validateGstNumber::{}", e);
            gstserviceOutcome.setMessage("Gst Proof Fetched Failed" + e.getMessage());
            gstserviceOutcome.setOutcome(false);
        }
        return gstserviceOutcome;
    }

    public ServiceOutcome<String> getMcaBase64(String companyName) {


        ServiceOutcome gstserviceOutcome = new ServiceOutcome();

        try {
            log.info("Checking Mca validation for  ::{}", companyName);
            String gstToken = getGstToken();
            String gstTID = "";

            if (!gstToken.equals("") && !gstToken.isEmpty()) {
                gstTID = getGstTransactionID(gstToken);
            }

            if (!gstTID.equals("") && !gstTID.isEmpty()) {

                HttpHeaders headers = new HttpHeaders();
                setHeaderDetails(headers);
                headers.add("txnid", gstTID);
                // Request object
                JSONObject requestJson = new JSONObject();
                requestJson.put("company_name", companyName);

                log.info("Request to GST Valid ::{}", requestJson.toString());

                HttpEntity<String> requestEntity = new HttpEntity<>(requestJson.toString(), headers);
                ResponseEntity<String> mcaResponse = restTemplate.exchange(
                        environmentVal.getMcaFetchUrl() + gstTID,
                        HttpMethod.POST, requestEntity, String.class);
                if (mcaResponse.getStatusCode() == HttpStatus.OK && mcaResponse.getBody() != null) {
                    // Parse the JSON response
                    JSONObject jsonResponse = new JSONObject(mcaResponse.getBody());
                    JSONArray imagesArray = jsonResponse.getJSONObject("message")
                            .getJSONArray("messages")
                            .getJSONObject(0)
                            .getJSONArray("images");
                    // Convert JSONArray to List<String>
                    List<String> imageList = new ArrayList<>();
                    for (int i = 0; i < imagesArray.length(); i++) {
                        imageList.add(imagesArray.getString(i)); // Convert each item to String
                    }

                    // Now you can proceed with the PDF conversion
                    String pdfBase64 = convertImagesToPdfBase64(imageList);

                    // Return the PDF as base64
                    gstserviceOutcome.setData(pdfBase64);
                    gstserviceOutcome.setMessage("Mca Proof Fetched Sucessfully");
                    gstserviceOutcome.setOutcome(true);
                }
            }
        } catch (Exception e) {
            log.error("Exception occurred in validateGstNumber::{}", e);
            gstserviceOutcome.setMessage("Mca Proof Fetched Failed \n" + e.getMessage());
            gstserviceOutcome.setOutcome(false);
        }
        return gstserviceOutcome;
    }

    private String convertImagesToPdfBase64(List<String> imageBase64List) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Initialize PDF writer
        PdfWriter pdfWriter = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        Document document = new Document(pdfDocument);

        for (String imageBase64 : imageBase64List) {
            // Convert base64 string to byte array
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
            ImageData imageData = ImageDataFactory.create(imageBytes);

            // Add the image to the document
            Image image = new Image(imageData);
            document.add(image);
        }

        // Close the document
        document.close();
        // Convert PDF to Base64
        byte[] pdfBytes = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(pdfBytes);
    }


    public ServiceOutcome<String> getReferenceDataByRequestIdAndCheckUniqueId(String checkName, String checkUniqueId) {
        ServiceOutcome<String> serviceOutcome = new ServiceOutcome<>();
        try {
            ConventionalVendorliChecksToPerform byCheckUniqueId = liCheckToPerformRepository.findByCheckUniqueId(Long.valueOf(checkUniqueId));
            List<ConventionalCandidateExperience> byConventionalCandidateId = conventionalCandidateExperienceRepository.findByConventionalRequestId(Long.valueOf(byCheckUniqueId.getRequestId()));

            // Variable to hold the result
            String matchedEmployerName = null;

            Optional<ConventionalCandidateExperience> first = byConventionalCandidateId.stream()
                    .filter(experience -> checkName.contains(experience.getEmploymentType())) // Filter by employmentType containing checkName
                    .findFirst();

            if (first.isPresent()) {
                Long candidateCafExperienceId = first.get().getCandidateCafExperience();
                Optional<CandidateCafExperience> byId = candidateCafExperienceRepository.findById(candidateCafExperienceId);
                if (byId.isPresent()) {
                    String candidateEmployerName = byId.get().getCandidateEmployerName();
                    serviceOutcome.setData(candidateEmployerName);
                    serviceOutcome.setOutcome(true);
                    serviceOutcome.setMessage("Employer Found");
                }
            }


        } catch (Exception e) {
            log.error("Exception occurred in getReferenceDataByRequestIdAndCheckUniqueId::{}", e);
            serviceOutcome.setMessage("An error occurred: " + e.getMessage());
            serviceOutcome.setOutcome(false);
        }
        return serviceOutcome;
    }


    public ServiceOutcome<String> processDomainSearch(String companyName) {
        ServiceOutcome<String> serviceOutcome = new ServiceOutcome<>();

        try {
            log.info("Initiating WHOIS search for company: {}", companyName);

            // Using Playwright for browser automation
            try (Playwright playwright = Playwright.create()) {
                Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true)); // Set to true for headless
                BrowserContext context = browser.newContext();
                com.microsoft.playwright.Page page = context.newPage();

                // Navigate to the WHOIS search page
                page.navigate("https://www.whois.com/whois/");

                // Fill in the search field with the company name
                page.locator("input[placeholder='Enter Domain Name or IP Address']").fill(companyName);
                Thread.sleep(4000); // Wait for input to settle

                // Press Enter to search
                page.keyboard().press("Enter");
                Thread.sleep(4000); // Wait for results to load

                // Take screenshot and convert to Base64
                byte[] screenshotBytes = page.screenshot();
                String croppedBase64Img = cropScreenshot(screenshotBytes);

                // Convert the cropped Base64 image to PDF and get the Base64 PDF string
                List<String> imageList = Collections.singletonList(croppedBase64Img);
                String pdfBase64 = convertImagesToPdfBase64(imageList); // Reuse your existing method

                // Set the base64 PDF result in ServiceOutcome
                serviceOutcome.setData(pdfBase64);
                serviceOutcome.setMessage("Domain search successful");
                serviceOutcome.setOutcome(true);

                // Clean up the browser context
                context.close();
                browser.close();
            }
        } catch (Exception e) {
            log.error("Error occurred during WHOIS search for company: {}", companyName, e);
            serviceOutcome.setMessage("WHOIS search failed: " + e.getMessage());
            serviceOutcome.setOutcome(false);
        }

        return serviceOutcome;
    }


    private String cropScreenshot(byte[] screenshotBytes) throws IOException {
        // Read the screenshot into a BufferedImage
        ByteArrayInputStream bais = new ByteArrayInputStream(screenshotBytes);
        BufferedImage originalImage = ImageIO.read(bais);

        int topCrop = (int) (2 * 37.795); // 2 cm from the top
        int rightCrop = (int) (11 * 37.795); // 11 cm from the right
        int bottomCrop = (int) (5 * 37.795); // 3 cm from the bottom, moved up by 1 cm
        // Calculate new dimensions
        int newWidth = originalImage.getWidth() - rightCrop;
        int newHeight = originalImage.getHeight() - topCrop - bottomCrop;
        // Ensure newWidth and newHeight are valid
        if (newWidth <= 0 || newHeight <= 0) {
            throw new IllegalArgumentException("Cropping dimensions exceed original image dimensions.");
        }
        // Create cropped image
        BufferedImage croppedImage = originalImage.getSubimage(0, topCrop, newWidth, newHeight);
        // Convert cropped image to Base64
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        ImageIO.write(croppedImage, "png", baos);
        byte[] croppedBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(croppedBytes);
    }

    @Autowired
    private EntityManager entityManager;


    public List<CandidateTrackerDto> getCandidateRequestDetails(Date startDate, Date endDate) {
        try {
            String sql = "SELECT " +
                    "COALESCE(creq.request_id, '') AS requestId, " +
                    "COALESCE(creq.candidate_id, '') AS candidateId, " +
                    "COALESCE(creq.ps_no, '') AS psNo, " +
                    "COALESCE(creq.name, '') AS name, " +
                    "COALESCE(sm.status_name, '') AS statusCode, " +
                    "COALESCE(creq.verification_status, '') AS verificationStatus, " +
                    "COALESCE(creq.fast_track, '') AS fastTrack, " +
                    "COALESCE(DATE_FORMAT(creq.created_on, '%d-%m-%Y'), '') AS caseInitiatedOn, " +
                    "COALESCE(DATE_FORMAT(c.created_on, '%d-%m-%Y'), '') AS bgvInitiatedOn, " +
                    "COALESCE(DATEDIFF(CURDATE(), c.created_on) - (DATEDIFF(CURDATE(), c.created_on) DIV 7) * 2, '') AS ageing, " +
                    "COALESCE(DATE_FORMAT(ADDDATE(c.created_on, CASE WHEN DAYOFWEEK(c.created_on) = 7 THEN 17 ELSE 15 END), '%d-%m-%Y'), '') AS eta, " +
                    "COALESCE(DATE_FORMAT(MAX(CASE WHEN cvp.check_name LIKE '%EMPLOYMENT CHECK - CURRENT EMPLOYER%' THEN cvp.created_on END), '%d-%m-%Y'), '') AS currentEmploymentInitiationDate, " +
                    "COALESCE(DATE_FORMAT((SELECT MAX(tcontent.created_on) " +
                    "                    FROM t_dgv_content tcontent " +
                    "                    WHERE tcontent.candidate_id = tbasic.candidate_id " +
                    "                      AND tcontent.sub_category = 'FINAL' " +
                    "                    ORDER BY tcontent.created_on DESC " +
                    "                    LIMIT 1), '%d-%m-%Y'), '') AS finalReportDispatchDate, " +
                    "COALESCE(GROUP_CONCAT(DISTINCT CASE WHEN cvp.check_status NOT IN (2, 7) THEN cvp.check_name ELSE NULL END ORDER BY cvp.check_name ASC), '') AS completedChecks, " +
                    "COALESCE(GROUP_CONCAT(DISTINCT CASE WHEN cvp.check_status IN (2, 7) THEN cvp.check_name ELSE NULL END ORDER BY cvp.check_name ASC), '') AS pendingChecks, " +
                    "COALESCE(GROUP_CONCAT(DISTINCT CASE WHEN cvp.check_status IN (3) THEN cvp.check_name ELSE NULL END ORDER BY cvp.check_name ASC), '') AS insufficencyChecks, " +
                    "COALESCE(GROUP_CONCAT(DISTINCT CASE WHEN cvp.stop_check IS NOT NULL THEN cvp.check_name ELSE NULL END ORDER BY cvp.check_name ASC), '') AS stopChecks, " +
                    "COALESCE(GROUP_CONCAT(DISTINCT CASE WHEN cvp.is_check_status_triggered = FAlSE THEN cvp.check_name ELSE NULL END ORDER BY cvp.check_name ASC), '') AS nottriggeredchecks " +
                    "FROM t_dgv_conventional_candidate_request creq " +
                    "LEFT JOIN (SELECT c.request_id, c.created_on " +
                    "           FROM t_dgv_conventional_vendorchecks_to_perform c " +
                    "           GROUP BY c.request_id, c.created_on) c ON c.request_id = creq.request_id " +
                    "LEFT JOIN t_dgv_candidate_basic tbasic ON creq.request_id = tbasic.conventional_request_id " +
                    "LEFT JOIN t_dgv_status_master sm ON creq.status = sm.status_master_id " +
                    "LEFT JOIN t_dgv_conventional_vendorchecks_to_perform cvp ON cvp.request_id = creq.request_id " +
                    "WHERE creq.created_on BETWEEN :startDate AND :endDate " +
                    "GROUP BY creq.request_id";

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);

            // Convert Object[] to CandidateTrackerDto
            List<Object[]> resultList = query.getResultList();
            List<CandidateTrackerDto> candidateTrackerDtos = new ArrayList<>();

            for (Object[] row : resultList) {
                CandidateTrackerDto dto = new CandidateTrackerDto();
                dto.setRequestId((String) row[0]);
                dto.setCandidateId((String) row[1]);
                dto.setPsNo((String) row[2]);
                dto.setName((String) row[3]);
                dto.setStatusCode((String) row[4]);
                dto.setVerificationStatus((String) row[5]);
                dto.setFastTrack((String) row[6]);
                dto.setCaseInitiatedOn((String) row[7]);
                dto.setBgvInitiatedOn((String) row[8]);
                dto.setAgeing((String) row[9]);
                dto.setEta((String) row[10]);
                dto.setCurrentEmploymentInitiationDate((String) row[11]);
                dto.setFinalReportDispatchedDate((String) row[12]);
                dto.setCompletedChecks(Arrays.asList(((String) row[13]).split(", ")));
                dto.setPendingChecks(Arrays.asList(((String) row[14]).split(", ")));
                dto.setInsufficencyChecks(Arrays.asList(((String) row[15]).split(", ")));
                dto.setStopChecks(Arrays.asList(((String) row[16]).split(", ")));
                dto.setNottriggeredchecks(Arrays.asList(((String) row[17]).split(", ")));
                candidateTrackerDtos.add(dto);
            }

            return candidateTrackerDtos;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    public List<InsufficiencyTrakerDto> getInsufftrackerDetails(Date startdate, Date enddate) {
        List<InsufficiencyTrakerDto> insufficiencyTrakerDtos = new ArrayList<>();
        try {
            String sql = "WITH CandidateReqTable AS ( " +
                    "    SELECT request_id " +
                    "    FROM t_dgv_conventional_candidate_request " +
                    "    WHERE created_on BETWEEN :startDate AND :endDate " +
                    "), " +

                    "InsufficiencyLatest AS ( " +
                    "    SELECT request_id, check_name, check_unique_id, check_status, created_on " +
                    "    FROM t_dgv_conventional_licheck_history " +
                    "    WHERE check_status LIKE '%insufficiency%' " +
                    "      AND request_id IN (SELECT request_id FROM CandidateReqTable) " +
                    "), " +

                    "InProgressAfterInsuff AS ( " +
                    "    SELECT t.request_id, t.check_name, t.check_unique_id, t.check_status, t.created_on, " +
                    "           ROW_NUMBER() OVER (PARTITION BY t.request_id, t.check_unique_id ORDER BY t.created_on DESC) AS rn " +
                    "    FROM t_dgv_conventional_licheck_history t " +
                    "    JOIN InsufficiencyLatest i ON t.request_id = i.request_id " +
                    "                                 AND t.check_unique_id = i.check_unique_id " +
                    "                                 AND t.created_on > i.created_on " +
                    "    WHERE t.check_status LIKE '%inprogress%' " +
                    ") " +

                    "SELECT i.request_id AS requestId, " +
                    "       i.check_name AS checkName, " +
                    "       i.check_unique_id AS checkUniqueId, " +
                    "       DATE_FORMAT(i.created_on, '%d-%m-%Y') AS InsuffCreatedOn, " +
                    "       DATE_FORMAT(p.created_on, '%d-%m-%Y') AS inprogressCreatedOn, " +
                    "       CASE " +
                    "           WHEN p.created_on IS NOT NULL THEN " +
                    "               DATE_FORMAT(ADDDATE(p.created_on, " +
                    "                           CASE WHEN DAYOFWEEK(p.created_on) IN (1, 7) THEN 17 ELSE 15 END), " +
                    "                          '%d-%m-%Y') " +
                    "           ELSE NULL " +
                    "       END AS InsufficiencyEta " +
                    "FROM InsufficiencyLatest i " +
                    "LEFT JOIN InProgressAfterInsuff p " +
                    "       ON i.request_id = p.request_id " +
                    "       AND i.check_unique_id = p.check_unique_id " +
                    "       AND p.rn = 1 " +
                    "ORDER BY i.request_id, i.check_unique_id, i.created_on;";

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("startDate", startdate);
            query.setParameter("endDate", enddate);

            List<Object[]> results = query.getResultList();


            for (Object[] row : results) {
                InsufficiencyTrakerDto dto = new InsufficiencyTrakerDto();
                dto.setRequestId(((Number) row[0]).longValue());
                dto.setCheckName((String) row[1]);
                dto.setCheckUniqueId(((Number) row[2]).toString());
                dto.setInsuffCreatedOn((String) row[3]);
                dto.setInprogressCreatedOn((String) row[4]);
                dto.setInsufficiencyEta((String) row[5]);
                insufficiencyTrakerDtos.add(dto);
            }
            return insufficiencyTrakerDtos;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<VendorTrackerDto> getVendorTrackerDetails(Date startDate, Date endDate, String vendorId) {
        List<VendorTrackerDto> trackerList = new ArrayList<>();
        try {
            String sql = "WITH stop_check_values AS ( " +
                    "  SELECT request_id, " +
                    "         CASE WHEN MAX(CASE WHEN stop_check = 'TRUE' THEN 1 ELSE 0 END) = 1 THEN 'TRUE' ELSE 'FALSE' END AS stop_check_value " +
                    "  FROM t_dgv_conventional_vendorchecks_to_perform " +
                    "  GROUP BY request_id " +
                    "), " +
                    "licheck_status_history AS ( " +
                    "  SELECT  check_unique_id, check_status, created_on, " +
                    "    LAG(check_status) OVER (PARTITION BY check_unique_id ORDER BY created_on) AS prev_status, " +
                    "    LAG(created_on) OVER (PARTITION BY check_unique_id ORDER BY created_on) AS prev_created_on " +
                    "  FROM t_dgv_conventional_licheck_history " +
                    "), " +
                    "status_boundaries AS ( " +
                    "  SELECT  check_unique_id, " +
                    "    MIN(CASE WHEN check_status = 'INPROGRESS' THEN created_on END) AS first_inprogress, " +
                    "    MAX(CASE WHEN check_status IN ('CLEAR', 'MAJORDISCREPANCY', 'MINORDISCREPANCY', 'UNABLETOVERIFY') THEN created_on END) AS final_status_date, " +
                    "    MAX(CASE WHEN check_status = 'INSUFFICIENCY' THEN created_on END) AS last_insufficiency, " +
                    "    MAX(created_on) AS latest_date " +
                    "  FROM t_dgv_conventional_licheck_history " +
                    "  GROUP BY check_unique_id " +
                    "), " +
                    "exclude_durations AS ( " +
                    "  SELECT  check_unique_id, SUM(DATEDIFF(created_on, prev_created_on)) AS total_exclude " +
                    "  FROM licheck_status_history " +
                    "  WHERE check_status = 'INPROGRESS' AND prev_status = 'INSUFFICIENCY' " +
                    "  GROUP BY check_unique_id " +
                    "), " +
                    "ageing_calc AS ( " +
                    "  SELECT  sb.check_unique_id, " +
                    "    CASE  " +
                    "      WHEN sb.first_inprogress IS NOT NULL AND sb.final_status_date IS NOT NULL THEN  " +
                    "        DATEDIFF(sb.final_status_date, sb.first_inprogress) - COALESCE(ed.total_exclude, 0) " +
                    "      WHEN sb.first_inprogress IS NOT NULL AND sb.final_status_date IS NULL AND sb.last_insufficiency IS NOT NULL THEN  " +
                    "        DATEDIFF(sb.last_insufficiency, sb.first_inprogress) - COALESCE(ed.total_exclude, 0) " +
                    "      WHEN sb.first_inprogress IS NOT NULL AND sb.final_status_date IS NULL AND sb.last_insufficiency IS NULL THEN  " +
                    "        DATEDIFF(CURDATE(), sb.first_inprogress) " +
                    "      ELSE NULL " +
                    "    END AS ageing_days " +
                    "  FROM status_boundaries sb " +
                    "  LEFT JOIN exclude_durations ed ON sb.check_unique_id = ed.check_unique_id " +
                    "), " +
                    "completed_dates AS ( " +
                    "  SELECT  check_unique_id,  MAX(created_on) AS completed_date " +
                    "  FROM t_dgv_conventional_licheck_history " +
                    "  WHERE check_status IN ('CLEAR', 'MAJORDISCREPANCY', 'MINORDISCREPANCY', 'UNABLETOVERIFY') " +
                    "  GROUP BY check_unique_id " +
                    "), " +
                    "CandidateReqTable AS ( " +
                    "  SELECT cvp.check_unique_id " +
                    "  FROM t_dgv_conventional_vendorchecks_to_perform cvp " +
                    "  JOIN t_dgv_vendor_checks vp ON cvp.vendor_check = vp.vendor_check_id " +
                    "  WHERE vp.vendor_id = :vendorId " +
                    "    AND cvp.created_on BETWEEN :startDate AND :endDate " +
                    "), " +
                    "InsuffHistory AS ( " +
                    "  SELECT request_id, check_name, check_unique_id, check_status, created_on " +
                    "  FROM t_dgv_conventional_licheck_history " +
                    "  WHERE check_status LIKE '%INSUFFICIENCY%' " +
                    "    AND check_unique_id IN (SELECT check_unique_id FROM CandidateReqTable) " +
                    "), " +
                    "LatestInsuff AS ( " +
                    "  SELECT * FROM ( " +
                    "      SELECT *, ROW_NUMBER() OVER (PARTITION BY check_unique_id ORDER BY created_on DESC) AS rn " +
                    "      FROM InsuffHistory " +
                    "  ) ranked WHERE rn = 1 " +
                    "), " +
                    "NextStatusAfterInsuff AS ( " +
                    "  SELECT  p.check_unique_id, p.created_on, " +
                    "    ROW_NUMBER() OVER (PARTITION BY p.check_unique_id ORDER BY p.created_on) AS rn " +
                    "  FROM t_dgv_conventional_licheck_history p " +
                    "  JOIN LatestInsuff i ON p.check_unique_id = i.check_unique_id " +
                    "  WHERE p.created_on > i.created_on AND p.check_status NOT LIKE '%INSUFFICIENCY%' " +
                    "), " +
                    "InsuffCalcTemp AS ( " +
                    "  SELECT i.check_unique_id, i.created_on AS insuff_raised_date, p.created_on AS insuff_cleared_date " +
                    "  FROM LatestInsuff i " +
                    "  LEFT JOIN NextStatusAfterInsuff p ON i.check_unique_id = p.check_unique_id AND p.rn = 1 " +
                    "), " +
                    "DaysGenerator AS ( " +
                    "  SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL " +
                    "  SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 " +
                    "), " +
                    "AllDays AS ( " +
                    "  SELECT ict.check_unique_id, ADDDATE(ict.insuff_raised_date, INTERVAL (a.d + 10*b.d + 100*c.d) DAY) AS d " +
                    "  FROM InsuffCalcTemp ict " +
                    "  JOIN DaysGenerator a " +
                    "  JOIN DaysGenerator b " +
                    "  JOIN DaysGenerator c " +
                    "  WHERE ADDDATE(ict.insuff_raised_date, INTERVAL (a.d + 10*b.d + 100*c.d) DAY) <= IFNULL(ict.insuff_cleared_date, CURDATE()) " +
                    "), " +
                    "InsuffCalc AS ( " +
                    "  SELECT ict.check_unique_id, ict.insuff_raised_date, ict.insuff_cleared_date, COUNT(*) AS insuff_tat " +
                    "  FROM AllDays ad " +
                    "  JOIN InsuffCalcTemp ict ON ad.check_unique_id = ict.check_unique_id " +
                    "  WHERE DAYOFWEEK(ad.d) NOT IN (1, 7) " +
                    "  GROUP BY ict.check_unique_id, ict.insuff_raised_date, ict.insuff_cleared_date " +
                    ") " +
                    "SELECT  " +
                    "  COALESCE(cv.check_unique_id, '') AS checkUniqueId, " +
                    "  COALESCE(cv.request_id, '') AS requestId, " +
                    "  COALESCE(cv.candidate_id, '') AS candidateId, " +
                    "  COALESCE(cr.name, '') AS candidateName, " +
                    "  COALESCE(cv.check_name, '') AS checkName, " +
                    "  COALESCE(cv.check_remarks, '') AS check_remarks, " +
                    "  COALESCE(vsm.checkstatus_code, '') AS statusCode, " +
                    "  (SELECT clh.check_status FROM t_dgv_conventional_licheck_history clh  " +
                    "   WHERE clh.check_unique_id = cv.check_unique_id  " +
                    "   ORDER BY clh.created_on DESC LIMIT 1) AS ClientCheckStatus, " +
                    "  COALESCE(DATE_FORMAT(cv.created_on, '%d-%m-%Y'), '') AS caseReceivedOn, " +
                    "  COALESCE(ac.ageing_days, '') AS ageing, " +
                    "  COALESCE(scv.stop_check_value, '') AS stopChecks, " +
                    "  COALESCE(DATE_FORMAT(cd.completed_date, '%d-%m-%Y'), '') AS completedDate, " +
                    "  DATE_FORMAT(ic.insuff_raised_date, '%d-%m-%Y') AS insuffRaisedDate, " +
                    "  DATE_FORMAT(ic.insuff_cleared_date, '%d-%m-%Y') AS insuffClearedDate, " +
                    "  ic.insuff_tat AS insuffTat " +
                    "FROM t_dgv_vendor_checks vc " +
                    "JOIN t_dgv_conventional_vendorchecks_to_perform cv ON vc.vendor_check_id = cv.vendor_check " +
                    "JOIN t_dgv_conventional_candidate_request cr ON cr.request_id = cv.request_id " +
                    "LEFT JOIN t_dgv_vendor_uploaded_checks vup ON vc.vendor_check_id = vup.vendor_check_id " +
                    "LEFT JOIN stop_check_values scv ON scv.request_id = cv.request_id " +
                    "LEFT JOIN t_dgv_vendor_checkstatus_master vsm ON cv.check_status = vsm.vendor_checkstatus_master_id " +
                    "LEFT JOIN ageing_calc ac ON cv.check_unique_id = ac.check_unique_id " +
                    "LEFT JOIN completed_dates cd ON cv.check_unique_id = cd.check_unique_id " +
                    "LEFT JOIN InsuffCalc ic ON cv.check_unique_id = ic.check_unique_id " +
                    "WHERE vc.vendor_id = :vendorId AND vc.created_at BETWEEN :startDate AND :endDate " +
                    "ORDER BY vc.created_at DESC ";

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("vendorId", vendorId);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);

            List<Object[]> resultList = query.getResultList();

            for (Object[] row : resultList) {
                VendorTrackerDto dto = new VendorTrackerDto();
                dto.setCheckUniqueId((String) row[0]);
                dto.setRequestId((String) row[1]);
                dto.setCandidateId((String) row[2]);
                dto.setCandidateName((String) row[3]);
                dto.setCheckName((String) row[4]);
                dto.setCheckRemarks((String) row[5]);
                dto.setStatusCode((String) row[6]);
                dto.setClientCheckStatus((String) row[7]);
                dto.setCaseReceivedOn((String) row[8]);
                dto.setAgeing((row[9] != null) ? row[9].toString() : null);
                dto.setStopChecks((String) row[10]);
                dto.setCompletedDate((String) row[11]);
                dto.setInsuffRaisedDate((String) row[12]);
                dto.setInsuffClearedDate((String) row[13]);
                dto.setInsuffTat((row[14] != null) ? row[14].toString() : null);
                if (row[14] != null) {
                    int tat = Integer.parseInt(row[14].toString());
                    dto.setInsuffTat(String.valueOf(tat - 1));
                }

                trackerList.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return trackerList;
    }


    public byte[] generateExcelForCandidateTracker(List<CandidateTrackerDto> trackerData, List<InsufficiencyTrakerDto> insufftrackerDetails) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // First sheet: Candidate Tracker Data
            Sheet candidateSheet = workbook.createSheet("Conventional Tracker Data");
            candidateSheet.createFreezePane(0, 1);  // Freeze the top row

            int rowNum = 0;

            // Define header and data styles
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setWrapText(true);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Font dataFont = workbook.createFont();
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setFont(dataFont);
            dataStyle.setWrapText(true);  // Enable text wrapping for multi-line cells

            // Candidate Tracker Sheet Headers
            String[] candidateHeaders = {
                    "Request ID", "Candidate ID", "PS No", "Name", "Case Status", "Report Status",
                    "Fast Track", "Case Initiated Date", "BGV Initiated Date", "Ageing", "ETA",
                    "Current Employer Initaition Date", "Final Report Dispatched Date", "Pending Checks", "Completed Checks"
            };
            Row candidateHeaderRow = candidateSheet.createRow(rowNum++);
            for (int i = 0; i < candidateHeaders.length; i++) {
                Cell cell = candidateHeaderRow.createCell(i);
                cell.setCellValue(candidateHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate Candidate Tracker Data
            for (CandidateTrackerDto tracker : trackerData) {
                Row row = candidateSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(tracker.getRequestId());
                row.createCell(1).setCellValue(tracker.getCandidateId());
                row.createCell(2).setCellValue(tracker.getPsNo());
                row.createCell(3).setCellValue(tracker.getName());
                row.createCell(4).setCellValue(tracker.getStatusCode());
                row.createCell(5).setCellValue(tracker.getVerificationStatus());
                row.createCell(6).setCellValue(tracker.getFastTrack());
                row.createCell(7).setCellValue(tracker.getCaseInitiatedOn());
                row.createCell(8).setCellValue(tracker.getBgvInitiatedOn());
                row.createCell(9).setCellValue(tracker.getAgeing());
                row.createCell(10).setCellValue(tracker.getEta());
                row.createCell(11).setCellValue(tracker.getCurrentEmploymentInitiationDate());
                row.createCell(12).setCellValue(tracker.getFinalReportDispatchedDate());

                // Format Pending and Completed Checks as multi-line strings
                String pendingChecks = String.join("\n", tracker.getPendingChecks());
                String completedChecks = String.join("\n", tracker.getCompletedChecks());

                row.createCell(13).setCellValue(pendingChecks);
                row.createCell(14).setCellValue(completedChecks);

                // Apply data style to cells
                for (int i = 0; i <= 14; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            // Auto-size columns for Candidate Tracker Sheet
            for (int i = 0; i < candidateHeaders.length; i++) {
                candidateSheet.autoSizeColumn(i);
            }

            // Second sheet: Insufficiency Tracker Data
            Sheet insuffSheet = workbook.createSheet("Insufficiency Tracker Data");
            insuffSheet.createFreezePane(0, 1);  // Freeze the top row

            rowNum = 0;

            // Insufficiency Tracker Sheet Headers
            String[] insuffHeaders = {
                    "Request ID", "Check Unique ID", "Check Name", "Insufficiency Created On", "In-Progress Created On", "Insufficiency ETA"
            };
            Row insuffHeaderRow = insuffSheet.createRow(rowNum++);
            for (int i = 0; i < insuffHeaders.length; i++) {
                Cell cell = insuffHeaderRow.createCell(i);
                cell.setCellValue(insuffHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate Insufficiency Tracker Data
            for (InsufficiencyTrakerDto insuff : insufftrackerDetails) {
                Row row = insuffSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(insuff.getRequestId());
                row.createCell(1).setCellValue(insuff.getCheckUniqueId());
                row.createCell(2).setCellValue(insuff.getCheckName());
                row.createCell(3).setCellValue(insuff.getInsuffCreatedOn());
                row.createCell(4).setCellValue(insuff.getInprogressCreatedOn());
                row.createCell(5).setCellValue(insuff.getInsufficiencyEta());

                // Apply data style to cells
                for (int i = 0; i <= 5; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            // Auto-size columns for Insufficiency Tracker Sheet
            for (int i = 0; i < insuffHeaders.length; i++) {
                insuffSheet.autoSizeColumn(i);
            }

            // Write workbook to output stream
            workbook.write(outputStream);

            // Return the byte array
            return outputStream.toByteArray();
        }
    }

    public byte[] generateExcelForInsufficiencyTracker(List<CandidateTrackerDto> trackerData) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Conventional Tracker Data");
            sheet.createFreezePane(0, 1);  // Freeze the top row

            int rowNum = 0;

            // Define header and data styles
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setWrapText(true);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Font dataFont = workbook.createFont();
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setFont(dataFont);
            dataStyle.setWrapText(true);  // Enable text wrapping for multi-line cells

            // Create header row
            String[] headers = {
                    "Request ID", "Candidate ID", "PS No", "Name", "Case Status", "Report Status",
                    "Fast Track", "Case Initiated Date", "BGV Initiated Date", "Ageing", "ETA",
                    "Final Report Dispatched Date", "Pending Checks", "Completed Checks"
            };
            Row headerRow = sheet.createRow(rowNum++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data rows
            for (CandidateTrackerDto tracker : trackerData) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(tracker.getRequestId());
                row.createCell(1).setCellValue(tracker.getCandidateId());
                row.createCell(2).setCellValue(tracker.getPsNo());
                row.createCell(3).setCellValue(tracker.getName());
                row.createCell(4).setCellValue(tracker.getStatusCode());
                row.createCell(5).setCellValue(tracker.getVerificationStatus());
                row.createCell(6).setCellValue(tracker.getFastTrack());
                row.createCell(7).setCellValue(tracker.getCaseInitiatedOn());
                row.createCell(8).setCellValue(tracker.getBgvInitiatedOn());
                row.createCell(9).setCellValue(tracker.getAgeing());
                row.createCell(10).setCellValue(tracker.getEta());
                row.createCell(11).setCellValue(tracker.getFinalReportDispatchedDate());

                // Format Pending and Completed Checks as multi-line strings
                String pendingChecks = String.join("\n", tracker.getPendingChecks());
                String completedChecks = String.join("\n", tracker.getCompletedChecks());

                row.createCell(12).setCellValue(pendingChecks);
                row.createCell(13).setCellValue(completedChecks);

                // Apply data style to cells
                for (int i = 0; i <= 13; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write workbook to output stream
            workbook.write(outputStream);

            // Return the byte array
            return outputStream.toByteArray();
        }
    }


    @Transactional
    public ServiceOutcome<CombinedTrackerDto> generateTrackerData(TrackerRequestDto trackerRequestDto) throws Exception {
        ServiceOutcome<CombinedTrackerDto> serviceOutcome = new ServiceOutcome<>();
        try {
            CombinedTrackerDto combinedTrackerDto = new CombinedTrackerDto();
            log.debug("start generateTrackerData() ");
            String strToDate = "";
            String strFromDate = "";
            strToDate = trackerRequestDto.getToDate() != null ? trackerRequestDto.getToDate() : ApplicationDateUtils.getStringTodayAsDDMMYYYY();
            strFromDate = trackerRequestDto.getFromDate() != null ? trackerRequestDto.getFromDate() : ApplicationDateUtils.subtractNoOfDaysFromDateAsDDMMYYYY(new SimpleDateFormat("dd/MM/yyyy").parse(strToDate), 7);
            Date startDate = formatter.parse(strFromDate + " 00:00:00");
            Date endDate = formatter.parse(strToDate + " 23:59:59");
            List<CandidateTrackerDto> candidateTrackerDtos = getCandidateRequestDetails(startDate, endDate);
            List<InsufficiencyTrakerDto> insufftrackerDetails = getInsufftrackerDetails(startDate, endDate);
            trackerRequestDto.setTrackerDtoList(candidateTrackerDtos);
            combinedTrackerDto.setCandidateTrackerDtos(candidateTrackerDtos);
            combinedTrackerDto.setInsufficiencyTrakerDtos(insufftrackerDetails);
            serviceOutcome.setData(combinedTrackerDto);
            serviceOutcome.setMessage("fetched response successfully");
            serviceOutcome.setOutcome(true);
            log.debug("end generateTrackerData() ");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serviceOutcome;
    }

    @Override
    public ServiceOutcome<List<CriminalCheck>> getCriminalChecksByCheckUniqueId(String checkUniqueId) {
        ServiceOutcome<List<CriminalCheck>> outcome = new ServiceOutcome<>();
        try {
            String jpql = "SELECT c FROM CriminalCheck c WHERE c.checkUniqueId = :checkUniqueId";
            List<CriminalCheck> result = entityManager.createQuery(jpql, CriminalCheck.class)
                    .setParameter("checkUniqueId", checkUniqueId)
                    .getResultList();

            outcome.setData(result);
            outcome.setOutcome(true);
            outcome.setStatus("200");
            outcome.setMessage("Criminal checks fetched successfully");
        } catch (Exception e) {
            log.error("Error fetching CriminalCheck by checkUniqueId: {}", checkUniqueId, e);
            outcome.setOutcome(false);
            outcome.setStatus("500");
            outcome.setMessage("Error fetching criminal checks: " + e.getMessage());
        }
        return outcome;
    }

    @Override
    public ServiceOutcome<CombinedVendorTrackerDto> generateVendorTrackerData(TrackerRequestDto trackerRequestDto) throws Exception {
        ServiceOutcome<CombinedVendorTrackerDto> serviceOutcome = new ServiceOutcome<>();
        log.info("Invoked generateVendorTrackerData with request: {}", trackerRequestDto);
        try {
            CombinedVendorTrackerDto combinedTrackerDto = new CombinedVendorTrackerDto();
            log.debug("Start generating VendorTrackerData");

            String strToDate = trackerRequestDto.getToDate() != null ? trackerRequestDto.getToDate() : ApplicationDateUtils.getStringTodayAsDDMMYYYY();
            String strFromDate = trackerRequestDto.getFromDate() != null
                    ? trackerRequestDto.getFromDate()
                    : ApplicationDateUtils.subtractNoOfDaysFromDateAsDDMMYYYY(new SimpleDateFormat("dd/MM/yyyy").parse(strToDate), 7);

            log.info("Date range for tracker: from {} to {}", strFromDate, strToDate);

            Date startDate = formatter.parse(strFromDate + " 00:00:00");
            Date endDate = formatter.parse(strToDate + " 23:59:59");

            String userId = trackerRequestDto.getUserId();
            if (userId == null || userId.isEmpty()) {
                log.warn("UserId is empty in TrackerRequestDto");
                serviceOutcome.setMessage("UserId is required");
                return serviceOutcome;
            }

            log.info("Fetching vendor tracker details for VendorID: {}", userId);
            List<VendorTrackerDto> vendorTrackerDetails = getVendorTrackerDetails(startDate, endDate, userId);

            trackerRequestDto.setTrackerDtoList(vendorTrackerDetails);
            combinedTrackerDto.setCandidateTrackerDtos(vendorTrackerDetails);

            serviceOutcome.setData(combinedTrackerDto);
            serviceOutcome.setMessage("Fetched response successfully");
            serviceOutcome.setOutcome(true);

            log.info("VendorTrackerData generated successfully for Vendor ID: {}", userId);
        } catch (Exception e) {
            log.error("Error in generateVendorTrackerData: {}", e.getMessage(), e);
            serviceOutcome.setMessage("Error occurred while generating vendor tracker data");
            serviceOutcome.setOutcome(false);
        }
        return serviceOutcome;
    }


    @Autowired
    AgentUplaodedChecksRepository agentUplaodedChecksRepository;


    @Override
    public ServiceOutcome<List<UanDto>> getAgentUploadedData(String requestId) {
        ServiceOutcome<List<UanDto>> serviceOutcome = new ServiceOutcome<>();

        try {
            // Null check for requestId
            if (requestId == null) {
                serviceOutcome.setOutcome(false);
                serviceOutcome.setMessage("RequestId cannot be null");
                return serviceOutcome;
            }

            List<AgentUploadedChecks> byRequestId = agentUplaodedChecksRepository.findByRequestID(requestId);
            if (byRequestId == null || byRequestId.isEmpty()) {
                serviceOutcome.setOutcome(false);
                serviceOutcome.setMessage("No records found for the given RequestId");
                return serviceOutcome;
            }
            List<UanDto> uanDtos = byRequestId.stream()
                    .map(agentCheck -> {
                        UanDto uanDto = new UanDto();
                        uanDto.setCheckName(agentCheck.getUanNo());
                        uanDto.setSourceId(agentCheck.getSource().getSourceName());
                        uanDto.setColorCode(agentCheck.getColorCode());
                        String createdBy = Optional.ofNullable(agentCheck.getCreatedBy())
                                .map(createdByUser -> createdByUser)
                                .orElse(null);
                        uanDto.setCreatedBy(createdBy);
                        uanDto.setDocumentName(agentCheck.getDocumentName());
                        uanDto.setPathKey(agentCheck.getPathKey());
                        uanDto.setRemarks(agentCheck.getRemarks());
                        uanDto.setRequestId(agentCheck.getRequestID());
                        uanDto.setUanNo(agentCheck.getUanNo());
                        if (agentCheck.getUpdatedBy() != null) {
                            uanDto.setCreatedBy(agentCheck.getUpdatedBy());
                        }
                        return uanDto;
                    })
                    .collect(Collectors.toList());
            serviceOutcome.setData(uanDtos);
            serviceOutcome.setOutcome(true);
        } catch (Exception e) {
            // Log exception and return failure message
            e.printStackTrace();
            serviceOutcome.setOutcome(false);
            serviceOutcome.setMessage("An error occurred while retrieving UAN data");
        }

        return serviceOutcome;
    }


    public ServiceOutcome<Source> findSourcebySourceName(String sourceName) {
        ServiceOutcome<Source> serviceOutcome = new ServiceOutcome<>();
        try {
            // Attempt to fetch the source by its name
            Source bySourceName = sourceRepository.findBySourceName(sourceName);

            // Check if the source is null
            if (bySourceName == null) {
                serviceOutcome.setOutcome(false);
                serviceOutcome.setMessage("Source not found with the given name: " + sourceName);
            } else {
                serviceOutcome.setData(bySourceName);
                serviceOutcome.setOutcome(true);
            }
        } catch (Exception e) {
            // Log the exception and update the ServiceOutcome object
            e.printStackTrace();
            serviceOutcome.setStatus("ERROR");
            serviceOutcome.setMessage("An error occurred while fetching the source: " + e.getMessage());
        }
        return serviceOutcome;
    }

    public ServiceOutcome<List<PurgeResponseDto>> generatePurgeReport(String startDate, String endDate) {
        ServiceOutcome<List<PurgeResponseDto>> outcome = new ServiceOutcome<>();
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date startDateFormatted = format.parse(startDate + " 00:00:00");
            Date endDateFormatted = format.parse(endDate + " 23:59:59");

            List<ConventionalVendorCandidatesSubmitted> candidates = conventionalCandidatesSubmittedRepository
                    .findByDateRangePurged(startDateFormatted, endDateFormatted, 2l);

            if (candidates.isEmpty()) {
                outcome.setOutcome(false);
                outcome.setMessage("No data found for the given date range.");
                return outcome;
            }

            List<PurgeResponseDto> responseList = candidates.stream().map(candidate -> {
                PurgeResponseDto dto = new PurgeResponseDto();
                dto.setApplicantId(candidate.getApplicantId() != null ? candidate.getApplicantId().toString() : "");
                dto.setName("PURGED");
                dto.setCreatedOn(candidate.getCreatedOn() != null ? candidate.getCreatedOn().toString() : "");
                dto.setDateOfBirth("PURGED");
                dto.setPurgedDate(candidate.getPurgedDate() != null ? candidate.getPurgedDate().toString() : "");
                return dto;
            }).collect(Collectors.toList());

            outcome.setData(responseList);
            outcome.setOutcome(true);
            outcome.setMessage("Report generated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            outcome.setOutcome(false);
            outcome.setStatus("ERROR");
            outcome.setMessage("Error generating report: " + e.getMessage());
        }
        return outcome;
    }

    public ServiceOutcome<String> updateCheckStatusByCheckUniqueId(String checkUniqueId, String selectedCheckstatus) {
        ServiceOutcome<String> serviceOutcome = new ServiceOutcome<>();
        try {
            // Step 1: Find the record by checkUniqueId
            ConventionalVendorliChecksToPerform check = liCheckToPerformRepository.findByCheckUniqueId(Long.valueOf(checkUniqueId));

            if (check == null) {
                serviceOutcome.setOutcome(false);
                serviceOutcome.setMessage("Check not found with the given CheckUniqueId :: " + checkUniqueId);
                return serviceOutcome;
            }

            // Step 2: Perform both updates with dynamic status
            int updated1 = liCheckToPerformRepository.updateConventionalCheckStatus(
                    Long.valueOf(checkUniqueId), Integer.valueOf(selectedCheckstatus));

            int updated2 = liCheckToPerformRepository.updateVendorCheckStatus(
                    Long.valueOf(checkUniqueId), Integer.valueOf(selectedCheckstatus));

            if (updated1 > 0 && updated2 > 0) {
                serviceOutcome.setOutcome(true);
                serviceOutcome.setData(checkUniqueId);
                serviceOutcome.setMessage("Update check status successfully");
            } else {
                serviceOutcome.setOutcome(false);
                serviceOutcome.setMessage("Update failed for one or both entities");
            }

        } catch (Exception e) {
            e.printStackTrace();
            serviceOutcome.setStatus("ERROR");
            serviceOutcome.setMessage("An error occurred while updating check status: " + e.getMessage());
        }

        return serviceOutcome;
    }


}

