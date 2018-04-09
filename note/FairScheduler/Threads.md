## 三个线程
```java
// FairScheduler.java

@VisibleForTesting
Thread updateThread;

@VisibleForTesting
Thread schedulingThread;

Thread preemptionThread;
```


### updateThread
- 定时地调用update方法, 更新每个队列的资源需求,fairShare等信息.
> Recompute the internal variables used by the scheduler - per-job weights, fair shares, deficits, minimum slot allocations, and amount of used and required resources per job.


### schedulingThread
> Thread which attempts scheduling resources continuously, asynchronous to the node heartbeats.

- 定时地调用continuousSchedulingAttempt方法, 依次遍历集群中的所有节点, 对每个节点执行attemptScheduling进行资源分配.

- 不是已经有node heartbeat了吗, 为什么还要来这么一个线程 ?
由配置参数`isContinuousSchedulingEnabled()`决定.


### preemptionThread
- 由配置参数`getPreemptionEnabled()`决定是否启用抢占.
- Yarn FairScheduler的抢占机制详解: <https://blog.csdn.net/zhanyuanlin/article/details/71516286>

- 是一个守护进程, 一直在运行, 而不像其他两个是定时的.

- Scheduler的context中维护着一个StarvedApps对象, 记录当前有哪些饥饿进程, preemptionThread就专门处理这里面的进程, 为他们抢container.
- 分配container的时候这些饥饿进程享有优先权.

那么问题来了, Scheduler是如何标识饥饿进程的??
在updateThread更新完每个队列的fairShare之后, 通过updateStarvedApps更新StarvedApps中的appAttemp.

从FSAppAttempt中的isStarved()函数来看, 如果一个appAttempt的资源使用量小于其fairshare值, 就认为是饥饿.

nextStarvationCheck 判断appAttempt的时间间隔, 如果时间未到就当做该appAttempt不处于饥饿状态.




##...

- Context: holds basic information to be passed around classes.
