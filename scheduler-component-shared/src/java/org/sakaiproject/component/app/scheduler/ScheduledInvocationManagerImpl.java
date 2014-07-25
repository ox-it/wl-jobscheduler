package org.sakaiproject.component.app.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.sakaiproject.api.app.scheduler.DelayedInvocation;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.component.app.scheduler.jobs.ScheduledInvocationJob;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.time.api.Time;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ScheduledInvocationManagerImpl implements ScheduledInvocationManager {

	private static final Log LOG = LogFactory.getLog(ScheduledInvocationManagerImpl.class);
	public static final String GROUP_NAME = "org.sakaiproject.component.app.scheduler.jobs.ScheduledInvocationJob";


	/** Dependency: IdManager */
	protected IdManager m_idManager = null;

	public void setIdManager(IdManager service) {
		m_idManager = service;
	}

	/** Dependency: SchedulerManager */
	protected SchedulerManager m_schedulerManager = null;

	public void setSchedulerManager(SchedulerManager service) {
		m_schedulerManager = service;
	}




	public void init() {
		LOG.info("init()");
    }

   public void destroy() {
      LOG.info("destroy()");
   }

	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.scheduler.ScheduledInvocationManager#createDelayedInvocation(org.sakaiproject.time.api.Time, java.lang.String, java.lang.String)
	 */
	public String createDelayedInvocation(Time time, String componentId, String opaqueContext) {
		try {
			String uuid = m_idManager.createUuid();
			Scheduler scheduler = m_schedulerManager.getScheduler();
			JobDetail detail =
					scheduler.getJobDetail(componentId, GROUP_NAME);
			if (detail == null) {
				detail = new JobDetail(componentId, GROUP_NAME, ScheduledInvocationJob.class);
				detail.setDurability(true);
				scheduler.addJob(detail, false);
			}

			// Non-repeating trigger.
			Trigger trigger = new SimpleTrigger(uuid, GROUP_NAME, new Date(time.getTime()));
			trigger.getJobDataMap().put("contextId", opaqueContext);
			trigger.setJobName(componentId);
			trigger.setJobGroup(GROUP_NAME);

			scheduler.scheduleJob(trigger);
			LOG.info("Created new Delayed Invocation: uuid=" + uuid);
			return uuid;
		} catch (SchedulerException se) {
			LOG.error("Failed to create new Delayed Invocation: componentId=" + componentId +
					", opaqueContext=" + opaqueContext, se);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.scheduler.ScheduledInvocationManager#deleteDelayedInvocation(java.lang.String)
	 */
	public void deleteDelayedInvocation(String uuid) {

		LOG.debug("Removing Delayed Invocation: " + uuid);
		try {
			m_schedulerManager.getScheduler().unscheduleJob(uuid, GROUP_NAME);
		} catch (SchedulerException e) {
			LOG.error("Failed to remove Delayed Invocation: uuid="+ uuid);
		}

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.scheduler.ScheduledInvocationManager#deleteDelayedInvocation(java.lang.String, java.lang.String)
	 */
	public void deleteDelayedInvocation(String componentId, String opaqueContext) {
		LOG.debug("componentId=" + componentId + ", opaqueContext=" + opaqueContext);

		try {
			Scheduler scheduler = m_schedulerManager.getScheduler();
			String[] jobNames = scheduler.getJobNames(GROUP_NAME);
			for (String jobName : jobNames) {
				if (componentId.length() > 0 && !(jobName.equals(componentId))) {
					// If we're filtering by component Id and it doesn't match skip.
					continue;
				}
				JobDetail detail = scheduler.getJobDetail(jobName, GROUP_NAME);
				if (detail != null) {
					Trigger[] triggers = scheduler.getTriggersOfJob(jobName, GROUP_NAME);
					for (Trigger trigger: triggers) {
						String contextId = trigger.getJobDataMap().getString("contextId");
						if (opaqueContext.length() > 0 && !(opaqueContext.equals(contextId))) {
							// If we're filtering by opaqueContent and it doesn't match skip.
							continue;
						}
						// Unscehdule the trigger.
						scheduler.unscheduleJob(trigger.getName(), trigger.getGroup());
					}
				}
			}
		} catch (SchedulerException se) {
			LOG.error("Failure while attempting to remove invocations matching: componentId=" + componentId + ", opaqueContext=" + opaqueContext, se);
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.scheduler.ScheduledInvocationManager#findDelayedInvocations(java.lang.String, java.lang.String)
	 */
	public DelayedInvocation[] findDelayedInvocations(String componentId, String opaqueContext) {
		LOG.debug("componentId=" + componentId + ", opaqueContext=" + opaqueContext);
		List<DelayedInvocation> invocations = new ArrayList<DelayedInvocation>();
		try {
			Scheduler scheduler = m_schedulerManager.getScheduler();
			String[] jobNames = scheduler.getJobNames(GROUP_NAME);
			for (String jobName : jobNames) {
				if (componentId.length() > 0 && !(jobName.equals(componentId))) {
					// If we're filtering by component Id and it doesn't match skip.
					continue;
				}
				JobDetail detail = scheduler.getJobDetail(jobName, GROUP_NAME);
				if (detail != null) {
					Trigger[] triggers = scheduler.getTriggersOfJob(jobName, GROUP_NAME);
					for (Trigger trigger: triggers) {
						String contextId = trigger.getJobDataMap().getString("contextId");
						if (opaqueContext.length() > 0 && !(opaqueContext.equals(contextId))) {
							// If we're filtering by opaqueContent and it doesn't match skip.
							continue;
						}
						// Add this one to the list.
						invocations.add(new DelayedInvocation(trigger.getName(), trigger.getNextFireTime(), jobName, contextId));
					}
				}
			}
		} catch (SchedulerException se) {
			LOG.error("Failure while attempting to remove invocations matching: componentId=" + componentId + ", opaqueContext=" + opaqueContext, se);
		}
		return invocations.toArray(new DelayedInvocation[]{});
	}
}
