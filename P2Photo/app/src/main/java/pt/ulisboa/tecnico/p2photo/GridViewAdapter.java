package pt.ulisboa.tecnico.p2photo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class GridViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private ArrayList<Bitmap> data = new ArrayList();

    public GridViewAdapter(Context context, int layoutResourceId, ArrayList data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.image = (ImageView) row.findViewById(R.id.image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }


        Bitmap item = data.get(position);

        holder.image.setOnClickListener(arg0 -> {
            zoomImageFromThumb2(arg0, item);
        });

        holder.image.setScaleType(ImageView.ScaleType.FIT_XY);
        holder.image.setImageBitmap(item);
        return row;
    }

    static class ViewHolder {
        ImageView image;
    }

    private void zoomImageFromThumb2(final View thumbView, Bitmap imageResId) {
        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) ((Activity) context)
                .findViewById(R.id.expanded_image);
        expandedImageView.setImageBitmap(imageResId);

        expandedImageView.setVisibility(View.VISIBLE);

        expandedImageView.setOnClickListener(v -> {

            expandedImageView.setVisibility(View.INVISIBLE);
        });
    }


}