# drools-persistance-mariadb
This project implements drools persistence with maradb

## Setup

Project is created with:
* This project acts as a drools engine and demonstrates that the KieSession can be persisted in an external database like MariaDB.
* Persistence is achieved with Java Persistence API (JPA) support from Drools.
* An external Maria DB is required and the URL needs to be configured in the source code for the connection.
* Hibernate is used as JPA impl.
* Bitronix is used as Transaction Manager
* For more details on Persistence and Transaction with drools, refer https://docs.jboss.org/drools/release/7.42.0.Final/drools-docs/html_single/#_persistence_and_transactions

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
Change Maria DB URL at src/main/resources/config.properties with actuals.


## How to run
### NewDroolsSessionOnMariaDb.java
* Acts as a stand-alone program that creates a new KieSession and adds some sample facts. 
* Fire the rules on the facts added. 
* After firing,  some more facts are added (note that rules are not fired intentionally for these facts) to demonstrate that these facts would be stored in Maria DB.
* After the class is run, check the MariaDB database to check if the session is persisted.
* Also, note the "session id" created from the logs.

### ReloadSavedDroolsSessionFromMariaDb.java
* Modify the session id to load. 
* When the program run, it loads the session persisted in Maria DB and fires the rules on them.

