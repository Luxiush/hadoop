### allocate
#### 1. FifoScheduler.allocate()
- AM通过这一接口将资源的使用情况(需要申请的资源, 不用的container等)更新到scheduler中对应的appAtempt.同时取走appAttempt中暂存的资源.
- 目前已知的AM与Scheduler通信的唯一api.
```java
/**
 * The main api between the ApplicationMaster and the Scheduler.
 * The ApplicationMaster is updating his future resource requirements
 * and may release containers he doens't need.
 *
 * @param appAttemptId
 * @param ask	要请求的资源
 * @param release 将要释放的container
 * @param blacklistAdditions
 * @param blacklistRemovals
 * @param updateRequests
 * @return the {@link Allocation} for the application
 */
@Override
public Allocation allocate(ApplicationAttemptId appAttemptId,
    List<ResourceRequest> ask, List<ContainerId> release,
    List<String> blacklistAdditions, List<String> blacklistRemovals,
  ContainerUpdates updateRequests){
    FifoAppAttempt application = getApplicationAttempt(applicationAttemptId);
    // 释放掉应用不再使用的container
    releaseContainers(release, application);
    // 更新对应appAttempt中的资源请求.
    application.updateResourceRequests(ask);
    // 更新黑名单
    application.updateBlacklist(blacklistAdditions, blacklistRemovals);
    // 将已经分配的资源返回给AM
    return new Allocation(application.pullNewlyAllocatedContainers(),
          headroom, null, null, null, application.pullUpdatedNMTokens());
}
```
- Scheduler分配的container会先保存到AppAttempt中, 之后AM在调用allocate与Scheduler通信的同时会pull走已经分配的container.
- 确切的来说AM最后得到的只是一个个NMToken, AM凭这些NMToken去向NM获取真正的container. 而AppAttempt中保存的container只是Scheduler用来记录AM资源使用情况的一个数据结构.


##### 1.1 SchedulerApplicationAttempt.updateResourceRequests()
```
|-> SchedulerApplicationAttempt.updateResourceRequests()
  -> AppSchedulingInfo.UpdateResourceRequest()
    -> AppSchedulingInfo.AddToPlacementSet()
      -> LocalitySchedulingPlacementSet.updateResourceRequests(),
      -> AppSchedulingInfo.UpdatePedingResource()
        ->| schedulerKeys.add(schedulerKey);

```

```java
public boolean updateResourceRequests(List<ResourceRequest> requests);
```
schedulerKeys

##### 1.2 SchedulerApplicationAttempt.pullNewlyAllocatedContainers()
将newlyAllocatedContainers中的内容返回, 并清空

##### 1.3 SchedulerApplicationAttempt.pullUpdatedNMTokens()
将updatedNMTokens的内容返回, 并清空


##### 各个ResourceRequest的组织关系
```
各类的包含关系:
|-> ApplicationAttempt
  -> AppSchedulingInfo
    -> SchdulerKeyToPlacementSets(map<SchedulerRequestKey, SchedulingPlacementSet>)

ApplicationAttempt: 从一个Scheduler的视角描述一个application attempt.
AppSchedulingInfo: 调度相关, 主要维护一个PlacementSet
SchedulingPlacementSet: 用来保存当前未处理的资源请求, 当有新的资源请求或者分配了新的container时都需要更新相应的PlacementSet.
```

1. 在LocalitySchedulingPlacementSet中用一个resourceRequestMap来维护所有的ResourceRequest:
```java
private final Map<String, ResourceRequest> resourceRequestMap =
      new ConcurrentHashMap<>();
```
- key为ResourceRequest的ResourceName: The **name** of the host or rack on which the allocation is desired.
- value为对应的ResourceRequest.

2. 在AppSchedulingInfo中对SchedulingPlacementSet做了进一步封装:
```java
private final ConcurrentSkipListSet<SchedulerRequestKey>
    schedulerKeys = new ConcurrentSkipListSet<>();

final Map<SchedulerRequestKey, SchedulingPlacementSet<SchedulerNode>>
    schedulerKeyToPlacementSets = new ConcurrentHashMap<>();
```
- 通过schedulerKeyToPlacementSets, 用SchedulerRequestKey来索引所有的PlacementSet.
- Scheduler在nodeUpdate的时候会为schedulerKeys中的每个SchedulerKey分配container, 然后再更新schedulerKeyToPlacementSets中对应的ResourceRequest.

3. 在SchedulerApplicationAttempt中维护着一个AppSchedulingInfo对象. 用来保存该应用的所有ResourceRequest.
