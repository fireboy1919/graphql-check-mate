package com.graphqlcheckmate.resolvers

import com.graphqlcheckmate.resolvers.resolverbases.MutationResolvers
import com.graphqlcheckmate.services.TodoListService
import viaduct.api.Resolver
import viaduct.api.grts.TodoItem

/**
 * Resolver for the createTodoItem mutation.
 * Creates a new TODO item in a list.
 */
@Resolver
class CreateTodoItemResolver(
    private val todoListService: TodoListService
) : MutationResolvers.CreateTodoItem() {
    override suspend fun resolve(ctx: Context): TodoItem {
        val input = ctx.arguments.input
        val todoListId = input.todoListId.internalID
        val orderIndex = input.orderIndex ?: 0

        val entity = todoListService.createTodoItem(
            authenticatedClient = ctx.authenticatedClient,
            todoListId = todoListId,
            text = input.text,
            orderIndex = orderIndex
        )

        return TodoItem.Builder(ctx)
            .id(ctx.globalIDFor(TodoItem.Reflection, entity.id))
            .todoListId(entity.todo_list_id)
            .text(entity.text)
            .completed(entity.completed)
            .orderIndex(entity.order_index)
            .createdAt(entity.created_at)
            .updatedAt(entity.updated_at)
            .build()
    }
}
