package ba.sum.fpmoz.libraryapp.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.UUID;

import ba.sum.fpmoz.libraryapp.R;
import ba.sum.fpmoz.libraryapp.models.Book;

public class AddBookDialogFragment extends Fragment {

    FirebaseStorage storage;
    StorageReference storageReference;

    Uri filePath;

    String bookImageUrl;

    private static final int IMAGE_PICK_REQUEST = 22;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        this.storage = FirebaseStorage.getInstance();
        this.storageReference = this.storage.getReference();
        View view = inflater.inflate(R.layout.book_item_dialog_view, container, false);
        Button selectImageButton = view.findViewById(R.id.buttonSelectImage);
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(
                        Intent.createChooser(i, "Odaberite sliku knjige"), 22
                );
            }
        });

        Button insertBookButton = view.findViewById(R.id.buttonInsertBook);
        insertBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = ((EditText) view.findViewById(R.id.editTextName)).getText().toString();
                String author = ((EditText) view.findViewById(R.id.editTextAuthor)).getText().toString();
                String genre = ((EditText) view.findViewById(R.id.editTextGenre)).getText().toString();
                String yearString = ((EditText) view.findViewById(R.id.editTextYear)).getText().toString();
                String characters = ((EditText)view.findViewById(R.id.editTextCharacters)).getText().toString();
                String description = ((EditText)view.findViewById(R.id.editTextDescription)).getText().toString();

                if (!validateInputs(name, author, genre, yearString, characters, description) || bookImageUrl == null) {
                    Toast.makeText(getContext(), "Please upload an image and fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                Long year = Long.parseLong(yearString);

                addBookToDatabase(name, author, genre, year, characters, description, bookImageUrl);
                resetForm(view);
            }
        });
        return view;
    }

    private void resetForm(View view) {
        ((EditText) view.findViewById(R.id.editTextName)).setText("");
        ((EditText) view.findViewById(R.id.editTextAuthor)).setText("");
        ((EditText) view.findViewById(R.id.editTextGenre)).setText("");
        ((EditText) view.findViewById(R.id.editTextYear)).setText("");
        ((EditText) view.findViewById(R.id.editTextCharacters)).setText("");
        ((EditText) view.findViewById(R.id.editTextDescription)).setText("");
        bookImageUrl = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode,data);

        if (requestCode == IMAGE_PICK_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            uploadImage();
        }
    }

    private void uploadImage() {
        if (filePath != null) {
            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Učitavam sliku");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Slika je učitana na server", Toast.LENGTH_LONG).show();
                            ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    bookImageUrl = task.getResult().toString();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Greška pri učitavanju slike" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void addBookToDatabase(String name, String author, String genre, Long year, String characters, String description, String imageUrl) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference booksRef = database.getReference("books");

        String bookId = booksRef.push().getKey();
        Book book = new Book(name, author, genre, year, characters, description, imageUrl, new HashMap<>());
        booksRef.child(bookId).setValue(book)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Book added successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add book" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private boolean validateInputs(String name, String author, String genre, String year, String characters, String description) {
        if (name.isEmpty() || author.isEmpty() || genre.isEmpty() || year.isEmpty() || characters.isEmpty() || description.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}