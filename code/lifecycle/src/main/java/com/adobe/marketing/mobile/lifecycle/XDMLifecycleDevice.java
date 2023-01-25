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

package com.adobe.marketing.mobile.lifecycle;

import java.util.HashMap;
import java.util.Map;

/**
 * Class {@code Device} representing a subset of the XDM Device data type fields. An identified
 * device, application or device browser instance that is trackable across sessions.
 */
@SuppressWarnings("unused")
class XDMLifecycleDevice {

    private String manufacturer;
    private String modelNumber;
    private String model;
    private int screenHeight;
    private int screenWidth;
    private XDMLifecycleDeviceTypeEnum type;

    XDMLifecycleDevice() {}

    Map<String, Object> serializeToXdm() {
        Map<String, Object> map = new HashMap<String, Object>();

        if (this.manufacturer != null) {
            map.put("manufacturer", this.manufacturer);
        }

        if (this.model != null) {
            map.put("model", this.model);
        }

        if (this.modelNumber != null) {
            map.put("modelNumber", this.modelNumber);
        }

        if (this.screenHeight > 0) {
            map.put("screenHeight", this.screenHeight);
        }

        if (this.screenWidth > 0) {
            map.put("screenWidth", this.screenWidth);
        }

        if (this.type != null) {
            map.put("type", this.type.toString());
        }

        return map;
    }

    /**
     * Returns the Manufacturer property The name of the organization who owns the design and
     * creation of the device, for example, 'Apple' is the manufacturer of the iPhone.
     *
     * @return {@link String} value or null if the property is not set
     */
    String getManufacturer() {
        return this.manufacturer;
    }

    /**
     * Sets the Manufacturer property The name of the organization who owns the design and creation
     * of the device, for example, 'Apple' is the manufacturer of the iPhone.
     *
     * @param newValue the new Manufacturer value
     */
    void setManufacturer(final String newValue) {
        this.manufacturer = newValue;
    }

    /**
     * Returns the Model number property The unique model number designation assigned by the
     * manufacturer for this device. Model numbers are not versions, but unique identifiers that
     * identify a particular model configuration. While the model for a particular phone might be
     * 'iPhone 6S' the model number would be 'A1633', or 'A1634' based on configuration at the time
     * of sale.
     *
     * @return {@link String} value or null if the property is not set
     */
    String getModelNumber() {
        return this.modelNumber;
    }

    /**
     * Sets the Model number property The unique model number designation assigned by the
     * manufacturer for this device. Model numbers are not versions, but unique identifiers that
     * identify a particular model configuration. While the model for a particular phone might be
     * 'iPhone 6S' the model number would be 'A1633', or 'A1634' based on configuration at the time
     * of sale.
     *
     * @param newValue the new Model number value
     */
    void setModelNumber(final String newValue) {
        this.modelNumber = newValue;
    }

    /**
     * Returns the Model property The name of the model for the device. This is the common,
     * human-readable, or marketing name for the device. For example, the 'iPhone 6S' is a
     * particular model of mobile phone.
     *
     * @return {@link String} value or null if the property is not set
     */
    String getModel() {
        return this.model;
    }

    /**
     * Sets the Model property The name of the model for the device. This is the common,
     * human-readable, or marketing name for the device. For example, the 'iPhone 6S' is a
     * particular model of mobile phone.
     *
     * @param newValue the new Model value
     */
    void setModel(final String newValue) {
        this.model = newValue;
    }

    /**
     * Returns the Screen height property The number of vertical pixels of the device's active
     * display in the default orientation.
     *
     * @return int value
     */
    int getScreenHeight() {
        return this.screenHeight;
    }

    /**
     * Sets the Screen height property The number of vertical pixels of the device's active display
     * in the default orientation.
     *
     * @param newValue the new Screen height value
     */
    void setScreenHeight(final int newValue) {
        this.screenHeight = newValue;
    }

    /**
     * Returns the Screen width property The number of horizontal pixels of the device's active
     * display in the default orientation.
     *
     * @return int value
     */
    int getScreenWidth() {
        return this.screenWidth;
    }

    /**
     * Sets the Screen width property The number of horizontal pixels of the device's active display
     * in the default orientation.
     *
     * @param newValue the new Screen width value
     */
    void setScreenWidth(final int newValue) {
        this.screenWidth = newValue;
    }

    /**
     * Returns the Type property Type of device being tracked.
     *
     * @return {@link XDMLifecycleDeviceTypeEnum} value or null if the property is not set
     */
    XDMLifecycleDeviceTypeEnum getType() {
        return this.type;
    }

    /**
     * Sets the Type property Type of device being tracked.
     *
     * @param newValue the new Type value
     */
    void setType(final XDMLifecycleDeviceTypeEnum newValue) {
        this.type = newValue;
    }
}
