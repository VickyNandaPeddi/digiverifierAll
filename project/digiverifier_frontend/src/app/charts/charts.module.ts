import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ChartsRoutingModule } from './charts-routing.module';
import { ChartsComponent } from './charts.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AgentDetailsComponent } from './superadmin/agent-details/agent-details.component';
import { SelectedActivitiesComponent } from './superadmin/selected-activities/selected-activities.component';
import { CustomerActivitiesComponent } from './superadmin/customer-activities/customer-activities.component';
import { RatePerItemComponent } from './superadmin/rate-per-item/rate-per-item.component';
import { RateperreportComponent } from './superadmin/rateperreport/rateperreport.component';
import { ActivityComparisionComponent } from './superadmin/activity-comparision/activity-comparision.component';


@NgModule({
  declarations: [
    ChartsComponent,
    AgentDetailsComponent,
    SelectedActivitiesComponent,
    CustomerActivitiesComponent,
    RatePerItemComponent,
    RateperreportComponent,
    ActivityComparisionComponent
  ],
  imports: [
    CommonModule,
    ChartsRoutingModule,
    FormsModule,
    ReactiveFormsModule
  ],
  exports: [ 
    AgentDetailsComponent, SelectedActivitiesComponent, CustomerActivitiesComponent, RatePerItemComponent,
    RateperreportComponent, ActivityComparisionComponent]
})
export class ChartsModule { }
