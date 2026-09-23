package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.myapplication.DogWalkerApp
import com.example.myapplication.R
import com.example.myapplication.data.Role
import com.example.myapplication.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var ownerSelected = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val app = DogWalkerApp.instance
        app.session.currentUserId()?.let { uid ->
            app.repo.userById(uid)?.let {
                goHome(it.role)
                return
            }
        }

        selectedTab(binding.tabOwner, binding.tabWalker, true)
        binding.tabOwner.setOnClickListener {
            ownerSelected = true
            selectedTab(binding.tabOwner, binding.tabWalker, true)
        }
        binding.tabWalker.setOnClickListener {
            ownerSelected = false
            selectedTab(binding.tabOwner, binding.tabWalker, false)
        }
        binding.goRegister.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }
        binding.forgotPassword.setOnClickListener {
            Toast.makeText(requireContext(), "Usa las cuentas demo o regístrate", Toast.LENGTH_SHORT).show()
        }
        binding.btnLogin.setOnClickListener {
            val email = binding.inputEmail.text.toString()
            val pass = binding.inputPassword.text.toString()
            if (email.isBlank() || pass.isBlank()) {
                Toast.makeText(requireContext(), "Completa correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val user = app.repo.userByEmail(email)
            val expected = if (ownerSelected) Role.OWNER else Role.WALKER
            if (user == null || user.password != pass) {
                Toast.makeText(requireContext(), "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (user.role != expected) {
                Toast.makeText(requireContext(), "Elige la pestaña de ${if (user.role == Role.OWNER) "Dueño" else "Paseador"}", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            app.session.login(user.id)
            goHome(user.role)
        }
    }

    private fun goHome(role: Role) {
        val dest = if (role == Role.OWNER) R.id.ownerHomeFragment else R.id.walkerHomeFragment
        findNavController().navigate(
            dest,
            null,
            NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
