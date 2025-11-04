-- Add TODO lists functionality
-- Users can create TODO lists within groups
-- Each TODO list has a subject and multiple items

-- Create todo_lists table
CREATE TABLE IF NOT EXISTS public.todo_lists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subject TEXT NOT NULL,
    group_id UUID NOT NULL REFERENCES public.groups(id) ON DELETE CASCADE,
    owner_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- Create todo_items table
CREATE TABLE IF NOT EXISTS public.todo_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    todo_list_id UUID NOT NULL REFERENCES public.todo_lists(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT false,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_todo_lists_group_id ON public.todo_lists(group_id);
CREATE INDEX IF NOT EXISTS idx_todo_lists_owner_id ON public.todo_lists(owner_id);
CREATE INDEX IF NOT EXISTS idx_todo_items_todo_list_id ON public.todo_items(todo_list_id);
CREATE INDEX IF NOT EXISTS idx_todo_items_order ON public.todo_items(todo_list_id, order_index);

-- Update trigger for todo_lists
CREATE OR REPLACE FUNCTION update_todo_lists_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_todo_lists_timestamp
    BEFORE UPDATE ON public.todo_lists
    FOR EACH ROW
    EXECUTE FUNCTION update_todo_lists_updated_at();

-- Update trigger for todo_items
CREATE OR REPLACE FUNCTION update_todo_items_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_todo_items_timestamp
    BEFORE UPDATE ON public.todo_items
    FOR EACH ROW
    EXECUTE FUNCTION update_todo_items_updated_at();

-- Enable RLS on new tables
ALTER TABLE public.todo_lists ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.todo_items ENABLE ROW LEVEL SECURITY;

-- RLS Policies for todo_lists
-- Users can view todo lists in groups they are members of
CREATE POLICY "Users can view todo lists in their groups"
    ON public.todo_lists
    FOR SELECT
    USING (public.is_group_member(group_id));

-- Users can create todo lists in groups they are members of
CREATE POLICY "Users can create todo lists in their groups"
    ON public.todo_lists
    FOR INSERT
    WITH CHECK (public.is_group_member(group_id) AND auth.uid() = owner_id);

-- Users can update todo lists they own in groups they are members of
CREATE POLICY "Users can update their own todo lists"
    ON public.todo_lists
    FOR UPDATE
    USING (owner_id = auth.uid() AND public.is_group_member(group_id));

-- Users can delete todo lists they own
CREATE POLICY "Users can delete their own todo lists"
    ON public.todo_lists
    FOR DELETE
    USING (owner_id = auth.uid());

-- RLS Policies for todo_items
-- Users can view todo items if they can view the parent todo list
CREATE POLICY "Users can view todo items in their groups"
    ON public.todo_items
    FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.todo_lists
            WHERE id = todo_items.todo_list_id
            AND public.is_group_member(group_id)
        )
    );

-- Users can create todo items in todo lists they can access
CREATE POLICY "Users can create todo items in accessible lists"
    ON public.todo_items
    FOR INSERT
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.todo_lists
            WHERE id = todo_items.todo_list_id
            AND public.is_group_member(group_id)
        )
    );

-- Users can update todo items in todo lists they can access
CREATE POLICY "Users can update todo items in accessible lists"
    ON public.todo_items
    FOR UPDATE
    USING (
        EXISTS (
            SELECT 1 FROM public.todo_lists
            WHERE id = todo_items.todo_list_id
            AND public.is_group_member(group_id)
        )
    );

-- Users can delete todo items in todo lists they own
CREATE POLICY "Users can delete todo items in their lists"
    ON public.todo_items
    FOR DELETE
    USING (
        EXISTS (
            SELECT 1 FROM public.todo_lists
            WHERE id = todo_items.todo_list_id
            AND owner_id = auth.uid()
        )
    );
