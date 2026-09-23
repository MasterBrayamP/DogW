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
import com.example.myapplication.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private var ownerSelected = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        selectedTab(binding.tabOwner, binding.tabWalker, true)
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.tabOwner.setOnClickListener {
            ownerSelected = true
            selectedTab(binding.tabOwner, binding.tabWalker, true)
        }
        binding.tabWalker.setOnClickListener {
            ownerSelected = false
            selectedTab(binding.tabOwner, binding.tabWalker, false)
        }
        binding.btnRegister.setOnClickListener {
            val name = binding.inputName.text.toString().trim()
            val email = binding.inputEmail.text.toString().trim()
            val pass = binding.inputPassword.text.toString()
            if (name.isBlank() || email.isBlank() || pass.length < 4) {
                Toast.makeText(requireContext(), "Nombre, correo y contraseña (4+)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val repo = DogWalkerApp.instance.repo
            if (repo.userByEmail(email) != null) {
                Toast.makeText(requireContext(), "Ese correo ya está registrado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val role = if (ownerSelected) Role.OWNER else Role.WALKER
            val user = repo.register(name, email, pass, role)
            DogWalkerApp.instance.session.login(user.id)
            val dest = if (role == Role.OWNER) R.id.ownerHomeFragment else R.id.walkerHomeFragment
            findNavController().navigate(
                dest,
                null,
                NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
