package com.graphqlcheckmate.services

import com.graphqlcheckmate.AuthenticatedSupabaseClient
import com.graphqlcheckmate.TodoListEntity
import com.graphqlcheckmate.TodoItemEntity

/**
 * Service for managing TODO lists and TODO items.
 * Handles CRUD operations for lists and items.
 */
class TodoListService(
    private val supabaseService: com.graphqlcheckmate.SupabaseService
) {
    /**
     * Get all TODO lists for a specific group
     */
    suspend fun getTodoListsByGroup(
        authenticatedClient: AuthenticatedSupabaseClient,
        groupId: String
    ): List<TodoListEntity> {
        return authenticatedClient.getTodoListsByGroup(groupId)
    }

    /**
     * Get a specific TODO list by ID
     */
    suspend fun getTodoListById(
        authenticatedClient: AuthenticatedSupabaseClient,
        id: String
    ): TodoListEntity? {
        return authenticatedClient.getTodoListById(id)
    }

    /**
     * Create a new TODO list
     */
    suspend fun createTodoList(
        authenticatedClient: AuthenticatedSupabaseClient,
        subject: String,
        groupId: String,
        ownerId: String
    ): TodoListEntity {
        return authenticatedClient.createTodoList(subject, groupId, ownerId)
    }

    /**
     * Update a TODO list
     */
    suspend fun updateTodoList(
        authenticatedClient: AuthenticatedSupabaseClient,
        id: String,
        subject: String?
    ): TodoListEntity {
        return authenticatedClient.updateTodoList(id, subject)
    }

    /**
     * Delete a TODO list
     */
    suspend fun deleteTodoList(
        authenticatedClient: AuthenticatedSupabaseClient,
        id: String
    ): Boolean {
        return authenticatedClient.deleteTodoList(id)
    }

    /**
     * Get all items for a specific TODO list
     */
    suspend fun getTodoItemsByList(
        authenticatedClient: AuthenticatedSupabaseClient,
        todoListId: String
    ): List<TodoItemEntity> {
        return authenticatedClient.getTodoItemsByList(todoListId)
    }

    /**
     * Get a specific TODO item by ID
     */
    suspend fun getTodoItemById(
        authenticatedClient: AuthenticatedSupabaseClient,
        id: String
    ): TodoItemEntity? {
        return authenticatedClient.getTodoItemById(id)
    }

    /**
     * Create a new TODO item
     */
    suspend fun createTodoItem(
        authenticatedClient: AuthenticatedSupabaseClient,
        todoListId: String,
        text: String,
        orderIndex: Int
    ): TodoItemEntity {
        return authenticatedClient.createTodoItem(todoListId, text, orderIndex)
    }

    /**
     * Update a TODO item
     */
    suspend fun updateTodoItem(
        authenticatedClient: AuthenticatedSupabaseClient,
        id: String,
        text: String?,
        completed: Boolean?,
        orderIndex: Int?
    ): TodoItemEntity {
        return authenticatedClient.updateTodoItem(id, text, completed, orderIndex)
    }

    /**
     * Delete a TODO item
     */
    suspend fun deleteTodoItem(
        authenticatedClient: AuthenticatedSupabaseClient,
        id: String
    ): Boolean {
        return authenticatedClient.deleteTodoItem(id)
    }
}
