package com.graphqlcheckmate.resolvers

import com.graphqlcheckmate.resolvers.resolverbases.QueryResolvers
import com.graphqlcheckmate.services.BlogPostService
import viaduct.api.Resolver
import viaduct.api.grts.BlogPost

/**
 * Resolver for the myBlogPosts query.
 * Returns all blog posts authored by the current user.
 */
@Resolver
class MyBlogPostsQueryResolver(
    private val blogPostService: BlogPostService
) : QueryResolvers.MyBlogPosts() {
    override suspend fun resolve(ctx: Context): List<BlogPost> {
        val userId = ctx.userId

        val blogPostEntities = blogPostService.getBlogPostsByUserId(
            authenticatedClient = ctx.authenticatedClient,
            userId = userId
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
