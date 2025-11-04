package com.graphqlcheckmate.resolvers

import com.graphqlcheckmate.resolvers.resolverbases.MutationResolvers
import com.graphqlcheckmate.services.TodoListService
import viaduct.api.Resolver

/**
 * Resolver for the deleteTodoItem mutation.
 * Deletes a TODO item.
 */
@Resolver
class DeleteTodoItemResolver(
    private val todoListService: TodoListService
) : MutationResolvers.DeleteTodoItem() {
    override suspend fun resolve(ctx: Context): Boolean {
        val input = ctx.arguments.input
        val todoItemId = input.id.internalID

        return todoListService.deleteTodoItem(
            authenticatedClient = ctx.authenticatedClient,
            id = todoItemId
        )
    }
}
