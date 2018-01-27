

```java
/**
 * Node managers information on available resources
 * and other static information.
 * 访问一个节点的基本信息
 */
public interface RMNode{}
```


```java
/**
 * NMContainerStatus includes the current information of a container. This
 * record is used by YARN only, whereas {@link ContainerStatus} is used both
 * inside YARN and by end-users.
 */
public abstract class NMContainerStatus {}
```


```java
/**
 * Represents a YARN Cluster Node from the viewpoint of the scheduler.
 * 从资源调度的角度对节点进行封装, 提供对节点的资源进行操作的一些方法
 */
public abstract class SchedulerNode {
  private final RMNode rmNode;
  private final String nodeName;
}
```


```java
/**
 * 针对Fifo和Capacity调度器, 对SchedulerNode进行扩展, 比如实现了资源预留机制
 */
public class FiCaSchedulerNode extends SchedulerNode {
  public synchronized void reserveResource(
        SchedulerApplicationAttempt application, SchedulerRequestKey priority,
        RMContainer container) {}
  public synchronized void unreserveResource(
        SchedulerApplicationAttempt application) {}
}
```



```java
/**
 * Helper library that:
 * - tracks the state of all cluster {@link SchedulerNode}s
 * - provides convenience methods to filter and sort nodes
 * SchedulerNode的一个集合
 */
@InterfaceAudience.Private
public class ClusterNodeTracker<N extends SchedulerNode> {
  private HashMap<NodeId, N> nodes = new HashMap<>();

}
```

--- 2018.1.18 ---

```java
public interface YarnScheduler extends EventHandler<SchedulerEvent> {

}
```


```java
public interface ResourceScheduler extends YarnScheduler, Recoverable {

}
```


```java
/**
 * Context of the ResourceManager.
 */
public interface RMContext extends ApplicationMasterServiceContext {

}
```


```java
public abstract class AbstractYarnScheduler
    <T extends SchedulerApplicationAttempt, N extends SchedulerNode>
    extends AbstractService implements ResourceScheduler {

}
```


```java
public class FifoScheduler extends
    AbstractYarnScheduler<FifoAppAttempt, FiCaSchedulerNode> implements
    Configurable {

}
```

--- 2018.1.20 ---





.
