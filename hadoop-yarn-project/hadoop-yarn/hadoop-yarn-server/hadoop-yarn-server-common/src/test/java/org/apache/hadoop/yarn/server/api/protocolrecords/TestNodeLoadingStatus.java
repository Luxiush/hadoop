/* Author: lxs */

package org.apache.hadoop.yarn.server.api.protocolrecords;

import org.apache.hadoop.util.SysInfo;
import org.apache.hadoop.yarn.server.api.records.NodeLoadingStatus;
import org.apache.hadoop.yarn.server.api.records.impl.pb.NodeLoadingStatusPBImpl;
import org.junit.Test;
import org.junit.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestNodeLoadingStatus{
	private static final Log LOG = LogFactory.getLog(TestNodeLoadingStatus.class);

	private static SysInfo sysInfo = SysInfo.newInstance();
	
//	@Test
	public void testConstructor(){
		/**
		LOG.info("new instance......");
		StringBuilder logMessage0 = new StringBuilder("");
		logMessage0.append("memSize: "+loadingStatus.getPhysialMemorySize())
							.append(",\ncpuFrequency: "+loadingStatus.getCpuFrequency())
							.append(",\nnumProcessor: "+loadingStatus.getNumProcessors())
							.append(",\nmemUsage: "+loadingStatus.getMemoryUsagePercentage())
							.append(",\ncpuUsage: "+loadingStatus.getCpuUsagePercentage());		
		LOG.info("\nloadingStatus: <<<\n"+logMessage0.toString()+"\n>>>");
		**/
		
		for(int i=0; i<10; i++){
			LOG.info("\n\n-----------i="+i+"-------------");
			NodeLoadingStatus loadingStatus = new NodeLoadingStatusPBImpl();
			
			StringBuilder logMessage = new StringBuilder("");
			
			logMessage.append("memSize: "+loadingStatus.getPhysialMemorySize())
								.append(",\ncpuFrequency: "+loadingStatus.getCpuFrequency())
								.append(",\nnumProcessor: "+loadingStatus.getNumProcessors())
								.append(",\nmemUsage: "+loadingStatus.getMemoryUsagePercentage())
								.append(",\ncpuUsage: "+loadingStatus.getCpuUsagePercentage());
			
			LOG.info("\nloadingStatus: <<<\n"+logMessage.toString()+"\n>>>");
			
			try {
	      // Sleep so we can compute the CPU usage
	      Thread.sleep(2000L);
	    } catch (InterruptedException e) {
	      // do nothing
	    }
		}
	}
	
	
	@Test
	public void testHeartbeat(){
		for(int i=0; i<10; i++){
			LOG.info("\n-----i:"+i+"------");
			NodeLoadingStatus S = NodeLoadingStatus.newInstance(sysInfo);
			
			try {
	      // Sleep so we can compute the CPU usage
	      Thread.sleep(1000L);
	    } catch (InterruptedException e) {
	      // do nothing
	    }
		}
	}
}