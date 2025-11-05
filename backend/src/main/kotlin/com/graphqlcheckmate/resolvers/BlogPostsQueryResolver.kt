package com.graphqlcheckmate.resolvers

import com.graphqlcheckmate.resolvers.resolverbases.QueryResolvers
import com.graphqlcheckmate.services.BlogPostService
import viaduct.api.Resolver
import viaduct.api.grts.BlogPost

/**
 * Resolver for the blogPosts query.
 * Returns blog posts based on the provided filters.
 */
@Resolver
class BlogPostsQueryResolver(
    private val blogPostService: BlogPostService
) : QueryResolvers.BlogPosts() {
    override suspend fun resolve(ctx: Context): List<BlogPost> {
        val groupId = ctx.arguments.groupId?.internalID
        val publishedOnly = ctx.arguments.publishedOnly ?: false

        val blogPostEntities = blogPostService.getBlogPosts(
            authenticatedClient = ctx.authenticatedClient,
            groupId = groupId,
            publishedOnly = publishedOnly
        )

        return blogPostEntities.map { entity ->
            BlogPost.Builder(ctx)
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
}
