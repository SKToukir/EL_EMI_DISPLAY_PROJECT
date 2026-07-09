package com.walton.startupbroadcast.utilities;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import java.util.HashMap;

public class FontManager {
    private static final HashMap<String, Typeface> fontCache = new HashMap<>();

    public static Typeface getFont(Context context, String fontName) {
        Typeface typeface = fontCache.get(fontName);
        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.getAssets(), "font/" + fontName);
                fontCache.put(fontName, typeface);
            } catch (Exception e) {
                e.printStackTrace();
                return Typeface.DEFAULT;
            }
        }
        return typeface;
    }

    public static void applyFont(TextView view, Context context, String fontName) {
        view.setTypeface(getFont(context, fontName));
    }
}
