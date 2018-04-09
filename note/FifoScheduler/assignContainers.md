#### 2. FifoScheduler.assigncontainers()
- 由NODE_UPDATED事件触发:
```
|-> NODE_UPDATE
-> nodeUpdate
-> assignContainers: 两层for, 遍历所有app的当前appAttempt, 然后依次处理每个appAttempt的所有SchedulerRequestKey.
-> assignContainersOnNode
  -> assignNodeLocalContainers(NodeLocal/RackLocal/OffSwitch)
    ->| assignContainer: 构造并分配container
```

```java
/**
 * Heart of the scheduler...
 * @param node node on which resources are available to be allocated
 */
private void assignContainers(FiCaSchedulerNode node){
  for application in applications:
    appAttempt = application.getCurrentAppAttempt()
    for schedulerKey in appAttempt.getSchedulerKeys():
      assignContainersOnNode(node, appAttempt, schedulerKey)
}
```

```java
private int assignContainer(FiCaSchedulerNode node, FifoAppAttempt application,
    SchedulerRequestKey schedulerKey, int assignableContainers,
    Resource capability, NodeType type) {
  // Create the container

  // Inform the application,  每成功分配一个container, 相应请求的numContainers就减1.
  application.allocate(......)

  // Inform the node

  // Increase used resource
}
```

```java
// FifoAppAttempt.java
public RMContainer allocate(NodeType type, FiCaSchedulerNode node,
    SchedulerRequestKey schedulerKey, Container container) {

  // Create RMContainer

  // Add it to allContainers list.

  // 更新队列的资源使用情况以及相关的resourceRequest.
  List<ResourceRequest> resourceRequestList = appSchedulingInfo.allocate(
      type, node, schedulerKey, container);

  // Update resource requests related to "request" and store in RMContainer

  // Inform the container
}

```

##### 2.1 关于NodeType
NODE_LOCAL, RACK_LOCAL, OFF_SWITCH三种类型的资源请求存在一种范围上的包含关系:NODE_LOCAL包含于RACK_LOCAL, RACK_LOCAL包含于OFF_SWITCH.
体现在代码上, 就是, 一个NODE_LOCAL类型的请求必定伴有相应的RACK_LOCAL和OFF_SWITCH类型的请求, 一个RACK_LOCAL类型的请求也一定伴有相应的OFF_SWITCH类型的请求.
在FifoScheduler的assignNodeLocalContainers,assignRackLocalContainer,assignOffSwitchContainer以及LocalitySchedulingPlacementSet的allocateNodeLocal,allocateRackLocal,allocateOffSwitch等函数都有体现.
