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

import java.util.List;

/** A Thread-Safe Queue used to store {@link DataEntity} Objects. */
public interface DataQueue {
    /**
     * Add a new {@link DataEntity} Object to {@link DataQueue}
     *
     * @param dataEntity, instance of {@link DataEntity}
     * @return true if successfully added else false.
     */
    boolean add(final DataEntity dataEntity);

    /** Retrieves the head of {@link DataQueue} else returns null if {@link DataQueue} is empty. */
    DataEntity peek();

    /**
     * Retrieves the first n entries in this {@link DataQueue}. Returns null if {@link DataQueue} is
     * empty.
     */
    List<DataEntity> peek(final int n);

    /**
     * Removes the head of this {@link DataQueue}
     *
     * @return true if successfully removed else returns false.
     */
    boolean remove();

    /**
     * Removed the first n elements in this {@link DataQueue}
     *
     * @return true if successfully removed else returns false.
     */
    boolean remove(final int n);

    /**
     * Removes all stored {@link DataEntity} objects.
     *
     * @return true if successfully removed else returns false.
     */
    boolean clear();

    /** Returns the count of {@link DataEntity} objects in this {@link DataQueue}. */
    int count();

    /** Closes the current {@link DataQueue}. */
    void close();
}
