package com.ibm.BLUdemo;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

@WebServlet(name="UploadFileServlet",urlPatterns={"/UploadFileServlet"})
public class UploadFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServletFileUpload uploader = null;

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
		if (!ServletFileUpload.isMultipartContent(request)) {
			throw new ServletException(
					"Content type is not multipart/form-data");
		}
		try {
			List<FileItem> fileItemsList = uploader.parseRequest(request);
			Iterator<FileItem> fileItemsIterator = fileItemsList.iterator();
			while (fileItemsIterator.hasNext()) {
				FileItem fileItem = fileItemsIterator.next();
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
				
				// Forwarding the request to Run the Naive Bayes Algorithm
				ServletContext servletContext = getServletContext();
				RequestDispatcher requestDispatcher = servletContext.getRequestDispatcher("/RunNaiveBayesServlet");

				request.setAttribute("arff_file_name", arff_file_name);
				requestDispatcher.forward(request, response);
			}
		} catch (FileUploadException e) {
			System.out.println("Exception in uploading file.");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Exception in uploading file.");
			e.printStackTrace();
		}
	}

}
