package com.ibm.BLUdemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.LOF;

import com.google.gson.Gson;

@WebServlet(name="GetOutliersServlet",urlPatterns={"/getOutliers"})
public class GetOutliersServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public GetOutliersServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException 
	{
		Instances train = (Instances) request.getSession().getAttribute("inputData");
		String jsonResponse;
		try {
			jsonResponse = generateOutlierReport(train,request);
			System.out.println(jsonResponse);
			response.setContentType("application/json");
		    response.setCharacterEncoding("UTF-8");
		    response.getWriter().write(jsonResponse);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {}
	
	private String generateOutlierReport(Instances train,HttpServletRequest request) throws Exception
	{
		List<String> instanceData=null;
		List<List<String>> outputList=new ArrayList<List<String>>();
		List<Map<String,String>> columns=new ArrayList<Map<String,String>>(train.numAttributes());
		Map<String,String> columnMap=null;
		String attName=null;
		
		Result result= new Result();
		
		Enumeration<Attribute> atts=train.enumerateAttributes();
		while(atts.hasMoreElements())
		{
			attName =  atts.nextElement().name();
			columnMap=new HashMap<String, String>();
			columnMap.put("title",attName);
			columnMap.put("name", attName);
			columns.add(columnMap);
		}
		result.setColumns(columns);
		
		LOF lof=new LOF();
		lof.setInputFormat(train);
		Instances filtered = LOF.useFilter(train, lof);
		
		double[] lofValues = new double[filtered.numInstances()];
		for (int i = 0; i < filtered.numInstances(); i++) 
		{
			lofValues[i]=filtered.get(i).value(filtered.numAttributes()-1);
		}	
		lofValues = findTopNValues(lofValues,Integer.parseInt(PropertiesUtility.getProperty("OUTLIER_COUNT")));
		
		for (int i = 0; i < lofValues.length; i++) 
		{
			double lofValue=lofValues[i];
			for (int j = 0; j < filtered.numInstances(); j++) 
			{
				if(lofValue==filtered.get(j).value(filtered.numAttributes()-1))
				{
					instanceData = new ArrayList<String>();
					for(int attrIndex=0;attrIndex<columns.size();attrIndex++)
					{
						instanceData.add(String.valueOf(filtered.get(j).value(attrIndex)));
					}
					outputList.add(instanceData);
				}
			}
		}
		result.setData(outputList);
		return new Gson().toJson(result);
	}
	
	/**
	 * Finds list of the highest 'n' values in the source list, ordered naturally, 
	 * with the highest value at the start of the array and returns it 
	 */
	private double[] findTopNValues(double[] values,int n) {

	    int length = values.length;
	    for (int i=1; i<length; i++) {
	        int curPos = i;
	        while ((curPos > 0) && (values[i] > values[curPos-1])) {
	            curPos--;
	        }

	        if (curPos != i) {
	            double element = values[i];
	            System.arraycopy(values, curPos, values, curPos+1, (i-curPos));
	            values[curPos] = element;
	        }
	    }       
	    return Arrays.copyOf(values, n);        
	}   

}
