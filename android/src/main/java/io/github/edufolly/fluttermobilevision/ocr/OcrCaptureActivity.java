package io.github.edufolly.fluttermobilevision.ocr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.util.Log;
import android.os.Handler;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.ArrayList;

import io.github.edufolly.fluttermobilevision.ui.CameraSource;
import io.github.edufolly.fluttermobilevision.util.AbstractCaptureActivity;
import io.github.edufolly.fluttermobilevision.util.MobileVisionException;

public final class OcrCaptureActivity extends AbstractCaptureActivity<OcrGraphic> {

    @SuppressLint("InlinedApi")
    protected void createCameraSource() throws MobileVisionException {
        Context context = getApplicationContext();

        TextRecognizer textRecognizer = new TextRecognizer.Builder(context)
                .build();

        OcrTrackerFactory ocrTrackerFactory = new OcrTrackerFactory(graphicOverlay, showText);

        textRecognizer.setProcessor(
                new MultiProcessor.Builder<>(ocrTrackerFactory).build());

        if (!textRecognizer.isOperational()) {
            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;

            if (hasLowStorage) {
                throw new MobileVisionException("Low Storage.");
            }
        }

        cameraSource = new CameraSource
                .Builder(getApplicationContext(), textRecognizer)
                .setFacing(camera)
                .setRequestedPreviewSize(previewWidth, previewHeight)
                .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null)
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .setRequestedFps(fps)
                .build();
    }

    protected boolean onTap(float rawX, float rawY) {
        if(!waitTap) {
            ArrayList<MyTextBlock> list = new ArrayList<>();
            for(int i = 0; i < 10; i++){
                Log.e("Testar", "" + i);
                for (OcrGraphic graphic : graphicOverlay.getGraphics()) {
                    list.add(new MyTextBlock(graphic.getTextBlock()));
                }
                String capturedText = "";
                for (int j=0; j < list.size(); j++){
                    capturedText += "\n" + (list.get(j)).getValue();
                }
                Log.e("Captured text",  capturedText) ;
            }
            String capturedText2 = "";
            for (int k=0; k < graphicOverlay.listAll.size(); k++){
                capturedText2 += "\n" + (graphicOverlay.listAll.get(k)).getValue() + (graphicOverlay.listAll.get(k)).getMap().top.;
            }
            Log.e("Captured text2", capturedText2);

            if (!list.isEmpty()) {
                Intent data = new Intent();
                data.putExtra(OBJECT, list);
                setResult(CommonStatusCodes.SUCCESS, data);
                finish();
                return true;
            }
            return false;
        }

        ArrayList<MyTextBlock> list = new ArrayList<>();

        if (multiple) {
            for (OcrGraphic graphic : graphicOverlay.getGraphics()) {
                list.add(new MyTextBlock(graphic.getTextBlock()));
            }
        } else {
            OcrGraphic graphic = graphicOverlay.getBest(rawX, rawY);
            if (graphic != null && graphic.getTextBlock() != null) {
                list.add(new MyTextBlock(graphic.getTextBlock()));
            }
        }

        if (!list.isEmpty()) {
            Intent data = new Intent();
            data.putExtra(OBJECT, list);
            setResult(CommonStatusCodes.SUCCESS, data);
            finish();
            return true;
        }

        return false;
    }
}