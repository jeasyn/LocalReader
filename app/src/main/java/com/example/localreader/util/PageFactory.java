package com.example.localreader.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.Toast;

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
 * @date 2021/1/9
 */
public class PageFactory {
    private static final String TAG = "PageFactory";
    private static PageFactory pageFactory;

    private Context context;
    private Config config;
    // 默认背景颜色
    private int m_backColor = 0xffff9e85;
    //页面宽
    private int mWidth;
    //页面高
    private int mHeight;
    //文字字体大小
    private float m_fontSize;
    //进度格式
    private DecimalFormat df;
    // 上下与边缘的距离
    private float marginHeight;
    // 左右与边缘的距离
    private float measureMarginWidth;
    // 左右与边缘的距离
    private float marginWidth;
    //状态栏距离底部高度
    private float statusMarginBottom;
    //行间距
    private float lineSpace;
    //文字画笔
    private Paint mPaint;
    //加载画笔
    private Paint waitPaint;
    //文字颜色
    private int m_textColor = Color.rgb(50, 65, 78);
    // 绘制内容的宽
    private float mVisibleHeight;
    // 绘制内容的宽
    private float mVisibleWidth;
    // 每页可以显示的行数
    private int mLineCount;
    //电池画笔
    private Paint paint;
    //电池字体大小
    private float mBatterryFontSize;
    //背景图片
    private Bitmap m_book_bg = null;
    //文件编码
//    private String m_strCharsetName = "GBK";
    //当前是否为第一页
    private boolean isFirstPage;
    //当前是否为最后一页
    private boolean isLastPage;
    //书本widget
    private PageWidget mBookPageWidget;
    //书本路径
    private String bookPath = "";
    //书本名字
    private String bookName = "";
    private Book book;
    //书本章节
    private int currentCharter = 0;
    //当前电量
    private BookUtil mBookUtil;
    private PageEvent mPageEvent;
    private Page currentPage;
    private Page prePage;
    private Page cancelPage;
    private BookTask bookTask;
    ContentValues values = new ContentValues();

    private static Status mStatus = Status.OPENING;

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
        mBookUtil = new BookUtil();
        this.context = context.getApplicationContext();
        config = Config.getInstance();
        //获取屏幕宽高
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metric = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metric);
        mWidth = metric.widthPixels;
        mHeight = metric.heightPixels;

        df = new DecimalFormat("#0.0");
        marginWidth = this.context.getResources().getDimension(R.dimen.readingMarginWidth);
        marginHeight = this.context.getResources().getDimension(R.dimen.readingMarginHeight);
        statusMarginBottom = this.context.getResources().getDimension(R.dimen.reading_status_margin_bottom);
        lineSpace = context.getResources().getDimension(R.dimen.reading_line_spacing);
        mVisibleWidth = mWidth - marginWidth * 2;
        mVisibleHeight = mHeight - marginHeight * 2;

        m_fontSize = config.getFontSize();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);// 画笔
        mPaint.setTextAlign(Paint.Align.LEFT);// 左对齐
        mPaint.setTextSize(m_fontSize);// 字体大小
        mPaint.setColor(m_textColor);// 字体颜色
        mPaint.setSubpixelText(true);// 设置该项为true，将有助于文本在LCD屏幕上的显示效果

        waitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);// 画笔
        waitPaint.setTextAlign(Paint.Align.LEFT);// 左对齐
        waitPaint.setTextSize(this.context.getResources().getDimension(R.dimen.reading_max_text_size));// 字体大小
        waitPaint.setColor(m_textColor);// 字体颜色
        waitPaint.setSubpixelText(true);// 设置该项为true，将有助于文本在LCD屏幕上的显示效果
        calculateLineCount();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBatterryFontSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, context.getResources().getDisplayMetrics());
        paint.setTextSize(mBatterryFontSize);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(m_textColor);
        context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));//注册广播,随时获取到电池电量信息

        initBg(config.getDayOrNight());
        measureMarginWidth();
    }

    private void measureMarginWidth() {
        float wordWidth = mPaint.measureText("\u3000");
        float width = mVisibleWidth % wordWidth;
        measureMarginWidth = marginWidth + width / 2;
    }

    //初始化背景
    private void initBg(Boolean isNight) {
        if (isNight) {
            Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.BLACK);
            setBgBitmap(bitmap);
            //设置字体颜色
            setM_textColor(Color.rgb(128, 128, 128));
            setBookPageBg(Color.BLACK);
        } else {
            //设置背景
            setBookBg(config.getBookBgType());
        }
    }

    private void calculateLineCount() {
        mLineCount = (int) (mVisibleHeight / (m_fontSize + lineSpace));// 可显示的行数
    }

    private void drawStatus(Bitmap bitmap) {
        String status = "";
        switch (mStatus) {
            case OPENING:
                status = context.getResources().getString(R.string.read_loading);
                break;
            case FAIL:
                status = context.getResources().getString(R.string.read_load_fail);
                break;
        }

        Canvas c = new Canvas(bitmap);
        c.drawBitmap(getBgBitmap(), 0, 0, null);
        waitPaint.setColor(getTextColor());
        waitPaint.setTextAlign(Paint.Align.CENTER);

        Rect targetRect = new Rect(0, 0, mWidth, mHeight);
        Paint.FontMetricsInt fontMetrics = waitPaint.getFontMetricsInt();

        int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        // 下面这行是实现水平居中，drawText对应改为传入targetRect.centerX()
        waitPaint.setTextAlign(Paint.Align.CENTER);
        c.drawText(status, targetRect.centerX(), baseline, waitPaint);
        mBookPageWidget.postInvalidate();
    }

    public void onDraw(Bitmap bitmap, List<String> m_lines, Boolean updateCharter) {
        if (getDirectoryList().size() > 0 && updateCharter) {
            currentCharter = getCurrentCharter();
        }
        //更新Book数据库中的数据
        if (currentPage != null && book != null) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    values.put("begin", currentPage.getBegin());
                    LitePal.update(Book.class, values, book.getId());
                }
            }.start();
        }

        Canvas c = new Canvas(bitmap);
        c.drawBitmap(getBgBitmap(), 0, 0, null);
        mPaint.setTextSize(getFontSize());
        mPaint.setColor(getTextColor());
        paint.setColor(getTextColor());
        if (m_lines.size() == 0) {
            return;
        }

        if (m_lines.size() > 0) {
            float y = marginHeight;
            for (String strLine : m_lines) {
                y += m_fontSize + lineSpace;
                c.drawText(strLine, measureMarginWidth, y, mPaint);
            }
        }

        //画进度及时间
        float fPercent = (float) (currentPage.getBegin() * 1.0 / mBookUtil.getBookLen());//进度

//        currentProgress = fPercent;
        if (mPageEvent != null) {
            mPageEvent.changeProgress(fPercent);
        }
        String strPercent = df.format(fPercent * 100) + "%";//进度文字
        c.drawText(strPercent, mWidth / 2, mHeight - statusMarginBottom, paint);//x y为坐标值

        /**save()无参传入这两个方法最终都调用native_save方法，而无参方法save()默认是保存Matrix和Clip这两个信息。
         如果允许，那么尽量使用无参的save()方法，而不是使用有参的save(int saveFlags)方法传入别的Flag。*/
        c.save();

        c.restore();

        //画章
        if (getDirectoryList().size() > 0) {
            String charterName = getDirectoryList().get(currentCharter).getCatalog();
            int nChaterWidth = (int) paint.measureText(charterName) + 1;
//            c.drawText(charterName, mWidth - marginWidth - nChaterWidth, statusMarginBottom + mBatterryFontSize, paint);
            c.drawText(charterName, 0, statusMarginBottom + mBatterryFontSize+30, paint);

        }

        mBookPageWidget.postInvalidate();
    }

    //向前翻页
    public void prePage() {
        if (currentPage.getBegin() <= 0) {
            if (!isFirstPage) {
                Toast.makeText(context, context.getResources().getString(R.string.read_to_head), Toast.LENGTH_SHORT).show();
            }
            isFirstPage = true;
            return;
        } else {
            isFirstPage = false;
        }

        cancelPage = currentPage;
        onDraw(mBookPageWidget.getCurPage(), currentPage.getLines(), true);
        currentPage = getPrePage();
        onDraw(mBookPageWidget.getNextPage(), currentPage.getLines(), true);
    }

    //向后翻页
    public void nextPage() {
        if (currentPage.getEnd() >= mBookUtil.getBookLen()) {
            if (!isLastPage) {
                Toast.makeText(context, context.getResources().getString(R.string.read_to_end), Toast.LENGTH_SHORT).show();
            }
            isLastPage = true;
            return;
        } else {
            isLastPage = false;
        }

        cancelPage = currentPage;
        onDraw(mBookPageWidget.getCurPage(), currentPage.getLines(), true);
        prePage = currentPage;
        currentPage = getNextPage();
        onDraw(mBookPageWidget.getNextPage(), currentPage.getLines(), true);
    }

    //取消翻页
    public void cancelPage() {
        currentPage = cancelPage;
    }

    /**
     * 打开书本
     *
     * @throws IOException
     */
    public void openBook(Book book) throws IOException {
        //清空数据
        currentCharter = 0;
        initBg(config.getDayOrNight());

        this.book = book;
        bookPath = book.getBookPath();
        bookName = book.getBookName().split(".txt")[0];

        mStatus = Status.OPENING;
        drawStatus(mBookPageWidget.getCurPage());
        drawStatus(mBookPageWidget.getNextPage());
        if (bookTask != null && bookTask.getStatus() != AsyncTask.Status.FINISHED) {
            bookTask.cancel(true);
        }
        bookTask = new BookTask();
        bookTask.execute(book.getBegin());
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
                PageFactory.mStatus = PageFactory.Status.FINISH;
                currentPage = getPageForBegin(begin);
                if (mBookPageWidget != null) {
                    currentPage(true);
                }
            } else {
                PageFactory.mStatus = PageFactory.Status.FAIL;
                drawStatus(mBookPageWidget.getCurPage());
                drawStatus(mBookPageWidget.getNextPage());
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
                mBookUtil.openBook(book);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    public Page getNextPage() {
        mBookUtil.setPosition(currentPage.getEnd());

        Page page = new Page();
        page.setBegin(currentPage.getEnd() + 1);
        page.setLines(getNextLines());
        page.setEnd(mBookUtil.getPosition());
        return page;
    }

    public Page getPrePage() {
        mBookUtil.setPosition(currentPage.getBegin());

        Page page = new Page();
        page.setEnd(mBookUtil.getPosition() - 1);
        page.setLines(getPreLines());
        page.setBegin(mBookUtil.getPosition());
        return page;
    }

    public Page getPageForBegin(long begin) {
        Page page = new Page();
        page.setBegin(begin);

        mBookUtil.setPosition(begin - 1);
        page.setLines(getNextLines());
        page.setEnd(mBookUtil.getPosition());
        return page;
    }

    public List<String> getNextLines() {
        List<String> lines = new ArrayList<>();
        float width = 0;
        String line = "";
        while (mBookUtil.next(true) != -1) {
            char word = (char) mBookUtil.next(false);
            //判断是否换行
            if ((word + "").equals("\r") && (((char) mBookUtil.next(true)) + "").equals("\n")) {
                mBookUtil.next(false);
                if (!line.isEmpty()) {
                    lines.add(line);
                    line = "";
                    width = 0;
                    if (lines.size() == mLineCount) {
                        break;
                    }
                }
            } else {
                float widthChar = mPaint.measureText(word + "");
                width += widthChar;
                if (width > mVisibleWidth) {
                    width = widthChar;
                    lines.add(line);
                    line = word + "";
                } else {
                    line += word;
                }
            }

            if (lines.size() == mLineCount) {
                if (!line.isEmpty()) {
                    mBookUtil.setPosition(mBookUtil.getPosition() - 1);
                }
                break;
            }
        }

        if (!line.isEmpty() && lines.size() < mLineCount) {
            lines.add(line);
        }
        return lines;
    }

    public List<String> getPreLines() {
        List<String> lines = new ArrayList<>();
        float width = 0;
        String line = "";
        char[] par = mBookUtil.preLine();
        while (par != null) {
            List<String> preLines = new ArrayList<>();
            for (int i = 0; i < par.length; i++) {
                char word = par[i];
                float widthChar = mPaint.measureText(word + "");
                width += widthChar;
                if (width > mVisibleWidth) {
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
            if (lines.size() >= mLineCount) {
                break;
            }
            width = 0;
            line = "";
            par = mBookUtil.preLine();
        }

        List<String> reLines = new ArrayList<>();
        int num = 0;
        for (int i = lines.size() - 1; i >= 0; i--) {
            if (reLines.size() < mLineCount) {
                reLines.add(0, lines.get(i));
            } else {
                num = num + lines.get(i).length();
            }
        }

        if (num > 0) {
            if (mBookUtil.getPosition() > 0) {
                mBookUtil.setPosition(mBookUtil.getPosition() + num + 2);
            } else {
                mBookUtil.setPosition(mBookUtil.getPosition() + num);
            }
        }
        return reLines;
    }

    //上一章
    public void preChapter() {
        if (mBookUtil.getBookCatalogList().size() > 0) {
            int num = currentCharter;
            if (num == 0) {
                num = getCurrentCharter();
            }
            num--;
            if (num >= 0) {
                long begin = mBookUtil.getBookCatalogList().get(num).getStartPosition();
                currentPage = getPageForBegin(begin);
                currentPage(true);
                currentCharter = num;
            }
        }
    }

    //下一章
    public void nextChapter() {
        int num = currentCharter;
        if (num == 0) {
            num = getCurrentCharter();
        }
        num++;
        if (num < getDirectoryList().size()) {
            long begin = getDirectoryList().get(num).getStartPosition();
            currentPage = getPageForBegin(begin);
            currentPage(true);
            currentCharter = num;
        }
    }

    //获取现在的章
    public int getCurrentCharter() {
        int num = 0;
        for (int i = 0; getDirectoryList().size() > i; i++) {
            BookCatalog bookCatalogue = getDirectoryList().get(i);
            if (currentPage.getEnd() >= bookCatalogue.getStartPosition()) {
                num = i;
            } else {
                break;
            }
        }
        return num;
    }

    //绘制当前页面
    public void currentPage(Boolean updateChapter) {
        onDraw(mBookPageWidget.getCurPage(), currentPage.getLines(), updateChapter);
        onDraw(mBookPageWidget.getNextPage(), currentPage.getLines(), updateChapter);
    }

    public void changeProgress(float progress) {
        long begin = (long) (mBookUtil.getBookLen() * progress);
        currentPage = getPageForBegin(begin);
        currentPage(true);
    }

    //改变章节进度
    public void changeChapter(long begin) {
        currentPage = getPageForBegin(begin);
        currentPage(true);
    }

    //改变亮度
    public void changeBrightness(Activity activity,float brightness){
        WindowManager.LayoutParams attributes = activity.getWindow().getAttributes();
        attributes.screenBrightness = brightness;
        activity.getWindow().setAttributes(attributes);
    }

    //获取系统亮度
    public int getBrightness(Activity activity){
        int brightness = 0;
        ContentResolver cr = activity.getContentResolver();
        try {
            brightness = Settings.System.getInt(cr,Settings.System.SCREEN_BRIGHTNESS);
            Settings.System.getFloat(cr, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return brightness;
    }

    //改变字体大小
    public void changeFontSize(int fontSize) {
        this.m_fontSize = fontSize;
        mPaint.setTextSize(m_fontSize);
        calculateLineCount();
        measureMarginWidth();
        currentPage = getPageForBegin(currentPage.getBegin());
        currentPage(true);
    }

    //改变背景
    public void changeBookBg(int type) {
        setBookBg(type);
        currentPage(false);
    }

    //设置读书页面的背景和字的颜色
    public void setBookBg(int type) {
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        int color = 0;
        switch (type) {
            case Config.BOOK_BG_WHITE:
                canvas.drawColor(context.getResources().getColor(R.color.read_bg_white));
                color = context.getResources().getColor(R.color.read_font_color_by_white);
                setBookPageBg(context.getResources().getColor(R.color.read_bg_white));
                break;
            case Config.BOOK_BG_YELLOW:
                canvas.drawColor(context.getResources().getColor(R.color.read_bg_yellow));
                color = context.getResources().getColor(R.color.read_font_color_by_yellow);
                setBookPageBg(context.getResources().getColor(R.color.read_bg_yellow));
                break;
            case Config.BOOK_BG_GRAY:
                canvas.drawColor(context.getResources().getColor(R.color.read_bg_gray));
                color = context.getResources().getColor(R.color.read_font_color_by_gray);
                setBookPageBg(context.getResources().getColor(R.color.read_bg_gray));
                break;
            case Config.BOOK_BG_GREEN:
                canvas.drawColor(context.getResources().getColor(R.color.read_bg_green));
                color = context.getResources().getColor(R.color.read_font_color_by_green);
                setBookPageBg(context.getResources().getColor(R.color.read_bg_green));
                break;
            case Config.BOOK_BG_BLUE:
                canvas.drawColor(context.getResources().getColor(R.color.read_bg_blue));
                color = context.getResources().getColor(R.color.read_font_color_by_blue);
                setBookPageBg(context.getResources().getColor(R.color.read_bg_blue));
                break;
        }

        setBgBitmap(bitmap);
        //设置字体颜色
        setM_textColor(color);
    }

    public void setBookPageBg(int color) {
        if (mBookPageWidget != null) {
            mBookPageWidget.setBgColor(color);
        }
    }

    //设置日间或者夜间模式
    public void setDayOrNight(Boolean isNight) {
        initBg(isNight);
        currentPage(false);
    }

    public void initData() {
        currentCharter = 0;
        bookPath = "";
        bookName = "";
        book = null;
        mBookPageWidget = null;
        mPageEvent = null;
        cancelPage = null;
        prePage = null;
        currentPage = null;
    }

    public static Status getStatus() {
        return mStatus;
    }

    public long getBookLen() {
        return mBookUtil.getBookLen();
    }

    public Page getCurrentPage() {
        return currentPage;
    }

    //获取书本的章
    public List<BookCatalog> getDirectoryList() {
        return mBookUtil.getBookCatalogList();
    }

    public String getBookPath() {
        return bookPath;
    }

    //是否是第一页
    public boolean isFirstPage() {
        return isFirstPage;
    }

    //是否是最后一页
    public boolean isLastPage() {
        return isLastPage;
    }

    //设置页面背景
    public void setBgBitmap(Bitmap BG) {
        m_book_bg = BG;
    }

    //设置页面背景
    public Bitmap getBgBitmap() {
        return m_book_bg;
    }

    //设置文字颜色
    public void setM_textColor(int m_textColor) {
        this.m_textColor = m_textColor;
    }

    //获取文字颜色
    public int getTextColor() {
        return this.m_textColor;
    }

    //获取文字大小
    public float getFontSize() {
        return this.m_fontSize;
    }

    public void setPageWidget(PageWidget mBookPageWidget) {
        this.mBookPageWidget = mBookPageWidget;
    }

    public void setPageEvent(PageEvent pageEvent) {
        this.mPageEvent = pageEvent;
    }

    public interface PageEvent {
        void changeProgress(float progress);
    }
}