import { Component, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { CandidateService } from 'src/app/services/candidate.service';
import { CustomerService } from 'src/app/services/customer.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-pending-reports',
  templateUrl: './pending-reports.component.html',
  styleUrls: ['./pending-reports.component.scss']
})
export class PendingReportsComponent implements OnInit {
  pageTitle = 'Pending Reports';
  pendingReports: any[] = [];
  searchText: string = '';  // Binding for search input
  page: number = 1;  // Default page
  pageSize: number = 10;  // Number of records per page
  filteredReports: any = [];  // Filtered data for pagination

  reportType: any;

  constructor(private customerService: CustomerService, private modalService: NgbModal, private candidateService: CandidateService) {
  this.reportType="";
    let vendorData = {
      VendorID: "2CDC7E3A"
    }
    this.customerService
      .fetchPendingReports(
        vendorData)
      .subscribe(
        (data: any) => {
          this.pendingReports = data.data;
          this.filteredReports = this.pendingReports;
        }
      )
  };
  ngOnInit(): void {

  }


  // Filter function to search by multiple fields
  search() {
    this.filteredReports = this.pendingReports.filter(item => {
      return item.candidateId.toLowerCase().includes(this.searchText.toLowerCase()) ||
        item.name.toLowerCase().includes(this.searchText.toLowerCase()) ||
        item.requestId.toLowerCase().includes(this.searchText.toLowerCase()) ||
        item.psNo.toLowerCase().includes(this.searchText.toLowerCase());
    });
    this.page = 1;  // Reset to the first page after search
  }

  // Get paginated data
  get paginatedReports() {
    const start = (this.page - 1) * this.pageSize;
    const end = start + this.pageSize;
    return this.filteredReports.slice(start, end);
  }

  // Function to handle page change
  changePage(page: number) {
    if (page > 0 && page <= this.totalPages()) {
      this.page = page;
    }
  }

  // Calculate total number of pages
  totalPages() {
    return Math.ceil(this.filteredReports.length / this.pageSize);
  }


  getReport(item: any) {
    debugger
    if (item.requestType.toLowerCase().includes("interim")) {
     this.reportType = "INTERIM";
    } else if (item.requestType.toLowerCase().includes("final")) {
      this.reportType  = "FINAL";
    }
    this.candidateService.generateReportWithReportType(item.requestId,this.reportType, "DONT","").subscribe(data =>
      // @ts-ignore
      window.open(data.data, "_blank")
    );
  }

  PendingReportConfirmation(content: any,item:any) {

   this.reportType=item.requestType;
    this.modalService.open(content, { centered: true }).result.then(
      (result) => {
        if (result === 'confirm') {
          this.uploadBgvPendingReports(item);
        } else {
        }
      },
      (reason) => {
      }
    );

  }
  isStatusValid(status: string): boolean {
    const validStatuses = ['interim', 'final', 'pending'];
    return validStatuses.some(validStatus => status.toLowerCase().includes(validStatus));
  }

  uploadBgvPendingReports(item:any) {


      if (item.requestType.toLowerCase().includes("interim")) {
        this.reportType = "INTERIM";
       } else if (item.requestType.toLowerCase().includes("final")) {
         this.reportType  = "FINAL";
       }
     this.candidateService.uploadBgvPendingReports(item.requestId, this.reportType, "UPDATE").subscribe(
       (result: any) => {
         if (result.outcome === true) {
           Swal.fire({
             title: result.message,
             icon: 'success'
           }).then((result) => {
             if (result.isConfirmed) {
               window.location.reload(); // Reloads the page on success
             }
           });
         } else {
           Swal.fire({
             title: result.message,
             icon: 'warning'
           }).then((result) => {
             if (result.isConfirmed) {
               window.location.reload(); // Reloads the page on warning
             }
           });
         }
       }
     );

    }
  getIconClass(item:any): string {
    if (!item?.reportResponse) {
            return "";
    }
   if (item.reportResponse?.includes('200') || item.reportResponse?.includes('Successfully Updated')) {
     return 'fas fa-check-circle'; // Green check mark for success
   } else {
     return 'fas fa-times-circle'; // Red cross for error
   }
 }

 getIconColor(item:any): string {
    if (!item?.reportResponse) {
         return "";
       }
   if (item?.reportResponse.includes('200') || item.reportResponse?.includes('Successfully Updated')) {
     return 'green';
   } else {
     return 'red';
   }
 }


}
