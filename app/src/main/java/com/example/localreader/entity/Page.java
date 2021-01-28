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
    private long firstIndex;
    /**
     * 当前阅读页面结尾的位置
     */
    private long lastIndex;
    private List<String> lines;

    public long getFirstIndex() {
        return firstIndex;
    }

    public void setFirstIndex(long firstIndex) {
        this.firstIndex = firstIndex;
    }

    public long getLastIndex() {
        return lastIndex;
    }

    public void setLastIndex(long lastIndex) {
        this.lastIndex = lastIndex;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }
}