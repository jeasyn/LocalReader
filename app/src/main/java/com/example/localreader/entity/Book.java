package com.example.localreader.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * @author xialijuan
 * @date 2020/11/07
 */
public class Book extends LitePalSupport implements Serializable {

    private int id;
    private String bookName;
    private String bookPath;
    private String charset;//文件编码
    private long begin;
    private int bookBg;//图书封面

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookPath() {
        return bookPath;
    }

    public void setBookPath(String bookPath) {
        this.bookPath = bookPath;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public long getBegin() {
        return begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public int getBookBg() {
        return bookBg;
    }

    public void setBookBg(int bookBg) {
        this.bookBg = bookBg;
    }
}
