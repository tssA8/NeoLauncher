package com.benq.ifp.neolauncher.menubar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.benq.ifp.neolauncher.R;


@SuppressLint("AppCompatCustomView")
public class CircleImageView extends ImageView {
    private static final String TAG = "CircleImageView";
    private Paint mPaint;

    private int mRadius;

    private float mScale;

    public CircleImageView(Context context) {
        super(context);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        mRadius = size / 2;

        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        mPaint = new Paint();
        Bitmap bitmap = drawableToBitmap(getDrawable());
        /**
         * This block of code handles the case where the bitmap is null.
         * Instead of crashing the application or leaving the CircleImageView blank,
         * it draws a default placeholder circle with a gray background.
         *
         * Steps:
         * 1. Log an error message indicating that the bitmap is null.
         * 2. Configure the Paint object with anti-aliasing enabled for smooth edges.
         * 3. Set the Paint color to a light gray (hex value: 0xFFCCCCCC).
         * 4. Use the Canvas object to draw a circle with the specified radius at the center.
         * 5. Return immediately to prevent further drawing operations, ensuring stability.
         *
         * This ensures that the CircleImageView is visually represented even when
         * the drawable resource fails to load or is not set.
         */

        if (bitmap == null) {
            Log.e(TAG, "onDraw, bitmap is null, using default image");
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.user_24);
        }
        else {
            BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

            mScale = (mRadius * 2.0f) / Math.min(bitmap.getHeight(), bitmap.getWidth());

            Matrix matrix = new Matrix();
            matrix.setScale(mScale, mScale);
            bitmapShader.setLocalMatrix(matrix);

            mPaint.setAntiAlias(true);
            mPaint.setShader(bitmapShader);

            canvas.drawCircle(mRadius, mRadius, mRadius, mPaint);
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            Log.e(TAG, "Drawable is null, cannot convert to Bitmap.");
            return null; // 返回 null，避免崩潰
        }
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) drawable;
            return bd.getBitmap();
        }
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }
}

