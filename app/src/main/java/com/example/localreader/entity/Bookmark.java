package com.example.localreader.entity;

import org.litepal.crud.LitePalSupport;

/**
 * @author xialijuan
 * @date 2020/12/30
 */
public class Bookmark extends LitePalSupport {

    private int id;
    private String bookPath;
    /**
     * 书签显示的部分文字
     */
    private String partContent;
    /**
     * 书签记录当前读的页面的第一个字的索引，包含空格和其他字符
     */
    private long firstIndex;
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

    public long getFirstIndex() {
        return firstIndex;
    }

    public void setFirstIndex(long firstIndex) {
        this.firstIndex = firstIndex;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
