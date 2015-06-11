package com.ibm.BLUdemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instances;
import weka.experiment.InstanceQuery;

import com.google.gson.Gson;
@WebServlet(name="GetPredictionsServlet",urlPatterns={"/getPredictions"})
public class GetPredictionsServlet extends HttpServlet {

	public GetPredictionsServlet() 
	{
		super();
	}
	
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    		throws ServletException, IOException 
    {
		try 
		{
			InstanceQuery query = new InstanceQuery();
			query.setCustomPropsFile(new File(request.getServletContext().getRealPath("/")+PropertiesUtility.getProperty("DB_PROPERTIES_PATH")));
			query.setUsername(PropertiesUtility.getProperty("DB_USER_NAME"));
			query.setPassword(PropertiesUtility.getProperty("DB_PASSWORD"));
			String trainingQuery = PropertiesUtility.getProperty("FETCH_QUERY");
			query.setQuery(trainingQuery);
			Instances train = query.retrieveInstances();
			int trainingTableClassifierIndex = (Integer) request.getSession().getAttribute("trainingTableClassifierIndex");
			if (trainingTableClassifierIndex != -1) 
			{
				train.setClassIndex(trainingTableClassifierIndex);
			} 
			else 
			{
				train.setClassIndex(train.numAttributes() - 1);
			}
			J48 j48 = new J48();
			j48.buildClassifier(train);
			
			// Reading the Testing Set
			BufferedReader breader = new BufferedReader(new FileReader(request.getSession().getAttribute("arff_file_name").toString()));
			Instances test = new Instances(breader);
		    // Use the same classifier index as the training table
			test.setClassIndex(trainingTableClassifierIndex); 
			breader.close();
			System.out.println("Number of Test instances::" + test.numInstances());
			
			
			Map<String,String> instanceData=null;
			PredictionResult result= new PredictionResult();
			List<Map<String,String>> outputList=new ArrayList<Map<String,String>>();
			
			Enumeration<Attribute> atts=test.enumerateAttributes();
			String[] columns = new String[test.numAttributes()+1];
			int attrIndex=0;
			while(atts.hasMoreElements())
			{
				columns[attrIndex]=atts.nextElement().name();
				attrIndex++;
			}
			columns[attrIndex]=PropertiesUtility.getProperty("PREDICTION_COLUMN_NAME");
			result.setColumns(columns);
			
			
			for (int i = 0; i < test.numInstances(); i++) 
			{
				instanceData = new HashMap<String, String>();
				for(attrIndex=0;attrIndex<test.numAttributes();attrIndex++)
				{
					instanceData.put(test.get(i).attribute(attrIndex).name(), String.valueOf(test.get(i).value(attrIndex)));
				}
				double pred = j48.classifyInstance(test.instance(i));
				if (pred >= Double.parseDouble(PropertiesUtility.getProperty("CLASSIFICATION_THRESHOLD"))) 
				{
					instanceData.put(PropertiesUtility.getProperty("PREDICTION_COLUMN_NAME"),"Yes");
				} 
				else 
				{
					instanceData.put(PropertiesUtility.getProperty("PREDICTION_COLUMN_NAME"),"No");
				}
				outputList.add(instanceData);
			}	
			result.setData(outputList);
			
			response.setContentType("application/json");
		    response.setCharacterEncoding("UTF-8");
		    response.getWriter().write(new Gson().toJson(result));
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
    
    }
	
	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    		throws ServletException, IOException 
    {
    	
    }

}
