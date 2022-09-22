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

import com.adobe.marketing.mobile.NetworkService.HttpConnection;
import com.adobe.marketing.mobile.services.HttpConnecting;
import com.adobe.marketing.mobile.services.HttpMethod;
import com.adobe.marketing.mobile.services.NetworkCallback;
import com.adobe.marketing.mobile.services.NetworkRequest;
import com.adobe.marketing.mobile.services.Networking;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.util.UrlUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;

// TODO: Remove this class when Java version of ConfigurationExtension is deleted
class RemoteDownloader {

	private static final String LOG_TAG = RemoteDownloader.class.getSimpleName();
	private static final int DEFAULT_CONNECTION_TIMEOUT = 10000;
	private static final int DEFAULT_READ_TIMEOUT = 10000;
	private static final int HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
	private static final int STREAM_WRITE_BUFFER_SIZE = 4096;
	protected static final String HTTP_HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
	protected static final String HTTP_HEADER_IF_RANGE = "If-Range";
	protected static final String HTTP_HEADER_RANGE = "Range";
	private static final String ETAG = "ETag";

	final CacheManager cacheManager;
	private final Networking networkService;
	protected final String url;
	protected final String directory;
	private final Map<String, String> requestProperties;

	/**
	 * @param networkService   {@code NetworkService} instance
	 * @param url url from which the content has to be downloaded
	 * @param directoryOverride optional directory for the download
	 * @throws MissingPlatformServicesException  if the downloader is initiated with null network or systemInfo service.
	 */
	public RemoteDownloader(final Networking networkService,
							final String url, final String directoryOverride) throws MissingPlatformServicesException {
		if (networkService == null) {
			throw new MissingPlatformServicesException("Remote Downloader - NetworkService not found!");
		}

		this.networkService = networkService;
		this.cacheManager = new CacheManager(ServiceProvider.getInstance().getDeviceInfoService());
		this.url = url;
		this.directory = directoryOverride;
		this.requestProperties = null;
	}

	/**
	 * @param networkService   {@code NetworkService} instance
	 * @param url url from which the content has to be downloaded
	 * @param directoryOverride optional directory for the download
	 * @param requestProperties {@code Map<String, String>} containing any additional key value pairs to be used while requesting a
	 *                        connection to the url
	 * @throws MissingPlatformServicesException  if the downloader is initiated with null network or systemInfo service.
	 */
	public RemoteDownloader(final Networking networkService,
							final String url, final String directoryOverride,
							final Map<String, String> requestProperties) throws MissingPlatformServicesException {
		if (networkService == null) {
			throw new MissingPlatformServicesException("Remote Downloader - NetworkService not found!");
		}

		this.networkService = networkService;
		this.cacheManager = new CacheManager(ServiceProvider.getInstance().getDeviceInfoService());
		this.url = url;
		this.directory = directoryOverride;
		this.requestProperties = new HashMap<String, String>(requestProperties);
	}


	/**
	 *
	 * Testing constructor - Solely used for unit testing purposes.
	 *
	 * @param networkService   {@code NetworkService} instance
	 * @param url url from which the content has to be downloaded
	 * @param cacheManager cacheManager instance used by the downloader
	 * @throws MissingPlatformServicesException  if the downloader is initiated with null network or systemInfo service.
	 */
	public RemoteDownloader(final Networking networkService,
							final String url, final CacheManager cacheManager) throws MissingPlatformServicesException {
		this(networkService, url, null, cacheManager);
	}

	/**
	 *
	 * Testing constructor - Solely used for unit testing purposes.
	 *
	 * @param networkService   {@code NetworkService} instance
	 * @param url url from which the content has to be downloaded
	 * @param directoryOverride optional directory for the download
	 * @param cacheManager cacheManager instance used by the downloader
	 * @throws MissingPlatformServicesException  if the downloader is initiated with null network or systemInfo service.
	 */
	public RemoteDownloader(final Networking networkService,
							final String url, final String directoryOverride,
							final CacheManager cacheManager) throws MissingPlatformServicesException {
		if (networkService == null) {
			throw new MissingPlatformServicesException("Remote Downloader - NetworkService not found!");
		}

		this.networkService = networkService;
		this.cacheManager = cacheManager;
		this.url = url;
		this.directory = directoryOverride;
		this.requestProperties = null;
	}



	/**
	 *
	 * Testing constructor - Solely used for unit testing purposes.
	 *
	 * @param networkService   {@code NetworkService} instance
	 * @param url url from which the content has to be downloaded
	 * @param cacheManager cacheManager instance used by the downloader
	 * @param requestProperties {@code Map<String, String>} containing any additional key value pairs to be used while requesting a
	 *                        connection to the url
	 * @throws MissingPlatformServicesException  if the downloader is initiated with null network or systemInfo service.
	 */
	public RemoteDownloader(final Networking networkService,
							final String url, final CacheManager cacheManager,
							final Map<String, String> requestProperties) throws MissingPlatformServicesException {
		if (networkService == null) {
			throw new MissingPlatformServicesException("Remote Downloader - NetworkService not found!");
		}

		this.networkService = networkService;
		this.cacheManager = cacheManager;
		this.url = url;
		this.directory = null;
		this.requestProperties = new HashMap<String, String>(requestProperties);
	}


	/**
	 * should be overridden by the subclass if it wants to make an async request
	 * <p>
	 *     Returns the following :
	 * <ol>
	 *   <li>The downloaded {@link File} on successful download.</li>
	 *   <li>The cached version of the file when the file has no modifications compared to the remote.</li>
	 *	 <li>Null, in case of any other network errors while downloading the file.</li>
	 *</ol>
	 *
	 * @param downloadedFile The downloaded file or cached file
	 */
	protected void onDownloadComplete(final File downloadedFile) {

	}

	/**
	 * Initializes the download from the given URL.
	 *
	 * @return A {@code boolean} indicating if the network request is processed
	 */
	public boolean startDownload() {
		// url validation
		if (!UrlUtils.stringIsUrl(url)) {
			Log.warning(LOG_TAG, "Given url is not valid and contents cannot be cached : (%s)", url);
			return false;
		}

		// currently there is no guarantee of happens-after relation between successive calls, due to the async nature of
		//the network calls.
		final File cachedFile = cacheManager != null ? cacheManager.getFileForCachedURL(url, directory, false) : null;
		HashMap<String, String> requestParameters = null;

		if (cachedFile != null) {
			// we have a cached file. Lets setup a conditional fetch
			requestParameters = getRequestParameters(cachedFile);
		}

		if (this.requestProperties != null) {
			if (requestParameters == null) {
				requestParameters = new HashMap<String, String>(this.requestProperties);
			} else {
				requestParameters.putAll(this.requestProperties);
			}
		}

		final NetworkRequest networkRequest = new NetworkRequest(url,
				HttpMethod.GET, null, requestParameters, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT);
		final NetworkCallback networkCallback = new NetworkCallback() {
			@Override
			public void call(HttpConnecting connection) {
				File remoteFile = processNetworkConnectionObject(connection, cachedFile);
				onDownloadComplete(remoteFile);
			}
		};

		networkService.connectAsync(networkRequest, networkCallback);
		return true;
	}


	public File startDownloadSync() {
		// URL Validation
		if (!UrlUtils.stringIsUrl(url)) {
			Log.warning(LOG_TAG, "Given url is not valid and contents cannot be cached : (%s)", url);
			return null;
		}

		final File cachedFile = cacheManager != null ? cacheManager.getFileForCachedURL(url, directory, false) : null;
		HashMap<String, String> requestParameters = null;

		if (cachedFile != null) {
			//we have a cached file. Lets setup a conditional fetch
			requestParameters = getRequestParameters(cachedFile);
		}

		if (this.requestProperties != null) {
			if (requestParameters == null) {
				requestParameters = new HashMap<String, String>(this.requestProperties);
			} else {
				requestParameters.putAll(this.requestProperties);
			}
		}

		final File[] result = new File[1];
		final CountDownLatch latch = new CountDownLatch(1);
		final NetworkRequest networkRequest = new NetworkRequest(url,
				HttpMethod.GET, null, requestParameters, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT);
		final NetworkCallback networkCallback = new NetworkCallback() {
			@Override
			public void call(final HttpConnecting connection) {
				File remoteFile = processNetworkConnectionObject(connection, cachedFile);
				result[0] = remoteFile;
				latch.countDown();
				onDownloadComplete(remoteFile);
			}
		};


		try {
			networkService.connectAsync(networkRequest, networkCallback);
			latch.await();
			return result[0];
		} catch (InterruptedException e) {
			return null;
		}
	}

	private File processNetworkConnectionObject(final HttpConnecting connection, final File cachedFile) {
		File newCachedFile = cachedFile;

		if (connection != null) {
			try {
				newCachedFile = processConnection(connection, cachedFile);
			} catch (Exception e) {
				Log.warning(LOG_TAG, "Cached Files - Unexpected exception while attempting to get remote file (%s)",
							e);
			} finally {
				connection.close();
			}
		}

		return newCachedFile;
	}

	/**
	 * Download the json file from a {@link HttpConnection}
	 * <p>
	 * The connection is expected to be non null.
	 *
	 * @param connection The Connection received from a {@link NetworkService} connect call
	 * @param cacheFile A file object representing a previously cached content. Can be null. If the server indicates partial
	 *                  content available, then append the remaining content from the server to this cache file
	 * @return A {@link File} containing the downloaded json content. Can be null due to error, or no new content available
	 */
	private File processConnection(final HttpConnecting connection, final File cacheFile) {
		//If no file found, then bail
		if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
			Log.debug(LOG_TAG, "File not found. (%s)", url);
			return null;
		}

		//If download is being resumed, then complete the download.
		if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) { // download remaining content
			return handlePartialContent(connection, cacheFile);
		}

		if (connection.getResponseCode() != HttpURLConnection.HTTP_OK
				&& connection.getResponseCode() != HTTP_REQUESTED_RANGE_NOT_SATISFIABLE) { // make sure we had no response other than OK
			Log.warning(LOG_TAG, "File could not be downloaded from URL (%s) Response: (%d) Message: (%s)",
						url,
						connection.getResponseCode(),
						connection.getResponseMessage());
			return cacheFile;
		}

		if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			return handleNewContent(connection);
		}

		return cacheFile;
	}

	/**
	 * Prepares the {@link Map} of request parameters that needs to go as a part of the header for the downloading of remote file
	 * <p>
	 *  Returns null, if there is no cache file associated with the remote download
	 *  Adds the following header, if the cache file exist
	 * <ol>
	 *   <li> The If-Modified-Since HTTP header for making conditional request. </li>
	 *   <li> The If-Range HTTP request header for making the range request conditional. </li>
	 *   <li> The Range HTTP request header indicating the part of a document that the server should return. </li>
	 *</ol>
	 *
	 * @param cacheFile cacheFile from which Last Modified Date and the file size is read
	 * @return {@code Map} the network header fields
	 */
	protected HashMap<String, String > getRequestParameters(final File cacheFile) {
		HashMap<String, String> requestParameters = new HashMap<String, String>();

		if (cacheFile != null) {
			final Long date = cacheManager != null ? cacheManager.getLastModifiedOfFile(cacheFile.getPath()) : 0L;

			// [AMSDK-7678]
			// HTTP_HEADER_IF_MODIFIED_SINCE header is added because of issue https://jira.corp.adobe.com/browse/AMSDK-7678
			// This header field address the conditional fetch, https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/If-Modified-Since
			// And the server responds with 304 if the contents of the remote download are not modified on the server.
			//
			// The If-Range header can be used either with a If-Modified-Since validator, or with an ETag, but not with both.
			// https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/If-Range
			// This is the reason why we should not use Etag, if we are doing partial download using Range and If-Range headers
			//
			// Next Step:
			// In future we should implementing the cache-control header, instead of Etag, If-Modified-Since or Range header(partial download).
			// Issue : https://jira.corp.adobe.com/browse/AMSDK-7698
			if (date != 0L) {
				final SimpleDateFormat rfc2822Formatter = createRFC2822Formatter();
				String lastModifiedDate = rfc2822Formatter.format(date);
				requestParameters.put(HTTP_HEADER_IF_RANGE, lastModifiedDate);
				requestParameters.put(HTTP_HEADER_IF_MODIFIED_SINCE, lastModifiedDate);
			}

			String rangeRequestString = String.format(Locale.US, "bytes=%d-", cacheFile.length());
			requestParameters.put(HTTP_HEADER_RANGE, rangeRequestString);
		}

		return requestParameters;
	}

	/**
	 * Creates a new cache file and downloads and stores the content from the connection into the cache file.
	 *
	 * @param connection An active network connection
	 * @return A cache file with the downloaded content. If there was an error while creating cache file, then null
	 */
	private File handleNewContent(final HttpConnecting connection) {
		cacheManager.deleteCachedDataForURL(url, directory);
		final Date lastModifiedDate = getResponseLastModifiedDate(connection);
		final String etag = connection.getResponsePropertyValue(ETAG);
		final File newCacheFile = cacheManager.createNewCacheFile(url, etag, directory, lastModifiedDate);


		if (newCacheFile == null) {
			Log.debug(LOG_TAG, "Could not create cache file on disk. Will not download from url (%s)", url);
			return null;
		}

		if (readInputStreamIntoFile(newCacheFile, connection.getInputStream(), false)) {
			File renamedCacheFile = cacheManager.markFileAsCompleted(newCacheFile);

			if (renamedCacheFile == null) {
				Log.debug(LOG_TAG, "Cached Files - Could not save cached file (%s)", url);
			} else {
				Log.debug(LOG_TAG, "Cached Files - Successfully downloaded content (%s) into (%s)", url,
						  renamedCacheFile.getAbsolutePath());
			}

			return renamedCacheFile;
		} else {
			return null;
		}
	}

	/**
	 * Appends the data retrieved from the network connection to the cache file supplied.
	 *
	 * @param connection An active network {@link HttpConnection}
	 * @param cacheFile A File where the network content will be appended to
	 *
	 * @return The {@link File} containing the appended data, and renamed to remove the "partial" marker. Null, if the rename was not successful
	 */
	private File handlePartialContent(final HttpConnecting connection, final File cacheFile) {
		File newCacheFile = null;

		if (cacheFile != null) {
			Log.debug(LOG_TAG,
					  "Cached Files - Found partial cached file. Downloading remaining content (%s)", url);
			// append data to existing file
			InputStream input = connection.getInputStream();
			readInputStreamIntoFile(cacheFile, input, true);
			newCacheFile = cacheManager != null ? cacheManager.markFileAsCompleted(cacheFile) : null;

			if (newCacheFile == null) {
				Log.debug(LOG_TAG, "Cached Files - Could not save cached file (%s)", url);
			} else {
				Log.debug(LOG_TAG, "Cached Files - Successfully downloaded remaining content (%s)", url);
			}
		} else {
			Log.debug(LOG_TAG, "Cached Files - partial cached file not found. Will be discarding the remaining content (%s)", url);
		}

		return newCacheFile;
	}

	/**
	 * Writes the inputStream into the file.
	 * <p>
	 * Will append the content of the inputStream to the existing file if the boolean is set as true.
	 *
	 *
	 * @param cachedFile File to which the content has to be written
	 * @param input Inputstream with json content
	 * @param append true, if you wanna append the input stream to the existing file content
	 * @return true if the inputstream has been successfully written into the file
	 */
	private boolean readInputStreamIntoFile(final File cachedFile, final InputStream input, final boolean append) {
		boolean result;

		if (cachedFile == null || input == null) {
			return false;
		}

		FileOutputStream output = null;

		try {
			output = new FileOutputStream(cachedFile, append);
			final byte[] data = new byte[STREAM_WRITE_BUFFER_SIZE];
			int count;

			while ((count = input.read(data)) != -1) {
				output.write(data, 0, count);
			}

			result = true;
		} catch (IOException e) {
			Log.error(LOG_TAG, "IOException while attempting to write remote file (%s)",
					  e);
			return false;
		} catch (Exception e) {
			Log.error(LOG_TAG, "Unexpected exception while attempting to write remote file (%s)",
					  e);
			return false;
		} finally {
			try {
				if (output != null) {
					output.close();
				}

			} catch (Exception e) {
				Log.error(LOG_TAG, "Unable to close the OutputStream (%s) ", e);
			}
		}

		return result;
	}

	/**
	 * Returns the value of "Last-Modified" key in the request headers map. If the key is not found, will return null.
	 *
	 * @param connection An active connection from which to get the request headers
	 * @return A value for the "Last-Modified" key if found. Null otherwise
	 */
	private Date getResponseLastModifiedDate(final HttpConnecting connection) {
		Date lastModifiedDate = null;
		final SimpleDateFormat rfc2822Formatter = createRFC2822Formatter();
		String lastModifiedString = connection.getResponsePropertyValue("Last-Modified");

		try {
			lastModifiedDate = rfc2822Formatter.parse(lastModifiedString);
		} catch (Exception e) {
			Log.debug(LOG_TAG, "Unable to parse the last modified date returned from the request (%s)", e);
			return null;
		}

		return lastModifiedDate;
	}

	/**
	 * Create a Date formatter in specific format.
	 *
	 * @return SimpleDateFormat
	 */
	protected SimpleDateFormat createRFC2822Formatter() {
		final String pattern = "EEE, dd MMM yyyy HH:mm:ss z";
		final SimpleDateFormat rfc2822formatter = new SimpleDateFormat(pattern, Locale.US);
		rfc2822formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return rfc2822formatter;
	}
}


