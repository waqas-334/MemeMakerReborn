package ja.burhanrashid52.photoeditor;


import android.content.Context;

class DpiHelper {

    static int spToPx(float sp, Context context) {
        return (int) (sp * context.getResources().getDisplayMetrics().scaledDensity);
    }

    static int pxToSp(float px, Context context) {
        return (int) (px / context.getResources().getDisplayMetrics().scaledDensity);
    }

    static int dpToPx(float dp, Context context) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    static int pxToDp(float px, Context context) {
        return (int) (px / context.getResources().getDisplayMetrics().density);
    }



}
