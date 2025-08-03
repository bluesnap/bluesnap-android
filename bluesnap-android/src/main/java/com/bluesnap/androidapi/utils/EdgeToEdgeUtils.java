package com.bluesnap.androidapi.utils;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Utility class to handle edge-to-edge layouts on Android 15+ with minimal UI changes
 */
public class EdgeToEdgeUtils {

    /**
     * Enable edge-to-edge experience for the activity
     */
    public static void enableEdgeToEdge(Activity activity) {
        if (Build.VERSION.SDK_INT >= 35) { // Android 15 (API 35)
            WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        }
    }

    /**
     * Apply system bar insets padding to a view to avoid overlap with status bar/navigation bar
     * This is the minimal approach - just add padding to the top-level container
     */
    public static void applySystemBarInsets(View view) {
        if (Build.VERSION.SDK_INT >= 35) { // Android 15 (API 35)
            ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                
                // Apply insets as padding to avoid content overlap
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                if (params != null) {
                    params.topMargin = insets.top;
                    params.bottomMargin = insets.bottom;
                    v.setLayoutParams(params);
                } else {
                    // Fallback to padding if margins aren't available
                    v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                }
                
                return WindowInsetsCompat.CONSUMED;
            });
        }
    }

    /**
     * Apply top system bar insets only (for header areas)
     */
    public static void applyTopSystemBarInsets(View view) {
        if (Build.VERSION.SDK_INT >= 35) { // Android 15 (API 35)
            ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                
                // Only apply top inset to avoid status bar overlap
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                if (params != null) {
                    params.topMargin = insets.top;
                    v.setLayoutParams(params);
                } else {
                    // Fallback to padding
                    v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), v.getPaddingBottom());
                }
                
                return windowInsets;
            });
        }
    }
}