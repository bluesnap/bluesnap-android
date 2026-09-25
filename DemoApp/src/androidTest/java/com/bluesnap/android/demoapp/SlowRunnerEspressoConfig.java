package com.bluesnap.android.demoapp;

import android.view.View;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.UiController;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;

import org.hamcrest.Matcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

/**
 * Enhanced Espresso configuration specifically for slow/busy CI runners
 * This class provides replacement methods that include proactive window focus handling
 */
public class SlowRunnerEspressoConfig {

    /**
     * Enhanced click action that waits for window focus before clicking
     * Use this instead of ViewActions.click() for better CI stability
     */
    public static ViewAction safeClick() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewActions.click().getConstraints();
            }

            @Override
            public String getDescription() {
                return "Safe click with window focus wait for slow CI";
            }

            @Override
            public void perform(UiController uiController, View view) {
                // First wait for the root view to have window focus
                waitForWindowFocusInternal(uiController, view, 60000);

                // Then perform the actual click
                ViewActions.click().perform(uiController, view);
            }
        };
    }

    /**
     * Enhanced type text action that waits for window focus before typing
     */
    public static ViewAction safeTypeText(String text) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewActions.typeText(text).getConstraints();
            }

            @Override
            public String getDescription() {
                return "Safe type text with window focus wait: " + text;
            }

            @Override
            public void perform(UiController uiController, View view) {
                // Wait for window focus first
                waitForWindowFocusInternal(uiController, view, 60000);

                // Then type the text
                ViewActions.typeText(text).perform(uiController, view);
            }
        };
    }

    /**
     * Enhanced clear text action that waits for window focus
     */
    public static ViewAction safeClearText() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewActions.clearText().getConstraints();
            }

            @Override
            public String getDescription() {
                return "Safe clear text with window focus wait";
            }

            @Override
            public void perform(UiController uiController, View view) {
                // Wait for window focus first
                waitForWindowFocusInternal(uiController, view, 60000);

                // Then clear the text
                ViewActions.clearText().perform(uiController, view);
            }
        };
    }

    /**
     * Enhanced scroll action that waits for window focus
     */
    public static ViewAction safeScrollTo() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewActions.scrollTo().getConstraints();
            }

            @Override
            public String getDescription() {
                return "Safe scroll with window focus wait";
            }

            @Override
            public void perform(UiController uiController, View view) {
                // Wait for window focus first
                waitForWindowFocusInternal(uiController, view, 60000);

                // Then scroll
                ViewActions.scrollTo().perform(uiController, view);
            }
        };
    }

    /**
     * Internal method to wait for window focus with a specific timeout
     */
    private static void waitForWindowFocusInternal(UiController uiController, View view, long timeoutMillis) {
        final long endTime = System.currentTimeMillis() + timeoutMillis;

        while (System.currentTimeMillis() < endTime) {
            // Check if any root view in the hierarchy has window focus
            View rootView = view.getRootView();
            if (rootView != null && rootView.hasWindowFocus() && !rootView.isLayoutRequested()) {
                return; // Success - we have focus and no layout pending
            }

            // Wait for main thread to be idle
            uiController.loopMainThreadUntilIdle();

            // Small sleep to avoid busy waiting
            uiController.loopMainThreadForAtLeast(200);
        }

        // If we get here, we timed out - log it but don't throw an exception
        // Let the actual Espresso action handle its own timeout
        System.err.println("SlowRunnerEspressoConfig: Window focus wait timed out after " + timeoutMillis + "ms");
    }

    /**
     * Setup method that should be called at the beginning of each test
     * This configures Espresso for slow CI environments
     */
    public static void setupForSlowRunner() {
        // Configure the existing timeout config
        EspressoTimeoutConfig.configureForSlowRunner();

        // Additional setup could go here
    }

    /**
     * Helper method that performs an action with automatic window focus waiting
     */
    public static void performWithWindowFocusWait(ViewInteraction viewInteraction, ViewAction... actions) {
        try {
            // First ensure we have window focus at the root level
            onView(isRoot()).perform(EspressoTimeoutConfig.waitForWindowFocus());
        } catch (Exception e) {
            // Continue anyway if root focus check fails
        }

        // Then perform the actions
        viewInteraction.perform(actions);
    }

    /**
     * Helper method for safe clicking with automatic retries
     */
    public static void performSafeClick(ViewInteraction viewInteraction) {
        performSafeClick(viewInteraction, 3); // Default 3 retries
    }

    /**
     * Helper method for safe clicking with specified retries
     */
    public static void performSafeClick(ViewInteraction viewInteraction, int maxRetries) {
        Exception lastException = null;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    // Wait a bit between retries
                    Thread.sleep(1000);
                }

                performWithWindowFocusWait(viewInteraction, safeClick());
                return; // Success

            } catch (Exception e) {
                lastException = e;
                System.err.println("SlowRunnerEspressoConfig: Click attempt " + (attempt + 1) + " failed: " + e.getMessage());
            }
        }

        // If all retries failed, throw the last exception
        if (lastException instanceof RuntimeException) {
            throw (RuntimeException) lastException;
        } else {
            throw new RuntimeException("Safe click failed after " + maxRetries + " attempts", lastException);
        }
    }
}