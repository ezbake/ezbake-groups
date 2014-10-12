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

package ezbake.groups.service;

import ezbake.base.thrift.EzBakeBaseService;
import ezbake.configuration.ClasspathConfigurationLoader;
import ezbake.configuration.EzConfiguration;
import ezbake.configuration.constants.EzBakePropertyConstants;
import ezbake.groups.thrift.EzGroupsConstants;
import ezbake.thrift.ThriftClientPool;
import ezbake.thrift.ThriftServerPool;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.embedded.RedisServer;

import java.net.InetAddress;
import java.util.Properties;
import java.util.Random;

/**
 * User: jhastings
 * Date: 8/12/14
 * Time: 3:58 PM
 */
public class EzGroupsIT {

    RedisServer redisServer;
    ThriftClientPool clientPool;
    ThriftServerPool pool;
    @Before
    public void startService() throws Exception {
        Random portChooser = new Random();
        int zooPort = portChooser.nextInt((20499 - 20000) + 1) + 20000;
        int redisPort = portChooser.nextInt((23958 - 22456) + 1) + 22456;

        Properties ezProps = new EzConfiguration(new ClasspathConfigurationLoader("/test.properties", "/graphconfig.properties")).getProperties();
        ezProps.setProperty(EzBakePropertyConstants.ZOOKEEPER_CONNECTION_STRING, "localhost:"+Integer.toString(zooPort));
        ezProps.setProperty(EzBakePropertyConstants.REDIS_PORT, Integer.toString(redisPort));
        ezProps.setProperty(EzBakePropertyConstants.REDIS_HOST, InetAddress.getLocalHost().getCanonicalHostName());

        redisServer = new RedisServer(redisPort);
        redisServer.start();

        pool = new ThriftServerPool(ezProps, 32844);
        pool.startCommonService(new EzGroupsService(), EzGroupsConstants.SERVICE_NAME, "12345");

        clientPool = new ThriftClientPool(ezProps);
    }

    @After
    public void stopService() throws InterruptedException {
        if (redisServer != null) {
            redisServer.stop();
        }
        if (pool != null) {
            pool.shutdown();
        }
        if (clientPool != null) {
            clientPool.close();
        }
    }

    @Test
    public void testPing() throws TException {
        EzBakeBaseService.Client client = clientPool.getClient(EzGroupsConstants.SERVICE_NAME, EzBakeBaseService.Client.class);
        client.ping();
    }
}
