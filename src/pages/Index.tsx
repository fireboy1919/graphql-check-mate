import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { supabase } from "@/integrations/supabase/client";
import { Session } from "@supabase/supabase-js";
import { UserList } from "@/components/UserList";
import { GroupSelector } from "@/components/GroupSelector";
import { TodoListManager } from "@/components/TodoListManager";
import { Button } from "@/components/ui/button";
import { LogOut, Shield, ListTodo } from "lucide-react";
import { useToast } from "@/hooks/use-toast";

const Index = () => {
  const [session, setSession] = useState<Session | null>(null);
  const [isAdmin, setIsAdmin] = useState(false);
  const [selectedGroupId, setSelectedGroupId] = useState<string | null>(null);
  const [selectedGroupName, setSelectedGroupName] = useState<string>("");
  const navigate = useNavigate();
  const { toast } = useToast();

  useEffect(() => {
    supabase.auth.getSession().then(({ data: { session } }) => {
      setSession(session);
      if (session?.user?.app_metadata?.is_admin) {
        setIsAdmin(true);
      }
      if (!session) {
        navigate("/auth");
      }
    });

    const {
      data: { subscription },
    } = supabase.auth.onAuthStateChange((_event, session) => {
      setSession(session);
      if (session?.user?.app_metadata?.is_admin) {
        setIsAdmin(true);
      } else {
        setIsAdmin(false);
      }
      if (!session) {
        navigate("/auth");
      }
    });

    return () => subscription.unsubscribe();
  }, [navigate]);

  const handleSignOut = async () => {
    await supabase.auth.signOut();
    navigate("/auth");
  };

  const handleSelectGroup = (groupId: string, groupName: string) => {
    setSelectedGroupId(groupId);
    setSelectedGroupName(groupName);
  };

  if (!session) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-primary/5 to-accent/5 p-4 md:p-8">
      <div className="max-w-6xl mx-auto space-y-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-xl bg-gradient-to-br from-primary to-primary-glow">
              <ListTodo className="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 className="text-3xl font-bold bg-gradient-to-r from-primary to-primary-glow bg-clip-text text-transparent">
                TODO Lists Manager
              </h1>
              {isAdmin && (
                <div className="flex items-center gap-1 text-xs text-primary font-medium mt-1">
                  <Shield className="h-3 w-3" />
                  <span>Admin</span>
                </div>
              )}
            </div>
          </div>
          <Button
            variant="outline"
            onClick={handleSignOut}
            className="transition-all duration-300 hover:border-destructive hover:text-destructive"
          >
            <LogOut className="h-4 w-4 mr-2" />
            Sign Out
          </Button>
        </div>

        {isAdmin && <UserList />}

        <div className="grid gap-6 md:grid-cols-[350px,1fr]">
          <div>
            <GroupSelector
              userId={session.user.id}
              onSelectGroup={handleSelectGroup}
              selectedGroupId={selectedGroupId || undefined}
            />
          </div>

          <div>
            {selectedGroupId ? (
              <TodoListManager
                groupId={selectedGroupId}
                groupName={selectedGroupName}
                userId={session.user.id}
              />
            ) : (
              <div className="text-center py-16 text-muted-foreground">
                <ListTodo className="h-16 w-16 mx-auto mb-4 opacity-20" />
                <p className="text-lg">Select a group to view its TODO lists</p>
                <p className="text-sm mt-2">
                  Create a new group or select an existing one from the left sidebar
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Index;
