package org.sakaiproject.component.app.scheduler.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;
import org.sakaiproject.component.cover.ComponentManager;

public class ScheduledInvocationJob implements StatefulJob {

	private static final Log LOG = LogFactory.getLog(ScheduledInvocationJob.class);


	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@SuppressWarnings("unchecked")
	public void execute(JobExecutionContext jobContext) throws JobExecutionException {

		String contextId = jobContext.getMergedJobDataMap().getString("contextId");
		String componentId = jobContext.getJobDetail().getName();

		try {
			ScheduledInvocationCommand command = (ScheduledInvocationCommand) ComponentManager.get(componentId);
			command.execute(contextId);
		} catch (Exception e) {
			LOG.error("Failed to execute component: [" + componentId + "]: ", e);
		}
	}

}
