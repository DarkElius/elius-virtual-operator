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

import elius.virtualoperator.lock.Lock;
import elius.virtualoperator.lock.LockDatabase;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST API resource interface for managing locks.
 * Exposes endpoints under the {@code /evo/lock} path.
 */
@Path("/evo/lock")
public class LockInterface {
	
	// Get logger
	private static Logger logger = LogManager.getLogger(LockInterface.class);
	
	// Lock database access
	private LockDatabase dbLock;

	
	/**
	 * Constructor
	 */
	public LockInterface() {
		// Initialize lock database access
		dbLock = new LockDatabase();
	}
	
	
	/**
     * Retrieves all stored locks from the database.
     * 
     * @return a JAX-RS Response containing the list of locks, No Content status if empty, or Internal Server Error on failure
     */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get() {
		
		// Log request received
		logger.trace("Lock get request received");
		
		// Fetch task from database
		List<Lock> lockList = dbLock.getAll();
		
		// Check errors
		if (null == lockList) {
			// Log request error
			logger.error("Error on getting lock list");
			
			// Return error
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();			
		}
	
		// Log request successfully
		logger.trace("Lock fetched");
		
		// Return created
		return Response.ok().entity(lockList).build();
	}
}
