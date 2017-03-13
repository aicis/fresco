/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.framework.sce.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.sce.util.Util;

public class DatabaseConfiguration {

	private static DatabaseConfiguration instance;

	//Required options
	private String driverClassName;
	private String username;
	private String password;	
	private String url;
	//optional options
	private int maxActive;
	private int maxIdle;
	private int initialSize;
	private boolean testWhileIdle = true;
	private String validationQuery = "SELECT 1";
	private int validationInterval = 60000;
	private int maxAge = 3600000;
	
	private DatabaseConfiguration() { 
		loadProperties();
	}
	
	public static DatabaseConfiguration getInstance() {
		if(instance == null) {
			instance = new DatabaseConfiguration();			
		}
		return instance;
	}
	
	private Properties prop;
	private final String defaultPropertiesLocation = "properties/db.properties";

	private synchronized void loadProperties() {
		InputStream is;
		try {
			is = Util.getInputStream(defaultPropertiesLocation);
			prop = new Properties();
			prop.load(is);
			
			//required
			driverClassName = prop.getProperty("dataSource.driverClassName");
			username = prop.getProperty("dataSource.username");
			password = prop.getProperty("dataSource.password");
			url = prop.getProperty("dataSource.url");
			
			//optional
			maxActive = Integer.parseInt(prop.getProperty("dataSource.maxActive", "10"));
			maxIdle = Integer.parseInt(prop.getProperty("dataSource.maxIdle", "10"));
			initialSize = Integer.parseInt(prop.getProperty("dataSource.initialSize", "5"));
			testWhileIdle = Boolean.parseBoolean(prop.getProperty("dataSource.testWhileIdle", "true"));
			validationQuery = prop.getProperty("dataSource.validationQuery", "SELECT 1");
			validationInterval = Integer.parseInt(prop.getProperty("dataSource.validationInterval", "60000"));
			maxAge = Integer.parseInt(prop.getProperty("dataSource.maxAge", "3600000"));
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new MPCException(
					"Could not read database properties file at localtion: "
							+ defaultPropertiesLocation);
		}
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getUrl() {
		return url;
	}

	public int getMaxActive() {
		return maxActive;
	}

	public int getMaxIdle() {
		return maxIdle;
	}

	public int getInitialSize() {
		return initialSize;
	}

	public boolean isTestWhileIdle() {
		return testWhileIdle;
	}

	public String getValidationQuery() {
		return validationQuery;
	}

	public int getValidationInterval() {
		return validationInterval;
	}

	public int getMaxAge() {
		return maxAge;
	}

	public Properties getProp() {
		return prop;
	}

	public String getDefaultPropertiesLocation() {
		return defaultPropertiesLocation;
	}

	@Override
	public String toString() {
		return "DatabaseConfiguration [driverClassName=" + driverClassName
				+ ", username=" + username + ", password=" + password
				+ ", url=" + url + ", maxActive=" + maxActive + ", maxIdle="
				+ maxIdle + ", initialSize=" + initialSize + ", testWhileIdle="
				+ testWhileIdle + ", validationQuery=" + validationQuery
				+ ", validationInterval=" + validationInterval + ", maxAge="
				+ maxAge + ", prop=" + prop + ", defaultPropertiesLocation="
				+ defaultPropertiesLocation + "]";
	}

}
