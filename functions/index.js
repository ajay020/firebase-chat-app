const functions = require("firebase-functions");
const admin = require("firebase-admin");

// Load the service account key JSON file
const serviceAccount = require("./serviceAccountKey.json");

// Initialize the app with a service account, granting admin privileges
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

exports.sendNotification = functions.firestore
    .document("chats/{chatId}/messages/{messageId}")
    .onCreate((snapshot, context) => {
      const messageData = snapshot.data();
      const receiverId = messageData.receiverId;

      // Retrieve the receiver's FCM token from Firestore
      return admin.firestore().collection("users").doc(receiverId).get()
          .then((userDoc) => {
            const fcmToken = userDoc.data().fcmToken;
              const message = {
                  token: fcmToken,
                  notification: {
                    title: "New Message",
                    body: messageData.text,
                  }
                };

                // send notification
                return admin.messaging().send(message);
          })
          .catch((error) => {
            console.error("Error sending notification:", error);
          });
    });
