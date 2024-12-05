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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

        // Convert the bitmap to a JPEG byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        canvasBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        // Define the server URL
        String serverUrl = "http://192.168.0.3:8080"; // Replace with your server's IP and port

        // Use a background thread to send the photo
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                // Create a request body with raw image data
                RequestBody requestBody = RequestBody.create(byteArray, MediaType.parse("image/jpeg"));

                // Build the request
                Request request = new Request.Builder()
                        .url(serverUrl)
                        .post(requestBody) // Send raw data
                        .addHeader("Content-Type", "image/jpeg") // Ensure content type is set
                        .build();

                // Execute the request
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(this, "Photo sent successfully!", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to send photo: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}