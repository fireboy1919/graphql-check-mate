package com.graphqlcheckmate.resolvers

import com.graphqlcheckmate.resolvers.resolverbases.MutationResolvers
import com.graphqlcheckmate.services.TodoListService
import viaduct.api.Resolver
import viaduct.api.grts.TodoItem

/**
 * Resolver for the updateTodoItem mutation.
 * Updates an existing TODO item.
 */
@Resolver
class UpdateTodoItemResolver(
    private val todoListService: TodoListService
) : MutationResolvers.UpdateTodoItem() {
    override suspend fun resolve(ctx: Context): TodoItem {
        val input = ctx.arguments.input
        val todoItemId = input.id.internalID

        val entity = todoListService.updateTodoItem(
            authenticatedClient = ctx.authenticatedClient,
            id = todoItemId,
            text = input.text,
            completed = input.completed,
            orderIndex = input.orderIndex
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
