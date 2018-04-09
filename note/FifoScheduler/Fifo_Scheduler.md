### FIFO Scheduler




- 已经大致弄清楚了nodeUpdate的过程: 依次读取每个application的每个schedulerRequestKey, 为每个schedulerRequestKey分配container. 但是schedulerRequestKey是如何被添加到appAttempt中的 ???
- 沿着调用链最终发现updateResourceRequests会更新到schedulerKeys, 但也只是将参数中的schedulerKey添加进去.
- 那么updateResourceRequests又是在哪里调用的 ??? 沿着调用链进一步往上找发现updateResourceRequests在FifoScheduler.allocate函数中被调用到, 而FifoScheduler.allocate又是声明为public的. 由此可得出: applicationAttempt只是Scheduler用于保存真实应用信息的一个数据结构. 真实应用通过Scheduler.allocate方法向Scheduler发送资源请求, Scheduler将该请求保存到对应的AppAttempt中, 然后在处理NODE_UPDAT事件时响应所有的AppAttempt的资源请求, 为其分配Container.
- RM给AM分配资源时只是给了AM一个凭证(NMToken), AM通过该凭证向NM获取真正的container. 当然RM中也必定有相应的Container对象来记录资源的使用情况, 只是AM在pull资源时候RM为每个container都生成一个凭证然后将凭证返回给AM. (参见 YarnScheduler.allocate())


- 结合 http://www.cnblogs.com/zhangchao0515/p/6955126.html 再看看FIFOScheduler的源码, 希望能有新的发现. 还以为有多详细, 也不过是将英文注释翻译成中文而已.

- 为什么会有CONTAINER_EXPIRED ???
在网上找到的资料中, RM分配的container需要由AM来取, 如果一定时间后还没有AM来认领则触发CONTAINER_EXPIRED事件将container回收. 但是目前还没搞懂在代码中是如何实现的.
RM分配的资源会先保存到AppAttempt中, 之后应用还需要通过对应的pull方法将资源从AppAttempt中取走(pull方法通常会返回一个Allocation对象).


FifoScheduler是如何体现 Fifo的 ?
Queue ?

- SchedulerNode 和 RMNode ??????


函数调用层次:
handle
-> nodeUpdate 调度器事件
-> assignContainer 在某个Node上分配container: 先取出Scheduler所有的要调度的appAtempt, 然后再取出每个appAtempt的所有资源请求(即SchedulerKey), 依次处理.
-> assignContainersOnNode(node, appAtempt, schedulerKey) 在node上处理appAtempt的schedulerKey.


- 资源调度也就是任务调度, 当AM要创建新的task时, 需要首先向RM申请container, 只有在分配到container后AM才能运行新的task. 所以Scheduler在为每个AM分配container的时候也就起到了任务调度的作用.


### 主要的几个函数:
#### 1. FifoScheduler.allocate()
...
#### 2. FifoScheduler.assigncontainers()
...
#### 3. FifoScheduler.handle()
...


### 测试用例
- hadoop-yarn-resourcemanager/src/test/java/....../scheduler/fifo/TestFifoScheduler.java
- 尝试运行测试文件, 以弄清楚Scheduler的具体运行过程. (`mvn test -Dtest=TestClassName`测试的输出在surefire-reports/中)
