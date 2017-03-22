package org.latefire.deals.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.io.File;
import org.latefire.deals.R;
import org.latefire.deals.database.DatabaseManager;
import org.latefire.deals.database.Deal;
import org.latefire.deals.databinding.ActivityCreateDealBinding;
import org.latefire.deals.utils.AlertDialogUtils;
import org.latefire.deals.utils.Constant;
import org.latefire.deals.utils.DeviceUtils;
import org.latefire.deals.utils.ProcessBitmap;

public class CreateDealActivity extends BaseActivity {

  private static final String LOG_TAG = CreateDealActivity.class.getSimpleName();

  ActivityCreateDealBinding b;
  DatabaseManager mDatabaseManager;
  private File mImageFile;
  private String mCurrentPhotoPath;
  private Deal mDeal;
  private Bitmap mBitmapImageDeal;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    b = DataBindingUtil.setContentView(this, R.layout.activity_create_deal);
    ButterKnife.bind(this);
    mDatabaseManager = DatabaseManager.getInstance();
    getSupportActionBar().setTitle("Create Deal");
    mDeal = new Deal();
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == Constant.FROM_GALLERY && resultCode == Activity.RESULT_OK) {
      Uri selectedImage = data.getData();
      mBitmapImageDeal = ProcessBitmap.decodeFile(this, selectedImage, 800, 800);
      b.imgDeal.setImageBitmap(mBitmapImageDeal);
    }
    if (requestCode == Constant.TAKE_A_PHOTO && resultCode == Activity.RESULT_OK) {
      File file = new File(mCurrentPhotoPath);
      mBitmapImageDeal = ProcessBitmap.decodeFile(file, 800, 800);
      b.imgDeal.setImageBitmap(mBitmapImageDeal);
    }
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
      @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      switch (requestCode) {
        case Constant.PERMISSION_REQUEST_CAMERA:
          DeviceUtils.takePicture(this, mImageFile);
          break;
        case Constant.PERMISSION_READ_WRITE_STORAGE:
          DeviceUtils.pickFromGallery(this);
          break;
        case Constant.PERMISSION_CREATE_FILE:
          mImageFile = DeviceUtils.getFileForCaptureImage(this);
          mCurrentPhotoPath = mImageFile != null ? mImageFile.getAbsolutePath() : "";
          break;
      }
    }
  }

  @OnClick(R.id.imgDeal) public void uploadImage(View view) {
    new AlertDialog.Builder(this).setTitle("Upload deal image")
        .setItems(R.array.upload_deal_image, (dialog, which) -> {
          dialog.dismiss();
          if (which == 0) {
            // take a picture
            mImageFile = DeviceUtils.getFileForCaptureImage(this);
            mCurrentPhotoPath = mImageFile != null ? mImageFile.getAbsolutePath() : "";
            //deal.setLocalPhotoPath(mCurrentPhotoPath);
          } else {
            // choose from gallery
            DeviceUtils.pickFromGallery(this);
          }
        })
        .create()
        .show();
  }

  @OnClick(R.id.btnSave) public void saveDeal(View view) {
    if (!validateInput()) {
      return;
    }
    showProgress();
    // Create a storage reference from our app
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    // Create a reference to "mountains.jpg"
    String imageName = String.valueOf(System.currentTimeMillis());
    StorageReference mountainsRef = storageRef.child(imageName + ".jpg");

    // Create a reference to 'images/mountains.jpg'
    StorageReference mountainImagesRef = storageRef.child("images/" + imageName + ".jpg");

    // While the file names are the same, the references point to different files
    mountainsRef.getName().equals(mountainImagesRef.getName());    // true
    mountainsRef.getPath().equals(mountainImagesRef.getPath());    // false

    // Get the data from an ImageView as bytes
    b.imgDeal.setDrawingCacheEnabled(true);
    b.imgDeal.buildDrawingCache();
    Bitmap bitmap = b.imgDeal.getDrawingCache();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
    byte[] data1 = baos.toByteArray();

    UploadTask uploadTask = mountainsRef.putBytes(data1);
    uploadTask.addOnFailureListener(exception -> {
      // Handle unsuccessful uploads
      dismissProgress();
    }).addOnSuccessListener(taskSnapshot -> {
      // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
      Uri downloadUrl = taskSnapshot.getDownloadUrl();
      mDeal.setPhoto(downloadUrl.toString());
      mDeal.setTitle(b.etTitle.getText().toString());
      mDeal.setDescription(b.etDescription.getText().toString());
      mDeal.setRegularPrice(Double.parseDouble(b.etPrice.getText().toString()));
      mDeal.setDealPrice(Double.parseDouble(b.etDealPrice.getText().toString()));
      mDeal.setLocation(b.etLocation.getText().toString());
      mDeal.setBusinessId("dummy-business");
      mDatabaseManager.createDeal(mDeal);
      dismissProgress();
      finish();
    });
  }

  private boolean validateInput() {
    if (b.etTitle.getText().toString().isEmpty()) {
      AlertDialogUtils.showError(this, getString(R.string.erro_input_title));
      return false;
    }
    if (b.etDescription.getText().toString().isEmpty()) {
      AlertDialogUtils.showError(this, getString(R.string.error_input_description));
      return false;
    }
    if (b.etLocation.getText().toString().isEmpty()) {
      AlertDialogUtils.showError(this, getString(R.string.error_input_location));
      return false;
    }
    if (b.etPrice.getText().toString().isEmpty()) {
      AlertDialogUtils.showError(this, getString(R.string.error_input_price));
      return false;
    }
    if (b.etDealPrice.getText().toString().isEmpty()) {
      AlertDialogUtils.showError(this, getString(R.string.error_input_deal_price));
      return false;
    }
    return true;
  }
}