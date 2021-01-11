package com.example.localreader.util;

import com.example.localreader.entity.Book;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Create by xlj on 2020/11/07
 */
public class BookShelfUtil {

    /**
     * 添加书架上的图书
     *
     * @param book
     * @return
     */
    public static boolean addBook(Book book) {
        return book.save();
    }

    /**
     * 将集合中的txt文件添加到书架上
     *
     * @param files
     */
    public static void importBooks(List<File> files) {
        if (files.size() != 0) {
            for (File selectFile : files) {
                Book book = new Book();
                book.setBookName(selectFile.getName());
                book.setBookPath(selectFile.getPath());
                book.setBegin(0);//默认没打开书
                BookShelfUtil.addBook(book);
            }
        }
    }

    /**
     * 删除所选图书集合
     *
     * @param selectBook
     * @return
     */
    public static List<Book> deleteBooks(List<Book> selectBook) {
        List<Book> books = queryBooks();
        for (Book book : selectBook) {
            for (Book book1 : books) {
                if (book.getId() == book1.getId()) {
                    books.remove(book1);
                    LitePal.delete(Book.class, book1.getId());
                    break;//执行remove方法后必须终止当前循环，否则报ConcurrentModificationException
                }
            }
        }
        return books;
    }

    /**
     * 查询数据库中的所有图书
     *
     * @return
     */
    public static List<Book> queryBooks() {
        List<Book> books = LitePal.findAll(Book.class);
        return books;
    }

    /**
     * 通过id查询数据库的图书
     *
     * @param id
     * @return
     */
    public static Book queryBookById(int id) {
        List<Book> books = queryBooks();
        for (Book book : books) {
            if (book.getId() == id) {
                return book;
            }
        }
        return null;
    }

    /**
     * 查询书架上所有图书的名称
     *
     * @return
     */
    public static List<String> getBookShelfName() {
        List<Book> books = queryBooks();
        List<String> bookNames = new ArrayList<>();
        for (Book book : books) {
            bookNames.add(book.getBookName());
        }
        return bookNames;
    }
}
