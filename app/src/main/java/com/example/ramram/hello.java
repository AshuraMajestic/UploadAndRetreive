package com.example.ramram;

import android.os.Bundle;
import android.util.Base64;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;


import java.nio.charset.StandardCharsets;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class hello extends AppCompatActivity {

    private ImageView imageView;
    private User user;
    private App app;
    private static final String APP_ID = "ashish_app-wqsph";
    private static final String COLLECTION_NAME = "image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);
        imageView = findViewById(R.id.imageView2);
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(APP_ID).build());
        loadFromMongoDB();
    }

    public void loadFromMongoDB() {

        user = app.currentUser();
        app.loginAsync(Credentials.anonymous(), it -> {
            if (it.isSuccess()) {
                MongoClient mongoClient = user.getMongoClient("mongodb-atlas");
                MongoDatabase mongoDatabase = mongoClient.getDatabase("UserDetail");
                MongoCollection<Document> collection = mongoDatabase.getCollection(COLLECTION_NAME);
                Document doc = new Document().append("userid", "6593c32a0d5c2186682567cd");
                collection.findOne(doc).getAsync(result -> {
                    if (result.isSuccess()) {
                        Document found = result.get();
                        if (found != null) {
                            try {
                                String json = found.toJson();
                                JSONObject jsonObject = new JSONObject(json);
                                try {
                                    String base64BinaryData = jsonObject.getString("image");
                                    byte[] imageBytes = base64BinaryData.getBytes(StandardCharsets.UTF_8);
                                    showToast(imageBytes.toString());
                                    setImageToImageView(imageBytes);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    showToast("Base errror");
                                }

                            } catch (Exception e) {
                                Log.e("AshuraDB", "Error converting document to JSON", e);
                                Toast.makeText(getApplicationContext(), "Error converting document to JSON", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "User Not Found", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Error: " + result.getError().toString(), Toast.LENGTH_LONG).show();
                        Log.d("AshuraDB", "Result" + result.getError().toString());
                    }
                });
            }
        });
    }
    private void showToast(String message) {
        Log.d("AshuraDB", message);
    }

//    Error code here
    private void setImageToImageView(byte[] imageBytes) {
        if (imageBytes != null && imageBytes.length > 0) {

            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                // Handle the case when decoding fails
                Log.d("AshuraDB", "Bitmap is null");
            }
        } else {
            // Handle the case when imageBytes is null or empty
            Log.d("AshuraDB", "Invalid image data");
        }
    }
}
