package ba.sum.fpmoz.libraryapp;

import static ba.sum.fpmoz.libraryapp.R.id.spinnerGender;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

import ba.sum.fpmoz.libraryapp.models.User;
import ba.sum.fpmoz.libraryapp.R;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase db;
    FirebaseAuth mAuth;
    StorageReference storageReference;
    String gender;
    String uuid;  //= "1";
    Uri filePath;

    EditText firstnameTxt;
    EditText lastnameTxt;
    Spinner spinnerGender;
    EditText dateOfBirthTxt;
    EditText cityTxt;
    EditText countryTxt;

    ImageView selectImageButton;

    ImageView navigationUserImage;

    TextView navigationUserFullName;

    NavigationView navigationView;

    private static final int IMAGE_PICK_REQUEST = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        this.uuid = currentUser.getUid();
        this.db = FirebaseDatabase.getInstance();

        Spinner spinner = findViewById(R.id.spinnerGender);

        String[] genderOptions = {"Muško", "Žensko", "Nije specificirano"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedGender = (String) parentView.getItemAtPosition(position);
                Toast.makeText(MainActivity.this, "Odabrani spol: " + selectedGender, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });

        EditText firstnameTxt = findViewById(R.id.editTextFirstName);
        EditText lastnameTxt = findViewById(R.id.editTextLastName);
        Spinner spinnerGender = findViewById(R.id.spinnerGender);
        EditText dateOfBirthTxt = findViewById(R.id.editTextBirthDate);
        EditText cityTxt = findViewById(R.id.editTextLocation);
        EditText countryTxt = findViewById(R.id.editTextCountry);

        Button submitBtn = findViewById(R.id.btnSaveProfile);
        DatabaseReference usersDbRef = this.db.getReference("users");

        storageReference = FirebaseStorage.getInstance().getReference();

        Button selectImageButton;
        selectImageButton = findViewById(R.id.btnSelectImage);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filePath != null) {
                    StorageReference fileReference = storageReference.child("profile_images/" + UUID.randomUUID().toString());
                    fileReference.putFile(filePath)
                            .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                User u = new User(
                                        firstnameTxt.getText().toString(),
                                        lastnameTxt.getText().toString(),
                                        gender,
                                        dateOfBirthTxt.getText().toString(),
                                        cityTxt.getText().toString(),
                                        countryTxt.getText().toString(),
                                        imageUrl
                                );
                                usersDbRef.child(uuid).setValue(u);
                                Toast.makeText(MainActivity.this, "Podaci uspješno spremljeni", Toast.LENGTH_SHORT).show();
                            }));
                } else {

                }
            }
        });

        this.fetchUserData();
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_logout) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                    FirebaseAuth.getInstance().signOut();
                    return true;
                }
                if (item.getItemId() == R.id.nav_profile) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                }
                if (item.getItemId() == R.id.nav_books) {
                    Intent intent = new Intent(getApplicationContext(), BookActivity.class);
                    startActivity(intent);
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                }
                return false;
            }
        });

        View headerView = navigationView.getHeaderView(0);

        navigationUserImage = headerView.findViewById(R.id.user_profile_image);
        navigationUserFullName = headerView.findViewById(R.id.user_firstlastname);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("LibraryApp aplikacija");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_book_24);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_PICK_REQUEST);
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            InputStream inputStream = null;
            try {
                inputStream = getContentResolver().openInputStream(filePath);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                selectImageButton.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void fetchUserData() {
        DatabaseReference usersDbRef = db.getReference("users");
        String userId = mAuth.getCurrentUser().getUid();

        usersDbRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    firstnameTxt.setText(user.firstname);
                } else {
                    // Obavijestite korisnika ili dodajte dodatnu logiku ako se objekt user vraća kao null.
                }
                if (user != null) {
                    // Provjerite da li je svojstvo lastname null prije postavljanja teksta
                    if (user.lastname != null) {
                        lastnameTxt.setText(user.lastname);
                    } else {
                        // Obavijestite korisnika ili dodajte dodatnu logiku ako je svojstvo lastname null
                    }
                } else {
                    // Obavijestite korisnika ili dodajte dodatnu logiku ako je objekt user null
                }
                if (user != null) {
                    // Provjerite da li je svojstvo dateOfBirth null prije postavljanja teksta
                    if (user.dateOfBirth != null) {
                        dateOfBirthTxt.setText(user.dateOfBirth);
                    } else {
                        // Obavijestite korisnika ili dodajte dodatnu logiku ako je svojstvo dateOfBirth null
                    }
                } else {
                    // Obavijestite korisnika ili dodajte dodatnu logiku ako je objekt user null
                }
                // Provjerite je li objekt user null prije pristupa svojstvima
                if (user != null) {
                    // Postavite grad korisnika ako nije null
                    if (user.city != null) {
                        cityTxt.setText(user.city);
                    } else {
                        // Obavijestite korisnika ili dodajte dodatnu logiku ako je svojstvo city null
                    }

                    // Postavite državu korisnika ako nije null
                    if (user.country != null) {
                        countryTxt.setText(user.country);
                    } else {
                        // Obavijestite korisnika ili dodajte dodatnu logiku ako je svojstvo country null
                    }

                    // Provjerite spol korisnika i postavite odgovarajući odabir u spinneru
                    if ("Muško".equalsIgnoreCase(user.gender)) {
                        spinnerGender.setSelection(0); // Muško
                    } else if ("Žensko".equalsIgnoreCase(user.gender)) {
                        spinnerGender.setSelection(1); // Žensko
                    } else if ("Nije specificirano".equalsIgnoreCase(user.gender)) {
                        spinnerGender.setSelection(2); // Nije specificirano
                    }
                } else {
                    // Obavijestite korisnika ili dodajte dodatnu logiku ako je objekt user null
                }
                if (user != null) {
                    // Provjerite je li svojstvo profileImageUrl null prije korištenja
                    if (user.profileImageUrl != null) {
                        // Učitajte sliku korisnika koristeći Picasso ili drugu biblioteku za učitavanje slika
                        Picasso.get()
                                .load(user.profileImageUrl)
                                .into(selectImageButton);
                } else {
                    // Obavijestite korisnika ili dodajte dodatnu logiku ako je svojstvo profileImageUrl null
                }
            } else {
                // Obavijestite korisnika ili dodajte dodatnu logiku ako je objekt user null
            }
            if (user != null) {
                // Provjerite je li svojstva firstname i lastname null prije korištenja
                if (user.firstname != null && user.lastname != null) {
                    // Postavite tekst u navigationUserFullName ako su svojstva firstname i lastname dostupna
                    navigationUserFullName.setText(user.firstname + " " + user.lastname);
                } else {
                    // Obavijestite korisnika ili dodajte dodatnu logiku ako su svojstva firstname i lastname null
                }
            } else {
                // Obavijestite korisnika ili dodajte dodatnu logiku ako je objekt user null
            }
                
        }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
