package ba.sum.fpmoz.libraryapp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import ba.sum.fpmoz.libraryapp.fragments.*;


public class BooksViewPagerAdapter extends FragmentStateAdapter {

    public BooksViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new BookFragment();
            case 1:
                return new AddBookDialogFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}