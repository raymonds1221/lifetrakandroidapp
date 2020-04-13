package com.salutron.lifetrakwatchapp.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;

import com.salutron.lifetrakwatchapp.model.BaseModel;

public class SalutronObjectList<T extends BaseModel> implements List<T> {
	private List<T> mList = new ArrayList<T>();

	@Override
	public boolean add(T object) {
		return mList.add(object);
	}

	@Override
	public void add(int location, T object) {
		mList.add(location, object);
	}

	@Override
	public boolean addAll(Collection<? extends T> arg0) {
		return mList.addAll(arg0);
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends T> arg1) {
		return mList.addAll(arg0, arg1);
	}

	@Override
	public void clear() {
		mList.clear();
	}

	@Override
	public boolean contains(Object object) {
		return mList.contains(object);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return mList.containsAll(arg0);
	}

	@Override
	public T get(int location) {
		return mList.get(location);
	}

	@Override
	public int indexOf(Object object) {
		return mList.indexOf(object);
	}

	@Override
	public boolean isEmpty() {
		return mList.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return mList.iterator();
	}

	@Override
	public int lastIndexOf(Object object) {
		return mList.lastIndexOf(object);
	}

	@Override
	public ListIterator<T> listIterator() {
		return mList.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int location) {
		return mList.listIterator(location);
	}

	@Override
	public T remove(int location) {
		return mList.remove(location);
	}

	@Override
	public boolean remove(Object object) {
		return mList.remove(object);
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		return mList.removeAll(arg0);
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		return mList.retainAll(arg0);
	}

	@Override
	public T set(int location, T object) {
		return mList.set(location, object);
	}

	@Override
	public int size() {
		return mList.size();
	}

	@Override
	public List<T> subList(int start, int end) {
		return mList.subList(start, end);
	}

	@Override
	public Object[] toArray() {
		return mList.toArray();
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] array) {
		return mList.toArray(array);
	}
}
