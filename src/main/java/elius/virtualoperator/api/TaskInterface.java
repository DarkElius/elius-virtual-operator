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

package elius.virtualoperator.api;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import elius.virtualoperator.task.Task;
import elius.virtualoperator.task.TaskDatabase;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/evo/task")
public class TaskInterface extends Application {

	// Get logger
	private static Logger logger = LogManager.getLogger(TaskInterface.class);
	
	// Task Database
	private TaskDatabase dbTask;
	
	/**
	 * Constructor
	 */
	public TaskInterface() {
		// Task Database instance
		dbTask = new TaskDatabase();
	}
	
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response add(Task task) {
		
		// Log request received
		logger.trace("Task add-to-queue request received");
		
		// Add task to queue
		if (0 != dbTask.add(task))
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

		// Log request successfully
		logger.trace("New Task added to queue");
		
		// Return Created
		return Response.status(Response.Status.CREATED).build();
	}
	
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get() {
		
		// Log request received
		logger.trace("Task get request received");
		
		// Fetch task from database
		List<Task> tasks = dbTask.getAll(999);
		
		// Check errors
		if (null == tasks) {
			// Log request error
			logger.error("Error on get task request");
			
			// Return error
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();			
		}
	
		// Log request successfully
		logger.trace("Task fetched");
		
		// Return created
		return Response.ok().entity(tasks).build();
	}
}
