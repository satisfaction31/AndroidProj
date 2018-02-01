package sampayan.edward.testtrasearch; /**
 * Created by Edward on 24/01/2018.
 */

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.client.Firebase;
import android.support.design.widget.FloatingActionButton;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * Created by Administrator on 16-03-2017.
 */

public class PostPicture extends AppCompatActivity {

    Button upload_photo, post_photo;
    ImageView user_image;
    TextView title,desc;
    public static final int READ_EXTERNAL_STORAGE = 0;
    private static final int GALLERY_INTENT = 2, REQUEST_CAMERA = 1;
    private ProgressDialog mProgressDialog;
    private Firebase mRoofRef;
    private Firebase mRoofRef2;
    private Uri mImageUri = null;
    private DatabaseReference mdatabaseRef;
    private StorageReference mStorage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_picture);

        Firebase.setAndroidContext(this);


        upload_photo = (Button)findViewById(R.id.bttnupload);
        post_photo = (Button)findViewById(R.id.bttnpost);
        user_image = (ImageView) findViewById(R.id.placepic);
        title = (EditText) findViewById(R.id.imgname);
        desc = (EditText) findViewById(R.id.imgcaption);


        //Initialize the Progress Bar
        mProgressDialog = new ProgressDialog(PostPicture.this);


        //Select image from External Storage...
        upload_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //Check for Runtime Permission
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(getApplicationContext(), "Call for Permission", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
                    }
                }
                else
                {
                    selectImage();
                }
            }
        });

        //Initialize Firebase Database paths for database and Storage

        mdatabaseRef = FirebaseDatabase.getInstance().getReference();
        mRoofRef = new Firebase("https://dashboard-c094a.firebaseio.com/").child("User_Details").push();  // Push will create new child every time we upload data
        mStorage = FirebaseStorage.getInstance().getReferenceFromUrl("gs://dashboard-c094a.appspot.com/");


        //Click on Post Button Title will upload to Database
        post_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String mName = title.getText().toString().trim();
                final String mDesc = desc.getText().toString().trim();

                if(mName.isEmpty()&& mDesc.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Fill all Field", Toast.LENGTH_SHORT).show();
                    return;
                }
                Firebase childRef_name = mRoofRef.child("Image_Title");
                childRef_name.setValue(mName);
                Firebase childRef_name2 = mRoofRef.child("Image_Desc");
                childRef_name2.setValue(mDesc);


                Toast.makeText(getApplicationContext(), "Updated Info", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(PostPicture.this, MainActivity.class));

            }
        });

    }



    //Check for Runtime Permissions for Storage Access
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    selectImage();
                return;
        }
        Toast.makeText(getApplicationContext(), "...", Toast.LENGTH_SHORT).show();
    }


    //If Access Granted gallery Will open
    private void selectImage() {

            final CharSequence[] items = {"Camera", "Gallery", "Cancel"};

            AlertDialog.Builder builder = new AlertDialog.Builder(PostPicture.this);
            builder.setTitle("Add Image");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    if(items[i].equals("Camera")){
//                        Intent intent1 = new Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                    Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if(intent1.resolveActivity(getPackageManager())!=null){
                            startActivityForResult(intent1,REQUEST_CAMERA);
                        }

                    }else if(items[i].equals("Gallery")){
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(intent.createChooser(intent,"Select File"), GALLERY_INTENT);
                    }else if(items[i].equals("Cancel")){
                        dialog.dismiss();
                    }
                }
            });
            builder.show();


    }


    //After Selecting image from gallery image will directly uploaded to Firebase Database
    //and Image will Show in Image View
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {

            mImageUri = data.getData();
            user_image.setImageURI(mImageUri);
            StorageReference filePath = mStorage.child("User_Images").child(mImageUri.getLastPathSegment());

            mProgressDialog.setMessage("Uploading Image....");
            mProgressDialog.show();

            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Uri downloadUri = taskSnapshot.getDownloadUrl();  //Ignore This error

                    mRoofRef.child("Image_URL").setValue(downloadUri.toString());

                    Glide.with(getApplicationContext())
                            .load(downloadUri)
                            .crossFade()
                            .placeholder(R.drawable.ic_launcher_background)
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(user_image);
                    Toast.makeText(getApplicationContext(), "Posted.", Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                }
            });
        }
        else if(requestCode == REQUEST_CAMERA && resultCode == RESULT_OK){
            //Recopy code above
            mImageUri = data.getData();
            user_image.setImageURI(mImageUri);
            StorageReference filePath = mStorage.child("User_Images").child(mImageUri.getLastPathSegment());

            mProgressDialog.setMessage("Uploading Image....");
            mProgressDialog.show();

            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Uri downloadUri = taskSnapshot.getDownloadUrl();  //Ignore This error

                    mRoofRef.child("Image_URL").setValue(downloadUri.toString());

                    Glide.with(getApplicationContext())
                            .load(downloadUri)
                            .crossFade()
                            .placeholder(R.drawable.ic_launcher_background)
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(user_image);
                    Toast.makeText(getApplicationContext(), "Posted.", Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                }
            });
        }
    }

}

