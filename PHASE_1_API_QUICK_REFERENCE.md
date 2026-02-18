# Phase 1 API Quick Reference Guide

## Authentication
All endpoints require Bearer token authentication via Spring Security.

```
Authorization: Bearer {token}
```

---

## 1. Contact Tags API

### Create a Tag
```
POST /api/tags
Content-Type: application/json

{
  "name": "VIP Clients",
  "color": "#FF5733",
  "description": "Important business clients"
}

Response:
{
  "success": true,
  "message": "Tag created successfully",
  "data": {
    "id": 1,
    "name": "VIP Clients",
    "color": "#FF5733",
    "description": "Important business clients",
    "createdAt": "2026-02-18T10:30:00",
    "updatedAt": "2026-02-18T10:30:00"
  }
}
```

### Get All User Tags
```
GET /api/tags

Response:
{
  "success": true,
  "message": "Tags retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "VIP Clients",
      "color": "#FF5733",
      "description": "Important business clients"
    },
    {
      "id": 2,
      "name": "Friends",
      "color": "#00B4D8"
    }
  ]
}
```

### Add Tag to Contact
```
POST /api/tags/{tagId}/contacts/{contactId}

Response:
{
  "success": true,
  "message": "Tag added to contact"
}
```

### Remove Tag from Contact
```
DELETE /api/tags/{tagId}/contacts/{contactId}

Response:
{
  "success": true,
  "message": "Tag removed from contact"
}
```

### Get All Contacts with Specific Tag
```
GET /api/tags/{tagId}/contacts?page=0&size=10

Response:
{
  "success": true,
  "message": "Contacts retrieved successfully",
  "data": {
    "content": [
      { ... contact objects ... }
    ],
    "pageable": { ... pagination info ... },
    "totalElements": 25,
    "totalPages": 3
  }
}
```

### Update Tag
```
PUT /api/tags/{tagId}
Content-Type: application/json

{
  "name": "VIP Clients",
  "color": "#FF0000",
  "description": "Updated description"
}

Response:
{
  "success": true,
  "message": "Tag updated successfully"
}
```

### Delete Tag
```
DELETE /api/tags/{tagId}

Response:
{
  "success": true,
  "message": "Tag deleted successfully"
}
```

---

## 2. Advanced Search API

### Perform Advanced Search
```
POST /api/search/advanced
Content-Type: application/json

{
  "name": "John",
  "email": "@company.com",
  "phoneNumber": "555",
  "relationship": "COLLEAGUE",
  "city": "New York",
  "birthdateFrom": "1990-01-01",
  "birthdateTo": "1995-12-31",
  "isFavorite": true,
  "tagIds": [1, 2],
  "websiteUrl": "linkedin",
  "sortBy": "name",
  "sortDirection": "ASC",
  "page": 0,
  "size": 10
}

Response:
{
  "success": true,
  "message": "Search completed successfully",
  "data": {
    "content": [
      { ... contact objects ... }
    ],
    "pageable": { ... },
    "totalElements": 15,
    "totalPages": 2,
    "currentPage": 0,
    "pageSize": 10
  }
}
```

### Search Criteria Reference
| Field | Type | Description | Example |
|-------|------|-------------|---------|
| name | string | Contact name (partial match) | "John" |
| email | string | Email address (partial match) | "@gmail" |
| phoneNumber | string | Phone number (partial match) | "555-1234" |
| relationship | string | Relationship type | "COLLEAGUE", "FRIEND", "FAMILY" |
| city | string | City from address (partial match) | "New York" |
| birthdateFrom | date | Birthdate range start | "1990-01-01" |
| birthdateTo | date | Birthdate range end | "2000-12-31" |
| isFavorite | boolean | Favorite status filter | true/false |
| tagIds | array | List of tag IDs to filter (AND logic) | [1, 2, 3] |
| websiteUrl | string | Website URL (partial match) | "linkedin" |
| sortBy | string | Sort field | "name", "createdAt", "email" |
| sortDirection | string | Sort order | "ASC", "DESC" |
| page | integer | Page number (0-based) | 0 |
| size | integer | Results per page | 10 |

---

## 3. Bulk Operations API

### Perform Bulk Action

```
POST /api/bulk/actions
Content-Type: application/json

{
  "contactIds": [1, 2, 3, 4, 5],
  "actionType": "ADD_TAG",
  "actionData": 10
}

Response:
{
  "success": true,
  "message": "Bulk action completed",
  "data": {
    "totalRequested": 5,
    "processed": 5,
    "failed": 0,
    "timestamp": "2026-02-18T11:00:00",
    "message": "Added tag to 5 contacts"
  }
}
```

### Bulk Action Types

#### 1. Delete Contacts
```json
{
  "contactIds": [1, 2, 3],
  "actionType": "DELETE",
  "actionData": null
}
```

#### 2. Add Tag to Multiple Contacts
```json
{
  "contactIds": [1, 2, 3],
  "actionType": "ADD_TAG",
  "actionData": 5  // tagId
}
```

#### 3. Remove Tag from Multiple Contacts
```json
{
  "contactIds": [1, 2, 3],
  "actionType": "REMOVE_TAG",
  "actionData": 5  // tagId
}
```

#### 4. Mark as Favorite
```json
{
  "contactIds": [1, 2, 3],
  "actionType": "MARK_FAVORITE",
  "actionData": null
}
```

#### 5. Unmark as Favorite
```json
{
  "contactIds": [1, 2, 3],
  "actionType": "UNMARK_FAVORITE",
  "actionData": null
}
```

#### 6. Change Relationship
```json
{
  "contactIds": [1, 2, 3],
  "actionType": "CHANGE_RELATIONSHIP",
  "actionData": "COLLEAGUE"  // COLLEAGUE, FRIEND, FAMILY, PROFESSIONAL, etc.
}
```

---

## Common Use Cases

### Use Case 1: Organization by Department
```
1. Create tags: "Engineering", "Marketing", "Sales", "HR"
2. Add appropriate tags to contacts
3. Filter contacts by specific tag to see team members
```

### Use Case 2: Find Specific Contacts
```
1. Use advanced search with multiple criteria
2. Filter by name, company email, location
3. Sort by name or creation date
4. Paginate through results
```

### Use Case 3: Bulk Email Campaign
```
1. Create "Newsletter" tag
2. Tag interested contacts
3. Use GET /api/tags/{tagId}/contacts to get list
4. Export and send campaign
```

### Use Case 4: Annual Review Bulk Update
```
1. Get all colleagues from a department (advanced search)
2. Bulk mark them as "Reviewed" tag
3. Track activities in contact timeline
```

### Use Case 5: Event Reminder Setup
```
1. For each contact, create birthday in ImportantDate
2. System will send reminders 7 days before
3. Emails sent at 9 AM daily
4. Can be customized per contact
```

---

## Error Handling

### Common Error Responses

#### 400 Bad Request
```json
{
  "success": false,
  "message": "Tag name is required",
  "data": null
}
```

#### 404 Not Found
```json
{
  "success": false,
  "message": "Contact not found",
  "data": null
}
```

#### 500 Internal Server Error
```json
{
  "success": false,
  "message": "An error occurred while processing your request",
  "data": null
}
```

---

## Rate Limiting (Recommended)

While not currently implemented, consider adding for production:

```
Requests per minute by endpoint:
- Tag endpoints: 100 req/min
- Search endpoints: 50 req/min
- Bulk operations: 10 req/min (due to resource intensity)
```

---

## Examples Using cURL

### Create a Tag
```bash
curl -X POST http://localhost:8080/api/tags \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "VIP",
    "color": "#FF5733",
    "description": "VIP Clients"
  }'
```

### Advanced Search
```bash
curl -X POST http://localhost:8080/api/search/advanced \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John",
    "isFavorite": true,
    "page": 0,
    "size": 10
  }'
```

### Bulk Add Tag
```bash
curl -X POST http://localhost:8080/api/bulk/actions \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "contactIds": [1, 2, 3],
    "actionType": "ADD_TAG",
    "actionData": 5
  }'
```

---

## Performance Tips

1. **Use Pagination**: Always use page and size parameters for large result sets
2. **Filter Early**: Use specific search criteria to reduce results before retrieval
3. **Tag Strategically**: Create 5-10 main tags rather than 100+ tags
4. **Batch Operations**: Use bulk endpoints for multiple operations instead of individual requests
5. **Monitor Activity**: Check contact activity timeline periodically for patterns

---

## Testing the APIs

### Using Swagger UI
Navigate to: `http://localhost:8080/swagger-ui.html`

All APIs are documented with:
- Request/response schemas
- Example values
- Try-it-out feature
- Authorization setup

### Using Postman
1. Import API endpoints
2. Add Bearer token to Authorization header
3. Use request templates provided above
4. Test different scenarios

---

## API Docs Reference

For complete API documentation including all parameters, return types, and examples:
- **Swagger URL**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **ReDoc**: `http://localhost:8080/api/docs`

---

## Changelog

### Phase 1 Release (2026-02-18)
- ✅ Contact Tagging System
- ✅ Advanced Search & Filtering  
- ✅ Bulk Operations
- ✅ Activity Logging
- ✅ Important Dates Reminders

---

**Last Updated**: February 18, 2026
**API Version**: 1.0
**Status**: Production Ready
