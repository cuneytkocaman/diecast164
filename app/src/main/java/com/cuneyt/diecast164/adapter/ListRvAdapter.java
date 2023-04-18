package com.cuneyt.diecast164.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.cuneyt.diecast164.ListActivity;
import com.cuneyt.diecast164.R;
import com.cuneyt.diecast164.assistantclass.DateTime;
import com.cuneyt.diecast164.assistantclass.MobileDeviceName;
import com.cuneyt.diecast164.entities.DiecastModel;
import com.cuneyt.diecast164.entities.ErrorLogModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class ListRvAdapter extends RecyclerView.Adapter<ListRvAdapter.DesignListObjectHolder> {

    private Context context;
    private List<DiecastModel> diecastModelList;
    private DatabaseReference referenceDiecast, referenceUser, referenceError;
    private FirebaseUser firebaseUser;

    private FirebaseAuth firebaseAuth;

    private DiecastModel diecastModel = new DiecastModel();
    private ErrorLogModel errorLogModel = new ErrorLogModel();
    private DateTime dateTime = new DateTime();
    private MobileDeviceName deviceName = new MobileDeviceName();

    public void setDiecastModel(DiecastModel diecastModel) {
        this.diecastModel = diecastModel;
    }

    public void setDiecastModelList(List<DiecastModel> diecastModelList) {
        this.diecastModelList = diecastModelList;
    }

    public ListRvAdapter(Context context, List<DiecastModel> diecastModelList) {
        this.context = context;
        this.diecastModelList = diecastModelList;
    }


    public class DesignListObjectHolder extends RecyclerView.ViewHolder {
        public TextView textRvCarBrand, textRvModel, textRvTrademark, textRvTrademarkCode;
        public ImageButton imgBtDelete, imgBtEdit;

        public DesignListObjectHolder(View view) {
            super(view);

            textRvCarBrand = view.findViewById(R.id.textRvCarBrand);
            textRvModel = view.findViewById(R.id.textRvModel);
            textRvTrademark = view.findViewById(R.id.textRvTrademark);
            textRvTrademarkCode = view.findViewById(R.id.textRvTrademarkCode);
            imgBtEdit = view.findViewById(R.id.imgBtEdit);
            imgBtDelete = view.findViewById(R.id.imgBtDelete);
        }
    }

    @NonNull
    @Override
    public DesignListObjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.car_list_row_design, parent, false); // Satır tasarımı bağlandı.

        return new DesignListObjectHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DesignListObjectHolder holder, int position) { // Satır tasrımdaki (device_row_design) görsel nesneler, modeldeki değişkenlerle ilişkilendirildi.

        try{
            String diecastId = diecastModelList.get(position).getId().toString();
            String trademark = diecastModelList.get(position).getTrademark().toString();
            String car = diecastModelList.get(position).getCarBrand().toString();
            String model = diecastModelList.get(position).getCarModel().toString();
            String code = diecastModelList.get(position).getTrademarkCode().toString();

            holder.textRvTrademark.setText(trademark);
            holder.textRvCarBrand.setText(car);
            holder.textRvModel.setText(model);
            holder.textRvTrademarkCode.setText(code);

            holder.imgBtDelete.setOnClickListener(new View.OnClickListener() { // Diecast silme işlemleri
                @Override
                public void onClick(View view) {
                    referenceDiecast = FirebaseDatabase.getInstance().getReference(context.getResources().getString(R.string.db_car));

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    View viewDelete = LayoutInflater.from(view.getRootView().getContext()).inflate(R.layout.alert_delete_diecast, null);

                    TextView textAlertYes = viewDelete.findViewById(R.id.textAlertYes);
                    TextView textAlertNo = viewDelete.findViewById(R.id.textAlertNo);

                    builder.setView(viewDelete);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    textAlertYes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            referenceUser = FirebaseDatabase.getInstance().getReference(context.getResources().getString(R.string.db_user));
                            referenceDiecast = FirebaseDatabase.getInstance().getReference(context.getResources().getString(R.string.db_car)); // Diecast'ler geçerli kullanıcının ID'si altında toplandığı için User DB'ye bağlanıldı.

                            firebaseUser = FirebaseAuth.getInstance().getCurrentUser(); // Geçerli kullanıcı belirtildi.

                            referenceUser.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    String currentUser = snapshot.child(firebaseUser.getUid()).child("id").getValue().toString();

                                    referenceDiecast.child(currentUser).child(diecastId).removeValue(); // Geçerli kullanıcı altında, her diecast'e özel Id altındaki araç silindi.

                                    Toast.makeText(context, "Diecast silindi.", Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

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

            holder.imgBtEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    referenceUser = FirebaseDatabase.getInstance().getReference(context.getResources().getString(R.string.db_user));
                    referenceDiecast = FirebaseDatabase.getInstance().getReference(context.getResources().getString(R.string.db_car)); // Diecast'ler geçerli kullanıcının ID'si altında toplandığı için User DB'ye bağlanıldı.
                    firebaseUser = FirebaseAuth.getInstance().getCurrentUser(); // Geçerli kullanıcı belirtildi.

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    View viewAdd = LayoutInflater.from(view.getRootView().getContext()).inflate(R.layout.alert_add_diecast, null);

                    EditText editAlertTrademark = viewAdd.findViewById(R.id.editAlertTrademark);
                    EditText editAlertBrand = viewAdd.findViewById(R.id.editAlertBrand);
                    EditText editAlertModel = viewAdd.findViewById(R.id.editAlertModel);
                    EditText editAlertCode = viewAdd.findViewById(R.id.editAlertCode);
                    TextView textAlertYes = viewAdd.findViewById(R.id.textAlertYes);
                    TextView textAlertNo = viewAdd.findViewById(R.id.textAlertNo);

                    builder.setView(viewAdd);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    editAlertTrademark.setText(trademark); // Güncellenecek diecast'e tıklayınca bilgiler Alert Dialog'daki ilgili alanlara aktarıldı.
                    editAlertBrand.setText(car);
                    editAlertModel.setText(model);
                    editAlertCode.setText(code);

                    textAlertYes.setText("Güncelle");

                    textAlertYes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            referenceUser.addValueEventListener(new ValueEventListener() { // Güncelleme işlemleri
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    String currentUser = snapshot.child(firebaseUser.getUid()).child("id").getValue().toString();

                                    HashMap<String, Object> updateDiecast = new HashMap<>();
                                    updateDiecast.put("carBrand", editAlertBrand.getText().toString());
                                    updateDiecast.put("carModel", editAlertModel.getText().toString());
                                    updateDiecast.put("carPhoto", "image");
                                    updateDiecast.put("trademark", editAlertTrademark.getText().toString());
                                    updateDiecast.put("trademarkCode", editAlertCode.getText().toString());

                                    referenceDiecast.child(currentUser).child(diecastId).updateChildren(updateDiecast);

                                    Toast.makeText(context, "Diecast güncellendi.", Toast.LENGTH_SHORT).show();
                                    alertDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

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

        } catch (Exception e){

            String mobileDevName = deviceName.deviceName().toString();
            String err = String.valueOf(e);
            String date = dateTime.currentlyDateTime();

            errorLogModel = new ErrorLogModel(mobileDevName, "ListRvAdapter", err, date);
            referenceError = FirebaseDatabase.getInstance().getReference(context.getResources().getString(R.string.db_error));
            referenceError.push().setValue(errorLogModel);
        }


    }

    @Override
    public int getItemCount() {
        return diecastModelList.size();
    }


    public void deleteItem(int position){
        diecastModelList.remove(position);
        notifyItemInserted(position);
    }

    /*ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            referenceUser = FirebaseDatabase.getInstance().getReference(context.getResources().getString(R.string.db_user)); // Oturum açmış kullanıcı adına ulaşmak için User DB'ye bağlanıldı

            referenceUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String currentUser = snapshot.child(firebaseUser.getUid()).child("id").getValue().toString();
                    String carId = diecastModel.getId().toString();

                    referenceDiecast.child(currentUser).child(carId).removeValue();

                    diecastModelList.remove(viewHolder.getAdapterPosition());

                    Toast.makeText(context, "Silindi", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    };*/
}
