package com.graphqlcheckmate.resolvers

import com.graphqlcheckmate.resolvers.resolverbases.QueryResolvers
import com.graphqlcheckmate.services.BlogPostService
import viaduct.api.Resolver
import viaduct.api.grts.BlogPost

/**
 * Resolver for the blogPostBySlug query.
 * Returns a blog post by slug within a specific group.
 */
@Resolver
class BlogPostBySlugQueryResolver(
    private val blogPostService: BlogPostService
) : QueryResolvers.BlogPostBySlug() {
    override suspend fun resolve(ctx: Context): BlogPost? {
        val groupId = ctx.arguments.groupId.internalID
        val slug = ctx.arguments.slug

        val entity = blogPostService.getBlogPostBySlug(
            authenticatedClient = ctx.authenticatedClient,
            groupId = groupId,
            slug = slug
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
