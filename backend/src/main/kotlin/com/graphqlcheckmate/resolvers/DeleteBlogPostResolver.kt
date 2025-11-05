package com.graphqlcheckmate.resolvers

import com.graphqlcheckmate.resolvers.resolverbases.MutationResolvers
import com.graphqlcheckmate.services.BlogPostService
import viaduct.api.Resolver

/**
 * Resolver for the deleteBlogPost mutation.
 * Deletes a blog post.
 */
@Resolver
class DeleteBlogPostResolver(
    private val blogPostService: BlogPostService
) : MutationResolvers.DeleteBlogPost() {
    override suspend fun resolve(ctx: Context): Boolean {
        val input = ctx.arguments.input
        val id = input.id.internalID

        return blogPostService.deleteBlogPost(
            authenticatedClient = ctx.authenticatedClient,
            id = id
        )
    }
}
