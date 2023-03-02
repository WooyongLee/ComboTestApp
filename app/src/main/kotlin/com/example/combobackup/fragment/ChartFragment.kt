package com.example.combobackup.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.combobackup.R
import com.example.combobackup.databinding.ChartFragmentBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.renderer.LineChartRenderer

class ChartFragment : Fragment() {
    lateinit var binding : ChartFragmentBinding

    override fun onCreateView (inflater : LayoutInflater, container: ViewGroup?, savedInstanceState : Bundle?) : View?
    {
        val rootView = inflater.inflate(R.layout.chart_fragment, container, false);

        // View Binding 설정
        binding = ChartFragmentBinding.inflate(inflater, container, false)

        binding.mainTextView.text = "View Binding Test Complete!"

        // Chart 설정
        var chart : LineChart = binding.mainLineChart
        var legend = chart.legend;

        legend.setTextColor(Color.BLUE)



        // return rootView;
        // Fragment Layout View 반환
        return binding.root
    }
}