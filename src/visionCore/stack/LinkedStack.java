package visionCore.stack;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

/**
 * A stack implementation that uses the concept of a linked list and doesn't inherits from the
 * common vector-based Stack interface.
 * Note that the for-each loop or using an Iterator is to be preferred over iterating with indices
 * and the .get(int i) method because of significantly better performance.
 * 
 * @author Pascal "Deconimus" Sielski
 *
 * @param <T> The Type of Objects, the LinkedStack should contain.
 */
public class LinkedStack<T> implements Stack<T> {
	
	private static final long serialVersionUID = 3854546428551233731L;
	private Item<T> top;
	private int size;
	
	/**
	 * Creates a LinkedStack. The top object will be null at this point.
	 */
	public LinkedStack() {
		this(null);
	}
	
	/**
	 * Creates a LinkedStack with a specified object to be the top.
	 */
	public LinkedStack(T obj) {
		
		if (obj != null) {
			
			top = new Item<T>(obj);
			size = 1;
			
		} else { 
			
			top = null;
			size = 0;
			
		}
		
	}
	
	@Override
	public T peek() {
		
		if (top != null) {
			return top.obj;
		}
		
		return null;
	}
	
	@Override
	public T pop() {
		
		if (top != null) {
			
			Item<T> oldTop = top;
			top = top.next;
			if (top != null) {
				top.parent = null;
			}
			
			size--;
			
			return oldTop.obj;
			
		}
		
		return null;
	}
	
	@Override
	public boolean push(T obj) {
		
		if (obj != null) {
			
			top = new Item<T>(obj, null, top);
			if (size > 0) {	top.next.parent = top; }
			
			size++;
			
			return true;
		}
		
		return false;
		
	}
	
	@Override
	public T get(int index) {
		
		if (index >= 0 && index < size) {
			
			Item<T> tmp = top;
			
			for (int i = 0; i < index; i++) {
				
				tmp = tmp.next;
				
			}
			
			return tmp.obj;
			
		}
		
		return null;
	}
	
	public int size() {
		return size;
	}
	
	@Override
	public void clear() {
		
		top = null;
		size = 0;
		
	}
	
	@Override
	public boolean remove(Object object) {
		
		for (Item item = top; item.next != null; item = item.next) {
			
			if (item.obj.equals(object)) {
				item.remove();
				return true;
			}
			
		}
		
		return false;
		
	}
	
	@Override
	public boolean remove(int index) {
		
		if (index <= size && index >= 0) {
			
			int i = 0;
			for (Item item = top; item.next != null && i < size; item = item.next) {
				
				if (i == index) {
					item.remove();
					return true;
				}
				
				i++;
			}
			
		}
		
		return false;
		
	}
	
	public boolean remove(int index, Item<T> item, int i) {
		
		if (index == i) {
			
			item.remove();
			return true;
			
		} else {
			
			if (item.next != null) {
				return remove(index, item.next, i++);
			} else { return false; }
			
		}
		
	}
	
	@SuppressWarnings("hiding")
	public class LinkedStackIterator<T> implements Iterator<T> {

		private Item<T> current;
		
		@Override
		public boolean hasNext() {
			
			if (current != null && current.next != null) {
				return true;
			}
			
			return false;
		}

		@Override
		public T next() {
			
			if (hasNext()) {
				current = current.next;
				return current.obj;
			}
			
			return null;
		}

		@Override
		public void remove() {
			
			current.remove();
			
		}
		
	}

	@Override
	public Iterator<T> iterator() {
		return new LinkedStackIterator<T>(); 
	}

	@Override
	public boolean add(T obj) {
		
		if (top == null) { 
			
			top = new Item<T>(obj, null);
			size++;
			
			return true;
		}
		
		Item<T> last = top;
		while(last.next != null) {
			
			last = last.next;
		}
		
		last.next = new Item<T>(obj, last);
		
		size++;
		
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> collection) {
		
		for (T t : collection) {
			add(t);
		}
		
		size += collection.size();
		
		return true;
	}

	@Override
	public boolean contains(Object obj) {
		
		for (T t : this) {
			
			if (t.equals(obj)) {
				
				return true;
				
			}
			
		}
		
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		
		for (Object obj : collection) {
			
			if (!contains(obj)) { return false; }
			
		}
		
		return true;
		
	}

	@Override
	public boolean isEmpty() {
		return top == null;
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		
		for (Object obj : collection) {
			
			if (!remove(obj)) { return false; }
			
		}
		
		size = 0;
		return true;
		
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		
		return retainAll(collection, top, false);
		
	}
	
	private boolean retainAll(Collection<?> collection, Item<T> item, boolean changed) {
		
		boolean b = false;
		
		for (Object obj : collection) {
			
			if (item.obj.equals(obj)) {
				b = true;
				break;
			}
			
		}
		
		if (!b) {
			item.remove();
			changed = true;
		}
		
		if (item.next != null) {
			
			return retainAll(collection, item.next, changed);
			
		} else { return changed; }
		
	}

	@Override
	public Object[] toArray() {
		
		@SuppressWarnings("unchecked")
		T[] ts = (T[])(Array.newInstance(top.obj.getClass(), size));
		
		int i = 0;
		for (T t : this) {
			
			ts[i] = t;
			i++;
			
		}
		
		return ts;
		
	}

	@SuppressWarnings("hiding")
	@Override
	/**
	 * Don't use this bullcrap method, it's just a pain in the ass.
	 */
	public <T> T[] toArray(T[] arg0) {
		
		return null;
		
	}

	@Override
	public boolean pushAll(Collection<T> collection) {
		
		for (T t : collection) {
			
			if (!push(t)) { return false; }
			
		}
		
		return true;
	}
	
	@Override
	public T getLast() {
		
		return get(size-1);
		
	}
	
	private class Item<E> {
		
		public E obj;
		public Item<E> next, parent;
		
		public Item(E obj) {
			this(obj, null, null);
		}
		
		public Item(E obj, Item<E> parent) {
			this(obj, parent, null);
		}
		
		public Item(E obj, Item<E> parent, Item<E> next) {
			this.obj = obj;
			this.parent = parent;
			this.next = next;
		}
		
		@SuppressWarnings("unchecked")
		public void remove() {
			
			if (parent == null) {
				
				top = (LinkedStack<T>.Item<T>) this.next;
				if (top != null) {
					top.parent = null;
				}
				
			} else {
				
				parent.next = this.next;
				
			}
			
			size--;
			
		}
		
	}

}
