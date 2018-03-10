/**
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.hadoop.yarn.server.resourcemanager.scheduler;

/**
 * Resource classification.
 * <p>
 * NODE_LOCAL, RACK_LOCAL, OFF_SWITCH三种类型的资源请求存在一种范围上的包含关系:
 * NODE_LOCAL包含于RACK_LOCAL, RACK_LOCAL包含于OFF_SWITCH.
 * 体现在代码上, 就是, 一个NODE_LOCAL类型的请求必定伴有相应的RACK_LOCAL和OFF_SWITCH类型的请求,
 * 一个RACK_LOCAL类型的请求也一定伴有相应的OFF_SWITCH类型的请求. </p>
 * <p>
 * (在FifoScheduler的assignNodeLocalContainers,assignRackLocalContainer,
 * assignOffSwitchContainer以及LocalitySchedulingPlacementSet的
 * allocateNodeLocal,allocateRackLocal,allocateOffSwitch等函数都有体现.) </p>
 */
public enum NodeType {
  NODE_LOCAL(0), RACK_LOCAL(1), OFF_SWITCH(2);

  private final int index;

  NodeType(int index) {
    this.index = index;
  }

  /**
   * @return the index of the node type
   */
  public int getIndex() {
    return index;
  }
}
