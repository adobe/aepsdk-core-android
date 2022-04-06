package com.adobe.marketing.mobile.internal.eventhub.history;

import java.util.Map;

class EventDataHelper {
    private EventDataHelper() {
    }

    /**
     * Converts the current {@link EventData} into a {@code long} decimal FNV1a 32-bit hash.
     * <p>
     * If a mask is provided, only use keys in the provided mask and alphabetize their order.
     *
     * @param mask {@code String[]} containing keys to be hashed
     * @return {@code long} containing the decimal FNV1a 32-bit hash.
     */
    static long toFnv1aHash(final Map<String, Object> eventData, final String[] mask) {
//        final StringBuilder kvpStringBuilder = new StringBuilder();
//        final Map<String, Variant> flattenedMap = EventDataFlattener.getFlattenedDataMap(this);
//
//        // if a mask is provided, only use keys in the provided mask and alphabetize their order
//        try {
//            if (mask != null && mask.length > 0) {
//                final String[] maskCopy = mask.clone();
//                Arrays.sort(maskCopy);
//
//                for (final String key : maskCopy) {
//                    // only retain keys which are present in the mask
//                    if (!StringUtils.isNullOrEmpty(key) && flattenedMap.containsKey(key)) {
//                        final Variant currentVariant = flattenedMap.get(key);
//                        kvpStringBuilder.append(key).append(":").append(convertVariantToString(currentVariant));
//                    }
//                }
//            } else {
//                final SortedMap<String, Variant> alphabeticalMap = new TreeMap<String, Variant>(flattenedMap);
//
//                for (final String key : alphabeticalMap.keySet()) {
//                    // ignore MapVariants and only add flattened values
//                    if (!(alphabeticalMap.get(key) instanceof MapVariant)) {
//                        final Variant currentVariant = flattenedMap.get(key);
//                        kvpStringBuilder.append(key).append(":").append(convertVariantToString(currentVariant));
//                    }
//                }
//            }
//        } catch (final VariantException variantException) {
//            Log.debug(LOG_TAG, "Unable to convert variant: %s.", variantException.getLocalizedMessage());
//            return 0;
//        }
//
//        // return hex string as decimal
//        return StringEncoder.convertStringToDecimalHash(kvpStringBuilder.toString());
        return 0;
    }
}
