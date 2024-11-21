package com.example.salo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayOutputStream;

public class Camera extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private ImageView imageViewPhoto;
    private LinearLayout shapeSelectorLayout;
    private Button buttonSendPhoto;
    private Button buttonTakePhoto;

    private Bitmap photoBitmap;
    private Bitmap canvasBitmap;
    private Canvas canvas;
    private Paint paint;
    private Path path;
    private String currentShape = "Line"; // Default shape

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // UI Elements
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        shapeSelectorLayout = findViewById(R.id.shapeSelectorLayout);
        buttonSendPhoto = findViewById(R.id.buttonSendPhoto);
        buttonTakePhoto = findViewById(R.id.buttonTakePhoto);

        // Initialize Paint and Path
        paint = new Paint();
        paint.setColor(Color.RED); // Change this to the desired color
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8); // Thickness of the drawing

        path = new Path();
        // Take Photo Button
        buttonTakePhoto.setOnClickListener(v -> takePhoto());

        // Shape Selector Buttons
        findViewById(R.id.buttonCircle).setOnClickListener(v -> currentShape = "Circle");
        findViewById(R.id.buttonSquare).setOnClickListener(v -> currentShape = "Square");
        findViewById(R.id.buttonLine).setOnClickListener(v -> currentShape = "Line");

        // Send Photo Button
        buttonSendPhoto.setOnClickListener(v -> sendPhoto());

        // Set touch listener for drawing
        imageViewPhoto.setOnTouchListener((v, event) -> {
            if (photoBitmap != null) {
                handleTouch(event);
                return true;
            }
            return false;
        });
    }

    // Launch Camera to Take Photo
    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    // Handle the Photo Capture Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            photoBitmap = (Bitmap) extras.get("data");

            // Create a mutable bitmap for drawing
            canvasBitmap = Bitmap.createBitmap(photoBitmap.getWidth(), photoBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(canvasBitmap);
            canvas.drawBitmap(photoBitmap, 0, 0, null);

            imageViewPhoto.setImageBitmap(canvasBitmap);

            // Show shape selector and send button
            shapeSelectorLayout.setVisibility(View.VISIBLE);
            buttonSendPhoto.setVisibility(View.VISIBLE);
            imageViewPhoto.setVisibility(View.VISIBLE);
            buttonTakePhoto.setVisibility(View.GONE); // Hide take photo button
        }
    }

    // Handle touch events for drawing shapes
    private void handleTouch(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        // Log touch events
        Log.d("TouchEvent", "Action: " + event.getAction() + ", X: " + x + ", Y: " + y);

        // Map touch coordinates to bitmap
        float scaleX = (float) canvasBitmap.getWidth() / imageViewPhoto.getWidth();
        float scaleY = (float) canvasBitmap.getHeight() / imageViewPhoto.getHeight();

        x *= scaleX;
        y *= scaleY;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d("TouchEvent", "Action DOWN at: " + x + ", " + y);
                path.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("TouchEvent", "Action MOVE at: " + x + ", " + y);
                if ("Line".equals(currentShape)) {
                    path.lineTo(x, y);
                    canvas.drawPath(path, paint);
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.d("TouchEvent", "Action UP at: " + x + ", " + y);
                if ("Circle".equals(currentShape)) {
                    canvas.drawCircle(x, y, 50, paint);
                } else if ("Square".equals(currentShape)) {
                    canvas.drawRect(x - 50, y - 50, x + 50, y + 50, paint);
                }
                path.reset();
                break;
        }

        imageViewPhoto.invalidate(); // Refresh the view
    }


    // Send the photo to a server
    private void sendPhoto() {
        if (canvasBitmap == null) {
            Toast.makeText(this, "No photo to send", Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        canvasBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        // Use an HTTP library to send the photo
        Toast.makeText(this, "Photo sent to server!", Toast.LENGTH_SHORT).show();
    }
}