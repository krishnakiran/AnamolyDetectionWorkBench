package com.ibm.BLUdemo;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.experiment.InstanceQuery;

@WebServlet(name="RunClassificationServlet",urlPatterns={"/RunClassificationServlet"})
public class RunClassificationServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public RunClassificationServlet() 
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
			Evaluation eval = new Evaluation(train);
			eval.crossValidateModel(j48, train, Integer.parseInt(PropertiesUtility.getProperty("NUM_CROSS_VALIDATION_FOLDS")), new Random(1));
			System.out.println(eval
					.toSummaryString("Results\n========\n", true));
			System.out.println(eval.fMeasure(1) + " " + eval.precision(1) + " "
					+ eval.recall(1));	
			response.setContentType("text/plain");
		    response.setCharacterEncoding("UTF-8");
		    response.getWriter().write(j48.toString()+"\n"+eval.toSummaryString("Results\n========\n", true));
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
