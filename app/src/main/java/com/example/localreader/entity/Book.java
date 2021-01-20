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
    private long position;//最后一次读的位置
    private String progress;//读书进度
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

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public int getBookBg() {
        return bookBg;
    }

    public void setBookBg(int bookBg) {
        this.bookBg = bookBg;
    }
}
