package com.example.localreader.entity;

import org.litepal.crud.LitePalSupport;

/**
 * @author xialijuan
 * @date 2021/1/9
 */
public class BookCatalog extends LitePalSupport {

    private int id;
    private String bookPath;
    private String catalog;
    private long startPosition;

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

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }
}
