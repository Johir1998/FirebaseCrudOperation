package com.example.firebasecrudoperation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button chooseImageButton, saveImageButton, displayImageButton;

    private ImageView imageView;

    private EditText imageNameEditText;

    private Uri imageUri;

    DatabaseReference databaseReference;

    StorageReference storageReference;

    StorageTask uploadTask;

    private static final int IMAGE_REQUEST = 1;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance().getReference("UPLOAD");

        storageReference = FirebaseStorage.getInstance().getReference("UPLOAD");


        chooseImageButton = findViewById(R.id.chooseImageButton_Id);
        saveImageButton = findViewById(R.id.saveImageButton_Id);
        displayImageButton = findViewById(R.id.displayImageButton_Id);

        imageView = findViewById(R.id.imageView_Id);

        imageNameEditText = findViewById(R.id.imageNameEditText_Id);

        chooseImageButton.setOnClickListener(this);
        saveImageButton.setOnClickListener(this);
        displayImageButton.setOnClickListener(this);
    }

    @Override

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.chooseImageButton_Id :

                openFileChooser();

                break;

            case R.id.saveImageButton_Id :

                if (uploadTask != null && uploadTask.isInProgress()) {

                    Toast.makeText(getApplicationContext(),"Upload is in progress.",Toast.LENGTH_LONG).show();
                } else {

                    saveData();
                }

                break;

            case R.id.displayImageButton_Id :

                Intent intent = new Intent(MainActivity.this, ImageActivity.class);
                startActivity(intent);

                break;
        }
    }

    //Getting the extension of an image.

    private void openFileChooser() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent,IMAGE_REQUEST);
    }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            saveImageButton.setVisibility(View.VISIBLE);

            imageUri = data.getData();
            Picasso.with(this).load(imageUri).into(imageView);
        }
    }

    public String getFileExtension(Uri imageUri) {

        ContentResolver contentResolver = getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }

    private void saveData() {

        final String imageName = imageNameEditText.getText().toString().trim();

        if (imageName.isEmpty()) {

            imageNameEditText.setError("Please enter the image name.");
            imageNameEditText.requestFocus();
            return;
        }

        StorageReference reference = storageReference.child(System.currentTimeMillis() + " . " + getFileExtension(imageUri));

        reference.putFile(imageUri)

                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content

                        Toast.makeText(getApplicationContext(),"Image is stored successfully.", Toast.LENGTH_LONG).show();

                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();

                        while (!urlTask.isSuccessful());

                        Uri downloadUrl = urlTask.getResult();

                        Upload upload = new Upload(imageName, downloadUrl.toString().trim());

                        String uploadId = databaseReference.push().getKey();

                        databaseReference.child(uploadId).setValue(upload);
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...

                        Toast.makeText(getApplicationContext(),"Image is not stored." + exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
