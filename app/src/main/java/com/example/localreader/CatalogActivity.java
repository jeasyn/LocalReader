package com.example.localreader;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.localreader.adapter.MyPagerAdapter;
import com.example.localreader.entity.Book;
import com.google.android.material.tabs.TabLayout;

public class CatalogActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private MyPagerAdapter pagerAdapter;
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
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        initView();
    }

    private void initView() {
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager(),book.getBookPath());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager, true);
    }
}
