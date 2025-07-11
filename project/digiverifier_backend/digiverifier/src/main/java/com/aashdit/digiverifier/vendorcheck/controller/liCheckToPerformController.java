/**
 *
 */
package com.aashdit.digiverifier.vendorcheck.controller;

import com.aashdit.digiverifier.common.model.ServiceOutcome;
import com.aashdit.digiverifier.config.admin.model.CriminalCheck;
import com.aashdit.digiverifier.config.admin.repository.VendorChecksRepository;
import com.aashdit.digiverifier.config.admin.service.UserService;
import com.aashdit.digiverifier.config.superadmin.Enum.ReportType;
import com.aashdit.digiverifier.config.superadmin.dto.DashboardDto;
import com.aashdit.digiverifier.config.superadmin.model.Source;
import com.aashdit.digiverifier.config.superadmin.model.VendorCheckStatusMaster;
import com.aashdit.digiverifier.utils.AwsUtils;
import com.aashdit.digiverifier.vendorcheck.dto.*;
import com.aashdit.digiverifier.vendorcheck.model.ConventionalAttributesMaster;
import com.aashdit.digiverifier.vendorcheck.model.ConventionalVendorCandidatesSubmitted;
import com.aashdit.digiverifier.vendorcheck.model.ModeOfVerificationStatusMaster;
import com.aashdit.digiverifier.vendorcheck.service.liCheckToPerformService;
import com.aashdit.digiverifier.vendorcheck.service.liCheckToPerformServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author ${Nanda Kishore}
 */

@Slf4j
@RestController
@RequestMapping("/api/vendorCheck")

public class liCheckToPerformController {

    @Autowired
    liCheckToPerformService liCheckToPerformService;

    @Autowired
    UserService userService;


    //when created it will be newupl


    @Operation(summary =  "performs vendorcheck and save the data")
    @PostMapping(path = "/updateBgvCheckStatusRowwise/", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ServiceOutcome<String> updateBgvCheckStatusRowwise(@RequestParam String vendorchecks) throws Exception {
        ServiceOutcome<String> response = liCheckToPerformService.UpdateBGVCheckStatusRowwise(vendorchecks, null);
        return response;
    }


    @Operation(summary =  "performs vendor check and save the data")
    @PostMapping(path = "/updateBgvCheckStatusRowwiseByAgent")
    public ServiceOutcome<String> updateBgvCheckStatusRowwiseAtAgentLogin(@RequestBody CheckUniqueIdRequest request) throws Exception {
        List<String> checkUniqueIds = request.getCheckUniqueIds();
        return liCheckToPerformService.UpdateBGVCheckStatusRowwiseByAgentLogin(request);
    }


    @GetMapping(path = "/resubmitBgvCheckStatusRowwise/{checkUniqueId}")
    public ServiceOutcome<String> resubmitBgvCheckStatusRowwise(@PathVariable("checkUniqueId") String checkUniqueId) throws Exception {
        ServiceOutcome<String> response = liCheckToPerformService.reSubmitUpdateBGVCheckStatusRowwise(checkUniqueId);
        return response;
    }

    @Operation(summary =  "finds All LiChecksRequired and returns the data")
    @GetMapping(value = "/findAllLiChecks")
    public ServiceOutcome<List<LicheckRequiredResponseDto>> findAllLiChecksRequired() throws Exception {
        ServiceOutcome<List<LicheckRequiredResponseDto>> response = liCheckToPerformService.findAllLiChecksRequired();
        response.setData(response.getData());
        response.setStatus("200");
        response.setOutcome(true);
        response.setMessage("liCheckPerform Saved Sucessfully");
        return response;
    }


    @Operation(summary =  "finds All LiChecksRequired and returns the data")
    @GetMapping(value = "/findAllLiChecks/{candidateId}")
    public ServiceOutcome<List<LicheckRequiredResponseDto>> findAllLiChecksRequiredbyCandidateId
            (@PathVariable("candidateId") String candidateId) throws Exception {
        ServiceOutcome<List<LicheckRequiredResponseDto>> response = liCheckToPerformService.findAllLiChecksRequiredbyCandidateId(candidateId);
        response.setData(response.getData());
        response.setStatus("200");
        response.setOutcome(true);
        response.setMessage("liCheckPerform Saved Sucessfully");
        return response;
    }

    @Operation(summary =  "finds All LiChecksRequired and returns the data")
    @GetMapping(value = "/findAllStopLiChecks/{candidateId}")
    public ServiceOutcome<List<LicheckRequiredResponseDto>> findAllStopLiChecksRequiredbyCandidateId
            (@PathVariable("candidateId") String candidateId) throws Exception {
        ServiceOutcome<List<LicheckRequiredResponseDto>> response = liCheckToPerformService.findAllStopLiChecksRequiredbyCandidateId(candidateId);
        response.setData(response.getData());
        response.setStatus("200");
        response.setOutcome(true);
        response.setMessage("liCheckPerform Saved Sucessfully");
        return response;
    }

    @Operation(summary =  "finds All LiChecksRequired and returns the data")
    @GetMapping(value = "/findAllNewUploadLiChecks/{candidateId}")
    public ServiceOutcome<List<LicheckRequiredResponseDto>> findAllNewUploadLiChecksRequiredbyCandidateId
            (@PathVariable("candidateId") String candidateId) throws Exception {
        ServiceOutcome<List<LicheckRequiredResponseDto>> response = liCheckToPerformService.findAllNewUploadLiChecksRequiredbyCandidateId(candidateId);
        response.setData(response.getData());
        response.setStatus("200");
        response.setOutcome(true);
        response.setMessage("liCheckPerform Saved Sucessfully");
        return response;
    }


    @Operation(summary =  "finds All LiChecksRequired and returns the data")
    @GetMapping(value = "/findConventionalCandidate/{candidateId}")
    public ServiceOutcome<ConventionalVendorCandidatesSubmitted> findConventionalCandidateByCandidateId
            (@PathVariable("candidateId") Long candidateId) throws Exception {
        ServiceOutcome<ConventionalVendorCandidatesSubmitted> response = liCheckToPerformService.findConventionalCandidateByCandidateId(candidateId);
        return response;
    }

    @Operation(summary =  "finds All LiChecksRequired and returns the data")
    @GetMapping(value = "/findAllLiChecksByCheckStatus/{checkStatus}")
    public ServiceOutcome<List<LicheckRequiredResponseDto>> findAllLiChecksRequiredbyCheckStatus
            (@PathVariable("checkStatus") String checkStatus) throws Exception {
        ServiceOutcome<List<LicheckRequiredResponseDto>> response = liCheckToPerformService.findAllLiChecksRequiredbyCheckStatus(checkStatus);
        response.setData(response.getData());
        response.setStatus("200");
        response.setOutcome(true);
        response.setMessage("liCheckPerform Saved Sucessfully");
        return response;
    }

    @Operation(summary =  "saves the submittedCandidate and returns the data")
    @PostMapping(value = "/saveSubmittedCandidates", produces = "application/json")
    public ServiceOutcome<SubmittedCandidates> saveConventionalVendorSubmittedCandidates(@RequestBody String
                                                                                                 VendorID) throws Exception {
        ServiceOutcome<SubmittedCandidates> response = liCheckToPerformService.saveConventionalVendorSubmittedCandidates(VendorID, false);
        response.setData(response.getData());
        response.setStatus(response.getStatus());
        response.setOutcome(response.getOutcome());
        response.setMessage(response.getMessage());
        return response;
    }

    @Operation(summary =  "fetchReportUploadPendingDetails")
    @PostMapping(value = "/fetchReportUploadPendingDetails", produces = "application/json")
    public ServiceOutcome<List<SubmittedCandidates>> fetchReportUploadPendingDetails(@RequestBody String VendorID) throws Exception {
        ServiceOutcome<List<SubmittedCandidates>> response = liCheckToPerformService.fetchReportUploadPendingDetails(VendorID);
        return response;
    }

    @Operation(summary =  "saves the submittedCandidate and returns the data")
    @GetMapping(value = "/refetchCandidateData", produces = "application/json")
    public ServiceOutcome<String> refetchCandidateBasicData(@RequestParam String requestId, @RequestParam Boolean checkFetchFlag) throws Exception {
        ServiceOutcome<String> response = liCheckToPerformService.refetchCandidateData(requestId, checkFetchFlag);
        return response;
    }

    @Operation(summary =  "saves the submittedCandidate and returns the data")
    @PostMapping(value = "/saveSubmittedCandidatesForTriggerCheckStatus/{requestId}", produces = "application/json")
    public ServiceOutcome<SubmittedCandidates> saveConventionalVendorSubmittedCandidatesTriggerCheckStatus(@RequestBody String VendorID, @PathVariable("requestId") String requestid) throws Exception {
        ServiceOutcome<SubmittedCandidates> response = liCheckToPerformService.triggerCandidateDataAndCheckData(VendorID, requestid);
        response.setData(response.getData());
        response.setStatus(response.getStatus());
        response.setOutcome(response.getOutcome());
        response.setMessage(response.getMessage());
        return response;
    }

    @Operation(summary =  "finds All submittedCandidates and returns the data")
    @GetMapping(value = "/findAllSubmittedCandidates")
    public ServiceOutcome<List<SubmittedCandidates>> findAllConventionalVendorSubmittedCandidates() throws
            Exception {
        ServiceOutcome<List<SubmittedCandidates>> response = liCheckToPerformService.findAllConventionalVendorSubmittedCandidates();
        response.setData(response.getData());
        response.setStatus("200");
        response.setOutcome(true);
        response.setMessage("candidates Fetched Sucessfully");
        return response;
    }

    @Operation(summary =  "saves the submittedCandidate and returns the data")
    @GetMapping(value = "/saveCandidateDocuments/{candidateId}")
    public ServiceOutcome<ConventionalCandidateDocDto> saveConventionalCandidateDocuments
            (@RequestBody FetchVendorConventionalCandidateDto fetchVendorConventionalCandidateDto) throws Exception {
        ServiceOutcome<ConventionalCandidateDocDto> response = liCheckToPerformService.saveConventionalCandidateDocumentInfo(fetchVendorConventionalCandidateDto);
        response.setData(response.getData());
        response.setStatus("200");
        response.setOutcome(true);
        response.setMessage("candidateDocument Saved Sucessfully");
        return response;
    }

    @Operation(summary =  "finds All submittedCandidates and returns the data")
    @GetMapping(value = "/findAllCandidateDocuments")
    public ServiceOutcome<List<ConventionalCandidateDocDto>> findAllConventionalCandidatesDocuments() throws
            Exception {
        ServiceOutcome<List<ConventionalCandidateDocDto>> response = liCheckToPerformService.findAllConventionalCandidateDocumentInfo();
        response.setData(response.getData());
        response.setStatus("200");
        response.setOutcome(true);
        response.setMessage("conventional Candidates documents Fetched Sucessfully");
        return response;
    }


    @Operation(summary =  "finds All submittedCandidates and returns the data")
    @GetMapping(value = "/findPrecisedUrl/{conventionalCandidateId}/{checkName}")
    public ServiceOutcome<CandidateuploadS3Documents> findAllfilesUploadedurls
            (@PathVariable("conventionalCandidateId") String caonvetionalCandidateId, @PathVariable("checkName") String checkName) throws Exception {
        ServiceOutcome<CandidateuploadS3Documents> response = liCheckToPerformService.findAllfilesUploadedurls(caonvetionalCandidateId, checkName);
        response.setData(response.getData());
        response.setStatus("200");
        response.setOutcome(true);
        response.setMessage("conventional Candidates documents Fetched Sucessfully");
        return response;
    }

    //which will be done on vendor uploads
    @Operation(summary =  "finds All submittedCandidates and returns the data")
    @GetMapping(value = "/updateLicheckStatusByVendor/{vendorCheckStatusMasterId}/{vendorCheckId}/{remarks}/{modeOfVericationPerformed}")
    public ServiceOutcome<?> updateLiCheckStatusByVendor(@PathVariable(value = "vendorCheckStatusMasterId") String
                                                                 vendorCheckStatusMasterId, @PathVariable(value = "vendorCheckId") String
                                                                 vendorCheckId, @PathVariable(value = "remarks") String
                                                                 remarks, @PathVariable("modeOfVericationPerformed") String modeOfVericationPerformed) throws Exception {
        ServiceOutcome<String> response = liCheckToPerformService.updateLiCheckStatusByVendor(vendorCheckStatusMasterId, vendorCheckId, remarks, modeOfVericationPerformed, "");
        response.setData(response.getData());
        response.setStatus("200");
        response.setOutcome(true);
        response.setMessage("conventional Candidates documents Fetched Sucessfully");
        return new ServiceOutcome<String>();

    }

//    @Operation(summary =  "")
//    @GetMapping(value = "/updateLicheckWithVendorcheck/{vendorCheckId}/{liCheckid}")
//    public ServiceOutcome<?> findUpdateLicheckWithVendorCheck(@PathVariable(value = "vendorCheckId") String
//                                                                      vendorId, @PathVariable(value = "liCheckid") String liCheckId) throws Exception {
//        ServiceOutcome<String> response = liCheckToPerformService.findUpdateLicheckWithVendorCheck(vendorId, liCheckId);
//        response.setData(response.getData());
//        response.setStatus("200");
//        response.setOutcome(true);
//        response.setMessage("conventionaffindAllSubmittedCandidatesByDateRangel Candidates documents Fetched Sucessfully");
//        return new ServiceOutcome<String>();
//    }


    @Operation(summary =  "finds All submittedCandidates and returns the data")
    @GetMapping(value = "/findAllVendorCheckStatus")
    public ServiceOutcome<?> findAllVendorCheckStatusMasterStatuses() {
        ServiceOutcome<List<VendorCheckStatusMaster>> response = liCheckToPerformService.findAllVendorCheckStatus();
        response.setData(response.getData());
        response.setStatus("200");
        response.setOutcome(true);
        response.setMessage("conventional Candidates documents Fetched Sucessfully");
        return response;

    }


//    @Operation(summary =  "finds All submittedCandidates and returns the data")
//    @GetMapping(value = "/updateConvetionalStatusByLicheckStatus")
//    public ServiceOutcome<?> updateConventionalCandidateStatusBasedOnLiCheckStatus() throws Exception {
//        ServiceOutcome<String> response = liCheckToPerformService.updateConventionalCandidateStatusBasedOnLiCheckStatus();
//        response.setData(response.getData());
//        response.setStatus("200");
//        response.setOutcome(true);
//        response.setMessage("conventional Candidates documents Fetched Sucessfully");
//        return new ServiceOutcome<String>();
//
//    }


    @Operation(summary =  "finds All LiChecksRequired and returns the data")
    @GetMapping(value = "/updateCandidateStatus")
    public ServiceOutcome<String> updateConventionalCandidateStatusByLicheckStatus() throws Exception {
        ServiceOutcome<String> response = liCheckToPerformService.updateCandidateStatusByLicheckStatus();
        response.setData(response.getData());
        response.setStatus("200");
        response.setOutcome(true);
        response.setMessage("liCheckPerform Saved Sucessfully");
        return response;
    }

    @Operation(summary =  "update verifcationstatus of candidate")
    @GetMapping(value = "/updateCandidateVerificationStatus")
    public ServiceOutcome<String> updateCandidateVerificationStatus() throws Exception {
        ServiceOutcome<String> response = liCheckToPerformService.updateCandidateVerificationStatus("");
        response.setData(response.getData());
        response.setStatus("200");
        response.setOutcome(true);
        response.setMessage("liCheckPerform Saved Sucessfully");
        return response;
    }

    @Operation(summary =  "finds All submittedCandidates by date range and returns the data")
    @RequestMapping(value = "/findAllSubmittedCandidatesByDateRange", method = {RequestMethod.POST})
    public ServiceOutcome<DashboardDto> findAllConventionalVendorSubmittedCandidatesByDateRange
            (@RequestHeader("Authorization") String authorization, @RequestBody DashboardDto dashboardDto) throws Exception {
        ServiceOutcome<DashboardDto> response = liCheckToPerformService.findAllConventionalVendorSubmittedCandidatesByDateRange(dashboardDto);
        response.setData(response.getData());
        response.setStatus("200");
        response.setOutcome(true);
        response.setMessage("candidates Fetched Sucessfully");
        return response;
    }

    @Operation(summary =  "finds All submittedCandidates by date range and returns the data")
    @RequestMapping(value = "/findAllSubmittedCandidatesByDateRangeForExcelExport", method = {RequestMethod.POST})
    public ServiceOutcome<DashboardDto> findAllConventionalVendorSubmittedCandidatesByDateRangeForExcelExport
            (@RequestHeader("Authorization") String authorization, @RequestBody DashboardDto dashboardDto) throws Exception {
        ServiceOutcome<DashboardDto> response = liCheckToPerformService.exportToExcelByDateRange(dashboardDto);
        return response;
    }

    @Operation(summary =  "finds All submittedCandidates by date range and returns the data")
    @RequestMapping(value = "/findCandidateSubmittedForInterimandFinal", method = {RequestMethod.POST})
    public ServiceOutcome<List<ConventionalVendorCandidatesSubmitted>> findCandidateSubmittedForInterimandFinal
            (@RequestHeader("Authorization") String authorization, @RequestBody DashboardDto dashboardDto) throws Exception {
        ServiceOutcome<List<ConventionalVendorCandidatesSubmitted>> response = liCheckToPerformService.findAllSubmittedCandidatesByDateRangeOnInterimAndFinal(dashboardDto);
        response.setData(response.getData());
        response.setStatus("200");
        response.setOutcome(true);
        response.setMessage("candidates Fetched Sucessfully");
        response.getData().forEach(resp -> {
            log.info("Created Date {}", resp.getCreatedOn());
        });
        return response;
    }

    @Autowired
    AwsUtils awsUtils;


    @Operation(summary =  "finds All submittedCandidates and returns the data")
    @PostMapping(value = "/generatePrecisedUrl")
    public ServiceOutcome<String> findAllfilesUploadedufdsfssdarls(@RequestBody String precisedUrl) throws
            Exception {
        ServiceOutcome<String> serviceOutcome = new ServiceOutcome<>();
//        String path = "Candidate/Convetional/" + candidateID + "/" + documentName;
        precisedUrl = precisedUrl.replace("\"", "");
        String presignedUrl = awsUtils.getPresignedUrl("digiverifier-new", precisedUrl);
        System.out.println(presignedUrl);
        serviceOutcome.setData(presignedUrl);
        serviceOutcome.setStatus("200");
        serviceOutcome.setOutcome(true);
        serviceOutcome.setMessage("conventional Candidates documents Fetched Sucessfully");
        return serviceOutcome;
    }

    //generate report


//    @Operation(summary =  "finds All submittedCandidates by date range and returns the data")
//    @RequestMapping(value = "/generateReport/{authToken}", method = {RequestMethod.GET, RequestMethod.POST})
//    public ServiceOutcome<String> generateReport( @PathVariable("authToken") String authToken) throws Exception {
//        ServiceOutcome<String> response = new ServiceOutcome<>();

    /// /        String s = reportService.generateDocument("2Z5G8HCUYWK6", authToken, ReportType.INTERIM);
    /// /        response.setData(s);
//        response.setStatus("200");
//        response.setOutcome(true);
//        response.setMessage("candidates Fetched Sucessfully");
//        return response;
//    }


//    @Operation(summary =  "finds All submittedCandidates by date range and returns the data")
//    @RequestMapping(value = "/generateReportForConventionalCandidate/{canididateId}/{reportType}", method = {RequestMethod.GET, RequestMethod.POST})
//    public ServiceOutcome<List<liReportDetails>> generateDocumentReportConventional
//            (@PathVariable(name = "canididateId") String candidateId, @PathVariable("reportType") String reportType) throws
//            Exception {
//        ServiceOutcome<List<liReportDetails>> response = liCheckToPerformService.generateDocumentConventional(candidateId, reportType);
//        response.setData(response.getData());
//        response.setStatus("200");
//        response.setOutcome(true);
//        response.setMessage("candidates Fetched Sucessfully");
//        return response;
//    }
    @Operation(summary =  "generate response for Report response")
    @RequestMapping(value = "/generateJsonByCandidateId/{canididateId}/{reportType}/{updated}", method = {RequestMethod.GET}, produces = "application/json")
    public ServiceOutcome<String> generateJsonByCandidateId(@PathVariable(name = "canididateId") String
                                                                    candidateId, @PathVariable("reportType") ReportType reportType, @PathVariable("updated") String update, @RequestParam("reportDeliveryDate") String reportDeliveryDate) throws
            Exception {
        ServiceOutcome<String> response = liCheckToPerformService.generateJsonRepsonseByConventionalCandidateId(candidateId, reportType, update, reportDeliveryDate);
        return response;
    }

    @Operation(summary =  "generate response for Report response")
    @RequestMapping(value = "/uploadBgvPendingReports/{canididateId}/{reportType}/{updated}", method = {RequestMethod.GET}, produces = "application/json")
    public ServiceOutcome<String> triggerPendingReport(@PathVariable(name = "canididateId") String
                                                               candidateId, @PathVariable("reportType") ReportType reportType, @PathVariable("updated") String update) throws
            Exception {
        ServiceOutcome<String> response = liCheckToPerformService.triggerPendingReport(candidateId, reportType, update);
        return response;
    }

    @Operation(summary =  "generate response for Report response")
    @RequestMapping(value = "/generateConventionalUtilizationReport", method = {RequestMethod.GET}, produces = "application/json")
    public ServiceOutcome<byte[]> generateJsonResponse() throws Exception {
        ServiceOutcome<byte[]> response = liCheckToPerformService.generateConventionalUtilizationReport();
        response.setData(response.getData());
        response.setStatus("200");
        response.setOutcome(true);
        return response;
    }

    @Autowired
    VendorChecksRepository vendorChecksRepository;

//    @Operation(summary =  "generate response for Report response")
//    @RequestMapping(value = "/generateConventionalUtilizationReportTest", method = {RequestMethod.GET}, produces = "application/json")
//    public ServiceOutcome<List<ReportUtilizationVendorDto>> generateJsonResponsefrontendtest() throws Exception {
//        ServiceOutcome<List<ReportUtilizationVendorDto>> response = new ServiceOutcome<>();

    /// /        List<ReportUtilizationVendorDto> allVendorCandidateAndSourceId = vendorChecksRepository.findAllVendorCandidateAndSourceId();
//        response.setData(allVendorCandidateAndSourceId);
//        response.setStatus("200");
//        response.setOutcome(true);
//        return response;
//    }
    @Operation(summary =  "generate response for Report response")
    @RequestMapping(value = "/generateReferenceDataForVendor/{candidateId}/{checkId}", method = {RequestMethod.GET}, produces = "application/json")
    public ServiceOutcome<List<VendorReferenceDataDto>> generateReferenceDataToVendor
    (@PathVariable("candidateId") Long candidateId, @PathVariable("checkId") Long checkId, @RequestParam("checkName") String checkName) throws Exception {
        ServiceOutcome<List<VendorReferenceDataDto>> response = liCheckToPerformService.generateReferenceDataToVendor(candidateId, checkId, checkName);
        response.setData(response.getData());
        response.setStatus("200");
        response.setOutcome(true);
        return response;
    }

    @Operation(summary =  "generate response for Report response")
    @RequestMapping(value = "/findAllModeOfVerificationPerformed", method = {RequestMethod.GET}, produces = "application/json")
    public ServiceOutcome<List<ModeOfVerificationStatusMaster>> findAllModeofVerificationPerformed() throws
            Exception {
        ServiceOutcome<List<ModeOfVerificationStatusMaster>> response = liCheckToPerformService.findAllModeOfVerifcationPerformed();
        response.setData(response.getData());
        response.setStatus("200");
        response.setOutcome(true);
        return response;
    }

    //    @Operation(summary =  "generate attributes for Report response")
//    @RequestMapping(value = "/generateReportAttributes", method = {RequestMethod.GET}, produces = "application/json")
//    public ServiceOutcome<ConventionalReportVendor> addConventionalVendorReportAttributes() throws Exception {
//        ServiceOutcome<ConventionalReportVendor> response = liCheckToPerformService.addConventionalVendorReportAttributes();
//        response.setData(response.getData());
//        response.setStatus("200");
//        response.setOutcome(true);
//        return response;
//    }
    @Operation(summary = "Save and  Update ConventionalAttributesMaster")
    @PostMapping("/saveConventionalAttributesMaster")
    public ResponseEntity<ServiceOutcome<ConventionalAttributesMaster>> saveConventionalAttributesMaster(@RequestBody ConventionalAttributesMaster conventionalAttributesMaster, @RequestHeader("Authorization") String authorization) {

        System.out.println("ConventionalAttributesMaster**************" + conventionalAttributesMaster);
        ServiceOutcome<ConventionalAttributesMaster> svcSearchResult = liCheckToPerformService.saveConventionalAttributesMaster(conventionalAttributesMaster);
        return new ResponseEntity<ServiceOutcome<ConventionalAttributesMaster>>(svcSearchResult, HttpStatus.OK);

    }


    @Operation(summary = "Get By Id ConventionalAttributesMaster")
    @GetMapping("/getConventionalAttributesMaster/{vendorCheckId}/{type}")
    public ResponseEntity<ServiceOutcome<?>> getConventionalAttributesMasterById(@PathVariable("vendorCheckId") Long sourceId, @PathVariable("type") String type) {
        ServiceOutcome<?> svcSearchResult = liCheckToPerformService.getConventionalAttributesMasterById(sourceId, type);
        return new ResponseEntity<ServiceOutcome<?>>(svcSearchResult, HttpStatus.OK);
    }

    @GetMapping("/searchAllCandidate")
    public ResponseEntity<ServiceOutcome<List<ConventionalVendorCandidatesSubmitted>>> searchConventionalText(@RequestParam("searchText") String searchText) {
        ServiceOutcome<List<ConventionalVendorCandidatesSubmitted>> listServiceOutcome = liCheckToPerformService.searchAllCandidate(searchText);
        return new ResponseEntity<ServiceOutcome<List<ConventionalVendorCandidatesSubmitted>>>(listServiceOutcome, HttpStatus.OK);
    }

    @GetMapping("/getCheckUniqueIdForRemarks")
    public ResponseEntity<ServiceOutcome<String>> getRemarksForValidate(@RequestParam("checkUniqueId") String checkUniqueId) {
        ServiceOutcome<String> listServiceOutcome = liCheckToPerformService.getRemarksForValidation(checkUniqueId);
        return new ResponseEntity<ServiceOutcome<String>>(listServiceOutcome, HttpStatus.OK);
    }

    @GetMapping("/reassignVendor/{checkUniqueId}/{vendorId}")
    public ResponseEntity<ServiceOutcome<String>> reAssignToAnotherVendor(@PathVariable("checkUniqueId") String checkUniqueId, @PathVariable("vendorId") String vendorId) {
        ServiceOutcome<String> listServiceOutcome = liCheckToPerformService.reAssignToAnotherVendor(checkUniqueId, vendorId);
        return new ResponseEntity<ServiceOutcome<String>>(listServiceOutcome, HttpStatus.OK);
    }

    @Autowired
    liCheckToPerformServiceImpl liCheckToPerformServiceimpl;

    @GetMapping("/updatebgvcheckstatustoonprogress/{requestId}/{checkUniqueId}")
    public ResponseEntity<ServiceOutcome<String>> updateBgvToprogress(@PathVariable("requestId") String requestId, @PathVariable("checkUniqueId") String checkUniqueId) {
        ServiceOutcome stringServiceOutcome = liCheckToPerformServiceimpl.updateBgvCheckRowwiseonProgress(Long.valueOf(requestId), Long.valueOf(checkUniqueId));
        return new ResponseEntity<ServiceOutcome<String>>(
                stringServiceOutcome, HttpStatus.OK);
    }

    @GetMapping("/downloadAllUploadDocuments/{requestId}")
    public ResponseEntity<byte[]> downloadZipFile(@PathVariable("requestId") String requestId) throws Exception {
        ServiceOutcome<byte[]> stringServiceOutcome = liCheckToPerformService.downloadAllFilebyRequestId(requestId);
        byte[] zipFileBytes = stringServiceOutcome.getData();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "your-filename.zip");
        return new ResponseEntity<>(zipFileBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/deletedatabase")
    public ServiceOutcome<String> deletedatabase(@RequestBody PurgeDto purgeDto) throws Exception {
        ServiceOutcome serviceOutcome = liCheckToPerformService.deleteData(purgeDto);
        return serviceOutcome;
    }

    @GetMapping("/updateIdentityCheckDisableStatus/{checkUniqueId}/{enableStatus}")
    public ServiceOutcome<String> updateIdentityCheckDisableStatus(@PathVariable("checkUniqueId") String checkUniqueId, @PathVariable("enableStatus") String enableStatus) throws Exception {
        ServiceOutcome<String> stringServiceOutcome = liCheckToPerformService.updateIdentityCheckDisableStatus(checkUniqueId, enableStatus);
        return stringServiceOutcome;
    }

    @Operation(summary = "Update ConventionalAttributesMaster")
    @PostMapping("/updateConventionalAttributesMaster/{attributeId}")
    public ResponseEntity<ServiceOutcome<ConventionalAttributesMaster>> updateConventionalAttributesMaster(@RequestBody ConventionalAttributesMaster conventionalAttributesMaster, @PathVariable("attributeId") Long attributeId, @RequestHeader("Authorization") String authorization) {

        System.out.println("ConventionalAttributesMaster**************" + conventionalAttributesMaster);
        System.out.println("CONVENTIONALATTRIBUTE_ID ==================== " + attributeId);
        ServiceOutcome<ConventionalAttributesMaster> svcSearchResult = liCheckToPerformService.updateConventionalAttributesMaster(conventionalAttributesMaster, attributeId);
        return new ResponseEntity<ServiceOutcome<ConventionalAttributesMaster>>(svcSearchResult, HttpStatus.OK);

    }

    @GetMapping("/generateDiscrepancyReport/{checkId}/{requestId}")
    public ServiceOutcome<String> generateDiscrepancyReport(@PathVariable("checkId") Long checkId, @PathVariable("requestId") Long requestId) throws Exception {
        ServiceOutcome<String> stringServiceOutcome = liCheckToPerformService.generateDiscrepancyreportAgent(ReportType.DISCREPANCY, checkId, requestId);
        return stringServiceOutcome;
    }

    @GetMapping("/updateCheckStatus/{checkUniqueId}/{selectedCheckStatusId}")
    public ServiceOutcome<String> updateCheckStatus(@PathVariable("checkUniqueId") String checkUniqueId, @PathVariable("selectedCheckStatusId") String selectedCheckStatusId) throws Exception {
        ServiceOutcome<String> stringServiceOutcome = liCheckToPerformService.updateCheckStatus(checkUniqueId, selectedCheckStatusId);
        return stringServiceOutcome;
    }

    @GetMapping("/gstDetails")
    public ServiceOutcome<String> getGstProofs(@RequestParam String gstInNumber) {
        ServiceOutcome serviceOutcome = liCheckToPerformService.getGstBase64(gstInNumber);
        return serviceOutcome;
    }

    @GetMapping("/mcaDetails")
    public ServiceOutcome<String> getMcaProofs(@RequestParam String companyName) {
        ServiceOutcome serviceOutcome = liCheckToPerformService.getMcaBase64(companyName);
        return serviceOutcome;
    }

    @GetMapping("/refereceDataByrequestIdAndCheckUniqueId")
    public ServiceOutcome<String> getReferenceDataByRequestIdAndCheckUniqueId(@RequestParam String checkName, @RequestParam String checkUniqueId) {
        ServiceOutcome serviceOutcome = liCheckToPerformService.getReferenceDataByRequestIdAndCheckUniqueId(checkName, checkUniqueId);
        return serviceOutcome;
    }

    @GetMapping("/domainSearch")
    public ServiceOutcome<String> getDomainSearchProofs(@RequestParam String companyName) {
        ServiceOutcome serviceOutcome = liCheckToPerformService.processDomainSearch(companyName);
        return serviceOutcome;
    }

    @Operation(summary =  "Tracker Data by date range and returns the data")
    @PostMapping(value = "/trackerdata")
    public ServiceOutcome<CombinedTrackerDto> generateTrackerData(@RequestBody TrackerRequestDto trackerRequestDto) throws Exception {
        ServiceOutcome<CombinedTrackerDto> serviceOutcome = liCheckToPerformService.generateTrackerData(trackerRequestDto);
        return serviceOutcome;
    }

    @Operation(summary =  "Vendor Tracker Data by date range and returns the data")
    @PostMapping(value = "/trackerdataVendor")
    public ServiceOutcome<CombinedVendorTrackerDto> generateTrackerDataVendor(@RequestBody TrackerRequestDto trackerRequestDto) throws Exception {
        ServiceOutcome<CombinedVendorTrackerDto> serviceOutcome = liCheckToPerformService.generateVendorTrackerData(trackerRequestDto);
        return serviceOutcome;
    }

    @Operation(summary =  "Upload Uan")
    @PostMapping(value = "/uploadUan", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ServiceOutcome<String> generateTrackerData(@RequestParam String uanDto, @RequestParam(value = "file", required = false) List<MultipartFile> proofDocumentNew) throws Exception {

        ServiceOutcome<String> serviceOutcome = userService.uploadUan(uanDto, proofDocumentNew);
        return serviceOutcome;
    }

    @Operation(summary =  "Upload Uan")
    @GetMapping(value = "/getAgentUploaded")
    public ServiceOutcome<UanDto> generateTrackerData(@RequestParam String requestId) throws Exception {
        ServiceOutcome<UanDto> serviceOutcome = liCheckToPerformService.getAgentUploadedData(requestId);
        return serviceOutcome;
    }

    @Operation(summary =  "Upload Uan")
    @GetMapping(value = "/getSourceName")
    public ServiceOutcome<Source> findSourcebySourceName(@RequestParam String sourceName) throws Exception {
        ServiceOutcome<Source> serviceOutcome = liCheckToPerformService.findSourcebySourceName(sourceName);
        return serviceOutcome;
    }


    @Operation(summary =  "updateCheckStatus")
    @GetMapping(value = "/updateCheckStatus")
    public ServiceOutcome<String> updateCheckStatusByCheckUniqueId(@RequestParam String checkUniqueId, @RequestParam String selectedCheckstatus) throws Exception {
        ServiceOutcome<String> serviceOutcome = liCheckToPerformService.updateCheckStatusByCheckUniqueId(checkUniqueId, selectedCheckstatus);
        return serviceOutcome;
    }

    @Operation(summary =  "Get Purged Candidates")
    @GetMapping(value = "/getPurgedCandidates", produces = "application/json")
    public ServiceOutcome<List<PurgeResponseDto>> getPurgedCandidates(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        ServiceOutcome<List<PurgeResponseDto>> outcome = new ServiceOutcome<>();
        try {
            outcome = liCheckToPerformService.generatePurgeReport(startDate, endDate);
        } catch (Exception e) {
            e.printStackTrace();
            outcome.setOutcome(false);
            outcome.setMessage("Error while fetching data: " + e.getMessage());
            outcome.setStatus("500");
        }
        return outcome;
    }

    @GetMapping("/criminalChecks/byCheckUniqueId")
    public ServiceOutcome<List<CriminalCheck>> getCriminalChecks(@RequestParam String checkUniqueId) {
        return liCheckToPerformService.getCriminalChecksByCheckUniqueId(checkUniqueId);
    }
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Object> handleAllThrowables(Throwable ex) {
        ex.printStackTrace(); // Prints the stack trace to console
        return new ResponseEntity<>("A fatal error occurred: " + ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

