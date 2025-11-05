package com.graphqlcheckmate.resolvers

import com.graphqlcheckmate.resolvers.resolverbases.MutationResolvers
import com.graphqlcheckmate.services.BlogPostService
import viaduct.api.Resolver
import viaduct.api.grts.BlogPost

/**
 * Resolver for the createBlogPost mutation.
 * Creates a new blog post in the specified group.
 */
@Resolver
class CreateBlogPostResolver(
    private val blogPostService: BlogPostService
) : MutationResolvers.CreateBlogPost() {
    override suspend fun resolve(ctx: Context): BlogPost {
        val input = ctx.arguments.input
        val userId = ctx.userId
        val groupId = input.groupId.internalID

        val entity = blogPostService.createBlogPost(
            authenticatedClient = ctx.authenticatedClient,
            groupId = groupId,
            userId = userId,
            title = input.title,
            slug = input.slug,
            content = input.content,
            published = input.published ?: false
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
