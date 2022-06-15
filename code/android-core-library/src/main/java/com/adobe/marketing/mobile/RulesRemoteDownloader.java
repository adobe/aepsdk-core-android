package com.adobe.marketing.mobile;

import com.adobe.marketing.mobile.services.Networking;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

// TODO: Remove this class when Java version of ConfigurationExtension is deleted
class RulesRemoteDownloader extends RemoteDownloader {

	private static final String LOG_TAG = RulesBundleNetworkProtocolHandler.class.getSimpleName();
	private RulesBundleNetworkProtocolHandler protocolHandler;

	/**
	 * This is the contract for a concrete implementation that would support
	 * processing a Rules Engine bundle downloaded from the end-point configured.
	 * <p>
	 *
	 * The interface allows the handling of the actual file type downloaded to be changed / plugged -in/out.
	 * The implementation is responsible for: <br>
	 *
	 * <ul>
	 *     <li>
	 *         Processing the downloaded bundle (file)
	 *     </li>
	 *     <li>
	 *         Recording the {@link Metadata} for the bundle. This metadata will be used by the
	 *         {@link RulesRemoteDownloader} while performing a conditional fetch later.
	 *     </li>
	 * </ul>
	 *
	 */
	interface RulesBundleNetworkProtocolHandler {
		/**
		 * Retrieve the {@code Metadata} for the original downloaded Rules bundle (file).
		 * <p>
		 *
		 *  The metadata should be recorded when this implementation processes the downloaded bundle the first time.
		 *
		 * @param cachedBundlePath The path for the <b>processed</b> bundle.
		 * @return The {@link Metadata} corresponding to the <b>un-processed</b> rules bundle.
		 *
		 * @see #processDownloadedBundle(File, String, long)
		 */
		Metadata getMetadata(File cachedBundlePath);

		/**
		 * Process the file that was downloaded by the {@code RulesRemoteDownloader}.
		 * <p>
		 *
		 * The implementation is free to process the file as it wishes. The processed contents should be stored in the
		 * {@code outputPath} path.
		 * <br>
		 *
		 * This method is also responsible to record the {@code downloadedBundle} metadata before the processing is complete.
		 * The metadata will then be used by the {@link RulesRemoteDownloader} when querying for the same bundle the next time.
		 *
		 * @param downloadedBundle The file that was downloaded by the {@code RulesRemoteDownloader}
		 * @param outputPath The absolute path of the output folder. The implementation is free to create sub-folders underneath.
		 * @param lastModifiedDateForBundle The last modified date obtained from the downloaded resource.
		 *
		 * @return Indication of whether the processing was successful.
		 */
		boolean processDownloadedBundle(File downloadedBundle, String outputPath, long lastModifiedDateForBundle);
	}

	interface Metadata {
		long getLastModifiedDate();
		long getSize();
	}

	/**
	 * Constructor.
	 *
	 * <p>
	 *
	 * The default {@link RulesBundleNetworkProtocolHandler} will be set here. The default is {@link ZipBundleHandler}.
	 * If the {@link CompressedFileService} is not available on he platform, the {@code ZipBundleHandler} will not be used, and
	 * this downloader will discard the downloaded files.
	 *
	 * @param networkService The platform {@link NetworkService} implementation
	 * @param compressedFileService The platform {@link CompressedFileService} implementation
	 * @param url The URL to download from
	 * @param directoryOverride The cache directory override.
	 * @throws MissingPlatformServicesException If the required platform services are not available on the platform. The {@link CompressedFileService} is <b>not</b> a
	 * required platform service. If it is not available, the {@link ZipBundleHandler} will not be used as the {@code RulesBundleNetworkProtocolHandler}
	 *
	 * @see #setRulesBundleProtocolHandler(RulesBundleNetworkProtocolHandler)
	 */
	RulesRemoteDownloader(final Networking networkService,
						  final CompressedFileService compressedFileService,
						  final String url, final String directoryOverride) throws MissingPlatformServicesException {
		super(networkService, url, directoryOverride);

		try {
			//The default file type is Zip.
			protocolHandler = new ZipBundleHandler(compressedFileService);
		} catch (MissingPlatformServicesException ex) {
			Log.trace(LOG_TAG, "Will not be using Zip Protocol to download rules (%s)", ex);
		}
	}

	RulesRemoteDownloader(final Networking networkService, final String url,
						  final CacheManager cacheManager) throws MissingPlatformServicesException {
		super(networkService, url, cacheManager);
	}

	@Override
	protected HashMap<String, String> getRequestParameters(final File cacheFile) {
		//Get the request parameters from the unzipped folder
		HashMap<String, String> requestParameters = new HashMap<String, String>();

		if (cacheFile == null || protocolHandler == null) {
			return requestParameters;
		}

		Metadata metadata = protocolHandler.getMetadata(cacheFile);

		if (metadata != null) {
			final Long date = metadata.getLastModifiedDate();

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
			// In future we should implementing the cache-control header, instead of Etag, If-Modified-Since or Range header (partial download).
			// Issue : https://jira.corp.adobe.com/browse/AMSDK-7698
			if (date != 0L) {
				final SimpleDateFormat rfc2822Formatter = createRFC2822Formatter();
				String lastModifiedDate = rfc2822Formatter.format(date);
				requestParameters.put(HTTP_HEADER_IF_MODIFIED_SINCE, lastModifiedDate);
				requestParameters.put(HTTP_HEADER_IF_RANGE, lastModifiedDate);
			}

			String rangeRequestString = String.format(Locale.US, "bytes=%d-", metadata.getSize());
			requestParameters.put(HTTP_HEADER_RANGE, rangeRequestString);
		}

		return requestParameters;
	}

	/**
	 * Process the downloaded file and return the destination path.
	 *
	 * @param downloadedFile The file that was downloaded by this.
	 * @return The destination path where the processed file contents were stored. Will be null if the processing failed.
	 */
	protected File processBundle(final File downloadedFile) {
		Log.trace(ConfigurationExtension.LOG_SOURCE, "Processing downloaded rules bundle");
		File processedFile = null;

		if (downloadedFile == null) {
			return null;
		}

		if (downloadedFile.isDirectory()) {
			processedFile = downloadedFile;
		} else {
			//The folder that has been processed will be sent out to the method (not the zip file)
			final Long date = cacheManager != null ? cacheManager.getLastModifiedOfFile(downloadedFile.getPath()) : 0L;
			String outputPath = cacheManager != null ? cacheManager.getBaseFilePath(url, directory) : null;

			if (outputPath != null && protocolHandler.processDownloadedBundle(downloadedFile, outputPath, date)) {
				processedFile = new File(outputPath);
			}
		}

		return processedFile;
	}

	/**
	 * Set an alternative {@code RulesBundleNetworkProtocolHandler} implementation.
	 *
	 * @param protocolHandler The {@link RulesBundleNetworkProtocolHandler} implementation to be used.
	 */
	void setRulesBundleProtocolHandler(final RulesBundleNetworkProtocolHandler protocolHandler) {
		this.protocolHandler = protocolHandler;
	}

	/**
	 * Downloads the rules bundle file if needed and processes the file.
	 * <p>
	 *
	 * The file will be processed using the {@link RulesBundleNetworkProtocolHandler} set.
	 * @return The processed file. If the download was not required, then returns the cached directory.
	 */
	@Override
	public File startDownloadSync() {
		Log.trace(ConfigurationExtension.LOG_SOURCE, "Start download of rules bundle file");
		File downloadedBundle =  super.startDownloadSync();
		//The downloaded file will be a zip / or something else.
		//We will need to convert that into a folder.
		File processedBundlePath = null;

		if (downloadedBundle != null && protocolHandler != null) {
			processedBundlePath = processBundle(downloadedBundle);
		}

		if (processedBundlePath == null) {
			//Purge the file since we do not know what to do with this
			cacheManager.deleteCachedDataForURL(url, directory);
		}

		return processedBundlePath;
	}

	/**
	 * Retrieves the contents of the cached file for this ConfigurationDownloader instance. If there is no cached
	 * file, <tt>null</tt> is returned.
	 * @return the String contents of the cached file, or <tt>null</tt> if there is no cached file or there was
	 * an error reading the file contents.
	 */
	File getCachedRulesFile() {
		final File cacheFile = super.cacheManager.getFileForCachedURL(url, directory, false);
		return cacheFile;
	}

}
