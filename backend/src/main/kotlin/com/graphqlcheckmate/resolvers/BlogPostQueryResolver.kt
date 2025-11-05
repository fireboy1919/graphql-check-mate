package com.graphqlcheckmate.resolvers

import com.graphqlcheckmate.resolvers.resolverbases.QueryResolvers
import com.graphqlcheckmate.services.BlogPostService
import viaduct.api.Resolver
import viaduct.api.grts.BlogPost

/**
 * Resolver for the blogPost query.
 * Returns a specific blog post by ID.
 */
@Resolver
class BlogPostQueryResolver(
    private val blogPostService: BlogPostService
) : QueryResolvers.BlogPost() {
    override suspend fun resolve(ctx: Context): BlogPost? {
        val id = ctx.arguments.id.internalID

        val entity = blogPostService.getBlogPostById(
            authenticatedClient = ctx.authenticatedClient,
            id = id
        ) ?: return null

        return BlogPost.Builder(ctx)
            .id(ctx.globalIDFor(BlogPost.Reflection, entity.id))
            .groupId(entity.group_id)
            .userId(entity.user_id)
            .title(entity.title)
            .slug(entity.slug)
            .content(entity.content)
            .published(entity.published)
            .createdAt(entity.created_at)
            .updatedAt(entity.updated_at)
            .build()
    }
}
