package com.ibm.BLUdemo;

import java.util.Set;

import com.ibm.nosql.json.api.BasicDBList;
import com.ibm.nosql.json.api.BasicDBObject;
import com.ibm.nosql.json.util.JSON;


public class VCAPServices {
	
	// VCAP_SERVICES is a system environment variable
	// Parse it to obtain the for DB2 connection info
	private String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
	private String hostname;
	private Integer port;
	private String username;
	private String password;
	private String db;
	private String jdbcurl;
	
	public VCAPServices(){
		if (VCAP_SERVICES != null) {
			// parse the VCAP JSON structure
			BasicDBObject obj = (BasicDBObject) JSON.parse(VCAP_SERVICES);
			String thekey = null;
			Set<String> keys = obj.keySet();
			System.out.println("Searching through VCAP keys");
			System.out.println("VCAP_SERVICES content: " + VCAP_SERVICES);

			// Look for the VCAP key that holds the IRDS information
			for (String eachkey : keys) {
				System.out.println("Key is: " + eachkey);
				if (eachkey.contains("BLU")) {
					thekey = eachkey;
				}
			}
			if (thekey == null) {
				System.out.println("Cannot find any BLUDW service in the VCAP; exiting");
				return;
			}
			try {
				BasicDBList list = (BasicDBList) obj.get(thekey);
				obj = (BasicDBObject) list.get("0");
				System.out.println("Service found: " + obj.get("name"));
				obj = (BasicDBObject) obj.get("credentials");
				hostname = (String) obj.get("hostname");
				db = (String) obj.get("db");
				port = (Integer) obj.get("port");
				username = (String) obj.get("username");
				password = (String) obj.get("password");
				jdbcurl = (String) obj.get("jdbcurl");
			} catch (Throwable e) {
				System.out.println("Error" + e);
			}

		} else {
			System.out.println("VCAP_SERVICES is null");
		}
	}
	
	public String getUser() {
		return username;
	}
	
	public void setUser(String user) {
		this.username = user;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	public Integer getPort() {
		return port;
	}
	
	public void setPort(Integer port) {
		this.port = port;
	}


	public String getDbName() {
		return db;
	}


	public void setDbName(String dbName) {
		this.db = dbName;
	}


	public String getUrl() {
		return jdbcurl;
	}


	public void setUrl(String url) {
		this.jdbcurl = url;
	}
	
}
