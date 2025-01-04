package com.example.taskifygo.util;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount;  // Number of columns
        private final int spacing;    // Spacing between items
        private final boolean includeEdge;  // Include edge spacing

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // left edge
                outRect.right = (column + 1) * spacing / spanCount; // right edge

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // bottom edge
            } else {
                outRect.left = column * spacing / spanCount; // left edge
                outRect.right = spacing - (column + 1) * spacing / spanCount; // right edge
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

