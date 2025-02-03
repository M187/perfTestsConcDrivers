package com.gw.report;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Setter
@Getter
public class ReportModel {

    public String header;
    public boolean isUser;

    public List<ReportRow> rows = Collections.synchronizedList(new ArrayList<ReportRow>());

    public synchronized void addRow(ReportRow row) {
        rows.add(row);
    }

    public ReportModel(String header, boolean isUser) {
        this.header = header;
        this.isUser = isUser;
    }

    public static ReportModel getMockObject(){
        ReportModel model = new ReportModel("mock", false);
        model.getRows().add(new ReportRow("",1l,2l,"3"));
        model.getRows().add(new ReportRow("",1l,2l,"3"));
        return model;
    }
}
