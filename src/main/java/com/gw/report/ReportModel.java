package com.gw.report;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Setter
@Getter
public class ReportModel {

    public List<ReportRow> rows = Collections.synchronizedList(new ArrayList<>());

    public synchronized void addRow(ReportRow row) {
        rows.add(row);
    }

    public ReportModel getMockObject(){
        ReportModel model = new ReportModel();
        model.getRows().add(new ReportRow(1l,1l,2l,3l));
        model.getRows().add(new ReportRow(1l,1l,2l,3l));
        return model;
    }
}
