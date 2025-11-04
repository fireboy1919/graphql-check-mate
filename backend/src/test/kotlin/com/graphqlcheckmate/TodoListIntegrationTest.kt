package com.graphqlcheckmate

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TodoListIntegrationTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val supabaseUrl = System.getenv("SUPABASE_URL") ?: "http://127.0.0.1:54321"
    private val supabaseKey = System.getenv("SUPABASE_ANON_KEY") ?: "sb_publishable_ACJWlzQHlZjBrEguHvfOxg_3BJgxAaH"
    private val graphqlEndpoint = "http://localhost:8080/graphql"

    private lateinit var authToken: String
    private lateinit var userId: String
    private lateinit var client: HttpClient

    @Serializable
    data class SignUpRequest(
        val email: String,
        val password: String
    )

    @Serializable
    data class SignUpResponse(
        val access_token: String,
        val user: User
    )

    @Serializable
    data class User(
        val id: String,
        val email: String
    )

    @BeforeAll
    fun setup() = runBlocking {
        client = HttpClient()

        // Create a test user
        val testEmail = "test-${System.currentTimeMillis()}@example.com"
        val testPassword = "TestPassword123!"

        try {
            val response = client.post("$supabaseUrl/auth/v1/signup") {
                contentType(ContentType.Application.Json)
                header("apikey", supabaseKey)
                setBody(json.encodeToString(SignUpRequest.serializer(), SignUpRequest(testEmail, testPassword)))
            }

            val signUpResponse = json.decodeFromString<SignUpResponse>(response.bodyAsText())
            authToken = signUpResponse.access_token
            userId = signUpResponse.user.id

            println("Test user created: $testEmail with ID: $userId")
        } catch (e: Exception) {
            println("Failed to create test user: ${e.message}")
            throw e
        }
    }

    private suspend fun executeGraphQL(query: String, variablesJson: String = "{}"): JsonElement {
        val requestBody = """
            {
                "query": ${Json.encodeToString(query)},
                "variables": $variablesJson
            }
        """.trimIndent()

        val response = client.post(graphqlEndpoint) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $authToken")
            header("X-User-Id", userId)
            setBody(requestBody)
        }

        return json.parseToJsonElement(response.bodyAsText())
    }

    @Test
    fun `test create group`() = runBlocking {
        val query = """
            mutation CreateGroup(${'$'}name: String!, ${'$'}description: String) {
                createGroup(input: {
                    name: ${'$'}name
                    description: ${'$'}description
                }) {
                    id
                    name
                    description
                    ownerId
                }
            }
        """.trimIndent()

        val variables = """
            {
                "name": "Test Group",
                "description": "Test Description"
            }
        """.trimIndent()

        val response = executeGraphQL(query, variables)
        val jsonObject = response.jsonObject

        assertTrue(jsonObject.contains("data"), "Response should have data")
        val data = jsonObject["data"]?.jsonObject
        assertNotNull(data)

        val group = data["createGroup"]?.jsonObject
        assertNotNull(group)
        assertEquals("Test Group", group["name"]?.jsonPrimitive?.content)
        assertEquals(userId, group["ownerId"]?.jsonPrimitive?.content)

        println("✅ Created group: ${group["id"]?.jsonPrimitive?.content}")
    }

    @Test
    fun `test full workflow - create group, create list, add items`() = runBlocking {
        println("Starting full workflow test...")

        // Step 1: Create a group
        val createGroupQuery = """
            mutation CreateGroup(${'$'}name: String!) {
                createGroup(input: { name: ${'$'}name }) {
                    id
                    name
                }
            }
        """.trimIndent()

        val groupResponse = executeGraphQL(createGroupQuery, """{"name": "Workflow Test Group"}""")
        val group = groupResponse.jsonObject["data"]?.jsonObject?.get("createGroup")?.jsonObject
        assertNotNull(group)
        val groupId = group["id"]?.jsonPrimitive?.content
        assertNotNull(groupId)

        println("✅ Step 1: Created group with ID: $groupId")

        // Step 2: Create a TODO list
        val createListQuery = """
            mutation CreateTodoList(${'$'}groupId: ID!, ${'$'}subject: String!) {
                createTodoList(input: {
                    groupId: ${'$'}groupId
                    subject: ${'$'}subject
                }) {
                    id
                    subject
                    groupId
                }
            }
        """.trimIndent()

        val listResponse = executeGraphQL(createListQuery, """
            {
                "groupId": "$groupId",
                "subject": "Test TODO List"
            }
        """.trimIndent())

        val todoList = listResponse.jsonObject["data"]?.jsonObject?.get("createTodoList")?.jsonObject
        assertNotNull(todoList)
        val listId = todoList["id"]?.jsonPrimitive?.content
        assertNotNull(listId)

        println("✅ Step 2: Created TODO list with ID: $listId")

        // Step 3: Add a TODO item
        val createItemQuery = """
            mutation CreateTodoItem(${'$'}todoListId: ID!, ${'$'}text: String!) {
                createTodoItem(input: {
                    todoListId: ${'$'}todoListId
                    text: ${'$'}text
                    orderIndex: 0
                }) {
                    id
                    text
                    completed
                }
            }
        """.trimIndent()

        val itemResponse = executeGraphQL(createItemQuery, """
            {
                "todoListId": "$listId",
                "text": "Test Item 1"
            }
        """.trimIndent())

        val todoItem = itemResponse.jsonObject["data"]?.jsonObject?.get("createTodoItem")?.jsonObject
        assertNotNull(todoItem)
        val itemId = todoItem["id"]?.jsonPrimitive?.content
        assertNotNull(itemId)

        println("✅ Step 3: Created TODO item with ID: $itemId")

        // Step 4: Toggle the item
        val updateItemQuery = """
            mutation UpdateTodoItem(${'$'}id: ID!, ${'$'}completed: Boolean) {
                updateTodoItem(input: {
                    id: ${'$'}id
                    completed: ${'$'}completed
                }) {
                    id
                    completed
                }
            }
        """.trimIndent()

        val updateResponse = executeGraphQL(updateItemQuery, """
            {
                "id": "$itemId",
                "completed": true
            }
        """.trimIndent())

        val updatedItem = updateResponse.jsonObject["data"]?.jsonObject?.get("updateTodoItem")?.jsonObject
        assertNotNull(updatedItem)
        assertEquals(true, updatedItem["completed"]?.jsonPrimitive?.boolean)

        println("✅ Step 4: Toggled item to completed")

        // Step 5: Query the list with items
        val queryListQuery = """
            query GetTodoLists(${'$'}groupId: ID!) {
                todoLists(groupId: ${'$'}groupId) {
                    id
                    subject
                    items {
                        id
                        text
                        completed
                    }
                }
            }
        """.trimIndent()

        val queryResponse = executeGraphQL(queryListQuery, """{"groupId": "$groupId"}""")

        val lists = queryResponse.jsonObject["data"]?.jsonObject?.get("todoLists")?.jsonArray
        assertNotNull(lists)
        assertTrue(lists.isNotEmpty(), "Should have at least one list")

        val fetchedList = lists[0].jsonObject
        val items = fetchedList["items"]?.jsonArray
        assertNotNull(items)
        assertTrue(items.isNotEmpty(), "List should have items")

        val fetchedItem = items[0].jsonObject
        assertEquals("Test Item 1", fetchedItem["text"]?.jsonPrimitive?.content)
        assertEquals(true, fetchedItem["completed"]?.jsonPrimitive?.boolean)

        println("✅ Step 5: Verified list and items")
        println("🎉 Full workflow test passed!")
    }
}
