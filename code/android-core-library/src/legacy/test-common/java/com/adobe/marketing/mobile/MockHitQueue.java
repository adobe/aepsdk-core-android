package com.adobe.marketing.mobile;

import java.io.File;
import java.util.Map;

class MockHitQueue<T extends AbstractHit, E extends AbstractHitSchema<T>> extends HitQueue<T, E> {
	MockHitQueue(PlatformServices services) {
		this(services, null, null, null, null);
	}

	MockHitQueue(PlatformServices services, File dbFile, String tableName, E hitSchema,
				 IHitProcessor hitProcessor) {
		super(services, dbFile, tableName, hitSchema, hitProcessor);
	}

	boolean selectOldestHitWasCalled;
	T selectOldestHitReturnValue;
	T selectOldestHit() {
		selectOldestHitWasCalled = true;
		return selectOldestHitReturnValue;
	}

	boolean queryHitWasCalled;
	Query queryHitParametersQuery;
	T queryHitReturnValue;
	@Override
	T queryHit(Query query) {
		queryHitWasCalled = true;
		queryHitParametersQuery = query;
		return queryHitReturnValue;
	}


	boolean updateHitWasCalled;
	T updateHitParametersHit;
	@Override
	boolean updateHit(final T hit) {
		updateHitWasCalled = true;
		updateHitParametersHit = hit;
		return true;
	}

	boolean updateAllHitsWasCalled;
	Map<String, Object>  updateAllHitsParameters;
	@Override
	boolean updateAllHits(final Map<String, Object> parameters) {
		updateAllHitsWasCalled = true;
		updateAllHitsParameters = parameters;
		return true;
	}


	boolean queueWasCalled;
	T queueParametersHit;
	boolean queueReturnValue;
	@Override
	protected boolean queue(final T hit) {
		queueWasCalled = true;
		queueParametersHit = hit;
		return queueReturnValue;
	}


	boolean bringOnlineWasCalled;
	@Override
	void bringOnline() {
		bringOnlineWasCalled = true;
	}

	boolean suspendWasCalled;
	@Override
	void suspend() {
		suspendWasCalled = true;
	}

	boolean deleteHitWithIdentifierWasCalled;
	String deleteHitWithIdentifierParametersIdentifier;
	boolean deleteHitWithIdentifierReturnValue;
	@Override
	boolean deleteHitWithIdentifier(final String identifier) {
		deleteHitWithIdentifierWasCalled = true;
		deleteHitWithIdentifierParametersIdentifier = identifier;
		return deleteHitWithIdentifierReturnValue;
	}


	long getSizeReturnValue;
	@Override
	protected long getSize() {
		return getSizeReturnValue;
	}


	long getSizeWithQueryReturnValue;
	@Override
	protected long getSize(Query query) {
		return getSizeWithQueryReturnValue;
	}

	boolean deleteAllHitsWasCalled;
	@Override
	protected void deleteAllHits() {
		deleteAllHitsWasCalled = true;
	}
}
