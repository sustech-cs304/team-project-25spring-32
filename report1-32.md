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
    

  **5 . Share Your Lifestyle** 

Transfer photos offline via Bluetooth/WiFi with zero quality loss. Instantly send vacation snaps to friends at the same party– no mobile data required.

## Non-functional requirements:

e.g., usability, safety, security, performance, etc.

### 1. **Usability**

- **User-Friendly Interface**: The application interface should be simple and intuitive, ensuring users can easily find and use all features, such as search, photo management, editing tools, etc.
- **Responsiveness**: All operations (e.g., search, photo loading, editing) should be completed within a reasonable time to ensure a smooth user experience.
- **Multi-Language Support**: The application should support multiple languages to accommodate global users.
- **User Guidance**: Provide a brief tutorial for new users to help them get started quickly.

### 2. **Security**

- **Privacy Protection**: User photos and videos should be set to private by default, with options to share with specific individuals or publicly.
- **Permission Control**: The application should clearly request necessary permissions (e.g., access to the photo library, location) and provide alternatives if the user denies permissions.

### 3. **Performance**

- **Fast Loading**: Photos and videos should load quickly, ensuring a smooth browsing experience.
- **Efficient Storage**: The application should optimize storage space, supporting photo and video compression to reduce space usage while maintaining high quality.

### 4. **Maintainability**

- **Code Readability**: The development team should follow good coding practices to ensure the code is easy to understand and maintain.
- **Logging**: The application should have detailed logging functionality to facilitate troubleshooting and fixes by the development team.

### 5. **Compatibility**

- **File Format Support**: Support multiple photo and video formats (e.g., JPEG, PNG, MP4) to ensure users can upload and edit various types of files.

## Data requirements:

| **Data Type** | **Purpose** | **Example** | **Source** |
| --- | --- | --- | --- |
| **User-Uploaded Content** |  |  |  |
| - Photo/Video Files | Album management,               AI analysis | JPEG/HEIC/MP4 | Local storage |
| - Metadata (EXIF) | Timeline sorting, geo-tagging | Timestamp, GPS, device model, camera settings | EXIF data extraction |
| - OCR Text Content | Text recognization | Notes from a picture of textbook | OCR Processing |
| **User Behavior&                      AI Genearted Data** |  |  |  |
| - Photo Interaction Metrics | Optimize recommendations( ps. more favourite, priority display) | Liked photos, Sharing history | User actions, AI classification |
| - Album Names/ Photo Tags | Face grouping, Smart albums，Content-based search( learn how the user classify) | Face/ Object/ Scene | User input, AI Processing, AI recommendation |
| - Editing Data | Auto photo-edit/enhance | Brightness, Contrast, Cropping parameters | Edit history, AI Processing |

## Technical requirements:

**Operating Environment：Android**: 8.0+ (API 26+)

**Development Tool**: Android Studio, GitHub

**Technology Stack：**

- **AI Technology**: Image recognition, natural language processing, video generation.
- **Programming language:** java, kotlin
- **Database**: SQLite
- **Development Environment**: Windows 11, Android.