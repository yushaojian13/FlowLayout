package com.ysj.flowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 支持标签点选效果的流布局，标签背景应包含default、pressed和selected态.
 * Created by yushaojian on 10/23/16.
 */

public class CheckedFlowLayout extends FlowLayout implements View.OnClickListener {
	private OnSelectChangedListener onSelectChangedListener;

	private Set<Integer> selectedPositions = new LinkedHashSet<>();

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

		selectedPositions.clear();
		if (selected) {
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				selectedPositions.add(i);
			}
		}

		notifySelectedTagsChanged();
	}

	private void clearSelected() {
		selectedPositions.clear();
		notifySelectedTagsChanged();
	}

	private void notifySelectedTagsChanged() {
		if (onSelectChangedListener != null) {
			onSelectChangedListener.onTagSelectChanged(this, selectedPositions);
		}
	}

	@Override
	public void onClick(View v) {
		v.setSelected(!v.isSelected());

		if (onSelectChangedListener != null) {
			int position = indexOfChild(v);

			if (v.isSelected()) {
				selectedPositions.add(position);
			} else {
				selectedPositions.remove(position);
			}

			onSelectChangedListener.onTagClick(this, position, v.isSelected());
			onSelectChangedListener.onTagSelectChanged(this, selectedPositions);
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
		 * @param position 标签位置
		 * @param selected 是否选中
		 */
		void onTagClick(CheckedFlowLayout checkedFlowLayout, int position, boolean selected);

		/**
		 * 已选的标签改变时调用
		 *
		 * @param checkedFlowLayout 标签所在的CheckedFlowLayout
		 * @param checkedPositions 所有已选的位置
		 */
		void onTagSelectChanged(CheckedFlowLayout checkedFlowLayout, Set<Integer> checkedPositions);
	}
}
