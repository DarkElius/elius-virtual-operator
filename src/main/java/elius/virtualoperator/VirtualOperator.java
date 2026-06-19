/**
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at
    
      http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
*/

package elius.virtualoperator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import elius.virtualoperator.db.DBTables;
import elius.virtualoperator.task.TaskManager;
import elius.virtualoperator.task.TaskSubmitter;
import elius.webapp.framework.properties.PropertiesManager;
import elius.webapp.framework.properties.PropertiesManagerFactory;

public class VirtualOperator {

    private static VirtualOperator instance;

	// Get logger
	private static Logger logger = LogManager.getLogger(VirtualOperator.class);

	// Task executor service
	private ExecutorService taskExecutor;
	
	// Scheduler service for task execution
	private ScheduledExecutorService scheduler;
	
	// Scheduled Future of scheduler service
	private ScheduledFuture<?> scheduledFuture;
	
	// Properties file
	private PropertiesManager appProperties;

	// Task Manager
	private TaskManager taskManager;

	/**
	 * Coonstructor
	 */
    public VirtualOperator() {
        instance = this;
    }

	/**
	 * Get Virtual Operator instance
	 * @return Virtual Operator instance
	 */
    public static VirtualOperator getInstance() {
        return instance;
    }
	

	/**
	 * Initialize application
	 */
	public void initialization() {
		// Log initialization
		logger.debug("Operator service initialization");
		
		// Application properties
		appProperties = PropertiesManagerFactory.getInstance(VirtualOperatorAttributes.EVO_PROPERTIES_FILE);
		
		// Initialize database (drop/create) if required
		if("Y".equalsIgnoreCase(appProperties.get(VirtualOperatorAttributes.PROP_DATABASE_INIT_ON_BOOT))) {
			
			// Log request received
			logger.debug("Database initialization request received");
			
			// Application database instance
			DBTables dbTask = new DBTables();
			
			// Database Initialization
			if (0 != dbTask.init()) {
				// Log error
				logger.error("Error during database initialization");
			} else {
				// Log successful
				logger.info("Database initialized");			
			}
		}


		// Read number of threads from properties
		int nThreads = appProperties.getInt(VirtualOperatorAttributes.PROP_EXECUTOR_SERVICE_THREADS, VirtualOperatorAttributes.DEFAULT_EXECUTOR_SERVICE_THREADS);

		// Initialize executor service
		taskExecutor = Executors.newFixedThreadPool(nThreads);	
		
		// Log executor service
		logger.debug("Executor service defined");
			
		// Initialize scheduler service
		scheduler = Executors.newSingleThreadScheduledExecutor();
		
		// Log executor service
		logger.debug("Scheduler service defined");

		
		// Activate executor service scheduler
		if("Y".equalsIgnoreCase(appProperties.get(VirtualOperatorAttributes.PROP_EXECUTOR_SERVICE_SCHEDULE_ACTIVE))) {
			
			// Read time interval from properties
			int timeInterval = appProperties.getInt(VirtualOperatorAttributes.PROP_EXECUTOR_SERVICE_TIME_INTERVAL, VirtualOperatorAttributes.DEFAULT_EXECUTOR_SERVICE_SCHEDULE_ACTIVE);
			
			// Schedule task submission
			scheduledFuture = scheduler.scheduleAtFixedRate(new TaskSubmitter(taskExecutor), 0, timeInterval, TimeUnit.SECONDS);
			
			// Log scheduler service
			logger.debug("Scheduler task submission defined");			
		} else {
			// Log scheduler service
			logger.warn("Scheduler task submission disabled");		
		}
		
		// Load Task Processes (Pluginss)
		taskManager = new TaskManager();
		try {
			taskManager.loadPlugins(appProperties.get(VirtualOperatorAttributes.PROP_TASK_PROCESS_PATH));
		} catch (Exception e) {
			logger.catching(e);
		}
	
		// Log initialization
		logger.info("Operator service initialized");		
	}
	
	
	/**
	 * Destroy application
	 */
	public void destroy() {
		// Log shutdown
		logger.debug("Operator service shutdown");

		// Log scheduler shutdown
		logger.debug("Shutdown scheduler");
		
		// Verify scheduler status
		if(null != scheduler) {
			
			// Log a message if the scheduler service was crashed
			if(scheduledFuture.isDone())
				logger.warn("Scheduler service was not running before shutdown");
			
			// Shutdown scheduler
	        scheduler.shutdown();
	        
	        // Wait for the end
	        while (!scheduler.isTerminated()) {
	        }
	        
			// Log scheduler shutdown
			logger.debug("Scheduler terminated");			
		} else {
			// Log missing scheduler
			logger.warn("Scheduler inactive");
		}


		// Log scheduler shutdown
		logger.debug("Shutdown task executor");
		
		// Verify task executor status
		if(null != taskExecutor) {

			// Shutdown task executor
			taskExecutor.shutdown();
        
			// Wait for the end
			while (!taskExecutor.isTerminated()) {
			}
        
			// Log task executor shutdown
			logger.debug("Task executor terminated");
		} else {
			// Log missing task executor
			logger.warn("Task executor inactive");
		}
        
		// Log shutdown
		logger.info("Operator service terminated");		
	}


	/**
	 * Get Task Manager 
	 * @return Task Manager
	 */
	public TaskManager getTaskManager() {
		return taskManager;
	}
}
