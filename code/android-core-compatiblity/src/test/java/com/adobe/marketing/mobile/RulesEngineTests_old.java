//
//package com.adobe.marketing.mobile;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.Assert.*;
//
//public class RulesEngineTests extends BaseTest {
//    private static final String EVENT_DATA_RULES_URL_KEY = "rules.url";
//    private static final String TRIGGERED_CONSEQUENCE_KEY = "triggeredconsequence";
//
//    private static final String LIFECYCLE_STATE_OWNER_NAME = "com.adobe.module.lifecycle";
//    private static final String DETAIL_KEY = "detail";
//    private static final String TYPE_KEY = "type";
//    private static final String HTML_KEY = "html";
//    private static final String ASSETS_PATH_KEY = "assetsPath";
//    private static final String TEMPLATEURL_KEY = "templateurl";
//
//    @Before
//    public void setupTests() {
//        super.beforeEach();
//        Log.setLoggingService(platformServices.getLoggingService());
//        Log.setLogLevel(LoggingMode.VERBOSE);
//    }
//
//    @After
//    public void tearDownTests() {
//        //Reset logger
//        Log.setLogLevel(LoggingMode.ERROR);
//        Log.setLoggingService(null);
//    }
//
//
//    @Test
//    public void downloadRules_When_RulesURlNull_Then_DownloadDoesNotKickOff() {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//        //Test
//        rulesEngine.downloadRules(null);
//        //Verify
//        assertFalse("connectUrlAsync() should not have been called!",
//                ((MockNetworkService)platformServices.getNetworkService()).connectUrlAsyncWasCalled);
//
//    }
//
//    @Test
//    public void downloadRules_When_RulesURlInValid_Then_DownloadShouldNotKickOff() {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//
//        EventData configRulesEventData = new EventData();
//        ArrayList<String> urls = new ArrayList<String>();
//        urls.add("invalid.com");
//        configRulesEventData.putObject(EVENT_DATA_RULES_URL_KEY, urls);
//        eventHub.setSharedState(EventDataKeys.Configuration.MODULE_NAME, configRulesEventData);
//
//        Event testEvent = new Event.Builder("RulesURlInValidTest", EventType.RULES_ENGINE, EventSource.REQUEST_CONTENT).build();
//
//        //Test
//        rulesEngine.downloadRules(testEvent);
//        //Verify
//        assertFalse("connectUrlAsync() should not have been called!",
//                ((MockNetworkService)platformServices.getNetworkService()).connectUrlAsyncWasCalled);
//
//    }
//
//    @Test
//    public void downloadRules_When_RulesURlValid_Then_DownloadShouldKickOff() throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//        rulesEngine.dispatcher = rulesDispatcher;
//
//        EventData configRulesEventData = new EventData();
//        ArrayList<String> urls = new ArrayList<String>();
//        urls.add("http://sample.com");
//        configRulesEventData.putObject(EVENT_DATA_RULES_URL_KEY, urls);
//        eventHub.setSharedState(EventDataKeys.Configuration.MODULE_NAME, configRulesEventData);
//
//        Event testEvent = new Event.Builder("RulesURlValidTest", EventType.RULES_ENGINE, EventSource.REQUEST_CONTENT).build();
//
//        //Test
//        rulesEngine.downloadRules(testEvent);
//        waitForExecutor(rulesEngine.getExecutor());
//
//        //Verify
//        assertTrue("connectUrlAsync() should have been called!",
//                ((MockNetworkService)platformServices.getNetworkService()).connectUrlWasCalled);
//        assertTrue(rulesDispatcher.dispatchLoadedConsequenceWasCalled);
//    }
//
//    @Test
//    public void handleRulesConfigurationEvent_When_EventInvalid() throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//        String mockUrl = "https://mock.com";
//
//        EventData configurationEventData = new EventData();
//        List<String> urls = new ArrayList<String>();
//        urls.add(mockUrl);
//        configurationEventData.putObject("other.url", urls);
//
//        Event testEvent = new Event
//                .Builder("Configuration Event", EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT)
//                .setData(configurationEventData)
//                .build();
//
//        //Test
//        rulesEngine.handleRulesConfigurationEvent(testEvent);
//        waitForExecutor(rulesEngine.getExecutor());
//
//        //Verify
//        assertFalse("connectUrlAsync() should not have been called!",
//                ((MockNetworkService)platformServices.getNetworkService()).connectUrlAsyncWasCalled);
//        assertEquals(0, rulesEngine.rulesConfigurations.size());
//
//    }
//
//    @Test
//    public void handleRulesConfigurationEvent_When_EventContainsValidUrl() throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new TestableRulesEngine(eventHub, platformServices);
//        String mockUrl = "https://mock.com";
//
//        EventData configurationEventData = new EventData();
//        List<String> urls = new ArrayList<String>();
//        urls.add(mockUrl);
//        configurationEventData.putObject("rules.url", urls);
//
//        Event testEvent = new Event
//                .Builder("Configuration Event", EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT)
//                .setData(configurationEventData)
//                .build();
//
//        //Setup mock network
//        Map<String, String> requestProperties = new HashMap<String, String>();
//        requestProperties.put("Last-Modified", "Fri, 1 Jan 2010 00:00:00 UTC");
//
//        platformServices.mockNetworkService.connectUrlReturnValue = new MockConnection("test response", 200, "",
//                requestProperties);
//
//        //Test
//        rulesEngine.handleRulesConfigurationEvent(testEvent);
//        waitForExecutor(rulesEngine.getExecutor());
//
//        //Verify
//        assertTrue("connectUrlAsync() should have been called!",
//                ((MockNetworkService)platformServices.getNetworkService()).connectUrlWasCalled);
//        assertEquals(mockUrl,
//                ((MockNetworkService)platformServices.getNetworkService()).connectUrlParametersUrl);
//        assertEquals(mockUrl, rulesEngine.rulesConfigurations.get(0).getRulesUrl());
//
//    }
//
//    @Test
//    public void handleRulesConfigurationEvent_When_EventContainsValidUrlWithTokens() throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new TestableRulesEngine(eventHub, platformServices);
//        String mockUrl = "https://mock.com?{%~sdkver%}";
//        String expectedExpandedUrl = "https://mock.com?mockSdkVersion";
//
//        EventData configurationEventData = new EventData();
//        List<String> urls = new ArrayList<String>();
//        urls.add(mockUrl);
//        configurationEventData.putObject("rules.url", urls);
//
//        Event testEvent = new Event
//                .Builder("Configuration Event", EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT)
//                .setData(configurationEventData)
//                .build();
//
//        //Setup mock network
//        Map<String, String> requestProperties = new HashMap<String, String>();
//        requestProperties.put("Last-Modified", "Fri, 1 Jan 2010 00:00:00 UTC");
//
//        platformServices.mockNetworkService.connectUrlReturnValue = new MockConnection("test response", 200, "",
//                requestProperties);
//
//        //Test
//        rulesEngine.handleRulesConfigurationEvent(testEvent);
//        waitForExecutor(rulesEngine.getExecutor());
//
//        //Verify
//        assertTrue("connectUrlAsync() should have been called!",
//                ((MockNetworkService)platformServices.getNetworkService()).connectUrlWasCalled);
//        assertEquals(expectedExpandedUrl,
//                ((MockNetworkService)platformServices.getNetworkService()).connectUrlParametersUrl);
//        assertEquals(expectedExpandedUrl, rulesEngine.rulesConfigurations.get(0).getRulesUrl());
//
//    }
//
//    @Test
//    public void handleRulesConfigurationEvent_When_EventContainsMultipleValidUrlWithTokens() throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new TestableRulesEngine(eventHub, platformServices);
//        String mockUrl = "https://mock.com?{%~sdkver%}";
//        String mockAnotherUrl = "https://mock.another.com?{%~sdkver%}";
//        String expectedExpandedUrl = "https://mock.com?mockSdkVersion";
//        String expectedAnotherExpandedUrl = "https://mock.another.com?mockSdkVersion";
//
//        EventData configurationEventData = new EventData();
//        List<String> urls = new ArrayList<String>();
//        urls.add(mockUrl);
//        urls.add(mockAnotherUrl);
//        configurationEventData.putObject("rules.url", urls);
//
//        Event testEvent = new Event
//                .Builder("Configuration Event", EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT)
//                .setData(configurationEventData)
//                .build();
//
//        //Setup mock network
//        Map<String, String> requestProperties = new HashMap<String, String>();
//        requestProperties.put("Last-Modified", "Fri, 1 Jan 2010 00:00:00 UTC");
//
//        platformServices.mockNetworkService.connectUrlReturnValue = new MockConnection("test response", 200, "",
//                requestProperties);
//
//        //Test
//        rulesEngine.handleRulesConfigurationEvent(testEvent);
//        waitForExecutor(rulesEngine.getExecutor());
//
//        //Verify
//        assertTrue("connectUrlAsync() should have been called!",
//                ((MockNetworkService)platformServices.getNetworkService()).connectUrlWasCalled);
//        assertEquals(expectedExpandedUrl, rulesEngine.rulesConfigurations.get(0).getRulesUrl());
//        assertEquals(expectedAnotherExpandedUrl, rulesEngine.rulesConfigurations.get(1).getRulesUrl());
//
//    }
//
//    @Test
//    public void handleRulesConfigurationEvent_When_EventContainsMultipleValidUrls() throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new TestableRulesEngine(eventHub, platformServices);
//        String mockUrl = "https://mock.com";
//        String mockAnotherUrl = "https://mock.another.com";
//
//        EventData configurationEventData = new EventData();
//        List<String> urls = new ArrayList<String>();
//        urls.add(mockUrl);
//        urls.add(mockAnotherUrl);
//        configurationEventData.putObject("rules.url", urls);
//
//        Event testEvent = new Event
//                .Builder("Configuration Event", EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT)
//                .setData(configurationEventData)
//                .build();
//
//        //Setup mock network
//        Map<String, String> requestProperties = new HashMap<String, String>();
//        requestProperties.put("Last-Modified", "Fri, 1 Jan 2010 00:00:00 UTC");
//
//        platformServices.mockNetworkService.connectUrlReturnValue = new MockConnection("test response", 200, "",
//                requestProperties);
//
//        //Test
//        rulesEngine.handleRulesConfigurationEvent(testEvent);
//        waitForExecutor(rulesEngine.getExecutor());
//
//        //Verify
//        assertTrue("connectUrlAsync() should have been called!",
//                ((MockNetworkService)platformServices.getNetworkService()).connectUrlWasCalled);
//        assertEquals(mockUrl, rulesEngine.rulesConfigurations.get(0).getRulesUrl());
//        assertEquals(mockAnotherUrl, rulesEngine.rulesConfigurations.get(1).getRulesUrl());
//
//
//    }
//
//    @Test
//    public void processRules_When_ValidDataAndRulesExists_Then_DispatchAction() throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//
//        EventData lifecycleData = new EventData();
//        Map<String, String> lifecycleSharedState = new HashMap<String, String>();
//        lifecycleSharedState.put("lckey", "lcvalue");
//        lifecycleData.putMap("lifecycle.contextData", lifecycleSharedState);
//        eventHub.setSharedState(LIFECYCLE_STATE_OWNER_NAME, lifecycleData);
//
//        EventData testEventData = new EventData();
//        testEventData.putString("key1", "values1");
//        Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
//                EventSource.REQUEST_CONTENT).setData(testEventData).build();
//
//        //Setup rules config
//        RulesConfiguration rulesConfiguration = new RulesConfiguration(platformServices
//                .fakeJsonUtilityService);
//        File rulesDirectoryFile = getResource("rules_unit_test1");
//        rulesConfiguration.setRulesFromDirectory(rulesDirectoryFile);
//        rulesEngine.rulesConfigurations.add(rulesConfiguration);
//
//        //Test
//        rulesEngine.processRules(testEvent);
//
//        //Verify
//        assertTrue("An event should have been dispatched", eventHub.isDispatchedCalled);
//        Map<String, Object> triggeredConsequenceMap = (Map<String, Object>) eventHub.dispatchedEvent
//                .getData().getObject(TRIGGERED_CONSEQUENCE_KEY);
//        assertEquals(rulesDirectoryFile + File.separator + "assets", triggeredConsequenceMap.get(ASSETS_PATH_KEY));
//
//        Map<String, Object> detail = (Map<String, Object>) triggeredConsequenceMap.get(DETAIL_KEY);
//        assertEquals("fullscreen", detail.get("template"));
//        assertEquals("48181acd22b3edaebc8a447868a7df7ce629920a.html", detail.get(HTML_KEY));
//
//    }
//
//    @Test
//    public void processRules_When_ValidRule_WithStateKeyPrefix_Then_DispatchAction()
//            throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//
//        EventData lifecycleData = new EventData();
//        Map<String, String> lifecycleSharedState = new HashMap<String, String>();
//        lifecycleSharedState.put("lckey", "lcvalue");
//        lifecycleData.putMap("lifecycle.contextData", lifecycleSharedState);
//        eventHub.setSharedState(LIFECYCLE_STATE_OWNER_NAME, lifecycleData);
//
//        EventData testEventData = new EventData();
//        testEventData.putString("key1", "values1");
//        Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
//                EventSource.REQUEST_CONTENT).setData(testEventData).build();
//
//        //Setup rules config
//        File file = getResource("rules_unit_test1");
//        RulesConfiguration rulesConfiguration = new RulesConfiguration(platformServices
//                .fakeJsonUtilityService);
//        rulesConfiguration.setRulesFromDirectory(file);
//        rulesEngine.rulesConfigurations.add(rulesConfiguration);
//
//        //Test
//        rulesEngine.processRules(testEvent);
//
//        //Verify
//        assertTrue("An event should have been dispatched", eventHub.isDispatchedCalled);
//    }
//
//    @Test
//    public void processRules_When_InvalidRule_WithEventTypeKeyPrefix_Then_NoActionsShouldBeDispatched() throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//
//        EventData testEventData = new EventData();
//        testEventData.putString("key1", "values1");
//        Event testEvent = new Event.Builder("test", EventType.LIFECYCLE,
//                EventSource.REQUEST_CONTENT).setData(testEventData).build();
//
//        //Setup rules config
//        File file = getResource("rules_unit_test3");
//        RulesConfiguration rulesConfiguration = new RulesConfiguration(platformServices
//                .fakeJsonUtilityService);
//        rulesConfiguration.setRulesFromDirectory(file);
//        rulesEngine.rulesConfigurations.add(rulesConfiguration);
//
//        //Test
//        rulesEngine.processRules(testEvent);
//
//        //Verify
//        assertFalse("No event should have been dispatched", eventHub.isDispatchedCalled);
//    }
//
//    @Test
//    public void processRules_When_ValidRule_WithEvenTypeKey_Then_ActionsShouldBeDispatched() throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//
//        EventData testEventData = new EventData();
//        testEventData.putString("key1", "values1");
//        Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
//                EventSource.REQUEST_CONTENT).setData(testEventData).build();
//
//        //Setup rules config
//        File file = getResource("rules_unit_test3");
//        RulesConfiguration rulesConfiguration = new RulesConfiguration(platformServices
//                .fakeJsonUtilityService);
//        rulesConfiguration.setRulesFromDirectory(file);
//        rulesEngine.rulesConfigurations.add(rulesConfiguration);
//
//        //Test
//        rulesEngine.processRules(testEvent);
//
//        //Verify
//        assertTrue("An event should have been dispatched", eventHub.isDispatchedCalled);
//    }
//
//    @Test
//    public void processRules_When_NoActionsSpecifiedInRule_Then_NoEventShouldBeDispatched() throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//
//        EventData testEventData = new EventData();
//        testEventData.putString("key1", "values1");
//        Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
//                EventSource.REQUEST_CONTENT).setData(testEventData).build();
//
//        //Setup rules config
//        File file = getResource("rules_unit_test4_noaction");
//        RulesConfiguration rulesConfiguration = new RulesConfiguration(platformServices
//                .fakeJsonUtilityService);
//        rulesConfiguration.setRulesFromDirectory(file);
//        rulesEngine.rulesConfigurations.add(rulesConfiguration);
//
//        //Test
//        rulesEngine.processRules(testEvent);
//
//        //Verify
//        assertFalse("No event should have been dispatched", eventHub.isDispatchedCalled);
//    }
//
//    @Test
//    public void processRules_When_NullKeyInConditionData_Then_NoEventShouldBeDispatched() throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//
//        EventData testEventData = new EventData();
//        testEventData.putString(null, "values1");
//        Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
//                EventSource.REQUEST_CONTENT).setData(testEventData).build();
//
//        //Setup rules config
//        File file = getResource("rules_unit_test3");
//        RulesConfiguration rulesConfiguration = new RulesConfiguration(platformServices
//                .fakeJsonUtilityService);
//        rulesConfiguration.setRulesFromDirectory(file);
//        rulesEngine.rulesConfigurations.add(rulesConfiguration);
//
//        //Test
//        rulesEngine.processRules(testEvent);
//
//        //Verify
//        assertFalse("No event should have been dispatched", eventHub.isDispatchedCalled);
//    }
//
//    @Test
//    public void
//    processRules_When_ValidRules_With_ValidConditionDataExists_And_SharedStatesIsNull_Then_CorrespondingConsequenceShouldBeDispatched()
//            throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//
//        EventData testEventData = new EventData();
//        testEventData.putString("key1", "values1");
//        Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
//                EventSource.REQUEST_CONTENT).setData(testEventData).build();
//
//        //Setup rules config
//        File file = getResource("rules_unit_test2");
//        RulesConfiguration rulesConfiguration = new RulesConfiguration(platformServices
//                .fakeJsonUtilityService);
//        rulesConfiguration.setRulesFromDirectory(file);
//        rulesEngine.rulesConfigurations.add(rulesConfiguration);
//
//        //Test
//        rulesEngine.processRules(testEvent);
//
//        //Verify
//        assertTrue("An event should have been dispatched", eventHub.isDispatchedCalled);
//    }
//
//    @Test
//    public void
//    processRules_When_ValidRules_With_ValidConditionDataExists_And_SharedStatesIsEmpty_Then_CorrespondingConsequenceShouldBeDispatched()
//            throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//
//        EventData testEventData = new EventData();
//        testEventData.putString("key1", "values1");
//        Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
//                EventSource.REQUEST_CONTENT).setData(testEventData).build();
//
//        //Setup rules config
//        File file = getResource("rules_unit_test2");
//        RulesConfiguration rulesConfiguration = new RulesConfiguration(platformServices
//                .fakeJsonUtilityService);
//        rulesConfiguration.setRulesFromDirectory(file);
//        rulesEngine.rulesConfigurations.add(rulesConfiguration);
//
//        //Test
//        rulesEngine.processRules(testEvent);
//
//        //Verify
//        assertTrue("An event should have been dispatched", eventHub.isDispatchedCalled);
//    }
//
//    @Test
//    public void
//    processRules_When_ValidRules_With_NullConditionData_And_SharedStatesExists_Then_CorrespondingConsequenceShouldBeDispatched()
//            throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//
//        EventData testEventData = new EventData();
//        testEventData.putString("key1", "values1");
//        Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
//                EventSource.REQUEST_CONTENT).setData(testEventData).build();
//
//
//        //Setup rules config
//        File file = getResource("rules_unit_test2");
//        RulesConfiguration rulesConfiguration = new RulesConfiguration(platformServices
//                .fakeJsonUtilityService);
//        rulesConfiguration.setRulesFromDirectory(file);
//        rulesEngine.rulesConfigurations.add(rulesConfiguration);
//
//        //Test
//        rulesEngine.processRules(testEvent);
//
//        //Verify
//        assertTrue("An event should have been dispatched", eventHub.isDispatchedCalled);
//    }
//
//
//    @Test
//    public void
//    processRules_When_ValidRules_With_EmptyConditionData_And_SharedStatesExists_Then_CorrespondingConsequenceShouldBeDispatched()
//            throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//
//        EventData testEventData = new EventData();
//        testEventData.putString("key1", "values1");
//        Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
//                EventSource.REQUEST_CONTENT).setData(testEventData).build();
//
//        //Setup rules config
//        File file = getResource("rules_unit_test2");
//        RulesConfiguration rulesConfiguration = new RulesConfiguration(platformServices
//                .fakeJsonUtilityService);
//        rulesConfiguration.setRulesFromDirectory(file);
//        rulesEngine.rulesConfigurations.add(rulesConfiguration);
//
//        //Empty condition data
//        Map<String, Object> data = new HashMap<String, Object>();
//
//        //Test
//        rulesEngine.processRules(testEvent);
//
//        //Verify
//        assertTrue("An event should have been dispatched", eventHub.isDispatchedCalled);
//    }
//
//    @Test
//    public void getRemoteDownloader_When_UrlIsNull_Then_ReturnsValidDownloaderInstance() throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//        //Test
//        RemoteDownloader remoteDownloader =  rulesEngine.getRemoteDownloader(null);
//        //Verify
//        assertNotNull(remoteDownloader);
//    }
//
//    @Test
//    public void getRemoteDownloader_When_UrlIsEmpty_Then_ReturnsValidDownloaderInstance() throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//        //Test
//        RemoteDownloader remoteDownloader =  rulesEngine.getRemoteDownloader("");
//        //Verify
//        assertNotNull(remoteDownloader);
//    }
//
//    @Test
//    public void getRemoteDownloader_When_UrlIsValid_Then_ReturnsValidDownloaderInstance() throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//        //Test
//        RemoteDownloader remoteDownloader =  rulesEngine.getRemoteDownloader("http://www.hello.com");
//        //Verify
//        assertNotNull(remoteDownloader);
//    }
//
//    @Test
//    public void onRulesDownloaded_When_FileIsNull_Then_SetsRulesConfigToNull() throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//        //Setup rules config
//        File file = getResource("rules_unit_test1");
//        RulesConfiguration rulesConfiguration = new RulesConfiguration(platformServices
//                .fakeJsonUtilityService);
//        rulesConfiguration.setRulesUrl("http://www.hello.com");
//        rulesConfiguration.setRulesFromDirectory(file);
//        rulesEngine.rulesConfigurations.add(rulesConfiguration);
//
//        //Test
//        rulesEngine.onRulesDownloaded(null, "http://www.hello.com");
//
//        //Verify that rules do not change
//        assertEquals(0, rulesEngine.rulesConfigurations.size());
//    }
//
//    @Test
//    public void onRulesDownloaded_When_FileIsValid_Then_OverridesExistingRulesConfiguration() throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//        //Setup rules config
//
//        File file = getResource("rules_unit_test1");
//        RulesConfiguration rulesConfiguration = new RulesConfiguration(platformServices
//                .fakeJsonUtilityService);
//        rulesConfiguration.setRulesUrl("http://www.hello.com");
//        rulesConfiguration.setRulesFromDirectory(file);
//        rulesEngine.rulesConfigurations.add(rulesConfiguration);
//
//        Rule parsedRule = rulesEngine.rulesConfigurations.get(0).getRules().get(0);
//
//        //Test
//        file = getResource("rules_unit_test3");
//        RulesRemoteDownloader remoteDownloader =  rulesEngine.getRemoteDownloader("http://www.hello.com");
//        remoteDownloader.processBundle(file);
//        //Verify
//        //Old rules are replaced with new rules.
//        List<Rule> rules = rulesEngine.rulesConfigurations.get(0).getRules();
//        assertEquals(1, rules.size());
//        //The old and the new rules are not equal.
//        assertEquals(parsedRule, rules.get(0));
//    }
//
//    @Test
//    public void processRules_When_DispatcherNull_Then_ShouldNotDispatchConsequence() throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new TestableRulesEngine(eventHub, platformServices);
//        //Force the dispatcher to be null!
//        ((TestableRulesEngine)rulesEngine).getDispatcherReturnValue = null;
//
//        EventData testEventData = new EventData();
//        testEventData.putString("key1", "values1");
//        Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
//                EventSource.REQUEST_CONTENT).setData(testEventData).build();
//
//        //Setup rules config
//        File file = getResource("rules_unit_test1");
//        RulesConfiguration rulesConfiguration = new RulesConfiguration(platformServices
//                .fakeJsonUtilityService);
//        rulesConfiguration.setRulesFromDirectory(file);
//        rulesEngine.rulesConfigurations.add(rulesConfiguration);
//
//        //Test
//        rulesEngine.processRules(testEvent);
//
//        //Verify
//        assertFalse("No event should have been dispatched", eventHub.isDispatchedCalled);
//    }
//
//    @Test
//    public void processRules_When_ValidEndDate_Then_DispatchConsequence() throws Exception {
//        //Setup
//        TestableRulesEngine rulesEngine = new TestableRulesEngine(eventHub, platformServices);
//
//        EventData lifecycleData = new EventData();
//        Map<String, String> lifecycleSharedState = new HashMap<String, String>();
//        lifecycleSharedState.put("lckey", "lcvalue");
//        lifecycleData.putMap("lifecycle.contextData", lifecycleSharedState);
//        eventHub.setSharedState(LIFECYCLE_STATE_OWNER_NAME, lifecycleData);
//
//        EventData testEventData = new EventData();
//        testEventData.putString("key1", "values1");
//        Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
//                EventSource.REQUEST_CONTENT).setData(testEventData).build();
//
//		/*
//		This rule is setup to have enddate = 4070908800 which is Thursday, January 1, 2099 12:00:00 AM. This should be greater than current date.
//		 */
//        //Setup rules config
//        File file = getResource("rules_enddate_test");
//        RulesConfiguration rulesConfiguration = new RulesConfiguration(platformServices
//                .fakeJsonUtilityService);
//        rulesConfiguration.setRulesFromDirectory(file);
//        rulesEngine.rulesConfigurations.add(rulesConfiguration);
//
//
//        //Test
//        rulesEngine.processRules(testEvent);
//
//        //Verify
//        assertEquals(1, rulesEngine.getDispatcherCalledCount);
//    }
//
//    @Test
//    public void processRules_When_ValidStartDateAndEndDate_Then_DispatchConsequence() throws Exception {
//        //Setup
//        TestableRulesEngine rulesEngine = new TestableRulesEngine(eventHub, platformServices);
//
//        EventData lifecycleData = new EventData();
//        Map<String, String> lifecycleSharedState = new HashMap<String, String>();
//        lifecycleSharedState.put("lckey", "lcvalue");
//        lifecycleData.putMap("lifecycle.contextData", lifecycleSharedState);
//        eventHub.setSharedState(LIFECYCLE_STATE_OWNER_NAME, lifecycleData);
//
//        EventData testEventData = new EventData();
//        testEventData.putString("key1", "values1");
//        Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
//                EventSource.REQUEST_CONTENT).setData(testEventData).build();
//
//		/*
//		This rule is setup to have enddate = 4070908800 (Thursday, January 1, 2099 12:00:00 AM ), which should be greater than current date;
//		and startDate = 1514764800 (Monday, January 1, 2018 12:00:00 AM) which should be less than the current date.
//		 */
//        //Setup rules config
//        File file = getResource("rules_startdate_test");
//        RulesConfiguration rulesConfiguration = new RulesConfiguration(platformServices
//                .fakeJsonUtilityService);
//        rulesConfiguration.setRulesFromDirectory(file);
//        rulesEngine.rulesConfigurations.add(rulesConfiguration);
//
//
//        //Test
//        rulesEngine.processRules(testEvent);
//
//        //Verify
//        assertEquals(1, rulesEngine.getDispatcherCalledCount);
//    }
//
//    @Test
//    public void processRules_When_ConsequenceDetailContainsTokens_Then_DispatchedEventContainsExpandedValues() throws
//            Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//
//        EventData lifecycleData = new EventData();
//        Map<String, String> lifecycleSharedState = new HashMap<String, String>();
//        lifecycleSharedState.put("lckey", null);
//        lifecycleData.putMap("lifecycle.contextData", lifecycleSharedState);
//        eventHub.setSharedState(LIFECYCLE_STATE_OWNER_NAME, lifecycleData);
//
//        EventData testEventData = new EventData();
//        testEventData.putString("action", "testAction");
//        Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
//                EventSource.REQUEST_CONTENT).setData(testEventData).build();
//
//        //Setup rules config
//        RulesConfiguration rulesConfiguration = new RulesConfiguration(platformServices
//                .fakeJsonUtilityService);
//        File file = getResource("rules_unit_test_with_tokens");
//        rulesConfiguration.setRulesFromDirectory(file);
//        rulesEngine.rulesConfigurations.add(rulesConfiguration);
//
//        //Test
//        rulesEngine.processRules(testEvent);
//
//        //Verify
//        assertTrue("An event should have been dispatched", eventHub.isDispatchedCalled);
//        Map<String, Object> triggeredConsequenceMap = (Map<String, Object>) eventHub.dispatchedEvent
//                .getData().getObject(TRIGGERED_CONSEQUENCE_KEY);
//        Map<String, Object> detail = (Map<String, Object>) triggeredConsequenceMap.get(DETAIL_KEY);
//        //Nested detail containers
//        Map<String, Object> nestedMap = (Map<String, Object>) detail.get("testSampleMap");
//        List<Object> nestedList = (List<Object>) detail.get("testSampleList");
//
//        assertEquals("https://www.endpoint.com/post/mockSdkVersion", detail.get(TEMPLATEURL_KEY));
//        assertEquals("mockSdkVersion", nestedMap.get("version"));
//        assertEquals("", nestedMap.get("stateData"));
//        assertEquals("mockSdkVersion", nestedList.get(0));
//        assertEquals("noToken", nestedList.get(1));
//
//    }
//
//    @Test
//    public void processRules_When_ConsequenceDetailContainsNestedJsonElements_Then_DispatchedEventContainsExpandedValues()
//            throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//
//        EventData lifecycleData = new EventData();
//        Map<String, String> lifecycleSharedState = new HashMap<String, String>();
//        lifecycleSharedState.put("lckey", "lcvalue");
//        lifecycleData.putMap("lifecycle.contextData", lifecycleSharedState);
//        eventHub.setSharedState(LIFECYCLE_STATE_OWNER_NAME, lifecycleData);
//
//        EventData testEventData = new EventData();
//        testEventData.putString("action", "testAction");
//        Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
//                EventSource.REQUEST_CONTENT).setData(testEventData).build();
//
//        //Setup rules config
//        RulesConfiguration rulesConfiguration = new RulesConfiguration(platformServices
//                .fakeJsonUtilityService);
//        File file = getResource("rules_unit_test_with_tokens_and_nestedcontainers");
//        rulesConfiguration.setRulesFromDirectory(file);
//        rulesEngine.rulesConfigurations.add(rulesConfiguration);
//
//        //Test
//        rulesEngine.processRules(testEvent);
//
//        //Verify
//        assertTrue("An event should have been dispatched", eventHub.isDispatchedCalled);
//        Map<String, Object> triggeredConsequenceMap = (Map<String, Object>) eventHub.dispatchedEvent
//                .getData().getObject(TRIGGERED_CONSEQUENCE_KEY);
//        Map<String, Object> detail = (Map<String, Object>) triggeredConsequenceMap.get(DETAIL_KEY);
//        //Nested detail containers
//        Map<String, Object> nestedMap = (Map<String, Object>) detail.get("testSampleMap");
//        List<Object> nestedList = (List<Object>) detail.get("testSampleList");
//
//        assertEquals("https://www.endpoint.com/post/mockSdkVersion", detail.get(TEMPLATEURL_KEY));
//        assertEquals("mockSdkVersion", nestedMap.get("version"));
//        assertEquals("lcvalue", nestedMap.get("stateData"));
//        assertEquals("mockSdkVersion", ((Map<String, Object>)nestedList.get(0)).get("nestedVersion"));
//        assertEquals("noToken", nestedList.get(1));
//        assertEquals("nestedListNotoken", ((List<Object>)nestedList.get(2)).get(0));
//        assertEquals(12345, ((List<Object>)nestedList.get(2)).get(1));
//
//    }
//
//    @Test
//    public void processRules_When_ConsequenceContainsUnknownType_Then_ConsequenceIsNotIgnored()
//            throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//
//        EventData testEventData = new EventData();
//        testEventData.putString("action", "testAction");
//        Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
//                EventSource.REQUEST_CONTENT).setData(testEventData).build();
//
//        //Setup rules config
//        RulesConfiguration rulesConfiguration = new RulesConfiguration(platformServices
//                .fakeJsonUtilityService);
//        File file = getResource("rules_json_extra_keys");
//        rulesConfiguration.setRulesFromDirectory(file);
//        rulesEngine.rulesConfigurations.add(rulesConfiguration);
//
//        //Test
//        rulesEngine.processRules(testEvent);
//
//        //Verify
//        assertTrue("An event should have been dispatched", eventHub.isDispatchedCalled);
//        Map<String, Object> triggeredConsequenceMap = (Map<String, Object>) eventHub.dispatchedEvent
//                .getData().getObject(TRIGGERED_CONSEQUENCE_KEY);
//        Map<String, Object> detail = (Map<String, Object>) triggeredConsequenceMap.get(DETAIL_KEY);
//        assertEquals("https://www.endpoint.com/post/mockSdkVersion", detail.get(TEMPLATEURL_KEY));
//        assertEquals("unknownType", triggeredConsequenceMap.get(TYPE_KEY));
//    }
//
//    @Test
//    public void processRules_onRulesDownloaded_WhenCacheFileNull_Then_RemoveRuleConfiguration() throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//        String mockUrl = "http://mock.url";
//
//
//        RulesConfiguration configuration = new RulesConfiguration(rulesEngine.getPlatformServices().getJsonUtilityService());
//        configuration.setRulesUrl(mockUrl);
//
//        rulesEngine.rulesConfigurations.add(configuration);
//
//        //Test
//        rulesEngine.onRulesDownloaded(null, mockUrl);
//
//        //Verify
//        assertEquals(0, rulesEngine.rulesConfigurations.size());
//    }
//
//    @Test
//    public void processRules_onRulesDownloaded_WhenCacheFileValid_ThenSetRulesFromCachedFile() throws Exception {
//        //Setup
//        RulesEngine rulesEngine = new RulesEngine(eventHub, platformServices);
//        String mockUrl = "http://mock.url";
//
//
//        RulesConfiguration configuration = new MockRulesConfiguration(
//                rulesEngine.getPlatformServices().getJsonUtilityService());
//        configuration.setRulesUrl(mockUrl);
//
//        rulesEngine.rulesConfigurations.add(configuration);
//        File mockCachedFile = new File("cachedFile");
//
//        //Test
//        rulesEngine.onRulesDownloaded(mockCachedFile, mockUrl);
//
//        //Verify
//        assertEquals(1, rulesEngine.rulesConfigurations.size());
//        assertEquals(mockCachedFile.getName(),
//                ((MockRulesConfiguration)rulesEngine.rulesConfigurations.get(0)).setRulesFromFileParameter.getName());
//    }
//
//    @Test
//    public void processRules_onRulesDownloaded_WhenNoRuleConfigurationSet_ThenRulesConfigurationDoesNotChange() {
//        //Setup
//        RulesEngine rulesEngine = new TestableRulesEngine(eventHub, platformServices);
//        String mockUrl = "http://mock.url";
//
//        File mockCachedFile = new File("cachedFile");
//
//        //Test
//        rulesEngine.onRulesDownloaded(mockCachedFile, mockUrl);
//
//        //Verify
//        assertEquals(0, rulesEngine.rulesConfigurations.size());
//    }
//}
