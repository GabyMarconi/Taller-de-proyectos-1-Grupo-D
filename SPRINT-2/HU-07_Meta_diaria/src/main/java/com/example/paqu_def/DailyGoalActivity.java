package com.example.paqu_def;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;

import java.util.Map;

public class DailyGoalActivity extends AppCompatActivity {
    private LottieAnimationView animationView;
    private CardView cardCasual, cardRegular, cardSerious, cardInsane;
    private MaterialButton btnSaveGoal, btnHardReset;
    private ProgressBar progressBarDaily;
    private TextView tvProgressText;
    private TextView tvCasualProgress, tvRegularProgress, tvSeriousProgress, tvInsaneProgress;

    private int selectedGoal = 10; // Meta por defecto: Regular (10 minutos)
    private int currentProgress = 0;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_goal);

        initializeViews();
        setupFirebase();
        setupAnimations();
        setupClickListeners();
        loadUserData(); // ✅ Cambiado de loadUserProgress() a loadUserData()
    }

    private void initializeViews() {
        animationView = findViewById(R.id.animationView);
        cardCasual = findViewById(R.id.cardCasual);
        cardRegular = findViewById(R.id.cardRegular);
        cardSerious = findViewById(R.id.cardSerious);
        cardInsane = findViewById(R.id.cardInsane);
        btnSaveGoal = findViewById(R.id.btnSaveGoal);
        btnHardReset = findViewById(R.id.btnHardReset);
        progressBarDaily = findViewById(R.id.progressBarDaily);
        tvProgressText = findViewById(R.id.tvProgressText);
        tvCasualProgress = findViewById(R.id.tvCasualProgress);
        tvRegularProgress = findViewById(R.id.tvRegularProgress);
        tvSeriousProgress = findViewById(R.id.tvSeriousProgress);
        tvInsaneProgress = findViewById(R.id.tvInsaneProgress);

        // Cargar animación Lottie desde URL
        animationView.setAnimationFromUrl("https://lottie.host/f46927ab-514b-42ac-ad7a-df90a9ddec02/fLr6l4FwAC.lottie");
        animationView.playAnimation();
        animationView.setRepeatCount(ValueAnimator.INFINITE);
    }

    private void setupFirebase() {
        firebaseManager = FirebaseManager.getInstance();
    }

    private void setupAnimations() {
        // Animación de entrada para las tarjetas
        animateCardEntrance(cardCasual, 100);
        animateCardEntrance(cardRegular, 200);
        animateCardEntrance(cardSerious, 300);
        animateCardEntrance(cardInsane, 400);
    }

    private void animateCardEntrance(View view, long delay) {
        view.setAlpha(0f);
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.setTranslationY(50f);

        view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void setupClickListeners() {
        cardCasual.setOnClickListener(v -> selectGoal(5, cardCasual));
        cardRegular.setOnClickListener(v -> selectGoal(10, cardRegular));
        cardSerious.setOnClickListener(v -> selectGoal(15, cardSerious));
        cardInsane.setOnClickListener(v -> selectGoal(20, cardInsane));

        btnSaveGoal.setOnClickListener(v -> saveUserGoal());
        btnHardReset.setOnClickListener(v -> showResetConfirmation());
    }

    private void selectGoal(int minutes, CardView selectedCard) {
        // Resetear todas las tarjetas
        resetCardSelection();

        // Animar la tarjeta seleccionada
        animateCardSelection(selectedCard);

        selectedGoal = minutes;
        updateProgressDisplay();
    }

    private void resetCardSelection() {
        CardView[] cards = {cardCasual, cardRegular, cardSerious, cardInsane};
        for (CardView card : cards) {
            card.animate().scaleX(1f).scaleY(1f).setDuration(300).start();
            card.setCardElevation(4f);
        }
    }

    private void animateCardSelection(CardView card) {
        // Animación de rebote
        card.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(300)
                .setInterpolator(new BounceInterpolator())
                .start();

        card.setCardElevation(12f);

        // Efecto de partículas (simulado con Lottie)
        showConfettiEffect();
    }

    private void showConfettiEffect() {
        // OPCIONAL: Si no tienes el archivo confetti.json, COMENTA o ELIMINA este método
        // Para evitar errores, puedes comentar estas líneas temporalmente:
        /*
        LottieAnimationView confetti = new LottieAnimationView(this);
        try {
            confetti.setAnimation(R.raw.conffeti);
            confetti.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT, 200));
            confetti.playAnimation();

            ((android.widget.FrameLayout) findViewById(android.R.id.content)).addView(confetti);
            confetti.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    ((android.widget.FrameLayout) findViewById(android.R.id.content)).removeView(confetti);
                }
            });
        } catch (Exception e) {
            // Si no existe el archivo, ignorar el error
        }
        */
    }

    private void saveUserGoal() {
        if (!firebaseManager.isUserLoggedIn()) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseManager.updateDailyGoal(selectedGoal, new FirebaseManager.SaveDataCallback() {
            @Override
            public void onSuccess() {
                animateButtonSuccess();
                Toast.makeText(DailyGoalActivity.this, "Meta guardada exitosamente", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(DailyGoalActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData() {
        String userId = firebaseManager.getCurrentUserId();
        if (userId == null) {
            // Usuario no logueado, usar valores por defecto
            updateProgressDisplay();
            return;
        }

        firebaseManager.getUserData(userId, new FirebaseManager.UserDataCallback() {
            @Override
            public void onSuccess(Map<String, Object> userData) {
                // Procesar datos de settings
                if (userData.containsKey(FirebaseManager.FIELD_SETTINGS)) {
                    Map<String, Object> settings = (Map<String, Object>) userData.get(FirebaseManager.FIELD_SETTINGS);
                    if (settings != null && settings.containsKey("dailyGoal")) {
                        selectedGoal = ((Long) settings.get("dailyGoal")).intValue();
                    }
                }

                // Procesar datos de progreso
                if (userData.containsKey(FirebaseManager.FIELD_PROGRESS)) {
                    Map<String, Object> progress = (Map<String, Object>) userData.get(FirebaseManager.FIELD_PROGRESS);
                    if (progress != null && progress.containsKey("currentDailyProgress")) {
                        currentProgress = ((Long) progress.get("currentDailyProgress")).intValue();
                    }
                }

                updateProgressDisplay();
            }

            @Override
            public void onError(String errorMessage) {
                // Si hay error, usar valores por defecto
                Toast.makeText(DailyGoalActivity.this, "Cargando valores por defecto", Toast.LENGTH_SHORT).show();
                updateProgressDisplay();
            }
        });
    }

    private void animateButtonSuccess() {
        btnSaveGoal.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction(() -> btnSaveGoal.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start())
                .start();
    }

    private void showResetConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Reiniciar Progreso")
                .setMessage("¿Estás seguro de que quieres reiniciar tu progreso diario?")
                .setPositiveButton("Sí", (dialog, which) -> hardResetProgress())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void hardResetProgress() {
        if (!firebaseManager.isUserLoggedIn()) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseManager.resetDailyProgress(new FirebaseManager.SaveDataCallback() {
            @Override
            public void onSuccess() {
                currentProgress = 0;
                updateProgressDisplay();
                Toast.makeText(DailyGoalActivity.this, "Progreso reiniciado", Toast.LENGTH_SHORT).show();

                // Animación de reinicio
                progressBarDaily.animate()
                        .scaleX(0.5f)
                        .scaleY(0.5f)
                        .setDuration(200)
                        .withEndAction(() -> progressBarDaily.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(400)
                                .start())
                        .start();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(DailyGoalActivity.this, "Error al reiniciar: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProgressDisplay() {
        // Actualizar texto de progreso
        tvCasualProgress.setText(currentProgress + "/5 minutos");
        tvRegularProgress.setText(currentProgress + "/10 minutos");
        tvSeriousProgress.setText(currentProgress + "/15 minutos");
        tvInsaneProgress.setText(currentProgress + "/20 minutos");

        // Calcular porcentaje
        int percentage = (selectedGoal > 0) ? (currentProgress * 100) / selectedGoal : 0;
        percentage = Math.min(percentage, 100);

        // Animar la barra de progreso
        ValueAnimator animator = ValueAnimator.ofInt(progressBarDaily.getProgress(), percentage);
        animator.setDuration(800);
        animator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            progressBarDaily.setProgress(progress);
            tvProgressText.setText(progress + "% completado");
        });
        animator.start();
    }

    // Método para simular progreso (para testing)
    public void addProgress(int minutes) {
        if (!firebaseManager.isUserLoggedIn()) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseManager.updateDailyProgress(minutes, new FirebaseManager.SaveDataCallback() {
            @Override
            public void onSuccess() {
                currentProgress += minutes;
                currentProgress = Math.min(currentProgress, selectedGoal);
                updateProgressDisplay();
                Toast.makeText(DailyGoalActivity.this, "Progreso agregado: " + minutes + "min", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(DailyGoalActivity.this, "Error al agregar progreso: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animationView != null) {
            animationView.cancelAnimation();
        }
    }
}