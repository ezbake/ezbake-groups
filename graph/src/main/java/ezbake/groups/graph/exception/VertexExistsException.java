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

package ezbake.groups.graph.exception;

/**
 * User: jhastings
 * Date: 6/27/14
 * Time: 7:57 AM
 */
public class VertexExistsException extends Exception {
    public VertexExistsException(String message) {
        super(message);
    }
    public VertexExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
