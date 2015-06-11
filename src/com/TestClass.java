package com;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ibm.BLUdemo.CSV2ARFF;
import com.ibm.BLUdemo.PropertiesUtility;

import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.LOF;
import weka.filters.unsupervised.instance.RemoveWithValues;

public class TestClass 
{
	
	public static void main(String[] args) throws Exception {
		
		URI uri = new URI("https://infosysuk.service-now.com/incident.do?JSON&sysparm_action=getRecords&sysparm_query=active%5Esys_updated_by=bhavin_raichura");
		System.out.println(uri.toString());
		/*
		
		CSV2ARFF c2a = new CSV2ARFF();
		String csvFilePath = "cab_data.csv";
		String arff_file_name = "cab_data.arff";
		c2a.convert(csvFilePath,arff_file_name );
		
		BufferedReader breader = new BufferedReader(new FileReader("cab_data.arff"));
		Instances test = new Instances(breader);
		Instance instance;
		
		LOF lof=new LOF();
		lof.setInputFormat(test);
		Instances filtered = LOF.useFilter(test, lof);
		for(int i=0;i<filtered.size();i++)
		{
			instance = filtered.get(i);
			System.out.println(instance.toStringNoWeight());
		}
		LOF lof=new LOF();
		lof.setInputFormat(test);
		Instances filtered = LOF.useFilter(test, lof);
		double[] lofValues = new double[filtered.numInstances()];
		List<double[]> outputList=new ArrayList<double[]>();
		
		for (int i = 0; i < filtered.numInstances(); i++) 
		{
			lofValues[i]=filtered.get(i).value(filtered.numAttributes()-1);
		}	
		lofValues = findTopNValues(lofValues,5);
		for (int i = 0; i < lofValues.length; i++) 
		{
			double lofValue=lofValues[i];
			for (int j = 0; j < filtered.numInstances(); j++) 
			{
				if(lofValue==filtered.get(j).value(filtered.numAttributes()-1))
				{
					outputList.add(filtered.get(j).toDoubleArray());
				}
			}
		}
		
		for(double[] entry : outputList)
		{
			for (int j = 0; j < entry.length; j++) 
			{
				System.out.print(entry[j]);
				System.out.print("    ");
			}
			System.out.println();
		}
		
	*/}
	
	/**
	 * Finds list of the highest 'n' values in the source list, ordered naturally, 
	 * with the highest value at the start of the array and returns it 
	 */
	public static double[] findTopNValues(double[] values,int n) {

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
