package com.example.localreader.entity;

import org.litepal.crud.LitePalSupport;

/**
 * @author xialijuan
 * @date 2021/1/9
 */
public class BookCatalog extends LitePalSupport {

    private int id;
    private String bookPath;
    private String bookCatalogue;
    private long bookCatalogueStartPos;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBookPath() {
        return bookPath;
    }

    public void setBookPath(String bookPath) {
        this.bookPath = bookPath;
    }

    public String getBookCatalogue() {
        return bookCatalogue;
    }

    public void setBookCatalogue(String bookCatalogue) {
        this.bookCatalogue = bookCatalogue;
    }

    public long getBookCatalogueStartPos() {
        return bookCatalogueStartPos;
    }

    public void setBookCatalogueStartPos(long bookCatalogueStartPos) {
        this.bookCatalogueStartPos = bookCatalogueStartPos;
    }
}
