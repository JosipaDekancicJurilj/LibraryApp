package ba.sum.fpmoz.libraryapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            redirectToMainActivity();
            return; // Dodajte return nakon redirectToMainActivity() kako biste spriječili nastavak izvršavanja metode ako je korisnik već prijavljen.
        }

        setContentView(R.layout.activity_login);


        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);

        loginButton.setOnClickListener(v -> loginUser());
    }


    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Unesite Vašu email adresu ili lozinku", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, "Prijava uspješna!", Toast.LENGTH_SHORT).show();
                redirectToMainActivity();
            } else {
                Toast.makeText(LoginActivity.this, "Greška prilikom prijave!" + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void redirectToMainActivity() {
        Intent i = new Intent(LoginActivity.this, BookActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
