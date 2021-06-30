package com.rav.bhaj.drools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rav.bhaj.drools.model.Student;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

public class NewDroolsSessionOnMariaDb {
	public static final String CORRELATION_DROOLS_TRANSACTIONS = "jdbc/BitronixJTADataSource";
	public static final String DRL_LOCATION = "rules/Grading.drl";
	private static final Logger LOGGER = LoggerFactory.getLogger(NewDroolsSessionOnMariaDb.class);
	private static Properties properties;

	public static void main(String[] args) throws InterruptedException {
		loadProperties();
		initializeDataSource();
		KieServices kieServices = KieServices.Factory.get();
		KieSession kieSession = kieServices.getStoreServices().newKieSession(getKieBase(kieServices), null,
				getKieEnvironment(kieServices));
		UserTransaction correlationTransactions;
		try {
			correlationTransactions = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
			correlationTransactions.begin();
			LOGGER.info("session id: {}", kieSession.getIdentifier());

			Student rama = new Student("123", "rama", "", 71, "SRKVS");
			Student sita = new Student("234", "sita", "", 80, "MHS");

			kieSession.insert(rama);
			kieSession.insert(sita);

			kieSession.fireAllRules();

			LOGGER.info("rama's grade is {} ", rama.getPassGrade());
			LOGGER.info("sita's grade is {} ", sita.getPassGrade());

			Student lakshman = new Student("456", "lakshman", "", 36, "SRKVS");
			Student hanuman = new Student("567", "hanuman", "", 45, "MHS");

			kieSession.insert(lakshman);
			kieSession.insert(hanuman);

			LOGGER.info("Total inserts at KieSession: {}", kieSession.getFactCount());

			Collection<FactHandle> factHandles = kieSession.getFactHandles();
			for (FactHandle handles : factHandles) {
				LOGGER.info(handles.toString());
			}

			correlationTransactions.commit();
		} catch (Exception e) {
			LOGGER.error("Error during kiesession operations: {} ", e.getLocalizedMessage());
		}
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

	private static Environment getKieEnvironment(KieServices kieServices) {
		Environment env = kieServices.newEnvironment();
		env.set(EnvironmentName.ENTITY_MANAGER_FACTORY,
				Persistence.createEntityManagerFactory("org.drools.persistence.jpa"));
		env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
		return env;
	}

	private static KieBase getKieBase(KieServices kieServices) {
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
		kieFileSystem.write(ResourceFactory.newClassPathResource(DRL_LOCATION));
		final KieRepository kieRepository = kieServices.getRepository();

		kieRepository.addKieModule(kieRepository::getDefaultReleaseId);
		KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
		kb.buildAll();
		KieModule kieModule = kb.getKieModule();
		KieContainer kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
		return kieContainer.getKieBase();
	}

	private static void loadProperties() {
		try (InputStream input = NewDroolsSessionOnMariaDb.class.getClassLoader()
				.getResourceAsStream("config.properties")) {
			properties = new Properties();
			properties.load(input);
		} catch (IOException ex) {
			LOGGER.error("Error during configuration loading: {} ", ex.getLocalizedMessage());
		}
	}

}
