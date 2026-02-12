package com.example.saveastray

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class QuizActivity : AppCompatActivity() {

    data class Question(
        val text: String,
        val optionA: String,
        val optionB: String
    )

    private val questions = listOf(
        Question("How do you usually spend your weekends?", "Going out & meeting friends", "Staying home & resting"),
        Question("When you get home, you prefer to...", "Play & do something engaging", "Relax quietly & enjoy calm"),
        Question("How do you feel about personal space?", "I like constant company", "I enjoy my alone time"),
        Question("Your living space is usually...", "Lively and a bit noisy", "Quiet and peaceful"),
        Question("How structured is your daily routine?", "Very structured & planned", "Flexible & go-with-the-flow"),
        Question("How do you handle stress?", "I like distractions & activity", "I prefer calm & comfort"),
        Question("What companion do you enjoy more?", "Expressive & interactive", "Calm & quietly supportive"),
        Question("How often are you at home?", "Not very often", "Most of the time"),
        Question("Your ideal environment feels...", "Energetic and fun", "Peaceful and cozy"),
        Question("When something unexpected happens...", "Adapt quickly & move on", "Prefer stability & routine")
    )

    private var currentQuestionIndex = 0
    private var activeScore = 0

    private lateinit var tvProgress: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvQuestion: TextView
    private lateinit var btnOption1: Button
    private lateinit var btnOption2: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        tvProgress = findViewById(R.id.tvProgress)
        progressBar = findViewById(R.id.progressBar)
        tvQuestion = findViewById(R.id.tvQuestion)
        btnOption1 = findViewById(R.id.btnOption1)
        btnOption2 = findViewById(R.id.btnOption2)

        val btnClose = findViewById<ImageButton>(R.id.btnClose)
        btnClose.setOnClickListener { finish() }

        showQuestion()

        btnOption1.setOnClickListener {
            activeScore++
            nextQuestion()
        }

        btnOption2.setOnClickListener {
            nextQuestion()
        }
    }

    private fun showQuestion() {
        if (currentQuestionIndex < questions.size) {
            val q = questions[currentQuestionIndex]

            tvProgress.text = "Question ${currentQuestionIndex + 1}/${questions.size}"
            tvQuestion.text = q.text
            btnOption1.text = q.optionA
            btnOption2.text = q.optionB

            val progressPercentage = ((currentQuestionIndex + 1) * 10)
            progressBar.progress = progressPercentage

        } else {
            showResult()
        }
    }

    private fun nextQuestion() {
        currentQuestionIndex++
        showQuestion()
    }

    private fun showResult() {
        val userIsActive = activeScore >= 5

        tvQuestion.text = "Finding your perfect matches..."

        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        db.collection("cats")
            .whereEqualTo("status", "Available")
            .get()
            .addOnSuccessListener { documents ->
                val matchedCats = ArrayList<Cat>()

                for (document in documents) {
                    val cat = document.toObject(Cat::class.java)
                    cat.id = document.id

                    if (userIsActive) {
                        if (cat.personality_energy == 3 || cat.personality_play == 3) {
                            matchedCats.add(cat)
                        }
                    } else {
                        if (cat.personality_energy <= 2) {
                            matchedCats.add(cat)
                        }
                    }
                }

                val intent = Intent(this, BrowseCatsActivity::class.java)
                if (matchedCats.isNotEmpty()) {
                    intent.putParcelableArrayListExtra("MATCHED_LIST", matchedCats)
                    intent.putExtra("IS_FILTERED", true)
                } else {
                    intent.putExtra("IS_FILTERED", false)
                }
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
            }
    }
}