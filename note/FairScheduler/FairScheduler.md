[Hadoop MapReduce Next Generation - Fair Scheduler]( http://archive.cloudera.com/cdh5/cdh/5/hadoop/hadoop-yarn/hadoop-yarn-site/FairScheduler.html )

每当一个节点更新就会触发一个NODE_UPDATE事件, Scheduler在收到该事件后, 根据一定策略(如FIFO)选出一个需要调度的app, 然后尝试在该节点上为app的所有ResourceRequest分配container.


## 队列结构
- 两种队列节点: FSParentQueue(内部节点), FSLeafQueue(叶子节点), 但是都继承自FSQueue.

- 每个FSParentQueue用一个List来保存所有的子节点, 构成一个树型队列结构.
```java
private final List<FSQueue> childQueues = new ArrayList<>();
```

- 每个FSLeafQueue也用一个List来需要调度的FSAppAttempt.
```java
private final List<FSAppAttempt> runnableApps = new ArrayList<>();
private final List<FSAppAttempt> nonRunnableApps = new ArrayList<>();
```

- QueueManager
> Maintains a list of queues as well as scheduling parameters for each queue, such as guaranteed share allocations, from the fair scheduler config file.

```java
// 保存所有的叶子节点
private final Collection<FSLeafQueue> leafQueues =
    new CopyOnWriteArrayList<FSLeafQueue>();
// 所有节点的索引
private final Map<String, FSQueue> queues = new HashMap<String, FSQueue>();
// 根节点
private FSParentQueue rootQueue;
```

- 创建节点
```java
FSQueue createQueue(String name, FSQueueType queueType) {
    List<String> newQueueNames = new ArrayList<>();
    // 根据name, 自底向上找出还没有创建的节点.
    FSParentQueue parent = buildNewQueueList(name, newQueueNames);
    FSQueue queue = null;

    if (parent != null) {
      // 根据newQueueNames, 自顶向下依次创建还未创建的节点.
      queue = createNewQueues(queueType, parent, newQueueNames);
    }

    return queue;
  }
```



## 如何实现FairShare?
通过继承SchedulingPolicy定义不同的策略.
FairSharePolicy.


每个策略都继承自SchedulingPolicy, 对外提供统一接口.
并且每个策略都基于Comparator接口实现一个比较器, 用于比较两个Schedulable对象的大小关系.
由于每个队列和应用都实现了Schedulable接口这样, 在调度的时候就变得非常简单, 只需根据定义的比较器进行一次排序即可.


原来接口是这么用的.


## 哪里体现了抢占?
```java
// 每次nodeUpdate最多只能分配该节点未使用资源的一半
void attemptScheduling(FSSchedulerNode node);
```

```java
// 在assignContainer之前, 检查队列的资源是否已经达到了最大值(maxShare)
boolean assignContainerPreCheck(FSSchedulerNode node);
```


fairShare 指的是什么???


计算资源本质上还是共享的, 因为都是跑在同一个集群上. 所谓的FairShare其实就是根据一定条件限制各个队列的资源使用情况,


一个队列的resourceUsage不能大过maxShare


## 关于队列的几个share值
yarn fair scheduler 之公平份额算法和抢占模型 <https://blog.csdn.net/wujun8/article/details/32316977>
```java
private Resource fairShare = Resources.createResource(0, 0);      // update的时候分配到的资源
private Resource steadyFairShare = Resources.createResource(0, 0);
protected Resource minShare;            // 任何时候队列的可用资源都不能低于minShare
private ConfigurableResource maxShare;  // 队列的最大资源使用量
```






.
