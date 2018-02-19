package com.example.jevil.autoclub;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jevil.autoclub.Adapters.ChatAdapter;
import com.example.jevil.autoclub.Models.GroupModel;
import com.example.jevil.autoclub.Models.LocationModel;
import com.example.jevil.autoclub.Models.MessageModel;
import com.example.jevil.autoclub.Models.UserModel;
import com.example.jevil.autoclub.Views.AuthDialog;
import com.example.jevil.autoclub.Views.GroupsActivity;
import com.example.jevil.autoclub.Views.PddActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    // переменные
    private final String TAG = "myLog";
    private Context context;
    private ArrayList<String> groupList;
    String currentGroup = "";
    private UserModel currentUser;
    IconGenerator iconFactory;
    HashMap<String, Marker> hashMapMarker;
    // получаем нашу базу
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    // ссылка на логин пользователя
    DatabaseReference usersRef = database.getReference("Users");

    // ссылка на координаты
    DatabaseReference locationRef = database.getReference("Location");

    // ссылка на чат
    private DatabaseReference chatRef = database.getReference("Chat");

    // получаем доступ к авторизации
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @BindView(R.id.clChat)
    ConstraintLayout clChat;

    // чат
    Button button;
    private RecyclerView rvChat;
    public List<MessageModel> result;
    private ChatAdapter chatAdapter;
    ChildEventListener childEventListenerChat, childEventListenerLocation;
    LinearLayoutManager llm;

    // карта
    private static final int REQUEST_LOCATION = 101;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    LocationManager locationManager;
    TextView tvSpeed;
    Location mLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        context = this;
        if (!userInTheSystem()) { // если пользователь не в системе, открываем диалог авторизации
            AuthDialog mydialog = new AuthDialog(this);
            mydialog.setCancelable(false);
            mydialog.show();
            mydialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    getCurrentUser();
                }
            });
        } else { // иначе меняем статус
            usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status")
                    .setValue("online");
            // получаем данные текущего пользователя из базы

            getCurrentUser();


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) { // если карта доступна
        mMap = googleMap;

        setMyLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // проверка по запрашиваемому коду
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // разрешение успешно получено
                setMyLocation();
            } else {
                // разрешение не получено
                Toast.makeText(this, "Разрешение не получено", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void setMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        } else {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);

            // если карта доступна, устанавливаем центрирование на местоположении устройства
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                                mLocation = location;
                                mMap.moveCamera(center);
                                // зум
//                                CameraUpdate zoom = CameraUpdateFactory.zoomTo(12);
//                                mMap.animateCamera(zoom);

                            }
                        }
                    });
            locationManager.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER, 500, 3, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location loc) {
                            tvSpeed.setText(String.valueOf(loc.getSpeed()));
                            // записываем координаты
                            if ((currentUser != null) && (currentGroup != null)) { // если данные о пользователе успели прийти

                                LocationModel locationModel = new LocationModel(loc.getLatitude(), loc.getLongitude(), currentUser.getNickname(), currentUser.getUid(), currentGroup, currentUser.getEmail());

                                Map<String, Object> groupValues = locationModel.toMap();
                                Map<String, Object> location = new HashMap<>();
                                location.put(currentUser.getUid(), groupValues);
                                locationRef.child(currentGroup).updateChildren(location);
                            }
                        }

                        @Override
                        public void onProviderDisabled(String arg0) {
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                        }

                    });
        }
    }

    private void requestPermissions() {
        // запрашиваем разрешение
        ActivityCompat.requestPermissions(this,
                new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_NETWORK_STATE
                },
                REQUEST_LOCATION);
    }

    public void addMarker(double lat, double lng, String name, String email) {

        Marker marker = hashMapMarker.get(email);
        if (marker != null) { // удаляем предыдущий маркер
            marker.remove();
            hashMapMarker.remove(email);
        }

        Marker mMarker;

        mMarker = mMap.addMarker(new MarkerOptions()
//                .snippet("Population: 2,074,200")
                .icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(name)))
                .position(new LatLng(lat, lng))
                .title(email));

        hashMapMarker.put(email, mMarker);

        Log.d("myLog", " id маркера: " + mMarker.getId());
    }

    private void displayChat() {
        childEventListenerChat = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                result.add(dataSnapshot.getValue(MessageModel.class));
                chatAdapter.notifyDataSetChanged();
                // прокручиваем спиоок к последнему сообщению
                llm.scrollToPosition(result.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        chatRef.child(currentGroup).addChildEventListener(childEventListenerChat);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
//            case R.id.action_create_group:
//                Intent intentCreateGroup = new Intent(this, CreateGroupActivity.class);
//                startActivity(intentCreateGroup);
//                break;
            case R.id.action_my_groups:
                Intent intentMyGroups = new Intent(this, GroupsActivity.class);
                startActivity(intentMyGroups);
                break;
            case R.id.action_pdd:
                Intent intentPdd = new Intent(this, PddActivity.class);
                startActivity(intentPdd);
                break;
            case R.id.action_sign_out:
                usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status")
                        .setValue("offline");
                mAuth.signOut();
                Intent i = new Intent( this , this.getClass() );
                finish();
                this.startActivity(i);
                // удаляем координаты пользователя
                //locationRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(currentGroup).removeValue();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getCurrentUser() {
        // получаем данные текущего пользователя
        usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(UserModel.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // получаем группы пользователя
        groupList = new ArrayList<>();

        // Получаем экземпляр элемента Spinner
        final Spinner spinner = findViewById(R.id.spinCurrentGroup);

        // Настраиваем адаптер
        final ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(this, R.layout.item_group_spinner_white, R.id.tvGroup, groupList);
        spinnerAdapter.setDropDownViewResource(R.layout.item_group_spinner);
        spinner.getBackground().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        // Вызываем адаптер
        spinner.setAdapter(spinnerAdapter);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("currentGroup").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentGroup = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("myGroups").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                GroupModel group = dataSnapshot.getValue(GroupModel.class);
                if (!group.getStatus().equals("request")) {
                    groupList.add(group.getName());
                    spinnerAdapter.notifyDataSetChanged();
                    clChat.setVisibility(View.VISIBLE);
                }
                // выбираем текущую группу
                if (group.getName().equals(currentGroup)) {
                    spinner.setSelection(spinnerAdapter.getPosition(currentGroup));

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                GroupModel group = dataSnapshot.getValue(GroupModel.class);

                groupList.add(group.getName());
                spinnerAdapter.notifyDataSetChanged();
                clChat.setVisibility(View.VISIBLE);

                // выбираем текущую группу
                if (group.getName().equals(currentGroup)) {
                    spinner.setSelection(spinnerAdapter.getPosition(currentGroup));

                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                try {
                    // если группа была удалена
                    groupList.remove(spinnerAdapter.getPosition(dataSnapshot.getKey()));
                    if (groupList.size() != 0) {
                        // ставим по умолчанию первую группу
                        spinner.setSelection(0);
                        usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("currentGroup")
                                .setValue(groupList.get(0));
                    } else {
                        // а если групп не осталось, прячем выбор группы
                        clChat.setVisibility(View.GONE);
                    }

                } catch (Exception e) {
                    Log.d("myLog", e.getMessage());
                }
                spinnerAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        result = new ArrayList<>();
        rvChat = findViewById(R.id.rvGroup);
        rvChat.setHasFixedSize(true);
        llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        // llm.setStackFromEnd(true);
        rvChat.setLayoutManager(llm);
        chatAdapter = new ChatAdapter(result);
        rvChat.setAdapter(chatAdapter);


        // устанавливаем текущую группу при выборе
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {
                // удаляем метки
                clearMarkers();
//                Iterator<Map.Entry<String, Marker>> entries = hashMapMarker.entrySet().iterator();
//                while (entries.hasNext()) {
//                    entries.next().getValue().remove();
//                }
//                hashMapMarker.clear();

                // при изменинии группы отвязываем слушатели на старые ссылки
                // чат
                if (childEventListenerChat != null) {
                    chatRef.child(currentGroup).removeEventListener(childEventListenerChat);
                    result.clear();
                    chatAdapter.notifyDataSetChanged();
                }
                // локация
                if (childEventListenerLocation != null) {
                    locationRef.child(currentGroup).removeEventListener(childEventListenerLocation);
                }

                usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("currentGroup")
                        .setValue(groupList.get(selectedItemPosition));
                currentGroup = groupList.get(selectedItemPosition);
                getUsersLocation();
                displayChat();
                setMyLocation();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // чат
        button = findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = findViewById(R.id.editText);

                // сейчас
                chatRef.child(currentGroup).push().setValue(new MessageModel(input.getText().toString(),
                        currentUser.getNickname(), currentUser.getUid()));
                input.setText("");
            }
        });

        // карта
        iconFactory = new IconGenerator(this); // генератор иконок для маркеров
        hashMapMarker = new HashMap<>(); // набор добавленных маркеров (для манипуляций)
        tvSpeed = findViewById(R.id.tvSpeed);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    public void onDestroy() {
        if (userInTheSystem()) { // если пользователь в системе, меняем статус при закрытии приложении
            usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status")
                    .setValue("offline");
        }
        super.onDestroy();
    }

    public boolean userInTheSystem() { // проверяем, авторизован ли пользователь
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null;
    }

    public void getUsersLocation() {
        childEventListenerLocation = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String uid = dataSnapshot.getKey();
                LocationModel locationModel = dataSnapshot.getValue(LocationModel.class);
                if (!locationModel.getUid().equals(currentUser.getUid()))
                    addMarker(locationModel.getLat(), locationModel.getLng(), locationModel.getNickname(), locationModel.getEmail());
//                   Log.d("myLog", "Добавлено: " + locationModel.getGroup() + " Пользователь: " + locationModel.getNickname() + " lat " + locationModel.getLat() + " lng " + locationModel.getLng());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        locationRef.child(currentGroup).addChildEventListener(childEventListenerLocation);
    }

    public void clearMarkers() {
        Iterator<Map.Entry<String, Marker>> entries = hashMapMarker.entrySet().iterator();
        while (entries.hasNext()) {
            entries.next().getValue().remove();
        }
        hashMapMarker.clear();
    }
}
