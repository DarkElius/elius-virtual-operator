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

package elius.virtualoperator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import elius.virtualoperator.db.DbTables;
import elius.virtualoperator.task.TaskManager;
import elius.virtualoperator.task.TaskSubmitter;
import elius.webapp.framework.application.ApplicationLifecycle;
import elius.webapp.framework.application.ApplicationContext;
import elius.webapp.framework.db.DbEngine;
import elius.webapp.framework.exceptions.LifecycleException;
import elius.webapp.framework.properties.PropertiesManager;
import elius.webapp.framework.properties.PropertiesManagerFactory;

import javax.sql.DataSource;

/**
 * Manage the Virtual Operator application lifecycle.
 */
public class VirtualOperator implements ApplicationLifecycle {

	// Get logger
	private static Logger logger = LogManager.getLogger(VirtualOperator.class);

	// Task executor service
	private ExecutorService taskExecutor;
	
	// Scheduler service for task execution
	private ScheduledExecutorService scheduler;
	
	// Scheduled Future of scheduler service
	private ScheduledFuture<?> scheduledFuture;
	
	// Properties file
	private final PropertiesManager appProperties;

	// Task Manager
	private TaskManager taskManager;


	// Database
    private final DbEngine dbEngine = new DbEngine();
    private static DataSource dataSource;	

	/**
	 * Coonstructor
	 */
    public VirtualOperator() {
		// Application properties
		appProperties = PropertiesManagerFactory.getInstance(VirtualOperatorAttributes.EVO_PROPERTIES_FILE);		
    }

	/**
	 * Static global get datasource 
	 * @return Db Datasource
	 */
    public static DataSource getDataSource() {
        return dataSource;
    }	

    @Override
    public void onStartup() throws LifecycleException {
		// Log initialization
		logger.debug("Virtual Operator initialization...");

		// Load database information
		String dsUrl = appProperties.get(VirtualOperatorAttributes.PROP_DATABASE_DATASOURCE_URL);
		String dsUsername = appProperties.get(VirtualOperatorAttributes.PROP_DATABASE_DATASOURCE_USERNAME);
		String dsPassword = appProperties.get(VirtualOperatorAttributes.PROP_DATABASE_DATASOURCE_PASSWORD);
		int dsMaxPoolSize = appProperties.getInt(VirtualOperatorAttributes.PROP_DATABASE_DATASOURCE_MAXPOOLSIZE, VirtualOperatorAttributes.DEFAULT_DATABASE_DATASOURCE_MAXPOOLSIZE);

		ApplicationContext.register(DataSource.class, dbEngine.createPool(
            dsUrl,
            dsUsername, 
            dsPassword, 
            dsMaxPoolSize
        ));
		
		// Initialize database (drop/create) if required
		if("Y".equalsIgnoreCase(appProperties.get(VirtualOperatorAttributes.PROP_DATABASE_INIT_ON_BOOT))) {
			
			// Log request received
			logger.debug("Database initialization request received");
			
			// Application database instance
			DbTables dbTask = new DbTables();
			
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
			// Save TaskManager to application context
			ApplicationContext.register(TaskManager.class, taskManager);
		} catch (Exception e) {
			logger.catching(e);
		}
	
		// Log initialization
		logger.info("Virtual Operator initialized");		
	}
	
	
	@Override
    public void onShutdown() throws LifecycleException {
        // Log shutdown start
        logger.debug("Virtual Operator shutdown...");

        // Shutdown Scheduler
        shutdownExecutor(scheduler, scheduledFuture, "Scheduler");

        // Shutdown Task Executor
        shutdownExecutor(taskExecutor, null, "Task executor");

        // Log shutdown complete
        logger.info("Virtual Operator terminated");     
    }

    /**
     * Safely shuts down an executor service with timeout and error handling.
     *
     * @param executor the executor service to shutdown
     * @param future the optional scheduled future associated with the executor
     * @param componentName the name of the component for logging
     */
    private void shutdownExecutor(ExecutorService executor, ScheduledFuture<?> future, String componentName) {
        if (executor == null) {
            logger.warn("{} inactive", componentName);
            return;
        }

        // Log a message if the service was already done/crashed
        if (future != null && future.isDone()) {
            logger.warn("{} service was not running before shutdown", componentName);
        }

        // Shutdown service
        executor.shutdown();

        // Wait safely for termination (replaces empty while loop)
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                logger.warn("{} did not terminate gracefully, forcing shutdown", componentName);
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            logger.error("The {} termination was interrupted", componentName, e);
        }

        logger.debug("{} terminated", componentName);
    }


	/**
	 * Get Task Manager 
	 * @return Task Manager
	 */
	public TaskManager getTaskManager() {
		return taskManager;
	}
}
