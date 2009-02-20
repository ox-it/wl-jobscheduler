package org.sakaiproject.component.app.scheduler.jobs;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Class to load all the sites, check if they have a provided group and if so refresh
 * the authz groups. We do this through authz groups so we don't load the site objects.
 * @author buckett
 *
 */
public class AuthzGroupProviderSync implements StatefulJob {

	private static final Log log = LogFactory.getLog(AuthzGroupProviderSync.class);
	
	// If it's been modified in the last hour ignore it.
	private long refreshAge = 7200000;
	
	private SessionManager sessionManager;
	private AuthzGroupService authzGroupService;

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		Session session = sessionManager.getCurrentSession();
		session.setUserEid("admin");
		session.setUserId("admin");
		int groupsTotal = 0, groupsProcessed = 0, groupsUpdated = 0, groupsNoProvider = 0, groupsTooNew = 0;
		long start = System.currentTimeMillis();
		try {
			List<AuthzGroup> groups = authzGroupService.getAuthzGroups(null, null);
			groupsTotal = groups.size();
			for (AuthzGroup group: groups) {
				groupsProcessed++;
				if (group.getProviderGroupId() != null && group.getProviderGroupId().length() > 0) {
					if (System.currentTimeMillis() - group.getModifiedTime().getTime() > refreshAge) {
						try {
							authzGroupService.save(group);
							groupsUpdated++;
						} catch (GroupNotDefinedException e) {
							log.warn("Failed to update group ("+ group.getReference()+ "), maybe deleted while processing");
						} catch (AuthzPermissionException e) {
							log.error("Lack of permission to update group: "+ group.getReference());
							throw new JobExecutionException(e);
						}
					} else {
						groupsTooNew++;
						if (log.isDebugEnabled()) {
							log.debug("Ignored group as it has been updated too recently: "+ group.getReference());
						}
					}
				} else {
					groupsNoProvider++;
					if (log.isDebugEnabled()) {
						log.debug("Ignored group as it doesn't have any provided groups: "+ group.getReference());
					}
				}
			}
		} finally {
			long duration = System.currentTimeMillis() - start;
			log.info("Summary (duration: "+ duration+ ") -"+
					" Total: "+ groupsTotal+
					" Processed: "+ groupsProcessed+
					" Updated: "+ groupsUpdated+
					" No Provider: "+ groupsNoProvider+
					" Too New: "+ groupsTooNew
					);
			session.invalidate();
		}
	}

	public void setRefreshAge(long refreshAge) {
		this.refreshAge = refreshAge;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}
}
