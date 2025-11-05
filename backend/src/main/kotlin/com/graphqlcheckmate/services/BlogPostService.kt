package com.graphqlcheckmate.services

import com.graphqlcheckmate.AuthenticatedSupabaseClient
import com.graphqlcheckmate.BlogPostEntity
import com.graphqlcheckmate.SupabaseService

/**
 * Service for managing blog posts.
 * Handles blog post creation, updates, deletion, and queries.
 */
open class BlogPostService(
    internal val supabaseService: SupabaseService
) {
    /**
     * Get all blog posts the user has access to.
     * Optionally filter by group and/or published status.
     */
    suspend fun getBlogPosts(
        authenticatedClient: AuthenticatedSupabaseClient,
        groupId: String? = null,
        publishedOnly: Boolean = false
    ): List<BlogPostEntity> {
        return authenticatedClient.getBlogPosts(groupId, publishedOnly)
    }

    /**
     * Get a specific blog post by ID.
     */
    suspend fun getBlogPostById(
        authenticatedClient: AuthenticatedSupabaseClient,
        id: String
    ): BlogPostEntity? {
        return authenticatedClient.getBlogPostById(id)
    }

    /**
     * Get a blog post by slug within a specific group.
     */
    suspend fun getBlogPostBySlug(
        authenticatedClient: AuthenticatedSupabaseClient,
        groupId: String,
        slug: String
    ): BlogPostEntity? {
        return authenticatedClient.getBlogPostBySlug(groupId, slug)
    }

    /**
     * Get all blog posts authored by a specific user.
     */
    suspend fun getBlogPostsByUserId(
        authenticatedClient: AuthenticatedSupabaseClient,
        userId: String
    ): List<BlogPostEntity> {
        return authenticatedClient.getBlogPostsByUserId(userId)
    }

    /**
     * Create a new blog post.
     */
    suspend fun createBlogPost(
        authenticatedClient: AuthenticatedSupabaseClient,
        groupId: String,
        userId: String,
        title: String,
        slug: String?,
        content: String,
        published: Boolean
    ): BlogPostEntity {
        return authenticatedClient.createBlogPost(
            groupId = groupId,
            userId = userId,
            title = title,
            slug = slug,
            content = content,
            published = published
        )
    }

    /**
     * Update an existing blog post.
     */
    suspend fun updateBlogPost(
        authenticatedClient: AuthenticatedSupabaseClient,
        id: String,
        title: String? = null,
        slug: String? = null,
        content: String? = null,
        published: Boolean? = null
    ): BlogPostEntity {
        return authenticatedClient.updateBlogPost(
            id = id,
            title = title,
            slug = slug,
            content = content,
            published = published
        )
    }

    /**
     * Delete a blog post.
     */
    suspend fun deleteBlogPost(
        authenticatedClient: AuthenticatedSupabaseClient,
        id: String
    ): Boolean {
        return authenticatedClient.deleteBlogPost(id)
    }
}
