package com.cuneyt.diecast164;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cuneyt.diecast164.assistantclass.DateTime;
import com.cuneyt.diecast164.assistantclass.MobileDeviceName;
import com.cuneyt.diecast164.databinding.ActivityLoginBinding;
import com.cuneyt.diecast164.entities.ErrorLogModel;
import com.cuneyt.diecast164.entities.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding loginBinding;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private DatabaseReference referenceError;
    private UserModel userModel = new UserModel();
    private ErrorLogModel errorLogModel = new ErrorLogModel();
    private MobileDeviceName deviceName = new MobileDeviceName();
    private DateTime dateTime = new DateTime();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {

            loginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);

            firebaseAuth = FirebaseAuth.getInstance();
            firebaseUser = firebaseAuth.getCurrentUser();

            loginBinding.textBtLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    loginUser();
                }
            });

            loginBinding.textBtRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intentRegister = new Intent(LoginActivity.this, com.cuneyt.diecast164.RegisterActivity.class);
                    startActivity(intentRegister);
                }
            });

            internetCheck();

        } catch (Exception e){

            String mobileDevName = deviceName.deviceName().toString();
            String err = String.valueOf(e);
            String date = dateTime.currentlyDateTime();

            errorLogModel = new ErrorLogModel(mobileDevName, "LoginActivity", err, date);
            referenceError = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_error));
            referenceError.push().setValue(errorLogModel);
        }


    }

    public void loginUser() {

        String inputUser = loginBinding.editUserName.getText().toString() + "@diecast.com";
        String inputPass = loginBinding.editPassword.getText().toString();

        userModel.setName(inputUser);
        userModel.setPassword(inputPass);

        if (!TextUtils.isEmpty(inputUser) && !TextUtils.isEmpty(inputPass)) {

            firebaseAuth.signInWithEmailAndPassword(userModel.getName(), userModel.getPassword())
                    .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() { // Giriş başarılı olunca yapılacaklar.
                        @Override
                        public void onSuccess(AuthResult authResult) {

                            Intent intentMain = new Intent(LoginActivity.this, ListActivity.class);
                            startActivity(intentMain);
                            finish();

                        }
                    }).addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this, "Kullanıcı adı veya parola hatalı.", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Kullancı adı veya Parola boş geçilemez.", Toast.LENGTH_LONG).show();

        }
    }

    public void onStart() {

        if (firebaseUser != null) {

            Intent intentMain = new Intent(LoginActivity.this, ListActivity.class);
            startActivity(intentMain);
            finish();
        }

        super.onStart();
    }

    private void internetCheck() {
        ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {


        } else {

            /*final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(LoginActivity.this);
            builder.setMessage("İnternet bağlantınızı kontrol edin.");
            builder.setCancelable(true);*/

            AlertDialog.Builder builderClose = new AlertDialog.Builder(LoginActivity.this);
            View viewClose = getLayoutInflater().inflate(R.layout.alert_internet_check, null);

            TextView textAlertClose = viewClose.findViewById(R.id.textAlertClose);

            builderClose.setView(viewClose);

            AlertDialog alertDialog = builderClose.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            textAlertClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

            /*builder.setPositiveButton("Çıkış", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish(); //Uygulamayı sonlandırıldı.
                }
            });

            android.app.AlertDialog alertDialog = builder.create();*/

            alertDialog.show();
        }
    }
}