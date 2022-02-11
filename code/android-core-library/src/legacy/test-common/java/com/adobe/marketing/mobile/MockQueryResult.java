package com.adobe.marketing.mobile;

import java.util.Map;

public class MockQueryResult implements DatabaseService.QueryResult {
	Object[][] results;
	int index = 0;

	MockQueryResult(final Object[][] results) {
		this.results = results;
	}

	@Override
	public int getCount() throws Exception {
		return results.length;
	}

	@Override
	public int getInt(int columnIndex) throws Exception {
		return (Integer)results[index][columnIndex];
	}

	@Override
	public double getDouble(int columnIndex) throws Exception {
		return (Double)results[index][columnIndex];
	}

	@Override
	public float getFloat(int columnIndex) throws Exception {
		return (Float)results[index][columnIndex];
	}

	@Override
	public long getLong(int columnIndex) throws Exception {
		return (Long)results[index][columnIndex];
	}

	@Override
	public String getString(int columnIndex) throws Exception {
		return (String)results[index][columnIndex];
	}

	@Override
	public boolean isNull(int columnIndex) throws Exception {
		return results[index][columnIndex] == null;
	}

	@Override
	public boolean moveToFirst() throws Exception {
		index = 0;
		return true;
	}

	@Override
	public boolean moveToLast() throws Exception {

		index = this.results.length - 1;
		return true;
	}

	@Override
	public boolean moveToNext() throws Exception {
		index++;
		return true;
	}

	@Override
	public void close() {

	}
}
