const express = require('express'); 
const multer = require('multer'); 
const path = require('path'); 
const fs = require('fs'); 
const cors = require('cors'); 
const bcrypt = require('bcryptjs'); 
const app = express(); 
const PORT = process.env.PORT || 8081; // 使用非特权端口  

const usersFilePath = path.join('/data_new/yaochenglin/photo-app/user', 'users.csv');  
// 群组文件存储路径
const groupsFilePath = path.join('/data_new/yaochenglin/photo-app/group', 'groups.json');
// 群组成员文件存储路径
const groupMembersFilePath = path.join('/data_new/yaochenglin/photo-app/group', 'group_members.json');

// 初始化用户文件 
if (!fs.existsSync(usersFilePath)) { 
  fs.writeFileSync(usersFilePath, JSON.stringify([])); 
}  

// 初始化群组文件
if (!fs.existsSync(path.dirname(groupsFilePath))) {
  fs.mkdirSync(path.dirname(groupsFilePath), { recursive: true });
}
if (!fs.existsSync(groupsFilePath)) {
  fs.writeFileSync(groupsFilePath, JSON.stringify([]));
}

// 初始化群组成员文件
if (!fs.existsSync(groupMembersFilePath)) {
  fs.writeFileSync(groupMembersFilePath, JSON.stringify({}));
}

// 用户模型 
class User { 
  constructor(username, email, password) { 
    this.username = username; 
    this.email = email; 
    this.password = password; 
    this.createdAt = new Date(); 
  } 
}  

// 群组模型
class GroupInfo {
  constructor(name, description, creator) {
    this.id = Date.now().toString() + Math.random().toString(36).substr(2, 5); // 生成唯一ID
    this.name = name;
    this.description = description;
    this.createdAt = new Date();
    this.memberCount = 1; // 默认创建者为第一个成员
    this.creator = creator; // 创建者用户名
    this.groupPath = `/groups/${this.id}`; // 群组路径
  }
}

// 读取用户数据 
const readUsers = () => { 
  const data = fs.readFileSync(usersFilePath, 'utf8'); 
  return JSON.parse(data); 
};  

// 写入用户数据 
const writeUsers = (users) => { 
  fs.writeFileSync(usersFilePath, JSON.stringify(users, null, 2)); 
}; 

// 读取群组数据
const readGroups = () => {
  const data = fs.readFileSync(groupsFilePath, 'utf8');
  return JSON.parse(data);
};

// 写入群组数据
const writeGroups = (groups) => {
  fs.writeFileSync(groupsFilePath, JSON.stringify(groups, null, 2));
};

// 读取群组成员数据
const readGroupMembers = () => {
  const data = fs.readFileSync(groupMembersFilePath, 'utf8');
  return JSON.parse(data);
};

// 写入群组成员数据
const writeGroupMembers = (members) => {
  fs.writeFileSync(groupMembersFilePath, JSON.stringify(members, null, 2));
};

// 创建上传目录（如果不存在） 
//const homeDir = process.env.HOME || process.env.USERPROFILE || __dirname; 
//const uploadDir = process.env.UPLOAD_DIR || path.join(homeDir, 'photo-app', 'uploads'); 
const uploadDir = '/data_new/yaochenglin/photo-app/uploads'; 
// 群组照片上传目录
const groupUploadDir = '/data_new/yaochenglin/photo-app/group-uploads';

console.log('上传目录:', uploadDir); 
if (!fs.existsSync(uploadDir)) { 
  fs.mkdirSync(uploadDir, { recursive: true }); 
}  

// 创建群组照片上传目录
console.log('群组照片上传目录:', groupUploadDir);
if (!fs.existsSync(groupUploadDir)) {
  fs.mkdirSync(groupUploadDir, { recursive: true });
}

//test: write permission 
//const testFile = path.join(uploadDir, 'test_write.txt'); 
//try { 
// fs.writeFileSync(testFile, 'test'); 
// fs.unlinkSync(testFile); // 写入成功后删除 
// console.log('有写权限'); 
//} catch (err) { 
// console.error('无写权限:', err.message); 
//}  

// 文件类型过滤 
const fileFilter = (req, file, cb) => { 
  // 只接受图片类型 
  if (file.mimetype.startsWith('image/')) { 
    cb(null, true); 
  } else { 
    cb(new Error('只允许上传图片文件!'), false); 
  } 
};

// 配置文件上传 
const storage = multer.diskStorage({ 
  destination: (req, file, cb) => { 
    cb(null, uploadDir); 
  }, 
  filename: (req, file, cb) => { 
    // 添加一个随机字符串，进一步防止文件名冲突 
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9); 
    const ext = path.extname(file.originalname); 
    cb(null, `${uniqueSuffix}${ext}`); 
  } 
});  

// 配置群组照片上传
const groupPhotoStorage = multer.diskStorage({
  destination: (req, file, cb) => {
    const groupId = req.params.groupId;
    const groupDir = path.join(groupUploadDir, groupId);
    
    // 确保该群组的目录存在
    if (!fs.existsSync(groupDir)) {
      fs.mkdirSync(groupDir, { recursive: true });
    }
    
    cb(null, groupDir);
  },
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    const ext = path.extname(file.originalname);
    cb(null, `${uniqueSuffix}${ext}`);
  }
});

const upload = multer({ 
  storage: storage, 
  fileFilter: fileFilter, 
  limits: { 
    fileSize: 10 * 1024 * 1024, // 10MB 
    files: 1 // 限制单次上传文件数 
  } 
});

// 群组照片上传配置
const groupPhotoUpload = multer({
  storage: groupPhotoStorage,
  fileFilter: fileFilter,
  limits: {
    fileSize: 10 * 1024 * 1024, // 10MB
    files: 1 // 限制单次上传文件数
  }
});

// 中间件 
app.use(cors()); // 跨域支持 
app.use(express.json()); 
app.use('/uploads', express.static(uploadDir)); // 提供静态文件服务  
app.use('/group-uploads', express.static(groupUploadDir)); // 提供群组照片静态文件服务

// ==================== 用户认证API ====================  

/**  
用户注册
POST /user/register */ 
app.post('/user/register', (req, res) => { 
  const { username, email, password } = req.body; 
  console.log('触发注册1111');
  if (!username || !email || !password) { 
    return res.status(400).json({ error: '需要用户名、邮箱和密码' }); 
  }  

  const users = readUsers();  

  // 检查用户名或邮箱是否已存在 
  if (users.some(u => u.username === username)) { 
    return res.status(400).json({ error: '用户名已存在' }); 
  }  

  if (users.some(u => u.email === email)) { 
    return res.status(400).json({ error: '邮箱已存在' }); 
  }  

  // 密码加密 
  const hashedPassword = bcrypt.hashSync(password, 10); 
  const newUser = new User(username, email, hashedPassword);  

  users.push(newUser); 
  writeUsers(users);  

  res.json({ 
    message: '注册成功', 
    success:true, 
    user: { 
      username: newUser.username, 
      email: newUser.email, 
      createdAt: newUser.createdAt 
    } 
  }); 
});  

/**  
用户登录
POST /user/login */ 
app.post('/user/login', (req, res) => { 
  const { username, password } = req.body; 
  console.log('触发登录1111');
  if (!username || !password) { 
    return res.status(400).json({ error: '需要用户名和密码' }); 
  }  

  const users = readUsers(); 
  const user = users.find(u => u.username === username);  

  if (!user || !bcrypt.compareSync(password, user.password)) { 
    return res.status(401).json({ error: '用户名或密码错误' }); 
  }  

  res.json({ 
    message: '登录成功', 
    success:true, 
    user: { 
      username: user.username, 
      email: user.email, 
      createdAt: user.createdAt 
    } 
  }); 
});  

/**  
获取用户信息
GET /user/info */ 
app.get('/user/info', (req, res) => { 
  const { username } = req.query;
  if (!username) { 
    return res.status(400).json({ error: '需要用户名' }); 
  }  

  const users = readUsers(); 
  const user = users.find(u => u.username === username);  

  if (!user) { 
    return res.status(404).json({ error: '用户不存在' }); 
  }  

  res.json({ 
    success:true, 
    user: { 
      username: user.username, 
      email: user.email, 
      createdAt: user.createdAt 
    } 
  }); 
});  

/**  
更新用户信息
PUT /user/info */ 
app.put('/user/info', (req, res) => { 
  const { username, newUsername, newEmail } = req.body;
  if (!username) { 
    return res.status(400).json({ error: '需要当前用户名' }); 
  }  

  const users = readUsers(); 
  const userIndex = users.findIndex(u => u.username === username);  

  if (userIndex === -1) { 
    return res.status(404).json({ error: '用户不存在' }); 
  }  

  const user = users[userIndex];  

  // 更新信息 
  if (newUsername) { 
    // 检查用户名是否已被使用 
    if (users.some(u => u.username === newUsername && u.username !== username)) { 
      return res.status(400).json({ error: '用户名已存在' }); 
    } 
    user.username = newUsername; 
  }  

  if (newEmail) { 
    // 检查邮箱是否已被使用 
    if (users.some(u => u.email === newEmail && u.username !== username)) { 
      return res.status(400).json({ error: '邮箱已存在' }); 
    } 
    user.email = newEmail; 
  }  

  users[userIndex] = user; 
  writeUsers(users);  

  res.json({ 
    message: '更新成功', 
    success:true, 
    updatedUser: { 
      username: user.username, 
      email: user.email, 
      createdAt: user.createdAt 
    } 
  }); 
});

// ==================== 群组API ====================

/**
 * 创建群组
 * POST /groups
 */
app.post('/groups', (req, res) => {
  const { name, description } = req.body;
  const username = req.body.creator || req.headers['x-username']; // 从请求体或头部获取创建者用户名

  if (!name || !description || !username) {
    return res.status(400).json({ error: '需要群组名称、描述和创建者' });
  }

  const users = readUsers();
  const user = users.find(u => u.username === username);

  if (!user) {
    return res.status(404).json({ error: '创建者用户不存在' });
  }

  const groups = readGroups();
  
  // 检查群组名是否已存在
  if (groups.some(g => g.name === name)) {
    return res.status(400).json({ error: '群组名已存在' });
  }

  const newGroup = new GroupInfo(name, description, username);
  groups.push(newGroup);
  writeGroups(groups);

  // 添加创建者到群组成员
  const groupMembers = readGroupMembers();
  groupMembers[newGroup.id] = [username];
  writeGroupMembers(groupMembers);

  res.json(newGroup);
});

/**
 * 获取公开群组
 * GET /groups/public
 */
app.get('/groups/public', (req, res) => {
  const page = parseInt(req.query.page) || 0;
  const size = parseInt(req.query.size) || 10;

  const groups = readGroups();
  
  // 分页处理
  const startIndex = page * size;
  const endIndex = startIndex + size;
  const paginatedGroups = groups.slice(startIndex, endIndex);

  res.json(paginatedGroups);
});

/**
 * 加入群组
 * POST /groups/{groupId}/join
 */
app.post('/groups/:groupId/join', (req, res) => {
  const { groupId } = req.params;
  const { invitationCode } = req.body;
  const username = req.body.username || req.headers['x-username']; // 从请求体或头部获取用户名

  if (!username) {
    return res.status(400).json({ error: '需要用户名' });
  }

  const users = readUsers();
  const user = users.find(u => u.username === username);

  if (!user) {
    return res.status(404).json({ error: '用户不存在' });
  }

  const groups = readGroups();
  const groupIndex = groups.findIndex(g => g.id === groupId);

  if (groupIndex === -1) {
    return res.status(404).json({ error: '群组不存在' });
  }

  const group = groups[groupIndex];
  
  // 检查用户是否已经加入群组
  const groupMembers = readGroupMembers();
  if (!groupMembers[groupId]) {
    groupMembers[groupId] = [];
  }
  
  if (groupMembers[groupId].includes(username)) {
    return res.status(400).json({ 
      success: false, 
      message: '您已经是该群组成员' 
    });
  }

  // 加入群组
  groupMembers[groupId].push(username);
  writeGroupMembers(groupMembers);

  // 更新群组成员数
  group.memberCount = groupMembers[groupId].length;
  groups[groupIndex] = group;
  writeGroups(groups);

  const response = {
    success: true,
    message: '成功加入群组',
    group: group
  };

  res.json(response);
});

/**
 * 退出群组
 * POST /groups/{groupId}/leave
 */
app.post('/groups/:groupId/leave', (req, res) => {
  const { groupId } = req.params;
  const username = req.body.username || req.headers['x-username']; // 从请求体或头部获取用户名

  if (!username) {
    return res.status(400).json({ error: '需要用户名' });
  }

  const groupMembers = readGroupMembers();
  
  if (!groupMembers[groupId] || !groupMembers[groupId].includes(username)) {
    return res.status(400).json({ 
      success: false, 
      message: '您不是该群组成员' 
    });
  }

  // 移除成员
  groupMembers[groupId] = groupMembers[groupId].filter(member => member !== username);
  writeGroupMembers(groupMembers);

  // 更新群组成员数
  const groups = readGroups();
  const groupIndex = groups.findIndex(g => g.id === groupId);
  
  if (groupIndex !== -1) {
    const group = groups[groupIndex];
    group.memberCount = groupMembers[groupId].length;
    
    // 如果没有成员了，可以选择删除该群组
    if (group.memberCount === 0) {
      groups.splice(groupIndex, 1);
      delete groupMembers[groupId];
      writeGroupMembers(groupMembers);
    } else {
      groups[groupIndex] = group;
    }
    
    writeGroups(groups);
    
    const response = {
      success: true,
      message: '成功退出群组'
    };
    
    if (groupIndex !== -1 && group.memberCount > 0) {
      response.group = group;
    }
    
    res.json(response);
  } else {
    res.json({
      success: true,
      message: '成功退出群组'
    });
  }
});

/**
 * 获取用户已加入的群组
 * GET /users/me/groups
 */
app.get('/users/me/groups', (req, res) => {
  const username = req.headers['x-username']; // 从头部获取用户名

  if (!username) {
    return res.status(400).json({ error: '需要用户名' });
  }

  const groupMembers = readGroupMembers();
  const groups = readGroups();
  
  // 查找用户加入的所有群组
  const userGroups = [];
  
  for (const [groupId, members] of Object.entries(groupMembers)) {
    if (members.includes(username)) {
      const group = groups.find(g => g.id === groupId);
      if (group) {
        userGroups.push(group);
      }
    }
  }
  
  res.json(userGroups);
});

/**
 * 上传群组照片
 * POST /groups/{groupId}/photos
 */
app.post('/groups/:groupId/photos', (req, res) => {
  const { groupId } = req.params;
  
  // 检查群组是否存在
  const groups = readGroups();
  const group = groups.find(g => g.id === groupId);
  
  if (!group) {
    return res.status(404).json({ error: '群组不存在' });
  }
  
  // 使用配置的multer中间件处理上传
  groupPhotoUpload.single('photo')(req, res, (err) => {
    if (err) {
      if (err.code === 'LIMIT_FILE_SIZE') {
        return res.status(400).json({ error: '文件大小不能超过10MB' });
      }
      return res.status(400).json({ error: err.message });
    }
    
    if (!req.file) {
      return res.status(400).json({ error: '没有上传文件' });
    }
    
    // 保存成功后返回图片信息
    res.json({
      message: '上传成功',
      filename: req.file.filename,
      path: `/group-uploads/${groupId}/${req.file.filename}`,
      mimetype: req.file.mimetype,
      size: req.file.size
    });
  });
});

/**
 * 获取群组照片列表
 * GET /groups/{groupId}/photos
 */
app.get('/groups/:groupId/photos', (req, res) => {
  const { groupId } = req.params;
  
  // 检查群组是否存在
  const groups = readGroups();
  const group = groups.find(g => g.id === groupId);
  
  if (!group) {
    return res.status(404).json({ error: '群组不存在' });
  }
  
  const groupPhotoDir = path.join(groupUploadDir, groupId);
  
  // 如果群组照片目录不存在，则创建它
  if (!fs.existsSync(groupPhotoDir)) {
    fs.mkdirSync(groupPhotoDir, { recursive: true });
    return res.json([]); // 返回空数组，因为没有照片
  }
  
  try {
    const files = fs.readdirSync(groupPhotoDir);
    
    // 过滤非图片文件
    const photos = files
      .filter(file => {
        const ext = path.extname(file).toLowerCase();
        return ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'].includes(ext);
      })
      .map(file => {
        // 返回照片路径
        return `/group-uploads/${groupId}/${file}`;
      });
    
    res.json(photos);
  } catch (err) {
    console.error('获取群组照片列表错误:', err);
    res.status(500).json({ error: '无法读取群组照片列表' });
  }
});

// 错误处理中间件 
app.use((err, req, res, next) => { 
  console.error(err.stack); 
  res.status(500).json({ error: err.message || '服务器内部错误' }); 
});  

// 图片上传接口 
app.post('/upload', (req, res) => { 
  upload.single('photo')(req, res, (err) => { 
    if (err) { 
      if (err.code === 'LIMIT_FILE_SIZE') { 
        return res.status(400).json({ error: '文件大小不能超过10MB' }); 
      } 
      return res.status(400).json({ error: err.message }); 
    }  

    if (!req.file) {
      return res.status(400).json({ error: '没有上传文件' });
    }

    // 保存成功后返回图片信息
    // 也可以在这里将文件信息保存到数据库
    res.json({
      message: '上传成功',
      filename: req.file.filename,
      path: `/uploads/${req.file.filename}`,
      mimetype: req.file.mimetype,
      size: req.file.size
    });
  }); 
});  

// 获取所有图片列表 
app.get('/photos', (req, res) => { 
  try { 
    const files = fs.readdirSync(uploadDir);  
    
    // 过滤非图片文件，并添加文件信息
    const photos = files
      .filter(file => {
        const ext = path.extname(file).toLowerCase();
        return ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'].includes(ext);
      })
      .map(file => {
        const filePath = path.join(uploadDir, file);
        const stats = fs.statSync(filePath);
        
        return {
          filename: file,
          path: `/uploads/${file}`,
          size: stats.size,
          createdAt: stats.birthtime
        };
      })
      // 按创建时间排序，最新的照片排在前面
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    res.json(photos);
  } catch (err) { 
    console.error('获取照片列表错误:', err); 
    res.status(500).json({ error: '无法读取图片列表' }); 
  } 
});  

// 删除照片 
app.delete('/photos/:filename', (req, res) => { 
  const filename = req.params.filename; 
  const filePath = path.join(uploadDir, filename);  

  // 检查文件是否存在 
  if (!fs.existsSync(filePath)) { 
    return res.status(404).json({ error: '文件不存在' }); 
  }  

  try { 
    fs.unlinkSync(filePath); 
    res.json({ message: '删除成功' }); 
  } catch (err) { 
    console.error('删除文件错误:', err); 
    res.status(500).json({ error: '删除文件失败' }); 
  } 
});  

// 健康检查端点 
app.get('/health', (req, res) => { 
  res.json({ status: 'ok', timestamp: new Date() }); 
});  

// 启动服务器 
app.listen(PORT, '0.0.0.0', () => { 
  console.log(`服务器运行在 http://服务器IP:${PORT}`); 
  console.log(`上传目录: ${uploadDir}`); 
  console.log('服务已启动'); 
});  

// 错误处理 
process.on('uncaughtException', (err) => { 
  console.error('未捕获的异常:', err.stack || err); 
  // 不要立即退出，给日志系统时间写入错误 
  setTimeout(() => { 
    console.log('程序将在记录错误后退出'); 
    process.exit(1); 
  }, 1000); 
});  

module.exports = app; 