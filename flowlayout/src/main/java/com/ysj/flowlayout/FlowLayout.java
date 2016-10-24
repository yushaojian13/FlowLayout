package com.ysj.flowlayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 流布局.
 * Created by yushaojian on 10/18/16.
 */

public class FlowLayout extends ViewGroup {
	// 支持的对齐方式
	private static final int LEFT		= 0;
	private static final int CENTER		= 1;
	private static final int RIGHT		= 2;
	private static final int BOTH_SIDES	= 3;

	// 初始的一行可能包含的子view个数
	private static final int INIT_LINE_CHILD_COUNT = 5;

	private int	align;			  // 对齐方式
	private int	lineSpacingExtra; // 额外的行间距
	private int	itemSpacingExtra; // 额外的item间距，两边对齐模式下间距可能会被拉大

	private List<Rect> rects = new ArrayList<>(INIT_LINE_CHILD_COUNT); // 逐次记录每一行子view的左上右下坐标值
	private int		   lineFirstChildIndex;							   // 逐次记录每一行最前面的子view的索引值

	private int			   tagTextSize;
	private ColorStateList tagTextColor;
	private int			   tagBackgroundId;

	protected List<String> tags = new ArrayList<>();

	public FlowLayout(Context context) {
		this(context, null);
	}

	public FlowLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		// 解析属性值
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
		align = typedArray.getInt(R.styleable.FlowLayout_align, LEFT);
		lineSpacingExtra = typedArray.getDimensionPixelSize(R.styleable.FlowLayout_lineSpacingExtra, 0);
		itemSpacingExtra = typedArray.getDimensionPixelSize(R.styleable.FlowLayout_itemSpacingExtra, 0);
		tagTextSize = typedArray.getDimensionPixelSize(R.styleable.FlowLayout_tagTextSize, 0);
		tagTextColor = typedArray.getColorStateList(R.styleable.FlowLayout_tagTextColor);
		tagBackgroundId = typedArray.getResourceId(R.styleable.FlowLayout_tagBackground, 0);
		typedArray.recycle();

		// 初始化子view坐标列表
		grow(INIT_LINE_CHILD_COUNT);

		setClickable(false);
	}

	/**
	 * 设置标签. 之前的标签都会被移除.
	 * 
	 * @param tags 标签数组
	 */
	public void setTags(String[] tags) {
		removeAllViews();

		for (String tag : tags) {
			addTag(tag);
		}
	}

	/**
	 * 添加一个标签
	 * 
	 * @param tag 标签
	 * @return 展示该标签的控件
	 */
	public TextView addTag(String tag) {
		tags.add(tag);

		MarginLayoutParams lp = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		TextView tagTV = new TextView(getContext());
		tagTV.setText(tag);
		tagTV.setGravity(Gravity.CENTER_VERTICAL);
		tagTV.setLines(1);

		if (tagTextColor != null) {
			tagTV.setTextColor(tagTextColor);
		}

		if (tagTextSize != 0) {
			tagTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, tagTextSize);
		}

		tagTV.setBackgroundResource(tagBackgroundId);

		addView(tagTV, lp);

		return tagTV;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		int paddingLeft = getPaddingLeft();
		int paddingRight = getPaddingRight();
		int paddingTop = getPaddingTop();
		int paddingBottom = getPaddingBottom();

		int layoutWidth = 0; // FlowLayout内容的宽度
		int layoutHeight = 0; // FlowLayout内容的高度

		int lineWidth = 0; // 一行的宽度
		int lineHeight = 0; // 一行的高度

		int childCount = this.getChildCount();

		/*
		 * 遍历子view，将子view进行Z型排列分行，计算出FlowLayout所需要的宽度与高度，
		 * 高度为所有行高及行间距之和，宽度为所有行宽中的最大值
		 */
		for (int i = 0; i < childCount; i++) {
			View child = this.getChildAt(i);

			// 如果child不可见，则它的宽高不影响FlowLayout的宽高
			if (child.getVisibility() == GONE) {
				// 但如果是最后一个子view，要更新一下FlowLayout的宽高
				if (i == childCount - 1) {
					layoutWidth = Math.max(layoutWidth, lineWidth);
					layoutHeight += lineHeight;
				}

				continue;
			}

			// 获取子view的宽高--start
			int childWidthWithMargin = 0;
			int childHeightWithMargin = 0;

			LayoutParams childLp = child.getLayoutParams();
			if (childLp instanceof MarginLayoutParams) {
				measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, layoutHeight);
				MarginLayoutParams mlp = (MarginLayoutParams) childLp;
				childWidthWithMargin = mlp.leftMargin + mlp.rightMargin;
				childHeightWithMargin = mlp.topMargin + mlp.bottomMargin;
			} else {
				measureChild(child, widthMeasureSpec, heightMeasureSpec);
			}

			childWidthWithMargin += child.getMeasuredWidth();
			childHeightWithMargin += child.getMeasuredHeight();
			// 获取子view的宽高--end

			// 如果行宽与子view宽之和大于FlowLayout可用的宽，则需要换行
			if (lineWidth + childWidthWithMargin > widthSize - paddingLeft - paddingRight) {
				// 产生新行，FlowLayout宽取为历史值与当前行的较大值，行宽高设为子view的宽高

				lineWidth -= itemSpacingExtra; // 减去该行最后一个子view引入的itemSpacingExtra
				layoutWidth = Math.max(layoutWidth, lineWidth);
				layoutHeight += lineHeight + lineSpacingExtra;

				lineWidth = childWidthWithMargin + itemSpacingExtra; // 如果一行只容得下这一个子view怎么办？
				lineHeight = childHeightWithMargin;
			} else {
				// 不需换行，行宽累加，行高为历史值与子view高的较大值

				lineWidth += childWidthWithMargin + itemSpacingExtra;
				lineHeight = Math.max(lineHeight, childHeightWithMargin);
			}

			// 更新一下FlowLayout的宽高
			if (i == childCount - 1) {
				layoutWidth = Math.max(layoutWidth, lineWidth);
				layoutHeight += lineHeight;
			}
		}

		setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? widthSize : layoutWidth + paddingLeft + paddingRight,
		                     heightMode == MeasureSpec.EXACTLY ? heightSize
		                                                       : layoutHeight + paddingTop + paddingBottom);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int paddingLeft = getPaddingLeft();
		int paddingRight = getPaddingRight();
		int paddingTop = getPaddingTop();

		int offsetX = paddingLeft;
		int offsetY = paddingTop;
		int widthUsed = 0;
		int lineHeight = 0;
		final int widthAvailable = r - l - paddingLeft - paddingRight;

		// 关键是计算子view的左上右下的坐标
		int childLeft;
		int childTop;
		int childRight;
		int childBottom;

		int childWidth;
		int childHeight;

		int childLeftMargin;
		int childTopMargin;
		int childRightMargin;
		int childBottomMargin;

		int childWidthWithMargin;
		int childHeightWithMargin;

		lineFirstChildIndex = 0;
		int lineChildCount = 0;

		int childCount = this.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = this.getChildAt(i);
			if (child.getVisibility() == GONE) {
				continue;
			}

			// 子view的宽高
			childWidth = child.getMeasuredWidth();
			childHeight = child.getMeasuredHeight();

			// 子view的margin
			LayoutParams childLp = child.getLayoutParams();
			if (childLp instanceof MarginLayoutParams) {
				MarginLayoutParams mlp = (MarginLayoutParams) childLp;

				childLeftMargin = mlp.leftMargin;
				childTopMargin = mlp.topMargin;
				childRightMargin = mlp.rightMargin;
				childBottomMargin = mlp.bottomMargin;
			} else {
				childLeftMargin = 0;
				childTopMargin = 0;
				childRightMargin = 0;
				childBottomMargin = 0;
			}

			childWidthWithMargin = childWidth + childLeftMargin + childRightMargin;
			childHeightWithMargin = childHeight + childTopMargin + childBottomMargin;

			// 如果需要换行
			if (widthUsed + childWidthWithMargin > widthAvailable) {
				widthUsed -= itemSpacingExtra; // 减去该行最后一个子view引入的itemSpacingExtra
				layoutLine(widthUsed, widthAvailable, lineChildCount, false);

				offsetX = paddingLeft;
				offsetY += lineHeight + lineSpacingExtra;

				widthUsed = 0;
				lineHeight = 0;
				lineChildCount = 0;
			}

			childLeft = offsetX + childLeftMargin;
			childTop = offsetY + childTopMargin;
			childRight = childLeft + childWidth;
			childBottom = childTop + childHeight;

			int lineChildIndex = i - lineFirstChildIndex;
			checkCapacity(lineChildIndex); // 如果一行的子view个数超过了rects的表示范围，则需要给它扩容

			Rect rect = rects.get(lineChildIndex);
			rect.set(childLeft, childTop, childRight, childBottom);
			lineChildCount++;

			if (childHeightWithMargin > lineHeight) {
				lineHeight = childHeightWithMargin;
			}
			widthUsed += childWidthWithMargin + itemSpacingExtra;
			offsetX += childWidthWithMargin + itemSpacingExtra;

			if (i == childCount - 1) {
				widthUsed -= itemSpacingExtra; // 减去该行最后一个子view引入的itemSpacingExtra
				layoutLine(widthUsed, widthAvailable, lineChildCount, true);
			}
		}
	}

	/**
	 * 排布一行子view
	 * 
	 * @param widthUsed 子view宽度之和
	 * @param widthAvailable 可用的宽度
	 * @param lineChildCount 子view个数
	 * @param isLastLine 是否最后一行
	 */
	private void layoutLine(int widthUsed, int widthAvailable, int lineChildCount, boolean isLastLine) {
		int lineOffset = 0;
		int offset = 0;

		if (align == LEFT) {
			lineOffset = 0;
		} else if (align == CENTER) {
			lineOffset = (widthAvailable - widthUsed) / 2;
		} else if (align == RIGHT) {
			lineOffset = widthAvailable - widthUsed;
		} else if (align == BOTH_SIDES) {
			if (lineChildCount > 1 && !isLastLine) {
				offset = (widthAvailable - widthUsed) / (lineChildCount - 1);
			}
		}

		Rect rect;
		for (int j = 0; j < lineChildCount; j++) {
			rect = rects.get(j);
			View child = getChildAt(lineFirstChildIndex + j);
			child.layout(lineOffset + offset * j + rect.left, rect.top, lineOffset + offset * j + rect.right,
			             rect.bottom);
		}

		lineFirstChildIndex += lineChildCount;
	}

	/**
	 * 检查rects容量
	 * 
	 * @param minSize 最小个数
	 */
	private void checkCapacity(int minSize) {
		if (minSize >= rects.size()) {
			grow(INIT_LINE_CHILD_COUNT / 2);
		}
	}

	/**
	 * 增加rects容量
	 * 
	 * @param count 增加的个数
	 */
	private void grow(int count) {
		if (count <= 0) {
			return;
		}

		for (int i = 0; i < count; i++) {
			Rect rect = new Rect();
			rects.add(rect);
		}
	}

	@Override
	protected LayoutParams generateLayoutParams(LayoutParams p) {
		return new MarginLayoutParams(p);
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new MarginLayoutParams(getContext(), attrs);
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new MarginLayoutParams(super.generateDefaultLayoutParams());
	}

}
