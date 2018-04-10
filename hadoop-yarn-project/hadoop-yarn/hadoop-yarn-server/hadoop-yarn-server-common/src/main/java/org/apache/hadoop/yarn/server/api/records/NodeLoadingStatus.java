/* Author: lxs */

package org.apache.hadoop.yarn.server.api.records;

import org.apache.hadoop.util.SysInfo;
import org.apache.hadoop.yarn.util.Records;

/**
 * 保存节点的负载状况信息
 */
public abstract class NodeLoadingStatus {
	
	public static NodeLoadingStatus newInstance(SysInfo sysInfo){
		NodeLoadingStatus status = Records.newRecord(NodeLoadingStatus.class);
		status.init(sysInfo);
		return status;
	}
	
	public static NodeLoadingStatus newInstance(){
		NodeLoadingStatus status = Records.newRecord(NodeLoadingStatus.class);
		return status;
	}
	
	public abstract void init(SysInfo sysInfo);
	
	public abstract long getPhysialMemorySize();
  public abstract int getNumProcessors();
  public abstract long getCpuFrequency();
  public abstract float getMemoryUsagePercentage();
  public abstract float getCpuUsagePercentage();
}