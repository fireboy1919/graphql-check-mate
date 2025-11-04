import { useState, useEffect } from "react";
import { useToast } from "@/hooks/use-toast";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import { Plus, Trash2, Edit2, Check, X, ListTodo } from "lucide-react";
import {
  executeGraphQL,
  GET_TODO_LISTS,
  CREATE_TODO_LIST,
  UPDATE_TODO_LIST,
  DELETE_TODO_LIST,
  CREATE_TODO_ITEM,
  UPDATE_TODO_ITEM,
  DELETE_TODO_ITEM,
} from "@/lib/graphql";

interface TodoItem {
  id: string;
  text: string;
  completed: boolean;
  orderIndex: number;
}

interface TodoList {
  id: string;
  subject: string;
  groupId: string;
  ownerId: string;
  items: TodoItem[];
}

interface TodoListManagerProps {
  groupId: string;
  groupName: string;
  userId: string;
}

export function TodoListManager({ groupId, groupName, userId }: TodoListManagerProps) {
  const [todoLists, setTodoLists] = useState<TodoList[]>([]);
  const [newListSubject, setNewListSubject] = useState("");
  const [newItemTexts, setNewItemTexts] = useState<Record<string, string>>({});
  const [editingSubject, setEditingSubject] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);
  const { toast } = useToast();

  const loadTodoLists = async () => {
    try {
      setIsLoading(true);
      const data = await executeGraphQL<{ todoLists: TodoList[] }>(GET_TODO_LISTS, {
        groupId,
      });
      setTodoLists(data.todoLists || []);
    } catch (error) {
      console.error("Failed to load TODO lists:", error);
      toast({
        title: "Error",
        description: "Failed to load TODO lists. Please try again.",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (groupId) {
      loadTodoLists();
    }
  }, [groupId]);

  const handleCreateList = async () => {
    if (!newListSubject.trim()) return;

    try {
      await executeGraphQL(CREATE_TODO_LIST, {
        groupId,
        subject: newListSubject,
      });
      setNewListSubject("");
      await loadTodoLists();
      toast({
        title: "Success",
        description: "TODO list created successfully",
      });
    } catch (error) {
      console.error("Failed to create TODO list:", error);
      toast({
        title: "Error",
        description: "Failed to create TODO list. Please try again.",
        variant: "destructive",
      });
    }
  };

  const handleUpdateListSubject = async (listId: string, subject: string) => {
    if (!subject.trim()) return;

    try {
      await executeGraphQL(UPDATE_TODO_LIST, {
        id: listId,
        subject,
      });
      setEditingSubject((prev) => {
        const next = { ...prev };
        delete next[listId];
        return next;
      });
      await loadTodoLists();
      toast({
        title: "Success",
        description: "TODO list updated successfully",
      });
    } catch (error) {
      console.error("Failed to update TODO list:", error);
      toast({
        title: "Error",
        description: "Failed to update TODO list. Please try again.",
        variant: "destructive",
      });
    }
  };

  const handleDeleteList = async (listId: string) => {
    try {
      await executeGraphQL(DELETE_TODO_LIST, {
        id: listId,
      });
      await loadTodoLists();
      toast({
        title: "Success",
        description: "TODO list deleted successfully",
      });
    } catch (error) {
      console.error("Failed to delete TODO list:", error);
      toast({
        title: "Error",
        description: "Failed to delete TODO list. Please try again.",
        variant: "destructive",
      });
    }
  };

  const handleCreateItem = async (listId: string) => {
    const text = newItemTexts[listId];
    if (!text?.trim()) return;

    try {
      const list = todoLists.find((l) => l.id === listId);
      const orderIndex = list?.items?.length || 0;

      await executeGraphQL(CREATE_TODO_ITEM, {
        todoListId: listId,
        text,
        orderIndex,
      });

      setNewItemTexts((prev) => {
        const next = { ...prev };
        delete next[listId];
        return next;
      });

      await loadTodoLists();
      toast({
        title: "Success",
        description: "TODO item added successfully",
      });
    } catch (error) {
      console.error("Failed to create TODO item:", error);
      toast({
        title: "Error",
        description: "Failed to create TODO item. Please try again.",
        variant: "destructive",
      });
    }
  };

  const handleToggleItem = async (itemId: string, completed: boolean) => {
    try {
      await executeGraphQL(UPDATE_TODO_ITEM, {
        id: itemId,
        completed: !completed,
      });
      await loadTodoLists();
    } catch (error) {
      console.error("Failed to toggle TODO item:", error);
      toast({
        title: "Error",
        description: "Failed to update TODO item. Please try again.",
        variant: "destructive",
      });
    }
  };

  const handleDeleteItem = async (itemId: string) => {
    try {
      await executeGraphQL(DELETE_TODO_ITEM, {
        id: itemId,
      });
      await loadTodoLists();
      toast({
        title: "Success",
        description: "TODO item deleted successfully",
      });
    } catch (error) {
      console.error("Failed to delete TODO item:", error);
      toast({
        title: "Error",
        description: "Failed to delete TODO item. Please try again.",
        variant: "destructive",
      });
    }
  };

  if (isLoading) {
    return <div className="text-center py-8">Loading TODO lists...</div>;
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <ListTodo className="h-5 w-5 text-primary" />
            <CardTitle>TODO Lists - {groupName}</CardTitle>
          </div>
          <CardDescription>
            Create and manage TODO lists within your group
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Create new list */}
          <div className="flex gap-2">
            <Input
              placeholder="New TODO list subject..."
              value={newListSubject}
              onChange={(e) => setNewListSubject(e.target.value)}
              onKeyPress={(e) => e.key === "Enter" && handleCreateList()}
            />
            <Button onClick={handleCreateList} size="sm">
              <Plus className="h-4 w-4 mr-2" />
              Create List
            </Button>
          </div>

          {/* Lists */}
          {todoLists.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              No TODO lists yet. Create one to get started!
            </div>
          ) : (
            <div className="space-y-4">
              {todoLists.map((list) => (
                <Card key={list.id} className="border-2">
                  <CardHeader className="pb-3">
                    <div className="flex items-center justify-between gap-2">
                      {editingSubject[list.id] !== undefined ? (
                        <div className="flex items-center gap-2 flex-1">
                          <Input
                            value={editingSubject[list.id]}
                            onChange={(e) =>
                              setEditingSubject((prev) => ({
                                ...prev,
                                [list.id]: e.target.value,
                              }))
                            }
                            onKeyPress={(e) =>
                              e.key === "Enter" &&
                              handleUpdateListSubject(list.id, editingSubject[list.id])
                            }
                          />
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={() =>
                              handleUpdateListSubject(list.id, editingSubject[list.id])
                            }
                          >
                            <Check className="h-4 w-4" />
                          </Button>
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={() =>
                              setEditingSubject((prev) => {
                                const next = { ...prev };
                                delete next[list.id];
                                return next;
                              })
                            }
                          >
                            <X className="h-4 w-4" />
                          </Button>
                        </div>
                      ) : (
                        <>
                          <CardTitle className="text-lg">{list.subject}</CardTitle>
                          <div className="flex items-center gap-2">
                            {list.ownerId === userId && (
                              <>
                                <Button
                                  size="sm"
                                  variant="ghost"
                                  onClick={() =>
                                    setEditingSubject((prev) => ({
                                      ...prev,
                                      [list.id]: list.subject,
                                    }))
                                  }
                                >
                                  <Edit2 className="h-4 w-4" />
                                </Button>
                                <Button
                                  size="sm"
                                  variant="ghost"
                                  onClick={() => handleDeleteList(list.id)}
                                >
                                  <Trash2 className="h-4 w-4" />
                                </Button>
                              </>
                            )}
                          </div>
                        </>
                      )}
                    </div>
                  </CardHeader>
                  <CardContent className="space-y-2">
                    {/* Items */}
                    {list.items
                      ?.sort((a, b) => a.orderIndex - b.orderIndex)
                      .map((item) => (
                        <div
                          key={item.id}
                          className="flex items-center gap-2 p-2 rounded hover:bg-accent"
                        >
                          <Checkbox
                            checked={item.completed}
                            onCheckedChange={() =>
                              handleToggleItem(item.id, item.completed)
                            }
                          />
                          <span
                            className={`flex-1 ${
                              item.completed
                                ? "line-through text-muted-foreground"
                                : ""
                            }`}
                          >
                            {item.text}
                          </span>
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={() => handleDeleteItem(item.id)}
                          >
                            <Trash2 className="h-3 w-3" />
                          </Button>
                        </div>
                      ))}

                    {/* Add new item */}
                    <div className="flex gap-2 mt-4">
                      <Input
                        placeholder="Add a new item..."
                        value={newItemTexts[list.id] || ""}
                        onChange={(e) =>
                          setNewItemTexts((prev) => ({
                            ...prev,
                            [list.id]: e.target.value,
                          }))
                        }
                        onKeyPress={(e) =>
                          e.key === "Enter" && handleCreateItem(list.id)
                        }
                      />
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => handleCreateItem(list.id)}
                      >
                        <Plus className="h-4 w-4" />
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
