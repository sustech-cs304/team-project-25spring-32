# Software Engineering Proposal

## Functional requirements:

list the features of the proposed system.

**Smart Album Features**

1. **Photo Management by Criteria**
    
    You can organize your photos efficiently by creating themed albums— Albums generate automatically based on time, location, or existing tags, or you can manually create an album and add selected photos. Additionally, you have the flexibility to copy or move photos between albums as needed.
    
2. **Search with Ease**
    
    Search photos instantly by customized tags, dates, locations, or even objects in the image. Save your frequent searches (e.g. "2023 Summer Trip" or "Cat Photos") . Keyword recommendation and word-completion. 
    
3. **Smart Editing Tools**
    
    Support basic tools like rotation, cropping, horizontal and vertical mirror flipping. More specific adjustment like adjusting contrast, brightness, or using filters. Elevate the photos with built-in AI-powered editing: one-tap background removal, instantly fix blurry old pictures with HD restoration.
    
4. **Moment Video**
    
    Get automatic video suggestions like "Best Smiles" or "Golden Hour Shots" based on given theme like an event, trip, or time frame.. You can manually make some adjustments on the selected photos. Automatically generates  personalized memorable videos by adding customized background music and transitions.
    
5. **Sharing & Social**
    
     Users can share photos or albums with friends and family  within the app. Albums can be set to private or public, and collaborative albums can allow multiple users to contribute and view shared photos.
    

## Non-functional requirements:

e.g., usability, safety, security, performance, etc.

### 1. **Usability**

- **User-Friendly Interface**: The application interface should be simple and intuitive, ensuring users can easily find and use all features, such as search, photo management, editing tools, etc.
- **Responsiveness**: All operations (e.g., search, photo loading, editing) should be completed within a reasonable time to ensure a smooth user experience. Search: <1s, photo loading: >1M/s, editing: one operation complete in 1s.
- **Multi-Language Support**: The application should support multiple languages to accommodate global users. Supported languages: English, Chinese.
- **User Guidance**: Provide a brief tutorial for new users to help them get started quickly. Limit reading time to less than 5 minutes.

### 2. **Security**

- **Privacy Protection**: User photos and videos should be set to private by default, with options to share with specific individuals or publicly.
- **Permission Control**: The application should clearly request necessary permissions (e.g., access to the photo library, location) and provide alternatives if the user denies permissions.

### 3. **Performance**

- **Fast Loading**: Photos and videos should load quickly, ensuring a smooth browsing experience.
- **Efficient Storage**: The application should optimize storage space, supporting photo and video compression to reduce space usage while maintaining high quality. The average compression rate should reach more than 50%.

### 4. **Maintainability**

- **Code Readability**: The development team should follow good coding practices to ensure the code is easy to understand and maintain. Key sections must be annotated in detail.
- **Logging**: The application should have detailed logging functionality to facilitate troubleshooting and fixes by the development team. Use the standard logging tools provided with the Android SDK.

### 5. **Compatibility**

- **File Format Support**: Support multiple photo and video formats (e.g., JPEG, PNG, MP4) to ensure users can upload and edit various types of files.

## Data requirements:

### Data Sources

1. User Personal Data
    - Basic user information (name, email, phone number, etc.), user roles (regular user, administrator), and user operation logs.
    - Users provide basic information during registration and login, and the system automatically records user operation logs.
2. User Behavior Data
    - Liked photos, Sharing history
    - User actions Recording
3. Photo Data
    - Storage path, name, format of the photos, shooting time and location, custom photo tags.
    - Specified by users when uploading photos, the system automatically extracts photo data, and the system automatically updates when users modify images.
4. Album Data
    - Album name, creation time, privacy settings of the album (public, private), and the owner of the album.
    - Users manually enter the album name when creating an album, and the system automatically extracts photo data.
5. AI Model
    - AI pre-trained data sources, model parameters for object recognition.
6. AI Generated Data
    - Album Names, Photo Tags
    - By training AI model to learn

### Data Storage Methods

1. Local Storage
    - Prioritize using device local storage ( Android file system)
    - Depends on the remaining space on the user’s device, typically no fixed limit
2. Server Storage
    - Use SQLite database for storage, providing a certain amount of storage space for each user (5GB)

### Data Storage/ Transmission Security

1. User Permission Settings
    - Manage permissions independently based on roles (such as regular users, administrators).
2. Data Encryption
    - Static data is encrypted using algorithms like AES-256, managed independently.

## Technical requirements:

**Operating Environment：Android**: 8.0+ (API 26+)

**Development Tool**: Android Studio, GitHub

**Technology Stack：**

- **AI Technology**: Image recognition, natural language processing(gpt), video generation(OpenCV).
- **Programming language:** java, kotlin
- **Database**: SQLite
- **Development Environment**: Windows 11, Android.