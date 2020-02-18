package com.lightricks.videotricks.util;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

/**
 * Static functions dealing with the view layout.
 */
public class LayoutUtils {

    /**
     * Apply the given dimension ratio to the specified view belonging to the provided constraint layout.
     * @param layout - constraint layout containing the specified view
     * @param viewId - resource ID of the view that needs to be adjusted
     * @param ratio - dimension ratio to apply to the view
     */
    public static void applyDimensionRatio(ConstraintLayout layout, @IdRes int viewId, @NonNull String ratio) {
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.setDimensionRatio(viewId, ratio);
        set.applyTo(layout);
    }

    /**
     * Retrieve the dimension ratio string from the given view. Note, that the view has to be
     * a direct child of ConstraintLayout.
     * @param view - view that is a direct child of ConstraintLayout
     * @return dimension ratio string
     */
    public static String getDimensionRatio(@NonNull View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params instanceof ConstraintLayout.LayoutParams) {
            ConstraintLayout.LayoutParams constraintParams = (ConstraintLayout.LayoutParams) params;
            return constraintParams.dimensionRatio;
        }

        throw new IllegalArgumentException("View is not a child of ConstraintLayout");
    }
}
