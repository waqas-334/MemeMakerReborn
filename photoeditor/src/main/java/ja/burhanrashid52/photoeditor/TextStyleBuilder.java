package ja.burhanrashid52.photoeditor;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * This class is used to wrap the styles to apply on the TextView on {@link PhotoEditor#addText(String, TextStyleBuilder)} and {@link PhotoEditor#editText(View, String, TextStyleBuilder)}
 * </p>
 *
 * @author <a href="https://github.com/Sulfkain">Christian Caballero</a>
 * @since 14/05/2019
 */
public class TextStyleBuilder {

    private Map<TextStyle, Object> values = new HashMap<>();

    protected Map<TextStyle, Object> getValues() {
        return values;
    }

    /**
     * Set this textSize style
     *
     * @param size Size to apply on text
     */
    public void withTextSize(@NonNull int size) {
        values.put(TextStyle.SIZE, size);
    }

    /**
     * Set this color style
     *
     * @param color Color to apply on text
     */
    public void withTextColor(@NonNull int color) {
        values.put(TextStyle.COLOR, color);
    }

    /**
     * Set this stroke color style
     *
     * @param color Color to apply to stroke
     */
    public void withStrokeColor(@NonNull int color) {
        values.put(TextStyle.STROKE_COLOR, color);
    }

    /**
     * Set this stroke width style
     *
     * @param width Width to apply to stroke
     */
    public void withStrokeWidth(@NonNull int width) {
        values.put(TextStyle.STROKE_WIDTH, width);
    }

    /**
     * Set this {@link Typeface} style
     *
     * @param textTypeface TypeFace to apply on text
     */
    public void withTextFont(@NonNull Typeface textTypeface) {
        values.put(TextStyle.FONT_FAMILY, textTypeface);
    }

    /**
     * Set this gravity style
     *
     * @param gravity Gravity style to apply on text
     */
    public void withGravity(@NonNull int gravity) {
        values.put(TextStyle.GRAVITY, gravity);
    }

    /**
     * Set this background color
     *
     * @param background Background color to apply on text, this method overrides the preview set on {@link TextStyleBuilder#withBackgroundDrawable(Drawable)}
     */
    public void withBackgroundColor(@NonNull int background) {
        values.put(TextStyle.BACKGROUND, background);
    }

    /**
     * Set this background {@link Drawable}, this method overrides the preview set on {@link TextStyleBuilder#withBackgroundColor(int)}
     *
     * @param bgDrawable Background drawable to apply on text
     */
    public void withBackgroundDrawable(@NonNull Drawable bgDrawable) {
        values.put(TextStyle.BACKGROUND, bgDrawable);
    }

    /**
     * Set this textAppearance style
     *
     * @param textAppearance Text style to apply on text
     */
    public void withTextAppearance(@NonNull int textAppearance) {
        values.put(TextStyle.TEXT_APPEARANCE, textAppearance);
    }

    /**
     * Set text MaxLines
     *
     * @param maxLines Text style to apply on text
     */
    public void withMaxLines(@NonNull int maxLines) {
        values.put(TextStyle.MAX_LINES, maxLines);
    }

    /**
     * Set text position
     *
     * @param positionX Text position X
     */
    public void withPositionX(@NonNull float positionX) {
        values.put(TextStyle.POSITION_X, positionX);
    }

    /**
     * Set text position
     *
     * @param positionY Text position Y
     */
    public void withPositionY(@NonNull float positionY) {
        values.put(TextStyle.POSITION_Y, positionY);
    }


    /**
     * Method to apply all the style setup on this Builder}
     *
     * @param textView TextView to apply the style
     */
    void applyStyle(@NonNull StrokeTextView textView) {
        for (Map.Entry<TextStyle, Object> entry : values.entrySet()) {
            switch (entry.getKey()) {
                case SIZE: {
                    final int size = (int) entry.getValue();
                    applyTextSize(textView, size);
                }
                break;

                case COLOR: {
                    final int color = (int) entry.getValue();
                    applyTextColor(textView, color);
                }
                break;
                case STROKE_COLOR: {
                    final int color = (int) entry.getValue();
                    applyStrokeColor(textView, color);
                }
                break;
                case STROKE_WIDTH: {
                    final int width = (int) entry.getValue();
                    applyStrokeWidth(textView, width);
                }
                break;
                case FONT_FAMILY: {
                    final Typeface typeface = (Typeface) entry.getValue();
                    applyFontFamily(textView, typeface);
                }
                break;

                case GRAVITY: {
                    final int gravity = (int) entry.getValue();
                    applyGravity(textView, gravity);
                }
                break;

                case BACKGROUND: {
                    if (entry.getValue() instanceof Drawable) {
                        final Drawable bg = (Drawable) entry.getValue();
                        applyBackgroundDrawable(textView, bg);

                    } else if (entry.getValue() instanceof Integer) {
                        final int color = (Integer) entry.getValue();
                        applyBackgroundColor(textView, color);
                    }
                }
                break;
                case TEXT_APPEARANCE: {
                    if (entry.getValue() instanceof Integer) {
                        final int styleAppearance = (Integer) entry.getValue();
                        applyTextAppearance(textView, styleAppearance);
                    }
                }
                break;
                case MAX_LINES: {
                    final int maxLines = (int) entry.getValue();
                    applyMaxLines(textView, maxLines);
                }
                break;
                case POSITION_X: {
                    final float positionX = (float) entry.getValue();
                    applyPositionX(textView, positionX);
                }
                break;
                case POSITION_Y: {
                    final float positionY = (float) entry.getValue();
                    applyPositionY(textView, positionY);
                }
                break;
            }
        }
    }

    protected void applyTextSize(StrokeTextView textView, int size) {
        textView.setTextSize(size);
    }

    protected void applyMaxLines(StrokeTextView textView, int maxLines) {
        textView.setMaxLines(maxLines);
        textView.setEllipsize(TextUtils.TruncateAt.END);
    }

    protected void applyTextColor(StrokeTextView textView, int color) {
        textView.setTextColor(color);
    }



    protected void applyStrokeColor(StrokeTextView textView, int color) {
        textView.setStrokeColor(color);
    }

    protected void applyStrokeWidth(StrokeTextView textView, int width) {
        textView.setStrokeWidth(width);
    }

    protected void applyFontFamily(StrokeTextView textView, Typeface typeface) {
        textView.setTypeface(typeface);
    }

    protected void applyGravity(StrokeTextView textView, int gravity) {
        textView.setGravity(gravity);
    }

    protected void applyBackgroundColor(StrokeTextView textView, int color) {
        textView.setBackgroundColor(color);
    }

    protected void applyPositionX(StrokeTextView textView, float positionX) {
        textView.setX(positionX);
    }

    protected void applyPositionY(StrokeTextView textView, float positionY) {
        textView.setY(positionY);
    }


    protected void applyBackgroundDrawable(StrokeTextView textView, Drawable bg) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            textView.setBackground(bg);
        } else {
            textView.setBackgroundDrawable(bg);
        }
    }

    protected void applyTextAppearance(StrokeTextView textView, int styleAppearance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.setTextAppearance(styleAppearance);
        } else {
            textView.setTextAppearance(textView.getContext(), styleAppearance);
        }
    }

    /**
     * Enum to maintain current supported style properties used on on {@link PhotoEditor#addText(String, TextStyleBuilder)} and {@link PhotoEditor#editText(View, String, TextStyleBuilder)}
     */
    protected enum TextStyle {
        SIZE("TextSize"),
        COLOR("TextColor"),
        STROKE_COLOR("StrokeColor"),
        STROKE_WIDTH("StrokeWidth"),
        GRAVITY("Gravity"),
        FONT_FAMILY("FontFamily"),
        BACKGROUND("Background"),
        MAX_LINES("MaxLines"),
        POSITION_X("PositionX"),
        POSITION_Y("PositionY"),
        TEXT_APPEARANCE("TextAppearance");

        TextStyle(String property) {
            this.property = property;
        }

        private String property;

        public String getProperty() {
            return property;
        }
    }
}