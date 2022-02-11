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

package com.adobe.marketing.mobile;

/**
 * The interface for EncodingService.
 * The base64 encoding API is valid on Android platform and java 1.8+. Even we can make of copy of that and keep the
 * code in Core, but for now it'd be better to just use the platform service for it.
 */
interface EncodingService {
	byte[] base64Decode(String input);
	byte[] base64Encode(byte[] input);
}
