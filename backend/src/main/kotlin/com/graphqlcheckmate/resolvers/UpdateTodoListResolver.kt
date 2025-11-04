package com.graphqlcheckmate.resolvers

import com.graphqlcheckmate.resolvers.resolverbases.MutationResolvers
import com.graphqlcheckmate.services.TodoListService
import viaduct.api.Resolver
import viaduct.api.grts.TodoList

/**
 * Resolver for the updateTodoList mutation.
 * Updates an existing TODO list.
 */
@Resolver
class UpdateTodoListResolver(
    private val todoListService: TodoListService
) : MutationResolvers.UpdateTodoList() {
    override suspend fun resolve(ctx: Context): TodoList {
        val input = ctx.arguments.input
        val todoListId = input.id.internalID

        val entity = todoListService.updateTodoList(
            authenticatedClient = ctx.authenticatedClient,
            id = todoListId,
            subject = input.subject
        )

        return TodoList.Builder(ctx)
            .id(ctx.globalIDFor(TodoList.Reflection, entity.id))
            .subject(entity.subject)
            .groupId(entity.group_id)
            .ownerId(entity.owner_id)
            .createdAt(entity.created_at)
            .updatedAt(entity.updated_at)
            .build()
    }
}
