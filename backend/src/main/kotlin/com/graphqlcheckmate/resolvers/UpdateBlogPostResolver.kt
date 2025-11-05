package com.graphqlcheckmate.resolvers

import com.graphqlcheckmate.resolvers.resolverbases.MutationResolvers
import com.graphqlcheckmate.services.BlogPostService
import viaduct.api.Resolver
import viaduct.api.grts.BlogPost

/**
 * Resolver for the updateBlogPost mutation.
 * Updates an existing blog post.
 */
@Resolver
class UpdateBlogPostResolver(
    private val blogPostService: BlogPostService
) : MutationResolvers.UpdateBlogPost() {
    override suspend fun resolve(ctx: Context): BlogPost {
        val input = ctx.arguments.input
        val id = input.id.internalID

        val entity = blogPostService.updateBlogPost(
            authenticatedClient = ctx.authenticatedClient,
            id = id,
            title = input.title,
            slug = input.slug,
            content = input.content,
            published = input.published
        )

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
