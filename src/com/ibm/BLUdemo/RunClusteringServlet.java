package com.ibm.BLUdemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MultiHashMap;

import weka.clusterers.SimpleKMeans;
import weka.core.Instances;

import com.google.gson.Gson;

@WebServlet(name="RunClusteringServlet",urlPatterns={"/clusterassignmentjson"})
public class RunClusteringServlet extends HttpServlet
{

	private static final long serialVersionUID = 1L;


	public RunClusteringServlet() {
		super();
	}


	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
    {

		Instances train;
		List<JSONOutput> clusterAssignments = new ArrayList<JSONOutput>();
		ClusterData clusterData=null;
		MultiHashMap multiHashMap = new MultiHashMap();
		try {

			train = (Instances) request.getSession().getAttribute("inputData");

			SimpleKMeans kMeans = new SimpleKMeans();
			kMeans.setPreserveInstancesOrder(true);
			kMeans.setNumClusters(Integer.parseInt(PropertiesUtility.getProperty("NUM_CLUSTERS")));
			kMeans.buildClusterer(train);
			int[] assignments = kMeans.getAssignments();

			for(int i=0;i<kMeans.getNumClusters();i++)
			{
				int j=0;
				for(int clusterNum : assignments)
				{
					if(i==clusterNum)
					{
						clusterData = new ClusterData();
						clusterData.setX(train.get(j).value(Integer.parseInt(PropertiesUtility.getProperty("SCATTER_PLOT_X_INDEX"))));
						clusterData.setY(train.get(j).value(Integer.parseInt(PropertiesUtility.getProperty("SCATTER_PLOT_Y_INDEX"))));
						clusterData.setSize(Math.random());
						clusterData.setShape(PropertiesUtility.getProperty("SCATTER_PLOT_SHAPE"));
						multiHashMap.put("Cluster "+clusterNum, clusterData);
					}
					j++;
				}

			}

			Iterator iter = multiHashMap.entrySet().iterator();
			JSONOutput jsonOutput = null;
			Entry entry=null;
			while(iter.hasNext())
			{
				entry = (Entry) iter.next();
				jsonOutput = new JSONOutput();
				jsonOutput.setKey(entry.getKey().toString());
				jsonOutput.setValues(multiHashMap.getCollection(entry.getKey()).toArray());
				clusterAssignments.add(jsonOutput);
			}
			 String jsonResponse = new Gson().toJson(clusterAssignments);
			 System.out.println(jsonResponse);
			 response.setContentType("application/json");
		     response.setCharacterEncoding("UTF-8");
		     response.getWriter().write(jsonResponse);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {}

}
