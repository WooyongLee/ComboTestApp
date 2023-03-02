package com.example.combobackup.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.Fragment
import com.example.combobackup.R
import com.example.combobackup.databinding.TableViewFragmentBinding
import com.example.combobackup.table.TableItem


class TableViewFragment : Fragment() {

    lateinit var binding : TableViewFragmentBinding

    private var list : ArrayList<TableItem>? = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.table_view_fragment, container, false);

        binding = TableViewFragmentBinding.inflate(inflater, container, false)
        binding.createObjectButton.text = "create object"

        // Test Set Content View
        CreateUIprogrammatically();

        for ( i in 0..9 )
        {
            val valueTV = TextView(context)
            valueTV.setText(i.toString() + " text")
            valueTV.setTextColor(Color.WHITE)
            valueTV.setLayoutParams(
                LinearLayout.LayoutParams(
                    LinearLayout .LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            val valueTV2 = TextView(context)
            valueTV2.setText((i-1).toString() + " text")
            valueTV2.setTextColor(Color.WHITE)
            valueTV2.setLayoutParams(
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            val linearLayout = LinearLayout(context)
            linearLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            linearLayout.id = i
            linearLayout.orientation = LinearLayout.HORIZONTAL
            linearLayout.background = context?.let { ContextCompat.getDrawable(it, R.drawable.table_border) }
            linearLayout.addView(valueTV)
            linearLayout.addView(valueTV2)

            binding.contentLayout.addView(linearLayout)
        }

        return rootView;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        for (i in 0..9) {
            var add = list!!.add(TableItem())

            if ( i == 0) Log.d("TableViewFragment", "onCreate(), list add ")
        }

        //region viewbinding? (주석)
//        val viewBinding : TableViewFragmentBinding
//
//        var recyclerView : RecyclerView = viewBinding.recyclerview
//        viewBinding.recyclerview.layoutManager = LinearLayoutManager(this.context)
//
//        var adapter = TableAdapter()
//        adapter.setList(list)
//        recyclerView.adapter = adapter
//
//        viewBinding.createObjectButton.setOnClickListener{
//            var add = list!!.add(TableItem())
//        }
        //endregion

        // binding = TableViewFragmentBinding.inflate(layoutInflater)
    }

    fun CreateUIprogrammatically() {
        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.FILL_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        //---create a layout---
        val layout = LinearLayout(this.context)
        layout.orientation = LinearLayout.VERTICAL

        //---create a textview---
        val tv = TextView(this.context)
        tv.text = "This is a TextView"
        tv.layoutParams = params

        //---create a button---
        val btn = Button(this.context)
        btn.setText("This is a Button")
        btn.setLayoutParams(params)

        //---adds the textview---
        layout.addView(tv)

        //---adds the button---
        layout.addView(btn)

        //---create a layout param for the layout---
        val layoutParam: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.FILL_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        activity?.setContentView(layout, layoutParam)
    }
}