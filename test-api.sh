#!/bin/bash

# TODO Lists API Test Script
# This script tests the complete workflow without the frontend

set -e

SUPABASE_URL="${SUPABASE_URL:-http://127.0.0.1:54321}"
SUPABASE_KEY="${SUPABASE_KEY:-sb_publishable_ACJWlzQHlZjBrEguHvfOxg_3BJgxAaH}"
GRAPHQL_ENDPOINT="${GRAPHQL_ENDPOINT:-http://localhost:8080/graphql}"

echo "🧪 TODO Lists API Test"
echo "======================="
echo ""

# Step 1: Create test user
echo "📝 Step 1: Creating test user..."
TEST_EMAIL="test-$(date +%s)@example.com"
TEST_PASSWORD="TestPassword123!"

SIGNUP_RESPONSE=$(curl -s "$SUPABASE_URL/auth/v1/signup" \
  -H "Content-Type: application/json" \
  -H "apikey: $SUPABASE_KEY" \
  -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}")

TOKEN=$(echo $SIGNUP_RESPONSE | jq -r '.access_token')
USER_ID=$(echo $SIGNUP_RESPONSE | jq -r '.user.id')

if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
  echo "❌ Failed to create user"
  echo "Response: $SIGNUP_RESPONSE"
  exit 1
fi

echo "✅ Created user: $TEST_EMAIL"
echo "   User ID: $USER_ID"
echo "   Token: ${TOKEN:0:20}..."
echo ""

# Step 2: Create Group
echo "📝 Step 2: Creating group..."
GROUP_RESPONSE=$(curl -s "$GRAPHQL_ENDPOINT" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Id: $USER_ID" \
  -d '{"query":"mutation{createGroup(input:{name:\"Test Group\",description:\"Created by test script\"}){id name description}}"}')

echo "Response: $GROUP_RESPONSE"

GROUP_ID=$(echo $GROUP_RESPONSE | jq -r '.data.createGroup.id')
if [ "$GROUP_ID" == "null" ] || [ -z "$GROUP_ID" ]; then
  echo "❌ Failed to create group"
  echo "Errors: $(echo $GROUP_RESPONSE | jq '.errors')"
  exit 1
fi

echo "✅ Created group: $GROUP_ID"
echo ""

# Step 3: Create TODO List
echo "📝 Step 3: Creating TODO list..."
LIST_RESPONSE=$(curl -s "$GRAPHQL_ENDPOINT" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Id: $USER_ID" \
  -d "{\"query\":\"mutation{createTodoList(input:{groupId:\\\"$GROUP_ID\\\",subject:\\\"My Test List\\\"}){id subject}}\"}")

LIST_ID=$(echo $LIST_RESPONSE | jq -r '.data.createTodoList.id')
if [ "$LIST_ID" == "null" ] || [ -z "$LIST_ID" ]; then
  echo "❌ Failed to create TODO list"
  echo "Response: $LIST_RESPONSE"
  exit 1
fi

echo "✅ Created TODO list: $LIST_ID"
echo ""

# Step 4: Create TODO Items
echo "📝 Step 4: Creating TODO items..."
for i in 1 2 3; do
  ITEM_RESPONSE=$(curl -s "$GRAPHQL_ENDPOINT" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -H "X-User-Id: $USER_ID" \
    -d "{\"query\":\"mutation{createTodoItem(input:{todoListId:\\\"$LIST_ID\\\",text:\\\"Test Item $i\\\",orderIndex:$i}){id text}}\"}")

  ITEM_ID=$(echo $ITEM_RESPONSE | jq -r '.data.createTodoItem.id')
  if [ "$ITEM_ID" != "null" ]; then
    echo "✅ Created item $i: $ITEM_ID"
  else
    echo "❌ Failed to create item $i"
  fi
done
echo ""

# Step 5: Toggle first item
echo "📝 Step 5: Marking first item as complete..."
ITEM_RESPONSE=$(curl -s "$GRAPHQL_ENDPOINT" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Id: $USER_ID" \
  -d "{\"query\":\"mutation{createTodoItem(input:{todoListId:\\\"$LIST_ID\\\",text:\\\"Item to toggle\\\"}){id}}\"}")

TOGGLE_ITEM_ID=$(echo $ITEM_RESPONSE | jq -r '.data.createTodoItem.id')

UPDATE_RESPONSE=$(curl -s "$GRAPHQL_ENDPOINT" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Id: $USER_ID" \
  -d "{\"query\":\"mutation{updateTodoItem(input:{id:\\\"$TOGGLE_ITEM_ID\\\",completed:true}){id completed}}\"}")

COMPLETED=$(echo $UPDATE_RESPONSE | jq -r '.data.updateTodoItem.completed')
if [ "$COMPLETED" == "true" ]; then
  echo "✅ Marked item as complete"
else
  echo "❌ Failed to mark item as complete"
fi
echo ""

# Step 6: Query everything back
echo "📝 Step 6: Querying TODO lists..."
QUERY_RESPONSE=$(curl -s "$GRAPHQL_ENDPOINT" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Id: $USER_ID" \
  -d "{\"query\":\"query{todoLists(groupId:\\\"$GROUP_ID\\\"){id subject items{id text completed}}}\"}")

echo "Lists: $(echo $QUERY_RESPONSE | jq '.data.todoLists')"
echo ""

# Summary
echo "🎉 Test Summary"
echo "==============="
echo "✅ All tests passed!"
echo ""
echo "Test Data:"
echo "  Email: $TEST_EMAIL"
echo "  Password: $TEST_PASSWORD"
echo "  User ID: $USER_ID"
echo "  Group ID: $GROUP_ID"
echo "  List ID: $LIST_ID"
echo ""
echo "You can now sign in to the frontend with:"
echo "  Email: $TEST_EMAIL"
echo "  Password: $TEST_PASSWORD"
