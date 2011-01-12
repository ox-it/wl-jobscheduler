package org.sakaiproject.component.app.scheduler.jobs;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * <p>This job finds all sites that have been softly deleted and checks their deletion time.
 * If it is older than site.soft.deletion.gracetime, they are then really deleted. The value is in days
 * and defaults to 30 if not set.</p>
 * 
 * <p>This does not take into account whether or not site.soft.deletion is enabled since there may be sites
 * which have been softly deleted but then the param is disabled, leaving them in limbo.</p>
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class SoftSiteDeletionJob implements Job {

	private static final Log log = LogFactory.getLog(SoftSiteDeletionJob.class);
	
	private final int GRACETIME_DEFAULT = 30;
	
	private SecurityAdvisor securityAdvisor;
	
	
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		log.info("SoftSiteDeletionJob started.");
		
		//get the gracetime config param
		int gracetime = serverConfigurationService.getInt("site.soft.deletion.gracetime", GRACETIME_DEFAULT);
		
		//get calendar for gracetime
		Calendar grace = Calendar.getInstance();
		grace.add(Calendar.DATE, -gracetime);
		log.debug("now: " + grace.getTimeInMillis());
		
		//get sites
		List<Site> sites = siteService.getSoftlyDeletedSites();
		log.info(sites.size() + " softly deleted site(s) will be processed");
		
		//foreach site, check soft deletion time
		//Note: we could do this in the SQL so we only get a list of sites that all need to be deleted.
		//but this would no doubt be db specific so would add extra complexity to the SQL layer
		//for now, just do it in code. There won't be many sites to process at once.
		for(Site s: sites) {
			log.info(s.getTitle());
			
			if(!s.isSoftlyDeleted()){
				continue;
			}
			
			//get calendar for the softly deleted date
			Date date = s.getSoftlyDeletedDate();
			Calendar cal=Calendar.getInstance();
		    cal.setTime(date);
		    log.debug("cal: " + cal.getTimeInMillis());
			
			//if this calendar date is past the gracetime, delete the site.
		    if(cal.getTimeInMillis() <= grace.getTimeInMillis()){
		    	log.info("Site: " + s.getId() + " is due for deletion");
		    	
		    	try {
					enableSecurityAdvisor();
		    		
					siteService.removeSite(s);
					log.info("Removed site: " + s.getId());
					
				} catch (PermissionException e) {
					log.error("Error removing site: " + e.getClass() + ":" + e.getMessage());
				} finally {
					disableSecurityAdvisor();
				}
		    }
		}
	}
	
	/**
	 * Setup a security advisor for this transaction
	 */
	private void enableSecurityAdvisor() {
		
		securityAdvisor = new SecurityAdvisor(){
			public SecurityAdvice isAllowed(String userId, String function, String reference){
				  return SecurityAdvice.ALLOWED;
			}
		};
		
		securityService.pushAdvisor(securityAdvisor);
	}

	/**
	 * Remove security advisor 
	 */
	private void disableSecurityAdvisor(){
		securityService.popAdvisor();
	}
	
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	private SiteService siteService;
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	private SecurityService securityService;
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
}
