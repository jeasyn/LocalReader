package com.example.localreader.entity;

import java.util.List;

/**
 * @author xialijuan
 * @date 2021/1/9
 */
public class Page {
    private long position;//最后一次读的位置
    private long end;//文件结尾的位置
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