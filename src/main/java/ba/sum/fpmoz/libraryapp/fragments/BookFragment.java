package ba.sum.fpmoz.libraryapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ba.sum.fpmoz.libraryapp.R;
import ba.sum.fpmoz.libraryapp.adapters.BookAdapter;
import ba.sum.fpmoz.libraryapp.models.Book;

public class BookFragment extends Fragment implements BookAdapter.addOnCompleteListener {

    FirebaseDatabase bookDatabase = FirebaseDatabase.getInstance();
    BookAdapter bookAdapter;
    RecyclerView bookRecyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.activity_book, container, false);

        this.bookRecyclerView = view.findViewById(R.id.bookListView);
        this.bookRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext())
        );
        FirebaseRecyclerOptions<Book> options = new FirebaseRecyclerOptions.Builder<Book>().setQuery(
                this.bookDatabase.getReference("books"),
                Book.class
        ).build();

        this.bookAdapter = new BookAdapter(options);
        this.bookRecyclerView.setAdapter(this.bookAdapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.bookAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        this.bookAdapter.stopListening();
    }

    @Override
    public void onRatingChanged(String bookId, float rating) {
        DatabaseReference bookDbRef = FirebaseDatabase.getInstance().getReference("books").child(bookId);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        bookDbRef.child("ratings").child(userId).setValue(rating).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                } else {
                }
            }
        });
    }

}