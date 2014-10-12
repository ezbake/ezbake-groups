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

package ezbake.groups.graph.impl;

import ezbake.groups.graph.api.GroupIDProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.framework.recipes.atomic.PromotedToLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

/**
 * User: jhastings
 * Date: 9/18/14
 * Time: 12:41 PM
 */
public class ZookeeperIDProvider implements GroupIDProvider {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperIDProvider.class);
    public static final String indexZkPath = "/ezbake/groups/index/counter";
    public static final String lockZkPath = "/ezbake/groups/index/lock";

    private DistributedAtomicLong index;

    @Inject
    public ZookeeperIDProvider(CuratorFramework curator) {
        if (curator.getState() == CuratorFrameworkState.LATENT) {
            curator.start();
        }
        index = new DistributedAtomicLong(curator, indexZkPath, curator.getZookeeperClient().getRetryPolicy(),
                PromotedToLock.builder()
                    .lockPath(lockZkPath)
                    .timeout(250, TimeUnit.MILLISECONDS)
                    .build());
    }

    /**
     * Zookeeper ID Provider only allows the setting of the current id to 0, and only if the value is not present in
     * zookeeper
     *
     * @param id index number
     * @throws Exception
     */
    @Override
    public void setCurrentID(long id) throws Exception {
        if (id > 0) {
            throw new Exception("Invalid index number: " + id + ". " + ZookeeperIDProvider.class.getSimpleName() +
                    " can only set the initial value to 0");
        }
        AtomicValue<Long> current = index.get();
        if (!current.succeeded() || current.succeeded() && current.postValue() > 0) {
            throw new Exception("Failed to set current id. Can only set value to 0, but current value is: " +
                    current.postValue());
        }
    }

    @Override
    public long currentID() throws Exception {
        AtomicValue<Long> current = index.get();
        if (!current.succeeded()) {
            throw new Exception("Failed to get id from zookeeper");
        }
        return current.postValue();
    }

    @Override
    public long nextID() throws Exception {
        AtomicValue<Long> current = index.increment();
        if (!current.succeeded()) {
            throw new Exception("Failed to increment id in zookeeper");
        }
        return current.postValue();
    }
}
