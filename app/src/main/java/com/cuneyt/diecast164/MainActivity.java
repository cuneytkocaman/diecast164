package com.cuneyt.diecast164;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cuneyt.diecast164.assistantclass.DateTime;
import com.cuneyt.diecast164.assistantclass.MobileDeviceName;
import com.cuneyt.diecast164.assistantclass.RandomId;
import com.cuneyt.diecast164.databinding.ActivityMainBinding;
import com.cuneyt.diecast164.entities.DiecastModel;
import com.cuneyt.diecast164.entities.ErrorLogModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mainBinding;
    private DatabaseReference referenceCar, referenceUser, referenceError;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private RandomId randomId = new RandomId();
    private MobileDeviceName deviceName = new MobileDeviceName();
    private ErrorLogModel errorLogModel = new ErrorLogModel();
    private DateTime dateTime = new DateTime();
    private DiecastModel diecastModel = new DiecastModel();
    private Uri imageUri;
    private StorageReference storageReference;

    private final int PICK_IMAGE_REQUEST = 71;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

            firebaseUser = FirebaseAuth.getInstance().getCurrentUser(); // Oturum açmış kullanıcı bilgilerine ulaşılması için firebaseUser'a aktarıldı. (firebaseUser.getUid, getName vs)

            referenceCar = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_car));
            referenceUser = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_user));

            //    referenceUser = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.db_user));

            storageReference = FirebaseStorage.getInstance().getReference();

            mainBinding.textBtSelectTrademark.setOnClickListener(new View.OnClickListener() { // Üretici Firma seçme işlemleri
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    View viewTrademark = getLayoutInflater().inflate(R.layout.alert_trademark_choise, null); // Alert Dialog tasarımı bağlandı.

                    TextView textAlertOk = viewTrademark.findViewById(R.id.textAlertOk);
                    TextView textAlertClose = viewTrademark.findViewById(R.id.textAlertClose);
                    RadioGroup radioGroupTrademark = viewTrademark.findViewById(R.id.rGroupTrademark); // Alert Dialog'da bulunan görsel öğeler

                    builder.setView(viewTrademark);

                    AlertDialog alertDialog = builder.create();
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    radioGroupTrademark.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup radioGroup, int i) {
                            RadioButton radioButton = radioGroup.findViewById(i);
                        }
                    });

                    textAlertOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            int radioSelectId = radioGroupTrademark.getCheckedRadioButtonId(); //Seçilen radiobuton'un id'si int değere atandı.

                            if (radioSelectId == -1) {
                                Toast.makeText(MainActivity.this, "Seçim yapılmadı.", Toast.LENGTH_LONG).show();

                            } else {
                                RadioButton radioButtonTra = radioGroupTrademark.findViewById(radioSelectId);

                                mainBinding.textBtSelectTrademark.setText(radioButtonTra.getText());

                                alertDialog.dismiss();
                            }
                        }
                    });

                    textAlertClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.show();
                }
            });

            mainBinding.imgBtSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (TextUtils.isEmpty(mainBinding.editCarBrand.getText().toString()) ||
                            TextUtils.isEmpty(mainBinding.editCarModel.getText().toString()) ||
                            mainBinding.textBtSelectTrademark.getText().equals("Üretici Seç")) {

                        Toast.makeText(MainActivity.this, "Üretici, Araç Markası ve Araç Modeli alanlarını girmelisiniz.", Toast.LENGTH_SHORT).show();

                    } else {
                        save();

                        Toast.makeText(getApplicationContext(), "Diecast Eklendi.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            mainBinding.imgBackList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intentList = new Intent(MainActivity.this, ListActivity.class);
                    startActivity(intentList);
                    finish();
                }
            });

        } catch (Exception e){

            String mobileDevName = deviceName.deviceName().toString();
            String err = String.valueOf(e);
            String date = dateTime.currentlyDateTime();

            errorLogModel = new ErrorLogModel(mobileDevName, "MainActivity", err, date);
            referenceError = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_error));
            referenceError.push().setValue(errorLogModel);
        }

    }

    public void save() {

        String uniqueId = randomId.randomUUID(); // Her kayıt için benzersiz ID oluşturuldu.

        referenceCar.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String userId = firebaseUser.getUid().toString();

                String unique = uniqueId;
                String trademark = mainBinding.textBtSelectTrademark.getText().toString();
                String car = mainBinding.editCarBrand.getText().toString();
                String model = mainBinding.editCarModel.getText().toString();
                String tradeCode = mainBinding.editTradeCode.getText().toString();
                String photo = "resim";

                diecastModel = new DiecastModel(unique, trademark, car, model, tradeCode, photo);

                referenceCar.child(userId).child(unique).setValue(diecastModel);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

   /*public void selectPhoto(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && data != null && data.getData() != null){

            imageUri = data.getData();
            mainBinding.imgCarPhoto.setImageURI(imageUri);
        }

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                mainBinding.imgCarPhoto.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void uploadImage(){ // Görseli firebase'e yükleme işlemleri.

        if(imageUri != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);

            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            String userId = firebaseUser.getUid().toString();

            Log.e("CUCU", userId);

            storageReference = storageReference.child(userId + "images/" + randomId.randomUUID().toString());

            storageReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date now = new Date();
        String fileName = dateFormat.format(now);

        storageReference = FirebaseStorage.getInstance().getReference("images/"+fileName);

        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                mainBinding.imgCarPhoto.setImageURI(null);
                Toast.makeText(MainActivity.this, "Başarılı", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Görsel yüklenemedi.", Toast.LENGTH_SHORT).show();
            }
        });
    }*/

}