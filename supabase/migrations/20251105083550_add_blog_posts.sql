-- Add blog posts functionality
-- Users can create blog posts within groups
-- Blog posts can be published (public) or draft (private to group members)

-- ============================================================================
-- TABLE CREATION
-- ============================================================================

-- Create the blog_posts table
CREATE TABLE IF NOT EXISTS public.blog_posts (
    -- Primary key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Foreign key to groups (for group-based access control)
    group_id UUID NOT NULL REFERENCES public.groups(id) ON DELETE CASCADE,

    -- Foreign key to user (author of the post)
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,

    -- Blog post fields
    title TEXT NOT NULL,
    slug TEXT NOT NULL,
    content TEXT NOT NULL DEFAULT '',
    published BOOLEAN NOT NULL DEFAULT false,

    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    -- Ensure slug is unique within a group
    UNIQUE(group_id, slug)
);

-- ============================================================================
-- INDEXES
-- ============================================================================

-- Index for group-based queries
CREATE INDEX IF NOT EXISTS idx_blog_posts_group_id
    ON public.blog_posts(group_id);

-- Index for author queries
CREATE INDEX IF NOT EXISTS idx_blog_posts_user_id
    ON public.blog_posts(user_id);

-- Index for chronological ordering
CREATE INDEX IF NOT EXISTS idx_blog_posts_created_at
    ON public.blog_posts(created_at DESC);

-- Composite index for published posts by group
CREATE INDEX IF NOT EXISTS idx_blog_posts_group_published
    ON public.blog_posts(group_id, published, created_at DESC);

-- Index for slug lookups
CREATE INDEX IF NOT EXISTS idx_blog_posts_slug
    ON public.blog_posts(group_id, slug);

-- ============================================================================
-- ROW-LEVEL SECURITY (RLS)
-- ============================================================================

-- Enable RLS on the table
ALTER TABLE public.blog_posts ENABLE ROW LEVEL SECURITY;

-- ============================================================================
-- RLS POLICIES
-- ============================================================================

-- SELECT Policy: Who can view blog posts
-- - Published posts can be viewed by anyone in the group
-- - Draft posts can only be viewed by the author and group members
-- - Admins can view everything
CREATE POLICY "Users can view blog posts"
    ON public.blog_posts
    FOR SELECT
    USING (
        public.is_group_member(group_id)
        OR public.is_admin()
    );

-- INSERT Policy: Who can create blog posts
-- Users can create posts in groups they are members of
CREATE POLICY "Users can create blog posts in their groups"
    ON public.blog_posts
    FOR INSERT
    WITH CHECK (
        public.is_group_member(group_id)
        OR public.is_admin()
    );

-- UPDATE Policy: Who can modify blog posts
-- Authors can update their own posts, admins can update any post
CREATE POLICY "Authors can update their blog posts"
    ON public.blog_posts
    FOR UPDATE
    USING (
        user_id = auth.uid()
        OR public.is_admin()
    );

-- DELETE Policy: Who can delete blog posts
-- Authors can delete their own posts, admins can delete any post
CREATE POLICY "Authors can delete their blog posts"
    ON public.blog_posts
    FOR DELETE
    USING (
        user_id = auth.uid()
        OR public.is_admin()
    );

-- ============================================================================
-- TRIGGERS
-- ============================================================================

-- Trigger to automatically update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_blog_posts_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_blog_posts_timestamp
    BEFORE UPDATE ON public.blog_posts
    FOR EACH ROW
    EXECUTE FUNCTION update_blog_posts_updated_at();

-- Function to generate slug from title
CREATE OR REPLACE FUNCTION generate_slug(title TEXT)
RETURNS TEXT AS $$
BEGIN
    RETURN lower(
        regexp_replace(
            regexp_replace(
                trim(title),
                '[^a-zA-Z0-9\s-]', '', 'g'
            ),
            '\s+', '-', 'g'
        )
    );
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Trigger to auto-generate slug if not provided
CREATE OR REPLACE FUNCTION auto_generate_slug()
RETURNS TRIGGER AS $$
BEGIN
    -- Only generate slug if it's empty or null
    IF NEW.slug IS NULL OR trim(NEW.slug) = '' THEN
        NEW.slug = generate_slug(NEW.title);

        -- Ensure uniqueness by appending timestamp if needed
        IF EXISTS (
            SELECT 1 FROM public.blog_posts
            WHERE group_id = NEW.group_id
            AND slug = NEW.slug
            AND id != COALESCE(NEW.id, '00000000-0000-0000-0000-000000000000'::uuid)
        ) THEN
            NEW.slug = NEW.slug || '-' || extract(epoch from now())::bigint::text;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER auto_generate_blog_post_slug
    BEFORE INSERT OR UPDATE ON public.blog_posts
    FOR EACH ROW
    EXECUTE FUNCTION auto_generate_slug();
