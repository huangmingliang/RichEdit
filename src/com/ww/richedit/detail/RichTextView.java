package com.ww.richedit.detail;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ww.richedit.BitmapUtils;
import com.ww.richedit.EditData;
import com.ww.richedit.test.Contact;

public class RichTextView extends ScrollView {

	private static final int TEXTVIEW_PADDING = 10;
	private static final int IMAGEVIEWVIEW_MARGIN = 10;

	private LinearLayout allLayout;

	public RichTextView(Context context) {
		super(context);
		init(context);
	}

	public RichTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public RichTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		allLayout = new LinearLayout(context);
		allLayout.setOrientation(LinearLayout.VERTICAL);
		allLayout.setBackgroundColor(Color.WHITE);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		addView(allLayout, layoutParams);
		setData();
	}

	private void setData() {
		List<EditData> list = Contact.list;
		for (int i = 0; i < list.size(); i++) {
			EditData ed = list.get(i);
			if (ed.inputStr != null) {
				addTextViewAtIndex(i, ed.inputStr);
			} else if (ed.imagePath != null) {
				addImageViewAtIndex(i, ed.imagePath);
			}
		}
	}

	private void addTextViewAtIndex(int index, String str) {
		TextView tv = new TextView(getContext());
		tv.setPadding(TEXTVIEW_PADDING, TEXTVIEW_PADDING, TEXTVIEW_PADDING, TEXTVIEW_PADDING);
		tv.setText(str);
		allLayout.addView(tv);
	}

	/** 在特定位置添加ImageView */
	private void addImageViewAtIndex(int index, String imagePath) {
		ImageView imageView = new ImageView(getContext());
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();
		Bitmap bmp = BitmapUtils.decodeSampledBitmapFromFile(imagePath, width, width / 2);
		imageView.setImageBitmap(bmp);

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, width / 2);
		lp.setMargins(IMAGEVIEWVIEW_MARGIN, IMAGEVIEWVIEW_MARGIN, IMAGEVIEWVIEW_MARGIN, IMAGEVIEWVIEW_MARGIN);
		imageView.setLayoutParams(lp);
		allLayout.addView(imageView);
	}

}
