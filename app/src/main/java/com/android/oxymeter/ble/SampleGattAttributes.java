/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.oxymeter.ble;

import com.android.oxymeter.utilities.CommonUtils;

import java.util.HashMap;
import java.util.UUID;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";


    public static String OXYGEN_RATE_MEASUREMENT = "49535343-fe7d-4ae5-8fa9-9fafd205e455";
    public static String OXYGEN_RATE_CHARACTERISTIC = "49535343-1e4d-4bd9-ba61-23c647249616";
    public static final UUID UUID_CHARACTER_RECEIVE_OXYGEN = UUID.fromString(OXYGEN_RATE_CHARACTERISTIC);
    public static String CLIENT_CHARACTERISTIC_CONFIG_OXYGEN = "00002902-0000-1000-8000-00805f9b34fb";


   /* public static final UUID  UUID_SERVICE_DATA                 = UUID.fromString("49535343-fe7d-4ae5-8fa9-9fafd205e455");
    public static final UUID       UUID_CHARACTER_RECEIVE       = UUID.fromString("49535343-1e4d-4bd9-ba61-23c647249616");
    public static final UUID       UUID_MODIFY_BT_NAME          = UUID.fromString("00005343-0000-1000-8000-00805F9B34FB");

    public static final UUID UUID_CLIENT_CHARACTER_CONFIG       = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");*/


    static {
        // Sample Services.
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access");// S1
        attributes.put(CommonUtils.convertFromInteger(0x1801).toString(), "Generic Attribute Service");// S2
        attributes.put(OXYGEN_RATE_MEASUREMENT, "Oxymeter Service");// S3 read write
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");// S4


        attributes.put(CommonUtils.convertFromInteger(0x180D).toString(), "Heart Rate");
        attributes.put(CommonUtils.convertFromInteger(0x1822).toString(), "Pulse Oximeter Service");
        attributes.put(CommonUtils.convertFromInteger(0x1810).toString(), "Blood Pressure");


        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");


        //S1
        attributes.put(CommonUtils.convertFromInteger(0x2A00).toString(), "Device Name");//
        attributes.put(CommonUtils.convertFromInteger(0x2A01).toString(), "Appearance");//
        attributes.put(CommonUtils.convertFromInteger(0x2A04).toString(), "Peripheral Preferred Connection Parameters");//

//S2
        attributes.put(CommonUtils.convertFromInteger(0x2A05).toString(), "Service Changed");//

        //S3
        attributes.put(OXYGEN_RATE_CHARACTERISTIC, "SpO2");//

        //S4
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");//
        attributes.put(CommonUtils.convertFromInteger(0x2A24).toString(), "Model Number String");//
        attributes.put(CommonUtils.convertFromInteger(0x2A25).toString(), "Serial Number String");//
        attributes.put(CommonUtils.convertFromInteger(0x2A27).toString(), "Hardware Revision String");//
        attributes.put(CommonUtils.convertFromInteger(0x2A26).toString(), "Firmware Revision String");
        attributes.put(CommonUtils.convertFromInteger(0x2A28).toString(), "Software Revision String");
        attributes.put(CommonUtils.convertFromInteger(0x2A23).toString(), "System ID");
        attributes.put(CommonUtils.convertFromInteger(0x2A2A).toString(), "IEEE 11073-20601 Regulatory Certification Data List");
        attributes.put(CommonUtils.convertFromInteger(0x2A50).toString(), "PnP ID");


    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
