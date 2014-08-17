package de.byoc.todo.service;

import java.util.List;

import de.byoc.todo.data.TodoItem;

public interface TodoItemService {

	List<TodoItem> getAllItems();

	TodoItem getItem(String itemId);

	TodoItem createItem(TodoItem toBeCreated);

	TodoItem createItem(TodoItem item, String namespace);

	void updateItem(TodoItem item);

	void deleteAllItems();

	void deleteItem(String itemId);

}
