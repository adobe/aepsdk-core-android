/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.services;

/** Creates and return instances of {@link DataQueue}. */
public interface DataQueuing {
    /**
     * Creates an instance of {@link DataQueue} if it was not previously cached, otherwise the
     * cached instance is returned.
     *
     * @param databaseName {@link String}: name of the database, to be created for {@link
     *     DataEntity} persistence.
     * @return instance of DataQueue.
     */
    DataQueue getDataQueue(final String databaseName);
}
