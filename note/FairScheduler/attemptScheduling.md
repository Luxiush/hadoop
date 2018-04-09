## FairScheduler.attemptScheduling()
- 由NODE_UPDATE事件触发
- 先分配预留的, 再考虑从队列中选择app.


### assignPreemptedContainers()
- 每个node中用一个map记录从别的地方抢占到预留给对应app的资源, 在assignPreemptedContainers中将其以container的形式分配给app
```java
  // Stores amount of resources preempted and reserved for each app
  @VisibleForTesting
  final Map<FSAppAttempt, Resource>
      resourcesPreemptedForApp = new LinkedHashMap<>();
```


### FSAppAttempt.assignContainer()
```java
private Resource assignContainer(FSSchedulerNode node, boolean reserved);
```


#### DelayScheduling
```java
/**
 * Delay scheduling: We often want to prioritize scheduling of node-local
 * containers over rack-local or off-switch containers. To achieve this
 * we first only allow node-local assignments for a given priority level,
 * then relax the locality threshold once we've had a long enough period
 * without successfully scheduling. We measure both the number of "missed"
 * scheduling opportunities since the last container was scheduled
 * at the current allowed level and the time since the last container
 * was scheduled. Currently we use only the former.
 */
private final Map<SchedulerRequestKey, NodeType> allowedLocalityLevel =
      new HashMap<>();

/**
 * Time of the last container scheduled at the current allowed level
 */
protected Map<SchedulerRequestKey, Long> lastScheduledContainer =
    new ConcurrentHashMap<>();
```
- 用allowedLocalityLevel记录当前各个SchedulerRequestKey的"localityLevel". 先将allowedLocalityLevel限制为NODE_LOCAL, 如果在规定时间内还没有分配到container, 则松弛为RACK_LOCAL. 一段时间后还是没有分配到container, 则继续松弛为OFF_SWITCH.
- 当然前提是对应的ResourceRequest允许松弛, 即"ResourceRequest.relaxLocality"为True(默认值).
- 在FSAppAttempt中用lastScheduledContainer来记录SchedulerRequestKey在当前"LocalityLevel"下上次分配到container的时间.



### assignContainer() [2]
```java
private Resource assignContainer(
      FSSchedulerNode node, PendingAsk pendingAsk, NodeType type,
      boolean reserved, SchedulerRequestKey schedulerKey)
```   

- 尝试在node上分配container, 如果node的可用资源不够, 则先分配一个reservation.


### assignReservedContainer()



### queueMgr.getRootQueue().assignContainer(node)

#### QueueManager




接着看FiarScheduler 还是 直接看论文, 设计一个Scheduler ? 还是对已有调度器做改进 ?
现在时间太零散.


抢占? 预留? 如何区别

.
