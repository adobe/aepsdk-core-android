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
// import org.junit.After;
// import org.junit.AfterClass;
// import org.junit.Before;
// import org.junit.BeforeClass;
// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.mockito.ArgumentCaptor;
// import org.mockito.Mock;
// import org.mockito.Mockito;
// import org.mockito.Spy;
// import org.mockito.invocation.InvocationOnMock;
// import org.mockito.junit.MockitoJUnitRunner;
// import org.mockito.stubbing.Answer;
// import android.app.Activity;
// import android.app.Application;
// import android.content.Context;
// import android.util.DisplayMetrics;
// import android.view.Display;
// import android.view.View;
// import android.view.ViewGroup;
// import android.view.ViewTreeObserver;
// import android.view.Window;
// import android.view.WindowManager;
// import static junit.framework.Assert.assertFalse;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.doAnswer;
// import static org.mockito.Mockito.doNothing;
// import static org.mockito.Mockito.reset;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
// import com.adobe.marketing.mobile.LoggingMode;
// import com.adobe.marketing.mobile.MobileCore;
// import com.adobe.marketing.mobile.services.internal.context.App;
// @RunWith(MockitoJUnitRunner.Silent.class)
// public class FloatingButtonManagerTests {
// 	private static final String VIEW_TAG = "ADBFloatingButtonTag";
// 	public static final String MOCK_ACTIVITY_LOCAL_CLASS_NAME = "com.mockclass";
// 	@Mock
// 	private Activity mockActivity;
// 	@Mock
// 	private WindowManager mockWindowManager;
// 	@Mock
// 	private Display mockDisplay;
// 	@Mock
// 	private Window mockWindow;
// 	@Mock
// 	private View mockDecorView;
// 	@Mock
// 	private ViewGroup mockRootView;
// 	@Mock
// 	private Context mockContext;
// 	@Mock
// 	private ViewTreeObserver mockViewTreeObserver;
// 	@Spy
// 	private AndroidUIService uiService;
// 	private FloatingButtonView floatingButtonViewSpy;
// 	private static Application mockApplication;
// 	private static ArgumentCaptor<Application.ActivityLifecycleCallbacks>
// activityLifecycleCallbacksArgumentCaptor;
// 	private static AppContextProvider appContextProvider = new AppContextProvider();
// 	private static class AppContextProvider implements App.AppContextProvider {
// 		private Activity currentActivity;
// 		public void setCurrentActivity(Activity currentActivity) {
// 			this.currentActivity = currentActivity;
// 		}
// 		@Override
// 		public Context getAppContext() {
// 			return mockApplication.getApplicationContext();
// 		}
// 		@Override
// 		public Activity getCurrentActivity() {
// 			return this.currentActivity;
// 		}
// 	}
// 	@BeforeClass
// 	public static void beforeClass() {
// 		mockApplication = Mockito.mock(Application.class);
// 		MobileCore.setApplication(mockApplication);
// 		App.getInstance().initializeApp(appContextProvider);
// 	}
// 	@AfterClass
// 	public static void afterClass() {
// 		//		App.clearAppResources();
// 	}
// 	@Before
// 	public void setup() {
// 		//Setup mock application to capture the lifecycleCallbacks for later
// 		activityLifecycleCallbacksArgumentCaptor =
// 			ArgumentCaptor.forClass(Application.ActivityLifecycleCallbacks.class);
// 		doNothing().when(mockApplication).registerActivityLifecycleCallbacks(
// 			activityLifecycleCallbacksArgumentCaptor.capture());
// 		//Create a floating button spy
// 		floatingButtonViewSpy = Mockito.spy(new FloatingButtonView(mockContext));
// 		when(floatingButtonViewSpy.getViewTreeObserver()).thenReturn(mockViewTreeObserver);
// 		//Mock the display Metrics
// 		doAnswer(new Answer() {
// 			@Override
// 			public Object answer(InvocationOnMock invocation) throws Throwable {
// 				DisplayMetrics displayMetrics = (DisplayMetrics) invocation.getArgument(0);
// 				displayMetrics.heightPixels = 500;
// 				displayMetrics.widthPixels = 300;
// 				return null;
// 			}
// 		}).when(mockDisplay).getMetrics(any(DisplayMetrics.class));
// 		when(mockWindowManager.getDefaultDisplay()).thenReturn(mockDisplay);
// 		when(mockActivity.getWindowManager()).thenReturn(mockWindowManager);
// 		when(mockDecorView.getRootView()).thenReturn(mockRootView);
// 		when(mockWindow.getDecorView()).thenReturn(mockDecorView);
// 		when(mockActivity.getWindow()).thenReturn(mockWindow);
// 		//Actually run the runnable - mocking the runOnUiThread()
// 		doAnswer(new Answer() {
// 			@Override
// 			public Object answer(InvocationOnMock invocation) throws Throwable {
// 				Runnable r = invocation.getArgument(0);
// 				r.run();
// 				return null;
// 			}
// 		}).when(mockActivity).runOnUiThread(any(Runnable.class));
// 		when(mockActivity.getLocalClassName()).thenReturn(MOCK_ACTIVITY_LOCAL_CLASS_NAME);
// 		appContextProvider.setCurrentActivity(mockActivity);
// 	}
// 	@After
// 	public void after() {
// 		reset(mockApplication);
// 	}
// 	@Test
// 	public void displayAddsButtonToScreen_When_NoButtonAlreadyExists() {
// 		//Setup
// 		FloatingButton floatingButton = new FloatingButtonManager(uiService, null);
// 		((FloatingButtonManager)floatingButton).addManagedButton(MOCK_ACTIVITY_LOCAL_CLASS_NAME,
// floatingButtonViewSpy);
// 		//test
// 		floatingButton.display();
// 		//verify
// 		Mockito.verify(mockRootView).addView(floatingButtonViewSpy);
// 	}
// 	@Test
// 	public void displayDoesNotAddButtonToScreen_When_ButtonAlreadyExists() {
// 		//Setup
// 		when(mockRootView.findViewWithTag(VIEW_TAG)).thenReturn(floatingButtonViewSpy);
// 		FloatingButton floatingButton = new FloatingButtonManager(uiService, null);
// 		((FloatingButtonManager)floatingButton).addManagedButton(MOCK_ACTIVITY_LOCAL_CLASS_NAME,
// floatingButtonViewSpy);
// 		//test
// 		floatingButton.display();
// 		//verify
// 		Mockito.verify(mockRootView, times(0)).addView(floatingButtonViewSpy);
// 	}
// 	@Test
// 	public void lifecycleCallbacksRemovesManagedButtons_When_AcitivityDestroyed() {
// 		//Setup
// 		FloatingButton floatingButton = new FloatingButtonManager(uiService, null);
// 		((FloatingButtonManager)floatingButton).addManagedButton(MOCK_ACTIVITY_LOCAL_CLASS_NAME,
// floatingButtonViewSpy);
// 		//test
// 		floatingButton.display();
// 		activityLifecycleCallbacksArgumentCaptor.getValue().onActivityDestroyed(mockActivity);
// 		//verify
//
//	assertFalse(((FloatingButtonManager)floatingButton).managedButtons.containsKey(MOCK_ACTIVITY_LOCAL_CLASS_NAME));
// 	}
// 	@Test
// 	public void lifecycleCallbacksPlacesExistingButtonInProperXY_When_ActivityResumed() {
// 		//Setup
// 		FloatingButton floatingButton = new FloatingButtonManager(uiService, null);
// 		((FloatingButtonManager)floatingButton).addManagedButton(MOCK_ACTIVITY_LOCAL_CLASS_NAME,
// floatingButtonViewSpy);
// 		when(floatingButtonViewSpy.getWidth()).thenReturn(50);
// 		when(floatingButtonViewSpy.getHeight()).thenReturn(50);
// 		ArgumentCaptor<FloatingButtonView.OnPositionChangedListener>
// onPositionChangedListenerArgumentCaptor =
// 			ArgumentCaptor.forClass(FloatingButtonView.OnPositionChangedListener.class);
//
//	doNothing().when(floatingButtonViewSpy).setOnPositionChangedListener(onPositionChangedListenerArgumentCaptor.capture());
// 		//display the button before activity resume
// 		floatingButton.display();
// 		onPositionChangedListenerArgumentCaptor.getValue().onPositionChanged(200, 300);
// 		when(mockRootView.findViewWithTag(VIEW_TAG)).thenReturn(floatingButtonViewSpy);
// 		//test
// 		activityLifecycleCallbacksArgumentCaptor.getValue().onActivityResumed(mockActivity);
// 		//verify that the existing button is placed in the same position as the last known position
// 		//(i.e. when onPositionChanged() was called last
// 		verify(floatingButtonViewSpy).setXYCompat(200, 300);
// 	}
// 	@Test
// 	public void
// lifecycleCallbacksPlacesExistingButtonInProperXY_When_ActivityResumedAfterOrientationChange() {
// 		//Setup
// 		FloatingButton floatingButton = new FloatingButtonManager(uiService, null);
// 		((FloatingButtonManager)floatingButton).addManagedButton(MOCK_ACTIVITY_LOCAL_CLASS_NAME,
// floatingButtonViewSpy);
// 		when(floatingButtonViewSpy.getWidth()).thenReturn(50);
// 		when(floatingButtonViewSpy.getHeight()).thenReturn(50);
// 		ArgumentCaptor<FloatingButtonView.OnPositionChangedListener>
// onPositionChangedListenerArgumentCaptor =
// 			ArgumentCaptor.forClass(FloatingButtonView.OnPositionChangedListener.class);
//
//	doNothing().when(floatingButtonViewSpy).setOnPositionChangedListener(onPositionChangedListenerArgumentCaptor.capture());
// 		//display the button before activity resume
// 		floatingButton.display();
// 		onPositionChangedListenerArgumentCaptor.getValue().onPositionChanged(200, 300);
// 		when(mockRootView.findViewWithTag(VIEW_TAG)).thenReturn(floatingButtonViewSpy);
// 		//Change orientation
// 		//Mock the display Metrics
// 		doAnswer(new Answer() {
// 			@Override
// 			public Object answer(InvocationOnMock invocation) throws Throwable {
// 				DisplayMetrics displayMetrics = (DisplayMetrics) invocation.getArgument(0);
// 				displayMetrics.heightPixels = 300; //from 500 before
// 				displayMetrics.widthPixels = 500; //from 300 before
// 				return null;
// 			}
// 		}).when(mockDisplay).getMetrics(any(DisplayMetrics.class));
// 		//test
// 		activityLifecycleCallbacksArgumentCaptor.getValue().onActivityResumed(mockActivity);
// 		//verify that the existing button is placed in the a position adjusted from the
// 		//last known position due to screen height and width change
// 		verify(floatingButtonViewSpy).setXYCompat(200, 250);
// 	}
// 	@Test
// 	public void lifecycleCallbacksUnregistersCallbacks_When_AllButtonsRemoved() {
// 		//Setup
// 		FloatingButton floatingButton = new FloatingButtonManager(uiService, null);
// 		((FloatingButtonManager)floatingButton).addManagedButton(MOCK_ACTIVITY_LOCAL_CLASS_NAME,
// floatingButtonViewSpy);
// 		when(floatingButtonViewSpy.getWidth()).thenReturn(50);
// 		when(floatingButtonViewSpy.getHeight()).thenReturn(50);
// 		when(mockRootView.findViewWithTag(VIEW_TAG)).thenReturn(floatingButtonViewSpy);
// 		//display the button
// 		floatingButton.display();
// 		//Remove the button
// 		floatingButton.remove();
// 		//test if activity resume after remove unregisters callbacks
// 		activityLifecycleCallbacksArgumentCaptor.getValue().onActivityResumed(mockActivity);
// 		//verify
//
//	verify(mockApplication).unregisterActivityLifecycleCallbacks(any(Application.ActivityLifecycleCallbacks.class));
// 	}
// 	@Test
// 	public void removeCallsSetVisibility_When_ButtonAlreadyVisible() {
// 		//Setup
// 		when(mockRootView.findViewWithTag(VIEW_TAG)).thenReturn(floatingButtonViewSpy);
// 		FloatingButton floatingButton = new FloatingButtonManager(uiService, null);
// 		((FloatingButtonManager)floatingButton).addManagedButton(MOCK_ACTIVITY_LOCAL_CLASS_NAME,
// floatingButtonViewSpy);
// 		//test
// 		floatingButton.remove();
// 		//verify
// 		Mockito.verify(floatingButtonViewSpy).setVisibility(View.GONE);
// 	}
// }
