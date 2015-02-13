/*******************************************************************************
 * Copyright (c) 2015 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.ibm.ws.massive.sa.client;

import java.util.Collection;

/**
 * <p>Implementors of this interface are indicating that they are making breaking changes to their data structure (i.e. adding a new enum value) and therefore these fields cannot
 * be stored in the JSON along with the rest of the fields. It is an indicator to {@link DataModelSerializer} that it should create a second object with a "2" on the end of the
 * attribute name.</p>
 * <p>For instance, if we have Object A that has a field, b, of type Object B and B implements this interface and has fields x, y and z, if z is a breaking change then the JSON
 * will be look like the following for A:</p>
 * <code>
 * {<br/>
 * &nbsp;&nbsp;"b" : {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;"x": "xValue",<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;"y": "yValue",<br/>
 * &nbsp;&nbsp;},<br/>
 * &nbsp;&nbsp;"b2" : {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;"z": "zValue"<br/>
 * &nbsp;&nbsp;}<br/>
 * }
 * </code>
 * <p>Classes using objects implementing this interface must not have any clashing fields suffixed with "2" already.</p>
 */
public interface HasBreakingChanges {

    /**
     * A collection of attribute names that should be stored in the second object as they would break the first object if they were ever loaded.
     *
     * @return The collection of names, can be empty but must not be <code>null</code>
     */
    Collection<String> attributesThatCauseBreakingChanges();

}
