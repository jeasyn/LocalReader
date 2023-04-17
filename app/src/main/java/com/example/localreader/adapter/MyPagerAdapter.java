package com.example.localreader.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.localreader.fragment.BookmarkFragment;
import com.example.localreader.fragment.CatalogFragment;

/**
 * Created by xialijuan on 30/12/2020.
 */
public class MyPagerAdapter extends FragmentStatePagerAdapter {

    private String[] title = {"目录", "书签"};
    private String bookPath;
    private CatalogFragment catalogueFragment;
    private BookmarkFragment bookMarkFragment;

    public MyPagerAdapter(@NonNull FragmentManager fm, int behavior, String bookPath) {
        super(fm, behavior);
        this.bookPath = bookPath;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (catalogueFragment == null) {
                    catalogueFragment = CatalogFragment.newInstance(bookPath);
                }
                return catalogueFragment;

            case 1:
                if (bookMarkFragment == null) {
                    bookMarkFragment = BookmarkFragment.newInstance(bookPath);
                }
                return bookMarkFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return title.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return title[position];
    }
}
