package ua.kharkiv.bolgrad46;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(Color.rgb(6, 23, 43));
        window.setNavigationBarColor(Color.rgb(4, 18, 36));

        setContentView(new HouseView(this));
    }

    private static final class HouseView extends View {
        private static final int BG = Color.rgb(6, 23, 43);
        private static final int BG_BOTTOM = Color.rgb(4, 16, 31);
        private static final int SURFACE = Color.rgb(9, 31, 55);
        private static final int SURFACE_2 = Color.rgb(12, 40, 69);
        private static final int BORDER = Color.rgb(28, 67, 105);
        private static final int BLUE = Color.rgb(37, 133, 255);
        private static final int BLUE_LIGHT = Color.rgb(83, 164, 255);
        private static final int TEXT = Color.rgb(246, 249, 255);
        private static final int MUTED = Color.rgb(139, 164, 197);
        private static final int GREEN = Color.rgb(54, 211, 128);
        private static final int WARM = Color.rgb(255, 194, 99);

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Handler handler = new Handler(Looper.getMainLooper());

        private float scale = 1f;
        private float virtualHeight = 2200f;
        private int selectedTab = 0;
        private float swipeOffset = 0f;
        private boolean dragging = false;
        private boolean opened = false;
        private ValueAnimator swipeAnimator;

        HouseView(Context context) {
            super(context);
            paint.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.NORMAL));
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            setBackgroundColor(BG);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            scale = getWidth() / 1080f;
            virtualHeight = getHeight() / scale;

            canvas.save();
            canvas.scale(scale, scale);

            paint.setShader(new LinearGradient(0, 0, 0, virtualHeight, BG, BG_BOTTOM, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, 1080, virtualHeight, paint);
            paint.setShader(null);

            if (selectedTab == 0) {
                drawHome(canvas);
            } else if (selectedTab == 1) {
                drawEventsScreen(canvas);
            } else if (selectedTab == 2) {
                drawRequestsScreen(canvas);
            } else {
                drawProfileScreen(canvas);
            }

            drawBottomNavigation(canvas);
            canvas.restore();
        }

        private void drawHome(Canvas c) {
            drawHeader(c, "Мой дом", "Болградская, 46");
            drawHero(c);
            drawSwipe(c);
            drawSystemCard(c);
            drawRecentEvents(c);
        }

        private void drawHeader(Canvas c, String title, String subtitle) {
            text(c, title, 54, 86, 48, TEXT, true);
            drawChevronDown(c, 340, 67, MUTED);
            text(c, subtitle, 54, 133, 25, MUTED, false);

            card(c, 906, 38, 1026, 158, 28, SURFACE, BORDER);
            drawBell(c, 966, 94);
            circle(c, 1001, 55, 9, BLUE);
        }

        private void drawHero(Canvas c) {
            final float left = 44;
            final float top = 180;
            final float right = 1036;
            final float bottom = 612;
            final float radius = 34;

            Path clip = new Path();
            clip.addRoundRect(new RectF(left, top, right, bottom), radius, radius, Path.Direction.CW);
            c.save();
            c.clipPath(clip);

            paint.setShader(new LinearGradient(left, top, right, bottom,
                    Color.rgb(15, 52, 92), Color.rgb(5, 21, 39), Shader.TileMode.CLAMP));
            c.drawRect(left, top, right, bottom, paint);
            paint.setShader(null);

            // Soft, deliberately illustrated background instead of a photo.
            circle(c, 92, 320, 125, Color.argb(150, 10, 42, 57));
            circle(c, 1010, 350, 160, Color.argb(150, 8, 50, 52));
            circle(c, 160, 520, 115, Color.argb(210, 9, 48, 49));
            circle(c, 945, 520, 130, Color.argb(215, 8, 45, 47));

            drawIllustratedHouse(c);
            drawWalkway(c);
            drawTrees(c);

            // Dark vignette at edges.
            paint.setShader(new LinearGradient(left, top, left, bottom,
                    Color.argb(0, 2, 10, 22), Color.argb(95, 2, 10, 22), Shader.TileMode.CLAMP));
            c.drawRect(left, top, right, bottom, paint);
            paint.setShader(null);

            c.restore();
            roundStroke(c, left, top, right, bottom, radius, BORDER, 2);

            roundFill(c, 68, 202, 230, 254, 26, Color.argb(220, 5, 22, 39));
            circle(c, 91, 228, 8, GREEN);
            text(c, "Онлайн", 111, 238, 18, TEXT, true);
        }

        private void drawIllustratedHouse(Canvas c) {
            // The facade follows the real entrance-side character of Bolgradskaya 46:
            // gray panel sections, green seams, left balcony stack and narrow stairwell windows.
            Path building = new Path();
            building.moveTo(190, 236);
            building.lineTo(970, 204);
            building.lineTo(1018, 565);
            building.lineTo(176, 565);
            building.close();

            paint.setColor(Color.rgb(113, 124, 133));
            c.drawPath(building, paint);

            // Slight warm/cool panel patches keep it illustrative.
            paint.setColor(Color.argb(55, 213, 207, 184));
            c.drawRect(350, 233, 720, 560, paint);
            paint.setColor(Color.argb(45, 67, 100, 123));
            c.drawRect(720, 220, 1005, 560, paint);

            // Panel seams.
            stroke.setColor(Color.rgb(63, 92, 78));
            stroke.setStrokeWidth(3);
            for (int i = 1; i < 9; i++) {
                float y = 238 + i * 35.5f;
                c.drawLine(188, y, 1012, y - 4, stroke);
            }
            for (int i = 0; i < 8; i++) {
                float x = 326 + i * 87f;
                c.drawLine(x, 228, x + 8, 562, stroke);
            }

            // Left stacked balconies, distinctive for the real house.
            for (int floor = 0; floor < 8; floor++) {
                float y = 251 + floor * 38.5f;
                float x = 206 + floor * 0.8f;
                paint.setColor(Color.rgb(86, 96, 101));
                c.drawRect(x, y, x + 132, y + 28, paint);
                stroke.setColor(Color.rgb(42, 55, 64));
                stroke.setStrokeWidth(2);
                c.drawRect(x, y, x + 132, y + 28, stroke);
                for (int k = 1; k < 4; k++) {
                    c.drawLine(x + k * 32, y + 2, x + k * 32, y + 27, stroke);
                }
                // Decorative panel texture.
                paint.setColor(Color.argb(75, 218, 208, 175));
                for (int p = 0; p < 6; p++) {
                    float px = x + 8 + p * 20;
                    c.drawCircle(px, y + 22, 2.5f, paint);
                }
            }

            // Regular apartment windows.
            for (int floor = 0; floor < 8; floor++) {
                float y = 248 + floor * 38.6f;
                for (int col = 0; col < 6; col++) {
                    float x = 375 + col * 98f;
                    if (col == 2) {
                        continue;
                    }
                    boolean lit = ((floor * 7 + col * 5) % 6 == 0) || (floor == 1 && col == 4);
                    drawWindow(c, x, y, 44, 25, lit);
                }
            }

            // Narrow stairwell windows in the central strip.
            for (int floor = 0; floor < 9; floor++) {
                float y = 226 + floor * 36.8f;
                boolean lit = floor == 2 || floor == 6;
                drawWindow(c, 568, y, 62, 19, lit);
            }

            // Entrance canopy and the recognizable blue utility/entrance zone.
            paint.setColor(Color.rgb(74, 78, 78));
            c.drawRect(444, 510, 742, 526, paint);
            paint.setColor(Color.rgb(22, 43, 57));
            c.drawRect(474, 526, 598, 570, paint);
            paint.setColor(Color.rgb(17, 93, 135));
            c.drawRect(598, 526, 716, 570, paint);
            stroke.setColor(Color.rgb(16, 39, 50));
            stroke.setStrokeWidth(3);
            c.drawRect(474, 526, 598, 570, stroke);
            c.drawRect(598, 526, 716, 570, stroke);

            // Door details and small mural motifs inspired by the entrance photo.
            paint.setColor(Color.rgb(56, 64, 67));
            c.drawRect(500, 535, 566, 570, paint);
            circle(c, 556, 553, 3, Color.rgb(194, 201, 205));
            paint.setColor(Color.rgb(244, 180, 58));
            c.drawCircle(625, 546, 10, paint);
            c.drawCircle(682, 548, 10, paint);
            paint.setColor(Color.rgb(31, 105, 74));
            c.drawRect(620, 554, 626, 569, paint);
            c.drawRect(679, 556, 685, 569, paint);

            // Steps painted blue/white like the reference entrance.
            paint.setColor(Color.rgb(88, 121, 142));
            c.drawRect(458, 570, 716, 578, paint);
            paint.setColor(Color.rgb(53, 139, 188));
            c.drawRect(472, 578, 700, 584, paint);
            paint.setColor(Color.rgb(218, 224, 221));
            c.drawRect(486, 584, 686, 590, paint);
        }

        private void drawWindow(Canvas c, float x, float y, float w, float h, boolean lit) {
            paint.setColor(Color.rgb(43, 56, 65));
            c.drawRect(x, y, x + w, y + h, paint);
            paint.setColor(lit ? WARM : Color.rgb(104, 132, 148));
            c.drawRect(x + 4, y + 4, x + w - 4, y + h - 4, paint);
            stroke.setColor(Color.rgb(59, 72, 78));
            stroke.setStrokeWidth(2);
            c.drawLine(x + w / 2, y + 3, x + w / 2, y + h - 3, stroke);
        }

        private void drawWalkway(Canvas c) {
            paint.setColor(Color.rgb(21, 33, 41));
            Path p = new Path();
            p.moveTo(390, 590);
            p.lineTo(770, 590);
            p.lineTo(930, 612);
            p.lineTo(175, 612);
            p.close();
            c.drawPath(p, paint);

            stroke.setColor(Color.argb(100, 79, 109, 127));
            stroke.setStrokeWidth(2);
            for (int i = 0; i < 6; i++) {
                float yy = 593 + i * 4;
                c.drawLine(190, yy, 920, yy, stroke);
            }
        }

        private void drawTrees(Canvas c) {
            // Simplified foliage keeps the hero visually soft and painted.
            paint.setColor(Color.rgb(18, 58, 48));
            c.drawRect(145, 430, 152, 575, paint);
            c.drawRect(840, 415, 848, 575, paint);
            int leaf = Color.rgb(20, 73, 58);
            circle(c, 145, 414, 48, leaf);
            circle(c, 115, 450, 58, leaf);
            circle(c, 175, 460, 56, Color.rgb(27, 81, 61));
            circle(c, 850, 404, 56, Color.rgb(24, 79, 60));
            circle(c, 895, 447, 64, leaf);
            circle(c, 818, 460, 54, Color.rgb(31, 88, 64));

            // One curved street light.
            stroke.setColor(Color.rgb(95, 109, 117));
            stroke.setStrokeWidth(4);
            Path lamp = new Path();
            lamp.moveTo(315, 565);
            lamp.lineTo(315, 430);
            lamp.quadTo(318, 399, 350, 399);
            c.drawPath(lamp, stroke);
            circle(c, 354, 400, 7, WARM);
            circle(c, 354, 400, 18, Color.argb(35, 255, 196, 103));
        }

        private void drawSwipe(Canvas c) {
            final float left = 44;
            final float top = 642;
            final float right = 1036;
            final float bottom = 846;
            card(c, left, top, right, bottom, 32, SURFACE, Color.rgb(31, 95, 156));

            roundStroke(c, 68, 668, 1012, 820, 76, Color.rgb(27, 101, 181), 2);

            float centerX = 154 + swipeOffset;
            float centerY = 744;

            // Blue glow rings.
            circle(c, centerX, centerY, 82, Color.rgb(16, 56, 101));
            roundStroke(c, centerX - 82, centerY - 82, centerX + 82, centerY + 82, 82, BLUE_LIGHT, 4);
            drawDoorIcon(c, centerX, centerY, Color.WHITE);

            if (!opened) {
                drawArrow(c, 330, 744, Color.rgb(25, 91, 160));
                drawArrow(c, 360, 744, Color.rgb(30, 110, 198));
                drawArrow(c, 390, 744, BLUE);
                text(c, "Свайп, чтобы открыть дверь", 445, 735, 25, TEXT, true);
                text(c, "Потяните вправо", 445, 778, 20, MUTED, false);
            } else {
                text(c, "Дверь открыта", 445, 735, 27, GREEN, true);
                text(c, "Команда выполнена", 445, 778, 20, MUTED, false);
            }
        }

        private void drawSystemCard(Canvas c) {
            card(c, 44, 874, 1036, 1028, 30, SURFACE, BORDER);
            drawShield(c, 123, 950);
            text(c, "Система работает", 190, 948, 25, TEXT, true);
            text(c, "Все сервисы активны", 190, 985, 19, MUTED, false);

            stroke.setColor(Color.rgb(30, 61, 91));
            stroke.setStrokeWidth(2);
            c.drawLine(610, 900, 610, 1001, stroke);
            c.drawLine(825, 900, 825, 1001, stroke);

            drawBuildingIcon(c, 706, 934, MUTED);
            textCentered(c, "9 этажей", 706, 995, 17, MUTED, false);
            drawSmallDoor(c, 927, 934, MUTED);
            textCentered(c, "3 подъезда", 927, 995, 17, MUTED, false);
        }

        private void drawRecentEvents(Canvas c) {
            text(c, "Последние события", 48, 1092, 28, TEXT, true);
            text(c, "Все события", 848, 1091, 18, BLUE, false);
            drawChevronRight(c, 1008, 1084, BLUE);

            card(c, 44, 1120, 1036, 1644, 30, SURFACE, BORDER);
            drawEventRow(c, 1140, 1, "Дверь открыта", "Сегодня, 08:37", BLUE);
            drawEventRow(c, 1300, 2, "Вход по коду", "Сегодня, 08:31", GREEN);
            drawEventRow(c, 1460, 3, "Плановое обслуживание", "Инженерная служба • Вчера, 16:42", BLUE);
        }

        private void drawEventRow(Canvas c, float y, int type, String title, String subtitle, int accent) {
            roundFill(c, 74, y + 18, 150, y + 94, 20, Color.argb(55, Color.red(accent), Color.green(accent), Color.blue(accent)));
            roundStroke(c, 74, y + 18, 150, y + 94, 20, Color.argb(130, Color.red(accent), Color.green(accent), Color.blue(accent)), 2);

            if (type == 1) {
                drawSmallDoor(c, 112, y + 56, accent);
            } else if (type == 2) {
                drawPerson(c, 112, y + 56, accent);
            } else {
                drawGear(c, 112, y + 56, accent);
            }

            text(c, title, 184, y + 52, 22, TEXT, true);
            text(c, subtitle, 184, y + 87, 18, MUTED, false);
            drawChevronRight(c, 998, y + 55, MUTED);

            if (y < 1450) {
                stroke.setColor(Color.rgb(25, 55, 84));
                stroke.setStrokeWidth(2);
                c.drawLine(184, y + 128, 1008, y + 128, stroke);
            }
        }

        private void drawEventsScreen(Canvas c) {
            drawSimpleTitle(c, "События", "Журнал доступа и уведомлений");
            card(c, 44, 205, 1036, 860, 30, SURFACE, BORDER);
            drawEventRow(c, 230, 1, "Дверь открыта", "Сегодня, 08:37", BLUE);
            drawEventRow(c, 390, 2, "Вход по коду", "Сегодня, 08:31", GREEN);
            drawEventRow(c, 550, 3, "Плановое обслуживание", "Вчера, 16:42", BLUE);
            drawEventRow(c, 710, 1, "Дверь закрыта", "Вчера, 15:18", BLUE);
        }

        private void drawRequestsScreen(Canvas c) {
            drawSimpleTitle(c, "Заявки", "Обращения по дому");
            roundFill(c, 44, 205, 1036, 290, 28, Color.rgb(19, 71, 127));
            text(c, "+   Оставить заявку", 90, 260, 23, TEXT, true);

            text(c, "Мои заявки", 48, 355, 25, TEXT, true);
            drawRequest(c, 400, "Не работает освещение", "Подъезд • 2 этаж", "В работе", Color.rgb(233, 166, 62));
            drawRequest(c, 550, "Протекает кран в тамбуре", "Подъезд", "Новая", BLUE);
            drawRequest(c, 700, "Не закрывается дверь", "Вход", "Завершена", GREEN);
        }

        private void drawRequest(Canvas c, float y, String title, String subtitle, String status, int statusColor) {
            card(c, 44, y, 1036, y + 128, 26, SURFACE, BORDER);
            text(c, title, 82, y + 48, 21, TEXT, true);
            text(c, subtitle, 82, y + 83, 17, MUTED, false);
            roundFill(c, 820, y + 40, 988, y + 86, 20,
                    Color.argb(55, Color.red(statusColor), Color.green(statusColor), Color.blue(statusColor)));
            textCentered(c, status, 904, y + 70, 15, statusColor, true);
        }

        private void drawProfileScreen(Canvas c) {
            drawSimpleTitle(c, "Профиль", "Настройки доступа");
            circle(c, 150, 290, 66, Color.rgb(26, 71, 118));
            drawPerson(c, 150, 290, Color.WHITE);
            text(c, "Житель дома", 245, 278, 28, TEXT, true);
            text(c, "Болградская, 46", 245, 317, 19, MUTED, false);

            drawProfileLine(c, 420, "Уведомления");
            drawProfileLine(c, 535, "Безопасность");
            drawProfileLine(c, 650, "О приложении");
        }

        private void drawSimpleTitle(Canvas c, String title, String subtitle) {
            text(c, title, 54, 92, 42, TEXT, true);
            text(c, subtitle, 54, 140, 22, MUTED, false);
        }

        private void drawProfileLine(Canvas c, float y, String label) {
            card(c, 44, y, 1036, y + 92, 24, SURFACE, BORDER);
            text(c, label, 80, y + 58, 21, TEXT, true);
            drawChevronRight(c, 995, y + 46, MUTED);
        }

        private void drawBottomNavigation(Canvas c) {
            float top = virtualHeight - 170;
            roundFill(c, 28, top, 1052, virtualHeight - 15, 32, Color.rgb(6, 24, 42));
            roundStroke(c, 28, top, 1052, virtualHeight - 15, 32, Color.rgb(22, 54, 85), 2);

            String[] labels = {"Главная", "События", "Заявки", "Профиль"};
            for (int i = 0; i < 4; i++) {
                float cx = 135 + i * 270;
                int color = selectedTab == i ? BLUE : MUTED;
                if (i == 0) {
                    drawHomeIcon(c, cx, top + 58, color);
                } else if (i == 1) {
                    drawClock(c, cx, top + 58, color);
                } else if (i == 2) {
                    drawClipboard(c, cx, top + 58, color);
                } else {
                    drawPerson(c, cx, top + 58, color);
                }
                textCentered(c, labels[i], cx, top + 118, 16, color, selectedTab == i);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX() / scale;
            float y = event.getY() / scale;
            float navTop = virtualHeight - 185;

            if (event.getAction() == MotionEvent.ACTION_UP && y >= navTop) {
                selectedTab = Math.max(0, Math.min(3, (int) (x / 270f)));
                dragging = false;
                invalidate();
                return true;
            }

            if (selectedTab != 0 || opened) {
                return true;
            }

            final float knobCenterX = 154 + swipeOffset;
            final float knobCenterY = 744;
            final float maxOffset = 760;

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float dx = x - knobCenterX;
                float dy = y - knobCenterY;
                dragging = dx * dx + dy * dy <= 105 * 105 || (y > 665 && y < 820 && x < 330);
                if (dragging && swipeAnimator != null) {
                    swipeAnimator.cancel();
                }
                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE && dragging) {
                swipeOffset = Math.max(0, Math.min(maxOffset, x - 154));
                invalidate();
                return true;
            }

            if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && dragging) {
                dragging = false;
                if (swipeOffset >= maxOffset * 0.72f) {
                    opened = true;
                    animateSwipe(maxOffset, 170);
                    Toast.makeText(getContext(), "Дверь открыта (демо)", Toast.LENGTH_SHORT).show();
                    handler.postDelayed(() -> {
                        animateSwipe(0f, 300);
                        handler.postDelayed(() -> {
                            opened = false;
                            invalidate();
                        }, 320);
                    }, 1400);
                } else {
                    animateSwipe(0f, 220);
                }
                return true;
            }

            return true;
        }

        private void animateSwipe(float target, int duration) {
            if (swipeAnimator != null) {
                swipeAnimator.cancel();
            }
            swipeAnimator = ValueAnimator.ofFloat(swipeOffset, target);
            swipeAnimator.setDuration(duration);
            swipeAnimator.addUpdateListener(animation -> {
                swipeOffset = (float) animation.getAnimatedValue();
                invalidate();
            });
            swipeAnimator.start();
        }

        private void drawBell(Canvas c, float cx, float cy) {
            stroke.setColor(TEXT);
            stroke.setStrokeWidth(5);
            RectF r = new RectF(cx - 22, cy - 28, cx + 22, cy + 22);
            c.drawArc(r, 200, 140, false, stroke);
            c.drawLine(cx - 19, cy + 8, cx - 19, cy + 18, stroke);
            c.drawLine(cx + 19, cy + 8, cx + 19, cy + 18, stroke);
            c.drawLine(cx - 19, cy + 18, cx + 19, cy + 18, stroke);
            circle(c, cx, cy + 28, 5, TEXT);
        }

        private void drawDoorIcon(Canvas c, float cx, float cy, int color) {
            stroke.setColor(color);
            stroke.setStrokeWidth(5);
            c.drawRoundRect(new RectF(cx - 27, cy - 40, cx + 27, cy + 39), 7, 7, stroke);
            circle(c, cx + 13, cy, 4, color);
            c.drawLine(cx - 39, cy + 42, cx + 39, cy + 42, stroke);
        }

        private void drawSmallDoor(Canvas c, float cx, float cy, int color) {
            stroke.setColor(color);
            stroke.setStrokeWidth(4);
            c.drawRoundRect(new RectF(cx - 15, cy - 22, cx + 15, cy + 22), 3, 3, stroke);
            circle(c, cx + 7, cy, 2.5f, color);
        }

        private void drawShield(Canvas c, float cx, float cy) {
            Path p = new Path();
            p.moveTo(cx, cy - 44);
            p.lineTo(cx + 39, cy - 25);
            p.lineTo(cx + 31, cy + 26);
            p.quadTo(cx, cy + 51, cx, cy + 51);
            p.quadTo(cx - 31, cy + 26, cx - 31, cy + 26);
            p.lineTo(cx - 39, cy - 25);
            p.close();
            paint.setColor(Color.rgb(11, 61, 46));
            c.drawPath(p, paint);
            stroke.setColor(GREEN);
            stroke.setStrokeWidth(3);
            c.drawPath(p, stroke);
            stroke.setStrokeWidth(5);
            c.drawLine(cx - 15, cy + 1, cx - 3, cy + 13, stroke);
            c.drawLine(cx - 3, cy + 13, cx + 20, cy - 14, stroke);
        }

        private void drawBuildingIcon(Canvas c, float cx, float cy, int color) {
            stroke.setColor(color);
            stroke.setStrokeWidth(3);
            c.drawRect(cx - 20, cy - 28, cx + 20, cy + 24, stroke);
            for (int y = -16; y <= 2; y += 18) {
                for (int x = -10; x <= 10; x += 20) {
                    c.drawRect(cx + x - 4, cy + y - 4, cx + x + 4, cy + y + 4, stroke);
                }
            }
        }

        private void drawPerson(Canvas c, float cx, float cy, int color) {
            stroke.setColor(color);
            stroke.setStrokeWidth(4);
            c.drawCircle(cx, cy - 13, 10, stroke);
            c.drawRoundRect(new RectF(cx - 20, cy + 2, cx + 20, cy + 26), 11, 11, stroke);
        }

        private void drawGear(Canvas c, float cx, float cy, int color) {
            stroke.setColor(color);
            stroke.setStrokeWidth(4);
            c.drawCircle(cx, cy, 18, stroke);
            c.drawCircle(cx, cy, 7, stroke);
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4.0;
                float x1 = cx + (float) Math.cos(a) * 20;
                float y1 = cy + (float) Math.sin(a) * 20;
                float x2 = cx + (float) Math.cos(a) * 28;
                float y2 = cy + (float) Math.sin(a) * 28;
                c.drawLine(x1, y1, x2, y2, stroke);
            }
        }

        private void drawHomeIcon(Canvas c, float cx, float cy, int color) {
            stroke.setColor(color);
            stroke.setStrokeWidth(5);
            Path p = new Path();
            p.moveTo(cx - 25, cy - 1);
            p.lineTo(cx, cy - 25);
            p.lineTo(cx + 25, cy - 1);
            p.lineTo(cx + 25, cy + 25);
            p.lineTo(cx - 25, cy + 25);
            p.close();
            c.drawPath(p, stroke);
            c.drawRect(cx - 7, cy + 7, cx + 7, cy + 25, stroke);
        }

        private void drawClock(Canvas c, float cx, float cy, int color) {
            stroke.setColor(color);
            stroke.setStrokeWidth(4);
            c.drawCircle(cx, cy, 27, stroke);
            c.drawLine(cx, cy, cx, cy - 15, stroke);
            c.drawLine(cx, cy, cx + 14, cy + 8, stroke);
        }

        private void drawClipboard(Canvas c, float cx, float cy, int color) {
            stroke.setColor(color);
            stroke.setStrokeWidth(4);
            c.drawRoundRect(new RectF(cx - 22, cy - 27, cx + 22, cy + 29), 5, 5, stroke);
            c.drawRoundRect(new RectF(cx - 10, cy - 33, cx + 10, cy - 21), 4, 4, stroke);
            c.drawLine(cx - 11, cy - 5, cx + 11, cy - 5, stroke);
            c.drawLine(cx - 11, cy + 8, cx + 11, cy + 8, stroke);
        }

        private void drawChevronDown(Canvas c, float cx, float cy, int color) {
            stroke.setColor(color);
            stroke.setStrokeWidth(4);
            c.drawLine(cx - 10, cy - 5, cx, cy + 5, stroke);
            c.drawLine(cx, cy + 5, cx + 10, cy - 5, stroke);
        }

        private void drawChevronRight(Canvas c, float cx, float cy, int color) {
            stroke.setColor(color);
            stroke.setStrokeWidth(4);
            c.drawLine(cx - 5, cy - 10, cx + 5, cy, stroke);
            c.drawLine(cx + 5, cy, cx - 5, cy + 10, stroke);
        }

        private void drawArrow(Canvas c, float cx, float cy, int color) {
            stroke.setColor(color);
            stroke.setStrokeWidth(7);
            c.drawLine(cx - 10, cy - 21, cx + 11, cy, stroke);
            c.drawLine(cx + 11, cy, cx - 10, cy + 21, stroke);
        }

        private void card(Canvas c, float l, float t, float r, float b, float radius, int fill, int border) {
            roundFill(c, l, t, r, b, radius, fill);
            roundStroke(c, l, t, r, b, radius, border, 2);
        }

        private void roundFill(Canvas c, float l, float t, float r, float b, float radius, int color) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            c.drawRoundRect(new RectF(l, t, r, b), radius, radius, paint);
        }

        private void roundStroke(Canvas c, float l, float t, float r, float b, float radius, int color, float width) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setColor(color);
            stroke.setStrokeWidth(width);
            c.drawRoundRect(new RectF(l, t, r, b), radius, radius, stroke);
        }

        private void circle(Canvas c, float cx, float cy, float radius, int color) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            c.drawCircle(cx, cy, radius, paint);
        }

        private void text(Canvas c, String value, float x, float y, float size, int color, boolean bold) {
            paint.setShader(null);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            paint.setTextSize(size);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTypeface(android.graphics.Typeface.create("sans", bold ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL));
            c.drawText(value, x, y, paint);
        }

        private void textCentered(Canvas c, String value, float x, float y, float size, int color, boolean bold) {
            paint.setShader(null);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            paint.setTextSize(size);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(android.graphics.Typeface.create("sans", bold ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL));
            c.drawText(value, x, y, paint);
            paint.setTextAlign(Paint.Align.LEFT);
        }
    }
}
