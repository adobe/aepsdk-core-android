package com.adobe.marketing.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.adobe.marketing.mobile.signal.SignalExtension;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SignalAPITest {

    @Test
    public void test_extensionVersion() {
        assertEquals("2.0.0", Signal.extensionVersion());
    }

    @Test
    public void test_extensionClass() {
        assertEquals(SignalExtension.class, Signal.EXTENSION);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void test_registerExtension() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic = Mockito.mockStatic(MobileCore.class)) {
            // mock MobileCore.registerExtension()
            ArgumentCaptor<Class> extensionClassCaptor = ArgumentCaptor.forClass(Class.class);
            ArgumentCaptor<ExtensionErrorCallback> callbackCaptor = ArgumentCaptor.forClass(
                    ExtensionErrorCallback.class
            );
            mobileCoreMockedStatic
                    .when(() -> MobileCore.registerExtension(extensionClassCaptor.capture(), callbackCaptor.capture()))
                    .thenReturn(true);
            // call registerExtension() API
            Signal.registerExtension();
            // verify: happy
            assertNotNull(callbackCaptor.getValue());
            assertEquals(SignalExtension.class, extensionClassCaptor.getValue());
            // verify: error callback was called
            callbackCaptor.getValue().error(ExtensionError.UNEXPECTED_ERROR);
        }
    }
}
