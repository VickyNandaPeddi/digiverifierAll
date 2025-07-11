import { Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { NgbDate, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { CandidateService } from 'src/app/services/candidate.service';
import { CustomerService } from 'src/app/services/customer.service';
import Swal from 'sweetalert2';
import * as XLSX from 'xlsx';

import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
@Component({
  selector: 'app-candidate-purged',
  templateUrl: './candidate-purged.component.html',
  styleUrls: ['./candidate-purged.component.scss'],
})
export class CandidatePurgedComponent implements OnInit {
  pageTitle = 'Purge Candiates';
  fromDate: any;
  toDate: any;
  setfromDate: any;
  settoDate: any;
  //@ts-ignore
  getToday: NgbDateStruct;
  //@ts-ignore
  getMinDate: NgbDateStruct;
  checkUniqueId: string = '';
  invalidRequestIds: boolean = false;
  selectedStatus: any;
  kyc: Boolean = false;
  hideLoadingBtn: Boolean = false;
  excelBuffer: any;
  fileName = 'export.xlsx';

  utilizationReportClick = new FormGroup({
    fromDate: new FormControl('', Validators.required),
    toDate: new FormControl('', Validators.required),
    organizationIds: new FormControl('', Validators.required),
    statusCode: new FormControl('', Validators.required),
    vendorStatusmasterId: new FormControl('', Validators.required),
  });
  utilizationReportFilter = new FormGroup({
    fromDate: new FormControl('', Validators.required),
    toDate: new FormControl('', Validators.required),
  });
  initToday: any;
  custId: any;
  ngOnInit(): void {}
  @ViewChild('statusUpdateModal') statusUpdateModal: any;
  @ViewChild('checkStatusModal') checkStatusModal: any;
  vendorCheckStatus: any = [];
  requestIds: string = ''; // User will enter comma-separated IDs

  constructor(
    private modalService: NgbModal,
    private candidateService: CandidateService,
    private customers: CustomerService
  ) {
    this.customers.getAllVendorCheckStatus().subscribe((data: any) => {

      this.vendorCheckStatus = data.data.filter(
        (item: any) => item.checkStatusCode !== 'CLEAR'
      );
    });
  }

  openStatusModal() {
    this.modalService.open(this.statusUpdateModal, { centered: true });
  }

  closeStatusModal(modal: any) {
    modal.close();
  }

  validateRequestIds() {
    const pattern = /^[0-9,]*$/; // Only numbers and commas allowed
    this.invalidRequestIds = !pattern.test(this.requestIds);
  }

  openCheckStatusModal() {
    this.checkUniqueId = ''; // reset
    this.modalService.open(this.checkStatusModal);
  }

  submitCheckStatus(modalRef: any) {
    const id = this.checkUniqueId?.trim();
    const status = this.selectedStatus;

    if (!id || !/^\d+$/.test(id)) {
      Swal.fire({
        title: 'Invalid ID',
        text: 'Please enter a valid numeric Check Unique ID.',
        icon: 'warning',
      });
      return;
    }

    if (!status) {
      Swal.fire({
        title: 'Status Not Selected',
        text: 'Please select a status before submitting.',
        icon: 'warning',
      });
      return;
    }

    // Call service with ID and selected status
    this.candidateService.updateCheckStatus(id, status).subscribe(
      (response) => {
        console.log('Check status response:', response);

        if (response?.outcome === true) {
          Swal.fire({
            title: response.message || 'Status updated successfully!',
            icon: 'success',
          }).then((result) => {
            if (result.isConfirmed) {
              modalRef.dismiss();
            }
          });
        } else {
          Swal.fire({
            title: 'Update Failed',
            text:
              response?.message ||
              'Something went wrong while updating the status.',
            icon: 'warning',
          });
        }
      },
      (error) => {
        console.error('Error checking status:', error);
        Swal.fire({
          title: 'Server Error',
          text:
            error?.error?.message ||
            'An error occurred while updating the check status.',
          icon: 'error',
        });
      }
    );
  }

  submitStatusUpdate() {
    const requestIdList = this.requestIds.split(',').map((id) => id.trim()); // Convert to list
    console.log('Request IDs:', requestIdList);

    this.candidateService.deleteDatabase(requestIdList).subscribe(
      (response) => {
        console.log('Database deleted successfully:', response);

        // Assuming the response contains an outcome (true or false) and a message.
        if (response.outcome === true) {
          Swal.fire({
            title: response.data,
            icon: 'success',
          }).then((result) => {
            if (result.isConfirmed) {
              window.location.reload();
            }
          });
        } else {
          Swal.fire({
            title: response.data,
            icon: 'warning',
          });
        }
      },
      (error) => {
        console.error('Error deleting database:', error);
        Swal.fire({
          title: 'An error occurred while deleting the database.',
          icon: 'error',
        });
      }
    );
  }
  filterLastMonth() {
    const today = new Date();
    const lastMonth = new Date(today.getFullYear(), today.getMonth() - 1, 1);
    const lastMonthEnd = new Date(today.getFullYear(), today.getMonth(), 0); // 0 = last day of previous month

    const fromStruct = this.convertToNgbDateStruct(lastMonth);
    const toStruct = this.convertToNgbDateStruct(lastMonthEnd);

    this.fromDate = fromStruct;
    this.toDate = toStruct;

    this.utilizationReportFilter.patchValue({
      fromDate: fromStruct,
      toDate: toStruct,
    });

    this.onSubmitFilter(this.utilizationReportFilter);
  }

  filterMonthToDate() {
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);

    const fromStruct = this.convertToNgbDateStruct(firstDay);
    const toStruct = this.convertToNgbDateStruct(today);

    this.fromDate = fromStruct;
    this.toDate = toStruct;

    this.utilizationReportFilter.patchValue({
      fromDate: fromStruct,
      toDate: toStruct,
    });

    this.onSubmitFilter(this.utilizationReportFilter);
  }

  filterToday() {
    const today = new Date();
    const todayStruct = this.convertToNgbDateStruct(today);

    this.fromDate = todayStruct;
    this.toDate = todayStruct;

    this.utilizationReportFilter.patchValue({
      fromDate: todayStruct,
      toDate: todayStruct,
    });

    this.onSubmitFilter(this.utilizationReportFilter);
  }

  convertToNgbDateStruct(date: Date): NgbDateStruct {
    return {
      year: date.getFullYear(),
      month: date.getMonth() + 1, // JS months are 0-based
      day: date.getDate(),
    };
  }

  onSubmitFilter(utilizationReportFilter: FormGroup): void {
    const fromDateRaw = utilizationReportFilter.value.fromDate;
    const toDateRaw = utilizationReportFilter.value.toDate;

    if (!fromDateRaw || !toDateRaw) {
      console.error('Missing date inputs');
      return;
    }

    // Convert to string in dd/MM/yyyy format
    const fromDateStr = this.formatDateTo_ddMMyyyy(fromDateRaw);
    const toDateStr = this.formatDateTo_ddMMyyyy(toDateRaw);

    this.fromDate = fromDateStr;
    this.toDate = toDateStr;

    this.customers.getPurgedCandidates(this.fromDate, this.toDate).subscribe({
      next: (response: any) => {
        debugger;
        if (
          response?.data &&
          Array.isArray(response.data) &&
          response.data.length > 0
        ) {
          this.exportToExcel(response.data);
        } else {
          Swal.fire({
            title: 'No records found for the selected dates.',
            icon: 'info',
            confirmButtonText: 'OK',
          });
        }
      },
      error: (err) => {
        console.error('API error:', err);
        Swal.fire({
          title: 'Error fetching data!',
          text: 'Please try again later.',
          icon: 'error',
          confirmButtonText: 'OK',
        });
      },
    });
  }

  formatDateTo_ddMMyyyy(date: {
    year: number;
    month: number;
    day: number;
  }): string {
    const dd = date.day < 10 ? `0${date.day}` : date.day;
    const mm = date.month < 10 ? `0${date.month}` : date.month;
    return `${dd}/${mm}/${date.year}`;
  }

  convertNgbDateToString(date: {
    year: number;
    month: number;
    day: number;
  }): string {
    const mm = date.month < 10 ? `0${date.month}` : date.month;
    const dd = date.day < 10 ? `0${date.day}` : date.day;
    return `${date.year}-${mm}-${dd}`;
  }

  onfromDate(event: any) {
    let year = event.year;
    let month = event.month <= 9 ? '0' + event.month : event.month;
    let day = event.day <= 9 ? '0' + event.day : event.day;
    let finalDate = day + '/' + month + '/' + year;
    this.fromDate = finalDate;
    this.getMinDate = { day: +day, month: +month, year: +year };
  }
  ontoDate(event: any) {
    let year = event.year;
    let month = event.month <= 9 ? '0' + event.month : event.month;
    let day = event.day <= 9 ? '0' + event.day : event.day;
    let finalDate = day + '/' + month + '/' + year;
    this.toDate = finalDate;
  }

  // exportToExcel(data: any[]) {
  //   const wsData: any[][] = [];

  //   // Add spacing before the title row
  //   wsData.push([], []);

  //   // Title starts in E (col 4) and spans E, F, G
  //   wsData.push([
  //     '',
  //     '',
  //     '',
  //     '', // A-D
  //     'Audit Trail / Purge Report (Confidential)', // E
  //     '',
  //     '', // F, G
  //   ]);

  //   // "Report Filters" label row
  //   wsData.push(['', '', '', '', 'Report Filters']);

  //   // Filter info
  //   wsData.push([
  //     '',
  //     '',
  //     '',
  //     'Start Date :',
  //     this.fromDate,
  //     'End Date :',
  //     this.toDate,
  //   ]);
  //   wsData.push(['', '', '', 'Search Text:', 'NA', 'Mode:', 'Purged']);
  //   wsData.push([
  //     '',
  //     '',
  //     '',
  //     'Customer:',
  //     'AdminDeep',
  //     'Company:',
  //     'AdminDeep',
  //   ]);
  //   wsData.push(['', '', '', 'Agent:', 'All', 'Candidate Status:', 'All']);
  //   wsData.push(['', '', '', 'Client Name:', 'AdminDeep']);
  //   wsData.push([
  //     '',
  //     '',
  //     '',
  //     'Address:',
  //     'No.18 & 18/1, Bikaner Signature Towers, Richmond Rd, Bengaluru, Karnataka 560025.',
  //   ]);

  //   wsData.push([]); // Empty row

  //   // Table headers
  //   wsData.push([
  //     'Sl#',
  //     'Agent Name',
  //     'App Id',
  //     'Candidate Type',
  //     'Candidate Name',
  //     'Date of Birth',
  //     'Candidate Uploaded',
  //     'Data Purged',
  //   ]);

  //   // Data rows
  //   let sl = 1;
  //   data.forEach((item) => {
  //     wsData.push([
  //       sl++,
  //       'Deep',
  //       Number(item.applicantId), // Ensure numeric format
  //       'Conventional',
  //       item.name || 'Purged',
  //       item.dateOfBirth,
  //       item.createdOn,
  //       item.purgedDate,
  //     ]);
  //   });

  //   const ws: XLSX.WorkSheet = XLSX.utils.aoa_to_sheet(wsData);

  //   // Merge E3:G3 for title (row 2, cols 4 to 6)
  //   ws['!merges'] = [
  //     { s: { r: 2, c: 4 }, e: { r: 2, c: 6 } }, // E3:G3 title
  //     { s: { r: 3, c: 4 }, e: { r: 3, c: 4 } }, // E4: Report Filters
  //     { s: { r: 8, c: 4 }, e: { r: 8, c: 6 } }, // E9:G9: Address
  //   ];

  //   // Apply style to title
  //   const titleCell = XLSX.utils.encode_cell({ r: 2, c: 4 }); // E3
  //   if (ws[titleCell]) {
  //     ws[titleCell].s = {
  //       alignment: { horizontal: 'center', vertical: 'center' },
  //       font: { bold: true, sz: 14 },
  //     };
  //   }

  //   // Set zoom level to 75%
  //   ws['!sheetViews'] = [
  //     {
  //       workbookViewId: 0,
  //       zoomScale: 75,
  //       rightToLeft: false,
  //       showGridLines: true,
  //     },
  //   ];

  //   // Set column widths
  //   ws['!cols'] = [
  //     { wch: 5 }, // A
  //     { wch: 15 }, // B
  //     { wch: 20 }, // C (App ID)
  //     { wch: 20 }, // D
  //     { wch: 35 }, // E
  //     { wch: 25 }, // F
  //     { wch: 30 }, // G
  //     { wch: 25 }, // H
  //   ];

  //   const wb: XLSX.WorkBook = XLSX.utils.book_new();
  //   XLSX.utils.book_append_sheet(wb, ws, 'Purged Candidates');
  //   XLSX.writeFile(wb, `Purged_Report_${this.fromDate}_to_${this.toDate}.xlsx`);
  // }

  // formatDate(dateStr: string): string {
  //   const date = new Date(dateStr);
  //   if (isNaN(date.getTime())) return '';
  //   const options: Intl.DateTimeFormatOptions = {
  //     day: '2-digit',
  //     month: 'short',
  //     year: 'numeric',
  //   };
  //   return date.toLocaleDateString('en-GB', options).replace(/ /g, '-'); // e.g. 07-Apr-2025
  // }

  exportToExcel(data: any[]) {
    const wsData: any[][] = [];

    // Add spacing before the title row
    wsData.push([], []);

    // Title starts in E (col 4) and spans E, F, G
    wsData.push([
      '',
      '',
      '',
      '', // A-D
      'Audit Trail / Purge Report (Confidential)', // E
      '',
      '', // F, G
    ]);

    // "Report Filters" label row
    wsData.push(['', '', '', '', 'Report Filters']);

    // Filter info
    wsData.push([
      '',
      '',
      '',
      'Start Date :',
      this.fromDate,
      'End Date :',
      this.toDate,
    ]);
    wsData.push(['', '', '', 'Search Text:', 'NA', 'Mode:', 'Purged']);
    wsData.push([
      '',
      '',
      '',
      'Customer:',
      'AdminDeep',
      'Company:',
      'AdminDeep',
    ]);
    wsData.push(['', '', '', 'Agent:', 'All', 'Candidate Status:', 'All']);
    wsData.push(['', '', '', 'Client Name:', 'AdminDeep']);
    wsData.push([
      '',
      '',
      '',
      'Address:',
      'No.18 & 18/1, Bikaner Signature Towers, Richmond Rd, Bengaluru, Karnataka 560025.',
    ]);

    wsData.push([]); // Empty row before table

    // Table headers
    wsData.push([
      'Sl#',
      'Agent Name',
      'App Id',
      'Candidate Type',
      'Candidate Name',
      'Date of Birth',
      'Candidate Uploaded',
      'Data Purged',
    ]);

    // Add data rows
    let sl = 1;
    data.forEach((item) => {
      wsData.push([
        sl++,
        'Deep',
        Number(item.applicantId), // Ensure numeric format
        'Conventional',
        item.name || 'Purged',
        item.dateOfBirth,
        this.formatDate(item.createdOn),
        this.formatDate(item.purgedDate),
      ]);
    });

    const ws: XLSX.WorkSheet = XLSX.utils.aoa_to_sheet(wsData);

    // Merge E3:G3 for title (row 2, cols 4 to 6)
    ws['!merges'] = [
      { s: { r: 2, c: 4 }, e: { r: 2, c: 6 } }, // E3:G3 title
      { s: { r: 3, c: 4 }, e: { r: 3, c: 4 } }, // E4: Report Filters
      { s: { r: 8, c: 4 }, e: { r: 8, c: 6 } }, // E9:G9: Address
    ];

    // Style the title cell
    const titleCell = XLSX.utils.encode_cell({ r: 2, c: 4 }); // E3
    if (ws[titleCell]) {
      ws[titleCell].s = {
        alignment: { horizontal: 'center', vertical: 'center' },
        font: { bold: true, sz: 14 },
      };
    }

    // Set zoom level to 75%
    ws['!sheetViews'] = [
      {
        workbookViewId: 0,
        zoomScale: 75,
        showGridLines: true,
      },
    ];

    // Set column widths
    ws['!cols'] = [
      { wch: 5 }, // A
      { wch: 15 }, // B
      { wch: 20 }, // C (App ID)
      { wch: 20 }, // D
      { wch: 35 }, // E
      { wch: 25 }, // F
      { wch: 30 }, // G
      { wch: 25 }, // H
    ];

    // Optional: Apply smaller font globally
    Object.keys(ws).forEach((key) => {
      if (key[0] === '!') return;
      if (!ws[key].s) ws[key].s = {};
      ws[key].s.font = { name: 'Calibri', sz: 9 }; // Adjust font size as needed
    });

    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Purged Candidates');
    XLSX.writeFile(wb, `Purged_Report_${this.fromDate}_to_${this.toDate}.xlsx`);
  }

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    if (isNaN(date.getTime())) return '';
    const options: Intl.DateTimeFormatOptions = {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    };
    return date.toLocaleDateString('en-GB', options).replace(/ /g, '-'); // e.g. 07-Apr-2025
  }
}
