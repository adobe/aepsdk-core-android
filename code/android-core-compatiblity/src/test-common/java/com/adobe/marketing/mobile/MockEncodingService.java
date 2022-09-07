package com.adobe.marketing.mobile;

public class MockEncodingService implements EncodingService {

	byte[] base64DecodeReturnValue;
	byte[] base64EncodeReturnValue;
	@Override
	public byte[] base64Decode(String input) {
		return base64DecodeReturnValue;
	}
	public byte[] base64Encode(byte[] input) {
		return base64EncodeReturnValue;
	}
}
