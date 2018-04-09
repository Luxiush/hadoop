/* Author: lxs */

package org.apache.hadoop.yarn.server.api.records.impl.pb;

import org.apache.hadoop.util.SysInfo;
import org.apache.hadoop.yarn.proto.YarnServerCommonProtos.NodeLoadingStatusProto;
import org.apache.hadoop.yarn.proto.YarnServerCommonProtos.NodeLoadingStatusProtoOrBuilder;
import org.apache.hadoop.yarn.server.api.records.NodeLoadingStatus;

import com.google.protobuf.TextFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NodeLoadingStatusPBImpl extends NodeLoadingStatus{
	private static final Log LOG = LogFactory.getLog(NodeLoadingStatusPBImpl.class);

	private static SysInfo sysInfo = SysInfo.newInstance();
	private NodeLoadingStatusProto.Builder builder;
	
	public NodeLoadingStatusPBImpl(){
		this.builder = NodeLoadingStatusProto.newBuilder();
		update();
	}

	public NodeLoadingStatusPBImpl(NodeLoadingStatusProto proto) {
		this.builder = NodeLoadingStatusProto.newBuilder(proto);
		update();
	}
	
	public NodeLoadingStatusProto getProto(){
		return this.builder.build();
	}

	@Override
	public void update() {	
		float cpuUsage = sysInfo.getCpuUsagePercentage();		
		long memSize = sysInfo.getPhysicalMemorySize();
		long memUsage = memSize-sysInfo.getAvailablePhysicalMemorySize();
		this.builder.setPhysicalMemorySize(memSize);
		this.builder.setMemoryUsagePercentage((float)memUsage / memSize * 100F);
		this.builder.setCpuUsagePercentage(cpuUsage);
		this.builder.setNumProcessors(sysInfo.getNumProcessors());
		this.builder.setCpuFrequency(sysInfo.getCpuFrequency());

		
		StringBuilder logMessage = new StringBuilder("");
		logMessage.append("memSize: "+memSize)
		.append(",\ncpuFrequency: "+sysInfo.getCpuFrequency())
		.append(",\nnumProcessor: "+sysInfo.getNumProcessors())
		.append(",\nmemUsage: "+memUsage)
		.append(",\ncpuUsage: "+cpuUsage);
		LOG.info("\nsysInfo: <<<\n"+logMessage.toString()+"\n>>>");
	}

	@Override
	public long getPhysialMemorySize() {
		return builder.getPhysicalMemorySize();
	}

	@Override
	public int getNumProcessors() {
		return builder.getNumProcessors();
	}

	@Override
	public long getCpuFrequency() {
		return builder.getCpuFrequency();
	}

	@Override
	public float getMemoryUsagePercentage() {
		return builder.getMemoryUsagePercentage();
	}

	@Override
	public float getCpuUsagePercentage() {
		return builder.getCpuUsagePercentage();
	}
	
	@Override
	public String toString(){
		return TextFormat.shortDebugString(getProto());
	}
}