package com.ibm.BLUdemo;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import weka.core.Instances;
import weka.experiment.InstanceQuery;

@WebServlet(name="FetchInstancesServlet",urlPatterns={"/FetchInstancesServlet"})
public class FetchInstancesServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private ServletFileUpload uploader = null;
	
	public FetchInstancesServlet() {
		super();
	}
	
	@Override
	public void init() throws ServletException {
		DiskFileItemFactory fileFactory = new DiskFileItemFactory();
		File filesDir = (File) getServletContext().getAttribute(
				"FILES_DIR_FILE");
		fileFactory.setRepository(filesDir);
		this.uploader = new ServletFileUpload(fileFactory);
	}
	
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
			String trainingDataTable = null,trainingTableClassifierIndex=null;
     		Instances train;
		try 
		{
			List<FileItem> fileItemsList = uploader.parseRequest(request);
			Iterator<FileItem> fileItemsIterator = fileItemsList.iterator();
			while (fileItemsIterator.hasNext()) 
			{
				FileItem fileItem = fileItemsIterator.next();
				if (fileItem.isFormField()) 
				{  
					String name = fileItem.getFieldName();   
					String value = fileItem.getString();  
					if(name.equalsIgnoreCase("tableName"))
					{
						trainingDataTable=value;
					}
					else if(name.equalsIgnoreCase("trainingTableClassifierIndex"))
					{
						trainingTableClassifierIndex=value;
						if (trainingTableClassifierIndex.equalsIgnoreCase("other")) {
							trainingTableClassifierIndex = value;
						}
						
					}
			    }
				else
				{
					System.out.println("FieldName=" + fileItem.getFieldName());
					System.out.println("FileName=" + fileItem.getName());
					System.out.println("ContentType=" + fileItem.getContentType());
					System.out.println("Size in bytes=" + fileItem.getSize());
					File file = new File(request.getServletContext().getAttribute(
							"FILES_DIR")
							+ File.separator + fileItem.getName());
					System.out.println("Absolute Path at server="
							+ file.getAbsolutePath());
					fileItem.write(file);
					// Converting the CSV file to ARFF file for Weka support
					CSV2ARFF c2a = new CSV2ARFF();
					String csvFilePath = file.getAbsolutePath();
					String arff_file_name = request.getServletContext().getAttribute(
							"FILES_DIR") + File.separator + fileItem.getName()+".arff";
					c2a.convert(csvFilePath,arff_file_name );
					System.out.println(arff_file_name);
					request.getSession().setAttribute("arff_file_name",arff_file_name);
				}
			}
			
			request.getSession().setAttribute("trainingTableClassifierIndex",Integer.parseInt(trainingTableClassifierIndex));
			request.getSession().setAttribute("trainingDataTable",trainingDataTable);
			
			InstanceQuery query = new InstanceQuery();
			query.setCustomPropsFile(new File(request.getServletContext().getRealPath("/")+PropertiesUtility.getProperty("DB_PROPERTIES_PATH")));
			query.setUsername(PropertiesUtility.getProperty("DB_USER_NAME"));
			query.setPassword(PropertiesUtility.getProperty("DB_PASSWORD"));
			String trainingQuery = PropertiesUtility.getProperty("FETCH_QUERY");
			query.setQuery(trainingQuery);
			train = query.retrieveInstances();
			request.getSession().setAttribute("inputData",train);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		response.sendRedirect("OutLierReport.html");
	}

}
