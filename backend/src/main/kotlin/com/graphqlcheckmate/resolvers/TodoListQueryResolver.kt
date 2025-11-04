package com.graphqlcheckmate.resolvers

import com.graphqlcheckmate.resolvers.resolverbases.QueryResolvers
import com.graphqlcheckmate.services.TodoListService
import viaduct.api.Resolver
import viaduct.api.grts.TodoList

/**
 * Resolver for the todoList query.
 * Returns a specific TODO list by ID.
 */
@Resolver
class TodoListQueryResolver(
    private val todoListService: TodoListService
) : QueryResolvers.TodoList() {
    override suspend fun resolve(ctx: Context): TodoList? {
        val todoListId = ctx.arguments.id.internalID

        val entity = todoListService.getTodoListById(
            ctx.authenticatedClient,
            todoListId
        ) ?: return null

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
