package com.example.localreader.entity;

/**
 * @author xialijuan
 * @date 2020/12/30
 */
public class BookCatalog {

    private int id;
    private String bookPath;
    /**
     * 章节名称
     */
    private String catalog;
    /**
     * 当前读的页面的第一个字的索引，包含空格和其他字符
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
