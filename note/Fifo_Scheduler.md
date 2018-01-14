### FIFO Scheduler
- 作为一个事件处理器需要处理以下事件:
```java
class FifoScheduler{

//...

  @Override
  public void handle(SchedulerEvent event) {
    switch(event.getType()) {
    case NODE_ADDED:
      // ...

    case NODE_REMOVED:
      // ...

    case NODE_RESOURCE_UPDATE:
      // ...

    case NODE_UPDATE:
      // ...

    case APP_ADDED:
      // ...

    case APP_REMOVED:
      // ...

    case APP_ATTEMPT_ADDED:
      // ...

    case APP_ATTEMPT_REMOVED:
      // ...

    case CONTAINER_EXPIRED:
      // ...

    case RELEASE_CONTAINER:
      // ...

    default:
      LOG.error("Invalid eventtype " + event.getType() + ". Ignoring!");
    }
  }
}
````


---
#### 1. NODE_ADDED事件
```java
case NODE_ADDED:
{
  NodeAddedSchedulerEvent nodeAddedEvent = (NodeAddedSchedulerEvent)event;
  /* 往ClusterNodeTracker里面添加一个SchedulerNode,
     同时更新集群的`总容量`,`节点总数`等信息 */
  addNode(nodeAddedEvent.getAddedRMNode());

  /* 根据函数名的意思应该是重新运行节点上的各个容器
     待进一步确认(TODO:) */
  recoverContainersOnNode(nodeAddedEvent.getContainerReports(),
    nodeAddedEvent.getAddedRMNode());
}
```


##### ClusterNodeTracker
-  一个辅助类, 提供操作集群节点的常用方法,
- 一个SchedulerNode的集合,

```java
/**
 - tracks the state of all cluster {@link SchedulerNode}s
 - provides convenience methods to filter and sort nodes
*/
public class ClusterNodeTracker<N extends SchedulerNode>{
  // 用一个HashMap来记录cluster中的各个节点

}
```

##### SchedulerNode
- 对RMNode进行封装, 提供一些适合Scheduler操作的接口

```java
// Represents a YARN Cluster Node from the viewpoint of the scheduler.
public abstract class SchedulerNode {
  public SchedulerNode(RMNode node, boolean usePortForNodeName,
        Set<String> labels) {

  }

  public SchedulerNode(RMNode node, boolean usePortForNodeName) {

  }
}
```

##### FiCaSchedulerNode
```java
public class FiCaSchedulerNode extends SchedulerNode{
  // TODO
}
```


---
### 2. NODE_REMOVED
```java
case NODE_REMOVED:
{
  // 从clusterTracker的HashMap中将对应节点删除, 同时更新cluster的`节点总数`等信息
  NodeRemovedSchedulerEvent nodeRemovedEvent = (NodeRemovedSchedulerEvent)event;
  removeNode(nodeRemovedEvent.getRemovedRMNode());
}
```


---
### 3. NODE_RESOURCE_UPDATE
```java
case NODE_RESOURCE_UPDATE:
{
  /* 先将节点从clusterTracker中删除,
  更新该节点的资源总量后再添加回clusterTracker中.
  (在此之前还会通知NodeLabelsManager) */
  NodeResourceUpdateSchedulerEvent nodeResourceUpdatedEvent =
      (NodeResourceUpdateSchedulerEvent)event;
  updateNodeResource(nodeResourceUpdatedEvent.getRMNode(),
    nodeResourceUpdatedEvent.getResourceOption());
}
```



### 4. NODE_UPDATE
- 触发核心的资源分配机制
- TODO:
```java
case NODE_UPDATE:
{
  NodeUpdateSchedulerEvent nodeUpdatedEvent =
  (NodeUpdateSchedulerEvent)event;
  nodeUpdate(nodeUpdatedEvent.getRMNode());
}
```



### 5. APP_ADDED
```java
case APP_ADDED:
{
  AppAddedSchedulerEvent appAddedEvent = (AppAddedSchedulerEvent) event;
  addApplication(appAddedEvent.getApplicationId(),
    appAddedEvent.getQueue(), appAddedEvent.getUser(),
    appAddedEvent.getIsAppRecovering());
}


public synchronized void addApplication(ApplicationId applicationId,
    String queue, String user, boolean isAppRecovering) {
  SchedulerApplication<FifoAppAttempt> application =
      new SchedulerApplication<>(DEFAULT_QUEUE, user);

  applications.put(applicationId, application);
  metrics.submitApp(user);  // 递归, 表示看不懂 (TODO:)

  LOG.info("...");
  if (isAppRecovering) {
    // ...
  } else {
    rmContext.getDispatcher().getEventHandler()
      .handle(new RMAppEvent(applicationId, RMAppEventType.APP_ACCEPTED));
  }
}

```



### 6. APP_REMOVED
```java
case APP_REMOVED:
{
  AppRemovedSchedulerEvent appRemovedEvent = (AppRemovedSchedulerEvent)event;
  doneApplication(appRemovedEvent.getApplicationID(),
    appRemovedEvent.getFinalState());
}


private synchronized void doneApplication(ApplicationId applicationId,
    RMAppState finalState) {
  SchedulerApplication<FifoAppAttempt> application =
      applications.get(applicationId);
  if (application == null){
    LOG.warn("Couldn't find application " + applicationId);
    return;
  }

  // Inform the activeUsersManager
  activeUsersManager.deactivateApplication(application.getUser(),
    applicationId);
  application.stop(finalState);
  applications.remove(applicationId);
}
```

--- Stop Here 218.1.14 ---
目前只是从handle()函数入手, 依次跟踪每种事件所调用到的函数, 每个函数是干什么的从名字就可以猜出来, 但是其中涉及到的每个类所维护的数据结构还没搞明白.
需要从一个更高点的层次去把握每个类的职责, 以及各个类之间的关系.



### 7. APP_ATTEMPT_ADDED
```java
case APP_ATTEMPT_ADDED:
{
  AppAttemptAddedSchedulerEvent appAttemptAddedEvent =
      (AppAttemptAddedSchedulerEvent) event;
  addApplicationAttempt(appAttemptAddedEvent.getApplicationAttemptId(),
    appAttemptAddedEvent.getTransferStateFromPreviousAttempt(),
    appAttemptAddedEvent.getIsAttemptRecovering());
}
```



### 8. APP_ATTEMPT_REMOVED
```java
case APP_ATTEMPT_REMOVED:
{
  AppAttemptRemovedSchedulerEvent appAttemptRemovedEvent =
      (AppAttemptRemovedSchedulerEvent) event;
  try {
    doneApplicationAttempt(
      appAttemptRemovedEvent.getApplicationAttemptID(),
      appAttemptRemovedEvent.getFinalAttemptState(),
      appAttemptRemovedEvent.getKeepContainersAcrossAppAttempts());
  } catch(IOException ie) {
    LOG.error("Unable to remove application "
        + appAttemptRemovedEvent.getApplicationAttemptID(), ie);
  }
}
```



### 9. CONTAINER_EXPIRED
```java
case CONTAINER_EXPIRED:
{
  ContainerExpiredSchedulerEvent containerExpiredEvent =
      (ContainerExpiredSchedulerEvent) event;
  ContainerId containerid = containerExpiredEvent.getContainerId();
  super.completedContainer(getRMContainer(containerid),
      SchedulerUtils.createAbnormalContainerStatus(
          containerid,
          SchedulerUtils.EXPIRED_CONTAINER),
      RMContainerEventType.EXPIRE);
}
```



### 10. RELEASE_CONTAINER
```java
case RELEASE_CONTAINER:
{
  if (!(event instanceof ReleaseContainerEvent)) {
    throw new RuntimeException("Unexpected event type: " + event);
  }
  RMContainer container = ((ReleaseContainerEvent) event).getContainer();
  completedContainer(container,
      SchedulerUtils.createAbnormalContainerStatus(
          container.getContainerId(),
          SchedulerUtils.RELEASED_CONTAINER),
      RMContainerEventType.RELEASED);
}
```
