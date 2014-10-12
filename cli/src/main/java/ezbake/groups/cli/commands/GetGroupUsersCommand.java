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

import ezbake.configuration.EzConfigurationLoaderException;
import ezbake.groups.graph.EzGroupsGraph;
import ezbake.groups.graph.exception.UserNotFoundException;
import ezbake.groups.graph.exception.VertexNotFoundException;
import ezbake.groups.graph.frames.vertex.User;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * User: jhastings
 * Date: 9/23/14
 * Time: 2:52 PM
 */
public class GetGroupUsersCommand extends GroupCommand {

    public GetGroupUsersCommand() { }

    public GetGroupUsersCommand(String groupName, String user, Properties properties) {
        super(properties);
        this.user = user;
        this.groupName = groupName;
    }

    @Override
    public void runCommand() throws EzConfigurationLoaderException {
        EzGroupsGraph graph = getGraph();

        String internalName = nameHelper.addRootGroupPrefix(groupName);
        try {
            Set<User> users = graph.groupMembers(userType(), user, internalName, true, true, false);

            System.out.println("Group members for group: " + groupName);
            for (User user : users) {
                System.out.println("User principal: " + user.getPrincipal() + ", name: " + user.getName());
            }

        } catch (VertexNotFoundException | UserNotFoundException e) {
            System.err.println("Unable to get group members: " + e.getMessage());
        } finally {
            try {
                graph.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
