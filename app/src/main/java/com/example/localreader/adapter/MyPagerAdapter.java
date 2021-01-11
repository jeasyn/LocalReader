package com.example.localreader.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.localreader.fragment.BookmarkFragment;
import com.example.localreader.fragment.CatalogFragment;

/**
 * @author xialijuan
 * @date 2020/12/22
 */
public class MyPagerAdapter extends FragmentPagerAdapter {

    private String[] title = {"目录", "书签"};
    private String bookPath;

    private CatalogFragment catalogueFragment;
    private BookmarkFragment bookMarkFragment;

    public MyPagerAdapter(@NonNull FragmentManager fm, String bookPath) {
        super(fm);
        this.bookPath = bookPath;
    }

    @Override
    public int getCount() {
        return title.length;
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
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return title[position];
    }
}
