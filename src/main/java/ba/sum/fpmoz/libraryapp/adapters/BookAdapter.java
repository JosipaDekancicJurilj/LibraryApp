package ba.sum.fpmoz.libraryapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import ba.sum.fpmoz.libraryapp.R;

import ba.sum.fpmoz.libraryapp.models.Book;

public class BookAdapter extends FirebaseRecyclerAdapter<Book, BookAdapter.BookViewHolder> {

    Context ctx;

    public BookAdapter(@NonNull FirebaseRecyclerOptions<Book> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull BookAdapter.BookViewHolder holder, int position, @NonNull Book model) {
        holder.bookItemName.setText(model.name);
        holder.bookItemAuthor.setText(model.author);
        holder.bookItemGenre.setText(model.genre);
        holder.bookItemYear.setText(model.year.toString());
        holder.bookItemCharacters.setText(model.characters);
        holder.bookItemDescription.setText(model.description);
        Picasso.get().load(model.image).into(holder.bookItemImageView);
        holder.bookRatingBar.setRating(model.getAverageRating());
    }

    @NonNull
    @Override
    public BookAdapter.BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.ctx = parent.getContext();
        View view = LayoutInflater.from(this.ctx).inflate(R.layout.book_item_list_view, parent, false);
        return new BookViewHolder(view);
    }

    public interface addOnCompleteListener {
        void onRatingChanged(String bookId, float rating);
    }

    public class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView bookItemImageView;
        TextView bookItemName, bookItemAuthor, bookItemGenre, bookItemYear, bookItemCharacters, bookItemDescription;
        RatingBar bookRatingBar;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            this.bookItemImageView = itemView.findViewById(R.id.bookItemImageView);
            this.bookItemName = itemView.findViewById(R.id.bookItemName);
            this.bookItemAuthor = itemView.findViewById(R.id.bookItemAuthor);
            this.bookItemGenre = itemView.findViewById(R.id.bookItemGenre);
            this.bookItemYear = itemView.findViewById(R.id.bookItemYear);
            this.bookItemCharacters = itemView.findViewById(R.id.bookItemCharacters);
            this.bookItemDescription = itemView.findViewById(R.id.bookItemDescription);
            this.bookRatingBar = itemView.findViewById(R.id.bookItemRating);

            bookRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    if (fromUser) {
                        String bookId = getRef(getAdapterPosition()).getKey();
                        DatabaseReference bookRef = getRef(getAdapterPosition());
                        bookRef.child("rating").setValue(rating).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Ako je ažuriranje uspješno, možete ovdje izvršiti dodatne radnje
                                } else {
                                    // Ako dođe do greške pri ažuriranju ocjene
                                }
                            }
                        });
                    }
                }
            });
        }
    }
}