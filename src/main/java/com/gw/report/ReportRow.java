package com.gw.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReportRow {

    long landingPageLoadTime;
    long loginLoadTime;
    long listLoadTime;
    long numberOfResultsReturned;
}
