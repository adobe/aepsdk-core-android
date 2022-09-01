///*
//  Copyright 2022 Adobe. All rights reserved.
//  This file is licensed to you under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License. You may obtain a copy
//  of the License at http://www.apache.org/licenses/LICENSE-2.0
//  Unless required by applicable law or agreed to in writing, software distributed under
//  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
//  OF ANY KIND, either express or implied. See the License for the specific language
//  governing permissions and limitations under the License.
// */
//
//package com.adobe.marketing.mobile;
//
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.junit.runners.Parameterized;
//
//import java.util.Arrays;
//import java.util.Collection;
//
//import static junit.framework.TestCase.*;
//
//@RunWith(Parameterized.class)
//public class LogTests {
//
//	static class MockLoggingService implements LoggingService {
//
//		static String traceTagParameter;
//		static String traceMessageParameter;
//		@Override
//		public void trace(String tag, String message) {
//			traceTagParameter = tag;
//			traceMessageParameter = message;
//		}
//
//		static String debugTagParameter;
//		static String debugMessageParameter;
//		@Override
//		public void debug(String tag, String message) {
//			debugTagParameter = tag;
//			debugMessageParameter = message;
//		}
//
//		static String warningTagParameter;
//		static String warningMessageParameter;
//		@Override
//		public void warning(String tag, String message) {
//			warningMessageParameter = message;
//			warningTagParameter = tag;
//
//		}
//
//		static String errorTagParameter;
//		static String errorMessageParameter;
//		@Override
//		public void error(String tag, String message) {
//			errorMessageParameter = message;
//			errorTagParameter = tag;
//		}
//
//		static String getTagParameter(final LoggingMode mode) {
//			switch (mode) {
//				case ERROR:
//					return errorTagParameter;
//
//				case WARNING:
//					return warningTagParameter;
//
//				case DEBUG:
//					return debugTagParameter;
//
//				case VERBOSE:
//					return traceTagParameter;
//			}
//
//			return "";
//		}
//
//		static String getMessageParameter(final LoggingMode mode) {
//			switch (mode) {
//				case ERROR:
//					return errorMessageParameter;
//
//				case WARNING:
//					return warningMessageParameter;
//
//				case DEBUG:
//					return debugMessageParameter;
//
//				case VERBOSE:
//					return traceMessageParameter;
//			}
//
//			return "";
//		}
//	}
//
//	public interface LogFunction {
//		void execute(String source, String msg, Object... args);
//	}
//
//	@Parameterized.Parameters
//	public static Collection<Object[]> data() {
//		return Arrays.asList(new Object[][] {
//			{
//				LoggingMode.ERROR, new LogFunction() {
//					@Override
//					public void execute(String source, String msg, Object... args) {
//						Log.error(source, msg, args);
//					}
//				}
//			},
//			{
//				LoggingMode.WARNING, new LogFunction() {
//					@Override
//					public void execute(String source, String msg, Object... args) {
//						Log.warning(source, msg, args);
//					}
//				}
//			},
//			{
//				LoggingMode.DEBUG, new LogFunction() {
//					@Override
//					public void execute(String source, String msg, Object... args) {
//						Log.debug(source, msg, args);
//					}
//				}
//			},
//			{
//				LoggingMode.VERBOSE, new LogFunction() {
//					@Override
//					public void execute(String source, String msg, Object... args) {
//						Log.trace(source, msg, args);
//					}
//				}
//			}
//		});
//	}
//
//	@Parameterized.Parameter(0)
//	public LoggingMode mode;
//
//	@Parameterized.Parameter(1)
//	public LogFunction func;
//
//
//	@BeforeClass
//	public static void beforeClass() {
//		Log.setLoggingService(new MockLoggingService());
//	}
//
//	@Before
//	public void before() {
//		MockLoggingService.traceMessageParameter = null;
//		MockLoggingService.traceTagParameter = null;
//		MockLoggingService.errorTagParameter = null;
//		MockLoggingService.errorMessageParameter = null;
//		MockLoggingService.debugMessageParameter = null;
//		MockLoggingService.debugTagParameter = null;
//		MockLoggingService.warningTagParameter = null;
//		MockLoggingService.warningMessageParameter = null;
//
//		Log.setLogLevel(LoggingMode.ERROR);
//	}
//
//	@Test
//	public void logNoFormatParameters() {
//		String source = "testSource";
//		String format = "test format string";
//		Log.setLogLevel(mode);
//		func.execute(source, format);
//		assertEquals(source, MockLoggingService.getTagParameter(mode));
//		assertEquals(format, MockLoggingService.getMessageParameter(mode));
//	}
//
//	@Test
//	public void logValidFormatParameter() {
//		String source = "testSource";
//		String format = "test format string %d";
//		int parameter = 1;
//		String msg = "test format string 1";
//		Log.setLogLevel(mode);
//		func.execute(source, format, parameter);
//		assertEquals(source, MockLoggingService.getTagParameter(mode));
//		assertEquals(msg, MockLoggingService.getMessageParameter(mode));
//	}
//
//	@Test
//	public void logInvalidFormatParameter() {
//		String source = "testSource";
//		String format = "test format string %x";
//		String parameter = "invalid"; // string type invalid for %x
//		String msg = format;
//		Log.setLogLevel(mode);
//		func.execute(source, format, parameter);
//		assertEquals(source, MockLoggingService.getTagParameter(mode));
//		assertEquals(msg, MockLoggingService.getMessageParameter(mode));
//	}
//
//	@Test
//	public void logInvalidFormatConversionCharacter() {
//		String source = "testSource";
//		String format = "test format string %v %u %w";
//		String parameter = "invalid";
//		String msg = format;
//		Log.setLogLevel(mode);
//		func.execute(source, format, parameter, parameter, parameter);
//		assertEquals(source, MockLoggingService.getTagParameter(mode));
//		assertEquals(msg, MockLoggingService.getMessageParameter(mode));
//	}
//
//	@Test
//	public void logInvalidFormatFlag() {
//		String source = "testSource";
//		String format = "test format string %-o"; // '-' invalid flag for base eight conversion
//		int parameter = 12;
//		String msg = format;
//		Log.setLogLevel(mode);
//		func.execute(source, format, parameter);
//		assertEquals(source, MockLoggingService.getTagParameter(mode));
//		assertEquals(msg, MockLoggingService.getMessageParameter(mode));
//	}
//
//	@Test
//	public void logNoMatchingFormatParameter() {
//		String source = "testSource";
//		String format = "test format string %s";
//		String msg = format;
//		Log.setLogLevel(mode);
//		func.execute(source, format); // no parameters provided for string format
//		assertEquals(source, MockLoggingService.getTagParameter(mode));
//		assertEquals(msg, MockLoggingService.getMessageParameter(mode));
//	}
//
//	@Test
//	public void logFormatParametersLessThanExpected() {
//		String source = "testSource";
//		String format = "test format string %s %d";
//		String parameter = "only one";
//		String msg = format;
//		Log.setLogLevel(mode);
//		func.execute(source, format, parameter);
//		assertEquals(source, MockLoggingService.getTagParameter(mode));
//		assertEquals(msg, MockLoggingService.getMessageParameter(mode));
//	}
//
//	@Test
//	public void logFormatParametersMoreThanExpected() {
//		String source = "testSource";
//		String format = "test format string %s";
//		String parameter = "only one";
//		String parameter2 = "second parameter";
//		String msg = "test format string only one";
//		Log.setLogLevel(mode);
//		func.execute(source, format, parameter, parameter2);
//		assertEquals(source, MockLoggingService.getTagParameter(mode));
//		assertEquals(msg, MockLoggingService.getMessageParameter(mode));
//	}
//
//	@Test
//	public void logErrorMessages() {
//		Log.setLogLevel(LoggingMode.ERROR);
//		func.execute("tag", "message");
//
//		switch (mode) {
//			case ERROR:
//				assertNotNull(MockLoggingService.getTagParameter(mode));
//				assertNotNull(MockLoggingService.getMessageParameter(mode));
//				break;
//
//			case WARNING:
//			case DEBUG:
//			case VERBOSE:
//				assertNull(MockLoggingService.getTagParameter(mode));
//				assertNull(MockLoggingService.getMessageParameter(mode));
//				break;
//
//			default:
//				fail("Unexpected LoggingMode parameter.");
//		}
//	}
//
//	@Test
//	public void logWarningMessages() {
//		Log.setLogLevel(LoggingMode.WARNING);
//		func.execute("tag", "message");
//
//		switch (mode) {
//			case ERROR:
//			case WARNING:
//				assertNotNull(MockLoggingService.getTagParameter(mode));
//				assertNotNull(MockLoggingService.getMessageParameter(mode));
//				break;
//
//			case DEBUG:
//			case VERBOSE:
//				assertNull(MockLoggingService.getTagParameter(mode));
//				assertNull(MockLoggingService.getMessageParameter(mode));
//				break;
//
//			default:
//				fail("Unexpected LoggingMode parameter.");
//		}
//
//	}
//
//	@Test
//	public void logDebugMessages() {
//		Log.setLogLevel(LoggingMode.DEBUG);
//		func.execute("tag", "message");
//
//		switch (mode) {
//			case ERROR:
//			case WARNING:
//			case DEBUG:
//				assertNotNull(MockLoggingService.getTagParameter(mode));
//				assertNotNull(MockLoggingService.getMessageParameter(mode));
//				break;
//
//			case VERBOSE:
//				assertNull(MockLoggingService.getTagParameter(mode));
//				assertNull(MockLoggingService.getMessageParameter(mode));
//				break;
//
//			default:
//				fail("Unexpected LoggingMode parameter.");
//		}
//	}
//
//	@Test
//	public void logVerboseMessages() {
//		Log.setLogLevel(LoggingMode.VERBOSE);
//		func.execute("tag", "message");
//
//		switch (mode) {
//			case ERROR:
//			case WARNING:
//			case DEBUG:
//			case VERBOSE:
//				assertNotNull(MockLoggingService.getTagParameter(mode));
//				assertNotNull(MockLoggingService.getMessageParameter(mode));
//				break;
//
//			default:
//				fail("Unexpected LoggingMode parameter.");
//		}
//	}
//
//	@Test
//	public void logNullSourceDoesNotPrint() {
//		Log.setLogLevel(mode);
//		func.execute(null, "message");
//		assertNull(MockLoggingService.getTagParameter(mode));
//		assertEquals("message", MockLoggingService.getMessageParameter(mode));
//	}
//
//	@Test
//	public void logNullMessageDoesNotPrint() {
//		Log.setLogLevel(mode);
//		func.execute("tag", null);
//		assertEquals("tag", MockLoggingService.getTagParameter(mode));
//		assertNull(MockLoggingService.getMessageParameter(mode));
//	}
//
//	@Test
//	public void logNullFormatDoesNotPrint() {
//		Log.setLogLevel(mode);
//		func.execute("tag", null, "format args");
//		assertEquals("tag", MockLoggingService.getTagParameter(mode));
//		assertNull(MockLoggingService.getMessageParameter(mode));
//	}
//
//}
