package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.myapplication.DogWalkerApp
import com.example.myapplication.R
import com.example.myapplication.data.Role
import com.example.myapplication.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val app = DogWalkerApp.instance
        val user = app.repo.userById(app.session.currentUserId().orEmpty()) ?: return
        binding.avatar.circleAvatar(user.name, "#22C55E")
        binding.txtName.text = user.name
        binding.txtRole.text = if (user.role == Role.OWNER) "Dueño" else "Paseador"
        binding.txtEmail.text = user.email
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnLogout.setOnClickListener {
            app.session.logout()
            findNavController().navigate(
                R.id.loginFragment,
                null,
                NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
