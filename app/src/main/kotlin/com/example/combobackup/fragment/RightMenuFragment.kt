package com.example.combobackup.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.combobackup.R

class RightMenuFragment: Fragment() {
    override fun onCreateView (inflater : LayoutInflater, container: ViewGroup?, savedInstanceState : Bundle?) : View?
    {
        val rootView = inflater.inflate(R.layout.right_menu_fragment, container, false);

        return rootView;
    }
}