///* *****************************************************************************
// * ADOBE CONFIDENTIAL
// * ___________________
// *
// * Copyright 2022 Adobe
// * All Rights Reserved.
// *
// * NOTICE: All information contained herein is, and remains
// * the property of Adobe and its suppliers, if any. The intellectual
// * and technical concepts contained herein are proprietary to Adobe
// * and its suppliers and are protected by all applicable intellectual
// * property laws, including trade secret and copyright laws.
// * Dissemination of this information or reproduction of this material
// * is strictly forbidden unless prior written permission is obtained
// * from Adobe.
// ******************************************************************************/
//
//package com.adobe.marketing.mobile.identity;
//
//import java.io.IOException;
//import java.io.InputStream;
//
///**
// * Mock class for HttpConnection used by Identity extension for advanced testing for hits retry
// */
//class IdentityMockConnection implements NetworkService.HttpConnection {
//	int responseCode;
//	InputStream inputStream;
//	boolean getInputStreamCalled = false;
//	InputStream errorStream;
//	boolean getErrorStreamCalled = false;
//
//	IdentityMockConnection(final int responseCode, final InputStream inputStream) {
//		this(responseCode, inputStream, null);
//	}
//
//	IdentityMockConnection(final int responseCode, final InputStream inputStream, final InputStream errorStream) {
//		this.responseCode = responseCode > 0 ? responseCode : 200;
//		this.inputStream = inputStream;
//		this.errorStream = errorStream;
//	}
//
//	@Override
//	public InputStream getInputStream() {
//		getInputStreamCalled = true;
//		return inputStream;
//	}
//
//	@Override
//	public InputStream getErrorStream() {
//		getErrorStreamCalled = true;
//		return errorStream;
//	}
//
//	@Override
//	public int getResponseCode() {
//		return responseCode;
//	}
//
//	@Override
//	public String getResponseMessage() {
//		return null;
//	}
//
//	@Override
//	public String getResponsePropertyValue(String s) {
//		return null;
//	}
//
//	@Override
//	public void close() {
//		if (inputStream != null) {
//			try {
//				inputStream.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//}