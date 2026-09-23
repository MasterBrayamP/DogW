package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.DogWalkerApp
import com.example.myapplication.R
import com.example.myapplication.data.WalkStatus
import com.example.myapplication.databinding.FragmentDogProfileBinding

class DogProfileFragment : Fragment() {
    private var _binding: FragmentDogProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDogProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dogId = arguments?.getString("dogId") ?: return
        val repo = DogWalkerApp.instance.repo
        val dog = repo.dogById(dogId) ?: return
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.avatar.circleAvatar(dog.name, dog.avatarColor)
        binding.txtName.text = dog.name
        binding.txtBreed.text = dog.breed
        binding.txtAge.text = dog.age.toString()
        binding.txtWeight.text = "${dog.weightKg} kg"
        binding.txtPersonality.text = dog.personality
        val walks = repo.walks().count { dogId in it.dogIds && it.status == WalkStatus.COMPLETED }
        binding.txtWalks.text = walks.toString()
        binding.disabilityBox.visible(dog.hasDisability)
        binding.txtCondition.text = dog.condition
        binding.btnRequestWalk.setOnClickListener {
            findNavController().navigate(R.id.requestWalkFragment, bundleOf("dogId" to dog.id))
        }
        binding.btnEdit.setOnClickListener {
            findNavController().navigate(R.id.registerDogFragment, RegisterDogFragment.args(dog.id))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
