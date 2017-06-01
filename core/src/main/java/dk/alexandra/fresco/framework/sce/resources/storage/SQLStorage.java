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
package dk.alexandra.fresco.framework.sce.resources.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.sce.configuration.DatabaseConfiguration;

public class SQLStorage implements Storage {

	private JdbcTemplate jdbcTemplate;
	private static SQLStorage instance;
	
	private static final String KEY = "key_id";
	private static final String DATA_ID = "data";

	public static SQLStorage getInstance() {
		if (instance == null) {
			instance = new SQLStorage();
		}
		return instance;
	}

	private SQLStorage() {
		this.jdbcTemplate = new JdbcTemplate();
		DataSource dataSource = dataSource();
		this.jdbcTemplate.setDataSource(dataSource);
	}

	public DataSource dataSource() {
		org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
		DatabaseConfiguration conf = DatabaseConfiguration.getInstance();
		dataSource.setDriverClassName(conf.getDriverClassName());
		dataSource.setUsername(conf.getUsername());
		dataSource.setPassword(conf.getPassword());
		dataSource.setUrl(conf.getUrl());
		dataSource.setMaxActive(conf.getMaxActive());
		dataSource.setMaxIdle(conf.getMaxIdle());
		dataSource.setInitialSize(conf.getInitialSize());
		dataSource.setTestWhileIdle(conf.isTestWhileIdle());
		dataSource.setValidationQuery(conf.getValidationQuery());
		dataSource.setValidationInterval(conf.getValidationInterval());
		dataSource.setMaxAge(conf.getMaxAge());
		return dataSource;
	}

	private boolean createTable(String tableName) {		
		String sqlStm = "CREATE TABLE IF NOT EXISTS "+tableName+" ("
				+ KEY + " VARCHAR(255) NOT NULL PRIMARY KEY, "
				+ DATA_ID + " BLOB NOT NULL);";
		int rows = this.jdbcTemplate.update(sqlStm);
		if(rows > 0) {
			return true;
		}
		return false;
	}

	@Override
	public boolean putObject(String name, String key, Serializable o) {
		createTable(name);
		String sqlStm = "INSERT INTO "+name+" ("+KEY+", "+DATA_ID+") VALUES (?, ?);";
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			byte[] bytes = baos.toByteArray();			
			int rows = this.jdbcTemplate.update(sqlStm, new Object[] {key, bytes});
			oos.close();
			if(rows > 0) {
				return true;
			} else {
				return false;
			}
		} catch(Exception e) {
			e.printStackTrace();
			throw new MPCException("Could not update the table "+name+" with the values: " + key+", "+o);
		}		
	}

	@Override
	public <T extends Serializable> T getObject(String name, String key) {
		String sqlStm = "SELECT * FROM "+name+" WHERE "+KEY+" = ?;";
		try {
			T data = this.jdbcTemplate.query(sqlStm, new Object[] {key}, new ResultSetExtractor<T>() {

				@SuppressWarnings("unchecked")
				@Override
				public T extractData(ResultSet rs) throws SQLException,
				DataAccessException {
					rs.next();
					InputStream is = rs.getBinaryStream(DATA_ID);
					ObjectInputStream ois;
					T res = null;
					try {
						ois = new ObjectInputStream(is);					
						res = (T) ois.readObject();
					} catch (IOException | ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return res;
				}

			});
			return data;
		} catch(Exception e) {
			Reporter.warn("Exception during fetching from database storage: " + e.getMessage());
			return null;
		}
	}

	@Override
	public boolean removeFromStorage(String name, String key) {
		String sqlStm = "DELETE FROM "+name+" WHERE "+KEY+" = ?;";
		try {
			int rows = this.jdbcTemplate.update(sqlStm, key);
			if(rows > 0) {
				return true;
			} else {
				return false;
			}
		} catch(Exception e) {
			//TODO: Remove once we know the exceptions comming out of this
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean removeNameFromStorage(String name) {
		String sqlStm = "DROP TABLE "+name+";";
		try {
			this.jdbcTemplate.update(sqlStm);
			return true;
		} catch(Exception e) {
			//TODO: Remove once we know the exceptions comming out of this
			e.printStackTrace();
			return false;
		}
	}

}
