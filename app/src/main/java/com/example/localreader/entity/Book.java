package com.example.localreader.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * @author xialijuan
 * @date 2020/12/05
 */
public class Book extends LitePalSupport implements Serializable {

    private int id;
    private String bookName;
    private String bookPath;
    /**
     * 文件编码
     */
    private String charset;
    /**
     * 当前读的页面的第一个字的索引，包含空格和其他字符
     */
    private long position;
    /**
     * 读书进度（已格式化）
     */
    private String progress;
    /**
     * 图书封面
     */
    private int bookBg;

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
