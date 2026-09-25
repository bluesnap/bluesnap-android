package com.bluesnap.android.demoapp;

import android.view.View;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.IdlingPolicy;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.action.ViewActions;

import org.hamcrest.Matcher;

import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

/**
 * Utility class to configure Espresso timeouts for slow/busy CI runners
 * Addresses the "Waited for the root of the view hierarchy to have window focus" timeout issues
 */
public class EspressoTimeoutConfig {

    private static boolean isConfigured = false;

    /**
     * Configure Espresso with extended timeouts for slow CI environments
     * This should be called before any Espresso operations in tests
     */
    public static void configureForSlowRunner() {
        if (isConfigured) {
            return; // Already configured
        }

        try {
            // Set master timeout policy to 60 seconds (up from default 10 seconds)
            IdlingPolicies.setMasterPolicyTimeout(60, TimeUnit.SECONDS);

            // Set idle timeout to 60 seconds (up from default 10 seconds)
            IdlingPolicies.setIdlingResourceTimeout(60, TimeUnit.SECONDS);

            isConfigured = true;

        } catch (Exception e) {
            // Log but don't fail - use default timeouts if configuration fails
            System.err.println("Warning: Failed to configure Espresso timeouts: " + e.getMessage());
        }
    }

    /**
     * Custom ViewAction that waits for the root view to have window focus
     * This replaces Espresso's internal RootViewPicker timeout with a longer custom timeout
     */
    public static ViewAction waitForWindowFocus(final long timeoutMillis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for window focus for " + timeoutMillis + "ms";
            }

            @Override
            public void perform(UiController uiController, View view) {
                final long endTime = System.currentTimeMillis() + timeoutMillis;

                while (System.currentTimeMillis() < endTime) {
                    // Check if the root view has window focus
                    if (view.hasWindowFocus() && !view.isLayoutRequested()) {
                        return; // Success - we have focus and no layout pending
                    }

                    // Wait for main thread to be idle
                    uiController.loopMainThreadUntilIdle();

                    // Small sleep to avoid busy waiting
                    uiController.loopMainThreadForAtLeast(100);
                }

                // If we get here, we timed out - but don't throw, let the actual test continue
                // The subsequent Espresso operations will handle their own timeouts
            }
        };
    }

    /**
     * Wait for window focus with default 60-second timeout for CI
     */
    public static ViewAction waitForWindowFocus() {
        return waitForWindowFocus(60000); // 60 seconds
    }

    /**
     * Perform a "safe" click that waits for window focus first
     */
    public static void safeClick(ViewInteraction viewInteraction) {
        // First ensure we have window focus
        onView(isRoot()).perform(waitForWindowFocus());
        // Then perform the click
        viewInteraction.perform(ViewActions.click());
    }

    /**
     * Perform a "safe" action that waits for window focus first
     */
    public static void safePerform(ViewInteraction viewInteraction, ViewAction... actions) {
        // First ensure we have window focus
        onView(isRoot()).perform(waitForWindowFocus());
        // Then perform the actions
        viewInteraction.perform(actions);
    }

    /**
     * Reset Espresso timeouts to defaults
     * Useful for cleanup in tests
     */
    public static void resetToDefaults() {
        try {
            // Reset to default Espresso timeouts (10 seconds)
            IdlingPolicies.setMasterPolicyTimeout(10, TimeUnit.SECONDS);
            IdlingPolicies.setIdlingResourceTimeout(10, TimeUnit.SECONDS);

            isConfigured = false;

        } catch (Exception e) {
            System.err.println("Warning: Failed to reset Espresso timeouts: " + e.getMessage());
        }
    }

    /**
     * Get current master policy timeout in seconds
     */
    public static long getCurrentMasterTimeoutSeconds() {
        try {
            IdlingPolicy masterPolicy = IdlingPolicies.getMasterIdlingPolicy();
            return masterPolicy.getIdleTimeout();
        } catch (Exception e) {
            return 10; // Default fallback
        }
    }

    /**
     * Configure extended timeouts only if running in CI environment
     * Detects CI by checking for common CI environment variables
     */
    public static void configureForCiIfNeeded() {
        if (isRunningInCi()) {
            configureForSlowRunner();
        }
    }

    /**
     * Detect if running in CI environment by checking environment variables
     */
    private static boolean isRunningInCi() {
        String[] ciEnvVars = {
            "CI",
            "CONTINUOUS_INTEGRATION",
            "GITHUB_ACTIONS",
            "JENKINS_URL",
            "BUILD_NUMBER",
            "ANDROID_EMULATOR_WAIT_TIME_BEFORE_KILL"
        };

        for (String envVar : ciEnvVars) {
            if (System.getenv(envVar) != null) {
                return true;
            }
        }

        return false;
    }
}