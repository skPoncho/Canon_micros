package com.example.proyecto1;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

public class CanvasFragment extends Fragment {
    double v_x, v_y, time;

    public CanvasFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_canvas, container, false);
        if (getArguments() != null) {
            v_x = getArguments().getDouble("v_x");
            v_y = getArguments().getDouble("v_y");
            time = getArguments().getDouble("t");
        }
        ((ImageView) view.findViewById(R.id.image)).setImageDrawable(new Grafica());
        return view;
    }

    private class Grafica extends Drawable {

        @Override
        public void draw(@NonNull Canvas canvas) {
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(10);
            canvas.drawLine(40,40, 40, 800, paint); // y axis
            canvas.drawLine(20,780, 1000, 780, paint); // x axis
            if (getArguments() == null) return;
            double px, py;
            double vel = Math.sqrt(Math.pow(v_x, 2) + Math.pow(v_y, 2));
            double r_max = Math.pow(vel, 2) / 9.81;
            double h_max = Math.pow(vel, 2) / (2 * 9.81);
            paint.setTextSize(40);
            paint.setStrokeWidth(2);
            canvas.drawText(String.format("%.3f m", r_max), 880, 820, paint);
            canvas.drawLine(940,40, 940, 800, paint);
            canvas.drawText(String.format("%.3f m", h_max), 50, 310, paint);
            canvas.drawLine(20,330, 1000, 330, paint);
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(10);
            double scale = 900 / r_max;
            for (int i = 0; i < 100; i++) {
                double t = time / 100 * i;
                px = v_x * t;
                py = v_y * t - 0.5 * 9.81 * Math.pow(t, 2);
                canvas.drawPoint((int) (40 + scale * px), (int) (780 - scale * py), paint);
            }
        }

        @Override
        public void setAlpha(int i) {

        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return PixelFormat.UNKNOWN;
        }
    }
}