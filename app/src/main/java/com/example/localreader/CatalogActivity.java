package com.example.localreader;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.localreader.adapter.MyPagerAdapter;
import com.example.localreader.entity.Book;
import com.google.android.material.tabs.TabLayout;

/**
 * Created by xialijuan on 30/12/2020.
 */
public class CatalogActivity extends AppCompatActivity {

    private Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        book = (Book) getIntent().getSerializableExtra("book_data");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(book.getBookName().split(".txt")[0]);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_title_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        initView();
    }

    private void initView() {
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager viewPager = findViewById(R.id.viewPager);

        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager(),0,book.getBookPath());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager, true);
    }
}
