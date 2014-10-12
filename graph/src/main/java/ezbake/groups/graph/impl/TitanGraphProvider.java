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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanType;
import com.tinkerpop.blueprints.Vertex;
import ezbake.configuration.EzConfiguration;
import ezbake.groups.graph.TitanGraphConfiguration;
import ezbake.groups.graph.api.GraphProvider;
import ezbake.groups.graph.frames.vertex.BaseVertex;
import ezbake.groups.graph.frames.vertex.Group;
import ezbake.groups.graph.frames.vertex.User;

import java.util.Properties;

/**
 * User: jhastings
 * Date: 6/23/14
 * Time: 10:32 AM
 */
public class TitanGraphProvider implements GraphProvider<TitanGraph>, Provider<TitanGraph> {

    private Properties ezConfiguration;
    @Inject
    public TitanGraphProvider(Properties ezConfiguration) {
        this.ezConfiguration = ezConfiguration;
    }

    @Override
    public TitanGraph getGraph(Properties ezConfiguration) {
        TitanGraph graph = TitanFactory.open(new TitanGraphConfiguration(ezConfiguration));
        initGraph(graph);

        return graph;
    }

    private void initGraph(TitanGraph graph) {
        TitanType index = graph.getType(BaseVertex.INDEX);
        if (index == null) {
            index = graph.makeKey(BaseVertex.INDEX).dataType(Long.class).indexed(Vertex.class).unique().make();
        }
        TitanType userIndex = graph.getType(User.PRINCIPAL);
        if  (userIndex == null) {
            userIndex = graph.makeKey(User.PRINCIPAL).dataType(String.class).indexed(Vertex.class).make();
        }
        TitanType groupName = graph.getType(Group.GROUP_NAME);
        if  (groupName == null) {
            groupName = graph.makeKey(Group.GROUP_NAME).dataType(String.class).indexed(Vertex.class).make();
        }
    }

    @Override
    public TitanGraph get() {
        return getGraph(ezConfiguration);
    }

}
