package com.example.localreader.util;

import android.content.ContentValues;
import android.os.Environment;
import android.text.TextUtils;

import com.example.localreader.entity.Book;
import com.example.localreader.entity.BookCatalog;
import com.example.localreader.entity.Bookmark;
import com.example.localreader.entity.Cache;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xialijuan
 * @date 2021/01/11
 */
public class BookUtil {

    private static final String CACHED_PATH = Environment.getExternalStorageDirectory() + "/LocalReader/";
    /**
     * 存储的字符数
     */
    private static final int CACHED_SIZE = 30000;

    private final ArrayList<Cache> caches = new ArrayList<>();
    /**
     * 目录
     */
    private List<BookCatalog> bookCatalogList = new ArrayList<>();

    private String bookName;
    private String bookPath;
    private long bookLen;
    private long position;
    private Book mBook;
//    private final File CACHED_PATH = context.getExternalFilesDir(null);

    public BookUtil() {
        // 路径/storage/emulated/0/LocalReader/
        File file = new File(CACHED_PATH);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public synchronized void openBook(Book book) throws IOException {
        this.mBook = book;
        // 如果当前缓存不是要打开的书本就缓存书本同时删除缓存
        if (bookPath == null || !bookPath.equals(book.getBookPath())) {
            cleanFileCache();
            this.bookPath = book.getBookPath();
            bookName = book.getBookName().split(".txt")[0];
            cacheBook();
        }
    }

    /**
     * 删除书签
     *
     * @param selectBooks 选中的图书集合
     */
    public static void deleteBookmarks(List<Book> selectBooks) {
        for (Book selectBook : selectBooks) {
            List<Bookmark> temp = LitePal.where("bookPath = ?", selectBook.getBookPath()).find(Bookmark.class);
            for (Bookmark bookmark : temp) {
                LitePal.delete(Bookmark.class, bookmark.getId());
            }
        }
    }

    /**
     * 清除文件缓存
     */
    private void cleanFileCache() {
        File file = new File(CACHED_PATH);
        if (!file.exists()) {
            file.mkdir();
        } else {
            File[] files = file.listFiles();
            for (File temp : files) {
                temp.delete();
            }
        }
    }

    public int next(boolean back) {
        position += 1;
        if (position > bookLen) {
            position = bookLen;
            return -1;
        }
        char result = current();
        if (back) {
            position -= 1;
        }
        return result;
    }

    public char[] preLine() {
        if (position <= 0) {
            return null;
        }
        StringBuilder line = new StringBuilder();
        while (position >= 0) {
            int word = pre(false);
            if (word == -1) {
                break;
            }
            char wordChar = (char) word;
            if ("\n".equals(wordChar + "") && "\r".equals(((char) pre(true)) + "")) {
                pre(false);
                break;
            }
            line.append(wordChar);
        }
        return line.toString().toCharArray();
    }

    private char current() {
        int cachePos = 0;
        int pos = 0;
        int len = 0;
        for (int i = 0; i < caches.size(); i++) {
            long size = caches.get(i).getSize();
            if (size + len - 1 >= position) {
                cachePos = i;
                pos = (int) (position - len);
                break;
            }
            len += size;
        }

        char[] charArray = block(cachePos);
        return charArray[pos];
    }

    private int pre(boolean back) {
        position -= 1;
        if (position < 0) {
            position = 0;
            return -1;
        }
        char result = current();
        if (back) {
            position += 1;
        }
        return result;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    /**
     * 缓存书本
     *
     * @throws IOException 抛出io异常
     */
    private void cacheBook() throws IOException {
        String strCharsetName;
        if (TextUtils.isEmpty(mBook.getCharset())) {
            strCharsetName = FileUtil.getCharset(bookPath);
            if (strCharsetName == null) {
                strCharsetName = "utf-8";
            }
            ContentValues values = new ContentValues();
            values.put("charset", strCharsetName);
            LitePal.update(Book.class, values, mBook.getId());
        } else {
            strCharsetName = mBook.getCharset();
        }

        File file = new File(bookPath);
        InputStreamReader reader = new InputStreamReader(new FileInputStream(file), strCharsetName);
        int index = 0;
        bookLen = 0;
        bookCatalogList.clear();
        caches.clear();
        while (true) {
            char[] buf = new char[CACHED_SIZE];
            int result = reader.read(buf);
            if (result == -1) {
                reader.close();
                break;
            }

            String bufStr = new String(buf);
            bufStr = bufStr.replaceAll("\r\n+\\s*", "\r\n\u3000\u3000");
            bufStr = bufStr.replaceAll("\u0000", "");
            buf = bufStr.toCharArray();
            bookLen += buf.length;

            Cache cache = new Cache();
            cache.setSize(buf.length);
            cache.setData(new WeakReference<>(buf));

            caches.add(cache);
            try {
                File cacheBook = new File(fileName(index));
                if (!cacheBook.exists()) {
                    cacheBook.createNewFile();
                }
                final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileName(index)), "UTF-16LE");
                writer.write(buf);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            index++;
        }

        new Thread() {
            @Override
            public void run() {
                getChapter();
            }
        }.start();

    }

    /**
     * 获取章节名称
     */
    private synchronized void getChapter() {
        try {
            long size = 0;
            for (int i = 0; i < caches.size(); i++) {
                char[] buf = block(i);
                String bufStr = new String(buf);
                String[] paragraphs = bufStr.split("\r\n");
                for (String str : paragraphs) {
                    boolean success = str.matches(".*第.{1,8}章.*") || str.matches(".*第.{1,8}节.*");
                    if (str.length() <= 30 && success) {
                        BookCatalog bookCatalog = new BookCatalog();
                        bookCatalog.setPosition(size);
                        bookCatalog.setCatalog(str);
                        bookCatalog.setBookPath(bookPath);
                        bookCatalogList.add(bookCatalog);
                    }
                    if (str.contains("\u3000\u3000")) {
                        size += str.length() + 2;
                    } else if (str.contains("\u3000")) {
                        size += str.length() + 1;
                    } else {
                        size += str.length();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<BookCatalog> getBookCatalogList() {
        return bookCatalogList;
    }

    public long getBookLen() {
        return bookLen;
    }

    private String fileName(int index) {
        return CACHED_PATH + bookName + index;
    }

    /**
     * 获取书本缓存
     *
     * @param index 书本缓存索引
     * @return 书本缓存数组
     */
    private char[] block(int index) {
        if (caches.size() == 0) {
            return new char[1];
        }
        char[] block = caches.get(index).getData().get();
        if (block == null) {
            InputStreamReader reader = null;
            try {
                File file = new File(fileName(index));
                int size = (int) file.length();
                block = new char[size / 2];
                reader = new InputStreamReader(new FileInputStream(file), "UTF-16LE");
                if (reader.read(block) != block.length) {
                    throw new RuntimeException("Error during reading " + fileName(index));
                }
            } catch (IOException e) {
                throw new RuntimeException("Error during reading " + fileName(index));
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Cache cache = caches.get(index);
            cache.setData(new WeakReference<>(block));
        }
        return block;
    }
}
