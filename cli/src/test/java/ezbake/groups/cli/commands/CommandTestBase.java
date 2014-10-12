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

package ezbake.groups.cli.commands;

import ezbake.configuration.constants.EzBakePropertyConstants;
import ezbake.local.zookeeper.LocalZookeeper;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Random;

/**
 * User: jhastings
 * Date: 9/2/14
 * Time: 12:09 PM
 */
public class CommandTestBase {
    public static final String REDIS_HOST = "localhost";//InetAddress.getLocalHost().getCanonicalHostName());

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @After
    public void cleanFileSystem() throws IOException {
        FileUtils.cleanDirectory(folder.getRoot());
    }

    static LocalZookeeper zoo;
    static RedisServer redis;
    static Properties globalProperties = new Properties();

    private static Random rgen = new Random(System.currentTimeMillis());
    public static int getRandomPort(int start, int end) {
        return rgen.nextInt((end - start) + 1) + start;
    }

    public static void startZookeeper() throws Exception {
        zoo = new LocalZookeeper();
        globalProperties.setProperty(EzBakePropertyConstants.ZOOKEEPER_CONNECTION_STRING, zoo.getConnectionString());
    }

    public static void stopZookeeper() throws IOException {
        if (zoo != null) {
            zoo.shutdown();
        }
    }

    public static void startRedis() throws IOException {
        int redisPort = getRandomPort(14000, 14500);
        redis = new RedisServer(redisPort);
        redis.start();
        globalProperties.setProperty(EzBakePropertyConstants.REDIS_HOST, REDIS_HOST);
        globalProperties.setProperty(EzBakePropertyConstants.REDIS_PORT, Integer.toString(redisPort));
    }

    public static void stopRedis() throws InterruptedException {
        if (redis != null) {
            redis.stop();
        }
    }
}
