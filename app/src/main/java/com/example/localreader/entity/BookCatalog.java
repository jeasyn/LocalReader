package com.example.localreader.entity;

/**
 * @author xialijuan
 * @date 2020/12/15
 */
public class BookCatalog {

    private int id;
    private String bookPath;
    /**
     * 章节名称
     */
    private String catalog;
    /**
     * 最后一次读的位置
     */
    private long position;

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

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }
}
