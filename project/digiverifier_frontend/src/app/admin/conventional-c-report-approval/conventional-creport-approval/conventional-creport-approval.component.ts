import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ModalDismissReasons, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {CandidateService} from 'src/app/services/candidate.service';
import Swal from 'sweetalert2';
import * as XLSX from 'xlsx';
import {LoaderService} from "../../../services/loader.service";
import {CustomerService} from "../../../services/customer.service";

@Component({
  selector: 'app-conventional-creport-approval',
  templateUrl: './conventional-creport-approval.component.html',
  styleUrls: ['./conventional-creport-approval.component.scss']
})
export class ConventionalCReportApprovalComponent implements OnInit {
  pageTitle = 'Pending Approval';
  candidateCode: any = '';
  selectedDate: string = '';
  formReportApproval = new FormGroup({
    criminalVerificationColorId: new FormControl(''),
    globalDatabseCaseDetailsColorId: new FormControl('')
  });
  formAddcomment = new FormGroup({
    addComments: new FormControl('', Validators.required),
    id: new FormControl(''),
    candidateCode: new FormControl(''),
  });
  cApplicationFormDetails: any;
  candidateName: any;
  candidateResume: string = '';

  formEditDOC = new FormGroup({
    colorId: new FormControl(''),
    vendorChecks: new FormControl(''),
  });
  vendorChecks: any;
  getColors: any = [];
  employ: any = [];
  getServiceConfigCodes: any = [];
  public CaseDetailsDoc: any = File;
  public globalCaseDoc: any = File;
  closeModal: string | undefined;
  jsonData: any;
  finalReportDisabled: number | undefined;
  approveenable: any;
  candidateIdView: any;
  candidateRequestId: any;
  agentUploadedData:any = [];
  sourceId:any;
  constructor(private modalService: NgbModal, private customer: CustomerService, private candidateService: CandidateService, private fb: FormBuilder, private router: ActivatedRoute, private navRouter: Router, private loaderService: LoaderService) {
    // this.candidateCode = this.router.snapshot.paramMap.get('candidateCode');
    const candidateid = localStorage.getItem('capprequestid');
    this.candidateRequestId = localStorage.getItem('capprequestid');
    this.candidateIdView = localStorage.getItem('cappcandidateId');
    this.candidateName = localStorage.getItem('cappname');
    console.log("candidate id cproval" + this.candidateCode);
    this.candidateCode = candidateid;
    this.approveenable = localStorage.getItem('approveenable');
    this.candidateService.getCandidateConventional_admin(this.candidateCode).subscribe((data: any) => {
      this.cApplicationFormDetails = data.data;
      console.log(this.cApplicationFormDetails, "------------candidate-----------");
      // @ts-ignore
      this.candidateName = this.cApplicationFormDetails.candidateName;
      this.employ = this.cApplicationFormDetails.vendorProofDetails;
//      this.finalReportDisabled = this.cApplicationFormDetails.finalReportStatus;
      if (this.cApplicationFormDetails.candidateResume) {
        this.candidateResume = 'data:application/pdf;base64,' + this.cApplicationFormDetails.candidateResume.document;
      }
    });
    this.candidateService.getAgentUploadedData(this.candidateCode).subscribe((data: any) => {
      console.log(JSON.stringify(data));
      this.agentUploadedData=data.data;
    });

    this.candidateService.getSourceBySource("UAN-CHECK").subscribe((data: any) => {
        this.sourceId=data.data.sourceId;
    });


    this.candidateService.getColors().subscribe((data: any) => {
      this.getColors = data.data;
      console.log(this.getColors);
    });

    this.uanForm = this.fb.group({
      uanNumber: ['', [Validators.required, Validators.pattern('^[0-9]+$')]], // Numeric input only
      remarks: ['', Validators.required], // Mandatory field
      colorCode: ['MINORDISCREPANCY', Validators.required] // Mandatory dropdown with default value
    });


  }

  ngOnInit(): void {
  }

  //Document View
  openResume(modalResume: any) {
    this.modalService.open(modalResume, {
      centered: true,
      backdrop: 'static',
      size: 'lg'
    });
    if (this.candidateResume) {
      $("#viewcandidateResume").attr("src", this.candidateResume);
    }
  }


  base64ToUint8Array(base64: any) {
    const binaryString = window.atob(base64);
    const len = binaryString.length;
    const bytes = new Uint8Array(len);

    for (let i = 0; i < len; i++) {
      bytes[i] = binaryString.charCodeAt(i);
    }

    return bytes;
  }
  fileview(base64Content: any) {
    const previewContainer = document.getElementById('preview-container');
    try {
      const uint8Array = this.base64ToUint8Array(base64Content);

      // Attempt to detect content type
      const type = this.detectContentType(uint8Array);
      // Create a Blob from Uint8Array
      const blob = new Blob([uint8Array], { type: type });

      const downloadLink = document.createElement('a');
      const objectURL = URL.createObjectURL(blob);

      downloadLink.href = objectURL;
      downloadLink.target = '_blank';
      // @ts-ignore
      previewContainer.innerHTML = '';
      // @ts-ignore
      previewContainer.appendChild(downloadLink);

      // Trigger a click event on the download link
      downloadLink.click();
      // Ensure to revoke the object URL after use to free up resources
      URL.revokeObjectURL(objectURL);
    } catch (error) {
      console.error('Error creating object URL:', error);
    }
  }
  downloadPdf(agentUploadedDocument: any) {
    // console.log(agentUploadedDocument, '******************************');
    if (agentUploadedDocument == null || agentUploadedDocument == '') {
      console.log('No Document Found');
    }

    this.customer
      .generatePrecisedUrl(agentUploadedDocument)
      .subscribe((data:any) => {
        window.open(data.data);
      });
  }
  detectContentType(uint8Array: Uint8Array): string {


    // Check for PDF magic number
    if (uint8Array[0] === 0x25 && uint8Array[1] === 0x50 && uint8Array[2] === 0x44 && uint8Array[3] === 0x46) {
      return 'application/pdf';
    }

    // Check for image magic numbers (jpeg, jpg, png)
    if (uint8Array[0] === 0xFF && uint8Array[1] === 0xD8) {
      return 'image/jpeg';
    } else if (
      uint8Array[0] === 0x89 &&
      uint8Array[1] === 0x50 &&
      uint8Array[2] === 0x4E &&
      uint8Array[3] === 0x47 &&
      uint8Array[4] === 0x0D &&
      uint8Array[5] === 0x0A &&
      uint8Array[6] === 0x1A &&
      uint8Array[7] === 0x0A
    ) {
      return 'image/png';
    }

    // Default to application/octet-stream if content type cannot be determined
    return 'application/octet-stream';
  }



  submitEditDOC() {
    this.patchAdddocValues();
    if (this.formEditDOC.valid) {
      console.log("..........................employeeeeee..........", this.formEditDOC.value)
      this.candidateService.updateCandidateVendorProofColor(this.formEditDOC.value).subscribe((result: any) => {
        if (result.outcome === true) {
          Swal.fire({
            title: result.message,
            icon: 'success'
          }).then((result) => {
            if (result.isConfirmed) {
              window.location.reload();
            }
          });
        } else {
          Swal.fire({
            title: result.message,
            icon: 'warning'
          })
        }
      });
    } else {
      Swal.fire({
        title: 'Please enter the required details.',
        icon: 'warning'
      })
    }
  }

  patchAdddocValues() {
    this.formEditDOC.patchValue({

      vendorChecks: this.vendorChecks
    });
  }

  openCertificate(modalCertificate: any, certificate: any) {
    this.modalService.open(modalCertificate, {
      centered: true,
      backdrop: 'static',
      size: 'lg'
    });
    // 'data:application/pdf;base64,' + certificate
    if (certificate) {
      $("#viewcandidateCertificate").attr("src", 'data:application/pdf;base64,' + certificate);
    }
  }

  openVendorModal(modalExperience: any, vendorChecks: any,
  ) {
    console.log(vendorChecks)
    this.modalService.open(modalExperience, {
      centered: true,
      backdrop: 'static'
    });
    this.vendorChecks = vendorChecks

  }

  async generateFinalReport() {
    this.loaderService.show();
    try {
      const resp: any = await this.candidateService
        .generateReportWithReportType(
          this.candidateCode,
          "FINAL",
          "UPDATE",
          localStorage.getItem("reportdeliverydate")
        )
        .toPromise();
  
      console.log("resp of report:", resp);
  
      if (resp) {
        Swal.fire({
          title: resp.message,
          icon: resp.outcome ? 'success' : 'error',
          confirmButtonText: 'OK'
        }).then((result) => {
          if (result.isConfirmed && resp.outcome) {
            window.open(resp.data, "_blank");
            this.redirectToDashboard(); // Redirect after confirmation
          }
        });
      }
    } catch (error:any) {
      console.error(error);
      Swal.fire({
        title: "An error occurred!",
        text: error.message || "Something went wrong while generating the report.",
        icon: 'error',
        confirmButtonText: 'OK'
      });
    } finally {
      this.loaderService.hide();
    }
  }
  
  // Redirect Function
  redirectToDashboard() {
    const navURL = 'admin/ConventionalDashboard/';
    this.navRouter.navigate([navURL]);
  }
  
  // Modified submit function
  submitReportApproval() {
    this.generateFinalReport();
  }
  
  submitAddcomment(formAddcomment: FormGroup) {
    console.log("================================ ***** formAddcomment", this.formAddcomment.value)
    if (this.formAddcomment.valid) {
      this.candidateService.AddCommentsReports(this.formAddcomment.value).subscribe((result: any) => {
        window.open(result.data, "_blank");
        if (result.outcome === true) {
          Swal.fire({
            title: result.data.data.message,
            icon: 'success'
          }).then((result) => {
            if (result.isConfirmed) {
              window.location.reload();
            }
          });
        } else {
          Swal.fire({
            title: result.message,
            icon: 'warning'
          })
        }
      });
    } else {
      Swal.fire({
        title: 'Please enter the required details.',
        icon: 'warning'
      })
    }

  }

  openAddcommentModal(content: any) {
    this.formAddcomment.reset();
    this.patchAddcomentValues();
    this.modalService.open(content, {ariaLabelledBy: 'modal-basic-title'}).result.then((res) => {
      this.closeModal = `Closed with: ${res}`;
    }, (res) => {
      this.closeModal = `Dismissed ${this.getDismissReason(res)}`;
    });
  }

  patchAddcomentValues() {
    this.formAddcomment.patchValue({
      candidateCode: this.candidateCode
    });
  }

  private getDismissReason(reason: any): string {
    if (reason === ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return `with: ${reason}`;
    }
  }

  saveAsExcelFile(buffer: any, fileName: string): void {

    const data: Blob = new Blob([buffer], {type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'});
    const a: HTMLAnchorElement = document.createElement('a');
    const url: string = window.URL.createObjectURL(data);
    a.href = url;
    a.download = `${fileName}.xlsx`;
    a.click();
    window.URL.revokeObjectURL(url);
    a.remove();
  }

  reportData: any;


  convertToExcel(jsonData: any): void {

    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(jsonData);
    const workbook: XLSX.WorkBook = {Sheets: {'data': worksheet}, SheetNames: ['data']};

    const excelBuffer: any = XLSX.write(workbook, {bookType: 'xlsx', type: 'array'});
    this.saveAsExcelFile(excelBuffer, 'data'); // Provide a filename for the Excel sheet
  }

  saveAsExcel(buffer: any, fileName: string):
    void {
    const data: Blob = new Blob([buffer], {type: 'application/octet-stream'});
    const url: string = window.URL.createObjectURL(data);
    const link: HTMLAnchorElement = document.createElement('a');
    link.href = url;
    link.download = `${fileName}.xlsx`; // Add the file extension
    link.click();
  }
  confirmDateChangeAndProceedviewInterimReport(action: () => void) {
    const selectedDate = localStorage.getItem("reportdeliverydate");
    if (selectedDate) {
      this.modalService.open(this.dateChangeConfirm, { centered: true }).result.then(
        (result) => {
          if (result === 'confirm') {
            action(); // Proceed with original action
          } else {
            this.clearSelectedDate(); // Clear selected date if user chooses to reset
          }
        },
        (reason) => { }
      );
    } else {
      action(); // Proceed if no date change
    }
  }
  
  viewInterimReport() {
    const selectedDate = localStorage.getItem("reportdeliverydate");
    if (selectedDate) {
        this.confirmDateChangeAndProceedviewInterimReport(() => {
            this.generateInterimReport();
        });
    } else {
        this.generateInterimReport();
    }
}

  
  // Separate method for generating the report
  generateInterimReport() {
    this.candidateService.generateReportWithReportType(
      this.candidateCode,
      "INTERIM",
      "DONT",
      localStorage.getItem("reportdeliverydate")
    ).subscribe((data: any) => window.open(data.data, "_blank"));
  }
  
  
  viewDiscrepancyReport(checkId:any){
  //   this.candidateService.generateReportWithReportType(this.candidateCode, "DISCREPANCY", "DONT").subscribe((data: any) =>
  //   window.open(data.data, "_blank")
  // );
    this.candidateService.discrepancyReport(checkId,this.candidateRequestId).subscribe((data:any) => {
      if(data.outcome == true){
        window.open(data.data, '_blank');

      }
    })
  }



  InterimReport() {
    // console.log(this.candidateCode, "-----------------------------------------------");
    // const navURL = 'admin/CV-Final-Approval/' + this.candidateCode;
    // this.navRouter.navigate([navURL]);
    this.candidateService.generateReportWithReportType(this.candidateCode, "INTERIM", "UPDATE",localStorage.getItem("reportdeliverydate")).subscribe(
      (result: any) => {
        if (result.outcome === true) {
          Swal.fire({
            title: result.message,
            icon: 'success'
          }).then((result) => {
            if (result.isConfirmed) {
           
            }
          });
        } else {
          Swal.fire({
            title: result.message,
            icon: 'warning'
          })
        }
      });
  }
  @ViewChild('dateChangeConfirm') dateChangeConfirm!: TemplateRef<any>;
  
  confirmDateChangeAndProceed(content: any, action: () => void) {
    if (this.selectedDate) {
      this.modalService.open(this.dateChangeConfirm, { centered: true }).result.then(
        (result) => {
          if (result === 'confirm') {
            action(); // Proceed with original action
          } else {
            this.clearSelectedDate(); // Clear selected date if user chooses to reset
          }
        },
        (reason) => { }
      );
    } else {
      action(); // Proceed if no date change
    }
  }
  
  clearSelectedDate() {
    console.log("cleared selected date");
    localStorage.removeItem("reportdeliverydate")
    this.selectedDate = '';
  }

  InterimConfirmation(content: any) {
    this.confirmDateChangeAndProceed(content, () => {
      this.modalService.open(content, { centered: true }).result.then(
        (result) => {
          if (result === 'confirm') {
            this.InterimReport();
          }
        },
        (reason) => { }
      );
    });
  }
  
  FinalConfirmation(content: any) {
    this.confirmDateChangeAndProceed(content, () => {
      this.modalService.open(content, { centered: true }).result.then(
        (result) => {
          if (result === 'confirm') {
            this.submitReportApproval();
          }
        },
        (reason) => { }
      );
    });
  }

 checkUniqueIdList: any=[];
 isAnyCheckboxSelected: boolean = false;
  triggerCheckStatusByAgent(checkUniqueId: string) {
   this.checkUniqueIdList=[];
   this.checkUniqueIdList.push(checkUniqueId);
    this.candidateService.triggerCheckStatusAtAgentByCheckUniqueId(this.checkUniqueIdList).subscribe(
      (result: any) => {
       if (result.outcome === true) {
                Swal.fire({
                  title: result.message,
                  icon: 'success',
                }).then((result) => {
                  if (result.isConfirmed) {
                    this.checkUniqueIdList=[];
                    window.location.reload();
                  }
                });
              }
              else {
                this.checkUniqueIdList=[];
                Swal.fire({
                  title: result.message,
                  icon: 'warning',
                });
              }
      }
    );
  }
//  childCheck(e:any){
//     this.checkUniqueIdList=[];
//     var sid = e.target.id;
//     if (e.target.checked) {
//       this.checkUniqueIdList.push(sid);
//     } else {
//       this.checkUniqueIdList.splice($.inArray(sid, this.checkUniqueIdList),1);
//     }
//     // Update flag based on whether checkUniqueIdList has any items
//     this.isAnyCheckboxSelected = this.checkUniqueIdList.length > 0;
//   }
//   childCheckselected(sid:any){
//     this.checkUniqueIdList.push(sid);
//   }
//  selectAll(e:any){
//     if (e.target.checked) {
//       $(".childCheck").prop('checked', true);
//       var  cboxRolesinput = $('.childCheck');
//       var arrNumber:any = [];
//       $.each(cboxRolesinput,function(idx,elem){
//         arrNumber.push($(this).val());
//       });
//       this.checkUniqueIdList = arrNumber;
//       this.isAnyCheckboxSelected = this.checkUniqueIdList.length > 0;
//       console.log(this.checkUniqueIdList);
//     } else {
//       $(".childCheck").prop('checked', false);
//     }
//   }

selectAll(event: any) {
  const checkboxes = document.getElementsByClassName('childCheck');
  this.checkUniqueIdList = [];

  for (let i = 0; i < checkboxes.length; i++) {
    let checkbox = checkboxes[i] as HTMLInputElement;
    checkbox.checked = event.target.checked;

    if (checkbox.checked) {
      this.checkUniqueIdList.push(checkbox.value);
    }
  }

  this.isAnyCheckboxSelected = this.checkUniqueIdList.length > 0;
}

childCheck(event: any) {
  const sid = event.target.value;

  if (event.target.checked) {
    this.checkUniqueIdList.push(sid);
  } else {
    const index = this.checkUniqueIdList.indexOf(sid);
    if (index > -1) {
      this.checkUniqueIdList.splice(index, 1);
    }
  }

  this.isAnyCheckboxSelected = this.checkUniqueIdList.length > 0;
}

 triggerCheckStatusArray() {

    const selectedCheckUniqueIdList = this.checkUniqueIdList.filter((id:any) => {
      const checkbox = document.getElementById(id) as HTMLInputElement;
      return checkbox.checked && !checkbox.disabled;
    });
    this.candidateService.triggerCheckStatusAtAgentByCheckUniqueId(selectedCheckUniqueIdList).subscribe(
      (result: any) => {
       if (result.outcome === true) {
                Swal.fire({
                  title: result.message,
                  icon: 'success',
                }).then((result) => {
                  if (result.isConfirmed) {
                    this.checkUniqueIdList=[];
                    window.location.reload();
                  }
                });
              }
              else {
                this.checkUniqueIdList=[];
                Swal.fire({
                  title: result.message,
                  icon: 'warning',
                }).then((result) => {
                  if (result.isConfirmed) {
                    this.checkUniqueIdList=[];
                    window.location.reload();
                  }
                });
              }
      }
    );
  }

 getIconClass(item:any): string {
     if (!item?.triggeredResponse) {
             return "";
     }
    if (item.triggeredResponse?.includes('200') || item.triggeredResponse?.includes('Successfully Updated')) {
      return 'fas fa-check-circle'; // Green check mark for success
    } else {
      return 'fas fa-times-circle'; // Red cross for error
    }
  }

  getIconColor(item:any): string {
     if (!item?.triggeredResponse) {
          return "";
        }
    if (item?.triggeredResponse.includes('200') || item.triggeredResponse?.includes('Successfully Updated')) {
      return 'green';
    } else {
      return 'red';
    }
  }
  allGreen(): boolean {
    const checkboxes = document.getElementsByClassName('childCheck');
    for (let i = 0; i < checkboxes.length; i++) {
      const checkbox = checkboxes[i] as HTMLInputElement;
      if (!checkbox.disabled && checkbox.checked) {
        const item = this.employ.find((emp: any) => emp.checkUniqueId === checkbox.value);
        if (!item || this.getIconColor(item) !== 'green') {
          return false;
        }
      }
    }
    return true;
  }

  uanForm: FormGroup;
  selectedFile: File | null = null;
  uploadedFiles: File[] = [];
  proofDocumentNew: File | null = null;
  fileError: boolean = false;

  openUploadUanModal(content: any) {
    this.modalService
      .open(content)
      .result.then(
      (res) => {
        console.log(content, '........................');
        this.closeModal = `Closed with: ${res}`;
      },
      (res) => {
        this.closeModal = `Dismissed ${this.getDismissReason(res)}`;
      }
    );
  }


  // Handle file selection and validation, ensuring only PDF files are allowed
  uploadGlobalCaseDetails(files: FileList | null) {
    if (!files || files.length === 0) {
      return;
    }

    Array.from(files).forEach(file => {
      if (file.type === 'application/pdf' && !this.uploadedFiles.some(f => f.name === file.name && f.size === file.size)) {
        this.uploadedFiles.push(file);
        this.proofDocumentNew = file;
        this.fileError = false;
      } else {
        this.fileError = true; // Only PDF files are allowed
      }
    });

    this.updatePreview(); // Refresh the preview after adding files
  }

  // Method to remove a file
  removeFile(index: number) {
    this.uploadedFiles.splice(index, 1); // Remove the file from the array
    this.updatePreview(); // Refresh the preview
  }

  // Method to update file preview in the DOM
  updatePreview() {
    const previewContainer = document.getElementById('preview-container');
    if (!previewContainer) return;

    previewContainer.innerHTML = ''; // Clear previous previews

    this.uploadedFiles.forEach((file, index) => {
      const fileType = file.type.toLowerCase();
      const fileContainer = document.createElement('div');
      fileContainer.classList.add('d-flex', 'align-items-center', 'mb-2');

      if (fileType === 'application/pdf' || fileType.startsWith('image/')) {
        const downloadLink = document.createElement('a');
        downloadLink.href = URL.createObjectURL(file);
        downloadLink.target = '_blank';
        downloadLink.textContent = `Preview File ${index + 1}: ${file.name}`;
        downloadLink.classList.add('custom-preview-btn', 'm-2');
        fileContainer.appendChild(downloadLink);
      } else {
        const warningText = document.createElement('p');
        warningText.textContent = `Preview not available for ${file.name}.`;
        fileContainer.appendChild(warningText);
      }

      const removeButton = document.createElement('button');
      removeButton.innerHTML = '&times;'; // Cross mark
      removeButton.classList.add('btn', 'btn-danger', 'btn-sm', 'ml-2');
      removeButton.onclick = () => this.removeFile(index);
      fileContainer.appendChild(removeButton);
      previewContainer.appendChild(fileContainer);
    });
  }


  // Handle form submission
  onSubmit() {
    if (this.uanForm.valid && this.uploadedFiles.length > 0) {
      const formData = new FormData();
      this.uploadedFiles.forEach((file) => {
        formData.append('file', file); // Append each file, using the same key 'files'
      });
      const dtoData =
      {
        uanNo: this.uanForm.get('uanNumber')?.value,
        remarks: this.uanForm.get('remarks')?.value,
        colorCode: this.uanForm.get('colorCode')?.value,
        sourceId:this.sourceId,
        requestId: this.candidateCode
      };

       formData.append(
        'uanDto', JSON.stringify(dtoData)
      );

      this.candidateService.uploadUan(formData).subscribe((result: any) => {
        if (result.outcome === true) {
          Swal.fire({
            title: result.message,
            icon: 'success'
          });
          window.location.reload();
        } else {
          Swal.fire({
            title: result.message,
            icon: 'warning'
          })
        }
      });

    } else {
      console.log('Please fill out the form correctly and upload at least one PDF file.');
    }
  }

  openDateModal(content: any) {
    this.modalService.open(content, { centered: true });
  }

  confirmDateChange(modal: any) {
    if (!this.selectedDate) {
      alert("Please select a date!");
      return;
    }
    else{
      // Convert selectedDate (YYYY-MM-DD) to a Date object
      const selectedDateTime = new Date(this.selectedDate + 'T00:00:00'); // Start of the day
      // Convert to IST manually
      const istOffset = 5.5 * 60 * 60 * 1000; // IST offset in milliseconds
      const istDateTime = new Date(selectedDateTime.getTime() + istOffset);
      // Convert to ISO 8601 format with timezone information
      const formattedDate = istDateTime.toISOString();
      this.selectedDate=formattedDate;
      localStorage.setItem("reportdeliverydate",this.selectedDate);
      modal.close();
    }
  }

}
