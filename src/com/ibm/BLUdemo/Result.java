package com.ibm.BLUdemo;

import java.util.List;
import java.util.Map;

public class Result 
{
	private List<List<String>> data;
	private List<Map<String,String>> columns;
	
	public List<Map<String, String>> getColumns() {
		return columns;
	}
	public void setColumns(List<Map<String, String>> columns) {
		this.columns = columns;
	}
	public List<List<String>> getData() {
		return data;
	}
	public void setData(List<List<String>> data) {
		this.data = data;
	}
	
}
