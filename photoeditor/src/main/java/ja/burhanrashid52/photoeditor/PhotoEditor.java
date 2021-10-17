package ja.burhanrashid52.photoeditor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.annotation.UiThread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static ja.burhanrashid52.photoeditor.ConstantsKt.CAPTION_BOTTOM_POSITION;
import static ja.burhanrashid52.photoeditor.ConstantsKt.CAPTION_DEFAULT_POSITION;
import static ja.burhanrashid52.photoeditor.ConstantsKt.CAPTION_TOP_POSITION;

/**
 * <p>
 * This class in initialize by {@link PhotoEditor.Builder} using a builder pattern with multiple
 * editing attributes
 * </p>
 *
 * @author <a href="https://github.com/burhanrashid52">Burhanuddin Rashid</a>
 * @version 0.1.1
 * @since 18/01/2017
 */
public class PhotoEditor implements BrushViewChangeListener {

    private static final String TAG = "PhotoEditor";
    private final LayoutInflater mLayoutInflater;
    private Context context;
    private PhotoEditorView parentView;
    private ImageView imageView;
    private View deleteView;
    private BrushDrawingView brushDrawingView;
    private List<View> addedViews;
    private List<View> redoViews;
    private OnPhotoEditorListener mOnPhotoEditorListener;
    private boolean isTextPinchZoomable;
    private Typeface mDefaultTextTypeface;
    private Typeface mDefaultEmojiTypeface;


    private PhotoEditor(Builder builder) {
        this.context = builder.context;
        this.parentView = builder.parentView;
        this.imageView = builder.imageView;
        this.deleteView = builder.deleteView;
        this.brushDrawingView = builder.brushDrawingView;
        this.isTextPinchZoomable = builder.isTextPinchZoomable;
        this.mDefaultTextTypeface = builder.textTypeface;
        this.mDefaultEmojiTypeface = builder.emojiTypeface;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        brushDrawingView.setBrushViewChangeListener(this);
        addedViews = new ArrayList<>();
        redoViews = new ArrayList<>();
    }

    /**
     * This will add image on {@link PhotoEditorView} which you drag,rotate and scale using pinch
     * if {@link PhotoEditor.Builder#setPinchTextScalable(boolean)} enabled
     *
     * @param desiredImage bitmap image you want to add
     */
    public void addImage(Bitmap desiredImage) {
        final View imageRootView = getLayout(ViewType.IMAGE);
        final ImageView imageView = imageRootView.findViewById(R.id.imgPhotoEditorImage);
        final FrameLayout frmBorder = imageRootView.findViewById(R.id.frmBorder);
        final ImageView imgClose = imageRootView.findViewById(R.id.imgPhotoEditorClose);

        imageView.setImageBitmap(desiredImage);

        MultiTouchListener multiTouchListener = getMultiTouchListener();
        multiTouchListener.setOnGestureControl(new MultiTouchListener.OnGestureControl() {
            @Override
            public void onClick() {
                boolean isBackgroundVisible = frmBorder.getTag() != null && (boolean) frmBorder.getTag();
                frmBorder.setBackgroundResource(isBackgroundVisible ? 0 : R.drawable.rounded_border_tv);
                imgClose.setVisibility(isBackgroundVisible ? View.GONE : View.VISIBLE);
                frmBorder.setTag(!isBackgroundVisible);
            }

            @Override
            public void onLongClick() {

            }
        });

        imageRootView.setOnTouchListener(multiTouchListener);

        addViewToParent(imageRootView, ViewType.IMAGE);

    }

    /**
     * This add the text on the {@link PhotoEditorView} with provided parameters
     * by default {@link TextView#setText(int)} will be 18sp
     *
     * @param text              text to display
     * @param colorCodeTextView text color to be displayed
     */
    @SuppressLint("ClickableViewAccessibility")
    public void addText(String text, final int colorCodeTextView) {
        addText(null, text, colorCodeTextView);
    }

    /**
     * This add the text on the {@link PhotoEditorView} with provided parameters
     * by default {@link TextView#setText(int)} will be 18sp
     *
     * @param textTypeface      typeface for custom font in the text
     * @param text              text to display
     * @param colorCodeTextView text color to be displayed
     */
    @SuppressLint("ClickableViewAccessibility")
    public void addText(@Nullable Typeface textTypeface, String text, final int colorCodeTextView) {
        final TextStyleBuilder styleBuilder = new TextStyleBuilder();

        styleBuilder.withTextColor(colorCodeTextView);
        if (textTypeface != null) {
            styleBuilder.withTextFont(textTypeface);
        }

        addText(text, styleBuilder);
    }

    /**
     * This add the text on the {@link PhotoEditorView} with provided parameters
     * by default {@link TextView#setText(int)} will be 18sp
     *
     * @param text         text to display
     * @param styleBuilder text style builder with your style
     */
    @SuppressLint("ClickableViewAccessibility")
    public void addText(String text, @Nullable TextStyleBuilder styleBuilder) {
        brushDrawingView.setBrushDrawingMode(false);
        final View textRootView = getLayout(ViewType.TEXT);
        final StrokeTextView textInputTv = textRootView.findViewById(R.id.tvPhotoEditorText);
        final ImageView imgClose = textRootView.findViewById(R.id.imgPhotoEditorClose);
        final FrameLayout frmBorder = textRootView.findViewById(R.id.frmBorder);


        textInputTv.setText(text);
        if (styleBuilder != null)
            styleBuilder.applyStyle(textInputTv);

        MultiTouchListener multiTouchListener = getMultiTouchListener();
        multiTouchListener.setOnGestureControl(new MultiTouchListener.OnGestureControl() {
            @Override
            public void onClick() {
               /* boolean isBackgroundVisible = frmBorder.getTag() != null && (boolean) frmBorder.getTag();
                frmBorder.setBackgroundResource(isBackgroundVisible ? 0 : R.drawable.rounded_border_tv);
                imgClose.setVisibility(isBackgroundVisible ? View.GONE : View.VISIBLE);
                frmBorder.setTag(!isBackgroundVisible);*/


                frmBorder.setBackgroundResource(R.drawable.rounded_border_tv);
                imgClose.setVisibility(View.VISIBLE);

                String textInput = textInputTv.getText().toString();
                int currentTextColor = textInputTv.getCurrentTextColor();
                if (mOnPhotoEditorListener != null) {
                    mOnPhotoEditorListener.onEditTextChangeListener(textRootView, textInputTv, currentTextColor);
                }
            }

            @Override
            public void onLongClick() {

            }
        });

        frmBorder.setBackgroundResource(0);
        imgClose.setVisibility(View.GONE);

        textRootView.setOnTouchListener(multiTouchListener);
        addViewToParent(textRootView, ViewType.TEXT);
    }

    /**
     * @param captionWrapper text with style
     */
    public void addText(CaptionWrapper captionWrapper) {
        brushDrawingView.setBrushDrawingMode(false);
        final View textRootView = getLayout(ViewType.TEXT);
        final StrokeTextView textInputTv = textRootView.findViewById(R.id.tvPhotoEditorText);
        final ImageView imgClose = textRootView.findViewById(R.id.imgPhotoEditorClose);
        final FrameLayout frmBorder = textRootView.findViewById(R.id.frmBorder);

        textInputTv.setText(captionWrapper.getText());

        if (captionWrapper.getFontSize() != null) {
            textInputTv.setTextSize(captionWrapper.getFontSize());
        }
        Log.d("PhotoEditor", "textInputTv: " + textInputTv.getTextSize());

        if (captionWrapper.getMaxLines() != null) {
            textInputTv.setMaxLines(captionWrapper.getMaxLines());
        }
        if (captionWrapper.getStrokeWidth() != null) {
            textInputTv.setStrokeWidth(captionWrapper.getStrokeWidth());
        }

        if (captionWrapper.getFontTypeFace() != null) {
            textInputTv.setTypeface(captionWrapper.getFontTypeFace());
        }

        textInputTv.setStrokeColor(Color.parseColor(captionWrapper.getStrokeColor()));
        textInputTv.setTextColor(Color.parseColor(captionWrapper.getTextColor()));

        textInputTv.setTag(captionWrapper);

        MultiTouchListener multiTouchListener = getMultiTouchListener();
        multiTouchListener.setOnGestureControl(new MultiTouchListener.OnGestureControl() {
            @Override
            public void onClick() {

                clearHelperBox();

                frmBorder.setBackgroundResource(R.drawable.rounded_border_tv);
                imgClose.setVisibility(View.VISIBLE);

                int currentTextColor = textInputTv.getCurrentTextColor();
                if (mOnPhotoEditorListener != null) {
                    mOnPhotoEditorListener.onEditTextChangeListener(textRootView, textInputTv, currentTextColor);
                }
            }

            @Override
            public void onLongClick() {

            }
        });

        frmBorder.setBackgroundResource(0);
        imgClose.setVisibility(View.GONE);

        textRootView.setOnTouchListener(multiTouchListener);
        addViewToParent(textRootView, ViewType.TEXT, captionWrapper.getPositionX(), captionWrapper.getPositionY());

        if (TextUtils.isEmpty(captionWrapper.getText())) {
            int currentTextColor = textInputTv.getCurrentTextColor();
            if (mOnPhotoEditorListener != null) {
                mOnPhotoEditorListener.onEditTextChangeListener(textRootView, textInputTv, currentTextColor);
            }
        }
    }

    /*
     */

    /**
     *
     *//*
    public void addNewText(CaptionWrapper captionWrapper) {
        brushDrawingView.setBrushDrawingMode(false);
        final View textRootView = getLayout(ViewType.TEXT);
        final StrokeTextView textInputTv = textRootView.findViewById(R.id.tvPhotoEditorText);
        final ImageView imgClose = textRootView.findViewById(R.id.imgPhotoEditorClose);
        final FrameLayout frmBorder = textRootView.findViewById(R.id.frmBorder);

        textInputTv.setText(captionWrapper.getText());

        if (captionWrapper.getFontSize() != null) {
            textInputTv.setTextSize(captionWrapper.getFontSize());
        }
        if (captionWrapper.getMaxLines() != null) {
            textInputTv.setMaxLines(captionWrapper.getMaxLines());
        }
        if (captionWrapper.getStrokeWidth() != null) {
            textInputTv.setStrokeWidth(captionWrapper.getStrokeWidth());
        }

        if (captionWrapper.getFontTypeFace() != null) {
            textInputTv.setTypeface(captionWrapper.getFontTypeFace());
        }

        textInputTv.setStrokeColor(Color.parseColor(captionWrapper.getStrokeColor()));
        textInputTv.setTextColor(Color.parseColor(captionWrapper.getTextColor()));

        textInputTv.setTag(captionWrapper);

        MultiTouchListener multiTouchListener = getMultiTouchListener();
        multiTouchListener.setOnGestureControl(new MultiTouchListener.OnGestureControl() {
            @Override
            public void onClick() {

                clearHelperBox();

                frmBorder.setBackgroundResource(R.drawable.rounded_border_tv);
                imgClose.setVisibility(View.VISIBLE);

                int currentTextColor = textInputTv.getCurrentTextColor();
                if (mOnPhotoEditorListener != null) {
                    mOnPhotoEditorListener.onEditTextChangeListener(textRootView, textInputTv, currentTextColor);
                }
            }

            @Override
            public void onLongClick() {

            }
        });

        frmBorder.setBackgroundResource(0);
        imgClose.setVisibility(View.GONE);

        textRootView.setOnTouchListener(multiTouchListener);
        addViewToParent(textRootView, ViewType.TEXT, captionWrapper.getPositionX(), captionWrapper.getPositionY());


    }*/
    public void removeView(View viewBeingEdited) {
        if (viewBeingEdited != null) {
            Object tag = viewBeingEdited.getTag();
            if (tag != null) {
                ViewType viewType = (ViewType) tag;
                viewUndo(viewBeingEdited, viewType);
            }
        }
    }

    public void removeViewWithoutHistory(View viewBeingEdited) {
        if (viewBeingEdited != null) {
            Object tag = viewBeingEdited.getTag();
            if (tag != null) {
                ViewType viewType = (ViewType) tag;
                viewUndoWithoutHistory(viewBeingEdited, viewType);
            }
        }
    }

    /**
     * This add the text on the {@link PhotoEditorView} with provided parameters
     * by default {@link TextView#setText(int)} will be 18sp
     *
     * @param text text to display
     */
    @SuppressLint("ClickableViewAccessibility")
    public void addText(String text) {
        brushDrawingView.setBrushDrawingMode(false);
        final View textRootView = getLayout(ViewType.TEXT);
        final StrokeTextView textInputTv = textRootView.findViewById(R.id.tvPhotoEditorText);
        final ImageView imgClose = textRootView.findViewById(R.id.imgPhotoEditorClose);
        final FrameLayout frmBorder = textRootView.findViewById(R.id.frmBorder);

        textInputTv.setText(text);

        MultiTouchListener multiTouchListener = getMultiTouchListener();
        multiTouchListener.setOnGestureControl(new MultiTouchListener.OnGestureControl() {
            @Override
            public void onClick() {
                boolean isBackgroundVisible = frmBorder.getTag() != null && (boolean) frmBorder.getTag();
                frmBorder.setBackgroundResource(isBackgroundVisible ? 0 : R.drawable.rounded_border_tv);
                imgClose.setVisibility(isBackgroundVisible ? View.GONE : View.VISIBLE);
                frmBorder.setTag(!isBackgroundVisible);
            }

            @Override
            public void onLongClick() {
                String textInput = textInputTv.getText().toString();
                int currentTextColor = textInputTv.getCurrentTextColor();
                if (mOnPhotoEditorListener != null) {
                    mOnPhotoEditorListener.onEditTextChangeListener(textRootView, textInputTv, currentTextColor);
                }
            }
        });

        textRootView.setOnTouchListener(multiTouchListener);
        addViewToParent(textRootView, ViewType.TEXT);
    }


    /**
     * This will update text and color on provided view
     *
     * @param view      view on which you want update
     * @param inputText text to update {@link TextView}
     * @param colorCode color to update on {@link TextView}
     */
    public void editText(@NonNull View view, String inputText, @NonNull int colorCode) {
        editText(view, null, inputText, colorCode);
    }

    /**
     * This will update the text and color on provided view
     *
     * @param view         root view where text view is a child
     * @param textTypeface update typeface for custom font in the text
     * @param inputText    text to update {@link TextView}
     * @param colorCode    color to update on {@link TextView}
     */
    public void editText(@NonNull View view, @Nullable Typeface textTypeface, String inputText, @NonNull int colorCode) {
        final TextStyleBuilder styleBuilder = new TextStyleBuilder();
        styleBuilder.withTextColor(colorCode);
        if (textTypeface != null) {
            styleBuilder.withTextFont(textTypeface);
        }

        editText(view, inputText, styleBuilder);
    }

    /**
     * This will update the text and color on provided view
     *
     * @param view         root view where text view is a child
     * @param inputText    text to update {@link TextView}
     * @param styleBuilder style to apply on {@link TextView}
     */
    public void editText(@NonNull View view, String inputText, @Nullable TextStyleBuilder styleBuilder) {
        StrokeTextView inputTextView = view.findViewById(R.id.tvPhotoEditorText);
        if (inputTextView != null && addedViews.contains(view) && !TextUtils.isEmpty(inputText)) {
            inputTextView.setText(inputText);
            if (styleBuilder != null)
                styleBuilder.applyStyle(inputTextView);

            parentView.updateViewLayout(view, view.getLayoutParams());
            int i = addedViews.indexOf(view);
            if (i > -1) addedViews.set(i, view);
        }
    }

    /**
     * This will update the text and color on provided view
     *
     * @param view           root view where text view is a child
     * @param inputText      text to update {@link TextView}
     * @param captionWrapper style to apply on {@link CaptionWrapper}
     */
    public void editText(@NonNull View view, String inputText, CaptionWrapper captionWrapper) {
        StrokeTextView inputTextView = view.findViewById(R.id.tvPhotoEditorText);
        if (inputTextView != null && addedViews.contains(view)) {

            inputTextView.setText(inputText);

            if (captionWrapper.getFontSize() != null) {
                inputTextView.setTextSize(captionWrapper.getFontSize());
            }
            if (captionWrapper.getMaxLines() != null) {
                inputTextView.setMaxLines(captionWrapper.getMaxLines());
            }
            if (captionWrapper.getStrokeWidth() != null) {
                inputTextView.setStrokeWidth(captionWrapper.getStrokeWidth());
            }

            if (captionWrapper.getFontTypeFace() != null) {
                inputTextView.setTypeface(captionWrapper.getFontTypeFace());
            }

            inputTextView.setStrokeColor(Color.parseColor(captionWrapper.getStrokeColor()));
            inputTextView.setTextColor(Color.parseColor(captionWrapper.getTextColor()));
            inputTextView.setTag(captionWrapper);


            parentView.updateViewLayout(view, view.getLayoutParams());
            int i = addedViews.indexOf(view);
            if (i > -1) addedViews.set(i, view);
        }
    }


    /**
     * Adds emoji to the {@link PhotoEditorView} which you drag,rotate and scale using pinch
     * if {@link PhotoEditor.Builder#setPinchTextScalable(boolean)} enabled
     *
     * @param emojiName unicode in form of string to display emoji
     */
    public void addEmoji(String emojiName) {
        addEmoji(null, emojiName);
    }

    /**
     * Adds emoji to the {@link PhotoEditorView} which you drag,rotate and scale using pinch
     * if {@link PhotoEditor.Builder#setPinchTextScalable(boolean)} enabled
     *
     * @param emojiTypeface typeface for custom font to show emoji unicode in specific font
     * @param emojiName     unicode in form of string to display emoji
     */
    public void addEmoji(Typeface emojiTypeface, String emojiName) {
        brushDrawingView.setBrushDrawingMode(false);
        final View emojiRootView = getLayout(ViewType.EMOJI);
        final StrokeTextView emojiTextView = emojiRootView.findViewById(R.id.tvPhotoEditorText);
        final FrameLayout frmBorder = emojiRootView.findViewById(R.id.frmBorder);
        final ImageView imgClose = emojiRootView.findViewById(R.id.imgPhotoEditorClose);

        if (emojiTypeface != null) {
            emojiTextView.setTypeface(emojiTypeface);
        }
        emojiTextView.setTextSize(56);
        emojiTextView.setText(emojiName);
        MultiTouchListener multiTouchListener = getMultiTouchListener();
        multiTouchListener.setOnGestureControl(new MultiTouchListener.OnGestureControl() {
            @Override
            public void onClick() {
                boolean isBackgroundVisible = frmBorder.getTag() != null && (boolean) frmBorder.getTag();
                frmBorder.setBackgroundResource(isBackgroundVisible ? 0 : R.drawable.rounded_border_tv);
                imgClose.setVisibility(isBackgroundVisible ? View.GONE : View.VISIBLE);
                frmBorder.setTag(!isBackgroundVisible);
            }

            @Override
            public void onLongClick() {
            }
        });
        emojiRootView.setOnTouchListener(multiTouchListener);
        addViewToParent(emojiRootView, ViewType.EMOJI);
    }

    /**
     * Add to root view from image,emoji and text to our parent view
     *
     * @param rootView rootview of image,text and emoji
     */
    private void addViewToParent(View rootView, ViewType viewType, float positionX, float positionY) {

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (positionY == CAPTION_TOP_POSITION) {
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            params.addRule(RelativeLayout.ALIGN_TOP, parentView.getSource().getId());
        } else if (positionY == CAPTION_BOTTOM_POSITION) {
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            params.addRule(RelativeLayout.ALIGN_BOTTOM, parentView.getSource().getId());
        } else if (positionY == CAPTION_DEFAULT_POSITION) {
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
        } else {

            /*// calculate positions according to scale
            // TODO drawable  null check
            float originalWidth = parentView.getSource().getDrawable().getIntrinsicWidth();
            float originalHeight = parentView.getSource().getDrawable().getIntrinsicHeight();
            float scaledWidth = parentView.getSource().getWidth();
            float scaledHeight = parentView.getSource().getHeight();
            float widthScaleDifference = (originalWidth / scaledWidth);
            float heightScaleDifference = (originalHeight / scaledHeight);

            // no need because already directly adding to correct view location
//            int[] imageViewLocation = new int[2];
//            parentView.getSource().getLocationOnScreen(imageViewLocation);

            // widthScaleDifference is in position X,Y should also be converted to device pixels first
            int marginStart = Math.round(*//*imageViewLocation[0] +*//* (context.getResources().getDisplayMetrics().density * positionX) / widthScaleDifference);
            int marginTop = Math.round(*//*imageViewLocation[1] +  *//*(context.getResources().getDisplayMetrics().density * positionY) / heightScaleDifference);

            // -12dp because of frmBorder margin
            // -8dp because of tvPhotoEditorText margin
            int marginStartWithFramePadding = Math.round((marginStart - DpiHelper.dpToPx(12, context)));
            int marginTopWithFramePadding = Math.round((marginTop - DpiHelper.dpToPx(8, context)));
*/
            params.leftMargin = (int) positionX;
            params.topMargin = (int) positionY;
        }

        parentView.addView(rootView, params);
        addedViews.add(rootView);
        if (mOnPhotoEditorListener != null)
            mOnPhotoEditorListener.onAddViewListener(rootView, viewType, addedViews.size());
    }


    /**
     * Add to root view from image,emoji and text to our parent view
     *
     * @param rootView rootview of image,text and emoji
     */
    private void addViewToParent(View rootView, ViewType viewType) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        parentView.addView(rootView, params);
        addedViews.add(rootView);
        if (mOnPhotoEditorListener != null)
            mOnPhotoEditorListener.onAddViewListener(rootView, viewType, addedViews.size());
    }

    /**
     * Create a new instance and scalable touchview
     *
     * @return scalable multitouch listener
     */
    @NonNull
    private MultiTouchListener getMultiTouchListener() {
        MultiTouchListener multiTouchListener = new MultiTouchListener(
                deleteView,
                parentView,
                this.imageView,
                isTextPinchZoomable,
                mOnPhotoEditorListener);

        //multiTouchListener.setOnMultiTouchListener(this);

        return multiTouchListener;
    }

    /**
     * Get root view by its type i.e image,text and emoji
     *
     * @param viewType image,text or emoji
     * @return rootview
     */
    private View getLayout(final ViewType viewType) {
        View rootView = null;
        switch (viewType) {
            case TEXT:
                rootView = mLayoutInflater.inflate(R.layout.view_photo_editor_text, null);
                StrokeTextView txtText = rootView.findViewById(R.id.tvPhotoEditorText);
                if (txtText != null && mDefaultTextTypeface != null) {
                    txtText.setGravity(Gravity.CENTER);
                    if (mDefaultEmojiTypeface != null) {
                        txtText.setTypeface(mDefaultTextTypeface);
                    }
                }
                break;
            case IMAGE:
                rootView = mLayoutInflater.inflate(R.layout.view_photo_editor_image, null);
                break;
            case EMOJI:
                rootView = mLayoutInflater.inflate(R.layout.view_photo_editor_text, null);
                StrokeTextView txtTextEmoji = rootView.findViewById(R.id.tvPhotoEditorText);
                if (txtTextEmoji != null) {
                    if (mDefaultEmojiTypeface != null) {
                        txtTextEmoji.setTypeface(mDefaultEmojiTypeface);
                    }
                    txtTextEmoji.setGravity(Gravity.CENTER);
                    txtTextEmoji.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
                break;
        }

        if (rootView != null) {
            //We are setting tag as ViewType to identify what type of the view it is
            //when we remove the view from stack i.e onRemoveViewListener(ViewType viewType, int numberOfAddedViews);
            rootView.setTag(viewType);
            final ImageView imgClose = rootView.findViewById(R.id.imgPhotoEditorClose);
            final View finalRootView = rootView;
            if (imgClose != null) {
                imgClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PhotoEditor.this.viewUndo(finalRootView, viewType);
                    }
                });
            }
        }
        rootView.setTag(viewType);
        return rootView;
    }

    /**
     * Enable/Disable drawing mode to draw on {@link PhotoEditorView}
     *
     * @param brushDrawingMode true if mode is enabled
     */
    public void setBrushDrawingMode(boolean brushDrawingMode) {
        if (brushDrawingView != null)
            brushDrawingView.setBrushDrawingMode(brushDrawingMode);
    }

    /**
     * @return true is brush mode is enabled
     */
    public Boolean getBrushDrawableMode() {
        return brushDrawingView != null && brushDrawingView.getBrushDrawingMode();
    }

    /**
     * set the size of bursh user want to paint on canvas i.e {@link BrushDrawingView}
     *
     * @param size size of brush
     */
    public void setBrushSize(float size) {
        if (brushDrawingView != null)
            brushDrawingView.setBrushSize(size);
    }

    /**
     * set opacity/transparency of brush while painting on {@link BrushDrawingView}
     *
     * @param opacity opacity is in form of percentage
     */
    public void setOpacity(@IntRange(from = 0, to = 100) int opacity) {
        if (brushDrawingView != null) {
            opacity = (int) ((opacity / 100.0) * 255.0);
            brushDrawingView.setOpacity(opacity);
        }
    }

    /**
     * set brush color which user want to paint
     *
     * @param color color value for paint
     */
    public void setBrushColor(@ColorInt int color) {
        if (brushDrawingView != null)
            brushDrawingView.setBrushColor(color);
    }

    /**
     * set the eraser size
     * <br></br>
     * <b>Note :</b> Eraser size is different from the normal brush size
     *
     * @param brushEraserSize size of eraser
     */
    public void setBrushEraserSize(float brushEraserSize) {
        if (brushDrawingView != null)
            brushDrawingView.setBrushEraserSize(brushEraserSize);
    }

    void setBrushEraserColor(@ColorInt int color) {
        if (brushDrawingView != null)
            brushDrawingView.setBrushEraserColor(color);
    }

    /**
     * @return provide the size of eraser
     * @see PhotoEditor#setBrushEraserSize(float)
     */
    public float getEraserSize() {
        return brushDrawingView != null ? brushDrawingView.getEraserSize() : 0;
    }

    /**
     * @return provide the size of eraser
     * @see PhotoEditor#setBrushSize(float)
     */
    public float getBrushSize() {
        if (brushDrawingView != null)
            return brushDrawingView.getBrushSize();
        return 0;
    }

    /**
     * @return provide the size of eraser
     * @see PhotoEditor#setBrushColor(int)
     */
    public int getBrushColor() {
        if (brushDrawingView != null)
            return brushDrawingView.getBrushColor();
        return 0;
    }

    /**
     * <p>
     * Its enables eraser mode after that whenever user drags on screen this will erase the existing
     * paint
     * <br>
     * <b>Note</b> : This eraser will work on paint views only
     * <p>
     */
    public void brushEraser() {
        if (brushDrawingView != null)
            brushDrawingView.brushEraser();
    }

    /*private void viewUndo() {
        if (addedViews.size() > 0) {
            parentView.removeView(addedViews.remove(addedViews.size() - 1));
            if (mOnPhotoEditorListener != null)
                mOnPhotoEditorListener.mOnPhotoEditorListener(addedViews.size());
        }
    }*/

    private void viewUndo(View removedView, ViewType viewType) {
        clearHelperBox();
        if (addedViews.size() > 0) {
            if (addedViews.contains(removedView)) {
                parentView.removeView(removedView);
                addedViews.remove(removedView);
                redoViews.add(removedView);
                if (mOnPhotoEditorListener != null) {
                    mOnPhotoEditorListener.onRemoveViewListener(removedView, viewType, addedViews.size());
                }
            }
        }
        Log.d("PhotoEditor", "viewUndo: " + addedViews.size());
    }

    private void viewUndoWithoutHistory(View removedView, ViewType viewType) {
        clearHelperBox();
        if (addedViews.size() > 0) {
            if (addedViews.contains(removedView)) {
                parentView.removeView(removedView);
                addedViews.remove(removedView);
                if (mOnPhotoEditorListener != null) {
                    mOnPhotoEditorListener.onRemoveViewListener(removedView, viewType, addedViews.size());
                }
            }
        }
        Log.d("PhotoEditor", "viewUndo: " + addedViews.size());
    }

    /**
     * Undo the last operation perform on the {@link PhotoEditor}
     *
     * @return true if there nothing more to undo
     */
    public boolean undo() {
        clearHelperBox();
        if (addedViews.size() > 0) {
            View removeView = addedViews.get(addedViews.size() - 1);
            if (removeView instanceof BrushDrawingView) {
                return brushDrawingView != null && brushDrawingView.undo();
            } else {
                addedViews.remove(addedViews.size() - 1);
                parentView.removeView(removeView);
                redoViews.add(removeView);
            }
            if (mOnPhotoEditorListener != null) {
                Object viewTag = removeView.getTag();
                if (viewTag != null && viewTag instanceof ViewType) {
                    mOnPhotoEditorListener.onRemoveViewListener(removeView, ((ViewType) viewTag), addedViews.size());
                }
            }

        }
        Log.d("PhotoEditor", "undo: " + addedViews.size());
        return addedViews.size() != 0;
    }

    /**
     * Redo the last operation perform on the {@link PhotoEditor}
     *
     * @return true if there nothing more to redo
     */
    public boolean redo() {
        clearHelperBox();
        if (redoViews.size() > 0) {
            View redoView = redoViews.get(redoViews.size() - 1);
            if (redoView instanceof BrushDrawingView) {
                return brushDrawingView != null && brushDrawingView.redo();
            } else {
                redoViews.remove(redoViews.size() - 1);
                parentView.addView(redoView);
                addedViews.add(redoView);
            }
            Object viewTag = redoView.getTag();
            if (mOnPhotoEditorListener != null && viewTag != null && viewTag instanceof ViewType) {
                mOnPhotoEditorListener.onAddViewListener(redoView, ((ViewType) viewTag), addedViews.size());
            }
        }
        Log.d("PhotoEditor", "redo: " + addedViews.size());

        return redoViews.size() != 0;
    }


    /**
     * Returns total number of views added. (for undo)
     */
    public int getAddedViewsCount() {
        return addedViews.size();
    }

    /**
     * Returns total number of views added. (for redo)
     */
    public int getRedoViewsCount() {
        return redoViews.size();
    }

    /**
     * Returns rotated bitmap
     */
    @Nullable
    public Bitmap getClockWiseRotatedImage(Bitmap bitmap) {
//        if (parentView.getSource() != null && (parentView.getSource().getDrawable() != null)) {
//            Bitmap bitmap = ((BitmapDrawable) parentView.getSource().getDrawable()).getBitmap();
            return BitmapUtil.rotate(90f, bitmap);

//            parentView.getSource().setPivotX( parentView.getSource().getWidth() / 2f);
//            parentView.getSource().setPivotY( parentView.getSource().getHeight() / 2f);
//            parentView.getSource().setRotation(parentView.getSource().getRotation() + 90f);
//        }
//        return null;
    }

    /**
     * Rotates Image to Left
     */
    public void rotateImageCounterClockWise() {
        if (parentView.getSource() != null && (parentView.getSource().getDrawable() != null)) {
            Bitmap bitmap = ((BitmapDrawable) parentView.getSource().getDrawable()).getBitmap();
            parentView.getSource().setImageBitmap(BitmapUtil.rotate(-90f, bitmap));
        }
    }

    /**
     * Flips Image vertically
     */
    public void flipVertical() {
        if (parentView.getSource() != null && (parentView.getSource().getDrawable() != null)) {
            Bitmap bitmap = ((BitmapDrawable) parentView.getSource().getDrawable()).getBitmap();
            parentView.getSource().setImageBitmap(BitmapUtil.flipVertical(bitmap));
        }
    }

    /**
     * Flips Image horizontally
     */
    public void flipHorizontal() {
        if (parentView.getSource() != null && (parentView.getSource().getDrawable() != null)) {
            Bitmap bitmap = ((BitmapDrawable) parentView.getSource().getDrawable()).getBitmap();
            parentView.getSource().setImageBitmap(BitmapUtil.flipHorizontal(bitmap));
        }
    }


    private void clearBrushAllViews() {
        if (brushDrawingView != null)
            brushDrawingView.clearAll();
    }

    /**
     * Removes all the edited operations performed {@link PhotoEditorView}
     * This will also clear the undo and redo stack
     */
    public void clearAllViews() {
        for (int i = 0; i < addedViews.size(); i++) {
            parentView.removeView(addedViews.get(i));
        }
        if (addedViews.contains(brushDrawingView)) {
            parentView.addView(brushDrawingView);
        }
        addedViews.clear();
        redoViews.clear();
        clearBrushAllViews();
    }

    /**
     * Remove all helper boxes from views
     */
    @UiThread
    public void clearHelperBox() {
        for (int i = 0; i < parentView.getChildCount(); i++) {
            View childAt = parentView.getChildAt(i);
            FrameLayout frmBorder = childAt.findViewById(R.id.frmBorder);
            if (frmBorder != null) {
                frmBorder.setBackgroundResource(0);
            }
            ImageView imgClose = childAt.findViewById(R.id.imgPhotoEditorClose);
            if (imgClose != null) {
                imgClose.setVisibility(View.GONE);
            }
        }
    }

 /*   public void hideCaptionFrame() {

        for (View view : addedViews) {
            FrameLayout frmBorder = view.findViewById(R.id.frmBorder);
            ImageView imgClose = view.findViewById(R.id.imgPhotoEditorClose);
            if (imgClose != null) {
                imgClose.setVisibility(View.GONE);
            }
            if (frmBorder != null) {
                frmBorder.setBackgroundResource(0);
            }
        }
    }*/

    /**
     * Setup of custom effect using effect type and set parameters values
     *
     * @param customEffect {@link CustomEffect.Builder#setParameter(String, Object)}
     */
    public void setFilterEffect(CustomEffect customEffect) {
        parentView.setFilterEffect(customEffect);
    }

    /**
     * Set pre-define filter available
     *
     * @param filterType type of filter want to apply {@link PhotoEditor}
     */
    public void setFilterEffect(PhotoFilter filterType) {
        parentView.setFilterEffect(filterType);
    }


    /**
     * A callback to save the edited image asynchronously
     */
    public interface OnSaveListener {

        /**
         * Call when edited image is saved successfully on given path
         *
         * @param imagePath path on which image is saved
         */
        void onSuccess(@NonNull String imagePath);

        /**
         * Call when failed to saved image on given path
         *
         * @param exception exception thrown while saving image
         */
        void onFailure(@NonNull Exception exception);
    }

    /**
     * Save the edited image on given path
     *
     * @param imagePath      path on which image to be saved
     * @param onSaveListener callback for saving image
     * @see OnSaveListener
     */
    @RequiresPermission(allOf = {Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void saveAsFile(@NonNull final String imagePath, @NonNull final OnSaveListener onSaveListener) {
        saveAsFile(imagePath, new SaveSettings.Builder().build(), onSaveListener);
    }

    /**
     * Save the edited image on given path
     *
     * @param imagePath      path on which image to be saved
     * @param onSaveListener callback for saving image
     * @see OnSaveListener
     */
    @SuppressLint("StaticFieldLeak")
    @RequiresPermission(allOf = {Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void saveFileTemporarily(@NonNull final String imagePath,
                                    @NonNull final OnSaveListener onSaveListener) {
        Log.d(TAG, "Image Path: " + imagePath);
        parentView.saveFilter(new OnSaveBitmap() {
            @Override
            public void onBitmapReady(Bitmap saveBitmap) {
                new AsyncTask<String, String, Exception>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        clearHelperBox();
                        parentView.getSource().setDrawingCacheEnabled(false);
                    }

                    @SuppressLint("MissingPermission")
                    @Override
                    protected Exception doInBackground(String... strings) {
                        // Create a media file name
                        File file = new File(imagePath);
                        try {
                            FileOutputStream out = new FileOutputStream(file, false);
                            if (parentView != null) {
                                parentView.getSource().setDrawingCacheEnabled(true);
                                Bitmap drawingCache = parentView.getSource().getDrawingCache();
                                drawingCache.compress(Bitmap.CompressFormat.PNG, 100, out);
                            }
                            out.flush();
                            out.close();
                            Log.d(TAG, "Filed Saved Successfully");
                            return null;
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d(TAG, "Failed to save File");
                            return e;
                        }
                    }

                    @Override
                    protected void onPostExecute(Exception e) {
                        super.onPostExecute(e);
                        if (e == null) {
                            onSaveListener.onSuccess(imagePath);
                        } else {
                            onSaveListener.onFailure(e);
                        }
                    }

                }.execute();
            }

            @Override
            public void onFailure(Exception e) {
                onSaveListener.onFailure(e);
            }
        });
    }


    /**
     * Save the edited image on given path
     *
     * @param imagePath      path on which image to be saved
     * @param saveSettings   builder for multiple save options {@link SaveSettings}
     * @param onSaveListener callback for saving image
     * @see OnSaveListener
     */
    @SuppressLint("StaticFieldLeak")
    @RequiresPermission(allOf = {Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void saveAsFile(@NonNull final String imagePath,
                           @NonNull final SaveSettings saveSettings,
                           @NonNull final OnSaveListener onSaveListener) {
        Log.d(TAG, "Image Path: " + imagePath);
        parentView.saveFilter(new OnSaveBitmap() {
            @Override
            public void onBitmapReady(Bitmap saveBitmap) {
                new AsyncTask<String, String, Exception>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        clearHelperBox();
                        parentView.setDrawingCacheEnabled(false);
                    }

                    @SuppressLint("MissingPermission")
                    @Override
                    protected Exception doInBackground(String... strings) {
                        // Create a media file name
                        File file = new File(imagePath);
                        try {
                            FileOutputStream out = new FileOutputStream(file, false);
                            if (parentView != null) {
                                parentView.setDrawingCacheEnabled(true);
                                Bitmap drawingCache = saveSettings.isTransparencyEnabled()
                                        ? BitmapUtil.removeTransparency(parentView.getDrawingCache())
                                        : parentView.getDrawingCache();
                                drawingCache.compress(saveSettings.getCompressFormat(), saveSettings.getCompressQuality(), out);
                            }
                            out.flush();
                            out.close();
                            Log.d(TAG, "Filed Saved Successfully");
                            return null;
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d(TAG, "Failed to save File");
                            return e;
                        }
                    }

                    @Override
                    protected void onPostExecute(Exception e) {
                        super.onPostExecute(e);
                        if (e == null) {
                            //Clear all views if its enabled in save settings
                            if (saveSettings.isClearViewsEnabled()) clearAllViews();
                            onSaveListener.onSuccess(imagePath);
                        } else {
                            onSaveListener.onFailure(e);
                        }
                    }

                }.execute();
            }

            @Override
            public void onFailure(Exception e) {
                onSaveListener.onFailure(e);
            }
        });
    }

    /**
     * Save the edited image on given path
     *
     * @param saveSettings   builder for multiple save options {@link SaveSettings}
     * @param onSaveListener callback for saving image
     * @see OnSaveListener
     */
    @SuppressLint("StaticFieldLeak")
    @RequiresPermission(allOf = {Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void saveAsFileNew(
            @NonNull final SaveSettings saveSettings,
            @NonNull final OnSaveListener onSaveListener) {

        parentView.saveFilter(new OnSaveBitmap() {
            @Override
            public void onBitmapReady(Bitmap saveBitmap) {
                new AsyncTask<String, String, Exception>() {

                    String imagePath = "";

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        clearHelperBox();
                        parentView.setDrawingCacheEnabled(false);
                    }

                    @SuppressLint("MissingPermission")
                    @Override
                    protected Exception doInBackground(String... strings) {

                        try {

                            if (parentView != null) {
                                parentView.setDrawingCacheEnabled(true);
                                Bitmap drawingCache = saveSettings.isTransparencyEnabled()
                                        ? BitmapUtil.removeTransparency(parentView.getDrawingCache())
                                        : parentView.getDrawingCache();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                                    File dirDest = new File(Environment.DIRECTORY_PICTURES, "MemeMaker");

                                    long date = System.currentTimeMillis();
                                    String extension = ".png";

                                    ContentValues newImage = new ContentValues();
                                    newImage.put(MediaStore.Images.Media.DISPLAY_NAME, date + extension);
                                    newImage.put(MediaStore.MediaColumns.MIME_TYPE, "image/" + extension);
                                    newImage.put(MediaStore.MediaColumns.DATE_ADDED, date);
                                    newImage.put(MediaStore.MediaColumns.DATE_MODIFIED, date);
                                    newImage.put(MediaStore.MediaColumns.SIZE, drawingCache.getByteCount());
                                    newImage.put(MediaStore.MediaColumns.WIDTH, drawingCache.getWidth());
                                    newImage.put(MediaStore.MediaColumns.HEIGHT, drawingCache.getHeight());
                                    newImage.put(MediaStore.MediaColumns.RELATIVE_PATH, dirDest + File.separator);
                                    newImage.put(MediaStore.Images.Media.IS_PENDING, 1);

                                    Uri newImageUri = context.getContentResolver().insert(collection, newImage);
                                    OutputStream out = context.getContentResolver().openOutputStream(newImageUri, "w");
                                    drawingCache.compress(saveSettings.getCompressFormat(), saveSettings.getCompressQuality(), out);

                                    newImage.clear();
                                    newImage.put(MediaStore.Images.Media.IS_PENDING, 0);
                                    context.getContentResolver().update(newImageUri, newImage, null, null);

                                    imagePath = FileUtils.getPath(context, newImageUri);

                                } else if (isExternalStorageWriteable()) {

                                    String externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                                    File dir = new File(externalDir, "MemeMaker");
                                    if (!dir.exists()) {
                                        dir.mkdirs();
                                    }
                                    long date = System.currentTimeMillis();
                                    String extension = ".png";

                                    File file = new File(dir, date + extension);
                                    FileOutputStream out = new FileOutputStream(file, false);
                                    drawingCache.compress(saveSettings.getCompressFormat(), saveSettings.getCompressQuality(), out);

                                    //Scan Broadcast
                                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                    intent.setData(Uri.fromFile(file));
                                    context.sendBroadcast(intent);

                                    imagePath = file.getPath();
                                }
                            }
                            return null;
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d(TAG, "Failed to save File");
                            return e;
                        }
                    }

                    @Override
                    protected void onPostExecute(Exception e) {
                        super.onPostExecute(e);
                        if (e == null) {
                            //Clear all views if its enabled in save settings
                            if (saveSettings.isClearViewsEnabled()) clearAllViews();
                            onSaveListener.onSuccess(imagePath);
                        } else {
                            onSaveListener.onFailure(e);
                        }
                    }

                }.execute();
            }

            @Override
            public void onFailure(Exception e) {
                onSaveListener.onFailure(e);
            }
        });
    }


    public boolean isExternalStorageWriteable() {
        return Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED);
    }

    /**
     * Save the edited image on given path
     *
     * @param imagePath      path on which image to be saved
     * @param onSaveListener callback for saving image
     * @see OnSaveListener
     */
    @SuppressLint("StaticFieldLeak")
    public void saveAsTemplate(@NonNull final String imagePath,
                               @NonNull final OnSaveListener onSaveListener) {
        Log.d(TAG, "Image Path: " + imagePath);
        new AsyncTask<String, String, Exception>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                clearHelperBox();
                parentView.setDrawingCacheEnabled(false);
            }

            @Override
            protected Exception doInBackground(String... strings) {
                // Create a media file name
                File file = new File(imagePath);
                try {
                    FileOutputStream out = new FileOutputStream(file, false);
                    if (parentView != null && parentView.getSource().getDrawable() != null) {
                        parentView.getSource().setDrawingCacheEnabled(true);
                        Bitmap drawingCache = parentView.getSource().getDrawingCache();
                        drawingCache.compress(Bitmap.CompressFormat.PNG, 100, out);
                    }
                    out.flush();
                    out.close();
                    Log.d(TAG, "Filed Saved Successfully");
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "Failed to save File");
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Exception e) {
                super.onPostExecute(e);
                if (e == null) {
                    onSaveListener.onSuccess(imagePath);
                } else {
                    onSaveListener.onFailure(e);
                }
            }

        }.execute();
    }



    /*  *//**
     * Save the edited image on given path
     *
     * @param destinationFilePath path on which image to be saved
     * @param onSaveListener      callback for saving image
     * @see OnSaveListener
     *//*
    @SuppressLint("StaticFieldLeak")
    @RequiresPermission(allOf = {Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void saveAsTemplate(@NonNull final String destinationFilePath,
                               @NonNull final Bitmap sourceBitmap,
                               @NonNull final OnSaveListener onSaveListener) {
        Log.d(TAG, "Image Path: " + destinationFilePath);
        new AsyncTask<String, String, Exception>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                clearHelperBox();
                parentView.setDrawingCacheEnabled(false);
            }

            @SuppressLint("MissingPermission")
            @Override
            protected Exception doInBackground(String... strings) {
                // Create a media file name
                File file = new File(destinationFilePath);
                try {

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    sourceBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos); // YOU can also save it in JPEG
                    byte[] bitmapData = bos.toByteArray();

                    FileOutputStream out = new FileOutputStream(file, false);
                    out.write(bitmapData);
                    out.flush();
                    out.close();
                    Log.d(TAG, "Filed Saved Successfully");
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "Failed to save File");
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Exception e) {
                super.onPostExecute(e);
                if (e == null) {
                    onSaveListener.onSuccess(destinationFilePath);
                } else {
                    onSaveListener.onFailure(e);
                }
            }

        }.execute();
    }*/


    /**
     * Save the edited image as bitmap
     *
     * @param onSaveBitmap callback for saving image as bitmap
     * @see OnSaveBitmap
     */
    @SuppressLint("StaticFieldLeak")
    public void saveAsBitmap(@NonNull final OnSaveBitmap onSaveBitmap) {
        saveAsBitmap(new SaveSettings.Builder().build(), onSaveBitmap);
    }

    /**
     * Save the edited image as bitmap
     *
     * @param saveSettings builder for multiple save options {@link SaveSettings}
     * @param onSaveBitmap callback for saving image as bitmap
     * @see OnSaveBitmap
     */
    @SuppressLint("StaticFieldLeak")
    public void saveAsBitmap(@NonNull final SaveSettings saveSettings,
                             @NonNull final OnSaveBitmap onSaveBitmap) {
        parentView.saveFilter(new OnSaveBitmap() {
            @Override
            public void onBitmapReady(Bitmap saveBitmap) {
                new AsyncTask<String, String, Bitmap>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        clearHelperBox();
                        parentView.setDrawingCacheEnabled(false);
                    }

                    @Override
                    protected Bitmap doInBackground(String... strings) {
                        if (parentView != null) {
                            parentView.setDrawingCacheEnabled(true);
                            return saveSettings.isTransparencyEnabled() ?
                                    BitmapUtil.removeTransparency(parentView.getDrawingCache())
                                    : parentView.getDrawingCache();
                        } else {
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        super.onPostExecute(bitmap);
                        if (bitmap != null) {
                            if (saveSettings.isClearViewsEnabled()) clearAllViews();
                            onSaveBitmap.onBitmapReady(bitmap);
                        } else {
                            onSaveBitmap.onFailure(new Exception("Failed to load the bitmap"));
                        }
                    }

                }.execute();
            }

            @Override
            public void onFailure(Exception e) {
                onSaveBitmap.onFailure(e);
            }
        });
    }

    private static String convertEmoji(String emoji) {
        String returnedEmoji;
        try {
            int convertEmojiToInt = Integer.parseInt(emoji.substring(2), 16);
            returnedEmoji = new String(Character.toChars(convertEmojiToInt));
        } catch (NumberFormatException e) {
            returnedEmoji = "";
        }
        return returnedEmoji;
    }

    /**
     * Callback on editing operation perform on {@link PhotoEditorView}
     *
     * @param onPhotoEditorListener {@link OnPhotoEditorListener}
     */
    public void setOnPhotoEditorListener(@NonNull OnPhotoEditorListener onPhotoEditorListener) {
        this.mOnPhotoEditorListener = onPhotoEditorListener;
    }

    /**
     * Check if any changes made need to save
     *
     * @return true if nothing is there to change
     */
    public boolean isCacheEmpty() {
        return addedViews.size() == 0 && redoViews.size() == 0;
    }


    @Override
    public void onViewAdd(BrushDrawingView brushDrawingView) {
        if (redoViews.size() > 0) {
            redoViews.remove(redoViews.size() - 1);
        }
        addedViews.add(brushDrawingView);
        if (mOnPhotoEditorListener != null) {
            mOnPhotoEditorListener.onAddViewListener(brushDrawingView, ViewType.BRUSH_DRAWING, addedViews.size());
        }
    }

    @Override
    public void onViewRemoved(BrushDrawingView brushDrawingView) {
      /*  if (addedViews.size() > 0) {
            View removeView = addedViews.remove(addedViews.size() - 1);
            if (!(removeView instanceof BrushDrawingView)) {
                parentView.removeView(removeView);
            }
            redoViews.add(removeView);
        }
        if (mOnPhotoEditorListener != null) {
            mOnPhotoEditorListener.onRemoveViewListener(ViewType.BRUSH_DRAWING, addedViews.size());
        }*/
    }

    @Override
    public void onStartDrawing() {
        if (mOnPhotoEditorListener != null) {
            mOnPhotoEditorListener.onStartViewChangeListener(ViewType.BRUSH_DRAWING);
        }
    }

    @Override
    public void onStopDrawing() {
        if (mOnPhotoEditorListener != null) {
            mOnPhotoEditorListener.onStopViewChangeListener(ViewType.BRUSH_DRAWING);
        }
    }


    /**
     * Builder pattern to define {@link PhotoEditor} Instance
     */
    public static class Builder {

        private Context context;
        private PhotoEditorView parentView;
        private ImageView imageView;
        private View deleteView;
        private BrushDrawingView brushDrawingView;
        private Typeface textTypeface;
        private Typeface emojiTypeface;
        //By Default pinch zoom on text is enabled
        private boolean isTextPinchZoomable = true;

        /**
         * Building a PhotoEditor which requires a Context and PhotoEditorView
         * which we have setup in our xml layout
         *
         * @param context         context
         * @param photoEditorView {@link PhotoEditorView}
         */
        public Builder(Context context, PhotoEditorView photoEditorView) {
            this.context = context;
            parentView = photoEditorView;
            imageView = photoEditorView.getSource();
            brushDrawingView = photoEditorView.getBrushDrawingView();
        }

        Builder setDeleteView(View deleteView) {
            this.deleteView = deleteView;
            return this;
        }

        /**
         * set default text font to be added on image
         *
         * @param textTypeface typeface for custom font
         * @return {@link Builder} instant to build {@link PhotoEditor}
         */
        public Builder setDefaultTextTypeface(Typeface textTypeface) {
            this.textTypeface = textTypeface;
            return this;
        }

        /**
         * set default font specific to add emojis
         *
         * @param emojiTypeface typeface for custom font
         * @return {@link Builder} instant to build {@link PhotoEditor}
         */
        public Builder setDefaultEmojiTypeface(Typeface emojiTypeface) {
            this.emojiTypeface = emojiTypeface;
            return this;
        }

        /**
         * set false to disable pinch to zoom on text insertion.By deafult its true
         *
         * @param isTextPinchZoomable flag to make pinch to zoom
         * @return {@link Builder} instant to build {@link PhotoEditor}
         */
        public Builder setPinchTextScalable(boolean isTextPinchZoomable) {
            this.isTextPinchZoomable = isTextPinchZoomable;
            return this;
        }

        /**
         * @return build PhotoEditor instance
         */
        public PhotoEditor build() {
            return new PhotoEditor(this);
        }
    }

    /**
     * Provide the list of emoji in form of unicode string
     *
     * @param context context
     * @return list of emoji unicode
     */
    public static ArrayList<String> getEmojis(Context context) {
        ArrayList<String> convertedEmojiList = new ArrayList<>();
        String[] emojiList = context.getResources().getStringArray(R.array.photo_editor_emoji);
        for (String emojiUnicode : emojiList) {
            convertedEmojiList.add(convertEmoji(emojiUnicode));
        }
        return convertedEmojiList;
    }
}
