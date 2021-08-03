package com.rav.bhaj.drools.session;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rav.bhaj.drools.entity.SessionInfoDetails;
import com.rav.bhaj.drools.model.Student;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

public class WorkingWithMutlipleSessions {
	public static final String CORRELATION_DROOLS_TRANSACTIONS = "jdbc/BitronixJTADataSource";
	public static final String DRL_LOCATION = "rules/Grading.drl";
	private static final Logger LOGGER = LoggerFactory.getLogger(WorkingWithMutlipleSessions.class);
	private static KieServices kieServices = KieServices.Factory.get();
	private static Properties properties;

	private static Student rama = new Student("123", "rama", "", 71, "SRKVS");
	private static Student sita = new Student("234", "sita", "", 80, "MHS");
	private static Student lakshman = new Student("456", "lakshman", "", 36, "SRKVS");
	private static Student hanuman = new Student("456", "hanuman", "", 56, "SRKVS");
	private static Student ravana = new Student("456", "ravana", "", 25, "SRKVS");

	public static void main(String[] args) throws InterruptedException {
		loadProperties();
		initializeDataSource();
		KieSession kieSession1 = null, kieSession2, kieSession3;

		/** Scenario 1 */
		UserTransaction correlationTransactions;
		try {
			correlationTransactions = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
			correlationTransactions.begin();

			kieSession1 = getNewKieSession();
			LOGGER.info("New Session: session id: {}", kieSession1.getIdentifier());

			kieSession1.insert(rama);
			kieSession1.insert(sita);

			kieSession1.fireAllRules();

			kieSession1.insert(lakshman);

			printFacts(kieSession1);
			correlationTransactions.commit();
		} catch (Exception e) {
			LOGGER.error("Error in KieSession 1 Operations: {}", e.getLocalizedMessage());
		}

		/** Scenario 2 */
		UserTransaction correlationTransactions2;
		try {
			correlationTransactions2 = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
			correlationTransactions2.begin();

			kieSession2 = reloadKieSession(kieSession1.getIdentifier());
			LOGGER.info("Reload Session: {}. Reloaded session id: {}", kieSession1.getIdentifier(),
					kieSession2.getIdentifier());

			kieSession2.insert(hanuman);

			kieSession2.fireAllRules();
			printFacts(kieSession2);
			correlationTransactions2.commit();
		} catch (Exception e) {
			LOGGER.error("Error in KieSession 2 Operations: {}", e.getLocalizedMessage());
		}

		/** Scenario 3 */
		UserTransaction correlationTransactions3;
		try {
			correlationTransactions3 = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
			correlationTransactions3.begin();
			kieSession3 = getNewKieSession();
			LOGGER.info("Another new session: {}", kieSession3.getIdentifier());
			kieSession3.insert(ravana);
			kieSession3.fireAllRules();
			printFacts(kieSession3);
			correlationTransactions3.commit();
		} catch (Exception e) {
			LOGGER.error("Error in KieSession 3 Operations: {}", e.getLocalizedMessage());
		}
		while (true) {

		}
	}

	private static void printFacts(KieSession kieSession) {
		LOGGER.info("Total inserts at KieSession: {}", kieSession.getFactCount());
		Collection<FactHandle> factHandles = kieSession.getFactHandles();
		for (FactHandle handles : factHandles) {
			LOGGER.info(handles.toString());
		}
	}

	private static KieSession reloadKieSession(long identifier) {
		return kieServices.getStoreServices().loadKieSession(identifier, getKieBase(), null, getKieEnvironment());
	}

	private static KieSession getNewKieSession() {
		return kieServices.getStoreServices().newKieSession(getKieBase(), null, getKieEnvironment());
	}

	private static void initializeDataSource() {
		PoolingDataSource ds = new PoolingDataSource();
		ds.setUniqueName(CORRELATION_DROOLS_TRANSACTIONS);
		ds.setClassName(properties.getProperty("pooling.data.source.name"));
		ds.setMaxPoolSize(Integer.parseInt(properties.getProperty("pooling.size")));
		ds.setAllowLocalTransactions(true);
		ds.getDriverProperties().put("user", properties.getProperty("db.user.name"));
		ds.getDriverProperties().put("password", properties.getProperty("db.user.password"));
		ds.getDriverProperties().put("url", properties.getProperty("db.url"));
		ds.init();
	}

	private static Environment getKieEnvironment() {
		Environment env = kieServices.newEnvironment();
		env.set(EnvironmentName.ENTITY_MANAGER_FACTORY,
				Persistence.createEntityManagerFactory("org.drools.persistence.jpa"));
		env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
		return env;
	}

	private static KieBase getKieBase() {
		KieContainer kieClasspathContainer = kieServices.getKieClasspathContainer();
		return kieClasspathContainer.getKieBase("rules");
	}

	private static void loadProperties() {
		try (InputStream input = WorkingWithMutlipleSessions.class.getClassLoader()
				.getResourceAsStream("config.properties")) {
			properties = new Properties();
			properties.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	//use this method to store sessionId into seperate table
	private static void persistSessionDetails(KieSession kieSession) {
		EntityManagerFactory emf = (EntityManagerFactory) kieSession.getEnvironment()
				.get(EnvironmentName.ENTITY_MANAGER_FACTORY);
		EntityManager em = emf.createEntityManager();
		SessionInfoDetails details = new SessionInfoDetails();
		details.setId(123L);
		details.setSessionId(kieSession.getIdentifier());
		details.setSession("SessionToStore");
		em.persist(details);
		em.flush();
	}
}
