package com.example.localreader.util;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;

import com.example.localreader.entity.Book;
import com.example.localreader.entity.BookCatalog;
import com.example.localreader.entity.Bookmark;
import com.example.localreader.entity.Cache;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xialijuan
 * @date 2021/01/11
 */
public class BookUtil {

    /**
     * 将缓存的文件存储的位置
     */
    private File cachedFile;
    private Book book;
    /**
     * 缓存的字符数
     */
    private static final int CACHED_SIZE = 30000;

    private final ArrayList<Cache> caches = new ArrayList<>();
    /**
     * 目录
     */
    private List<BookCatalog> bookCatalogList = new ArrayList<>();

    private String bookPath;
    private long bookLen;
    private long firstIndex;

    public BookUtil(Context context) {
        cachedFile = context.getExternalFilesDir(null);
        if (!cachedFile.exists()) {
            cachedFile.mkdir();
        }
    }

    public synchronized void openBook(Book book) throws IOException {
        this.book = book;
        // 如果当前缓存不是要打开的书本就缓存书本同时删除缓存
        if (bookPath == null || !bookPath.equals(book.getBookPath())) {
            cleanFileCache();
            this.bookPath = book.getBookPath();
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
        if (!cachedFile.exists()) {
            cachedFile.mkdir();
        } else {
            File[] files = cachedFile.listFiles();
            for (File temp : files) {
                temp.delete();
            }
        }
    }

    /**
     * 读上一页的一行
     * @return
     */
    public char[] previousLine() {
        if (firstIndex <= 0) {
            return null;
        }
        StringBuilder line = new StringBuilder();
        while (firstIndex >= 0) {
            int word = previous(false);
            if (word == -1) {
                break;
            }
            char wordChar = (char) word;
            if ("\n".equals(wordChar + "") && "\r".equals(((char) previous(true)) + "")) {
                previous(false);
                break;
            }
            line.insert(0,wordChar);
        }
        return line.toString().toCharArray();
    }

    private char current() {
        int cachePos = 0;
        int pos = 0;
        int len = 0;
        for (int i = 0; i < caches.size(); i++) {
            long size = caches.get(i).getSize();
            if (size + len - 1 >= firstIndex) {
                cachePos = i;
                pos = (int) (firstIndex - len);
                break;
            }
            len += size;
        }

        char[] charArray = block(cachePos);
        return charArray[pos];
    }

    private int previous(boolean back) {
        firstIndex -= 1;
        if (firstIndex < 0) {
            firstIndex = 0;
            return -1;
        }
        char result = current();
        if (back) {
            firstIndex += 1;
        }
        return result;
    }

    public int next(boolean back) {
        firstIndex += 1;
        if (firstIndex > bookLen) {
            firstIndex = bookLen;
            return -1;
        }
        char result = current();
        if (back) {
            firstIndex -= 1;
        }
        return result;
    }

    /**
     * 缓存书本
     *
     * @throws IOException
     */
    private void cacheBook() throws IOException {
        String strCharsetName;
        if (TextUtils.isEmpty(book.getCharset())) {
            strCharsetName = FileUtil.getCharset(bookPath);
            if (strCharsetName == null) {
                strCharsetName = "utf-8";
            }
            ContentValues values = new ContentValues();
            values.put("charset", strCharsetName);
            LitePal.update(Book.class, values, book.getId());
        } else {
            strCharsetName = book.getCharset();
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

        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("demo-pool-%d").build();

        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());

        poolExecutor.execute(()->getChapter());
        poolExecutor.shutdown();
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
                        bookCatalog.setFirstIndex(size);
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
                e.printStackTrace();
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

    public long getFirstIndex() {
        return firstIndex;
    }

    public void setFirstIndex(long firstIndex) {
        this.firstIndex = firstIndex;
    }

    public List<BookCatalog> getBookCatalogList() {
        return bookCatalogList;
    }

    public long getBookLen() {
        return bookLen;
    }

    private String fileName(int index) {
        return cachedFile.getPath() + index;
    }
}
