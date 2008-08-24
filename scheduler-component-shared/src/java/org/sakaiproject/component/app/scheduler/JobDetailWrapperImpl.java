/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.component.app.scheduler;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.sakaiproject.api.app.scheduler.JobDetailWrapper;
import org.sakaiproject.api.app.scheduler.JobBeanWrapper;

public class JobDetailWrapperImpl implements JobDetailWrapper
{
  private JobDetail jobDetail;
  private boolean isSelected = false;
  private List triggerWrapperList;
  private Integer triggerCount;

  private static final Log LOG = LogFactory.getLog(JobDetailWrapperImpl.class);

  public JobDetailWrapperImpl()
  {
  }

  /**
   * @return Returns the triggerCount.
   */
  public Integer getTriggerCount()
  {
    return triggerCount;
  }

  /**
   * @param triggerCount The triggerCount to set.
   */
  public void setTriggerCount(Integer triggerCount)
  {
    this.triggerCount = triggerCount;
  }

  /**
   * @return Returns the triggerWrapperList.
   */
  public List getTriggerWrapperList()
  {
    return triggerWrapperList;
  }

  /**
   * @param triggerWrapperList The triggerWrapperList to set.
   */
  public void setTriggerWrapperList(List triggerWrapperList)
  {
    this.triggerCount = new Integer(triggerWrapperList.size());
    this.triggerWrapperList = triggerWrapperList;
  }

  /**
   * @return Returns the jobDetail.
   */
  public JobDetail getJobDetail()
  {
    return jobDetail;
  }

  /**
   * @param jobDetail The jobDetail to set.
   */
  public void setJobDetail(JobDetail jobDetail)
  {
    this.jobDetail = jobDetail;
  }

  /**
   * @return Returns the isSelected.
   */
  public boolean getIsSelected()
  {
    return isSelected;
  }

  /**
   * @param isSelected The isSelected to set.
   */
  public void setIsSelected(boolean isSelected)
  {
    this.isSelected = isSelected;
  }

   public String getJobType() {
      String jobType = (String) getJobDetail().getJobDataMap().get(JobBeanWrapper.JOB_TYPE);
      if (jobType != null) {
         return jobType;
      }
      return getJobDetail().getJobClass().getName();
   }
}