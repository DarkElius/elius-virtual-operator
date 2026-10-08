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
When the project is compiled it'll generate a **jar** file

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
To start the application execute
 *java -jar -Dewa.config.path=/etc/evo/config -Devo.task.path=/etc/evo/tasks target/EliusVirtualOperator-<release>.jar*


**Parameters**

 * **ewa.config.path**, set the resource directory complete path
       *i.e.: -Dewa.config.path=/etc/evo/config*

 * **evo.task.path**, set the task directory complete path (plugins)
       *i.e.: -Devo.task.path=/etc/evo/tasks*
       

 * **log4j.configurationFile**, Optional, set an external Apache Log4j2 xml configuration
       *i.e.: -Dlog4j.configurationFile=/my-path-name/resources/log4j2.xml*



**Database connection configuration**

Please check *evo.properties* file.




API
-----
Please refer to Bruno collection



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


