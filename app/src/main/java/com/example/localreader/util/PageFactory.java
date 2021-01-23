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
import android.os.AsyncTask;
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
import com.example.localreader.view.PageWidget;

import org.litepal.LitePal;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xialijuan
 * @date 2020/11/21
 */
public class PageFactory {
    private static final String TAG = "PageFactory";
    private static PageFactory pageFactory;

    private Context context;
    private Config config;
    /**
     * 页面宽
     */
    private int width;
    /**
     * 页面高
     */
    private int height;
    /**
     * 文字大小
     */
    private float fontSize;
    private DecimalFormat df;
    /**
     * 上下与边缘的距离
     */
    private float marginHeight;
    /**
     * 左右与边缘的距离
     */
    private float measureMarginWidth;
    /**
     * 左右与边缘的距离
     */
    private float marginWidth;
    /**
     * 状态栏距离底部高度
     */
    private float statusMarginBottom;
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
    private Paint waitPaint;
    /**
     * 文字颜色
     */
    private int textColor = Color.rgb(50, 65, 78);
    /**
     * 绘制内容的高
     */
    private float visibleHeight;
    /**
     * 绘制内容的宽
     */
    private float visibleWidth;
    /**
     * 每页可以显示的行数
     */
    private int lineCount;
    /**
     * 电池画笔
     */
    private Paint paint;
    /**
     * 电池字体大小
     */
    private float mBatterryFontSize;
    /**
     * 背景图片
     */
    private Bitmap readBg = null;
    /**
     * 当前是否为第一页
     */
    private boolean isFirstPage;
    /**
     * 当前是否为最后一页
     */
    private boolean isLastPage;
    /**
     * 书本widget
     */
    private PageWidget bookPageWidget;
    /**
     * 书本路径
     */
    private String bookPath = "";
    /**
     * 书本名字
     */
    private String bookName = "";
    private Book book;
    private BookUtil bookUtil;
    private int currentCharter = 0;
    private PageEvent pageEvent;
    private Page currentPage;
    private Page prePage;
    private Page cancelPage;
    private BookTask bookTask;
    ContentValues values = new ContentValues();
    private static Status status = Status.OPENING;
    private String progress;
    private long position;

    public enum Status {
        OPENING,
        FINISH,
        FAIL,
    }

    public static synchronized PageFactory getInstance() {
        return pageFactory;
    }

    public static synchronized PageFactory createPageFactory(Context context) {
        if (pageFactory == null) {
            pageFactory = new PageFactory(context);
        }
        return pageFactory;
    }

    private PageFactory(Context context) {
        bookUtil = new BookUtil(context);
        this.context = context.getApplicationContext();
        config = Config.getInstance();
        //获取屏幕宽高
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
        df = new DecimalFormat("#0.0");
        marginWidth = this.context.getResources().getDimension(R.dimen.read_margin_width);
        marginHeight = this.context.getResources().getDimension(R.dimen.read_margin_height);
        statusMarginBottom = this.context.getResources().getDimension(R.dimen.read_status_margin_bottom);
        lineSpace = context.getResources().getDimension(R.dimen.read_line_spacing);
        visibleWidth = width - marginWidth * 2;
        visibleHeight = height - marginHeight * 2;

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
        waitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // 左对齐
        waitPaint.setTextAlign(Paint.Align.LEFT);
        // 字体大小
        waitPaint.setTextSize(this.context.getResources().getDimension(R.dimen.read_max_text_size));
        // 字体颜色
        waitPaint.setColor(textColor);
        // 设置该项为true，将有助于文本在LCD屏幕上的显示效果
        waitPaint.setSubpixelText(true);
        calculateLineCount();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBatterryFontSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, context.getResources().getDisplayMetrics());
        paint.setTextSize(mBatterryFontSize);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(textColor);

        initBg(config.isNight());
        measureMarginWidth();
    }

    private void measureMarginWidth() {
        float wordWidth = fontPaint.measureText("\u3000");
        float width = visibleWidth % wordWidth;
        measureMarginWidth = marginWidth + width / 2;
    }

    /**
     * 初始化背景
     *
     * @param isNight
     */
    private void initBg(Boolean isNight) {
        if (isNight) {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
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

    private void calculateLineCount() {
        // 可显示的行数
        lineCount = (int) (visibleHeight / (fontSize + lineSpace));
    }

    private void drawStatus(Bitmap bitmap) {
        String status = "";
        switch (PageFactory.status) {
            case OPENING:
                status = context.getResources().getString(R.string.read_loading);
                break;
            case FAIL:
                status = context.getResources().getString(R.string.read_load_fail);
                break;
            default:
                break;
        }

        Canvas c = new Canvas(bitmap);
        c.drawBitmap(getBgBitmap(), 0, 0, null);
        waitPaint.setColor(getTextColor());
        waitPaint.setTextAlign(Paint.Align.CENTER);

        Rect targetRect = new Rect(0, 0, width, height);
        Paint.FontMetricsInt fontMetrics = waitPaint.getFontMetricsInt();

        int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        // 下面这行是实现水平居中，drawText对应改为传入targetRect.centerX()
        waitPaint.setTextAlign(Paint.Align.CENTER);
        c.drawText(status, targetRect.centerX(), baseline, waitPaint);
        bookPageWidget.postInvalidate();
    }

    public void onDraw(Bitmap bitmap, List<String> lines, Boolean updateCharter) {
        if (getDirectoryList().size() > 0 && updateCharter) {
            currentCharter = getCurrentCharter();
        }
        // 更新Book数据库中的数据
        if (currentPage != null && book != null) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    values.put("position", currentPage.getPosition());
                    LitePal.update(Book.class, values, book.getId());
                }
            }.start();
        }

        Canvas c = new Canvas(bitmap);
        c.drawBitmap(getBgBitmap(), 0, 0, null);
        fontPaint.setTextSize(getFontSize());
        fontPaint.setColor(getTextColor());
        paint.setColor(getTextColor());
        if (lines.size() == 0) {
            return;
        }

        if (lines.size() > 0) {
            float y = marginHeight;
            for (String strLine : lines) {
                y += fontSize + lineSpace;
                c.drawText(strLine, measureMarginWidth, y, fontPaint);
            }
        }

        // 画进度
        float fPercent = (float) (currentPage.getPosition() * 1.0 / bookUtil.getBookLen());
        if (pageEvent != null) {
            pageEvent.changeProgress(fPercent);
        }
        // 进度文字
        progress = df.format(fPercent * 100) + "%";

        c.drawText(progress, width / 2, height - statusMarginBottom, paint);
        // save()无参传入这两个方法最终都调用native_save方法，而无参方法save()默认是保存Matrix和Clip这两个信息。
        // 如果允许，那么尽量使用无参的save()方法，而不是使用有参的save(int saveFlags)方法传入别的Flag。
        c.save();

        c.restore();

        //画章
        if (getDirectoryList().size() > 0) {
            String charterName = getDirectoryList().get(currentCharter).getCatalog();
            c.drawText(charterName, 0, statusMarginBottom + mBatterryFontSize + 30, paint);
        }

        bookPageWidget.postInvalidate();
    }

    /**
     * 向前翻页
     */
    public void upPage() {
        Log.d("position", pageFactory.getPosition() + " ");
        if (currentPage.getPosition() <= 0) {
            if (!isFirstPage) {
                Toast.makeText(context, context.getResources().getString(R.string.read_to_head), Toast.LENGTH_SHORT).show();
            }
            isFirstPage = true;
            return;
        } else {
            isFirstPage = false;
        }
        position = currentPage.getPosition();
        cancelPage = currentPage;
        onDraw(bookPageWidget.getCurPage(), currentPage.getLines(), true);
        currentPage = getPrePage();
        onDraw(bookPageWidget.getNextPage(), currentPage.getLines(), true);
    }

    /**
     * 向后翻页
     */
    public void nextPage() {
        Log.d("position", pageFactory.getPosition() + " ");
        if (currentPage.getEnd() >= bookUtil.getBookLen()) {
            if (!isLastPage) {
                Toast.makeText(context, context.getResources().getString(R.string.read_to_end), Toast.LENGTH_SHORT).show();
            }
            isLastPage = true;
            return;
        } else {
            isLastPage = false;
        }
        position = currentPage.getPosition();
        cancelPage = currentPage;
        onDraw(bookPageWidget.getCurPage(), currentPage.getLines(), true);
        prePage = currentPage;
        currentPage = getNextPage();
        onDraw(bookPageWidget.getNextPage(), currentPage.getLines(), true);
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
    public void openBook(Book book) throws IOException {
        currentCharter = 0;
        initBg(config.isNight());

        this.book = book;
        bookPath = book.getBookPath();
        bookName = book.getBookName().split(".txt")[0];

        status = Status.OPENING;
        drawStatus(bookPageWidget.getCurPage());
        drawStatus(bookPageWidget.getNextPage());
        if (bookTask != null && bookTask.getStatus() != AsyncTask.Status.FINISHED) {
            bookTask.cancel(true);
        }
        bookTask = new BookTask();
        bookTask.execute(book.getPosition());
    }

    private class BookTask extends AsyncTask<Long, Void, Boolean> {
        private long begin = 0;

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (isCancelled()) {
                return;
            }
            if (result) {
                PageFactory.status = PageFactory.Status.FINISH;
                currentPage = getPageForBegin(begin);
                if (bookPageWidget != null) {
                    currentPage(true);
                }
            } else {
                PageFactory.status = PageFactory.Status.FAIL;
                drawStatus(bookPageWidget.getCurPage());
                drawStatus(bookPageWidget.getNextPage());
                Toast.makeText(context, context.getResources().getString(R.string.read_load_fail), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Boolean doInBackground(Long... params) {
            begin = params[0];
            try {
                bookUtil.openBook(book);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    public Page getNextPage() {
        bookUtil.setPosition(currentPage.getEnd());

        Page page = new Page();
        page.setPosition(currentPage.getEnd() + 1);
        page.setLines(getNextLines());
        page.setEnd(bookUtil.getPosition());
        return page;
    }

    public Page getPrePage() {
        bookUtil.setPosition(currentPage.getPosition());

        Page page = new Page();
        page.setEnd(bookUtil.getPosition() - 1);
        page.setLines(getPreLines());
        page.setPosition(bookUtil.getPosition());
        return page;
    }

    public Page getPageForBegin(long begin) {
        Page page = new Page();
        page.setPosition(begin);

        bookUtil.setPosition(begin - 1);
        page.setLines(getNextLines());
        page.setEnd(bookUtil.getPosition());
        return page;
    }

    public List<String> getNextLines() {
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
                    bookUtil.setPosition(bookUtil.getPosition() - 1);
                }
                break;
            }
        }

        if (!line.isEmpty() && lines.size() < lineCount) {
            lines.add(line);
        }
        return lines;
    }

    public List<String> getPreLines() {
        List<String> lines = new ArrayList<>();
        float width = 0;
        String line = "";
        char[] par = bookUtil.preLine();
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
            par = bookUtil.preLine();
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
            if (bookUtil.getPosition() > 0) {
                bookUtil.setPosition(bookUtil.getPosition() + num + 2);
            } else {
                bookUtil.setPosition(bookUtil.getPosition() + num);
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
                long begin = bookUtil.getBookCatalogList().get(num).getPosition();
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
            long begin = getDirectoryList().get(num).getPosition();
            currentPage = getPageForBegin(begin);
            currentPage(true);
            currentCharter = num;
        }
    }

    /**
     * 获取现在的章
     *
     * @return
     */
    public int getCurrentCharter() {
        int num = 0;
        for (int i = 0; getDirectoryList().size() > i; i++) {
            BookCatalog bookCatalogue = getDirectoryList().get(i);
            if (currentPage.getEnd() >= bookCatalogue.getPosition()) {
                num = i;
            } else {
                break;
            }
        }
        return num;
    }

    /**
     * 绘制当前页面
     *
     * @param updateChapter
     */
    public void currentPage(Boolean updateChapter) {
        onDraw(bookPageWidget.getCurPage(), currentPage.getLines(), updateChapter);
        onDraw(bookPageWidget.getNextPage(), currentPage.getLines(), updateChapter);
    }

    public void changeProgress(float progress) {
        long begin = (long) (bookUtil.getBookLen() * progress);
        currentPage = getPageForBegin(begin);
        currentPage(true);
    }

    /**
     * 改变章节进度
     *
     * @param begin
     */
    public void changeChapter(long begin) {
        currentPage = getPageForBegin(begin);
        currentPage(true);
    }

    /**
     * 改变亮度
     *
     * @param activity
     * @param brightness
     */
    public void changeBrightness(Activity activity, float brightness) {
        WindowManager.LayoutParams attributes = activity.getWindow().getAttributes();
        attributes.screenBrightness = brightness;
        activity.getWindow().setAttributes(attributes);
    }

    /**
     * 获取系统亮度
     *
     * @param activity
     * @return
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
     *
     * @param fontSize
     */
    public void changeFontSize(int fontSize) {
        this.fontSize = fontSize;
        fontPaint.setTextSize(this.fontSize);
        calculateLineCount();
        measureMarginWidth();
        currentPage = getPageForBegin(currentPage.getPosition());
        currentPage(true);
    }

    /**
     * 改变背景
     *
     * @param type
     */
    public void changeBookBg(int type) {
        setBookBg(type);
        currentPage(false);
    }

    /**
     * 设置读书页面的背景和字的颜色
     *
     * @param type
     */
    public void setBookBg(int type) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        int color = 0;
        switch (type) {
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

    public void setBookPageBg(int color) {
        if (bookPageWidget != null) {
            bookPageWidget.setBgColor(color);
        }
    }

    /**
     * 设置日间或者夜间模式
     *
     * @param isNight
     */
    public void setDayOrNight(Boolean isNight) {
        initBg(isNight);
        currentPage(false);
    }

    public void initData() {
        currentCharter = 0;
        bookPath = "";
        bookName = "";
        book = null;
        bookPageWidget = null;
        pageEvent = null;
        cancelPage = null;
        prePage = null;
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

    /**
     * 获取书本的章
     *
     * @return
     */
    public List<BookCatalog> getDirectoryList() {
        return bookUtil.getBookCatalogList();
    }

    public String getBookPath() {
        return bookPath;
    }

    public boolean isFirstPage() {
        return isFirstPage;
    }

    public boolean isLastPage() {
        return isLastPage;
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

    public void setPageWidget(PageWidget mBookPageWidget) {
        this.bookPageWidget = mBookPageWidget;
    }

    public long getPosition() {
        return position;
    }

    public String getProgress() {
        return progress;
    }

    public void setPageEvent(PageEvent pageEvent) {
        this.pageEvent = pageEvent;
    }

    public interface PageEvent {
        /**
         * 读书进度监听
         *
         * @param progress
         */
        void changeProgress(float progress);
    }
}