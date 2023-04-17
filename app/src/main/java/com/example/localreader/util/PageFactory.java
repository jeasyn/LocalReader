package com.example.localreader.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.localreader.R;
import com.example.localreader.entity.Book;
import com.example.localreader.entity.BookCatalog;
import com.example.localreader.entity.Config;
import com.example.localreader.entity.Page;
import com.example.localreader.listener.PageListener;
import com.example.localreader.view.PageView;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.litepal.LitePal;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by xialijuan on 25/12/2020.
 */
public class PageFactory {

    private final String TAG = "PageFactory";
    private static PageFactory pageFactory;
    private Context context;
    private Config config;
    /**
     * 页面宽
     */
    private int screenWidth;
    /**
     * 页面高
     */
    private int screenHeight;
    /**
     * 阅读的文字大小
     */
    private float fontSize;
    /**
     * 页面文字与上下边缘的距离（书本高边距）
     */
    private float marginHeight;
    /**
     * 页面文字与左右边缘的距离（书本宽边距）
     */
    private float marginWidth;
    private float measureMarginWidth;
    /**
     * 状态栏距离底部高度
     */
    private float statusMarginBottomHeight;
    /**
     * 行间距
     */
    private float lineSpace;
    /**
     * 文字画笔
     */
    private Paint fontPaint;
    /**
     * 加载画笔
     */
    private Paint loadPaint;
    /**
     * 文字颜色
     */
    private int textColor = Color.rgb(50, 65, 78);
    /**
     * 绘制内容的可见高
     */
    private float visibleHeight;
    /**
     * 绘制内容的可见宽
     */
    private float visibleWidth;
    /**
     * 每页可以显示的行数
     */
    private int lineCount;
    /**
     * 画笔
     */
    private Paint paint;
    /**
     * 字体大小
     */
    private float mFontSize;
    /**
     * 背景图片
     */
    private Bitmap readBg;
    /**
     * 当前是否为第一页
     */
    private boolean firstPage;
    /**
     * 当前是否为最后一页
     */
    private boolean lastPage;
    /**
     * 书本视图
     */
    private PageView pageView;
    /**
     * 书本路径
     */
    private String bookPath = "";
    /**
     * 书本名字
     */
    private Book book;
    private BookUtil bookUtil;
    private int currentCharter = 0;
    private PageListener pageListener;
    private Page currentPage;
    private Page cancelPage;
    private static Status status = Status.OPENING;
    private String progress;
    private long firstIndex;
    private Activity activity;

    public enum Status {
        /**
         * 正在打开图书中
         */
        OPENING,
        /**
         * 打开图书完成
         */
        FINISH,
        /**
         * 打开图书失败
         */
        FAIL,
    }

    public PageFactory(Context context) {
        bookUtil = new BookUtil(context);
        this.context = context.getApplicationContext();
        config = Config.getInstance();
        //获取屏幕宽高
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        marginWidth = this.context.getResources().getDimension(R.dimen.read_margin_width);
        marginHeight = this.context.getResources().getDimension(R.dimen.read_margin_height);
        statusMarginBottomHeight = this.context.getResources().getDimension(R.dimen.read_status_margin_bottom);
        lineSpace = context.getResources().getDimension(R.dimen.read_line_spacing);
        visibleWidth = screenWidth - marginWidth * 2;
        visibleHeight = screenHeight - marginHeight * 2;

        fontSize = config.getFontSize();
        // 画笔
        fontPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // 左对齐
        fontPaint.setTextAlign(Paint.Align.LEFT);
        // 字体大小
        fontPaint.setTextSize(fontSize);
        // 字体颜色
        fontPaint.setColor(textColor);
        // 设置该项为true，将有助于文本在LCD屏幕上的显示效果
        fontPaint.setSubpixelText(true);

        // 画笔
        loadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // 左对齐
        loadPaint.setTextAlign(Paint.Align.LEFT);
        // 字体大小
        loadPaint.setTextSize(this.context.getResources().getDimension(R.dimen.read_max_text_size));
        // 字体颜色
        loadPaint.setColor(textColor);
        // 设置该项为true，将有助于文本在LCD屏幕上的显示效果
        loadPaint.setSubpixelText(true);
        getLineCount();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFontSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, context.getResources().getDisplayMetrics());
        paint.setTextSize(mFontSize);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(textColor);

        initBg(config.isNight());
        measureMarginWidth();
    }

    /**
     * 设置边距宽度
     */
    private void measureMarginWidth() {
        float wordWidth = fontPaint.measureText("\u3000");
        float width = visibleWidth % wordWidth;
        measureMarginWidth = marginWidth + width / 2;
    }

    /**
     * 初始化背景
     * @param night 是否是夜间模式
     */
    private void initBg(boolean night) {
        if (night) {
            Bitmap bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.BLACK);
            setBgBitmap(bitmap);
            // 设置字体颜色
            setTextColor(Color.rgb(128, 128, 128));
            setBookPageBg(Color.BLACK);
        } else {
            // 设置背景
            setBookBg(config.getBookBg());
        }
    }

    private void getLineCount() {
        // 可显示的行数
        lineCount = (int) (visibleHeight / (fontSize + lineSpace));
    }

    /**
     * 加载图书状态
     * @param bitmap 当前背景颜色
     */
    private void drawStatus(Bitmap bitmap) {
        String status = "";
        if (PageFactory.status == Status.OPENING) {
            status = context.getResources().getString(R.string.read_loading);
        }
        Canvas c = new Canvas(bitmap);
        c.drawBitmap(getBgBitmap(), 0, 0, null);
        loadPaint.setColor(getTextColor());
        loadPaint.setTextAlign(Paint.Align.CENTER);

        Rect targetRect = new Rect(0, 0, screenWidth, screenHeight);
        Paint.FontMetricsInt fontMetrics = loadPaint.getFontMetricsInt();

        int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        // 下面这行是实现水平居中，drawText对应改为传入targetRect.centerX()
        loadPaint.setTextAlign(Paint.Align.CENTER);
        c.drawText(status, targetRect.centerX(), baseline, loadPaint);
        pageView.postInvalidate();
    }

    private void onDraw(Bitmap bitmap, List<String> lines, Boolean updateCharter) {
        if (getDirectoryList().size() > 0 && updateCharter) {
            currentCharter = getCurrentCharter();
        }
        // 使用线程池更新Book表中的数据
        if (currentPage != null && book != null) {

            ThreadFactory threadFactory = new ThreadFactoryBuilder()
                    .setNameFormat("demo-pool-%d").build();

            ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());

            ContentValues values = new ContentValues();
            poolExecutor.execute(()->{
                values.put("firstIndex", currentPage.getFirstIndex());
                LitePal.update(Book.class, values, book.getId());
                Log.d(TAG,Thread.currentThread().getName()+" "+currentPage.getFirstIndex());
            });
            poolExecutor.shutdown();
        }

        Canvas c = new Canvas(bitmap);
        c.drawBitmap(getBgBitmap(), 0, 0, null);
        fontPaint.setTextSize(getFontSize());
        fontPaint.setColor(getTextColor());
        paint.setColor(getTextColor());
        if (lines.size() == 0) {
            return;
        }else {
            float y = marginHeight;
            for (String strLine : lines) {
                y += fontSize + lineSpace;
                c.drawText(strLine, measureMarginWidth, y, fontPaint);
            }
        }
        // 画进度
        float fPercent = (float) (currentPage.getFirstIndex() * 1.0 / bookUtil.getBookLen());
        if (pageListener != null) {
            pageListener.changeProgress(fPercent);
        }
        // 进度文字
        DecimalFormat df = new DecimalFormat("#0.0");
        progress = df.format(fPercent * 100) + "%";

        c.drawText(progress, screenWidth / 2, screenHeight - statusMarginBottomHeight, paint);
        c.save();
        c.restore();

        //画章
        if (getDirectoryList().size() > 0) {
            String charterName = getDirectoryList().get(currentCharter).getCatalog();
            c.drawText(charterName, 0, statusMarginBottomHeight + mFontSize + 30, paint);
        }
        pageView.postInvalidate();
    }

    /**
     * 向前翻页
     */
    public void upPage() {
        if (currentPage.getFirstIndex() <= 0) {
            if (!firstPage) {
                Toast.makeText(context, context.getResources().getString(R.string.read_to_head), Toast.LENGTH_SHORT).show();
            }
            firstPage = true;
            return;
        } else {
            firstPage = false;
        }
        firstIndex = currentPage.getFirstIndex();
        cancelPage = currentPage;
        onDraw(pageView.getCurPage(), currentPage.getLines(), true);
        currentPage = getPrePage();
        onDraw(pageView.getNextPage(), currentPage.getLines(), true);
    }

    /**
     * 向后翻页
     */
    public void nextPage() {
        if (currentPage.getLastIndex() >= bookUtil.getBookLen()) {
            if (!lastPage) {
                Toast.makeText(context, context.getResources().getString(R.string.read_to_end), Toast.LENGTH_SHORT).show();
            }
            lastPage = true;
            return;
        } else {
            lastPage = false;
        }
        firstIndex = currentPage.getFirstIndex();
        cancelPage = currentPage;
        onDraw(pageView.getCurPage(), currentPage.getLines(), true);
        currentPage = getNextPage();
        onDraw(pageView.getNextPage(), currentPage.getLines(), true);
    }

    /**
     * 取消翻页
     */
    public void cancelPage() {
        currentPage = cancelPage;
    }

    /**
     * 打开书本
     *
     * @throws IOException
     */
    public void openBook(Book book,Activity activity) throws IOException {
        this.activity = activity;
        currentCharter = 0;
        initBg(config.isNight());

        this.book = book;
        bookPath = book.getBookPath();

        status = Status.OPENING;
        drawStatus(pageView.getCurPage());
        drawStatus(pageView.getNextPage());
        readStatus(book.getFirstIndex());
    }

    /**
     * 阅读状态
     * @param firstIndex 当前页面的第一个字的索引
     */
    private void readStatus(long firstIndex){
        try {
            bookUtil.openBook(book);
        } catch (IOException e) {
            setStatus(false,firstIndex);
            e.printStackTrace();
            return;
        }
        setStatus(true,firstIndex);
    }

    /**
     * 设置阅读状态
     * @param status 阅读状态
     * @param firstIndex 当前页面的第一个字的索引
     */
    private void setStatus(boolean status, long firstIndex){
        if (status) {
            PageFactory.status = PageFactory.Status.FINISH;
            currentPage = getPageForBegin(firstIndex);
            if (pageView != null) {
                currentPage(true);
            }
        } else {
            PageFactory.status = PageFactory.Status.FAIL;
            activity.finish();
            Toast.makeText(context, context.getResources().getString(R.string.read_load_fail), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取下一个页面的对象
     * @return 下一页面对象
     */
    private Page getNextPage() {
        bookUtil.setFirstIndex(currentPage.getLastIndex());

        Page page = new Page();
        page.setFirstIndex(currentPage.getLastIndex() + 1);
        page.setLines(getNextLines());
        page.setLastIndex(bookUtil.getFirstIndex());
        return page;
    }

    /**
     * 获取上一个页面的对象
     * @return 上一页面对象
     */
    private Page getPrePage() {
        bookUtil.setFirstIndex(currentPage.getFirstIndex());

        Page page = new Page();
        page.setLastIndex(bookUtil.getFirstIndex() - 1);
        page.setLines(getPreLines());
        page.setFirstIndex(bookUtil.getFirstIndex());
        return page;
    }

    private Page getPageForBegin(long begin) {
        Page page = new Page();
        page.setFirstIndex(begin);

        bookUtil.setFirstIndex(begin - 1);
        page.setLines(getNextLines());
        page.setLastIndex(bookUtil.getFirstIndex());
        return page;
    }

    /**
     * 得到下一行
     * @return
     */
    private List<String> getNextLines() {
        List<String> lines = new ArrayList<>();
        float width = 0;
        String line = "";
        while (bookUtil.next(true) != -1) {
            char word = (char) bookUtil.next(false);
            // 判断是否换行
            if ("\r".equals(word + "") && "\n".equals(((char) bookUtil.next(true)) + "")) {
                bookUtil.next(false);
                if (!line.isEmpty()) {
                    lines.add(line);
                    line = "";
                    width = 0;
                    if (lines.size() == lineCount) {
                        break;
                    }
                }
            } else {
                float widthChar = fontPaint.measureText(word + "");
                width += widthChar;
                if (width > visibleWidth) {
                    width = widthChar;
                    lines.add(line);
                    line = word + "";
                } else {
                    line += word;
                }
            }

            if (lines.size() == lineCount) {
                if (!line.isEmpty()) {
                    bookUtil.setFirstIndex(bookUtil.getFirstIndex() - 1);
                }
                break;
            }
        }

        if (!line.isEmpty() && lines.size() < lineCount) {
            lines.add(line);
        }
        return lines;
    }

    /**
     * 得到上一行
     * @return
     */
    private List<String> getPreLines() {
        List<String> lines = new ArrayList<>();
        float width = 0;
        String line = "";
        char[] par = bookUtil.previousLine();
        while (par != null) {
            List<String> preLines = new ArrayList<>();
            for (int i = 0; i < par.length; i++) {
                char word = par[i];
                float widthChar = fontPaint.measureText(word + "");
                width += widthChar;
                if (width > visibleWidth) {
                    width = widthChar;
                    preLines.add(line);
                    line = word + "";
                } else {
                    line += word;
                }
            }
            if (!line.isEmpty()) {
                preLines.add(line);
            }
            lines.addAll(0, preLines);
            if (lines.size() >= lineCount) {
                break;
            }
            width = 0;
            line = "";
            par = bookUtil.previousLine();
        }

        List<String> reLines = new ArrayList<>();
        int num = 0;
        for (int i = lines.size() - 1; i >= 0; i--) {
            if (reLines.size() < lineCount) {
                reLines.add(0, lines.get(i));
            } else {
                num = num + lines.get(i).length();
            }
        }

        if (num > 0) {
            if (bookUtil.getFirstIndex() > 0) {
                bookUtil.setFirstIndex(bookUtil.getFirstIndex() + num + 2);
            } else {
                bookUtil.setFirstIndex(bookUtil.getFirstIndex() + num);
            }
        }
        return reLines;
    }

    /**
     * 上一章
     */
    public void preChapter() {
        if (bookUtil.getBookCatalogList().size() > 0) {
            int num = currentCharter;
            if (num == 0) {
                num = getCurrentCharter();
            }
            num--;
            if (num >= 0) {
                long begin = bookUtil.getBookCatalogList().get(num).getFirstIndex();
                currentPage = getPageForBegin(begin);
                currentPage(true);
                currentCharter = num;
            }
        }
    }

    /**
     * 下一章
     */
    public void nextChapter() {
        int num = currentCharter;
        if (num == 0) {
            num = getCurrentCharter();
        }
        num++;
        if (num < getDirectoryList().size()) {
            long begin = getDirectoryList().get(num).getFirstIndex();
            currentPage = getPageForBegin(begin);
            currentPage(true);
            currentCharter = num;
        }
    }

    /**
     * 获取现在的章
     * @return
     */
    public int getCurrentCharter() {
        int num = 0;
        for (int i = 0; getDirectoryList().size() > i; i++) {
            BookCatalog bookCatalogue = getDirectoryList().get(i);
            if (currentPage.getLastIndex() >= bookCatalogue.getFirstIndex()) {
                num = i;
            } else {
                break;
            }
        }
        return num;
    }

    /**
     * 绘制当前页面
     * @param updateChapter 是否更新章节
     */
    private void currentPage(Boolean updateChapter) {
        onDraw(pageView.getCurPage(), currentPage.getLines(), updateChapter);
        onDraw(pageView.getNextPage(), currentPage.getLines(), updateChapter);
    }

    public void changeProgress(float progress) {
        long begin = (long) (bookUtil.getBookLen() * progress);
        currentPage = getPageForBegin(begin);
        currentPage(true);
    }

    /**
     * 改变章节进度
     * @param firstIndex 当前页面的第一个字的索引
     */
    public void changeChapter(long firstIndex) {
        currentPage = getPageForBegin(firstIndex);
        currentPage(true);
    }

    /**
     * 改变亮度
     * @param activity 当前页面的activity
     * @param brightness 亮度
     */
    public void changeBrightness(Activity activity, float brightness) {
        WindowManager.LayoutParams attributes = activity.getWindow().getAttributes();
        attributes.screenBrightness = brightness;
        activity.getWindow().setAttributes(attributes);
    }

    /**
     * 获取亮度
     * @param activity 当前页面的activity
     * @return 亮度
     */
    public int getBrightness(Activity activity) {
        int brightness = 0;
        ContentResolver cr = activity.getContentResolver();
        try {
            brightness = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
            Settings.System.getFloat(cr, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return brightness;
    }

    /**
     * 改变字体大小
     * @param fontSize 新的字体大小
     */
    public void changeFontSize(int fontSize) {
        this.fontSize = fontSize;
        fontPaint.setTextSize(this.fontSize);
        getLineCount();
        measureMarginWidth();
        currentPage = getPageForBegin(currentPage.getFirstIndex());
        currentPage(true);
    }

    /**
     * 改变背景
     *
     * @param bg 背景
     */
    public void changeBookBg(int bg) {
        setBookBg(bg);
        currentPage(false);
    }

    /**
     * 设置读书页面的背景和字的颜色
     *
     * @param bg 背景颜色
     */
    private void setBookBg(int bg) {
        Bitmap bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        int color = 0;
        switch (bg) {
            case Config.BOOK_BG_WHITE:
                canvas.drawColor(ContextCompat.getColor(context, R.color.read_bg_white));
                color = ContextCompat.getColor(context, R.color.read_font_color_by_white);
                setBookPageBg(ContextCompat.getColor(context, R.color.read_bg_white));
                break;
            case Config.BOOK_BG_YELLOW:
                canvas.drawColor(ContextCompat.getColor(context, R.color.read_bg_yellow));
                color = ContextCompat.getColor(context, R.color.read_font_color_by_yellow);
                setBookPageBg(ContextCompat.getColor(context, R.color.read_bg_yellow));
                break;
            case Config.BOOK_BG_GRAY:
                canvas.drawColor(ContextCompat.getColor(context, R.color.read_bg_gray));
                color = ContextCompat.getColor(context, R.color.read_font_color_by_gray);
                setBookPageBg(ContextCompat.getColor(context, R.color.read_bg_gray));
                break;
            case Config.BOOK_BG_GREEN:
                canvas.drawColor(ContextCompat.getColor(context, R.color.read_bg_green));
                color = ContextCompat.getColor(context, R.color.read_font_color_by_green);
                setBookPageBg(ContextCompat.getColor(context, R.color.read_bg_green));
                break;
            case Config.BOOK_BG_BLUE:
                canvas.drawColor(ContextCompat.getColor(context, R.color.read_bg_blue));
                color = ContextCompat.getColor(context, R.color.read_font_color_by_blue);
                setBookPageBg(ContextCompat.getColor(context, R.color.read_bg_blue));
                break;
            default:
                break;
        }

        setBgBitmap(bitmap);
        // 设置字体颜色
        setTextColor(color);
    }

    /**
     * 设置阅读的背景颜色
     * @param color 背景颜色
     */
    private void setBookPageBg(int color) {
        if (pageView != null) {
            pageView.setBgColor(color);
        }
    }

    /**
     * 设置日间或者夜间模式
     *
     * @param night 是否为夜间模式
     */
    public void setDayOrNight(boolean night) {
        initBg(night);
        currentPage(false);
    }

    public static synchronized PageFactory getInstance() {
        return pageFactory;
    }

    public static synchronized void createPageFactory(Context context) {
        if (pageFactory == null) {
            pageFactory = new PageFactory(context);
        }
    }

    public void initData() {
        currentCharter = 0;
        bookPath = "";
        book = null;
        pageView = null;
        pageListener = null;
        cancelPage = null;
        currentPage = null;
    }

    public static Status getStatus() {
        return status;
    }

    public long getBookLen() {
        return bookUtil.getBookLen();
    }

    public Page getCurrentPage() {
        return currentPage;
    }

    public List<BookCatalog> getDirectoryList() {
        return bookUtil.getBookCatalogList();
    }

    public String getBookPath() {
        return bookPath;
    }

    public boolean isFirstPage() {
        return firstPage;
    }

    public boolean isLastPage() {
        return lastPage;
    }

    public void setBgBitmap(Bitmap bitmap) {
        readBg = bitmap;
    }

    public Bitmap getBgBitmap() {
        return readBg;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getTextColor() {
        return this.textColor;
    }

    public float getFontSize() {
        return this.fontSize;
    }

    public void setPageWidget(PageView mBookPageView) {
        this.pageView = mBookPageView;
    }

    public long getFirstIndex() {
        return firstIndex;
    }

    public String getProgress() {
        return progress;
    }

    public void setPageListener(PageListener pageListener) {
        this.pageListener = pageListener;
    }
}