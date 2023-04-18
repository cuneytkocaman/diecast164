package com.cuneyt.diecast164;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.cuneyt.diecast164.adapter.ListRvAdapter;
import com.cuneyt.diecast164.assistantclass.DateTime;
import com.cuneyt.diecast164.assistantclass.MobileDeviceName;
import com.cuneyt.diecast164.assistantclass.RandomId;
import com.cuneyt.diecast164.databinding.ActivityListBinding;
import com.cuneyt.diecast164.entities.DiecastModel;
import com.cuneyt.diecast164.entities.ErrorLogModel;
import com.cuneyt.diecast164.entities.UserModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ListActivity extends AppCompatActivity {
    private ActivityListBinding listBinding;

    private DiecastModel diecastModel = new DiecastModel();

    private UserModel userModel = new UserModel();
    private ErrorLogModel errorLogModel = new ErrorLogModel();
    private ArrayList<DiecastModel> diecastLists = new ArrayList<>(); // Veri kümesi. Modelden verileri almak için oluşturuldu.
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private ListRvAdapter carRvAdapter;
    private DatabaseReference referenceDiecast, referenceUser, referenceError;

    private RandomId randomId = new RandomId();
    private MobileDeviceName deviceName = new MobileDeviceName();
    private DateTime dateTime = new DateTime();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        try {

            listBinding = DataBindingUtil.setContentView(this, R.layout.activity_list);

            carListShow();

            firebaseAuth = FirebaseAuth.getInstance();
            firebaseUser = firebaseAuth.getCurrentUser(); // Oturum açmış kullanıcı bilgilerine ulaşılması için firebaseUser'a aktarıldı. (firebaseUser.getUid, getName vs)
            referenceUser = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_user)); // Oturum açmış kullanıcı adına ulaşmak için User DB'ye bağlanıldı
            referenceDiecast = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_car));

            referenceUser.addValueEventListener(new ValueEventListener() { // Oturum açmış kullancının adını almak için Firebase'e bağlanıldı.
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String currentUser = snapshot.child(firebaseUser.getUid()).child("name").getValue().toString(); // Oturum açmış kullanıcının adı alındı
                    listBinding.textCurrentUser.setText(currentUser);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            listBinding.imgBtAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    carAdd();
                }
            });

            listBinding.imgBtLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AlertDialog.Builder builderLogout = new AlertDialog.Builder(ListActivity.this);
                    View viewLogout = getLayoutInflater().inflate(R.layout.alert_sign_out, null);

                    TextView textAlertYes = viewLogout.findViewById(R.id.textAlertYes);
                    TextView textAlertNo = viewLogout.findViewById(R.id.textAlertNo);

                    builderLogout.setView(viewLogout);

                    AlertDialog alertDialog = builderLogout.create();
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    textAlertYes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            FirebaseAuth.getInstance().signOut();

                            Intent intentLogout = new Intent(ListActivity.this, LoginActivity.class);
                            startActivity(intentLogout);
                            finish();

                            alertDialog.dismiss();
                        }
                    });

                    textAlertNo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();
                }
            });


            rvItemCount();

            rvSearch();

        /*ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(listBinding.rvCar);*/

        } catch (Exception e){

            String mobileDevName = deviceName.deviceName().toString();
            String err = String.valueOf(e);
            String date = dateTime.currentlyDateTime();

            errorLogModel = new ErrorLogModel(mobileDevName, "ListActivity", err, date);
            referenceError = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_error));
            referenceError.push().setValue(errorLogModel);
        }
    }

    public void carListShow() {

        listBinding.rvCar.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, true); // VERTICAL, true: RecyclerView'e eklenen veri en alttan üste doğru eklenir.
        linearLayoutManager.setStackFromEnd(true); // RecyclerView'e eklenen veri sayfayı otomatik kaydırır.
        listBinding.rvCar.setLayoutManager(linearLayoutManager);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewInflater = inflater.inflate(R.layout.car_list_row_design, null);

        carRvAdapter = new ListRvAdapter(this, diecastLists);
        listBinding.rvCar.setAdapter(carRvAdapter); // RecyclerView'de çalışacak Adapter tanıltıldı.

        referenceUser = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_user));

        referenceUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String currentUserId = snapshot.child(firebaseUser.getUid()).child("id").getValue().toString();

                referenceDiecast = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_car)).child(currentUserId);

                referenceDiecast.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        diecastLists.clear();

                        for (DataSnapshot d : snapshot.getChildren()) {

                            DiecastModel diecast = d.getValue(DiecastModel.class);

                            diecastLists.add(diecast);

                        }

                        Collections.sort(diecastLists, new Comparator<DiecastModel>() { //RecyclerView A->Z sıralama
                            @Override
                            public int compare(DiecastModel diecastModel, DiecastModel t1) {
                                return t1.getCarBrand().compareToIgnoreCase(diecastModel.getCarBrand());
                            }
                        });

                        carRvAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void carAdd() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
        View viewAdd = getLayoutInflater().inflate(R.layout.alert_add_diecast, null);

        EditText editAlertTrademark = viewAdd.findViewById(R.id.editAlertTrademark);
        EditText editAlertBrand = viewAdd.findViewById(R.id.editAlertBrand);
        EditText editAlertModel = viewAdd.findViewById(R.id.editAlertModel);
        EditText editAlertCode = viewAdd.findViewById(R.id.editAlertCode);
        TextView textAlertYes = viewAdd.findViewById(R.id.textAlertYes);
        TextView textAlertNo = viewAdd.findViewById(R.id.textAlertNo);

        builder.setView(viewAdd);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        referenceDiecast = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_car));

        textAlertYes.setText("Evet");

        textAlertYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String brand = editAlertBrand.getText().toString();
                int lenghtBrand = brand.length();

                if (TextUtils.isEmpty(editAlertTrademark.getText().toString()) ||
                        TextUtils.isEmpty(editAlertBrand.getText().toString()) ||
                        TextUtils.isEmpty(editAlertModel.getText().toString())) {

                    Toast.makeText(ListActivity.this, "Üretici, Araç Markası ve Araç Modeli alanlarını girmelisiniz.", Toast.LENGTH_SHORT).show();

                } else {

                    String uId = randomId.randomUUID(); // Her kayıt için benzersiz ID oluşturuldu.

                    referenceDiecast.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String userId = firebaseUser.getUid().toString();
                            String trademark = editAlertTrademark.getText().toString();
                            String car = editAlertBrand.getText().toString();
                            String model = editAlertModel.getText().toString();
                            String tradeCode = editAlertCode.getText().toString();
                            String photo = "picture";

                            if (lenghtBrand == 1){
                                SpannableString sCar = new SpannableString(car); // SpannableString: String ifadenin belirli bir kısmını almaya yarar.
                                String subStringCar = sCar.subSequence(0,1).toString(); // Her aracın ID'sinin başına araç markasının ilk 2 harfi alındı. Bunun sebebi Firebase JSON'da veriyi okumayı kolaylaştırmak.

                                SpannableString sModel = new SpannableString(model);
                                String subStringModel = sModel.subSequence(0,1).toString(); // Araç isminin ve modelinin tamamının alınmamasının sebebi, bazı isimlerde boşluk olmadı.

                                String uniqueId = subStringCar + "-" + subStringModel + "-" + uId; // ID'nin başına eklendi.

                                diecastModel = new DiecastModel(uniqueId, trademark, car, model, tradeCode, photo);

                                referenceDiecast.child(userId).child(uniqueId).setValue(diecastModel);

                                Toast.makeText(getApplicationContext(), "Diecast Eklendi.", Toast.LENGTH_SHORT).show();

                            } else {
                                SpannableString sCar = new SpannableString(car); // SpannableString: String ifadenin belirli bir kısmını almaya yarar.
                                String subStringCar = sCar.subSequence(0,2).toString(); // Her aracın ID'sinin başına araç markasının ilk 2 harfi alındı. Bunun sebebi Firebase JSON'da veriyi okumayı kolaylaştırmak.

                                SpannableString sModel = new SpannableString(model);
                                String subStringModel = sModel.subSequence(0,1).toString(); // Araç isminin ve modelinin tamamının alınmamasının sebebi, bazı isimlerde boşluk olmadı.

                                String uniqueId = subStringCar + "-" + subStringModel + "-" + uId; // ID'nin başına eklendi.

                                diecastModel = new DiecastModel(uniqueId, trademark, car, model, tradeCode, photo);

                                referenceDiecast.child(userId).child(uniqueId).setValue(diecastModel);

                                Toast.makeText(getApplicationContext(), "Diecast Eklendi.", Toast.LENGTH_SHORT).show();

                            }
                            alertDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });

        textAlertNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    public void rvItemCount() { // RecyclerView'deki elemanları saymak için yapılan işlemler.

        referenceDiecast = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_car)); // Diecast sayısını öğrenmek için Car DB'ye bağlanıldı.
        String currentUserId = firebaseAuth.getCurrentUser().getUid(); // Diecast'ler geçerli kullanıcı ID'si altında tutulduğu için, geçerli kullanıcının ID'si alındı.

        referenceDiecast.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int countCar = (int) snapshot.getChildrenCount(); // Sayma işlemi yaptırıldı.
                    listBinding.textTotalCar.setText(String.valueOf(countCar));

                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void rvSearch() { // RecyclerView Arama İşlemleri
        listBinding.searchView.clearFocus();

        listBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                ArrayList<DiecastModel> newDiecasts = new ArrayList<>();

                for (DiecastModel diecast : diecastLists) {

                    if (diecast.getCarBrand().toLowerCase().contains(s.toLowerCase()) ||
                            diecast.getCarModel().toLowerCase().contains(s.toLowerCase()) ||
                            diecast.getTrademarkCode().toLowerCase().contains(s.toLowerCase())) { // Araç markasına göre arama yaptırıldı.

                        newDiecasts.add(diecast);
                    }
                }
                carRvAdapter.setDiecastModelList(newDiecasts);
                carRvAdapter.notifyDataSetChanged();

                return true;
            }
        });
    }

    /*ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            referenceUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String currentUser = snapshot.child(firebaseUser.getUid()).child("id").getValue().toString();
                    String carId = diecastModel.getId().toString();

                    referenceDiecast.child(currentUser).child(carId).removeValue();

                    diecastLists.remove(viewHolder.getAdapterPosition());
                    carRvAdapter.notifyDataSetChanged();

                    Snackbar snackbar = Snackbar.make(listBinding.constListMain, "Diecst silindi", Snackbar.LENGTH_LONG);
                    snackbar.show();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("HATA", error.toString());
                }
            });
        }
    };*/
}