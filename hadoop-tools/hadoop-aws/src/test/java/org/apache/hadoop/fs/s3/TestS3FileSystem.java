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

package org.apache.hadoop.fs.s3;

import java.io.IOException;
import java.net.URI;

import junit.framework.TestCase;

import org.apache.hadoop.conf.Configuration;

@Deprecated
public class TestS3FileSystem extends TestCase {

  public static final URI EXPECTED = URI.create("s3://c");

  public void testInitialization() throws IOException {
    initializationTest("s3://a:b@c");
    initializationTest("s3://a:b@c/");
    initializationTest("s3://a:b@c/path");
    initializationTest("s3://a@c");
    initializationTest("s3://a@c/");
    initializationTest("s3://a@c/path");
    initializationTest("s3://c");
    initializationTest("s3://c/");
    initializationTest("s3://c/path");
  }
  
  private void initializationTest(String initializationUri)
    throws IOException {
    
    S3FileSystem fs = new S3FileSystem(new InMemoryFileSystemStore());
    fs.initialize(URI.create(initializationUri), new Configuration());
    assertEquals(EXPECTED, fs.getUri());
  }

}
