import { useState, useEffect } from "react";
import { useToast } from "@/hooks/use-toast";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Users, Plus, Trash2, UserPlus } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  executeGraphQL,
  GET_GROUPS,
  CREATE_GROUP,
  ADD_GROUP_MEMBER,
  REMOVE_GROUP_MEMBER,
  SEARCH_USERS,
} from "@/lib/graphql";

interface Group {
  id: string;
  name: string;
  description?: string;
  ownerId: string;
  members: { id: string; userId: string; joinedAt: string }[];
}

interface User {
  id: string;
  email: string;
}

interface GroupSelectorProps {
  userId: string;
  onSelectGroup: (groupId: string, groupName: string) => void;
  selectedGroupId?: string;
}

export function GroupSelector({ userId, onSelectGroup, selectedGroupId }: GroupSelectorProps) {
  const [groups, setGroups] = useState<Group[]>([]);
  const [newGroupName, setNewGroupName] = useState("");
  const [newGroupDescription, setNewGroupDescription] = useState("");
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [showMembersDialog, setShowMembersDialog] = useState(false);
  const [selectedGroup, setSelectedGroup] = useState<Group | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const { toast } = useToast();

  const loadGroups = async () => {
    try {
      setIsLoading(true);
      const data = await executeGraphQL<{ groups: Group[] }>(GET_GROUPS);
      setGroups(data.groups || []);
    } catch (error) {
      console.error("Failed to load groups:", error);
      toast({
        title: "Error",
        description: "Failed to load groups. Please try again.",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadGroups();
  }, []);

  const handleCreateGroup = async () => {
    if (!newGroupName.trim()) return;

    try {
      await executeGraphQL(CREATE_GROUP, {
        name: newGroupName,
        description: newGroupDescription || null,
      });
      setNewGroupName("");
      setNewGroupDescription("");
      setShowCreateDialog(false);
      await loadGroups();
      toast({
        title: "Success",
        description: "Group created successfully",
      });
    } catch (error) {
      console.error("Failed to create group:", error);
      toast({
        title: "Error",
        description: "Failed to create group. Please try again.",
        variant: "destructive",
      });
    }
  };

  const handleSearchUsers = async () => {
    if (!searchQuery.trim()) return;

    try {
      const data = await executeGraphQL<{ searchUsers: User[] }>(SEARCH_USERS, {
        query: searchQuery,
      });
      setSearchResults(data.searchUsers || []);
    } catch (error) {
      console.error("Failed to search users:", error);
      toast({
        title: "Error",
        description: "Failed to search users. Please try again.",
        variant: "destructive",
      });
    }
  };

  const handleAddMember = async (addUserId: string) => {
    if (!selectedGroup) return;

    try {
      await executeGraphQL(ADD_GROUP_MEMBER, {
        groupId: selectedGroup.id,
        userId: addUserId,
      });
      setSearchQuery("");
      setSearchResults([]);
      await loadGroups();
      // Update selected group
      const updatedGroups = await executeGraphQL<{ groups: Group[] }>(GET_GROUPS);
      const updatedGroup = updatedGroups.groups?.find((g) => g.id === selectedGroup.id);
      if (updatedGroup) setSelectedGroup(updatedGroup);
      toast({
        title: "Success",
        description: "Member added successfully",
      });
    } catch (error) {
      console.error("Failed to add member:", error);
      toast({
        title: "Error",
        description: "Failed to add member. Please try again.",
        variant: "destructive",
      });
    }
  };

  const handleRemoveMember = async (removeUserId: string) => {
    if (!selectedGroup) return;

    try {
      await executeGraphQL(REMOVE_GROUP_MEMBER, {
        groupId: selectedGroup.id,
        userId: removeUserId,
      });
      await loadGroups();
      // Update selected group
      const updatedGroups = await executeGraphQL<{ groups: Group[] }>(GET_GROUPS);
      const updatedGroup = updatedGroups.groups?.find((g) => g.id === selectedGroup.id);
      if (updatedGroup) setSelectedGroup(updatedGroup);
      toast({
        title: "Success",
        description: "Member removed successfully",
      });
    } catch (error) {
      console.error("Failed to remove member:", error);
      toast({
        title: "Error",
        description: "Failed to remove member. Please try again.",
        variant: "destructive",
      });
    }
  };

  if (isLoading) {
    return <div className="text-center py-8">Loading groups...</div>;
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Users className="h-5 w-5 text-primary" />
            <CardTitle>Your Groups</CardTitle>
          </div>
          <Dialog open={showCreateDialog} onOpenChange={setShowCreateDialog}>
            <DialogTrigger asChild>
              <Button size="sm">
                <Plus className="h-4 w-4 mr-2" />
                Create Group
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Create New Group</DialogTitle>
                <DialogDescription>
                  Create a new group to organize your TODO lists
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4">
                <Input
                  placeholder="Group name"
                  value={newGroupName}
                  onChange={(e) => setNewGroupName(e.target.value)}
                />
                <Input
                  placeholder="Description (optional)"
                  value={newGroupDescription}
                  onChange={(e) => setNewGroupDescription(e.target.value)}
                />
                <Button onClick={handleCreateGroup} className="w-full">
                  Create Group
                </Button>
              </div>
            </DialogContent>
          </Dialog>
        </div>
        <CardDescription>
          Select a group to view and manage its TODO lists
        </CardDescription>
      </CardHeader>
      <CardContent>
        {groups.length === 0 ? (
          <div className="text-center py-8 text-muted-foreground">
            No groups yet. Create one to get started!
          </div>
        ) : (
          <div className="grid gap-2">
            {groups.map((group) => (
              <div
                key={group.id}
                className={`flex items-center justify-between p-3 rounded-lg border-2 cursor-pointer transition-all ${
                  selectedGroupId === group.id
                    ? "border-primary bg-primary/5"
                    : "border-border hover:border-primary/50"
                }`}
                onClick={() => onSelectGroup(group.id, group.name)}
              >
                <div>
                  <h3 className="font-semibold">{group.name}</h3>
                  {group.description && (
                    <p className="text-sm text-muted-foreground">{group.description}</p>
                  )}
                  <p className="text-xs text-muted-foreground mt-1">
                    {group.members?.length || 0} members
                  </p>
                </div>
                {group.ownerId === userId && (
                  <Dialog open={showMembersDialog && selectedGroup?.id === group.id} onOpenChange={(open) => {
                    setShowMembersDialog(open);
                    if (open) setSelectedGroup(group);
                    else setSelectedGroup(null);
                  }}>
                    <DialogTrigger asChild onClick={(e) => e.stopPropagation()}>
                      <Button size="sm" variant="outline">
                        <UserPlus className="h-4 w-4" />
                      </Button>
                    </DialogTrigger>
                    <DialogContent>
                      <DialogHeader>
                        <DialogTitle>Manage Members - {group.name}</DialogTitle>
                        <DialogDescription>
                          Add or remove members from this group
                        </DialogDescription>
                      </DialogHeader>
                      <div className="space-y-4">
                        {/* Current members */}
                        <div>
                          <h4 className="font-medium mb-2">Current Members</h4>
                          <div className="space-y-2">
                            {group.members?.map((member) => (
                              <div
                                key={member.id}
                                className="flex items-center justify-between p-2 rounded border"
                              >
                                <span className="text-sm">{member.userId}</span>
                                {member.userId !== userId && (
                                  <Button
                                    size="sm"
                                    variant="ghost"
                                    onClick={() => handleRemoveMember(member.userId)}
                                  >
                                    <Trash2 className="h-3 w-3" />
                                  </Button>
                                )}
                              </div>
                            ))}
                          </div>
                        </div>

                        {/* Add new member */}
                        <div>
                          <h4 className="font-medium mb-2">Add Member</h4>
                          <div className="flex gap-2">
                            <Input
                              placeholder="Search by email..."
                              value={searchQuery}
                              onChange={(e) => setSearchQuery(e.target.value)}
                              onKeyPress={(e) => e.key === "Enter" && handleSearchUsers()}
                            />
                            <Button onClick={handleSearchUsers}>Search</Button>
                          </div>
                          {searchResults.length > 0 && (
                            <div className="mt-2 space-y-2">
                              {searchResults.map((user) => (
                                <div
                                  key={user.id}
                                  className="flex items-center justify-between p-2 rounded border"
                                >
                                  <span className="text-sm">{user.email}</span>
                                  <Button
                                    size="sm"
                                    onClick={() => handleAddMember(user.id)}
                                  >
                                    Add
                                  </Button>
                                </div>
                              ))}
                            </div>
                          )}
                        </div>
                      </div>
                    </DialogContent>
                  </Dialog>
                )}
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
