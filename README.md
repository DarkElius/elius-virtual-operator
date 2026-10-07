Elius Virtual Operator
-----



Description
-----
The virtual operator is a software that can execute different tasks independently.

Tasks are not included in the application: you can download them from the repository.

Task can be implemented using *Elius Virtual Operator Task* interface available on the reopsitory.
 
Requests can be submitted and monitored using http interface.

The software is scalable and can be installed on multiple nodes to better manage high workloads.




Components
-----
When the project is compiled it'll generate a **war** file

The **resources** directory can be placed every where in your file system.

It contains:
 * **ewa.properties**, the framework properties file
 
 
 * **log4j2.xml**, the Apache Log4j2 log configuration file
 
 
 * **evo.properties**, the virtual operator generic properties file



Compilation
-----
To compile this project you need to compile and install the *Elius WebApp Framework* on your local Maven repository.

When the *Elius WebApp Framework* will be stable it'll be released on official Maven repository.




Run
-----
This project must be deployed on an application server: I'm currently using Apache Tomcat 11.


**Parameters**

There are two parameters that muset be set at jvm level:
 * **ewa.path**, set the resource directory complete path
       *i.e.: -Dewa.path=/my-path-name/resources*
       

 * **log4j.configurationFile**, set the Apache Log4j2 xml complete path
       *i.e.: -Dlog4j.configurationFile=/my-path-name/resources/log4j2.xml*



**Database connection configuration - H2 (example only)**

H2 is used only as an example embedded database for development and testing.
It is **not required at runtime** and is marked as `provided` in the Maven configuration.

To use H2 with Tomcat:

1. Download H2 from the official website
2. Copy `h2*.jar` into `$TOMCAT_HOME/lib`
3. Configure the JNDI DataSource as shown below

*<Resource driverClassName="org.h2.Driver"*

 *maxIdle="2" maxTotal="10" maxWaitMillis="5"*
  
 *name="jdbc/evodb"*
  
 *type="javax.sql.DataSource"*
  
 *url="jdbc:h2:mem:evodb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"*
  
 *username="" password=""*

*/>*




API
-----

**Tasks**

* Add a EVO_DEBUG task
  
  *curl -X PUT -d '{"status": "NEW", "type": "EVO_DEBUG", "priority": "LOW", "details": "nothing", "requester": "generic", "owner": "foo"}' http://localhost:8080/EliusVirtualOperator/api/evo/task  --header "Content-Type:application/json"*
  
* Add a SHELL task

  *curl -X PUT -d '{"status": "NEW", "type": "EVO_SHELL", "priority": "LOW", "details": "test.sh", "requester": "generic", "owner": "foo"}' http://localhost:8080/EliusVirtualOperator/api/evo/task  --header "Content-Type:application/json"*
  
  *test.sh* must be present in the directory specified in the *evo-shell.txt* property file under label *task.shell.scripts.path*

* Get all tasks

  *curl http://localhost:8080/api/evo/task*
  
**Management**
  
* Get all locks
  
  *curl http://localhost:8080/EliusVirtualOperator/api/evo/lock*


**Authentications**

To enable the authentication modify the following line in the WEB-INF/web.xml

From
  */api/evo-DISABLED/*


To
  */api/evo/*



Create your own task
-----
To create your own task 
  * Download *Elius Virtual Operator Task* interface available on the reopsitory.
  * Implement the plugin
  * Copy the jar in the project (i.e.: *resources/tasks*)
  * Set the correct path inside *task.process.path* in the *evo.properties*
  * Copy needed plugin propertie in *resources* directory


Road Map
-----
These are more or less the steps I want to implement in the near future

 * GUI
 
 * Create a virtual operator option to force the node to work only on specify a task type 




### License

Apache 2.0 - <https://www.apache.org/licenses/LICENSE-2.0>


