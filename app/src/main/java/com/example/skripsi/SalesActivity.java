package com.example.skripsi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skripsi.model.InputDataPlg;
import com.example.skripsi.model.ListOnline;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SalesActivity extends AppCompatActivity {

    @BindView(R.id.cardViewListOnline)
    CardView cViewListOnline;

    ListOnlineAdapter myAdapter;
    RecyclerView recyclerView;
    private DatabaseReference userListRef, onlineStatus, connectedRef;
    private RecyclerView.LayoutManager layoutManager;
    ArrayList<ListOnline> list;
    FirebaseDatabase db;
    DatabaseReference dbRef;
    FirebaseUser user;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_online);

        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarListOnline);
        setSupportActionBar(toolbar);
        toolbar.setTitle("List Online");

        //setup recyclerview
        recyclerView = findViewById(R.id.listOnline);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        myAdapter = new ListOnlineAdapter(list);

        //firebase
        db = FirebaseDatabase.getInstance();
        userListRef = db.getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        readAndWrite();
        recyclerView.setAdapter(myAdapter);
        recyclerView.setVisibility(View.VISIBLE);
        readDB();
    }

    //ambil data lalu kirim ke db
    private void readAndWrite(){
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        String lastSeen = format.format(currentTime);
        String id= user.getUid();

        preferences=getSharedPreferences("profil", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("id",id);
        editor.commit();

        onlineStatus=db.getReference("listOnline/"+id);
        connectedRef= FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue(Boolean.class)){
                    DatabaseReference dbRead= db.getReference().child("Akun").child(id);
                    dbRead.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                onlineStatus.onDisconnect().removeValue();//delete child from listOnline

                            ListOnline listOnline = snapshot.getValue(ListOnline.class);

                            String nama= listOnline.getNamaPanjang();
                            String nip=listOnline.getNip();
                            String role= listOnline.getRole();
                            String phone = listOnline.getNoTelp();
                            ListOnline listOnlineAkun = new ListOnline (nama, nip, role, lastSeen, phone);
                            onlineStatus.setValue(listOnlineAkun);

                            Log.d("List Online", nama+""+nip+""+role+""+phone+""+lastSeen);


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("List Online: ", error.getMessage());
                        }
                    });
                    myAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e ("list online: ", error.getMessage());
            }
        });
    }

    //untuk recyclerview
    public void readDB(){
//        Query query= dbRef.child("listOnline").orderByChild("role").equalTo("Sales");
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
//                        ListOnline listOnline = dataSnapshot.getValue(ListOnline.class);
//                        list.add(listOnline);
//                    }
//                    myAdapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("List Online Error:", error.getMessage());
//            }
//        });

        userListRef = db.getReference().child("listOnline");
        userListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    ListOnline listOnline = dataSnapshot.getValue(ListOnline.class);
                    list.add(listOnline);
                }

                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("List Online:", error.getMessage());
            }
        });
    }

    public void onStop(){
        super.onStop();
        onlineStatus.onDisconnect().removeValue();//delete child from listOnline
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sales_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_logout:
                onlineStatus.onDisconnect().removeValue();//delete child from listOnline
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(SalesActivity.this, LoginActivity.class));
                finish();
                break;
            case R.id.input_data:
                startActivity(new Intent(SalesActivity.this, InputDataPlg.class));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}