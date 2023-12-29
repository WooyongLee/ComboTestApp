package com.example.combobackup.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.fragment.app.Fragment
import com.example.combobackup.R
import com.example.combobackup.databinding.HalfViewFragmentBinding

class HalfViewFragment : Fragment() {

    // 22. 11. 18, Fragment 내 Fragment 사용 시, view Binding은 피하자
    // duplicate inflating class fragment error가 발생
    // lateinit var binding: HalfViewFragmentBinding

    override fun onCreateView (inflater : LayoutInflater, container: ViewGroup?, savedInstanceState : Bundle?) : View?
    {
        // binding = HalfViewFragmentBinding.inflate(inflater, container, false)

        val rootView = inflater.inflate(R.layout.half_view_fragment, container, false);

        rootView!!.setOnTouchListener { v, event ->
            var width = 0
            when (event.getActionMasked()) {
                MotionEvent.ACTION_DOWN -> {}
                MotionEvent.ACTION_MOVE -> {
                    width = (rootView.findViewById(R.id.halfViewMainLayout) as ConstraintLayout).width
                    val guideLine: Guideline = rootView.findViewById(R.id.guideline) as Guideline
                    val params = guideLine.getLayoutParams() as ConstraintLayout.LayoutParams
                    params.guidePercent = event.getRawX() / width

                    guideLine.setLayoutParams(params)
                }
                else -> false
            }
            true
        }

        return rootView;
    }
}