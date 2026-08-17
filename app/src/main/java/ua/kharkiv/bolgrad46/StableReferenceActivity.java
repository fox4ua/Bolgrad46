package ua.kharkiv.bolgrad46;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.lang.reflect.Constructor;

public class StableReferenceActivity extends Activity {
    private static final int BG = Color.rgb(247, 249, 253);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(BG);
        window.setNavigationBarColor(BG);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG);

        View screen = createReferenceScreen();
        root.addView(screen, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        ));

        setContentView(root);
    }

    private View createReferenceScreen() {
        View result;

        try {
            Class<?> screenClass = Class.forName(
                "ua.kharkiv.bolgrad46.ReferenceActivity$ReferenceView"
            );
            Constructor<?> constructor = screenClass.getDeclaredConstructor(android.content.Context.class);
            constructor.setAccessible(true);
            result = (View) constructor.newInstance(this);
        } catch (Exception exception) {
            TextView fallback = new TextView(this);
            fallback.setText("Не удалось загрузить интерфейс");
            fallback.setTextColor(Color.rgb(35, 54, 91));
            fallback.setTextSize(20f);
            fallback.setGravity(Gravity.CENTER);
            fallback.setBackgroundColor(BG);
            result = fallback;
        }

        return result;
    }
}
