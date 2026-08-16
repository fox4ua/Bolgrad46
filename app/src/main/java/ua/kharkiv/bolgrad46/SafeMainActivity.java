package ua.kharkiv.bolgrad46;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
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
 * It also applies a small amount of runtime UI tuning that is intentionally kept outside the
 * main screen implementation so the compatibility path and the normal Android path look the same.
 */
public class SafeMainActivity extends MainActivity {
    private static final int BG = Color.rgb(4, 16, 31);

    private View appRoot;
    private ScrollView activeSwipeScroll;
    private boolean swipeGesture;

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

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (appRoot != null) {
            int action = event.getActionMasked();

            if (action == MotionEvent.ACTION_DOWN) {
                View swipe = findSwipeControl(appRoot, event.getRawX(), event.getRawY());
                if (swipe != null) {
                    activeSwipeScroll = findParentScrollView(swipe);
                    if (activeSwipeScroll != null) {
                        swipeGesture = true;
                        activeSwipeScroll.requestDisallowInterceptTouchEvent(true);
                    }
                }
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                if (swipeGesture && activeSwipeScroll != null) {
                    activeSwipeScroll.requestDisallowInterceptTouchEvent(false);
                }
                swipeGesture = false;
                activeSwipeScroll = null;
            }
        }

        return super.dispatchTouchEvent(event);
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
            TextView textView = (TextView) view;
            CharSequence value = textView.getText();

            if (value != null && "Мой дом".contentEquals(value)) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 27f);
                textView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                textView.setLetterSpacing(-0.015f);
                textView.setIncludeFontPadding(false);
            } else if (value != null && "Болградская, 46".contentEquals(value)) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f);
                textView.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
                textView.setLetterSpacing(0f);
                textView.setIncludeFontPadding(false);
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int childCount = group.getChildCount();
            for (int i = 0; i < childCount; i++) {
                tuneViewTree(group.getChildAt(i));
            }
        }
    }

    private View findSwipeControl(View view, float rawX, float rawY) {
        View result = null;

        if (view.getVisibility() == View.VISIBLE && isPointInside(view, rawX, rawY)) {
            if ("SwipeControl".equals(view.getClass().getSimpleName())) {
                result = view;
            } else if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                for (int i = group.getChildCount() - 1; i >= 0 && result == null; i--) {
                    result = findSwipeControl(group.getChildAt(i), rawX, rawY);
                }
            }
        }

        return result;
    }

    private boolean isPointInside(View view, float rawX, float rawY) {
        Rect rect = new Rect();
        boolean visible = view.getGlobalVisibleRect(rect);
        return visible && rect.contains(Math.round(rawX), Math.round(rawY));
    }

    private ScrollView findParentScrollView(View view) {
        ScrollView result = null;
        android.view.ViewParent parent = view.getParent();

        while (parent != null && result == null) {
            if (parent instanceof ScrollView) {
                result = (ScrollView) parent;
            } else {
                parent = parent.getParent();
            }
        }

        return result;
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
