package com.modeln.build.entity.test.flex;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.junit.Test;

import com.modeln.build.entity.AbstractEntityIntegrationTest;
import com.modeln.build.entity.auth.Group;
import com.modeln.build.entity.auth.User;
import com.modeln.build.entity.build.Build;
import com.modeln.build.entity.test.flex.FlexTestSuite;
import com.modeln.build.enums.auth.Encryption;
import com.modeln.build.enums.auth.Role;
import com.modeln.build.enums.auth.Permission;
import com.modeln.build.enums.auth.Status;
import com.modeln.build.enums.auth.Title;
import com.modeln.build.enums.build.SupportStatus;
import com.modeln.build.enums.build.SourceVersionControlSystem;

public class FlexTestSuiteIntegrationTest extends AbstractEntityIntegrationTest {
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSimpleCRUDOperationOnLegacyTable() throws Exception {		
		Query queryCountGroups = em_bc.createQuery ("SELECT count(x) FROM Group x");
		Number countGroups = (Number) queryCountGroups.getSingleResult ();
		assertTrue(!countGroups.equals(0));
		
		Query queryFindAllGroup = em_bc.createQuery("SELECT g FROM Group g");
		List<Group> groups = queryFindAllGroup.getResultList();
		assertTrue(countGroups.intValue() == groups.size());
		
		//Count number of Entities
		Query queryCountUsers = em_bc.createQuery ("SELECT count(x) FROM User x");
		Number countUsers = (Number) queryCountUsers.getSingleResult ();
		assertTrue(!countUsers.equals(0));
		
		//Retrieve Entities
		Query queryFindAllUser = em_bc.createQuery("SELECT g FROM User g");
		List<User> users = queryFindAllUser.getResultList();
		assertTrue(countUsers.intValue() == users.size());
		
		//Count number of Entities
		Query queryCountBuilds = em_bc.createQuery ("SELECT count(x) FROM Build x");
		Number countBuilds = (Number) queryCountBuilds.getSingleResult ();
		assertTrue(!countBuilds.equals(0));
		
		//Retrieve Entities
		Query queryFindAllBuild = em_bc.createQuery("SELECT g FROM Build g");
		List<Build> builds = queryFindAllBuild.getResultList();
		assertTrue(countBuilds.intValue() == builds.size());
		
		//Count number of Entities
		Query queryCountFlexTestSuites = em_bc.createQuery ("SELECT count(x) FROM FlexTestSuite x");
		Number countFlexTestSuites = (Number) queryCountFlexTestSuites.getSingleResult ();
		assertTrue(!countFlexTestSuites.equals(0));
		
		//Retrieve Entities
		Query queryFindAllFlexTestSuite = em_bc.createQuery("SELECT g FROM FlexTestSuite g");
		queryFindAllFlexTestSuite.setMaxResults(10);
		List<FlexTestSuite> flexTestSuites = queryFindAllFlexTestSuite.getResultList();
		assertTrue(flexTestSuites.size() != 0);
		
		//Create Depend Entities
		Group group = new Group();
		group.setGid((countGroups.intValue() * 10));
		group.setDesc("desc");
		group.setName("group1");
		group.setType(Role.ADMIN);
		Set<Permission> permEdit = new HashSet<Permission>();
		permEdit.add(Permission.EDIT);
		group.setPermGroup(permEdit);
		Set<Permission> permEditAndDelete = new HashSet<Permission>();
		permEditAndDelete.add(Permission.EDIT);
		permEditAndDelete.add(Permission.DELETE);
		group.setPermUser(permEditAndDelete);
		group.setPermSelf(permEdit);
		group.setPermListing(permEditAndDelete);
		
		// Creates Dependent Entity
		User user = new User();
		user.setUsername("username");
		user.setFirstName("Firstname");
		user.setLastName("Lastname");
		user.setCountry("us");
		user.setLanguage("en");
		user.setAccountStatus(Status.ACTIVE);
		user.setEmailAddress("email@company.com");
		user.setPassword("password");
		user.setPasswordEncryption(Encryption.CRYPT);
		user.setTitle(Title.MR);
		user.setPrimaryGroup(group);
		user.setLoginSuccess(new Date());
		user.setLoginFailure(new Date());
		user.setLoginFailureCount(2);
		
		// Creates Depend Entity
		Build build = new Build();
		build.setBuildComments("buildComments");
		Set<com.modeln.build.enums.build.Status> buildStatus = new HashSet<com.modeln.build.enums.build.Status>();
		buildStatus.add(com.modeln.build.enums.build.Status.PASSING);
		build.setBuildStatus(buildStatus);
		build.setSupportStatus(SupportStatus.ACTIVE);
		build.setComments("comments");
		build.setDownloadUri("downloadUri");
		build.setStartTime(new Date());
		build.setEndTime(new Date());
		build.setHostname("hostname");
		build.setJdkVendor("jdkVendor");
		build.setJdkVersion("jdkVersion");
		build.setJobUrl("jobUrl");
		build.setOsArch("osArch");
		build.setOsName("osName");
		build.setOsVersion("osVersion");
		build.setStatus("status");
		build.setKeyAlgorithm("keyAlgorithm");
		build.setVerPublicKey("verPublicKey");
		build.setVerPrivateKey("verPrivateKey");
		build.setUser(user);
		build.setUsername("username");
		build.setVersion("version");
		build.setVersionControlId("versionControlId");
		build.setVersionControlRoot("versionControlRoot");
		build.setVersionControlType(SourceVersionControlSystem.GIT);
		
		// Creates Entity
		FlexTestSuite flexTestSuite = new FlexTestSuite();
		flexTestSuite.setBuild(build);
		flexTestSuite.setEnvName("envName");
		flexTestSuite.setGroupId(1l);
		flexTestSuite.setGroupName("groupName");
		flexTestSuite.setHostname("hostname");
		flexTestSuite.setJdbcUrl("jdbcUrl");
		flexTestSuite.setJdkVendor("jdkVendor");
		flexTestSuite.setJdkVersion("jdkVersion");
		flexTestSuite.setMaxThreads(3);
		flexTestSuite.setName("name");
		flexTestSuite.setOsArch("osArch");
		flexTestSuite.setOsName("osName");
		flexTestSuite.setOsVersion("osVersion");
		flexTestSuite.setStartDate(new Date());
		flexTestSuite.setEndDate(new Date());
		flexTestSuite.setSuiteOptions("suiteOptions");
		flexTestSuite.setTestCount(1);
		flexTestSuite.setUsername("username");
		flexTestSuite.setFailureSrc("failureSrc");
		
		// Persists to the database
		try{
		tx_bc.begin();
		em_bc.persist(group);
		em_bc.persist(user);
		em_bc.persist(build);
		em_bc.persist(flexTestSuite);
		tx_bc.commit();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		assertNotNull("Group ID should not be null", group.getId());
		assertNotNull("User ID should not be null", user.getId());
		assertNotNull("Build ID should not be null", build.getId());
		assertNotNull("FlexTestSuite ID should not be null", flexTestSuite.getId());
		
		flexTestSuite = em_bc.find(FlexTestSuite.class, flexTestSuite.getId());
		assertTrue(flexTestSuite.getBuild().getId() == build.getId());
		assertTrue(flexTestSuite.getEnvName().equals("envName"));
		assertTrue(flexTestSuite.getGroupId().equals(1l));
		assertTrue(flexTestSuite.getGroupName().equals("groupName"));
		assertTrue(flexTestSuite.getHostname().equals("hostname"));
		assertTrue(flexTestSuite.getJdbcUrl().equals("jdbcUrl"));
		assertTrue(flexTestSuite.getJdkVendor().equals("jdkVendor"));
		assertTrue(flexTestSuite.getJdkVersion().equals("jdkVersion"));
		assertTrue(flexTestSuite.getMaxThreads().equals(3));
		assertTrue(flexTestSuite.getName().equals("name"));
		assertTrue(flexTestSuite.getOsArch().equals("osArch"));
		assertTrue(flexTestSuite.getOsName().equals("osName"));
		assertTrue(flexTestSuite.getOsVersion().equals("osVersion"));
		assertTrue(flexTestSuite.getStartDate() != null);
		assertTrue(flexTestSuite.getEndDate() != null);
		assertTrue(flexTestSuite.getSuiteOptions().equals("suiteOptions"));
		assertTrue(flexTestSuite.getTestCount().equals(1));
		assertTrue(flexTestSuite.getUsername().equals("username"));
		assertTrue(flexTestSuite.getFailureSrc().equals("failureSrc"));
		
		//Update Entity	
		flexTestSuite.setName("updateName");
		tx_bc.begin();
		em_bc.persist(flexTestSuite);	
		tx_bc.commit();
		
		flexTestSuite = em_bc.find(FlexTestSuite.class, flexTestSuite.getId());
		assertTrue(flexTestSuite.getBuild().getId() == build.getId());
		assertTrue(flexTestSuite.getEnvName().equals("envName"));
		assertTrue(flexTestSuite.getGroupId().equals(1l));
		assertTrue(flexTestSuite.getGroupName().equals("groupName"));
		assertTrue(flexTestSuite.getHostname().equals("hostname"));
		assertTrue(flexTestSuite.getJdbcUrl().equals("jdbcUrl"));
		assertTrue(flexTestSuite.getJdkVendor().equals("jdkVendor"));
		assertTrue(flexTestSuite.getJdkVersion().equals("jdkVersion"));
		assertTrue(flexTestSuite.getMaxThreads().equals(3));
		assertTrue(flexTestSuite.getName().equals("updateName"));
		assertTrue(flexTestSuite.getOsArch().equals("osArch"));
		assertTrue(flexTestSuite.getOsName().equals("osName"));
		assertTrue(flexTestSuite.getOsVersion().equals("osVersion"));
		assertTrue(flexTestSuite.getStartDate() != null);
		assertTrue(flexTestSuite.getEndDate() != null);
		assertTrue(flexTestSuite.getSuiteOptions().equals("suiteOptions"));
		assertTrue(flexTestSuite.getTestCount().equals(1));
		assertTrue(flexTestSuite.getUsername().equals("username"));
		assertTrue(flexTestSuite.getFailureSrc().equals("failureSrc"));
		
		//Delete Entity
		tx_bc.begin();
		em_bc.remove(flexTestSuite);
		em_bc.remove(build);
		em_bc.remove(user);
		em_bc.remove(group);
		tx_bc.commit();
		
		Query queryFindAllBuilds = em_bc.createQuery("SELECT u FROM Build u");
		builds = queryFindAllBuilds.getResultList();
		assertTrue(builds.size() == countBuilds.intValue());	
		
		Query queryFindAllUsers = em_bc.createQuery("SELECT u FROM User u");
		users = queryFindAllUsers.getResultList();
		assertTrue(users.size() == countUsers.intValue());	
		
		Query queryFindAllGroups = em_bc.createQuery("SELECT u FROM Group u");
		groups = queryFindAllGroups.getResultList();
		assertTrue(groups.size() == countGroups.intValue());
		
		Query queryCountFlexTestSuiteAfter = em_bc.createQuery ("SELECT count(x) FROM FlexTestSuite x");
		Number countFlexTestSuiteAfter = (Number) queryCountFlexTestSuiteAfter.getSingleResult ();
		assertTrue(countFlexTestSuites.intValue() == countFlexTestSuiteAfter.intValue());	
	}
}
