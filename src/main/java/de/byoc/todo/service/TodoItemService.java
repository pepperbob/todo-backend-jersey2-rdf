package de.byoc.todo.service;

import java.util.List;

import de.byoc.todo.data.TodoItem;

public interface TodoItemService {

	void createItem(TodoItem toBeCreated);
	
	List<TodoItem> getAllItems();
	
	void deleteAllItems();
	
}
