package com.example.paqu_def;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // Nombres de colecciones y campos
    public static final String COLLECTION_USERS = "users";
    public static final String FIELD_USER_INFO = "userInfo";
    public static final String FIELD_SETTINGS = "settings";
    public static final String FIELD_PROGRESS = "progress";

    private FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Configuración para soporte offline (importante para después)
        db.enableNetwork()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase online mode enabled");
                    } else {
                        Log.w(TAG, "Failed to enable online mode", task.getException());
                    }
                });
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    // ===== MÉTODOS PARA USUARIOS =====
    public interface UserDataCallback {
        void onSuccess(Map<String, Object> userData);
        void onError(String errorMessage);
    }
    public interface SaveDataCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    // Crear o actualizar usuario completo
    public void saveUserData(String userId, Map<String, Object> userData, SaveDataCallback callback) {
        db.collection(COLLECTION_USERS).document(userId)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data saved successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user data: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    // Obtener datos completos del usuario
    public void getUserData(String userId, UserDataCallback callback) {
        db.collection(COLLECTION_USERS).document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> userData = document.getData();
                            callback.onSuccess(userData);
                        } else {
                            callback.onError("User document does not exist");
                        }
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    // Actualizar solo la meta diaria
    public void updateDailyGoal(int dailyGoal, SaveDataCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        Map<String, Object> updateData = new HashMap<>();
        updateData.put(FIELD_SETTINGS + ".dailyGoal", dailyGoal);
        updateData.put(FIELD_USER_INFO + ".lastUpdated", new Date());

        db.collection(COLLECTION_USERS).document(user.getUid())
                .update(updateData)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Actualizar progreso diario
    public void updateDailyProgress(int minutesCompleted, SaveDataCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        // Primero obtenemos el progreso actual para sumarlo
        getUserData(user.getUid(), new UserDataCallback() {
            @Override
            public void onSuccess(Map<String, Object> userData) {
                int currentProgress = 0;
                int totalMinutes = 0;

                if (userData.containsKey(FIELD_PROGRESS)) {
                    Map<String, Object> progress = (Map<String, Object>) userData.get(FIELD_PROGRESS);
                    if (progress != null) {
                        currentProgress = progress.containsKey("currentDailyProgress") ?
                                ((Long) progress.get("currentDailyProgress")).intValue() : 0;
                        totalMinutes = progress.containsKey("totalMinutes") ?
                                ((Long) progress.get("totalMinutes")).intValue() : 0;
                    }
                }

                // Actualizar progreso
                Map<String, Object> updateData = new HashMap<>();
                updateData.put(FIELD_PROGRESS + ".currentDailyProgress", currentProgress + minutesCompleted);
                updateData.put(FIELD_PROGRESS + ".totalMinutes", totalMinutes + minutesCompleted);
                updateData.put(FIELD_PROGRESS + ".lastUpdated", new Date());
                updateData.put(FIELD_PROGRESS + ".lastActivityDate", new Date());

                db.collection(COLLECTION_USERS).document(user.getUid())
                        .update(updateData)
                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    // Reiniciar progreso diario
    public void resetDailyProgress(SaveDataCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        Map<String, Object> updateData = new HashMap<>();
        updateData.put(FIELD_PROGRESS + ".currentDailyProgress", 0);
        updateData.put(FIELD_PROGRESS + ".lastUpdated", new Date());

        db.collection(COLLECTION_USERS).document(user.getUid())
                .update(updateData)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ===== OTROS MÉTODOS=====
    public void updateUserAvatar(String avatarCode, SaveDataCallback callback) {
        FirebaseUser user = getCurrentUser();
        if (user == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("userInfo.avatar", avatarCode); // ej: "llama_avatar", "condor_avatar"

        db.collection("users").document(user.getUid())
                .update(updateData)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ===== MÉTODOS PARA AUTENTICACIÓN =====
    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }
    public String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    // ===== MÉTODOS PARA OFFLINE (para implementar después) =====
    public void enablePersistence() {
        // Esto se activará cuando implementemos offline
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    // ===== MÉTODOS ÚTILES PARA CREAR ESTRUCTURAS DE DATOS =====
    public static Map<String, Object> createDefaultUserData(FirebaseUser user, String avatarCode) {
        Map<String, Object> userData = new HashMap<>();

        // userInfo
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("email", user.getEmail());
        userInfo.put("displayName", user.getDisplayName() != null ? user.getDisplayName() : "Usuario Paqu");
        userInfo.put("avatar", avatarCode);
        userInfo.put("createdAt", new Date());
        userInfo.put("lastLogin", new Date());

        // settings (valores por defecto)
        Map<String, Object> settings = new HashMap<>();
        settings.put("dailyGoal", 10);
        settings.put("notifications", true);
        settings.put("soundEffects", true);
        settings.put("language", "es");

        // progress (inicializado en 0)
        Map<String, Object> progress = new HashMap<>();
        progress.put("currentDailyProgress", 0);
        progress.put("lastUpdated", new Date());
        progress.put("totalMinutes", 0);
        progress.put("currentStreak", 0);
        progress.put("longestStreak", 0);
        progress.put("lastActivityDate", new Date());

        userData.put(FIELD_USER_INFO, userInfo);
        userData.put(FIELD_SETTINGS, settings);
        userData.put(FIELD_PROGRESS, progress);

        return userData;
    }
}