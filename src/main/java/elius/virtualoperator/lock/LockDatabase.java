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

package elius.virtualoperator.lock;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import elius.virtualoperator.VirtualOperator;
import elius.webapp.framework.db.DBManager;

public class LockDatabase {
	
	// Get logger
	private static Logger logger = LogManager.getLogger(LockDatabase.class);
	
	// Database interface
	private DBManager db;
	
	// Insert lock in the distributed lock table
	private static final String SQL_INSERT = "INSERT INTO DISTRIBUTED_LOCK (APPLICATION, OWNER, ID, ACQUIRE, EXPIRE) VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?)";

	// Get lock in the distributed lock table
	private static final String SQL_SELECT_ALL = "SELECT * FROM DISTRIBUTED_LOCK FOR FETCH ONLY";

	// Update lock in the distributed lock table, release
	private static final String SQL_UPDATE_FREE = "UPDATE DISTRIBUTED_LOCK SET OWNER = '' WHERE APPLICATION = ? AND OWNER = ? AND ID = ?";

	// Update lock in the distributed lock table, set lock
	private static final String SQL_UPDATE = "UPDATE DISTRIBUTED_LOCK SET OWNER = ?, ID = ?, ACQUIRE = CURRENT_TIMESTAMP, EXPIRE = ? WHERE (APPLICATION = ? AND OWNER = '') OR (APPLICATION = ? AND EXPIRE <= CURRENT_TIMESTAMP)";
 
	// Remove lock in the distributed lock table
	private static final String SQL_DELETE = "DELETE FROM DISTRIBUTED_LOCK";

	
	/**
	 * Constructor
	 */
	public LockDatabase() {
		// Initialize database manager with the virtual operator data-source name
		db = new DBManager(VirtualOperator.getDataSource());
	}
	
	
	/**
	 * Add lock in the distributed lock table
	 * @param lock Lock
	 * @return 0 Successful, 1 Error
	 */
	public int insert(Lock lock) {
		// Log add task
		logger.trace("Add lock Application(" + lock.getApplication() + ") + Owner(" + lock.getOwner() + ")");

		// Insert lock and check return code
		if (0 != db.update(SQL_INSERT, lock.getApplication(), lock.getOwner(), lock.getUuid().toString(), lock.getExpire().toString())) {
			// Log the error
			logger.error("No rows inserted");
			// Return error
			return 1;
		}
		
		// Return successful
		return 0;			
	}
	
	
	/**
	 * Delete lock
	 * @return 0 Successful, 1 Error
	 */
	public int delete() {
		// Log add task
		logger.trace("Delete lock");

		// Delete lock and check return code
		if (0 != db.update(SQL_DELETE)) {
			// Log the error
			logger.error("No rows deleted");
			// Return error
			return 1;
		}
		
		// Return successful
		return 0;			
	}
	
	
	/**
	 * Get lock information for selected application
	 * @return Lock or null in case of errors or if it's not present
	 */
	public Lock get(String application) {
		// Get lock for the specified application
		List<Lock> lockList = getAll();
		
		// No data
		if(null == lockList)
			return null;
		
		// Scroll lock list
		for (Lock lock : lockList) {
			// Search lock for selected application
	        if (lock.getApplication().equals(application)) {
	        	// Found
	            return lock;
	        }
	    }
		
		// Return not found
		return null;
	}


	

	/**
	 * Get all locks
	 * @return Lock list or null in case of errors
	 */
	public List<Lock> getAll() {		
		// Log start
		logger.trace("Get locks");

		// Get active lock
		List<Map<String, Object>> table = db.executeQuery(SQL_SELECT_ALL);
		
		// Check the result
		if (null == table) {
			// Log the error
			logger.error("No rows fetched");
			// Return error
			return null;
		}	
		
		// List of tasks
		List<Lock> lockList =  new ArrayList<Lock>();
		
		// Scroll results
		for(Map<String, Object> tableRow : table) {

			// Create lock
			Lock lock = new Lock();

			// Set lock application
			lock.setApplication((String)tableRow.get("APPLICATION"));		
			// Set owner
			lock.setOwner((String)tableRow.get("OWNER"));
			// Set UUID
			lock.setUuid(UUID.fromString((String)tableRow.get("ID")));
			// Set acquire date
			lock.setAcquired((Timestamp)tableRow.get("ACQUIRE"));
			// Set expire date
			lock.setExpire((Timestamp)tableRow.get("EXPIRE"));
		

			// Add to list
			lockList.add(lock);
		}
		
		// Return result
		return lockList;
	}
	
	
	/**
	 * Update lock
	 * @param lock Lock
	 * @return 0 Successful, 1 Error
	 */
	public int update(Lock lock ) {
		// Log add task
		logger.trace("Get lock");
		
		// Update lock and check return code
		if (0 != db.update(SQL_UPDATE, lock.getOwner(), lock.getUuid().toString(), lock.getExpire().toString(), lock.getApplication(), lock.getApplication())) {
			// Log the error
			logger.error("No rows updated");
			// Return error
			return 1;
		}
		
		// Return successful
		return 0;
	}

	
	/**
	 * Free lock
	 * @param lock Lock to remove
	 * @return 0 Successful, 1 Error
	 */
	public int free(Lock lock ) {
		// Log add task
		logger.trace("Get lock");
		
		// Update lock and check return code
		if (0 != db.update(SQL_UPDATE_FREE, lock.getApplication(), lock.getOwner(), lock.getUuid().toString())) {
			// Log the error
			logger.error("No rows updated");
			// Return error
			return 1;
		}
		
		// Return successful
		return 0;
	}
	
}
