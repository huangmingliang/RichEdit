package com.ww.richedit;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

public class RichEditText extends ScrollView {

	private static final int EDIT_PADDING = 10; // edittext常规padding是10dp
	private static final int EDIT_FIRST_PADDING_TOP = 10; // 第一个EditText的paddingTop值

	private int viewTagIndex = 1; // 新生的view都会打一个tag，对每个view来说，这个tag是唯一的。
	private LinearLayout allLayout; // 这个是所有子view的容器，scrollView内部的唯一一个ViewGroup
	private LayoutInflater inflater;
	private OnKeyListener keyListener; // 所有EditText的软键盘监听器
	private OnFocusChangeListener focusListener; // 所有EditText的焦点监听listener
	private EditText lastFocusEdit; // 最近被聚焦的EditText

	// private LayoutTransition mTransitioner; //
	// 只在图片View添加或remove时，触发transition动画

	private int editNormalPadding = 0; //

	private int disappearingImageIndex = 0;

	public RichEditText(Context context) {
		super(context);
		init(context);
	}

	public RichEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public RichEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		inflater = LayoutInflater.from(context);
		// 1. 初始化allLayout
		allLayout = new LinearLayout(context);
		allLayout.setOrientation(LinearLayout.VERTICAL);
		allLayout.setBackgroundColor(Color.WHITE);
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		addView(allLayout, layoutParams);
		// 2. 初始化键盘退格监听
		// 主要用来处理点击回删按钮时，view的一些列合并操作
		keyListener = new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
					EditText edit = (EditText) v;
					onBackspacePress(edit);
				}
				return false;
			}
		};
		// 3.焦点监听
		focusListener = new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					lastFocusEdit = (EditText) v;
				}
			}
		};
		LinearLayout.LayoutParams firstEditParam = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		editNormalPadding = dip2px(EDIT_PADDING);
		EditText firstEdit = createEditText("input here", dip2px(EDIT_FIRST_PADDING_TOP));
		allLayout.addView(firstEdit, firstEditParam);
		lastFocusEdit = firstEdit;
	}

	private void addEditTextAtIndex(final int index, String editStr) {
		EditText addEditText = createEditText("", getResources().getDimensionPixelSize(R.dimen.edit_padding_top));
		addEditText.setText(editStr);
		allLayout.addView(addEditText, index);
	}

	/** 插入一张图片 */
	private void insertImage(Bitmap bitmap, String imagePath) {
		String lastEditStr = lastFocusEdit.getText().toString();
		int cursorIndex = lastFocusEdit.getSelectionStart();
		String editStr1 = lastEditStr.substring(0, cursorIndex).trim();
		int lastEditIndex = allLayout.indexOfChild(lastFocusEdit);
		if (TextUtils.isEmpty(lastEditStr) || TextUtils.isEmpty(editStr1)) {
			// 如果EditText为空，或者光标已经顶在了editText的最前面，则直接插入图片，并且EditText下移即可
			addImageViewAtIndex(lastEditIndex, bitmap, imagePath);
		} else {
			// 如果EditText非空且光标不在最顶端，则需要添加新的imageView和EditText
			lastFocusEdit.setText(editStr1);
			String editStr2 = lastEditStr.substring(cursorIndex).trim();
			if (allLayout.getChildCount() - 1 == lastEditIndex || editStr2.length() > 0) {
				addEditTextAtIndex(lastEditIndex + 1, editStr2);
			}

			addImageViewAtIndex(lastEditIndex + 1, bitmap, imagePath);
			lastFocusEdit.requestFocus();
			lastFocusEdit.setSelection(editStr1.length(), editStr1.length());
		}
		hideKeyBoard();
	}

	/** 隐藏小键盘 */
	public void hideKeyBoard() {
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(lastFocusEdit.getWindowToken(), 0);
	}

	/** 在特定位置添加ImageView */
	private void addImageViewAtIndex(final int index, Bitmap bmp, String imagePath) {
		final RelativeLayout imageLayout = (RelativeLayout) inflater.inflate(R.layout.edit_imageview, null);
		imageLayout.setTag(viewTagIndex++);
		DataImageView imageView = (DataImageView) imageLayout.findViewById(R.id.edit_imageView);
		imageView.setImageBitmap(bmp);
		imageView.setBitmap(bmp);
		imageView.setAbsolutePath(imagePath);

		// 调整imageView的高度
		int imageHeight = getWidth() * bmp.getHeight() / bmp.getWidth();
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, imageHeight);
		lp.setMargins(10, 10, 10, 10);
		imageView.setLayoutParams(lp);
		// onActivityResult无法触发动画，此处post处理
		allLayout.postDelayed(new Runnable() {
			@Override
			public void run() {
				allLayout.addView(imageLayout, index);
			}
		}, 200);
	}

	private EditText createEditText(String hint, int paddingTop) {
		EditText editText = (EditText) inflater.inflate(R.layout.edit_item1, null);
		editText.setOnKeyListener(keyListener);
		editText.setTag(viewTagIndex++);
		editText.setPadding(editNormalPadding, paddingTop, editNormalPadding, 0);
		editText.setHint(hint);
		editText.setOnFocusChangeListener(focusListener);
		return editText;
	}

	/** 处理软键盘backSpace回退事件
	 * 
	 * @param editTxt
	 *            光标所在的文本输入框 */
	private void onBackspacePress(EditText editTxt) {
		int startSelectionIndex = editTxt.getSelectionStart();
		Log.e("", "##10");
		// 只有在光标已经顶到文本输入框的最前方，在判定是否删除之前的图片，或两个View合并
		if (startSelectionIndex == 0) {
			int editIndex = allLayout.indexOfChild(editTxt);
			View preView = allLayout.getChildAt(editIndex - 1); // 如果editIndex-1<0,
																// 则返回的是null
			if (null != preView) {
				if (preView instanceof RelativeLayout) {
					// 光标EditText的上一个view对应的是图片
					Log.e("", "##13");
					onImageCloseClick(preView);
					Log.e("", "##14");
				} else if (preView instanceof EditText) {
					// 光标EditText的上一个view对应的还是文本框EditText
					String str1 = editTxt.getText().toString();
					EditText preEdit = (EditText) preView;
					Log.e("", "##11");
					String str2 = preEdit.getText().toString();
					Log.e("", "##12");
					// 合并文本view时，不需要transition动画
					allLayout.setLayoutTransition(null);
					allLayout.removeView(editTxt);
					// allLayout.setLayoutTransition(mTransitioner); //
					// 恢复transition动画

					// 文本合并
					preEdit.setText(str2 + str1);
					preEdit.requestFocus();
					preEdit.setSelection(str2.length(), str2.length());
					lastFocusEdit = preEdit;
				}
			}
		}
	}

	/** 处理图片叉掉的点击事件
	 * 
	 * @param view整个image对应的relativeLayout
	 *            view
	 * @type 删除类型 0代表backspace删除 1代表按红叉按钮删除 */
	private void onImageCloseClick(View view) {
		// if (!mTransitioner.isRunning()) {
		disappearingImageIndex = allLayout.indexOfChild(view);
		allLayout.removeView(view);
		// }
	}

	/** dp和pixel转换
	 * 
	 * @param dipValue
	 *            dp值
	 * @return 像素值 */
	public int dip2px(float dipValue) {
		float m = getContext().getResources().getDisplayMetrics().density;
		return (int) (dipValue * m + 0.5f);
	}

	/** 根据绝对路径添加view
	 * 
	 * @param imagePath */
	public void insertImage(String imagePath) {
		// 根据view的宽度，动态缩放bitmap尺寸
		Bitmap bmp = BitmapUtils.decodeSampledBitmapFromFile(imagePath, getWidth(), getWidth() / 2);
		insertImage(bmp, imagePath);
	}

	public void setData(List<EditData> data) {
		for (int i = 0; i < data.size(); i++) {
			EditData ed = data.get(i);
			if (ed.inputStr != null) {
				addEditTextAtIndex(i, ed.inputStr);
			} else if (ed.imagePath != null) {
				Bitmap bmp = BitmapUtils.decodeSampledBitmapFromFile(ed.imagePath, getWidth(), getWidth() / 2);
				insertImage(bmp, ed.imagePath);
			}
		}

	}

	/** 对外提供的接口, 生成编辑数据上传 */
	public List<EditData> buildEditData() {
		List<EditData> dataList = new ArrayList<EditData>();
		int num = allLayout.getChildCount();
		for (int index = 0; index < num; index++) {
			View itemView = allLayout.getChildAt(index);
			EditData itemData = new EditData();
			if (index == num - 1) {
				EditText item = (EditText) itemView;
				String str = item.getText().toString();
				if (!TextUtils.isEmpty(str)) {
					itemData.inputStr = str;
					dataList.add(itemData);
				}
			} else if (itemView instanceof EditText) {
				EditText item = (EditText) itemView;
				itemData.inputStr = item.getText().toString();
				dataList.add(itemData);
			} else if (itemView instanceof RelativeLayout) {
				DataImageView item = (DataImageView) itemView.findViewById(R.id.edit_imageView);
				itemData.imagePath = item.getAbsolutePath();
				itemData.bitmap = item.getBitmap();
				dataList.add(itemData);
			}
		}

		return dataList;
	}

}
