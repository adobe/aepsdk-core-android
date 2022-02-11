//package com.adobe.marketing.mobile;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
//class TestableRulesEngine extends RulesEngine {
//
//	class ProcessRulesArgument {
//		Event event;
//
//		ProcessRulesArgument(Event event) {
//			this.event = event;
//		}
//	}
//
//	public TestableRulesEngine(EventHub hub, PlatformServices services) {
//		super(hub, services);
//	}
//
//	int processRulesCalledTimes = 0;
//	List<ProcessRulesArgument> processRulesArguments = new ArrayList<ProcessRulesArgument>();
//
//	@Override
//	void processRules(Event event) {
//		processRulesCalledTimes++;
//		processRulesArguments.add(new ProcessRulesArgument(event));
//		super.processRules(event);
//	}
//
//
//	protected RulesEngineDispatcherRulesEngineResponseContent getDispatcherReturnValue;
//	protected int getDispatcherCalledCount = 0;
//	@Override
//	protected RulesEngineDispatcherRulesEngineResponseContent getDispatcher() {
//		++getDispatcherCalledCount;
//		return getDispatcherReturnValue;
//	}
//
//
//	File onRulesDownloadedCachedFileParameter;
//	String onRulesDownloadedUrlParameter;
//	@Override
//	void onRulesDownloaded(File cachedFile, String url) {
//		onRulesDownloadedCachedFileParameter = cachedFile;
//		onRulesDownloadedUrlParameter = url;
//	}
//}
