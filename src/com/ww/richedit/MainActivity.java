package com.ww.richedit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.ww.richedit.detail.DetailActivity;

public class MainActivity extends Activity implements OnClickListener {
	private Button bt0;
	private Button bt1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		init();
	}

	private void init() {
		bt0 = (Button) findViewById(R.id.bt0);
		bt1 = (Button) findViewById(R.id.bt1);
		bt0.setOnClickListener(this);
		bt1.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt0:
			Intent i = new Intent();
			i.setClass(MainActivity.this, EditActivity.class);
			startActivity(i);
			break;
		case R.id.bt1:
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, DetailActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

}
