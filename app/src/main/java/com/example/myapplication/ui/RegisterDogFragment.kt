package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.DogWalkerApp
import com.example.myapplication.R
import com.example.myapplication.data.Dog
import com.example.myapplication.databinding.FragmentRegisterDogBinding
import java.util.UUID

class RegisterDogFragment : Fragment() {
    private var _binding: FragmentRegisterDogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterDogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dogId = arguments?.getString(ARG_DOG_ID)
        val repo = DogWalkerApp.instance.repo
        val existing = dogId?.let { repo.dogById(it) }
        if (existing != null) {
            binding.title.text = "Editar perro"
            binding.inputName.setText(existing.name)
            binding.inputBreed.setText(existing.breed)
            binding.inputAge.setText(existing.age.toString())
            binding.inputWeight.setText(existing.weightKg.toString())
            binding.inputPersonality.setText(existing.personality)
            binding.checkDisability.isChecked = existing.hasDisability
            binding.inputCondition.setText(existing.condition)
            binding.btnSave.text = "Guardar cambios"
        }
        binding.inputCondition.visible(binding.checkDisability.isChecked)
        binding.checkDisability.setOnCheckedChangeListener { _, checked ->
            binding.inputCondition.visible(checked)
        }
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnSave.setOnClickListener {
            val ownerId = DogWalkerApp.instance.session.currentUserId() ?: return@setOnClickListener
            val name = binding.inputName.text.toString().trim()
            val breed = binding.inputBreed.text.toString().trim()
            val age = binding.inputAge.text.toString().toIntOrNull()
            val weight = binding.inputWeight.text.toString().toIntOrNull()
            if (name.isBlank() || breed.isBlank() || age == null || weight == null) {
                Toast.makeText(requireContext(), "Completa nombre, raza, edad y peso", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val dog = Dog(
                id = existing?.id ?: UUID.randomUUID().toString(),
                ownerId = ownerId,
                name = name,
                breed = breed,
                age = age,
                weightKg = weight,
                personality = binding.inputPersonality.text.toString().trim().ifBlank { "Amigable" },
                hasDisability = binding.checkDisability.isChecked,
                condition = binding.inputCondition.text.toString().trim(),
                avatarColor = existing?.avatarColor ?: colors.random()
            )
            if (existing == null) repo.addDog(dog) else repo.updateDog(dog)
            Toast.makeText(requireContext(), "Perro guardado", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_DOG_ID = "dogId"
        private val colors = listOf("#EAB308", "#A16207", "#22C55E", "#0EA5E9", "#F97316")
        fun args(dogId: String) = bundleOf(ARG_DOG_ID to dogId)
    }
}
