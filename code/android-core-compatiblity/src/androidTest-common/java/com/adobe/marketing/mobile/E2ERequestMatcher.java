package com.adobe.marketing.mobile;

public class E2ERequestMatcher extends E2ETestableNetworkService.NetworkRequestMatcher {
	private String urlFragment;
	E2ERequestMatcher(String urlFragment) {
		this.urlFragment = urlFragment;
	}

	public boolean match(E2ETestableNetworkService.NetworkRequest request) {
		return request != null && request.url != null && request.url.contains(urlFragment);
	}
}