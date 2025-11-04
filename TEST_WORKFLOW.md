# TODO Lists Application - Test Workflow

## Prerequisites

Ensure all services are running:
- Backend: http://localhost:8080
- Frontend: http://localhost:5173
- Supabase: http://127.0.0.1:54323

## Manual Testing Steps

### 1. Sign Up / Sign In

1. Open http://localhost:5173 in your browser
2. You should see the Auth page
3. **Sign up** with a new email (e.g., `test@example.com`) and password (at least 6 characters)
4. Or **sign in** if you already have an account

**Troubleshooting:**
- If you see a blank page, check the browser console (F12)
- If authentication fails, check Supabase is running: `supabase status`

### 2. Verify You're Logged In

After signing in, you should:
- Be redirected to the main page
- See "TODO Lists Manager" at the top
- See "Your Groups" section on the left

**Troubleshooting:**
- If you're redirected back to auth, your session isn't being stored
- Open browser DevTools (F12) → Application → Local Storage
- Check if there's a `supabase.auth.token` entry

### 3. Create a Group

1. Click **"Create Group"** button in the left sidebar
2. Enter a name (e.g., "My First Group")
3. Optional: Add a description
4. Click **"Create Group"**

**Expected Result:** The group appears in the left sidebar

**Troubleshooting:**
- If you get "Failed to create group":
  1. Open browser console (F12) and look for errors
  2. Check if GraphQL requests are being sent
  3. Verify the request includes `Authorization` header

### 4. Create a TODO List

1. Click on your group to select it
2. In the right panel, enter a subject (e.g., "Sprint Tasks")
3. Click **"Create List"**

**Expected Result:** A new TODO list card appears

### 5. Add TODO Items

1. In the TODO list, use the bottom input field
2. Type an item (e.g., "Design database schema")
3. Press Enter or click the **+** button
4. Add 2-3 more items

**Expected Result:** Items appear in the list

### 6. Check Off Items

1. Click the checkbox next to an item
2. The item should show with a strikethrough
3. Click again to uncheck

**Expected Result:** Items toggle between complete and incomplete

### 7. Edit and Delete

1. Click the edit icon (pencil) on the list subject to rename it
2. Click the trash icon on an item to delete it
3. Click the trash icon on the list subject to delete the entire list

## Common Issues and Solutions

### Issue: "Failed to create group" Error

**Symptoms:** Error toast when trying to create a group

**Causes:**
1. Not authenticated (no token)
2. Backend not receiving Authorization header
3. Backend not running

**Solutions:**

#### Solution 1: Check Authentication in Browser Console

```javascript
// Open browser console (F12) and run:
const { data } = await supabase.auth.getSession()
console.log('Session:', data.session)
console.log('User:', data.session?.user)
console.log('Token:', data.session?.access_token)
```

If session is null → You're not logged in. Sign out and sign in again.

#### Solution 2: Test GraphQL Directly with curl

First, get your auth token from browser console:
```javascript
const { data } = await supabase.auth.getSession()
console.log('Token:', data.session.access_token)
console.log('User ID:', data.session.user.id)
```

Then test with curl:
```bash
# Replace YOUR_TOKEN and USER_ID with actual values
curl http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "X-User-Id: USER_ID" \
  -d '{"query":"mutation { createGroup(input: {name: \"Test Group\"}) { id name } }"}'
```

Expected response:
```json
{"data":{"createGroup":{"id":"R3JvdXA6...","name":"Test Group"}}}
```

#### Solution 3: Check Backend Logs

The backend logs show each request. Look for:
- `INFO Application -- Responding at http://0.0.0.0:8080` (backend started)
- Errors with "Authorization header required" (frontend not sending token)

#### Solution 4: Restart Services

```bash
# Stop everything
pkill -f "mise run backend"
pkill -f "npm run dev"

# Start again
mise run dev
```

### Issue: "No groups yet" but you created one

**Cause:** Group was created but query is failing

**Solution:**
1. Refresh the page (F5)
2. Check browser console for errors
3. Check backend logs for errors

### Issue: Backend returns "Authorization header required"

**Cause:** The GraphQL client isn't sending the auth token

**Solution:**

Check the `executeGraphQL` function in `src/lib/graphql.ts`. It should wait for the session with retries. If it's not working:

1. Increase the retry attempts in graphql.ts:
```typescript
const maxAttempts = 20;  // Increase from 10 to 20
```

2. Add more logging:
```typescript
console.log('[GraphQL] Session acquired:', !!session);
console.log('[GraphQL] Access token length:', session?.access_token?.length);
```

## Backend API Testing (Without Frontend)

### Create Test User

```bash
curl http://127.0.0.1:54321/auth/v1/signup \
  -H "Content-Type: application/json" \
  -H "apikey: sb_publishable_ACJWlzQHlZjBrEguHvfOxg_3BJgxAaH" \
  -d '{
    "email": "testuser@example.com",
    "password": "TestPassword123!"
  }'
```

Save the `access_token` and `user.id` from the response.

### Test Group Creation

```bash
# Set your token and user ID
TOKEN="your_access_token_here"
USER_ID="your_user_id_here"

curl http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Id: $USER_ID" \
  -d '{
    "query": "mutation { createGroup(input: {name: \"API Test Group\", description: \"Created via curl\"}) { id name description ownerId } }"
  }'
```

### Test Full Workflow

```bash
# 1. Create Group
GROUP_RESPONSE=$(curl -s http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Id: $USER_ID" \
  -d '{"query":"mutation{createGroup(input:{name:\"Test\"}){id}}"}')

GROUP_ID=$(echo $GROUP_RESPONSE | jq -r '.data.createGroup.id')
echo "Created group: $GROUP_ID"

# 2. Create TODO List
LIST_RESPONSE=$(curl -s http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Id: $USER_ID" \
  -d "{\"query\":\"mutation{createTodoList(input:{groupId:\\\"$GROUP_ID\\\",subject:\\\"My List\\\"}){id}}\"}")

LIST_ID=$(echo $LIST_RESPONSE | jq -r '.data.createTodoList.id')
echo "Created list: $LIST_ID"

# 3. Create TODO Item
curl -s http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Id: $USER_ID" \
  -d "{\"query\":\"mutation{createTodoItem(input:{todoListId:\\\"$LIST_ID\\\",text:\\\"First item\\\"}){id text}}\"}" | jq
```

## Success Criteria

✅ You should be able to:
1. Sign up / Sign in successfully
2. Create a group
3. Create a TODO list in the group
4. Add items to the list
5. Check off items
6. Edit list subjects
7. Delete items and lists

## Getting Help

If none of the above solutions work:

1. Check all services are running:
   ```bash
   mise run status
   ```

2. Check the logs:
   - Backend logs (in the terminal where you ran `mise run backend`)
   - Frontend browser console (F12)
   - Supabase logs (`supabase status`)

3. Try the curl commands above to test the backend directly

4. If the backend works with curl but not from the frontend, the issue is in the frontend authentication
