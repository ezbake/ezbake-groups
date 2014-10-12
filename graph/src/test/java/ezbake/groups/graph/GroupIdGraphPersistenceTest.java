/*   Copyright (C) 2013-2014 Computer Sciences Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */

package ezbake.groups.graph;

import com.thinkaurelius.titan.core.TitanGraph;
import ezbake.configuration.ClasspathConfigurationLoader;
import ezbake.configuration.EzConfiguration;
import ezbake.configuration.constants.EzBakePropertyConstants;
import ezbake.groups.graph.api.GroupIDProvider;
import ezbake.groups.graph.api.GroupIDPublisher;
import ezbake.groups.graph.exception.*;
import ezbake.groups.graph.frames.vertex.BaseVertex;
import ezbake.groups.graph.frames.vertex.Group;
import ezbake.groups.graph.frames.vertex.User;
import ezbake.groups.graph.impl.RedisIDProvider;
import ezbake.groups.graph.impl.TitanGraphIDPublisher;
import ezbake.groups.graph.impl.TitanGraphProvider;
import ezbake.local.zookeeper.LocalZookeeper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Random;

/**
 * This test is designed to simulate the graph starting up, shutting down, and the redis id key going missing
 */
public class GroupIdGraphPersistenceTest {
    private static final Logger logger = LoggerFactory.getLogger(GroupIdGraphPersistenceTest.class);
    public static final String REDIS_HOST = "localhost";//InetAddress.getLocalHost().getCanonicalHostName());
    public static String graphConfig = "/graphconfig.properties";

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    static Random rgen = new Random(System.currentTimeMillis());

    static Properties ezConfiguration;
    static LocalZookeeper zookeeper;
    static RedisServer redisServer;
    static int redisPort = getRandomPort(14000, 25000);

    @BeforeClass
    public static void start() throws Exception {
        zookeeper = new LocalZookeeper();
        redisServer = new RedisServer(redisPort);
        redisServer.start();

        ezConfiguration = new EzConfiguration(new ClasspathConfigurationLoader(graphConfig)).getProperties();
        ezConfiguration.setProperty(EzBakePropertyConstants.ZOOKEEPER_CONNECTION_STRING, zookeeper.getConnectionString());

        logger.debug("REDIS connection: {}    {}", InetAddress.getLocalHost().getCanonicalHostName());
        ezConfiguration.setProperty(EzBakePropertyConstants.REDIS_HOST, REDIS_HOST);
        ezConfiguration.setProperty(EzBakePropertyConstants.REDIS_PORT, Integer.toString(redisPort));
        ezConfiguration.setProperty("storage.directory", folder.getRoot().toString());
    }

    @AfterClass
    public static void stop() throws InterruptedException, IOException {
        if (zookeeper != null) {
            zookeeper.shutdown();
        }
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    public static int getRandomPort(int start, int end) {
        return rgen.nextInt((end - start) + 1) + start;
    }


    TitanGraph graph;
    GroupIDProvider idProvider;
    JedisPool jedis;

    @Before
    public void setUpTest() {
        graph = new TitanGraphProvider(ezConfiguration).get();

        jedis = new JedisPool(new JedisPoolConfig(),
                ezConfiguration.getProperty(EzBakePropertyConstants.REDIS_HOST),
                Integer.valueOf(ezConfiguration.getProperty(EzBakePropertyConstants.REDIS_PORT)));

        CuratorFramework curator = CuratorFrameworkFactory.builder()
                .connectString(zookeeper.getConnectionString())
                .retryPolicy(new RetryNTimes(5, 1000))
                .build();
        GroupIDPublisher publisher = new TitanGraphIDPublisher(graph);

        idProvider = new RedisIDProvider(jedis, curator, publisher);
    }


    @Test
    public void clearRedisTest() throws InvalidVertexTypeException, AccessDeniedException, IndexUnavailableException, UserNotFoundException, VertexExistsException, InvalidGroupNameException {
        EzGroupsGraph ezGroupsGraph = new EzGroupsGraph(ezConfiguration, graph, idProvider);

        // Create some users
        User u1 = ezGroupsGraph.addUser(BaseVertex.VertexType.USER, "user1", "user one");
        User u2 = ezGroupsGraph.addUser(BaseVertex.VertexType.USER, "user2", "user two");

        // Create a group
        Group g1 = ezGroupsGraph.addGroup(BaseVertex.VertexType.USER, "user1", "group1");
        Group g2 = ezGroupsGraph.addGroup(BaseVertex.VertexType.USER, "user2", "group2");

        // Clear out redis - lose the index!
        Jedis jedi = jedis.getResource();
        jedi.flushAll();
        jedi.close();

        // Create some users
        User u3 = ezGroupsGraph.addUser(BaseVertex.VertexType.USER, "user3", "user three");
        User u4 = ezGroupsGraph.addUser(BaseVertex.VertexType.USER, "user4", "user four");

        Assert.assertTrue(u1.getIndex() < u2.getIndex());
        Assert.assertTrue(u2.getIndex() < g1.getIndex());
        Assert.assertTrue(g1.getIndex() < g2.getIndex());
        Assert.assertTrue(g2.getIndex() < u3.getIndex());
        Assert.assertTrue(u3.getIndex() < u4.getIndex());

        logger.info("issued IDs in order: {} {} {} {} {} {}", u1.getIndex(), u2.getIndex(), g1.getIndex(),
                g2.getIndex(), u3.getIndex(), u4.getIndex());

    }



}
