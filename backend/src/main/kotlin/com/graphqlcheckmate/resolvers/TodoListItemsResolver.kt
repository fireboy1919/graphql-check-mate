package com.graphqlcheckmate.resolvers

import com.graphqlcheckmate.resolvers.resolverbases.TodoListResolvers
import com.graphqlcheckmate.services.TodoListService
import viaduct.api.Resolver
import viaduct.api.grts.TodoItem

/**
 * Field resolver for TodoList.items.
 * Returns all items for the TODO list.
 */
@Resolver(objectValueFragment = "fragment _ on TodoList { id }")
class TodoListItemsResolver(
    private val todoListService: TodoListService
) : TodoListResolvers.Items() {
    override suspend fun resolve(ctx: Context): List<TodoItem> {
        // Access parent TodoList via objectValue
        val todoListId = ctx.objectValue.getId().internalID

        val itemEntities = todoListService.getTodoItemsByList(
            ctx.authenticatedClient,
            todoListId
        )

        return itemEntities.map { entity ->
            TodoItem.Builder(ctx)
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
}
