package com.test;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.ibm.BLUdemo.AnamolyData;
import com.ibm.BLUdemo.PredictionResult;
import com.ibm.BLUdemo.PropertiesUtility;

@Path("/TicketService")
@Produces("text/plain")
public class TicketServiceImpl{

	@Path("/ping")
	@GET
	@Produces("text/plain")
	public String sayHi(@QueryParam("text")String text) 
	{
		return "Hi "+text;
	}
	
	
	@Path("/createTicket")
	@POST
	@Produces("application/json")
	@Consumes("text/plain")
	public String createTicket(String jsonString) throws Exception
	{
		AnamolyData anamolyData=new Gson().fromJson(jsonString, AnamolyData.class);
		String[] anomalyRecord=null;
		RecordData recordData=null;
		String[] columns ={"SR#","ServiceNow_Link","Assigned_To","Description"};
		 Map<String,String> instanceData = null;
	     List<Map<String,String>> resultList=new ArrayList<Map<String,String>>();
		for(int anamolyIndex=0;anamolyIndex<anamolyData.getAnomalydata().length;anamolyIndex++)
		{
			anomalyRecord = anamolyData.getAnomalydata()[anamolyIndex];
			recordData=createServiceCloudIncident(anomalyRecord);
			
			if(recordData.getRecords()!=null & recordData.getRecords().length>0)
			{
				 instanceData = new java.util.LinkedHashMap<String, String>();
		         instanceData.put("SR#",recordData.getRecords()[0].getNumber());
		         instanceData.put("ServiceNow_Link",PropertiesUtility.getProperty("SERVICE_NOW_URL")+recordData.getRecords()[0].getSys_id());
		         instanceData.put("Assigned_To",recordData.getRecords()[0].getSys_updated_by());
		         instanceData.put("Description",recordData.getRecords()[0].getShort_description());
		         resultList.add(instanceData);
			}
		}
		PredictionResult result= new PredictionResult();
	    result.setColumns(columns);
	    result.setData(resultList);
		return new Gson().toJson(result);
	}
	
	private RecordData createServiceCloudIncident(String[] anomalyRecord) throws Exception
	{
		// Set up an HTTP client that makes a connection to REST API.
	    DefaultHttpClient client = new DefaultHttpClient();
	    
	    URI uri = new URI(PropertiesUtility.getProperty("SERVICE_NOW_INSERT_URL"));
	    
	    client.getCredentialsProvider().setCredentials(
				new AuthScope(uri.getHost(), uri.getPort(),AuthScope.ANY_SCHEME),
				new UsernamePasswordCredentials(PropertiesUtility.getProperty("SERVICE_NOW_USER"), PropertiesUtility.getProperty("SERVICE_NOW_PASSWORD")));

	    HttpParams params = client.getParams();
	    params.setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 30000);
	    HttpHost target = new HttpHost("localhost", 80, "http");
	    HttpPost insertRequest = new HttpPost(PropertiesUtility.getProperty("SERVICE_NOW_INSERT_URL"));
	    JSONObject jsonObject = new JSONObject();
	    jsonObject.put("short_description",Arrays.toString(anomalyRecord));
	    jsonObject.put("priority",1);
	    jsonObject.put("u_assignedtoname","krishna_kiran");
	    insertRequest.setEntity(new StringEntity(jsonObject.toString()));
	 // Execute the request.
	    System.out.println("POST " + PropertiesUtility.getProperty("SERVICE_NOW_INSERT_URL") + "...\n");
	    HttpResponse response = client.execute(insertRequest);
	    String responseText=EntityUtils.toString(response.getEntity());
	    System.out.println(responseText);
	    return new Gson().fromJson(responseText,RecordData.class);
	}
	
	@Path("/getMyTickets")
	@GET
	@Produces("application/json")
	public String getMyTickets() throws Exception
	{
		String[] columns ={"SR#","ServiceNow_Link","Assigned_To","Description"};
		Map<String,String> instanceData = null;
	    List<Map<String,String>> resultList=new ArrayList<Map<String,String>>();
	     
	     DefaultHttpClient client = new DefaultHttpClient();
		    
	    URI uri = new URI(PropertiesUtility.getProperty("SERVICE_NOW_MY_TICKETS_URL"));
	    
	    client.getCredentialsProvider().setCredentials(
				new AuthScope(uri.getHost(), uri.getPort(),AuthScope.ANY_SCHEME),
				new UsernamePasswordCredentials(PropertiesUtility.getProperty("SERVICE_NOW_USER"), PropertiesUtility.getProperty("SERVICE_NOW_PASSWORD")));

	    HttpParams params = client.getParams();
	    params.setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 30000);
	    HttpGet getRequest = new HttpGet(uri);
	    HttpResponse response = client.execute(getRequest);
	    String responseText=EntityUtils.toString(response.getEntity());
	    System.out.println(responseText);
	    RecordData recordData= new Gson().fromJson(responseText,RecordData.class);
			
		if(recordData.getRecords()!=null & recordData.getRecords().length>0)
		{
			 for(int i=0;i<recordData.getRecords().length;i++)
			 {
				 instanceData = new java.util.LinkedHashMap<String, String>();
		         instanceData.put("SR#",recordData.getRecords()[i].getNumber());
		         instanceData.put("ServiceNow_Link",PropertiesUtility.getProperty("SERVICE_NOW_URL")+recordData.getRecords()[i].getSys_id());
		         instanceData.put("Assigned_To",recordData.getRecords()[i].getSys_updated_by());
		         instanceData.put("Description",recordData.getRecords()[i].getShort_description());
		         resultList.add(instanceData);
			 }
		}
		PredictionResult result= new PredictionResult();
	    result.setColumns(columns);
	    result.setData(resultList);
		return new Gson().toJson(result);
	}
	

}
