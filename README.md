# 🐾 DogPal 
**DogPal** is a mobile app designed for dog owners to discover and join dog-related events. It also enables organizers to create and manage events, track attendance, and gain insights into attendance, event performance, and dog demographics.

## 📱 Features

### For Dog Owners (Attendees):
- Create a profile for yourself and your dog(s)
- Add, update, or remove dogs anytime.
- Browse and filter upcoming dog-friendly events
- Join events based on criteria like breed restrictions, date, and category
- View personalized dashboard (Upcoming / Passed / Cancelled)
- Submit event feedback and receive cancellation alerts
- 
**Event Participation**  
- Select which dogs to bring to each event.  
  - The app automatically checks:
    - Breed restrictions (if enabled)
    - Maximum capacity (either for dogs or owners or both)
    - That you are not the organizer of the event

  - Once all checks pass, users can successfully join the event.
  - View other participants in joined events.  
  - Message them via WhatsApp deep link—but only if both users are attending the same event.

### For Organizers:
- Create, update, and cancel events
- Set restrictions (breed, participant limits, etc.)
- View joined attendees and their dogs
- Generate event reports (attendance, demographics, feedback)
- Analyze event performance through feedback stats and dog demographics, and attendance.


## 📊 Reports Module
Organizers can visualize:
- Breed and age group distribution of dogs
- Gender ratios
- Attendance statistics per event
- Categorical event breakdowns
- Identify the top 3 best-performing and the least-performing event
- See the 10 most recent events you've hosted
- Access breed and age group charts for dogs brought to past events


## 🛠️ Tech Stack
- **Frontend:** Android (Java), XML
- **Database & Auth:** Firebase Firestore & Firebase Authentication  
- **Image Storage:** Cloudinary
- **Libraries**: Glide, MPAndroidChart


## 📦 Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/Mobinap/dogpal.git

2. Open the project in Android Studio.
3. Allow Gradle to sync and download all dependencies.
4. Set up your own Firebase project and replace the google-services.json file in the app/ directory.
5. Create a Cloudinary account and provide your credentials in the relevant config file (used for dog image uploads).
6. Connect a physical Android device or start an emulator.
7. Click Run ▶️ in Android Studio to build and launch the app.

## Developed by Mobina ##
