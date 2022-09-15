/* *****************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Adobe and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Adobe
 * and its suppliers and are protected by all applicable intellectual
 * property laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe.
 ******************************************************************************/

package com.adobe.marketing.mobile;

/**
 * Class to represent a pair of Objects.
 * May be used to return two Object results from a method.
 * @param <T>
 * @param <S>
 */
class IdentityGenericPair<T, S> {
	private final T first;
	private final S second;

	IdentityGenericPair(final T first, final S second) {
		this.first = first;
		this.second = second;
	}

	T getFirst() {
		return first;
	}

	S getSecond() {
		return second;
	}
}
