package com.example.combobackup

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.combobackup.databinding.MqttRequesterViewBinding

class MqttActivity : FragmentActivity() {

    lateinit var binding : MqttRequesterViewBinding


    override  fun onCreate(savedInstanceState : Bundle?){
        super.onCreate(savedInstanceState)
        binding = MqttRequesterViewBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.connectButton.setOnClickListener {

        }

        binding.sendButton.setOnClickListener{

        }
    }
}