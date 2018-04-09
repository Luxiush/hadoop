/* Author: lxs */

package org.apache.hadoop.yarn.server.api.records;


/**
 * 保存节点的负载状况信息
 */
public abstract class NodeLoadingStatus {
	
	public abstract void update();
	
	public abstract long getPhysialMemorySize();
  public abstract int getNumProcessors();
  public abstract long getCpuFrequency();
  public abstract float getMemoryUsagePercentage();
  public abstract float getCpuUsagePercentage();
}