package com.test;

import java.util.Date;

import weka.classifiers.trees.J48;

public class SampleUtility {

public String getDate()
{
	return new Date().toString();
}

public String invokeWekaAPI()
{
	J48 j48 = new J48();
	return j48.getTechnicalInformation().toBibTex();
}
	
}
