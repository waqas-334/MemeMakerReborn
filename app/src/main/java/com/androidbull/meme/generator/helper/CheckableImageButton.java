package com.androidbull.meme.generator.helper;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Checkable;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityEventCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import com.androidbull.meme.generator.R;


public final class CheckableImageButton extends AppCompatImageButton implements Checkable
{
    private static final int[] DRAWABLE_STATE_CHECKED = new int[]{android.R.attr.state_checked};

    private boolean mChecked;

    public CheckableImageButton(Context context)
    {
        this(context, null);
    }

    public CheckableImageButton(Context context, AttributeSet attrs)
    {
        this(context, attrs, R.attr.imageButtonStyle);
    }

    public CheckableImageButton(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        ViewCompat.setAccessibilityDelegate(this, new AccessibilityDelegateCompat()
        {
            @Override
            public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event)
            {
                super.onInitializeAccessibilityEvent(host, event);
                event.setChecked(isChecked());
            }

            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info)
            {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setCheckable(true);
                info.setChecked(isChecked());
            }
        });
    }

    @Override
    public void setChecked(boolean checked)
    {
        if (mChecked != checked)
        {
            mChecked = checked;
            refreshDrawableState();
            sendAccessibilityEvent(AccessibilityEventCompat.TYPE_WINDOW_CONTENT_CHANGED);
        }
    }

    @Override
    public boolean isChecked()
    {
        return mChecked;
    }

    @Override
    public void toggle()
    {
        setChecked(!mChecked);
    }

    @Override
    public boolean performClick()
    {
        toggle();
        return super.performClick();
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace)
    {
        if (mChecked)
            return mergeDrawableStates(super.onCreateDrawableState(extraSpace + DRAWABLE_STATE_CHECKED.length), DRAWABLE_STATE_CHECKED);
        else
            return super.onCreateDrawableState(extraSpace);
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        CheckableImageButton.SavedState result = new CheckableImageButton.SavedState(super.onSaveInstanceState());
        result.checked = mChecked;
        return result;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!(state instanceof CheckableImageButton.SavedState))
        {
            super.onRestoreInstanceState(state);
            return;
        }

        CheckableImageButton.SavedState ss = (CheckableImageButton.SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        setChecked(ss.checked);
    }

    protected static class SavedState extends BaseSavedState
    {
        boolean checked;

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags)
        {
            super.writeToParcel(out, flags);
            out.writeInt(checked ? 1 : 0);
        }

        public static final Creator<CheckableImageButton.SavedState> CREATOR = new Creator<CheckableImageButton.SavedState>()
        {
            public CheckableImageButton.SavedState createFromParcel(Parcel in)
            {
                return new CheckableImageButton.SavedState(in);
            }

            public CheckableImageButton.SavedState[] newArray(int size)
            {
                return new CheckableImageButton.SavedState[size];
            }
        };

        private SavedState(Parcel in)
        {
            super(in);
            checked = in.readInt() == 1;
        }
    }
}