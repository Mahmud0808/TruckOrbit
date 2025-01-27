package com.immon.truckorbit.ui.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.immon.truckorbit.data.Constants.USER_DATABASE
import com.immon.truckorbit.data.LocalDB
import com.immon.truckorbit.data.enums.AccountTypeModel
import com.immon.truckorbit.data.models.UserModel
import com.immon.truckorbit.databinding.FragmentSignInBinding
import com.immon.truckorbit.ui.activities.MainActivity.Companion.replaceFragment
import com.immon.truckorbit.ui.fragments.admin.MainFragment
import com.immon.truckorbit.ui.fragments.base.BaseFragment


class SignInFragment : BaseFragment() {

    private lateinit var binding: FragmentSignInBinding
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override val isLightStatusbar: Boolean
        get() = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)

        binding.btnSignIn.setOnClickListener {
            binding.btnSignIn.startAnimation {
                loginUser()
            }
        }

        binding.txtCreateAccount.setOnClickListener {
            replaceFragment(SignUpFragment(), true)
        }

        return binding.root
    }

    private fun loginUser() {
        val email: String = binding.etEmail.getText().toString().trim()
        val password: String = binding.etPassword.getText().toString().trim()

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Valid email is required"
            binding.etEmail.requestFocus()
            binding.btnSignIn.revertAnimation()
            return
        }

        if (password.isEmpty() || password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            binding.etPassword.requestFocus()
            binding.btnSignIn.revertAnimation()
            return
        }


        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    val firebaseUser = mAuth.currentUser

                    if (firebaseUser != null) {
                        val userId = firebaseUser.uid

                        firestore.collection(USER_DATABASE).document(userId)
                            .get()
                            .addOnCompleteListener { dbTask: Task<DocumentSnapshot?> ->
                                binding.btnSignIn.revertAnimation()

                                if (dbTask.isSuccessful) {
                                    val document = dbTask.result

                                    if (document != null && document.exists()) {
                                        val userModel = document.toObject(UserModel::class.java)

                                        if (userModel != null) {
                                            val accountType = userModel.accountType

                                            when (accountType) {
                                                AccountTypeModel.DRIVER -> {
                                                    LocalDB.putBoolean("is_admin", false)
                                                    Toast.makeText(
                                                        context,
                                                        "Logged in as DRIVER",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }

                                                AccountTypeModel.ADMIN -> {
                                                    LocalDB.putBoolean("is_admin", true)
                                                    replaceFragment(MainFragment())
                                                }
                                            }
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "User data not found",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to fetch user data",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                } else {
                    binding.btnSignIn.revertAnimation()

                    Toast.makeText(
                        context,
                        "Login failed: " + task.exception?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}