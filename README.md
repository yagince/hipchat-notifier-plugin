# HipChatNotifier Plugin

for HipChat API v2

## Configure

### Global Settings

Manage Jenkins -> System Settings -> Hip Chat Notifier (section)

1. API Token
  - your api token
  - see) https://{xxxx}.hipchat.com/account/api

### Job Settings

Job Page -> Configure -> Post-build Action (section) -> Add post-build action

### normal

1. RoomName or RoomID
  - The name or id of the room which to notify
  - Take a look at the Room's web page If you want to use the room id
2. Message Format (SUCCESS)
3. Message Format (FAILED)

### advanced

4. post success notification
  - To set whether to notify if the build is successful
5. success notification to notify:true
  - To set whether to notify parameter of success notification to true
6. post failed notification
  - To set whether to notify if the build is failed
7. failed notification to notify:true
  - To set whether to notify parameter of failed notification to true