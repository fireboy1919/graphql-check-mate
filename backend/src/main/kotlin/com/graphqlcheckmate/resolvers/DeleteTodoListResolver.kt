package com.graphqlcheckmate.resolvers

import com.graphqlcheckmate.resolvers.resolverbases.MutationResolvers
import com.graphqlcheckmate.services.TodoListService
import viaduct.api.Resolver

/**
 * Resolver for the deleteTodoList mutation.
 * Deletes a TODO list and all its items.
 */
@Resolver
class DeleteTodoListResolver(
    private val todoListService: TodoListService
) : MutationResolvers.DeleteTodoList() {
    override suspend fun resolve(ctx: Context): Boolean {
        val input = ctx.arguments.input
        val todoListId = input.id.internalID

        return todoListService.deleteTodoList(
            authenticatedClient = ctx.authenticatedClient,
            id = todoListId
        )
    }
}
