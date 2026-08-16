package ua.kharkiv.bolgrad46;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Compatibility launcher for Android/MIUI builds where Window#setDecorFitsSystemWindows(false)
 * can be called before DecorView has created its WindowInsetsController.
 *
 * Runtime UI tuning is applied here so both the normal Android path and the MIUI recovery path
 * use the same polished controls.
 */
public class SafeMainActivity extends MainActivity {
    private static final int BG = Color.rgb(4, 16, 31);
    private static final int TEXT = Color.rgb(247, 249, 253);

    private View appRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
        } catch (NullPointerException exception) {
            if (isWindowInsetsControllerCrash(exception)) {
                recoverFromInsetsControllerCrash();
            } else {
                throw exception;
            }
        }

        installUiTuning();
    }

    private boolean isWindowInsetsControllerCrash(NullPointerException exception) {
        boolean result = false;
        String message = exception.getMessage();

        if (Build.VERSION.SDK_INT >= 30 && message != null) {
            if (message.contains("WindowInsetsController")) {
                result = true;
            }
        }

        return result;
    }

    private void recoverFromInsetsControllerCrash() {
        Window window = getWindow();
        window.setStatusBarColor(BG);
        window.setNavigationBarColor(BG);

        if (Build.VERSION.SDK_INT >= 29) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }

        invokePrivateNoArgs("buildShell");

        View rootView = getPrivateRoot();
        setContentView(rootView);

        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        invokePrivateNoArgs("applyInsets");
        invokePrivateInt("showPage", 0);
    }

    private void installUiTuning() {
        appRoot = getPrivateRoot();
        appRoot.getViewTreeObserver().addOnGlobalLayoutListener(() -> tuneViewTree(appRoot));
        appRoot.post(() -> tuneViewTree(appRoot));
    }

    private void tuneViewTree(View view) {
        if (view instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) view;
            scrollView.setVerticalScrollBarEnabled(false);
            scrollView.setHorizontalScrollBarEnabled(false);
            scrollView.setScrollbarFadingEnabled(true);
            scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }

        if (view instanceof TextView) {
            tuneText((TextView) view);
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int index = 0;

            while (index < group.getChildCount()) {
                View child = group.getChildAt(index);

                if (isLegacySwipeControl(child) || child instanceof AdaptiveSwipeControl) {
                    ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
                    group.removeViewAt(index);

                    HoldToOpenControl replacement = new HoldToOpenControl(this);
                    group.addView(replacement, index, layoutParams);
                    child = replacement;
                }

                tuneViewTree(child);
                index++;
            }
        }
    }

    private boolean isLegacySwipeControl(View view) {
        return "SwipeControl".equals(view.getClass().getSimpleName());
    }

    private void tuneText(TextView textView) {
        CharSequence value = textView.getText();

        if (value != null && "Мой дом".contentEquals(value)) {
            View parent = (View) textView.getParent();
            if (parent != null) {
                parent.setVisibility(View.GONE);
            } else {
                textView.setVisibility(View.GONE);
            }
        } else if (value != null && "Болградская, 46".contentEquals(value)) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f);
            textView.setTextColor(TEXT);
            textView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            textView.setLetterSpacing(-0.01f);
            textView.setIncludeFontPadding(false);
        }
    }

    private View getPrivateRoot() {
        View result;

        try {
            Field field = MainActivity.class.getDeclaredField("root");
            field.setAccessible(true);
            result = (View) field.get(this);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to restore application root view", exception);
        }

        return result;
    }

    private void invokePrivateNoArgs(String methodName) {
        try {
            Method method = MainActivity.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(this);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to invoke " + methodName, exception);
        }
    }

    private void invokePrivateInt(String methodName, int value) {
        try {
            Method method = MainActivity.class.getDeclaredMethod(methodName, int.class);
            method.setAccessible(true);
            method.invoke(this, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to invoke " + methodName, exception);
        }
    }
}
