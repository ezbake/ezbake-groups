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

import ezbake.configuration.ClasspathConfigurationLoader;
import ezbake.configuration.EzConfiguration;
import ezbake.configuration.EzConfigurationLoaderException;
import ezbake.configuration.constants.EzBakePropertyConstants;
import ezbake.local.zookeeper.LocalZookeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.util.Properties;

/**
 * User: jhastings
 * Date: 7/30/14
 * Time: 12:53 AM
 */
public class EzGroupsServiceGuiceTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    Properties properties;
    LocalZookeeper lz;
    RedisServer rs;

    @Before
    public void setUp() throws Exception {
        properties = new EzConfiguration(new ClasspathConfigurationLoader("/test.properties", "/graphconfig.properties")).getProperties();
        properties.setProperty("storage.directory", folder.getRoot().toString());


        lz = new LocalZookeeper();
        properties.setProperty(EzBakePropertyConstants.ZOOKEEPER_CONNECTION_STRING, lz.getConnectionString());

        rs = new RedisServer(29302);
        rs.start();
        properties.setProperty(EzBakePropertyConstants.REDIS_PORT, Integer.toString(29302));
        properties.setProperty(EzBakePropertyConstants.REDIS_HOST, "localhost");
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        if (lz != null) {
            lz.shutdown();
        }
        if (rs != null) {
            rs.stop();
        }
    }

    @Test
    public void testInstance() throws EzConfigurationLoaderException {
        EzGroupsService service = new EzGroupsService();
        service.setConfigurationProperties(properties);

        service.getThriftProcessor();
    }

}
