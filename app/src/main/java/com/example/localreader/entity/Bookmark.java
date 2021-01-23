package com.example.localreader.entity;

import org.litepal.crud.LitePalSupport;

/**
 * @author xialijuan
 * @date 2021/12/05
 */
public class Bookmark extends LitePalSupport {

    private int id ;
    private String bookPath;
    /**
     * 书签显示的部分文字
     */
    private String partContent;
    /**
     * 书签记录页面最后一次读的位置
     */
    private long position;
    /**
     * 添加书签的时间
     */
    private String time;

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

    public String getPartContent() {
        return partContent;
    }

    public void setPartContent(String partContent) {
        this.partContent = partContent;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
