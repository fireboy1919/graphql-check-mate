package com.graphqlcheckmate.resolvers

import com.graphqlcheckmate.resolvers.resolverbases.MutationResolvers
import com.graphqlcheckmate.services.TodoListService
import viaduct.api.Resolver
import viaduct.api.grts.TodoList

/**
 * Resolver for the createTodoList mutation.
 * Creates a new TODO list in a group.
 */
@Resolver
class CreateTodoListResolver(
    private val todoListService: TodoListService
) : MutationResolvers.CreateTodoList() {
    override suspend fun resolve(ctx: Context): TodoList {
        val input = ctx.arguments.input
        val userId = ctx.userId
        val groupId = input.groupId.internalID

        val entity = todoListService.createTodoList(
            authenticatedClient = ctx.authenticatedClient,
            subject = input.subject,
            groupId = groupId,
            ownerId = userId
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
