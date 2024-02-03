package ba.sum.fpmoz.libraryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import ba.sum.fpmoz.libraryapp.adapters.BooksViewPagerAdapter;
import ba.sum.fpmoz.libraryapp.models.User;

public class BookActivity extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager2 viewPager2;

    ImageView navigationUserImage;

    TextView navigationUserFullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_tabbed_view);

        this.tabLayout = findViewById(R.id.tabLayout);
        this.viewPager2 = findViewById(R.id.viewPager2);

        BooksViewPagerAdapter booksViewPagerAdapter = new BooksViewPagerAdapter(this);
        this.viewPager2.setAdapter(booksViewPagerAdapter);

        new TabLayoutMediator(this.tabLayout, this.viewPager2,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Knjige");
                            break;
                        case 1:
                            tab.setText("Dodaj knjigu");
                            break;
                    }
                }
        ).attach();
        NavigationView navigationView = findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);

        navigationUserImage = headerView.findViewById(R.id.user_profile_image);
        navigationUserFullName = headerView.findViewById(R.id.user_firstlastname);

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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("LibraryApp aplikacija");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_book_24);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        this.fetchUserData();

    }

    private void fetchUserData() {
        DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference("users");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        userDbRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    // Pristupite poljima objekta User
                    Picasso.get()
                            .load(user.profileImageUrl)
                            .into(navigationUserImage);

                    navigationUserFullName.setText(user.firstname + " " + user.lastname); // Premještanje ovdje unutar if bloka
                } else {
                    // Rukovanje ako je objekt User null
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

}