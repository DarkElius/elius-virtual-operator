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

package elius.virtualoperator.task;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import elius.virtualoperator.VirtualOperator;
import elius.virtualoperator.db.DbTables;
import elius.webapp.framework.db.DbManager;
import elius.webapp.framework.db.DbUtils;

/**
 * This class give the functions to manages taks inside the database.
 */
public class TaskDatabase {
	
	// Get logger
	private static Logger logger = LogManager.getLogger(TaskDatabase.class);
	
	// Database interface
	private DbManager db;
	
	// Insert task in the TaskQueue table
	private static final String SQL_INSERT = "INSERT INTO TASK_QUEUE (ID, STATUS, TYPE, PRIORITY, DETAILS, REQUESTER, OWNER) VALUES (?, ?, ?, ?, ?, ?, ?)";

	// Select all task in the TaskQueue table with a limit
	// Order by Status, Higher Priority, Last Arrived
	private static final String SQL_SELECT_ALL_MAX_ROWS = "SELECT * FROM TASK_QUEUE ORDER BY DATE DESC FETCH FIRST ? ROWS ONLY";
	
	// Select all task in the TaskQueue table
	// Order by Status, Higher Priority, Last Arrived
	// private static final String SQL_SELECT_ALL = "SELECT * FROM TASK_QUEUE ORDER BY DATE DESC FOR FETCH ONLY";
	
	// Select new task in the TaskQueue table with a limit
	// Order by Higher Priority, Older
	private static final String SQL_SELECT_NEW_MAX_ROWS = "SELECT * FROM TASK_QUEUE WHERE STATUS = 1 ORDER BY PRIORITY DESC, DATE ASC FETCH FIRST ? ROWS ONLY";
	
	// Update task status and set the owner in the TaskQueue table
	private static final String SQL_UPDATE_OWN = "UPDATE TASK_QUEUE SET STATUS = ?, OWNER = ? WHERE ID = ?";

	// Update task status in the TaskQueue table
	private static final String SQL_UPDATE_OWNER = "UPDATE TASK_QUEUE SET OWNER = ? WHERE ID = ?";

	// Update task status in the TaskQueue table
	private static final String SQL_UPDATE_STATUS = "UPDATE TASK_QUEUE SET STATUS = ? WHERE ID = ?";
	
	// Update task result in the TaskQueue table
	private static final String SQL_UPDATE_RESULT = "UPDATE TASK_QUEUE SET STATUS = ?, RESULT = ? WHERE ID = ?";
	
	/**
	 * Constructor
	 */
	public TaskDatabase() {
		db = new DbManager(VirtualOperator.getDataSource());
	}
	
	/**
	 * Add task to the internal queue
	 * @param task Task
	 * @return 0 Successfully, 1 Error
	 */
	public int add(Task task) {
		// Log add task
		logger.trace("Add task(" + task.getUuid().toString() + ")");

		// Check the number of rows inserted
		if (db.update(SQL_INSERT, task.getUuid().toString(), task.getStatus().getId(),
				task.getType(), task.getPriority().getId(), task.getDetails(), task.getRequester(), task.getOwner()) > 0) {
			// Log the error
			logger.error("No rows inserted");
			// Return error
			return 1;
		}
		
		// Return successfully
		return 0;			
	}
	
	
	/**
	 * Own task in the queue
	 * @param uuid Task UUID
	 * @param status New task status
	 * @param owner Owner
	 * @return 0 Successfully, 1 Error
	 */
	public int own(UUID uuid, TaskStatus status, String owner) {
		// Log update task
		logger.trace("Update task(" + uuid + ") set status(" + status.getName() + ") owner(" + owner + ")");

		// Check the number of rows updated
		if (db.update(SQL_UPDATE_OWN, status.getId(), owner, uuid.toString()) > 0) {
			// Log the error
			logger.error("No rows uptdated");
			// Return error
			return 1;
		}
		
		// Return successfully
		return 0;		
	}	
	
	
	/**
	 * Update task owner 
	 * @param uuid Task UUID
	 * @param owner Owner
	 * @return 0 Successfully, 1 Error
	 */
	public int updateOwner(UUID uuid, String owner) {
		// Log update task
		logger.trace("Update task(" + uuid + ") set owner(" + owner + ")");

		// Check the number of rows updated
		if (db.update(SQL_UPDATE_OWNER, owner, uuid.toString()) > 0) {
			// Log the error
			logger.error("No rows uptdated");
			// Return error
			return 1;
		}
		
		// Return successfully
		return 0;		
	}
	
	
	/**
	 * Update task status 
	 * @param uuid Task UUID
	 * @param status New task status
	 * @return 0 Successfully, 1 Error
	 */
	public int updateStatus(UUID uuid, TaskStatus status) {
		// Log update task
		logger.trace("Update task(" + uuid + ") set status(" + status.getName() + ")");

		// Check the number of rows updated
		if (db.update(SQL_UPDATE_STATUS, status.getId(), uuid.toString()) > 0) {
			// Log the error
			logger.error("No rows uptdated");
			// Return error
			return 1;
		}
		
		// Return successfully
		return 0;		
	}
	
	
	/**
	 * Update task result 
	 * @param uuid Task UUID
	 * @param status New task status
	 * @param result Task result
	 * @return 0 Successfully, 1 Error
	 */
	public int updateResult(UUID uuid, TaskStatus status, String result) {
		// Log update task
		logger.trace("Update task(" + uuid + ") set status(" + status.getName() + ") result(" + DbUtils.truncate(result, 64, true) + ")");
		
		// Check result lenght before update db
		String chekedResult = DbUtils.truncate(result, DbTables.TASK_RESULT_MAX_LENGTH, true);

		// Check the update result
		if (db.update(SQL_UPDATE_RESULT, status.getId(), chekedResult, uuid.toString()) > 0) {
			// Log the error
			logger.error("No rows uptdated");
			// Return error
			return 1;
		}
		
		// Return successfully
		return 0;		
	}

	
	/**
	 * Get all tasks
	 * @param nTasks The maximum number of task 
	 * @return Task list or null in case of missing connection with the database
	 */
 	public List<Task> getAll(int nTasks) {
 		return get(SQL_SELECT_ALL_MAX_ROWS, nTasks);
 	}

	/**
	 * Get new tasks
	 * @param nTasks The maximum number of task 
	 * @return Task list or null in case of missing connection with the database
	 */
 	public List<Task> getNew(int nTasks) {
 		return get(SQL_SELECT_NEW_MAX_ROWS, nTasks);
 	}
 	
	
	/**
	 * Get task 
	 * @param sql SQL to be executed
	 * @param nTasks The maximum number of task
	 * @return TaskList or null in case of errors
	 */
 	private List<Task> get(String sql, int nTasks) {
		// Log add task
		logger.trace("Get task");

		// Result table
		List<Map<String, Object>> table = db.executeQuery(sql, nTasks);
				
		// Check the result
		if (null == table) {
			// Log the error
			logger.error("No rows fetched");
			// Return error
			return null;
		}	

		// List of tasks
		List<Task> taskList =  new ArrayList<Task>();

		// Read rows from database
		for(Map<String, Object> tableRow : table) {
			
			// Define new task
			Task t = new Task();
			// Set task id
			t.setUuid(UUID.fromString((String)tableRow.get("ID")));
			// Set task time stamp
			t.setDate((Timestamp)tableRow.get("DATE"));
			// Set task status
			t.setStatus(TaskStatus.getById((int)tableRow.get("STATUS")));
			// Set task type
			t.setType((String)tableRow.get("TYPE"));
			// Set task priority
			t.setPriority(TaskPriority.getById((int)tableRow.get("PRIORITY")));
			// Set task details
			t.setDetails((String)tableRow.get("DETAILS"));
			// Set task result
			t.setResult((String)tableRow.get("RESULT"));
			// Set task requester
			t.setRequester((String)tableRow.get("REQUESTER"));
			// Set task owner
			t.setOwner((String)tableRow.get("OWNER"));	
			
			// Add to list
			taskList.add(t);
		}

		// Return task list
		return taskList;
	}

}