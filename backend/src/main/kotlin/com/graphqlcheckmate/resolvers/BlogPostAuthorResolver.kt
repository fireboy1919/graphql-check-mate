package com.graphqlcheckmate.resolvers

import com.graphqlcheckmate.resolvers.resolverbases.BlogPostResolvers
import com.graphqlcheckmate.services.UserService
import viaduct.api.Resolver
import viaduct.api.grts.User

/**
 * Resolver for the author field on BlogPost.
 * Returns the user who authored the blog post.
 */
@Resolver(objectValueFragment = "fragment _ on BlogPost { userId }")
class BlogPostAuthorResolver(
    private val userService: UserService
) : BlogPostResolvers.Author() {
    override suspend fun resolve(ctx: Context): User? {
        val userId = ctx.objectValue.getUserId()

        val userEntity = userService.getUserById(
            authenticatedClient = ctx.authenticatedClient,
            userId = userId
        ) ?: return null

        return User.Builder(ctx)
            .id(userEntity.id)
            .email(userEntity.email)
            .createdAt(userEntity.created_at)
            .build()
    }
}
