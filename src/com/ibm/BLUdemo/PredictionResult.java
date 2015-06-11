package com.ibm.BLUdemo;

import java.util.List;
import java.util.Map;

public class PredictionResult {

	private String[] columns;
	private List<Map<String, String>> data;
	public String[] getColumns() {
		return columns;
	}
	public void setColumns(String[] columns) {
		this.columns = columns;
	}
	public List<Map<String, String>> getData() {
		return data;
	}
	public void setData(List<Map<String, String>> data) {
		this.data = data;
	}



}
