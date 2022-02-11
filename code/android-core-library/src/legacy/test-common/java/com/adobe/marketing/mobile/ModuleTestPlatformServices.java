package com.adobe.marketing.mobile;

public class ModuleTestPlatformServices implements PlatformServices {
	private TestableNetworkService testableNetworkService;
	private FakeJsonUtilityService fakeJsonUtilityService;
	private FakeLocalStorageService fakeLocalStorageService;
	private FakeLoggingService fakeLoggingService;
	private FakeDatabaseService mockStructuredDataService;
	private MockSystemInfoService mockSystemInfoService;
	private MockSystemNotificationService mockSystemNotificationService;
	private MockUIService mockUIService;
	private MockDeepLinkService mockDeepLinkService;
	private FakeCompressedFileService fakeCompressedFileService;
	private FakeEncodingService fakeEncodingService;

	public ModuleTestPlatformServices() {
		testableNetworkService = new TestableNetworkService();
		fakeJsonUtilityService = new FakeJsonUtilityService();
		fakeLocalStorageService = new FakeLocalStorageService();
		fakeLoggingService = new FakeLoggingService();
		mockStructuredDataService = new FakeDatabaseService();
		mockSystemInfoService = new MockSystemInfoService();
		mockSystemNotificationService = new MockSystemNotificationService();
		mockUIService = new MockUIService();
		mockDeepLinkService = new MockDeepLinkService();
		fakeCompressedFileService = new FakeCompressedFileService();
		fakeEncodingService = new FakeEncodingService();
	}

	public TestableNetworkService getTestableNetworkService() {
		return testableNetworkService;
	}

	public MockSystemNotificationService getMockSystemNotificationService() {
		return mockSystemNotificationService;
	}

	public MockSystemInfoService getMockSystemInfoService() {
		return mockSystemInfoService;
	}

	public MockUIService getMockUIService() {
		return mockUIService;
	}

	public FakeDatabaseService getFakeDatabaseService() {
		return mockStructuredDataService;
	}

	@Override
	public LoggingService getLoggingService() {
		return fakeLoggingService;
	}

	@Override
	public NetworkService getNetworkService() {
		return testableNetworkService;
	}

	@Override
	public LocalStorageService getLocalStorageService() {
		return fakeLocalStorageService;
	}

	@Override
	public DatabaseService getDatabaseService() {
		return mockStructuredDataService;
	}

	@Override
	public SystemNotificationService getSystemNotificationService() {
		return mockSystemNotificationService;
	}

	@Override
	public SystemInfoService getSystemInfoService() {
		return mockSystemInfoService;
	}

	@Override
	public UIService getUIService() {
		return null;
	}

	@Override
	public JsonUtilityService getJsonUtilityService() {
		return fakeJsonUtilityService;
	}

	@Override
	public DeepLinkService getDeepLinkService() {
		return mockDeepLinkService;
	}

	@Override
	public EncodingService getEncodingService() {
		return fakeEncodingService;
	}

	@Override
	public CompressedFileService getCompressedFileService() {
		return fakeCompressedFileService;
	}

	public void setFakeLocalStorageService(FakeLocalStorageService fakeLocalStorageService) {
		this.fakeLocalStorageService = fakeLocalStorageService;
	}

	public void setMockStructuredDataService(FakeDatabaseService structuredDataService) {
		this.mockStructuredDataService = structuredDataService;
	}

}
