package com.graphqlcheckmate.resolvers

import com.graphqlcheckmate.resolvers.resolverbases.QueryResolvers
import com.graphqlcheckmate.services.TodoListService
import viaduct.api.Resolver
import viaduct.api.grts.TodoList

/**
 * Resolver for the todoLists query.
 * Returns all TODO lists for a specific group.
 */
@Resolver
class TodoListsQueryResolver(
    private val todoListService: TodoListService
) : QueryResolvers.TodoLists() {
    override suspend fun resolve(ctx: Context): List<TodoList> {
        val groupId = ctx.arguments.groupId.internalID

        val todoListEntities = todoListService.getTodoListsByGroup(
            ctx.authenticatedClient,
            groupId
        )

        return todoListEntities.map { entity ->
            TodoList.Builder(ctx)
                .id(ctx.globalIDFor(TodoList.Reflection, entity.id))
                .subject(entity.subject)
                .groupId(entity.group_id)
                .ownerId(entity.owner_id)
                .createdAt(entity.created_at)
                .updatedAt(entity.updated_at)
                .build()
        }
    }
}
