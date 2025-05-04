package com.example.digitalinkwrapper;
import android.content.Context;
import android.graphics.PointF;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.vision.digitalink.DigitalInkRecognition;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions;
import com.google.mlkit.vision.digitalink.Ink;
import com.google.mlkit.vision.digitalink.RecognitionResult;

import java.util.ArrayList;
import java.util.List;

public class DigitalInkHelper {
    private static final String TAG = "DigitalInkHelper";
    private static DigitalInkRecognizer recognizer;
    private final RemoteModelManager remoteModelManager;

    public interface Callback {
        void onSuccess(String result);
        void onFailure(Exception e);
    }

    public DigitalInkHelper(Context context) throws Exception {
        DigitalInkRecognitionModelIdentifier modelIdentifier =
                DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US");
        if (modelIdentifier == null) {
            throw new Exception("Model identifier is null");
        }
        DigitalInkRecognitionModel model =
                DigitalInkRecognitionModel.builder(modelIdentifier).build();
        recognizer = DigitalInkRecognition.getClient(
                DigitalInkRecognizerOptions.builder(model).build());
        remoteModelManager = RemoteModelManager.getInstance();

        remoteModelManager.download(model, new DownloadConditions.Builder().build())
                .addOnSuccessListener(unused -> Log.i(TAG, "Model downloaded"))
                .addOnFailureListener(e -> Log.e(TAG, "Model download failed", e));
    }

    public void recognizeInk(List<List<PointF>> strokes, Callback callback) {
        Ink.Builder inkBuilder = Ink.builder();

        long timestamp = System.currentTimeMillis();
        for (List<PointF> strokePoints : strokes) {
            Ink.Stroke.Builder strokeBuilder = Ink.Stroke.builder();
            for (PointF point : strokePoints) {
                strokeBuilder.addPoint(Ink.Point.create(point.x, point.y, timestamp));
                timestamp += 10;
            }
            inkBuilder.addStroke(strokeBuilder.build());
        }

        Ink ink = inkBuilder.build();

        recognizer.recognize(ink)
                .addOnSuccessListener(result -> {
                    if (!result.getCandidates().isEmpty()) {
                        callback.onSuccess(result.getCandidates().get(0).getText());
                    } else {
                        callback.onSuccess("");
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static void initialize(Context context) throws Exception {
        DigitalInkRecognitionModelIdentifier modelIdentifier =
                DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US");
        if (modelIdentifier == null) {
            throw new Exception("Model identifier is null");
        }
        DigitalInkRecognitionModel model =
                DigitalInkRecognitionModel.builder(modelIdentifier).build();
        recognizer = DigitalInkRecognition.getClient(
                DigitalInkRecognizerOptions.builder(model).build());
        RemoteModelManager.getInstance().download(model, new DownloadConditions.Builder().build())
                .addOnSuccessListener(unused -> Log.i("DigitalInkHelper", "Model downloaded"))
                .addOnFailureListener(e -> Log.e("DigitalInkHelper", "Model download failed", e));
    }

    public static String recognizeInkSync(List<List<PointF>> strokes) {
        Ink.Builder inkBuilder = Ink.builder();
        for (List<PointF> strokePoints : strokes) {
            Ink.Stroke.Builder strokeBuilder = Ink.Stroke.builder();
            for (PointF point : strokePoints) {
                strokeBuilder.addPoint(Ink.Point.create(point.x, point.y, System.currentTimeMillis()));
            }
            inkBuilder.addStroke(strokeBuilder.build());
        }

        final String[] resultText = new String[1];
        final Object lock = new Object();

        recognizer.recognize(inkBuilder.build())
                .addOnSuccessListener(result -> {
                    if (!result.getCandidates().isEmpty()) {
                        resultText[0] = result.getCandidates().get(0).getText();
                    } else {
                        resultText[0] = "";
                    }
                    synchronized (lock) {
                        lock.notify();
                    }
                })
                .addOnFailureListener(e -> {
                    resultText[0] = "ERROR: " + e.getMessage();
                    synchronized (lock) {
                        lock.notify();
                    }
                });

        try {
            synchronized (lock) {
                lock.wait(5000);
            }
        } catch (InterruptedException e) {
            return "ERROR: Interrupted";
        }

        return resultText[0];
    }
}