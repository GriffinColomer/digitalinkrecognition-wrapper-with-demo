package com.example.wrapper;

import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalinkwrapper.DigitalInkHelper;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private DrawingView drawingView;
    private TextView outputText;
    private DigitalInkHelper inkHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawingView = findViewById(R.id.drawingView);
        outputText = findViewById(R.id.outputText);
        Button recognizeBtn = findViewById(R.id.recognizeButton);
        Button clearBtn = findViewById(R.id.clearButton);

        try {
            inkHelper = new DigitalInkHelper(this);
        } catch (Exception e) {
            Log.e("MainActivity", "Failed to initialize ink helper", e);
        }

        recognizeBtn.setOnClickListener(v -> {
            List<List<PointF>> strokes = drawingView.getStrokes();
            if (inkHelper != null) {
                inkHelper.recognizeInk(strokes, new DigitalInkHelper.Callback() {
                    @Override
                    public void onSuccess(String result) {
                        runOnUiThread(() -> outputText.setText(result));
                    }

                    @Override
                    public void onFailure(Exception e) {
                        runOnUiThread(() -> outputText.setText("Error: " + e.getMessage()));
                    }
                });
            }
        });

        clearBtn.setOnClickListener(v -> {
            drawingView.clear();
            outputText.setText("");
        });
    }
}