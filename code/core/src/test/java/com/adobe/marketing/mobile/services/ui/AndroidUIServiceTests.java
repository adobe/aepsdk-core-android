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

package com.adobe.marketing.mobile.services.ui;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import com.adobe.marketing.mobile.services.AppContextService;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.ServiceProviderModifier;
import com.adobe.marketing.mobile.services.ui.internal.MessagesMonitor;
import java.util.Calendar;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AndroidUIServiceTests {

    @Mock private AppContextService mockAppContextService;

    @Mock private MessagesMonitor mockMessagesMonitor;

    @Mock private Activity mockActivity;

    @Mock private Context mockContext;

    @Mock private AlarmManager mockAlarmManager;

    @Mock private Intent mockIntent;

    private AndroidUIService androidUIService;

    @Before
    public void setup() {
        androidUIService = new AndroidUIService();
        ServiceProviderModifier.setAppContextService(mockAppContextService);
    }

    @After
    public void cleanup() {}

    @Test
    public void isMessageDisplayedReturnsFalse_When_MessageMonitorIsDisplayedIsFalse() {
        when(mockMessagesMonitor.isDisplayed()).thenReturn(false);
        androidUIService.messagesMonitor = mockMessagesMonitor;

        assertFalse(MessagesMonitor.getInstance().isDisplayed());
    }

    @Test
    public void alertIsShown_When_NoOtherMessagesAreDisplayed() {
        // setup
        when(mockMessagesMonitor.isDisplayed()).thenReturn(false);
        androidUIService.messagesMonitor = mockMessagesMonitor;

        ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        doNothing().when(mockActivity).runOnUiThread(argumentCaptor.capture());

        when(mockAppContextService.getCurrentActivity()).thenReturn(mockActivity);
        // test
        androidUIService.showAlert(AlertSetting.build("title", "message", "ok", "no"), null);
        // verify that the runOnUIThread was called with a valid runnable
        assertNotNull(argumentCaptor.getValue());
    }

    @Test
    public void alertIsNotShown_When_OtherMessagesAreDisplayed() {
        // setup
        when(mockMessagesMonitor.isDisplayed()).thenReturn(true);
        androidUIService.messagesMonitor = mockMessagesMonitor;

        when(mockAppContextService.getCurrentActivity()).thenReturn(mockActivity);
        // test
        androidUIService.showAlert(AlertSetting.build("title", "message", "ok", "no"), null);
        // verify
        verify(mockActivity, times(0)).runOnUiThread(any(Runnable.class));
    }

    @Test
    public void alertIsNotShown_When_ActivityIsNull() {
        // setup
        when(mockMessagesMonitor.isDisplayed()).thenReturn(false);
        androidUIService.messagesMonitor = mockMessagesMonitor;

        when(mockAppContextService.getCurrentActivity()).thenReturn(null);
        // test
        androidUIService.showAlert(AlertSetting.build("title", "message", "ok", "no"), null);
        // verify
        verify(mockActivity, times(0)).runOnUiThread(any(Runnable.class));
    }

    @Test
    public void alertIsNotShown_When_negativeAndPositiveButtonTextIsMissing() {
        // setup
        when(mockMessagesMonitor.isDisplayed()).thenReturn(false);
        androidUIService.messagesMonitor = mockMessagesMonitor;

        when(mockAppContextService.getCurrentActivity()).thenReturn(mockActivity);
        // test
        androidUIService.showAlert(AlertSetting.build("title", "message", null, null), null);
        // verify
        verify(mockActivity, times(0)).runOnUiThread(any(Runnable.class));
    }

    @Test
    public void localNotificationIsShown_When_NoOtherMessagesAreDisplayed() {
        // setup
        when(mockMessagesMonitor.isDisplayed()).thenReturn(false);
        androidUIService.messagesMonitor = mockMessagesMonitor;

        when(mockContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(mockAlarmManager);
        when(mockContext.getApplicationContext()).thenReturn(mockContext);

        when(mockAppContextService.getApplicationContext()).thenReturn(mockContext);
        // test
        androidUIService.showLocalNotification(
                NotificationSetting.build(
                        "id", "content", 123456, 123, "myscheme://link", null, "sound.wav", null));
        // verify that the Alarm was set
        ArgumentCaptor<Long> triggerTimeCaptor = ArgumentCaptor.forClass(long.class);
        // The Pending Intent is null matched only because in this test we are not able to mock a
        // static call
        // to PendingIntent.getBroadcast() without using additional libraries - which is a no-no
        verify(mockAlarmManager)
                .set(eq(AlarmManager.RTC_WAKEUP), triggerTimeCaptor.capture(), isNull());
        // verify that the alarm time is within the delta of 50ms :)
        long expectedTriggerTime = getTriggerTimeForFireDate(123456);
        assertTrue(triggerTimeCaptor.getValue() - expectedTriggerTime < 50);
    }

    @Test
    public void localNotificationWithTitleIsShown_When_NoOtherMessagesAreDisplayed() {
        // setup
        when(mockMessagesMonitor.isDisplayed()).thenReturn(false);
        androidUIService.messagesMonitor = mockMessagesMonitor;

        when(mockContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(mockAlarmManager);
        when(mockContext.getApplicationContext()).thenReturn(mockContext);

        when(mockAppContextService.getApplicationContext()).thenReturn(mockContext);
        // test
        androidUIService.showLocalNotification(
                NotificationSetting.build(
                        "id",
                        "content",
                        123456,
                        123,
                        "myscheme://link",
                        null,
                        "sound.wav",
                        "title"));
        // verify that the Alarm was set
        ArgumentCaptor<Long> triggerTimeCaptor = ArgumentCaptor.forClass(long.class);
        // The Pending Intent is null matched only because in this test we are not able to mock a
        // static call
        // to PendingIntent.getBroadcast() without using additional libraries - which is a no-no
        verify(mockAlarmManager)
                .set(eq(AlarmManager.RTC_WAKEUP), triggerTimeCaptor.capture(), isNull());
        // verify that the alarm time is within the delta of 50ms :)
        long expectedTriggerTime = getTriggerTimeForFireDate(123456);
        assertTrue(triggerTimeCaptor.getValue() - expectedTriggerTime < 50);
    }

    @Test
    public void localNotificationIsShown_When_OtherMessagesAreDisplayed() {
        // setup
        when(mockMessagesMonitor.isDisplayed()).thenReturn(true);
        androidUIService.messagesMonitor = mockMessagesMonitor;

        when(mockContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(mockAlarmManager);
        when(mockContext.getApplicationContext()).thenReturn(mockContext);

        when(mockAppContextService.getApplicationContext()).thenReturn(mockContext);
        // test
        androidUIService.showLocalNotification(
                NotificationSetting.build(
                        "id", "content", 123456, 123, "myscheme://link", null, "sound.wav", null));

        long expectedTriggerTime = getTriggerTimeForFireDate(123456);
        // verify that the Alarm was set
        ArgumentCaptor<Long> triggerTimeCaptor = ArgumentCaptor.forClass(long.class);
        // The Pending Intent is null matched only because in this test we are not able to mock a
        // static call
        // to PendingIntent.getBroadcast() without using additional libraries - which is a no-no
        verify(mockAlarmManager)
                .set(eq(AlarmManager.RTC_WAKEUP), triggerTimeCaptor.capture(), isNull());
        // verify that the alarm time is within the delta of 50ms :)
        assertTrue(triggerTimeCaptor.getValue() - expectedTriggerTime < 50);
    }

    @Test
    public void localNotificationWithTitleIsShown_When_OtherMessagesAreDisplayed() {
        // setup
        when(mockMessagesMonitor.isDisplayed()).thenReturn(true);
        androidUIService.messagesMonitor = mockMessagesMonitor;

        when(mockContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(mockAlarmManager);
        when(mockContext.getApplicationContext()).thenReturn(mockContext);

        when(mockAppContextService.getApplicationContext()).thenReturn(mockContext);
        // test
        androidUIService.showLocalNotification(
                NotificationSetting.build(
                        "id",
                        "content",
                        123456,
                        123,
                        "myscheme://link",
                        null,
                        "sound.wav",
                        "title"));

        long expectedTriggerTime = getTriggerTimeForFireDate(123456);
        // verify that the Alarm was set
        ArgumentCaptor<Long> triggerTimeCaptor = ArgumentCaptor.forClass(long.class);
        // The Pending Intent is null matched only because in this test we are not able to mock a
        // static call
        // to PendingIntent.getBroadcast() without using additional libraries - which is a no-no
        verify(mockAlarmManager)
                .set(eq(AlarmManager.RTC_WAKEUP), triggerTimeCaptor.capture(), isNull());
        // verify that the alarm time is within the delta of 50ms :)
        assertTrue(triggerTimeCaptor.getValue() - expectedTriggerTime < 50);
    }

    @Test
    public void localNotificationIsShown_When_OtherMessagesAreDisplayed1() {
        // setup
        when(mockMessagesMonitor.isDisplayed()).thenReturn(true);
        androidUIService.messagesMonitor = mockMessagesMonitor;

        when(mockContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(mockAlarmManager);
        when(mockContext.getApplicationContext()).thenReturn(mockContext);

        when(mockAppContextService.getApplicationContext()).thenReturn(mockContext);
        // test
        androidUIService.showLocalNotification(
                NotificationSetting.build(
                        "id", "content", 0, 123, "myscheme://link", null, "sound.wav", null));

        // verify that the Alarm was set
        ArgumentCaptor<Long> triggerTimeCaptor = ArgumentCaptor.forClass(long.class);
        // The Pending Intent is null matched only because in this test we are not able to mock a
        // static call
        // to PendingIntent.getBroadcast() without using additional libraries - which is a no-no
        verify(mockAlarmManager)
                .set(eq(AlarmManager.RTC_WAKEUP), triggerTimeCaptor.capture(), isNull());
        // verify that the alarm time is within the delta of 50ms :)
        long expectedTriggerTime = getTriggerTimeForDelaySecs(123);
        assertTrue(triggerTimeCaptor.getValue() - expectedTriggerTime < 50);
    }

    @Test
    public void localNotificationWithTitleIsShown_When_OtherMessagesAreDisplayed1() {
        // setup
        when(mockMessagesMonitor.isDisplayed()).thenReturn(true);
        androidUIService.messagesMonitor = mockMessagesMonitor;

        when(mockContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(mockAlarmManager);
        when(mockContext.getApplicationContext()).thenReturn(mockContext);

        when(mockAppContextService.getApplicationContext()).thenReturn(mockContext);
        // test
        androidUIService.showLocalNotification(
                NotificationSetting.build(
                        "id", "content", 0, 123, "myscheme://link", null, "sound.wav", "title"));

        // verify that the Alarm was set
        ArgumentCaptor<Long> triggerTimeCaptor = ArgumentCaptor.forClass(long.class);
        // The Pending Intent is null matched only because in this test we are not able to mock a
        // static call
        // to PendingIntent.getBroadcast() without using additional libraries - which is a no-no
        verify(mockAlarmManager)
                .set(eq(AlarmManager.RTC_WAKEUP), triggerTimeCaptor.capture(), isNull());
        // verify that the alarm time is within the delta of 50ms :)
        long expectedTriggerTime = getTriggerTimeForDelaySecs(123);
        assertTrue(triggerTimeCaptor.getValue() - expectedTriggerTime < 50);
    }

    @Test
    public void localNotificationIsNotShown_When_ContextIsNull() {
        // setup
        when(mockMessagesMonitor.isDisplayed()).thenReturn(false);
        androidUIService.messagesMonitor = mockMessagesMonitor;

        when(mockAppContextService.getApplicationContext()).thenReturn(mockContext);
        // test
        androidUIService.showLocalNotification(
                NotificationSetting.build(
                        "id",
                        "content",
                        123456,
                        123,
                        "myscheme://link",
                        null,
                        "sound.wav",
                        "title"));

        // The Pending Intent is null matched only because in this test we are not able to mock a
        // static call
        // to PendingIntent.getBroadcast() without using additional libraries - which is a no-no
        verify(mockAlarmManager, times(0)).set(eq(AlarmManager.RTC_WAKEUP), anyLong(), isNull());
    }

    @Test
    public void localNotificationWithTitleIsNotShown_When_ContextIsNull() {
        // setup
        when(mockMessagesMonitor.isDisplayed()).thenReturn(false);
        androidUIService.messagesMonitor = mockMessagesMonitor;

        when(mockAppContextService.getApplicationContext()).thenReturn(mockContext);
        // test
        androidUIService.showLocalNotification(
                NotificationSetting.build(
                        "id", "content", 123456, 123, "myscheme://link", null, "sound.wav", null));

        // The Pending Intent is null matched only because in this test we are not able to mock a
        // static call
        // to PendingIntent.getBroadcast() without using additional libraries - which is a no-no
        verify(mockAlarmManager, times(0)).set(eq(AlarmManager.RTC_WAKEUP), anyLong(), isNull());
    }

    @Test
    public void showUrlStartsActivity_When_ValidUrl() {
        // Setup
        when(mockAppContextService.getCurrentActivity()).thenReturn(mockActivity);

        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
        doNothing().when(mockActivity).startActivity(intentArgumentCaptor.capture());

        AndroidUIService spyUIService = spy(new AndroidUIService());
        doReturn(mockIntent).when(spyUIService).getIntentWithURI("myappscheme://host");
        // test
        spyUIService.showUrl("myappscheme://host");
        // verify
        Intent actualIntent = intentArgumentCaptor.getValue();
        assertEquals(mockIntent, actualIntent);
    }

    @Test
    public void showUrlDoesNotStartsActivity_When_NullUrl() {
        // Setup
        when(mockContext.getApplicationContext()).thenReturn(mockContext);

        when(mockAppContextService.getApplicationContext()).thenReturn(mockContext);
        AndroidUIService spyUIService = spy(new AndroidUIService());
        doReturn(mockIntent).when(spyUIService).getIntentWithURI(anyString());
        // test
        spyUIService.showUrl(null);
        // verify
        verify(mockContext, times(0)).startActivity(any(Intent.class));
    }

    @Test
    public void messageMonitorDismissedCalled_When_AlertCanceled() {
        // Setup
        androidUIService.messagesMonitor = mockMessagesMonitor;
        // test
        DialogInterface.OnCancelListener onCancelListener =
                androidUIService.getAlertDialogOnCancelListener(null);
        onCancelListener.onCancel(null);
        // verify
        verify(mockMessagesMonitor).dismissed();
    }

    @Test
    public void messageMonitorDismissedCalled_When_AlertButtonClicked() {
        // Setup
        androidUIService.messagesMonitor = mockMessagesMonitor;
        // test
        DialogInterface.OnClickListener onClickListener =
                androidUIService.getAlertDialogOnClickListener(null);
        onClickListener.onClick(null, 0);
        // verify
        verify(mockMessagesMonitor).dismissed();
    }

    @Test
    public void messageMonitorDisplayedCalled_When_AlertButtonShown() {
        // Setup
        when(mockMessagesMonitor.isDisplayed()).thenReturn(false);
        androidUIService.messagesMonitor = mockMessagesMonitor;

        when(mockAppContextService.getCurrentActivity()).thenReturn(mockActivity);

        // test
        androidUIService.showAlert(AlertSetting.build("title", "message", "ok", "no"), null);
        // verify
        verify(mockMessagesMonitor).displayed();
    }

    @Test
    public void messageMonitorDisplayedNotCalled_When_LocalNotificationShown() {
        // Setup
        androidUIService.messagesMonitor = mockMessagesMonitor;
        // test
        androidUIService.showLocalNotification(
                NotificationSetting.build(
                        "id", "content", 123456, 12, "", null, "sound.wav", null));
        // verify
        verify(mockMessagesMonitor, times(0)).displayed();
    }

    @Test
    public void testSetURIHandler() {
        final String specialURI = "abc.com";
        Intent specialIntent = new Intent();
        ServiceProvider.getInstance()
                .setURIHandler(
                        new URIHandler() {
                            @Override
                            public Intent getURIDestination(String uri) {
                                if (specialURI.equals(uri)) {
                                    return specialIntent;
                                }
                                return null;
                            }
                        });
        Intent intent = ServiceProvider.getInstance().getUIService().getIntentWithURI("abc.com");
        assertSame(specialIntent, intent);
        Intent defaultIntent =
                ServiceProvider.getInstance().getUIService().getIntentWithURI("xyz.com");
        assertNotSame(specialIntent, defaultIntent);
    }

    private long getTriggerTimeForFireDate(long fireDate) {
        Calendar calendar = Calendar.getInstance();

        // do math to calculate number of seconds to add, because android api for calendar.builder
        // is API 26...
        final int secondsUntilFireDate = (int) (fireDate - (calendar.getTimeInMillis() / 1000));

        if (secondsUntilFireDate > 0) {
            calendar.add(Calendar.SECOND, secondsUntilFireDate);
        }

        return calendar.getTimeInMillis();
    }

    private long getTriggerTimeForDelaySecs(int delaySecs) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, delaySecs);

        return calendar.getTimeInMillis();
    }
}
