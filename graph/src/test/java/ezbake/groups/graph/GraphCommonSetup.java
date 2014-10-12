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

import com.thinkaurelius.titan.diskstorage.StorageException;
import com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration;
import ezbake.configuration.ClasspathConfigurationLoader;
import ezbake.configuration.EzConfiguration;
import ezbake.configuration.EzConfigurationLoaderException;
import ezbake.groups.graph.impl.NaiveIDProvider;
import ezbake.groups.graph.impl.TitanGraphProvider;
import ezbake.groups.graph.query.SpecialAppGroupQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.Properties;

/**
 * User: jhastings
 * Date: 7/28/14
 * Time: 8:21 PM
 */
public class GraphCommonSetup {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    public final static String graphConfig = "/graphconfig.properties";

    public Properties ezConfiguration;
    public EzGroupsGraph graph;
    public SpecialAppGroupQuery query;

    @Before
    public void setUp() throws StorageException, EzConfigurationLoaderException {
        ezConfiguration = new EzConfiguration(new ClasspathConfigurationLoader(graphConfig)).getProperties();
        ezConfiguration.setProperty("storage.directory", folder.getRoot().toString());

        GraphDatabaseConfiguration conf = new GraphDatabaseConfiguration(new TitanGraphConfiguration(ezConfiguration));
        conf.getBackend().clearStorage();
        graph = new EzGroupsGraph(ezConfiguration, new TitanGraphProvider(ezConfiguration).get(), new NaiveIDProvider());
        query = new SpecialAppGroupQuery(graph.getFramedGraph(), graph.appGroupId);
    }

    @After
    public void wipeGraph() throws StorageException, IOException {
        if (graph != null) {
            graph.close();
        }
    }
}
