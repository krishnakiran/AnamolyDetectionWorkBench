package com.ibm.BLUdemo;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;

public class CSV2ARFF {

	/**
	 * Utility to convert CSV to ARFF
	 * takes 2 arguments: - CSV input file - ARFF output file
	 */
	public void convert(String csv, String arff) throws Exception {
		
		// load CSV
		CSVLoader loader = new CSVLoader();
		System.out.println("csv file path:"+csv);
		loader.setSource(new File(csv));
		Instances data = loader.getDataSet();

		// save ARFF
		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);
		saver.setFile(new File(arff));
		saver.writeBatch();
	}

}
