package org.sakaiproject.component.app.scheduler.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.sakaiproject.event.api.UsageSessionService;

/**
 * Job to cleanup old sessions from the database.
 * Outside of the session service as when merging into K1 jobscheduler won't be available.
 * @author buckett
 *
 */
public class SessionCleanupJob implements StatefulJob {

	private UsageSessionService usageSessionService;

	public void setUsageSessionService(UsageSessionService usageSessionService) {
		this.usageSessionService = usageSessionService;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		usageSessionService.cleanupSessions();
	}

}
