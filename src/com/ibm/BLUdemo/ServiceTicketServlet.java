package com.ibm.BLUdemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import weka.core.Attribute;
import weka.core.Instances;

import com.google.gson.Gson;
import com.sforce.soap.enterprise.Connector;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.Error;
import com.sforce.soap.enterprise.QueryResult;
import com.sforce.soap.enterprise.SaveResult;
import com.sforce.soap.enterprise.sobject.Case;
import com.sforce.soap.enterprise.sobject.Contact;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * Servlet implementation class ServiceTicketServlet
 */
@WebServlet(name="ServiceTicketServlet",urlPatterns={"/ServiceTicketServlet"})
public class ServiceTicketServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
	public EnterpriseConnection getConnection() throws Exception {
		ConnectorConfig config = new ConnectorConfig();
		EnterpriseConnection connection=null;
	    config.setUsername(PropertiesUtility.getProperty("SERVICE_CLOUD_USER"));
	    config.setPassword(PropertiesUtility.getProperty("SERVICE_CLOUD_PASSWORD"));
	    //config.setTraceMessage(true);
	    try {
	      
	      connection = Connector.newConnection(config);
	      // display some current settings
	      System.out.println("Auth EndPoint: "+config.getAuthEndpoint());
	      System.out.println("Service EndPoint: "+config.getServiceEndpoint());
	      System.out.println("Username: "+config.getUsername());
	      System.out.println("SessionId: "+config.getSessionId());
	    } catch (ConnectionException e1) {
	        e1.printStackTrace();
	    }  
	    return connection;
	}
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ServiceTicketServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String payLoad=getBody(request);
		AnamolyData anamolyData=new Gson().fromJson(payLoad, AnamolyData.class);
		Map<String,String> anamolyRecordMap=null;
		List<Map<String,String>> serviceTicketDataList=new ArrayList<Map<String,String>>();
		
		Instances train = (Instances) request.getSession().getAttribute("inputData");
		Enumeration<Attribute> atts=train.enumerateAttributes();
		String[] attributeNames=new String[train.numAttributes()];
		int attributeIndex=0;
		while(atts.hasMoreElements())
		{
			attributeNames[attributeIndex]=atts.nextElement().name();
			attributeIndex++;
		}
		
		String[] anomalyRecord=null;
		for(int anamolyIndex=0;anamolyIndex<anamolyData.getAnomalydata().length;anamolyIndex++)
		{
			anomalyRecord = anamolyData.getAnomalydata()[anamolyIndex];
			anamolyRecordMap = new HashMap<String, String>();
			for(int i=0;i<anomalyRecord.length;i++)
			{
				anamolyRecordMap.put(attributeNames[i], anomalyRecord[i]);
			}
			serviceTicketDataList.add(anamolyRecordMap);
		}
		response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    response.addHeader( "Access-Control-Allow-Origin", "*" ); // open your api to any client 
	    response.addHeader( "Access-Control-Allow-Methods", "POST" ); // a allow post
	    response.addHeader( "Access-Control-Max-Age", "1000" ); //
	    response.getWriter().write(createCases(serviceTicketDataList));
	}
	
	private String getBody(HttpServletRequest request) throws IOException {

	    String body = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    BufferedReader bufferedReader = null;

	    try {
	        InputStream inputStream = request.getInputStream();
	        if (inputStream != null) {
	            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	            char[] charBuffer = new char[128];
	            int bytesRead = -1;
	            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
	                stringBuilder.append(charBuffer, 0, bytesRead);
	            }
	        } else {
	            stringBuilder.append("");
	        }
	    } catch (IOException ex) {
	        throw ex;
	    } finally {
	        if (bufferedReader != null) {
	            try {
	                bufferedReader.close();
	            } catch (IOException ex) {
	                throw ex;
	            }
	        }
	    }

	    body = stringBuilder.toString();
	    return body;
	}
	
	 private String createCases(List<Map<String,String>> serviceTicketDataList) {
	   
	   Case[] records = new Case[serviceTicketDataList.size()];
	   Map<String,String> anomalyRecord=null;
	   StringBuffer ticketIds=new StringBuffer();
	   Case ticket=null;
	   PredictionResult result=null;
	   Contact contact=null;
	   try {
		   
		  int i=0;
		  for(Iterator<Map<String,String>> ticketIter=serviceTicketDataList.iterator();ticketIter.hasNext();)
		  {
			  anomalyRecord = ticketIter.next();
			  ticket = new Case();
			  ticket.setStatus("New");
			  ticket.setOrigin("Web");
			  ticket.setSubject("Anamoly Record from AnamolyDetection Work Bench - "+new Date().toString());
			  ticket.setReason("Performance");
			  ticket.setDescription(new Gson().toJson(anomalyRecord));
			  contact = queryContacts();
			  if(contact!=null)
			  {
				  ticket.setContactId(contact.getId());
			  }
			  records[i]=ticket;
			  i++;
		  }
	     // create the records in Salesforce.com
	     SaveResult[] saveResults = getConnection().create(records);

	     // check the returned results for any errors
	     ticketIds.append("The Following SRs are Created in Service cloud for the Anomaly Records\n");
	     
	     String[] columns ={"SR#","ServiceCloud_Link","Assigned_To","Description"};
	     Map<String,String> instanceData = null;
	     List<Map<String,String>> outputList=new ArrayList<Map<String,String>>();
	     
	     for (i=0; i< saveResults.length; i++) {
	       if (saveResults[i].isSuccess()) 
	       {
	         System.out.println(i+". Successfully created record - Id: " + saveResults[i].getId());
	         //ticketIds.append("SR LINK:: "+PropertiesUtility.getProperty("SERVICE_CLOUD_URL")+saveResults[i].getId()+"\n");
	         instanceData = new HashMap<String, String>();
	         instanceData.put("SR#",saveResults[i].getId());
	         instanceData.put("ServiceCloud_Link",PropertiesUtility.getProperty("SERVICE_CLOUD_URL")+saveResults[i].getId());
	         instanceData.put("Assigned_To",contact.getFirstName()+","+contact.getLastName());
	         instanceData.put("Description",records[i].getDescription());
	         outputList.add(instanceData);
	       } 
	       else 
	       {
	         Error[] errors = saveResults[i].getErrors();
	         for (int j=0; j< errors.length; j++) {
	           System.out.println("ERROR creating record: " + errors[j].getMessage());
	         }
	       }    
	     }
	     result= new PredictionResult();
	     result.setColumns(columns);
	     result.setData(outputList);
	     
	   } catch (Exception e) {
	     e.printStackTrace();
	   }    
	   return new Gson().toJson(result);
	 }
	 
	  private Contact queryContacts() {
		    
		    try {
		      // query for the 5 newest contacts      
		      QueryResult queryResults = getConnection().query("SELECT Id, FirstName, LastName, Account.Name " +
		      		"FROM Contact WHERE FirstName='Bhavin'");
		      if (queryResults.getSize() > 0) {
		        for (int i=0;i<queryResults.getRecords().length;i++) {
		          // cast the SObject to a strongly-typed Contact
		          Contact c = (Contact)queryResults.getRecords()[i];
		          System.out.println("Id: " + c.getId() + " - Name: "+c.getFirstName()+" "+
		              c.getLastName());
		          return c;
		        }
		      }
		      
		    } catch (Exception e) {
		      e.printStackTrace();
		    }    
		    return null;
		  }
	  	  

}
