/**
 *
 */
package com.aashdit.digiverifier.vendorcheck.service;

import com.aashdit.digiverifier.common.model.ServiceOutcome;
import com.aashdit.digiverifier.config.admin.model.CriminalCheck;
import com.aashdit.digiverifier.config.superadmin.Enum.ReportType;
import com.aashdit.digiverifier.config.superadmin.dto.DashboardDto;
import com.aashdit.digiverifier.config.superadmin.model.Source;
import com.aashdit.digiverifier.config.superadmin.model.VendorCheckStatusMaster;
import com.aashdit.digiverifier.vendorcheck.dto.*;
import com.aashdit.digiverifier.vendorcheck.model.ConventionalAttributesMaster;
import com.aashdit.digiverifier.vendorcheck.model.ConventionalVendorCandidatesSubmitted;
import com.aashdit.digiverifier.vendorcheck.model.ModeOfVerificationStatusMaster;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;

/**
 * @author ${Nanda Kishore}
 */
@Service
public interface liCheckToPerformService<T> {
    public ServiceOutcome<DashboardDto> findAllConventionalVendorSubmittedCandidatesByDateRange(DashboardDto dashboardDto) throws Exception;

    public ServiceOutcome<List<ConventionalVendorCandidatesSubmitted>> findAllSubmittedCandidatesByDateRangeOnInterimAndFinal(DashboardDto dashboardDto) throws Exception;

    public ServiceOutcome<LicheckRequiredResponseDto> addUpdateLiCheckToPerformData(FetchVendorConventionalCandidateDto licheckDto, String message) throws Exception;

    public ServiceOutcome<List<LicheckRequiredResponseDto>> findAllLiChecksRequired() throws Exception;

    public ServiceOutcome<List<LicheckRequiredResponseDto>> findAllLiChecksRequiredbyCandidateId(String candidateId) throws Exception;

    public ServiceOutcome<List<LicheckRequiredResponseDto>> findAllStopLiChecksRequiredbyCandidateId(String candidateId) throws Exception;

    public ServiceOutcome<List<LicheckRequiredResponseDto>> findAllNewUploadLiChecksRequiredbyCandidateId(String candidateId) throws Exception;

    public ServiceOutcome<List<LicheckRequiredResponseDto>> findAllLiChecksRequiredbyCheckStatus(String checkStatus) throws Exception;

    public ServiceOutcome<SubmittedCandidates> saveConventionalVendorSubmittedCandidates(String vendorId, boolean schedulerCondition) throws Exception;

    public ServiceOutcome<SubmittedCandidates> triggerCandidateDataAndCheckData(String VendorID, String requestId) throws Exception;

    public ServiceOutcome<List<SubmittedCandidates>> findAllConventionalVendorSubmittedCandidates() throws Exception;

    public ServiceOutcome<String> UpdateBGVCheckStatusRowwise(String vendorChecksString, String modeOfVerificationPerformed) throws Exception;

    public ServiceOutcome<ConventionalCandidateDocDto> saveConventionalCandidateDocumentInfo(FetchVendorConventionalCandidateDto fetchVendorConventionalCandidateDto) throws Exception;

    public ServiceOutcome<List<ConventionalCandidateDocDto>> findAllConventionalCandidateDocumentInfo() throws Exception;

    public ServiceOutcome<CandidateuploadS3Documents> findAllfilesUploadedurls(String candidateId, String checkName) throws Exception;

    public ServiceOutcome<String> findUpdateLicheckWithVendorCheck(Long vendorCheckId, Long liCheckId) throws Exception;

    public ServiceOutcome<String> updateLiCheckStatusByVendor(String vendorCheckStatusMasterId, String vendorCheckId, String remarks, String modeOfVericationPerformed, String licheckResponse) throws Exception;

    public ServiceOutcome<String> updateCandidateStatusByLicheckStatus();

    public ServiceOutcome<String> updateCandidateVerificationStatus(String requestId);

    public ServiceOutcome<List<VendorCheckStatusMaster>> findAllVendorCheckStatus();

//    public ServiceOutcome<List<liReportDetails>> generateDocumentConventional(String candidateId, String reportType);

    public ServiceOutcome<String> generateJsonRepsonseByConventionalCandidateId(String candidateId, ReportType reportType, String update, String reportDeliveryDate);

    public ServiceOutcome<String> generateConventionalCandidateReport(Long candidateCode, ReportType reportType, String update, String reportDeliveryDate);

    public ServiceOutcome<CombinedTrackerDto> generateTrackerData(TrackerRequestDto trackerRequestDto) throws Exception;

    public ServiceOutcome<ConventionalVendorCandidatesSubmitted> findConventionalCandidateByCandidateId(Long candiateId);

    public ServiceOutcome<byte[]> generateConventionalUtilizationReport() throws Exception;

    public ServiceOutcome<List<VendorReferenceDataDto>> generateReferenceDataToVendor(Long candidateId, Long checkId, String checkName) throws Exception;


    public ServiceOutcome<List<ModeOfVerificationStatusMaster>> findAllModeOfVerifcationPerformed() throws Exception;


    //new code
    ServiceOutcome<ConventionalAttributesMaster> saveConventionalAttributesMaster(ConventionalAttributesMaster conventionalAttributesMaster);

    //    ServiceOutcome<List<ConventionalAttributesMaster>> getConventionalAttributesMasterById(Long Id);
    ServiceOutcome getConventionalAttributesMasterById(Long vendorCheckId, String type);

    public ServiceOutcome<List<ConventionalVendorCandidatesSubmitted>> searchAllCandidate(String searchText);

    public ServiceOutcome<String> getRemarksForValidation(String checkUniqueId);

    public ServiceOutcome<String> reAssignToAnotherVendor(String checkUniqueId, String vendorId);

    public ServiceOutcome<byte[]> downloadAllFilebyRequestId(String requestId) throws Exception;

    public ServiceOutcome<String> deleteData(PurgeDto purgeDto) throws ParseException;

    public ServiceOutcome<String> updateIdentityCheckDisableStatus(String checkUniqueId, String enableStatus);

    ServiceOutcome<ConventionalAttributesMaster> updateConventionalAttributesMaster(ConventionalAttributesMaster conventionalAttributesMaster, Long attributeId);

    public ServiceOutcome<String> refetchCandidateData(String requestId, Boolean checkFetchFlag);

    public ServiceOutcome<DashboardDto> exportToExcelByDateRange(DashboardDto dashboardDto);

    public ServiceOutcome<String> reSubmitUpdateBGVCheckStatusRowwise(String checkUniqueId) throws Exception;

    public ServiceOutcome<String> generateDiscrepancyreport(Long candidateId, ReportType reportType, String updated, Long uniqueCheckId) throws Exception;

    public ServiceOutcome<String> generateDiscrepancyreportAgent(ReportType reportType, Long uniqueCheckId, Long requestId) throws Exception;

    public ServiceOutcome<String> UpdateBGVCheckStatusRowwiseByAgentLogin(CheckUniqueIdRequest checkUniqueIds) throws Exception;

    public ServiceOutcome<String> updateCheckStatus(String checkUniqueId, String selectedCheckStatusId) throws Exception;

    public ServiceOutcome<List<SubmittedCandidates>> fetchReportUploadPendingDetails(String VendorID) throws Exception;

    public ServiceOutcome<String> triggerPendingReport(String requestId, ReportType reportType, String update);


    public ServiceOutcome<String> getGstBase64(String gstnumber);

    public ServiceOutcome<String> getMcaBase64(String companyName);

    public ServiceOutcome<String> getReferenceDataByRequestIdAndCheckUniqueId(String requestId, String checkUniqueId);

    public ServiceOutcome<String> processDomainSearch(String companyName);

    ServiceOutcome<UanDto> getAgentUploadedData(String requestId);

    ServiceOutcome<Source> findSourcebySourceName(String sourceName);

    ServiceOutcome<String> updateCheckStatusByCheckUniqueId(String checkUniqueId, String selectedCheckstatus);

    public ServiceOutcome<List<PurgeResponseDto>> generatePurgeReport(String startDate, String endDate);

    public ServiceOutcome<CombinedVendorTrackerDto> generateVendorTrackerData(TrackerRequestDto trackerRequestDto) throws Exception;

    ServiceOutcome<List<CriminalCheck>> getCriminalChecksByCheckUniqueId(String checkUniqueId);
}
