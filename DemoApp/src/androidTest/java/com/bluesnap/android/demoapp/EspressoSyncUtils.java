package com.bluesnap.android.demoapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

import android.view.View;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;

import org.hamcrest.Matcher;

/**
 * Utility class for better Espresso synchronization without Thread.sleep()
 */
public class EspressoSyncUtils {

    /**
     * Custom ViewAction that waits for a view to be displayed before proceeding
     */
    public static ViewAction waitForViewToBeDisplayed() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(View.class);
            }

            @Override
            public String getDescription() {
                return "Wait for view to be displayed";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();
                // Additional wait for view to be stable
                uiController.loopMainThreadForAtLeast(100);
            }
        };
    }

    /**
     * Safely type text into a field with proper synchronization
     */
    public static void safeTypeText(ViewInteraction viewInteraction, String text) {
        // Ensure view is displayed and ready
        viewInteraction.check(matches(isDisplayed()));
        
        // Clear and type with proper synchronization
        viewInteraction.perform(clearText());
        viewInteraction.perform(waitForViewToBeDisplayed());
        viewInteraction.perform(typeText(text));
        viewInteraction.perform(waitForViewToBeDisplayed());
    }

    /**
     * Safely type text into a field by ID with proper synchronization
     */
    public static void safeTypeTextById(int viewId, String text) {
        safeTypeText(onView(withId(viewId)), text);
    }

    /**
     * Safely type text into a field with parent constraint
     */
    public static void safeTypeTextWithParent(int viewId, int parentId, String text) {
        safeTypeText(onView(allOf(withId(viewId), ViewMatchers.isDescendantOfA(withId(parentId)))), text);
    }

    /**
     * Wait for UI to be idle with timeout
     */
    public static void waitForIdle() {
        Espresso.onIdle();
    }

    /**
     * Register a temporary IdlingResource for a specific duration
     */
    public static void waitWithIdlingResource(long milliseconds) {
        ElapsedTimeIdlingResource idlingResource = new ElapsedTimeIdlingResource(milliseconds);
        IdlingRegistry.getInstance().register(idlingResource);
        // Force Espresso to wait for the resource
        waitForIdle();
        IdlingRegistry.getInstance().unregister(idlingResource);
    }

    /**
     * Quick wait for UI stability - shorter than full IdlingResource
     */
    public static void quickWait() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Ensure keyboard is closed and UI is stable
     */
    public static void ensureKeyboardClosedAndStable() {
        closeSoftKeyboard();
        waitForIdle();
    }

    /**
     * Custom ViewAction that ensures a field is ready for input
     */
    public static ViewAction ensureFieldReady() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(View.class);
            }

            @Override
            public String getDescription() {
                return "Ensure field is ready for input";
            }

            @Override
            public void perform(UiController uiController, View view) {
                // Ensure main thread is idle
                uiController.loopMainThreadUntilIdle();
                
                // Give additional time for field to be ready
                uiController.loopMainThreadForAtLeast(50);
                
                // Focus the view if it's focusable
                if (view.isFocusable() && !view.isFocused()) {
                    view.requestFocus();
                    uiController.loopMainThreadUntilIdle();
                }
            }
        };
    }

    /**
     * Retry mechanism for flaky UI operations - optimized for speed
     */
    public static void retryOperation(Runnable operation, int maxRetries, String operationName) {
        Exception lastException = null;
        
        for (int i = 0; i < maxRetries; i++) {
            try {
                if (i == 0) {
                    // First attempt - just ensure idle
                    waitForIdle();
                } else {
                    // Subsequent attempts - short wait
                    quickWait();
                }
                operation.run();
                return; // Success, exit retry loop
            } catch (Exception e) {
                lastException = e;
                // Don't wait after the last failed attempt
                if (i < maxRetries - 1) {
                    quickWait();
                }
            }
        }
        
        // If we get here, all retries failed
        throw new RuntimeException("Operation '" + operationName + "' failed after " + maxRetries + " retries. Last error: " + lastException.getMessage(), lastException);
    }

    /**
     * Wait for a view to be displayed with timeout and retries
     */
    public static ViewAction waitForViewToBeDisplayedWithRetry() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(View.class);
            }

            @Override
            public String getDescription() {
                return "Wait for view to be displayed with retry logic";
            }

            @Override
            public void perform(UiController uiController, View view) {
                // Wait with multiple checks for view to be properly displayed
                int attempts = 0;
                while (attempts < 10 && (view.getVisibility() != View.VISIBLE || view.getWidth() == 0 || view.getHeight() == 0)) {
                    uiController.loopMainThreadUntilIdle();
                    uiController.loopMainThreadForAtLeast(50);
                    attempts++;
                }
                
                // Final check to ensure view is ready
                uiController.loopMainThreadUntilIdle();
            }
        };
    }
}