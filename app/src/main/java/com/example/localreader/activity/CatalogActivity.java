package com.example.localreader.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.localreader.R;
import com.example.localreader.adapter.MyPagerAdapter;
import com.example.localreader.entity.Book;
import com.google.android.material.tabs.TabLayout;

public class CatalogActivity extends AppCompatActivity {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MyPagerAdapter mAdapter;
    private Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        book = (Book) getIntent().getSerializableExtra("book_data");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(book.getBookName().split(".txt")[0]);
        toolbar.setNavigationIcon(R.drawable.ic_title_back);
        toolbar.setNavigationOnClickListener((v)->finish());


        initView();
    }

    private void initView() {


        mTabLayout = findViewById(R.id.tabLayout);
        mViewPager = findViewById(R.id.viewPager);


        mAdapter = new MyPagerAdapter(getSupportFragmentManager(),book.getBookPath());
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager, true);
    }
}
