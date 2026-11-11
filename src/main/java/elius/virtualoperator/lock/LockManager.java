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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import elius.virtualoperator.VirtualOperatorAttributes;
import elius.virtualoperator.VirtualOperatorId;
import elius.webapp.framework.properties.PropertiesManager;
import elius.webapp.framework.properties.PropertiesManagerFactory;

/**
 * Manager to acquire or release locks on database.
 */
public class LockManager {

	// Get logger
	private static Logger logger = LogManager.getLogger(LockManager.class);
	
	// Lock database access
	private final LockDatabase dbLock;
	
	// Lock instance
	private final Lock lock;
	
	// Properties file
	private final PropertiesManager appProperties;
	
	// Expire in seconds
	private int expire;
	
	// Max retry to get lock
	private int maxRetry;

	// Max time to wait in seconds between retries
	private int maxRetrySleep;

	
	/**
	 * Constructor
	 */
	public LockManager() {
		
		// Initialize lock database access
		dbLock = new LockDatabase();
		
		// Application properties
		appProperties = PropertiesManagerFactory.getInstance(VirtualOperatorAttributes.EVO_PROPERTIES_FILE);
		
		// Lock
		lock = new Lock();
	}

	
	/**
	 * Initialize lock manager
	 * The process verify if the application lock record is already defined
	 * otherwise it insert the record. 
	 * No duplicated allowed due to primary key
	 */
	public void initialization() {
		// Log start initialization
		logger.info("Initialize Lock Manager");
		
		// Get application name
		String application = appProperties.get(VirtualOperatorAttributes.PROP_LOCK_MANAGER_APPLICATION, VirtualOperatorAttributes.DEFAULT_LOCK_MANAGER_APPLICATION);
		
		// Search if there are other records for the same application
		if(null == dbLock.get(application)) {
			
			// Set application name
			lock.setApplication(application);
			// Empty Owner
			lock.setOwner("");
			
			// Other parameters like Id and time stamps are already initialized by the constructor
			
			// Insert empty lock
			if (dbLock.insert(lock) > 0) {
				// Log successful
				logger.info("Log Manager initialized for selected application={}", application);
			} else {
				// Log lock already present
				logger.warn("Log Manager already initialized for selected application={}", application);
			}
		}
		

		// Expire in seconds
		expire = appProperties.getInt(VirtualOperatorAttributes.PROP_LOCK_MANAGER_EXPIRE, VirtualOperatorAttributes.DEFAULT_LOCK_MANAGER_EXPIRE);

		// Max retry to get lock
		maxRetry = appProperties.getInt(VirtualOperatorAttributes.PROP_LOCK_MANAGER_MAX_RETRY, VirtualOperatorAttributes.DEFAULT_LOCK_MANAGER_MAX_RETRY);

		// Max time to wait in seconds between retries
		maxRetrySleep = appProperties.getInt(VirtualOperatorAttributes.PROP_LOCK_MANAGER_MAX_RETRY_SLEEP, VirtualOperatorAttributes.DEFAULT_LOCK_MANAGER_MAX_RETRY_SLEEP);
	
		
		// Log end initialization
		logger.info("Lock Manager initialized");
	}
	
	

	/**
	 * Request lock
	 * @return 0 Acquired, 1 Not acquired
	 */
	public int lock() {
		
		// Set owner: try to put much information possible to identify the process
		lock.setOwner(VirtualOperatorId.get());
		
		// Set expire date to understand when the lock could be considered invalid
		lock.setExpire(new Timestamp(System.currentTimeMillis() + (expire * 1000)));

		// Log start acquiring lock
		logger.trace("Acquiring lock, owner={}", lock.getOwner());
		
		boolean locked = false;
		
		// Try to acquire lock
		for (int i = 0; (i < maxRetry) && (!locked); i++) {
			
			// Log tentative
			logger.trace("Try to get lock, tentative {} of {}", (i + 1), maxRetry);
			
			// Try to get lock
			if(0 == dbLock.update(lock)) {
				
				// Set exit status
				locked = true;

				// Log success
				logger.debug("Lock acquired by owner={}", lock.getOwner());
				
			} else {
				try {
					
					// Wait in seconds
					Thread.sleep(maxRetrySleep * 1000L);
					
				} catch (InterruptedException e) {
					// Log error
					logger.error("Error during sleeping between lock tentatives", e);
					/* Clean up whatever needs to be handled before interrupting  */
					Thread.currentThread().interrupt();
				}
			}		
		}
		
		// Lock not acquired
		if(!locked) {
			// Not an error or warning because it depends on the configuration and architecture design: set on debug messages
			logger.debug("Lock not acquired by owner={}", lock.getOwner());
			// Exit with not acquired
			return 1;
		}
			
		// Return acquired
		return 0;
	}
	
	
	/**
	 * Release lock
	 * @return 0 Release, 1 Fail to release
	 */
	public int unlock() {
		// Log start removing lock
		logger.trace("Removing lock, owner={}", lock.getOwner());

		// Release the lock
		if (0 != dbLock.free(lock)) {
			// Log warning
			logger.error("Failed to unlock, owner={}", lock.getOwner());
			
			// Return warning
			return 1;
		}
		
		// Log success
		logger.debug("Unlocked, owner={}", lock.getOwner());
		
		return 0;
	}

}
