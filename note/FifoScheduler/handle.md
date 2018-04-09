#### 3. FifoScheduler.handle()
- Scheduler作为一个事件处理器的对外接口.
```java
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
```

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
#### 2. NODE_REMOVED
```java
case NODE_REMOVED:
{
  // 从clusterTracker的HashMap中将对应节点删除, 同时更新cluster的`节点总数`等信息
  NodeRemovedSchedulerEvent nodeRemovedEvent = (NodeRemovedSchedulerEvent)event;
  removeNode(nodeRemovedEvent.getRemovedRMNode());
}
```


---
#### 3. NODE_RESOURCE_UPDATE
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

---
#### 4. NODE_UPDATE
assignContainers.md


---
#### 5. APP_ADDED
```java
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

- 用一个map来记录Scheduler当前需要调度的所有应用
```java
public abstract class AbstractYarnScheduler
    <T extends SchedulerApplicationAttempt, N extends SchedulerNode>
    extends AbstractService implements ResourceScheduler {
  // ...
  protected ConcurrentMap<ApplicationId, SchedulerApplication<T>> applications;
  // ...
}
```


---
#### 6. APP_REMOVED
```java
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


---
#### 7. APP_ATTEMPT_ADDED
```java
/**[lxs]
 * addApplication的时候只是在保存所有需要调用应用信息的applications中
 * 为该applicationId添加一条记录,
 * 还没有为需要调度的应用创建调度实例appAttempt.
 * appAttempt在这里添加.
**/
public synchronized void
    addApplicationAttempt(ApplicationAttemptId appAttemptId,
        boolean transferStateFromPreviousAttempt,
        boolean isAttemptRecovering) {
  SchedulerApplication<FifoAppAttempt> application =
      applications.get(appAttemptId.getApplicationId());
  // ...
  application.setCurrentAppAttempt(schedulerApp);

  // ...
}
```


---
#### 8. APP_ATTEMPT_REMOVED
```java
private synchronized void doneApplicationAttempt(
    ApplicationAttemptId applicationAttemptId,
    RMAppAttemptState rmAppAttemptFinalState, boolean keepContainers)
    throws IOException {
  // Kill all 'live' containers

  // Clean up pending requests, metrics etc.
  attempt.stop(rmAppAttemptFinalState);
}

```


---
#### 9. CONTAINER_EXPIRED
- container超时了, Scheduler将Container分配给了应用, 但是应用过了很久没有来取.
- 代码中哪里会触发这一事件 ???
```java
super.completedContainer(getRMContainer(containerid),
    SchedulerUtils.createAbnormalContainerStatus(
        containerid,
        SchedulerUtils.EXPIRED_CONTAINER),
    RMContainerEventType.EXPIRE);
```


---
#### 10. RELEASE_CONTAINER
- container用完, 将其释放
```java
super.completedContainer(container,
    SchedulerUtils.createAbnormalContainerStatus(
        container.getContainerId(),
        SchedulerUtils.RELEASED_CONTAINER),
    RMContainerEventType.RELEASED);
```
