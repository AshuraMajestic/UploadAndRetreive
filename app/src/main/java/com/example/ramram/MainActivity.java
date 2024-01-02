package com.example.ramram;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.bson.Document;
import org.bson.types.Binary;

import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class MainActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextDescription;
    private ImageView imageView;
    String appId = "ashish_app-wqsph";
    private Uri selectedImageUri;
    User user;
    App app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextName = findViewById(R.id.editTextName);
        editTextDescription = findViewById(R.id.editTextDescription);
        imageView = findViewById(R.id.imageView);
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(appId).build());
    }

    public void selectImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    public void saveToMongoDB(View view) {
        Credentials credentials = Credentials.anonymous();
        user=app.currentUser();
        app.loginAsync(credentials, it -> {
            if (it.isSuccess()) {
                MongoClient mongoClient = user.getMongoClient("mongodb-atlas");
                MongoDatabase mongoDatabase = mongoClient.getDatabase("UserDetail");
                MongoCollection<Document> collection = mongoDatabase.getCollection("image");

                try (InputStream inputStream = getContentResolver().openInputStream(selectedImageUri)) {
                    if (inputStream != null) {
                        byte[] imageBytes = new byte[inputStream.available()];
                        inputStream.read(imageBytes);
                        showToast(imageBytes.toString());
                        Binary imageData = new Binary(imageBytes);

                        Document document = new Document("userid", user.getId())
                                .append("name", editTextName.getText().toString())
                                .append("description", editTextDescription.getText().toString())
                                .append("image", imageData);

                        collection.insertOne(document).getAsync(result -> {
                            if (result.isSuccess()) {
                                Log.d("AshuraDB", "User Created" + result);
                                Toast.makeText(getApplicationContext(), "User Created", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), hello.class);
                                startActivity(intent);
                            } else {
                                Log.d("AshuraDB", "User Failed to create:" + result.getError().toString());
                            }
                        });
                        Log.d("AshuraDB","data"+document.toString());
                        showToast("Data saved to MongoDB");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    showToast("Error reading image");
                }
            } else {
                showToast("Failed to authenticate with MongoDB Realm");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);
        }
    }

    private void showToast(String message) {
        Log.d("AshuraDB",message);
    }
    public void start(View view){
        Intent intent = new Intent(getApplicationContext(), hello.class);
        startActivity(intent);
    }

    private static final int PICK_IMAGE_REQUEST = 1;
}
