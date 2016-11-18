package visionCore.stack;

import java.io.Serializable;
import java.util.Collection;

public abstract interface Stack<T> extends Collection<T>, Serializable {
	
	/**
	 * Returns the top object without deleting it.
	 */
	public abstract T peek();
	
	/**
	 * Returns the top object and removes it from the stack.
	 */
	public abstract T pop();
	
	/**
	 * Adds a new object to the top.
	 * @param obj The object to be added.
	 */
	public abstract boolean push(T obj);
	
	/**
	 * Adds all objects from a given collection to the top.
	 * @param obj The object to be added.
	 */
	public abstract boolean pushAll(Collection<T> collection);
	
	/**
	 * Returns the object saved with the given index.
	 */
	public abstract T get(int index);
	
	/**
	 * Returns the size of this LinkedStack.
	 */
	public abstract int size();
	
	/**
	 * Clears the stack.
	 */
	public abstract void clear();
	
	/**
	 * Removes the given object from the stack if existing.
	 */
	public abstract boolean remove(Object obj);
	
	/**
	 * Removes the object of the given index from the stack if existing.
	 */
	public abstract boolean remove(int index);
	
	/**
	 * @return The lowest object.
	 */
	public abstract T getLast();

}
