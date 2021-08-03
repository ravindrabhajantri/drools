# drools-persistance-H2
This project implements drools persistence with H2

## Setup

Project is created with:
* This project acts as a drools engine and demonstrates that the KieSession can be persisted in H2 database.
* Persistence is achieved with Java Persistence API (JPA) support from Drools.
* H2 DB libraries are required and the URL needs to be configured in the source code for the connection.
* Hibernate is used as JPA impl.
* Bitronix is used as Transaction Manager
* For more details on Persistence and Transaction with drools, refer https://docs.jboss.org/drools/release/7.42.0.Final/drools-docs/html_single/#_persistence_and_transactions

## H2 DB modes and configuration
### Embedded H2 - In-memory
* Data is lost when connection lost
* Not suitable for restoring session after restart
* Connection URL
```
db.url=jdbc:h2:mem:DROOLS
```

### Embedded H2 - File based
* File based
* Can be used to restore session after restart
* Parallel access not possible. For example the same H2 DB file can not be shared between two instances/projects/containers
* Connection URL
```
db.url=jdbc:h2:file:<Path>/DROOLS
```

### External H2 In-memory
* Download and run H2 Server as an external server
* Can be used to restore session after drools restarts
* Session is restored as long as the H2 server is kept running
* Data is lost when the H2 server restarts.
* Parallel access is possible
* Connection URL
```
db.url=jdbc:h2:tcp://<IP:PORT>/mem:DROLS;DB_CLOSE_DELAY=-1; 
```

### External H2 - File based
* Download and run H2 Server as and external server
* Can be used to restore session after drools restarts
* Session is restored even after H2 server restarts
* Data is lost when the H2 server restarts.
* Parallel access is possible
* Connection URL
```
db.url=jdbc:h2:tcp://<IP:PORT>/<Path>/DROOLS;DB_CLOSE_DELAY=-1;
```

### Note
After down-loading external H2 binaries, modify h2.bat in bin folder to allow database creation if not exists. Changes are shown below.
```
@java -cp "h2-1.4.200.jar;%H2DRIVERS%;%CLASSPATH%" org.h2.tools.Console %*
```
to 
```
@java -cp "h2-1.4.200.jar;%H2DRIVERS%;%CLASSPATH%" org.h2.tools.Server -ifNotExists %* -webAllowOthers -tcpAllowOthers
```
   

## Description
Student objects are created and pushed into session and rules are run to calculate pass grade based on the marks obtained.

Model:
```
	$ public class Student implements Serializable {
		private String studentId;
		private String studentName;
		private String passGrade;
		private int marksObtained;
		private String school;
	  ...
    }
```
Example DRL:
```
rule "Grade Fail"
	when 
		student: Student(marksObtained < 35)
	then
		student.setPassGrade("fail");
		System.out.println("Rule Correlation: Grade for student "+student.getStudentName()+" is Fail");
	end
```

## Before run
Configure H2 URL at src/main/resources/config.properties with actuals.


## How to run
### WorkingWithMutlipleSessions.java
* Acts as a stand-alone program that shows that creation of multiple sessions and reloading of existing KieSession
* Sample facts are added and rules are fired.
* The facts which are inserted in the session are are printed.
* When facts from session are printed, note the number of facts for the new/reloaded sessions and their updated status from rule firing.

### SessionInfoDetails.java
* If you want to store the KieSession id in user-defined table for future reference, create an entity like this.
* WorkingWithMutlipleSessions.java have a method that makes use of existing Entity Manager Factory to persist the user defined table.
