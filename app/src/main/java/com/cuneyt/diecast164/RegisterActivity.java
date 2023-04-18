package com.cuneyt.diecast164;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.cuneyt.diecast164.assistantclass.DateTime;
import com.cuneyt.diecast164.assistantclass.MobileDeviceName;
import com.cuneyt.diecast164.databinding.ActivityRegisterBinding;
import com.cuneyt.diecast164.entities.ErrorLogModel;
import com.cuneyt.diecast164.entities.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding regBinding;

    private UserModel userModel = new UserModel();
    private DatabaseReference reference, referenceError;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private DateTime dateTime = new DateTime();
    private MobileDeviceName deviceName = new MobileDeviceName();
    private ErrorLogModel errorLogModel = new ErrorLogModel();

    private final String DB_USER = "Users";
    private final String DB_DIECAST = "Diecast";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        try {
            regBinding = DataBindingUtil.setContentView(this, R.layout.activity_register);

            firebaseAuth = FirebaseAuth.getInstance();

            regBinding.textBtRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String regName = regBinding.editRegName.getText().toString();
                    String pass = regBinding.editRegPassword.getText().toString();
                    String passAgain = regBinding.editRegPassAgain.getText().toString();
                    int passLenght = pass.length();
                    int regNameLenght = regName.length();

                    if (TextUtils.isEmpty(regBinding.editRegName.getText().toString()) ||
                            TextUtils.isEmpty(regBinding.editRegPassword.getText().toString()) ||
                            TextUtils.isEmpty(regBinding.editRegPassAgain.getText().toString())) {

                        Toast.makeText(RegisterActivity.this, "Koleksiyoner Adı ve Parola boş geçilemez.", Toast.LENGTH_LONG).show();

                    } else if (passLenght < 6){
                        Toast.makeText(RegisterActivity.this, "Parola 6 karakter olmalıdır.", Toast.LENGTH_SHORT).show();

                    } else if (!pass.equals(passAgain)){
                        Toast.makeText(RegisterActivity.this, "Parolalar aynı değil.", Toast.LENGTH_SHORT).show();

                    } else if (regNameLenght <= 3){
                        Toast.makeText(RegisterActivity.this, "Kullanıcı adı 3 karakterden uzun olmalıdır.", Toast.LENGTH_SHORT).show();

                    } else {
                        registerUser();
                    }
                }
            });

        } catch (Exception e){

            String mobileDevName = deviceName.deviceName().toString();
            String err = String.valueOf(e);
            String date = dateTime.currentlyDateTime();

            errorLogModel = new ErrorLogModel(mobileDevName, "RegisterActivity", err, date);
            referenceError = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_error));
            referenceError.push().setValue(errorLogModel);
        }
    }

    public void registerUser() {

        String date = dateTime.currentlyDateTime();
        String inputUserName = regBinding.editRegName.getText().toString() + date +"@diecast.com"; // Firebase'e e-mail olarak giriş yapılacağı için kullanıcının adının yanına @diescast.com uzantısı yazıldı. Üye olunduğu tarih ve saat isimlerin DB'de farklı olması için, isme eklendi.
        String inputPassword = regBinding.editRegPassword.getText().toString();

        userModel.setName(inputUserName);
        userModel.setPassword(inputPassword);

        firebaseAuth.createUserWithEmailAndPassword(userModel.getName(), userModel.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() { // addCompleteListener: İşlemin durumu hakkında bilgi verir.
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {  // Kayıt başarılı ise çalışır ve kullanıcı kaydı tamamlanır.

                            firebaseUser = firebaseAuth.getCurrentUser();
                            reference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.db_user)); // Realtime Database'de ağaçların root bölümünü getirir.

                            reference.addValueEventListener(new ValueEventListener() { // Realtime Database'e kullanıcıları ekleme işlemleri.
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                 //   firebaseUser = firebaseAuth.getCurrentUser();

                                    String id = firebaseUser.getUid().toString(); // Oturum açan kullancının Id'si.
                                    String userName = regBinding.editRegName.getText().toString();

                                    userModel = new UserModel(id, userName);

                                    reference.child(id).setValue(userModel); // Kullanıcı DB'ye eklendi.

                                    Intent intentLogin = new Intent(RegisterActivity.this, LoginActivity.class);
                                    //intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intentLogin);
                                    finish();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });



                            Toast.makeText(RegisterActivity.this, "Kayıt başarılı.", Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(RegisterActivity.this, "Kayıt başarısız", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}