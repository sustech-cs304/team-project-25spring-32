const request = require('supertest');
const fs = require('fs');
const path = require('path');
const app = require('./app');

// Define test data
const testUser = {
  username: 'testuser',
  email: 'test@example.com',
  password: 'password123'
};

const testGroup = {
  name: 'TestGroup',
  description: 'This is a test group',
  creator: testUser.username
};

// Setup and teardown
beforeAll(() => {
  // Clean up group data files
  const groupsFilePath = path.join('/data_new/yaochenglin/photo-app/group', 'groups.json');
  const groupMembersFilePath = path.join('/data_new/yaochenglin/photo-app/group', 'group_members.json');
  
  // Create directory if not exists
  if (!fs.existsSync(path.dirname(groupsFilePath))) {
    fs.mkdirSync(path.dirname(groupsFilePath), { recursive: true });
  }
  
  // Reset group data
  fs.writeFileSync(groupsFilePath, JSON.stringify([]));
  fs.writeFileSync(groupMembersFilePath, JSON.stringify({}));
});

// Close all connections after all tests
afterAll(async () => {
  // Note: Using a timeout to allow pending connections to close
  await new Promise(resolve => setTimeout(resolve, 500));
});

describe('Group API Tests', () => {
  let groupId;
  let authHeader;

  // Create test user and authenticate
  beforeAll(async () => {
    try {
      // Register test user
      await request(app)
        .post('/user/register')
        .send(testUser);
      
      console.log('Test user registered or already exists');
    } catch (err) {
      console.log('Error during user registration:', err);
    }

    // Login to get authentication
    try {
      await request(app)
        .post('/user/login')
        .send({
          username: testUser.username,
          password: testUser.password
        });

      // Set auth header for subsequent requests
      authHeader = { 'x-username': testUser.username };
    } catch (err) {
      console.log('Error during login:', err);
    }
  });

  // Test: Create a new group
  test('Create group', async () => {
    const response = await request(app)
      .post('/groups')
      .set(authHeader)
      .send(testGroup);

    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('id');
    expect(response.body.name).toBe(testGroup.name);
    expect(response.body.description).toBe(testGroup.description);
    expect(response.body.creator).toBe(testUser.username);
    expect(response.body.memberCount).toBe(1);

    // Save group ID for subsequent tests
    groupId = response.body.id;
  });

  // Test: Get public groups
  test('Get public groups', async () => {
    const response = await request(app)
      .get('/groups/public')
      .query({ page: 0, size: 10 });

    expect(response.status).toBe(200);
    expect(Array.isArray(response.body)).toBe(true);
    expect(response.body.length).toBeGreaterThanOrEqual(1);
    expect(response.body[0]).toHaveProperty('id');
  });

  // Test: Get user's joined groups
  test('Get user joined groups', async () => {
    const response = await request(app)
      .get('/users/me/groups')
      .set(authHeader);

    expect(response.status).toBe(200);
    expect(Array.isArray(response.body)).toBe(true);
    expect(response.body.length).toBeGreaterThanOrEqual(1);
    
    // Verify the user is in the newly created group
    const foundGroup = response.body.find(group => group.id === groupId);
    expect(foundGroup).toBeTruthy();
  });

  // Test: Second user joining the group
  test('Second user joins group', async () => {
    const secondUser = {
      username: 'testuser2',
      email: 'test2@example.com',
      password: 'password123'
    };

    // Try to register second user
    try {
      await request(app)
        .post('/user/register')
        .send(secondUser);
    } catch (err) {
      console.log('Second user may already exist');
    }

    // Second user joins the group
    const joinResponse = await request(app)
      .post(`/groups/${groupId}/join`)
      .set({ 'x-username': secondUser.username })
      .send({});

    expect(joinResponse.status).toBe(200);
    expect(joinResponse.body.success).toBe(true);
    expect(joinResponse.body.group.memberCount).toBe(2);
  });

  // Test: Upload a photo to the group
  test('Upload group photo', async () => {
    // Create a test image
    const testImagePath = path.join(__dirname, 'test-image.jpg');
    if (!fs.existsSync(testImagePath)) {
      fs.writeFileSync(testImagePath, 'test image content');
    }

    const response = await request(app)
      .post(`/groups/${groupId}/photos`)
      .set(authHeader)
      .attach('photo', testImagePath);

    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('path');
    // Only check for property existence, not specific string value
    expect(response.body).toHaveProperty('message');
  });

  // Test: Get group photos
  test('Get group photos list', async () => {
    const response = await request(app)
      .get(`/groups/${groupId}/photos`)
      .set(authHeader);

    expect(response.status).toBe(200);
    expect(Array.isArray(response.body)).toBe(true);
  });

  // Test: User leaving the group
  test('User leaves group', async () => {
    const leaveResponse = await request(app)
      .post(`/groups/${groupId}/leave`)
      .set({ 'x-username': 'testuser2' })
      .send({});

    expect(leaveResponse.status).toBe(200);
    expect(leaveResponse.body.success).toBe(true);
    expect(leaveResponse.body.group.memberCount).toBe(1);
  });

  // Cleanup: Delete test data
  afterAll(async () => {
    // Creator leaves group, which should delete it
    await request(app)
      .post(`/groups/${groupId}/leave`)
      .set(authHeader)
      .send({});
    
    // Verify group was deleted
    const groupsResponse = await request(app)
      .get('/groups/public')
      .query({ page: 0, size: 10 });
    
    const foundGroup = groupsResponse.body.find(group => group.id === groupId);
    expect(foundGroup).toBeFalsy();
  });
}); 