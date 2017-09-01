package com.mocoo.hang.rtprinter.adapter;

import android.util.SparseArray;
import android.view.View;

public class ViewHolder {
	@SuppressWarnings("unchecked")
	/**
	 * viewholder的简洁用�?
	 * @param view
	 * @param id
	 * @return
	 */
	public static <T extends View> T get(View view, int id) {
		SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
		if (viewHolder == null) {
			viewHolder = new SparseArray<View>();
			view.setTag(viewHolder);
		}
		View childView = viewHolder.get(id);
		if (childView == null) {
			childView = view.findViewById(id);
			viewHolder.put(id, childView);
		}
		return (T) childView;
	}
}