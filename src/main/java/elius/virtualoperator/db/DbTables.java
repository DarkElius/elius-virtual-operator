/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package elius.virtualoperator.db;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import elius.webapp.framework.application.ApplicationContext;
import elius.webapp.framework.db.DbManager;


/**
 * Drop / create virtual operator tables.
 */
public class DbTables {

	/** Task details, max length */
	public static final int TASK_DETAILS_MAX_LENGTH = 1024;
	/** Task result, max length */
	public static final int TASK_RESULT_MAX_LENGTH = 4096;


	// Get Logger
	private static Logger logger = LogManager.getLogger(DbTables.class);
	
	// Database interface
	private final DbManager db;
	
	// Drop table LOCK
	private static final String SQL_TABLE_DISTRIBUTED_LOCK_DROP = "DROP TABLE DISTRIBUTED_LOCK";

	// Create table LOCK
	private static final String SQL_TABLE_DISTRIBUTED_LOCK = "CREATE TABLE DISTRIBUTED_LOCK ("
															+ "  APPLICATION VARCHAR(128) NOT NULL DEFAULT 'EVO'"
															+ ", OWNER VARCHAR(128) NOT NULL DEFAULT ''"
															+ ", ID CHAR(36) NOT NULL DEFAULT ' '"
															+ ", ACQUIRE TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
															+ ", EXPIRE TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
															+ ", PRIMARY KEY (APPLICATION))";

	// Drop table TASK_QUEUE
	private static final String SQL_TABLE_TASK_QUEUE_DROP = "DROP TABLE TASK_QUEUE";

	// Create table TASK_QUEUE
	private static final String SQL_TABLE_TASK_QUEUE = "CREATE TABLE TASK_QUEUE ("
															+ "  ID CHAR(36) NOT NULL DEFAULT ' '"
															+ ", DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
															+ ", STATUS INTEGER NOT NULL DEFAULT 0"
															+ ", TYPE VARCHAR(128) NOT NULL DEFAULT ''"
															+ ", PRIORITY INTEGER NOT NULL DEFAULT 0"
															+ ", DETAILS VARCHAR(" + TASK_DETAILS_MAX_LENGTH  + ") NOT NULL DEFAULT ''"
															+ ", RESULT VARCHAR(" + TASK_RESULT_MAX_LENGTH  + ") NOT NULL DEFAULT ''"
															+ ", REQUESTER VARCHAR(128) NOT NULL DEFAULT ''"
															+ ", OWNER VARCHAR(128) NOT NULL DEFAULT ''"
															+ ", PRIMARY KEY (ID))";

															

	/**
	 * Constructor
	 */
	public DbTables() {
		db = new DbManager(ApplicationContext.get(DataSource.class));
	}
	
	
	/**
	 * Initialize Virtual Operator database
	 * @return 0 Successfully initialized, 1 Error
	 */
	public int init() {
		// Log initialization 
		logger.debug("Database initialization");
	
		// Distributed lock, Drop
		if (0 != db.execute(SQL_TABLE_DISTRIBUTED_LOCK_DROP)) {
			// Log action
			logger.warn("Table DISTRIBUTED_LOCK doesn't exist");
		} else {
			// Log action
			logger.debug("Table DISTRIBUTED_LOCK dropped");
		}

		// Distributed lock, Create
		if (0 != db.execute(SQL_TABLE_DISTRIBUTED_LOCK)) return 1;
		logger.debug("Table DISTRIBUTED_LOCK created");
		
		// Queue, Drop
		if (0 != db.execute(SQL_TABLE_TASK_QUEUE_DROP))  {
			// Log action
			logger.warn("Table TASK_QUEUE doesn't exist");
		} else {
			// Log action
			logger.debug("Table TASK_QUEUE dropped");
		}

		// Queue, Create
		if (0 != db.execute(SQL_TABLE_TASK_QUEUE)) return 1;
		logger.debug("Table TASK_QUEUE created");
		
		// Set return code
		return  0;
	}

}
