# MessageFriend

This is a simple messaging app that can be send and receive text based SMS messages. In addition, the user has the ability to create auto reply messages to respond to received messages. 

## User Stories/Objectives

- As a user, I want to be able to send a text message.
- As a user, I want to pick a contact I can message.
- As a user, I want to be notified when I receive a text message.
- As a user, I want to be able to view a list of all messages I have received.
- As a user, I want to be able to *automatically* send a message in reply to one received.
- As a user, I want to customize the automatic reply in the application settings.

## Getting Started

- Clone or fork this repository onto your local machine.
- In Android Studio select open existing project.
- Go to the path where you cloned this project on your local machine.
- Install necessary gradle and other updates if prompted by Android Studio.

## Important Notes
- If run on an emulator messages can only be sent and received from emulators installed on your local machine. 
- MessageFriend uses the native contacts app to allow the user to search for and select contacts. If run on an emulator, contacts will have to be manually entered into the contacts app installed on the emulator in order for them to appear in MessageFriend.
- In order to enable automatic replies click on the settings icon in the action bar and follow the prompts in the settings activity.
- At this time multimedia messages as well as group messages/conversations are not supported by this app. These may be added at a later date.

## License
The contents of this repository are covered under the [Apache License, Version 2.0](messageFriend/LICENSE)
