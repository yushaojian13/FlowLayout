package com.ysj.flowlayoutapp;

import java.util.List;
import java.util.Set;

import com.ysj.flowlayout.CheckedFlowLayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class HomeActivity extends AppCompatActivity implements CheckedFlowLayout.OnSelectChangedListener {
	private TextView		  tagsTV;
	private CheckedFlowLayout flowLayout;
	private ToggleButton	  toggleButton;

	private static final String[] tags = { "hello", "wonderful", "foo", "geek", "ViewGroup", "TextView", "Bundle",
	                                       "View", "AppCompatActivity" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		tagsTV = (TextView) findViewById(R.id.tagsTV);
		flowLayout = (CheckedFlowLayout) findViewById(R.id.flowlayout);
		toggleButton = (ToggleButton) findViewById(R.id.toggleBtn);

		flowLayout.setTags(tags);

		flowLayout.setOnSelectChangedListener(this);
	}

	public void toggleAll(View view) {
		flowLayout.setSelected(toggleButton.isChecked());
	}

	@Override
	public void onTagClick(CheckedFlowLayout checkedFlowLayout, int position, boolean selected) {
		Toast.makeText(this, "position " + position + " selected " + selected, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onTagSelectChanged(CheckedFlowLayout checkedFlowLayout, Set<Integer> checkedPositions) {
		tagsTV.setText(checkedPositions.toString());
	}
}
