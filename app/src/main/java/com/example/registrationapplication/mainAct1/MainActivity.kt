package com.example.registrationapplication.mainAct1

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import com.example.registrationapplication.Model
import com.example.registrationapplication.R
import com.example.registrationapplication.usersActivity.UsersActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rengwuxian.materialedittext.MaterialEditText
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var currentId: String
    }

    lateinit var relativeId: RelativeLayout
    lateinit var auth: FirebaseAuth
    lateinit var users: DatabaseReference
    lateinit var dataBase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSignIn: Button = findViewById(R.id.btn_SignIn_Main_Act)
        val btnRegister: Button = findViewById(R.id.btn_Register_MainAct)

        relativeId = findViewById(R.id.root_element)


        auth = FirebaseAuth.getInstance()
        dataBase = FirebaseDatabase.getInstance()
        users = dataBase.getReference("Users")

        btnRegister.setOnClickListener { showRegisterWindow() }
        btnSignIn.setOnClickListener { showSignWindow() }
    }

    private fun showSignWindow() {
        val emailSignIn: EditText = findViewById(R.id.emailField_AM)
        val passwordSignIn: EditText = findViewById(R.id.passwordField_AM)

        if (emailSignIn.text.isEmpty()) {
            Snackbar.make(relativeId, "Please enter your email address", Snackbar.LENGTH_SHORT)
                .show()
            return
        }
        if (passwordSignIn.text.isEmpty()) {
            Snackbar.make(relativeId, "You didn't enter your password", Snackbar.LENGTH_SHORT)
                .show()
        } else if (passwordSignIn.text.toString().length in 1..5) {
            passwordSignIn.text.append("00000")
        }

        auth.signInWithEmailAndPassword(emailSignIn.text.toString(), passwordSignIn.text.toString())
            .addOnSuccessListener {
                currentId = auth.currentUser!!.uid
                users.child(currentId).get().addOnSuccessListener {
                    val status = it.child("status").value.toString()
                    if (status == "blocked") {
                        Snackbar.make(relativeId, "This user is blocked", Snackbar.LENGTH_SHORT).show()
                    } else switchAct()
                }


            }.addOnFailureListener {
                Snackbar.make(relativeId, "Invalid Email or password", Snackbar.LENGTH_SHORT).show()

            }
    }

    private fun switchAct() {
        val newAct = Intent(this, UsersActivity::class.java)
        startActivity(newAct)
        finish()
    }

    private fun showRegisterWindow() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("User registration")

        val inflater: LayoutInflater = LayoutInflater.from(this)
        val registerWindow: android.view.View? = inflater.inflate(R.layout.register_window, null)

        dialogBuilder.setView(registerWindow)


        val email: MaterialEditText = registerWindow!!.findViewById(R.id.emailField_register_w)
        val name: MaterialEditText = registerWindow.findViewById(R.id.nameField_register_w)
        val password: MaterialEditText = registerWindow.findViewById(R.id.passwordField_register_w)
        val confirmPassword: MaterialEditText =
            registerWindow.findViewById(R.id.confirm_passwordField_register_w)

        dialogBuilder.setNegativeButton(
            getString(R.string.cancel_text)
        ) { dialog, which ->
            dialog!!.dismiss()
        }

        dialogBuilder.setPositiveButton(
            getString(R.string.add_text),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    if (TextUtils.isEmpty(email.text.toString())) {
                        Snackbar.make(relativeId, "Please enter your email", Snackbar.LENGTH_SHORT)
                            .show()

                        return
                    }
                    if (TextUtils.isEmpty(name.text.toString())) {
                        Snackbar.make(relativeId, "Please enter your name", Snackbar.LENGTH_SHORT)
                            .show()

                        return
                    }
                    if (password.text.toString() != confirmPassword.text.toString()) {
                        Snackbar.make(relativeId, "Password mismatch", Snackbar.LENGTH_SHORT).show()
                        return
                    } else if (password.text.toString().length in 1..5) {
                        password.text?.append("00000")
                    }

                    auth.createUserWithEmailAndPassword(
                        email.text.toString(),
                        password.text.toString()
                    )
                        .addOnSuccessListener(object :
                            OnSuccessListener<AuthResult> {
                            override fun onSuccess(p0: AuthResult?) {

                                val user = Model()
                                user.email = email.text.toString()
                                user.name = name.text.toString()

                                user.password = password.text.toString()
                                user.id = auth.currentUser!!.uid.toString();
                                user.status = "not blocked"
                                user.registrationDate = getCurrentDateTime().toString("yyyy/MM/dd")
                                user.lastLogin =
                                    getCurrentDateTime().toString("yyyy/MM/dd HH:mm:ss")

                                FirebaseAuth.getInstance().currentUser?.let {
                                    users.child(it.uid)
                                        .setValue(user)
                                        .addOnSuccessListener(OnSuccessListener<Void> {
                                            Snackbar.make(
                                                relativeId,
                                                "Congratulations, you are registered.",
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                            return@OnSuccessListener
                                        }).addOnFailureListener {
                                            Snackbar.make(relativeId, "ERROR", Snackbar.LENGTH_LONG)
                                                .show()
                                        }
                                }
                            }
                        })
                }
            })

        dialogBuilder.show()

    }

    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }
}