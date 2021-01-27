package com.example.localreader.entity;

import java.util.List;

/**
 * @author xialijuan
 * @date 2020/12/27
 */
public class Page {
    /**
     * 当前读的页面的第一个字的索引，包含空格和其他字符
     */
    private long position;
    /**
     * 当前阅读页面结尾的位置
     */
    private long end;
    private List<String> lines;

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }
}