package com.gw.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReportRow {

    long loginPageLoadTime;
    long listLoadTime;
    long numberOfResultsReturned;
}
