package com.ysj.flowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yushaojian on 10/23/16.
 */

public class CheckedFlowLayout extends FlowLayout implements View.OnClickListener {
	private OnSelectChangedListener onSelectChangedListener;

	private List<String> selectedTags = new ArrayList<>();

	public CheckedFlowLayout(Context context) {
		this(context, null);
	}

	public CheckedFlowLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CheckedFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		setClickable(true);
	}

	/**
	 * 设置标签. 之前的标签都会被移除.
	 *
	 * @param tags 标签数组
	 */
	@Override
	public void setTags(String[] tags) {
		super.setTags(tags);

		clearSelected();
	}

	@Override
	public TextView addTag(String tag) {
		TextView tagTV = super.addTag(tag);
		tagTV.setOnClickListener(this);
		return tagTV;
	}

	@Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);

		selectedTags.clear();
		if (selected) {
			selectedTags.addAll(tags);
		}

		notifySelectedTagsChanged();
	}

	private void clearSelected() {
		selectedTags.clear();
		notifySelectedTagsChanged();
	}

	private void notifySelectedTagsChanged() {
		if (onSelectChangedListener != null) {
			onSelectChangedListener.onTagSelectChanged(this, selectedTags);
		}
	}

	@Override
	public void onClick(View v) {
		v.setSelected(!v.isSelected());

		if (onSelectChangedListener != null) {
			TextView tagTV = (TextView) v;
			String tag = tagTV.getText().toString();

			if (tagTV.isSelected()) {
				selectedTags.add(tag);
			} else {
				selectedTags.remove(tag);
			}

			onSelectChangedListener.onTagClick(this, tag,  indexOfChild(v), tagTV.isSelected());
			onSelectChangedListener.onTagSelectChanged(this, selectedTags);
		}
	}

	public void setOnSelectChangedListener(OnSelectChangedListener onSelectChangedListener) {
		this.onSelectChangedListener = onSelectChangedListener;
	}

	public interface OnSelectChangedListener {
		/**
		 * 标签被点击时回调.
		 *
		 * @param checkedFlowLayout 标签所在的CheckedFlowLayout
		 * @param tag 标签名
		 * @param position 标签位置
		 * @param selected 是否选中
		 */
		void onTagClick(CheckedFlowLayout checkedFlowLayout, String tag, int position, boolean selected);

		/**
		 * 已选的标签改变时调用
		 *
		 * @param checkedFlowLayout 标签所在的CheckedFlowLayout
		 * @param checkedTags 所有已选的tags
		 */
		void onTagSelectChanged(CheckedFlowLayout checkedFlowLayout, List<String> checkedTags);
	}
}
