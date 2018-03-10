package org.apache.hadoop.yarn.server.resourcemanager.scheduler.fifo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.net.NetworkTopology;
import org.apache.hadoop.yarn.api.protocolrecords.AllocateResponse;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerState;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.QueueInfo;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.ResourceOption;
import org.apache.hadoop.yarn.api.records.ResourceRequest;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.event.AsyncDispatcher;
import org.apache.hadoop.yarn.event.Dispatcher;
import org.apache.hadoop.yarn.event.Event;
import org.apache.hadoop.yarn.event.EventHandler;
import org.apache.hadoop.yarn.event.InlineDispatcher;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.exceptions.YarnRuntimeException;
import org.apache.hadoop.yarn.factories.RecordFactory;
import org.apache.hadoop.yarn.factory.providers.RecordFactoryProvider;
import org.apache.hadoop.yarn.server.api.protocolrecords.UpdateNodeResourceRequest;
import org.apache.hadoop.yarn.server.resourcemanager.Application;
import org.apache.hadoop.yarn.server.resourcemanager.MockAM;
import org.apache.hadoop.yarn.server.resourcemanager.MockNM;
import org.apache.hadoop.yarn.server.resourcemanager.MockNodes;
import org.apache.hadoop.yarn.server.resourcemanager.MockRM;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.RMContextImpl;
import org.apache.hadoop.yarn.server.resourcemanager.ResourceManager;
import org.apache.hadoop.yarn.server.resourcemanager.Task;
import org.apache.hadoop.yarn.server.resourcemanager.ahs.RMApplicationHistoryWriter;
import org.apache.hadoop.yarn.server.resourcemanager.metrics.SystemMetricsPublisher;
import org.apache.hadoop.yarn.server.resourcemanager.nodelabels.NullRMNodeLabelsManager;
import org.apache.hadoop.yarn.server.resourcemanager.nodelabels.RMNodeLabelsManager;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.RMApp;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.RMAppImpl;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.attempt.RMAppAttempt;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.attempt.RMAppAttemptImpl;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.attempt.RMAppAttemptMetrics;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeResourceUpdateEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.Allocation;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.ContainerUpdates;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.QueueMetrics;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.ResourceScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerAppReport;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerApplicationAttempt;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerNode;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerNodeReport;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.TestSchedulerUtils;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacitySchedulerConfiguration;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.AppAddedSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.AppAttemptAddedSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeAddedSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeRemovedSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeResourceUpdateSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeUpdateSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.SchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.security.NMTokenSecretManagerInRM;
import org.apache.hadoop.yarn.server.resourcemanager.security.RMContainerTokenSecretManager;
import org.apache.hadoop.yarn.server.utils.BuilderUtils;
import org.apache.hadoop.yarn.util.resource.Resources;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestFifoScheduler2 {	
  private static final Log LOG = LogFactory.getLog(TestFifoScheduler.class);
  private final int GB = 1024;

  private ResourceManager resourceManager = null;
  private static Configuration conf;
  private static final RecordFactory recordFactory = 	
      RecordFactoryProvider.getRecordFactory(null);

  private final static ContainerUpdates NULL_UPDATE_REQUESTS =
      new ContainerUpdates();
  
  private static int setUpCnt = 0;
  private static int tearDownCnt = 0;

  @Before
  public void setUp() throws Exception {
  	LOG.info("\n>>>>>> setUp " + (++setUpCnt));
    conf = new Configuration();
    conf.setClass(YarnConfiguration.RM_SCHEDULER,
        FifoScheduler.class, ResourceScheduler.class);
    resourceManager = new MockRM(conf);
    LOG.info("\nsetUp " + setUpCnt + " Done<<<<<<");
  }

  @After
  public void tearDown() throws Exception {
  	LOG.info("\n>>>>>> tearDown " + (++tearDownCnt));
    resourceManager.stop();
    LOG.info("\ntearDown " + tearDownCnt + " Done<<<<<<");
  }
  
//通过NODE_ADDED事件向RM注册一个节点
 private org.apache.hadoop.yarn.server.resourcemanager.NodeManager
     registerNode(String hostName, int containerManagerPort, int nmHttpPort,
         String rackName, Resource capability) throws IOException,
         YarnException {
   org.apache.hadoop.yarn.server.resourcemanager.NodeManager nm =
       new org.apache.hadoop.yarn.server.resourcemanager.NodeManager(hostName,
           containerManagerPort, nmHttpPort, rackName, capability,
           resourceManager);
   
   NodeAddedSchedulerEvent nodeAddEvent1 =
       new NodeAddedSchedulerEvent(resourceManager.getRMContext().getRMNodes()
           .get(nm.getNodeId()));
   resourceManager.getResourceScheduler().handle(nodeAddEvent1);
   return nm;
 }
 
 private ApplicationAttemptId createAppAttemptId(int appId, int attemptId) {
   ApplicationId appIdImpl = ApplicationId.newInstance(0, appId);
   ApplicationAttemptId attId =
       ApplicationAttemptId.newInstance(appIdImpl, attemptId);
   return attId;
 }

 private ResourceRequest createResourceRequest(int memory, String host,
     int priority, int numContainers) {
   ResourceRequest request = recordFactory
       .newRecordInstance(ResourceRequest.class);
   request.setCapability(Resources.createResource(memory));
   request.setResourceName(host);
   request.setNumContainers(numContainers);
   Priority prio = recordFactory.newRecordInstance(Priority.class);
   prio.setPriority(priority);
   request.setPriority(prio);
   return request;
 }
 
 @Test(timeout=5000)
 public void testFifoSchedulerCapacityWhenNoNMs() {
	 LOG.info("\n------Test fifo scheduler capacity when no NMs.");
   FifoScheduler scheduler = new FifoScheduler();
   QueueInfo queueInfo = scheduler.getQueueInfo(null, false, false);
   Assert.assertEquals(0.0f, queueInfo.getCurrentCapacity(), 0.0f);
 }
	
 @Test(timeout=5000)
 public void testAppAttemptMetrics() throws Exception {
	 LOG.info("\n------ testAppAttemptMetrics.");
   AsyncDispatcher dispatcher = new InlineDispatcher();
   
   FifoScheduler scheduler = new FifoScheduler();
   RMApplicationHistoryWriter writer = mock(RMApplicationHistoryWriter.class);
   RMContext rmContext = new RMContextImpl(dispatcher, null,
       null, null, null, null, null, null, null, scheduler);
   ((RMContextImpl) rmContext).setSystemMetricsPublisher(
       mock(SystemMetricsPublisher.class));

   Configuration conf = new Configuration();
   ((RMContextImpl) rmContext).setScheduler(scheduler);
   scheduler.setRMContext(rmContext);
   scheduler.init(conf);
   scheduler.start();
   scheduler.reinitialize(conf, rmContext);
   QueueMetrics metrics = scheduler.getRootQueueMetrics();
   int beforeAppsSubmitted = metrics.getAppsSubmitted();

   ApplicationId appId = BuilderUtils.newApplicationId(200, 1);
   ApplicationAttemptId appAttemptId = BuilderUtils.newApplicationAttemptId(
       appId, 1);

   SchedulerEvent appEvent = new AppAddedSchedulerEvent(appId, "queue", "user");
   scheduler.handle(appEvent);
   SchedulerEvent attemptEvent =
       new AppAttemptAddedSchedulerEvent(appAttemptId, false);
   scheduler.handle(attemptEvent);

   appAttemptId = BuilderUtils.newApplicationAttemptId(appId, 2);
   SchedulerEvent attemptEvent2 =
       new AppAttemptAddedSchedulerEvent(appAttemptId, false);
   scheduler.handle(attemptEvent2);

   int afterAppsSubmitted = metrics.getAppsSubmitted();
   Assert.assertEquals(1, afterAppsSubmitted - beforeAppsSubmitted);
   scheduler.stop();
 }
 
  /*
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	
	}
   */
}
