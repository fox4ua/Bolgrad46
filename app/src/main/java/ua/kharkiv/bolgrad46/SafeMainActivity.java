package ua.kharkiv.bolgrad46;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Compatibility launcher for Android/MIUI builds where Window#setDecorFitsSystemWindows(false)
 * can be called before DecorView has created its WindowInsetsController.
 *
 * MainActivity already calls Activity#onCreate before that platform crash happens, so this
 * wrapper catches only that specific framework NPE and completes the normal screen setup using
 * the legacy layout flags. Other NullPointerExceptions are rethrown unchanged.
 */
public class SafeMainActivity extends MainActivity {
    private static final int BG = Color.rgb(4, 16, 31);

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
